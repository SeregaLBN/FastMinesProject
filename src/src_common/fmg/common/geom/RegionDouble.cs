using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;

namespace fmg.common.geom {

public class RegionDouble {
   private readonly PointDouble[] _points;

   public RegionDouble(int size) {
      _points = new PointDouble[size];
      for (var i = 0; i < size; i++)
         _points[i] = new PointDouble();
   }

   public IEnumerable<PointDouble> Points => _points;

   public PointDouble GetPoint(int index) {
      return _points[index];
   }

   public void SetPoint(int index, double x, double y) {
      _points[index].X = x;
      _points[index].Y = y;
   }
   public void SetPoint(int index, PointDouble p) {
      _points[index] = p;
   }

   public int CountPoints => _points.Length;

   public RectDouble GetBounds() {
      double minX = _points[0].X, maxX = _points[0].X;
      double minY = _points[0].Y, maxY = _points[0].Y;
      for (var i = 1; i < _points.Length; i++) {
         minX = Math.Min(minX, _points[i].X);
         maxX = Math.Max(maxX, _points[i].X);
         minY = Math.Min(minY, _points[i].Y);
         maxY = Math.Max(maxY, _points[i].Y);
      }
      return new RectDouble(minX, minY, maxX - minX, maxY - minY);
   }

   /// <summary> PointInPolygon </summary>
   public bool Contains(PointDouble point) {
      double x = point.X + 0.01;
      double y = point.Y + 0.01;
      var count = 0;
      for (var i = 0; i < _points.Length; i++) {
         var j = (i + 1)%_points.Length;
         if (_points[i].Y.HasMinDiff(_points[j].Y))
            continue;
         if (_points[i].Y > y && _points[j].Y > y)
            continue;
         if (_points[i].Y < y && _points[j].Y < y)
            continue;
         if (Math.Abs(Math.Max(_points[i].Y, _points[j].Y) - y) < 0.001)
            count++;
         else if (Math.Abs(Math.Min(_points[i].Y, _points[j].Y) - y) < 0.001)
            continue;
         else {
            double t = (y - _points[i].Y)/(_points[j].Y - _points[i].Y);
            if (t > 0 && t < 1 && _points[i].X + t*(_points[j].X - _points[i].X) >= x)
               count++;
         }
      }
      return ((count & 1) == 1);
   }

   public override bool Equals(object other) {
      if (ReferenceEquals(this, other))
         return true;
      if (ReferenceEquals(null, other))
         return false;
      var o = other as RegionDouble;
      if (o == null)
         return false;
      if (_points.Length != o._points.Length)
         return false;
      return !_points.Where((p, i) => p != o._points[i]).Any();
   }

   public override int GetHashCode() {
      return _points.Aggregate(0, (current, p) => current^p.GetHashCode());
   }

   public override string ToString() {
      var sb = new StringBuilder();
      //sb.Append(super.ToString());
      sb.Append('{');
      for (var i = 0; i < _points.Length; i++) {
         var p = _points[i];
         //sb.Append(i).Append('=');
         sb.Append(p);
         if (i != _points.Length - 1)
            sb.Append(", ");
      }
      sb.Append('}');
      return sb.ToString();
   }
}

public static class RegionDoubleExt {

   public static IEnumerable<int> RegionDoubleAsXyxyxySequence(this RegionDouble region, BoundDouble padding, bool firstToLast) {
      var res = region.Points.Select(p => new[] { p.X + padding.Left, p.Y + padding.Top }).SelectMany(x => x);
      if (firstToLast) {
         // Add the first point also at the end of the array if the line should be closed.
         var p0 = region.GetPoint(0);
         res = res.Concat(new[] { p0.X + padding.Left, p0.Y + padding.Top });
      }
      return res.Select(x => (int)x);
   }

   public static IEnumerable<int> PointsDoubleAsXyxyxySequence(this IEnumerable<PointDouble> coords, bool firstToLast) {
      IEnumerable<double> res;
      if (firstToLast) {
         var points = coords as IList<PointDouble> ?? coords.ToList();
         res = points.Select(c => new[] {c.X, c.Y}).SelectMany(x => x).ToList();
         // Add the first point also at the end of the array if the line should be closed.
         res = res.Concat(new[] {points.First().X, points.First().Y});
      } else {
         res = coords.Select(c => new[] {c.X, c.Y}).SelectMany(x => x).ToList();
      }
      return res.Select(i => (int) i);
   }

}
}