package fmg.common.geom;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Region {

   protected final List<Point> points;

   public Region(int size) {
      List<Point> points = new ArrayList<>(size);
      for (int i=0; i<size; i++)
         points.add(new Point());
      this.points = Collections.unmodifiableList(points);
   }

   public List<Point> getPoints() { return points; }

   public Point getPoint(int index) { return points.get(index); }
   public void setPoint(int index, int x, int y) {
      Point p = points.get(index);
      p.x = x;
      p.y = y;
   }

   public int getCountPoints() { return points.size(); }

   public Rect getBounds() {
      Point p = points.get(0);
      int minX = p.x, maxX = p.x;
      int minY = p.y, maxY = p.y;
      for (int i=1; i < points.size(); ++i) {
         p = points.get(i);
         minX = Math.min(minX, p.x);
         maxX = Math.max(maxX, p.x);
         minY = Math.min(minY, p.y);
         maxY = Math.max(maxY, p.y);
      }
      return new Rect(minX, minY, maxX-minX, maxY-minY);
   }

   /** PointInPolygon */
   public boolean contains(Point point) {
      double x = point.x+0.01;
      double y = point.y+0.01;
      int count = 0;
      int len = points.size();
      for (int i=0; i < len; ++i) {
         int j = (i+1) % len;
         Point pi = points.get(i);
         Point pj = points.get(j);
         if (pi.y == pj.y) continue;
         if (pi.y > y && pj.y > y) continue;
         if (pi.y < y && pj.y < y) continue;
         if (Math.max(pi.y, pj.y) == y) count++;
         else
            if (Math.min(pi.y, pj.y) == y) continue;
            else {
               double t = (y-pi.y)/(pj.y-pi.y);
               if (t>0 && t<1 && pi.x+t*(pj.x-pi.x) >= x) count++;
            }
         }
      return ((count & 1) == 1);
   }

   @Override
   public int hashCode() { return points.hashCode(); }

   @Override
   public boolean equals(Object obj) {
      if (this == obj) return true;
      if (!(obj instanceof Region))
         return false;
      return equals((Region) obj);
   }

   public boolean equals(Region other) { return (other != null) && points.equals(other.points); }

   @Override
   public String toString() {
      StringBuffer sb = new StringBuffer();
      sb.append("{ ");
      boolean first = true;
      for (Point p : points) {
         if (!first)
            first = false;
         else
            sb.append(", ");
         sb.append(p.toString());
      }
      sb.append(" }");
      return sb.toString();
   }

   public Region moveXY(Size offset) {
      for (Point p : points) {
         p.x += offset.width;
         p.y += offset.height;
      }
      return this;
   }

   @Deprecated
   public static Region moveXY(Region self, Size offset) {
      if (offset.width == 0 && offset.height == 0)
         return self;
      int len = self.points.size();
      Region copy = new Region(len);
      for (int i=0; i<len; ++i) {
         Point p = self.points.get(i);
         int x = p.x + offset.width;
         int y = p.y + offset.height;
         copy.setPoint(i, x, y);
      }
      return copy;
   }

}
