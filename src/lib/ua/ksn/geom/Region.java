package ua.ksn.geom;

public class Region {
	protected final Point[] points;
	public Region(int size) {
		points = new Point[size];
		for (int i=0; i<size; i++)
			points[i] = new Point();
	}

	public Point getPoint(int index) { return points[index]; }
	public void setPoint(int index, int x, int y) { points[index].x = x; points[index].y = y; }
	
	public int getCountPoints() { return points.length; }

	public Rect getBounds() {
		int minX = points[0].x, maxX = points[0].x;
		int minY = points[0].y, maxY = points[0].y;
        for (int i=1; i<points.length; i++) {
            minX = Math.min(minX, points[i].x);
            maxX = Math.max(maxX, points[i].x);
            minY = Math.min(minY, points[i].y);
            maxY = Math.max(maxY, points[i].y);
        }
        return new Rect(minX, minY, maxX-minX, maxY-minY);
    }

	/** PointInPolygon */
	public boolean Contains(Point point) {
		double x = point.x+0.01;
		double y = point.y+0.01;
		int count = 0;
		for (int i=0; i<points.length; i++) {
			int j = (i+1)%points.length;
			if (points[i].y == points[j].y) continue;
			if (points[i].y > y && points[j].y > y) continue;
			if (points[i].y < y && points[j].y < y) continue;
			if (Math.max(points[i].y, points[j].y) == y) count++;
			else
				if (Math.min(points[i].y, points[j].y) == y) continue;
				else {
					double t = (double)(y-points[i].y)/(points[j].y-points[i].y);
					if (t>0 && t<1 && points[i].x+t*(points[j].x-points[i].x) >= x) count++;
				}
			}
		return ((count & 1) == 1);
	}

	@Override
    public boolean equals(Object other) {
		if (this == other)
			return true;
		if (!(other instanceof Region))
			return false;
		Region o = (Region)other;
		if (points.length != o.points.length)
			return false;
		for (int i=0; i<points.length; i++)
			if (!points[i].equals(o.points[i]))
				return false;
		return true;
    }
	@Override
    public int hashCode() {
    	int h = 0;
		for (Point p : points)
			h ^= p.hashCode();
		return h;
    }
	@Override
    public String toString() {
		StringBuffer sb = new StringBuffer();
		//sb.append(super.toString());
		sb.append('{');
		for (int i=0; i<points.length; i++) {
			Point p = points[i];
			//sb.append(i).append('=');
			sb.append(p.toString());
			if (i != points.length-1)
				sb.append("; ");
		}
		sb.append('}');
    	return sb.toString();
    }
}