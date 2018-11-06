package fmg.common;

import java.util.Locale;

import fmg.common.geom.DoubleExt;

/**
 * Сylindrical-coordinate representations of points in an RGB color model
 * <p>
 *  HSV - Hue, Saturation, Value (тон, насыщенность, значение) <br>
 *    it also <br>
 *  HSB - Hue, Saturation, Brightness (тон, насыщенность, яркость)
 * </p>
 *
 * White: h=any; s=0; v=100 <br>
 * Black: h=any; s=any; v=0
 **/
public class HSV {

    /** Hue — цветовой тон (цвет/оттенок): <ul> 0°..360°
     * <li> 0° - red
     * <li> 60° - yellow
     * <li> 120° - green
     * <li> 180° - aqua
     * <li> 240° - blue
     * <li> 300° - fuchsia
     * <li> 360° - red
     **/
    public double h;

    /** Saturation — насыщенность: 0%..100% (white(gray)..color) */
    public double s;

    /** Value or Brightness — значение цвета или яркость: 0%..100% (black..color) */
    public double v;

    /** Alpha chanel: 0..255 */
    public int a;

    public HSV() {
        h = 0;
        s = 100;
        v = 100;
        a = 0xFF;
    }

    public HSV(int h, int s, int v) {
        this.h = h;
        this.s = s;
        this.v = v;
        this.a = 0xFF;

        fix();
    }

    public HSV(int h, int s, int v, int a) {
        this.h = h;
        this.s = s;
        this.v = v;
        this.a = Color.checkA(a);

        fix();
    }

    public HSV(Color rgba) {
        this.a = rgba.a;
        fromColorDouble(rgba.r, rgba.g, rgba.b);
    }

    public HSV addHue(double addonH) {
        h += addonH;
        fix();
        return this;
    }

    private void fromColorDouble(double r, double g, double b) {
        double max = Math.max(Math.max(r, g), b);
        double min = Math.min(Math.min(r, g), b);

        { // calc H
            if (DoubleExt.hasMinDiff(max, min))
                h = 0;
            else if (DoubleExt.hasMinDiff(max, r))
                h = 60 * (g - b) / (max - min) + ((g < b) ? 360 : 0);
            else if (DoubleExt.hasMinDiff(max, g))
                h = 60 * (b - r) / (max - min) + 120;
            else if (DoubleExt.hasMinDiff(max, b))
                h = 60 * (r - g) / (max - min) + 240;
            else
                throw new RuntimeException();
        }
        s = DoubleExt.hasMinDiff(max, 0) ? 0 : 100*(1 - min/max);
        v = max*100/255;

        fix();
    }

    public Color toColor() {
        double[] rgb = toColorDouble();
        double r = rgb[0];
        double g = rgb[1];
        double b = rgb[2];
        return new Color(a, (int)(r*255/100), (int)(g*255/100), (int)(b*255/100));
    }

    private double[] toColorDouble() {
        fix();

        double vMin = (100 - s) * v / 100;
        double delta = (v - vMin) * ((h % 60) / 60.0);
        double vInc = vMin + delta;
        double vDec = v - delta;

        double r, g, b;
        switch (((int)(h / 60)) % 6) {
        case 0:
            r = v; g = vInc; b = vMin;
            break;
        case 1:
            r = vDec; g = v; b = vMin;
            break;
        case 2:
            r = vMin; g = v; b = vInc;
            break;
        case 3:
            r = vMin; g = vDec; b = v;
            break;
        case 4:
            r = vInc; g = vMin; b = v;
            break;
        case 5:
            r = v; g = vMin; b = vDec;
            break;
        default:
            throw new RuntimeException();
        }
        return new double[] {r, g, b};
    }

    /** Update HSV to grayscale */
    public void grayscale() {
        double[] rgb = toColorDouble();
        double r = rgb[0] * 0.2126;
        double g = rgb[1] * 0.7152;
        double b = rgb[2] * 0.0722;
        fromColorDouble(r, g, b);
    }

    private final void fix() {
        if (h < 0) {
            h %= 360;
            h += 360;
        } else {
            if (h >= 360)
                h %= 360;
        }

        if (s < 0) {
            s = 0;
        } else {
            if (s > 100)
                s = 100;
        }

        if (v < 0) {
            v = 0;
        } else {
            if (v >= 100)
                v = 100;
        }

        if (a < 0) {
            a = 0;
        } else {
            if (a > 255)
                a = 255;
        }
    }

    @Override
    public int hashCode() {
        int result = 31 + a;
        long temp = Double.doubleToLongBits(h);
        result = 31 * result + (int) (temp ^ (temp >>> 32));
        temp = Double.doubleToLongBits(s);
        result = 31 * result + (int) (temp ^ (temp >>> 32));
        temp = Double.doubleToLongBits(v);
        return 31 * result + (int) (temp ^ (temp >>> 32));
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        HSV other = (HSV) obj;
        return (a == other.a) &&
               (Double.doubleToLongBits(h) == Double.doubleToLongBits(other.h)) &&
               (Double.doubleToLongBits(s) == Double.doubleToLongBits(other.s)) &&
               (Double.doubleToLongBits(v) == Double.doubleToLongBits(other.v));
    }

    @Override
    public String toString() {
        //return "HSV[h=" + h + ", s=" + s + "%, v=" + v + "%, a=" + a + "]";
        return (a == 255)
            ? String.format(Locale.US, "HSV[h=%.3f, s=%.3f%%, v=%.3f%%]", h, s, v)
            : String.format(Locale.US, "HSV[h=%.3f, s=%.3f%%, v=%.3f%%, a=%d]", h, s, v, a);
    }

/*
    // test
    public static void main(String[] args) {
        try {
            Color clrIn = new Color(0xFF985410);
            HSV hsv = new HSV(clrIn);
            Color clrOut = hsv.toColor();
            System.out.println("clrIn=" + clrIn + "  =>  " + hsv);
            System.out.println("clrOt=" + clrOut+ "  <=  HSV");
        } catch (Exception ex) {
            ex.printStackTrace(System.err);
        }
    }
*/

}
