////////////////////////////////////////////////////////////////////////////////
//                               FastMines project
// file name: "ShapePenrousePeriodic1.cs"
//
// Реализация класса PenrousePeriodic1 - один из вариантов периодической мозаики Пенроуза (ромбы 72°-108° & 36°- 144°)
// Author: 2010-2018  -  Serhii Kryvulia aka SeregaLBN
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
using System.Collections.Generic;
using Fmg.Common;
using Fmg.Common.Geom;

namespace Fmg.Core.Mosaic.Shape {

    public class ShapePenrousePeriodic1 : BaseShape {

        public override SizeDouble GetSize(Matrisize sizeField) {
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
                  z * ((sizeField.m + 8) / 9) +
                  k * ((sizeField.m + 7) / 9) +
                  g * ((sizeField.m + 6) / 9) +
                  g * ((sizeField.m + 5) / 9) +
                  z * ((sizeField.m + 4) / 9) +
                  g * ((sizeField.m + 3) / 9) +
                  g * ((sizeField.m + 2) / 9) +
                  z * ((sizeField.m + 1) / 9) +
                  g * ((sizeField.m + 0) / 9),
                  e +
                  f * ((sizeField.n + 13) / 14) +
                  b * ((sizeField.n + 12) / 14) +
                  h * ((sizeField.n + 11) / 14) +
                  h * ((sizeField.n + 10) / 14) +
                  b * ((sizeField.n +  9) / 14) +
                  f * ((sizeField.n +  8) / 14) +
                  b * ((sizeField.n +  7) / 14) +
                  c * ((sizeField.n +  6) / 14) +
                  b * ((sizeField.n +  5) / 14) +
                  h * ((sizeField.n +  4) / 14) +
                  h * ((sizeField.n +  3) / 14) +
                  a * ((sizeField.n +  2) / 14) +
                  h * ((sizeField.n +  1) / 14) +
                  e * ((sizeField.n +  0) / 14));

            // когда размер поля мал...
            if (sizeField.n < 14) { // ...нужно вычислять не только по общей формуле, а и убрать остатки по ширине...
                if ((sizeField.m % 9) == 7)
                    if (sizeField.n < 7)
                        result.Width -= g - z;
                if ((sizeField.m % 9) == 6)
                    if (sizeField.n < 4)
                        result.Width -= g - z;
                if ((sizeField.m % 9) == 4)
                    if (sizeField.n < 9)
                        result.Width -= g;
                if ((sizeField.m % 9) == 3) {
                    if (sizeField.n < 14)
                        result.Width -= z;
                    if (sizeField.n < 3)
                        result.Width -= g - z;
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

        public override int GetNeighborNumber(int direction) {
            switch (direction) {
            case 100: case 108: case 114: case 115:
                return  7;
            case  21: case  24: case  37: case  42: case  49: case  72:
            case  81: case  83: case  86: case  87: case  89: case  93:
            case  94: case  95: case  97: case  98: case 111: case 112: case 117: case 124:
                return  8;
            case   0: case   7: case   9: case  10: case  15: case  17:
            case  46: case  52: case  54: case  61: case  65:
            case  66: case  67: case  68: case  70: case  71: case  74:
            case  77: case  85: case 101: case 105: case 125:
                return  9;
            case   2: case   5: case  11: case  12: case  13: case  14:
            case  18: case  20: case  23: case  25: case  26: case  28:
            case  29: case  30: case  31: case  32: case  33: case  35:
            case  36: case  38: case  39: case  40: case  41: case  43:
            case  44: case  47: case  48: case  50: case  51: case  57:
            case  58: case  62: case  63: case  64: case  69: case  73:
            case  76: case  78: case  79: case  80: case  82: case  84:
            case  88: case  90: case  91: case  92: case  96: case  99:
            case 102: case 103: case 104: case 106: case 107: case 109: case  22:
            case 116: case 118: case 119: case 120: case 121: case 122: case 123:
                return 10;
            case   1: case   6: case   8: case  16: case  19: case  45:
            case  53: case  55: case  56: case  59: case  60: case  75: case 110: case 113:
                return 11;
            case   3: case   4: case  27: case  34:
                return 12;
            default:
                throw new ArgumentException("Invalid value direction=" + direction);
                //throw new Exception("Забыл case #" + direction);
            }
        }
        public override int GetVertexNumber(int direction) { return 4; }

        static double vertexIntersection = 0.0;
        public override double GetVertexIntersection() {
            if (vertexIntersection < 1) {
                var cntDirection = GetDirectionCount(); // 0..125
                var sum = 0;
                for (var dir = 0; dir < cntDirection; dir++)
                    sum += GetNeighborNumber(dir) +
                        4 + // соседние фигуры, которые граничат с гранями this, участвуют в подсчёте два раза...
                        4; // ...сама this участвует подсчёте все 4 раза
                vertexIntersection = ((double)sum) / GetVertexNumber(-1) / cntDirection;
                //System.out.println("PenrousePeriodic1::getVertexgetVertexNeighbor == " + vertexIntersection);
            }
            return vertexIntersection;
        }

        public override Size GetDirectionSizeField() { return new Size(9, 14); }
        public override double A => Math.Sqrt(Area * 63 / (39 * SIN72 + 24 * SIN36));
        public double B => A + C;
        public double C => A * SIN18;
        public double E => H + C;
        public double H => A * SIN54;
        public double F => H - C;
        public double G => A * SIN72;
        public double Z => A * SIN36;
        public double K => Z + G;
        public override double GetSq(double borderWidth) {
            //var w = borderWidth / 2.0;
            return A / SIN99 * SIN36 / SQRT2;
        }

        public override int GetMaxCellFillModeValue() {
            return base.GetMaxCellFillModeValue() + 2;
        }
    }

}
