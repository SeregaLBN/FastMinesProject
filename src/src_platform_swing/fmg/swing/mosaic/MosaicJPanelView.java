package fmg.swing.mosaic;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.beans.PropertyChangeEvent;
import java.util.Collection;
import java.util.HashSet;
import java.util.concurrent.TimeUnit;

import javax.swing.Icon;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import fmg.common.geom.RectDouble;
import fmg.common.geom.SizeDouble;
import fmg.core.mosaic.MosaicDrawModel;
import fmg.core.mosaic.MosaicGameModel;
import fmg.core.mosaic.MosaicHelper;
import fmg.core.mosaic.MosaicInitData;
import fmg.core.mosaic.cells.BaseCell;
import fmg.swing.img.Flag;
import fmg.swing.img.Mine;
import fmg.swing.utils.Cast;
import fmg.swing.utils.ImgUtils;
import io.reactivex.disposables.Disposable;
import io.reactivex.subjects.PublishSubject;
import io.reactivex.subjects.Subject;

/** MVC: view. SWING implementation over control {@link JPanel} */
public class MosaicJPanelView extends MosaicSwingView<JPanel, Icon, MosaicDrawModel<Icon>> {

    private JPanel _control;
    private Flag.ControllerIcon _imgFlag = new Flag.ControllerIcon();
    private Mine.ControllerIcon _imgMine = new Mine.ControllerIcon();
    private final Collection<BaseCell> _modifiedCells = new HashSet<>();
    boolean zoomFocusToMosaicField = !true;
    private boolean useDebounce = true;
    private final Subject<SizeDouble> sizeObserv;
    private final Disposable sizeDebounce;


    public MosaicJPanelView() {
        super(new MosaicDrawModel<Icon>());
        changeSizeImagesMineFlag();

        sizeObserv   = !useDebounce ? null : PublishSubject.create();
        sizeDebounce = !useDebounce ? null : sizeObserv
                .debounce(300, TimeUnit.MILLISECONDS)
                .subscribe(size -> SwingUtilities.invokeLater(() -> this.setSize(size)));
    }

    @Override
    protected JPanel createImage() {
        // will return once created window
        return getControl();
    }

    public JPanel getControl() {
        if (_control == null)
            _control = new JPanel() {
                private static final long serialVersionUID = 1L;

                boolean forceSimpleDraw = false;
                BufferedImage lastImg;

                @Override
                protected void paintComponent(Graphics g) {
                    //super.paintComponent(g);

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

                        Dimension sizeCtrl = this.getSize();

                        SizeDouble sd = model.getSize();
                        Dimension sizeModel = new Dimension();
                        sizeModel.setSize(sd.width, sd.height);

                        if (zoomFocusToMosaicField) {
                            SizeDouble sm = model.getMosaicSize();
                            Dimension sizeField = new Dimension();
                            sizeField.setSize(sm.width, sm.height);

                            SizeDouble offset = model.getMosaicOffset();

                            Dimension sizeImage = new Dimension();
                            sizeImage.setSize(sm.width + offset.width, sm.height + offset.height); // exclude left and bottom offsets

                            if ((lastImg == null) || (lastImg.getWidth() != sizeImage.width || lastImg.getHeight() != sizeImage.height))
                                lastImg = new BufferedImage(sizeImage.width, sizeImage.height, BufferedImage.TYPE_INT_ARGB);

                            boolean isControlResized = !sizeModel.equals(sizeCtrl);

                            if (!isControlResized)
                            { // draw to buffered image
                                Graphics2D gImg = lastImg.createGraphics();
                                gImg.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                                gImg.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);

                                draw.accept(gImg);
                                g2d.drawImage(lastImg, (int)-offset.width, (int)-offset.height, null);

                                gImg.dispose();
                            } // else // no else!
                            {
                                g2d = (Graphics2D)g2d.create();

                                double sx = sizeCtrl.getWidth()  / sizeField.width;
                                double sy = sizeCtrl.getHeight() / sizeField.height;
                                g2d.scale(sx, sy);

                                g2d.drawImage(lastImg, (int)-offset.width, (int)-offset.height, null);

                                g2d.dispose();
                            }
                        } else {
                            boolean isControlResized = !sizeModel.equals(sizeCtrl);

//                            g.setColor(java.awt.Color.GREEN);
//                            g.fillRect(0, 0, sizeCtrl.width, sizeCtrl.height);

                            if (isControlResized) {
                                // if this control is resized  then use cached image
                                g2d = (Graphics2D)g2d.create();

                                double sx = sizeCtrl.getWidth()  / sizeModel.width;
                                double sy = sizeCtrl.getHeight() / sizeModel.height;
                                g2d.scale(sx, sy);

                                g2d.drawImage(lastImg, 0,0, null);

                                g2d.dispose();
                            } else {
                                if ((lastImg == null) || (lastImg.getWidth() != sizeModel.width || lastImg.getHeight() != sizeModel.height))
                                    lastImg = new BufferedImage(sizeModel.width, sizeModel.height, BufferedImage.TYPE_INT_ARGB);

                                // draw to buffered image (caching image)
                                Graphics2D gImg = lastImg.createGraphics();
                                gImg.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                                gImg.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);

                                draw.accept(gImg);
                                g2d.drawImage(lastImg, 0,0, null);

                                gImg.dispose();
                            }
                        }
                    }
                }

                @Override
                public void setBounds(int x, int y, int width, int height) {
                    if ((width > 0) && (height > 0)) {
                        SizeDouble size = new SizeDouble(width, height);
                        if (useDebounce)
                            sizeObserv.onNext(size);
                        else
                            getModel().setSize(size);
                    }
                    super.setBounds(x, y, width, height);
                }

                @Override
                public Dimension getPreferredSize() {
                    SizeDouble size = getModel().getSize();
                    return Cast.toSize(size);
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
                System.out.println("MosaicViewSwing.draw: repaint={" + (int)minX +","+ (int)minY +","+ (int)(maxX-minX) +","+ (int)(maxY-minY) + "}");

            MosaicDrawModel<?> model = getModel();
            SizeDouble offset = model.getMosaicOffset();
            control.repaint((int)(minX + offset.width), (int)(minY + offset.height), (int)(maxX-minX), (int)(maxY-minY));
        }
        //control.invalidate();
    }

    @Override
    protected void onPropertyChanged(PropertyChangeEvent ev) {
        super.onPropertyChanged(ev);
        if (PROPERTY_IMAGE.equals(ev.getPropertyName()))
            getImage(); // implicit call draw() -> drawBegin() -> drawModified() -> control.repaint() -> JPanel.paintComponent -> drawSwing()
    }

    @Override
    protected void onPropertyModelChanged(PropertyChangeEvent ev) {
        super.onPropertyModelChanged(ev);
        switch (ev.getPropertyName()) {
        case MosaicGameModel.PROPERTY_MOSAIC_TYPE:
        case MosaicGameModel.PROPERTY_AREA:
            changeSizeImagesMineFlag();
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
            System.err.println("Error: too thick pen! There is no area for displaying the flag/mine image...");
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
        if (sizeDebounce != null)
            sizeDebounce.dispose();
        getModel().close();
        super.close();
        _control = null;
        _imgFlag.close();
        _imgMine.close();
    }

}
