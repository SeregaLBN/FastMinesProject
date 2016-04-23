package fmg.common.geom;

public class Coord {
   public int x, y;

   public Coord() { }
   public Coord(int x, int y) { this.x=x; this.y=y; }
   public Coord(Coord c) { this.x=c.x; this.y=c.y; }

   @Override
   public boolean equals(Object other) {
      if (this == other) return true;
      if (!(other instanceof Coord))
         return false;
      return equals((Coord)other);
   }

   public boolean equals(Coord other) { return (other != null) && (x == other.x) && (y == other.y); }

   @Override
   public int hashCode() { return 31 * (31 + x) + y; }

   @Override
   public String toString() { return getClass().getSimpleName() + "{ x:"+x+", y:"+y+" }"; }

}
