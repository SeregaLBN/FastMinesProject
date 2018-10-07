package fmg.core.img;

/**
 * Image MVC: model
 * Model of animated image data/properties/characteristics
 */
public interface IAnimatedModel extends IImageModel {

    public static final String PROPERTY_ANIMATED       = "Animated";
    public static final String PROPERTY_ANIMATE_PERIOD = "AnimatePeriod";
    public static final String PROPERTY_TOTAL_FRAMES   = "TotalFrames";
    public static final String PROPERTY_CURRENT_FRAME  = "CurrentFrame";

    boolean isAnimated();
    void setAnimated(boolean value);

    /** Overall animation period (in milliseconds) */
    long getAnimatePeriod();
    void setAnimatePeriod(long value);

    /** Total frames of the animated period (animate iterations) */
    int getTotalFrames();
    void setTotalFrames(int value);

    int getCurrentFrame();
    void setCurrentFrame(int value);

//    /** frames per second (hint: max is 60) */
//    default int  getFPS() { return (int)(getTotalFrames() * 1000 / getAnimatePeriod()); }
//    default void setFPS(int value) { setTotalFrames((int)(value * getAnimatePeriod() / 1000)); }

}
