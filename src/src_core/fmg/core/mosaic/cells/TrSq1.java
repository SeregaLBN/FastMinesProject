////////////////////////////////////////////////////////////////////////////////
//                               FastMines project
//                                      © Sergey Krivulya (KSerg, aka SeregaLBN)
// file name: "TrSq1.java"
//
// Реализация класса TrSq1 - мозаика из 4х треугольников и 2х квадратов
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
 * Комбинация. Мозаика из 4х треугольников и 2х квадратов
 * @see BaseCell
 **/
public class TrSq1 extends BaseCell {
   public static class AttrTrSq1 extends BaseAttribute {

      @Override
      public SizeDouble getSize(Matrisize sizeField) {
         double b = getB();
         double k = getK();
         double n = getN();
         double m = getM();
         SizeDouble result = new SizeDouble(
               b+n*((sizeField.m-1+2)/3)+
                 k*((sizeField.m-1+1)/3)+
                 m*((sizeField.m-1+0)/3),
               b+n* (sizeField.n-1));

         if (sizeField.n == 1) {
            if ((sizeField.m % 3) == 2) result.width -= m;
            if ((sizeField.m % 3) == 0) result.width -= k;
         }
         if (sizeField.m == 1)
            if ((sizeField.n & 1) == 0)
               result.height -= m;

         return result;
      }

      @Override
      public int getNeighborNumber(int direction) {
          switch (direction) {
          case 1: case 2: case 3: case 5: return 9;
          case 0: case 4: return 12;
          default:
             throw new IllegalArgumentException("Invalid value direction=" + direction);
          }
      }
      @Override
      public int getVertexNumber(int direction) {
         switch (direction) {
         case 1: case 2: case 3: case 5: return 3;
         case 0: case 4: return 4;
         default:
            throw new IllegalArgumentException("Invalid value direction="+direction);
          }
      }
      @Override
      public double getVertexIntersection() { return 5.; }
      @Override
      public Size getDirectionSizeField() { return new Size(3, 2); }
      @Override
      protected double getA() { return Math.sqrt(3*getArea()/(1+SQRT3/2)); }
      protected double getB() { return getN()+getM(); }
      protected double getK() { return getN()-getM(); }
      protected double getN() { return getA()*SIN75; }
      protected double getM() { return getA()*SIN15; }
      @Override
      public double getSq(double borderWidth) {
         double w = borderWidth/2.;
         return (getA()*SQRT3 - w*6) / (4*SIN75);
      }
   }

   public TrSq1(AttrTrSq1 attr, Coord coord) {
      super(attr, coord,
            (coord.y&1)*3+(coord.x%3) // 0..5
         );
   }

   @Override
   public AttrTrSq1 getAttr() {
      return (AttrTrSq1) super.getAttr();
   }

   @Override
   protected List<Coord> getCoordsNeighbor() {
      List<Coord> neighborCoord = new ArrayList<>(getAttr().getNeighborNumber(getDirection()));

      // определяю координаты соседей
       switch (direction) {
       case 0:
          neighborCoord.add(new Coord(coord.x-2, coord.y-1));
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
          break;
       case 1:
          neighborCoord.add(new Coord(coord.x-2, coord.y-1));
          neighborCoord.add(new Coord(coord.x-1, coord.y-1));
          neighborCoord.add(new Coord(coord.x  , coord.y-1));
          neighborCoord.add(new Coord(coord.x+1, coord.y-1));
          neighborCoord.add(new Coord(coord.x-1, coord.y  ));
          neighborCoord.add(new Coord(coord.x+1, coord.y  ));
          neighborCoord.add(new Coord(coord.x+2, coord.y  ));
          neighborCoord.add(new Coord(coord.x-1, coord.y+1));
          neighborCoord.add(new Coord(coord.x  , coord.y+1));
          break;
       case 2:
          neighborCoord.add(new Coord(coord.x-1, coord.y-1));
          neighborCoord.add(new Coord(coord.x  , coord.y-1));
          neighborCoord.add(new Coord(coord.x-2, coord.y  ));
          neighborCoord.add(new Coord(coord.x-1, coord.y  ));
          neighborCoord.add(new Coord(coord.x+1, coord.y  ));
          neighborCoord.add(new Coord(coord.x-2, coord.y+1));
          neighborCoord.add(new Coord(coord.x-1, coord.y+1));
          neighborCoord.add(new Coord(coord.x  , coord.y+1));
          neighborCoord.add(new Coord(coord.x+1, coord.y+1));
          break;
       case 3:
          neighborCoord.add(new Coord(coord.x-1, coord.y-1));
          neighborCoord.add(new Coord(coord.x  , coord.y-1));
          neighborCoord.add(new Coord(coord.x+1, coord.y-1));
          neighborCoord.add(new Coord(coord.x+2, coord.y-1));
          neighborCoord.add(new Coord(coord.x-2, coord.y  ));
          neighborCoord.add(new Coord(coord.x-1, coord.y  ));
          neighborCoord.add(new Coord(coord.x+1, coord.y  ));
          neighborCoord.add(new Coord(coord.x  , coord.y+1));
          neighborCoord.add(new Coord(coord.x+1, coord.y+1));
          break;
       case 4:
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
          neighborCoord.add(new Coord(coord.x+2, coord.y+1));
          break;
       case 5:
          neighborCoord.add(new Coord(coord.x  , coord.y-1));
          neighborCoord.add(new Coord(coord.x+1, coord.y-1));
          neighborCoord.add(new Coord(coord.x-1, coord.y  ));
          neighborCoord.add(new Coord(coord.x+1, coord.y  ));
          neighborCoord.add(new Coord(coord.x+2, coord.y  ));
          neighborCoord.add(new Coord(coord.x-1, coord.y+1));
          neighborCoord.add(new Coord(coord.x  , coord.y+1));
          neighborCoord.add(new Coord(coord.x+1, coord.y+1));
          neighborCoord.add(new Coord(coord.x+2, coord.y+1));
          break;
       }

      return neighborCoord;
   }

   @Override
   protected void calcRegion() {
      AttrTrSq1 attr = getAttr();
      double b = attr.getB();
      double k = attr.getK();
      double n = attr.getN();
      double m = attr.getM();

      double oX = b + n * (coord.x/3*2); // offset X
      double oY = n + n*2*(coord.y/2);   // offset Y

      switch (direction) {
      case 0:
         region.setPoint(0, oX - m  , oY - n);
         region.setPoint(1, oX      , oY    );
         region.setPoint(2, oX - n  , oY + m);
         region.setPoint(3, oX - b  , oY - k);
         break;
      case 1:
         region.setPoint(1, oX      , oY    );
         region.setPoint(2, oX - m  , oY - n);
         region.setPoint(0, oX + k  , oY - k);
         break;
      case 2:
         region.setPoint(0, oX + k  , oY - k);
         region.setPoint(1, oX + n  , oY + m);
         region.setPoint(2, oX      , oY    );
         break;
      case 3:
         region.setPoint(1, oX - m  , oY + n);
         region.setPoint(2, oX - n  , oY + m);
         region.setPoint(0, oX      , oY    );
         break;
      case 4:
         region.setPoint(0, oX + n  , oY + m);
         region.setPoint(3, oX      , oY    );
         region.setPoint(2, oX - m  , oY + n);
         region.setPoint(1, oX + k  , oY + b);
         break;
      case 5:
         region.setPoint(0, oX + n  , oY + m);
         region.setPoint(1, oX + n+k, oY + n);
         region.setPoint(2, oX + k  , oY + b);
         break;
      }
   }

   @Override
   public RectDouble getRcInner(double borderWidth) {
      AttrTrSq1 attr = getAttr();
      double b = attr.getB();
      double k = attr.getK();
      double n = attr.getN();
      double m = attr.getM();
      double w = borderWidth/2.;
      double sq = attr.getSq(borderWidth);
      double sq2 = sq/2;

      double oX = b + n * (coord.x/3*2); // offset X
      double oY = n + n*2*(coord.y/2);   // offset Y


      double ksw1 = k/2-sq2-w/SQRT2;
      double ksw2 = k/2+sq2+w/SQRT2;
      PointDouble center = new PointDouble(); // координата вписанного в фигуру квадрата (не совпадает с центром фигуры)
      switch (direction) {
      case 0:  center.x = oX - b/2;    center.y = oY - k/2;    break;
      case 1:  center.x = oX + ksw1;   center.y = oY - ksw2;   break;
      case 2:  center.x = oX + ksw2;   center.y = oY - ksw1;   break;
      case 3:  center.x = oX + ksw2-n; center.y = oY - ksw2+n; break;
      case 4:  center.x = oX + k/2;    center.y = oY + b/2;    break;
      case 5:  center.x = oX + ksw1+n; center.y = oY + ksw2+m; break;
      }

      return new RectDouble(
         center.x - sq2,
         center.y - sq2,
         sq, sq);
   }

   @Override
   public int getShiftPointBorderIndex() {
      switch (direction) {
      case 1: case 3: return 1;
      }
      return 2;
   }
}