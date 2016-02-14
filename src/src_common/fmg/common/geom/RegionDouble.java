package fmg.common.geom;

import java.util.Arrays;

public class RegionDouble {
   protected final PointDouble[] points;

   public RegionDouble(int size) {
      points = new PointDouble[size];
      for (int i=0; i<size; i++)
         points[i] = new PointDouble();
   }

   public PointDouble getPoint(int index) { return points[index]; }
   public void setPoint(int index, double x, double y) { points[index].x = x; points[index].y = y; }
   
   public int getCountPoints() { return points.length; }

   public RectDouble getBounds() {
      double minX = points[0].x, maxX = points[0].x;
      double minY = points[0].y, maxY = points[0].y;
      for (int i=1; i<points.length; i++) {
         minX = Math.min(minX, points[i].x);
         maxX = Math.max(maxX, points[i].x);
         minY = Math.min(minY, points[i].y);
         maxY = Math.max(maxY, points[i].y);
      }
      return new RectDouble(minX, minY, maxX-minX, maxY-minY);
    }

   /** PointInPolygon */
   public boolean Contains(PointDouble point) {
      double x = point.x+0.01;
      double y = point.y+0.01;
      int count = 0;
      for (int i=0; i<points.length; i++) {
         int j = (i+1)%points.length;
         if (points[i].y == points[j].y) continue;
         if (points[i].y > y && points[j].y > y) continue;
         if (points[i].y < y && points[j].y < y) continue;
         if (Math.max(points[i].y, points[j].y) == y) count++;
         else
            if (Math.min(points[i].y, points[j].y) == y) continue;
            else {
               double t = (y-points[i].y)/(points[j].y-points[i].y);
               if (t>0 && t<1 && points[i].x+t*(points[j].x-points[i].x) >= x) count++;
            }
         }
      return ((count & 1) == 1);
   }

   @Override
   public int hashCode() { return 31 + Arrays.hashCode(points); }

   @Override
   public boolean equals(Object obj) {
      if (this == obj) return true;
      if (!(obj instanceof RegionDouble))
         return false;
      return equals((RegionDouble) obj);
   }

   public boolean equals(RegionDouble other) { return (other != null) && Arrays.equals(points, other.points); }

   @Override
   public String toString() {
      StringBuffer sb = new StringBuffer();
      sb.append("{ ");
      for (int i=0; i<points.length; i++) {
         PointDouble p = points[i];
         sb.append(p.toString());
         if (i != points.length-1)
            sb.append(", ");
      }
      sb.append(" }");
      return sb.toString();
   }

   @Override
   public RegionDouble clone() {
      int cnt = getCountPoints();
      RegionDouble clon = new RegionDouble(cnt);
      for (int i=0; i<cnt; i++) {
         PointDouble p = this.getPoint(i);
         clon.setPoint(i, p.x, p.y);
      }
      return clon;
   }

   public static RegionDouble moveXY(RegionDouble self, BoundDouble padding) {
      if (padding.isEmpty())
         return self;
      RegionDouble res = self.clone();
      for (int i=0; i<res.getCountPoints(); i++) {
         res.points[i].x = res.points[i].x + padding.left;
         res.points[i].y = res.points[i].y + padding.top;
      }
      return res;
   }

}