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

   public static class DoubleExt {
      /** Equals N digit precision */
      public static bool EqualsPrecision(this double value1, double value2, double precision = 0.00001) {
         return Math.Abs(value1 - value1) <= precision;
      }

      /// <summary> https://msdn.microsoft.com/en-us/library/ya2zha7s%28v=vs.110%29.aspx </summary>
      public static bool HasMinDiff(this double value1, double value2, int units = 1) {
         var lValue1 = BitConverter.DoubleToInt64Bits(value1);
         var lValue2 = BitConverter.DoubleToInt64Bits(value2);

         // If the signs are different, return false except for +0 and -0.
         if ((lValue1 >> 63) != (lValue2 >> 63)) {
            return value1.Equals(value2);
         }

         var diff = Math.Abs(lValue1 - lValue2);
         return (diff <= units);
      }
   }
}