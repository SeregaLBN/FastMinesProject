////////////////////////////////////////////////////////////////////////////////
//                               FastMines project
//                                      © Sergey Krivulya (KSerg, aka SeregaLBN)
// file name: "PenrousePeriodic1.java"
//
// Реализация класса PenrousePeriodic1 - один из вариантов периодической мозаики Пенроуза (ромбы 72°-108° & 36°- 144°)
// Copyright (C) 2011 Sergey Krivulya
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
using fmg.common;
using fmg.common.geom;
using System.Collections.Generic;

namespace fmg.core.mosaic.cells {

   /// <summary> PenrousePeriodic1 - один из вариантов периодической мозаики Пенроуза (ромбы 72°-108° & 36°- 144°) </summary>
   public class PenrousePeriodic1 : BaseCell {

      public class AttrPenrousePeriodic1 : BaseAttribute {

         public AttrPenrousePeriodic1(double area)
            : base(area)
         {}

         public override SizeDouble GetOwnerSize(Matrisize sizeField) {
            var a = A;
            var b = B;
            var c = C;
            var e = E;
            var f = F;
            var h = H;
            var g = G;
            var k = K;
            var z = Z;
            var result = new SizeDouble(
                  g +
                  z*((sizeField.m+8)/9) +
                  k*((sizeField.m+7)/9) +
                  g*((sizeField.m+6)/9) +
                  g*((sizeField.m+5)/9) +
                  z*((sizeField.m+4)/9) +
                  g*((sizeField.m+3)/9) +
                  g*((sizeField.m+2)/9) +
                  z*((sizeField.m+1)/9) +
                  g*((sizeField.m+0)/9),
                  e +
                  f*((sizeField.n+13)/14) +
                  b*((sizeField.n+12)/14) +
                  h*((sizeField.n+11)/14) +
                  h*((sizeField.n+10)/14) +
                  b*((sizeField.n+ 9)/14) +
                  f*((sizeField.n+ 8)/14) +
                  b*((sizeField.n+ 7)/14) +
                  c*((sizeField.n+ 6)/14) +
                  b*((sizeField.n+ 5)/14) +
                  h*((sizeField.n+ 4)/14) +
                  h*((sizeField.n+ 3)/14) +
                  a*((sizeField.n+ 2)/14) +
                  h*((sizeField.n+ 1)/14) +
                  e*((sizeField.n+ 0)/14));

            // когда размер поля мал...
            if (sizeField.n < 14) { // ...нужно вычислять не только по общей формуле, а и убрать остатки по ширине...
               if ((sizeField.m % 9) == 7)
                  if (sizeField.n < 7)
                     result.Width -= g-z;
               if ((sizeField.m % 9) == 6)
                  if (sizeField.n < 4)
                     result.Width -= g-z;
               if ((sizeField.m % 9) == 4)
                  if (sizeField.n < 9)
                     result.Width -= g;
               if ((sizeField.m % 9) == 3) {
                  if (sizeField.n < 14)
                     result.Width -= z;
                  if (sizeField.n < 3)
                     result.Width -= g-z;
               }
               if ((sizeField.m % 9) == 2)
                  if (sizeField.n < 5)
                     result.Width -= z;
            }
            if (sizeField.m < 5) { // .. и высоте
               if ((sizeField.n % 14) == 0) {
                  if (sizeField.m < 4)
                     result.Height -= h;
                  if (sizeField.m < 2)
                     result.Height -= c;
               }
               if ((sizeField.n % 14) == 13)
                  if (sizeField.m < 2)
                     result.Height -= c;
               if ((sizeField.n % 14) == 7)
                  if (sizeField.m < 3)
                     result.Height -= f;
               if ((sizeField.n % 14) == 6)
                  if (sizeField.m < 5)
                     result.Height -= f;
               if ((sizeField.n % 14) == 5)
                  if (sizeField.m < 3)
                     result.Height -= a;
               if ((sizeField.n % 14) == 4)
                  if (sizeField.m < 2)
                     result.Height -= f;
               if ((sizeField.n % 14) == 3)
                  if (sizeField.m < 5)
                     result.Height -= f;
               if ((sizeField.n % 14) == 2)
                  if (sizeField.m < 3)
                     result.Height -= f;
               if ((sizeField.n % 14) == 1)
                  if (sizeField.m < 3)
                     result.Height -= f;
            }
            return result;
         }

         public override int getNeighborNumber(int direction) {
            switch (direction) {
            case 100: case 108: case 114: case 115: return  7;

            case  21: case  24: case  37: case  42: case  49: case  72:
            case  81: case  83: case  86: case  87: case  89: case  93:
            case  94: case  95: case  97: case  98: case 111: case 112: case 117: case 124: return  8;

            case   0: case   7: case   9: case  10: case  15: case  17:
            case  46: case  52: case  54: case  61: case  65:
            case  66: case  67: case  68: case  70: case  71: case  74:
            case  77: case  85: case 101: case 105: case 125: return  9;

            case   2: case   5: case  11: case  12: case  13: case  14: 
            case  18: case  20: case  23: case  25: case  26: case  28: 
            case  29: case  30: case  31: case  32: case  33: case  35: 
            case  36: case  38: case  39: case  40: case  41: case  43: 
            case  44: case  47: case  48: case  50: case  51: case  57: 
            case  58: case  62: case  63: case  64: case  69: case  73: 
            case  76: case  78: case  79: case  80: case  82: case  84: 
            case  88: case  90: case  91: case  92: case  96: case  99: 
            case 102: case 103: case 104: case 106: case 107: case 109: case  22: 
            case 116: case 118: case 119: case 120: case 121: case 122: case 123: return 10;

            case   1: case   6: case   8: case  16: case  19: case  45: 
            case  53: case  55: case  56: case  59: case  60: case  75: case 110: case 113: return 11;

            case   3: case   4: case  27: case  34: return 12;

            default:
               throw new ArgumentException("Invalid value direction=" + direction);
               //throw new Exception("Забыл case #" + direction);
            }
         }
         public override int getVertexNumber(int direction) { return 4; }

         static double vertexIntersection = 0.0;
         public override double getVertexIntersection() {
            if (vertexIntersection < 1) {
               var cntDirection = GetDirectionCount(); // 0..125
               var sum = 0;
               for (var dir=0; dir<cntDirection; dir++)
                  sum += getNeighborNumber(dir) +
                     4 + // соседние фигуры, которые граничат с гранями this, участвуют в подсчёте два раза... 
                     4; // ...сама this участвует подсчёте все 4 раза
               vertexIntersection = ((double)sum) / getVertexNumber(-1) / cntDirection;
   //          System.out.println("PenrousePeriodic1::getVertexgetVertexNeighbor == " + vertexIntersection);
            }
            return vertexIntersection;
         }

         public override Size GetDirectionSizeField() { return new Size(9, 14); }
         public override double A => Math.Sqrt(Area*63/(39*SIN72 + 24*SIN36));
         public double B => A + C;
         public double C => A * SIN18;
         public double E => H + C;
         public double H => A * SIN54;
         public double F => H - C;
         public double G => A * SIN72;
         public double Z => A * SIN36;
         public double K => Z + G;
         public override double GetSq(int borderWidth) {
            //var w = borderWidth/2.0;
            return A/SIN99 * SIN36 / SQRT2;
         }

         public override int getMaxBackgroundFillModeValue() {
            return base.getMaxBackgroundFillModeValue() + 2;
         }
      }

      public PenrousePeriodic1(AttrPenrousePeriodic1 attr, Coord coord)
         : base(attr, coord,
               (coord.y%14)*9 + (coord.x%9) // 0..125
            )
      {}

      private new AttrPenrousePeriodic1 Attr => (AttrPenrousePeriodic1) base.Attr;

      protected override IList<Coord> GetCoordsNeighbor() {
         var neighborCoord = new Coord[Attr.getNeighborNumber(getDirection())];

         // определяю координаты соседей
         switch (direction) {
         case 0:
            neighborCoord[ 0] = new Coord(coord.x-2, coord.y  );
            neighborCoord[ 1] = new Coord(coord.x-1, coord.y-1);
            neighborCoord[ 2] = new Coord(coord.x  , coord.y-1);
            neighborCoord[ 3] = new Coord(coord.x+1, coord.y-1);
            neighborCoord[ 4] = new Coord(coord.x+1, coord.y  );
            neighborCoord[ 5] = new Coord(coord.x+1, coord.y+1);
            neighborCoord[ 6] = new Coord(coord.x+1, coord.y+2);
            neighborCoord[ 7] = new Coord(coord.x  , coord.y+1);
            neighborCoord[ 8] = new Coord(coord.x-1, coord.y  );
            break;
         case 1:
            neighborCoord[ 0] = new Coord(coord.x  , coord.y-1);
            neighborCoord[ 1] = new Coord(coord.x  , coord.y-2);
            neighborCoord[ 2] = new Coord(coord.x+1, coord.y-2);
            neighborCoord[ 3] = new Coord(coord.x+1, coord.y-1);
            neighborCoord[ 4] = new Coord(coord.x+2, coord.y  );
            neighborCoord[ 5] = new Coord(coord.x+1, coord.y  );
            neighborCoord[ 6] = new Coord(coord.x  , coord.y+1);
            neighborCoord[ 7] = new Coord(coord.x  , coord.y+2);
            neighborCoord[ 8] = new Coord(coord.x-1, coord.y+1);
            neighborCoord[ 9] = new Coord(coord.x-2, coord.y  );
            neighborCoord[10] = new Coord(coord.x-1, coord.y  );
            break;
         case 2:
            neighborCoord[ 0] = new Coord(coord.x-1, coord.y-1);
            neighborCoord[ 1] = new Coord(coord.x-1, coord.y-2);
            neighborCoord[ 2] = new Coord(coord.x  , coord.y-2);
            neighborCoord[ 3] = new Coord(coord.x  , coord.y-1);
            neighborCoord[ 4] = new Coord(coord.x+1, coord.y  );
            neighborCoord[ 5] = new Coord(coord.x+1, coord.y+1);
            neighborCoord[ 6] = new Coord(coord.x+1, coord.y+2);
            neighborCoord[ 7] = new Coord(coord.x  , coord.y+1);
            neighborCoord[ 8] = new Coord(coord.x-1, coord.y+1);
            neighborCoord[ 9] = new Coord(coord.x-1, coord.y  );
            break;
         case 3:
            neighborCoord[ 0] = new Coord(coord.x-2, coord.y-1);
            neighborCoord[ 1] = new Coord(coord.x-2, coord.y-2);
            neighborCoord[ 2] = new Coord(coord.x-1, coord.y-2);
            neighborCoord[ 3] = new Coord(coord.x-1, coord.y-1);
            neighborCoord[ 4] = new Coord(coord.x  , coord.y-1);
            neighborCoord[ 5] = new Coord(coord.x+1, coord.y  );
            neighborCoord[ 6] = new Coord(coord.x+2, coord.y+1);
            neighborCoord[ 7] = new Coord(coord.x+2, coord.y+2);
            neighborCoord[ 8] = new Coord(coord.x+1, coord.y+1);
            neighborCoord[ 9] = new Coord(coord.x  , coord.y+1);
            neighborCoord[10] = new Coord(coord.x-1, coord.y  );
            neighborCoord[11] = new Coord(coord.x-2, coord.y  );
            break;
         case 4:
            neighborCoord[ 0] = new Coord(coord.x  , coord.y-1);
            neighborCoord[ 1] = new Coord(coord.x+1, coord.y-2);
            neighborCoord[ 2] = new Coord(coord.x+1, coord.y-1);
            neighborCoord[ 3] = new Coord(coord.x+2, coord.y-1);
            neighborCoord[ 4] = new Coord(coord.x+2, coord.y  );
            neighborCoord[ 5] = new Coord(coord.x+1, coord.y  );
            neighborCoord[ 6] = new Coord(coord.x+1, coord.y+1);
            neighborCoord[ 7] = new Coord(coord.x+1, coord.y+2);
            neighborCoord[ 8] = new Coord(coord.x  , coord.y+1);
            neighborCoord[ 9] = new Coord(coord.x-1, coord.y+1);
            neighborCoord[10] = new Coord(coord.x-1, coord.y  );
            neighborCoord[11] = new Coord(coord.x-1, coord.y-1);
            break;
         case 5:
            neighborCoord[ 0] = new Coord(coord.x-1, coord.y  );
            neighborCoord[ 1] = new Coord(coord.x-1, coord.y-1);
            neighborCoord[ 2] = new Coord(coord.x  , coord.y-2);
            neighborCoord[ 3] = new Coord(coord.x  , coord.y-1);
            neighborCoord[ 4] = new Coord(coord.x+1, coord.y-1);
            neighborCoord[ 5] = new Coord(coord.x+1, coord.y  );
            neighborCoord[ 6] = new Coord(coord.x+1, coord.y+1);
            neighborCoord[ 7] = new Coord(coord.x+2, coord.y+2);
            neighborCoord[ 8] = new Coord(coord.x+1, coord.y+2);
            neighborCoord[ 9] = new Coord(coord.x  , coord.y+1);
            break;
         case 6:
            neighborCoord[ 0] = new Coord(coord.x-2, coord.y-1);
            neighborCoord[ 1] = new Coord(coord.x-1, coord.y-2);
            neighborCoord[ 2] = new Coord(coord.x-1, coord.y-1);
            neighborCoord[ 3] = new Coord(coord.x  , coord.y-1);
            neighborCoord[ 4] = new Coord(coord.x+1, coord.y  );
            neighborCoord[ 5] = new Coord(coord.x+2, coord.y  );
            neighborCoord[ 6] = new Coord(coord.x+2, coord.y+1);
            neighborCoord[ 7] = new Coord(coord.x+1, coord.y+1);
            neighborCoord[ 8] = new Coord(coord.x  , coord.y+1);
            neighborCoord[ 9] = new Coord(coord.x-1, coord.y  );
            neighborCoord[10] = new Coord(coord.x-2, coord.y  );
            break;
         case 7:
            neighborCoord[ 0] = new Coord(coord.x-1, coord.y-1);
            neighborCoord[ 1] = new Coord(coord.x  , coord.y-1);
            neighborCoord[ 2] = new Coord(coord.x+1, coord.y-1);
            neighborCoord[ 3] = new Coord(coord.x+2, coord.y  );
            neighborCoord[ 4] = new Coord(coord.x+1, coord.y  );
            neighborCoord[ 5] = new Coord(coord.x+1, coord.y+1);
            neighborCoord[ 6] = new Coord(coord.x  , coord.y+1);
            neighborCoord[ 7] = new Coord(coord.x-1, coord.y+1);
            neighborCoord[ 8] = new Coord(coord.x-1, coord.y  );
            break;
         case 8:
            neighborCoord[ 0] = new Coord(coord.x-2, coord.y  );
            neighborCoord[ 1] = new Coord(coord.x-1, coord.y  );
            neighborCoord[ 2] = new Coord(coord.x  , coord.y-1);
            neighborCoord[ 3] = new Coord(coord.x+1, coord.y  );
            neighborCoord[ 4] = new Coord(coord.x+2, coord.y  );
            neighborCoord[ 5] = new Coord(coord.x+2, coord.y+1);
            neighborCoord[ 6] = new Coord(coord.x+2, coord.y+2);
            neighborCoord[ 7] = new Coord(coord.x+1, coord.y+1);
            neighborCoord[ 8] = new Coord(coord.x  , coord.y+1);
            neighborCoord[ 9] = new Coord(coord.x-1, coord.y+1);
            neighborCoord[10] = new Coord(coord.x-2, coord.y+1);
            break;
         case 9:
            neighborCoord[ 0] = new Coord(coord.x-1, coord.y-1);
            neighborCoord[ 1] = new Coord(coord.x  , coord.y-1);
            neighborCoord[ 2] = new Coord(coord.x+1, coord.y-1);
            neighborCoord[ 3] = new Coord(coord.x+1, coord.y  );
            neighborCoord[ 4] = new Coord(coord.x+1, coord.y+1);
            neighborCoord[ 5] = new Coord(coord.x  , coord.y+1);
            neighborCoord[ 6] = new Coord(coord.x-1, coord.y+2);
            neighborCoord[ 7] = new Coord(coord.x-1, coord.y+1);
            neighborCoord[ 8] = new Coord(coord.x-1, coord.y  );
            break;
         case 10:
            neighborCoord[ 0] = new Coord(coord.x-2, coord.y-1);
            neighborCoord[ 1] = new Coord(coord.x-1, coord.y-1);
            neighborCoord[ 2] = new Coord(coord.x  , coord.y-1);
            neighborCoord[ 3] = new Coord(coord.x+1, coord.y-1);
            neighborCoord[ 4] = new Coord(coord.x+2, coord.y  );
            neighborCoord[ 5] = new Coord(coord.x+2, coord.y+1);
            neighborCoord[ 6] = new Coord(coord.x+1, coord.y  );
            neighborCoord[ 7] = new Coord(coord.x  , coord.y+1);
            neighborCoord[ 8] = new Coord(coord.x-1, coord.y  );
            break;
         case 11:
            neighborCoord[ 0] = new Coord(coord.x-1, coord.y  );
            neighborCoord[ 1] = new Coord(coord.x  , coord.y-1);
            neighborCoord[ 2] = new Coord(coord.x+1, coord.y  );
            neighborCoord[ 3] = new Coord(coord.x+1, coord.y+1);
            neighborCoord[ 4] = new Coord(coord.x  , coord.y+1);
            neighborCoord[ 5] = new Coord(coord.x  , coord.y+2);
            neighborCoord[ 6] = new Coord(coord.x-1, coord.y+2);
            neighborCoord[ 7] = new Coord(coord.x-2, coord.y+2);
            neighborCoord[ 8] = new Coord(coord.x-2, coord.y+1);
            neighborCoord[ 9] = new Coord(coord.x-1, coord.y+1);
            break;
         case 12:
            neighborCoord[ 0] = new Coord(coord.x-1, coord.y  );
            neighborCoord[ 1] = new Coord(coord.x-1, coord.y-1);
            neighborCoord[ 2] = new Coord(coord.x  , coord.y-1);
            neighborCoord[ 3] = new Coord(coord.x  , coord.y-2);
            neighborCoord[ 4] = new Coord(coord.x+1, coord.y-1);
            neighborCoord[ 5] = new Coord(coord.x+2, coord.y  );
            neighborCoord[ 6] = new Coord(coord.x+2, coord.y+1);
            neighborCoord[ 7] = new Coord(coord.x+1, coord.y  );
            neighborCoord[ 8] = new Coord(coord.x  , coord.y+1);
            neighborCoord[ 9] = new Coord(coord.x-2, coord.y  );
            break;
         case 13:
            neighborCoord[ 0] = new Coord(coord.x-1, coord.y-1);
            neighborCoord[ 1] = new Coord(coord.x-1, coord.y-2);
            neighborCoord[ 2] = new Coord(coord.x  , coord.y-1);
            neighborCoord[ 3] = new Coord(coord.x+1, coord.y  );
            neighborCoord[ 4] = new Coord(coord.x+1, coord.y+1);
            neighborCoord[ 5] = new Coord(coord.x  , coord.y+1);
            neighborCoord[ 6] = new Coord(coord.x-1, coord.y+2);
            neighborCoord[ 7] = new Coord(coord.x-2, coord.y+1);
            neighborCoord[ 8] = new Coord(coord.x-1, coord.y+1);
            neighborCoord[ 9] = new Coord(coord.x-1, coord.y  );
            break;
         case 14:
            neighborCoord[ 0] = new Coord(coord.x-2, coord.y-1);
            neighborCoord[ 1] = new Coord(coord.x-2, coord.y-2);
            neighborCoord[ 2] = new Coord(coord.x-1, coord.y-1);
            neighborCoord[ 3] = new Coord(coord.x  , coord.y-1);
            neighborCoord[ 4] = new Coord(coord.x+1, coord.y  );
            neighborCoord[ 5] = new Coord(coord.x+2, coord.y+1);
            neighborCoord[ 6] = new Coord(coord.x+1, coord.y+1);
            neighborCoord[ 7] = new Coord(coord.x  , coord.y+1);
            neighborCoord[ 8] = new Coord(coord.x-1, coord.y  );
            neighborCoord[ 9] = new Coord(coord.x-2, coord.y  );
            break;
         case 15:
            neighborCoord[ 0] = new Coord(coord.x-1, coord.y  );
            neighborCoord[ 1] = new Coord(coord.x-1, coord.y-1);
            neighborCoord[ 2] = new Coord(coord.x  , coord.y-1);
            neighborCoord[ 3] = new Coord(coord.x+1, coord.y-1);
            neighborCoord[ 4] = new Coord(coord.x+2, coord.y-1);
            neighborCoord[ 5] = new Coord(coord.x+2, coord.y  );
            neighborCoord[ 6] = new Coord(coord.x+1, coord.y  );
            neighborCoord[ 7] = new Coord(coord.x+1, coord.y+1);
            neighborCoord[ 8] = new Coord(coord.x  , coord.y+1);
            break;
         case 16:
            neighborCoord[ 0] = new Coord(coord.x-1, coord.y-1);
            neighborCoord[ 1] = new Coord(coord.x  , coord.y-1);
            neighborCoord[ 2] = new Coord(coord.x+1, coord.y-1);
            neighborCoord[ 3] = new Coord(coord.x+1, coord.y  );
            neighborCoord[ 4] = new Coord(coord.x+1, coord.y+1);
            neighborCoord[ 5] = new Coord(coord.x  , coord.y+2);
            neighborCoord[ 6] = new Coord(coord.x  , coord.y+3);
            neighborCoord[ 7] = new Coord(coord.x-1, coord.y+2);
            neighborCoord[ 8] = new Coord(coord.x-2, coord.y+2);
            neighborCoord[ 9] = new Coord(coord.x  , coord.y+1);
            neighborCoord[10] = new Coord(coord.x-1, coord.y  );
            break;
         case 17:
            neighborCoord[ 0] = new Coord(coord.x-2, coord.y  );
            neighborCoord[ 1] = new Coord(coord.x-2, coord.y-1);
            neighborCoord[ 2] = new Coord(coord.x-1, coord.y-1);
            neighborCoord[ 3] = new Coord(coord.x  , coord.y-1);
            neighborCoord[ 4] = new Coord(coord.x+1, coord.y  );
            neighborCoord[ 5] = new Coord(coord.x+1, coord.y+1);
            neighborCoord[ 6] = new Coord(coord.x  , coord.y+2);
            neighborCoord[ 7] = new Coord(coord.x  , coord.y+1);
            neighborCoord[ 8] = new Coord(coord.x-1, coord.y  );
            break;
         case 18:
            neighborCoord[ 0] = new Coord(coord.x-1, coord.y  );
            neighborCoord[ 1] = new Coord(coord.x-1, coord.y-1);
            neighborCoord[ 2] = new Coord(coord.x  , coord.y-1);
            neighborCoord[ 3] = new Coord(coord.x+1, coord.y  );
            neighborCoord[ 4] = new Coord(coord.x+2, coord.y-1);
            neighborCoord[ 5] = new Coord(coord.x+2, coord.y  );
            neighborCoord[ 6] = new Coord(coord.x+2, coord.y+1);
            neighborCoord[ 7] = new Coord(coord.x+1, coord.y+1);
            neighborCoord[ 8] = new Coord(coord.x  , coord.y+1);
            neighborCoord[ 9] = new Coord(coord.x-1, coord.y+1);
            break;
         case 19:
            neighborCoord[ 0] = new Coord(coord.x-2, coord.y-2);
            neighborCoord[ 1] = new Coord(coord.x-1, coord.y-2);
            neighborCoord[ 2] = new Coord(coord.x  , coord.y-2);
            neighborCoord[ 3] = new Coord(coord.x  , coord.y-1);
            neighborCoord[ 4] = new Coord(coord.x+1, coord.y-1);
            neighborCoord[ 5] = new Coord(coord.x+1, coord.y  );
            neighborCoord[ 6] = new Coord(coord.x+1, coord.y+1);
            neighborCoord[ 7] = new Coord(coord.x  , coord.y+1);
            neighborCoord[ 8] = new Coord(coord.x-1, coord.y+1);
            neighborCoord[ 9] = new Coord(coord.x-1, coord.y  );
            neighborCoord[10] = new Coord(coord.x-1, coord.y-1);
            break;
         case 20:
            neighborCoord[ 0] = new Coord(coord.x-2, coord.y  );
            neighborCoord[ 1] = new Coord(coord.x-1, coord.y  );
            neighborCoord[ 2] = new Coord(coord.x  , coord.y-1);
            neighborCoord[ 3] = new Coord(coord.x+1, coord.y  );
            neighborCoord[ 4] = new Coord(coord.x+2, coord.y-1);
            neighborCoord[ 5] = new Coord(coord.x+2, coord.y  );
            neighborCoord[ 6] = new Coord(coord.x+1, coord.y+1);
            neighborCoord[ 7] = new Coord(coord.x  , coord.y+1);
            neighborCoord[ 8] = new Coord(coord.x-1, coord.y+1);
            neighborCoord[ 9] = new Coord(coord.x-2, coord.y+1);
            break;
         case 21:
            neighborCoord[ 0] = new Coord(coord.x-2, coord.y-1);
            neighborCoord[ 1] = new Coord(coord.x-1, coord.y-2);
            neighborCoord[ 2] = new Coord(coord.x  , coord.y-1);
            neighborCoord[ 3] = new Coord(coord.x+1, coord.y-1);
            neighborCoord[ 4] = new Coord(coord.x+1, coord.y  );
            neighborCoord[ 5] = new Coord(coord.x  , coord.y+1);
            neighborCoord[ 6] = new Coord(coord.x-1, coord.y  );
            neighborCoord[ 7] = new Coord(coord.x-1, coord.y-1);
            break;
         case 22:
            neighborCoord[ 0] = new Coord(coord.x-2, coord.y  );
            neighborCoord[ 1] = new Coord(coord.x-1, coord.y  );
            neighborCoord[ 2] = new Coord(coord.x  , coord.y-1);
            neighborCoord[ 3] = new Coord(coord.x+1, coord.y  );
            neighborCoord[ 4] = new Coord(coord.x+2, coord.y  );
            neighborCoord[ 5] = new Coord(coord.x+1, coord.y+1);
            neighborCoord[ 6] = new Coord(coord.x  , coord.y+1);
            neighborCoord[ 7] = new Coord(coord.x  , coord.y+2);
            neighborCoord[ 8] = new Coord(coord.x-1, coord.y+2);
            neighborCoord[ 9] = new Coord(coord.x-1, coord.y+1);
            break;
         case 23:
            neighborCoord[ 0] = new Coord(coord.x-2, coord.y-1);
            neighborCoord[ 1] = new Coord(coord.x-2, coord.y-2);
            neighborCoord[ 2] = new Coord(coord.x-2, coord.y-3);
            neighborCoord[ 3] = new Coord(coord.x-1, coord.y-2);
            neighborCoord[ 4] = new Coord(coord.x  , coord.y-1);
            neighborCoord[ 5] = new Coord(coord.x+1, coord.y  );
            neighborCoord[ 6] = new Coord(coord.x  , coord.y+1);
            neighborCoord[ 7] = new Coord(coord.x-1, coord.y+1);
            neighborCoord[ 8] = new Coord(coord.x-1, coord.y  );
            neighborCoord[ 9] = new Coord(coord.x-1, coord.y-1);
            break;
         case 24:
            neighborCoord[ 0] = new Coord(coord.x-1, coord.y-1);
            neighborCoord[ 1] = new Coord(coord.x-1, coord.y-2);
            neighborCoord[ 2] = new Coord(coord.x  , coord.y-1);
            neighborCoord[ 3] = new Coord(coord.x+1, coord.y  );
            neighborCoord[ 4] = new Coord(coord.x-1, coord.y+1);
            neighborCoord[ 5] = new Coord(coord.x-2, coord.y+1);
            neighborCoord[ 6] = new Coord(coord.x-2, coord.y  );
            neighborCoord[ 7] = new Coord(coord.x-1, coord.y  );
            break;
         case 25:
            neighborCoord[ 0] = new Coord(coord.x-2, coord.y-1);
            neighborCoord[ 1] = new Coord(coord.x-2, coord.y-2);
            neighborCoord[ 2] = new Coord(coord.x-1, coord.y-1);
            neighborCoord[ 3] = new Coord(coord.x  , coord.y-1);
            neighborCoord[ 4] = new Coord(coord.x+1, coord.y  );
            neighborCoord[ 5] = new Coord(coord.x  , coord.y+1);
            neighborCoord[ 6] = new Coord(coord.x  , coord.y+2);
            neighborCoord[ 7] = new Coord(coord.x-1, coord.y+1);
            neighborCoord[ 8] = new Coord(coord.x-2, coord.y+1);
            neighborCoord[ 9] = new Coord(coord.x-1, coord.y  );
            break;
         case 26:
            neighborCoord[ 0] = new Coord(coord.x-1, coord.y  );
            neighborCoord[ 1] = new Coord(coord.x-1, coord.y-1);
            neighborCoord[ 2] = new Coord(coord.x  , coord.y-1);
            neighborCoord[ 3] = new Coord(coord.x+1, coord.y-1);
            neighborCoord[ 4] = new Coord(coord.x+1, coord.y  );
            neighborCoord[ 5] = new Coord(coord.x  , coord.y+1);
            neighborCoord[ 6] = new Coord(coord.x-1, coord.y+1);
            neighborCoord[ 7] = new Coord(coord.x-1, coord.y+2);
            neighborCoord[ 8] = new Coord(coord.x-2, coord.y+1);
            neighborCoord[ 9] = new Coord(coord.x-3, coord.y+1);
            break;
         case 27:
            neighborCoord[ 0] = new Coord(coord.x  , coord.y-1);
            neighborCoord[ 1] = new Coord(coord.x+1, coord.y-1);
            neighborCoord[ 2] = new Coord(coord.x+2, coord.y-2);
            neighborCoord[ 3] = new Coord(coord.x+2, coord.y-1);
            neighborCoord[ 4] = new Coord(coord.x+2, coord.y  );
            neighborCoord[ 5] = new Coord(coord.x+1, coord.y  );
            neighborCoord[ 6] = new Coord(coord.x  , coord.y+1);
            neighborCoord[ 7] = new Coord(coord.x  , coord.y+2);
            neighborCoord[ 8] = new Coord(coord.x-1, coord.y+2);
            neighborCoord[ 9] = new Coord(coord.x-1, coord.y+1);
            neighborCoord[10] = new Coord(coord.x-2, coord.y  );
            neighborCoord[11] = new Coord(coord.x-1, coord.y  );
            break;
         case 28:
            neighborCoord[ 0] = new Coord(coord.x-1, coord.y-1);
            neighborCoord[ 1] = new Coord(coord.x  , coord.y-1);
            neighborCoord[ 2] = new Coord(coord.x+1, coord.y-2);
            neighborCoord[ 3] = new Coord(coord.x+1, coord.y-1);
            neighborCoord[ 4] = new Coord(coord.x+1, coord.y  );
            neighborCoord[ 5] = new Coord(coord.x  , coord.y+1);
            neighborCoord[ 6] = new Coord(coord.x+1, coord.y+2);
            neighborCoord[ 7] = new Coord(coord.x  , coord.y+2);
            neighborCoord[ 8] = new Coord(coord.x-1, coord.y+1);
            neighborCoord[ 9] = new Coord(coord.x-1, coord.y  );
            break;
         case 29:
            neighborCoord[ 0] = new Coord(coord.x-2, coord.y-1);
            neighborCoord[ 1] = new Coord(coord.x-1, coord.y-1);
            neighborCoord[ 2] = new Coord(coord.x  , coord.y-2);
            neighborCoord[ 3] = new Coord(coord.x  , coord.y-1);
            neighborCoord[ 4] = new Coord(coord.x+1, coord.y  );
            neighborCoord[ 5] = new Coord(coord.x+1, coord.y+1);
            neighborCoord[ 6] = new Coord(coord.x  , coord.y+1);
            neighborCoord[ 7] = new Coord(coord.x-1, coord.y+1);
            neighborCoord[ 8] = new Coord(coord.x-1, coord.y  );
            neighborCoord[ 9] = new Coord(coord.x-2, coord.y  );
            break;
         case 30:
            neighborCoord[ 0] = new Coord(coord.x  , coord.y-1);
            neighborCoord[ 1] = new Coord(coord.x+1, coord.y-2);
            neighborCoord[ 2] = new Coord(coord.x+1, coord.y-1);
            neighborCoord[ 3] = new Coord(coord.x+1, coord.y  );
            neighborCoord[ 4] = new Coord(coord.x+1, coord.y+1);
            neighborCoord[ 5] = new Coord(coord.x  , coord.y+1);
            neighborCoord[ 6] = new Coord(coord.x-1, coord.y+1);
            neighborCoord[ 7] = new Coord(coord.x-2, coord.y+1);
            neighborCoord[ 8] = new Coord(coord.x-1, coord.y  );
            neighborCoord[ 9] = new Coord(coord.x-1, coord.y-1);
            break;
         case 31:
            neighborCoord[ 0] = new Coord(coord.x+1, coord.y-1);
            neighborCoord[ 1] = new Coord(coord.x+2, coord.y-1);
            neighborCoord[ 2] = new Coord(coord.x+1, coord.y  );
            neighborCoord[ 3] = new Coord(coord.x+2, coord.y  );
            neighborCoord[ 4] = new Coord(coord.x+2, coord.y+1);
            neighborCoord[ 5] = new Coord(coord.x+1, coord.y+1);
            neighborCoord[ 6] = new Coord(coord.x  , coord.y+1);
            neighborCoord[ 7] = new Coord(coord.x-1, coord.y+1);
            neighborCoord[ 8] = new Coord(coord.x-1, coord.y  );
            neighborCoord[ 9] = new Coord(coord.x  , coord.y-1);
            break;
         case 32:
            neighborCoord[ 0] = new Coord(coord.x  , coord.y-1);
            neighborCoord[ 1] = new Coord(coord.x+1, coord.y-1);
            neighborCoord[ 2] = new Coord(coord.x+2, coord.y-1);
            neighborCoord[ 3] = new Coord(coord.x+2, coord.y-2);
            neighborCoord[ 4] = new Coord(coord.x+3, coord.y-1);
            neighborCoord[ 5] = new Coord(coord.x+2, coord.y  );
            neighborCoord[ 6] = new Coord(coord.x+2, coord.y+1);
            neighborCoord[ 7] = new Coord(coord.x+1, coord.y  );
            neighborCoord[ 8] = new Coord(coord.x-1, coord.y  );
            neighborCoord[ 9] = new Coord(coord.x-1, coord.y-1);
            break;
         case 33:
            neighborCoord[ 0] = new Coord(coord.x-1, coord.y  );
            neighborCoord[ 1] = new Coord(coord.x+1, coord.y-1);
            neighborCoord[ 2] = new Coord(coord.x+1, coord.y-2);
            neighborCoord[ 3] = new Coord(coord.x+2, coord.y-1);
            neighborCoord[ 4] = new Coord(coord.x+1, coord.y  );
            neighborCoord[ 5] = new Coord(coord.x+1, coord.y+1);
            neighborCoord[ 6] = new Coord(coord.x  , coord.y+1);
            neighborCoord[ 7] = new Coord(coord.x-1, coord.y+1);
            neighborCoord[ 8] = new Coord(coord.x-2, coord.y+1);
            neighborCoord[ 9] = new Coord(coord.x-2, coord.y  );
            break;
         case 34:
            neighborCoord[ 0] = new Coord(coord.x-2, coord.y  );
            neighborCoord[ 1] = new Coord(coord.x  , coord.y-1);
            neighborCoord[ 2] = new Coord(coord.x  , coord.y-2);
            neighborCoord[ 3] = new Coord(coord.x+1, coord.y-1);
            neighborCoord[ 4] = new Coord(coord.x+1, coord.y  );
            neighborCoord[ 5] = new Coord(coord.x+2, coord.y  );
            neighborCoord[ 6] = new Coord(coord.x+2, coord.y+1);
            neighborCoord[ 7] = new Coord(coord.x+2, coord.y+2);
            neighborCoord[ 8] = new Coord(coord.x+1, coord.y+2);
            neighborCoord[ 9] = new Coord(coord.x+1, coord.y+1);
            neighborCoord[10] = new Coord(coord.x  , coord.y+1);
            neighborCoord[11] = new Coord(coord.x-1, coord.y  );
            break;
         case 35:
            neighborCoord[ 0] = new Coord(coord.x  , coord.y-1);
            neighborCoord[ 1] = new Coord(coord.x  , coord.y-2);
            neighborCoord[ 2] = new Coord(coord.x+1, coord.y-2);
            neighborCoord[ 3] = new Coord(coord.x+1, coord.y-1);
            neighborCoord[ 4] = new Coord(coord.x+1, coord.y  );
            neighborCoord[ 5] = new Coord(coord.x+1, coord.y+1);
            neighborCoord[ 6] = new Coord(coord.x+1, coord.y+2);
            neighborCoord[ 7] = new Coord(coord.x  , coord.y+2);
            neighborCoord[ 8] = new Coord(coord.x  , coord.y+1);
            neighborCoord[ 9] = new Coord(coord.x-1, coord.y  );
            break;
         case 36:
            neighborCoord[ 0] = new Coord(coord.x-2, coord.y-1);
            neighborCoord[ 1] = new Coord(coord.x-1, coord.y-1);
            neighborCoord[ 2] = new Coord(coord.x  , coord.y-1);
            neighborCoord[ 3] = new Coord(coord.x+1, coord.y-1);
            neighborCoord[ 4] = new Coord(coord.x+1, coord.y  );
            neighborCoord[ 5] = new Coord(coord.x+2, coord.y+1);
            neighborCoord[ 6] = new Coord(coord.x+1, coord.y+1);
            neighborCoord[ 7] = new Coord(coord.x  , coord.y+1);
            neighborCoord[ 8] = new Coord(coord.x-1, coord.y+1);
            neighborCoord[ 9] = new Coord(coord.x-1, coord.y  );
            break;
         case 37:
            neighborCoord[ 0] = new Coord(coord.x-1, coord.y  );
            neighborCoord[ 1] = new Coord(coord.x  , coord.y-1);
            neighborCoord[ 2] = new Coord(coord.x+1, coord.y-1);
            neighborCoord[ 3] = new Coord(coord.x+2, coord.y-1);
            neighborCoord[ 4] = new Coord(coord.x+2, coord.y  );
            neighborCoord[ 5] = new Coord(coord.x+1, coord.y  );
            neighborCoord[ 6] = new Coord(coord.x+1, coord.y+1);
            neighborCoord[ 7] = new Coord(coord.x  , coord.y+1);
            break;
         case 38:
            neighborCoord[ 0] = new Coord(coord.x-1, coord.y  );
            neighborCoord[ 1] = new Coord(coord.x  , coord.y-1);
            neighborCoord[ 2] = new Coord(coord.x+1, coord.y-1);
            neighborCoord[ 3] = new Coord(coord.x+1, coord.y  );
            neighborCoord[ 4] = new Coord(coord.x+1, coord.y+1);
            neighborCoord[ 5] = new Coord(coord.x+1, coord.y+2);
            neighborCoord[ 6] = new Coord(coord.x  , coord.y+2);
            neighborCoord[ 7] = new Coord(coord.x-1, coord.y+3);
            neighborCoord[ 8] = new Coord(coord.x-1, coord.y+2);
            neighborCoord[ 9] = new Coord(coord.x  , coord.y+1);
            break;
         case 39:
            neighborCoord[ 0] = new Coord(coord.x-2, coord.y  );
            neighborCoord[ 1] = new Coord(coord.x-1, coord.y-1);
            neighborCoord[ 2] = new Coord(coord.x  , coord.y-1);
            neighborCoord[ 3] = new Coord(coord.x+1, coord.y-2);
            neighborCoord[ 4] = new Coord(coord.x+1, coord.y-1);
            neighborCoord[ 5] = new Coord(coord.x+1, coord.y  );
            neighborCoord[ 6] = new Coord(coord.x+2, coord.y+1);
            neighborCoord[ 7] = new Coord(coord.x+1, coord.y+1);
            neighborCoord[ 8] = new Coord(coord.x  , coord.y+1);
            neighborCoord[ 9] = new Coord(coord.x-1, coord.y  );
            break;
         case 40:
            neighborCoord[ 0] = new Coord(coord.x-1, coord.y-1);
            neighborCoord[ 1] = new Coord(coord.x  , coord.y-2);
            neighborCoord[ 2] = new Coord(coord.x  , coord.y-1);
            neighborCoord[ 3] = new Coord(coord.x+2, coord.y-1);
            neighborCoord[ 4] = new Coord(coord.x+2, coord.y  );
            neighborCoord[ 5] = new Coord(coord.x+1, coord.y  );
            neighborCoord[ 6] = new Coord(coord.x+1, coord.y+1);
            neighborCoord[ 7] = new Coord(coord.x  , coord.y+1);
            neighborCoord[ 8] = new Coord(coord.x-1, coord.y+1);
            neighborCoord[ 9] = new Coord(coord.x-1, coord.y  );
            break;
         case 41:
            neighborCoord[ 0] = new Coord(coord.x-1, coord.y-1);
            neighborCoord[ 1] = new Coord(coord.x+1, coord.y-1);
            neighborCoord[ 2] = new Coord(coord.x+1, coord.y  );
            neighborCoord[ 3] = new Coord(coord.x+1, coord.y+1);
            neighborCoord[ 4] = new Coord(coord.x+1, coord.y+2);
            neighborCoord[ 5] = new Coord(coord.x+1, coord.y+3);
            neighborCoord[ 6] = new Coord(coord.x  , coord.y+2);
            neighborCoord[ 7] = new Coord(coord.x-1, coord.y+2);
            neighborCoord[ 8] = new Coord(coord.x  , coord.y+1);
            neighborCoord[ 9] = new Coord(coord.x-1, coord.y  );
            break;
         case 42:
            neighborCoord[ 0] = new Coord(coord.x-2, coord.y-1);
            neighborCoord[ 1] = new Coord(coord.x  , coord.y-1);
            neighborCoord[ 2] = new Coord(coord.x+1, coord.y  );
            neighborCoord[ 3] = new Coord(coord.x+2, coord.y  );
            neighborCoord[ 4] = new Coord(coord.x+1, coord.y+1);
            neighborCoord[ 5] = new Coord(coord.x  , coord.y+1);
            neighborCoord[ 6] = new Coord(coord.x-1, coord.y  );
            neighborCoord[ 7] = new Coord(coord.x-2, coord.y  );
            break;
         case 43:
            neighborCoord[ 0] = new Coord(coord.x-2, coord.y-1);
            neighborCoord[ 1] = new Coord(coord.x  , coord.y-2);
            neighborCoord[ 2] = new Coord(coord.x  , coord.y-3);
            neighborCoord[ 3] = new Coord(coord.x+1, coord.y-2);
            neighborCoord[ 4] = new Coord(coord.x  , coord.y-1);
            neighborCoord[ 5] = new Coord(coord.x+1, coord.y  );
            neighborCoord[ 6] = new Coord(coord.x  , coord.y+1);
            neighborCoord[ 7] = new Coord(coord.x-1, coord.y+1);
            neighborCoord[ 8] = new Coord(coord.x-1, coord.y  );
            neighborCoord[ 9] = new Coord(coord.x-1, coord.y-1);
            break;
         case 44:
            neighborCoord[ 0] = new Coord(coord.x-2, coord.y  );
            neighborCoord[ 1] = new Coord(coord.x-1, coord.y  );
            neighborCoord[ 2] = new Coord(coord.x-1, coord.y-1);
            neighborCoord[ 3] = new Coord(coord.x  , coord.y-1);
            neighborCoord[ 4] = new Coord(coord.x+1, coord.y-1);
            neighborCoord[ 5] = new Coord(coord.x+1, coord.y  );
            neighborCoord[ 6] = new Coord(coord.x+1, coord.y+1);
            neighborCoord[ 7] = new Coord(coord.x  , coord.y+1);
            neighborCoord[ 8] = new Coord(coord.x-1, coord.y+1);
            neighborCoord[ 9] = new Coord(coord.x-2, coord.y+1);
            break;
         case 45:
            neighborCoord[ 0] = new Coord(coord.x-1, coord.y-1);
            neighborCoord[ 1] = new Coord(coord.x-2, coord.y-2);
            neighborCoord[ 2] = new Coord(coord.x-1, coord.y-2);
            neighborCoord[ 3] = new Coord(coord.x  , coord.y-2);
            neighborCoord[ 4] = new Coord(coord.x  , coord.y-1);
            neighborCoord[ 5] = new Coord(coord.x+1, coord.y  );
            neighborCoord[ 6] = new Coord(coord.x+1, coord.y+1);
            neighborCoord[ 7] = new Coord(coord.x  , coord.y+1);
            neighborCoord[ 8] = new Coord(coord.x-1, coord.y+2);
            neighborCoord[ 9] = new Coord(coord.x-1, coord.y+1);
            neighborCoord[10] = new Coord(coord.x-1, coord.y  );
            break;
         case 46:
            neighborCoord[ 0] = new Coord(coord.x-1, coord.y  );
            neighborCoord[ 1] = new Coord(coord.x-1, coord.y-1);
            neighborCoord[ 2] = new Coord(coord.x  , coord.y-2);
            neighborCoord[ 3] = new Coord(coord.x  , coord.y-1);
            neighborCoord[ 4] = new Coord(coord.x+1, coord.y  );
            neighborCoord[ 5] = new Coord(coord.x  , coord.y+1);
            neighborCoord[ 6] = new Coord(coord.x-1, coord.y+1);
            neighborCoord[ 7] = new Coord(coord.x-2, coord.y+2);
            neighborCoord[ 8] = new Coord(coord.x-2, coord.y+1);
            break;
         case 47:
            neighborCoord[ 0] = new Coord(coord.x-2, coord.y-1);
            neighborCoord[ 1] = new Coord(coord.x-1, coord.y-2);
            neighborCoord[ 2] = new Coord(coord.x-1, coord.y-1);
            neighborCoord[ 3] = new Coord(coord.x  , coord.y-1);
            neighborCoord[ 4] = new Coord(coord.x+1, coord.y  );
            neighborCoord[ 5] = new Coord(coord.x+1, coord.y+1);
            neighborCoord[ 6] = new Coord(coord.x  , coord.y+1);
            neighborCoord[ 7] = new Coord(coord.x-1, coord.y+2);
            neighborCoord[ 8] = new Coord(coord.x-1, coord.y+1);
            neighborCoord[ 9] = new Coord(coord.x-1, coord.y  );
            break;
         case 48:
            neighborCoord[ 0] = new Coord(coord.x-2, coord.y+1);
            neighborCoord[ 1] = new Coord(coord.x-1, coord.y  );
            neighborCoord[ 2] = new Coord(coord.x-1, coord.y-1);
            neighborCoord[ 3] = new Coord(coord.x  , coord.y-1);
            neighborCoord[ 4] = new Coord(coord.x+1, coord.y-1);
            neighborCoord[ 5] = new Coord(coord.x+2, coord.y  );
            neighborCoord[ 6] = new Coord(coord.x+1, coord.y  );
            neighborCoord[ 7] = new Coord(coord.x  , coord.y+1);
            neighborCoord[ 8] = new Coord(coord.x-1, coord.y+1);
            neighborCoord[ 9] = new Coord(coord.x-2, coord.y+2);
            break;
         case 49:
            neighborCoord[ 0] = new Coord(coord.x-1, coord.y  );
            neighborCoord[ 1] = new Coord(coord.x-1, coord.y-1);
            neighborCoord[ 2] = new Coord(coord.x  , coord.y-1);
            neighborCoord[ 3] = new Coord(coord.x+1, coord.y  );
            neighborCoord[ 4] = new Coord(coord.x  , coord.y+1);
            neighborCoord[ 5] = new Coord(coord.x  , coord.y+2);
            neighborCoord[ 6] = new Coord(coord.x-1, coord.y+2);
            neighborCoord[ 7] = new Coord(coord.x-1, coord.y+1);
            break;
         case 50:
            neighborCoord[ 0] = new Coord(coord.x-2, coord.y  );
            neighborCoord[ 1] = new Coord(coord.x-2, coord.y-1);
            neighborCoord[ 2] = new Coord(coord.x-1, coord.y-1);
            neighborCoord[ 3] = new Coord(coord.x  , coord.y-1);
            neighborCoord[ 4] = new Coord(coord.x+1, coord.y  );
            neighborCoord[ 5] = new Coord(coord.x+1, coord.y+1);
            neighborCoord[ 6] = new Coord(coord.x+1, coord.y+2);
            neighborCoord[ 7] = new Coord(coord.x  , coord.y+1);
            neighborCoord[ 8] = new Coord(coord.x-1, coord.y+1);
            neighborCoord[ 9] = new Coord(coord.x-1, coord.y  );
            break;
         case 51:
            neighborCoord[ 0] = new Coord(coord.x-1, coord.y  );
            neighborCoord[ 1] = new Coord(coord.x-1, coord.y-1);
            neighborCoord[ 2] = new Coord(coord.x  , coord.y-1);
            neighborCoord[ 3] = new Coord(coord.x+1, coord.y-1);
            neighborCoord[ 4] = new Coord(coord.x+2, coord.y-1);
            neighborCoord[ 5] = new Coord(coord.x+1, coord.y  );
            neighborCoord[ 6] = new Coord(coord.x  , coord.y+1);
            neighborCoord[ 7] = new Coord(coord.x  , coord.y+2);
            neighborCoord[ 8] = new Coord(coord.x-1, coord.y+1);
            neighborCoord[ 9] = new Coord(coord.x-2, coord.y+1);
            break;
         case 52:
            neighborCoord[ 0] = new Coord(coord.x-1, coord.y-1);
            neighborCoord[ 1] = new Coord(coord.x  , coord.y-1);
            neighborCoord[ 2] = new Coord(coord.x+1, coord.y-1);
            neighborCoord[ 3] = new Coord(coord.x+1, coord.y  );
            neighborCoord[ 4] = new Coord(coord.x+1, coord.y+1);
            neighborCoord[ 5] = new Coord(coord.x  , coord.y+2);
            neighborCoord[ 6] = new Coord(coord.x  , coord.y+1);
            neighborCoord[ 7] = new Coord(coord.x-1, coord.y+1);
            neighborCoord[ 8] = new Coord(coord.x-1, coord.y  );
            break;
         case 53:
            neighborCoord[ 0] = new Coord(coord.x  , coord.y-1);
            neighborCoord[ 1] = new Coord(coord.x-1, coord.y-2);
            neighborCoord[ 2] = new Coord(coord.x  , coord.y-2);
            neighborCoord[ 3] = new Coord(coord.x+1, coord.y-2);
            neighborCoord[ 4] = new Coord(coord.x+1, coord.y-1);
            neighborCoord[ 5] = new Coord(coord.x+1, coord.y  );
            neighborCoord[ 6] = new Coord(coord.x  , coord.y+1);
            neighborCoord[ 7] = new Coord(coord.x-1, coord.y+2);
            neighborCoord[ 8] = new Coord(coord.x-1, coord.y+1);
            neighborCoord[ 9] = new Coord(coord.x-2, coord.y+1);
            neighborCoord[10] = new Coord(coord.x-1, coord.y  );
            break;
         case 54:
            neighborCoord[ 0] = new Coord(coord.x-1, coord.y  );
            neighborCoord[ 1] = new Coord(coord.x  , coord.y-1);
            neighborCoord[ 2] = new Coord(coord.x+1, coord.y-1);
            neighborCoord[ 3] = new Coord(coord.x+1, coord.y  );
            neighborCoord[ 4] = new Coord(coord.x+1, coord.y+1);
            neighborCoord[ 5] = new Coord(coord.x+2, coord.y+1);
            neighborCoord[ 6] = new Coord(coord.x+1, coord.y+2);
            neighborCoord[ 7] = new Coord(coord.x  , coord.y+1);
            neighborCoord[ 8] = new Coord(coord.x-1, coord.y+1);
            break;
         case 55:
            neighborCoord[ 0] = new Coord(coord.x-2, coord.y  );
            neighborCoord[ 1] = new Coord(coord.x-1, coord.y-1);
            neighborCoord[ 2] = new Coord(coord.x  , coord.y-1);
            neighborCoord[ 3] = new Coord(coord.x+1, coord.y-1);
            neighborCoord[ 4] = new Coord(coord.x+1, coord.y-2);
            neighborCoord[ 5] = new Coord(coord.x+2, coord.y-1);
            neighborCoord[ 6] = new Coord(coord.x+2, coord.y  );
            neighborCoord[ 7] = new Coord(coord.x+1, coord.y  );
            neighborCoord[ 8] = new Coord(coord.x  , coord.y+1);
            neighborCoord[ 9] = new Coord(coord.x-1, coord.y  );
            neighborCoord[10] = new Coord(coord.x-2, coord.y+1);
            break;
         case 56:
            neighborCoord[ 0] = new Coord(coord.x-1, coord.y  );
            neighborCoord[ 1] = new Coord(coord.x  , coord.y-1);
            neighborCoord[ 2] = new Coord(coord.x  , coord.y-2);
            neighborCoord[ 3] = new Coord(coord.x+1, coord.y-1);
            neighborCoord[ 4] = new Coord(coord.x+1, coord.y  );
            neighborCoord[ 5] = new Coord(coord.x+1, coord.y+1);
            neighborCoord[ 6] = new Coord(coord.x+1, coord.y+2);
            neighborCoord[ 7] = new Coord(coord.x+1, coord.y+3);
            neighborCoord[ 8] = new Coord(coord.x  , coord.y+2);
            neighborCoord[ 9] = new Coord(coord.x  , coord.y+1);
            neighborCoord[10] = new Coord(coord.x-1, coord.y+1);
            break;
         case 57:
            neighborCoord[ 0] = new Coord(coord.x-2, coord.y  );
            neighborCoord[ 1] = new Coord(coord.x-1, coord.y-1);
            neighborCoord[ 2] = new Coord(coord.x-1, coord.y-2);
            neighborCoord[ 3] = new Coord(coord.x  , coord.y-1);
            neighborCoord[ 4] = new Coord(coord.x+1, coord.y-1);
            neighborCoord[ 5] = new Coord(coord.x+1, coord.y  );
            neighborCoord[ 6] = new Coord(coord.x+1, coord.y+1);
            neighborCoord[ 7] = new Coord(coord.x  , coord.y+1);
            neighborCoord[ 8] = new Coord(coord.x-1, coord.y  );
            neighborCoord[ 9] = new Coord(coord.x-2, coord.y+1);
            break;
         case 58:
            neighborCoord[ 0] = new Coord(coord.x-1, coord.y  );
            neighborCoord[ 1] = new Coord(coord.x  , coord.y-1);
            neighborCoord[ 2] = new Coord(coord.x+1, coord.y-1);
            neighborCoord[ 3] = new Coord(coord.x+1, coord.y-2);
            neighborCoord[ 4] = new Coord(coord.x+2, coord.y-1);
            neighborCoord[ 5] = new Coord(coord.x+2, coord.y  );
            neighborCoord[ 6] = new Coord(coord.x+2, coord.y+1);
            neighborCoord[ 7] = new Coord(coord.x+1, coord.y  );
            neighborCoord[ 8] = new Coord(coord.x  , coord.y+1);
            neighborCoord[ 9] = new Coord(coord.x-1, coord.y+1);
            break;
         case 59:
            neighborCoord[ 0] = new Coord(coord.x-1, coord.y  );
            neighborCoord[ 1] = new Coord(coord.x  , coord.y-1);
            neighborCoord[ 2] = new Coord(coord.x  , coord.y-2);
            neighborCoord[ 3] = new Coord(coord.x+1, coord.y-1);
            neighborCoord[ 4] = new Coord(coord.x+1, coord.y  );
            neighborCoord[ 5] = new Coord(coord.x+1, coord.y+1);
            neighborCoord[ 6] = new Coord(coord.x  , coord.y+1);
            neighborCoord[ 7] = new Coord(coord.x  , coord.y+2);
            neighborCoord[ 8] = new Coord(coord.x-1, coord.y+2);
            neighborCoord[ 9] = new Coord(coord.x-2, coord.y+2);
            neighborCoord[10] = new Coord(coord.x-1, coord.y+1);
            break;
         case 60:
            neighborCoord[ 0] = new Coord(coord.x-1, coord.y-1);
            neighborCoord[ 1] = new Coord(coord.x-1, coord.y-2);
            neighborCoord[ 2] = new Coord(coord.x  , coord.y-1);
            neighborCoord[ 3] = new Coord(coord.x+1, coord.y-1);
            neighborCoord[ 4] = new Coord(coord.x+2, coord.y-1);
            neighborCoord[ 5] = new Coord(coord.x+2, coord.y  );
            neighborCoord[ 6] = new Coord(coord.x+1, coord.y+1);
            neighborCoord[ 7] = new Coord(coord.x+1, coord.y  );
            neighborCoord[ 8] = new Coord(coord.x  , coord.y+1);
            neighborCoord[ 9] = new Coord(coord.x-1, coord.y  );
            neighborCoord[10] = new Coord(coord.x-2, coord.y  );
            break;
         case 61:
            neighborCoord[ 0] = new Coord(coord.x-1, coord.y  );
            neighborCoord[ 1] = new Coord(coord.x  , coord.y-1);
            neighborCoord[ 2] = new Coord(coord.x+1, coord.y-1);
            neighborCoord[ 3] = new Coord(coord.x+1, coord.y  );
            neighborCoord[ 4] = new Coord(coord.x  , coord.y+1);
            neighborCoord[ 5] = new Coord(coord.x  , coord.y+2);
            neighborCoord[ 6] = new Coord(coord.x-1, coord.y+2);
            neighborCoord[ 7] = new Coord(coord.x-2, coord.y+1);
            neighborCoord[ 8] = new Coord(coord.x-1, coord.y+1);
            break;
         case 62:
            neighborCoord[ 0] = new Coord(coord.x-2, coord.y  );
            neighborCoord[ 1] = new Coord(coord.x-1, coord.y-1);
            neighborCoord[ 2] = new Coord(coord.x  , coord.y-1);
            neighborCoord[ 3] = new Coord(coord.x+1, coord.y-1);
            neighborCoord[ 4] = new Coord(coord.x+2, coord.y-1);
            neighborCoord[ 5] = new Coord(coord.x+2, coord.y  );
            neighborCoord[ 6] = new Coord(coord.x+1, coord.y  );
            neighborCoord[ 7] = new Coord(coord.x  , coord.y+1);
            neighborCoord[ 8] = new Coord(coord.x-1, coord.y+1);
            neighborCoord[ 9] = new Coord(coord.x-1, coord.y  );
            break;
         case 63:
            neighborCoord[ 0] = new Coord(coord.x-2, coord.y+1);
            neighborCoord[ 1] = new Coord(coord.x-2, coord.y  );
            neighborCoord[ 2] = new Coord(coord.x-1, coord.y  );
            neighborCoord[ 3] = new Coord(coord.x  , coord.y-1);
            neighborCoord[ 4] = new Coord(coord.x+1, coord.y  );
            neighborCoord[ 5] = new Coord(coord.x+2, coord.y  );
            neighborCoord[ 6] = new Coord(coord.x+1, coord.y+1);
            neighborCoord[ 7] = new Coord(coord.x+1, coord.y+2);
            neighborCoord[ 8] = new Coord(coord.x  , coord.y+1);
            neighborCoord[ 9] = new Coord(coord.x-1, coord.y+1);
            break;
         case 64:
            neighborCoord[ 0] = new Coord(coord.x  , coord.y-1);
            neighborCoord[ 1] = new Coord(coord.x+1, coord.y-2);
            neighborCoord[ 2] = new Coord(coord.x+1, coord.y-3);
            neighborCoord[ 3] = new Coord(coord.x+2, coord.y-2);
            neighborCoord[ 4] = new Coord(coord.x+2, coord.y-1);
            neighborCoord[ 5] = new Coord(coord.x+1, coord.y-1);
            neighborCoord[ 6] = new Coord(coord.x+1, coord.y  );
            neighborCoord[ 7] = new Coord(coord.x  , coord.y+1);
            neighborCoord[ 8] = new Coord(coord.x-1, coord.y  );
            neighborCoord[ 9] = new Coord(coord.x-1, coord.y-1);
            break;
         case 65:
            neighborCoord[ 0] = new Coord(coord.x-2, coord.y  );
            neighborCoord[ 1] = new Coord(coord.x-2, coord.y-1);
            neighborCoord[ 2] = new Coord(coord.x-1, coord.y  );
            neighborCoord[ 3] = new Coord(coord.x  , coord.y-1);
            neighborCoord[ 4] = new Coord(coord.x+1, coord.y  );
            neighborCoord[ 5] = new Coord(coord.x+1, coord.y+1);
            neighborCoord[ 6] = new Coord(coord.x+1, coord.y+2);
            neighborCoord[ 7] = new Coord(coord.x  , coord.y+1);
            neighborCoord[ 8] = new Coord(coord.x-1, coord.y+1);
            break;
         case 66:
            neighborCoord[ 0] = new Coord(coord.x  , coord.y-1);
            neighborCoord[ 1] = new Coord(coord.x+1, coord.y-2);
            neighborCoord[ 2] = new Coord(coord.x+1, coord.y-1);
            neighborCoord[ 3] = new Coord(coord.x+1, coord.y  );
            neighborCoord[ 4] = new Coord(coord.x  , coord.y+1);
            neighborCoord[ 5] = new Coord(coord.x  , coord.y+2);
            neighborCoord[ 6] = new Coord(coord.x-1, coord.y+1);
            neighborCoord[ 7] = new Coord(coord.x-1, coord.y  );
            neighborCoord[ 8] = new Coord(coord.x-1, coord.y-1);
            break;
         case 67:
            neighborCoord[ 0] = new Coord(coord.x-1, coord.y-1);
            neighborCoord[ 1] = new Coord(coord.x  , coord.y-2);
            neighborCoord[ 2] = new Coord(coord.x  , coord.y-1);
            neighborCoord[ 3] = new Coord(coord.x+1, coord.y-1);
            neighborCoord[ 4] = new Coord(coord.x+1, coord.y  );
            neighborCoord[ 5] = new Coord(coord.x+1, coord.y+1);
            neighborCoord[ 6] = new Coord(coord.x  , coord.y+1);
            neighborCoord[ 7] = new Coord(coord.x-1, coord.y+1);
            neighborCoord[ 8] = new Coord(coord.x-1, coord.y  );
            break;
         case 68:
            neighborCoord[ 0] = new Coord(coord.x-2, coord.y+1);
            neighborCoord[ 1] = new Coord(coord.x-1, coord.y  );
            neighborCoord[ 2] = new Coord(coord.x  , coord.y-1);
            neighborCoord[ 3] = new Coord(coord.x+1, coord.y  );
            neighborCoord[ 4] = new Coord(coord.x+2, coord.y-1);
            neighborCoord[ 5] = new Coord(coord.x+2, coord.y+1);
            neighborCoord[ 6] = new Coord(coord.x+1, coord.y+1);
            neighborCoord[ 7] = new Coord(coord.x  , coord.y+1);
            neighborCoord[ 8] = new Coord(coord.x-1, coord.y+1);
            break;
         case 69:
            neighborCoord[ 0] = new Coord(coord.x-2, coord.y-1);
            neighborCoord[ 1] = new Coord(coord.x-1, coord.y-2);
            neighborCoord[ 2] = new Coord(coord.x-1, coord.y-3);
            neighborCoord[ 3] = new Coord(coord.x  , coord.y-2);
            neighborCoord[ 4] = new Coord(coord.x  , coord.y-1);
            neighborCoord[ 5] = new Coord(coord.x+1, coord.y-1);
            neighborCoord[ 6] = new Coord(coord.x+1, coord.y+1);
            neighborCoord[ 7] = new Coord(coord.x  , coord.y+1);
            neighborCoord[ 8] = new Coord(coord.x-1, coord.y  );
            neighborCoord[ 9] = new Coord(coord.x-1, coord.y-1);
            break;
         case 70:
            neighborCoord[ 0] = new Coord(coord.x-1, coord.y-1);
            neighborCoord[ 1] = new Coord(coord.x  , coord.y-2);
            neighborCoord[ 2] = new Coord(coord.x+1, coord.y-2);
            neighborCoord[ 3] = new Coord(coord.x+1, coord.y-1);
            neighborCoord[ 4] = new Coord(coord.x+1, coord.y  );
            neighborCoord[ 5] = new Coord(coord.x+2, coord.y  );
            neighborCoord[ 6] = new Coord(coord.x+1, coord.y+1);
            neighborCoord[ 7] = new Coord(coord.x  , coord.y+1);
            neighborCoord[ 8] = new Coord(coord.x  , coord.y-1);
            break;
         case 71:
            neighborCoord[ 0] = new Coord(coord.x  , coord.y-1);
            neighborCoord[ 1] = new Coord(coord.x+1, coord.y-2);
            neighborCoord[ 2] = new Coord(coord.x+2, coord.y-2);
            neighborCoord[ 3] = new Coord(coord.x+2, coord.y-1);
            neighborCoord[ 4] = new Coord(coord.x+1, coord.y-1);
            neighborCoord[ 5] = new Coord(coord.x+1, coord.y  );
            neighborCoord[ 6] = new Coord(coord.x  , coord.y+1);
            neighborCoord[ 7] = new Coord(coord.x-1, coord.y+1);
            neighborCoord[ 8] = new Coord(coord.x-1, coord.y  );
            break;
         case 72:
            neighborCoord[ 0] = new Coord(coord.x-1, coord.y+1);
            neighborCoord[ 1] = new Coord(coord.x-1, coord.y  );
            neighborCoord[ 2] = new Coord(coord.x  , coord.y-1);
            neighborCoord[ 3] = new Coord(coord.x+1, coord.y  );
            neighborCoord[ 4] = new Coord(coord.x+1, coord.y+1);
            neighborCoord[ 5] = new Coord(coord.x  , coord.y+1);
            neighborCoord[ 6] = new Coord(coord.x-1, coord.y+2);
            neighborCoord[ 7] = new Coord(coord.x-2, coord.y+2);
            break;
         case 73:
            neighborCoord[ 0] = new Coord(coord.x-1, coord.y-1);
            neighborCoord[ 1] = new Coord(coord.x-1, coord.y-2);
            neighborCoord[ 2] = new Coord(coord.x  , coord.y-1);
            neighborCoord[ 3] = new Coord(coord.x+1, coord.y-1);
            neighborCoord[ 4] = new Coord(coord.x+1, coord.y  );
            neighborCoord[ 5] = new Coord(coord.x+1, coord.y+1);
            neighborCoord[ 6] = new Coord(coord.x+1, coord.y+2);
            neighborCoord[ 7] = new Coord(coord.x  , coord.y+1);
            neighborCoord[ 8] = new Coord(coord.x-1, coord.y  );
            neighborCoord[ 9] = new Coord(coord.x-2, coord.y  );
            break;
         case 74:
            neighborCoord[ 0] = new Coord(coord.x-1, coord.y  );
            neighborCoord[ 1] = new Coord(coord.x  , coord.y-1);
            neighborCoord[ 2] = new Coord(coord.x  , coord.y-2);
            neighborCoord[ 3] = new Coord(coord.x+1, coord.y-1);
            neighborCoord[ 4] = new Coord(coord.x+1, coord.y  );
            neighborCoord[ 5] = new Coord(coord.x+1, coord.y+1);
            neighborCoord[ 6] = new Coord(coord.x  , coord.y+1);
            neighborCoord[ 7] = new Coord(coord.x  , coord.y+2);
            neighborCoord[ 8] = new Coord(coord.x-1, coord.y+1);
            break;
         case 75:
            neighborCoord[ 0] = new Coord(coord.x-1, coord.y-1);
            neighborCoord[ 1] = new Coord(coord.x-1, coord.y-2);
            neighborCoord[ 2] = new Coord(coord.x  , coord.y-1);
            neighborCoord[ 3] = new Coord(coord.x+1, coord.y-1);
            neighborCoord[ 4] = new Coord(coord.x+2, coord.y-2);
            neighborCoord[ 5] = new Coord(coord.x+2, coord.y-1);
            neighborCoord[ 6] = new Coord(coord.x+2, coord.y  );
            neighborCoord[ 7] = new Coord(coord.x+1, coord.y  );
            neighborCoord[ 8] = new Coord(coord.x+1, coord.y+1);
            neighborCoord[ 9] = new Coord(coord.x  , coord.y+1);
            neighborCoord[10] = new Coord(coord.x-1, coord.y  );
            break;
         case 76:
            neighborCoord[ 0] = new Coord(coord.x-1, coord.y  );
            neighborCoord[ 1] = new Coord(coord.x  , coord.y-1);
            neighborCoord[ 2] = new Coord(coord.x+1, coord.y-2);
            neighborCoord[ 3] = new Coord(coord.x+1, coord.y-1);
            neighborCoord[ 4] = new Coord(coord.x+1, coord.y  );
            neighborCoord[ 5] = new Coord(coord.x+2, coord.y+1);
            neighborCoord[ 6] = new Coord(coord.x+1, coord.y+1);
            neighborCoord[ 7] = new Coord(coord.x+1, coord.y+2);
            neighborCoord[ 8] = new Coord(coord.x  , coord.y+1);
            neighborCoord[ 9] = new Coord(coord.x-1, coord.y+1);
            break;
         case 77:
            neighborCoord[ 0] = new Coord(coord.x-2, coord.y  );
            neighborCoord[ 1] = new Coord(coord.x-1, coord.y-1);
            neighborCoord[ 2] = new Coord(coord.x  , coord.y-2);
            neighborCoord[ 3] = new Coord(coord.x  , coord.y-1);
            neighborCoord[ 4] = new Coord(coord.x+1, coord.y  );
            neighborCoord[ 5] = new Coord(coord.x+2, coord.y+1);
            neighborCoord[ 6] = new Coord(coord.x+1, coord.y+2);
            neighborCoord[ 7] = new Coord(coord.x+1, coord.y+1);
            neighborCoord[ 8] = new Coord(coord.x-1, coord.y  );
            break;
         case 78:
            neighborCoord[ 0] = new Coord(coord.x-1, coord.y-1);
            neighborCoord[ 1] = new Coord(coord.x  , coord.y-1);
            neighborCoord[ 2] = new Coord(coord.x+1, coord.y-2);
            neighborCoord[ 3] = new Coord(coord.x+1, coord.y  );
            neighborCoord[ 4] = new Coord(coord.x+2, coord.y  );
            neighborCoord[ 5] = new Coord(coord.x+2, coord.y+1);
            neighborCoord[ 6] = new Coord(coord.x+1, coord.y+1);
            neighborCoord[ 7] = new Coord(coord.x  , coord.y+2);
            neighborCoord[ 8] = new Coord(coord.x  , coord.y+1);
            neighborCoord[ 9] = new Coord(coord.x-1, coord.y  );
            break;
         case 79:
            neighborCoord[ 0] = new Coord(coord.x-2, coord.y-1);
            neighborCoord[ 1] = new Coord(coord.x-1, coord.y-1);
            neighborCoord[ 2] = new Coord(coord.x  , coord.y-2);
            neighborCoord[ 3] = new Coord(coord.x  , coord.y-1);
            neighborCoord[ 4] = new Coord(coord.x+1, coord.y-1);
            neighborCoord[ 5] = new Coord(coord.x+2, coord.y-1);
            neighborCoord[ 6] = new Coord(coord.x+1, coord.y  );
            neighborCoord[ 7] = new Coord(coord.x+1, coord.y+1);
            neighborCoord[ 8] = new Coord(coord.x  , coord.y+1);
            neighborCoord[ 9] = new Coord(coord.x-1, coord.y  );
            break;
         case 80:
            neighborCoord[ 0] = new Coord(coord.x-1, coord.y  );
            neighborCoord[ 1] = new Coord(coord.x-1, coord.y-1);
            neighborCoord[ 2] = new Coord(coord.x  , coord.y-1);
            neighborCoord[ 3] = new Coord(coord.x+1, coord.y-1);
            neighborCoord[ 4] = new Coord(coord.x+2, coord.y  );
            neighborCoord[ 5] = new Coord(coord.x+2, coord.y+1);
            neighborCoord[ 6] = new Coord(coord.x+1, coord.y  );
            neighborCoord[ 7] = new Coord(coord.x  , coord.y+1);
            neighborCoord[ 8] = new Coord(coord.x-1, coord.y+1);
            neighborCoord[ 9] = new Coord(coord.x-2, coord.y  );
            break;
         case 81:
            neighborCoord[ 0] = new Coord(coord.x-2, coord.y+1);
            neighborCoord[ 1] = new Coord(coord.x-1, coord.y  );
            neighborCoord[ 2] = new Coord(coord.x  , coord.y-1);
            neighborCoord[ 3] = new Coord(coord.x+1, coord.y  );
            neighborCoord[ 4] = new Coord(coord.x+2, coord.y+1);
            neighborCoord[ 5] = new Coord(coord.x+1, coord.y+1);
            neighborCoord[ 6] = new Coord(coord.x  , coord.y+1);
            neighborCoord[ 7] = new Coord(coord.x-1, coord.y+1);
            break;
         case 82:
            neighborCoord[ 0] = new Coord(coord.x-1, coord.y-1);
            neighborCoord[ 1] = new Coord(coord.x-2, coord.y-1);
            neighborCoord[ 2] = new Coord(coord.x-1, coord.y-2);
            neighborCoord[ 3] = new Coord(coord.x  , coord.y-1);
            neighborCoord[ 4] = new Coord(coord.x+1, coord.y-1);
            neighborCoord[ 5] = new Coord(coord.x+1, coord.y  );
            neighborCoord[ 6] = new Coord(coord.x+1, coord.y+1);
            neighborCoord[ 7] = new Coord(coord.x  , coord.y+1);
            neighborCoord[ 8] = new Coord(coord.x-1, coord.y+1);
            neighborCoord[ 9] = new Coord(coord.x-1, coord.y  );
            break;
         case 83:
            neighborCoord[ 0] = new Coord(coord.x-1, coord.y-1);
            neighborCoord[ 1] = new Coord(coord.x  , coord.y-1);
            neighborCoord[ 2] = new Coord(coord.x+1, coord.y  );
            neighborCoord[ 3] = new Coord(coord.x+2, coord.y  );
            neighborCoord[ 4] = new Coord(coord.x+2, coord.y+1);
            neighborCoord[ 5] = new Coord(coord.x+1, coord.y+1);
            neighborCoord[ 6] = new Coord(coord.x  , coord.y+1);
            neighborCoord[ 7] = new Coord(coord.x-1, coord.y  );
            break;
         case 84:
            neighborCoord[ 0] = new Coord(coord.x-1, coord.y-2);
            neighborCoord[ 1] = new Coord(coord.x-1, coord.y-3);
            neighborCoord[ 2] = new Coord(coord.x  , coord.y-2);
            neighborCoord[ 3] = new Coord(coord.x  , coord.y-1);
            neighborCoord[ 4] = new Coord(coord.x+1, coord.y-1);
            neighborCoord[ 5] = new Coord(coord.x+1, coord.y  );
            neighborCoord[ 6] = new Coord(coord.x+1, coord.y+1);
            neighborCoord[ 7] = new Coord(coord.x  , coord.y+1);
            neighborCoord[ 8] = new Coord(coord.x-1, coord.y  );
            neighborCoord[ 9] = new Coord(coord.x-1, coord.y-1);
            break;
         case 85:
            neighborCoord[ 0] = new Coord(coord.x-2, coord.y  );
            neighborCoord[ 1] = new Coord(coord.x-1, coord.y  );
            neighborCoord[ 2] = new Coord(coord.x-1, coord.y-1);
            neighborCoord[ 3] = new Coord(coord.x  , coord.y-1);
            neighborCoord[ 4] = new Coord(coord.x+2, coord.y  );
            neighborCoord[ 5] = new Coord(coord.x+1, coord.y  );
            neighborCoord[ 6] = new Coord(coord.x+1, coord.y+1);
            neighborCoord[ 7] = new Coord(coord.x  , coord.y+1);
            neighborCoord[ 8] = new Coord(coord.x-1, coord.y+1);
            break;
         case 86:
            neighborCoord[ 0] = new Coord(coord.x-1, coord.y  );
            neighborCoord[ 1] = new Coord(coord.x-1, coord.y-1);
            neighborCoord[ 2] = new Coord(coord.x+1, coord.y  );
            neighborCoord[ 3] = new Coord(coord.x+1, coord.y+1);
            neighborCoord[ 4] = new Coord(coord.x+2, coord.y+2);
            neighborCoord[ 5] = new Coord(coord.x+1, coord.y+2);
            neighborCoord[ 6] = new Coord(coord.x  , coord.y+2);
            neighborCoord[ 7] = new Coord(coord.x  , coord.y+1);
            break;
         case 87:
            neighborCoord[ 0] = new Coord(coord.x-2, coord.y  );
            neighborCoord[ 1] = new Coord(coord.x-2, coord.y-1);
            neighborCoord[ 2] = new Coord(coord.x-1, coord.y-1);
            neighborCoord[ 3] = new Coord(coord.x  , coord.y-1);
            neighborCoord[ 4] = new Coord(coord.x+1, coord.y  );
            neighborCoord[ 5] = new Coord(coord.x  , coord.y+1);
            neighborCoord[ 6] = new Coord(coord.x-1, coord.y  );
            neighborCoord[ 7] = new Coord(coord.x-1, coord.y+1);
            break;
         case 88:
            neighborCoord[ 0] = new Coord(coord.x-1, coord.y  );
            neighborCoord[ 1] = new Coord(coord.x-2, coord.y-1);
            neighborCoord[ 2] = new Coord(coord.x-1, coord.y-1);
            neighborCoord[ 3] = new Coord(coord.x  , coord.y-1);
            neighborCoord[ 4] = new Coord(coord.x+1, coord.y-1);
            neighborCoord[ 5] = new Coord(coord.x+1, coord.y  );
            neighborCoord[ 6] = new Coord(coord.x  , coord.y+1);
            neighborCoord[ 7] = new Coord(coord.x+1, coord.y+2);
            neighborCoord[ 8] = new Coord(coord.x  , coord.y+2);
            neighborCoord[ 9] = new Coord(coord.x-1, coord.y+1);
            break;
         case 89:
            neighborCoord[ 0] = new Coord(coord.x-1, coord.y  );
            neighborCoord[ 1] = new Coord(coord.x-2, coord.y-1);
            neighborCoord[ 2] = new Coord(coord.x-1, coord.y-1);
            neighborCoord[ 3] = new Coord(coord.x  , coord.y-1);
            neighborCoord[ 4] = new Coord(coord.x+1, coord.y-1);
            neighborCoord[ 5] = new Coord(coord.x+1, coord.y  );
            neighborCoord[ 6] = new Coord(coord.x  , coord.y+1);
            neighborCoord[ 7] = new Coord(coord.x-1, coord.y+1);
            break;
         case 90:
            neighborCoord[ 0] = new Coord(coord.x-1, coord.y+1);
            neighborCoord[ 1] = new Coord(coord.x-1, coord.y  );
            neighborCoord[ 2] = new Coord(coord.x  , coord.y-1);
            neighborCoord[ 3] = new Coord(coord.x+1, coord.y-1);
            neighborCoord[ 4] = new Coord(coord.x+2, coord.y  );
            neighborCoord[ 5] = new Coord(coord.x+1, coord.y  );
            neighborCoord[ 6] = new Coord(coord.x+1, coord.y+1);
            neighborCoord[ 7] = new Coord(coord.x  , coord.y+2);
            neighborCoord[ 8] = new Coord(coord.x  , coord.y+1);
            neighborCoord[ 9] = new Coord(coord.x-1, coord.y+2);
            break;
         case 91:
            neighborCoord[ 0] = new Coord(coord.x-1, coord.y  );
            neighborCoord[ 1] = new Coord(coord.x-1, coord.y-1);
            neighborCoord[ 2] = new Coord(coord.x  , coord.y-1);
            neighborCoord[ 3] = new Coord(coord.x+1, coord.y  );
            neighborCoord[ 4] = new Coord(coord.x+2, coord.y  );
            neighborCoord[ 5] = new Coord(coord.x+2, coord.y+1);
            neighborCoord[ 6] = new Coord(coord.x+1, coord.y+1);
            neighborCoord[ 7] = new Coord(coord.x  , coord.y+1);
            neighborCoord[ 8] = new Coord(coord.x-1, coord.y+2);
            neighborCoord[ 9] = new Coord(coord.x-1, coord.y+1);
            break;
         case 92:
            neighborCoord[ 0] = new Coord(coord.x-2, coord.y-1);
            neighborCoord[ 1] = new Coord(coord.x-1, coord.y-1);
            neighborCoord[ 2] = new Coord(coord.x-1, coord.y-2);
            neighborCoord[ 3] = new Coord(coord.x  , coord.y-2);
            neighborCoord[ 4] = new Coord(coord.x  , coord.y-1);
            neighborCoord[ 5] = new Coord(coord.x+1, coord.y  );
            neighborCoord[ 6] = new Coord(coord.x+1, coord.y+1);
            neighborCoord[ 7] = new Coord(coord.x  , coord.y+1);
            neighborCoord[ 8] = new Coord(coord.x-1, coord.y  );
            neighborCoord[ 9] = new Coord(coord.x-2, coord.y  );
            break;
         case 93:
            neighborCoord[ 0] = new Coord(coord.x-1, coord.y  );
            neighborCoord[ 1] = new Coord(coord.x-1, coord.y-1);
            neighborCoord[ 2] = new Coord(coord.x  , coord.y-1);
            neighborCoord[ 3] = new Coord(coord.x+1, coord.y-1);
            neighborCoord[ 4] = new Coord(coord.x+1, coord.y  );
            neighborCoord[ 5] = new Coord(coord.x  , coord.y+1);
            neighborCoord[ 6] = new Coord(coord.x-1, coord.y+1);
            neighborCoord[ 7] = new Coord(coord.x-2, coord.y  );
            break;
         case 94:
            neighborCoord[ 0] = new Coord(coord.x-2, coord.y-1);
            neighborCoord[ 1] = new Coord(coord.x-1, coord.y-1);
            neighborCoord[ 2] = new Coord(coord.x  , coord.y-1);
            neighborCoord[ 3] = new Coord(coord.x+1, coord.y  );
            neighborCoord[ 4] = new Coord(coord.x+1, coord.y+1);
            neighborCoord[ 5] = new Coord(coord.x  , coord.y+1);
            neighborCoord[ 6] = new Coord(coord.x-1, coord.y+1);
            neighborCoord[ 7] = new Coord(coord.x-1, coord.y  );
            break;
         case 95:
            neighborCoord[ 0] = new Coord(coord.x-1, coord.y-1);
            neighborCoord[ 1] = new Coord(coord.x-1, coord.y-2);
            neighborCoord[ 2] = new Coord(coord.x+1, coord.y-1);
            neighborCoord[ 3] = new Coord(coord.x  , coord.y-1);
            neighborCoord[ 4] = new Coord(coord.x  , coord.y+1);
            neighborCoord[ 5] = new Coord(coord.x-1, coord.y+1);
            neighborCoord[ 6] = new Coord(coord.x-2, coord.y+1);
            neighborCoord[ 7] = new Coord(coord.x-1, coord.y  );
            break;
         case 96:
            neighborCoord[ 0] = new Coord(coord.x  , coord.y-1);
            neighborCoord[ 1] = new Coord(coord.x-1, coord.y-2);
            neighborCoord[ 2] = new Coord(coord.x  , coord.y-2);
            neighborCoord[ 3] = new Coord(coord.x+1, coord.y-1);
            neighborCoord[ 4] = new Coord(coord.x+1, coord.y  );
            neighborCoord[ 5] = new Coord(coord.x+2, coord.y+1);
            neighborCoord[ 6] = new Coord(coord.x+1, coord.y+1);
            neighborCoord[ 7] = new Coord(coord.x  , coord.y+1);
            neighborCoord[ 8] = new Coord(coord.x-1, coord.y+1);
            neighborCoord[ 9] = new Coord(coord.x-1, coord.y-1);
            break;
         case 97:
            neighborCoord[ 0] = new Coord(coord.x-1, coord.y  );
            neighborCoord[ 1] = new Coord(coord.x  , coord.y-1);
            neighborCoord[ 2] = new Coord(coord.x+1, coord.y-1);
            neighborCoord[ 3] = new Coord(coord.x+2, coord.y-2);
            neighborCoord[ 4] = new Coord(coord.x+2, coord.y-1);
            neighborCoord[ 5] = new Coord(coord.x+1, coord.y  );
            neighborCoord[ 6] = new Coord(coord.x+1, coord.y+1);
            neighborCoord[ 7] = new Coord(coord.x  , coord.y+1);
            break;
         case 98:
            neighborCoord[ 0] = new Coord(coord.x-1, coord.y  );
            neighborCoord[ 1] = new Coord(coord.x  , coord.y-1);
            neighborCoord[ 2] = new Coord(coord.x+1, coord.y-2);
            neighborCoord[ 3] = new Coord(coord.x+1, coord.y-1);
            neighborCoord[ 4] = new Coord(coord.x+1, coord.y  );
            neighborCoord[ 5] = new Coord(coord.x+1, coord.y+1);
            neighborCoord[ 6] = new Coord(coord.x  , coord.y+2);
            neighborCoord[ 7] = new Coord(coord.x  , coord.y+1);
            break;
         case 99:
            neighborCoord[ 0] = new Coord(coord.x-1, coord.y+1);
            neighborCoord[ 1] = new Coord(coord.x-1, coord.y  );
            neighborCoord[ 2] = new Coord(coord.x-1, coord.y-1);
            neighborCoord[ 3] = new Coord(coord.x  , coord.y-1);
            neighborCoord[ 4] = new Coord(coord.x+1, coord.y-1);
            neighborCoord[ 5] = new Coord(coord.x+1, coord.y  );
            neighborCoord[ 6] = new Coord(coord.x  , coord.y+1);
            neighborCoord[ 7] = new Coord(coord.x  , coord.y+2);
            neighborCoord[ 8] = new Coord(coord.x-1, coord.y+2);
            neighborCoord[ 9] = new Coord(coord.x-2, coord.y+2);
            break;
         case 100:
            neighborCoord[ 0] = new Coord(coord.x-1, coord.y  );
            neighborCoord[ 1] = new Coord(coord.x-1, coord.y-1);
            neighborCoord[ 2] = new Coord(coord.x  , coord.y-1);
            neighborCoord[ 3] = new Coord(coord.x+1, coord.y  );
            neighborCoord[ 4] = new Coord(coord.x+1, coord.y+1);
            neighborCoord[ 5] = new Coord(coord.x  , coord.y+1);
            neighborCoord[ 6] = new Coord(coord.x-1, coord.y+1);
            break;
         case 101:
            neighborCoord[ 0] = new Coord(coord.x-1, coord.y-1);
            neighborCoord[ 1] = new Coord(coord.x  , coord.y-1);
            neighborCoord[ 2] = new Coord(coord.x+1, coord.y-1);
            neighborCoord[ 3] = new Coord(coord.x+1, coord.y  );
            neighborCoord[ 4] = new Coord(coord.x+2, coord.y  );
            neighborCoord[ 5] = new Coord(coord.x+1, coord.y+1);
            neighborCoord[ 6] = new Coord(coord.x  , coord.y+1);
            neighborCoord[ 7] = new Coord(coord.x-1, coord.y+1);
            neighborCoord[ 8] = new Coord(coord.x-1, coord.y  );
            break;
         case 102:
            neighborCoord[ 0] = new Coord(coord.x-2, coord.y-1);
            neighborCoord[ 1] = new Coord(coord.x-1, coord.y-1);
            neighborCoord[ 2] = new Coord(coord.x  , coord.y-1);
            neighborCoord[ 3] = new Coord(coord.x+1, coord.y-1);
            neighborCoord[ 4] = new Coord(coord.x+2, coord.y-1);
            neighborCoord[ 5] = new Coord(coord.x+2, coord.y  );
            neighborCoord[ 6] = new Coord(coord.x+1, coord.y  );
            neighborCoord[ 7] = new Coord(coord.x  , coord.y+1);
            neighborCoord[ 8] = new Coord(coord.x-1, coord.y+1);
            neighborCoord[ 9] = new Coord(coord.x-1, coord.y  );
            break;
         case 103:
            neighborCoord[ 0] = new Coord(coord.x-2, coord.y  );
            neighborCoord[ 1] = new Coord(coord.x-1, coord.y  );
            neighborCoord[ 2] = new Coord(coord.x  , coord.y-1);
            neighborCoord[ 3] = new Coord(coord.x+1, coord.y-1);
            neighborCoord[ 4] = new Coord(coord.x+1, coord.y  );
            neighborCoord[ 5] = new Coord(coord.x+2, coord.y  );
            neighborCoord[ 6] = new Coord(coord.x+1, coord.y+1);
            neighborCoord[ 7] = new Coord(coord.x  , coord.y+1);
            neighborCoord[ 8] = new Coord(coord.x-1, coord.y+1);
            neighborCoord[ 9] = new Coord(coord.x-2, coord.y+1);
            break;
         case 104:
            neighborCoord[ 0] = new Coord(coord.x-2, coord.y  );
            neighborCoord[ 1] = new Coord(coord.x-1, coord.y-1);
            neighborCoord[ 2] = new Coord(coord.x  , coord.y-1);
            neighborCoord[ 3] = new Coord(coord.x  , coord.y-2);
            neighborCoord[ 4] = new Coord(coord.x+1, coord.y-1);
            neighborCoord[ 5] = new Coord(coord.x+2, coord.y  );
            neighborCoord[ 6] = new Coord(coord.x+1, coord.y  );
            neighborCoord[ 7] = new Coord(coord.x  , coord.y+1);
            neighborCoord[ 8] = new Coord(coord.x-1, coord.y+1);
            neighborCoord[ 9] = new Coord(coord.x-1, coord.y  );
            break;
         case 105:
            neighborCoord[ 0] = new Coord(coord.x-1, coord.y  );
            neighborCoord[ 1] = new Coord(coord.x-1, coord.y-2);
            neighborCoord[ 2] = new Coord(coord.x  , coord.y-1);
            neighborCoord[ 3] = new Coord(coord.x+1, coord.y  );
            neighborCoord[ 4] = new Coord(coord.x  , coord.y+1);
            neighborCoord[ 5] = new Coord(coord.x-1, coord.y+2);
            neighborCoord[ 6] = new Coord(coord.x-1, coord.y+1);
            neighborCoord[ 7] = new Coord(coord.x-2, coord.y+1);
            neighborCoord[ 8] = new Coord(coord.x-2, coord.y  );
            break;
         case 106:
            neighborCoord[ 0] = new Coord(coord.x-2, coord.y  );
            neighborCoord[ 1] = new Coord(coord.x-2, coord.y-2);
            neighborCoord[ 2] = new Coord(coord.x-1, coord.y-1);
            neighborCoord[ 3] = new Coord(coord.x  , coord.y-2);
            neighborCoord[ 4] = new Coord(coord.x  , coord.y-1);
            neighborCoord[ 5] = new Coord(coord.x+1, coord.y  );
            neighborCoord[ 6] = new Coord(coord.x+1, coord.y+1);
            neighborCoord[ 7] = new Coord(coord.x  , coord.y+1);
            neighborCoord[ 8] = new Coord(coord.x-1, coord.y+1);
            neighborCoord[ 9] = new Coord(coord.x-1, coord.y  );
            break;
         case 107:
            neighborCoord[ 0] = new Coord(coord.x-2, coord.y-1);
            neighborCoord[ 1] = new Coord(coord.x-1, coord.y-2);
            neighborCoord[ 2] = new Coord(coord.x-1, coord.y-1);
            neighborCoord[ 3] = new Coord(coord.x  , coord.y-1);
            neighborCoord[ 4] = new Coord(coord.x+1, coord.y-1);
            neighborCoord[ 5] = new Coord(coord.x+1, coord.y  );
            neighborCoord[ 6] = new Coord(coord.x  , coord.y+1);
            neighborCoord[ 7] = new Coord(coord.x-1, coord.y+1);
            neighborCoord[ 8] = new Coord(coord.x-2, coord.y+1);
            neighborCoord[ 9] = new Coord(coord.x-1, coord.y  );
            break;
         case 108:
            neighborCoord[ 0] = new Coord(coord.x  , coord.y-1);
            neighborCoord[ 1] = new Coord(coord.x  , coord.y-2);
            neighborCoord[ 2] = new Coord(coord.x+1, coord.y-2);
            neighborCoord[ 3] = new Coord(coord.x+1, coord.y-1);
            neighborCoord[ 4] = new Coord(coord.x+1, coord.y  );
            neighborCoord[ 5] = new Coord(coord.x+1, coord.y+1);
            neighborCoord[ 6] = new Coord(coord.x  , coord.y+1);
            break;
         case 109:
            neighborCoord[ 0] = new Coord(coord.x-1, coord.y+1);
            neighborCoord[ 1] = new Coord(coord.x-1, coord.y  );
            neighborCoord[ 2] = new Coord(coord.x  , coord.y-1);
            neighborCoord[ 3] = new Coord(coord.x+1, coord.y-1);
            neighborCoord[ 4] = new Coord(coord.x+1, coord.y  );
            neighborCoord[ 5] = new Coord(coord.x+1, coord.y+1);
            neighborCoord[ 6] = new Coord(coord.x+2, coord.y+2);
            neighborCoord[ 7] = new Coord(coord.x+1, coord.y+2);
            neighborCoord[ 8] = new Coord(coord.x  , coord.y+2);
            neighborCoord[ 9] = new Coord(coord.x  , coord.y+1);
            break;
         case 110:
            neighborCoord[ 0] = new Coord(coord.x-1, coord.y  );
            neighborCoord[ 1] = new Coord(coord.x-1, coord.y-1);
            neighborCoord[ 2] = new Coord(coord.x  , coord.y-1);
            neighborCoord[ 3] = new Coord(coord.x+1, coord.y-1);
            neighborCoord[ 4] = new Coord(coord.x+2, coord.y-1);
            neighborCoord[ 5] = new Coord(coord.x+1, coord.y  );
            neighborCoord[ 6] = new Coord(coord.x  , coord.y+1);
            neighborCoord[ 7] = new Coord(coord.x+1, coord.y+2);
            neighborCoord[ 8] = new Coord(coord.x  , coord.y+2);
            neighborCoord[ 9] = new Coord(coord.x-1, coord.y+2);
            neighborCoord[10] = new Coord(coord.x-1, coord.y+1);
            break;
         case 111:
            neighborCoord[ 0] = new Coord(coord.x-1, coord.y  );
            neighborCoord[ 1] = new Coord(coord.x-1, coord.y-1);
            neighborCoord[ 2] = new Coord(coord.x  , coord.y-1);
            neighborCoord[ 3] = new Coord(coord.x+1, coord.y-1);
            neighborCoord[ 4] = new Coord(coord.x+1, coord.y  );
            neighborCoord[ 5] = new Coord(coord.x+1, coord.y+1);
            neighborCoord[ 6] = new Coord(coord.x  , coord.y+1);
            neighborCoord[ 7] = new Coord(coord.x-1, coord.y+1);
            break;
         case 112:
            neighborCoord[ 0] = new Coord(coord.x  , coord.y-1);
            neighborCoord[ 1] = new Coord(coord.x+1, coord.y-1);
            neighborCoord[ 2] = new Coord(coord.x+2, coord.y-1);
            neighborCoord[ 3] = new Coord(coord.x+1, coord.y  );
            neighborCoord[ 4] = new Coord(coord.x  , coord.y+1);
            neighborCoord[ 5] = new Coord(coord.x-1, coord.y+1);
            neighborCoord[ 6] = new Coord(coord.x-2, coord.y+1);
            neighborCoord[ 7] = new Coord(coord.x-1, coord.y  );
            break;
         case 113:
            neighborCoord[ 0] = new Coord(coord.x-1, coord.y  );
            neighborCoord[ 1] = new Coord(coord.x-1, coord.y-1);
            neighborCoord[ 2] = new Coord(coord.x  , coord.y-1);
            neighborCoord[ 3] = new Coord(coord.x+1, coord.y-1);
            neighborCoord[ 4] = new Coord(coord.x+1, coord.y  );
            neighborCoord[ 5] = new Coord(coord.x  , coord.y+1);
            neighborCoord[ 6] = new Coord(coord.x+1, coord.y+1);
            neighborCoord[ 7] = new Coord(coord.x+1, coord.y+2);
            neighborCoord[ 8] = new Coord(coord.x  , coord.y+2);
            neighborCoord[ 9] = new Coord(coord.x-1, coord.y+2);
            neighborCoord[10] = new Coord(coord.x-1, coord.y+1);
            break;
         case 114:
            neighborCoord[ 0] = new Coord(coord.x  , coord.y-1);
            neighborCoord[ 1] = new Coord(coord.x+1, coord.y-1);
            neighborCoord[ 2] = new Coord(coord.x+2, coord.y-1);
            neighborCoord[ 3] = new Coord(coord.x+2, coord.y  );
            neighborCoord[ 4] = new Coord(coord.x+1, coord.y  );
            neighborCoord[ 5] = new Coord(coord.x-1, coord.y+1);
            neighborCoord[ 6] = new Coord(coord.x-1, coord.y  );
            break;
         case 115:
            neighborCoord[ 0] = new Coord(coord.x-1, coord.y  );
            neighborCoord[ 1] = new Coord(coord.x  , coord.y-1);
            neighborCoord[ 2] = new Coord(coord.x+1, coord.y-1);
            neighborCoord[ 3] = new Coord(coord.x+1, coord.y  );
            neighborCoord[ 4] = new Coord(coord.x  , coord.y+1);
            neighborCoord[ 5] = new Coord(coord.x-1, coord.y+1);
            neighborCoord[ 6] = new Coord(coord.x-2, coord.y+1);
            break;
         case 116:
            neighborCoord[ 0] = new Coord(coord.x-2, coord.y  );
            neighborCoord[ 1] = new Coord(coord.x-1, coord.y-1);
            neighborCoord[ 2] = new Coord(coord.x  , coord.y-1);
            neighborCoord[ 3] = new Coord(coord.x  , coord.y-2);
            neighborCoord[ 4] = new Coord(coord.x+1, coord.y-2);
            neighborCoord[ 5] = new Coord(coord.x+1, coord.y-1);
            neighborCoord[ 6] = new Coord(coord.x+1, coord.y+1);
            neighborCoord[ 7] = new Coord(coord.x  , coord.y+1);
            neighborCoord[ 8] = new Coord(coord.x-1, coord.y+1);
            neighborCoord[ 9] = new Coord(coord.x-1, coord.y  );
            break;
         case 117:
            neighborCoord[ 0] = new Coord(coord.x-2, coord.y  );
            neighborCoord[ 1] = new Coord(coord.x-1, coord.y-1);
            neighborCoord[ 2] = new Coord(coord.x  , coord.y-2);
            neighborCoord[ 3] = new Coord(coord.x  , coord.y-1);
            neighborCoord[ 4] = new Coord(coord.x+1, coord.y-1);
            neighborCoord[ 5] = new Coord(coord.x+1, coord.y  );
            neighborCoord[ 6] = new Coord(coord.x  , coord.y+1);
            neighborCoord[ 7] = new Coord(coord.x-1, coord.y  );
            break;
         case 118:
            neighborCoord[ 0] = new Coord(coord.x-2, coord.y  );
            neighborCoord[ 1] = new Coord(coord.x-1, coord.y  );
            neighborCoord[ 2] = new Coord(coord.x-1, coord.y-1);
            neighborCoord[ 3] = new Coord(coord.x  , coord.y-1);
            neighborCoord[ 4] = new Coord(coord.x+1, coord.y-1);
            neighborCoord[ 5] = new Coord(coord.x+1, coord.y  );
            neighborCoord[ 6] = new Coord(coord.x+2, coord.y+1);
            neighborCoord[ 7] = new Coord(coord.x+1, coord.y+1);
            neighborCoord[ 8] = new Coord(coord.x  , coord.y+1);
            neighborCoord[ 9] = new Coord(coord.x-1, coord.y+1);
            break;
         case 119:
            neighborCoord[ 0] = new Coord(coord.x-1, coord.y  );
            neighborCoord[ 1] = new Coord(coord.x-1, coord.y-1);
            neighborCoord[ 2] = new Coord(coord.x  , coord.y-1);
            neighborCoord[ 3] = new Coord(coord.x+1, coord.y-1);
            neighborCoord[ 4] = new Coord(coord.x+2, coord.y-1);
            neighborCoord[ 5] = new Coord(coord.x+2, coord.y  );
            neighborCoord[ 6] = new Coord(coord.x+1, coord.y  );
            neighborCoord[ 7] = new Coord(coord.x+1, coord.y+1);
            neighborCoord[ 8] = new Coord(coord.x  , coord.y+1);
            neighborCoord[ 9] = new Coord(coord.x-1, coord.y+1);
            break;
         case 120:
            neighborCoord[ 0] = new Coord(coord.x-1, coord.y  );
            neighborCoord[ 1] = new Coord(coord.x  , coord.y-1);
            neighborCoord[ 2] = new Coord(coord.x+1, coord.y-1);
            neighborCoord[ 3] = new Coord(coord.x+1, coord.y  );
            neighborCoord[ 4] = new Coord(coord.x+1, coord.y+1);
            neighborCoord[ 5] = new Coord(coord.x+2, coord.y+2);
            neighborCoord[ 6] = new Coord(coord.x+2, coord.y+3);
            neighborCoord[ 7] = new Coord(coord.x+1, coord.y+2);
            neighborCoord[ 8] = new Coord(coord.x  , coord.y+2);
            neighborCoord[ 9] = new Coord(coord.x  , coord.y+1);
            break;
         case 121:
            neighborCoord[ 0] = new Coord(coord.x-2, coord.y  );
            neighborCoord[ 1] = new Coord(coord.x-1, coord.y-1);
            neighborCoord[ 2] = new Coord(coord.x  , coord.y-1);
            neighborCoord[ 3] = new Coord(coord.x+1, coord.y-1);
            neighborCoord[ 4] = new Coord(coord.x+1, coord.y  );
            neighborCoord[ 5] = new Coord(coord.x+2, coord.y  );
            neighborCoord[ 6] = new Coord(coord.x+2, coord.y+1);
            neighborCoord[ 7] = new Coord(coord.x+1, coord.y+1);
            neighborCoord[ 8] = new Coord(coord.x  , coord.y+1);
            neighborCoord[ 9] = new Coord(coord.x-1, coord.y  );
            break;
         case 122:
            neighborCoord[ 0] = new Coord(coord.x+1, coord.y-2);
            neighborCoord[ 1] = new Coord(coord.x+1, coord.y-1);
            neighborCoord[ 2] = new Coord(coord.x+2, coord.y-1);
            neighborCoord[ 3] = new Coord(coord.x+2, coord.y  );
            neighborCoord[ 4] = new Coord(coord.x+1, coord.y  );
            neighborCoord[ 5] = new Coord(coord.x+1, coord.y+1);
            neighborCoord[ 6] = new Coord(coord.x  , coord.y+1);
            neighborCoord[ 7] = new Coord(coord.x-1, coord.y+1);
            neighborCoord[ 8] = new Coord(coord.x-1, coord.y  );
            neighborCoord[ 9] = new Coord(coord.x  , coord.y-1);
            break;
         case 123:
            neighborCoord[ 0] = new Coord(coord.x-2, coord.y  );
            neighborCoord[ 1] = new Coord(coord.x-1, coord.y-1);
            neighborCoord[ 2] = new Coord(coord.x-1, coord.y  );
            neighborCoord[ 3] = new Coord(coord.x+1, coord.y-1);
            neighborCoord[ 4] = new Coord(coord.x+1, coord.y  );
            neighborCoord[ 5] = new Coord(coord.x+2, coord.y  );
            neighborCoord[ 6] = new Coord(coord.x+1, coord.y+1);
            neighborCoord[ 7] = new Coord(coord.x  , coord.y+1);
            neighborCoord[ 8] = new Coord(coord.x-1, coord.y+1);
            neighborCoord[ 9] = new Coord(coord.x-2, coord.y+1);
            break;
         case 124:
            neighborCoord[ 0] = new Coord(coord.x-2, coord.y  );
            neighborCoord[ 1] = new Coord(coord.x  , coord.y-1);
            neighborCoord[ 2] = new Coord(coord.x+1, coord.y-1);
            neighborCoord[ 3] = new Coord(coord.x+2, coord.y-2);
            neighborCoord[ 4] = new Coord(coord.x+2, coord.y  );
            neighborCoord[ 5] = new Coord(coord.x+1, coord.y  );
            neighborCoord[ 6] = new Coord(coord.x  , coord.y+1);
            neighborCoord[ 7] = new Coord(coord.x-1, coord.y  );
            break;
         case 125:
            neighborCoord[ 0] = new Coord(coord.x-1, coord.y  );
            neighborCoord[ 1] = new Coord(coord.x  , coord.y-1);
            neighborCoord[ 2] = new Coord(coord.x+1, coord.y-2);
            neighborCoord[ 3] = new Coord(coord.x+1, coord.y  );
            neighborCoord[ 4] = new Coord(coord.x+2, coord.y  );
            neighborCoord[ 5] = new Coord(coord.x+1, coord.y+1);
            neighborCoord[ 6] = new Coord(coord.x  , coord.y+1);
            neighborCoord[ 7] = new Coord(coord.x-1, coord.y+1);
            neighborCoord[ 8] = new Coord(coord.x-2, coord.y  );
            break;
         }

   #if DEBUG
         { // debug check
            int i=0;
            for (; i<neighborCoord.Length; i++)
               if (neighborCoord[i] == null)
                  break;
            if (i != Attr.getNeighborNumber(direction))
               throw new Exception("Исправь AttrPenrousePeriodic1.getNeighborNumber("+direction+")...");
         }
   #endif

   //      for (int i=0; i<neighborCoord.length; i++)
   //         if (neighborCoord[i] == null)
   //            neighborCoord[i] = null;

         return neighborCoord;
      }

      protected override void CalcRegion() {
         AttrPenrousePeriodic1 attr = Attr;
         double a = attr.A;
         double c = attr.C;
         double e = attr.E;
         double f = attr.F;
         double h = attr.H;
         double g = attr.G;
         double k = attr.K;
         double z = attr.Z;

         double gz = g-z;

         double periodicX = (coord.x/ 9)*(4*k + 2*g);
         double periodicY = (coord.y/14)*(10*h + 6*f + 2*c + a);//4*f + 13*h + e);

         // координаты верхнего левого угла прямоугольника описавающего фигуру
         double left = .0, top = 0.0;

         switch (direction) {
         case  71:
            left += z;
            goto case 35;
         case  35: case  53: case  62: case  70: case  98: case 125:
            left += gz;
            goto case 8;
         case   8: case  17: case  80: case  89: case 116:
            left += z;
            goto case 7;
         case   7: case  16: case  26: case  34: case  44: case  52: case  61: case  79: case  88: case  97: case 107: case 115: case 124:
            left += z;
            goto case 43;
         case  43: case  78:
            left += gz;
            goto case 6;
         case   6: case  15: case  25: case  51: case  60: case  69: case  96: case 106: case 114: case 122: case 123:
            left += z;
            goto case 5;
         case   5: case  33: case  42: case  41: case  59: case  68: case  77: case 105: case 113:
            left += gz;
            goto case 24;
         case  24: case  32: case  87: case  86:
            left += z;
            goto case 4;
         case   4: case  14: case  23: case  31: case  40: case  50: case  58: case  67: case  76: case  95: case 104: case 112: case 121:
            left += z;
            goto case 13;
         case  13: case  22: case  49: case  85: case  94: case 120:
            left += gz;
            goto case 39;
         case  39: case  66: case  75: case 103: case 111:
            left += z;
            goto case 3;
         case   3: case  12: case  21: case  30: case  38: case  48: case  57: case  56: case  84: case  93: case 102: case 110: case 119:
            left += z;
            goto case 2;
         case   2: case 101:
            left += gz;
            goto case 11;
         case  11: case  20: case  29: case  37: case  47: case  64: case  65: case  74: case  83: case  92: case 109:
            left += z;
            goto case 1;
         case   1: case  10: case  19: case  28: case  73: case  82: case  91: case 100: case 108:
            left += gz;
            goto case 46;
         case  46: case  55: case  54: case 118:
            left += z;
            goto case 0;
         case   0: case   9: case  18: case  27: case  36: case  45: case  63: case  72: case  81: case  90: case  99: case 117:
            left += periodicX;
            break;
         default:
            throw new Exception("Забыл left case #" + direction);
         }

         double fc = f-c;
         switch (direction) {
         case 118: case 123:
            top += fc;
            goto case 120;
         case 120:
            top += c;
            goto case 119;
         case 119: case 121: case 125:
            top += c;
            goto case 117;
         case 117: case 124:
            top += fc;
            goto case 109;
         case 109: case 122:
            top += f;
            goto case 110;
         case 110: case 111: case 112: case 113:
            top += c;
            goto case 99;
         case  99: case 108: case 100: case 103: case 114: case 115: case 116:
            top += f;
            goto case 101;
         case 101: case 105:
            top += c;
            goto case 90;
         case  90: case  91: case 102: case 104: case 106: case 107:
            top += f;
            goto case 98;
         case  98:
            top += c;
            goto case 81;
         case  81: case  97:
            top += fc;
            goto case 92;
         case  92: case  93: case  94: case  95: case  86: case  96:
            top += c;
            goto case 83;
         case  83: case  87:
            top += f;
            goto case 72;
         case  72: case  82: case  85: case  88: case  89:
            top += c;
            goto case 74;
         case  74: case  84: case  76: case  77: case  80: 
            top += c;
            goto case 75;
         case  75:
            top += fc;
            goto case 73;
         case  73: case  78:
            top += c;
            goto case 63;
         case  63: case  65: case  68: case  79:
            top += f;
            goto case 66;
         case  66: case  67:
            top += f;
            goto case 71;
         case  71: case  70: case  54: case  64: case  56: case  59: case  69: case  61:
            top += c;
            goto case 55;
         case  55: case  57: case  58: case  60:
            top += f;
            goto case 62;
         case  62:
            top += c;
            goto case 48;
         case  48: case  49: case  50:
            top += fc;
            goto case 46;
         case  46: case  47: case  51: case  52:
            top += f;
            goto case 45;
         case  45: case  38: case  41: case  53:
            top += c;
            goto case 36;
         case  36: case  37: case  39: case  40: case  42: case  44:
            top += h;
            goto case 27;
         case  27: case  28: case  29: case  30: case  31: case  33: case  34: case  43:
            top += c;
            goto case 20;
         case  20: case  32:
            top += fc;
            goto case 35;
         case  35:
            top += c;
            goto case 18;
         case  18: case  22: case  26:
            top += f;
            goto case 11;
         case  11: case  21: case  24: case  25:
            top += f;
            goto case 9;
         case   9: case  13: case  17: case  16: case  19: case  23:
            top += c;
            goto case 8;
         case   8: case  10: case  12: case  14: case  15:
            top += h;
            goto case 0;
         case   0: case   1: case   2: case   3: case   4: case   5: case   6: case   7:
            top += periodicY;
            break;
         default:
            throw new Exception("Забыл top case #" + direction);
         }

         switch (direction) {
         case  0: case 12: case  15: case  26: case 31:
         case 37: case 44: case  58: case  63: case 68:
         case 84: case 91: case 97: case 104: case 119: case 124:
            region.SetPoint(0, left + k, top + c);
            region.SetPoint(1, left + g, top + e);
            region.SetPoint(2, left    , top + h);
            region.SetPoint(3, left + z, top    );
            break;
         case 1: case 4: case 27: case 33: case 48: case 74: case 90: case 114:
            region.SetPoint(0, left + k, top    );
            region.SetPoint(1, left + g, top + h);
            region.SetPoint(2, left    , top + e);
            region.SetPoint(3, left + z, top + c);
            break;
         case  2: case  5: case 22: case 28: case  35: case  43: case  49: case 62:
         case 73: case 78: case 85: case 98: case 101: case 105: case 120: case 125:
            region.SetPoint(0, left+2*z, top + h);
            region.SetPoint(1, left + z, top+2*h);
            region.SetPoint(2, left    , top + h);
            region.SetPoint(3, left + z, top    );
            break;
         case 3: case 6: case 29: case 34: case 50: case 77: case 100: case 107:
            region.SetPoint(0, left + k, top + e);
            region.SetPoint(1, left + z, top + h);
            region.SetPoint(2, left    , top    );
            region.SetPoint(3, left + g, top + c);
            break;
         case  7: case 10: case 14: case 18: case  30: case  36: case  42: case  57:
         case 65: case 76: case 79: case 81: case 102: case 106: case 117: case 121:
            region.SetPoint(0, left + k, top + h);
            region.SetPoint(1, left + z, top + e);
            region.SetPoint(2, left    , top + c);
            region.SetPoint(3, left + g, top    );
            break;
         case  8: case 20: case 32: case  55: case  60: case  75:
         case 80: case 83: case 87: case 103: case 118: case 123:
            region.SetPoint(0, left+2*g, top + c);
            region.SetPoint(1, left + g, top+2*c);
            region.SetPoint(2, left    , top + c);
            region.SetPoint(3, left + g, top    );
            break;
         case  9: case 11: case 24: case 39: case 46: case  51: case  61: case 64:
         case 66: case 72: case 93: case 96: case 99: case 112: case 122:
            region.SetPoint(0, left + g, top    );
            region.SetPoint(1, left + g, top + a);
            region.SetPoint(2, left    , top+a+c);
            region.SetPoint(3, left    , top + c);
            break;
         case 13: case 16: case 38: case 53: case 59: case 71: case 88: case 95: case 110: case 115:
            region.SetPoint(0, left + z, top    );
            region.SetPoint(1, left + z, top + a);
            region.SetPoint(2, left    , top+a+h);
            region.SetPoint(3, left    , top + h);
            break;
         case 17: case 21: case 25: case 40: case  47: case  52: case  54: case 67:
         case 69: case 86: case 89: case 92: case 109: case 111: case 116:
            region.SetPoint(0, left + g, top + c);
            region.SetPoint(1, left + g, top+c+a);
            region.SetPoint(2, left    , top + a);
            region.SetPoint(3, left    , top    );
            break;
         case 19: case 23: case 41: case 45: case 56: case 70: case 82: case 94: case 108: case 113: 
            region.SetPoint(0, left + z, top + h);
            region.SetPoint(1, left + z, top+h+a);
            region.SetPoint(2, left    , top + a);
            region.SetPoint(3, left    , top    );
            break;
         default:
            throw new Exception("Забыл case #" + direction);
         }
      }

      public override RectDouble getRcInner(int borderWidth) {
         var attr = Attr;
         var area = attr.Area;
   //      var w = borderWidth/2.0;
         var sq  = attr.GetSq(borderWidth);
         var sq2 = sq/2;

         var center = new PointDouble(); // координата центра квадрата
         center.X = (region.GetPoint(0).X+region.GetPoint(2).X) / 2.0;

         switch (direction) {
         case  0: case 12: case 15: case  26: case  31:
         case 37: case 44: case 58: case  63: case  68:
         case 84: case 91: case 97: case 104: case 119: case 124:
         case  2: case  5: case 22: case  28: case  35: case  43: case  49: case  62:
         case 73: case 78: case 85: case  98: case 101: case 105: case 120: case 125:
         case  7: case 10: case 14: case 18: case  30: case  36: case  42: case  57:
         case 65: case 76: case 79: case 81: case 102: case 106: case 117: case 121:
         case  8: case 20: case 32: case  55: case  60: case  75:
         case 80: case 83: case 87: case 103: case 118: case 123:
         case 17: case 21: case 25: case  40: case  47: case  52: case  54: case  67:
         case 69: case 86: case 89: case  92: case 109: case 111: case 116:
         case 19: case 23: case 41: case  45: case  56: case  70: case  82: case  94: case 108: case 113: 
            center.Y = (region.GetPoint(3).Y+region.GetPoint(1).Y) / 2.0;
            break;

         case  1: case  4: case 27: case 33: case  48: case  74: case  90: case 114:
         case  3: case  6: case 29: case  34: case  50: case  77: case 100: case 107:
         case  9: case 11: case 24: case 39: case  46: case  51: case  61: case  64:
         case 66: case 72: case 93: case 96: case  99: case 112: case 122:
         case 13: case 16: case 38: case 53: case  59: case  71: case  88: case  95: case 110: case 115:
            center.Y = (region.GetPoint(0).Y+region.GetPoint(2).Y) / 2.0;
            break;
         default:
            throw new Exception("Забыл case #" + direction);
         }

         return new RectDouble(
            center.X - sq2,
            center.Y - sq2,
            sq, sq);
      }

      public override int getShiftPointBorderIndex() { return 2; }

      public override Color getBackgroundFillColor(int fillMode, Color defaultColor, Func<int, Color> repositoryColor) {
         //return base.getBackgroundFillColor(fillMode, defaultColor, repositoryColor);

         if (fillMode == Attr.getMaxBackgroundFillModeValue())
         {
            switch (getDirection()) {
            case   0: case   1: case   6: case   7: case  84: case  85:
            case  76: case 118: case 109: case 110: case 113: case 122: case 123:
               return repositoryColor(1);
            case   3: case   4: case  19: case  27: case  16: case  34: case  55:
            case  56: case  59: case  60: case  45: case  53: case  75: case   8:
               return repositoryColor(2);
            case   2: case  10: case  12: case  11: case  21:
            case   5: case  14: case  15: case  24: case  25:
            case  36: case  28: case  37: case  46: case  47:
            case  42: case  43: case  44: case  51: case  52:
            case  49: case  57: case  58: case  66: case  67:
               return repositoryColor(3);
            case  13: case  23: case  20: case  29: case  32: case  33: case  38: case  48: case  50: case  41: 
               return repositoryColor(4);
            case  22: case  30: case  31: case  39: case  40:
            case  72: case  81: case  89: case  97: case  98:
               return repositoryColor(5);
            case   9: case  17: case  18: case  26: case  35:
            case  54: case  64: case  63: case  65: case  73:
            case  61: case  68: case  69: case  78: case  79:
            case  99: case 116: case 117: case 124: case 125:
            case 111: case 112: case 119: case 120: case 121:
               return repositoryColor(6);
            case  62:
               return repositoryColor(7);
            case  91: case  92: case  93: case 101: case 102:
            case  86: case  96: case 104: case 105: case 106:
               return repositoryColor(8);
            case  70: case  71: case  74: case  77: case  80: case  82: case  83: case  87: case 107:
            case  88: case  90: case  94: case  95: case 100: case 103: case 114: case 115: case 108:
               return repositoryColor(9);
            default:
               throw new Exception("Забыл case #" + getDirection());
            }
         } else
         if (fillMode == (Attr.getMaxBackgroundFillModeValue()-1))
         {
            switch (getDirection()) {
            case   1: case   3: case  13: case  19: case  20:
            case   8: case 103: case 110: case 113:
            case   4: case   6: case  16: case  23: case  32:
            case  27: case  29: case  38: case  45: case  55:
            case  33: case  34: case  41: case  53: case  60:
            case  48: case  50: case  56: case  59: case  75:
            case  80: case  82: case  88: case  90: case 107:
               return repositoryColor(1);

            case  54: case  64: case  63: case  65: case  73:
            case  61: case  68: case  69: case  78: case  79:
            case  86: case  96: case 104: case 105: case 106:
            case  91: case  92: case  93: case 101: case 102:
            case  99: case 116: case 117: case 124: case 125:
               return repositoryColor(2);

            case   2: case  10: case  12: case  11: case  21:
            case   5: case  14: case  15: case  24: case  25:
            case  36: case  28: case  37: case  46: case  47:
            case  42: case  43: case  44: case  51: case  52:
            case  49: case  57: case  58: case  66: case  67:
               return repositoryColor(3);

            case  22: case  30: case  31: case  39: case  40:
            case  72: case  81: case  89: case  97: case  98:
            case 111: case 112: case 119: case 120: case 121:
            case   9: case  17: case  18: case  26: case  35:
               return repositoryColor(4);

            case   0: case  74: case  83: case  84: case  76:
            case  85: case  77: case  87: case   7: case  70:
            case  62: case  71: case 108: case 100: case 109:
            case 118: case  94: case  95: case 122: case 114: case 123: case 115:
               return repositoryColor(5);

            default:
               throw new Exception("Забыл case #" + getDirection());
            }
         }
         return base.getBackgroundFillColor(fillMode, defaultColor, repositoryColor);

   //      if (direction == dddd)
   //         return repositoryColor(1);
   //
   //      for (BaseCell cell1: cell.getNeighbors())
   //         if ((cell1 != null) && (cell1.direction == dddd))
   //            return repositoryColor(2);
   //
   //      for (BaseCell cell1: cell.getNeighbors())
   //         if (cell1 != null)
   //            for (BaseCell cell2: cell1.getNeighbors())
   //               if ((cell2 != null) && (cell2.direction == dddd))
   //                  return repositoryColor(3);
   //
   //      for (BaseCell cell1: cell.getNeighbors())
   //         if (cell1 != null)
   //            for (BaseCell cell2: cell1.getNeighbors())
   //               if (cell2 != null)
   //                  for (BaseCell cell3: cell2.getNeighbors())
   //                     if ((cell3 != null) && (cell3.direction == dddd))
   //                        return repositoryColor(4);
   //
   //      for (BaseCell cell1: cell.getNeighbors())
   //         if (cell1 != null)
   //            for (BaseCell cell2: cell1.getNeighbors())
   //               if (cell2 != null)
   //                  for (BaseCell cell3: cell2.getNeighbors())
   //                     if (cell3 != null)
   //                        for (BaseCell cell4: cell3.getNeighbors())
   //                           if ((cell4 != null) && (cell4.direction == dddd))
   //                              return repositoryColor(5);
   //
   //      return repositoryColor(0);
      }

   }
}
