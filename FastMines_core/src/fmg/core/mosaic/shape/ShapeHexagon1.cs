////////////////////////////////////////////////////////////////////////////////
//                               FastMines project
// file name: "ShapeHexagon1.cs"
//
// Реализация класса Hexagon1 - правильный 6-ти угольник (сота)
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

    public class ShapeHexagon1 : BaseShape {
    
        public override SizeDouble GetSize(Matrisize sizeField) {
            var a = A;
            var result = new SizeDouble(
                  a * (sizeField.m       + 0.5) * SQRT3,
                  a * (sizeField.n * 1.5 + 0.5));
    
            if (sizeField.n == 1)
                result.Width -= B / 2;
    
            return result;
        }
    
        public override int GetNeighborNumber(int direction) { return 6; }
        public override int GetVertexNumber(int direction) { return 6; }
        public override double GetVertexIntersection() { return 3; }
        public override Size GetDirectionSizeField() { return new Size(1, 2); }
        public override double A => Math.Sqrt(2 * Area / SQRT27);
        /// <summary> пол стороны треугольника </summary>
        public double B => A * SQRT3;
        public override double GetSq(double borderWidth) {
            var w = borderWidth / 2.0;
            return 2 * (B - 2 * w) / (SQRT3 + 1);
        }
    }

}
