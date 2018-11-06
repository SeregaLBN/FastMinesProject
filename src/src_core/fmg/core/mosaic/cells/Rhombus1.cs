////////////////////////////////////////////////////////////////////////////////
//                               FastMines project
// file name: "Rhombus1.cs"
//
// Реализация класса Rhombus1 - 3 ромба, составляющие равносторонний шестиугольник
// Author: 2002-2018  -  Serhii Kryvulia aka SeregaLBN
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
using fmg.common;
using fmg.common.geom;
using System.Collections.Generic;

namespace fmg.core.mosaic.cells {

   /// <summary> Rhombus1 - 3 ромба, составляющие равносторонний шестиугольник </summary>
   public class Rhombus1 : BaseCell {

      public class AttrRhombus1 : BaseAttribute {

         public override SizeDouble GetSize(Matrisize sizeField) {
            var a = A;
            var r = R;
            var c = C;
            var result = new SizeDouble(
                  c+a   *((sizeField.m+2)/3) +
                   (a+c)*((sizeField.m+1)/3) +
                      c *((sizeField.m+0)/3),
                      r * (sizeField.n+1));

            if (sizeField.m == 1)
               result.Height -= r;
            if (sizeField.n == 1)
               switch (sizeField.m % 3) {
               case 0: result.Width -= a/2; break;
               case 2: result.Width -= a; break;
               }

            return result;
         }

         public override int GetNeighborNumber(int direction) { return 10; }
         public override int GetVertexNumber(int direction) { return 4; }
         public override double GetVertexIntersection() { return 4.5; } // (3+3+6+6)/4.
         public override Size GetDirectionSizeField() { return new Size(3, 2); }
         public override double A => Math.Sqrt(Area*2/SQRT3);
         public double C => A / 2;
         public double H => A * SQRT3;
         public double R => H / 2;
         public override double GetSq(double borderWidth) {
            var w = borderWidth/2.0;
            return (A*SQRT3 - w*4)/(SQRT3+1);
         }

         public override int GetMaxBackgroundFillModeValue() {
            return base.GetMaxBackgroundFillModeValue()+1;
         }
      }

      public Rhombus1(AttrRhombus1 attr, Coord coord)
         : base(attr, coord,
               (coord.y&1)*3+(coord.x%3) // 0..5
            )
      {}

      private new AttrRhombus1 Attr => (AttrRhombus1) base.Attr;

      protected override IList<Coord> GetCoordsNeighbor() {
         var neighborCoord = new Coord[Attr.GetNeighborNumber(getDirection())];

         // определяю координаты соседей
          switch (direction) {
          case 0:
             neighborCoord[ 0] = new Coord(coord.x+1, coord.y-2);
             neighborCoord[ 1] = new Coord(coord.x+2, coord.y-2);
             neighborCoord[ 2] = new Coord(coord.x-2, coord.y-1);
             neighborCoord[ 3] = new Coord(coord.x-1, coord.y-1);
             neighborCoord[ 4] = new Coord(coord.x  , coord.y-1);
             neighborCoord[ 5] = new Coord(coord.x+1, coord.y-1);
             neighborCoord[ 6] = new Coord(coord.x-1, coord.y  );
             neighborCoord[ 7] = new Coord(coord.x+1, coord.y  );
             neighborCoord[ 8] = new Coord(coord.x-1, coord.y+1);
             neighborCoord[ 9] = new Coord(coord.x  , coord.y+1);
             break;
          case 1:
             neighborCoord[ 0] = new Coord(coord.x  , coord.y-2);
             neighborCoord[ 1] = new Coord(coord.x+1, coord.y-2);
             neighborCoord[ 2] = new Coord(coord.x-1, coord.y-1);
             neighborCoord[ 3] = new Coord(coord.x  , coord.y-1);
             neighborCoord[ 4] = new Coord(coord.x-1, coord.y  );
             neighborCoord[ 5] = new Coord(coord.x+1, coord.y  );
             neighborCoord[ 6] = new Coord(coord.x-1, coord.y+1);
             neighborCoord[ 7] = new Coord(coord.x  , coord.y+1);
             neighborCoord[ 8] = new Coord(coord.x-1, coord.y+2);
             neighborCoord[ 9] = new Coord(coord.x  , coord.y+2);
             break;
          case 2:
             neighborCoord[ 0] = new Coord(coord.x-1, coord.y-1);
             neighborCoord[ 1] = new Coord(coord.x  , coord.y-1);
             neighborCoord[ 2] = new Coord(coord.x-1, coord.y  );
             neighborCoord[ 3] = new Coord(coord.x+1, coord.y  );
             neighborCoord[ 4] = new Coord(coord.x-2, coord.y+1);
             neighborCoord[ 5] = new Coord(coord.x-1, coord.y+1);
             neighborCoord[ 6] = new Coord(coord.x  , coord.y+1);
             neighborCoord[ 7] = new Coord(coord.x+1, coord.y+1);
             neighborCoord[ 8] = new Coord(coord.x-2, coord.y+2);
             neighborCoord[ 9] = new Coord(coord.x-1, coord.y+2);
             break;
          case 3:
             neighborCoord[ 0] = new Coord(coord.x-2, coord.y-2);
             neighborCoord[ 1] = new Coord(coord.x-1, coord.y-2);
             neighborCoord[ 2] = new Coord(coord.x-1, coord.y-1);
             neighborCoord[ 3] = new Coord(coord.x  , coord.y-1);
             neighborCoord[ 4] = new Coord(coord.x+1, coord.y-1);
             neighborCoord[ 5] = new Coord(coord.x+2, coord.y-1);
             neighborCoord[ 6] = new Coord(coord.x-1, coord.y  );
             neighborCoord[ 7] = new Coord(coord.x+1, coord.y  );
             neighborCoord[ 8] = new Coord(coord.x  , coord.y+1);
             neighborCoord[ 9] = new Coord(coord.x+1, coord.y+1);
             break;
          case 4:
             neighborCoord[ 0] = new Coord(coord.x  , coord.y-1);
             neighborCoord[ 1] = new Coord(coord.x+1, coord.y-1);
             neighborCoord[ 2] = new Coord(coord.x-1, coord.y  );
             neighborCoord[ 3] = new Coord(coord.x+1, coord.y  );
             neighborCoord[ 4] = new Coord(coord.x-1, coord.y+1);
             neighborCoord[ 5] = new Coord(coord.x  , coord.y+1);
             neighborCoord[ 6] = new Coord(coord.x+1, coord.y+1);
             neighborCoord[ 7] = new Coord(coord.x+2, coord.y+1);
             neighborCoord[ 8] = new Coord(coord.x+1, coord.y+2);
             neighborCoord[ 9] = new Coord(coord.x+2, coord.y+2);
             break;
          case 5:
             neighborCoord[ 0] = new Coord(coord.x-1, coord.y-2);
             neighborCoord[ 1] = new Coord(coord.x  , coord.y-2);
             neighborCoord[ 2] = new Coord(coord.x  , coord.y-1);
             neighborCoord[ 3] = new Coord(coord.x+1, coord.y-1);
             neighborCoord[ 4] = new Coord(coord.x-1, coord.y  );
             neighborCoord[ 5] = new Coord(coord.x+1, coord.y  );
             neighborCoord[ 6] = new Coord(coord.x  , coord.y+1);
             neighborCoord[ 7] = new Coord(coord.x+1, coord.y+1);
             neighborCoord[ 8] = new Coord(coord.x  , coord.y+2);
             neighborCoord[ 9] = new Coord(coord.x+1, coord.y+2);
             break;
          }

         return neighborCoord;
      }

      protected override void CalcRegion() {
         var attr = Attr;
         var a = attr.A;
         var c = attr.C;
         var h = attr.H;
         var r = attr.R;

         // определение координат точек фигуры
         var oX = a*(coord.x/3*3+1)+c; // offset X
         var oY = h*(coord.y/2)    +h; // offset Y

         switch (direction) {
         case 0:
            region.SetPoint(0, oX      , oY - h);
            region.SetPoint(1, oX - c  , oY - r);
            region.SetPoint(2, oX - a-c, oY - r);
            region.SetPoint(3, oX - a  , oY - h);
            break;
         case 1:
            region.SetPoint(0, oX      , oY - h);
            region.SetPoint(1, oX + c  , oY - r);
            region.SetPoint(2, oX      , oY    );
            region.SetPoint(3, oX - c  , oY - r);
            break;
         case 2:
            region.SetPoint(0, oX + a+c, oY - r);
            region.SetPoint(1, oX + a  , oY    );
            region.SetPoint(2, oX      , oY    );
            region.SetPoint(3, oX + c  , oY - r);
            break;
         case 3:
            region.SetPoint(0, oX - c  , oY - r);
            region.SetPoint(1, oX      , oY    );
            region.SetPoint(2, oX - a  , oY    );
            region.SetPoint(3, oX - a-c, oY - r);
            break;
         case 4:
            region.SetPoint(0, oX + a  , oY    );
            region.SetPoint(1, oX + a+c, oY + r);
            region.SetPoint(2, oX + c  , oY + r);
            region.SetPoint(3, oX      , oY    );
            break;
         case 5:
            region.SetPoint(0, oX + a+c, oY - r);
            region.SetPoint(1, oX + a+a, oY    );
            region.SetPoint(2, oX + a+c, oY + r);
            region.SetPoint(3, oX + a  , oY    );
            break;
         }
      }

      public override RectDouble getRcInner(double borderWidth) {
         var attr = Attr;
         var a = attr.A;
         var c = attr.C;
         var h = attr.H;
         var r = attr.R;
         //var w = borderWidth / 2.0;
         var sq  = attr.GetSq(borderWidth);
         var sq2 = sq/2;

         var oX = a*(coord.x/3*3+1)+c; // offset X
         var oY = h*(coord.y/2)    +h; // offset Y

         var center = new PointDouble(); // координата центра квадрата
         switch (direction) {
         case 0: center.X = oX - c*1.5; center.Y = oY - r*1.5; break;
         case 1: center.X = oX;         center.Y = oY - r;     break;
         case 2: center.X = oX + c*1.5; center.Y = oY - r*0.5; break;
         case 3: center.X = oX - c*1.5; center.Y = oY - r*0.5; break;
         case 4: center.X = oX + c*1.5; center.Y = oY + r*0.5; break;
         case 5: center.X = oX + a+c;   center.Y = oY;         break;
         }

         return new RectDouble(
            center.X - sq2,
            center.Y - sq2,
            sq, sq);
      }

      public override int getShiftPointBorderIndex() { return 2; }

      public override Color getBackgroundFillColor(int fillMode, Color defaultColor, Func<int, Color> repositoryColor) {
         if (fillMode == Attr.GetMaxBackgroundFillModeValue()) {
            switch ((getCoord().y%4)*3+(getCoord().x%3)) { // почти как вычисление direction...
            // подсвечиваю 4 группы, составляющие каждая шестигранник из 3х ромбов
            case 0: case  1: case  3: return repositoryColor(0);
            case 2: case  4: case  5: return repositoryColor(1);
            case 6: case  7: case  9: return repositoryColor(2);
            case 8: case 10: case 11: return repositoryColor(3);
            }
         }
         return base.getBackgroundFillColor(fillMode, defaultColor, repositoryColor);
      }

   }

}
