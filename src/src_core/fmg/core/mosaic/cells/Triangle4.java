////////////////////////////////////////////////////////////////////////////////
//                               FastMines project
//                                      © Sergey Krivulya (KSerg, aka SeregaLBN)
// file name: "Triangle4.java"
//
// Реализация класса Triangle4 - треугольник 30°-30°-120°
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
 * Треугольник. Вариант 4 - треугольник 30°-30°-120°
 * @see BaseCell
 **/
public class Triangle4 extends BaseCell {
   private static enum ComplexityMode {
      /** original */
      eUnrealMode,
      eMeanMode,
      eOptimalMode,
      eSimpeMode
   }
   public static class AttrTriangle4 extends BaseAttribute {

      private final static ComplexityMode Mode = ComplexityMode.eOptimalMode; // TODO: check others to view...

      @Override
      public SizeDouble getSize(Matrisize sizeField) {
         double b = getB();
         double r = getRIn();
         double R = getROut();
         SizeDouble result = new SizeDouble(
                b+b *((sizeField.m+2)/3) +
                  b *((sizeField.m+0)/3),
               (R+r)*((sizeField.n+1)/2));

         switch (Mode) {
         case eUnrealMode:
         case eOptimalMode:
            // none  ...
            break;
         case eMeanMode:
         case eSimpeMode:
            {
               double u = getSnip()/2; // Snip * cos60
               double c = u*SQRT3; // Snip * cos30
               switch (sizeField.m%3) {
               case 0: result.width -= u+u; break;
               case 1: result.width -= u+c; break;
               case 2: result.width -= u; break;
               }
               if (Mode == ComplexityMode.eMeanMode) {
                  if ((sizeField.n % 4) == 3)
                     result.height -= u;
               } else {
                  if ((sizeField.n & 1) == 1)
                     result.height -= u;
               }
            }
            break;
         }

         if (sizeField.m == 1)
            if ((sizeField.n % 4) == 3)
               result.height -= R;
         if (sizeField.n == 1)
            if ((sizeField.m % 3) == 1)
               result.width -= b;

         return result;
      }

      @Override
      public int getNeighborNumber(int direction) {
         switch(Mode) {
         case eUnrealMode : return 21;
         case eMeanMode:
            switch(direction) {
            case 2: case 11:                                 return 7;
            case 1: case 5: case 8: case 10:                 return 5;
            case 0: case 3: case 4: case 6 : case 7: case 9: return 3;
            default: throw new RuntimeException("Unknown direction==" + direction);
            }
         case eOptimalMode:
            switch(direction) {
            case 4: case 5: case 9: case 10:  return 6;
            case 0: case 1: case 2: case 3:
            case 6: case 7: case 8: case 11:  return 7;
            default: throw new RuntimeException("Unknown direction==" + direction);
            }
         case eSimpeMode  : return 3;
         default: throw new RuntimeException("Unknown Mode==" + Mode);
         }
      }
      @Override
      public int getVertexNumber(int direction) {
         switch(Mode) {
         case eUnrealMode: return 3;
         case eMeanMode:
            switch(direction) {
            case 0: case 3: case 4: case 6: case 7: case 9: return 5;
            case 1: case 5: case 8: case 10:                return 4;
            case 2: case 11:                                return 3;
            default: throw new RuntimeException("Unknown direction==" + direction);
            }
         case eOptimalMode: return 4;
         case eSimpeMode  : return 5;
         default: throw new RuntimeException("Unknown Mode==" + Mode);
         }
      }
      @Override
      public double getVertexIntersection() {
         switch(Mode) {
         case eUnrealMode : return 9.;   // (12+12+3)/3.
         case eSimpeMode  : return 2.2;  // (2+2+2+2+3)/5.
         case eOptimalMode: return 3.25; // (6+3+2+2)/4.
         case eMeanMode   : return 2.62777777778;
            // ( (2+2+2+2+3)/ getVertexNumber(0 or 3 or 4 or 6 or 7 or 9) * 6шт  +
             //   ( 2+2+3+4 )/ getVertexNumber(1 or 5 or 8 or 10) * 4шт  +
             //   (  3+4+4  )/ getVertexNumber(2 or 11) * 2шт ) / 12шт
            //
             // ((2+2+2+2+3)/5.*6 + (2+2+3+4)/4.*4 + (3+4+4)/3.*2 ) / 12
             // (11/5.*6 + 11/4.*4 + 11/3.*2) / 12
             // 11*2*(1/5.*3 + 1/4.*2 + 1/3.) / 12
             // 11*2*(3/5. + 1/2. + 1/3.) / 12
             // 11*(3/5. + 1/2. + 1/3.) / 6
             // 11*(3/30. + 1/12. + 1/18.)
             // http://www.google.com/search?q=11*%283/30.+%2B+1/12.+%2B+1/18.%29
             //  2.62777777778
         default: throw new RuntimeException("Unknown Mode==" + Mode);
         }
      }
      @Override
      public Size getDirectionSizeField() { return new Size(3, 4); }
      @Override
      protected double getA   () { return Math.sqrt(getArea()*SQRT48); }
      protected double getB   () { return getA()/2; }
      protected double getROut() { return getA()/SQRT3; }
      protected double getRIn () { return getROut()/2; }
      //private double __snip  = 2.3456789 + new java.util.Random(java.util.UUID.randomUUID().hashCode()).nextInt(15);
      protected double getSnip() { return getA()/(/*12*/6.789012345 /*__snip*/); }
      @Override
      public double getSq(int borderWidth) {
         double w = borderWidth/2.;
         return (getA()-w*2/TAN15)/(SQRT3+3);
      }
   }

   public Triangle4(AttrTriangle4 attr, Coord coord) {
      super(attr, coord,
            (coord.y&3)*3+(coord.x%3) // 0..11
         );
   }

   @Override
   public AttrTriangle4 getAttr() {
      return (AttrTriangle4) super.getAttr();
   }

   @Override
   protected List<Coord> getCoordsNeighbor() {
      List<Coord> neighborCoord = new ArrayList<>(getAttr().getNeighborNumber(getDirection()));

      // определяю координаты соседей
      switch (AttrTriangle4.Mode) {
      case eUnrealMode:
         switch (direction) {
         case 0:
            neighborCoord.add(new Coord(coord.x-1, coord.y-2));
            neighborCoord.add(new Coord(coord.x+1, coord.y-2));
            neighborCoord.add(new Coord(coord.x-1, coord.y-1));
            neighborCoord.add(new Coord(coord.x  , coord.y-1));
            neighborCoord.add(new Coord(coord.x+1, coord.y-1));
            neighborCoord.add(new Coord(coord.x+2, coord.y-1));
            neighborCoord.add(new Coord(coord.x-2, coord.y  ));
            neighborCoord.add(new Coord(coord.x-1, coord.y  ));
            neighborCoord.add(new Coord(coord.x+1, coord.y  ));
            neighborCoord.add(new Coord(coord.x+2, coord.y  ));
            neighborCoord.add(new Coord(coord.x-3, coord.y+1));
            neighborCoord.add(new Coord(coord.x-2, coord.y+1));
            neighborCoord.add(new Coord(coord.x-1, coord.y+1));
            neighborCoord.add(new Coord(coord.x  , coord.y+1));
            neighborCoord.add(new Coord(coord.x+1, coord.y+1));
            neighborCoord.add(new Coord(coord.x-3, coord.y+2));
            neighborCoord.add(new Coord(coord.x-2, coord.y+2));
            neighborCoord.add(new Coord(coord.x-1, coord.y+2));
            neighborCoord.add(new Coord(coord.x  , coord.y+2));
            neighborCoord.add(new Coord(coord.x-2, coord.y+3));
            neighborCoord.add(new Coord(coord.x  , coord.y+3));
            break;
         case 1:
            neighborCoord.add(new Coord(coord.x-2, coord.y-2));
            neighborCoord.add(new Coord(coord.x  , coord.y-2));
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
            neighborCoord.add(new Coord(coord.x+2, coord.y+1));
            neighborCoord.add(new Coord(coord.x-1, coord.y+2));
            neighborCoord.add(new Coord(coord.x  , coord.y+2));
            neighborCoord.add(new Coord(coord.x+1, coord.y+2));
            neighborCoord.add(new Coord(coord.x+2, coord.y+2));
            neighborCoord.add(new Coord(coord.x  , coord.y+3));
            neighborCoord.add(new Coord(coord.x+2, coord.y+3));
            break;
         case 2:
            neighborCoord.add(new Coord(coord.x-3, coord.y-2));
            neighborCoord.add(new Coord(coord.x-1, coord.y-2));
            neighborCoord.add(new Coord(coord.x  , coord.y-2));
            neighborCoord.add(new Coord(coord.x+2, coord.y-2));
            neighborCoord.add(new Coord(coord.x-3, coord.y-1));
            neighborCoord.add(new Coord(coord.x-2, coord.y-1));
            neighborCoord.add(new Coord(coord.x-1, coord.y-1));
            neighborCoord.add(new Coord(coord.x  , coord.y-1));
            neighborCoord.add(new Coord(coord.x+1, coord.y-1));
            neighborCoord.add(new Coord(coord.x+2, coord.y-1));
            neighborCoord.add(new Coord(coord.x+3, coord.y-1));
            neighborCoord.add(new Coord(coord.x-3, coord.y  ));
            neighborCoord.add(new Coord(coord.x-2, coord.y  ));
            neighborCoord.add(new Coord(coord.x-1, coord.y  ));
            neighborCoord.add(new Coord(coord.x+1, coord.y  ));
            neighborCoord.add(new Coord(coord.x+2, coord.y  ));
            neighborCoord.add(new Coord(coord.x+3, coord.y  ));
            neighborCoord.add(new Coord(coord.x-3, coord.y+1));
            neighborCoord.add(new Coord(coord.x-1, coord.y+1));
            neighborCoord.add(new Coord(coord.x  , coord.y+1));
            neighborCoord.add(new Coord(coord.x+2, coord.y+1));
            break;
         case 3:
            neighborCoord.add(new Coord(coord.x-2, coord.y-1));
            neighborCoord.add(new Coord(coord.x  , coord.y-1));
            neighborCoord.add(new Coord(coord.x+1, coord.y-1));
            neighborCoord.add(new Coord(coord.x+3, coord.y-1));
            neighborCoord.add(new Coord(coord.x-3, coord.y  ));
            neighborCoord.add(new Coord(coord.x-2, coord.y  ));
            neighborCoord.add(new Coord(coord.x-1, coord.y  ));
            neighborCoord.add(new Coord(coord.x+1, coord.y  ));
            neighborCoord.add(new Coord(coord.x+2, coord.y  ));
            neighborCoord.add(new Coord(coord.x+3, coord.y  ));
            neighborCoord.add(new Coord(coord.x-3, coord.y+1));
            neighborCoord.add(new Coord(coord.x-2, coord.y+1));
            neighborCoord.add(new Coord(coord.x-1, coord.y+1));
            neighborCoord.add(new Coord(coord.x  , coord.y+1));
            neighborCoord.add(new Coord(coord.x+1, coord.y+1));
            neighborCoord.add(new Coord(coord.x+2, coord.y+1));
            neighborCoord.add(new Coord(coord.x+3, coord.y+1));
            neighborCoord.add(new Coord(coord.x-2, coord.y+2));
            neighborCoord.add(new Coord(coord.x  , coord.y+2));
            neighborCoord.add(new Coord(coord.x+1, coord.y+2));
            neighborCoord.add(new Coord(coord.x+3, coord.y+2));
            break;
         case 4:
            neighborCoord.add(new Coord(coord.x-2, coord.y-3));
            neighborCoord.add(new Coord(coord.x  , coord.y-3));
            neighborCoord.add(new Coord(coord.x-2, coord.y-2));
            neighborCoord.add(new Coord(coord.x-1, coord.y-2));
            neighborCoord.add(new Coord(coord.x  , coord.y-2));
            neighborCoord.add(new Coord(coord.x+1, coord.y-2));
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
            neighborCoord.add(new Coord(coord.x+2, coord.y+1));
            neighborCoord.add(new Coord(coord.x  , coord.y+2));
            neighborCoord.add(new Coord(coord.x+2, coord.y+2));
            break;
         case 5:
            neighborCoord.add(new Coord(coord.x  , coord.y-3));
            neighborCoord.add(new Coord(coord.x+2, coord.y-3));
            neighborCoord.add(new Coord(coord.x  , coord.y-2));
            neighborCoord.add(new Coord(coord.x+1, coord.y-2));
            neighborCoord.add(new Coord(coord.x+2, coord.y-2));
            neighborCoord.add(new Coord(coord.x+3, coord.y-2));
            neighborCoord.add(new Coord(coord.x-1, coord.y-1));
            neighborCoord.add(new Coord(coord.x  , coord.y-1));
            neighborCoord.add(new Coord(coord.x+1, coord.y-1));
            neighborCoord.add(new Coord(coord.x+2, coord.y-1));
            neighborCoord.add(new Coord(coord.x+3, coord.y-1));
            neighborCoord.add(new Coord(coord.x-2, coord.y  ));
            neighborCoord.add(new Coord(coord.x-1, coord.y  ));
            neighborCoord.add(new Coord(coord.x+1, coord.y  ));
            neighborCoord.add(new Coord(coord.x+2, coord.y  ));
            neighborCoord.add(new Coord(coord.x-2, coord.y+1));
            neighborCoord.add(new Coord(coord.x-1, coord.y+1));
            neighborCoord.add(new Coord(coord.x  , coord.y+1));
            neighborCoord.add(new Coord(coord.x+1, coord.y+1));
            neighborCoord.add(new Coord(coord.x-1, coord.y+2));
            neighborCoord.add(new Coord(coord.x+1, coord.y+2));
            break;
         case 6:
            neighborCoord.add(new Coord(coord.x-2, coord.y-2));
            neighborCoord.add(new Coord(coord.x  , coord.y-2));
            neighborCoord.add(new Coord(coord.x+1, coord.y-2));
            neighborCoord.add(new Coord(coord.x+3, coord.y-2));
            neighborCoord.add(new Coord(coord.x-3, coord.y-1));
            neighborCoord.add(new Coord(coord.x-2, coord.y-1));
            neighborCoord.add(new Coord(coord.x-1, coord.y-1));
            neighborCoord.add(new Coord(coord.x  , coord.y-1));
            neighborCoord.add(new Coord(coord.x+1, coord.y-1));
            neighborCoord.add(new Coord(coord.x+2, coord.y-1));
            neighborCoord.add(new Coord(coord.x+3, coord.y-1));
            neighborCoord.add(new Coord(coord.x-3, coord.y  ));
            neighborCoord.add(new Coord(coord.x-2, coord.y  ));
            neighborCoord.add(new Coord(coord.x-1, coord.y  ));
            neighborCoord.add(new Coord(coord.x+1, coord.y  ));
            neighborCoord.add(new Coord(coord.x+2, coord.y  ));
            neighborCoord.add(new Coord(coord.x+3, coord.y  ));
            neighborCoord.add(new Coord(coord.x-2, coord.y+1));
            neighborCoord.add(new Coord(coord.x  , coord.y+1));
            neighborCoord.add(new Coord(coord.x+1, coord.y+1));
            neighborCoord.add(new Coord(coord.x+3, coord.y+1));
            break;
         case 7:
            neighborCoord.add(new Coord(coord.x  , coord.y-2));
            neighborCoord.add(new Coord(coord.x+2, coord.y-2));
            neighborCoord.add(new Coord(coord.x-1, coord.y-1));
            neighborCoord.add(new Coord(coord.x  , coord.y-1));
            neighborCoord.add(new Coord(coord.x+1, coord.y-1));
            neighborCoord.add(new Coord(coord.x+2, coord.y-1));
            neighborCoord.add(new Coord(coord.x-2, coord.y  ));
            neighborCoord.add(new Coord(coord.x-1, coord.y  ));
            neighborCoord.add(new Coord(coord.x+1, coord.y  ));
            neighborCoord.add(new Coord(coord.x+2, coord.y  ));
            neighborCoord.add(new Coord(coord.x-2, coord.y+1));
            neighborCoord.add(new Coord(coord.x-1, coord.y+1));
            neighborCoord.add(new Coord(coord.x  , coord.y+1));
            neighborCoord.add(new Coord(coord.x+1, coord.y+1));
            neighborCoord.add(new Coord(coord.x+2, coord.y+1));
            neighborCoord.add(new Coord(coord.x-2, coord.y+2));
            neighborCoord.add(new Coord(coord.x-1, coord.y+2));
            neighborCoord.add(new Coord(coord.x  , coord.y+2));
            neighborCoord.add(new Coord(coord.x+1, coord.y+2));
            neighborCoord.add(new Coord(coord.x-2, coord.y+3));
            neighborCoord.add(new Coord(coord.x  , coord.y+3));
            break;
         case 8:
            neighborCoord.add(new Coord(coord.x-1, coord.y-2));
            neighborCoord.add(new Coord(coord.x+1, coord.y-2));
            neighborCoord.add(new Coord(coord.x-2, coord.y-1));
            neighborCoord.add(new Coord(coord.x-1, coord.y-1));
            neighborCoord.add(new Coord(coord.x  , coord.y-1));
            neighborCoord.add(new Coord(coord.x+1, coord.y-1));
            neighborCoord.add(new Coord(coord.x-2, coord.y  ));
            neighborCoord.add(new Coord(coord.x-1, coord.y  ));
            neighborCoord.add(new Coord(coord.x+1, coord.y  ));
            neighborCoord.add(new Coord(coord.x+2, coord.y  ));
            neighborCoord.add(new Coord(coord.x-1, coord.y+1));
            neighborCoord.add(new Coord(coord.x  , coord.y+1));
            neighborCoord.add(new Coord(coord.x+1, coord.y+1));
            neighborCoord.add(new Coord(coord.x+2, coord.y+1));
            neighborCoord.add(new Coord(coord.x+3, coord.y+1));
            neighborCoord.add(new Coord(coord.x  , coord.y+2));
            neighborCoord.add(new Coord(coord.x+1, coord.y+2));
            neighborCoord.add(new Coord(coord.x+2, coord.y+2));
            neighborCoord.add(new Coord(coord.x+3, coord.y+2));
            neighborCoord.add(new Coord(coord.x  , coord.y+3));
            neighborCoord.add(new Coord(coord.x+2, coord.y+3));
            break;
         case 9:
            neighborCoord.add(new Coord(coord.x-2, coord.y-3));
            neighborCoord.add(new Coord(coord.x  , coord.y-3));
            neighborCoord.add(new Coord(coord.x-3, coord.y-2));
            neighborCoord.add(new Coord(coord.x-2, coord.y-2));
            neighborCoord.add(new Coord(coord.x-1, coord.y-2));
            neighborCoord.add(new Coord(coord.x  , coord.y-2));
            neighborCoord.add(new Coord(coord.x-3, coord.y-1));
            neighborCoord.add(new Coord(coord.x-2, coord.y-1));
            neighborCoord.add(new Coord(coord.x-1, coord.y-1));
            neighborCoord.add(new Coord(coord.x  , coord.y-1));
            neighborCoord.add(new Coord(coord.x+1, coord.y-1));
            neighborCoord.add(new Coord(coord.x-2, coord.y  ));
            neighborCoord.add(new Coord(coord.x-1, coord.y  ));
            neighborCoord.add(new Coord(coord.x+1, coord.y  ));
            neighborCoord.add(new Coord(coord.x+2, coord.y  ));
            neighborCoord.add(new Coord(coord.x-1, coord.y+1));
            neighborCoord.add(new Coord(coord.x  , coord.y+1));
            neighborCoord.add(new Coord(coord.x+1, coord.y+1));
            neighborCoord.add(new Coord(coord.x+2, coord.y+1));
            neighborCoord.add(new Coord(coord.x-1, coord.y+2));
            neighborCoord.add(new Coord(coord.x+1, coord.y+2));
            break;
         case 10:
            neighborCoord.add(new Coord(coord.x  , coord.y-3));
            neighborCoord.add(new Coord(coord.x+2, coord.y-3));
            neighborCoord.add(new Coord(coord.x-1, coord.y-2));
            neighborCoord.add(new Coord(coord.x  , coord.y-2));
            neighborCoord.add(new Coord(coord.x+1, coord.y-2));
            neighborCoord.add(new Coord(coord.x+2, coord.y-2));
            neighborCoord.add(new Coord(coord.x-2, coord.y-1));
            neighborCoord.add(new Coord(coord.x-1, coord.y-1));
            neighborCoord.add(new Coord(coord.x  , coord.y-1));
            neighborCoord.add(new Coord(coord.x+1, coord.y-1));
            neighborCoord.add(new Coord(coord.x+2, coord.y-1));
            neighborCoord.add(new Coord(coord.x-2, coord.y  ));
            neighborCoord.add(new Coord(coord.x-1, coord.y  ));
            neighborCoord.add(new Coord(coord.x+1, coord.y  ));
            neighborCoord.add(new Coord(coord.x+2, coord.y  ));
            neighborCoord.add(new Coord(coord.x-2, coord.y+1));
            neighborCoord.add(new Coord(coord.x-1, coord.y+1));
            neighborCoord.add(new Coord(coord.x  , coord.y+1));
            neighborCoord.add(new Coord(coord.x+1, coord.y+1));
            neighborCoord.add(new Coord(coord.x-2, coord.y+2));
            neighborCoord.add(new Coord(coord.x  , coord.y+2));
            break;
         case 11:
            neighborCoord.add(new Coord(coord.x-3, coord.y-1));
            neighborCoord.add(new Coord(coord.x-1, coord.y-1));
            neighborCoord.add(new Coord(coord.x  , coord.y-1));
            neighborCoord.add(new Coord(coord.x+2, coord.y-1));
            neighborCoord.add(new Coord(coord.x-3, coord.y  ));
            neighborCoord.add(new Coord(coord.x-2, coord.y  ));
            neighborCoord.add(new Coord(coord.x-1, coord.y  ));
            neighborCoord.add(new Coord(coord.x+1, coord.y  ));
            neighborCoord.add(new Coord(coord.x+2, coord.y  ));
            neighborCoord.add(new Coord(coord.x+3, coord.y  ));
            neighborCoord.add(new Coord(coord.x-3, coord.y+1));
            neighborCoord.add(new Coord(coord.x-2, coord.y+1));
            neighborCoord.add(new Coord(coord.x-1, coord.y+1));
            neighborCoord.add(new Coord(coord.x  , coord.y+1));
            neighborCoord.add(new Coord(coord.x+1, coord.y+1));
            neighborCoord.add(new Coord(coord.x+2, coord.y+1));
            neighborCoord.add(new Coord(coord.x+3, coord.y+1));
            neighborCoord.add(new Coord(coord.x-3, coord.y+2));
            neighborCoord.add(new Coord(coord.x-1, coord.y+2));
            neighborCoord.add(new Coord(coord.x  , coord.y+2));
            neighborCoord.add(new Coord(coord.x+2, coord.y+2));
            break;
         }
         break;
      case eMeanMode:
         switch (direction) {
         case 0:
            neighborCoord.add(new Coord(coord.x-1, coord.y+1));
            neighborCoord.add(new Coord(coord.x  , coord.y+1));
            neighborCoord.add(new Coord(coord.x+1, coord.y+1));
            break;
         case 1:
            neighborCoord.add(new Coord(coord.x+1, coord.y  ));
            neighborCoord.add(new Coord(coord.x  , coord.y+1));
            neighborCoord.add(new Coord(coord.x+1, coord.y+1));
            neighborCoord.add(new Coord(coord.x+1, coord.y+2));
            neighborCoord.add(new Coord(coord.x  , coord.y+3));
            break;
         case 2:
            neighborCoord.add(new Coord(coord.x-3, coord.y-1));
            neighborCoord.add(new Coord(coord.x  , coord.y-1));
            neighborCoord.add(new Coord(coord.x+3, coord.y-1));
            neighborCoord.add(new Coord(coord.x-3, coord.y  ));
            neighborCoord.add(new Coord(coord.x-1, coord.y  ));
            neighborCoord.add(new Coord(coord.x+3, coord.y  ));
            neighborCoord.add(new Coord(coord.x  , coord.y+1));
            break;
         case 3:
            neighborCoord.add(new Coord(coord.x  , coord.y-1));
            neighborCoord.add(new Coord(coord.x+1, coord.y  ));
            neighborCoord.add(new Coord(coord.x  , coord.y+1));
            break;
         case 4:
            neighborCoord.add(new Coord(coord.x-1, coord.y-1));
            neighborCoord.add(new Coord(coord.x  , coord.y-1));
            neighborCoord.add(new Coord(coord.x-1, coord.y  ));
            break;
         case 5:
            neighborCoord.add(new Coord(coord.x-1, coord.y-1));
            neighborCoord.add(new Coord(coord.x  , coord.y-1));
            neighborCoord.add(new Coord(coord.x+1, coord.y-1));
            neighborCoord.add(new Coord(coord.x  , coord.y+1));
            neighborCoord.add(new Coord(coord.x-1, coord.y+2));
            break;
         case 6:
            neighborCoord.add(new Coord(coord.x  , coord.y-1));
            neighborCoord.add(new Coord(coord.x+1, coord.y  ));
            neighborCoord.add(new Coord(coord.x  , coord.y+1));
            break;
         case 7:
            neighborCoord.add(new Coord(coord.x-1, coord.y  ));
            neighborCoord.add(new Coord(coord.x-1, coord.y+1));
            neighborCoord.add(new Coord(coord.x  , coord.y+1));
            break;
         case 8:
            neighborCoord.add(new Coord(coord.x-1, coord.y-2));
            neighborCoord.add(new Coord(coord.x  , coord.y-1));
            neighborCoord.add(new Coord(coord.x-1, coord.y+1));
            neighborCoord.add(new Coord(coord.x  , coord.y+1));
            neighborCoord.add(new Coord(coord.x+1, coord.y+1));
            break;
         case 9:
            neighborCoord.add(new Coord(coord.x-1, coord.y-1));
            neighborCoord.add(new Coord(coord.x  , coord.y-1));
            neighborCoord.add(new Coord(coord.x+1, coord.y-1));
            break;
         case 10:
            neighborCoord.add(new Coord(coord.x  , coord.y-3));
            neighborCoord.add(new Coord(coord.x+1, coord.y-2));
            neighborCoord.add(new Coord(coord.x  , coord.y-1));
            neighborCoord.add(new Coord(coord.x+1, coord.y-1));
            neighborCoord.add(new Coord(coord.x+1, coord.y  ));
            break;
         case 11:
            neighborCoord.add(new Coord(coord.x  , coord.y-1));
            neighborCoord.add(new Coord(coord.x-3, coord.y  ));
            neighborCoord.add(new Coord(coord.x-1, coord.y  ));
            neighborCoord.add(new Coord(coord.x+3, coord.y  ));
            neighborCoord.add(new Coord(coord.x-3, coord.y+1));
            neighborCoord.add(new Coord(coord.x  , coord.y+1));
            neighborCoord.add(new Coord(coord.x+3, coord.y+1));
            break;
         }
         break;
      case eOptimalMode:
         switch (direction) {
         case 0:
            neighborCoord.add(new Coord(coord.x+1, coord.y-2));
            neighborCoord.add(new Coord(coord.x+1, coord.y-1));
            neighborCoord.add(new Coord(coord.x+2, coord.y-1));
            neighborCoord.add(new Coord(coord.x-1, coord.y  ));
            neighborCoord.add(new Coord(coord.x-1, coord.y+1));
            neighborCoord.add(new Coord(coord.x  , coord.y+1));
            neighborCoord.add(new Coord(coord.x+1, coord.y+1));
            break;
         case 1:
            neighborCoord.add(new Coord(coord.x+1, coord.y  ));
            neighborCoord.add(new Coord(coord.x-1, coord.y+1));
            neighborCoord.add(new Coord(coord.x  , coord.y+1));
            neighborCoord.add(new Coord(coord.x+1, coord.y+1));
            neighborCoord.add(new Coord(coord.x+1, coord.y+2));
            neighborCoord.add(new Coord(coord.x+2, coord.y+2));
            neighborCoord.add(new Coord(coord.x+2, coord.y+3));
            break;
         case 2:
            neighborCoord.add(new Coord(coord.x+2, coord.y-2));
            neighborCoord.add(new Coord(coord.x  , coord.y-1));
            neighborCoord.add(new Coord(coord.x+2, coord.y-1));
            neighborCoord.add(new Coord(coord.x+3, coord.y-1));
            neighborCoord.add(new Coord(coord.x-1, coord.y  ));
            neighborCoord.add(new Coord(coord.x+1, coord.y  ));
            neighborCoord.add(new Coord(coord.x  , coord.y+1));
            break;
         case 3:
            neighborCoord.add(new Coord(coord.x  , coord.y-1));
            neighborCoord.add(new Coord(coord.x+1, coord.y-1));
            neighborCoord.add(new Coord(coord.x+1, coord.y  ));
            neighborCoord.add(new Coord(coord.x  , coord.y+1));
            neighborCoord.add(new Coord(coord.x+2, coord.y+1));
            neighborCoord.add(new Coord(coord.x+3, coord.y+1));
            neighborCoord.add(new Coord(coord.x+3, coord.y+2));
            break;
         case 4:
            neighborCoord.add(new Coord(coord.x-1, coord.y-1));
            neighborCoord.add(new Coord(coord.x  , coord.y-1));
            neighborCoord.add(new Coord(coord.x-1, coord.y  ));
            neighborCoord.add(new Coord(coord.x+1, coord.y+1));
            neighborCoord.add(new Coord(coord.x+2, coord.y+1));
            neighborCoord.add(new Coord(coord.x+2, coord.y+2));
            break;
         case 5:
            neighborCoord.add(new Coord(coord.x+2, coord.y-3));
            neighborCoord.add(new Coord(coord.x+2, coord.y-2));
            neighborCoord.add(new Coord(coord.x+3, coord.y-2));
            neighborCoord.add(new Coord(coord.x-1, coord.y-1));
            neighborCoord.add(new Coord(coord.x  , coord.y-1));
            neighborCoord.add(new Coord(coord.x+1, coord.y-1));
            break;
         case 6:
            neighborCoord.add(new Coord(coord.x-2, coord.y-2));
            neighborCoord.add(new Coord(coord.x-3, coord.y-1));
            neighborCoord.add(new Coord(coord.x-2, coord.y-1));
            neighborCoord.add(new Coord(coord.x  , coord.y-1));
            neighborCoord.add(new Coord(coord.x-1, coord.y  ));
            neighborCoord.add(new Coord(coord.x+1, coord.y  ));
            neighborCoord.add(new Coord(coord.x  , coord.y+1));
            break;
         case 7:
            neighborCoord.add(new Coord(coord.x-1, coord.y  ));
            neighborCoord.add(new Coord(coord.x-1, coord.y+1));
            neighborCoord.add(new Coord(coord.x  , coord.y+1));
            neighborCoord.add(new Coord(coord.x+1, coord.y+1));
            neighborCoord.add(new Coord(coord.x-2, coord.y+2));
            neighborCoord.add(new Coord(coord.x-1, coord.y+2));
            neighborCoord.add(new Coord(coord.x-2, coord.y+3));
            break;
         case 8:
            neighborCoord.add(new Coord(coord.x-1, coord.y-2));
            neighborCoord.add(new Coord(coord.x-2, coord.y-1));
            neighborCoord.add(new Coord(coord.x-1, coord.y-1));
            neighborCoord.add(new Coord(coord.x+1, coord.y  ));
            neighborCoord.add(new Coord(coord.x-1, coord.y+1));
            neighborCoord.add(new Coord(coord.x  , coord.y+1));
            neighborCoord.add(new Coord(coord.x+1, coord.y+1));
            break;
         case 9:
            neighborCoord.add(new Coord(coord.x-2, coord.y-3));
            neighborCoord.add(new Coord(coord.x-3, coord.y-2));
            neighborCoord.add(new Coord(coord.x-2, coord.y-2));
            neighborCoord.add(new Coord(coord.x-1, coord.y-1));
            neighborCoord.add(new Coord(coord.x  , coord.y-1));
            neighborCoord.add(new Coord(coord.x+1, coord.y-1));
            break;
         case 10:
            neighborCoord.add(new Coord(coord.x  , coord.y-1));
            neighborCoord.add(new Coord(coord.x+1, coord.y-1));
            neighborCoord.add(new Coord(coord.x+1, coord.y  ));
            neighborCoord.add(new Coord(coord.x-2, coord.y+1));
            neighborCoord.add(new Coord(coord.x-1, coord.y+1));
            neighborCoord.add(new Coord(coord.x-2, coord.y+2));
            break;
         case 11:
            neighborCoord.add(new Coord(coord.x-1, coord.y-1));
            neighborCoord.add(new Coord(coord.x  , coord.y-1));
            neighborCoord.add(new Coord(coord.x-1, coord.y  ));
            neighborCoord.add(new Coord(coord.x-3, coord.y+1));
            neighborCoord.add(new Coord(coord.x-2, coord.y+1));
            neighborCoord.add(new Coord(coord.x  , coord.y+1));
            neighborCoord.add(new Coord(coord.x-3, coord.y+2));
            break;
         }
         break;
      case eSimpeMode:
         switch (direction) {
         case 0:
            neighborCoord.add(new Coord(coord.x-1, coord.y+1));
            neighborCoord.add(new Coord(coord.x  , coord.y+1));
            neighborCoord.add(new Coord(coord.x+1, coord.y+1));
            break;
         case 1:
            neighborCoord.add(new Coord(coord.x+1, coord.y  ));
            neighborCoord.add(new Coord(coord.x  , coord.y+1));
            neighborCoord.add(new Coord(coord.x+1, coord.y+1));
            break;
         case 2:
            neighborCoord.add(new Coord(coord.x  , coord.y-1));
            neighborCoord.add(new Coord(coord.x-1, coord.y  ));
            neighborCoord.add(new Coord(coord.x  , coord.y+1));
            break;
         case 3:
            neighborCoord.add(new Coord(coord.x  , coord.y-1));
            neighborCoord.add(new Coord(coord.x+1, coord.y  ));
            neighborCoord.add(new Coord(coord.x  , coord.y+1));
            break;
         case 4:
            neighborCoord.add(new Coord(coord.x-1, coord.y-1));
            neighborCoord.add(new Coord(coord.x  , coord.y-1));
            neighborCoord.add(new Coord(coord.x-1, coord.y  ));
            break;
         case 5:
            neighborCoord.add(new Coord(coord.x-1, coord.y-1));
            neighborCoord.add(new Coord(coord.x  , coord.y-1));
            neighborCoord.add(new Coord(coord.x+1, coord.y-1));
            break;
         case 6:
            neighborCoord.add(new Coord(coord.x  , coord.y-1));
            neighborCoord.add(new Coord(coord.x+1, coord.y  ));
            neighborCoord.add(new Coord(coord.x  , coord.y+1));
            break;
         case 7:
            neighborCoord.add(new Coord(coord.x-1, coord.y  ));
            neighborCoord.add(new Coord(coord.x-1, coord.y+1));
            neighborCoord.add(new Coord(coord.x  , coord.y+1));
            break;
         case 8:
            neighborCoord.add(new Coord(coord.x-1, coord.y+1));
            neighborCoord.add(new Coord(coord.x  , coord.y+1));
            neighborCoord.add(new Coord(coord.x+1, coord.y+1));
            break;
         case 9:
            neighborCoord.add(new Coord(coord.x-1, coord.y-1));
            neighborCoord.add(new Coord(coord.x  , coord.y-1));
            neighborCoord.add(new Coord(coord.x+1, coord.y-1));
            break;
         case 10:
            neighborCoord.add(new Coord(coord.x  , coord.y-1));
            neighborCoord.add(new Coord(coord.x+1, coord.y-1));
            neighborCoord.add(new Coord(coord.x+1, coord.y  ));
            break;
         case 11:
            neighborCoord.add(new Coord(coord.x  , coord.y-1));
            neighborCoord.add(new Coord(coord.x-1, coord.y  ));
            neighborCoord.add(new Coord(coord.x  , coord.y+1));
            break;
         }
         break;
      }

      return neighborCoord;
   }

   @Override
   protected void calcRegion() {
      AttrTriangle4 attr = getAttr();
      double a = attr.getA();
      double b = attr.getB();
      double R = attr.getROut();
      double r = attr.getRIn();
      double s = (AttrTriangle4.Mode != ComplexityMode.eUnrealMode) ? attr.getSnip() : 0;
      double c = (AttrTriangle4.Mode != ComplexityMode.eUnrealMode) ? s/2*SQRT3 : 0; // s * cos30
      double u = (AttrTriangle4.Mode != ComplexityMode.eUnrealMode) ? s/2 : 0; // s * cos60

      // определение координат точек фигуры
      double oX =  (coord.x/3)*a + b;      // offset X
      double oY = ((coord.y/4)*2+1)*(R+r); // offset Y
      switch (AttrTriangle4.Mode) {
      case eUnrealMode:
      case eOptimalMode:
         break;
      case eMeanMode:
      case eSimpeMode:
         oX -= u;
         break;
      }

      switch (AttrTriangle4.Mode) {
      case eUnrealMode:
         switch (direction) {
         case 0:
            region.setPoint(0, oX    , oY - R-r);
            region.setPoint(1, oX    , oY - r  );
            region.setPoint(2, oX - b, oY      );
            break;
         case 1:
            region.setPoint(0, oX + b, oY - R  );
            region.setPoint(1, oX + b, oY      );
            region.setPoint(2, oX    , oY - R-r);
            break;
         case 2:
            region.setPoint(0, oX + a, oY - R-r);
            region.setPoint(1, oX + b, oY - R  );
            region.setPoint(2, oX    , oY - R-r);
            break;
         case 3:
            region.setPoint(0, oX + b, oY      );
            region.setPoint(1, oX - b, oY      );
            region.setPoint(2, oX    , oY - r  );
            break;
         case 4:
            region.setPoint(0, oX    , oY - R-r);
            region.setPoint(1, oX + b, oY      );
            region.setPoint(2, oX    , oY - r  );
            break;
         case 5:
            region.setPoint(0, oX + a, oY - R-r);
            region.setPoint(1, oX + b, oY      );
            region.setPoint(2, oX + b, oY - R  );
            break;
         case 6:
            region.setPoint(0, oX + b, oY      );
            region.setPoint(1, oX    , oY + r  );
            region.setPoint(2, oX - b, oY      );
            break;
         case 7:
            region.setPoint(0, oX + b, oY      );
            region.setPoint(1, oX    , oY + R+r);
            region.setPoint(2, oX    , oY + r  );
            break;
         case 8:
            region.setPoint(0, oX + b, oY      );
            region.setPoint(1, oX + a, oY + R+r);
            region.setPoint(2, oX + b, oY + R  );
            break;
         case 9:
            region.setPoint(0, oX    , oY + r  );
            region.setPoint(1, oX    , oY + R+r);
            region.setPoint(2, oX - b, oY      );
            break;
         case 10:
            region.setPoint(0, oX + b, oY      );
            region.setPoint(1, oX + b, oY + R  );
            region.setPoint(2, oX    , oY + R+r);
            break;
         case 11:
            region.setPoint(0, oX + a, oY + R+r);
            region.setPoint(1, oX    , oY + R+r);
            region.setPoint(2, oX + b, oY + R  );
            break;
         }
         break;
      case eMeanMode:
         switch (direction) {
         case 0:
            region.setPoint(0, oX    , oY-R-r+s);
            region.setPoint(1, oX    , oY - r  );
            region.setPoint(2, oX-b+c, oY - u  );
            region.setPoint(3, oX-b+u, oY - c  );
            region.setPoint(4, oX - u, oY-R-r+c);
            break;
         case 1:
            region.setPoint(0, oX + b, oY - R  );
            region.setPoint(1, oX + b, oY      );
            region.setPoint(2, oX + u, oY-R-r+c);
            region.setPoint(3, oX + c, oY-R-r+u);
            break;
         case 2:
            region.setPoint(0, oX + a, oY - R-r);
            region.setPoint(1, oX + b, oY - R  );
            region.setPoint(2, oX    , oY - R-r);
            break;
         case 3:
            region.setPoint(0, oX+b-s, oY      );
            region.setPoint(1, oX-b+s, oY      );
            region.setPoint(2, oX-b+c, oY - u  );
            region.setPoint(3, oX    , oY - r  );
            region.setPoint(4, oX+b-c, oY - u  );
            break;
         case 4:
            region.setPoint(0, oX + u, oY-R-r+c);
            region.setPoint(1, oX+b-u, oY - c  );
            region.setPoint(2, oX+b-c, oY - u  );
            region.setPoint(3, oX    , oY - r  );
            region.setPoint(4, oX    , oY-R-r+s);
            break;
         case 5:
            region.setPoint(0, oX+a-u, oY-R-r+c);
            region.setPoint(1, oX + b, oY      );
            region.setPoint(2, oX + b, oY - R  );
            region.setPoint(3, oX+a-c, oY-R-r+u);
            break;
         case 6:
            region.setPoint(0, oX+b-c, oY + u  );
            region.setPoint(1, oX    , oY + r  );
            region.setPoint(2, oX-b+c, oY + u  );
            region.setPoint(3, oX-b+s, oY      );
            region.setPoint(4, oX+b-s, oY      );
            break;
         case 7:
            region.setPoint(0, oX+b-u, oY + c  );
            region.setPoint(1, oX + u, oY+R+r-c);
            region.setPoint(2, oX    , oY+R+r-s);
            region.setPoint(3, oX    , oY + r  );
            region.setPoint(4, oX+b-c, oY + u  );
            break;
         case 8:
            region.setPoint(0, oX + b, oY      );
            region.setPoint(1, oX+a-u, oY+R+r-c);
            region.setPoint(2, oX+a-c, oY+R+r-u);
            region.setPoint(3, oX + b, oY + R  );
            break;
         case 9:
            region.setPoint(0, oX    , oY + r  );
            region.setPoint(1, oX    , oY+R+r-s);
            region.setPoint(2, oX - u, oY+R+r-c);
            region.setPoint(3, oX-b+u, oY + c  );
            region.setPoint(4, oX-b+c, oY + u  );
            break;
         case 10:
            region.setPoint(0, oX + b, oY      );
            region.setPoint(1, oX + b, oY + R  );
            region.setPoint(2, oX + c, oY+R+r-u);
            region.setPoint(3, oX + u, oY+R+r-c);
            break;
         case 11:
            region.setPoint(0, oX + a, oY + R+r);
            region.setPoint(2, oX + b, oY + R  );
            region.setPoint(1, oX    , oY + R+r);
            break;
         }
         break;
      case eOptimalMode:
         switch (direction) {
         case 0:
            region.setPoint(0, oX    , oY - R-r);
            region.setPoint(1, oX    , oY - r  );
            region.setPoint(2, oX-b+c, oY - u  );
            region.setPoint(3, oX-b+u, oY - c  );
            break;
         case 1:
            region.setPoint(0, oX + b, oY - R  );
            region.setPoint(1, oX + b, oY      );
            region.setPoint(2, oX + u, oY-R-r+c);
            region.setPoint(3, oX + c, oY-R-r+u);
            break;
         case 2:
            region.setPoint(0, oX + a, oY -R -r);
            region.setPoint(1, oX + b, oY - R  );
            region.setPoint(2, oX + c, oY-R-r+u);
            region.setPoint(3, oX + s, oY -R -r);
            break;
         case 3:
            region.setPoint(0, oX+b  , oY      );
            region.setPoint(1, oX-b+s, oY      );
            region.setPoint(2, oX-b+c, oY - u  );
            region.setPoint(3, oX    , oY - r  );
            break;
         case 4:
            region.setPoint(0, oX + u, oY-R-r+c);
            region.setPoint(1, oX + b, oY      );
            region.setPoint(2, oX    , oY - r  );
            region.setPoint(3, oX    , oY-R-r+s);
            break;
         case 5:
            region.setPoint(0, oX+a  , oY - R-r);
            region.setPoint(1, oX+b+u, oY - c  );
            region.setPoint(2, oX + b, oY - s  );
            region.setPoint(3, oX + b, oY - R  );
            break;
         case 6:
            region.setPoint(0, oX+b-c, oY + u  );
            region.setPoint(1, oX    , oY + r  );
            region.setPoint(2, oX-b  , oY      );
            region.setPoint(3, oX+b-s, oY      );
            break;
         case 7:
            region.setPoint(0, oX+b-u, oY + c  );
            region.setPoint(1, oX    , oY + R+r);
            region.setPoint(2, oX    , oY + r  );
            region.setPoint(3, oX+b-c, oY + u  );
            break;
         case 8:
            region.setPoint(0, oX + b, oY      );
            region.setPoint(1, oX+a-u, oY+R+r-c);
            region.setPoint(2, oX+a-c, oY+R+r-u);
            region.setPoint(3, oX + b, oY + R  );
            break;
         case 9:
            region.setPoint(0, oX    , oY + r  );
            region.setPoint(1, oX    , oY+R+r-s);
            region.setPoint(2, oX - u, oY+R+r-c);
            region.setPoint(3, oX - b, oY      );
            break;
         case 10:
            region.setPoint(0, oX + b, oY + s  );
            region.setPoint(1, oX + b, oY + R  );
            region.setPoint(2, oX    , oY+R+r  );
            region.setPoint(3, oX+b-u, oY + c  );
            break;
         case 11:
            region.setPoint(0, oX+a-c, oY+R+r-u);
            region.setPoint(1, oX+a-s, oY + R+r);
            region.setPoint(2, oX    , oY + R+r);
            region.setPoint(3, oX + b, oY + R  );
            break;
         }
         break;
      case eSimpeMode:
         switch (direction) {
         case 0:
            region.setPoint(0, oX    , oY-R-r+s);
            region.setPoint(1, oX    , oY - r  );
            region.setPoint(2, oX-b+c, oY - u  );
            region.setPoint(3, oX-b+u, oY - c  );
            region.setPoint(4, oX - u, oY-R-r+c);
            break;
         case 1:
            region.setPoint(0, oX + b, oY - R  );
            region.setPoint(1, oX + b, oY - s  );
            region.setPoint(2, oX+b-u, oY - c  );
            region.setPoint(3, oX + u, oY-R-r+c);
            region.setPoint(4, oX + c, oY-R-r+u);
            break;
         case 2:
            region.setPoint(0, oX+a-c, oY-R-r+u);
            region.setPoint(1, oX + b, oY - R  );
            region.setPoint(2, oX + c, oY-R-r+u);
            region.setPoint(3, oX + s, oY -R -r);
            region.setPoint(4, oX+a-s, oY -R -r);
            break;
         case 3:
            region.setPoint(0, oX+b-s, oY      );
            region.setPoint(1, oX-b+s, oY      );
            region.setPoint(2, oX-b+c, oY - u  );
            region.setPoint(3, oX    , oY - r  );
            region.setPoint(4, oX+b-c, oY - u  );
            break;
         case 4:
            region.setPoint(0, oX + u, oY-R-r+c);
            region.setPoint(1, oX+b-u, oY - c  );
            region.setPoint(2, oX+b-c, oY - u  );
            region.setPoint(3, oX    , oY - r  );
            region.setPoint(4, oX    , oY-R-r+s);
            break;
         case 5:
            region.setPoint(0, oX+a-u, oY-R-r+c);
            region.setPoint(1, oX+b+u, oY - c  );
            region.setPoint(2, oX + b, oY - s  );
            region.setPoint(3, oX + b, oY - R  );
            region.setPoint(4, oX+a-c, oY-R-r+u);
            break;
         case 6:
            region.setPoint(0, oX+b-c, oY + u  );
            region.setPoint(1, oX    , oY + r  );
            region.setPoint(2, oX-b+c, oY + u  );
            region.setPoint(3, oX-b+s, oY      );
            region.setPoint(4, oX+b-s, oY      );
            break;
         case 7:
            region.setPoint(0, oX+b-u, oY + c  );
            region.setPoint(1, oX + u, oY+R+r-c);
            region.setPoint(2, oX    , oY+R+r-s);
            region.setPoint(3, oX    , oY + r  );
            region.setPoint(4, oX+b-c, oY + u  );
            break;
         case 8:
            region.setPoint(0, oX+b+u, oY + c  );
            region.setPoint(1, oX+a-u, oY+R+r-c);
            region.setPoint(2, oX+a-c, oY+R+r-u);
            region.setPoint(3, oX + b, oY + R  );
            region.setPoint(4, oX + b, oY + s  );
            break;
         case 9:
            region.setPoint(0, oX    , oY + r  );
            region.setPoint(1, oX    , oY+R+r-s);
            region.setPoint(2, oX - u, oY+R+r-c);
            region.setPoint(3, oX-b+u, oY + c  );
            region.setPoint(4, oX-b+c, oY + u  );
            break;
         case 10:
            region.setPoint(0, oX + b, oY + s  );
            region.setPoint(1, oX + b, oY + R  );
            region.setPoint(2, oX + c, oY+R+r-u);
            region.setPoint(3, oX + u, oY+R+r-c);
            region.setPoint(4, oX+b-u, oY + c  );
            break;
         case 11:
            region.setPoint(0, oX+a-s, oY + R+r);
            region.setPoint(1, oX + s, oY + R+r);
            region.setPoint(2, oX + c, oY+R+r-u);
            region.setPoint(3, oX + b, oY + R  );
            region.setPoint(4, oX+a-c, oY+R+r-u);
            break;
         }
         break;
      }
   }

   @Override
   public RectDouble getRcInner(int borderWidth) {
      AttrTriangle4 attr = getAttr();
      double w = borderWidth/2.;
      double sq    = attr.getSq(borderWidth);
      double sq2   = sq/2;
      double sq2w  = sq2+w;
      double sq2w3 = sq2+w/SQRT3;

      PointDouble center = new PointDouble(); // координата центра квадрата
      switch (direction) {
      case 0: case 10:
         center.x = region.getPoint(1).x - sq2w;
         center.y = region.getPoint(1).y - sq2w3;
         break;
      case 1: case 9:
         center.x = region.getPoint(0).x - sq2w;
         center.y = region.getPoint(0).y + sq2w3;
         break;
      case 2: case 6:
         center.x = region.getPoint(1).x;
         switch (AttrTriangle4.Mode) {
         case eUnrealMode : center.y = region.getPoint(                 0    ).y + sq2w; break;
         case eMeanMode   : center.y = region.getPoint((direction==2) ? 0 : 4).y + sq2w; break;
         case eOptimalMode: center.y = region.getPoint(                   3  ).y + sq2w; break;
         case eSimpeMode  : center.y = region.getPoint(                     4).y + sq2w; break;
         }
         break;
      case 3: case 11:
         switch (AttrTriangle4.Mode) {
         case eUnrealMode : center.x = region.getPoint(                     2).x; break;
         case eMeanMode   : center.x = region.getPoint((direction==3) ? 3 : 2).x; break;
         case eOptimalMode: center.x = region.getPoint(                 3    ).x; break;
         case eSimpeMode  : center.x = region.getPoint(                 3    ).x; break;
         }
         center.y = region.getPoint(1).y - sq2w;
         break;
      case 4: case 8:
         switch (AttrTriangle4.Mode) {
         case eUnrealMode:
            center.x = region.getPoint(2).x + sq2w;
            center.y = region.getPoint(2).y - sq2w3;
            break;
         case eOptimalMode:
            center.x = region.getPoint(3).x + sq2w;
            center.y = region.getPoint((direction==4) ? 2 : 3).y - sq2w3;
            break;
         case eMeanMode  :
         case eSimpeMode :
            center.x = region.getPoint(3).x + sq2w;
            center.y = region.getPoint(3).y - sq2w3;
            break;
         }
         break;
      case 5: case 7:
         switch (AttrTriangle4.Mode) {
         case eUnrealMode : center.x = region.getPoint(                 2    ).x + sq2w;
                            center.y = region.getPoint(                 2    ).y + sq2w3; break;
         case eMeanMode   : center.x = region.getPoint((direction==5) ? 2 : 3).x + sq2w;
                            center.y = region.getPoint((direction==5) ? 2 : 3).y + sq2w3; break;
         case eOptimalMode: center.x = region.getPoint((direction!=5) ? 2 : 3).x + sq2w;
                            center.y = region.getPoint((direction!=5) ? 2 : 3).y + sq2w3; break;
         case eSimpeMode  : center.x = region.getPoint(                     3).x + sq2w;
                            center.y = region.getPoint(                     3).y + sq2w3; break;
         }
         break;
      }

      return new RectDouble(
         center.x - sq2,
         center.y - sq2,
         sq, sq);
   }

   @Override
   public int getShiftPointBorderIndex() {
      switch (AttrTriangle4.Mode) {
      case eUnrealMode:
         switch (direction) {
         case 1: case 3: case 5: case 7: case 9: case 11: return 1;
         default: return 2;
         }
      case eMeanMode:
         switch (direction) {
         case 1: case 3: case 5: case 7: case 9: case 11: return 1;
         case 4: case 8: return 3;
         default: return 2;
         }
      case eOptimalMode:
         switch (direction) {
         case 1: case 3: case 5: case 7: return 1;
         case 8: return 3;
         default: return 2;
         }
      case eSimpeMode:
         switch (direction) {
         case 1: case 2: case 3: case 5: case 7: case 9: case 11: return 2;
         default: return 3;
         }
      default: throw new RuntimeException("Unknown Mode==" + AttrTriangle4.Mode);
      }
   }
}