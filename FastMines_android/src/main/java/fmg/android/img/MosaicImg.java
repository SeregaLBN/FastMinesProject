package fmg.android.img;

import java.util.Collection;

import fmg.core.img.MosaicAnimatedModel;
import fmg.core.mosaic.MosaicImageController;
import fmg.core.mosaic.cells.BaseCell;
import fmg.core.types.EMosaic;
import fmg.android.mosaic.MosaicAndroidView;

/** Representable {@link EMosaic} as image */
public final class MosaicImg {
    private MosaicImg() {}

    /**
     * Representable {@link EMosaic} as image
     * <br>
     * Base image view Android implementation
     *
     * @param <TImage> Android specific image: {@link android.graphics.Bitmap}
     */
    public abstract static class AndroidView<TImage>
                   extends MosaicAndroidView<TImage, Void, MosaicAnimatedModel<Void>>
    {

        protected boolean useBackgroundColor = true;

        protected AndroidView() {
            super(new MosaicAnimatedModel<Void>());
        }

        @Override
        protected void drawBody() {
            //super.drawBody(); // !hide super implementation

            MosaicAnimatedModel<Void> model = getModel();

            useBackgroundColor = true;
            switch (model.getRotateMode()) {
            case fullMatrix:
                drawModified(model.getMatrix());
                break;
            case someCells:
                // draw static part
                drawModified(model.getNotRotatedCells());

                // draw rotated part
                useBackgroundColor = false;
                model.getRotatedCells(rotatedCells -> drawModified(rotatedCells));
                break;
            }
        }

        @Override
        public void close() {
            getModel().close();
            super.close();
        }

    }

    /////////////////////////////////////////////////////////////////////////////////////////////////////
    //    custom implementations
    /////////////////////////////////////////////////////////////////////////////////////////////////////

    /** Mosaic image view implementation over {@link android.graphics.Bitmap} */
    public static class BitmapView extends AndroidView<android.graphics.Bitmap> {

        private BmpCanvas wrap = new BmpCanvas();

        @Override
        protected android.graphics.Bitmap createImage() {
            return wrap.createImage(getModel().getSize());
        }

        @Override
        protected void drawModified(Collection<BaseCell> modifiedCells) {
            drawAndroid(wrap.getCanvas(), modifiedCells, useBackgroundColor);
        }

        @Override
        public void close() {
            wrap.close();
        }

    }

    /** Mosaic image controller implementation for {@link BitmapView} */
    public static class BitmapController extends MosaicImageController<android.graphics.Bitmap, BitmapView> {

        public BitmapController() {
            super(new BitmapView());
        }

        @Override
        public void close() {
            getView().close();
            super.close();
        }

    }

}
