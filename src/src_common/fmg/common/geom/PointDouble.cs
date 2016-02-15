using System;

namespace fmg.common.geom {

   public struct PointDouble {
      public double X, Y;

      //public PointDouble() { X=Y=0; }
      public PointDouble(double x, double y) { X = x; Y = y; }
      public PointDouble(PointDouble p) { X = p.X; Y = p.Y; }
      public PointDouble(Point p) { X = p.X; Y = p.Y; }

      public static bool operator !=(PointDouble p1, PointDouble p2) { return !p1.X.HasMinDiff(p2.X) || !p1.Y.HasMinDiff(p2.Y); }
      public static bool operator ==(PointDouble p1, PointDouble p2) { return  p1.X.HasMinDiff(p2.X) &&  p1.Y.HasMinDiff(p2.Y); }

      public override bool Equals(object other) {
         if (ReferenceEquals(null, other))
            return false;
         return (other is PointDouble) && (this == (PointDouble)other);
      }
      public override int GetHashCode() {
         var bits = BitConverter.DoubleToInt64Bits(X);
         bits ^= BitConverter.DoubleToInt64Bits(Y) * 31;
         return (((int)bits) ^ ((int)(bits >> 32)));
      }

      public override string ToString() {
         return "{x:" + X + ", y:" + Y + "}";
      }

      public PointDouble Move(SizeDouble s) { return Move(s.Width, s.Height); }
      public PointDouble Move(double w, double h) {
         X += w;
         Y += h;
         return this;
      }
   }


   public static class PointDoubleExt {
#if WINDOWS_RT || WINDOWS_UWP
      public static PointDouble ToFmRectDouble(this Windows.Foundation.Point self) { return new PointDouble(self.X, self.Y); }
      public static Windows.Foundation.Point ToWinPoint(this PointDouble self) { return new Windows.Foundation.Point { X = self.X, Y = self.Y }; }
#elif WINDOWS_FORMS
      ...
#endif
   }

}
