using System;
using System.Linq;
using System.Text;

namespace ua.ksn.geom {

public class Region {
   protected readonly Point[] points;

   public Region(int size) {
      points = new Point[size];
      for (int i=0; i < size; i++)
         points[i] = new Point();
   }

   public Point getPoint(int index) { return points[index]; }
   public void setPoint(int index, int x, int y) { points[index].x = x; points[index].y = y; }

   public int CountPoints { get { return points.Length; } }

   public Rect getBounds() {
      int minX = points[0].x, maxX = points[0].x;
      int minY = points[0].y, maxY = points[0].y;
      for (int i=1; i < points.Length; i++) {
         minX = Math.Min(minX, points[i].x);
         maxX = Math.Max(maxX, points[i].x);
         minY = Math.Min(minY, points[i].y);
         maxY = Math.Max(maxY, points[i].y);
      }
      return new Rect(minX, minY, maxX - minX, maxY - minY);
   }

   /// <summary>
   /// PointInPolygon
   /// </summary>
   /// <param name="point"></param>
   /// <returns></returns>
   public bool Contains(Point point) {
      double x = point.x + 0.01;
      double y = point.y + 0.01;
      int count = 0;
      for (int i=0; i < points.Length; i++) {
         int j = (i + 1) % points.Length;
         if (points[i].y == points[j].y)
            continue;
         if (points[i].y > y && points[j].y > y)
            continue;
         if (points[i].y < y && points[j].y < y)
            continue;
         if (Math.Abs(Math.Max(points[i].y, points[j].y) - y) < 0.001)
            count++;
         else
            if (Math.Abs(Math.Min(points[i].y, points[j].y) - y) < 0.001)
               continue;
            else {
               double t = (y - points[i].y) / (points[j].y - points[i].y);
               if (t > 0 && t < 1 && points[i].x + t * (points[j].x - points[i].x) >= x)
                  count++;
            }
      }
      return ((count & 1) == 1);
   }

   public override bool Equals(Object other) {
      if (ReferenceEquals(this, other))
         return true;
      if (ReferenceEquals(null, other))
         return false;
      var o = other as Region;
      if (o == null)
         return false;
      if (points.Length != o.points.Length)
         return false;
      return !points.Where((p, i) => p != o.points[i]).Any();
   }

   public override int GetHashCode() {
      int h = 0;
      foreach (Point p in points)
         h ^= p.GetHashCode();
      return h;
   }

   public override String ToString() {
      var sb = new StringBuilder();
      //sb.Append(super.ToString());
      sb.Append('{');
      for (int i=0; i < points.Length; i++) {
         Point p = points[i];
         //sb.Append(i).Append('=');
         sb.Append(p.ToString());
         if (i != points.Length - 1)
            sb.Append("; ");
      }
      sb.Append('}');
      return sb.ToString();
   }
}
}