////////////////////////////////////////////////////////////////////////////////
//                               FastMines project
//                                      © Sergey Krivulya (KSerg, aka SeregaLBN)
// file name: "Triangle3.java"
//
// Реализация класса Triangle3 - треугольник 45°-90°-45°
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

package fmg.core.mosaic.cells;

import java.util.ArrayList;
import java.util.List;

import fmg.common.geom.*;

/**
 * Треугольник. Вариант 3 - треугольник 45°-90°-45°(квадрат разделённый на 4 части)
 * @see BaseCell
 **/
public class Triangle3 extends BaseCell {
   public static class AttrTriangle3 extends BaseAttribute {

      @Override
      public SizeDouble getSize(Matrisize sizeField) {
         double a = getA();
         return new SizeDouble(a * ((sizeField.m + (sizeField.m & 1)) / 2),
                               a * ((sizeField.n + (sizeField.n & 1)) / 2));
      }

      @Override
      public int getNeighborNumber(int direction) { return 14; }
      @Override
      public int getVertexNumber(int direction) { return 3; }
      @Override
      public double getVertexIntersection() { return 6.6666666666666666666666666666667; } // (8+8+4)/3.
      @Override
      public Size getDirectionSizeField() { return new Size(2, 2); }
      @Override
      protected double getA() { return 2*getB(); }
      /** пол стороны треугольника */
      protected double getB() { return Math.sqrt(getArea()); }
      @Override
      public double getSq(double borderWidth) {
         double w = borderWidth/2.;
         return (getA() - w*2 / TAN45_2 ) / 3;
      }
   }

   public Triangle3(AttrTriangle3 attr, Coord coord) {
      super(attr, coord,
            ((coord.y&1)<<1)+(coord.x&1) // 0..3
         );
   }

   @Override
   public AttrTriangle3 getAttr() {
      return (AttrTriangle3) super.getAttr();
   }

   @Override
   protected List<Coord> getCoordsNeighbor() {
      List<Coord> neighborCoord = new ArrayList<>(getAttr().getNeighborNumber(getDirection()));

      // определяю координаты соседей
      switch (direction) {
      case 0:
         neighborCoord.add(new Coord(coord.x-1, coord.y-2));
         neighborCoord.add(new Coord(coord.x+1, coord.y-2));
         neighborCoord.add(new Coord(coord.x-1, coord.y-1));
         neighborCoord.add(new Coord(coord.x  , coord.y-1));
         neighborCoord.add(new Coord(coord.x+1, coord.y-1));
         neighborCoord.add(new Coord(coord.x+2, coord.y-1));
         neighborCoord.add(new Coord(coord.x+3, coord.y-1));
         neighborCoord.add(new Coord(coord.x-2, coord.y  ));
         neighborCoord.add(new Coord(coord.x-1, coord.y  ));
         neighborCoord.add(new Coord(coord.x+1, coord.y  ));
         neighborCoord.add(new Coord(coord.x+2, coord.y  ));
         neighborCoord.add(new Coord(coord.x  , coord.y+1));
         neighborCoord.add(new Coord(coord.x+1, coord.y+1));
         neighborCoord.add(new Coord(coord.x+2, coord.y+1));
         break;
      case 1:
         neighborCoord.add(new Coord(coord.x  , coord.y-2));
         neighborCoord.add(new Coord(coord.x  , coord.y-1));
         neighborCoord.add(new Coord(coord.x+1, coord.y-1));
         neighborCoord.add(new Coord(coord.x+2, coord.y-1));
         neighborCoord.add(new Coord(coord.x-1, coord.y  ));
         neighborCoord.add(new Coord(coord.x+1, coord.y  ));
         neighborCoord.add(new Coord(coord.x-1, coord.y+1));
         neighborCoord.add(new Coord(coord.x  , coord.y+1));
         neighborCoord.add(new Coord(coord.x+1, coord.y+1));
         neighborCoord.add(new Coord(coord.x+2, coord.y+1));
         neighborCoord.add(new Coord(coord.x-1, coord.y+2));
         neighborCoord.add(new Coord(coord.x  , coord.y+2));
         neighborCoord.add(new Coord(coord.x+1, coord.y+2));
         neighborCoord.add(new Coord(coord.x+1, coord.y+3));
         break;
      case 2:
         neighborCoord.add(new Coord(coord.x-1, coord.y-3));
         neighborCoord.add(new Coord(coord.x-1, coord.y-2));
         neighborCoord.add(new Coord(coord.x  , coord.y-2));
         neighborCoord.add(new Coord(coord.x+1, coord.y-2));
         neighborCoord.add(new Coord(coord.x-2, coord.y-1));
         neighborCoord.add(new Coord(coord.x-1, coord.y-1));
         neighborCoord.add(new Coord(coord.x  , coord.y-1));
         neighborCoord.add(new Coord(coord.x+1, coord.y-1));
         neighborCoord.add(new Coord(coord.x-1, coord.y  ));
         neighborCoord.add(new Coord(coord.x+1, coord.y  ));
         neighborCoord.add(new Coord(coord.x-2, coord.y+1));
         neighborCoord.add(new Coord(coord.x-1, coord.y+1));
         neighborCoord.add(new Coord(coord.x  , coord.y+1));
         neighborCoord.add(new Coord(coord.x  , coord.y+2));
         break;
      case 3:
         neighborCoord.add(new Coord(coord.x-2, coord.y-1));
         neighborCoord.add(new Coord(coord.x-1, coord.y-1));
         neighborCoord.add(new Coord(coord.x  , coord.y-1));
         neighborCoord.add(new Coord(coord.x-2, coord.y  ));
         neighborCoord.add(new Coord(coord.x-1, coord.y  ));
         neighborCoord.add(new Coord(coord.x+1, coord.y  ));
         neighborCoord.add(new Coord(coord.x+2, coord.y  ));
         neighborCoord.add(new Coord(coord.x-3, coord.y+1));
         neighborCoord.add(new Coord(coord.x-2, coord.y+1));
         neighborCoord.add(new Coord(coord.x-1, coord.y+1));
         neighborCoord.add(new Coord(coord.x  , coord.y+1));
         neighborCoord.add(new Coord(coord.x+1, coord.y+1));
         neighborCoord.add(new Coord(coord.x-1, coord.y+2));
         neighborCoord.add(new Coord(coord.x+1, coord.y+2));
         break;
      }

      return neighborCoord;
   }

   @Override
   protected void calcRegion() {
      AttrTriangle3 attr = getAttr();
      double a = attr.getA();
      double b = attr.getB();

      double oX = a*(coord.x>>1); // offset X
      double oY = a*(coord.y>>1); // offset Y

      switch (direction) {
      case 0:
         region.setPoint(0, oX + a, oY    );
         region.setPoint(2, oX    , oY    );
         region.setPoint(1, oX + b, oY + b);
         break;
      case 1:
         region.setPoint(0, oX + a, oY    );
         region.setPoint(2, oX + b, oY + b);
         region.setPoint(1, oX + a, oY + a);
         break;
      case 2:
         region.setPoint(2, oX    , oY + a);
         region.setPoint(1, oX + b, oY + b);
         region.setPoint(0, oX    , oY    );
         break;
      case 3:
         region.setPoint(2, oX    , oY + a);
         region.setPoint(1, oX + a, oY + a);
         region.setPoint(0, oX + b, oY + b);
         break;
      }
   }

   @Override
   public RectDouble getRcInner(double borderWidth) {
      AttrTriangle3 attr = getAttr();
      double sq = attr.getSq(borderWidth);
      double w = borderWidth/2.;

      PointDouble center = new PointDouble(); // координата вписанного в фигуру квадрата (не совпадает с центром фигуры)
      switch (direction) {
      case 0:
         center.x = region.getPoint(1).x;
         center.y = region.getPoint(0).y + sq/2 + w;
         break;
      case 1:
         center.x = region.getPoint(0).x - sq/2 - w;
         center.y = region.getPoint(2).y;
         break;
      case 2:
         center.x = region.getPoint(0).x + sq/2 + w;
         center.y = region.getPoint(1).y;
         break;
      case 3:
         center.x = region.getPoint(0).x;
         center.y = region.getPoint(1).y - sq/2 - w;
         break;
      }

      return new RectDouble(
         center.x - sq/2,
         center.y - sq/2,
         sq, sq);
   }

   @Override
   public int getShiftPointBorderIndex() {
      switch (direction) {
      case 0: case 1: return 1;
      }
      return 2;
   }
}