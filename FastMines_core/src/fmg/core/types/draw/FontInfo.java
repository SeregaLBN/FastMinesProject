package fmg.core.types.draw;

import static fmg.core.img.PropertyConst.PROPERTY_BOLD;
import static fmg.core.img.PropertyConst.PROPERTY_FONT_NAME;
import static fmg.core.img.PropertyConst.PROPERTY_SIZE;

import java.util.Objects;
import java.util.function.Consumer;

import fmg.common.Logger;
import fmg.common.geom.DoubleExt;

/** Minimal font descripton */
public class FontInfo2 {

    /** font name */
    private String name = "SansSerif"; // Arial

    /** font is bold? */
    private boolean bold = false;

    /** font size */
    private double size = 10;

    private Consumer<String> changedCallback;

    public FontInfo2() { }
    public FontInfo2(String fontName, boolean isBold, double fontSize) {
        this.name = fontName;
        this.bold = isBold;
        this.size = fontSize;
    }

    public String getName() { return name; }
    public void setName(String fontName) {
        Objects.requireNonNull(fontName);
        if (this.name.equals(fontName))
            return;
        this.name = fontName;
        if (changedCallback != null)
            changedCallback.accept(PROPERTY_FONT_NAME);
    }

    public boolean isBold() { return bold; }
    public void setBold(boolean isBold) {
        if (this.bold == isBold)
            return;
        this.bold = isBold;
        if (changedCallback != null)
            changedCallback.accept(PROPERTY_BOLD);
    }

    public double getSize() { return size; }
    public void setSize(double size) {
        assert(size > 0.01);
        if (size < 0.01) {
            //throw new IllegalArgumentException("Font size value must be positive: size=" + size);
            Logger.error("Font size value must be positive: size=" + size);
            size = 0.1;
        }
        if (DoubleExt.almostEquals(this.size, size))
            return;
        this.size = size;
        if (changedCallback != null)
            changedCallback.accept(PROPERTY_SIZE);
    }

    @Override
    public String toString() {
        return "FontInfo{fontName=" + name + ", isBold=" + bold + ", size=" + size + "}";
    }

    @Override
    public int hashCode() {
        return Objects.hash(bold, name, size);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        FontInfo2 other = (FontInfo2)obj;
        return (bold == other.bold)
            && Objects.equals(name, other.name)
            && (Double.doubleToLongBits(size) == Double.doubleToLongBits(other.size));
    }

    public void setListener(Consumer<String> callback) {
        if ((callback != null) && (changedCallback != null))
            throw new IllegalArgumentException("Can only set the controller once");
        changedCallback = callback;
    }

}
