package fmg.swing.mosaic;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.ImageObserver;
import java.beans.PropertyChangeEvent;
import java.util.Collection;
import java.util.HashSet;

import javax.swing.Icon;
import javax.swing.JPanel;
import javax.swing.Timer;

import fmg.common.Logger;
import fmg.common.geom.RectDouble;
import fmg.common.geom.SizeDouble;
import fmg.core.app.model.MosaicInitData;
import fmg.core.mosaic.MosaicDrawModel;
import fmg.core.mosaic.MosaicGameModel;
import fmg.core.mosaic.MosaicHelper;
import fmg.core.mosaic.cells.BaseCell;
import fmg.swing.img.Flag;
import fmg.swing.img.Mine;
import fmg.swing.utils.Cast;
import fmg.swing.utils.ImgUtils;

/** MVC: view. SWING implementation over control {@link JPanel} */
public class MosaicJPanelView extends MosaicSwingView<JPanel, Icon, MosaicDrawModel<Icon>> {

    private JPanel _control;
    private Flag.IconController _imgFlag = new Flag.IconController();
    private Mine.IconController _imgMine = new Mine.IconController();
    private final Collection<BaseCell> _modifiedCells = new HashSet<>();

    boolean zoomFocusToMosaicField = false; // experimental
    boolean forceSimpleDraw = false;

    private Timer timerDebunceSize;
    private boolean useDebounce = true;
    private boolean isControlResizing;
    private SizeDouble lastSize;


    private BufferedImage lastImg;
    private SizeDouble lastImgMosaicSize;
    private SizeDouble lastImgMosaicOffset;

    public MosaicJPanelView() {
        super(new MosaicDrawModel<>());
        changeSizeImagesMineFlag();

        timerDebunceSize = new Timer(300, ev -> {
            timerDebunceSize.stop();
            setSizeFinish(lastSize);
        });
    }

    @Override
    protected JPanel createImage() {
        // will return once created window
        return getControl();
    }

    @Override
    public void setSize(SizeDouble size) {
        if (useDebounce) {
            isControlResizing = true;

            lastSize = size;
            if (timerDebunceSize.isRunning())
                timerDebunceSize.restart();
            else
                timerDebunceSize.start();
        } else {
            super.setSize(size);
        }
    }
    private void setSizeFinish(SizeDouble size) {
        isControlResizing = false;
        if (this.getSize().equals(size))
            this.getControl().repaint();
        else
            super.setSize(size);
    }

    public JPanel getControl() {
        if (_control == null)
            _control = new JPanel() {
                private static final long serialVersionUID = 1L;

                @Override
                protected void paintComponent(Graphics g) {
                    //super.paintComponent(g);
                    MosaicJPanelView.this.drawJPanel(g);
                }

                @Override
                public void setBounds(int x, int y, int width, int height) {
                    //Logger.info("MosaicJPanelView::getControl::JPanel::setBounds: x={0}, y={1}, width={2}, height={3}", x, y, width, height);
                    if ((width > 0) && (height > 0))
                        MosaicJPanelView.this.setSize(new SizeDouble(width, height));
                    super.setBounds(x, y, width, height);
                }

                @Override
                public Dimension getPreferredSize() {
                    return Cast.toSize(getModel().getSize());
                }

                @Override
                public Dimension getMinimumSize() {
                    MosaicGameModel m = getModel();
                    SizeDouble minSize = MosaicHelper.getSize(m.getMosaicType(), MosaicInitData.AREA_MINIMUM, m.getSizeField());
                    Dimension dim = new Dimension();
                    dim.setSize(minSize.width, minSize.height);
                    return dim;
                }

                @Override
                public void setBackground(Color bg) {
                    getModel().setBackgroundColor(Cast.toColor(bg));
                }

                @Override
                public Color getBackground() {
                    return Cast.toColor(getModel().getBackgroundColor());
                }

            };
        return _control;
    }

    @Override
    protected void drawModified(Collection<BaseCell> modifiedCells) {
        JPanel control = getControl();

        assert !_alreadyPainted;

        if (modifiedCells == null) { // mark NULL if all mosaic is changed
            _modifiedCells.clear();
            control.repaint();
        } else {
            _modifiedCells.addAll(modifiedCells);

            double minX=0, minY=0, maxX=0, maxY=0;
            boolean first = true;
            for (BaseCell cell : modifiedCells) {
                RectDouble rc = cell.getRcOuter();
                if (first) {
                    first = false;
                    minX = rc.x;
                    minY = rc.y;
                    maxX = rc.right();
                    maxY = rc.bottom();
                } else {
                    minX = Math.min(minX, rc.x);
                    minY = Math.min(minY, rc.y);
                    maxX = Math.max(maxX, rc.right());
                    maxY = Math.max(maxY, rc.bottom());
                }
            }
            if (_DEBUG_DRAW_FLOW)
                Logger.info("MosaicViewSwing.draw: repaint={" + (int)minX +","+ (int)minY +","+ (int)(maxX-minX) +","+ (int)(maxY-minY) + "}");

            MosaicDrawModel<?> model = getModel();
            SizeDouble offset = model.getMosaicOffset();
            control.repaint((int)(minX + offset.width), (int)(minY + offset.height), (int)(maxX-minX), (int)(maxY-minY));
        }
    }

    private void drawJPanel(Graphics g) {
        MosaicDrawModel<?> model = getModel();

        java.util.function.Consumer<Graphics2D> draw = gg -> {
            Rectangle clipBounds = g.getClipBounds();
            MosaicJPanelView.this.drawSwing(gg,
                                            (clipBounds==null)
                                                ? null
                                                : toDrawCells(Cast.toRectDouble(clipBounds)),
                                            true/*_modifiedCells.isEmpty() || (_modifiedCells.size() == model.getMatrix().size())*/);
            _modifiedCells.clear();
        };

        Graphics2D g2d = (Graphics2D)g;
        if (forceSimpleDraw) {
            // classic simple draw
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            draw.accept(g2d);

        } else {
            // smart drawing

            Dimension sizeCtrl = getControl().getSize();
            SizeDouble size       = model.getSize();
            SizeDouble mosaicSize = model.getMosaicSize();
            SizeDouble offset     = model.getMosaicOffset();

            if (zoomFocusToMosaicField) {
                Dimension sizeField = new Dimension();
                sizeField.setSize(mosaicSize.width, mosaicSize.height);

                Dimension sizeImage = new Dimension();
                sizeImage.setSize(mosaicSize.width + offset.width, mosaicSize.height + offset.height); // exclude left and bottom offsets

                if ((lastImg == null) || (lastImg.getWidth() != sizeImage.width || lastImg.getHeight() != sizeImage.height))
                    lastImg = new BufferedImage(sizeImage.width, sizeImage.height, BufferedImage.TYPE_INT_ARGB);


                if (!isControlResizing)
                { // draw to buffered image
                    Graphics2D gImg = lastImg.createGraphics();
                    gImg.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    gImg.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);

                    draw.accept(gImg);

                    gImg.dispose();
                } // else // no else!
                {
                    g2d = (Graphics2D)g2d.create();

                    double sx = sizeCtrl.width  / sizeField.width;
                    double sy = sizeCtrl.height / sizeField.height;
                    g2d.scale(sx, sy);

                    g2d.drawImage(lastImg, (int)-offset.width, (int)-offset.height, null);

                    g2d.dispose();
                }
            } else {

                g.setColor(Cast.toColor(model.getBackgroundColor()));
                g.fillRect(0, 0, sizeCtrl.width, sizeCtrl.height);

                if (isControlResizing && (lastImg != null)) {
                    // if this control is resized  then use cached image
                    g2d = (Graphics2D)g2d.create();


                    // tmp update
                    model.setSize(new SizeDouble(sizeCtrl.width, sizeCtrl.height));

                    // calc new values
                    SizeDouble newOffset     = model.getMosaicOffset();
                    SizeDouble newMosaicSize = model.getMosaicSize();

                    // restore
                    model.setSize(size);

//                    Logger.info("sizeCtrl={0}", sizeCtrl);
//                    Logger.info("newOffset={0}, newMosaicSize={1}", newOffset, newMosaicSize);
//                    Logger.info("lastImgMosaicSize={0}, lastImgMosaicSize={1}", lastImgMosaicSize, lastImgMosaicSize);

                    g2d.drawImage(lastImg,
                                  (int) newOffset.width,
                                  (int) newOffset.height,
                                  (int)(newOffset.width  + newMosaicSize.width),
                                  (int)(newOffset.height + newMosaicSize.height),
                                  (int) lastImgMosaicOffset.width,
                                  (int) lastImgMosaicOffset.height,
                                  (int)(lastImgMosaicOffset.width  + lastImgMosaicSize.width),
                                  (int)(lastImgMosaicOffset.height + lastImgMosaicSize.height),
                                  (ImageObserver)null);
                    g2d.dispose();
                } else {
                    if ((lastImg == null) || (lastImg.getWidth() != size.width) || (lastImg.getHeight() != size.height))
                        lastImg = new BufferedImage((int)size.width, (int)size.height, BufferedImage.TYPE_INT_ARGB);

                    // draw to buffered image (caching image)
                    Graphics2D gImg = lastImg.createGraphics();
                    gImg.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    gImg.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);

                    draw.accept(gImg);
                    g2d.drawImage(lastImg, 0,0, null);
                    lastImgMosaicSize   = mosaicSize;
                    lastImgMosaicOffset = offset;

                    gImg.dispose();
                }
            }
        }
    }

    @Override
    protected void onPropertyChanged(PropertyChangeEvent ev) {
        super.onPropertyChanged(ev);
        if (PROPERTY_IMAGE.equals(ev.getPropertyName()))
            getImage(); // implicit call draw() -> drawBegin() -> drawModified() -> control.repaint() -> JPanel.paintComponent -> drawSwing()
    }

    @Override
    protected void onModelPropertyChanged(PropertyChangeEvent ev) {
        super.onModelPropertyChanged(ev);
        switch (ev.getPropertyName()) {
        case MosaicGameModel.PROPERTY_MOSAIC_TYPE:
        case MosaicGameModel.PROPERTY_AREA:
            changeSizeImagesMineFlag();
            break;
        default:
            // none
        }
        super.onModelPropertyChanged(ev);
        switch (ev.getPropertyName()) {
        case MosaicGameModel.PROPERTY_MOSAIC_TYPE:
        case MosaicGameModel.PROPERTY_SIZE_FIELD:
            lastImg = null;
            break;
        default:
            // none
        }
    }

    /** переустанавливаю заного размер мины/флага для мозаики */
    protected void changeSizeImagesMineFlag() {
        MosaicDrawModel<Icon> model = getModel();
        double sq = model.getCellAttr().getSq(model.getPenBorder().getWidth());
        if (sq <= 0) {
            Logger.error("Error: too thick pen! There is no area for displaying the flag/mine image...");
            sq = 3; // ат балды...
        }

        final int max = 30;
        if (sq > max) {
            _imgFlag.getModel().setSize(new SizeDouble(sq, sq));
            _imgMine.getModel().setSize(new SizeDouble(sq, sq));
            model.setImgFlag(_imgFlag.getImage());
            model.setImgMine(_imgMine.getImage());
        } else {
            _imgFlag.getModel().setSize(new SizeDouble(max, max));
            _imgMine.getModel().setSize(new SizeDouble(max, max));
            int iSq = (int)sq;
            if (iSq < 1)
                return;
            model.setImgFlag(ImgUtils.zoom(_imgFlag.getImage(), iSq, iSq));
            model.setImgMine(ImgUtils.zoom(_imgMine.getImage(), iSq, iSq));
        }
    }

    @Override
    public void close() {
        if ((timerDebunceSize != null) && timerDebunceSize.isRunning())
            timerDebunceSize.stop();
        super.close();
        getModel().close();
        _control = null;
        _imgFlag.close();
        _imgMine.close();
    }

}
