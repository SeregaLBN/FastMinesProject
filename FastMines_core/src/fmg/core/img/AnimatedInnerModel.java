package fmg.core.img;

import java.beans.PropertyChangeListener;

import fmg.common.geom.BoundDouble;
import fmg.common.geom.SizeDouble;
import fmg.common.notifier.NotifyPropertyChanged;
import fmg.core.types.Property;

/** MVC: inner model. Animated image characteristics. */
final class AnimatedInnerModel implements IAnimatedModel {

    /** Image is animated? */
    @Property(PROPERTY_ANIMATED)
    private boolean animated;

    /** Overall animation period (in milliseconds) */
    @Property(PROPERTY_ANIMATE_PERIOD)
    private long animatePeriod = 3000;

    /** Total frames of the animated period */
    @Property(PROPERTY_TOTAL_FRAMES)
    private int totalFrames = 30;

    @Property(PROPERTY_CURRENT_FRAME)
    private int currentFrame = 0;

    private NotifyPropertyChanged notifier = new NotifyPropertyChanged(this);

    // #region: begin unusable code
    @Override
    public SizeDouble getSize()                 { throw new UnsupportedOperationException(); }
    @Override
    public void setSize(SizeDouble value)       { throw new UnsupportedOperationException(); }

    @Override
    public BoundDouble getPadding()             { throw new UnsupportedOperationException(); }
    @Override
    public void setPadding(BoundDouble padding) { throw new UnsupportedOperationException(); }
    // #region: end unusable code

    /** Image is animated? */
    @Override
    public boolean isAnimated() { return animated; }
    @Override
    public void setAnimated(boolean value) {
        notifier.setProperty(animated, value, PROPERTY_ANIMATED);
    }

    /** Overall animation period (in milliseconds) */
    @Override
    public long getAnimatePeriod() { return animatePeriod; }
    /** Overall animation period (in milliseconds) */
    @Override
    public void setAnimatePeriod(long value) {
        notifier.setProperty(animatePeriod, value, PROPERTY_ANIMATE_PERIOD);
    }

    /** Total frames of the animated period */
    @Override
    public int getTotalFrames() { return totalFrames; }
    @Override
    public void setTotalFrames(int value) {
        if (notifier.setProperty(totalFrames, value, PROPERTY_TOTAL_FRAMES))
            setCurrentFrame(0);
    }

    @Override
    public int getCurrentFrame() { return currentFrame; }
    @Override
    public void setCurrentFrame(int value) {
        notifier.setProperty(currentFrame, value, PROPERTY_CURRENT_FRAME);
    }

    @Override
    public void close() {
        notifier.close();
    }

    @Override
    public void addListener(PropertyChangeListener listener) {
        notifier.addListener(listener);
    }
    @Override
    public void removeListener(PropertyChangeListener listener) {
        notifier.removeListener(listener);
    }

}
