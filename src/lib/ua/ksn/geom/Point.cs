namespace ua.ksn.geom {

public struct Point {
   public int x,y;

   //public Point() { x=y=0; }
   public Point(int x, int y) { this.x = x; this.y = y; }
   public Point(Point p) { this.x = p.x; this.y = p.y; }

   public static bool operator !=(Point p1, Point p2) { return (p1.x != p2.x) || (p1.y != p2.y); }
   public static bool operator ==(Point p1, Point p2) { return (p1.x == p2.x) && (p1.y == p2.y); }

   public override bool Equals(object other) {
      if (!(other is Point))
         return false;
      return this == (Point)other;
   }

   public override int GetHashCode() {
      int sum = x + y;
      return sum * (sum + 1) / 2 + y;
   }

   public override string ToString() {
      return "[x=" + x + ", y=" + y + "]";
   }

#if WINDOWS_RT
   public static explicit operator Windows.Foundation.Point(Point self) { return new Windows.Foundation.Point() { X = self.x, Y = self.y }; }
#elif WINDOWS_FORMS
      ...
#endif
}
}