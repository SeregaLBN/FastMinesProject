////////////////////////////////////////////////////////////////////////////////
//                               FastMines project
// file name: "ShapeTrSq1.cs"
//
// Реализация класса TrSq1 - мозаика из 4х треугольников и 2х квадратов
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

    public class ShapeTrSq1 : BaseShape {

        public override SizeDouble GetSize(Matrisize sizeField) {
            var b = B;
            var k = K;
            var n = N;
            var m = M;
            var result = new SizeDouble(
                  b + n * ((sizeField.m - 1 + 2) / 3) +
                      k * ((sizeField.m - 1 + 1) / 3) +
                      m * ((sizeField.m - 1 + 0) / 3),
                  b + n *  (sizeField.n - 1));

            if (sizeField.n == 1) {
                if ((sizeField.m % 3) == 2) result.Width -= m;
                if ((sizeField.m % 3) == 0) result.Width -= k;
            }
            if (sizeField.m == 1)
                if ((sizeField.n & 1) == 0)
                    result.Height -= m;

            return result;
        }

        public override int GetNeighborNumber(int direction) {
            switch (direction) {
            case 1: case 2: case 3: case 5: return 9;
            case 0: case 4: return 12;
            default:
                throw new ArgumentException("Invalid value direction=" + direction);
            }
        }
        public override int GetVertexNumber(int direction) {
            switch (direction) {
            case 1: case 2: case 3: case 5: return 3;
            case 0: case 4: return 4;
            default:
                throw new ArgumentException("Invalid value direction="+direction);
            }
        }
        public override double GetVertexIntersection() { return 5.0; }
        public override Size GetDirectionSizeField() { return new Size(3, 2); }
        public override double A => Math.Sqrt(3 * Area / (1 + SQRT3 / 2));
        public double B => N + M;
        public double K => N - M;
        public double N => A * SIN75;
        public double M => A * SIN15;
        public override double GetSq(double borderWidth) {
            var w = borderWidth / 2.0;
            return (A * SQRT3 - w * 6) / (4 * SIN75);
        }
    }

}
