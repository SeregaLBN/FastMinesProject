////////////////////////////////////////////////////////////////////////////////
//                               FastMines project
// file name: "ShapeTrapezoid3.cs"
//
// Реализация класса Trapezoid3 - 8 трапеций, складывающихся в шестигранник
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

    public class ShapeTrapezoid3 : BaseShape {

        public override SizeDouble GetSize(Matrisize sizeField) {
            var a = A;
            var b = B;
            var R = ROut;
            var result = new SizeDouble(
                     R * ((sizeField.m + 1) / 2),
                 a + b * ((sizeField.n + 1) / 2) +
                     a * ((sizeField.n + 0) / 2));

            if (sizeField.m == 1)
                switch (sizeField.n % 4) {
                case 0: result.Height -= a; break;
                case 3: result.Height -= a * 1.5; break;
                }
            if (sizeField.n == 1)
                if ((sizeField.m & 1) == 1)
                    result.Width -= RIn;

            return result;
        }

        public override int GetNeighborNumber(int direction) {
             switch (direction) {
             case  2: case  5: case 11: case 12: return 6;
             case  0: case  7: case  9: case 14: return 10;
             case  1: case  3: case  4: case  6:
             case  8: case 10: case 13: case 15: return 11;
             default:
                 throw new ArgumentException("Invalid value direction=" + direction);
             }
        }
        public override int GetVertexNumber(int direction) { return 4; }

        static double _vertexIntersection = 0.0;
        public override double GetVertexIntersection() {
            if (_vertexIntersection < 1) {
                var cntDirection = GetDirectionCount(); // 0..11
                double sum = 0;
                for (var dir = 0; dir < cntDirection; dir++)
                    switch (dir) {
                    case  2: case  5: case 11: case 12:
                        sum += (4 + 4 + 3 + 3) / 4.0;
                        break;
                    case  0: case  7: case  9: case 14:
                        sum += (6 + 6 + 3 + 3) / 4.0;
                        break;
                    case  1: case  3: case  4: case  6:
                    case  8: case 10: case 13: case 15:
                        sum += (6 + 6 + 4 + 3) / 4.0;
                        break;
                    default:
                        throw new Exception("Забыл case #" + dir);
                   }
                _vertexIntersection = sum / cntDirection;
            }
            return _vertexIntersection;
        }

        public override Size GetDirectionSizeField() { return new Size(4, 4); }
        public override double A => Math.Sqrt(Area / SQRT27) * 2;
        public double B => A * 2;
        public double C => A / 2;
        public double ROut => A * SQRT3;
        public double RIn => ROut / 2;
        public override double GetSq(double borderWidth) {
            var w = borderWidth / 2.0;
            return (A * SQRT3 - w * 4) / (SQRT3 + 1);
        }
    }

}
