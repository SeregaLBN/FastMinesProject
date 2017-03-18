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

package fmg.core.mosaic.cells;

import java.util.ArrayList;
import java.util.List;

import fmg.common.geom.*;

/**
 * Пятиугольник. Тип №2 и №4 - равносторонний
 * @see BaseCell
 **/
public class PentagonT24 extends BaseCell {
   public static class AttrPentagonT24 extends BaseAttribute {

      @Override
      public SizeDouble getOwnerSize(Matrisize sizeField) {
         double a = getA();
         double b = getB();
         SizeDouble result = new SizeDouble(
               b + sizeField.m * a,
               b + sizeField.n * a);

         if (sizeField.n == 1)
            result.width -= getC();

            return result;
      }

      @Override
      public int getNeighborNumber(int direction) { return 7; }
      @Override
      public int getVertexNumber(int direction) { return 5; }
      @Override
      public double getVertexIntersection() { return 3.4; } // (3+3+3+4+4)/5.
      @Override
      public Size GetDirectionSizeField() { return new Size(2, 2); }
      @Override
      protected double getA() { return Math.sqrt(getArea()); }
      protected double getB() { return getA()*6/11; }
      protected double getC() { return getB()/2; }
      @Override
      public double getSq(int borderWidth) {
         double w = borderWidth/2.;
         return getA()*8/11-(w+w/SIN135a) / SQRT2;
      }
   }

   public PentagonT24(AttrPentagonT24 attr, Coord coord) {
      super(attr, coord,
                 ((coord.y&1)<<1) + (coord.x&1) // 0..3
            );
   }

   @Override
   public AttrPentagonT24 getAttr() {
      return (AttrPentagonT24) super.getAttr();
   }

   @Override
   protected List<Coord> getCoordsNeighbor() {
      List<Coord> neighborCoord = new ArrayList<>(getAttr().getNeighborNumber(getDirection()));

      // определяю координаты соседей
      switch (direction) {
      case 0:
         neighborCoord.add(new Coord(coord.x-1, coord.y-1));
         neighborCoord.add(new Coord(coord.x  , coord.y-1));
         neighborCoord.add(new Coord(coord.x-1, coord.y  ));
         neighborCoord.add(new Coord(coord.x+1, coord.y  ));
         neighborCoord.add(new Coord(coord.x-1, coord.y+1));
         neighborCoord.add(new Coord(coord.x  , coord.y+1));
         neighborCoord.add(new Coord(coord.x+1, coord.y+1));
         break;
      case 1:
         neighborCoord.add(new Coord(coord.x-1, coord.y-1));
         neighborCoord.add(new Coord(coord.x  , coord.y-1));
         neighborCoord.add(new Coord(coord.x+1, coord.y-1));
         neighborCoord.add(new Coord(coord.x-1, coord.y  ));
         neighborCoord.add(new Coord(coord.x+1, coord.y  ));
         neighborCoord.add(new Coord(coord.x-1, coord.y+1));
         neighborCoord.add(new Coord(coord.x  , coord.y+1));
         break;
      case 2:
         neighborCoord.add(new Coord(coord.x  , coord.y-1));
         neighborCoord.add(new Coord(coord.x+1, coord.y-1));
         neighborCoord.add(new Coord(coord.x-1, coord.y  ));
         neighborCoord.add(new Coord(coord.x+1, coord.y  ));
         neighborCoord.add(new Coord(coord.x-1, coord.y+1));
         neighborCoord.add(new Coord(coord.x  , coord.y+1));
         neighborCoord.add(new Coord(coord.x+1, coord.y+1));
         break;
      case 3:
         neighborCoord.add(new Coord(coord.x-1, coord.y-1));
         neighborCoord.add(new Coord(coord.x  , coord.y-1));
         neighborCoord.add(new Coord(coord.x+1, coord.y-1));
         neighborCoord.add(new Coord(coord.x-1, coord.y  ));
         neighborCoord.add(new Coord(coord.x+1, coord.y  ));
         neighborCoord.add(new Coord(coord.x  , coord.y+1));
         neighborCoord.add(new Coord(coord.x+1, coord.y+1));
         break;
      }

      return neighborCoord;
   }

   @Override
   protected void CalcRegion() {
      AttrPentagonT24 attr = getAttr();
      double a = attr.getA();
      double b = attr.getB();
      double c = attr.getC();

      // определение координат точек фигуры
      double oX = a*((coord.x>>1)<<1); // offset X
      double oY = a*((coord.y>>1)<<1); // offset Y
      switch (direction) {
      case 0:
         region.setPoint(0, oX +       a, oY + b      );
         region.setPoint(1, oX + c +   a, oY + c +   a);
         region.setPoint(2, oX + b      , oY + b +   a);
         region.setPoint(3, oX          , oY +       a);
         region.setPoint(4, oX + c      , oY + c      );
         break;
      case 1:
         region.setPoint(0, oX + c + 2*a, oY + c      );
         region.setPoint(1, oX +     2*a, oY +       a);
         region.setPoint(2, oX + c +   a, oY + c +   a);
         region.setPoint(3, oX +       a, oY + b      );
         region.setPoint(4, oX + b +   a, oY          );
         break;
      case 2:
         region.setPoint(0, oX + c +   a, oY + c +   a);
         region.setPoint(1, oX + b +   a, oY +     2*a);
         region.setPoint(2, oX +       a, oY + b + 2*a);
         region.setPoint(3, oX + c      , oY + c + 2*a);
         region.setPoint(4, oX + b      , oY + b +   a);
         break;
      case 3:
         region.setPoint(0, oX +     2*a, oY +       a);
         region.setPoint(1, oX + b + 2*a, oY + b +   a);
         region.setPoint(2, oX + c + 2*a, oY + c + 2*a);
         region.setPoint(3, oX + b +   a, oY +     2*a);
         region.setPoint(4, oX + c +   a, oY + c +   a);
         break;
      }
   }

   @Override
   public RectDouble getRcInner(int borderWidth) {
      AttrPentagonT24 attr = getAttr();
      double sq = attr.getSq(borderWidth);
      double w = borderWidth/2.;
      double w2 = w/SQRT2;

      RectDouble square = new RectDouble();
      switch (direction) {
      case 0:
         square.x = region.getPoint(4).x+w2;
         square.y = region.getPoint(1).y-w2 - sq;
         break;
      case 1:
         square.x = region.getPoint(2).x+w2;
         square.y = region.getPoint(0).y+w2;
         break;
      case 2:
         square.x = region.getPoint(0).x-w2 - sq;
         square.y = region.getPoint(3).y-w2 - sq;
         break;
      case 3:
         square.x = region.getPoint(2).x-w2 - sq;
         square.y = region.getPoint(4).y+w2;
         break;
      }
      square.width = sq;
      square.height = sq;
      return square;
   }

   @Override
   public int getShiftPointBorderIndex() {
      switch (direction) {
      case 0: case 1:
         return 2;
      }
      return 3;
   }
}
