////////////////////////////////////////////////////////////////////////////////
//                               FastMines project
// file name: "ShapeTrSq2.cs"
//
// Реализация класса TrSq2 - мозаика из 24х треугольников и 12х квадратов (на 1 квадрат приходится 2 треугольника)
// Author: 2002-2018  -  Serhii Kryvulia aka SeregaLBN
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
using Fmg.Common.Geom;

namespace Fmg.Core.Mosaic.Shape {

    public class ShapeTrSq2 : BaseShape {

        public override SizeDouble GetSize(Matrisize sizeField) {
            var a = A;
            var b = B;
            var h = H;
            var result = new SizeDouble(
                    b + h * ((sizeField.m + 2) / 3) +
                    a *     ((sizeField.m + 1) / 3) +
                    b *     ((sizeField.m + 0) / 3),
                    b + h * ((sizeField.n + 2) / 3) +
                    a *     ((sizeField.n + 1) / 3) +
                    b *     ((sizeField.n + 0) / 3));

            if (sizeField.n < 5) {
                var x = sizeField.m % 6;
                switch (sizeField.n) {
                case 1:
                    switch (x) { case 0: case 2: case 5: result.Width -= b; break; }
                    break;
                case 2: case 3: case 4:
                    if (x == 5) result.Width -= b;
                    break;
                }
            }
            if (sizeField.m < 5) {
                var y = sizeField.n % 6;
                switch (sizeField.m) {
                case 1:
                    switch (y) { case 2: case 3: case 5: result.Height -= b; break; }
                    break;
                case 2: case 3: case 4:
                    if (y == 2) result.Height -= b;
                    break;
                }
            }

            return result;
        }

        public override int GetNeighborNumber(int direction) {
            switch (direction) {
            case  1: case  2: case  4: case  5:
            case  6: case  8: case  9: case 11:
            case 12: case 13: case 15: case 16:
            case 19: case 20: case 22: case 23:
            case 24: case 26: case 27: case 29:
            case 30: case 31: case 33: case 34: return 9;
            case  0: case  3: case  7: case 10:
            case 14: case 17: case 18: case 21:
            case 25: case 28: case 32: case 35: return 12;
            default:
                throw new ArgumentException("Invalid value direction="+direction);
            }
        }
        public override int GetVertexNumber(int direction) {
            switch (direction) {
            case  0: case  3: case  7: case 10:
            case 14: case 17: case 18: case 21:
            case 25: case 28: case 32: case 35: return 4;
            case  1: case  2: case  4: case  5:
            case  6: case  8: case  9: case 11:
            case 12: case 13: case 15: case 16:
            case 19: case 20: case 22: case 23:
            case 24: case 26: case 27: case 29:
            case 30: case 31: case 33: case 34: return 3;
            default:
                throw new ArgumentException("Invalid value direction="+direction);
            }
        }
        public override double GetVertexIntersection() { return 5.0; }
        public override Size GetDirectionSizeField() { return new Size(6, 6); }
        /// <summary> </summary> размер стороны треугольника и квадрата */
        public override double A => Math.Sqrt(6 * Area / (2 + SQRT3));
        public double B => A / 2;
        public double H => B * SQRT3;
        public override double GetSq(double borderWidth) {
            var w = borderWidth / 2.0;
            return (A * SQRT3 - w * 6) / (2 + SQRT3) - 1;
        }
    }

}
