////////////////////////////////////////////////////////////////////////////////
//                               FastMines project
// file name: "ShapeTrapezoid1.cs"
//
// Реализация класса Trapezoid1 - 3 трапеции, составляющие равносторонний треугольник
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

    public class ShapeTrapezoid1 : BaseShape {

        public override SizeDouble GetSize(Matrisize sizeField) {
            var a = A;
            var c = C;
            var r = RIn;
            var R = ROut;
            var result = new SizeDouble(
                    c + a *  (sizeField.m + 1),
                    R     * ((sizeField.n + 1) / 2) +
                    r     * ((sizeField.n + 0) / 2));

            if (sizeField.n < 4)
                if ((sizeField.m % 3) != 0)
                    result.Width -= c;

            return result;
        }

        public override int GetNeighborNumber(int direction) { return 8; }
        public override int GetVertexNumber(int direction) { return 4; }
        public override double GetVertexIntersection() { return 3.6; } // (3+3+3+3+6)/5.
        public override Size GetDirectionSizeField() { return new Size(3, 4); }
        public override double A => Math.Sqrt(Area / SQRT27) * 2;
        public double B => A * 2;
        public double C => A / 2;
        public double ROut => A * SQRT3;
        public double RIn => ROut / 2;
        public override double GetSq(double borderWidth) {
           var w = borderWidth / 2.0;
           return (A * SQRT3 - w * 4)/(SQRT3 + 1);
        }
    }

}
