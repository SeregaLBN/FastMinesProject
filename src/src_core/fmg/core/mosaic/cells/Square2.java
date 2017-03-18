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

package fmg.core.mosaic.cells;

import java.util.ArrayList;
import java.util.List;

import fmg.common.geom.*;

/**
 * Квадрат. Вариант 2 - сдвинутые ряды
 * @see BaseCell
 **/
public class Square2 extends BaseCell {
   public static class AttrSquare2 extends BaseAttribute {

      @Override
      public SizeDouble getOwnerSize(Matrisize sizeField) {
         double a = getA(); // размер стороны квадрата
         return new SizeDouble(
            sizeField.m * a + a/2,
            sizeField.n * a);
      }

      @Override
      public int getNeighborNumber(int direction) { return 6; }
      @Override
      public int getVertexNumber(int direction) { return 4; }
      @Override
      public double getVertexIntersection() { return 3; }
      @Override
      public Size GetDirectionSizeField() { return new Size(1, 2); }
      @Override
      protected double getA() { return Math.sqrt(getArea()); }
      @Override
      public double getSq(int borderWidth) {
         double w = borderWidth/2.;
         return getA()-2*w;
      }
   }

   public Square2(AttrSquare2 attr, Coord coord) {
      super(attr, coord,
                 coord.y&1 // 0..1
            );
   }

   @Override
   public AttrSquare2 getAttr() {
      return (AttrSquare2) super.getAttr();
   }

   @Override
   protected List<Coord> getCoordsNeighbor() {
      List<Coord> neighborCoord = new ArrayList<>(getAttr().getNeighborNumber(getDirection()));

      // определяю координаты соседей
      neighborCoord.add(new Coord(coord.x- direction   , coord.y-1));
      neighborCoord.add(new Coord(coord.x+(direction^1), coord.y-1));
      neighborCoord.add(new Coord(coord.x-1            , coord.y  ));
      neighborCoord.add(new Coord(coord.x+1            , coord.y  ));
      neighborCoord.add(new Coord(coord.x- direction   , coord.y+1));
      neighborCoord.add(new Coord(coord.x+(direction^1), coord.y+1));

      return neighborCoord;
   }

   @Override
   public boolean PointInRegion(PointDouble point) {
      if ((point.x < region.getPoint(3).x) || (point.x >= region.getPoint(0).x) ||
         (point.y < region.getPoint(0).y) || (point.y >= region.getPoint(2).y))
         return false;
      return true;
   }

   @Override
   protected void CalcRegion() {
      AttrSquare2 attr = getAttr();
      double a = attr.getA();

      double x1 = a * (coord.x + 0) + ((direction != 0) ? 0 : a / 2);
      double x2 = a * (coord.x + 1) + ((direction != 0) ? 0 : a / 2);
      double y1 = a * (coord.y + 0);
      double y2 = a * (coord.y + 1);

      region.setPoint(0, x2, y1);
      region.setPoint(1, x2, y2);
      region.setPoint(2, x1, y2);
      region.setPoint(3, x1, y1);
   }

   @Override
   public RectDouble getRcInner(int borderWidth) {
      AttrSquare2 attr = getAttr();
      double sq = attr.getSq(borderWidth);
      double w = borderWidth/2.;

      return new RectDouble(
         region.getPoint(3).x + w,
         region.getPoint(3).y + w,
         sq, sq);
   }

   @Override
   public int getShiftPointBorderIndex() { return 2; }
}
