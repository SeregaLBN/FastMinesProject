package ksn.utils.Types.GraphPrimitives;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

public class Size implements Externalizable {
	public long cx;
	public long cy;
	
	public Size() {
		this(0, 0);
	}
	public Size(long cx, long cy) {
		this.cx = cx;
		this.cy = cy;
	}
	public Size(Size size) {
		this(size.cx, size.cy);
	}

	@Override
	protected Object clone() throws CloneNotSupportedException {
		return new Size(this);
	}
	@Override
	public boolean equals(Object obj) {
		if (obj instanceof Size) {
			return (((Size)obj).cx==this.cx) && (((Size)obj).cy==this.cy);
		}
		return false;
	}
	@Override
	public String toString() {
		return super.toString()+"; cx="+cx+"; cy="+cy;
	}

	public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
		cx = in.readLong();
		cy = in.readLong();
	}
	public void writeExternal(ObjectOutput out) throws IOException {
		out.writeLong(cx);
		out.writeLong(cy);
	}
}
