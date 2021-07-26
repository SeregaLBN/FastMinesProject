////////////////////////////////////////////////////////////////////////////////
//                               FastMines project
// file name: "Trapezoid3.cs"
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

namespace Fmg.Core.Mosaic.Cells {

    /// <summary> Trapezoid3 - 8 трапеций, складывающихся в шестигранник </summary>
    public class Trapezoid3 : BaseCell {

        public class AttrTrapezoid3 : BaseAttribute {

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

         public Trapezoid3(AttrTrapezoid3 attr, Coord coord)
            : base(attr, coord,
                       ((coord.y & 3) << 2) + (coord.x & 3) // 0..15
                  )
        { }

        private new AttrTrapezoid3 Attr => (AttrTrapezoid3) base.Attr;

        protected override IList<Coord> GetCoordsNeighbor() {
            var neighborCoord = new Coord[Attr.GetNeighborNumber(GetDirection())];

            // определяю координаты соседей
            switch (direction) {
            case 0:
                neighborCoord[ 0] = new Coord(coord.x-1, coord.y-1);
                neighborCoord[ 1] = new Coord(coord.x+1, coord.y-1);
                neighborCoord[ 2] = new Coord(coord.x-1, coord.y  );
                neighborCoord[ 3] = new Coord(coord.x+1, coord.y  );
                neighborCoord[ 4] = new Coord(coord.x-2, coord.y+1);
                neighborCoord[ 5] = new Coord(coord.x-1, coord.y+1);
                neighborCoord[ 6] = new Coord(coord.x  , coord.y+1);
                neighborCoord[ 7] = new Coord(coord.x+1, coord.y+1);
                neighborCoord[ 8] = new Coord(coord.x-2, coord.y+2);
                neighborCoord[ 9] = new Coord(coord.x  , coord.y+2);
                break;
            case 1:
                neighborCoord[ 0] = new Coord(coord.x  , coord.y-2);
                neighborCoord[ 1] = new Coord(coord.x-2, coord.y-1);
                neighborCoord[ 2] = new Coord(coord.x  , coord.y-1);
                neighborCoord[ 3] = new Coord(coord.x+1, coord.y-1);
                neighborCoord[ 4] = new Coord(coord.x+2, coord.y-1);
                neighborCoord[ 5] = new Coord(coord.x-2, coord.y  );
                neighborCoord[ 6] = new Coord(coord.x-1, coord.y  );
                neighborCoord[ 7] = new Coord(coord.x+1, coord.y  );
                neighborCoord[ 8] = new Coord(coord.x+2, coord.y  );
                neighborCoord[ 9] = new Coord(coord.x-2, coord.y+1);
                neighborCoord[10] = new Coord(coord.x  , coord.y+1);
                break;
            case 2:
                neighborCoord[ 0] = new Coord(coord.x-1, coord.y  );
                neighborCoord[ 1] = new Coord(coord.x+1, coord.y  );
                neighborCoord[ 2] = new Coord(coord.x-2, coord.y+1);
                neighborCoord[ 3] = new Coord(coord.x-1, coord.y+1);
                neighborCoord[ 4] = new Coord(coord.x  , coord.y+1);
                neighborCoord[ 5] = new Coord(coord.x+1, coord.y+1);
                break;
            case 3:
                neighborCoord[ 0] = new Coord(coord.x-2, coord.y-2);
                neighborCoord[ 1] = new Coord(coord.x-2, coord.y-1);
                neighborCoord[ 2] = new Coord(coord.x-1, coord.y-1);
                neighborCoord[ 3] = new Coord(coord.x  , coord.y-1);
                neighborCoord[ 4] = new Coord(coord.x+2, coord.y-1);
                neighborCoord[ 5] = new Coord(coord.x-2, coord.y  );
                neighborCoord[ 6] = new Coord(coord.x-1, coord.y  );
                neighborCoord[ 7] = new Coord(coord.x+1, coord.y  );
                neighborCoord[ 8] = new Coord(coord.x+2, coord.y  );
                neighborCoord[ 9] = new Coord(coord.x-2, coord.y+1);
                neighborCoord[10] = new Coord(coord.x  , coord.y+1);
                break;
            case 4:
                neighborCoord[ 0] = new Coord(coord.x  , coord.y-1);
                neighborCoord[ 1] = new Coord(coord.x+2, coord.y-1);
                neighborCoord[ 2] = new Coord(coord.x-2, coord.y  );
                neighborCoord[ 3] = new Coord(coord.x-1, coord.y  );
                neighborCoord[ 4] = new Coord(coord.x+1, coord.y  );
                neighborCoord[ 5] = new Coord(coord.x+2, coord.y  );
                neighborCoord[ 6] = new Coord(coord.x-2, coord.y+1);
                neighborCoord[ 7] = new Coord(coord.x  , coord.y+1);
                neighborCoord[ 8] = new Coord(coord.x+1, coord.y+1);
                neighborCoord[ 9] = new Coord(coord.x+2, coord.y+1);
                neighborCoord[10] = new Coord(coord.x+2, coord.y+2);
                break;
            case 5:
                neighborCoord[ 0] = new Coord(coord.x-1, coord.y-1);
                neighborCoord[ 1] = new Coord(coord.x  , coord.y-1);
                neighborCoord[ 2] = new Coord(coord.x+1, coord.y-1);
                neighborCoord[ 3] = new Coord(coord.x+2, coord.y-1);
                neighborCoord[ 4] = new Coord(coord.x-1, coord.y  );
                neighborCoord[ 5] = new Coord(coord.x+1, coord.y  );
                break;
            case 6:
                neighborCoord[ 0] = new Coord(coord.x  , coord.y-1);
                neighborCoord[ 1] = new Coord(coord.x+2, coord.y-1);
                neighborCoord[ 2] = new Coord(coord.x-2, coord.y  );
                neighborCoord[ 3] = new Coord(coord.x-1, coord.y  );
                neighborCoord[ 4] = new Coord(coord.x+1, coord.y  );
                neighborCoord[ 5] = new Coord(coord.x+2, coord.y  );
                neighborCoord[ 6] = new Coord(coord.x-2, coord.y+1);
                neighborCoord[ 7] = new Coord(coord.x-1, coord.y+1);
                neighborCoord[ 8] = new Coord(coord.x  , coord.y+1);
                neighborCoord[ 9] = new Coord(coord.x+2, coord.y+1);
                neighborCoord[10] = new Coord(coord.x  , coord.y+2);
                break;
            case 7:
                neighborCoord[ 0] = new Coord(coord.x  , coord.y-2);
                neighborCoord[ 1] = new Coord(coord.x+2, coord.y-2);
                neighborCoord[ 2] = new Coord(coord.x-1, coord.y-1);
                neighborCoord[ 3] = new Coord(coord.x  , coord.y-1);
                neighborCoord[ 4] = new Coord(coord.x+1, coord.y-1);
                neighborCoord[ 5] = new Coord(coord.x+2, coord.y-1);
                neighborCoord[ 6] = new Coord(coord.x-1, coord.y  );
                neighborCoord[ 7] = new Coord(coord.x+1, coord.y  );
                neighborCoord[ 8] = new Coord(coord.x-1, coord.y+1);
                neighborCoord[ 9] = new Coord(coord.x+1, coord.y+1);
                break;
            case 8:
                neighborCoord[ 0] = new Coord(coord.x  , coord.y-2);
                neighborCoord[ 1] = new Coord(coord.x-2, coord.y-1);
                neighborCoord[ 2] = new Coord(coord.x-1, coord.y-1);
                neighborCoord[ 3] = new Coord(coord.x  , coord.y-1);
                neighborCoord[ 4] = new Coord(coord.x+2, coord.y-1);
                neighborCoord[ 5] = new Coord(coord.x-2, coord.y  );
                neighborCoord[ 6] = new Coord(coord.x-1, coord.y  );
                neighborCoord[ 7] = new Coord(coord.x+1, coord.y  );
                neighborCoord[ 8] = new Coord(coord.x+2, coord.y  );
                neighborCoord[ 9] = new Coord(coord.x  , coord.y+1);
                neighborCoord[10] = new Coord(coord.x+2, coord.y+1);
                break;
            case 9:
                neighborCoord[ 0] = new Coord(coord.x-1, coord.y-1);
                neighborCoord[ 1] = new Coord(coord.x+1, coord.y-1);
                neighborCoord[ 2] = new Coord(coord.x-1, coord.y  );
                neighborCoord[ 3] = new Coord(coord.x+1, coord.y  );
                neighborCoord[ 4] = new Coord(coord.x-1, coord.y+1);
                neighborCoord[ 5] = new Coord(coord.x  , coord.y+1);
                neighborCoord[ 6] = new Coord(coord.x+1, coord.y+1);
                neighborCoord[ 7] = new Coord(coord.x+2, coord.y+1);
                neighborCoord[ 8] = new Coord(coord.x  , coord.y+2);
                neighborCoord[ 9] = new Coord(coord.x+2, coord.y+2);
                break;
            case 10:
                neighborCoord[ 0] = new Coord(coord.x+2, coord.y-2);
                neighborCoord[ 1] = new Coord(coord.x-2, coord.y-1);
                neighborCoord[ 2] = new Coord(coord.x  , coord.y-1);
                neighborCoord[ 3] = new Coord(coord.x+1, coord.y-1);
                neighborCoord[ 4] = new Coord(coord.x+2, coord.y-1);
                neighborCoord[ 5] = new Coord(coord.x-2, coord.y  );
                neighborCoord[ 6] = new Coord(coord.x-1, coord.y  );
                neighborCoord[ 7] = new Coord(coord.x+1, coord.y  );
                neighborCoord[ 8] = new Coord(coord.x+2, coord.y  );
                neighborCoord[ 9] = new Coord(coord.x  , coord.y+1);
                neighborCoord[10] = new Coord(coord.x+2, coord.y+1);
                break;
            case 11:
                neighborCoord[ 0] = new Coord(coord.x-1, coord.y  );
                neighborCoord[ 1] = new Coord(coord.x+1, coord.y  );
                neighborCoord[ 2] = new Coord(coord.x-1, coord.y+1);
                neighborCoord[ 3] = new Coord(coord.x  , coord.y+1);
                neighborCoord[ 4] = new Coord(coord.x+1, coord.y+1);
                neighborCoord[ 5] = new Coord(coord.x+2, coord.y+1);
                break;
            case 12:
                neighborCoord[ 0] = new Coord(coord.x-2, coord.y-1);
                neighborCoord[ 1] = new Coord(coord.x-1, coord.y-1);
                neighborCoord[ 2] = new Coord(coord.x  , coord.y-1);
                neighborCoord[ 3] = new Coord(coord.x+1, coord.y-1);
                neighborCoord[ 4] = new Coord(coord.x-1, coord.y  );
                neighborCoord[ 5] = new Coord(coord.x+1, coord.y  );
                break;
            case 13:
                neighborCoord[ 0] = new Coord(coord.x-2, coord.y-1);
                neighborCoord[ 1] = new Coord(coord.x  , coord.y-1);
                neighborCoord[ 2] = new Coord(coord.x-2, coord.y  );
                neighborCoord[ 3] = new Coord(coord.x-1, coord.y  );
                neighborCoord[ 4] = new Coord(coord.x+1, coord.y  );
                neighborCoord[ 5] = new Coord(coord.x+2, coord.y  );
                neighborCoord[ 6] = new Coord(coord.x-2, coord.y+1);
                neighborCoord[ 7] = new Coord(coord.x-1, coord.y+1);
                neighborCoord[ 8] = new Coord(coord.x  , coord.y+1);
                neighborCoord[ 9] = new Coord(coord.x+2, coord.y+1);
                neighborCoord[10] = new Coord(coord.x-2, coord.y+2);
                break;
            case 14:
                neighborCoord[ 0] = new Coord(coord.x-2, coord.y-2);
                neighborCoord[ 1] = new Coord(coord.x  , coord.y-2);
                neighborCoord[ 2] = new Coord(coord.x-2, coord.y-1);
                neighborCoord[ 3] = new Coord(coord.x-1, coord.y-1);
                neighborCoord[ 4] = new Coord(coord.x  , coord.y-1);
                neighborCoord[ 5] = new Coord(coord.x+1, coord.y-1);
                neighborCoord[ 6] = new Coord(coord.x-1, coord.y  );
                neighborCoord[ 7] = new Coord(coord.x+1, coord.y  );
                neighborCoord[ 8] = new Coord(coord.x-1, coord.y+1);
                neighborCoord[ 9] = new Coord(coord.x+1, coord.y+1);
                break;
            case 15:
                neighborCoord[ 0] = new Coord(coord.x-2, coord.y-1);
                neighborCoord[ 1] = new Coord(coord.x  , coord.y-1);
                neighborCoord[ 2] = new Coord(coord.x-2, coord.y  );
                neighborCoord[ 3] = new Coord(coord.x-1, coord.y  );
                neighborCoord[ 4] = new Coord(coord.x+1, coord.y  );
                neighborCoord[ 5] = new Coord(coord.x+2, coord.y  );
                neighborCoord[ 6] = new Coord(coord.x-2, coord.y+1);
                neighborCoord[ 7] = new Coord(coord.x  , coord.y+1);
                neighborCoord[ 8] = new Coord(coord.x+1, coord.y+1);
                neighborCoord[ 9] = new Coord(coord.x+2, coord.y+1);
                neighborCoord[10] = new Coord(coord.x  , coord.y+2);
                break;
            }

            return neighborCoord;
        }

        protected override void CalcRegion() {
            var attr = Attr;
            var a = attr.A;
            var b = attr.B;
            var c = attr.C;
            var R = attr.ROut;
            var r = attr.RIn;

            // определение координат точек фигуры
            var oX = (R * 2) * (coord.x / 4) + R; // offset X
            var oY = (a * 6) * (coord.y / 4) + a + b; // offset Y

            switch (direction) {
            case 0:
                region.SetPoint(0, oX - r, oY - a-c);
                region.SetPoint(1, oX - r, oY - c  );
                region.SetPoint(2, oX - R, oY      );
                region.SetPoint(3, oX - R, oY - b  );
                break;
            case 1:
                region.SetPoint(0, oX    , oY - a-b);
                region.SetPoint(1, oX    , oY - b  );
                region.SetPoint(2, oX - r, oY - a-c);
                region.SetPoint(3, oX - R, oY - b  );
                break;
            case 2:
                region.SetPoint(0, oX + r, oY - a-c);
                region.SetPoint(1, oX + r, oY - c  );
                region.SetPoint(2, oX    , oY      );
                region.SetPoint(3, oX    , oY - b  );
                break;
            case 3:
                region.SetPoint(0, oX + R, oY - b  );
                region.SetPoint(1, oX + r, oY - a-c);
                region.SetPoint(2, oX    , oY - b  );
                region.SetPoint(3, oX    , oY - a-b);
                break;
            case 4:
                region.SetPoint(0, oX    , oY      );
                region.SetPoint(1, oX    , oY + a  );
                region.SetPoint(2, oX - R, oY      );
                region.SetPoint(3, oX - r, oY - c  );
                break;
            case 5:
                region.SetPoint(0, oX    , oY - b  );
                region.SetPoint(1, oX    , oY      );
                region.SetPoint(2, oX - r, oY - c  );
                region.SetPoint(3, oX - r, oY - a-c);
                break;
            case 6:
                region.SetPoint(0, oX + R, oY      );
                region.SetPoint(1, oX    , oY + a  );
                region.SetPoint(2, oX    , oY      );
                region.SetPoint(3, oX + r, oY - c  );
                break;
            case 7:
                region.SetPoint(0, oX + R, oY - b  );
                region.SetPoint(1, oX + R, oY      );
                region.SetPoint(2, oX + r, oY - c  );
                region.SetPoint(3, oX + r, oY - a-c);
                break;
            case 8:
                region.SetPoint(0, oX    , oY + a  );
                region.SetPoint(1, oX - r, oY + a+c);
                region.SetPoint(2, oX - R, oY + a  );
                region.SetPoint(3, oX - R, oY      );
                break;
            case 9:
                region.SetPoint(0, oX    , oY + a  );
                region.SetPoint(1, oX    , oY + a+b);
                region.SetPoint(2, oX - r, oY + b+c);
                region.SetPoint(3, oX - r, oY + a+c);
                break;
            case 10:
                region.SetPoint(0, oX + R, oY      );
                region.SetPoint(1, oX + R, oY + a  );
                region.SetPoint(2, oX + r, oY + a+c);
                region.SetPoint(3, oX    , oY + a  );
                break;
            case 11:
                region.SetPoint(0, oX + R, oY + a  );
                region.SetPoint(1, oX + R, oY + a+b);
                region.SetPoint(2, oX + r, oY + b+c);
                region.SetPoint(3, oX + r, oY + a+c);
                break;
            case 12:
                region.SetPoint(0, oX - r, oY + a+c);
                region.SetPoint(1, oX - r, oY + b+c);
                region.SetPoint(2, oX - R, oY + a+b);
                region.SetPoint(3, oX - R, oY + a  );
                break;
            case 13:
                region.SetPoint(0, oX    , oY + a+b);
                region.SetPoint(1, oX - R, oY + b*2);
                region.SetPoint(2, oX - R, oY + a+b);
                region.SetPoint(3, oX - r, oY + b+c);
                break;
            case 14:
                region.SetPoint(0, oX + r, oY + a+c);
                region.SetPoint(1, oX + r, oY + b+c);
                region.SetPoint(2, oX    , oY + a+b);
                region.SetPoint(3, oX    , oY + a  );
                break;
            case 15:
                region.SetPoint(0, oX + R, oY + a+b);
                region.SetPoint(1, oX + R, oY + b*2);
                region.SetPoint(2, oX    , oY + a+b);
                region.SetPoint(3, oX + r, oY + b+c);
                break;
            }
        }

        public override RectDouble GetRcInner(double borderWidth) {
            var attr = Attr;
            var a = attr.A;
            var b = attr.B;
            var c = attr.C;
            var R = attr.ROut;
            var r = attr.RIn;
            //var w = borderWidth / 2.0;
            var sq  = attr.GetSq(borderWidth);
            var sq2 = sq / 2;

            var oX = (R * 2) * (coord.x / 4) + R; // offset X
            var oY = (a * 6) * (coord.y / 4) + a + b; // offset Y

            var center = new PointDouble(); // координата центра квадрата
            switch (direction) {
            case 0:  center.X = oX - r*1.50; center.Y = oY - a;      break;
            case 1:  center.X = oX - r*0.75; center.Y = oY - c*4.25; break;
            case 2:  center.X = oX + r*0.50; center.Y = oY - a;      break;
            case 3:  center.X = oX + r*0.75; center.Y = oY - c*4.25; break;
            case 4:  center.X = oX - r*0.75; center.Y = oY + c*0.25; break;
            case 5:  center.X = oX - r*0.50; center.Y = oY - a;      break;
            case 6:  center.X = oX + r*0.75; center.Y = oY + c*0.25; break;
            case 7:  center.X = oX + r*1.50; center.Y = oY - a;      break;
            case 8:  center.X = oX - r*1.25; center.Y = oY + c*1.75; break;
            case 9:  center.X = oX - r*0.50; center.Y = oY + b;      break;
            case 10: center.X = oX + r*1.25; center.Y = oY + c*1.75; break;
            case 11: center.X = oX + r*1.50; center.Y = oY + b;      break;
            case 12: center.X = oX - r*1.50; center.Y = oY + b;      break;
            case 13: center.X = oX - r*1.25; center.Y = oY + c*6.25; break;
            case 14: center.X = oX + r*0.50; center.Y = oY + b;      break;
            case 15: center.X = oX + r*1.25; center.Y = oY + c*6.25; break;
            }

            return new RectDouble(
                center.X - sq2,
                center.Y - sq2,
                sq, sq);
        }

        public override int GetShiftPointBorderIndex() {
            switch (direction) {
            case 1: case 10: return 3;
            case 6: case 13: return 1;
            }
            return 2;
        }

    }

}
