using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;

namespace fmg.common.geom {

public class Region {
   private readonly Point[] _points;

   public Region(int size) {
      _points = new Point[size];
      for (int i = 0; i < size; i++)
         _points[i] = new Point();
   }

   public IEnumerable<Point> Points => _points;

   public Point getPoint(int index) {
      return _points[index];
   }

   public void setPoint(int index, int x, int y) {
      _points[index].x = x;
      _points[index].y = y;
   }

   public int CountPoints { get { return _points.Length; } }

   public Rect getBounds() {
      int minX = _points[0].x, maxX = _points[0].x;
      int minY = _points[0].y, maxY = _points[0].y;
      for (int i = 1; i < _points.Length; i++) {
         minX = Math.Min(minX, _points[i].x);
         maxX = Math.Max(maxX, _points[i].x);
         minY = Math.Min(minY, _points[i].y);
         maxY = Math.Max(maxY, _points[i].y);
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
      for (int i = 0; i < _points.Length; i++) {
         int j = (i + 1)%_points.Length;
         if (_points[i].y == _points[j].y)
            continue;
         if (_points[i].y > y && _points[j].y > y)
            continue;
         if (_points[i].y < y && _points[j].y < y)
            continue;
         if (Math.Abs(Math.Max(_points[i].y, _points[j].y) - y) < 0.001)
            count++;
         else if (Math.Abs(Math.Min(_points[i].y, _points[j].y) - y) < 0.001)
            continue;
         else {
            double t = (y - _points[i].y)/(_points[j].y - _points[i].y);
            if (t > 0 && t < 1 && _points[i].x + t*(_points[j].x - _points[i].x) >= x)
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
      if (_points.Length != o._points.Length)
         return false;
      return !_points.Where((p, i) => p != o._points[i]).Any();
   }

   public override int GetHashCode() {
      int h = 0;
      foreach (Point p in _points)
         h ^= p.GetHashCode();
      return h;
   }

   public override String ToString() {
      var sb = new StringBuilder();
      //sb.Append(super.ToString());
      sb.Append('{');
      for (int i = 0; i < _points.Length; i++) {
         Point p = _points[i];
         //sb.Append(i).Append('=');
         sb.Append(p.ToString());
         if (i != _points.Length - 1)
            sb.Append("; ");
      }
      sb.Append('}');
      return sb.ToString();
   }
}

public static class RegionExt {

   public static IEnumerable<int> RegionAsXyxyxySequence(this Region region, Bound padding, bool firstToLast) {
      var res = region.Points.Select(p => new[] { p.x + padding.Left, p.y + padding.Top }).SelectMany(x => x);
      if (firstToLast) {
         // Add the first point also at the end of the array if the line should be closed.
         var p0 = region.getPoint(0);
         res = res.Concat(new[] { p0.x + padding.Left, p0.y + padding.Top });
      }
      return res;
   }

   public static IEnumerable<int> PointsAsXyxyxySequence(this IEnumerable<PointDouble> coords, bool firstToLast) {
      IEnumerable<double> res;
      if (firstToLast) {
         var points = coords as IList<PointDouble> ?? coords.ToList();
         res = points.Select(c => new[] {c.x, c.y}).SelectMany(x => x).ToList();
         // Add the first point also at the end of the array if the line should be closed.
         res = res.Concat(new[] {points.First().x, points.First().y});
      } else {
         res = coords.Select(c => new[] {c.x, c.y}).SelectMany(x => x).ToList();
      }
      return res.Select(i => (int) i);
   }

}
}