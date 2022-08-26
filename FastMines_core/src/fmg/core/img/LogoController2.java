package fmg.core.img;

import fmg.common.HSV;
import fmg.common.ui.UiInvoker;

/** MVC controller of logo image
 * @param <TImage> platform specific view/image/picture or other display context/canvas/window/panel
 * @param <TView> MVC view  */
public class LogoController2<TImage,
                            TView extends IImageView2<TImage>>
    extends ImageController2<TImage, LogoModel2, TView>
{

    /** Overall animation period (in milliseconds) */
    private long animatePeriod = 3000;

    /** frames per second (hint: max is 60) */
    private int fps = 15;

    private int currentFrame = 0;

    /** rotate image */
    private boolean rotateImage;

    /** animation of polar lights */
    private boolean polarLights = true;

    /** animation direction (example: clockwise or counterclockwise for simple rotation) */
    private boolean clockwise = true;

    @Override
    protected void init(LogoModel2 model, TView view) {
        super.init(model, view);
        if (isAnimated())
            startAnimation();
    }

    public boolean isAnimated() { return rotateImage || polarLights; }

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

    public boolean isPolarLights() {
        return polarLights;
    }
    public void setPolarLights(boolean polarLights) {
        this.polarLights = polarLights;
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

        // logo rotate
        if (rotateImage)
            model.setRotateAngle(angle);

        // polar light transform
        if (polarLights)
            model.setPaletteColorOffset(angle);
    }

    @Override
    public void close() {
        UiInvoker.Animator.get().unsubscribe(this);
        super.close();
    }

    public LogoController2<TImage,TView> asMine() {
        for (HSV item : model.getPalette())
            item.grayscale();
        return this;
    }

}
