package fmg.common.geom;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class RegionDouble {

   protected final List<PointDouble> points;

   public RegionDouble(int size) {
      List<PointDouble> points = new ArrayList<>(size);
      for (int i=0; i<size; i++)
         points.add( new PointDouble() );
      this.points = Collections.unmodifiableList(points);
   }

   public List<PointDouble> getPoints() { return points; }

   public PointDouble getPoint(int index) { return points.get(index); }
   public void setPoint(int index, double x, double y) {
      PointDouble p = points.get(index);
      p.x = x;
      p.y = y;
   }

   public int getCountPoints() { return points.size(); }

   public RectDouble getBounds() {
      PointDouble p = points.get(0);
      double minX = p.x, maxX = p.x;
      double minY = p.y, maxY = p.y;
      for (int i=1; i<points.size(); ++i) {
         p = points.get(i);
         minX = Math.min(minX, p.x);
         maxX = Math.max(maxX, p.x);
         minY = Math.min(minY, p.y);
         maxY = Math.max(maxY, p.y);
      }
      return new RectDouble(minX, minY, maxX-minX, maxY-minY);
   }

   /** PointInPolygon */
   public boolean contains(PointDouble point) {
      double x = point.x+0.01;
      double y = point.y+0.01;
      int count = 0;
      int len = points.size();
      for (int i=0; i < len; ++i) {
         int j = (i+1) % len;
         PointDouble pi = points.get(i);
         PointDouble pj = points.get(j);
         if (pi.y == pj.y) continue;
         if ((pi.y > y) && (pj.y > y)) continue;
         if ((pi.y < y) && (pj.y < y)) continue;
         if (Math.max(pi.y, pj.y) == y) count++;
         else
            if (Math.min(pi.y, pj.y) == y) continue;
            else {
               double t = (y-pi.y)/(pj.y-pi.y);
               if ((t>0) && (t<1) && ((pi.x+(t*(pj.x-pi.x))) >= x)) count++;
            }
         }
      return ((count & 1) == 1);
   }

   @Override
   public int hashCode() { return points.hashCode(); }

   @Override
   public boolean equals(Object obj) {
      if (this == obj) return true;
      if (!(obj instanceof RegionDouble))
         return false;
      return equals((RegionDouble) obj);
   }

   public boolean equals(RegionDouble other) { return (other != null) && points.equals(other.points); }

   @Override
   public String toString() {
      StringBuffer sb = new StringBuffer();
      sb.append("{ ");
      boolean first = true;
      for (PointDouble p : points) {
         if (!first)
            first = false;
         else
            sb.append(", ");
         sb.append(p.toString());
      }
      sb.append(" }");
      return sb.toString();
   }

   public RegionDouble moveXY(SizeDouble offset) {
      for (PointDouble p : points) {
         p.x += offset.width;
         p.y += offset.height;
      }
      return this;
   }

   /** create a new region, if there an offset */
   public static RegionDouble moveXY(RegionDouble self, SizeDouble offset) {
      if (offset.width == 0 && offset.height == 0)
         return self;
      int len = self.points.size();
      RegionDouble copy = new RegionDouble(len);
      for (int i=0; i<len; ++i) {
         PointDouble p = self.points.get(i);
         double x = p.x + offset.width;
         double y = p.y + offset.height;
         copy.setPoint(i, x, y);
      }
      return copy;
   }

}
