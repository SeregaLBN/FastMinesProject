////////////////////////////////////////////////////////////////////////////////
//                               FastMines project
// file name: "ShapePentagonT10.cs"
//
// Реализация класса PentagonT10 - 5-ти угольник, тип №10
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
using Fmg.Common;
using Fmg.Common.Geom;

namespace Fmg.Core.Mosaic.Shape {

    public class ShapePentagonT10 : BaseShape {

        public override SizeDouble GetSize(Matrisize sizeField) {
            var a = A;
            var result = new SizeDouble(
                  2 * a +
                  5 * a * ((sizeField.m + 1) / 2) +
                      a * ((sizeField.m + 0) / 2),
                  2 * a +
                  3 * a * ((sizeField.n + 2) / 3) +
                  3 * a * ((sizeField.n + 1) / 3) +
                      a * ((sizeField.n + 0) / 3));

            if (sizeField.n == 1)
                if ((sizeField.m & 1) == 1)
                    result.Width -= 3 * a;
                else
                    result.Width -= a;
            if (sizeField.n == 2)
                if ((sizeField.m & 1) == 1)
                    result.Width -= 2 * a;
                else
                    result.Width -= a;

            if (sizeField.m == 1)
                if (((sizeField.n % 6) == 4) ||
                   ((sizeField.n % 6) == 5))
                    result.Height -= 2 * a;

            return result;
        }

        public override int GetNeighborNumber(int direction) {
            switch (direction) {
            case 0: case 1: case 6: case 7: return 7;
            case 2: case 3: case 4: case 5: case 8: case 9: case 10: case 11: return 6;
            default:
                throw new ArgumentException("Invalid value direction=" + direction);
            }
        }
        public override int GetVertexNumber(int direction) { return 5; }

        static double _vertexIntersection = 0.0;
        public override double GetVertexIntersection() {
            if (_vertexIntersection < 1) {
                var cntDirection = GetDirectionCount(); // 0..11
                double sum = 0;
                for (var dir = 0; dir < cntDirection; dir++)
                    switch (dir) {
                    case 0: case 1: case 6: case 7:
                        sum += 3;
                        break;
                    case 2: case 3: case 4: case 5:
                    case 8: case 9: case 10: case 11:
                        sum += 16.0 / 5.0;
                        break;
                    default:
                        throw new Exception("Забыл case #" + dir);
                    }
                _vertexIntersection = sum / cntDirection;
                //System.out.println("PentagonT10::getVertexNeighbor == " + _vertexIntersection);
            }
            return _vertexIntersection;
        }

        public override Size GetDirectionSizeField() { return new Size(2, 6); }
        public override double A => Math.Sqrt(Area / 7);
        public override double GetSq(double borderWidth) {
            var w = borderWidth / 2.0;
            return 2 * (A - w);
        }

        public override int GetMaxCellFillModeValue() {
            return base.GetMaxCellFillModeValue() + 1;
            //return 1;
        }
    }

}
