////////////////////////////////////////////////////////////////////////////////
//                               FastMines project
// file name: "ShapeQuadrangle1.cs"
//
// Реализация класса Quadrangle1 - четырёхугольник 120°-90°-60°-90°
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

    public class ShapeQuadrangle1 : BaseShape {

        public override SizeDouble GetSize(Matrisize sizeField) {
            var a = A;
            var b = B;
            var h = H;
            var m = M;
            var result = new SizeDouble(
                m + m * ((sizeField.m + 2) / 3)+
                    h * ((sizeField.m + 1) / 3)+
                    m * ((sizeField.m + 0) / 3),
                b + b * ((sizeField.n + 1) / 2)+
                    a * ((sizeField.n + 0) / 2));

            if (sizeField.m == 1)
                if ((sizeField.n & 1) == 0)
                    result.Height -= a / 4;
            if (sizeField.m == 2)
                if ((sizeField.n % 4) == 0)
                    result.Height -= a / 4;
            if ((sizeField.n == 1) || (sizeField.n == 2)) {
                if ((sizeField.m % 3) == 2)
                    result.Width -= m;
                if ((sizeField.m % 3) == 0)
                    result.Width -= m;
            }

            return result;
        }

        public override int GetNeighborNumber(int direction) { return 9; }
        public override int GetVertexNumber(int direction) { return 4; }
        public override double GetVertexIntersection() { return 4.25; } // (3+4+4+6)/4.
        public override Size GetDirectionSizeField() { return new Size(3, 4); }
        public override double A => Math.Sqrt(Area / SQRT3) * 2;
        public double B => A / 2;
        public double H => B * SQRT3;
        public double N => A * 0.75;
        public double M => H / 2;
        public double Z => A / (1 + SQRT3);
        public double Zx => Z * SQRT3 / 2;
        public double Zy => Z / 2;
        public override double GetSq(double borderWidth) {
            var w = borderWidth / 2.0;
            return (A * SQRT3 - w * 2 * (1 + SQRT3)) / (SQRT3 + 2);
        }
    }

}
