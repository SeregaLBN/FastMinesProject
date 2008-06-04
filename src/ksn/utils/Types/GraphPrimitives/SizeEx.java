package ksn.utils.Types.GraphPrimitives;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

public class SizeEx extends Size {
	SizeEx()                 { super(); }
	SizeEx(Size  p)          { super(p); }
	SizeEx(Point p)          { super(p.x, p.y); }
	SizeEx(Rect  p)          { super(p.right - p.left, p.bottom-p.top); }
	SizeEx(long nX, long nY) { super(nX, nY); }
	SizeEx operatorPlus (Size a) {cx+=a.cx; cy+=a.cy; return this;}
	SizeEx operatorMinus(Size a) {cx-=a.cx; cy-=a.cy; return this;}
	SizeEx operatorMul(long val) {cx*=val ; cy*=val ; return this;}
	SizeEx operatorDiv(long val) {cx/=val ; cy/=val ; return this;}
	Point getPoint() { return new Point((short)cx, (short)cy); }
	Rect  getRect () { return new Rect(0,0,cx,cy); }

	@Override
	protected Object clone() throws CloneNotSupportedException {
		return new SizeEx(this);
	}
	@Override
	public boolean equals(Object obj) {
		return super.equals(obj);
//		if (obj instanceof SizeEx) {
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
