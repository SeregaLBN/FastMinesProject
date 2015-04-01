using System;

namespace fmg.common.geom {

public struct SizeDouble {
   public double width, height;

   //public SizeDouble() { width=height=0; }
   public SizeDouble(double width, double height) { this.width = width; this.height = height; }
   public SizeDouble(SizeDouble s) { this.width = s.width; this.height = s.height; }

   public static bool operator !=(SizeDouble s1, SizeDouble s2) { return (s1.width != s2.width) || (s1.height != s2.height); }
   public static bool operator ==(SizeDouble s1, SizeDouble s2) { return (s1.width == s2.width) && (s1.height == s2.height); }

   public override bool Equals(object other) {
      if (ReferenceEquals(null, other))
         return false;
      return (other is SizeDouble) && (this == (SizeDouble)other);
   }
   public override int GetHashCode() {
      long bits = BitConverter.DoubleToInt64Bits(width);
      bits ^= BitConverter.DoubleToInt64Bits(height) * 31;
      return (((int)bits) ^ ((int)(bits >> 32)));
   }

   public override string ToString() {
      return "[width=" + width + ", height=" + height + "]";
   }
}
}