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

using System;
using fmg.common.geom;
 
namespace fmg.core.mosaic.cells {

/// <summary> Треугольник. Вариант 4 - треугольник 30°-30°-120° </summary>
public class Triangle4 : BaseCell {
   public enum ComplexityMode {
      /** original */
      eUnrealMode,
      eMeanMode,
      eOptimalMode,
      eSimpeMode
   }

   public class AttrTriangle4 : BaseAttribute {
      public AttrTriangle4(int area)
         : base(area)
      {}

      public static readonly ComplexityMode Mode = ComplexityMode.eOptimalMode; // TODO: check others to view...

      public override Size GetOwnerSize(Size sizeField) {
         double b = B;
         double r = RIn;
         double R = ROut;
         Size result = new Size(
               (int)( b+b *((sizeField.width+2)/3) +
                        b *((sizeField.width+0)/3)),
               (int)((R+r)*((sizeField.height+1)/2)));

         switch (Mode) {
         case ComplexityMode.eUnrealMode:
         case ComplexityMode.eOptimalMode:
            // none  ...
            break;
         case ComplexityMode.eMeanMode:
         case ComplexityMode.eSimpeMode: {
               double u = CalcSnip() / 2; // Snip * cos60
               double c = u * SQRT3; // Snip * cos30
               switch (sizeField.width % 3) {
               case 0: result.width -= (int)(u + u); break;
               case 1: result.width -= (int)(u + c); break;
               case 2: result.width -= (int)u; break;
               }
               if (Mode == ComplexityMode.eMeanMode) {
                  if ((sizeField.height % 4) == 3)
                     result.height -= (int)u;
               } else {
                  if ((sizeField.height & 1) == 1)
                     result.height -= (int)u;
               }
            }
            break;
         }

         if (sizeField.width == 1)
            if ((sizeField.height % 4) == 3)
               result.height -= (int)R;
         if (sizeField.height == 1)
            if ((sizeField.width % 3) == 1)
               result.width -= (int)b;

         return result;
      }

      public override int getNeighborNumber(bool max) {
         switch (Mode) {
         case ComplexityMode.eUnrealMode : return 21;
         case ComplexityMode.eMeanMode   : return max ? 7 : 3;
         case ComplexityMode.eOptimalMode: return max ? 7 : 6;
         case ComplexityMode.eSimpeMode  : return 3;
         default: throw new InvalidOperationException("Unknown Mode==" + Mode);
         }
      }
      public override int getNeighborNumber(int direction) {
         if (Mode == ComplexityMode.eMeanMode) {
            switch (direction) {
            case 2: case 11:                                 return 7;
            case 1: case 5: case 8: case 10:                 return 5;
            case 0: case 3: case 4: case 6 : case 7: case 9: return 3;
            default: throw new InvalidOperationException("Unknown direction==" + direction);
            }
         }
         if (Mode == ComplexityMode.eOptimalMode) {
            switch(direction) {
            case 4: case 5: case 9: case 10:  return 6;
            case 0: case 1: case 2: case 3:
            case 6: case 7: case 8: case 11:  return 7;
            default: throw new InvalidOperationException("Unknown direction==" + direction);
            }
         }
         return getNeighborNumber(true /* || false */); // no matter
      }

      public override int getVertexNumber(int direction) {
         switch (Mode) {
         case ComplexityMode.eUnrealMode: return 3;
         case ComplexityMode.eMeanMode:
            switch (direction) {
            case 0: case 3: case 4: case 6: case 7: case 9: return 5;
            case 1: case 5: case 8: case 10:                return 4;
            case 2: case 11:                                return 3;
            default: throw new InvalidOperationException("Unknown direction==" + direction);
            }
         case ComplexityMode.eOptimalMode: return 4;
         case ComplexityMode.eSimpeMode  : return 5;
         default: throw new InvalidOperationException("Unknown Mode==" + Mode);
         }
      }

      public override double getVertexIntersection() {
         switch (Mode) {
         case ComplexityMode.eUnrealMode : return 9.0; // (12+12+3)/3.
         case ComplexityMode.eSimpeMode  : return 2.2; // (2+2+2+2+3)/5.
         case ComplexityMode.eOptimalMode: return 3.25; // (6+3+2+2)/4.
         case ComplexityMode.eMeanMode   : return 2.62777777778;
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
         default: throw new InvalidOperationException("Unknown Mode==" + Mode);
         }
      }

      public override Size GetDirectionSizeField() { return new Size(3, 4); }
      public override double A => Math.Sqrt(Area*SQRT48);
      public double B => A / 2;
      public double ROut => A / SQRT3;
      public double RIn => ROut / 2;
      //private double __snip  = 2.3456789 + new Random(Guid.NewGuid().GetHashCode()).Next(15);
      public double CalcSnip() { return A / (/*12*/6.789012345 /*__snip*/); }
      public override double GetSq(int borderWidth) {
         double w = borderWidth/2.0;
         return (A-w*2/TAN15)/(SQRT3+3);
      }
   }

   public Triangle4(AttrTriangle4 attr, Coord coord)
      : base(attr, coord,
            (coord.y&3)*3+(coord.x%3) // 0..11
         )
   {}

   private new AttrTriangle4 Attr {
      get { return (AttrTriangle4) base.Attr; }
   }

   protected override Coord?[] GetCoordsNeighbor() {
      var neighborCoord = new Coord?[Attr.getNeighborNumber(true)];

      // определяю координаты соседей
      switch (AttrTriangle4.Mode) {
      case ComplexityMode.eUnrealMode:
         #region
         switch (direction) {
         case 0:
            neighborCoord[ 0] = new Coord(coord.x-1, coord.y-2);
            neighborCoord[ 1] = new Coord(coord.x+1, coord.y-2);
            neighborCoord[ 2] = new Coord(coord.x-1, coord.y-1);
            neighborCoord[ 3] = new Coord(coord.x  , coord.y-1);
            neighborCoord[ 4] = new Coord(coord.x+1, coord.y-1);
            neighborCoord[ 5] = new Coord(coord.x+2, coord.y-1);
            neighborCoord[ 6] = new Coord(coord.x-2, coord.y  );
            neighborCoord[ 7] = new Coord(coord.x-1, coord.y  );
            neighborCoord[ 8] = new Coord(coord.x+1, coord.y  );
            neighborCoord[ 9] = new Coord(coord.x+2, coord.y  );
            neighborCoord[10] = new Coord(coord.x-3, coord.y+1);
            neighborCoord[11] = new Coord(coord.x-2, coord.y+1);
            neighborCoord[12] = new Coord(coord.x-1, coord.y+1);
            neighborCoord[13] = new Coord(coord.x  , coord.y+1);
            neighborCoord[14] = new Coord(coord.x+1, coord.y+1);
            neighborCoord[15] = new Coord(coord.x-3, coord.y+2);
            neighborCoord[16] = new Coord(coord.x-2, coord.y+2);
            neighborCoord[17] = new Coord(coord.x-1, coord.y+2);
            neighborCoord[18] = new Coord(coord.x  , coord.y+2);
            neighborCoord[19] = new Coord(coord.x-2, coord.y+3);
            neighborCoord[20] = new Coord(coord.x  , coord.y+3);
            break;
         case 1:
            neighborCoord[ 0] = new Coord(coord.x-2, coord.y-2);
            neighborCoord[ 1] = new Coord(coord.x  , coord.y-2);
            neighborCoord[ 2] = new Coord(coord.x-2, coord.y-1);
            neighborCoord[ 3] = new Coord(coord.x-1, coord.y-1);
            neighborCoord[ 4] = new Coord(coord.x  , coord.y-1);
            neighborCoord[ 5] = new Coord(coord.x+1, coord.y-1);
            neighborCoord[ 6] = new Coord(coord.x-2, coord.y  );
            neighborCoord[ 7] = new Coord(coord.x-1, coord.y  );
            neighborCoord[ 8] = new Coord(coord.x+1, coord.y  );
            neighborCoord[ 9] = new Coord(coord.x+2, coord.y  );
            neighborCoord[10] = new Coord(coord.x-2, coord.y+1);
            neighborCoord[11] = new Coord(coord.x-1, coord.y+1);
            neighborCoord[12] = new Coord(coord.x  , coord.y+1);
            neighborCoord[13] = new Coord(coord.x+1, coord.y+1);
            neighborCoord[14] = new Coord(coord.x+2, coord.y+1);
            neighborCoord[15] = new Coord(coord.x-1, coord.y+2);
            neighborCoord[16] = new Coord(coord.x  , coord.y+2);
            neighborCoord[17] = new Coord(coord.x+1, coord.y+2);
            neighborCoord[18] = new Coord(coord.x+2, coord.y+2);
            neighborCoord[19] = new Coord(coord.x  , coord.y+3);
            neighborCoord[20] = new Coord(coord.x+2, coord.y+3);
            break;
         case 2:
            neighborCoord[ 0] = new Coord(coord.x-3, coord.y-2);
            neighborCoord[ 1] = new Coord(coord.x-1, coord.y-2);
            neighborCoord[ 2] = new Coord(coord.x  , coord.y-2);
            neighborCoord[ 3] = new Coord(coord.x+2, coord.y-2);
            neighborCoord[ 4] = new Coord(coord.x-3, coord.y-1);
            neighborCoord[ 5] = new Coord(coord.x-2, coord.y-1);
            neighborCoord[ 6] = new Coord(coord.x-1, coord.y-1);
            neighborCoord[ 7] = new Coord(coord.x  , coord.y-1);
            neighborCoord[ 8] = new Coord(coord.x+1, coord.y-1);
            neighborCoord[ 9] = new Coord(coord.x+2, coord.y-1);
            neighborCoord[10] = new Coord(coord.x+3, coord.y-1);
            neighborCoord[11] = new Coord(coord.x-3, coord.y  );
            neighborCoord[12] = new Coord(coord.x-2, coord.y  );
            neighborCoord[13] = new Coord(coord.x-1, coord.y  );
            neighborCoord[14] = new Coord(coord.x+1, coord.y  );
            neighborCoord[15] = new Coord(coord.x+2, coord.y  );
            neighborCoord[16] = new Coord(coord.x+3, coord.y  );
            neighborCoord[17] = new Coord(coord.x-3, coord.y+1);
            neighborCoord[18] = new Coord(coord.x-1, coord.y+1);
            neighborCoord[19] = new Coord(coord.x  , coord.y+1);
            neighborCoord[20] = new Coord(coord.x+2, coord.y+1);
            break;
         case 3:
            neighborCoord[ 0] = new Coord(coord.x-2, coord.y-1);
            neighborCoord[ 1] = new Coord(coord.x  , coord.y-1);
            neighborCoord[ 2] = new Coord(coord.x+1, coord.y-1);
            neighborCoord[ 3] = new Coord(coord.x+3, coord.y-1);
            neighborCoord[ 4] = new Coord(coord.x-3, coord.y  );
            neighborCoord[ 5] = new Coord(coord.x-2, coord.y  );
            neighborCoord[ 6] = new Coord(coord.x-1, coord.y  );
            neighborCoord[ 7] = new Coord(coord.x+1, coord.y  );
            neighborCoord[ 8] = new Coord(coord.x+2, coord.y  );
            neighborCoord[ 9] = new Coord(coord.x+3, coord.y  );
            neighborCoord[10] = new Coord(coord.x-3, coord.y+1);
            neighborCoord[11] = new Coord(coord.x-2, coord.y+1);
            neighborCoord[12] = new Coord(coord.x-1, coord.y+1);
            neighborCoord[13] = new Coord(coord.x  , coord.y+1);
            neighborCoord[14] = new Coord(coord.x+1, coord.y+1);
            neighborCoord[15] = new Coord(coord.x+2, coord.y+1);
            neighborCoord[16] = new Coord(coord.x+3, coord.y+1);
            neighborCoord[17] = new Coord(coord.x-2, coord.y+2);
            neighborCoord[18] = new Coord(coord.x  , coord.y+2);
            neighborCoord[19] = new Coord(coord.x+1, coord.y+2);
            neighborCoord[20] = new Coord(coord.x+3, coord.y+2);
            break;
         case 4:
            neighborCoord[ 0] = new Coord(coord.x-2, coord.y-3);
            neighborCoord[ 1] = new Coord(coord.x  , coord.y-3);
            neighborCoord[ 2] = new Coord(coord.x-2, coord.y-2);
            neighborCoord[ 3] = new Coord(coord.x-1, coord.y-2);
            neighborCoord[ 4] = new Coord(coord.x  , coord.y-2);
            neighborCoord[ 5] = new Coord(coord.x+1, coord.y-2);
            neighborCoord[ 6] = new Coord(coord.x-2, coord.y-1);
            neighborCoord[ 7] = new Coord(coord.x-1, coord.y-1);
            neighborCoord[ 8] = new Coord(coord.x  , coord.y-1);
            neighborCoord[ 9] = new Coord(coord.x+1, coord.y-1);
            neighborCoord[10] = new Coord(coord.x+2, coord.y-1);
            neighborCoord[11] = new Coord(coord.x-2, coord.y  );
            neighborCoord[12] = new Coord(coord.x-1, coord.y  );
            neighborCoord[13] = new Coord(coord.x+1, coord.y  );
            neighborCoord[14] = new Coord(coord.x+2, coord.y  );
            neighborCoord[15] = new Coord(coord.x-1, coord.y+1);
            neighborCoord[16] = new Coord(coord.x  , coord.y+1);
            neighborCoord[17] = new Coord(coord.x+1, coord.y+1);
            neighborCoord[18] = new Coord(coord.x+2, coord.y+1);
            neighborCoord[19] = new Coord(coord.x  , coord.y+2);
            neighborCoord[20] = new Coord(coord.x+2, coord.y+2);
            break;
         case 5:
            neighborCoord[ 0] = new Coord(coord.x  , coord.y-3);
            neighborCoord[ 1] = new Coord(coord.x+2, coord.y-3);
            neighborCoord[ 2] = new Coord(coord.x  , coord.y-2);
            neighborCoord[ 3] = new Coord(coord.x+1, coord.y-2);
            neighborCoord[ 4] = new Coord(coord.x+2, coord.y-2);
            neighborCoord[ 5] = new Coord(coord.x+3, coord.y-2);
            neighborCoord[ 6] = new Coord(coord.x-1, coord.y-1);
            neighborCoord[ 7] = new Coord(coord.x  , coord.y-1);
            neighborCoord[ 8] = new Coord(coord.x+1, coord.y-1);
            neighborCoord[ 9] = new Coord(coord.x+2, coord.y-1);
            neighborCoord[10] = new Coord(coord.x+3, coord.y-1);
            neighborCoord[11] = new Coord(coord.x-2, coord.y  );
            neighborCoord[12] = new Coord(coord.x-1, coord.y  );
            neighborCoord[13] = new Coord(coord.x+1, coord.y  );
            neighborCoord[14] = new Coord(coord.x+2, coord.y  );
            neighborCoord[15] = new Coord(coord.x-2, coord.y+1);
            neighborCoord[16] = new Coord(coord.x-1, coord.y+1);
            neighborCoord[17] = new Coord(coord.x  , coord.y+1);
            neighborCoord[18] = new Coord(coord.x+1, coord.y+1);
            neighborCoord[19] = new Coord(coord.x-1, coord.y+2);
            neighborCoord[20] = new Coord(coord.x+1, coord.y+2);
            break;
         case 6:
            neighborCoord[ 0] = new Coord(coord.x-2, coord.y-2);
            neighborCoord[ 1] = new Coord(coord.x  , coord.y-2);
            neighborCoord[ 2] = new Coord(coord.x+1, coord.y-2);
            neighborCoord[ 3] = new Coord(coord.x+3, coord.y-2);
            neighborCoord[ 4] = new Coord(coord.x-3, coord.y-1);
            neighborCoord[ 5] = new Coord(coord.x-2, coord.y-1);
            neighborCoord[ 6] = new Coord(coord.x-1, coord.y-1);
            neighborCoord[ 7] = new Coord(coord.x  , coord.y-1);
            neighborCoord[ 8] = new Coord(coord.x+1, coord.y-1);
            neighborCoord[ 9] = new Coord(coord.x+2, coord.y-1);
            neighborCoord[10] = new Coord(coord.x+3, coord.y-1);
            neighborCoord[11] = new Coord(coord.x-3, coord.y  );
            neighborCoord[12] = new Coord(coord.x-2, coord.y  );
            neighborCoord[13] = new Coord(coord.x-1, coord.y  );
            neighborCoord[14] = new Coord(coord.x+1, coord.y  );
            neighborCoord[15] = new Coord(coord.x+2, coord.y  );
            neighborCoord[16] = new Coord(coord.x+3, coord.y  );
            neighborCoord[17] = new Coord(coord.x-2, coord.y+1);
            neighborCoord[18] = new Coord(coord.x  , coord.y+1);
            neighborCoord[19] = new Coord(coord.x+1, coord.y+1);
            neighborCoord[20] = new Coord(coord.x+3, coord.y+1);
            break;
         case 7:
            neighborCoord[ 0] = new Coord(coord.x  , coord.y-2);
            neighborCoord[ 1] = new Coord(coord.x+2, coord.y-2);
            neighborCoord[ 2] = new Coord(coord.x-1, coord.y-1);
            neighborCoord[ 3] = new Coord(coord.x  , coord.y-1);
            neighborCoord[ 4] = new Coord(coord.x+1, coord.y-1);
            neighborCoord[ 5] = new Coord(coord.x+2, coord.y-1);
            neighborCoord[ 6] = new Coord(coord.x-2, coord.y  );
            neighborCoord[ 7] = new Coord(coord.x-1, coord.y  );
            neighborCoord[ 8] = new Coord(coord.x+1, coord.y  );
            neighborCoord[ 9] = new Coord(coord.x+2, coord.y  );
            neighborCoord[10] = new Coord(coord.x-2, coord.y+1);
            neighborCoord[11] = new Coord(coord.x-1, coord.y+1);
            neighborCoord[12] = new Coord(coord.x  , coord.y+1);
            neighborCoord[13] = new Coord(coord.x+1, coord.y+1);
            neighborCoord[14] = new Coord(coord.x+2, coord.y+1);
            neighborCoord[15] = new Coord(coord.x-2, coord.y+2);
            neighborCoord[16] = new Coord(coord.x-1, coord.y+2);
            neighborCoord[17] = new Coord(coord.x  , coord.y+2);
            neighborCoord[18] = new Coord(coord.x+1, coord.y+2);
            neighborCoord[19] = new Coord(coord.x-2, coord.y+3);
            neighborCoord[20] = new Coord(coord.x  , coord.y+3);
            break;
         case 8:
            neighborCoord[ 0] = new Coord(coord.x-1, coord.y-2);
            neighborCoord[ 1] = new Coord(coord.x+1, coord.y-2);
            neighborCoord[ 2] = new Coord(coord.x-2, coord.y-1);
            neighborCoord[ 3] = new Coord(coord.x-1, coord.y-1);
            neighborCoord[ 4] = new Coord(coord.x  , coord.y-1);
            neighborCoord[ 5] = new Coord(coord.x+1, coord.y-1);
            neighborCoord[ 6] = new Coord(coord.x-2, coord.y  );
            neighborCoord[ 7] = new Coord(coord.x-1, coord.y  );
            neighborCoord[ 8] = new Coord(coord.x+1, coord.y  );
            neighborCoord[ 9] = new Coord(coord.x+2, coord.y  );
            neighborCoord[10] = new Coord(coord.x-1, coord.y+1);
            neighborCoord[11] = new Coord(coord.x  , coord.y+1);
            neighborCoord[12] = new Coord(coord.x+1, coord.y+1);
            neighborCoord[13] = new Coord(coord.x+2, coord.y+1);
            neighborCoord[14] = new Coord(coord.x+3, coord.y+1);
            neighborCoord[15] = new Coord(coord.x  , coord.y+2);
            neighborCoord[16] = new Coord(coord.x+1, coord.y+2);
            neighborCoord[17] = new Coord(coord.x+2, coord.y+2);
            neighborCoord[18] = new Coord(coord.x+3, coord.y+2);
            neighborCoord[19] = new Coord(coord.x  , coord.y+3);
            neighborCoord[20] = new Coord(coord.x+2, coord.y+3);
            break;
         case 9:
            neighborCoord[ 0] = new Coord(coord.x-2, coord.y-3);
            neighborCoord[ 1] = new Coord(coord.x  , coord.y-3);
            neighborCoord[ 2] = new Coord(coord.x-3, coord.y-2);
            neighborCoord[ 3] = new Coord(coord.x-2, coord.y-2);
            neighborCoord[ 4] = new Coord(coord.x-1, coord.y-2);
            neighborCoord[ 5] = new Coord(coord.x  , coord.y-2);
            neighborCoord[ 6] = new Coord(coord.x-3, coord.y-1);
            neighborCoord[ 7] = new Coord(coord.x-2, coord.y-1);
            neighborCoord[ 8] = new Coord(coord.x-1, coord.y-1);
            neighborCoord[ 9] = new Coord(coord.x  , coord.y-1);
            neighborCoord[10] = new Coord(coord.x+1, coord.y-1);
            neighborCoord[11] = new Coord(coord.x-2, coord.y  );
            neighborCoord[12] = new Coord(coord.x-1, coord.y  );
            neighborCoord[13] = new Coord(coord.x+1, coord.y  );
            neighborCoord[14] = new Coord(coord.x+2, coord.y  );
            neighborCoord[15] = new Coord(coord.x-1, coord.y+1);
            neighborCoord[16] = new Coord(coord.x  , coord.y+1);
            neighborCoord[17] = new Coord(coord.x+1, coord.y+1);
            neighborCoord[18] = new Coord(coord.x+2, coord.y+1);
            neighborCoord[19] = new Coord(coord.x-1, coord.y+2);
            neighborCoord[20] = new Coord(coord.x+1, coord.y+2);
            break;
         case 10:
            neighborCoord[ 0] = new Coord(coord.x  , coord.y-3);
            neighborCoord[ 1] = new Coord(coord.x+2, coord.y-3);
            neighborCoord[ 2] = new Coord(coord.x-1, coord.y-2);
            neighborCoord[ 3] = new Coord(coord.x  , coord.y-2);
            neighborCoord[ 4] = new Coord(coord.x+1, coord.y-2);
            neighborCoord[ 5] = new Coord(coord.x+2, coord.y-2);
            neighborCoord[ 6] = new Coord(coord.x-2, coord.y-1);
            neighborCoord[ 7] = new Coord(coord.x-1, coord.y-1);
            neighborCoord[ 8] = new Coord(coord.x  , coord.y-1);
            neighborCoord[ 9] = new Coord(coord.x+1, coord.y-1);
            neighborCoord[10] = new Coord(coord.x+2, coord.y-1);
            neighborCoord[11] = new Coord(coord.x-2, coord.y  );
            neighborCoord[12] = new Coord(coord.x-1, coord.y  );
            neighborCoord[13] = new Coord(coord.x+1, coord.y  );
            neighborCoord[14] = new Coord(coord.x+2, coord.y  );
            neighborCoord[15] = new Coord(coord.x-2, coord.y+1);
            neighborCoord[16] = new Coord(coord.x-1, coord.y+1);
            neighborCoord[17] = new Coord(coord.x  , coord.y+1);
            neighborCoord[18] = new Coord(coord.x+1, coord.y+1);
            neighborCoord[19] = new Coord(coord.x-2, coord.y+2);
            neighborCoord[20] = new Coord(coord.x  , coord.y+2);
            break;
         case 11:
            neighborCoord[ 0] = new Coord(coord.x-3, coord.y-1);
            neighborCoord[ 1] = new Coord(coord.x-1, coord.y-1);
            neighborCoord[ 2] = new Coord(coord.x  , coord.y-1);
            neighborCoord[ 3] = new Coord(coord.x+2, coord.y-1);
            neighborCoord[ 4] = new Coord(coord.x-3, coord.y  );
            neighborCoord[ 5] = new Coord(coord.x-2, coord.y  );
            neighborCoord[ 6] = new Coord(coord.x-1, coord.y  );
            neighborCoord[ 7] = new Coord(coord.x+1, coord.y  );
            neighborCoord[ 8] = new Coord(coord.x+2, coord.y  );
            neighborCoord[ 9] = new Coord(coord.x+3, coord.y  );
            neighborCoord[10] = new Coord(coord.x-3, coord.y+1);
            neighborCoord[11] = new Coord(coord.x-2, coord.y+1);
            neighborCoord[12] = new Coord(coord.x-1, coord.y+1);
            neighborCoord[13] = new Coord(coord.x  , coord.y+1);
            neighborCoord[14] = new Coord(coord.x+1, coord.y+1);
            neighborCoord[15] = new Coord(coord.x+2, coord.y+1);
            neighborCoord[16] = new Coord(coord.x+3, coord.y+1);
            neighborCoord[17] = new Coord(coord.x-3, coord.y+2);
            neighborCoord[18] = new Coord(coord.x-1, coord.y+2);
            neighborCoord[19] = new Coord(coord.x  , coord.y+2);
            neighborCoord[20] = new Coord(coord.x+2, coord.y+2);
            break;
         }
         #endregion
         break;
      case ComplexityMode.eMeanMode:
         #region
         switch (direction) {
         case 0:
            neighborCoord[0] = new Coord(coord.x-1, coord.y+1);
            neighborCoord[1] = new Coord(coord.x  , coord.y+1);
            neighborCoord[2] = new Coord(coord.x+1, coord.y+1);
            break;
         case 1:
            neighborCoord[0] = new Coord(coord.x+1, coord.y  );
            neighborCoord[1] = new Coord(coord.x  , coord.y+1);
            neighborCoord[2] = new Coord(coord.x+1, coord.y+1);
            neighborCoord[3] = new Coord(coord.x+1, coord.y+2);
            neighborCoord[4] = new Coord(coord.x  , coord.y+3);
            break;
         case 2:
            neighborCoord[0] = new Coord(coord.x-3, coord.y-1);
            neighborCoord[1] = new Coord(coord.x  , coord.y-1);
            neighborCoord[2] = new Coord(coord.x+3, coord.y-1);
            neighborCoord[3] = new Coord(coord.x-3, coord.y  );
            neighborCoord[4] = new Coord(coord.x-1, coord.y  );
            neighborCoord[5] = new Coord(coord.x+3, coord.y  );
            neighborCoord[6] = new Coord(coord.x  , coord.y+1);
            break;
         case 3:
            neighborCoord[0] = new Coord(coord.x  , coord.y-1);
            neighborCoord[1] = new Coord(coord.x+1, coord.y  );
            neighborCoord[2] = new Coord(coord.x  , coord.y+1);
            break;
         case 4:
            neighborCoord[0] = new Coord(coord.x-1, coord.y-1);
            neighborCoord[1] = new Coord(coord.x  , coord.y-1);
            neighborCoord[2] = new Coord(coord.x-1, coord.y  );
            break;
         case 5:
            neighborCoord[0] = new Coord(coord.x-1, coord.y-1);
            neighborCoord[1] = new Coord(coord.x  , coord.y-1);
            neighborCoord[2] = new Coord(coord.x+1, coord.y-1);
            neighborCoord[3] = new Coord(coord.x  , coord.y+1);
            neighborCoord[4] = new Coord(coord.x-1, coord.y+2);
            break;
         case 6:
            neighborCoord[0] = new Coord(coord.x  , coord.y-1);
            neighborCoord[1] = new Coord(coord.x+1, coord.y  );
            neighborCoord[2] = new Coord(coord.x  , coord.y+1);
            break;
         case 7:
            neighborCoord[0] = new Coord(coord.x-1, coord.y  );
            neighborCoord[1] = new Coord(coord.x-1, coord.y+1);
            neighborCoord[2] = new Coord(coord.x  , coord.y+1);
            break;
         case 8:
            neighborCoord[0] = new Coord(coord.x-1, coord.y-2);
            neighborCoord[1] = new Coord(coord.x  , coord.y-1);
            neighborCoord[2] = new Coord(coord.x-1, coord.y+1);
            neighborCoord[3] = new Coord(coord.x  , coord.y+1);
            neighborCoord[4] = new Coord(coord.x+1, coord.y+1);
            break;
         case 9:
            neighborCoord[0] = new Coord(coord.x-1, coord.y-1);
            neighborCoord[1] = new Coord(coord.x  , coord.y-1);
            neighborCoord[2] = new Coord(coord.x+1, coord.y-1);
            break;
         case 10:
            neighborCoord[0] = new Coord(coord.x  , coord.y-3);
            neighborCoord[1] = new Coord(coord.x+1, coord.y-2);
            neighborCoord[2] = new Coord(coord.x  , coord.y-1);
            neighborCoord[3] = new Coord(coord.x+1, coord.y-1);
            neighborCoord[4] = new Coord(coord.x+1, coord.y  );
            break;
         case 11:
            neighborCoord[0] = new Coord(coord.x  , coord.y-1);
            neighborCoord[1] = new Coord(coord.x-3, coord.y  );
            neighborCoord[2] = new Coord(coord.x-1, coord.y  );
            neighborCoord[3] = new Coord(coord.x+3, coord.y  );
            neighborCoord[4] = new Coord(coord.x-3, coord.y+1);
            neighborCoord[5] = new Coord(coord.x  , coord.y+1);
            neighborCoord[6] = new Coord(coord.x+3, coord.y+1);
            break;
         }
         #endregion
         break;
      case ComplexityMode.eOptimalMode:
         #region
         switch (direction) {
         case 0:
               neighborCoord[0] = new Coord(coord.x+1, coord.y-2);
               neighborCoord[1] = new Coord(coord.x+1, coord.y-1);
               neighborCoord[2] = new Coord(coord.x+2, coord.y-1);
               neighborCoord[3] = new Coord(coord.x-1, coord.y  );
               neighborCoord[4] = new Coord(coord.x-1, coord.y+1);
               neighborCoord[5] = new Coord(coord.x  , coord.y+1);
               neighborCoord[6] = new Coord(coord.x+1, coord.y+1);
               break;
         case 1:
               neighborCoord[0] = new Coord(coord.x+1, coord.y  );
               neighborCoord[1] = new Coord(coord.x-1, coord.y+1);
               neighborCoord[2] = new Coord(coord.x  , coord.y+1);
               neighborCoord[3] = new Coord(coord.x+1, coord.y+1);
               neighborCoord[4] = new Coord(coord.x+1, coord.y+2);
               neighborCoord[5] = new Coord(coord.x+2, coord.y+2);
               neighborCoord[6] = new Coord(coord.x+2, coord.y+3);
               break;
         case 2:
               neighborCoord[0] = new Coord(coord.x+2, coord.y-2);
               neighborCoord[1] = new Coord(coord.x  , coord.y-1);
               neighborCoord[2] = new Coord(coord.x+2, coord.y-1);
               neighborCoord[3] = new Coord(coord.x+3, coord.y-1);
               neighborCoord[4] = new Coord(coord.x-1, coord.y  );
               neighborCoord[5] = new Coord(coord.x+1, coord.y  );
               neighborCoord[6] = new Coord(coord.x  , coord.y+1);
               break;
         case 3:
               neighborCoord[0] = new Coord(coord.x  , coord.y-1);
               neighborCoord[1] = new Coord(coord.x+1, coord.y-1);
               neighborCoord[2] = new Coord(coord.x+1, coord.y  );
               neighborCoord[3] = new Coord(coord.x  , coord.y+1);
               neighborCoord[4] = new Coord(coord.x+2, coord.y+1);
               neighborCoord[5] = new Coord(coord.x+3, coord.y+1);
               neighborCoord[6] = new Coord(coord.x+3, coord.y+2);
               break;
         case 4:
               neighborCoord[0] = new Coord(coord.x-1, coord.y-1);
               neighborCoord[1] = new Coord(coord.x  , coord.y-1);
               neighborCoord[2] = new Coord(coord.x-1, coord.y  );
               neighborCoord[3] = new Coord(coord.x+1, coord.y+1);
               neighborCoord[4] = new Coord(coord.x+2, coord.y+1);
               neighborCoord[5] = new Coord(coord.x+2, coord.y+2);
               break;
         case 5:
               neighborCoord[0] = new Coord(coord.x+2, coord.y-3);
               neighborCoord[1] = new Coord(coord.x+2, coord.y-2);
               neighborCoord[2] = new Coord(coord.x+3, coord.y-2);
               neighborCoord[3] = new Coord(coord.x-1, coord.y-1);
               neighborCoord[4] = new Coord(coord.x  , coord.y-1);
               neighborCoord[5] = new Coord(coord.x+1, coord.y-1);
               break;
         case 6:
               neighborCoord[0] = new Coord(coord.x-2, coord.y-2);
               neighborCoord[1] = new Coord(coord.x-3, coord.y-1);
               neighborCoord[2] = new Coord(coord.x-2, coord.y-1);
               neighborCoord[3] = new Coord(coord.x  , coord.y-1);
               neighborCoord[4] = new Coord(coord.x-1, coord.y  );
               neighborCoord[5] = new Coord(coord.x+1, coord.y  );
               neighborCoord[6] = new Coord(coord.x  , coord.y+1);
               break;
         case 7:
               neighborCoord[0] = new Coord(coord.x-1, coord.y  );
               neighborCoord[1] = new Coord(coord.x-1, coord.y+1);
               neighborCoord[2] = new Coord(coord.x  , coord.y+1);
               neighborCoord[3] = new Coord(coord.x+1, coord.y+1);
               neighborCoord[4] = new Coord(coord.x-2, coord.y+2);
               neighborCoord[5] = new Coord(coord.x-1, coord.y+2);
               neighborCoord[6] = new Coord(coord.x-2, coord.y+3);
               break;
         case 8:
               neighborCoord[0] = new Coord(coord.x-1, coord.y-2);
               neighborCoord[1] = new Coord(coord.x-2, coord.y-1);
               neighborCoord[2] = new Coord(coord.x-1, coord.y-1);
               neighborCoord[3] = new Coord(coord.x+1, coord.y  );
               neighborCoord[4] = new Coord(coord.x-1, coord.y+1);
               neighborCoord[5] = new Coord(coord.x  , coord.y+1);
               neighborCoord[6] = new Coord(coord.x+1, coord.y+1);
               break;
         case 9:
               neighborCoord[0] = new Coord(coord.x-2, coord.y-3);
               neighborCoord[1] = new Coord(coord.x-3, coord.y-2);
               neighborCoord[2] = new Coord(coord.x-2, coord.y-2);
               neighborCoord[3] = new Coord(coord.x-1, coord.y-1);
               neighborCoord[4] = new Coord(coord.x  , coord.y-1);
               neighborCoord[5] = new Coord(coord.x+1, coord.y-1);
               break;
         case 10:
               neighborCoord[0] = new Coord(coord.x  , coord.y-1);
               neighborCoord[1] = new Coord(coord.x+1, coord.y-1);
               neighborCoord[2] = new Coord(coord.x+1, coord.y  );
               neighborCoord[3] = new Coord(coord.x-2, coord.y+1);
               neighborCoord[4] = new Coord(coord.x-1, coord.y+1);
               neighborCoord[5] = new Coord(coord.x-2, coord.y+2);
               break;
         case 11:
               neighborCoord[0] = new Coord(coord.x-1, coord.y-1);
               neighborCoord[1] = new Coord(coord.x  , coord.y-1);
               neighborCoord[2] = new Coord(coord.x-1, coord.y  );
               neighborCoord[3] = new Coord(coord.x-3, coord.y+1);
               neighborCoord[4] = new Coord(coord.x-2, coord.y+1);
               neighborCoord[5] = new Coord(coord.x  , coord.y+1);
               neighborCoord[6] = new Coord(coord.x-3, coord.y+2);
               break;
         }
         #endregion
         break;
      case ComplexityMode.eSimpeMode:
         #region
         switch (direction) {
         case 0:
            neighborCoord[0] = new Coord(coord.x-1, coord.y+1);
            neighborCoord[1] = new Coord(coord.x  , coord.y+1);
            neighborCoord[2] = new Coord(coord.x+1, coord.y+1);
            break;
         case 1:
            neighborCoord[0] = new Coord(coord.x+1, coord.y  );
            neighborCoord[1] = new Coord(coord.x  , coord.y+1);
            neighborCoord[2] = new Coord(coord.x+1, coord.y+1);
            break;
         case 2:
            neighborCoord[0] = new Coord(coord.x  , coord.y-1);
            neighborCoord[1] = new Coord(coord.x-1, coord.y  );
            neighborCoord[2] = new Coord(coord.x  , coord.y+1);
            break;
         case 3:
            neighborCoord[0] = new Coord(coord.x  , coord.y-1);
            neighborCoord[1] = new Coord(coord.x+1, coord.y  );
            neighborCoord[2] = new Coord(coord.x  , coord.y+1);
            break;
         case 4:
            neighborCoord[0] = new Coord(coord.x-1, coord.y-1);
            neighborCoord[1] = new Coord(coord.x  , coord.y-1);
            neighborCoord[2] = new Coord(coord.x-1, coord.y  );
            break;
         case 5:
            neighborCoord[0] = new Coord(coord.x-1, coord.y-1);
            neighborCoord[1] = new Coord(coord.x  , coord.y-1);
            neighborCoord[2] = new Coord(coord.x+1, coord.y-1);
            break;
         case 6:
            neighborCoord[0] = new Coord(coord.x  , coord.y-1);
            neighborCoord[1] = new Coord(coord.x+1, coord.y  );
            neighborCoord[2] = new Coord(coord.x  , coord.y+1);
            break;
         case 7:
            neighborCoord[0] = new Coord(coord.x-1, coord.y  );
            neighborCoord[1] = new Coord(coord.x-1, coord.y+1);
            neighborCoord[2] = new Coord(coord.x  , coord.y+1);
            break;
         case 8:
            neighborCoord[0] = new Coord(coord.x-1, coord.y+1);
            neighborCoord[1] = new Coord(coord.x  , coord.y+1);
            neighborCoord[2] = new Coord(coord.x+1, coord.y+1);
            break;
         case 9:
            neighborCoord[0] = new Coord(coord.x-1, coord.y-1);
            neighborCoord[1] = new Coord(coord.x  , coord.y-1);
            neighborCoord[2] = new Coord(coord.x+1, coord.y-1);
            break;
         case 10:
            neighborCoord[0] = new Coord(coord.x  , coord.y-1);
            neighborCoord[1] = new Coord(coord.x+1, coord.y-1);
            neighborCoord[2] = new Coord(coord.x+1, coord.y  );
            break;
         case 11:
            neighborCoord[0] = new Coord(coord.x  , coord.y-1);
            neighborCoord[1] = new Coord(coord.x-1, coord.y  );
            neighborCoord[2] = new Coord(coord.x  , coord.y+1);
            break;
         }
         #endregion
         break;
      }
      return neighborCoord;
   }

   protected override void CalcRegion() {
      AttrTriangle4 attr = Attr;
      double a = attr.A;
      double b = attr.B;
      double R = attr.ROut;
      double r = attr.RIn;
      double s = (AttrTriangle4.Mode != ComplexityMode.eUnrealMode) ? attr.CalcSnip() : 0;
      double c = (AttrTriangle4.Mode != ComplexityMode.eUnrealMode) ? s/2*SQRT3 : 0; // s * cos30
      double u = (AttrTriangle4.Mode != ComplexityMode.eUnrealMode) ? s/2 : 0; // s * cos60

      // определение координат точек фигуры
      double oX =  (coord.x/3)*a + b;      // offset X
      double oY = ((coord.y/4)*2+1)*(R+r); // offset Y
      switch (AttrTriangle4.Mode) {
      case ComplexityMode.eUnrealMode:
      case ComplexityMode.eOptimalMode:
         break;
      case ComplexityMode.eMeanMode:
      case ComplexityMode.eSimpeMode:
         oX -= u;
         break;
      }

      switch (AttrTriangle4.Mode) {
      case ComplexityMode.eUnrealMode:
         #region
         switch (direction) {
         case 0:
            region.setPoint(0, (int)(oX    ), (int)(oY - R-r));
            region.setPoint(1, (int)(oX    ), (int)(oY - r  ));
            region.setPoint(2, (int)(oX - b), (int)(oY      ));
            break;
         case 1:
            region.setPoint(0, (int)(oX + b), (int)(oY - R  ));
            region.setPoint(1, (int)(oX + b), (int)(oY      ));
            region.setPoint(2, (int)(oX    ), (int)(oY - R-r));
            break;
         case 2:
            region.setPoint(0, (int)(oX + a), (int)(oY - R-r));
            region.setPoint(1, (int)(oX + b), (int)(oY - R  ));
            region.setPoint(2, (int)(oX    ), (int)(oY - R-r));
            break;
         case 3:
            region.setPoint(0, (int)(oX + b), (int)(oY      ));
            region.setPoint(1, (int)(oX - b), (int)(oY      ));
            region.setPoint(2, (int)(oX    ), (int)(oY - r  ));
            break;
         case 4:
            region.setPoint(0, (int)(oX    ), (int)(oY - R-r));
            region.setPoint(1, (int)(oX + b), (int)(oY      ));
            region.setPoint(2, (int)(oX    ), (int)(oY - r  ));
            break;
         case 5:
            region.setPoint(0, (int)(oX + a), (int)(oY - R-r));
            region.setPoint(1, (int)(oX + b), (int)(oY      ));
            region.setPoint(2, (int)(oX + b), (int)(oY - R  ));
            break;
         case 6:
            region.setPoint(0, (int)(oX + b), (int)(oY      ));
            region.setPoint(1, (int)(oX    ), (int)(oY + r  ));
            region.setPoint(2, (int)(oX - b), (int)(oY      ));
            break;
         case 7:
            region.setPoint(0, (int)(oX + b), (int)(oY      ));
            region.setPoint(1, (int)(oX    ), (int)(oY + R+r));
            region.setPoint(2, (int)(oX    ), (int)(oY + r  ));
            break;
         case 8:
            region.setPoint(0, (int)(oX + b), (int)(oY      ));
            region.setPoint(1, (int)(oX + a), (int)(oY + R+r));
            region.setPoint(2, (int)(oX + b), (int)(oY + R  ));
            break;
         case 9:
            region.setPoint(0, (int)(oX    ), (int)(oY + r  ));
            region.setPoint(1, (int)(oX    ), (int)(oY + R+r));
            region.setPoint(2, (int)(oX - b), (int)(oY      ));
            break;
         case 10:
            region.setPoint(0, (int)(oX + b), (int)(oY      ));
            region.setPoint(1, (int)(oX + b), (int)(oY + R  ));
            region.setPoint(2, (int)(oX    ), (int)(oY + R+r));
            break;
         case 11:
            region.setPoint(0, (int)(oX + a), (int)(oY + R+r));
            region.setPoint(1, (int)(oX    ), (int)(oY + R+r));
            region.setPoint(2, (int)(oX + b), (int)(oY + R  ));
            break;
         }
         #endregion
         break;
      case ComplexityMode.eMeanMode:
         #region
         switch (direction) {
         case 0:
            region.setPoint(0, (int)(oX    ), (int)(oY-R-r+s));
            region.setPoint(1, (int)(oX    ), (int)(oY - r  ));
            region.setPoint(2, (int)(oX-b+c), (int)(oY - u  ));
            region.setPoint(3, (int)(oX-b+u), (int)(oY - c  ));
            region.setPoint(4, (int)(oX - u), (int)(oY-R-r+c));
            break;
         case 1:
            region.setPoint(0, (int)(oX + b), (int)(oY - R  ));
            region.setPoint(1, (int)(oX + b), (int)(oY      ));
            region.setPoint(2, (int)(oX + u), (int)(oY-R-r+c));
            region.setPoint(3, (int)(oX + c), (int)(oY-R-r+u));
            break;
         case 2:
            region.setPoint(0, (int)(oX + a), (int)(oY - R-r));
            region.setPoint(1, (int)(oX + b), (int)(oY - R  ));
            region.setPoint(2, (int)(oX    ), (int)(oY - R-r));
            break;
         case 3:
            region.setPoint(0, (int)(oX+b-s), (int)(oY      ));
            region.setPoint(1, (int)(oX-b+s), (int)(oY      ));
            region.setPoint(2, (int)(oX-b+c), (int)(oY - u  ));
            region.setPoint(3, (int)(oX    ), (int)(oY - r  ));
            region.setPoint(4, (int)(oX+b-c), (int)(oY - u  ));
            break;
         case 4:
            region.setPoint(0, (int)(oX + u), (int)(oY-R-r+c));
            region.setPoint(1, (int)(oX+b-u), (int)(oY - c  ));
            region.setPoint(2, (int)(oX+b-c), (int)(oY - u  ));
            region.setPoint(3, (int)(oX    ), (int)(oY - r  ));
            region.setPoint(4, (int)(oX    ), (int)(oY-R-r+s));
            break;
         case 5:
            region.setPoint(0, (int)(oX+a-u), (int)(oY-R-r+c));
            region.setPoint(1, (int)(oX + b), (int)(oY      ));
            region.setPoint(2, (int)(oX + b), (int)(oY - R  ));
            region.setPoint(3, (int)(oX+a-c), (int)(oY-R-r+u));
            break;
         case 6:
            region.setPoint(0, (int)(oX+b-c), (int)(oY + u  ));
            region.setPoint(1, (int)(oX    ), (int)(oY + r  ));
            region.setPoint(2, (int)(oX-b+c), (int)(oY + u  ));
            region.setPoint(3, (int)(oX-b+s), (int)(oY      ));
            region.setPoint(4, (int)(oX+b-s), (int)(oY      ));
            break;
         case 7:
            region.setPoint(0, (int)(oX+b-u), (int)(oY + c  ));
            region.setPoint(1, (int)(oX + u), (int)(oY+R+r-c));
            region.setPoint(2, (int)(oX    ), (int)(oY+R+r-s));
            region.setPoint(3, (int)(oX    ), (int)(oY + r  ));
            region.setPoint(4, (int)(oX+b-c), (int)(oY + u  ));
            break;
         case 8:
            region.setPoint(0, (int)(oX + b), (int)(oY      ));
            region.setPoint(1, (int)(oX+a-u), (int)(oY+R+r-c));
            region.setPoint(2, (int)(oX+a-c), (int)(oY+R+r-u));
            region.setPoint(3, (int)(oX + b), (int)(oY + R  ));
            break;
         case 9:
            region.setPoint(0, (int)(oX    ), (int)(oY + r  ));
            region.setPoint(1, (int)(oX    ), (int)(oY+R+r-s));
            region.setPoint(2, (int)(oX - u), (int)(oY+R+r-c));
            region.setPoint(3, (int)(oX-b+u), (int)(oY + c  ));
            region.setPoint(4, (int)(oX-b+c), (int)(oY + u  ));
            break;
         case 10:
            region.setPoint(0, (int)(oX + b), (int)(oY      ));
            region.setPoint(1, (int)(oX + b), (int)(oY + R  ));
            region.setPoint(2, (int)(oX + c), (int)(oY+R+r-u));
            region.setPoint(3, (int)(oX + u), (int)(oY+R+r-c));
            break;
         case 11:
            region.setPoint(0, (int)(oX + a), (int)(oY + R+r));
            region.setPoint(2, (int)(oX + b), (int)(oY + R  ));
            region.setPoint(1, (int)(oX    ), (int)(oY + R+r));
            break;
         }
         #endregion
         break;
      case ComplexityMode.eOptimalMode:
         #region
         switch (direction) {
         case 0:
            region.setPoint(0, (int)(oX    ), (int)(oY - R-r));
            region.setPoint(1, (int)(oX    ), (int)(oY - r  ));
            region.setPoint(2, (int)(oX-b+c), (int)(oY - u  ));
            region.setPoint(3, (int)(oX-b+u), (int)(oY - c  ));
            break;
         case 1:
            region.setPoint(0, (int)(oX + b), (int)(oY - R  ));
            region.setPoint(1, (int)(oX + b), (int)(oY      ));
            region.setPoint(2, (int)(oX + u), (int)(oY-R-r+c));
            region.setPoint(3, (int)(oX + c), (int)(oY-R-r+u));
            break;
         case 2:
            region.setPoint(0, (int)(oX + a), (int)(oY -R -r));
            region.setPoint(1, (int)(oX + b), (int)(oY - R  ));
            region.setPoint(2, (int)(oX + c), (int)(oY-R-r+u));
            region.setPoint(3, (int)(oX + s), (int)(oY -R -r));
            break;
         case 3:
            region.setPoint(0, (int)(oX+b  ), (int)(oY      ));
            region.setPoint(1, (int)(oX-b+s), (int)(oY      ));
            region.setPoint(2, (int)(oX-b+c), (int)(oY - u  ));
            region.setPoint(3, (int)(oX    ), (int)(oY - r  ));
            break;
         case 4:
            region.setPoint(0, (int)(oX + u), (int)(oY-R-r+c));
            region.setPoint(1, (int)(oX + b), (int)(oY      ));
            region.setPoint(2, (int)(oX    ), (int)(oY - r  ));
            region.setPoint(3, (int)(oX    ), (int)(oY-R-r+s));
            break;
         case 5:
            region.setPoint(0, (int)(oX+a  ), (int)(oY - R-r));
            region.setPoint(1, (int)(oX+b+u), (int)(oY - c  ));
            region.setPoint(2, (int)(oX + b), (int)(oY - s  ));
            region.setPoint(3, (int)(oX + b), (int)(oY - R  ));
            break;
         case 6:
            region.setPoint(0, (int)(oX+b-c), (int)(oY + u  ));
            region.setPoint(1, (int)(oX    ), (int)(oY + r  ));
            region.setPoint(2, (int)(oX-b  ), (int)(oY      ));
            region.setPoint(3, (int)(oX+b-s), (int)(oY      ));
            break;
         case 7:
            region.setPoint(0, (int)(oX+b-u), (int)(oY + c  ));
            region.setPoint(1, (int)(oX    ), (int)(oY + R+r));
            region.setPoint(2, (int)(oX    ), (int)(oY + r  ));
            region.setPoint(3, (int)(oX+b-c), (int)(oY + u  ));
            break;
         case 8:
            region.setPoint(0, (int)(oX + b), (int)(oY      ));
            region.setPoint(1, (int)(oX+a-u), (int)(oY+R+r-c));
            region.setPoint(2, (int)(oX+a-c), (int)(oY+R+r-u));
            region.setPoint(3, (int)(oX + b), (int)(oY + R  ));
            break;
         case 9:
            region.setPoint(0, (int)(oX    ), (int)(oY + r  ));
            region.setPoint(1, (int)(oX    ), (int)(oY+R+r-s));
            region.setPoint(2, (int)(oX - u), (int)(oY+R+r-c));
            region.setPoint(3, (int)(oX - b), (int)(oY      ));
            break;
         case 10:
            region.setPoint(0, (int)(oX + b), (int)(oY + s  ));
            region.setPoint(1, (int)(oX + b), (int)(oY + R  ));
            region.setPoint(2, (int)(oX    ), (int)(oY+R+r  ));
            region.setPoint(3, (int)(oX+b-u), (int)(oY + c  ));
            break;
         case 11:
            region.setPoint(0, (int)(oX+a-c), (int)(oY+R+r-u));
            region.setPoint(1, (int)(oX+a-s), (int)(oY + R+r));
            region.setPoint(2, (int)(oX    ), (int)(oY + R+r));
            region.setPoint(3, (int)(oX + b), (int)(oY + R  ));
            break;
         }
         #endregion
         break;
      case ComplexityMode.eSimpeMode:
         #region
         switch (direction) {
         case 0:
            region.setPoint(0, (int)(oX    ), (int)(oY-R-r+s));
            region.setPoint(1, (int)(oX    ), (int)(oY - r  ));
            region.setPoint(2, (int)(oX-b+c), (int)(oY - u  ));
            region.setPoint(3, (int)(oX-b+u), (int)(oY - c  ));
            region.setPoint(4, (int)(oX - u), (int)(oY-R-r+c));
            break;
         case 1:
            region.setPoint(0, (int)(oX + b), (int)(oY - R  ));
            region.setPoint(1, (int)(oX + b), (int)(oY - s  ));
            region.setPoint(2, (int)(oX+b-u), (int)(oY - c  ));
            region.setPoint(3, (int)(oX + u), (int)(oY-R-r+c));
            region.setPoint(4, (int)(oX + c), (int)(oY-R-r+u));
            break;
         case 2:
            region.setPoint(0, (int)(oX+a-c), (int)(oY-R-r+u));
            region.setPoint(1, (int)(oX + b), (int)(oY - R  ));
            region.setPoint(2, (int)(oX + c), (int)(oY-R-r+u));
            region.setPoint(3, (int)(oX + s), (int)(oY -R -r));
            region.setPoint(4, (int)(oX+a-s), (int)(oY -R -r));
            break;
         case 3:
            region.setPoint(0, (int)(oX+b-s), (int)(oY      ));
            region.setPoint(1, (int)(oX-b+s), (int)(oY      ));
            region.setPoint(2, (int)(oX-b+c), (int)(oY - u  ));
            region.setPoint(3, (int)(oX    ), (int)(oY - r  ));
            region.setPoint(4, (int)(oX+b-c), (int)(oY - u  ));
            break;
         case 4:
            region.setPoint(0, (int)(oX + u), (int)(oY-R-r+c));
            region.setPoint(1, (int)(oX+b-u), (int)(oY - c  ));
            region.setPoint(2, (int)(oX+b-c), (int)(oY - u  ));
            region.setPoint(3, (int)(oX    ), (int)(oY - r  ));
            region.setPoint(4, (int)(oX    ), (int)(oY-R-r+s));
            break;
         case 5:
            region.setPoint(0, (int)(oX+a-u), (int)(oY-R-r+c));
            region.setPoint(1, (int)(oX+b+u), (int)(oY - c  ));
            region.setPoint(2, (int)(oX + b), (int)(oY - s  ));
            region.setPoint(3, (int)(oX + b), (int)(oY - R  ));
            region.setPoint(4, (int)(oX+a-c), (int)(oY-R-r+u));
            break;
         case 6:
            region.setPoint(0, (int)(oX+b-c), (int)(oY + u  ));
            region.setPoint(1, (int)(oX    ), (int)(oY + r  ));
            region.setPoint(2, (int)(oX-b+c), (int)(oY + u  ));
            region.setPoint(3, (int)(oX-b+s), (int)(oY      ));
            region.setPoint(4, (int)(oX+b-s), (int)(oY      ));
            break;
         case 7:
            region.setPoint(0, (int)(oX+b-u), (int)(oY + c  ));
            region.setPoint(1, (int)(oX + u), (int)(oY+R+r-c));
            region.setPoint(2, (int)(oX    ), (int)(oY+R+r-s));
            region.setPoint(3, (int)(oX    ), (int)(oY + r  ));
            region.setPoint(4, (int)(oX+b-c), (int)(oY + u  ));
            break;
         case 8:
            region.setPoint(0, (int)(oX+b+u), (int)(oY + c  ));
            region.setPoint(1, (int)(oX+a-u), (int)(oY+R+r-c));
            region.setPoint(2, (int)(oX+a-c), (int)(oY+R+r-u));
            region.setPoint(3, (int)(oX + b), (int)(oY + R  ));
            region.setPoint(4, (int)(oX + b), (int)(oY + s  ));
            break;
         case 9:
            region.setPoint(0, (int)(oX    ), (int)(oY + r  ));
            region.setPoint(1, (int)(oX    ), (int)(oY+R+r-s));
            region.setPoint(2, (int)(oX - u), (int)(oY+R+r-c));
            region.setPoint(3, (int)(oX-b+u), (int)(oY + c  ));
            region.setPoint(4, (int)(oX-b+c), (int)(oY + u  ));
            break;
         case 10:
            region.setPoint(0, (int)(oX + b), (int)(oY + s  ));
            region.setPoint(1, (int)(oX + b), (int)(oY + R  ));
            region.setPoint(2, (int)(oX + c), (int)(oY+R+r-u));
            region.setPoint(3, (int)(oX + u), (int)(oY+R+r-c));
            region.setPoint(4, (int)(oX+b-u), (int)(oY + c  ));
            break;
         case 11:
            region.setPoint(0, (int)(oX+a-s), (int)(oY + R+r));
            region.setPoint(1, (int)(oX + s), (int)(oY + R+r));
            region.setPoint(2, (int)(oX + c), (int)(oY+R+r-u));
            region.setPoint(3, (int)(oX + b), (int)(oY + R  ));
            region.setPoint(4, (int)(oX+a-c), (int)(oY+R+r-u));
            break;
         }
         #endregion
         break;
      }
   }

   public override Rect getRcInner(int borderWidth) {
      AttrTriangle4 attr = Attr;
      double w = borderWidth/2.0;
      double sq    = attr.GetSq(borderWidth);
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
         case ComplexityMode.eUnrealMode : center.y = region.getPoint(                 0    ).y + sq2w; break;
         case ComplexityMode.eMeanMode   : center.y = region.getPoint((direction==2) ? 0 : 4).y + sq2w; break;
         case ComplexityMode.eOptimalMode: center.y = region.getPoint(                   3  ).y + sq2w; break;
         case ComplexityMode.eSimpeMode  : center.y = region.getPoint(                     4).y + sq2w; break;
         }
         break;
      case 3: case 11:
         switch (AttrTriangle4.Mode) {
         case ComplexityMode.eUnrealMode : center.x = region.getPoint(                     2).x; break;
         case ComplexityMode.eMeanMode   : center.x = region.getPoint((direction==3) ? 3 : 2).x; break;
         case ComplexityMode.eOptimalMode: center.x = region.getPoint(                 3    ).x; break;
         case ComplexityMode.eSimpeMode  : center.x = region.getPoint(                 3    ).x; break;
         }
         center.y = region.getPoint(1).y - sq2w;
         break;
      case 4: case 8:
         switch (AttrTriangle4.Mode) {
         case ComplexityMode.eUnrealMode:
            center.x = region.getPoint(2).x + sq2w;
            center.y = region.getPoint(2).y - sq2w3;
            break;
         case ComplexityMode.eOptimalMode:
            center.x = region.getPoint(3).x + sq2w;
            center.y = region.getPoint((direction==4) ? 2 : 3).y - sq2w3;
            break;
         case ComplexityMode.eMeanMode  :
         case ComplexityMode.eSimpeMode :
            center.x = region.getPoint(3).x + sq2w;
            center.y = region.getPoint(3).y - sq2w3;
            break;
         }
         break;
      case 5: case 7:
         switch (AttrTriangle4.Mode) {
         case ComplexityMode.eUnrealMode : center.x = region.getPoint(                 2    ).x + sq2w;
                                           center.y = region.getPoint(                 2    ).y + sq2w3; break;
         case ComplexityMode.eMeanMode   : center.x = region.getPoint((direction==5) ? 2 : 3).x + sq2w;
                                           center.y = region.getPoint((direction==5) ? 2 : 3).y + sq2w3; break;
         case ComplexityMode.eOptimalMode: center.x = region.getPoint((direction!=5) ? 2 : 3).x + sq2w;
                                           center.y = region.getPoint((direction!=5) ? 2 : 3).y + sq2w3; break;
         case ComplexityMode.eSimpeMode  : center.x = region.getPoint(                     3).x + sq2w;
                                           center.y = region.getPoint(                     3).y + sq2w3; break;
         }
         break;
      }

      Rect square = new Rect();
      square.x = (int) (center.x - sq2);
      square.y = (int) (center.y - sq2);
      square.width =
      square.height = (int) sq;
      return square;
   }

   public override int getShiftPointBorderIndex() {
      switch (AttrTriangle4.Mode) {
      case ComplexityMode.eUnrealMode:
         switch (direction) {
         case 1: case 3: case 5: case 7: case 9: case 11: return 1;
         default: return 2;
         }
      case ComplexityMode.eMeanMode:
         switch (direction) {
         case 1: case 3: case 5: case 7: case 9: case 11: return 1;
         case 4: case 8: return 3;
         default: return 2;
         }
      case ComplexityMode.eOptimalMode:
         switch (direction) {
         case 1: case 3: case 5: case 7: return 1;
         case 8: return 3;
         default: return 2;
         }
      case ComplexityMode.eSimpeMode:
         switch (direction) {
         case 1: case 2: case 3: case 5: case 7: case 9: case 11: return 2;
         default: return 3;
         }
      default: throw new InvalidOperationException("Unknown Mode==" + AttrTriangle4.Mode);
      }
   }
}
}