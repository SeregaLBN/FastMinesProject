package ksn.utils.Types.GraphPrimitives;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

public class Rect implements Externalizable {
	public long left;
	public long top;
	public long right;
	public long bottom;

	public Rect() {
		this(0l, 0l, 0l, 0l);
	}
	public Rect(long left, long top, long right, long bottom) {
		this.left   = left;
		this.top    = top;
		this.right  = right;
		this.bottom = bottom;
	}
	public Rect(Rect rc) {
		this(rc.left, rc.top, rc.right, rc.bottom);
	}

	@Override
	protected Object clone() throws CloneNotSupportedException {
		return new Rect(this);
	}
	@Override
	public boolean equals(Object obj) {
		if (obj instanceof Rect) {
			return (((Rect)obj).left==left) && (((Rect)obj).top == top) && (((Rect)obj).right == right) && (((Rect)obj).bottom == bottom);
		}
		return false;
	}
	@Override
	public String toString() {
		return super.toString()+"; left="+left+"; top="+top+"; right="+right+"; bottom="+bottom;
	}

	public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
		long left   = in.readLong();
		long top    = in.readLong();
		long right  = in.readLong();
		long bottom = in.readLong();
		if (left   > right) throw new java.io.InvalidObjectException("left > right");
		if (bottom > top  ) throw new java.io.InvalidObjectException("bottom > top");
		this.left   = left;
		this.top    = top;
		this.right  = right;
		this.bottom = bottom;
	}
	public void writeExternal(ObjectOutput out) throws IOException {
		out.writeLong(left);
		out.writeLong(top);
		out.writeLong(right);
		out.writeLong(bottom);
	}
}
