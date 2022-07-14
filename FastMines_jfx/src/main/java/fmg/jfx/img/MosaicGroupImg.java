package fmg.jfx.img;

import java.util.stream.Stream;

import fmg.common.Color;
import fmg.common.Pair;
import fmg.common.geom.PointDouble;
import fmg.core.img.MosaicGroupController;
import fmg.core.img.MosaicGroupModel;
import fmg.core.types.EMosaicGroup;

/** Representable {@link fmg.core.types.EMosaicGroup} as image */
@Deprecated
public final class MosaicGroupImg {
    private MosaicGroupImg() {}

    /**
     * Representable {@link fmg.core.types.EMosaicGroup} as image
     * <br>
     * Base image view JFX implementation
     *
     * @param <TImage> JFX specific image: {@link javafx.scene.image.Image} or {@link javafx.scene.canvas.Canvas}
     */
    public abstract static class JfxView<TImage> extends MosaicSkillOrGroupView<TImage, MosaicGroupModel> {

        /** @param group - may be null. if Null - representable image of EMosaicGroup.class */
        protected JfxView(EMosaicGroup group) {
            super(new MosaicGroupModel(group));
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

    /** MosaicsGroup image view implementation over {@link javafx.scene.canvas.Canvas} */
    static class CanvasView extends JfxView<javafx.scene.canvas.Canvas> {

        private CanvasJfx canvas = new CanvasJfx(this);

        /** @param skill - may be null. if Null - representable image of EMosaicGroup.class */
        public CanvasView(EMosaicGroup group) { super(group); }

        @Override
        protected javafx.scene.canvas.Canvas createImage() { return canvas.create(); }

        @Override
        protected void drawBody() { draw(canvas.getGraphics()); }

    }

    /** MosaicsGroup image view implementation over {@link javafx.scene.image.Image} */
    static class ImageJfxView extends JfxView<javafx.scene.image.Image> {

        private ImageJfx img = new ImageJfx(this);

        /** @param skill - may be null. if Null - representable image of EMosaicGroup.class */
        public ImageJfxView(EMosaicGroup group) { super(group); }

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

    /** MosaicsGroup image controller implementation for {@link CanvasView} */
    public static class CanvasController extends MosaicGroupController<javafx.scene.canvas.Canvas, MosaicGroupImg.CanvasView> {

        public CanvasController(EMosaicGroup group) {
            super(group==null, new MosaicGroupImg.CanvasView(group));
        }

        @Override
        public void close() {
            super.close();
            getView().close();
        }

    }

    /** MosaicsGroup image controller implementation for {@link ImageJfxView} */
    public static class ImageJfxController extends MosaicGroupController<javafx.scene.image.Image, MosaicGroupImg.ImageJfxView> {

        public ImageJfxController(EMosaicGroup group) {
            super(group==null, new MosaicGroupImg.ImageJfxView(group));
        }

        @Override
        public void close() {
            super.close();
            getView().close();
        }

    }

}
