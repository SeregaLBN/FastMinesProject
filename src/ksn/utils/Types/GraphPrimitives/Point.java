package ksn.utils.Types.GraphPrimitives;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

public class Point implements Externalizable {
	public long x;
	public long y;

	public Point() {
		this(0l, 0l);
	}
	public Point(long x, long y) {
		this.x = x;
		this.y = y;
	}
	public Point(Point p) {
		this(p.x, p.y);
	}
	@Override
	protected Object clone() throws CloneNotSupportedException {
		return new Point(this);
	}
	@Override
	public boolean equals(Object obj) {
		if (obj instanceof Point) {
			return (((Point)obj).x == x) && (((Point)obj).y == y);
		}
		return false;
	}
	@Override
	public String toString() {
		return super.toString()+"; x="+x+"; y="+y;
	}

	public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
		x = in.readLong();
		y = in.readLong();
	}
	public void writeExternal(ObjectOutput out) throws IOException {
		out.writeLong(x);
		out.writeLong(y);
	}
}
