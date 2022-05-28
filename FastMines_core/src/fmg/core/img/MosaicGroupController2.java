package fmg.core.img;

import fmg.common.HSV;
import fmg.common.geom.PointDouble;
import fmg.common.geom.SizeDouble;
import fmg.common.geom.util.FigureHelper;
import fmg.common.ui.UiInvoker;
import fmg.core.types.EMosaicGroup;

/** MVC controller of {@link EMosaicGroup} image
 * @param <TImage> platform specific view/image/picture or other display context/canvas/window/panel
 * @param <TImageView> MVC view */
public abstract class MosaicGroupController2<TImage,
                                            TView extends IImageView2<TImage>>
    extends ImageController2<TImage, MosaicGroupModel2, TView>
{

//    protected MosaicGroupController2(boolean showBurgerMenu, TImageView imageView) {
//        super(imageView);
//
//        getBurgerMenuModel().setShow(showBurgerMenu);
//
//        addModelTransformer(new MosaicGroupTransformer());
//        usePolarLightFgTransforming(true);
//        useRotateTransforming(true);
//    }
//
//    public BurgerMenuModel getBurgerMenuModel() { return getView().getBurgerMenuModel(); }
//

    public boolean isAnimated() { return rotateImage || polarLightsFg || polarLightsBk; }

    /** Overall animation period (in milliseconds) */
    private long animatePeriod = 3000;

    /** frames per second (hint: max is 60) */
    private int fps = 15;

    private int currentFrame = 0;

    /** rotate image */
    private boolean rotateImage;

    /** animation of polar lights (foreground) */
    private boolean polarLightsFg = true;

    /** animation of polar lights (background) */
    private boolean polarLightsBk = true;

    /** animation direction (example: clockwise or counterclockwise for simple rotation) */
    private boolean clockwise = true;


    protected void init(TView view, EMosaicGroup mosaicGroup) {
        super.init(new MosaicGroupModel2(mosaicGroup), view);
    }

    public long getAnimatePeriod() {
        return animatePeriod;
    }
    public void setAnimatePeriod(long animatePeriod) {
        this.animatePeriod = animatePeriod;
    }

    public int getFps() {
        return fps;
    }
    public void setFps(int fps) {
        this.fps = Math.max(1, Math.min(60, fps));
    }

    public boolean isRotateImage() {
        return rotateImage;
    }
    public void setRotateImage(boolean rotateImage) {
        this.rotateImage = rotateImage;
        if (isAnimated())
            startAnimation();
        else
            pauseAnimation();
    }

    public boolean isPolarLightsForeground() {
        return polarLightsFg;
    }
    public void setPolarLightsForeground(boolean polarLights) {
        this.polarLightsFg = polarLights;
        if (isAnimated())
            startAnimation();
        else
            pauseAnimation();
    }

    public boolean isPolarLightsBackground() {
        return polarLightsBk;
    }
    public void setPolarLightsBackground(boolean polarLights) {
        this.polarLightsBk = polarLights;
        if (isAnimated())
            startAnimation();
        else
            pauseAnimation();
    }

    public boolean isClockwise() {
        return clockwise;
    }
    public void setClockwise(boolean clockwise) {
        this.clockwise = clockwise;
    }

    private void startAnimation() {
        UiInvoker.Animator.get().subscribe(this, this::nextAnimation);
    }

    private void pauseAnimation() {
        UiInvoker.Animator.get().pause(this);
    }

    private void nextAnimation(long timeOfStartAnimation) {
        long mod = timeOfStartAnimation % animatePeriod;
        int currFrame = (int)(mod * fps / 1000.0);
        if (currFrame != this.currentFrame) {
            this.currentFrame = currFrame;
            applyTransforming();
        }
    }

    private void applyTransforming() {
        var lm = getModel();

        long totalFrames = animatePeriod * fps / 1000;
        double rotateAngle = currentFrame * 360.0 / totalFrames;
        if (!clockwise)
            rotateAngle = -rotateAngle;

        // rotate
        if (rotateImage) {
            lm.getRays().clear();
            lm.getInn().clear();
            lm.getOct().clear();

            SizeDouble size = lm.getSize();
            PointDouble center = new PointDouble(size.width/2.0, size.height/2.0);
            FigureHelper.rotateCollection(lm.getRays(), rotateAngle, center);
            FigureHelper.rotateCollection(lm.getInn() , rotateAngle, center);
            FigureHelper.rotateCollection(lm.getOct() , rotateAngle, center);
        }

        // polar light transform
        if (polarLights) {
            HSV[] palette = lm.getPalette();
            for (int i = 0; i < palette.length; ++i)
                palette[i].h = MosaicGroupModel2.DEFAULT_PALETTE[i].h + rotateAngle;
        }

        onModelChanged(ImageHelper.PROPERTY_NAME_OTHER);
    }

    @Override
    public void close() {
        UiInvoker.Animator.get().unsubscribe(this);
        super.close();
    }

}
