package fmg.swing.img;

import java.util.Collection;

import fmg.core.img.MosaicAnimatedModel;
import fmg.core.mosaic.MosaicImageController;
import fmg.core.mosaic.cells.BaseCell;
import fmg.swing.mosaic.MosaicSwingView;

/** Representable {@link fmg.core.types.EMosaic} as image */
public final class MosaicImg {
    private MosaicImg() {}

    /**
     * Representable {@link fmg.core.types.EMosaic} as image
     * <br>
     * Base image view SWING implementation
     *
     * @param <TImage> SWING specific image: {@link java.awt.Image} or {@link javax.swing.Icon}
     */
    public abstract static class SwingView<TImage>
                   extends MosaicSwingView<TImage, Void, MosaicAnimatedModel<Void>>
    {

        protected boolean _useBackgroundColor = true;

        protected SwingView() {
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
            super.close();
            getModel().close();
        }

    }

    /////////////////////////////////////////////////////////////////////////////////////////////////////
    //    custom implementations
    /////////////////////////////////////////////////////////////////////////////////////////////////////

    /** Mosaic image view implementation over {@link javax.swing.Icon} */
    static class IconView extends SwingView<javax.swing.Icon> {

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
    static class ImageAwtView extends SwingView<java.awt.Image> {

        private ImageAwt img = new ImageAwt(this);

        @Override
        protected java.awt.Image createImage() { return img.create(); }

        @Override
        protected void drawModified(Collection<BaseCell> modifiedCells) {
            img.drawWrapper(g -> drawSwing(g, modifiedCells, _useBackgroundColor));
        }

    }

    /** Mosaic image controller implementation for {@link IconView} */
    public static class IconController extends MosaicImageController<javax.swing.Icon, MosaicImg.IconView> {

        public IconController() {
            super(new MosaicImg.IconView());
        }

        @Override
        public void close() {
            super.close();
            getView().close();
        }

    }

    /** Mosaic image controller implementation for {@link ImageAwtView} */
    public static class ImageAwtController extends MosaicImageController<java.awt.Image, MosaicImg.ImageAwtView> {

        public ImageAwtController() {
            super(new MosaicImg.ImageAwtView());
        }

        @Override
        public void close() {
            super.close();
            getView().close();
        }

    }

}
