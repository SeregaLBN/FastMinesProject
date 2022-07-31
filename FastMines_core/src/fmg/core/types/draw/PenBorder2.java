package fmg.core.types.draw;

import java.util.Objects;
import java.util.function.Consumer;

import fmg.common.Color;
import fmg.common.geom.DoubleExt;
import fmg.core.img.ImageHelper;

/** Характеристики кисти у рамки ячейки */
public class PenBorder2 {

    private Color colorShadow;
    private Color colorLight;
    private double width;

    private Consumer<String> changedCallback;

    public PenBorder2() {
        this(Color.Black(), Color.White(), 3);
//        this(Color.Green(), Color.Red(), 1);
    }

    public PenBorder2(
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

    public void setListener(Consumer<String> callback) {
        if ((callback != null) && (changedCallback != null))
            throw new IllegalArgumentException("Can only set the controller once");
        changedCallback = callback;
    }

}
