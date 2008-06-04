package ksn.utils.Types.GraphPrimitives;

public class RectEx extends Rect {
	// Перемещение прямоугольника по X и/или Y (без изменений размеров прямоугольника)
	public RectEx moveX(long x)         { left += x; right += x; return this; }
	public RectEx moveY(long y)         { top  += y; bottom+= y; return this; }
	public RectEx move (long x, long y) { moveX(x); return moveY(y); }
	public RectEx move (Size p)         { return move(p.cx, p.cy); }

	// Выравнивание прямоугольника (без изменений размеров прямоугольника)
	public RectEx alignLeft  (long l)         { right  += l-left  ; left   = l; return this; } // выровнять прямоугольник по левой   стороне к заданному значению
	public RectEx alignRight (long r)         { left   += r-right ; right  = r; return this; } // выровнять прямоугольник по правой  стороне к заданному значению
	public RectEx alignTop   (long t)         { bottom += t-top   ; top    = t; return this; } // выровнять прямоугольник по верхней стороне к заданному значению
	public RectEx alignBottom(long b)         { top    += b-bottom; bottom = b; return this; } // выровнять прямоугольник по нижней  стороне к заданному значению
	public RectEx alignLT    (long x, long y) { alignLeft (x); return alignTop   (y); }
	public RectEx alignRT    (long x, long y) { alignRight(x); return alignTop   (y); }
	public RectEx alignLB    (long x, long y) { alignLeft (x); return alignBottom(y); }
	public RectEx alignRB    (long x, long y) { alignRight(x); return alignBottom(y); }
	public RectEx alignLT    (Point p) { return alignLT(p.x, p.y); }
	public RectEx alignRT    (Point p) { return alignRT(p.x, p.y); }
	public RectEx alignLB    (Point p) { return alignLB(p.x, p.y); }
	public RectEx alignRB    (Point p) { return alignRB(p.x, p.y); }

	public RectEx alignCenter(Point   c)      { return alignCenter(c.x, c.y); }                                                  // совместить центр прямоугольника с заданной точкой центра
	public RectEx alignCenter(Rect    r)      { return alignCenter(r.left+((r.right-r.left)>>1), r.top+((r.bottom-r.top)>>1)); } // совместить центр прямоугольника с центром заданного прямоугольника
	public RectEx alignCenter(long x, long y) { return move(x-left-(width()>>1), y-top-(height()>>1)); }                         // совместить центр прямоугольника с заданнымм координатами

	// get/set metods
	public long   width () { return right -left; }
	public long   height() { return bottom-top ; }
	public RectEx width (long w) { right  = left+w; return this; }
	public RectEx height(long h) { bottom = top +h; return this; }

	public PointEx pointLT()               { return new PointEx(left , top   ); }
	public PointEx pointRT()               { return new PointEx(right, top   ); }
	public PointEx pointLB()               { return new PointEx(left , bottom); }
	public PointEx pointRB()               { return new PointEx(right, bottom); }
	public RectEx  pointLT(long x, long y) { left  = x; top    = y; return this; }
	public RectEx  pointRT(long x, long y) { right = x; top    = y; return this; }
	public RectEx  pointLB(long x, long y) { left  = x; bottom = y; return this; }
	public RectEx  pointRB(long x, long y) { right = x; bottom = y; return this; }
	public RectEx  pointLT(Point pLT)      { return pointLT(pLT.x, pLT.y); }
	public RectEx  pointRT(Point pRT)      { return pointRT(pRT.x, pRT.y); }
	public RectEx  pointLB(Point pLB)      { return pointLB(pLB.x, pLB.y); }
	public RectEx  pointRB(Point pRB)      { return pointRB(pRB.x, pRB.y); }

	public PointEx center()               { return new PointEx(left+(width()>>1), top+(height()>>1)); }
	public RectEx  center(Point c)        { return alignCenter( c ); } // совместить центр прямоугольника с заданной точкой центра
	public RectEx  center(Rect  r)        { return alignCenter( r ); } // совместить центр прямоугольника с центром заданного прямоугольника
	public RectEx  center(long x, long y) { return alignCenter(x,y); } // совместить центр прямоугольника с заданными координатами
	public SizeEx  size()       { return new SizeEx(width(), height()); }
	public RectEx  size(Size s) { width(s.cx); return height(s.cy); }

	public Size   getSize  () {return size();}
	public SizeEx getSizeEx() {return size();}

	// constructors
	public RectEx(Point pLT, Point pRB)           { super(pLT.x, pLT.y, pRB.x, pRB.y); } // left=pLT.x  ; top=pLT.y ; right=pRB.x   ; bottom=pRB.y   ; }
	public RectEx(Rect r)                         { super(r); }                          // left=r.left ; top=r.top ; right=r.right ; bottom=r.bottom; }
	public RectEx(long l, long t, long r, long b) { super(l, t, r, b); }                 // left=l      ; top=t     ; right=r       ; bottom=b       ; }
	public RectEx(Size size)                      { super(0, 0, size.cx, size.cy); }     // left=         top=0     ; right=size.cx ; bottom=size.cy ; }
	public RectEx(long width, long height)        { super(0, 0, width, height); }        // left=         top=0     ; right=width   ; bottom=height  ; }
	public RectEx()                               { super(); }                           // left=         top=        right=          bottom=0       ; }


//	public static void main(String[] args) throws Exception { // test
//		RectEx rc1 = new RectEx(4,5,10,9);
//		System.out.println("rc1                    : "+ rc1);
//		System.out.println("сenter()               : "+ rc1.center());
//		System.out.println("alignCenter(Point     ): "+ rc1.alignCenter(new Point(2,3)));
//		System.out.println("alignCenter(Rect      ): "+ rc1.alignCenter(new Rect(1,1,3,5)));
//		System.out.println("alignCenter(long, long): "+ rc1.alignCenter(2, 3));
//		System.out.println("сenter()               : "+ rc1.center());
//	}
}