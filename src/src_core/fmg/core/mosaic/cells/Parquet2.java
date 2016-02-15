////////////////////////////////////////////////////////////////////////////////
//                               FastMines project
//                                      © Sergey Krivulya (KSerg, aka SeregaLBN)
// file name: "Parquet2.java"
//
// Реализация класса Parquet2 - ещё один паркет
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
import fmg.common.geom.RectDouble;
import fmg.common.geom.Size;
import fmg.common.geom.SizeDouble;

/**
 * Паркет. Вариант №2
 * @see BaseCell
 **/
public class Parquet2 extends BaseCell {
   public static class AttrParquet2 extends BaseAttribute {
      public AttrParquet2(double area) {
         super(area);
      }

      @Override
      public SizeDouble getOwnerSize(Matrisize sizeField) {
         double a = getA();
         SizeDouble result = new SizeDouble(
               (sizeField.m*2+2) * a,
               (sizeField.n*2+2) * a);

         if (sizeField.m == 1)
            result.height -= a;

         return result;
      }
   
      @Override
      public int getNeighborNumber(boolean max) { return 7; }
      @Override
      public int getNeighborNumber(int direction) { return 7; }
      @Override
      public int getVertexNumber(int direction) { return 4; }
      @Override
      public double getVertexIntersection() { return 3.5; } // (4+4+3+3) / 4
      @Override
      public Size GetDirectionSizeField() { return new Size(2, 2); }
      @Override
      protected double getA() { return Math.sqrt(getArea())/2; }
      @Override
      public double getSq(int borderWidth) {
         double w = borderWidth/2.;
         return getA()-w*SQRT2;
      }
   }

   public Parquet2(AttrParquet2 attr, Coord coord) {
      super(attr, coord,
                 ((coord.y&1)<<1) + (coord.x&1) // 0..3
            );
   }

   @Override
   public AttrParquet2 getAttr() {
      return (AttrParquet2) super.getAttr();
   }

   @Override
   protected Coord[] GetCoordsNeighbor() {
      Coord[] neighborCoord = new Coord[getAttr().getNeighborNumber(true)];

      // определяю координаты соседей
       switch (direction) {
       case 0:
          neighborCoord[0] = new Coord(coord.x-1, coord.y-1);
          neighborCoord[1] = new Coord(coord.x  , coord.y-1);
          neighborCoord[2] = new Coord(coord.x+1, coord.y-1);
          neighborCoord[3] = new Coord(coord.x-1, coord.y  );
          neighborCoord[4] = new Coord(coord.x+1, coord.y  );
          neighborCoord[5] = new Coord(coord.x  , coord.y+1);
          neighborCoord[6] = new Coord(coord.x+1, coord.y+1);
          break;
       case 1:
          neighborCoord[0] = new Coord(coord.x  , coord.y-1);
          neighborCoord[1] = new Coord(coord.x+1, coord.y-1);
          neighborCoord[2] = new Coord(coord.x-1, coord.y  );
          neighborCoord[3] = new Coord(coord.x+1, coord.y  );
          neighborCoord[4] = new Coord(coord.x-1, coord.y+1);
          neighborCoord[5] = new Coord(coord.x  , coord.y+1);
          neighborCoord[6] = new Coord(coord.x+1, coord.y+1);
          break;
       case 2:
          neighborCoord[0] = new Coord(coord.x-1, coord.y-1);
          neighborCoord[1] = new Coord(coord.x  , coord.y-1);
          neighborCoord[2] = new Coord(coord.x+1, coord.y-1);
          neighborCoord[3] = new Coord(coord.x-1, coord.y  );
          neighborCoord[4] = new Coord(coord.x+1, coord.y  );
          neighborCoord[5] = new Coord(coord.x-1, coord.y+1);
          neighborCoord[6] = new Coord(coord.x  , coord.y+1);
          break;
       case 3:
          neighborCoord[0] = new Coord(coord.x-1, coord.y-1);
          neighborCoord[1] = new Coord(coord.x  , coord.y-1);
          neighborCoord[2] = new Coord(coord.x-1, coord.y  );
          neighborCoord[3] = new Coord(coord.x+1, coord.y  );
          neighborCoord[4] = new Coord(coord.x-1, coord.y+1);
          neighborCoord[5] = new Coord(coord.x  , coord.y+1);
          neighborCoord[6] = new Coord(coord.x+1, coord.y+1);
          break;
       }

      return neighborCoord;
   }

   @Override
   protected void CalcRegion() {
      AttrParquet2 attr = getAttr();
      double a = attr.getA();

      switch (direction) {
      case 0:
         region.setPoint(0, (2 * coord.x + 2) * a, (2 * coord.y + 0) * a);
         region.setPoint(1, (2 * coord.x + 4) * a, (2 * coord.y + 2) * a);
         region.setPoint(2, (2 * coord.x + 3) * a, (2 * coord.y + 3) * a);
         region.setPoint(3, (2 * coord.x + 1) * a, (2 * coord.y + 1) * a);
         break;
      case 1:
         region.setPoint(0, (2 * coord.x + 3) * a, (2 * coord.y + 1) * a);
         region.setPoint(1, (2 * coord.x + 4) * a, (2 * coord.y + 2) * a);
         region.setPoint(2, (2 * coord.x + 2) * a, (2 * coord.y + 4) * a);
         region.setPoint(3, (2 * coord.x + 1) * a, (2 * coord.y + 3) * a);
         break;
      case 2:
         region.setPoint(0, (2 * coord.x + 2) * a, (2 * coord.y + 0) * a);
         region.setPoint(1, (2 * coord.x + 3) * a, (2 * coord.y + 1) * a);
         region.setPoint(2, (2 * coord.x + 1) * a, (2 * coord.y + 3) * a);
         region.setPoint(3, (2 * coord.x + 0) * a, (2 * coord.y + 2) * a);
         break;
      case 3:
         region.setPoint(0, (2 * coord.x + 1) * a, (2 * coord.y + 1) * a);
         region.setPoint(1, (2 * coord.x + 3) * a, (2 * coord.y + 3) * a);
         region.setPoint(2, (2 * coord.x + 2) * a, (2 * coord.y + 4) * a);
         region.setPoint(3, (2 * coord.x + 0) * a, (2 * coord.y + 2) * a);
         break;
      }
   }

   @Override
   public RectDouble getRcInner(int borderWidth) {
      AttrParquet2 attr = getAttr();
      double sq = attr.getSq(borderWidth);
      double w = borderWidth/2.;

      RectDouble square = new RectDouble();
      switch (direction) {
      case 0: case 3:
         square.x = region.getPoint(0).x + w/SQRT2;
         square.y = region.getPoint(3).y + w/SQRT2;
         break;
      case 1: case 2:
         square.x = region.getPoint(2).x + w/SQRT2;
         square.y = region.getPoint(1).y + w/SQRT2;
         break;
      }
      square.width = sq;
      square.height = sq;
      return square;
   }

   @Override
   public int getShiftPointBorderIndex() { return 2; }
}
