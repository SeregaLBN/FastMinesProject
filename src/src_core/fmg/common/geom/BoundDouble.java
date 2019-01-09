package fmg.common.geom;

import java.util.Locale;

/** Padding / Margin */
public class BoundDouble {

    public double left, right, top, bottom;

    public BoundDouble(BoundDouble copy) { this.left = copy.left; this.top = copy.top; this.right = copy.right; this.bottom = copy.bottom; }
    public BoundDouble(double bound) { left = top = right = bottom = bound; }
    public BoundDouble(double left, double top, double right, double bottom) {
        this.left   = left;
        this.top    = top;
        this.right  = right;
        this.bottom = bottom;
    }

    public double getLeftAndRight() { return left + right; }
    public double getTopAndBottom() { return top + bottom; }

    public boolean isEmpty() { return left == 0 && right == 0 && top == 0 && bottom == 0; }

    @Override
    public int hashCode() {
        long temp = Double.doubleToLongBits(bottom);
        int result = 31 + (int) (temp ^ (temp >>> 32));
        temp = Double.doubleToLongBits(left);
        result = 31 * result + (int) (temp ^ (temp >>> 32));
        temp = Double.doubleToLongBits(right);
        result = 31 * result + (int) (temp ^ (temp >>> 32));
        temp = Double.doubleToLongBits(top);
        return 31 * result + (int) (temp ^ (temp >>> 32));
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null) return false;
        if (!(obj instanceof BoundDouble))
            return false;
        return equals((BoundDouble) obj);
    }

    public boolean equals(BoundDouble other) {
        return (other != null) &&
               (Double.doubleToLongBits(bottom) == Double.doubleToLongBits(other.bottom)) &&
               (Double.doubleToLongBits(left)   == Double.doubleToLongBits(other.left)) &&
               (Double.doubleToLongBits(right)  == Double.doubleToLongBits(other.right)) &&
               (Double.doubleToLongBits(top)    == Double.doubleToLongBits(other.top));
    }

    @Override
    public String toString() { return String.format(Locale.US, "{lft=%.2f, rght=%.2f, top=%.2f, bttm=%.2f}", left, right, top, bottom); }

}
