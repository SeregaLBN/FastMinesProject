////////////////////////////////////////////////////////////////////////////////
//                               FastMines project
//                                      © Sergey Krivulya (KSerg, aka SeregaLBN)
// file name: "Parquet1.java"
//
// Реализация класса Parquet1 - паркет в елку (herring-bone parquet)
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
 * Паркет в елку
 * @see BaseCell
 **/
public class Parquet1 extends BaseCell {
   public static class AttrParquet1 extends BaseAttribute {

      @Override
      public SizeDouble getOwnerSize(Matrisize sizeField) {
         double a = getA();
         SizeDouble result = new SizeDouble(
               (sizeField.m*2+1) * a,
               (sizeField.n*2+2) * a);

         if (sizeField.m == 1)
            result.height -= a;

         return result;
      }

      @Override
      public int getNeighborNumber(int direction) { return 6; }
      @Override
      public int getVertexNumber(int direction) { return 4; }
      @Override
      public double getVertexIntersection() { return 3; }
      @Override
      public Size GetDirectionSizeField() { return new Size(2, 1); }
      @Override
      protected double getA() { return Math.sqrt(getArea())/2; }
      @Override
      public double getSq(int borderWidth) {
         double w = borderWidth/2.;
         return getA()-w*SQRT2;
      }
   }

   public Parquet1(AttrParquet1 attr, Coord coord) {
      super(attr, coord,
                 coord.x&1 // 0..1
            );
   }

   @Override
   public AttrParquet1 getAttr() {
      return (AttrParquet1) super.getAttr();
   }

   @Override
   protected List<Coord> getCoordsNeighbor() {
      List<Coord> neighborCoord = new ArrayList<>(getAttr().getNeighborNumber(getDirection()));

      // определяю координаты соседей
      boolean bdir = (direction != 0);
      neighborCoord.add(new Coord(bdir ? coord.x  : coord.x-1, coord.y-1));
      neighborCoord.add(new Coord(bdir ? coord.x-1: coord.x  , bdir ? coord.y  : coord.y-1));
      neighborCoord.add(new Coord(       coord.x+1           , bdir ? coord.y  : coord.y-1));
      neighborCoord.add(new Coord(       coord.x-1           , bdir ? coord.y+1: coord.y));
      neighborCoord.add(new Coord(bdir ? coord.x  : coord.x+1, bdir ? coord.y+1: coord.y));
      neighborCoord.add(new Coord(bdir ? coord.x+1: coord.x  , coord.y+1));

      return neighborCoord;
   }

   @Override
   protected void CalcRegion() {
      AttrParquet1 attr = getAttr();
      double a = attr.getA();

      switch (direction) {
      case 0:
         region.setPoint(0, a * (2 + 2 * coord.x), a * (0 + 2 * coord.y));
         region.setPoint(1, a * (3 + 2 * coord.x), a * (1 + 2 * coord.y));
         region.setPoint(2, a * (1 + 2 * coord.x), a * (3 + 2 * coord.y));
         region.setPoint(3, a * (0 + 2 * coord.x), a * (2 + 2 * coord.y));
         break;
      case 1:
         region.setPoint(0, a * (1 + 2 * coord.x), a * (1 + 2 * coord.y));
         region.setPoint(1, a * (3 + 2 * coord.x), a * (3 + 2 * coord.y));
         region.setPoint(2, a * (2 + 2 * coord.x), a * (4 + 2 * coord.y));
         region.setPoint(3, a * (0 + 2 * coord.x), a * (2 + 2 * coord.y));
         break;
      }
   }

   @Override
   public RectDouble getRcInner(int borderWidth) {
      AttrParquet1 attr = getAttr();
      double sq = attr.getSq(borderWidth);
      double w = borderWidth/2.;
      boolean bdir = (direction != 0);

      return new RectDouble(
         (bdir ? region.getPoint(0).x: region.getPoint(2).x) + w / SQRT2,
         (bdir ? region.getPoint(3).y: region.getPoint(1).y) + w / SQRT2,
         sq, sq);
   }

   @Override
   public int getShiftPointBorderIndex() { return 2; }
}
