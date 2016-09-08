package fmg.jfx;

import java.util.List;

/**
 * Приведение типов от платформонезависемых читых Java классов fmg.common.geom.* к библиотечным javafx классам
 */
public final class Cast {

   public static javafx.geometry.Point2D     toPoint      (fmg.common.geom.Point       p) { return new javafx.geometry.Point2D    (     p.x     ,      p.y); }
   public static javafx.geometry.Point2D     toPoint      (fmg.common.geom.PointDouble p) { return new javafx.geometry.Point2D    (     p.x     ,      p.y); }
 //public static fmg.common.geom.Point       toPoint      (javafx.geometry.Point2D     p) { return new fmg.common.geom.Point      ((int)p.getX(), (int)p.getY()); }
   public static fmg.common.geom.PointDouble toPointDouble(javafx.geometry.Point2D     p) { return new fmg.common.geom.PointDouble(     p.getX(),      p.getY()); }

   public static javafx.geometry.Rectangle2D toRect      (fmg.common.geom.Rect        rc) { return new javafx.geometry.Rectangle2D(     rc.x        ,      rc.y        ,      rc.width     ,      rc.height); }
   public static javafx.geometry.Rectangle2D toRect      (fmg.common.geom.RectDouble  rc) { return new javafx.geometry.Rectangle2D(     rc.x        ,      rc.y        ,      rc.width     ,      rc.height); }
 //public static fmg.common.geom.Rect        toRect      (javafx.geometry.Rectangle2D rc) { return new fmg.common.geom.Rect       ((int)rc.getMinX(), (int)rc.getMinY(), (int)rc.getWidth(), (int)rc.getHeight()); }
   public static fmg.common.geom.RectDouble  toRectDouble(javafx.geometry.Rectangle2D rc) { return new fmg.common.geom.RectDouble (     rc.getMinX(),      rc.getMinY(),      rc.getWidth(),      rc.getHeight()); }

   public static javafx.geometry.Dimension2D  toSize      (fmg.common.geom.SizeDouble  size) { return new javafx.geometry.Dimension2D(     size.width     ,      size.height); }
   public static javafx.geometry.Dimension2D  toSize      (fmg.common.geom.Size        size) { return new javafx.geometry.Dimension2D(     size.width     ,      size.height); }
 //public static fmg.common.geom.Size         toSize      (javafx.geometry.Dimension2D size) { return new fmg.common.geom.Size       ((int)size.getWidth(), (int)size.getHeight()); }
   public static fmg.common.geom.SizeDouble   toSizeDouble(javafx.geometry.Dimension2D size) { return new fmg.common.geom.SizeDouble (     size.getWidth(),      size.getHeight()); }

   public static double[] toPolygon(fmg.common.geom.Region region, boolean xCoord) {
      return region.getPoints().stream().mapToDouble(p -> xCoord ? p.x : p.y).toArray();
   }

   public static double[] toPolygon(fmg.common.geom.RegionDouble region, boolean xCoord) {
      return region.getPoints().stream().mapToDouble(p -> xCoord ? p.x : p.y).toArray();
   }

   public static double[] toPolygon(List<fmg.common.geom.PointDouble> region, boolean xCoord) {
      return region.stream().mapToDouble(p -> xCoord ? p.x : p.y).toArray();
   }

   public static javafx.scene.paint.Color toColor(        fmg.common.Color clr) { return javafx.scene.paint.Color.rgb( clr.getR(), clr.getG(), clr.getB(), clr.getA() / 255.); }
   public static         fmg.common.Color toColor(javafx.scene.paint.Color clr) { return new fmg.common.Color(
         (int)(255*clr.getOpacity()),
         (int)(255*clr.getRed()),
         (int)(255*clr.getGreen()),
         (int)(255*clr.getBlue())); }

}
