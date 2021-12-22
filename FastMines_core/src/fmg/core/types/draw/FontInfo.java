package fmg.core.types.draw;

import java.beans.PropertyChangeListener;

import fmg.common.Logger;
import fmg.common.geom.DoubleExt;
import fmg.common.notifier.INotifyPropertyChanged;
import fmg.common.notifier.NotifyPropertyChanged;
import fmg.core.types.Property;

/** minimal font descripton */
public class FontInfo implements INotifyPropertyChanged {

    public static final String PROPERTY_NAME = "Name";
    public static final String PROPERTY_BOLD = "Bold";
    public static final String PROPERTY_SIZE = "Size";

    /** font name */
    @Property(PROPERTY_NAME)
    private String name = "SansSerif"; // Arial

    /** font is bold? */
    @Property(PROPERTY_BOLD)
    private boolean bold = false;

    /** font size */
    @Property(PROPERTY_SIZE)
    private double size = 10;

    protected NotifyPropertyChanged notifier = new NotifyPropertyChanged(this);

    public FontInfo() { }
    public FontInfo(String fontName, boolean isBold, int fontSize) {
        this.name = fontName;
        this.bold = isBold;
        this.size = fontSize;
    }

    public String getName() { return name; }
    public void setName(String fontName) {
        if (this.name.equals(fontName))
            return;
        String old = this.name;
        this.name = fontName;
        notifier.firePropertyChanged(old, fontName, PROPERTY_NAME);
    }

    public boolean isBold() { return bold; }
    public void setBold(boolean isBold) {
        if (this.bold == isBold)
            return;
        boolean old = this.bold;
        this.bold = isBold;
        notifier.firePropertyChanged(old, isBold, PROPERTY_BOLD);
    }

    public double getSize() { return size; }
    public void setSize(double size) {
        assert(size > 0.01);
        if (size < 0.01) {
            //throw new IllegalArgumentException("Font size value must be positive: size=" + size);
            Logger.error("Font size value must be positive: size=" + size);
            size = 0.1;
        }
        double old = this.size;
        if (DoubleExt.hasMinDiff(this.size, size))
            return;
        this.size = size;
        notifier.firePropertyChanged(old, size, PROPERTY_SIZE);
    }

    @Override
    public int hashCode() {
        int result = 31 + ((name == null) ? 0 : name.hashCode());
        result = 31 * result + (bold ? 1231 : 1237);
        long temp = Double.doubleToLongBits(size);
        return 31 * result + (int)(temp ^ (temp >>> 32));
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        FontInfo other = (FontInfo)obj;
        if (name == null) {
            if (other.name != null)
                return false;
        }
        return name.equals(other.name) &&
                (bold == other.bold) &&
                DoubleExt.hasMinDiff(size, other.size);
    }

    @Override
    public String toString() {
        return "FontInfo{fontName=" + name + ", isBold=" + bold + ", size=" + size + "}";
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
