package ua.ksn.geom;

public class Coord {
	public static final Coord INCORRECT_COORD = new Coord(-1, -1); // null

	public int x,y;
	public Coord() { x=y=0; }
	public Coord(int x, int y) { this.x=x; this.y=y; }
	public Coord(Coord c) { this.x=c.x; this.y=c.y; }

	public int getX() { return x; }
	public void setX(int x) { this.x = x; }
	public int getY() { return y; }
	public void setY(int y) { this.y = y; }

	@Override
    public boolean equals(Object other) {
		if (this == other)
			return true;
		if (!(other instanceof Coord))
			return false;
		return equals((Coord)other);
    }
    public boolean equals(Coord other) {
		if (null == other)
			return true;
		return (x == other.x) && (y == other.y);
    }
	@Override
    public int hashCode() {
    	int sum = x+y;
    	return sum * (sum + 1)/2 + y;
    }
	@Override
    public String toString() {
    	return getClass().getName() + "[x="+x+", y="+y+"]";
    }
}
