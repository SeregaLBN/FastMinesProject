////////////////////////////////////////////////////////////////////////////////
//                               FastMines project
// file name: "Square2.cs"
//
// Реализация класса Square2 - квадрат (перекошенный вариант поля)
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

    public class ShapeSquare2 : BaseShape {

        public override SizeDouble GetSize(Matrisize sizeField) {
            var a = A; // размер стороны квадрата
            return new SizeDouble(
                  sizeField.m * a + a / 2,
                  sizeField.n * a);
        }

        public override int GetNeighborNumber(int direction) { return 6; }
        public override int GetVertexNumber(int direction) { return 4; }
        public override double GetVertexIntersection() { return 3; }
        public override Size GetDirectionSizeField() { return new Size(1, 2); }
        public override double A => Math.Sqrt(Area);
        public override double GetSq(double borderWidth) {
            var w = borderWidth / 2.0;
            return A - 2 * w;
        }
    }

}
