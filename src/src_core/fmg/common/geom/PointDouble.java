package fmg.common.geom;

import java.util.Locale;

public class PointDouble {

    public double x, y;

    public PointDouble() { x = y = 0;    }
    public PointDouble(double x, double y) { this.x = x; this.y = y; }
    public PointDouble(PointDouble p) { this.x = p.x; this.y = p.y; }

    @Override
    public int hashCode() {
        long temp = Double.doubleToLongBits(x);
        int result = 31 + (int) (temp ^ (temp >>> 32));
        temp = Double.doubleToLongBits(y);
        return 31 * result + (int) (temp ^ (temp >>> 32));
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof PointDouble))
            return false;
        PointDouble other = (PointDouble) obj;
        return (Double.doubleToLongBits(x) == Double.doubleToLongBits(other.x)) &&
               (Double.doubleToLongBits(y) == Double.doubleToLongBits(other.y));
    }

    @Override
    public String toString() { return String.format(Locale.US, "{x=%.2f, y=%.2f}", x, y); }

}
