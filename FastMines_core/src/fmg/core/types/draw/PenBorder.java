package fmg.core.types.draw;

import java.beans.PropertyChangeListener;

import fmg.common.Color;
import fmg.common.geom.DoubleExt;
import fmg.common.notifier.INotifyPropertyChanged;
import fmg.common.notifier.NotifyPropertyChanged;
import fmg.core.types.Property;

/** Характеристики кисти у рамки ячейки */
public class PenBorder implements INotifyPropertyChanged {

    public static final String PROPERTY_COLOR_SHADOW = "ColorShadow";
    public static final String PROPERTY_COLOR_LIGHT  = "ColorLight";
    public static final String PROPERTY_WIDTH        = "Width";

    @Property(PROPERTY_COLOR_SHADOW)
    private Color colorShadow;

    @Property(PROPERTY_COLOR_LIGHT)
    private Color colorLight;

    @Property(PROPERTY_WIDTH)
    private double width;

    protected NotifyPropertyChanged notifier = new NotifyPropertyChanged(this);

    public PenBorder() {
        this(Color.Black(), Color.White(), 3);
//        this(Color.Green(), Color.Red(), 1);
    }

    public PenBorder(
        Color colorShadow,
        Color colorLight,
        int iWidth)
    {
        this.colorShadow = colorShadow;
        this.colorLight  = colorLight;
        this.width = iWidth;
    }

    public Color getColorShadow() {
        return colorShadow;
    }

    public void setColorShadow(Color colorShadow) {
        notifier.setProperty(this.colorShadow, colorShadow, PROPERTY_COLOR_SHADOW);
    }

    public Color getColorLight() {
        return colorLight;
    }

    public void setColorLight(Color colorLight) {
        notifier.setProperty(this.colorLight, colorLight, PROPERTY_COLOR_LIGHT);
    }

    public double getWidth() {
        return width;
    }

    public void setWidth(double width) {
        double old = this.width;
        if (DoubleExt.almostEquals(old, width))
            return;
        this.width = width;
        notifier.firePropertyChanged(old, width, PROPERTY_WIDTH);
    }

    @Override
    public int hashCode() {
        int result = 31 + colorLight.hashCode();
        result = 31 * result + colorShadow.hashCode();
        long temp = Double.doubleToLongBits(width);
        return 31 * result + (int)(temp ^ (temp >>> 32));
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof PenBorder)) return false;
        PenBorder penObj = (PenBorder) obj;
        return DoubleExt.almostEquals(width, penObj.width)
                && colorShadow.equals(penObj.colorShadow)
                && colorLight.equals(penObj.colorLight);
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
