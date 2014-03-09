namespace ua.ksn.geom {

public struct Point {
   public int x, y;

   //public Point() { x=y=0; }
   public Point(int x, int y) { this.x = x; this.y = y; }
   public Point(Point p) { this.x = p.x; this.y = p.y; }

   public static bool operator !=(Point p1, Point p2) { return (p1.x != p2.x) || (p1.y != p2.y); }
   public static bool operator ==(Point p1, Point p2) { return (p1.x == p2.x) && (p1.y == p2.y); }

   public override bool Equals(object other) {
      if (ReferenceEquals(null, other))
         return false;
      return (other is Point) && (this == (Point)other);
   }

   public override int GetHashCode() {
      int sum = x + y;
      return sum * (sum + 1) / 2 + y;
   }

   public override string ToString() {
      return "[x=" + x + ", y=" + y + "]";
   }

   public Point Move(Size s) { return Move(s.width, s.height); }
   public Point Move(int w, int h) {
      x += w;
      y += h;
      return this;
   }
}

   public static class PointExt {
#if WINDOWS_RT
      public static Point ToFmRect(this Windows.Foundation.Point self) { return new Point((int)self.X, (int)self.Y); }
      public static Windows.Foundation.Point ToWinPoint(this Point self) { return new Windows.Foundation.Point { X = self.x, Y = self.y }; }
#elif WINDOWS_FORMS
      ...
#endif
   }
}