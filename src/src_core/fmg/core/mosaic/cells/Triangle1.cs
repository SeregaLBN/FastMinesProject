////////////////////////////////////////////////////////////////////////////////
//                               FastMines project
//                                      © Sergey Krivulya (KSerg, aka SeregaLBN)
// file name: "Triangle1.java"
//
// Реализация класса Triangle1 - равносторонний треугольник (вариант поля №1)
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

   /// <summary> Треугольник. Вариант 1 - равносторонний, классика </summary>
   public class Triangle1 : BaseCell {

      public class AttrTriangle1 : BaseAttribute {

         public AttrTriangle1(double area)
            : base(area)
         {}

         public override SizeDouble GetOwnerSize(Matrisize sizeField) {
            return new SizeDouble(
                  B * (sizeField.m+1),
                  H * (sizeField.n+0));
         }

         public override int getNeighborNumber(int direction) { return 12; }
         public override int getVertexNumber(int direction) { return 3; }
         public override double getVertexIntersection() { return 6; }
         public override Size GetDirectionSizeField() { return new Size(2, 2); }
         public override double A => B * 2.0f; // размер стороны треугольника
         /// <summary> </summary> пол стороны треугольника */
         public double B => Math.Sqrt(Area/SQRT3);
         /// <summary> </summary> высота треугольника */
         public double H => B * SQRT3;
         public override double GetSq(int borderWidth) {
            var w = borderWidth/2.0;
            return (H*2 - 6*w)/(SQRT3+2);
         }
      }

      public Triangle1(AttrTriangle1 attr, Coord coord)
         : base(attr, coord,
               ((coord.y&1)<<1)+(coord.x&1) // 0..3
            )
      {}

      private new AttrTriangle1 Attr => (AttrTriangle1) base.Attr;

      protected override IList<Coord> GetCoordsNeighbor() {
         var neighborCoord = new Coord[Attr.getNeighborNumber(getDirection())];

         // определяю координаты соседей
         switch (direction) {
         case 0: case 3:
            neighborCoord[ 0] = new Coord(coord.x-1, coord.y-1);
            neighborCoord[ 1] = new Coord(coord.x  , coord.y-1);
            neighborCoord[ 2] = new Coord(coord.x+1, coord.y-1);
            neighborCoord[ 3] = new Coord(coord.x-2, coord.y  );
            neighborCoord[ 4] = new Coord(coord.x-1, coord.y  );
            neighborCoord[ 5] = new Coord(coord.x+1, coord.y  );
            neighborCoord[ 6] = new Coord(coord.x+2, coord.y  );
            neighborCoord[ 7] = new Coord(coord.x-2, coord.y+1);
            neighborCoord[ 8] = new Coord(coord.x-1, coord.y+1);
            neighborCoord[ 9] = new Coord(coord.x  , coord.y+1);
            neighborCoord[10] = new Coord(coord.x+1, coord.y+1);
            neighborCoord[11] = new Coord(coord.x+2, coord.y+1);
            break;
         case 1: case 2:
            neighborCoord[ 0] = new Coord(coord.x-2, coord.y-1);
            neighborCoord[ 1] = new Coord(coord.x-1, coord.y-1);
            neighborCoord[ 2] = new Coord(coord.x  , coord.y-1);
            neighborCoord[ 3] = new Coord(coord.x+1, coord.y-1);
            neighborCoord[ 4] = new Coord(coord.x+2, coord.y-1);
            neighborCoord[ 5] = new Coord(coord.x-2, coord.y  );
            neighborCoord[ 6] = new Coord(coord.x-1, coord.y  );
            neighborCoord[ 7] = new Coord(coord.x+1, coord.y  );
            neighborCoord[ 8] = new Coord(coord.x+2, coord.y  );
            neighborCoord[ 9] = new Coord(coord.x-1, coord.y+1);
            neighborCoord[10] = new Coord(coord.x  , coord.y+1);
            neighborCoord[11] = new Coord(coord.x+1, coord.y+1);
            break;
         }

         return neighborCoord;
      }

      protected override void CalcRegion() {
         var attr = Attr;
         var a = attr.A;
         var b = attr.B;
         var h = attr.H;

         var oX = a*(coord.x>>1); // offset X
         var oY = h* coord.y;     // offset Y

         switch (direction) {
         case 0:
            region.SetPoint(0, oX +   b, oY    );
            region.SetPoint(1, oX + a  , oY + h);
            region.SetPoint(2, oX      , oY + h);
            break;
         case 1:
            region.SetPoint(0, oX + a+b, oY    );
            region.SetPoint(1, oX + a  , oY + h);
            region.SetPoint(2, oX +   b, oY    );
            break;
         case 2:
            region.SetPoint(0, oX + a  , oY    );
            region.SetPoint(1, oX +   b, oY + h);
            region.SetPoint(2, oX      , oY    );
            break;
         case 3:
            region.SetPoint(0, oX + a  , oY    );
            region.SetPoint(1, oX + a+b, oY + h);
            region.SetPoint(2, oX +   b, oY + h);
            break;
         }
      }

      public override RectDouble getRcInner(int borderWidth) {
         var attr = Attr;
         var b = attr.B;
         var sq = attr.GetSq(borderWidth);
         var w = borderWidth/2.0;

         var center = new PointDouble(); // координата вписанного в фигуру квадрата (не совпадает с центром фигуры)
         switch (direction) {
         case 0: case 3:
            center.X = region.GetPoint(2).X + b;
            center.Y = region.GetPoint(2).Y - sq/2 - w;
            break;
         case 1: case 2:
            center.X = region.GetPoint(2).X + b;
            center.Y = region.GetPoint(2).Y + sq/2 + w;
            break;
         }

         return new RectDouble(
            center.X - sq/2,
            center.Y - sq/2,
            sq, sq);
      }

      public override int getShiftPointBorderIndex() {
         switch (direction) {
         case 0: case 3: return 2;
         }
         return 1;
      }

   }

}
