using System;

namespace fmg.common.geom {

   public struct Rect {
      public int X, Y, Width, Height;

      // get/set methods
      public int Left() { return X; }
      public int Right() { return X + Width; }
      public int Top() { return Y; }
      public int Bottom() { return Y + Height; }
      public Rect Left(int v) { X = v; return this; }
      public Rect Right(int v) { Width = v - X; return this; }
      public Rect Top(int v) { Y = v; return this; }
      public Rect Bottom(int v) { Height = v - Y; return this; }

      // constructors
      //public Rect() { X = Y = 0; Width = 0; Height = 0; }
      public Rect(Point pLT, Point pRB) { X = pLT.X; Y = pLT.Y; Width = pRB.X - pLT.X; Height = pRB.Y - pLT.Y; }
      public Rect(Rect r) { X = r.X; Y = r.Y; Width = r.Width; Height = r.Height; }
      public Rect(int x, int y, int w, int h) { X = x; Y = y; Width = w; Height = h; }
      public Rect(Size size) { X = Y = 0; Width = size.Width; Height = size.Height; }
      public Rect(int width, int height) { X = Y = 0; Width = width; Height = height; }

      public bool Intersects(Rect rc) {
         return ((X >= rc.X) || (X < rc.Right())) && ((Y >= rc.Y) || (Y < rc.Bottom()));
      }

      public bool Contains(Point point) {
         return (point.X >= Left()) && (point.X < Right()) && (point.Y >= Top()) && (point.Y < Bottom());
      }

      public static bool operator !=(Rect r1, Rect r2) { return (r1.X != r2.X) || (r1.Y != r2.Y) || (r1.Width != r2.Width) || (r1.Height != r2.Height); }
      public static bool operator ==(Rect r1, Rect r2) { return (r1.X == r2.X) && (r1.Y == r2.Y) && (r1.Width == r2.Width) && (r1.Height == r2.Height); }

      public override bool Equals(object other) {
         if (ReferenceEquals(null, other))
            return false;
         return (other is Rect) && (this == (Rect)other);
      }

      public override int GetHashCode() {
         return X ^ Y ^ Width ^ Height;
      }

      public override string ToString() {
         return "{x:" + X + ", y:" + Y + ", w:" + Width + ", h:" + Height + "}";
      }
   }

   public static class RectExt {
      // Перемещение прямоугольника по X и/или Y (без изменений размеров прямоугольника)
      public static Rect MoveX(this Rect self, int dx) { self.X += dx; return self; }
      public static Rect MoveY(this Rect self, int dy) { self.Y += dy; return self; }
      public static Rect MoveXY(this Rect self, int dx, int dy) { return self.MoveX(dx).MoveY(dy); }
      public static Rect MoveXY(this Rect self, Size s) { return self.MoveXY(s.Width, s.Height); }

      // Выравнивание прямоугольника (без изменений размеров прямоугольника)
      public static Rect AlignLeft(this Rect self, int l) { self.X = l; return self; } // выровнять прямоугольник по левой   стороне к заданному значению
      public static Rect AlignRight(this Rect self, int r) { self.X = r - self.Width; return self; } // выровнять прямоугольник по правой  стороне к заданному значению
      public static Rect AlignTop(this Rect self, int t) { self.Y = t; return self; } // выровнять прямоугольник по верхней стороне к заданному значению
      public static Rect AlignBottom(this Rect self, int b) { self.Y = b - self.Height; return self; } // выровнять прямоугольник по нижней  стороне к заданному значению
      public static Rect AlignLT(this Rect self, int x, int y) { return self.AlignLeft(x).AlignTop(y); }
      public static Rect AlignRT(this Rect self, int x, int y) { return self.AlignRight(x).AlignTop(y); }
      public static Rect AlignLB(this Rect self, int x, int y) { return self.AlignLeft(x).AlignBottom(y); }
      public static Rect AlignRB(this Rect self, int x, int y) { return self.AlignRight(x).AlignBottom(y); }
      public static Rect AlignLT(this Rect self, Point p) { return self.AlignLT(p.X, p.Y); }
      public static Rect AlignRT(this Rect self, Point p) { return self.AlignRT(p.X, p.Y); }
      public static Rect AlignLB(this Rect self, Point p) { return self.AlignLB(p.X, p.Y); }
      public static Rect AlignRB(this Rect self, Point p) { return self.AlignRB(p.X, p.Y); }

      public static Rect AlignCenter(this Rect self, Point c) { return AlignCenter(self, c.X, c.Y); }   // совместить центр прямоугольника с заданной точкой центра
      public static Rect AlignCenter(this Rect self, Rect r) { return AlignCenter(self, r.Center()); } // совместить центр прямоугольника с центром заданного прямоугольника
      public static Rect AlignCenter(this Rect self, int x, int y) { var c = self.Center(); return MoveXY(self, x - c.X, y - c.Y); } // совместить центр прямоугольника с заданнымм координатами

      public static Point PointLT(this Rect self) { return new Point(self.X, self.Y); }
      public static Point PointRT(this Rect self) { return new Point(self.X + self.Width, self.Y); }
      public static Point PointLB(this Rect self) { return new Point(self.X, self.Y + self.Height); }
      public static Point PointRB(this Rect self) { return new Point(self.X + self.Width, self.Y + self.Height); }
      public static Rect PointLT(this Rect self, int x, int y) { self.X = x; self.Y = y; return self; }
      public static Rect PointRT(this Rect self, int x, int y) { self.Width = x - self.X; self.Y = y; return self; }
      public static Rect PointLB(this Rect self, int x, int y) { self.X = x; self.Height = y - self.Y; return self; }
      public static Rect PointRB(this Rect self, int x, int y) { self.Width = x - self.X; self.Height = y - self.Y; return self; }
      public static Rect PointLT(this Rect self, Point pLT) { return PointLT(self, pLT.X, pLT.Y); }
      public static Rect PointRT(this Rect self, Point pRT) { return PointRT(self, pRT.X, pRT.Y); }
      public static Rect PointLB(this Rect self, Point pLB) { return PointLB(self, pLB.X, pLB.Y); }
      public static Rect PointRB(this Rect self, Point pRB) { return PointRB(self, pRB.X, pRB.Y); }

      public static Point Center(this Rect self) { return new Point(self.X + (self.Width >> 1), self.Y + (self.Height >> 1)); }
      public static Rect Center(this Rect self, Point c) { return AlignCenter(self, c); } // совместить центр прямоугольника с заданной точкой центра
      public static Rect Center(this Rect self, Rect r) { return AlignCenter(self, r); } // совместить центр прямоугольника с центром заданного прямоугольника
      public static Rect Center(this Rect self, int x, int y) { return AlignCenter(self, x, y); } // совместить центр прямоугольника с заданными координатами
      public static Size Size(this Rect self) { return new Size(self.Width, self.Height); }
      public static Rect Size(this Rect self, Size s) { self.Width = s.Width; self.Height = s.Height; return self; }

      /// <summary>Найти равномерно вписанный Rect</summary>
      public static Rect CalcInnerRect(Size sizeInner, Size sizeOutward) {
         // Есть размер (sizeOutward) внешнего прямоугольника и
         // размер (sizeInner) прямоугольника который должен быть равномерно вписан
         // во внешний прямоугольник, т.е. кторый должен быть или увеличен или уменьшен.
         // Относительные координаты этого вписаного прямоугольника и находятся.
         var percent = Math.Min(
               (float)sizeOutward.Width / sizeInner.Width,
               (float)sizeOutward.Height / sizeInner.Height);
         var rect = new Rect {
            Width = (int)(sizeInner.Width * percent),
            Height = (int)(sizeInner.Height * percent)
         };
         rect.X = (sizeOutward.Width - rect.Width) >> 1;
         rect.Y = (sizeOutward.Height - rect.Height) >> 1;
         return rect;
      }

      public static Rect CalcInnerRect(Rect rcInner, Rect rcOutward) {
         var rect = CalcInnerRect(rcInner.Size(), rcOutward.Size());
         rect.X += rcOutward.X;
         rect.Y += rcOutward.Y;
         return rect;
      }


#if WINDOWS_RT || WINDOWS_UWP
      public static Rect ToFmRect(this Windows.Foundation.Rect self) { return new Rect((int)self.X, (int)self.Y, (int)self.Width, (int)self.Height); }
      public static Windows.Foundation.Rect ToWinRect(this Rect self) { return new Windows.Foundation.Rect(self.X, self.Y, self.Width, self.Height); }
#elif WINDOWS_FORMS
      ...
#endif
   }
}