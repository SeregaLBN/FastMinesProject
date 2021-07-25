package fmg.jfx.img;

import java.util.stream.Stream;

import fmg.common.Color;
import fmg.common.Pair;
import fmg.common.geom.PointDouble;
import fmg.core.img.MosaicSkillController;
import fmg.core.img.MosaicSkillModel;
import fmg.core.types.ESkillLevel;

/** Representable {@link fmg.core.types.ESkillLevel} as image */
public final class MosaicSkillImg {
    private MosaicSkillImg() {}

    /**
     * Representable {@link fmg.core.types.ESkillLevel} as image
     * <br>
     * Base image view JavaFX implementation
     *
     * @param <TImage> JFX specific image: {@link javafx.scene.image.Image} or {@link javafx.scene.canvas.Canvas}
     **/
    public abstract static class JfxView<TImage> extends MosaicSkillOrGroupView<TImage, MosaicSkillModel> {

        /** @param skill - may be null. if Null - representable image of ESkillLevel.class */
        protected JfxView(ESkillLevel skill) {
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

    /** MosaicsSkill image view implementation over {@javafx.scene.canvas.Canvas} */
    public static class CanvasView extends JfxView<javafx.scene.canvas.Canvas> {

        private CanvasJfx canvas = new CanvasJfx(this);

        /** @param skill - may be null. if Null - representable image of ESkillLevel.class */
        public CanvasView(ESkillLevel skill) { super(skill); }

        @Override
        protected javafx.scene.canvas.Canvas createImage() { return canvas.create(); }

        @Override
        protected void drawBody() { draw(canvas.getGraphics()); }

    }

    /** MosaicsSkill image view implementation over {@link javafx.scene.image.Image} */
    public static class ImageJfxView extends JfxView<javafx.scene.image.Image> {

        private ImageJfx img = new ImageJfx(this);

        /** @param skill - may be null. if Null - representable image of ESkillLevel.class */
        public ImageJfxView(ESkillLevel skill) { super(skill); }

        @Override
        protected javafx.scene.image.Image createImage() {
            img.createCanvas();
            return null; // img.createImage(); // fake empty image
        }

        @Override
        protected void drawBody() {
            draw(img.getGraphics());
            setImage(img.createImage()); // real image
        }

    }

    /** MosaicsSkill image controller implementation for {@link CanvasView} */
    public static class CanvasController extends MosaicSkillController<javafx.scene.canvas.Canvas, MosaicSkillImg.CanvasView> {

        public CanvasController(ESkillLevel skill) {
            super(skill == null, new MosaicSkillImg.CanvasView(skill));
        }

        @Override
        public void close() {
            super.close();
            getView().close();
        }

    }

    /** MosaicsSkill image controller implementation for {@link ImageJfxView} */
    public static class ImageJfxController extends MosaicSkillController<javafx.scene.image.Image, MosaicSkillImg.ImageJfxView> {

        public ImageJfxController(ESkillLevel skill) {
            super(skill == null, new MosaicSkillImg.ImageJfxView(skill));
        }

        @Override
        public void close() {
            super.close();
            getView().close();
        }

    }

}
