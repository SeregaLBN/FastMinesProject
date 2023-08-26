package fmg.core.types.draw;

import static fmg.core.img.PropertyConst.PROPERTY_COLOR_LIGHT;
import static fmg.core.img.PropertyConst.PROPERTY_COLOR_SHADOW;
import static fmg.core.img.PropertyConst.PROPERTY_WIDTH;

import java.util.Objects;
import java.util.function.Consumer;

import fmg.common.Color;
import fmg.common.geom.DoubleExt;

/** Характеристики кисти у рамки ячейки */
public class PenBorder {

    /** may be changed */
    public static double DefaultWidth = 3;

    private Color colorShadow;
    private Color colorLight;
    private double width;

    private Consumer<String> changedCallback;

    public PenBorder() {
        this(Color.Black(), Color.White(), DefaultWidth);
    }

    public PenBorder(
        Color colorShadow,
        Color colorLight,
        double iWidth)
    {
        this.colorShadow = colorShadow;
        this.colorLight  = colorLight;
        this.width = iWidth;
    }

    public Color getColorShadow() {
        return colorShadow;
    }

    public void setColorShadow(Color colorShadow) {
        if (this.colorShadow.equals(colorShadow))
            return;

        this.colorShadow = Objects.requireNonNull(colorShadow);

        if (changedCallback != null)
            changedCallback.accept(PROPERTY_COLOR_SHADOW);
    }

    public Color getColorLight() {
        return colorLight;
    }

    public void setColorLight(Color colorLight) {
        if (this.colorLight.equals(colorLight))
            return;

        this.colorLight = Objects.requireNonNull(colorLight);

        if (changedCallback != null)
            changedCallback.accept(PROPERTY_COLOR_LIGHT);
    }

    public double getWidth() {
        return width;
    }

    public void setWidth(double width) {
        if (DoubleExt.almostEquals(this.width, width))
            return;

        this.width = width;

        if (changedCallback != null)
            changedCallback.accept(PROPERTY_WIDTH);
    }

    @Override
    public String toString() {
        return "PenBorder{colorShadow=" + colorShadow + ", colorLight=" + colorLight + ", width=" + width + "}";
    }

    @Override
    public int hashCode() {
        return Objects.hash(colorLight, colorShadow, width);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        PenBorder other = (PenBorder)obj;
        return Objects.equals(colorLight, other.colorLight)
            && Objects.equals(colorShadow, other.colorShadow)
            && (Double.doubleToLongBits(width) == Double.doubleToLongBits(other.width));
    }

    public void setListener(Consumer<String> callback) {
        if ((callback != null) && (changedCallback != null))
            throw new IllegalArgumentException("Can only set the controller once");
        changedCallback = callback;
    }

}
