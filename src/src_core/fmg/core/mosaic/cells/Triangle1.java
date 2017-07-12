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

package fmg.core.mosaic.cells;

import java.util.ArrayList;
import java.util.List;

import fmg.common.geom.*;

/**
 * Треугольник. Вариант 1 - равносторонний, классика
 * @see BaseCell
 **/
public class Triangle1 extends BaseCell {
   public static class AttrTriangle1 extends BaseAttribute {

      @Override
      public SizeDouble getOwnerSize(Matrisize sizeField) {
         double b = getB();
         double h = getH();
         return new SizeDouble(
               b * (sizeField.m+1),
               h * (sizeField.n+0));
      }

      @Override
      public int getNeighborNumber(int direction) { return 12; }
      @Override
      public int getVertexNumber(int direction) { return 3; }
      @Override
      public double getVertexIntersection() { return 6; }
      @Override
      public Size getDirectionSizeField() { return new Size(2, 2); }
      @Override
      protected double getA() { return getB() * 2.f; } // размер стороны треугольника
      /** пол стороны треугольника */
      protected double getB() { return Math.sqrt(getArea()/SQRT3); }
      /** высота треугольника */
      protected double getH() { return getB() * SQRT3; }
      @Override
      public double getSq(int borderWidth) {
         double w = borderWidth/2.;
         return (getH()*2 - 6*w)/(SQRT3+2);
         //return (getA()*SQRT3 - 6*w)/(SQRT3+2);
      }
   }

   public Triangle1(AttrTriangle1 attr, Coord coord) {
      super(attr, coord,
            ((coord.y&1)<<1)+(coord.x&1) // 0..3
         );
   }

   @Override
   public AttrTriangle1 getAttr() {
      return (AttrTriangle1) super.getAttr();
   }

   @Override
   protected List<Coord> getCoordsNeighbor() {
      List<Coord> neighborCoord = new ArrayList<>(getAttr().getNeighborNumber(getDirection()));

      // определяю координаты соседей
       switch (direction) {
       case 0: case 3:
          neighborCoord.add(new Coord(coord.x-1, coord.y-1));
          neighborCoord.add(new Coord(coord.x  , coord.y-1));
          neighborCoord.add(new Coord(coord.x+1, coord.y-1));
          neighborCoord.add(new Coord(coord.x-2, coord.y  ));
          neighborCoord.add(new Coord(coord.x-1, coord.y  ));
          neighborCoord.add(new Coord(coord.x+1, coord.y  ));
          neighborCoord.add(new Coord(coord.x+2, coord.y  ));
          neighborCoord.add(new Coord(coord.x-2, coord.y+1));
          neighborCoord.add(new Coord(coord.x-1, coord.y+1));
          neighborCoord.add(new Coord(coord.x  , coord.y+1));
          neighborCoord.add(new Coord(coord.x+1, coord.y+1));
          neighborCoord.add(new Coord(coord.x+2, coord.y+1));
          break;
       case 1: case 2:
          neighborCoord.add(new Coord(coord.x-2, coord.y-1));
          neighborCoord.add(new Coord(coord.x-1, coord.y-1));
          neighborCoord.add(new Coord(coord.x  , coord.y-1));
          neighborCoord.add(new Coord(coord.x+1, coord.y-1));
          neighborCoord.add(new Coord(coord.x+2, coord.y-1));
          neighborCoord.add(new Coord(coord.x-2, coord.y  ));
          neighborCoord.add(new Coord(coord.x-1, coord.y  ));
          neighborCoord.add(new Coord(coord.x+1, coord.y  ));
          neighborCoord.add(new Coord(coord.x+2, coord.y  ));
          neighborCoord.add(new Coord(coord.x-1, coord.y+1));
          neighborCoord.add(new Coord(coord.x  , coord.y+1));
          neighborCoord.add(new Coord(coord.x+1, coord.y+1));
          break;
       }

      return neighborCoord;
   }

   @Override
   protected void calcRegion() {
      AttrTriangle1 attr = getAttr();
      double a = attr.getA();
      double b = attr.getB();
      double h = attr.getH();

      double oX = a*(coord.x>>1); // offset X
      double oY = h* coord.y;     // offset Y

      switch (direction) {
      case 0:
         region.setPoint(0, oX +   b, oY    );
         region.setPoint(1, oX + a  , oY + h);
         region.setPoint(2, oX      , oY + h);
         break;
      case 1:
         region.setPoint(0, oX + a+b, oY    );
         region.setPoint(1, oX + a  , oY + h);
         region.setPoint(2, oX +   b, oY    );
         break;
      case 2:
         region.setPoint(0, oX + a  , oY    );
         region.setPoint(1, oX +   b, oY + h);
         region.setPoint(2, oX      , oY    );
         break;
      case 3:
         region.setPoint(0, oX + a  , oY    );
         region.setPoint(1, oX + a+b, oY + h);
         region.setPoint(2, oX +   b, oY + h);
         break;
      }
   }

   @Override
   public RectDouble getRcInner(int borderWidth) {
      AttrTriangle1 attr = getAttr();
      double b = attr.getB();
      double sq = attr.getSq(borderWidth);
      double w = borderWidth/2.;

      PointDouble center = new PointDouble(); // координата вписанного в фигуру квадрата (не совпадает с центром фигуры)
      switch (direction) {
      case 0: case 3:
         center.x = region.getPoint(2).x + b;
         center.y = region.getPoint(2).y - sq/2 - w;
         break;
      case 1: case 2:
         center.x = region.getPoint(2).x + b;
         center.y = region.getPoint(2).y + sq/2 + w;
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
      case 0: case 3: return 2;
      }
      return 1;
   }
}