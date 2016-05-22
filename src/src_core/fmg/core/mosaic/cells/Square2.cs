////////////////////////////////////////////////////////////////////////////////
//                               FastMines project
//                                      © Sergey Krivulya (KSerg, aka SeregaLBN)
// file name: "Square2.java"
//
// Реализация класса Square2 - квадрат (перекошенный вариант поля)
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

   /// <summary> Квадрат. Вариант 2 - сдвинутые ряды </summary>
   public class Square2 : BaseCell {

      public class AttrSquare2 : BaseAttribute {

         public AttrSquare2(double area)
            : base(area)
         {}

         public override SizeDouble GetOwnerSize(Matrisize sizeField) {
            var a = A; // размер стороны квадрата
            return new SizeDouble(
                  sizeField.m * a + a/2,
                  sizeField.n * a);
         }

         public override int getNeighborNumber(bool max) { return 6; }
         public override int getNeighborNumber(int direction) { return 6; }
         public override int getVertexNumber(int direction) { return 4; }
         public override double getVertexIntersection() { return 3; }
         public override Size GetDirectionSizeField() { return new Size(1, 2); }
         public override double A => Math.Sqrt(Area);
         public override double GetSq(int borderWidth) {
            var w = borderWidth/2.0;
            return A-2*w;
         }
      }

      public Square2(AttrSquare2 attr, Coord coord)
         : base(attr, coord,
                    coord.y&1 // 0..1
               )
      {}

      private new AttrSquare2 Attr => (AttrSquare2) base.Attr;

      protected override IList<Coord> GetCoordsNeighbor() {
         var neighborCoord = new Coord[Attr.getNeighborNumber(true)];

         // определяю координаты соседей
          neighborCoord[0] = new Coord(coord.x- direction   , coord.y-1);
         neighborCoord[1] = new Coord(coord.x+(direction^1), coord.y-1);
         neighborCoord[2] = new Coord(coord.x-1            , coord.y);
         neighborCoord[3] = new Coord(coord.x+1            , coord.y);
         neighborCoord[4] = new Coord(coord.x- direction   , coord.y+1);
         neighborCoord[5] = new Coord(coord.x+(direction^1), coord.y+1);

         return neighborCoord;
      }

      public override bool PointInRegion(PointDouble point) {
         if ((point.X < region.GetPoint(3).X) || (point.X >= region.GetPoint(0).X) ||
            (point.Y < region.GetPoint(0).Y) || (point.Y >= region.GetPoint(2).Y))
            return false;
         return true;
      }

      protected override void CalcRegion() {
         var attr = Attr;
         var a = attr.A;

         var x1 = a * (coord.x + 0) + ((direction != 0) ? 0 : a / 2);
         var x2 = a * (coord.x + 1) + ((direction != 0) ? 0 : a / 2);
         var y1 = a * (coord.y + 0);
         var y2 = a * (coord.y + 1);

         region.SetPoint(0, x2, y1);
         region.SetPoint(1, x2, y2);
         region.SetPoint(2, x1, y2);
         region.SetPoint(3, x1, y1);
      }

      public override RectDouble getRcInner(int borderWidth) {
         var attr = Attr;
         var sq = attr.GetSq(borderWidth);
         var w = borderWidth/2.0;

         return new RectDouble(
            region.GetPoint(3).X + w,
            region.GetPoint(3).Y + w,
            sq, sq);
      }

      public override int getShiftPointBorderIndex() { return 2; }

   }

}
