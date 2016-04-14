package fmg.swing;

import java.awt.Dimension;
import java.awt.Polygon;
import java.awt.Rectangle;
import java.util.List;

import fmg.common.geom.PointDouble;
import fmg.common.geom.Rect;
import fmg.common.geom.RectDouble;
import fmg.common.geom.Region;
import fmg.common.geom.RegionDouble;
import fmg.common.geom.Size;
import fmg.common.geom.SizeDouble;

/**
 * Приведение типов от платформонезависемых читых Java классов fmg.common.geom.* к библиотечным SWING/AWT классам java.awt.*\java.swing.*
 */
public final class Cast {

   public static        java.awt.Point       toPoint      (fmg.common.geom.Point p) { return new        java.awt.Point      (p.x, p.y); }
   public static fmg.common.geom.Point       toPoint      (       java.awt.Point p) { return new fmg.common.geom.Point      (p.x, p.y); }
   public static fmg.common.geom.PointDouble toPointDouble(       java.awt.Point p) { return new fmg.common.geom.PointDouble(p.x, p.y); }

   public static java.awt.geom.Point2D.Double toPoint( fmg.common.geom.PointDouble p) { return new java.awt.geom.Point2D.Double(p.x, p.y); }
   public static  fmg.common.geom.PointDouble toPoint(java.awt.geom.Point2D.Double p) { return new  fmg.common.geom.PointDouble(p.x, p.y); }

   public static Rectangle  toRect      (Rect       rc) { return new Rectangle (     rc.x,      rc.y,      rc.width,      rc.height); }
   public static Rectangle  toRect      (RectDouble rc) { return new Rectangle ((int)rc.x, (int)rc.y, (int)rc.width, (int)rc.height); }
   public static Rect       toRect      (Rectangle  rc) { return new Rect      (     rc.x,      rc.y,      rc.width,      rc.height); }
   public static RectDouble toRectDouble(Rectangle  rc) { return new RectDouble(     rc.x,      rc.y,      rc.width,      rc.height); }

   public static Dimension  toSize      (SizeDouble size) { return new Dimension ((int)size.width, (int)size.height); }
   public static Dimension  toSize      (Size       size) { return new Dimension (     size.width,      size.height); }
   public static Size       toSize      (Dimension  size) { return new Size      (     size.width,      size.height); }
   public static SizeDouble toSizeDouble(Dimension  size) { return new SizeDouble(     size.width,      size.height); }

   public static Polygon   toPolygon(Region region) {
      Polygon polygon = new Polygon();
      for (int i=0; i<region.getCountPoints(); i++) {
         fmg.common.geom.Point p = region.getPoint(i);
         polygon.addPoint(p.x, p.y);
      }
      return polygon;
   }

   public static Polygon toPolygon(RegionDouble region) {
      Polygon polygon = new Polygon();
      for (int i = 0; i < region.getCountPoints(); i++) {
         fmg.common.geom.PointDouble p = region.getPoint(i);
         polygon.addPoint((int) p.x, (int) p.y);
      }
      return polygon;
   }

   public static Polygon toPolygon(List<PointDouble> region) {
      Polygon p = new Polygon();
      region.forEach(pd -> p.addPoint((int) pd.x, (int) pd.y));
      return p;
   }

   public static   java.awt.Color toColor(fmg.common.Color clr) { return new java.awt.Color( 0xFF & clr.getR()    ,  0xFF & clr.getG() ,  0xFF & clr.getB()  ,  0xFF & clr.getA() ); }
   public static fmg.common.Color toColor(java.awt.Color   clr) { return new fmg.common.Color((byte)clr.getAlpha(), (byte) clr.getRed(), (byte)clr.getGreen(), (byte)clr.getBlue()); }
}