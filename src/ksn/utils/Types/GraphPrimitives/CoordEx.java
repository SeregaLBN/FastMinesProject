package ksn.utils.Types.GraphPrimitives;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

public class CoordEx extends Coord {
	public CoordEx()                 { super(); }
	public CoordEx(Coord p)          { super(p); }
	public CoordEx(short X, short Y) { super(X, Y); }
	public CoordEx operatorPlus (Coord a  ) { X+=a.X; Y+=a.Y; return this; }
	public CoordEx operatorMinus(Coord a  ) { X-=a.X; Y-=a.Y; return this; }
	public CoordEx operatorMul  (short val) { X*=val; Y*=val; return this; }
	public CoordEx operatorDiv  (short val) { X/=val; Y/=val; return this; }

	@Override
	protected Object clone() throws CloneNotSupportedException {
		return new CoordEx(this);
	}
	@Override
	public boolean equals(Object obj) {
		return super.equals(obj);
//		if (obj instanceof CoordEx) {
//			return super.equals(obj);
//		}
//		return false;
	}
	@Override
	public String toString() {
		return super.toString();
	}

	@Override
	public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
		super.readExternal(in);
	}
	@Override
	public void writeExternal(ObjectOutput out) throws IOException {
		super.writeExternal(out);
	}
}
