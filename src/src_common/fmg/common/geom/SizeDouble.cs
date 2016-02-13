using System;

namespace fmg.common.geom {

public struct SizeDouble {
   public double Width, Height;

   //public SizeDouble() { width=height=0; }
   public SizeDouble(double width, double height) { Width = width; Height = height; }
   public SizeDouble(SizeDouble s) { Width = s.Width; Height = s.Height; }

   public static bool operator !=(SizeDouble s1, SizeDouble s2) { return !s1.Width.HasMinDiff(s2.Width) || !s1.Height.HasMinDiff(s2.Height); }
   public static bool operator ==(SizeDouble s1, SizeDouble s2) { return  s1.Width.HasMinDiff(s2.Width) &&  s1.Height.HasMinDiff(s2.Height); }

   public override bool Equals(object other) {
      if (ReferenceEquals(null, other))
         return false;
      return (other is SizeDouble) && (this == (SizeDouble)other);
   }
   public override int GetHashCode() {
      var bits = BitConverter.DoubleToInt64Bits(Width);
      bits ^= BitConverter.DoubleToInt64Bits(Height) * 31;
      return (((int)bits) ^ ((int)(bits >> 32)));
   }

   public override string ToString() {
      return "{width:" + Width + ", height:" + Height + "}";
   }
}

   public static class SizeDoubleExt {
#if WINDOWS_RT || WINDOWS_UWP
      public static SizeDouble ToFmSizeDouble(this Windows.Foundation.Size self) { return new SizeDouble(self.Width, self.Height); }
      public static Windows.Foundation.Size ToWinSize(this SizeDouble self) { return new Windows.Foundation.Size { Width = self.Width, Height = self.Height }; }
#elif WINDOWS_FORMS
      ...
#else
      ...
#endif
   }
}