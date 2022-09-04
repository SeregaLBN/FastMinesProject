package fmg.swing.mosaic;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.ImageObserver;
import java.util.Collection;
import java.util.HashSet;
import java.util.Objects;
import java.util.stream.Collectors;

import javax.swing.Icon;
import javax.swing.JPanel;
import javax.swing.Timer;

import fmg.common.geom.RectDouble;
import fmg.common.geom.SizeDouble;
import fmg.core.app.model.MosaicInitData;
import fmg.core.img.MosaicDrawContext;
import fmg.core.mosaic.IMosaicView2;
import fmg.core.mosaic.MosaicHelper;
import fmg.core.mosaic.MosaicModel2;
import fmg.core.mosaic.cells.BaseCell;
import fmg.swing.img.Flag2;
import fmg.swing.img.Logo2;
import fmg.swing.img.MosaicImg2;
import fmg.swing.utils.Cast;


/*
 * 1. Model changed -> invalidate()              -> redraw all
 * 2. Clicks        -> invalidate(modifiedCells) -> redraw modified cells (without bakground)
 * 3. GUI framefork -> invalidate(clip region)   -> redraw cells in region (with bakground)
 */

/** MVC: view. SWING implementation over control {@link JPanel} */
public class MosaicJPanelView2 implements IMosaicView2<JPanel>, AutoCloseable {

    private final MosaicModel2 model;
    private final Flag2.FlagSwingIconController imgFlag;
    private final Logo2.LogoSwingIconController imgMine;
    private JPanel control;
    private final Collection<BaseCell> modifiedCells = new HashSet<>();
    private boolean valid = false;
    private boolean drawBk = true;
    boolean zoomFocusToMosaicField = false; // experimental
    boolean forceSimpleDraw = false;

    private Timer timerDebounceSize;
    private boolean useDebounce = true;
    private boolean isControlResizing;
    private SizeDouble lastSize;

    private BufferedImage lastImg;
    private SizeDouble lastImgMosaicSize;
    private SizeDouble lastImgMosaicOffset;

    public MosaicJPanelView2(
        MosaicModel2 model,
        Flag2.FlagSwingIconController imgFlag,
        Logo2.LogoSwingIconController imgMine)
    {
        this.model = model;
        this.imgFlag = imgFlag;
        this.imgMine = imgMine;

        imgMine.asMine();

        timerDebounceSize = new Timer(300, ev -> {
            timerDebounceSize.stop();
            setSizeFinish(lastSize);
        });
    }

    @Override
    public JPanel getImage() {
        // will return once created window
        return getControl();
    }

    public SizeDouble getSize() {
        return model.getSize();
    }

    public void setSize(SizeDouble size) {
        if (useDebounce) {
            isControlResizing = true;

            lastSize = size;
            if (timerDebounceSize.isRunning())
                timerDebounceSize.restart();
            else
                timerDebounceSize.start();
        } else {
            model.setSize(size);
        }
    }
    private void setSizeFinish(SizeDouble size) {
        isControlResizing = false;
        if (model.getSize().equals(size))
            invalidate();
        else
            model.setSize(size);
    }

    public JPanel getControl() {
        if (control == null)
            control = new JPanel() {
                private static final long serialVersionUID = 1L;

                @Override
                protected void paintComponent(Graphics g) {
                    //super.paintComponent(g);
                    MosaicJPanelView2.this.drawJPanel((Graphics2D)g);
                }

                @Override
                public void setBounds(int x, int y, int width, int height) {
                    //Logger.info("MosaicJPanelView::getControl::JPanel::setBounds: x={0}, y={1}, width={2}, height={3}", x, y, width, height);
                    if ((width > 0) && (height > 0))
                        MosaicJPanelView2.this.setSize(new SizeDouble(width, height));
                    super.setBounds(x, y, width, height);
                }

                @Override
                public Dimension getPreferredSize() {
                    return Cast.toSize(model.getSize());
                }

                @Override
                public Dimension getMinimumSize() {
                    SizeDouble minSize = MosaicHelper.getSize(model.getMosaicType(), MosaicInitData.AREA_MINIMUM, model.getSizeField());
                    Dimension dim = new Dimension();
                    dim.setSize(minSize.width, minSize.height);
                    return dim;
                }

                @Override
                public void setBackground(Color bg) {
                    model.setBackgroundColor(Cast.toColor(bg));
                }

                @Override
                public Color getBackground() {
                    return Cast.toColor(model.getBackgroundColor());
                }

            };
        return control;
    }

    private void drawJPanel(Graphics2D g) {
        try {
            if (valid) {
                // called from GUI framefork? TODO recheck...
                valid = false;
                drawBk = true;
                isControlResizing = false;

                modifiedCells.clear();
                Rectangle clipBounds = g.getClipBounds();
                if (clipBounds == null) {
                    modifiedCells.addAll(model.getMatrix());
                } else {
                    // check to redraw all mosaic cells
                    SizeDouble size = model.getSize();
                    if ((clipBounds.x <= 0) &&
                        (clipBounds.y <= 0) &&
                        (clipBounds.width  >= (int)size.width) &&
                        (clipBounds.height >= (int)size.height))
                    {
                        modifiedCells.addAll(model.getMatrix());
                    } else {
                        SizeDouble offset = model.getMosaicOffset();

                        // redraw only when needed...
                        RectDouble rc = new RectDouble(clipBounds.x, clipBounds.y, clipBounds.width, clipBounds.height);
                        modifiedCells.addAll(
                            model.getMatrix().stream()
                                .filter(cell -> cell.getRcOuter()
                                                    .moveXY(offset.width, offset.height)
                                                    .intersection(rc)) // ...when the cells and update region intersect
                                .collect(Collectors.toList()));
                    }
                }
            }

            MosaicDrawContext<Icon> dc = new MosaicDrawContext<>(
                model,
                drawBk,
                model::getBackgroundColor,
                () -> modifiedCells,
                imgMine::getImage,
                imgFlag::getImage);

            drawJPanel2(g, dc);

        } finally {
            valid = true;
            drawBk = true;
            modifiedCells.clear();
        }
    }

    private void drawJPanel2(Graphics2D g, MosaicDrawContext<Icon> drawContext) {
        if (forceSimpleDraw) {
            // classic simple draw
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            MosaicImg2.draw(g, drawContext);

        } else {
            // smart drawing

            Dimension sizeCtrl = getControl().getSize();
            SizeDouble size       = model.getSize();
            SizeDouble mosaicSize = model.getSizeMosaic();
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

                    MosaicImg2.draw(gImg, drawContext);

                    gImg.dispose();
                } // else // no else!
                {
                    g = (Graphics2D)g.create();

                    double sx = sizeCtrl.width  / sizeField.width;
                    double sy = sizeCtrl.height / sizeField.height;
                    g.scale(sx, sy);

                    g.drawImage(lastImg, (int)-offset.width, (int)-offset.height, null);

                    g.dispose();
                }
            } else {

                g.setColor(Cast.toColor(model.getBackgroundColor()));
                g.fillRect(0, 0, sizeCtrl.width, sizeCtrl.height);

                if (isControlResizing && (lastImg != null)) {
                    // if this control is resized  then use cached image
                    g = (Graphics2D)g.create();


                    // tmp update
                    model.setSize(new SizeDouble(sizeCtrl.width, sizeCtrl.height));

                    // calc new values
                    SizeDouble newOffset     = model.getMosaicOffset();
                    SizeDouble newMosaicSize = model.getSizeMosaic();

                    // restore
                    model.setSize(size);

                    g.drawImage(
                        lastImg,
                        (int) newOffset.width,
                        (int) newOffset.height,
                        (int)(newOffset.width  + newMosaicSize.width),
                        (int)(newOffset.height + newMosaicSize.height),
                        (int) lastImgMosaicOffset.width,
                        (int) lastImgMosaicOffset.height,
                        (int)(lastImgMosaicOffset.width  + lastImgMosaicSize.width),
                        (int)(lastImgMosaicOffset.height + lastImgMosaicSize.height),
                        (ImageObserver)null);
                    g.dispose();
                } else {
                    if ((lastImg == null) || (lastImg.getWidth() != size.width) || (lastImg.getHeight() != size.height))
                        lastImg = new BufferedImage((int)size.width, (int)size.height, BufferedImage.TYPE_INT_ARGB);

                    // draw to buffered image (caching image)
                    Graphics2D gImg = lastImg.createGraphics();
                    gImg.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    gImg.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);

                    MosaicImg2.draw(gImg, drawContext);
                    g.drawImage(lastImg, 0, 0, null);
                    lastImgMosaicSize   = mosaicSize;
                    lastImgMosaicOffset = offset;

                    gImg.dispose();
                }
            }
        }
    }

    public void onModelChanged(String property) {
        switch (property) {
        case MosaicModel2.PROPERTY_MOSAIC_TYPE:
        case MosaicModel2.PROPERTY_SIZE_FIELD:
            lastImg = null;
            break;
        default:
            // none
        }
    }

    @Override
    public void invalidate(Collection<BaseCell> modifiedCells) {
        Objects.requireNonNull(modifiedCells);
        if (modifiedCells.isEmpty())
            throw new IllegalArgumentException("Required not empty");

        valid = false;
        drawBk = false;
        this.modifiedCells.addAll(modifiedCells);

        control.repaint();
    }

    @Override
    public void invalidate() {
        valid = false;
        drawBk = true;
        this.modifiedCells.clear(); // all matrix

        control.repaint();
    }

    @Override
    public boolean isValid() {
        return valid;
    }

    @Override
    public void reset() {
        valid = false;
        drawBk = true;
        this.modifiedCells.clear();
    }

    @Override
    public void close() {
        if ((timerDebounceSize != null) && timerDebounceSize.isRunning())
            timerDebounceSize.stop();
        control = null;
    }

}
