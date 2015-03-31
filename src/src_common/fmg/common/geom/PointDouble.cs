using System;

namespace ua.ksn.geom {

public struct PointDouble {
   public double x, y;

   //public PointDouble() { x=y=0; }
   public PointDouble(double x, double y) { this.x = x; this.y = y; }
   public PointDouble(PointDouble p) { this.x = p.x; this.y = p.y; }

   public static bool operator !=(PointDouble p1, PointDouble p2) { return (p1.x != p2.x) || (p1.y != p2.y); }
   public static bool operator ==(PointDouble p1, PointDouble p2) { return (p1.x == p2.x) && (p1.y == p2.y); }

   public override bool Equals(object other) {
      if (ReferenceEquals(null, other))
         return false;
      return (other is PointDouble) && (this == (PointDouble)other);
   }
   public override int GetHashCode() {
      long bits = BitConverter.DoubleToInt64Bits(x);
      bits ^= BitConverter.DoubleToInt64Bits(y) * 31;
      return (((int)bits) ^ ((int)(bits >> 32)));
   }

   public override string ToString() {
      return "[x=" + x + ", y=" + y + "]";
   }
}
}