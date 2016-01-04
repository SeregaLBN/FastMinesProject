namespace fmg.common.geom {

public struct Size {
   public int width,height;

   //public Size() { width=height=0; }
   public Size(int width, int height) { this.width = width; this.height = height; }
   public Size(Size c) { this.width = c.width; this.height = c.height; }

   public static bool operator !=(Size s1, Size s2) { return (s1.width != s2.width) || (s1.height != s2.height); }
   public static bool operator ==(Size s1, Size s2) { return (s1.width == s2.width) && (s1.height == s2.height); }

   public override bool Equals(object other) {
      if (ReferenceEquals(null, other))
         return false;
      return (other is Size) && (this == (Size)other);
   }
   public override int GetHashCode() {
      int sum = width + height;
      return sum * (sum + 1) / 2 + height;
   }
   public override string ToString() {
      return "{w:" + width + ", h:" + height + "}";
   }
}

public static class SizeExt {
#if WINDOWS_RT || WINDOWS_UWP
   public static Size ToFmSize(this Windows.Foundation.Size self) { return new Size((int)self.Width, (int)self.Height); }
   public static Windows.Foundation.Size ToWinSize(this Size self) { return new Windows.Foundation.Size { Width = self.width, Height = self.height }; }
#elif WINDOWS_FORMS
      ...
#else
      ...
#endif
}
}