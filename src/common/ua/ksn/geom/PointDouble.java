package ua.ksn.geom;

public class PointDouble {
	public double x,y;
	public PointDouble() { x=y=0; }
	public PointDouble(double x, double y) { this.x=x; this.y=y; }
	public PointDouble(PointDouble p) { this.x=p.x; this.y=p.y; }

	public double getX() { return x; }
	public void setX(double x) { this.x = x; }
	public double getY() { return y; }
	public void setY(double y) { this.y = y; }

	@Override
    public boolean equals(Object other) {
		if (this == other)
			return true;
		if (!(other instanceof PointDouble))
			return false;
		PointDouble p = (PointDouble)other;
		return (x == p.x) && (y == p.y);
    }
	@Override
    public int hashCode() {
        long bits = java.lang.Double.doubleToLongBits(getX());
        bits ^= java.lang.Double.doubleToLongBits(getY()) * 31;
        return (((int) bits) ^ ((int) (bits >> 32)));
    }
	@Override
    public String toString() {
    	return "[x="+x+", y="+y+"]";
    }
}
