package fmg.swing.utils;

import java.awt.GraphicsConfiguration;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.geom.AffineTransform;
import java.util.List;

import fmg.common.Logger;

/** Приведение типов от платформо-независимых чистых Java классов fmg.common.geom.* к библиотечным SWING/AWT классам java.awt.*\java.swing.* */
public final class Cast {

    public static        java.awt.Point       toPoint      (fmg.common.geom.Point p) { return new        java.awt.Point      (p.x, p.y); }
    public static fmg.common.geom.Point       toPoint      (       java.awt.Point p) { return new fmg.common.geom.Point      (p.x, p.y); }
    public static fmg.common.geom.PointDouble toPointDouble(       java.awt.Point p) { return new fmg.common.geom.PointDouble(p.x, p.y); }

    public static java.awt.geom.Point2D.Double toPoint( fmg.common.geom.PointDouble p) { return new java.awt.geom.Point2D.Double(p.x, p.y); }
    public static  fmg.common.geom.PointDouble toPoint(java.awt.geom.Point2D.Double p) { return new  fmg.common.geom.PointDouble(p.x, p.y); }

    public static java.awt.Rectangle         toRect      (fmg.common.geom.Rect       rc) { return new java.awt.Rectangle        (     rc.x,      rc.y,      rc.width,      rc.height); }
    public static java.awt.Rectangle         toRect      (fmg.common.geom.RectDouble rc) { return new java.awt.Rectangle        ((int)rc.x, (int)rc.y, (int)rc.width, (int)rc.height); }
    public static fmg.common.geom.Rect       toRect      (java.awt.Rectangle         rc) { return new fmg.common.geom.Rect      (     rc.x,      rc.y,      rc.width,      rc.height); }
    public static fmg.common.geom.RectDouble toRectDouble(java.awt.Rectangle         rc) { return new fmg.common.geom.RectDouble(     rc.x,      rc.y,      rc.width,      rc.height); }

    public static java.awt.Dimension         toSize      (fmg.common.geom.SizeDouble size) { return new java.awt.Dimension        ((int)size.width, (int)size.height); }
    public static java.awt.Dimension         toSize      (fmg.common.geom.Size       size) { return new java.awt.Dimension        (     size.width,      size.height); }
    public static fmg.common.geom.Size       toSize      (java.awt.Dimension         size) { return new fmg.common.geom.Size      (     size.width,      size.height); }
    public static fmg.common.geom.SizeDouble toSizeDouble(java.awt.Dimension         size) { return new fmg.common.geom.SizeDouble(     size.width,      size.height); }

    public static java.awt.Polygon toPolygon(fmg.common.geom.Region region) {
        java.awt.Polygon polygon = new java.awt.Polygon();
        for (int i=0; i<region.getCountPoints(); i++) {
            fmg.common.geom.Point p = region.getPoint(i);
            polygon.addPoint(p.x, p.y);
        }
        return polygon;
    }

    public static java.awt.Polygon toPolygon(fmg.common.geom.RegionDouble region) {
        java.awt.Polygon polygon = new java.awt.Polygon();
        for (int i = 0; i < region.getCountPoints(); i++) {
            fmg.common.geom.PointDouble p = region.getPoint(i);
            polygon.addPoint((int) p.x, (int) p.y);
        }
        return polygon;
    }

    public static java.awt.Polygon toPolygon(List<fmg.common.geom.PointDouble> region) {
        java.awt.Polygon p = new java.awt.Polygon();
        region.forEach(pd -> p.addPoint((int) pd.x, (int) pd.y));
        return p;
    }

    public static   java.awt.Color toColor(fmg.common.Color clr) { return new java.awt.Color( clr.getR(), clr.getG(), clr.getB(), clr.getA()); }
    public static fmg.common.Color toColor(java.awt.Color   clr) { return new fmg.common.Color(clr.getAlpha(), clr.getRed(), clr.getGreen(), clr.getBlue()); }

    private static final double DISPLAY_DENSITY;
    static {
        double scale = 1; // 100%
        try {
            GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
            GraphicsDevice gd = ge.getDefaultScreenDevice();
            GraphicsConfiguration gc = gd.getDefaultConfiguration();
            AffineTransform at = gc.getDefaultTransform();
            scale = at.getScaleX();
        } catch (Throwable ex) {
            Logger.error("Can`t find display scale", ex);
        }

        double logicalDpi = scale * 96;
        double density = logicalDpi / 72;
        Logger.info("Curent display density: " + density);

        DISPLAY_DENSITY = density;
    }

    /** Pixels to DPI */
    public static double pxToDp(double px) {
        return px / DISPLAY_DENSITY;
    }

    /** DPI to pixels */
    public static double dpToPx(double dp) {
        return dp * DISPLAY_DENSITY;
    }

}
