////////////////////////////////////////////////////////////////////////////////
//                               FastMines project
// file name: "ShapeRhombus1.cs"
//
// Реализация класса Rhombus1 - 3 ромба, составляющие равносторонний шестиугольник
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

    public class ShapeRhombus1 : BaseShape {

       public override SizeDouble GetSize(Matrisize sizeField) {
           var a = A;
           var r = R;
           var c = C;
           var result = new SizeDouble(
                 c + a  * ((sizeField.m + 2) / 3) +
                (a + c) * ((sizeField.m + 1) / 3) +
                     c  * ((sizeField.m + 0) / 3),
                     r  *  (sizeField.n + 1));

           if (sizeField.m == 1)
              result.Height -= r;
           if (sizeField.n == 1)
               switch (sizeField.m % 3) {
               case 0: result.Width -= a / 2; break;
               case 2: result.Width -= a; break;
               }

           return result;
       }

       public override int GetNeighborNumber(int direction) { return 10; }
       public override int GetVertexNumber(int direction) { return 4; }
       public override double GetVertexIntersection() { return 4.5; } // (3+3+6+6)/4.
       public override Size GetDirectionSizeField() { return new Size(3, 2); }
       public override double A => Math.Sqrt(Area*2/SQRT3);
       public double C => A / 2;
       public double H => A * SQRT3;
       public double R => H / 2;
       public override double GetSq(double borderWidth) {
            var w = borderWidth / 2.0;
            return (A * SQRT3 - w * 4) / (SQRT3 + 1);
       }

       public override int GetMaxCellFillModeValue() {
            return base.GetMaxCellFillModeValue() + 1;
        }
    }

}
