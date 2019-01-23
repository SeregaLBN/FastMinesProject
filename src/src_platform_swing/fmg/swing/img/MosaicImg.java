package fmg.swing.img;

import java.util.Collection;

import fmg.core.img.MosaicAnimatedModel;
import fmg.core.mosaic.MosaicImageController;
import fmg.core.mosaic.cells.BaseCell;
import fmg.swing.mosaic.MosaicSwingView;

/**
 * Representable {@link fmg.core.types.EMosaic} as image
 * <br>
 * base SWING implementation
 *
 * @param <TImage> SWING specific image: {@link java.awt.Image} or {@link javax.swing.Icon}
 */
public abstract class MosaicImg<TImage>
        extends MosaicSwingView<TImage, Void, MosaicAnimatedModel<Void>>
{

    protected boolean _useBackgroundColor = true;

    protected MosaicImg() {
        super(new MosaicAnimatedModel<Void>());
    }

    @Override
    protected void drawBody() {
        //super.drawBody(); // !hide super implementation

        MosaicAnimatedModel<Void> model = getModel();

        _useBackgroundColor = true;
        switch (model.getRotateMode()) {
        case fullMatrix:
            drawModified(model.getMatrix());
            break;
        case someCells:
            // draw static part
            drawModified(model.getNotRotatedCells());

            // draw rotated part
            _useBackgroundColor = false;
            model.getRotatedCells(rotatedCells -> drawModified(rotatedCells));
            break;
        }
    }

    @Override
    public void close() {
        getModel().close();
        super.close();
    }

    /////////////////////////////////////////////////////////////////////////////////////////////////////
    //    custom implementations
    /////////////////////////////////////////////////////////////////////////////////////////////////////

    /** Mosaic image view implementation over {@link javax.swing.Icon} */
    static class Icon extends MosaicImg<javax.swing.Icon> {

        private IconSwing ico = new IconSwing(this);

        @Override
        protected javax.swing.Icon createImage() { return ico.create(); }

        @Override
        protected void drawModified(Collection<BaseCell> modifiedCells) {
            drawSwing(ico.getGraphics(), modifiedCells, _useBackgroundColor);
        }

        @Override
        public void close() {
            ico.close();
            super.close();
            ico = null;
        }

    }

    /** Mosaics image view implementation over {@link java.awt.Image} */
    static class Image extends MosaicImg<java.awt.Image> {

        private ImageAwt img = new ImageAwt(this);

        @Override
        protected java.awt.Image createImage() { return img.create(); }

        @Override
        protected void drawModified(Collection<BaseCell> modifiedCells) {
            img.drawWrapper(g -> drawSwing(g, modifiedCells, _useBackgroundColor));
        }

    }

    /** Mosaic image controller implementation for {@link Icon} */
    public static class ControllerIcon extends MosaicImageController<javax.swing.Icon, MosaicImg.Icon> {

        public ControllerIcon() {
            super(new MosaicImg.Icon());
        }

        @Override
        public void close() {
            getView().close();
            super.close();
        }

    }

    /** Mosaic image controller implementation for {@link Image} */
    public static class ControllerImage extends MosaicImageController<java.awt.Image, MosaicImg.Image> {

        public ControllerImage() {
            super(new MosaicImg.Image());
        }

        @Override
        public void close() {
            getView().close();
            super.close();
        }

    }

}
