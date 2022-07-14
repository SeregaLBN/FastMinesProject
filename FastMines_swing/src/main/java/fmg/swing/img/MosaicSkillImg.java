package fmg.swing.img;

import java.util.stream.Stream;

import fmg.common.Color;
import fmg.common.Pair;
import fmg.common.geom.PointDouble;
import fmg.core.img.MosaicSkillController;
import fmg.core.img.MosaicSkillModel;
import fmg.core.types.ESkillLevel;

/** Representable {@link fmg.core.types.ESkillLevel} as image */
@Deprecated
public final class MosaicSkillImg {
    private MosaicSkillImg() {}

    /**
     * Representable {@link fmg.core.types.ESkillLevel} as image
     * <br>
     * Base image view SWING implementation
     *
     * @param <TImage> SWING specific image: {@link java.awt.Image} or {@link javax.swing.Icon}
     **/
    public abstract static class SwingView<TImage> extends MosaicSkillOrGroupView<TImage, MosaicSkillModel> {

        /** @param skill - may be null. if Null - representable image of ESkillLevel.class */
        protected SwingView(ESkillLevel skill) {
            super(new MosaicSkillModel(skill));
        }

        @Override
        protected Stream<Pair<Color, Stream<PointDouble>>> getCoords() { return getModel().getCoords(); }

        @Override
        public void close() {
            super.close();
            getModel().close();
        }

    }

    /////////////////////////////////////////////////////////////////////////////////////////////////////
    //    custom implementations
    /////////////////////////////////////////////////////////////////////////////////////////////////////

    /** MosaicsSkill image view implementation over {@link javax.swing.Icon} */
    static class IconView extends SwingView<javax.swing.Icon> {

        private IconSwing ico = new IconSwing(this);

        public IconView(ESkillLevel skill) { super(skill); }

        @Override
        protected javax.swing.Icon createImage() { return ico.create(); }

        @Override
        protected void drawBody() { draw(ico.getGraphics()); }

        @Override
        public void close() {
            ico.close();
            super.close();
            ico = null;
        }

    }

    /** MosaicsSkill image view implementation over {@link java.awt.Image} */
    static class ImageAwtView extends SwingView<java.awt.Image> {

        private ImageAwt img = new ImageAwt(this);

        public ImageAwtView(ESkillLevel skill) { super(skill); }

        @Override
        protected java.awt.Image createImage() { return img.create(); }

        @Override
        protected void drawBody() { img.drawWrapper(g -> draw(g)); }

    }

    /** MosaicsSkill image controller implementation for {@link IconView} */
    public static class IconController extends MosaicSkillController<javax.swing.Icon, MosaicSkillImg.IconView> {

        public IconController(ESkillLevel skill) {
            super(skill == null, new MosaicSkillImg.IconView(skill));
        }

        @Override
        public void close() {
            super.close();
            getView().close();
        }

    }

    /** MosaicsSkill image controller implementation for {@link ImageAwtView} */
    public static class ImageAwtController extends MosaicSkillController<java.awt.Image, MosaicSkillImg.ImageAwtView> {

        public ImageAwtController(ESkillLevel skill) {
            super(skill == null, new MosaicSkillImg.ImageAwtView(skill));
        }

        @Override
        public void close() {
            super.close();
            getView().close();
        }

    }

}
