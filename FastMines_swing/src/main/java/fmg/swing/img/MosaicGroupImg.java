package fmg.swing.img;

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
     * Base image view SWING implementation
     *
     * @param <TImage> SWING specific image: {@link java.awt.Image} or {@link javax.swing.Icon}
     **/
    public abstract static class SwingView<TImage> extends MosaicSkillOrGroupView<TImage, MosaicGroupModel> {

        /** @param group - may be null. if Null - representable image of EMosaicGroup.class */
        protected SwingView(EMosaicGroup group) {
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

    /** MosaicsGroup image view implementation over {@link javax.swing.Icon} */
    static class IconView extends SwingView<javax.swing.Icon> {

        private IconSwing ico = new IconSwing(this);

        public IconView(EMosaicGroup group) { super(group); }

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

    /** MosaicsGroup image view implementation over {@link java.awt.Image} */
    static class ImageAwtView extends SwingView<java.awt.Image> {

        private ImageAwt img = new ImageAwt(this);

        public ImageAwtView(EMosaicGroup group) { super(group); }

        @Override
        protected java.awt.Image createImage() { return img.create(); }

        @Override
        protected void drawBody() { img.drawWrapper(g -> draw(g)); }

    }

    /** MosaicsGroup image controller implementation for {@link IconView} */
    public static class IconController extends MosaicGroupController<javax.swing.Icon, MosaicGroupImg.IconView> {

        public IconController(EMosaicGroup group) {
            super(group==null, new MosaicGroupImg.IconView(group));
        }

        @Override
        public void close() {
            super.close();
            getView().close();
        }

    }

    /** MosaicsGroup image controller implementation for {@link ImageAwtView} */
    public static class ImageAwtController extends MosaicGroupController<java.awt.Image, MosaicGroupImg.ImageAwtView> {

        public ImageAwtController(EMosaicGroup group) {
            super(group==null, new MosaicGroupImg.ImageAwtView(group));
        }

        @Override
        public void close() {
            super.close();
            getView().close();
        }

    }

}
