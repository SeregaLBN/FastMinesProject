package ksn.utils.Types.GraphPrimitives;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

public class PointEx extends Point {
	public PointEx()               { super(); }
	public PointEx(Point p)        { super(p); }
	public PointEx(Size  p)        { super(p.cx, p.cy); }
	public PointEx(long x, long y) { super(x, y); }
	public PointEx operatorPlus (Point a ) { x+=a.x; y+=a.y; return this; }
	public PointEx operatorMinus(Point a ) { x-=a.x; y-=a.y; return this; }
	public PointEx operatorMul  (long val) { x*=val; y*=val; return this; }
	public PointEx operatorDiv  (long val) { x/=val; y/=val; return this; }
	public Size getSize() { return new Size(x, y); }
	public Rect getRect() { return new Rect(0,0,x,y); }

	@Override
	protected Object clone() throws CloneNotSupportedException {
		return new PointEx(this);
	}
	@Override
	public boolean equals(Object obj) {
		return super.equals(obj);
//		if (obj instanceof PointEx) {
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

//	public static void main(String[] args) throws Exception { // test
//    	Point s1 = new Point(10, 21);
//    	PointEx s2 = new PointEx(10, 21);
//    	System.out.println(s1.getClass().getName());
//    	System.out.println(s1.clone().getClass().getName());
//    	System.out.println(s2.getClass().getName());
//    	System.out.println(s2.clone().getClass().getName());
//
//    	System.out.println(s1);
//    	System.out.println(s2);
//    	System.out.println(s1.equals(s1));
//    	System.out.println(s1.equals(s2));
//    	System.out.println(s1.equals(s1.clone()));
//    	System.out.println(s1.equals(s2.clone()));
//    	System.out.println(s2.equals(s1));
//    	System.out.println(s2.equals(s2));
//    	System.out.println(s2.equals(s1.clone()));
//    	System.out.println(s2.equals(s2.clone()));
//    }
}
