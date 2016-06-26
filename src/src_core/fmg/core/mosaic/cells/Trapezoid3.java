////////////////////////////////////////////////////////////////////////////////
//                               FastMines project
//                                      © Sergey Krivulya (KSerg, aka SeregaLBN)
// file name: "Trapezoid3.java"
//
// Реализация класса Trapezoid3 - 8 трапеций, складывающихся в шестигранник
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

import fmg.common.geom.Coord;
import fmg.common.geom.Matrisize;
import fmg.common.geom.PointDouble;
import fmg.common.geom.RectDouble;
import fmg.common.geom.Size;
import fmg.common.geom.SizeDouble;

/**
 * Trapezoid3 - 8 трапеций, складывающихся в шестигранник
 * @see BaseCell
 **/
public class Trapezoid3 extends BaseCell {
   public static class AttrTrapezoid3 extends BaseAttribute {
      public AttrTrapezoid3(double area) {
         super(area);
      }

      @Override
      public SizeDouble getOwnerSize(Matrisize sizeField) {
         double a = getA();
         double b = getB();
         double R = getROut();
         SizeDouble result = new SizeDouble(
                 R *((sizeField.m+1)/2),
               a+b *((sizeField.n+1)/2)+
                 a *((sizeField.n+0)/2));

         if (sizeField.m == 1)
            switch (sizeField.n % 4) {
            case 0: result.height -= a; break;
            case 3: result.height -= a*1.5; break;
            }
         if (sizeField.n == 1)
            if ((sizeField.m & 1) == 1)
               result.width -= getRIn();

         return result;
      }

      @Override
      public int getNeighborNumber(int direction) {
          switch (direction) {
          case  2: case  5: case 11: case 12: return 6;
          case  0: case  7: case  9: case 14: return 10;
          case  1: case  3: case  4: case  6:
          case  8: case 10: case 13: case 15: return 11;
          default:
             throw new IllegalArgumentException("Invalid value direction=" + direction);
          }
      }
      @Override
      public int getVertexNumber(int direction) { return 4; }

      static double vertexIntersection = 0.;
      @Override
      public double getVertexIntersection() {
         if (vertexIntersection < 1) {
            final int cntDirection = GetDirectionCount(); // 0..11
            double sum = 0;
            for (int dir=0; dir<cntDirection; dir++)
               switch (dir) {
                case  2: case  5: case 11: case 12:
                  sum += (4+4+3+3)/4.;
                  break;
                case  0: case  7: case  9: case 14:
                  sum += (6+6+3+3)/4.;
                  break;
                case  1: case  3: case  4: case  6:
                case  8: case 10: case 13: case 15:
                   sum += (6+6+4+3)/4.;
                  break;
               default:
                  throw new RuntimeException("Забыл case #" + dir);
               }
            vertexIntersection = sum / cntDirection;
//            System.out.println("Trapezoid3::getVertexNeighbor == " + vertexIntersection);
         }
         return vertexIntersection;
      }

      @Override
      public Size GetDirectionSizeField() { return new Size(4, 4); }
      @Override
      protected double getA   () { return Math.sqrt(getArea()/SQRT27)*2; }
      protected double getB   () { return getA()*2; }
      protected double getC   () { return getA()/2; }
      protected double getROut() { return getA()*SQRT3; }
      protected double getRIn () { return getROut()/2; }
      @Override
      public double getSq(int borderWidth) {
         double w = borderWidth/2.;
         return (getA()*SQRT3 - w*4)/(SQRT3+1);
      }
   }

   public Trapezoid3(AttrTrapezoid3 attr, Coord coord) {
      super(attr, coord,
            ((coord.y&3)<<2)+(coord.x&3) // 0..15
         );
   }

   @Override
   public AttrTrapezoid3 getAttr() {
      return (AttrTrapezoid3) super.getAttr();
   }

   @Override
   protected List<Coord> getCoordsNeighbor() {
      List<Coord> neighborCoord = new ArrayList<>(getAttr().getNeighborNumber(getDirection()));

      // определяю координаты соседей
       switch (direction) {
       case 0:
          neighborCoord.add(new Coord(coord.x-1, coord.y-1));
          neighborCoord.add(new Coord(coord.x+1, coord.y-1));
          neighborCoord.add(new Coord(coord.x-1, coord.y  ));
          neighborCoord.add(new Coord(coord.x+1, coord.y  ));
          neighborCoord.add(new Coord(coord.x-2, coord.y+1));
          neighborCoord.add(new Coord(coord.x-1, coord.y+1));
          neighborCoord.add(new Coord(coord.x  , coord.y+1));
          neighborCoord.add(new Coord(coord.x+1, coord.y+1));
          neighborCoord.add(new Coord(coord.x-2, coord.y+2));
          neighborCoord.add(new Coord(coord.x  , coord.y+2));
          break;
       case 1:
          neighborCoord.add(new Coord(coord.x  , coord.y-2));
          neighborCoord.add(new Coord(coord.x-2, coord.y-1));
          neighborCoord.add(new Coord(coord.x  , coord.y-1));
          neighborCoord.add(new Coord(coord.x+1, coord.y-1));
          neighborCoord.add(new Coord(coord.x+2, coord.y-1));
          neighborCoord.add(new Coord(coord.x-2, coord.y  ));
          neighborCoord.add(new Coord(coord.x-1, coord.y  ));
          neighborCoord.add(new Coord(coord.x+1, coord.y  ));
          neighborCoord.add(new Coord(coord.x+2, coord.y  ));
          neighborCoord.add(new Coord(coord.x-2, coord.y+1));
          neighborCoord.add(new Coord(coord.x  , coord.y+1));
          break;
       case 2:
          neighborCoord.add(new Coord(coord.x-1, coord.y  ));
          neighborCoord.add(new Coord(coord.x+1, coord.y  ));
          neighborCoord.add(new Coord(coord.x-2, coord.y+1));
          neighborCoord.add(new Coord(coord.x-1, coord.y+1));
          neighborCoord.add(new Coord(coord.x  , coord.y+1));
          neighborCoord.add(new Coord(coord.x+1, coord.y+1));
          break;
       case 3:
          neighborCoord.add(new Coord(coord.x-2, coord.y-2));
          neighborCoord.add(new Coord(coord.x-2, coord.y-1));
          neighborCoord.add(new Coord(coord.x-1, coord.y-1));
          neighborCoord.add(new Coord(coord.x  , coord.y-1));
          neighborCoord.add(new Coord(coord.x+2, coord.y-1));
          neighborCoord.add(new Coord(coord.x-2, coord.y  ));
          neighborCoord.add(new Coord(coord.x-1, coord.y  ));
          neighborCoord.add(new Coord(coord.x+1, coord.y  ));
          neighborCoord.add(new Coord(coord.x+2, coord.y  ));
          neighborCoord.add(new Coord(coord.x-2, coord.y+1));
          neighborCoord.add(new Coord(coord.x  , coord.y+1));
          break;
       case 4:
          neighborCoord.add(new Coord(coord.x  , coord.y-1));
          neighborCoord.add(new Coord(coord.x+2, coord.y-1));
          neighborCoord.add(new Coord(coord.x-2, coord.y  ));
          neighborCoord.add(new Coord(coord.x-1, coord.y  ));
          neighborCoord.add(new Coord(coord.x+1, coord.y  ));
          neighborCoord.add(new Coord(coord.x+2, coord.y  ));
          neighborCoord.add(new Coord(coord.x-2, coord.y+1));
          neighborCoord.add(new Coord(coord.x  , coord.y+1));
          neighborCoord.add(new Coord(coord.x+1, coord.y+1));
          neighborCoord.add(new Coord(coord.x+2, coord.y+1));
          neighborCoord.add(new Coord(coord.x+2, coord.y+2));
          break;
       case 5:
          neighborCoord.add(new Coord(coord.x-1, coord.y-1));
          neighborCoord.add(new Coord(coord.x  , coord.y-1));
          neighborCoord.add(new Coord(coord.x+1, coord.y-1));
          neighborCoord.add(new Coord(coord.x+2, coord.y-1));
          neighborCoord.add(new Coord(coord.x-1, coord.y  ));
          neighborCoord.add(new Coord(coord.x+1, coord.y  ));
          break;
       case 6:
          neighborCoord.add(new Coord(coord.x  , coord.y-1));
          neighborCoord.add(new Coord(coord.x+2, coord.y-1));
          neighborCoord.add(new Coord(coord.x-2, coord.y  ));
          neighborCoord.add(new Coord(coord.x-1, coord.y  ));
          neighborCoord.add(new Coord(coord.x+1, coord.y  ));
          neighborCoord.add(new Coord(coord.x+2, coord.y  ));
          neighborCoord.add(new Coord(coord.x-2, coord.y+1));
          neighborCoord.add(new Coord(coord.x-1, coord.y+1));
          neighborCoord.add(new Coord(coord.x  , coord.y+1));
          neighborCoord.add(new Coord(coord.x+2, coord.y+1));
          neighborCoord.add(new Coord(coord.x  , coord.y+2));
          break;
       case 7:
          neighborCoord.add(new Coord(coord.x  , coord.y-2));
          neighborCoord.add(new Coord(coord.x+2, coord.y-2));
          neighborCoord.add(new Coord(coord.x-1, coord.y-1));
          neighborCoord.add(new Coord(coord.x  , coord.y-1));
          neighborCoord.add(new Coord(coord.x+1, coord.y-1));
          neighborCoord.add(new Coord(coord.x+2, coord.y-1));
          neighborCoord.add(new Coord(coord.x-1, coord.y  ));
          neighborCoord.add(new Coord(coord.x+1, coord.y  ));
          neighborCoord.add(new Coord(coord.x-1, coord.y+1));
          neighborCoord.add(new Coord(coord.x+1, coord.y+1));
          break;
       case 8:
          neighborCoord.add(new Coord(coord.x  , coord.y-2));
          neighborCoord.add(new Coord(coord.x-2, coord.y-1));
          neighborCoord.add(new Coord(coord.x-1, coord.y-1));
          neighborCoord.add(new Coord(coord.x  , coord.y-1));
          neighborCoord.add(new Coord(coord.x+2, coord.y-1));
          neighborCoord.add(new Coord(coord.x-2, coord.y  ));
          neighborCoord.add(new Coord(coord.x-1, coord.y  ));
          neighborCoord.add(new Coord(coord.x+1, coord.y  ));
          neighborCoord.add(new Coord(coord.x+2, coord.y  ));
          neighborCoord.add(new Coord(coord.x  , coord.y+1));
          neighborCoord.add(new Coord(coord.x+2, coord.y+1));
          break;
       case 9:
          neighborCoord.add(new Coord(coord.x-1, coord.y-1));
          neighborCoord.add(new Coord(coord.x+1, coord.y-1));
          neighborCoord.add(new Coord(coord.x-1, coord.y  ));
          neighborCoord.add(new Coord(coord.x+1, coord.y  ));
          neighborCoord.add(new Coord(coord.x-1, coord.y+1));
          neighborCoord.add(new Coord(coord.x  , coord.y+1));
          neighborCoord.add(new Coord(coord.x+1, coord.y+1));
          neighborCoord.add(new Coord(coord.x+2, coord.y+1));
          neighborCoord.add(new Coord(coord.x  , coord.y+2));
          neighborCoord.add(new Coord(coord.x+2, coord.y+2));
          break;
       case 10:
          neighborCoord.add(new Coord(coord.x+2, coord.y-2));
          neighborCoord.add(new Coord(coord.x-2, coord.y-1));
          neighborCoord.add(new Coord(coord.x  , coord.y-1));
          neighborCoord.add(new Coord(coord.x+1, coord.y-1));
          neighborCoord.add(new Coord(coord.x+2, coord.y-1));
          neighborCoord.add(new Coord(coord.x-2, coord.y  ));
          neighborCoord.add(new Coord(coord.x-1, coord.y  ));
          neighborCoord.add(new Coord(coord.x+1, coord.y  ));
          neighborCoord.add(new Coord(coord.x+2, coord.y  ));
          neighborCoord.add(new Coord(coord.x  , coord.y+1));
          neighborCoord.add(new Coord(coord.x+2, coord.y+1));
          break;
       case 11:
          neighborCoord.add(new Coord(coord.x-1, coord.y  ));
          neighborCoord.add(new Coord(coord.x+1, coord.y  ));
          neighborCoord.add(new Coord(coord.x-1, coord.y+1));
          neighborCoord.add(new Coord(coord.x  , coord.y+1));
          neighborCoord.add(new Coord(coord.x+1, coord.y+1));
          neighborCoord.add(new Coord(coord.x+2, coord.y+1));
          break;
       case 12:
          neighborCoord.add(new Coord(coord.x-2, coord.y-1));
          neighborCoord.add(new Coord(coord.x-1, coord.y-1));
          neighborCoord.add(new Coord(coord.x  , coord.y-1));
          neighborCoord.add(new Coord(coord.x+1, coord.y-1));
          neighborCoord.add(new Coord(coord.x-1, coord.y  ));
          neighborCoord.add(new Coord(coord.x+1, coord.y  ));
          break;
       case 13:
          neighborCoord.add(new Coord(coord.x-2, coord.y-1));
          neighborCoord.add(new Coord(coord.x  , coord.y-1));
          neighborCoord.add(new Coord(coord.x-2, coord.y  ));
          neighborCoord.add(new Coord(coord.x-1, coord.y  ));
          neighborCoord.add(new Coord(coord.x+1, coord.y  ));
          neighborCoord.add(new Coord(coord.x+2, coord.y  ));
          neighborCoord.add(new Coord(coord.x-2, coord.y+1));
          neighborCoord.add(new Coord(coord.x-1, coord.y+1));
          neighborCoord.add(new Coord(coord.x  , coord.y+1));
          neighborCoord.add(new Coord(coord.x+2, coord.y+1));
          neighborCoord.add(new Coord(coord.x-2, coord.y+2));
          break;
       case 14:
          neighborCoord.add(new Coord(coord.x-2, coord.y-2));
          neighborCoord.add(new Coord(coord.x  , coord.y-2));
          neighborCoord.add(new Coord(coord.x-2, coord.y-1));
          neighborCoord.add(new Coord(coord.x-1, coord.y-1));
          neighborCoord.add(new Coord(coord.x  , coord.y-1));
          neighborCoord.add(new Coord(coord.x+1, coord.y-1));
          neighborCoord.add(new Coord(coord.x-1, coord.y  ));
          neighborCoord.add(new Coord(coord.x+1, coord.y  ));
          neighborCoord.add(new Coord(coord.x-1, coord.y+1));
          neighborCoord.add(new Coord(coord.x+1, coord.y+1));
          break;
       case 15:
          neighborCoord.add(new Coord(coord.x-2, coord.y-1));
          neighborCoord.add(new Coord(coord.x  , coord.y-1));
          neighborCoord.add(new Coord(coord.x-2, coord.y  ));
          neighborCoord.add(new Coord(coord.x-1, coord.y  ));
          neighborCoord.add(new Coord(coord.x+1, coord.y  ));
          neighborCoord.add(new Coord(coord.x+2, coord.y  ));
          neighborCoord.add(new Coord(coord.x-2, coord.y+1));
          neighborCoord.add(new Coord(coord.x  , coord.y+1));
          neighborCoord.add(new Coord(coord.x+1, coord.y+1));
          neighborCoord.add(new Coord(coord.x+2, coord.y+1));
          neighborCoord.add(new Coord(coord.x  , coord.y+2));
          break;
       }

      return neighborCoord;
   }

   @Override
   protected void CalcRegion() {
      AttrTrapezoid3 attr = getAttr();
      double a = attr.getA();
      double b = attr.getB();
      double c = attr.getC();
      double R = attr.getROut();
      double r = attr.getRIn();

      // определение координат точек фигуры
      double oX = (R*2)*(coord.x/4) + R; // offset X
      double oY = (a*6)*(coord.y/4) + a + b; // offset Y

      switch (direction) {
      case 0:
         region.setPoint(0, oX - r, oY - a-c);
         region.setPoint(1, oX - r, oY - c  );
         region.setPoint(2, oX - R, oY      );
         region.setPoint(3, oX - R, oY - b  );
         break;
      case 1:
         region.setPoint(0, oX    , oY - a-b);
         region.setPoint(1, oX    , oY - b  );
         region.setPoint(2, oX - r, oY - a-c);
         region.setPoint(3, oX - R, oY - b  );
         break;
      case 2:
         region.setPoint(0, oX + r, oY - a-c);
         region.setPoint(1, oX + r, oY - c  );
         region.setPoint(2, oX    , oY      );
         region.setPoint(3, oX    , oY - b  );
         break;
      case 3:
         region.setPoint(0, oX + R, oY - b  );
         region.setPoint(1, oX + r, oY - a-c);
         region.setPoint(2, oX    , oY - b  );
         region.setPoint(3, oX    , oY - a-b);
         break;
      case 4:
         region.setPoint(0, oX    , oY      );
         region.setPoint(1, oX    , oY + a  );
         region.setPoint(2, oX - R, oY      );
         region.setPoint(3, oX - r, oY - c  );
         break;
      case 5:
         region.setPoint(0, oX    , oY - b  );
         region.setPoint(1, oX    , oY      );
         region.setPoint(2, oX - r, oY - c  );
         region.setPoint(3, oX - r, oY - a-c);
         break;
      case 6:
         region.setPoint(0, oX + R, oY      );
         region.setPoint(1, oX    , oY + a  );
         region.setPoint(2, oX    , oY      );
         region.setPoint(3, oX + r, oY - c  );
         break;
      case 7:
         region.setPoint(0, oX + R, oY - b  );
         region.setPoint(1, oX + R, oY      );
         region.setPoint(2, oX + r, oY - c  );
         region.setPoint(3, oX + r, oY - a-c);
         break;
      case 8:
         region.setPoint(0, oX    , oY + a  );
         region.setPoint(1, oX - r, oY + a+c);
         region.setPoint(2, oX - R, oY + a  );
         region.setPoint(3, oX - R, oY      );
         break;
      case 9:
         region.setPoint(0, oX    , oY + a  );
         region.setPoint(1, oX    , oY + a+b);
         region.setPoint(2, oX - r, oY + b+c);
         region.setPoint(3, oX - r, oY + a+c);
         break;
      case 10:
         region.setPoint(0, oX + R, oY      );
         region.setPoint(1, oX + R, oY + a  );
         region.setPoint(2, oX + r, oY + a+c);
         region.setPoint(3, oX    , oY + a  );
         break;
      case 11:
         region.setPoint(0, oX + R, oY + a  );
         region.setPoint(1, oX + R, oY + a+b);
         region.setPoint(2, oX + r, oY + b+c);
         region.setPoint(3, oX + r, oY + a+c);
         break;
      case 12:
         region.setPoint(0, oX - r, oY + a+c);
         region.setPoint(1, oX - r, oY + b+c);
         region.setPoint(2, oX - R, oY + a+b);
         region.setPoint(3, oX - R, oY + a  );
         break;
      case 13:
         region.setPoint(0, oX    , oY + a+b);
         region.setPoint(1, oX - R, oY + b*2);
         region.setPoint(2, oX - R, oY + a+b);
         region.setPoint(3, oX - r, oY + b+c);
         break;
      case 14:
         region.setPoint(0, oX + r, oY + a+c);
         region.setPoint(1, oX + r, oY + b+c);
         region.setPoint(2, oX    , oY + a+b);
         region.setPoint(3, oX    , oY + a  );
         break;
      case 15:
         region.setPoint(0, oX + R, oY + a+b);
         region.setPoint(1, oX + R, oY + b*2);
         region.setPoint(2, oX    , oY + a+b);
         region.setPoint(3, oX + r, oY + b+c);
         break;
      }
   }

   @Override
   public RectDouble getRcInner(int borderWidth) {
      AttrTrapezoid3 attr = getAttr();
      double a = attr.getA();
      double b = attr.getB();
      double c = attr.getC();
      double R = attr.getROut();
      double r = attr.getRIn();
//      double w = borderWidth/2.;
      double sq  = attr.getSq(borderWidth);
      double sq2 = sq/2;

      double oX = (R*2)*(coord.x/4) + R; // offset X
      double oY = (a*6)*(coord.y/4) + a + b; // offset Y

      PointDouble center = new PointDouble(); // координата центра квадрата
      switch (direction) {
      case 0:  center.x = oX - r*1.50; center.y = oY - a;      break;
      case 1:  center.x = oX - r*0.75; center.y = oY - c*4.25; break;
      case 2:  center.x = oX + r*0.50; center.y = oY - a;      break;
      case 3:  center.x = oX + r*0.75; center.y = oY - c*4.25; break;
      case 4:  center.x = oX - r*0.75; center.y = oY + c*0.25; break;
      case 5:  center.x = oX - r*0.50; center.y = oY - a;      break;
      case 6:  center.x = oX + r*0.75; center.y = oY + c*0.25; break;
      case 7:  center.x = oX + r*1.50; center.y = oY - a;      break;
      case 8:  center.x = oX - r*1.25; center.y = oY + c*1.75; break;
      case 9:  center.x = oX - r*0.50; center.y = oY + b;      break;
      case 10: center.x = oX + r*1.25; center.y = oY + c*1.75; break;
      case 11: center.x = oX + r*1.50; center.y = oY + b;      break;
      case 12: center.x = oX - r*1.50; center.y = oY + b;      break;
      case 13: center.x = oX - r*1.25; center.y = oY + c*6.25; break;
      case 14: center.x = oX + r*0.50; center.y = oY + b;      break;
      case 15: center.x = oX + r*1.25; center.y = oY + c*6.25; break;
      }

      return new RectDouble(
         center.x - sq2,
         center.y - sq2,
         sq, sq);
   }

   @Override
   public int getShiftPointBorderIndex() {
      switch (direction) {
      case 1: case 10: return 3;
      case 6: case 13: return 1;
      }
      return 2;
   }
}