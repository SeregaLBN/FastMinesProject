using System;

namespace Fmg.Common.Geom {

    public struct RectDouble {
        public double X, Y, Width, Height;

        // get/set methods
        public double Left() { return X; }
        public double Right() { return X + Width; }
        public double Top() { return Y; }
        public double Bottom() { return Y + Height; }
        public RectDouble Left(double v) { X = v; return this; }
        public RectDouble Right(double v) { Width = v - X; return this; }
        public RectDouble Top(double v) { Y = v; return this; }
        public RectDouble Bottom(double v) { Height = v - Y; return this; }

        // constructors
        //public RectDouble() { X = Y = 0; Width = 0; Height = 0; }
        public RectDouble(PointDouble pLT, PointDouble pRB) { X = pLT.X; Y = pLT.Y; Width = pRB.X - pLT.X; Height = pRB.Y - pLT.Y; }
        public RectDouble(RectDouble r) { X = r.X; Y = r.Y; Width = r.Width; Height = r.Height; }
        public RectDouble(double x, double y, double w, double h) { X = x; Y = y; Width = w; Height = h; }
        public RectDouble(SizeDouble size) { X = Y = 0; Width = size.Width; Height = size.Height; }
        public RectDouble(double width, double height) { X = Y = 0; Width = width; Height = height; }

        public bool Intersection(RectDouble rc) {
            return (X < (rc.X + rc.Width )) && ((X + Width ) > rc.X) &&
                   (Y < (rc.Y + rc.Height)) && ((Y + Height) > rc.Y);
        }

        public bool Contains(PointDouble point) {
            return (point.X >= Left()) && (point.X < Right()) && (point.Y >= Top()) && (point.Y < Bottom());
        }

        public static bool operator !=(RectDouble r1, RectDouble r2) { return !r1.X.HasMinDiff(r2.X) || !r1.Y.HasMinDiff(r2.Y) || !r1.Width.HasMinDiff(r2.Width) || !r1.Height.HasMinDiff(r2.Height); }
        public static bool operator ==(RectDouble r1, RectDouble r2) { return r1.X.HasMinDiff(r2.X) && r1.Y.HasMinDiff(r2.Y) && r1.Width.HasMinDiff(r2.Width) && r1.Height.HasMinDiff(r2.Height); }

        public override bool Equals(object other) {
            if (ReferenceEquals(null, other))
                return false;
            return (other is RectDouble) && (this == (RectDouble)other);
        }

        public override int GetHashCode() {
            var hashCode = X.GetHashCode();
            hashCode = (hashCode * 397) ^ Y.GetHashCode();
            hashCode = (hashCode * 397) ^ Width.GetHashCode();
            hashCode = (hashCode * 397) ^ Height.GetHashCode();
            return hashCode;
        }

        public override string ToString() {
            //return "{x:" + X + ", y:" + Y + ", w:" + Width + ", h:" + Height + "}";
            return string.Format("{{x:{0:0.00}, y:{1:0.00}, w:{2:0.00}, h:{3:0.00}}}", X, Y, Width, Height);
        }
    }

    public static class RectDoubleExt {
        // Перемещение прямоугольника по X и/или Y (без изменений размеров прямоугольника)
        public static RectDouble MoveX(this RectDouble self, double dx) { self.X += dx; return self; }
        public static RectDouble MoveY(this RectDouble self, double dy) { self.Y += dy; return self; }
        public static RectDouble MoveXY(this RectDouble self, double dx, double dy) { return self.MoveX(dx).MoveY(dy); }
        public static RectDouble MoveXY(this RectDouble self, SizeDouble s) { return self.MoveXY(s.Width, s.Height); }

        // Выравнивание прямоугольника (без изменений размеров прямоугольника)
        public static RectDouble AlignLeft(this RectDouble self, double l) { self.X = l; return self; } // выровнять прямоугольник по левой   стороне к заданному значению
        public static RectDouble AlignRight(this RectDouble self, double r) { self.X = r - self.Width; return self; } // выровнять прямоугольник по правой  стороне к заданному значению
        public static RectDouble AlignTop(this RectDouble self, double t) { self.Y = t; return self; } // выровнять прямоугольник по верхней стороне к заданному значению
        public static RectDouble AlignBottom(this RectDouble self, double b) { self.Y = b - self.Height; return self; } // выровнять прямоугольник по нижней  стороне к заданному значению
        public static RectDouble AlignLT(this RectDouble self, double x, double y) { return self.AlignLeft(x).AlignTop(y); }
        public static RectDouble AlignRT(this RectDouble self, double x, double y) { return self.AlignRight(x).AlignTop(y); }
        public static RectDouble AlignLB(this RectDouble self, double x, double y) { return self.AlignLeft(x).AlignBottom(y); }
        public static RectDouble AlignRB(this RectDouble self, double x, double y) { return self.AlignRight(x).AlignBottom(y); }
        public static RectDouble AlignLT(this RectDouble self, PointDouble p) { return self.AlignLT(p.X, p.Y); }
        public static RectDouble AlignRT(this RectDouble self, PointDouble p) { return self.AlignRT(p.X, p.Y); }
        public static RectDouble AlignLB(this RectDouble self, PointDouble p) { return self.AlignLB(p.X, p.Y); }
        public static RectDouble AlignRB(this RectDouble self, PointDouble p) { return self.AlignRB(p.X, p.Y); }

        public static RectDouble AlignCenter(this RectDouble self, PointDouble c) { return AlignCenter(self, c.X, c.Y); }   // совместить центр прямоугольника с заданной точкой центра
        public static RectDouble AlignCenter(this RectDouble self, RectDouble r) { return AlignCenter(self, r.Center()); } // совместить центр прямоугольника с центром заданного прямоугольника
        public static RectDouble AlignCenter(this RectDouble self, double x, double y) { var c = self.Center(); return MoveXY(self, x - c.X, y - c.Y); } // совместить центр прямоугольника с заданнымм координатами

        public static PointDouble PointLT(this RectDouble self) { return new PointDouble(self.X, self.Y); }
        public static PointDouble PointRT(this RectDouble self) { return new PointDouble(self.X + self.Width, self.Y); }
        public static PointDouble PointLB(this RectDouble self) { return new PointDouble(self.X, self.Y + self.Height); }
        public static PointDouble PointRB(this RectDouble self) { return new PointDouble(self.X + self.Width, self.Y + self.Height); }
        public static RectDouble PointLT(this RectDouble self, double x, double y) { self.X = x; self.Y = y; return self; }
        public static RectDouble PointRT(this RectDouble self, double x, double y) { self.Width = x - self.X; self.Y = y; return self; }
        public static RectDouble PointLB(this RectDouble self, double x, double y) { self.X = x; self.Height = y - self.Y; return self; }
        public static RectDouble PointRB(this RectDouble self, double x, double y) { self.Width = x - self.X; self.Height = y - self.Y; return self; }
        public static RectDouble PointLT(this RectDouble self, PointDouble pLT) { return PointLT(self, pLT.X, pLT.Y); }
        public static RectDouble PointRT(this RectDouble self, PointDouble pRT) { return PointRT(self, pRT.X, pRT.Y); }
        public static RectDouble PointLB(this RectDouble self, PointDouble pLB) { return PointLB(self, pLB.X, pLB.Y); }
        public static RectDouble PointRB(this RectDouble self, PointDouble pRB) { return PointRB(self, pRB.X, pRB.Y); }

        public static PointDouble Center(this RectDouble self) { return new PointDouble(self.X + self.Width / 2, self.Y + self.Height / 2); }
        public static RectDouble Center(this RectDouble self, PointDouble c) { return AlignCenter(self, c); } // совместить центр прямоугольника с заданной точкой центра
        public static RectDouble Center(this RectDouble self, RectDouble r) { return AlignCenter(self, r); } // совместить центр прямоугольника с центром заданного прямоугольника
        public static RectDouble Center(this RectDouble self, double x, double y) { return AlignCenter(self, x, y); } // совместить центр прямоугольника с заданными координатами
        public static SizeDouble SizeDouble(this RectDouble self) { return new SizeDouble(self.Width, self.Height); }
        public static RectDouble SizeDouble(this RectDouble self, SizeDouble s) { self.Width = s.Width; self.Height = s.Height; return self; }

        /// <summary>Найти равномерно вписанный RectDouble</summary>
        public static RectDouble CalcInnerRect(SizeDouble sizeInner, SizeDouble sizeOutward) {
            // Есть размер (sizeOutward) внешнего прямоугольника и
            // размер (sizeInner) прямоугольника который должен быть равномерно вписан
            // во внешний прямоугольник, т.е. кторый должен быть или увеличен или уменьшен.
            // Относительные координаты этого вписаного прямоугольника и находятся.
            var percent = Math.Min(
                  sizeOutward.Width / sizeInner.Width,
                  sizeOutward.Height / sizeInner.Height);
            var rect = new RectDouble {
                Width = sizeInner.Width * percent,
                Height = sizeInner.Height * percent
            };
            rect.X = (sizeOutward.Width - rect.Width) / 2;
            rect.Y = (sizeOutward.Height - rect.Height) / 2;
            return rect;
        }

        public static RectDouble CalcInnerRect(RectDouble rcInner, RectDouble rcOutward) {
            var rect = CalcInnerRect(rcInner.SizeDouble(), rcOutward.SizeDouble());
            rect.X += rcOutward.X;
            rect.Y += rcOutward.Y;
            return rect;
        }

    }

}
