package fmg.android.img;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import fmg.common.Color;
import fmg.common.Pair;
import fmg.common.geom.PointDouble;
import fmg.core.img.IImageController;
import fmg.core.img.MosaicSkillController;
import fmg.core.img.MosaicSkillModel;
import fmg.core.types.ESkillLevel;

/**
 * Representable {@link ESkillLevel} as image
 * <br>
 * Android impl
 *
 * @param <TImage> Android specific image: {@link android.graphics.Bitmap})
 **/
public abstract class MosaicSkillImg<TImage> extends MosaicSkillOrGroupView<TImage, MosaicSkillModel> {

    /** @param skill - may be null. if Null - representable image of ESkillLevel.class */
    protected MosaicSkillImg(ESkillLevel skill) {
        super(new MosaicSkillModel(skill));
    }

    @Override
    protected Stream<Pair<Color, Stream<PointDouble>>> getCoords() { return getModel().getCoords(); }

    @Override
    public void close() {
        getModel().close();
        super.close();
    }

    /////////////////////////////////////////////////////////////////////////////////////////////////////
    //    custom implementations
    /////////////////////////////////////////////////////////////////////////////////////////////////////

    /** MosaicsSkill image view implementation over {@link android.graphics.Bitmap} */
    static class Bitmap extends MosaicSkillImg<android.graphics.Bitmap> {

        private BmpCanvas wrap = new BmpCanvas();

        public Bitmap(ESkillLevel skill) { super(skill); }

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

    /** MosaicsSkill image controller implementation for {@link Bitmap} */
    public static class ControllerBitmap extends MosaicSkillController<android.graphics.Bitmap, Bitmap> {

        public ControllerBitmap(ESkillLevel skill) {
            super(skill == null, new MosaicSkillImg.Bitmap(skill));
        }

        @Override
        public void close() {
            getView().close();
            super.close();
        }

    }

}
