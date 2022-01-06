package fmg.common.geom;

import java.util.Locale;

public class SizeDouble {

    public double width, height;

    public SizeDouble() { width = height = 0; }
    public SizeDouble(double width, double height) { this.width = width; this.height = height; }
    public SizeDouble(SizeDouble c) { this.width = c.width; this.height = c.height; }

    @Override
    public int hashCode() {
        long temp = Double.doubleToLongBits(height);
        int result = 31 + (int) (temp ^ (temp >>> 32));
        temp = Double.doubleToLongBits(width);
        return 31 * result + (int) (temp ^ (temp >>> 32));
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof SizeDouble))
            return false;
        SizeDouble other = (SizeDouble) obj;
        return (Double.doubleToLongBits(height) == Double.doubleToLongBits(other.height)) &&
               (Double.doubleToLongBits(width)  == Double.doubleToLongBits(other.width));
    }

    @Override
    public String toString() { return String.format(Locale.US, "{w=%.2f, h=%.2f}", width, height); }

}
