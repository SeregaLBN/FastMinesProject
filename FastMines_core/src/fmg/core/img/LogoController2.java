package fmg.core.img;

import fmg.common.HSV;
import fmg.common.geom.PointDouble;
import fmg.common.geom.SizeDouble;
import fmg.common.geom.util.FigureHelper;
import fmg.common.ui.UiInvoker;

/** MVC controller of logo image
 * @param <TImage> platform specific view/image/picture or other display context/canvas/window/panel
 * @param <TView> MVC view  */
public class LogoController2<TImage,
                            TView extends IImageView2<TImage>>
    extends ImageController2<TImage, LogoModel2, TView>
{

    public boolean isAnimated() { return rotateImage || polarLights; }

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

    private boolean animationWasUsed = false;

    protected void init(TView view) {
        super.init(new LogoModel2(), view);
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
        animationWasUsed = true;
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

        // logo rotate
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
                palette[i].h = LogoModel2.DEFAULT_PALETTE[i].h + rotateAngle;
        }

        onModelChanged(ImageHelper.PROPERTY_NAME_OTHER);
    }

    @Override
    public void close() throws Exception {
        if (animationWasUsed) // do not call UiInvoker.ANIMATOR if it is not already used
            UiInvoker.Animator.get().unsubscribe(this);
        super.close();
    }

    public LogoController2<TImage,TView> asMine() {
        for (HSV item : getModel().getPalette())
            item.grayscale();
        return this;
    }

}
