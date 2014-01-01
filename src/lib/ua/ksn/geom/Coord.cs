namespace ua.ksn.geom {

public struct Coord {
   public static readonly Coord INCORRECT_COORD = new Coord(-1, -1); // null

   public int x, y;
   //public Coord() { x=y=0; }
   public Coord(int x, int y) { this.x = x; this.y = y; }
   public Coord(Coord c) { this.x = c.x; this.y = c.y; }

   public static bool operator !=(Coord c1, Coord c2) { return (c1.x != c2.x) || (c1.y != c2.y); }
   public static bool operator ==(Coord c1, Coord c2) { return (c1.x == c2.x) && (c1.y == c2.y); }

   public override bool Equals(object other) {
      if (!(other is Coord))
         return false;
      return this == (Coord)other;
   }
   public override int GetHashCode() {
      int sum = x + y;
      return sum * (sum + 1) / 2 + y;
   }
   public override string ToString() {
      return "[x=" + x + ", y=" + y + "]";
   }
}
}