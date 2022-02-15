////////////////////////////////////////////////////////////////////////////////
//                               FastMines project
// file name: "ShapeTriangle3.cs"
//
// Реализация класса Triangle3 - треугольник 45°-90°-45°
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

    public class ShapeTriangle3 : BaseShape {

        public override SizeDouble GetSize(Matrisize sizeField) {
           var a = A;
           return new SizeDouble(a * ((sizeField.m + (sizeField.m & 1)) / 2),
                                 a * ((sizeField.n + (sizeField.n & 1)) / 2));
        }

        public override int GetNeighborNumber(int direction) { return 14; }
        public override int GetVertexNumber(int direction) { return 3; }
        public override double GetVertexIntersection() { return 6.6666666666666666666666666666667; } // (8+8+4)/3.
        public override Size GetDirectionSizeField() { return new Size(2, 2); }
        public override double A => 2 * B;
        /// <summary> пол стороны треугольника </summary>
        public double B => Math.Sqrt(Area);
        public override double GetSq(double borderWidth) {
            var w = borderWidth / 2.0;
            return (A - w * 2 / TAN45_2) / 3;
        }
    }

}
