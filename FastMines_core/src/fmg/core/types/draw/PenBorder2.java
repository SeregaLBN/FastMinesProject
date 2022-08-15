package fmg.core.types.draw;

import java.util.Objects;

import fmg.common.Color;
import fmg.common.geom.DoubleExt;

/** Характеристики кисти у рамки ячейки */
public class PenBorder2 {

    private Color colorShadow;
    private Color colorLight;
    private double width;

    public PenBorder2() {
        this(Color.Black(), Color.White(), 3);
//        this(Color.Green(), Color.Red(), 1);
    }

    public PenBorder2(
        Color colorShadow,
        Color colorLight,
        double penWidth)
    {
        this.colorShadow = colorShadow;
        this.colorLight  = colorLight;
        this.width = penWidth;
    }

    public Color getColorShadow() {
        return colorShadow;
    }

    public void setColorShadow(Color colorShadow) {
        if (this.colorShadow.equals(colorShadow))
            return;

        this.colorShadow = Objects.requireNonNull(colorShadow);
    }

    public Color getColorLight() {
        return colorLight;
    }

    public void setColorLight(Color colorLight) {
        if (this.colorLight.equals(colorLight))
            return;

        this.colorLight = Objects.requireNonNull(colorLight);
    }

    public double getWidth() {
        return width;
    }

    public void setWidth(double width) {
        if (DoubleExt.almostEquals(this.width, width))
            return;

        this.width = width;
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

}
