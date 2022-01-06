package fmg.core.img;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import fmg.common.Color;
import fmg.common.geom.BoundDouble;
import fmg.common.geom.DoubleExt;
import fmg.common.geom.SizeDouble;
import fmg.common.notifier.NotifyPropertyChanged;
import fmg.core.types.Property;

/** MVC: model. Common animated image characteristics. */
public abstract class AnimatedImageModel implements IAnimatedModel {

    public static final Color DEFAULT_BK_COLOR         = Color.DarkOrange(); // Color.Coral(); //
    public static final Color DEFAULT_FOREGROUND_COLOR = Color.LightSeaGreen(); // Color.Orchid(); //
    public static final int   DEFAULT_IMAGE_SIZE = 100;
    public static final int   DEFAULT_PADDING = (int)(DEFAULT_IMAGE_SIZE * 0.05); // 5%

    public static final String PROPERTY_BACKGROUND_COLOR = "BackgroundColor";
    public static final String PROPERTY_BORDER_COLOR     = "BorderColor";
    public static final String PROPERTY_BORDER_WIDTH     = "BorderWidth";
    public static final String PROPERTY_FOREGROUND_COLOR = "ForegroundColor";
    public static final String PROPERTY_ROTATE_ANGLE     = "RotateAngle";
    public static final String PROPERTY_POLAR_LIGHTS     = "PolarLights";
    public static final String PROPERTY_ANIME_DIRECTION  = "AnimeDirection";


    /** width and height in pixel */
    @Property(PROPERTY_SIZE)
    private SizeDouble size = new SizeDouble(DEFAULT_IMAGE_SIZE, DEFAULT_IMAGE_SIZE);

    /** inside padding */
    @Property(PROPERTY_PADDING)
    private BoundDouble padding = new BoundDouble(DEFAULT_PADDING);

    @Property(PROPERTY_FOREGROUND_COLOR)
    private Color foregroundColor = DEFAULT_FOREGROUND_COLOR;

    /** background fill color */
    @Property(PROPERTY_BACKGROUND_COLOR)
    private Color backgroundColor = DEFAULT_BK_COLOR;

    @Property(PROPERTY_BORDER_COLOR)
    private Color borderColor = Color.Maroon().darker(0.5);

    @Property(PROPERTY_BORDER_WIDTH)
    private double borderWidth = 3;

    /** 0° .. +360° */
    @Property(PROPERTY_ROTATE_ANGLE)
    private double rotateAngle;

    /** animation of polar lights */
    @Property(PROPERTY_POLAR_LIGHTS)
    private boolean polarLights = true;

    /** animation direction (example: clockwise or counterclockwise for simple rotation) */
    @Property(PROPERTY_ANIME_DIRECTION)
    private boolean animeDirection = true;

    private final AnimatedInnerModel innerModel = new AnimatedInnerModel();
    protected NotifyPropertyChanged notifier = new NotifyPropertyChanged(this);
    private final PropertyChangeListener onInnerModelPropertyChangedListener = this::onInnerModelPropertyChanged;

    protected AnimatedImageModel() {
        innerModel.addListener(onInnerModelPropertyChangedListener);
    }

    /** width and height in pixel */
    @Override
    public SizeDouble getSize() { return size; }
    @Override
    public void setSize(SizeDouble size) {
        IImageModel.checkSize(size);
        SizeDouble old = this.size;
        if (notifier.setProperty(this.size, size, PROPERTY_SIZE))
            setPadding(IImageModel.recalcPadding(padding, this.size, old));
    }

    /** inside padding */
    @Override
    public BoundDouble getPadding() { return padding; }
    @Override
    public void setPadding(BoundDouble padding) {
        IImageModel.checkPadding(this, padding);
        notifier.setProperty(this.padding, new BoundDouble(padding), PROPERTY_PADDING);
    }

    public Color getForegroundColor() { return foregroundColor; }
    public void setForegroundColor(Color value) {
        notifier.setProperty(foregroundColor, value, PROPERTY_FOREGROUND_COLOR);
    }

    /** background fill color */
    public Color getBackgroundColor() { return backgroundColor; }
    public void setBackgroundColor(Color value) {
        notifier.setProperty(backgroundColor, value, PROPERTY_BACKGROUND_COLOR);
    }

    public Color getBorderColor() { return borderColor; }
    public void setBorderColor(Color value) {
        notifier.setProperty(borderColor, value, PROPERTY_BORDER_COLOR);
    }

    public double getBorderWidth() { return borderWidth; }
    public void setBorderWidth(double value) {
        if (!DoubleExt.hasMinDiff(borderWidth, value)) {
            double old = borderWidth;
            borderWidth = value;
            notifier.firePropertyChanged(old, value, PROPERTY_BORDER_WIDTH);
        }
    }

    /** 0° .. +360° */
    public double getRotateAngle() { return rotateAngle; }
    public void setRotateAngle(double value) {
        notifier.setProperty(rotateAngle, fixAngle(value), PROPERTY_ROTATE_ANGLE);
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
    public boolean isAnimated() { return innerModel.isAnimated(); }
    @Override
    public void setAnimated(boolean value) { innerModel.setAnimated(value); }

//    @Deprecated
//    public void SetRIandRAD(int redrawInterval/* = 100*/, double rotateAngleDelta/* = 1.4*/) {
//        double totalFrames = 360 / rotateAngleDelta;
//        double animatePeriod = totalFrames * redrawInterval;
//        setTotalFrames((int)totalFrames);
//        setAnimatePeriod((long)animatePeriod);
//    }

    /** Overall animation period (in milliseconds) */
    @Override
    public long getAnimatePeriod() { return innerModel.getAnimatePeriod(); }
    /** Overall animation period (in milliseconds) */
    @Override
    public void setAnimatePeriod(long value) { innerModel.setAnimatePeriod(value); }

    /** Total frames of the animated period */
    @Override
    public int getTotalFrames() { return innerModel.getTotalFrames(); }
    @Override
    public void setTotalFrames(int value) { innerModel.setTotalFrames(value); }

    @Override
    public int getCurrentFrame() { return innerModel.getCurrentFrame(); }
    @Override
    public void setCurrentFrame(int value) { innerModel.setCurrentFrame(value); }

    public boolean isPolarLights() { return polarLights; }
    public void setPolarLights(boolean polarLights) {
        notifier.setProperty(this.polarLights, polarLights, PROPERTY_POLAR_LIGHTS);
    }

    public boolean getAnimeDirection() { return animeDirection; }
    public void setAnimeDirection(boolean animeDirection) {
        notifier.setProperty(this.animeDirection, animeDirection, PROPERTY_ANIME_DIRECTION);
    }

    protected void onInnerModelPropertyChanged(PropertyChangeEvent ev) {
        // refire
        notifier.firePropertyChanged(ev.getOldValue(), ev.getNewValue(), ev.getPropertyName());
    }

    @Override
    public void close() {
        innerModel.removeListener(onInnerModelPropertyChangedListener);
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
