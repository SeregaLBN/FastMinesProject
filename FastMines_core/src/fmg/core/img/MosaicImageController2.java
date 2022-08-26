package fmg.core.img;

import java.util.function.Consumer;

import fmg.common.Color;
import fmg.common.HSV;
import fmg.common.ui.UiInvoker;
import fmg.core.types.EMosaicGroup;
import fmg.core.types.draw.PenBorder2;

/** MVC controller of {@link EMosaicGroup} image
 * @param <TImage> platform specific view/image/picture or other display context/canvas/window/panel
 * @param <TView> mosaic view */
public abstract class MosaicImageController2<TImage,
                                            TView extends IImageView2<TImage>>
    extends ImageController2<TImage, MosaicImageModel2, TView>
{

    /** Overall animation period (in milliseconds) */
    private long animatePeriod = 3000;

    /** frames per second (hint: max is 60) */
    private int fps = 15;

    private int currentFrame = 0;

    /** rotate image */
    private boolean rotateImage;

    /** animation of polar lights (background) */
    private boolean polarLightsBk = false;

    /** animation direction (example: clockwise or counterclockwise for simple rotation) */
    private boolean clockwise = true;


    @Override
    protected void init(MosaicImageModel2 model, TView view) {
        super.init(model, view);
        if (isAnimated())
            startAnimation();
    }

    public boolean isAnimated() { return rotateImage || polarLightsBk; }

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
        double rotateAngleDelta = 360.0 / totalFrames;
        if (!clockwise)
            rotateAngleDelta = -rotateAngleDelta;
        double angle = currentFrame * rotateAngleDelta;

        // rotate
        model.setRotateAngle(angle);
        switch (model.getRotateMode()) {
        case FULL_MATRIX:
            model.rotateMatrix();
            break;
        case SOME_CELLS:
            model.rotateCells(rotateAngleDelta);
            break;
        default:
            throw new RuntimeException("Unsupported RotateMode=" + model.getRotateMode());
        }

        // polar light transform
        if (polarLightsBk)
            model.setBackgroundAngle(angle);
    }

    @Override
    public void close() {
        UiInvoker.Animator.get().unsubscribe(this);
        super.close();
    }

    // TODO metod as protected
    public static <T> void draw(MosaicImageModel2 m, Consumer<MosaicDrawContext<T>> realDraw) {
        Color bkClr = new HSV(m.getBackgroundColor())
                        .addHue(m.getBackgroundAngle())
                        .toColor();
        switch (m.getRotateMode()) {
        case FULL_MATRIX:
            realDraw.accept(new MosaicDrawContext<>(m, true, () -> bkClr, m::getMatrix, null, null));
            break;

        case SOME_CELLS:
            // 1. draw static part
            realDraw.accept(new MosaicDrawContext<>(m, true, () -> bkClr, m::getNotRotatedCells, null, null));

            // 2. draw rotated part
            PenBorder2 pb = m.getPenBorder();
            // save
            double borderWidth = pb.getWidth();
            Color colorLight  = pb.getColorLight();
            Color colorShadow = pb.getColorShadow();

            // unset notifier (щоб не призводило до малювання із методу малювання)
            var callback = m.getListener();
            m.setListener(null); // lock to fire changing model
            // modify
            pb.setWidth(2 * borderWidth);
            pb.setColorLight(colorLight.darker(0.5));
            pb.setColorShadow(colorShadow.darker(0.5));

            realDraw.accept(new MosaicDrawContext<>(m, false, () -> bkClr, m::getRotatedCells, null, null));

            // restore
            pb.setWidth(borderWidth);
            pb.setColorLight(colorLight);
            pb.setColorShadow(colorShadow);
            m.setListener(callback);

            break;

        default:
            throw new IllegalArgumentException();
        }
    }


}
