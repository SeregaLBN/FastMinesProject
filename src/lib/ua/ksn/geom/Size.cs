namespace ua.ksn.geom {

public struct Size {
   public int width,height;

   //public Size() { width=height=0; }
   public Size(int width, int height) { this.width = width; this.height = height; }
   public Size(Size c) { this.width = c.width; this.height = c.height; }

   public static bool operator !=(Size s1, Size s2) { return (s1.width != s2.width) || (s1.height != s2.height); }
   public static bool operator ==(Size s1, Size s2) { return (s1.width == s2.width) && (s1.height == s2.height); }

   public override bool Equals(object other) {
      if (!(other is Size))
         return false;
      return this == (Size)other;
   }
   public override int GetHashCode() {
      int sum = width + height;
      return sum * (sum + 1) / 2 + height;
   }
   public override string ToString() {
      return "[w=" + width + ", h=" + height + "]";
   }
}
}