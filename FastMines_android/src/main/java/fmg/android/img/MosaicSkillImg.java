package fmg.android.img;

import java.util.stream.Stream;

import fmg.common.Color;
import fmg.common.Pair;
import fmg.common.geom.PointDouble;
import fmg.core.img.MosaicSkillController;
import fmg.core.img.MosaicSkillModel;
import fmg.core.types.ESkillLevel;

/** Representable {@link ESkillLevel} as image */
@Deprecated
public final class MosaicSkillImg {
    private MosaicSkillImg() {}

    /**
     * Representable {@link ESkillLevel} as image
     * <br>
     * Base image view Android implementation
     *
     * @param <TImage> Android specific image: {@link android.graphics.Bitmap}
     **/
    public abstract static class AndroidView<TImage> extends MosaicSkillOrGroupView<TImage, MosaicSkillModel> {

        /** @param skill - may be null. if Null - representable image of ESkillLevel.class */
        protected AndroidView(ESkillLevel skill) {
            super(new MosaicSkillModel(skill));
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

    /** MosaicsSkill image view implementation over {@link android.graphics.Bitmap} */
    public static class BitmapView extends AndroidView<android.graphics.Bitmap> {

        private BmpCanvas wrap = new BmpCanvas();

        public BitmapView(ESkillLevel skill) { super(skill); }

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

    /** MosaicsSkill image controller implementation for {@link BitmapView} */
    public static class BitmapController extends MosaicSkillController<android.graphics.Bitmap, BitmapView> {

        public BitmapController(ESkillLevel skill) {
            super(skill == null, new BitmapView(skill));
        }

        @Override
        public void close() {
            getView().close();
            super.close();
        }

    }

}
