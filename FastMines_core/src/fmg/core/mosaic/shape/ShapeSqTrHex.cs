////////////////////////////////////////////////////////////////////////////////
//                               FastMines project
// file name: "ShapeSqTrHex.cs"
//
// Реализация класса SqTrHex - мозаика из 6Square 4Triangle 2Hexagon
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
using System.Collections.Generic;
using Fmg.Common.Geom;

namespace Fmg.Core.Mosaic.Shape {

    public class ShapeSqTrHex : BaseShape {

        public override SizeDouble GetSize(Matrisize sizeField) {
            var a = A;
            var h = H;
            var result = new SizeDouble(
                   a / 2 + h + a / 2 * ((sizeField.m + 2) / 3) +
                           h         * ((sizeField.m + 1) / 3) +
                  (a / 2 + h)        * ((sizeField.m + 0) / 3),
                   a / 2 + h         * ((sizeField.n + 1) / 2) +
                   a * 3 / 2         * ((sizeField.n + 0) / 2));

            if (sizeField.n < 4) {
                int x = sizeField.m % 3;
                switch (sizeField.n) {
                case 1:
                    switch (x) { case 0:         result.Width -= h; goto case 1;
                                 case 1: case 2: result.Width -= h; break; }
                    break;
                case 2: case 3:
                    switch (x) { case 0: case 1: result.Width -= h; break; }
                    break;
              }
            }
            if (sizeField.m < 3) {
                int y = sizeField.n % 4;
                switch (sizeField.m) {
                case 1:
                    switch (y) { case 0:         result.Height -= a * 1.5; break;
                                 case 2: case 3: result.Height -= a / 2; break; }
                    break;
                case 2:
                    if (y == 0)                  result.Height -= a / 2;
                    break;
                }
            }

            return result;
        }

        public override int GetNeighborNumber(int direction) {
            switch (direction) {
            case  0: case  2: case  6: case  7: return 6;
            case  1: case  3: case  5: case  8: case  9: case 10:
                return 8;
            case  4: case 11:
                return 12;
            default:
                throw new ArgumentException("Invalid value direction="+direction);
            }
        }
        public override int GetVertexNumber(int direction) {
            switch (direction) {
            case  0: case  2: case  6: case  7: return 3;
            case  1: case  3: case  5: case  8: case  9: case 10:
                return 4;
            case  4: case 11:
                return 6;
            default:
                throw new ArgumentException("Invalid value direction="+direction);
            }
        }
        public override double GetVertexIntersection() { return 4.0; }
        public override Size GetDirectionSizeField() { return new Size(3, 4); }
        public override double A => Math.Sqrt(Area / (0.5 + 1 / SQRT3));
        public double H => A * SQRT3 / 2;
        public override double GetSq(double borderWidth) {
            var w = borderWidth / 2.0;
            return (A * SQRT3 - w * 6) / (2 + SQRT3);
        }
    }

}
