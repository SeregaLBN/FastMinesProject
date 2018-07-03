////////////////////////////////////////////////////////////////////////////////
//                               FastMines project
//                                      © Sergey Krivulya (KSerg, aka SeregaLBN)
// file name: "Quadrangle1.java"
//
// Реализация класса Quadrangle1 - четырёхугольник 120°-90°-60°-90°
// Copyright (C) 2002-2011 Sergey Krivulya
//
// This program is free software; you can redistribute it and/or
// modify it under the terms of the GNU General Public License
// as published by the Free Software Foundation; either version 2
// of the License, or (at your option) any later version.
//
// This program is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// GNU General Public License for more details.
//
// You should have received a copy of the GNU General Public License
// along with this program; if not, write to the Free Software
// Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
////////////////////////////////////////////////////////////////////////////////

using System;
using fmg.common.geom;
using System.Collections.Generic;

namespace fmg.core.mosaic.cells {

   /// <summary> Quadrangle1 - четырёхугольник 120°-90°-60°-90° </summary>
   public class Quadrangle1 : BaseCell {

      public class AttrQuadrangle1 : BaseAttribute {

         public override SizeDouble GetSize(Matrisize sizeField) {
            var a = A;
            var b = B;
            var h = H;
            var m = M;
            var result = new SizeDouble(
                  m + m*((sizeField.m+2)/3)+
                      h*((sizeField.m+1)/3)+
                      m*((sizeField.m+0)/3),
                  b + b*((sizeField.n+1)/2)+
                      a*((sizeField.n+0)/2));

            if (sizeField.m == 1)
               if ((sizeField.n & 1) == 0)
                  result.Height -= a/4;
            if (sizeField.m == 2)
               if ((sizeField.n % 4) == 0)
                  result.Height -= a/4;
            if ((sizeField.n == 1) || (sizeField.n == 2)) {
               if ((sizeField.m % 3) == 2)
                  result.Width -= m;
               if ((sizeField.m % 3) == 0)
                  result.Width -= m;
            }

            return result;
         }

         public override int GetNeighborNumber(int direction) { return 9; }
         public override int GetVertexNumber(int direction) { return 4; }
         public override double GetVertexIntersection() { return 4.25; } // (3+4+4+6)/4.
         public override Size GetDirectionSizeField() { return new Size(3, 4); }
         public override double A => Math.Sqrt(Area/SQRT3)*2;
         public double B => A / 2;
         public double H => B * SQRT3;
         public double N => A * 0.75;
         public double M => H / 2;
         public double Z => A / (1+SQRT3);
         public double Zx => Z * SQRT3/2;
         public double Zy => Z / 2;
         public override double GetSq(double borderWidth) {
            var w = borderWidth/2.0;
            return (A*SQRT3 - w*2*(1+SQRT3))/(SQRT3+2);
         }
      }

      public Quadrangle1(AttrQuadrangle1 attr, Coord coord)
         : base(attr, coord,
               (coord.y&3)*3 + (coord.x%3) // 0..11
            )
      {}

      private new AttrQuadrangle1 Attr => (AttrQuadrangle1) base.Attr;

      protected override IList<Coord> GetCoordsNeighbor() {
         var neighborCoord = new Coord[Attr.GetNeighborNumber(getDirection())];

         // определяю координаты соседей
          switch (direction) {
          case 0:
             neighborCoord[0] = new Coord(coord.x-1, coord.y-1);
             neighborCoord[1] = new Coord(coord.x  , coord.y-1);
             neighborCoord[2] = new Coord(coord.x-1, coord.y  );
             neighborCoord[3] = new Coord(coord.x+1, coord.y  );
             neighborCoord[4] = new Coord(coord.x+2, coord.y  );
             neighborCoord[5] = new Coord(coord.x-1, coord.y+1);
             neighborCoord[6] = new Coord(coord.x  , coord.y+1);
             neighborCoord[7] = new Coord(coord.x+1, coord.y+1);
             neighborCoord[8] = new Coord(coord.x+2, coord.y+1);
             break;
          case 1:
             neighborCoord[0] = new Coord(coord.x-2, coord.y-1);
             neighborCoord[1] = new Coord(coord.x-1, coord.y-1);
             neighborCoord[2] = new Coord(coord.x  , coord.y-1);
             neighborCoord[3] = new Coord(coord.x+1, coord.y-1);
             neighborCoord[4] = new Coord(coord.x-1, coord.y  );
             neighborCoord[5] = new Coord(coord.x+1, coord.y  );
             neighborCoord[6] = new Coord(coord.x-1, coord.y+1);
             neighborCoord[7] = new Coord(coord.x  , coord.y+1);
             neighborCoord[8] = new Coord(coord.x+1, coord.y+1);
             break;
          case 2:
             neighborCoord[0] = new Coord(coord.x-1, coord.y-1);
             neighborCoord[1] = new Coord(coord.x  , coord.y-1);
             neighborCoord[2] = new Coord(coord.x-2, coord.y  );
             neighborCoord[3] = new Coord(coord.x-1, coord.y  );
             neighborCoord[4] = new Coord(coord.x+1, coord.y  );
             neighborCoord[5] = new Coord(coord.x-2, coord.y+1);
             neighborCoord[6] = new Coord(coord.x-1, coord.y+1);
             neighborCoord[7] = new Coord(coord.x  , coord.y+1);
             neighborCoord[8] = new Coord(coord.x+1, coord.y+1);
             break;
          case 3:
             neighborCoord[0] = new Coord(coord.x-1, coord.y-1);
             neighborCoord[1] = new Coord(coord.x  , coord.y-1);
             neighborCoord[2] = new Coord(coord.x+1, coord.y-1);
             neighborCoord[3] = new Coord(coord.x+2, coord.y-1);
             neighborCoord[4] = new Coord(coord.x-1, coord.y  );
             neighborCoord[5] = new Coord(coord.x+1, coord.y  );
             neighborCoord[6] = new Coord(coord.x+2, coord.y  );
             neighborCoord[7] = new Coord(coord.x-1, coord.y+1);
             neighborCoord[8] = new Coord(coord.x  , coord.y+1);
             break;
          case 4:
             neighborCoord[0] = new Coord(coord.x-1, coord.y-1);
             neighborCoord[1] = new Coord(coord.x  , coord.y-1);
             neighborCoord[2] = new Coord(coord.x+1, coord.y-1);
             neighborCoord[3] = new Coord(coord.x-1, coord.y  );
             neighborCoord[4] = new Coord(coord.x+1, coord.y  );
             neighborCoord[5] = new Coord(coord.x-2, coord.y+1);
             neighborCoord[6] = new Coord(coord.x-1, coord.y+1);
             neighborCoord[7] = new Coord(coord.x  , coord.y+1);
             neighborCoord[8] = new Coord(coord.x+1, coord.y+1);
             break;
          case 5:
             neighborCoord[0] = new Coord(coord.x-2, coord.y-1);
             neighborCoord[1] = new Coord(coord.x-1, coord.y-1);
             neighborCoord[2] = new Coord(coord.x  , coord.y-1);
             neighborCoord[3] = new Coord(coord.x+1, coord.y-1);
             neighborCoord[4] = new Coord(coord.x-2, coord.y  );
             neighborCoord[5] = new Coord(coord.x-1, coord.y  );
             neighborCoord[6] = new Coord(coord.x+1, coord.y  );
             neighborCoord[7] = new Coord(coord.x-1, coord.y+1);
             neighborCoord[8] = new Coord(coord.x  , coord.y+1);
             break;
          case 6:
             neighborCoord[0] = new Coord(coord.x  , coord.y-1);
             neighborCoord[1] = new Coord(coord.x+1, coord.y-1);
             neighborCoord[2] = new Coord(coord.x-2, coord.y  );
             neighborCoord[3] = new Coord(coord.x-1, coord.y  );
             neighborCoord[4] = new Coord(coord.x+1, coord.y  );
             neighborCoord[5] = new Coord(coord.x-2, coord.y+1);
             neighborCoord[6] = new Coord(coord.x-1, coord.y+1);
             neighborCoord[7] = new Coord(coord.x  , coord.y+1);
             neighborCoord[8] = new Coord(coord.x+1, coord.y+1);
             break;
          case 7:
             neighborCoord[0] = new Coord(coord.x  , coord.y-1);
             neighborCoord[1] = new Coord(coord.x+1, coord.y-1);
             neighborCoord[2] = new Coord(coord.x-1, coord.y  );
             neighborCoord[3] = new Coord(coord.x+1, coord.y  );
             neighborCoord[4] = new Coord(coord.x+2, coord.y  );
             neighborCoord[5] = new Coord(coord.x-1, coord.y+1);
             neighborCoord[6] = new Coord(coord.x  , coord.y+1);
             neighborCoord[7] = new Coord(coord.x+1, coord.y+1);
             neighborCoord[8] = new Coord(coord.x+2, coord.y+1);
             break;
          case 8:
             neighborCoord[0] = new Coord(coord.x-1, coord.y-1);
             neighborCoord[1] = new Coord(coord.x  , coord.y-1);
             neighborCoord[2] = new Coord(coord.x+1, coord.y-1);
             neighborCoord[3] = new Coord(coord.x+2, coord.y-1);
             neighborCoord[4] = new Coord(coord.x-1, coord.y  );
             neighborCoord[5] = new Coord(coord.x+1, coord.y  );
             neighborCoord[6] = new Coord(coord.x-1, coord.y+1);
             neighborCoord[7] = new Coord(coord.x  , coord.y+1);
             neighborCoord[8] = new Coord(coord.x+1, coord.y+1);
             break;
          case 9:
             neighborCoord[0] = new Coord(coord.x-2, coord.y-1);
             neighborCoord[1] = new Coord(coord.x-1, coord.y-1);
             neighborCoord[2] = new Coord(coord.x  , coord.y-1);
             neighborCoord[3] = new Coord(coord.x+1, coord.y-1);
             neighborCoord[4] = new Coord(coord.x-2, coord.y  );
             neighborCoord[5] = new Coord(coord.x-1, coord.y  );
             neighborCoord[6] = new Coord(coord.x+1, coord.y  );
             neighborCoord[7] = new Coord(coord.x  , coord.y+1);
             neighborCoord[8] = new Coord(coord.x+1, coord.y+1);
             break;
          case 10:
             neighborCoord[0] = new Coord(coord.x-1, coord.y-1);
             neighborCoord[1] = new Coord(coord.x  , coord.y-1);
             neighborCoord[2] = new Coord(coord.x+1, coord.y-1);
             neighborCoord[3] = new Coord(coord.x+2, coord.y-1);
             neighborCoord[4] = new Coord(coord.x-1, coord.y  );
             neighborCoord[5] = new Coord(coord.x+1, coord.y  );
             neighborCoord[6] = new Coord(coord.x+2, coord.y  );
             neighborCoord[7] = new Coord(coord.x  , coord.y+1);
             neighborCoord[8] = new Coord(coord.x+1, coord.y+1);
             break;
          case 11:
             neighborCoord[0] = new Coord(coord.x-1, coord.y-1);
             neighborCoord[1] = new Coord(coord.x  , coord.y-1);
             neighborCoord[2] = new Coord(coord.x+1, coord.y-1);
             neighborCoord[3] = new Coord(coord.x-1, coord.y  );
             neighborCoord[4] = new Coord(coord.x+1, coord.y  );
             neighborCoord[5] = new Coord(coord.x-1, coord.y+1);
             neighborCoord[6] = new Coord(coord.x  , coord.y+1);
             neighborCoord[7] = new Coord(coord.x+1, coord.y+1);
             neighborCoord[8] = new Coord(coord.x+2, coord.y+1);
             break;
          }

         return neighborCoord;
      }

      protected override void CalcRegion() {
         var attr = Attr;
         var a = attr.A;
         var b = attr.B;
         var h = attr.H;
         var n = attr.N;
         var m = attr.M;

         // определение координат точек фигуры
         var oX = (h*2)*(coord.x/3) + h+m; // offset X
         var oY = (a*3)*(coord.y/4) + a+n; // offset Y

         switch (direction) {
         case 0:
            region.SetPoint(0, oX - h  , oY - n-n);
            region.SetPoint(1, oX - m  , oY - n  );
            region.SetPoint(2, oX - h-m, oY - n  );
            region.SetPoint(3, oX - h-m, oY - n-b);
            break;
         case 1:
            region.SetPoint(0, oX      , oY - n-n);
            region.SetPoint(1, oX - m  , oY - n  );
            region.SetPoint(2, oX - h  , oY - n-n);
            region.SetPoint(3, oX - m  , oY - n-a);
            break;
         case 2:
            region.SetPoint(0, oX + m  , oY - n-b);
            region.SetPoint(1, oX + m  , oY - n  );
            region.SetPoint(2, oX - m  , oY - n  );
            region.SetPoint(3, oX      , oY - n-n);
            break;
         case 3:
            region.SetPoint(0, oX - m  , oY - n  );
            region.SetPoint(1, oX - h  , oY      );
            region.SetPoint(2, oX - h-m, oY - n+b);
            region.SetPoint(3, oX - h-m, oY - n  );
            break;
         case 4:
            region.SetPoint(0, oX - m  , oY - n  );
            region.SetPoint(1, oX      , oY      );
            region.SetPoint(2, oX - m  , oY - n+a);
            region.SetPoint(3, oX - h  , oY      );
            break;
         case 5:
            region.SetPoint(0, oX + m  , oY - n  );
            region.SetPoint(1, oX + m  , oY - n+b);
            region.SetPoint(2, oX      , oY      );
            region.SetPoint(3, oX - m  , oY - n  );
            break;
         case 6:
            region.SetPoint(0, oX - m  , oY + n-b);
            region.SetPoint(1, oX - m  , oY + n  );
            region.SetPoint(2, oX - h-m, oY + n  );
            region.SetPoint(3, oX - h  , oY      );
            break;
         case 7:
            region.SetPoint(0, oX      , oY      );
            region.SetPoint(1, oX + m  , oY + n  );
            region.SetPoint(2, oX - m  , oY + n  );
            region.SetPoint(3, oX - m  , oY + n-b);
            break;
         case 8:
            region.SetPoint(0, oX + h  , oY      );
            region.SetPoint(1, oX + m  , oY + n  );
            region.SetPoint(2, oX      , oY      );
            region.SetPoint(3, oX + m  , oY + n-a);
            break;
         case 9:
            region.SetPoint(0, oX - m  , oY + n  );
            region.SetPoint(1, oX - m  , oY + n+b);
            region.SetPoint(2, oX - h  , oY + n+n);
            region.SetPoint(3, oX - h-m, oY + n  );
            break;
         case 10:
            region.SetPoint(0, oX + m  , oY + n  );
            region.SetPoint(1, oX      , oY + n+n);
            region.SetPoint(2, oX - m  , oY + n+b);
            region.SetPoint(3, oX - m  , oY + n  );
            break;
         case 11:
            region.SetPoint(0, oX + m  , oY + n  );
            region.SetPoint(1, oX + h  , oY + n+n);
            region.SetPoint(2, oX + m  , oY + n+a);
            region.SetPoint(3, oX      , oY + n+n);
            break;
         }
      }

      public override RectDouble getRcInner(double borderWidth) {
         var attr = Attr;
         var a = attr.A;
         var b = attr.B;
         var h = attr.H;
         var n = attr.N;
         var m = attr.M;
         var z = attr.Z;
         var zx = attr.Zx;
         var zy = attr.Zy;
         //var w = borderWidth / 2.0;
         var sq    = attr.GetSq(borderWidth);
         var sq2   = sq/2;

         var oX = (h*2)*(coord.x/3) + h+m; // offset X
         var oY = (a*3)*(coord.y/4) + a+n; // offset Y

         var center = new PointDouble(); // координата центра квадрата
         switch (direction) {
         case 0:  center.X = oX - h-m+zx; center.Y = oY - n-b+zy; break;
         case 1:  center.X = oX - m;      center.Y = oY - n-a+z ; break;
         case 2:  center.X = oX + m  -zx; center.Y = oY - n-b+zy; break;
         case 3:  center.X = oX - h-m+zx; center.Y = oY - n+b-zy; break;
         case 4:  center.X = oX - m;      center.Y = oY - n+a-z ; break;
         case 5:  center.X = oX + m  -zx; center.Y = oY - n+b-zy; break;
         case 6:  center.X = oX - m  -zx; center.Y = oY + n-b+zy; break;
         case 7:  center.X = oX - m  +zx; center.Y = oY + n-b+zy; break;
         case 8:  center.X = oX + m;      center.Y = oY + n-a+z ; break;
         case 9:  center.X = oX - m  -zx; center.Y = oY + n+b-zy; break;
         case 10: center.X = oX - m  +zx; center.Y = oY + n+b-zy; break;
         case 11: center.X = oX + m;      center.Y = oY + n+a-z ; break;
         }

         return new RectDouble(
            center.X - sq2,
            center.Y - sq2,
            sq, sq);
      }

      public override int getShiftPointBorderIndex() {
         switch (direction) {
         case 1: case  8: return 1;
         case 4: case 11: return 3;
         }
         return 2;
      }

   }

}
