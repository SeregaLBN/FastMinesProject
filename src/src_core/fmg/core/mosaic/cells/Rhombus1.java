////////////////////////////////////////////////////////////////////////////////
//                               FastMines project
//                                      © Sergey Krivulya (KSerg, aka SeregaLBN)
// file name: "Rhombus1.java"
//
// Реализация класса Rhombus1 - 3 ромба, составляющие равносторонний шестиугольник
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
import java.util.Map;

import fmg.common.Color;
import fmg.common.geom.Coord;
import fmg.common.geom.Matrisize;
import fmg.common.geom.PointDouble;
import fmg.common.geom.RectDouble;
import fmg.common.geom.Size;
import fmg.common.geom.SizeDouble;

/**
 * Rhombus1 - 3 ромба, составляющие равносторонний шестиугольник
 * @see BaseCell
 **/
public class Rhombus1 extends BaseCell {
   public static class AttrRhombus1 extends BaseAttribute {
      public AttrRhombus1(double area) {
         super(area);
      }

      @Override
      public SizeDouble getOwnerSize(Matrisize sizeField) {
         double a = getA();
         double r = getR();
         double c = getC();
         SizeDouble result = new SizeDouble(
               c+a   *((sizeField.m+2)/3) +
                (a+c)*((sizeField.m+1)/3) +
                   c *((sizeField.m+0)/3),
                   r * (sizeField.n+1));

         if (sizeField.m == 1)
            result.height -= r;
         if (sizeField.n == 1)
            switch (sizeField.m % 3) {
            case 0: result.width -= a/2; break;
            case 2: result.width -= a; break;
            }

         return result;
      }

      @Override
      public int getNeighborNumber(int direction) { return 10; }
      @Override
      public int getVertexNumber(int direction) { return 4; }
      @Override
      public double getVertexIntersection() { return 4.5; } // (3+3+6+6)/4.
      @Override
      public Size GetDirectionSizeField() { return new Size(3, 2); }
      @Override
      protected double getA() { return Math.sqrt(getArea()*2/SQRT3); }
      protected double getC() { return getA()/2; }
      protected double getH() { return getA()*SQRT3; }
      protected double getR() { return getH()/2; }
      @Override
      public double getSq(int borderWidth) {
         double w = borderWidth/2.;
         return (getA()*SQRT3 - w*4)/(SQRT3+1);
      }

      @Override
      public int getMaxBackgroundFillModeValue() {
         return super.getMaxBackgroundFillModeValue()+1;
      }
   }

   public Rhombus1(AttrRhombus1 attr, Coord coord) {
      super(attr, coord,
            (coord.y&1)*3+(coord.x%3) // 0..5
         );
   }

   @Override
   public AttrRhombus1 getAttr() {
      return (AttrRhombus1) super.getAttr();
   }

   @Override
   protected List<Coord> getCoordsNeighbor() {
      List<Coord> neighborCoord = new ArrayList<>(getAttr().getNeighborNumber(getDirection()));

      // определяю координаты соседей
       switch (direction) {
       case 0:
          neighborCoord.add(new Coord(coord.x+1, coord.y-2));
          neighborCoord.add(new Coord(coord.x+2, coord.y-2));
          neighborCoord.add(new Coord(coord.x-2, coord.y-1));
          neighborCoord.add(new Coord(coord.x-1, coord.y-1));
          neighborCoord.add(new Coord(coord.x  , coord.y-1));
          neighborCoord.add(new Coord(coord.x+1, coord.y-1));
          neighborCoord.add(new Coord(coord.x-1, coord.y  ));
          neighborCoord.add(new Coord(coord.x+1, coord.y  ));
          neighborCoord.add(new Coord(coord.x-1, coord.y+1));
          neighborCoord.add(new Coord(coord.x  , coord.y+1));
          break;
       case 1:
          neighborCoord.add(new Coord(coord.x  , coord.y-2));
          neighborCoord.add(new Coord(coord.x+1, coord.y-2));
          neighborCoord.add(new Coord(coord.x-1, coord.y-1));
          neighborCoord.add(new Coord(coord.x  , coord.y-1));
          neighborCoord.add(new Coord(coord.x-1, coord.y  ));
          neighborCoord.add(new Coord(coord.x+1, coord.y  ));
          neighborCoord.add(new Coord(coord.x-1, coord.y+1));
          neighborCoord.add(new Coord(coord.x  , coord.y+1));
          neighborCoord.add(new Coord(coord.x-1, coord.y+2));
          neighborCoord.add(new Coord(coord.x  , coord.y+2));
          break;
       case 2:
          neighborCoord.add(new Coord(coord.x-1, coord.y-1));
          neighborCoord.add(new Coord(coord.x  , coord.y-1));
          neighborCoord.add(new Coord(coord.x-1, coord.y  ));
          neighborCoord.add(new Coord(coord.x+1, coord.y  ));
          neighborCoord.add(new Coord(coord.x-2, coord.y+1));
          neighborCoord.add(new Coord(coord.x-1, coord.y+1));
          neighborCoord.add(new Coord(coord.x  , coord.y+1));
          neighborCoord.add(new Coord(coord.x+1, coord.y+1));
          neighborCoord.add(new Coord(coord.x-2, coord.y+2));
          neighborCoord.add(new Coord(coord.x-1, coord.y+2));
          break;
       case 3:
          neighborCoord.add(new Coord(coord.x-2, coord.y-2));
          neighborCoord.add(new Coord(coord.x-1, coord.y-2));
          neighborCoord.add(new Coord(coord.x-1, coord.y-1));
          neighborCoord.add(new Coord(coord.x  , coord.y-1));
          neighborCoord.add(new Coord(coord.x+1, coord.y-1));
          neighborCoord.add(new Coord(coord.x+2, coord.y-1));
          neighborCoord.add(new Coord(coord.x-1, coord.y  ));
          neighborCoord.add(new Coord(coord.x+1, coord.y  ));
          neighborCoord.add(new Coord(coord.x  , coord.y+1));
          neighborCoord.add(new Coord(coord.x+1, coord.y+1));
          break;
       case 4:
          neighborCoord.add(new Coord(coord.x  , coord.y-1));
          neighborCoord.add(new Coord(coord.x+1, coord.y-1));
          neighborCoord.add(new Coord(coord.x-1, coord.y  ));
          neighborCoord.add(new Coord(coord.x+1, coord.y  ));
          neighborCoord.add(new Coord(coord.x-1, coord.y+1));
          neighborCoord.add(new Coord(coord.x  , coord.y+1));
          neighborCoord.add(new Coord(coord.x+1, coord.y+1));
          neighborCoord.add(new Coord(coord.x+2, coord.y+1));
          neighborCoord.add(new Coord(coord.x+1, coord.y+2));
          neighborCoord.add(new Coord(coord.x+2, coord.y+2));
          break;
       case 5:
          neighborCoord.add(new Coord(coord.x-1, coord.y-2));
          neighborCoord.add(new Coord(coord.x  , coord.y-2));
          neighborCoord.add(new Coord(coord.x  , coord.y-1));
          neighborCoord.add(new Coord(coord.x+1, coord.y-1));
          neighborCoord.add(new Coord(coord.x-1, coord.y  ));
          neighborCoord.add(new Coord(coord.x+1, coord.y  ));
          neighborCoord.add(new Coord(coord.x  , coord.y+1));
          neighborCoord.add(new Coord(coord.x+1, coord.y+1));
          neighborCoord.add(new Coord(coord.x  , coord.y+2));
          neighborCoord.add(new Coord(coord.x+1, coord.y+2));
          break;
       }

      return neighborCoord;
   }

   @Override
   protected void CalcRegion() {
      AttrRhombus1 attr = getAttr();
      double a = attr.getA();
      double c = attr.getC();
      double h = attr.getH();
      double r = attr.getR();

      // определение координат точек фигуры
      double oX = a*(coord.x/3*3+1)+c; // offset X
      double oY = h*(coord.y/2)    +h; // offset Y

      switch (direction) {
      case 0:
         region.setPoint(0, oX      , oY - h);
         region.setPoint(1, oX - c  , oY - r);
         region.setPoint(2, oX - a-c, oY - r);
         region.setPoint(3, oX - a  , oY - h);
         break;
      case 1:
         region.setPoint(0, oX      , oY - h);
         region.setPoint(1, oX + c  , oY - r);
         region.setPoint(2, oX      , oY    );
         region.setPoint(3, oX - c  , oY - r);
         break;
      case 2:
         region.setPoint(0, oX + a+c, oY - r);
         region.setPoint(1, oX + a  , oY    );
         region.setPoint(2, oX      , oY    );
         region.setPoint(3, oX + c  , oY - r);
         break;
      case 3:
         region.setPoint(0, oX - c  , oY - r);
         region.setPoint(1, oX      , oY    );
         region.setPoint(2, oX - a  , oY    );
         region.setPoint(3, oX - a-c, oY - r);
         break;
      case 4:
         region.setPoint(0, oX + a  , oY    );
         region.setPoint(1, oX + a+c, oY + r);
         region.setPoint(2, oX + c  , oY + r);
         region.setPoint(3, oX      , oY    );
         break;
      case 5:
         region.setPoint(0, oX + a+c, oY - r);
         region.setPoint(1, oX + a+a, oY    );
         region.setPoint(2, oX + a+c, oY + r);
         region.setPoint(3, oX + a  , oY    );
         break;
      }
   }

   @Override
   public RectDouble getRcInner(int borderWidth) {
      AttrRhombus1 attr = getAttr();
      double a = attr.getA();
      double c = attr.getC();
      double h = attr.getH();
      double r = attr.getR();
//      double w = borderWidth/2.;
      double sq  = attr.getSq(borderWidth);
      double sq2 = sq/2;

      double oX = a*(coord.x/3*3+1)+c; // offset X
      double oY = h*(coord.y/2)    +h; // offset Y

      PointDouble center = new PointDouble(); // координата центра квадрата
      switch (direction) {
      case 0: center.x = oX - c*1.5; center.y = oY - r*1.5; break;
      case 1: center.x = oX;         center.y = oY - r;     break;
      case 2: center.x = oX + c*1.5; center.y = oY - r*0.5; break;
      case 3: center.x = oX - c*1.5; center.y = oY - r*0.5; break;
      case 4: center.x = oX + c*1.5; center.y = oY + r*0.5; break;
      case 5: center.x = oX + a+c;   center.y = oY;         break;
      }

      return new RectDouble(
         center.x - sq2,
         center.y - sq2,
         sq, sq);
   }

   @Override
   public int getShiftPointBorderIndex() { return 2; }

   @Override
   public Color getBackgroundFillColor(int fillMode, Color defaultColor, Map<Integer, Color> repositoryColor) {
      if (fillMode == getAttr().getMaxBackgroundFillModeValue()) {
         switch ((getCoord().y%4)*3+(getCoord().x%3)) { // почти как вычисление direction...
         // подсвечиваю 4 группы, составляющие каждая шестигранник из 3х ромбов
         case 0: case  1: case  3: return repositoryColor.get(0);
         case 2: case  4: case  5: return repositoryColor.get(1);
         case 6: case  7: case  9: return repositoryColor.get(2);
         case 8: case 10: case 11: return repositoryColor.get(3);
         }
      }
      return super.getBackgroundFillColor(fillMode, defaultColor, repositoryColor);
   }
}