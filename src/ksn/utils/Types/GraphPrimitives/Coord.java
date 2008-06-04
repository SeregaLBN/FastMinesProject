package ksn.utils.Types.GraphPrimitives;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

public class Coord implements Externalizable {
	public short X;
	public short Y;

	public Coord() {
		this((short)0, (short)0);
	}
	public Coord(short X, short Y) {
		this.X = X;
		this.Y = Y;
	}
	public Coord(Coord p) {
		this(p.X, p.Y);
	}
	@Override
	protected Object clone() throws CloneNotSupportedException {
		return new Coord(this);
	}
	@Override
	public boolean equals(Object obj) {
		if (obj instanceof Coord) {
			return (((Coord)obj).X == X) && (((Coord)obj).Y == Y);
		}
		return false;
	}
	@Override
	public String toString() {
		return super.toString()+"; X="+X+"; Y="+Y;
	}

	public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
		X = in.readShort();
		Y = in.readShort();
	}
	public void writeExternal(ObjectOutput out) throws IOException {
		out.writeShort(X);
		out.writeShort(Y);
	}
}
