////////////////////////////////////////////////////////////////////////////////
//                               FastMines project
// file name: "Triangle3.cs"
//
// Реализация класса Triangle3 - треугольник 45°-90°-45°
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
using fmg.common.geom;
using System.Collections.Generic;

namespace fmg.core.mosaic.cells {

   /// <summary> Треугольник. Вариант 3 - треугольник 45°-90°-45°(квадрат разделённый на 4 части) </summary>
   public class Triangle3 : BaseCell {

      public class AttrTriangle3 : BaseAttribute {

         public override SizeDouble GetSize(Matrisize sizeField) {
            var a = A;
            return new SizeDouble(a * ((sizeField.m + (sizeField.m & 1)) / 2),
                                  a * ((sizeField.n + (sizeField.n & 1)) / 2));
         }

         public override int GetNeighborNumber(int direction) { return 14; }
         public override int GetVertexNumber(int direction) { return 3; }
         public override double GetVertexIntersection() { return 6.6666666666666666666666666666667; } // (8+8+4)/3.
         public override Size GetDirectionSizeField() { return new Size(2, 2); }
         public override double A => 2 * B;
         /// <summary> пол стороны треугольника </summary>
         public double B => Math.Sqrt(Area);
         public override double GetSq(double borderWidth) {
            var w = borderWidth/2.0;
            return (A - w*2 / TAN45_2 ) / 3;
         }
      }

      public Triangle3(AttrTriangle3 attr, Coord coord)
         : base(attr, coord,
               ((coord.y&1)<<1)+(coord.x&1) // 0..3
            )
      {}

      private new AttrTriangle3 Attr => (AttrTriangle3) base.Attr;

      protected override IList<Coord> GetCoordsNeighbor() {
         var neighborCoord = new Coord[Attr.GetNeighborNumber(getDirection())];

         // определяю координаты соседей
         switch (direction) {
         case 0:
            neighborCoord[ 0] = new Coord(coord.x-1, coord.y-2);
            neighborCoord[ 1] = new Coord(coord.x+1, coord.y-2);
            neighborCoord[ 2] = new Coord(coord.x-1, coord.y-1);
            neighborCoord[ 3] = new Coord(coord.x  , coord.y-1);
            neighborCoord[ 4] = new Coord(coord.x+1, coord.y-1);
            neighborCoord[ 5] = new Coord(coord.x+2, coord.y-1);
            neighborCoord[ 6] = new Coord(coord.x+3, coord.y-1);
            neighborCoord[ 7] = new Coord(coord.x-2, coord.y  );
            neighborCoord[ 8] = new Coord(coord.x-1, coord.y  );
            neighborCoord[ 9] = new Coord(coord.x+1, coord.y  );
            neighborCoord[10] = new Coord(coord.x+2, coord.y  );
            neighborCoord[11] = new Coord(coord.x  , coord.y+1);
            neighborCoord[12] = new Coord(coord.x+1, coord.y+1);
            neighborCoord[13] = new Coord(coord.x+2, coord.y+1);
            break;
         case 1:
            neighborCoord[ 0] = new Coord(coord.x  , coord.y-2);
            neighborCoord[ 1] = new Coord(coord.x  , coord.y-1);
            neighborCoord[ 2] = new Coord(coord.x+1, coord.y-1);
            neighborCoord[ 3] = new Coord(coord.x+2, coord.y-1);
            neighborCoord[ 4] = new Coord(coord.x-1, coord.y  );
            neighborCoord[ 5] = new Coord(coord.x+1, coord.y  );
            neighborCoord[ 6] = new Coord(coord.x-1, coord.y+1);
            neighborCoord[ 7] = new Coord(coord.x  , coord.y+1);
            neighborCoord[ 8] = new Coord(coord.x+1, coord.y+1);
            neighborCoord[ 9] = new Coord(coord.x+2, coord.y+1);
            neighborCoord[10] = new Coord(coord.x-1, coord.y+2);
            neighborCoord[11] = new Coord(coord.x  , coord.y+2);
            neighborCoord[12] = new Coord(coord.x+1, coord.y+2);
            neighborCoord[13] = new Coord(coord.x+1, coord.y+3);
            break;
         case 2:
            neighborCoord[ 0] = new Coord(coord.x-1, coord.y-3);
            neighborCoord[ 1] = new Coord(coord.x-1, coord.y-2);
            neighborCoord[ 2] = new Coord(coord.x  , coord.y-2);
            neighborCoord[ 3] = new Coord(coord.x+1, coord.y-2);
            neighborCoord[ 4] = new Coord(coord.x-2, coord.y-1);
            neighborCoord[ 5] = new Coord(coord.x-1, coord.y-1);
            neighborCoord[ 6] = new Coord(coord.x  , coord.y-1);
            neighborCoord[ 7] = new Coord(coord.x+1, coord.y-1);
            neighborCoord[ 8] = new Coord(coord.x-1, coord.y  );
            neighborCoord[ 9] = new Coord(coord.x+1, coord.y  );
            neighborCoord[10] = new Coord(coord.x-2, coord.y+1);
            neighborCoord[11] = new Coord(coord.x-1, coord.y+1);
            neighborCoord[12] = new Coord(coord.x  , coord.y+1);
            neighborCoord[13] = new Coord(coord.x  , coord.y+2);
            break;
         case 3:
            neighborCoord[ 0] = new Coord(coord.x-2, coord.y-1);
            neighborCoord[ 1] = new Coord(coord.x-1, coord.y-1);
            neighborCoord[ 2] = new Coord(coord.x  , coord.y-1);
            neighborCoord[ 3] = new Coord(coord.x-2, coord.y  );
            neighborCoord[ 4] = new Coord(coord.x-1, coord.y  );
            neighborCoord[ 5] = new Coord(coord.x+1, coord.y  );
            neighborCoord[ 6] = new Coord(coord.x+2, coord.y  );
            neighborCoord[ 7] = new Coord(coord.x-3, coord.y+1);
            neighborCoord[ 8] = new Coord(coord.x-2, coord.y+1);
            neighborCoord[ 9] = new Coord(coord.x-1, coord.y+1);
            neighborCoord[10] = new Coord(coord.x  , coord.y+1);
            neighborCoord[11] = new Coord(coord.x+1, coord.y+1);
            neighborCoord[12] = new Coord(coord.x-1, coord.y+2);
            neighborCoord[13] = new Coord(coord.x+1, coord.y+2);
            break;
         }

         return neighborCoord;
      }

      protected override void CalcRegion() {
         var attr = Attr;
         var a = attr.A;
         var b = attr.B;

         var oX = a*(coord.x>>1); // offset X
         var oY = a*(coord.y>>1); // offset Y

         switch (direction) {
         case 0:
            region.SetPoint(0, oX + a, oY    );
            region.SetPoint(2, oX    , oY    );
            region.SetPoint(1, oX + b, oY + b);
            break;
         case 1:
            region.SetPoint(0, oX + a, oY    );
            region.SetPoint(2, oX + b, oY + b);
            region.SetPoint(1, oX + a, oY + a);
            break;
         case 2:
            region.SetPoint(2, oX    , oY + a);
            region.SetPoint(1, oX + b, oY + b);
            region.SetPoint(0, oX    , oY    );
            break;
         case 3:
            region.SetPoint(2, oX    , oY + a);
            region.SetPoint(1, oX + a, oY + a);
            region.SetPoint(0, oX + b, oY + b);
            break;
         }
      }

      public override RectDouble getRcInner(double borderWidth) {
         var attr = Attr;
         var sq = attr.GetSq(borderWidth);
         var w = borderWidth/2.0;

         var center = new PointDouble(); // координата вписанного в фигуру квадрата (не совпадает с центром фигуры)
         switch (direction) {
         case 0:
            center.X = region.GetPoint(1).X;
            center.Y = region.GetPoint(0).Y + sq/2 + w;
            break;
         case 1:
            center.X = region.GetPoint(0).X - sq/2 - w;
            center.Y = region.GetPoint(2).Y;
            break;
         case 2:
            center.X = region.GetPoint(0).X + sq/2 + w;
            center.Y = region.GetPoint(1).Y;
            break;
         case 3:
            center.X = region.GetPoint(0).X;
            center.Y = region.GetPoint(1).Y - sq/2 - w;
            break;
         }

         return new RectDouble(
            center.X - sq/2,
            center.Y - sq/2,
            sq, sq);
      }

      public override int getShiftPointBorderIndex() {
         switch (direction) {
         case 0: case 1: return 1;
         }
         return 2;
      }

   }

}
