package fmg.swing.mosaic;

import static fmg.core.img.PropertyConst.PROPERTY_MOSAIC_TYPE;
import static fmg.core.img.PropertyConst.PROPERTY_SIZE_FIELD;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.ImageObserver;
import java.util.Collection;
import java.util.HashSet;
import java.util.Objects;

import javax.swing.JPanel;
import javax.swing.Timer;

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
 * 2. Clicks        -> invalidate(modifiedCells) -> redraw modified cells (without background)
 * 3. GUI framefork -> invalidate(clip region)   -> redraw cells in region (with background)
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
                    MosaicJPanelView2.this.draw((Graphics2D)g);
                }

                @Override
                public void setBounds(int x, int y, int width, int height) {
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

    private void draw(Graphics2D g) {
        try {
            draw2(g);
        } finally {
            valid = true;
            drawBk = true;
            modifiedCells.clear();
        }
    }

    private void draw2(Graphics2D g) {
        SizeDouble size = model.getSize();

        if (isControlResizing && (lastImg != null)) {
            // if this control is resized  then use cached image

            Dimension sizeCtrl = getControl().getSize();
            g.setColor(Cast.toColor(model.getBackgroundColor()));
            g.fillRect(0, 0, sizeCtrl.width, sizeCtrl.height);

            // tmp update
            var callback = model.getListener();
            model.setListener(null);
            model.setSize(new SizeDouble(sizeCtrl.width, sizeCtrl.height));

            // calc new values
            SizeDouble newOffset     = model.getMosaicOffset();
            SizeDouble newMosaicSize = model.getSizeMosaic();

            // restore
            model.setSize(size);
            model.setListener(callback);

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
        } else {
            if ((lastImg == null) ||
                (lastImg.getWidth()  != (int)size.width) ||
                (lastImg.getHeight() != (int)size.height))
            {
                lastImg = new BufferedImage((int)size.width, (int)size.height, BufferedImage.TYPE_INT_ARGB);
                modifiedCells.clear(); // redraw all
            }

            var drawContext = new MosaicDrawContext<>(
                model,
                drawBk,
                model::getBackgroundColor,
                modifiedCells.isEmpty()
                    ? model::getMatrix
                    : () -> modifiedCells,
                imgMine::getImage,
                imgFlag::getImage);

            // draw to buffered image (caching image)
            Graphics2D gImg = lastImg.createGraphics();
            gImg.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            gImg.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);

            MosaicImg2.draw(gImg, drawContext);
            g.drawImage(lastImg, 0, 0, null);
            lastImgMosaicSize   = model.getSizeMosaic();
            lastImgMosaicOffset = model.getMosaicOffset();

            gImg.dispose();
        }
    }

    public void onModelChanged(String property) {
        switch (property) {
        case PROPERTY_MOSAIC_TYPE:
        case PROPERTY_SIZE_FIELD:
            lastImg = null;
            break;
        default:
            // none
        }
    }

    @Override
    public void invalidate() {
        valid = false;
        drawBk = true;
        this.modifiedCells.clear(); // all matrix

        if (control != null)
            control.repaint();
    }

    @Override
    public void invalidate(Collection<BaseCell> modifiedCells) {
        Objects.requireNonNull(modifiedCells);
        if (modifiedCells.isEmpty())
            throw new IllegalArgumentException("Required not empty");

        valid = false;
        drawBk = false;
        this.modifiedCells.addAll(modifiedCells);

        if (control != null)
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
