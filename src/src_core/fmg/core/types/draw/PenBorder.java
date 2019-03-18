package fmg.core.types.draw;

import java.beans.PropertyChangeListener;

import fmg.common.Color;
import fmg.common.geom.DoubleExt;
import fmg.common.notyfier.INotifyPropertyChanged;
import fmg.common.notyfier.NotifyPropertyChanged;

/** Характеристики кисти у рамки ячейки */
public class PenBorder implements INotifyPropertyChanged {

    private Color _colorShadow, _colorLight;
    private double _width;
    protected NotifyPropertyChanged _notifier = new NotifyPropertyChanged(this);

    public PenBorder() {
        this(Color.Black(), Color.White(), 3);
//        this(Color.Green(), Color.Red(), 1);
    }

    public PenBorder(
        Color colorShadow,
        Color colorLight,
        int iWidth)
    {
        _colorShadow = colorShadow;
        _colorLight  = colorLight;
        _width = iWidth;
    }

    public static final String PROPERTY_COLOR_SHADOW = "ColorShadow";
    public static final String PROPERTY_COLOR_LIGHT  = "ColorLight";
    public static final String PROPERTY_WIDTH        = "Width";

    public Color getColorShadow() {
        return _colorShadow;
    }

    public void setColorShadow(Color colorShadow) {
        _notifier.setProperty(_colorShadow, colorShadow, PROPERTY_COLOR_SHADOW);
    }

    public Color getColorLight() {
        return _colorLight;
    }

    public void setColorLight(Color colorLight) {
        _notifier.setProperty(_colorLight, colorLight, PROPERTY_COLOR_LIGHT);
    }

    public double getWidth() {
        return _width;
    }

    public void setWidth(double width) {
        double old = _width;
        if (DoubleExt.hasMinDiff(old, width))
            return;
        _width = width;
        _notifier.firePropertyChanged(old, width, PROPERTY_WIDTH);
    }

    @Override
    public int hashCode() {
        int result = 31 + _colorLight.hashCode();
        result = 31 * result + _colorShadow.hashCode();
        long temp = Double.doubleToLongBits(_width);
        return 31 * result + (int)(temp ^ (temp >>> 32));
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof PenBorder)) return false;
        PenBorder penObj = (PenBorder) obj;
        return DoubleExt.hasMinDiff(_width, penObj._width)
                && _colorShadow.equals(penObj._colorShadow)
                && _colorLight.equals(penObj._colorLight);
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
