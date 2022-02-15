////////////////////////////////////////////////////////////////////////////////
//                               FastMines project
// file name: "ShapeTriangle1.cs"
//
// Реализация класса Triangle1 - равносторонний треугольник (вариант поля №1)
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

    public class ShapeTriangle1 : BaseShape {

        public override SizeDouble GetSize(Matrisize sizeField) {
            return new SizeDouble(
                 B * (sizeField.m + 1),
                 H * (sizeField.n + 0));
        }

        public override int GetNeighborNumber(int direction) { return 12; }
        public override int GetVertexNumber(int direction) { return 3; }
        public override double GetVertexIntersection() { return 6; }
        public override Size GetDirectionSizeField() { return new Size(2, 2); }
        public override double A => B * 2.0f; // размер стороны треугольника
        /// <summary> </summary> пол стороны треугольника */
        public double B => Math.Sqrt(Area / SQRT3);
        /// <summary> </summary> высота треугольника */
        public double H => B * SQRT3;
        public override double GetSq(double borderWidth) {
            var w = borderWidth / 2.0;
            return (H * 2 - 6 * w) / (SQRT3 + 2);
        }
    }

}
