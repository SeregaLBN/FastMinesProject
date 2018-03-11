////////////////////////////////////////////////////////////////////////////////
//                               FastMines project
//                                      © Sergey Krivulya (KSerg, aka SeregaLBN)
// file name: "PentagonT10.java"
//
// Реализация класса PentagonT10 - 5-ти угольник, тип №10
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
import fmg.common.geom.*;

/**
 * Пятиугольник. Тип №10
 * @see BaseCell
 **/
public class PentagonT10 extends BaseCell {
   public static class AttrPentagonT10 extends BaseAttribute {

      @Override
      public SizeDouble getSize(Matrisize sizeField) {
         double a = getA();
         SizeDouble result = new SizeDouble(
               2*a +
               5*a*((sizeField.m+1)/2) +
                 a*((sizeField.m+0)/2),
               2*a +
               3*a*((sizeField.n+2)/3) +
               3*a*((sizeField.n+1)/3) +
                 a*((sizeField.n+0)/3));

         if (sizeField.n == 1)
            if ((sizeField.m & 1) == 1)
               result.width -= 3*a;
            else
               result.width -= a;
         if (sizeField.n == 2)
            if ((sizeField.m & 1) == 1)
               result.width -= 2*a;
            else
               result.width -= a;

         if (sizeField.m == 1)
            if (((sizeField.n % 6) == 4) ||
               ((sizeField.n % 6) == 5))
               result.height -= 2*a;

         return result;
      }

      @Override
      public int getNeighborNumber(int direction) {
         switch (direction) {
         case 0: case 1: case 6: case 7: return 7;
         case 2: case 3: case 4: case 5: case 8: case 9: case 10: case 11: return 6;
         default:
            throw new IllegalArgumentException("Invalid value direction=" + direction);
         }
      }
      @Override
      public int getVertexNumber(int direction) { return 5; }

      static double vertexIntersection = 0.;
      @Override
      public double getVertexIntersection() {
         if (vertexIntersection < 1) {
            final int cntDirection = getDirectionCount(); // 0..11
            double sum = 0;
            for (int dir=0; dir<cntDirection; dir++)
               switch (dir) {
               case 0: case 1: case 6: case 7:
                  sum += 3;
                  break;
               case 2: case 3: case 4: case 5: case 8: case 9: case 10: case 11:
                  sum += 16./5.;
                  break;
               default:
                  throw new RuntimeException("Забыл case #" + dir);
               }
            vertexIntersection = sum / cntDirection;
//            System.out.println("PentagonT10::getVertexNeighbor == " + vertexIntersection);
         }
         return vertexIntersection;
      }

      @Override
      public Size getDirectionSizeField() { return new Size(2, 6); }
      @Override
      protected double getA() { return Math.sqrt(getArea()/7); }
      @Override
      public double getSq(int borderWidth) {
         double w = borderWidth/2.;
         return 2*(getA()-w);
      }

      @Override
      public int getMaxBackgroundFillModeValue() {
         return super.getMaxBackgroundFillModeValue() + 1;
//         return 1;
      }
   }

   public PentagonT10(AttrPentagonT10 attr, Coord coord) {
      super(attr, coord,
            ((coord.y%6)<<1) + (coord.x&1) // 0..11
         );
   }

   @Override
   public AttrPentagonT10 getAttr() {
      return (AttrPentagonT10) super.getAttr();
   }

   @Override
   protected List<Coord> getCoordsNeighbor() {
      List<Coord> neighborCoord = new ArrayList<>(getAttr().getNeighborNumber(getDirection()));

      // определяю координаты соседей
      switch (direction) {
      case 0:
         neighborCoord.add(new Coord(coord.x  , coord.y-1));
         neighborCoord.add(new Coord(coord.x-1, coord.y  ));
         neighborCoord.add(new Coord(coord.x+1, coord.y  ));
         neighborCoord.add(new Coord(coord.x-1, coord.y+1));
         neighborCoord.add(new Coord(coord.x  , coord.y+1));
         neighborCoord.add(new Coord(coord.x+1, coord.y+1));
         neighborCoord.add(new Coord(coord.x-1, coord.y+2));
         break;
      case 1:
         neighborCoord.add(new Coord(coord.x  , coord.y-2));
         neighborCoord.add(new Coord(coord.x-1, coord.y-1));
         neighborCoord.add(new Coord(coord.x  , coord.y-1));
         neighborCoord.add(new Coord(coord.x+1, coord.y-1));
         neighborCoord.add(new Coord(coord.x-1, coord.y  ));
         neighborCoord.add(new Coord(coord.x+1, coord.y  ));
         neighborCoord.add(new Coord(coord.x  , coord.y+1));
         break;
      case 2:
         neighborCoord.add(new Coord(coord.x  , coord.y-1));
         neighborCoord.add(new Coord(coord.x+1, coord.y  ));
         neighborCoord.add(new Coord(coord.x-1, coord.y+1));
         neighborCoord.add(new Coord(coord.x  , coord.y+1));
         neighborCoord.add(new Coord(coord.x+1, coord.y+1));
         neighborCoord.add(new Coord(coord.x  , coord.y+2));
         break;
      case 3:
         neighborCoord.add(new Coord(coord.x-1, coord.y-1));
         neighborCoord.add(new Coord(coord.x  , coord.y-1));
         neighborCoord.add(new Coord(coord.x+1, coord.y-1));
         neighborCoord.add(new Coord(coord.x-1, coord.y  ));
         neighborCoord.add(new Coord(coord.x-1, coord.y+1));
         neighborCoord.add(new Coord(coord.x  , coord.y+1));
         break;
      case 4:
         neighborCoord.add(new Coord(coord.x  , coord.y-1));
         neighborCoord.add(new Coord(coord.x+1, coord.y-1));
         neighborCoord.add(new Coord(coord.x+1, coord.y  ));
         neighborCoord.add(new Coord(coord.x  , coord.y+1));
         neighborCoord.add(new Coord(coord.x+1, coord.y+1));
         neighborCoord.add(new Coord(coord.x+2, coord.y+1));
         break;
      case 5:
         neighborCoord.add(new Coord(coord.x+1, coord.y-2));
         neighborCoord.add(new Coord(coord.x-1, coord.y-1));
         neighborCoord.add(new Coord(coord.x  , coord.y-1));
         neighborCoord.add(new Coord(coord.x+1, coord.y-1));
         neighborCoord.add(new Coord(coord.x-1, coord.y  ));
         neighborCoord.add(new Coord(coord.x+1, coord.y+1));
         break;
      case 6:
         neighborCoord.add(new Coord(coord.x  , coord.y-2));
         neighborCoord.add(new Coord(coord.x-2, coord.y-1));
         neighborCoord.add(new Coord(coord.x-1, coord.y-1));
         neighborCoord.add(new Coord(coord.x  , coord.y-1));
         neighborCoord.add(new Coord(coord.x-1, coord.y  ));
         neighborCoord.add(new Coord(coord.x+1, coord.y  ));
         neighborCoord.add(new Coord(coord.x  , coord.y+1));
         break;
      case 7:
         neighborCoord.add(new Coord(coord.x-1, coord.y-1));
         neighborCoord.add(new Coord(coord.x-1, coord.y  ));
         neighborCoord.add(new Coord(coord.x+1, coord.y  ));
         neighborCoord.add(new Coord(coord.x-1, coord.y+1));
         neighborCoord.add(new Coord(coord.x  , coord.y+1));
         neighborCoord.add(new Coord(coord.x+1, coord.y+1));
         neighborCoord.add(new Coord(coord.x  , coord.y+2));
         break;
      case 8:
         neighborCoord.add(new Coord(coord.x-1, coord.y-1));
         neighborCoord.add(new Coord(coord.x  , coord.y-1));
         neighborCoord.add(new Coord(coord.x+1, coord.y-1));
         neighborCoord.add(new Coord(coord.x+1, coord.y  ));
         neighborCoord.add(new Coord(coord.x-1, coord.y+1));
         neighborCoord.add(new Coord(coord.x  , coord.y+1));
         break;
      case 9:
         neighborCoord.add(new Coord(coord.x  , coord.y-1));
         neighborCoord.add(new Coord(coord.x-1, coord.y  ));
         neighborCoord.add(new Coord(coord.x-2, coord.y+1));
         neighborCoord.add(new Coord(coord.x-1, coord.y+1));
         neighborCoord.add(new Coord(coord.x  , coord.y+1));
         neighborCoord.add(new Coord(coord.x  , coord.y+2));
         break;
      case 10:
         neighborCoord.add(new Coord(coord.x  , coord.y-1));
         neighborCoord.add(new Coord(coord.x+1, coord.y-1));
         neighborCoord.add(new Coord(coord.x-1, coord.y  ));
         neighborCoord.add(new Coord(coord.x-1, coord.y+1));
         neighborCoord.add(new Coord(coord.x  , coord.y+1));
         neighborCoord.add(new Coord(coord.x+1, coord.y+1));
         break;
      case 11:
         neighborCoord.add(new Coord(coord.x  , coord.y-2));
         neighborCoord.add(new Coord(coord.x  , coord.y-1));
         neighborCoord.add(new Coord(coord.x+1, coord.y-1));
         neighborCoord.add(new Coord(coord.x+2, coord.y-1));
         neighborCoord.add(new Coord(coord.x+1, coord.y  ));
         neighborCoord.add(new Coord(coord.x  , coord.y+1));
         break;
      }

      return neighborCoord;
   }

   private PointDouble getOffset() {
      AttrPentagonT10 attr = getAttr();
      double a = attr.getA();

      PointDouble o = new PointDouble(0,0);
      switch (direction) {
      case 0: case 6: case  8: case 9: case 10:          o.x = a*2+a*6*((coord.x+0)/2); break;
      case 1: case 2: case  3: case 4: case 5: case 7:   o.x = a*5+a*6*((coord.x+0)/2); break;
      case 11:                                           o.x = a*2+a*6*((coord.x+1)/2); break;
      }
      switch (direction) {
      case 0:                                            o.y = a*5 +a*14*(coord.y/6);   break;
      case 1:                                            o.y =      a*14*(coord.y/6);   break;
      case 2: case 3: case  4: case 5:                   o.y = a*6 +a*14*(coord.y/6);   break;
      case 6:                                            o.y = a*7 +a*14*(coord.y/6);   break;
      case 7:                                            o.y = a*12+a*14*(coord.y/6);   break;
      case 8: case 9: case 10: case 11:                  o.y = a*13+a*14*(coord.y/6);   break;
      }
      return o;
   }

   @Override
   protected void calcRegion() {
      AttrPentagonT10 attr = getAttr();
      double a = attr.getA();

      PointDouble o = getOffset();

      switch (direction) {
      case 0: case 3: case 7: case 8:
         region.setPoint(0, o.x + a  , o.y - a*3);
         region.setPoint(1, o.x + a*2, o.y - a*2);
         region.setPoint(2, o.x      , o.y      );
         region.setPoint(3, o.x - a*2, o.y - a*2);
         region.setPoint(4, o.x - a  , o.y - a*3);
         break;
      case 1: case 4: case 6: case 10:
         region.setPoint(0, o.x      , o.y      );
         region.setPoint(1, o.x + a*2, o.y + a*2);
         region.setPoint(2, o.x + a  , o.y + a*3);
         region.setPoint(3, o.x - a  , o.y + a*3);
         region.setPoint(4, o.x - a*2, o.y + a*2);
         break;
      case 2: case 11:
         region.setPoint(0, o.x - a*2, o.y - a*2);
         region.setPoint(1, o.x      , o.y      );
         region.setPoint(2, o.x - a*2, o.y + a*2);
         region.setPoint(3, o.x - a*3, o.y + a  );
         region.setPoint(4, o.x - a*3, o.y - a  );
         break;
      case 5: case 9:
         region.setPoint(0, o.x + a*2, o.y - a*2);
         region.setPoint(1, o.x + a*3, o.y - a  );
         region.setPoint(2, o.x + a*3, o.y + a  );
         region.setPoint(3, o.x + a*2, o.y + a*2);
         region.setPoint(4, o.x      , o.y      );
         break;
      }
   }

   @Override
   public RectDouble getRcInner(int borderWidth) {
      AttrPentagonT10 attr = getAttr();
      double sq = attr.getSq(borderWidth);
      double sq2 = sq/2;

      PointDouble center = new PointDouble(); // координата центра квадрата
      switch (direction) {
      case 0: case  3: case 7: case  8: center.x = region.getPoint(2).x; center.y = region.getPoint(1).y; break;
      case 1: case  4: case 6: case 10:
      case 2: case 11:                  center.x = region.getPoint(0).x; center.y = region.getPoint(1).y; break;
      case 5: case  9:                  center.x = region.getPoint(0).x; center.y = region.getPoint(4).y; break;
      }

      return new RectDouble(
         center.x - sq2,
         center.y - sq2,
         sq, sq);
   }

   @Override
   public int getShiftPointBorderIndex() {
      switch (direction) {
      case 1: case 4: case 5: case 6: case 9: case 10:
         return 3;
      }
      return 2;
   }

   @Override
   public Color getBackgroundFillColor(int fillMode, Color defaultColor, Map<Integer, Color> repositoryColor) {
      if (fillMode == getAttr().getMaxBackgroundFillModeValue())
      {
         switch (getDirection()) {
         case  2: case  3: case  4: case  5: return repositoryColor.get(0);
         case  8: case  9: case 10: case 11: return repositoryColor.get(1);
         case  1: case  7: return repositoryColor.get(2);
         case  0: case  6: return repositoryColor.get(3);
//         default:
//            return repositoryColor.get(-1);
         }
      }
      return super.getBackgroundFillColor(fillMode, defaultColor, repositoryColor);
   }
}