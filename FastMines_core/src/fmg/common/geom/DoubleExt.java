package fmg.common.geom;

public final class DoubleExt {
    private DoubleExt() {}

    /** Equals N digit precision */
    public static boolean equalsPrecision(double value1, double value2) {
        return equalsPrecision(value1, value2, 0.00001);
    }

    public static boolean equalsPrecision(double value1, double value2, double precision) {
        return Math.abs(value1 - value2) <= precision;
    }

    /** @return value1 == value2 */
    public static boolean almostEquals(double value1, double value2) {
        return almostEquals(value1, value2, 1);
    }

    public static boolean almostEquals(double value1, double value2, int units) {
        long lValue1 = Double.doubleToLongBits(value1);
        long lValue2 = Double.doubleToLongBits(value2);

        // If the signs are different, return false except for +0 and -0.
        if ((lValue1 >>> 63) != (lValue2 >>> 63)) {
            return 0 == Double.compare(lValue1, value2);
        }

        double diff = Math.abs(lValue1 - lValue2);
        return (diff <= units);
    }

}
