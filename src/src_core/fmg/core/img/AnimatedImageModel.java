package fmg.core.img;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import fmg.common.Color;
import fmg.common.geom.BoundDouble;
import fmg.common.geom.DoubleExt;
import fmg.common.geom.SizeDouble;
import fmg.common.notyfier.NotifyPropertyChanged;

/** MVC: model. Common animated image characteristics. */
public class AnimatedImageModel implements IAnimatedModel {

    public static final Color DefaultBkColor         = Color.DarkOrange();
    public static final Color DefaultForegroundColor = Color.Orchid();
    public static final int   DefaultImageSize = 100;
    public static final int   DefaultPadding = (int)(DefaultImageSize * 0.05); // 5%


    /** width and height in pixel */
    private SizeDouble _size = new SizeDouble(DefaultImageSize, DefaultImageSize);
    /** inside padding. Автоматически пропорционально регулирую при измениях размеров */
    private BoundDouble _padding = new BoundDouble(DefaultPadding);
    private Color _foregroundColor = DefaultForegroundColor;
    /** background fill color */
    private Color _backgroundColor = DefaultBkColor;
    private Color _borderColor = Color.Maroon().darker(0.5);
    private double _borderWidth = 3;
    /** 0° .. +360° */
    private double _rotateAngle;
    /** animation of polar lights */
    private boolean _polarLights = true;
    /** animation direction (example: clockwise or counterclockwise for simple rotation) */
    private boolean _animeDirection = true;
    private final AnimatedInnerModel _innerModel = new AnimatedInnerModel();
    private PropertyChangeListener innerModelListener = ev -> onInnerModelPropertyChanged(ev);
    protected NotifyPropertyChanged _notifier = new NotifyPropertyChanged(this);

    public AnimatedImageModel() {
        _innerModel.addListener(innerModelListener);
    }

    public static final String PROPERTY_PADDING          = "Padding";
    public static final String PROPERTY_BACKGROUND_COLOR = "BackgroundColor";
    public static final String PROPERTY_BORDER_COLOR     = "BorderColor";
    public static final String PROPERTY_BORDER_WIDTH     = "BorderWidth";
    public static final String PROPERTY_FOREGROUND_COLOR = "ForegroundColor";
    public static final String PROPERTY_ROTATE_ANGLE     = "RotateAngle";
    public static final String PROPERTY_POLAR_LIGHTS     = "PolarLights";
    public static final String PROPERTY_ANIME_DIRECTION  = "AnimeDirection";

    /** width and height in pixel */
    @Override
    public SizeDouble getSize() { return _size; }
    public void setSize(double widhtAndHeight) { setSize(new SizeDouble(widhtAndHeight, widhtAndHeight)); }
    @Override
    public void setSize(SizeDouble value) {
        SizeDouble old = _size;
        if (_notifier.setProperty(_size, value, PROPERTY_SIZE))
            recalcPadding(old);
    }

    /** inside padding */
    public BoundDouble getPadding() { return _padding; }
    public void setPadding(double bound) { setPadding(new BoundDouble(bound)); }
    public void setPadding(BoundDouble value) {
        if (value.getLeftAndRight() >= getSize().width)
            throw new IllegalArgumentException("Padding size is very large. Should be less than Width.");
        if (value.getTopAndBottom() >= getSize().height)
            throw new IllegalArgumentException("Padding size is very large. Should be less than Height.");
        BoundDouble paddingNew = new BoundDouble(value.left, value.top, value.right, value.bottom);
        _notifier.setProperty(_padding, paddingNew, PROPERTY_PADDING);
    }
    static BoundDouble recalcPadding(BoundDouble padding, SizeDouble current, SizeDouble old) {
        return new BoundDouble(padding.left   * current.width  / old.width,
                               padding.top    * current.height / old.height,
                               padding.right  * current.width  / old.width,
                               padding.bottom * current.height / old.height);
    }
    private void recalcPadding(SizeDouble old) {
        setPadding(recalcPadding(_padding, _size, old));
    }

    public Color getForegroundColor() { return _foregroundColor; }
    public void setForegroundColor(Color value) {
        _notifier.setProperty(_foregroundColor, value, PROPERTY_FOREGROUND_COLOR);
    }

    /** background fill color */
    public Color getBackgroundColor() { return _backgroundColor; }
    public void setBackgroundColor(Color value) {
        _notifier.setProperty(_backgroundColor, value, PROPERTY_BACKGROUND_COLOR);
    }

    public Color getBorderColor() { return _borderColor; }
    public void setBorderColor(Color value) {
        _notifier.setProperty(_borderColor, value, PROPERTY_BORDER_COLOR);
    }

    public double getBorderWidth() { return _borderWidth; }
    public void setBorderWidth(double value) {
        if (!DoubleExt.hasMinDiff(_borderWidth, value)) {
            double old = _borderWidth;
            _borderWidth = value;
            _notifier.onPropertyChanged(old, value, PROPERTY_BORDER_WIDTH);
        }
    }

    /** 0° .. +360° */
    public double getRotateAngle() { return _rotateAngle; }
    public void setRotateAngle(double value) {
        _notifier.setProperty(_rotateAngle, fixAngle(value), PROPERTY_ROTATE_ANGLE);
    }

    /** to diapason (0° .. +360°] */
    public static double fixAngle(double value) {
        return (value >= 360)
             ?              (value % 360)
             : (value < 0)
                ?           (value % 360) + 360
                :            value;
    }

    /** Image is animated? */
    @Override
    public boolean isAnimated() { return _innerModel.isAnimated(); }
    @Override
    public void setAnimated(boolean value) { _innerModel.setAnimated(value); }

    /** Overall animation period (in milliseconds) */
    @Override
    public long getAnimatePeriod() { return _innerModel.getAnimatePeriod(); }
    /** Overall animation period (in milliseconds) */
    @Override
    public void setAnimatePeriod(long value) { _innerModel.setAnimatePeriod(value); }

    /** Total frames of the animated period */
    @Override
    public int getTotalFrames() { return _innerModel.getTotalFrames(); }
    @Override
    public void setTotalFrames(int value) { _innerModel.setTotalFrames(value); }

    @Override
    public int getCurrentFrame() { return _innerModel.getCurrentFrame(); }
    @Override
    public void setCurrentFrame(int value) { _innerModel.setCurrentFrame(value); }

    public boolean isPolarLights() { return _polarLights; }
    public void setPolarLights(boolean polarLights) {
        _notifier.setProperty(_polarLights, polarLights, PROPERTY_POLAR_LIGHTS);
    }

    public boolean getAnimeDirection() { return _animeDirection; }
    public void setAnimeDirection(boolean animeDirection) {
        _notifier.setProperty(_animeDirection, animeDirection, PROPERTY_ANIME_DIRECTION);
    }

    protected void onInnerModelPropertyChanged(PropertyChangeEvent ev) {
        // refire
        _notifier.onPropertyChanged(ev.getOldValue(), ev.getNewValue(), ev.getPropertyName());
    }

    @Override
    public void close() {
        _innerModel.removeListener(innerModelListener);
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
