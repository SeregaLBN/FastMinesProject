package fmg.core.img;

import java.beans.PropertyChangeListener;

import fmg.common.geom.BoundDouble;
import fmg.common.geom.SizeDouble;
import fmg.common.notifier.NotifyPropertyChanged;

/** MVC: inner model. Animated image characteristics. */
final class AnimatedInnerModel implements IAnimatedModel {

    /** Image is animated? */
    private boolean _animated;
    /** Overall animation period (in milliseconds) */
    private long _animatePeriod = 3000;
    /** Total frames of the animated period */
    private int _totalFrames = 30;
    private int _currentFrame = 0;

    private NotifyPropertyChanged _notifier = new NotifyPropertyChanged(this);

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
    public boolean isAnimated() { return _animated; }
    @Override
    public void setAnimated(boolean value) {
        _notifier.setProperty(_animated, value, PROPERTY_ANIMATED);
    }

    /** Overall animation period (in milliseconds) */
    @Override
    public long getAnimatePeriod() { return _animatePeriod; }
    /** Overall animation period (in milliseconds) */
    @Override
    public void setAnimatePeriod(long value) {
        _notifier.setProperty(_animatePeriod, value, PROPERTY_ANIMATE_PERIOD);
    }

    /** Total frames of the animated period */
    @Override
    public int getTotalFrames() { return _totalFrames; }
    @Override
    public void setTotalFrames(int value) {
        if (_notifier.setProperty(_totalFrames, value, PROPERTY_TOTAL_FRAMES))
            setCurrentFrame(0);
    }

    @Override
    public int getCurrentFrame() { return _currentFrame; }
    @Override
    public void setCurrentFrame(int value) {
        _notifier.setProperty(_currentFrame, value, PROPERTY_CURRENT_FRAME);
    }

    @Override
    public void close() {
        _notifier.close();
    }

    @Override
    public void addListener(PropertyChangeListener listener) {
        _notifier.addListener(listener);
    }
    @Override
    public void removeListener(PropertyChangeListener listener) {
        _notifier.removeListener(listener);
    }

}
