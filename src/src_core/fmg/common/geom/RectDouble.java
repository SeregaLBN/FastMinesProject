package fmg.common.geom;

import java.util.Locale;

public class RectDouble {

   public double x, y, width, height;

   // Перемещение прямоугольника по X и/или Y (без изменений размеров прямоугольника)
   public RectDouble moveX(double dx)          { this.x += dx; return this; }
   public RectDouble moveY(double dy)          { this.y += dy; return this; }
   public RectDouble moveXY(double dx, double dy)  { moveX(dx); return moveY(dy); }
   public RectDouble moveXY(Size s)         { return moveXY(s.width, s.height); }
   public RectDouble moveXY(SizeDouble s)   { return moveXY(s.width, s.height); }

   // Выравнивание прямоугольника (без изменений размеров прямоугольника)
   public RectDouble alignLeft  (double l)         { this.x = l;             return this; } // выровнять прямоугольник по левой   стороне к заданному значению
   public RectDouble alignRight (double r)         { this.x = r-this.width;  return this; } // выровнять прямоугольник по правой  стороне к заданному значению
   public RectDouble alignTop   (double t)         { this.y = t;             return this; } // выровнять прямоугольник по верхней стороне к заданному значению
   public RectDouble alignBottom(double b)         { this.y = b-this.height; return this; } // выровнять прямоугольник по нижней  стороне к заданному значению
   public RectDouble alignLT    (double x, double y) { return alignLeft (x).alignTop   (y); }
   public RectDouble alignRT    (double x, double y) { return alignRight(x).alignTop   (y); }
   public RectDouble alignLB    (double x, double y) { return alignLeft (x).alignBottom(y); }
   public RectDouble alignRB    (double x, double y) { return alignRight(x).alignBottom(y); }
   public RectDouble alignLT    (PointDouble p) { return alignLT(p.x, p.y); }
   public RectDouble alignRT    (PointDouble p) { return alignRT(p.x, p.y); }
   public RectDouble alignLB    (PointDouble p) { return alignLB(p.x, p.y); }
   public RectDouble alignRB    (PointDouble p) { return alignRB(p.x, p.y); }

   public RectDouble alignCenter(PointDouble c) { return alignCenter(c.x, c.y); }   // совместить центр прямоугольника с заданной точкой центра
   public RectDouble alignCenter(RectDouble  r) { return alignCenter(r.center()); } // совместить центр прямоугольника с центром заданного прямоугольника
   public RectDouble alignCenter(double x, double y) { PointDouble c = this.center(); return moveXY(x-c.x, y-c.y); } // совместить центр прямоугольника с заданнымм координатами

   // get/set metods
   public double left  () { return x; }
   public double right () { return x+width; }
   public double top   () { return y; }
   public double bottom() { return y+height; }
   public RectDouble left  (double v) { x=v;          return this; }
   public RectDouble right (double v) { width = v-x;  return this; }
   public RectDouble top   (double v) { y=v;          return this; }
   public RectDouble bottom(double v) { height = v-y; return this; }

   public RectDouble width (double w) { this.width = w; return this;}
   public RectDouble height(double h) { this.height = h; return this;}

   public PointDouble PointLT() { return new PointDouble(this.x           , this.y); }
   public PointDouble PointRT() { return new PointDouble(this.x+this.width, this.y); }
   public PointDouble PointLB() { return new PointDouble(this.x           , this.y+this.height); }
   public PointDouble PointRB() { return new PointDouble(this.x+this.width, this.y+this.height); }
   public RectDouble PointLT(double x, double y)   { this.x = x;      this.y = y;       return this; }
   public RectDouble PointRT(double x, double y)   { width(x-this.x); this.y = y;       return this; }
   public RectDouble PointLB(double x, double y)   { this.x = x;      height(y-this.y); return this; }
   public RectDouble PointRB(double x, double y)   { width(x-this.x); height(y-this.y); return this; }
   public RectDouble PointLT(PointDouble pLT) { return PointLT(pLT.x, pLT.y); }
   public RectDouble PointRT(PointDouble pRT) { return PointRT(pRT.x, pRT.y); }
   public RectDouble PointLB(PointDouble pLB) { return PointLB(pLB.x, pLB.y); }
   public RectDouble PointRB(PointDouble pRB) { return PointRB(pRB.x, pRB.y); }

   public PointDouble center()                   { return new PointDouble(x+width/2, y+height/2); }
   public RectDouble  center(PointDouble c)      { return alignCenter( c ); } // совместить центр прямоугольника с заданной точкой центра
   public RectDouble  center(RectDouble  r)      { return alignCenter( r ); } // совместить центр прямоугольника с центром заданного прямоугольника
   public RectDouble  center(double x, double y) { return alignCenter(x,y); } // совместить центр прямоугольника с заданными координатами
   public SizeDouble  size()       { return new SizeDouble(width, height); }
   public RectDouble  size(Size s) { return width(s.width).height(s.height); }

   // constructors
   public RectDouble(PointDouble pLT, PointDouble pRB)       { this.x = pLT.x; this.y = pLT.y ; width(pRB.x-pLT.x); height(pRB.y-pLT.y); }
   public RectDouble(RectDouble r)                           { this.x = r.x; this.y = r.y ; width(r.width); height(r.height); }
   public RectDouble(double x, double y, double w, double h) { this.x = x; this.y = y; this.width = w;          this.height = h; }
   public RectDouble(SizeDouble size)                        { this.x =    this.y = 0; this.width = size.width; this.height = size.height; }
   public RectDouble(double width, double height)            { this.x =    this.y = 0; this.width = width;      this.height = height; }
   public RectDouble()                                       { this.x =    this.y = 0; this.width = 0;          this.height = 0; }

   /** Найти равномерно вписанный Rect */
   public static RectDouble CalcInnerRect(SizeDouble sizeInner, SizeDouble sizeOutward)
   {
      // Есть размер (sizeOutward) внешнего прямоугольника и
      // размер (sizeInner) прямоугольника который должен быть равномерно вписан
      // во внешний прямоугольник, т.е. кторый должен быть или увеличен или уменьшен.
      // Относительные координаты этого вписаного прямоугольника и находятся.
      double percent = Math.min(
            sizeOutward.width / sizeInner.width,
            sizeOutward.height / sizeInner.height);
      RectDouble rect = new RectDouble();
      rect.width = sizeInner.width * percent;
      rect.height = sizeInner.height * percent;
      rect.x = (sizeOutward.width-rect.width) / 2;
      rect.y = (sizeOutward.height-rect.height) / 2;
      return rect;
   }

   public static RectDouble CalcInnerRect(RectDouble rcInner, RectDouble rcOutward) {
      RectDouble rect = CalcInnerRect(rcInner.size(), rcOutward.size());
      rect.x += rcOutward.x;
      rect.y += rcOutward.y;
      return rect;
   }

   public static PointDouble getCenter(RectDouble rc) {
      return new PointDouble(rc.x + rc.width/2, rc.y + rc.height/2);
   }
   public static RectDouble setCenter(RectDouble rc, PointDouble newCenter) {
      return new RectDouble(
            newCenter.x - rc.width/2,
            newCenter.y - rc.height/2,
            rc.width, rc.height);
   }

   public boolean intersection(RectDouble rc) {
      return (x < (rc.x + rc.width )) && ((x + width ) > rc.x) &&
             (y < (rc.y + rc.height)) && ((y + height) > rc.y);
   }

   public boolean contains(PointDouble point) {
      return (point.x >= left()) && (point.x < right()) && (point.y >= top()) && (point.y < bottom());
   }


   @Override
   public int hashCode() {
      long temp = Double.doubleToLongBits(height);
      int result = 31 + (int) (temp ^ (temp >>> 32));
      temp = Double.doubleToLongBits(width);
      result = 31 * result + (int) (temp ^ (temp >>> 32));
      temp = Double.doubleToLongBits(x);
      result = 31 * result + (int) (temp ^ (temp >>> 32));
      temp = Double.doubleToLongBits(y);
      return 31 * result + (int) (temp ^ (temp >>> 32));
   }

   @Override
   public boolean equals(Object obj) {
      if (this == obj) return true;
      if (!(obj instanceof RectDouble))
         return false;
      return equals((RectDouble) obj);
   }

   public boolean equals(RectDouble other) {
      return (other != null) &&
             (Double.doubleToLongBits(height) == Double.doubleToLongBits(other.height)) &&
             (Double.doubleToLongBits(width)  == Double.doubleToLongBits(other.width)) &&
             (Double.doubleToLongBits(x)      == Double.doubleToLongBits(other.x)) &&
             (Double.doubleToLongBits(y)      == Double.doubleToLongBits(other.y));
   }

   @Override
   public String toString() { return String.format(Locale.US, "{x=%.2f, y=%.2f, w=%.2f, h=%.2f}", x, y, width, height); }

}
