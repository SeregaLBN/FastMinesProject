////////////////////////////////////////////////////////////////////////////////
//                               FastMines project
//                                      © Sergey Krivulya (KSerg, aka SeregaLBN)
// file name: "SqTrHex.java"
//
// Реализация класса SqTrHex - мозаика из 6Square 4Triangle 2Hexagon
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
 * Комбинация. мозаика из 6Square 4Triangle 2Hexagon
 * @see BaseCell
 **/
public class SqTrHex extends BaseCell {
   public static class AttrSqTrHex extends BaseAttribute {

      @Override
      public SizeDouble getOwnerSize(Matrisize sizeField) {
         double a = getA();
         double h = getH();
         SizeDouble result = new SizeDouble(
                a/2+h + a/2*((sizeField.m+2)/3) +
                       h * ((sizeField.m+1)/3) +
               (a/2+h)    * ((sizeField.m+0)/3),
                a/2   + h * ((sizeField.n+1)/2)+
                a*3/2*      ((sizeField.n+0)/2));

         if (sizeField.n < 4) {
            int x = sizeField.m % 3;
            switch (sizeField.n) {
            case 1:
               switch (x) { case 0: result.width -= h; case 1: case 2: result.width -= h; }
               break;
            case 2: case 3:
               switch (x) { case 0: case 1: result.width -= h; }
               break;
            }
         }
         if (sizeField.m < 3) {
            int y = sizeField.n % 4;
            switch (sizeField.m) {
            case 1:
               switch (y) { case 0: result.height -= a*1.5; break; case 2: case 3: result.height -= a/2; }
               break;
            case 2:
               if (y == 0) result.height -= a/2;
               break;
            }
         }

         return result;
      }

      @Override
      public int getNeighborNumber(int direction) {
         switch (direction) {
         case  0: case  2: case  6: case  7: return 6;
         case  1: case  3: case  5: case  8: case  9: case 10: return 8;
         case  4: case 11: return 12;
         default:
            throw new IllegalArgumentException("Invalid value direction="+direction);
          }
      }
      @Override
      public int getVertexNumber(int direction) {
         switch (direction) {
         case  0: case  2: case  6: case  7: return 3;
         case  1: case  3: case  5: case  8: case  9: case 10: return 4;
         case  4: case 11: return 6;
         default:
            throw new IllegalArgumentException("Invalid value direction="+direction);
          }
      }
      @Override
      public double getVertexIntersection() { return 4.; }
      @Override
      public Size getDirectionSizeField() { return new Size(3, 4); }
      @Override
      protected double getA() { return Math.sqrt(getArea()/(0.5+1/SQRT3)); }
      protected double getH() { return getA()*SQRT3/2; }
      @Override
      public double getSq(int borderWidth) {
         double w = borderWidth/2.;
         return (getA()*SQRT3 - w*6) / (2+SQRT3);
      }
   }

   public SqTrHex(AttrSqTrHex attr, Coord coord) {
      super(attr, coord,
            (coord.y&3)*3+(coord.x%3) // 0..11
         );
   }

   @Override
   public AttrSqTrHex getAttr() {
      return (AttrSqTrHex) super.getAttr();
   }

   @Override
   protected List<Coord> getCoordsNeighbor() {
      List<Coord> neighborCoord = new ArrayList<>(getAttr().getNeighborNumber(getDirection()));

      // определяю координаты соседей
      switch (direction) {
      case 0:
         neighborCoord.add(new Coord(coord.x-1, coord.y-1));
         neighborCoord.add(new Coord(coord.x+1, coord.y  ));
         neighborCoord.add(new Coord(coord.x-2, coord.y+1));
         neighborCoord.add(new Coord(coord.x-1, coord.y+1));
         neighborCoord.add(new Coord(coord.x  , coord.y+1));
         neighborCoord.add(new Coord(coord.x+1, coord.y+1));
         break;
      case 1:
         neighborCoord.add(new Coord(coord.x-2, coord.y-1));
         neighborCoord.add(new Coord(coord.x  , coord.y-1));
         neighborCoord.add(new Coord(coord.x-1, coord.y  ));
         neighborCoord.add(new Coord(coord.x+1, coord.y  ));
         neighborCoord.add(new Coord(coord.x-2, coord.y+1));
         neighborCoord.add(new Coord(coord.x-1, coord.y+1));
         neighborCoord.add(new Coord(coord.x  , coord.y+1));
         neighborCoord.add(new Coord(coord.x+1, coord.y+1));
         break;
      case 2:
         neighborCoord.add(new Coord(coord.x-3, coord.y-1));
         neighborCoord.add(new Coord(coord.x-1, coord.y-1));
         neighborCoord.add(new Coord(coord.x  , coord.y-1));
         neighborCoord.add(new Coord(coord.x-1, coord.y  ));
         neighborCoord.add(new Coord(coord.x-1, coord.y+1));
         neighborCoord.add(new Coord(coord.x  , coord.y+1));
         break;
      case 3:
         neighborCoord.add(new Coord(coord.x  , coord.y-1));
         neighborCoord.add(new Coord(coord.x+1, coord.y-1));
         neighborCoord.add(new Coord(coord.x-2, coord.y  ));
         neighborCoord.add(new Coord(coord.x-1, coord.y  ));
         neighborCoord.add(new Coord(coord.x+1, coord.y  ));
         neighborCoord.add(new Coord(coord.x-1, coord.y+1));
         neighborCoord.add(new Coord(coord.x  , coord.y+1));
         neighborCoord.add(new Coord(coord.x  , coord.y+2));
         break;
      case 4:
         neighborCoord.add(new Coord(coord.x-1, coord.y-1));
         neighborCoord.add(new Coord(coord.x  , coord.y-1));
         neighborCoord.add(new Coord(coord.x+1, coord.y-1));
         neighborCoord.add(new Coord(coord.x+2, coord.y-1));
         neighborCoord.add(new Coord(coord.x-1, coord.y  ));
         neighborCoord.add(new Coord(coord.x+1, coord.y  ));
         neighborCoord.add(new Coord(coord.x+2, coord.y  ));
         neighborCoord.add(new Coord(coord.x-1, coord.y+1));
         neighborCoord.add(new Coord(coord.x  , coord.y+1));
         neighborCoord.add(new Coord(coord.x+1, coord.y+1));
         neighborCoord.add(new Coord(coord.x+2, coord.y+1));
         neighborCoord.add(new Coord(coord.x-1, coord.y+2));
         break;
      case 5:
         neighborCoord.add(new Coord(coord.x-1, coord.y-2));
         neighborCoord.add(new Coord(coord.x  , coord.y-2));
         neighborCoord.add(new Coord(coord.x-1, coord.y-1));
         neighborCoord.add(new Coord(coord.x  , coord.y-1));
         neighborCoord.add(new Coord(coord.x+1, coord.y-1));
         neighborCoord.add(new Coord(coord.x+2, coord.y-1));
         neighborCoord.add(new Coord(coord.x-1, coord.y  ));
         neighborCoord.add(new Coord(coord.x+1, coord.y  ));
         break;
      case 6:
         neighborCoord.add(new Coord(coord.x-2, coord.y-1));
         neighborCoord.add(new Coord(coord.x  , coord.y-1));
         neighborCoord.add(new Coord(coord.x+1, coord.y-1));
         neighborCoord.add(new Coord(coord.x-1, coord.y  ));
         neighborCoord.add(new Coord(coord.x-1, coord.y+1));
         neighborCoord.add(new Coord(coord.x  , coord.y+1));
         break;
      case 7:
         neighborCoord.add(new Coord(coord.x  , coord.y-1));
         neighborCoord.add(new Coord(coord.x+1, coord.y  ));
         neighborCoord.add(new Coord(coord.x-2, coord.y+1));
         neighborCoord.add(new Coord(coord.x-1, coord.y+1));
         neighborCoord.add(new Coord(coord.x  , coord.y+1));
         neighborCoord.add(new Coord(coord.x+1, coord.y+1));
         break;
      case 8:
         neighborCoord.add(new Coord(coord.x-1, coord.y-1));
         neighborCoord.add(new Coord(coord.x+1, coord.y-1));
         neighborCoord.add(new Coord(coord.x-1, coord.y  ));
         neighborCoord.add(new Coord(coord.x+1, coord.y  ));
         neighborCoord.add(new Coord(coord.x-2, coord.y+1));
         neighborCoord.add(new Coord(coord.x-1, coord.y+1));
         neighborCoord.add(new Coord(coord.x  , coord.y+1));
         neighborCoord.add(new Coord(coord.x+1, coord.y+1));
         break;
      case 9:
         neighborCoord.add(new Coord(coord.x  , coord.y-2));
         neighborCoord.add(new Coord(coord.x+1, coord.y-2));
         neighborCoord.add(new Coord(coord.x-1, coord.y-1));
         neighborCoord.add(new Coord(coord.x  , coord.y-1));
         neighborCoord.add(new Coord(coord.x+1, coord.y-1));
         neighborCoord.add(new Coord(coord.x+2, coord.y-1));
         neighborCoord.add(new Coord(coord.x-1, coord.y  ));
         neighborCoord.add(new Coord(coord.x+1, coord.y  ));
         break;
      case 10:
         neighborCoord.add(new Coord(coord.x  , coord.y-1));
         neighborCoord.add(new Coord(coord.x+1, coord.y-1));
         neighborCoord.add(new Coord(coord.x-2, coord.y  ));
         neighborCoord.add(new Coord(coord.x-1, coord.y  ));
         neighborCoord.add(new Coord(coord.x+1, coord.y  ));
         neighborCoord.add(new Coord(coord.x  , coord.y+1));
         neighborCoord.add(new Coord(coord.x+1, coord.y+1));
         neighborCoord.add(new Coord(coord.x+1, coord.y+2));
         break;
      case 11:
         neighborCoord.add(new Coord(coord.x-1, coord.y-1));
         neighborCoord.add(new Coord(coord.x  , coord.y-1));
         neighborCoord.add(new Coord(coord.x+1, coord.y-1));
         neighborCoord.add(new Coord(coord.x+2, coord.y-1));
         neighborCoord.add(new Coord(coord.x-1, coord.y  ));
         neighborCoord.add(new Coord(coord.x+1, coord.y  ));
         neighborCoord.add(new Coord(coord.x+2, coord.y  ));
         neighborCoord.add(new Coord(coord.x  , coord.y+1));
         neighborCoord.add(new Coord(coord.x+1, coord.y+1));
         neighborCoord.add(new Coord(coord.x+2, coord.y+1));
         neighborCoord.add(new Coord(coord.x+3, coord.y+1));
         neighborCoord.add(new Coord(coord.x  , coord.y+2));
         break;
       }

      return neighborCoord;
   }

   private PointDouble getOffset() {
      AttrSqTrHex attr = getAttr();
      double a = attr.getA();
      double h = attr.getH();

      return new PointDouble(
            (h*2+a  )*(coord.x/3) + a+h,
            (h*2+a*3)*(coord.y/4) + a*2+h);
   }

   @Override
   protected void calcRegion() {
      AttrSqTrHex attr = getAttr();
      double a = attr.getA();
      double b = a/2;
      double h = attr.getH();

      PointDouble o = getOffset();
      switch (direction) {
      case 0:
         region.setPoint(0, o.x - b-h  , o.y - a-b-h);
         region.setPoint(1, o.x - h    , o.y - a-b  );
         region.setPoint(2, o.x - h-a  , o.y - a-b  );
         break;
      case 1:
         region.setPoint(0, o.x - b    , o.y - a-a-h);
         region.setPoint(1, o.x        , o.y - a-a  );
         region.setPoint(2, o.x - h    , o.y - a-b  );
         region.setPoint(3, o.x - b-h  , o.y - a-b-h);
         break;
      case 2:
         region.setPoint(0, o.x + b    , o.y - a-a-h);
         region.setPoint(1, o.x        , o.y - a-a  );
         region.setPoint(2, o.x - b    , o.y - a-a-h);
         break;
      case 3:
         region.setPoint(0, o.x - h    , o.y - a-b  );
         region.setPoint(1, o.x - h    , o.y - b    );
         region.setPoint(2, o.x - a-h  , o.y - b    );
         region.setPoint(3, o.x - a-h  , o.y - a-b  );
         break;
      case 4:
         region.setPoint(0, o.x        , o.y - a-a  );
         region.setPoint(1, o.x + h    , o.y - a-b  );
         region.setPoint(2, o.x + h    , o.y - b    );
         region.setPoint(3, o.x        , o.y        );
         region.setPoint(4, o.x - h    , o.y - b    );
         region.setPoint(5, o.x - h    , o.y - a-b  );
         break;
      case 5:
         region.setPoint(0, o.x + b    , o.y - a-a-h);
         region.setPoint(1, o.x + b+h  , o.y - a-b-h);
         region.setPoint(2, o.x + h    , o.y - a-b  );
         region.setPoint(3, o.x        , o.y - a-a  );
         break;
      case 6:
         region.setPoint(0, o.x - h    , o.y - b    );
         region.setPoint(1, o.x - b-h  , o.y - b+h  );
         region.setPoint(2, o.x - a-h  , o.y - b    );
         break;
      case 7:
         region.setPoint(0, o.x        , o.y        );
         region.setPoint(1, o.x + b    , o.y + h    );
         region.setPoint(2, o.x - b    , o.y + h    );
         break;
      case 8:
         region.setPoint(0, o.x + h    , o.y - b    );
         region.setPoint(1, o.x + h+b  , o.y - b+h  );
         region.setPoint(2, o.x + b    , o.y + h    );
         region.setPoint(3, o.x        , o.y        );
         break;
      case 9:
         region.setPoint(0, o.x - h    , o.y - b    );
         region.setPoint(1, o.x        , o.y        );
         region.setPoint(2, o.x - b    , o.y + h    );
         region.setPoint(3, o.x - b-h  , o.y - b+h  );
         break;
      case 10:
         region.setPoint(0, o.x + b    , o.y + h    );
         region.setPoint(1, o.x + b    , o.y + a+h  );
         region.setPoint(2, o.x - b    , o.y + a+h  );
         region.setPoint(3, o.x - b    , o.y + h    );
         break;
      case 11:
         region.setPoint(0, o.x + b+h  , o.y + h-b  );
         region.setPoint(1, o.x + b+h+h, o.y + h    );
         region.setPoint(2, o.x + b+h+h, o.y + a+h  );
         region.setPoint(3, o.x + b+h  , o.y + a+b+h);
         region.setPoint(4, o.x + b    , o.y + a+h  );
         region.setPoint(5, o.x + b    , o.y + h    );
         break;
      }
   }

   @Override
   public RectDouble getRcInner(int borderWidth) {
      AttrSqTrHex attr = getAttr();
      double a = attr.getA();
      double b = a/2;
      double h = attr.getH();
      double w = borderWidth/2.;
      double sq = getAttr().getSq(borderWidth);
      double sq2 = sq/2;

      PointDouble o = getOffset();

      PointDouble center = new PointDouble(); // координата центра вписанного в фигуру квадрата
      switch (direction) {
      case  0: center.x = o.x -  b-h;    center.y = o.y - a-b-w-sq2;   break;
      case  1: center.x = o.x - (b+h)/2; center.y = o.y - a-b-(b+h)/2; break;
      case  2: center.x = o.x;           center.y = o.y - a-a-h+w+sq2; break;
      case  3: center.x = o.x -  b-h;    center.y = o.y - a;           break;
      case  4: center.x = o.x;           center.y = o.y - a;           break;
      case  5: center.x = o.x + (b+h)/2; center.y = o.y - a-b-(b+h)/2; break;
      case  6: center.x = o.x -  b-h;    center.y = o.y - b+w+sq2;     break;
      case  7: center.x = o.x;           center.y = o.y + h-w-sq2;     break;
      case  8: center.x = o.x + (b+h)/2; center.y = o.y - b+(b+h)/2;   break;
      case  9: center.x = o.x - (b+h)/2; center.y = o.y - b+(b+h)/2;   break;
      case 10: center.x = o.x;           center.y = o.y + b+h;         break;
      case 11: center.x = o.x +  b+h;    center.y = o.y + b+h;         break;
      }

      return new RectDouble(
         center.x - sq2,
         center.y - sq2,
         sq, sq);
   }

   @Override
   public int getShiftPointBorderIndex() {
      switch (direction) {
      case 2: case  6: return 1;
      case 4: case 11: return 3;
      }
      return 2;
   }
}
