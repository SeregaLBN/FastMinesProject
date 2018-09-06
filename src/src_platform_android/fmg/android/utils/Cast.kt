package fmg.android.utils;

import java.util.List;

/**
 * Приведение типов от платформо-независимых чистых Java классов fmg.common.geom.* к библиотечным android классам
 */
public final class Cast {

   public static android.graphics.Point       toPoint      ( fmg.common.geom.Point       p) { return new android.graphics.Point      (       p.x,        p.y); }
   public static android.graphics.PointF      toPoint      ( fmg.common.geom.PointDouble p) { return new android.graphics.PointF     ((float)p.x, (float)p.y); }
   public static  fmg.common.geom.Point       toPoint      (android.graphics.Point       p) { return new  fmg.common.geom.Point      (       p.x,        p.y); }
   public static  fmg.common.geom.PointDouble toPointDouble(android.graphics.PointF      p) { return new  fmg.common.geom.PointDouble(       p.x,        p.y); }

   public static android.graphics.Rect        toRect      (fmg.common.geom.Rect         rc) { return new android.graphics.Rect      (       rc.x   ,        rc.y  ,        rc.right(),        rc.bottom()); }
   public static android.graphics.RectF       toRect      (fmg.common.geom.RectDouble   rc) { return new android.graphics.RectF     ((float)rc.x   , (float)rc.y  , (float)rc.right(), (float)rc.bottom()); }
   public static  fmg.common.geom.Rect        toRect      (android.graphics.Rect        rc) { return new  fmg.common.geom.Rect      (       rc.left,        rc.top,        rc.width(),        rc.height()); }
   public static  fmg.common.geom.RectDouble  toRectDouble(android.graphics.Rect        rc) { return new  fmg.common.geom.RectDouble(       rc.left,        rc.top,        rc.width(),        rc.height()); }
   public static  fmg.common.geom.RectDouble  toRectDouble(android.graphics.RectF       rc) { return new  fmg.common.geom.RectDouble(       rc.left,        rc.top,        rc.width(),        rc.height()); }

   public static    android.util.Size         toSize      (fmg.common.geom.Size       size) { return new    android.util.Size      (       size.width     ,        size.height); }
   public static    android.util.SizeF        toSize      (fmg.common.geom.SizeDouble size) { return new    android.util.SizeF     ((float)size.width     , (float)size.height); }
   public static fmg.common.geom.Size         toSize      (   android.util.Size       size) { return new fmg.common.geom.Size      (       size.getWidth(),        size.getHeight()); }
   public static fmg.common.geom.SizeDouble   toSizeDouble(   android.util.SizeF      size) { return new fmg.common.geom.SizeDouble(       size.getWidth(),        size.getHeight()); }

   public static android.graphics.Path toPolygon(fmg.common.geom.RegionDouble region) {
      android.graphics.Path p = new android.graphics.Path();
      fmg.common.geom.PointDouble dot = region.getPoint(0);
      p.moveTo((float)dot.x, (float)dot.y);
      for (int i=1; i<region.getCountPoints(); ++i) {
         dot = region.getPoint(i);
         p.lineTo((float)dot.x, (float)dot.y);
      }
      p.close();
      return p;
   }

   public static android.graphics.Path toPolygon(List<fmg.common.geom.PointDouble> region) {
      android.graphics.Path p = new android.graphics.Path();
      fmg.common.geom.PointDouble dot = region.get(0);
      p.moveTo((float)dot.x, (float)dot.y);
      for (int i=1; i<region.size(); ++i) {
         dot = region.get(i);
         p.lineTo((float)dot.x, (float)dot.y);
      }
      p.close();
      return p;
   }

   public static              int toColor(fmg.common.Color clr) { return (clr.getA() & 0xFF) << 24 | (clr.getR() & 0xFF) << 16 | (clr.getG() & 0xFF) << 8 | (clr.getB() & 0xFF); }
   public static fmg.common.Color toColor(             int clr) { return new fmg.common.Color(clr); }

}
