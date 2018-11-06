using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;

namespace fmg.common.geom {

    public class RegionDouble {

        public RegionDouble(int size) {
            var list = new List<PointDouble>(size);
            for (var i = 0; i < size; i++)
                list.Add(default(PointDouble));
            Points = list;
        }

        public List<PointDouble> Points { get; }
        public PointDouble GetPoint(int index) { return Points[index]; }
        public void SetPoint(int index, double x, double y) { Points[index] = new PointDouble(x, y); }
        public int CountPoints => Points.Count;

        public RectDouble GetBounds() {
            double minX = Points[0].X, maxX = Points[0].X;
            double minY = Points[0].Y, maxY = Points[0].Y;
            for (var i = 1; i < Points.Count; i++) {
                minX = Math.Min(minX, Points[i].X);
                maxX = Math.Max(maxX, Points[i].X);
                minY = Math.Min(minY, Points[i].Y);
                maxY = Math.Max(maxY, Points[i].Y);
            }
            return new RectDouble(minX, minY, maxX - minX, maxY - minY);
        }

        /// <summary> PointInPolygon </summary>
        public bool Contains(PointDouble point) {
            double x = point.X + 0.01;
            double y = point.Y + 0.01;
            var count = 0;
            for (var i = 0; i < Points.Count; i++) {
                var j = (i + 1) % Points.Count;
                if (Points[i].Y.HasMinDiff(Points[j].Y))
                    continue;
                if (Points[i].Y > y && Points[j].Y > y)
                    continue;
                if (Points[i].Y < y && Points[j].Y < y)
                    continue;
                if (Math.Abs(Math.Max(Points[i].Y, Points[j].Y) - y) < 0.001)
                    count++;
                else if (Math.Abs(Math.Min(Points[i].Y, Points[j].Y) - y) < 0.001)
                    continue;
                else {
                    double t = (y - Points[i].Y) / (Points[j].Y - Points[i].Y);
                    if (t > 0 && t < 1 && Points[i].X + t * (Points[j].X - Points[i].X) >= x)
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
            if (Points.Count != o.Points.Count)
                return false;
            return !Points.Where((p, i) => p != o.Points[i]).Any();
        }

        public override int GetHashCode() {
            return Points.Aggregate(0, (current, p) => current ^ p.GetHashCode());
        }

        public override string ToString() {
            var sb = new StringBuilder();
            //sb.Append(super.ToString());
            sb.Append('{');
            for (var i = 0; i < Points.Count; i++) {
                var p = Points[i];
                //sb.Append(i).Append('=');
                sb.Append(p);
                if (i != Points.Count - 1)
                    sb.Append(", ");
            }
            sb.Append('}');
            return sb.ToString();
        }
    }

    public static class RegionDoubleExt {

        public static IEnumerable<int> RegionDoubleAsXyxyxySequence(this RegionDouble region, SizeDouble offset, bool firstToLast) {
            var res = region.Points.Select(p => new[] { p.X + offset.Width, p.Y + offset.Height }).SelectMany(x => x);
            if (firstToLast) {
                // Add the first point also at the end of the array if the line should be closed.
                var p0 = region.GetPoint(0);
                res = res.Concat(new[] { p0.X + offset.Width, p0.Y + offset.Height });
            }
            return res.Select(x => (int)x);
        }

        public static IEnumerable<int> PointsDoubleAsXyxyxySequence(this IEnumerable<PointDouble> coords, bool firstToLast) {
            IEnumerable<double> res;
            if (firstToLast) {
                var points = coords as IList<PointDouble> ?? coords.ToList();
                res = points.Select(c => new[] { c.X, c.Y }).SelectMany(x => x).ToList();
                // Add the first point also at the end of the array if the line should be closed.
                res = res.Concat(new[] { points.First().X, points.First().Y });
            } else {
                res = coords.Select(c => new[] { c.X, c.Y }).SelectMany(x => x).ToList();
            }
            return res.Select(i => (int)i);
        }

    }

}
