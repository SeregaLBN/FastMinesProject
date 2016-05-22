////////////////////////////////////////////////////////////////////////////////
//                               FastMines project
//                                      © Sergey Krivulya (KSerg, aka SeregaLBN)
// file name: "PentagonT24.java"
//
// Реализация класса PentagonT24 - равносторонний 5-ти угольник, тип №2 и №4
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

   /// <summary> Пятиугольник. Тип №2 и №4 - равносторонний </summary>
   public class PentagonT24 : BaseCell {

      public class AttrPentagonT24 : BaseAttribute {

         public AttrPentagonT24(double area)
            : base(area)
         {}

         public override SizeDouble GetOwnerSize(Matrisize sizeField) {
            var a = A;
            var b = B;
            var result = new SizeDouble(
                  b + sizeField.m * a,
                  b + sizeField.n * a);

            if (sizeField.n == 1)
               result.Width -= C;

               return result;
         }

         public override int getNeighborNumber(bool max) { return 7; }
         public override int getNeighborNumber(int direction) { return 7; }
         public override int getVertexNumber(int direction) { return 5; }
         public override double getVertexIntersection() { return 3.4; } // (3+3+3+4+4)/5.
         public override Size GetDirectionSizeField() { return new Size(2, 2); }
         public override double A => Math.Sqrt(Area);
         public double B => A * 6/11;
         public double C => B / 2;
         public override double GetSq(int borderWidth) {
            var w = borderWidth/2.0;
            return A*8/11-(w+w/SIN135a) / SQRT2;
         }
      }

      public PentagonT24(AttrPentagonT24 attr, Coord coord)
         : base(attr, coord,
                    ((coord.y&1)<<1) + (coord.x&1) // 0..3
               )
      {}

      private new AttrPentagonT24 Attr => (AttrPentagonT24) base.Attr;

      protected override IList<Coord> GetCoordsNeighbor() {
         var neighborCoord = new Coord[Attr.getNeighborNumber(true)];

         // определяю координаты соседей
         switch (direction) {
         case 0:
            neighborCoord[0] = new Coord(coord.x-1, coord.y-1);
            neighborCoord[1] = new Coord(coord.x  , coord.y-1);
            neighborCoord[2] = new Coord(coord.x-1, coord.y  );
            neighborCoord[3] = new Coord(coord.x+1, coord.y  );
            neighborCoord[4] = new Coord(coord.x-1, coord.y+1);
            neighborCoord[5] = new Coord(coord.x  , coord.y+1);
            neighborCoord[6] = new Coord(coord.x+1, coord.y+1);
            break;
         case 1:
            neighborCoord[0] = new Coord(coord.x-1, coord.y-1);
            neighborCoord[1] = new Coord(coord.x  , coord.y-1);
            neighborCoord[2] = new Coord(coord.x+1, coord.y-1);
            neighborCoord[3] = new Coord(coord.x-1, coord.y  );
            neighborCoord[4] = new Coord(coord.x+1, coord.y  );
            neighborCoord[5] = new Coord(coord.x-1, coord.y+1);
            neighborCoord[6] = new Coord(coord.x  , coord.y+1);
            break;
         case 2:
            neighborCoord[0] = new Coord(coord.x  , coord.y-1);
            neighborCoord[1] = new Coord(coord.x+1, coord.y-1);
            neighborCoord[2] = new Coord(coord.x-1, coord.y  );
            neighborCoord[3] = new Coord(coord.x+1, coord.y  );
            neighborCoord[4] = new Coord(coord.x-1, coord.y+1);
            neighborCoord[5] = new Coord(coord.x  , coord.y+1);
            neighborCoord[6] = new Coord(coord.x+1, coord.y+1);
            break;
         case 3:
            neighborCoord[0] = new Coord(coord.x-1, coord.y-1);
            neighborCoord[1] = new Coord(coord.x  , coord.y-1);
            neighborCoord[2] = new Coord(coord.x+1, coord.y-1);
            neighborCoord[3] = new Coord(coord.x-1, coord.y  );
            neighborCoord[4] = new Coord(coord.x+1, coord.y  );
            neighborCoord[5] = new Coord(coord.x  , coord.y+1);
            neighborCoord[6] = new Coord(coord.x+1, coord.y+1);
            break;
         }

         return neighborCoord;
      }

      protected override void CalcRegion() {
         var attr = Attr;
         var a = attr.A;
         var b = attr.B;
         var c = attr.C;

         // определение координат точек фигуры
         var oX = a*((coord.x>>1)<<1); // offset X
         var oY = a*((coord.y>>1)<<1); // offset Y
         switch (direction) {
         case 0:
            region.SetPoint(0, oX +       a, oY + b      );
            region.SetPoint(1, oX + c +   a, oY + c +   a);
            region.SetPoint(2, oX + b      , oY + b +   a);
            region.SetPoint(3, oX          , oY +       a);
            region.SetPoint(4, oX + c      , oY + c      );
            break;
         case 1:
            region.SetPoint(0, oX + c + 2*a, oY + c      );
            region.SetPoint(1, oX +     2*a, oY +       a);
            region.SetPoint(2, oX + c +   a, oY + c +   a);
            region.SetPoint(3, oX +       a, oY + b      );
            region.SetPoint(4, oX + b +   a, oY          );
            break;
         case 2:
            region.SetPoint(0, oX + c +   a, oY + c +   a);
            region.SetPoint(1, oX + b +   a, oY +     2*a);
            region.SetPoint(2, oX +       a, oY + b + 2*a);
            region.SetPoint(3, oX + c      , oY + c + 2*a);
            region.SetPoint(4, oX + b      , oY + b +   a);
            break;
         case 3:
            region.SetPoint(0, oX +     2*a, oY +       a);
            region.SetPoint(1, oX + b + 2*a, oY + b +   a);
            region.SetPoint(2, oX + c + 2*a, oY + c + 2*a);
            region.SetPoint(3, oX + b +   a, oY +     2*a);
            region.SetPoint(4, oX + c +   a, oY + c +   a);
            break;
         }
      }

      public override RectDouble getRcInner(int borderWidth) {
         var attr = Attr;
         var sq = attr.GetSq(borderWidth);
         var w = borderWidth/2.0;
         var w2 = w/SQRT2;

         var square = new RectDouble();
         switch (direction) {
         case 0:
            square.X = region.GetPoint(4).X+w2;
            square.Y = region.GetPoint(1).Y-w2 - sq;
            break;
         case 1:
            square.X = region.GetPoint(2).X+w2;
            square.Y = region.GetPoint(0).Y+w2;
            break;
         case 2:
            square.X = region.GetPoint(0).X-w2 - sq;
            square.Y = region.GetPoint(3).Y-w2 - sq;
            break;
         case 3:
            square.X = region.GetPoint(2).X-w2 - sq;
            square.Y = region.GetPoint(4).Y+w2;
            break;
         }
         square.Width = sq;
         square.Height = sq;
         return square;
      }

      public override int getShiftPointBorderIndex() {
         switch (direction) {
         case 0: case 1:
            return 2;
         }
         return 3;
      }

   }

}
