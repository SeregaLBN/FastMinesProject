package fmg.core.img;

import fmg.common.ui.UiInvoker;
import fmg.core.types.EMosaicGroup;

/** MVC controller of {@link EMosaicGroup} image
 * @param <TImage> platform specific view/image/picture or other display context/canvas/window/panel
 * @param <TView> MVC view */
public abstract class MosaicGroupController<TImage,
                                            TView extends IImageView<TImage>>
    extends ImageController<TImage, TView, MosaicGroupModel>
{

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

    private BurgerMenuModel burgerModel;

    public BurgerMenuModel getBurgerModel() {
        return burgerModel;
    }

    @Override
    protected void init(MosaicGroupModel model, TView view) {
        super.init(model, view);
        burgerModel = new BurgerMenuModel();
        burgerModel.setListener(this::onModelChanged);
        if (isAnimated())
            startAnimation();
    }

    public boolean isAnimated() { return rotateImage || polarLightsFg || polarLightsBk; }

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
        if (currFrame == this.currentFrame)
            return;

        this.currentFrame = currFrame;

        long totalFrames = animatePeriod * fps / 1000;
        double angle = currentFrame * 360.0 / totalFrames;
        if (!clockwise)
            angle = -angle;

        // rotate
        if (rotateImage) {
            model.setRotateAngle(angle);
            burgerModel.setRotateAngle(angle);
        }

        // polar light transform
        if (polarLightsFg)
            model.setForegroundAngle(angle);
        if (polarLightsBk)
            model.setBackgroundAngle(angle);
    }

    private boolean lock = false;
    @Override
    protected void onModelChanged(String property) {
        if (!lock && PropertyConst.PROPERTY_SIZE.equals(property)) try {
            lock = true;
            burgerModel.setSize(model.getSize());
        } finally {
            lock = false;
        }
        super.onModelChanged(property);
    }

    @Override
    public void close() {
        UiInvoker.Animator.get().unsubscribe(this);
        super.close();
    }

}
