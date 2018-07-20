namespace fmg.common.geom {

   public struct Point {
      public int X, Y;

      //public Point() { X=Y=0; }
      public Point(int x, int y) { X = x; Y = y; }
      public Point(Point p) { X = p.X; Y = p.Y; }

      public static bool operator !=(Point p1, Point p2) { return (p1.X != p2.X) || (p1.Y != p2.Y); }
      public static bool operator ==(Point p1, Point p2) { return (p1.X == p2.X) && (p1.Y == p2.Y); }

      public override bool Equals(object other) {
         if (ReferenceEquals(null, other))
            return false;
         return (other is Point) && (this == (Point)other);
      }

      public override int GetHashCode() {
         int sum = X + Y;
         return sum * (sum + 1) / 2 + Y;
      }

      public override string ToString() {
         return "{x:" + X + ", y:" + Y + "}";
      }

      public Point Move(Size s) { return Move(s.Width, s.Height); }
      public Point Move(int w, int h) {
         X += w;
         Y += h;
         return this;
      }

   }

}
