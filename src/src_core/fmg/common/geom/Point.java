package fmg.common.geom;

import java.io.Serializable;

public class Point implements Serializable {

    private static final long serialVersionUID = 1L;

    public int x;
    public int y;

    public Point() { x = y = 0; }
    public Point(int x, int y) { this.x = x; this.y = y; }
    public Point(Point p) { this.x = p.x; this.y = p.y; }

    @Override
    public boolean equals(Object other) {
        if (this == other) return true;
        if (!(other instanceof Point))
            return false;
        Point p = (Point)other;
        return (x == p.x) && (y == p.y);
    }

    @Override
    public int hashCode() {
        int sum = x + y;
        return sum * (sum + 1) / 2 + y;
    }

    @Override
    public String toString() { return "{ x:" + x + ", y:" + y + " }"; }

}
