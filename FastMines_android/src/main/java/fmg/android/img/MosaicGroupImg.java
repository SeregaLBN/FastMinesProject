package fmg.android.img;

import java.util.stream.Stream;

import fmg.common.Color;
import fmg.common.Pair;
import fmg.common.geom.PointDouble;
import fmg.core.img.MosaicGroupController;
import fmg.core.img.MosaicGroupModel;
import fmg.core.types.EMosaicGroup;

/** Representable {@link EMosaicGroup} as image */
@Deprecated
public final class MosaicGroupImg {
    private MosaicGroupImg() {}

    /**
     * Representable {@link EMosaicGroup} as image
     * <br>
     * Base image view Android implementation
     *
     * @param <TImage> Android specific image: {@link android.graphics.Bitmap}
     **/
    abstract static class AndroidView<TImage> extends MosaicSkillOrGroupView<TImage, MosaicGroupModel> {

            /** @param group - may be null. if Null - representable image of EMosaicGroup.class */
            protected AndroidView(EMosaicGroup group) {
                super(new MosaicGroupModel(group));
            }

            @Override
            protected Stream<Pair<Color, Stream<PointDouble>>> getCoords() { return getModel().getCoords(); }

            @Override
            public void close() {
                getModel().close();
                super.close();
            }

    }

    /////////////////////////////////////////////////////////////////////////////////////////////////////
    //    custom implementations
    /////////////////////////////////////////////////////////////////////////////////////////////////////

    /** MosaicsGroup image view implementation over {@link android.graphics.Bitmap} */
    public static class BitmapView extends AndroidView<android.graphics.Bitmap> {

        private BmpCanvas wrap = new BmpCanvas();

        public BitmapView(EMosaicGroup group) { super(group); }

        @Override
        protected android.graphics.Bitmap createImage() {
            return wrap.createImage(getModel().getSize());
        }

        @Override
        protected void drawBody() {
            draw(wrap.getCanvas());
        }

        @Override
        public void close() {
            wrap.close();
        }

    }

    /** MosaicsGroup image controller implementation for {@link BitmapView} */
    public static class BitmapController extends MosaicGroupController<android.graphics.Bitmap, BitmapView> {

        public BitmapController(EMosaicGroup group) {
            super(group==null, new BitmapView(group));
        }

        @Override
        public void close() {
            getView().close();
            super.close();
        }

    }

}
