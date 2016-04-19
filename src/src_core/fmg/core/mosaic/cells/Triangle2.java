////////////////////////////////////////////////////////////////////////////////
//                               FastMines project
//                                      © Sergey Krivulya (KSerg, aka SeregaLBN)
// file name: "Triangle2.java"
//
// Реализация класса Triangle2 - равносторонний треугольник (вариант поля №2 - ёлочкой)
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

import fmg.common.geom.Coord;
import fmg.common.geom.Matrisize;
import fmg.common.geom.PointDouble;
import fmg.common.geom.RectDouble;
import fmg.common.geom.Size;
import fmg.common.geom.SizeDouble;

/**
 * Треугольник. Вариант 2 - равносторонний, ёлочкой 
 * @see BaseCell
 **/
public class Triangle2 extends BaseCell {
   public static class AttrTriangle2 extends BaseAttribute {
      public AttrTriangle2(double area) {
         super(area);
      }

      @Override
      public SizeDouble getOwnerSize(Matrisize sizeField) {
         double b = getB();
         double h = getH();
         return new SizeDouble(
               b * (sizeField.m+1),
               h * (sizeField.n+0));
      }
   
      @Override
      public int getNeighborNumber(boolean max) { return 8; }
      @Override
      public int getNeighborNumber(int direction) { return 8; }
      @Override
      public int getVertexNumber(int direction) { return 3; }
      @Override
      public double getVertexIntersection() { return 3.75; } // (4+4+4+3)/4.
      @Override
      public Size GetDirectionSizeField() { return new Size(2, 1); }
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
      }
   }

   public Triangle2(AttrTriangle2 attr, Coord coord) {
      super(attr, coord,
            coord.x&1 // 0..1
         );
   }

   @Override
   public AttrTriangle2 getAttr() {
      return (AttrTriangle2) super.getAttr();
   }

   @Override
   protected List<Coord> getCoordsNeighbor() {
      List<Coord> neighborCoord = new ArrayList<>(getAttr().getNeighborNumber(true));

      // определяю координаты соседей
       switch (direction) {
       case 0: case 3:
          neighborCoord[ 0] = new Coord(coord.x  , coord.y-1);
          neighborCoord[ 1] = new Coord(coord.x-2, coord.y  );
          neighborCoord[ 2] = new Coord(coord.x-1, coord.y  );
          neighborCoord[ 3] = new Coord(coord.x+1, coord.y  );
          neighborCoord[ 4] = new Coord(coord.x+2, coord.y  );
          neighborCoord[ 5] = new Coord(coord.x-1, coord.y+1);
          neighborCoord[ 6] = new Coord(coord.x  , coord.y+1);
          neighborCoord[ 7] = new Coord(coord.x+1, coord.y+1);
          break;                                          
       case 1: case 2:                                    
          neighborCoord[ 0] = new Coord(coord.x-1, coord.y-1);
          neighborCoord[ 1] = new Coord(coord.x  , coord.y-1);
          neighborCoord[ 2] = new Coord(coord.x+1, coord.y-1);
          neighborCoord[ 3] = new Coord(coord.x-2, coord.y  );
          neighborCoord[ 4] = new Coord(coord.x-1, coord.y  );
          neighborCoord[ 5] = new Coord(coord.x+1, coord.y  );
          neighborCoord[ 6] = new Coord(coord.x+2, coord.y  );
          neighborCoord[ 7] = new Coord(coord.x  , coord.y+1);
          break;
       }

      return neighborCoord;
   }

   @Override
   protected void CalcRegion() {
      AttrTriangle2 attr = getAttr();
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
      }
   }

   @Override
   public RectDouble getRcInner(int borderWidth) {
      AttrTriangle2 attr = getAttr();
      double b = attr.getB();
      double sq = attr.getSq(borderWidth);
      double w = borderWidth/2.;

      PointDouble center = new PointDouble(); // координата вписанного в фигуру квадрата (не совпадает с центром фигуры)
      switch (direction) {
      case 0:
         center.x = region.getPoint(2).x + b;
         center.y = region.getPoint(2).y - sq/2 - w;
         break;
      case 1:
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
      case 1: return 1;
      }
      return 2;
   }
}