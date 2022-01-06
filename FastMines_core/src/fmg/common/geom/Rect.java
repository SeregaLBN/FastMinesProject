package fmg.common.geom;

public class Rect {

    public int x, y, width, height;

    // Перемещение прямоугольника по X и/или Y (без изменений размеров прямоугольника)
    public Rect moveX(int dx)          { this.x += dx; return this; }
    public Rect moveY(int dy)          { this.y += dy; return this; }
    public Rect moveXY(int dx, int dy) { moveX(dx); return moveY(dy); }
    public Rect moveXY(Size s)    { return moveXY(s.width, s.height); }

    // Выравнивание прямоугольника (без изменений размеров прямоугольника)
    public Rect alignLeft  (int l)         { this.x = l;             return this; } // выровнять прямоугольник по левой   стороне к заданному значению
    public Rect alignRight (int r)         { this.x = r-this.width;  return this; } // выровнять прямоугольник по правой  стороне к заданному значению
    public Rect alignTop   (int t)         { this.y = t;             return this; } // выровнять прямоугольник по верхней стороне к заданному значению
    public Rect alignBottom(int b)         { this.y = b-this.height; return this; } // выровнять прямоугольник по нижней  стороне к заданному значению
    public Rect alignLT    (int x, int y) { alignLeft (x); return alignTop   (y); }
    public Rect alignRT    (int x, int y) { alignRight(x); return alignTop   (y); }
    public Rect alignLB    (int x, int y) { alignLeft (x); return alignBottom(y); }
    public Rect alignRB    (int x, int y) { alignRight(x); return alignBottom(y); }
    public Rect alignLT    (Point p) { return alignLT(p.x, p.y); }
    public Rect alignRT    (Point p) { return alignRT(p.x, p.y); }
    public Rect alignLB    (Point p) { return alignLB(p.x, p.y); }
    public Rect alignRB    (Point p) { return alignRB(p.x, p.y); }

    public Rect alignCenter(Point c) { return alignCenter(c.x, c.y); }   // совместить центр прямоугольника с заданной точкой центра
    public Rect alignCenter(Rect  r) { return alignCenter(r.center()); } // совместить центр прямоугольника с центром заданного прямоугольника
    public Rect alignCenter(int x, int y) { Point c = this.center(); return moveXY(x-c.x, y-c.y); } // совместить центр прямоугольника с заданнымм координатами

    // get/set metods
    public int left  () { return x; }
    public int right () { return x+width; }
    public int top   () { return y; }
    public int bottom() { return y+height; }
    public Rect left  (int v) { x=v;          return this; }
    public Rect right (int v) { width = v-x;  return this; }
    public Rect top   (int v) { y=v;          return this; }
    public Rect bottom(int v) { height = v-y; return this; }

    public Rect width (int w) { this.width = w; return this;}
    public Rect height(int h) { this.height = h; return this;}

    public Point PointLT() { return new Point(this.x           , this.y); }
    public Point PointRT() { return new Point(this.x+this.width, this.y); }
    public Point PointLB() { return new Point(this.x           , this.y+this.height); }
    public Point PointRB() { return new Point(this.x+this.width, this.y+this.height); }
    public Rect PointLT(int x, int y)   { this.x = x;      this.y = y;       return this; }
    public Rect PointRT(int x, int y)   { width(x-this.x); this.y = y;       return this; }
    public Rect PointLB(int x, int y)   { this.x = x;      height(y-this.y); return this; }
    public Rect PointRB(int x, int y)   { width(x-this.x); height(y-this.y); return this; }
    public Rect PointLT(Point pLT) { return PointLT(pLT.x, pLT.y); }
    public Rect PointRT(Point pRT) { return PointRT(pRT.x, pRT.y); }
    public Rect PointLB(Point pLB) { return PointLB(pLB.x, pLB.y); }
    public Rect PointRB(Point pRB) { return PointRB(pRB.x, pRB.y); }

    public Point center()             { return new Point(x+(width>>>1), y+(height>>>1)); }
    public Rect  center(Point c)      { return alignCenter( c ); } // совместить центр прямоугольника с заданной точкой центра
    public Rect  center(Rect  r)      { return alignCenter( r ); } // совместить центр прямоугольника с центром заданного прямоугольника
    public Rect  center(int x, int y) { return alignCenter(x,y); } // совместить центр прямоугольника с заданными координатами
    public Size  size()            { return new Size(width, height); }
    public Rect  size(Size s) { width(s.width); return height(s.height); }

    public Size toSize() { return size(); }

    // constructors
    public Rect(Point pLT, Point pRB) { this.x = pLT.x; this.y = pLT.y ; width(pRB.x-pLT.x); height(pRB.y-pLT.y); }
    public Rect(Rect r)               { this.x = r.x; this.y = r.y ; width(r.width); height(r.height); }
    public Rect(int x, int y, int w, int h) { this.x = x; this.y = y; this.width = w;          this.height = h; }
    public Rect(Size size)                  { this.x =    this.y = 0; this.width = size.width; this.height = size.height; }
    public Rect(int width, int height)      { this.x =    this.y = 0; this.width = width;      this.height = height; }
    public Rect()                           { this.x =    this.y = 0; this.width = 0;          this.height = 0; }

    /** Найти равномерно вписанный Rect */
    public static Rect CalcInnerRect(Size sizeInner, Size sizeOutward) {
        // Есть размер (sizeOutward) внешнего прямоугольника и
        // размер (sizeInner) прямоугольника который должен быть равномерно вписан
        // во внешний прямоугольник, т.е. кторый должен быть или увеличен или уменьшен.
        // Относительные координаты этого вписаного прямоугольника и находятся.
        float percent = java.lang.Math.min(
              (float)sizeOutward.width / sizeInner.width,
              (float)sizeOutward.height / sizeInner.height);
        Rect rect = new Rect();
        rect.width = (int) (sizeInner.width * percent);
        rect.height = (int) (sizeInner.height * percent);
        rect.x = (sizeOutward.width-rect.width)>>1;
        rect.y = (sizeOutward.height-rect.height)>>1;
       return rect;
    }

    public static Rect CalcInnerRect(Rect rcInner, Rect rcOutward) {
        Rect rect = CalcInnerRect(rcInner.size(), rcOutward.size());
        rect.x += rcOutward.x;
        rect.y += rcOutward.y;
        return rect;
    }

    public static Point getCenter(Rect rc) {
        return new Point(rc.x+(rc.width>>1), rc.y+(rc.height>>1));
    }
    public static Rect setCenter(Rect rc, Point newCenter) {
        return new Rect(
                newCenter.x - (rc.width>>1),
                newCenter.y - (rc.height>>1),
                rc.width, rc.height);
    }

    public boolean intersection(Rect rc) {
        return (x < (rc.x + rc.width )) && ((x + width ) > rc.x) &&
               (y < (rc.y + rc.height)) && ((y + height) > rc.y);
    }

    public boolean contains(Point point) {
        return (point.x >= left()) && (point.x < right()) && (point.y >= top()) && (point.y < bottom());
    }

    @Override
    public int hashCode() {
        int result = 31 + height;
        result = 31 * result + width;
        result = 31 * result + x;
        return 31 * result + y;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof Rect))
            return false;
        return equals((Rect) obj);
    }

    public boolean equals(Rect other) {
        return (other != null) &&
               (height == other.height) &&
               (width == other.width) &&
               (x == other.x) &&
               (y == other.y);
    }

    @Override
    public String toString() { return "{ x:" + x + ", y:" + y + ", w:" + width + ", h:" + height + " }"; }

}
