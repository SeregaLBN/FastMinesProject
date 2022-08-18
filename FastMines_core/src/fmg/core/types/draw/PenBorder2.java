package fmg.core.types.draw;

import java.util.Objects;
import java.util.function.Consumer;

import fmg.common.Color;
import fmg.common.geom.DoubleExt;
import fmg.core.img.ImageHelper;

/** Характеристики кисти у рамки ячейки */
public class PenBorder2 {

    /** may be changed */
    public static double DefaultWidth = 3;

    private Color colorShadow;
    private Color colorLight;
    private double width;

    private Consumer<String> changedCallback;

    public PenBorder2() {
        this(Color.Black(), Color.White(), DefaultWidth);
    }

    public PenBorder2(
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
            changedCallback.accept(ImageHelper.PROPERTY_OTHER);
    }

    public Color getColorLight() {
        return colorLight;
    }

    public void setColorLight(Color colorLight) {
        if (this.colorLight.equals(colorLight))
            return;

        this.colorLight = Objects.requireNonNull(colorLight);

        if (changedCallback != null)
            changedCallback.accept(ImageHelper.PROPERTY_OTHER);
    }

    public double getWidth() {
        return width;
    }

    public void setWidth(double width) {
        if (DoubleExt.almostEquals(this.width, width))
            return;

        this.width = width;

        if (changedCallback != null)
            changedCallback.accept(ImageHelper.PROPERTY_OTHER);
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
        PenBorder2 other = (PenBorder2)obj;
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
