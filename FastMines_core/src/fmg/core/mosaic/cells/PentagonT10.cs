////////////////////////////////////////////////////////////////////////////////
//                               FastMines project
// file name: "PentagonT10.cs"
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

namespace Fmg.Core.Mosaic.Cells {

    /// <summary> Пятиугольник. Тип №10 </summary>
    public class PentagonT10 : BaseCell {

        public class AttrPentagonT10 : BaseAttribute {

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

        public PentagonT10(AttrPentagonT10 attr, Coord coord)
            : base(attr, coord,
                       ((coord.y % 6) << 1) + (coord.x & 1) // 0..11
                  )
        { }

        private new AttrPentagonT10 Attr => (AttrPentagonT10)base.Attr;

        public override IList<Coord> GetCoordsNeighbor() {
            var neighborCoord = new Coord[Attr.GetNeighborNumber(GetDirection())];

            // определяю координаты соседей
            switch (direction) {
            case 0:
                neighborCoord[0] = new Coord(coord.x    , coord.y - 1);
                neighborCoord[1] = new Coord(coord.x - 1, coord.y    );
                neighborCoord[2] = new Coord(coord.x + 1, coord.y    );
                neighborCoord[3] = new Coord(coord.x - 1, coord.y + 1);
                neighborCoord[4] = new Coord(coord.x    , coord.y + 1);
                neighborCoord[5] = new Coord(coord.x + 1, coord.y + 1);
                neighborCoord[6] = new Coord(coord.x - 1, coord.y + 2);
                break;
            case 1:
                neighborCoord[0] = new Coord(coord.x    , coord.y - 2);
                neighborCoord[1] = new Coord(coord.x - 1, coord.y - 1);
                neighborCoord[2] = new Coord(coord.x    , coord.y - 1);
                neighborCoord[3] = new Coord(coord.x + 1, coord.y - 1);
                neighborCoord[4] = new Coord(coord.x - 1, coord.y    );
                neighborCoord[5] = new Coord(coord.x + 1, coord.y    );
                neighborCoord[6] = new Coord(coord.x    , coord.y + 1);
                break;
            case 2:
                neighborCoord[0] = new Coord(coord.x    , coord.y - 1);
                neighborCoord[1] = new Coord(coord.x + 1, coord.y    );
                neighborCoord[2] = new Coord(coord.x - 1, coord.y + 1);
                neighborCoord[3] = new Coord(coord.x    , coord.y + 1);
                neighborCoord[4] = new Coord(coord.x + 1, coord.y + 1);
                neighborCoord[5] = new Coord(coord.x    , coord.y + 2);
                break;
            case 3:
                neighborCoord[0] = new Coord(coord.x - 1, coord.y - 1);
                neighborCoord[1] = new Coord(coord.x    , coord.y - 1);
                neighborCoord[2] = new Coord(coord.x + 1, coord.y - 1);
                neighborCoord[3] = new Coord(coord.x - 1, coord.y    );
                neighborCoord[4] = new Coord(coord.x - 1, coord.y + 1);
                neighborCoord[5] = new Coord(coord.x    , coord.y + 1);
                break;
            case 4:
                neighborCoord[0] = new Coord(coord.x    , coord.y - 1);
                neighborCoord[1] = new Coord(coord.x + 1, coord.y - 1);
                neighborCoord[2] = new Coord(coord.x + 1, coord.y    );
                neighborCoord[3] = new Coord(coord.x    , coord.y + 1);
                neighborCoord[4] = new Coord(coord.x + 1, coord.y + 1);
                neighborCoord[5] = new Coord(coord.x + 2, coord.y + 1);
                break;
            case 5:
                neighborCoord[0] = new Coord(coord.x + 1, coord.y - 2);
                neighborCoord[1] = new Coord(coord.x - 1, coord.y - 1);
                neighborCoord[2] = new Coord(coord.x    , coord.y - 1);
                neighborCoord[3] = new Coord(coord.x + 1, coord.y - 1);
                neighborCoord[4] = new Coord(coord.x - 1, coord.y    );
                neighborCoord[5] = new Coord(coord.x + 1, coord.y + 1);
                break;
            case 6:
                neighborCoord[0] = new Coord(coord.x    , coord.y - 2);
                neighborCoord[1] = new Coord(coord.x - 2, coord.y - 1);
                neighborCoord[2] = new Coord(coord.x - 1, coord.y - 1);
                neighborCoord[3] = new Coord(coord.x    , coord.y - 1);
                neighborCoord[4] = new Coord(coord.x - 1, coord.y    );
                neighborCoord[5] = new Coord(coord.x + 1, coord.y    );
                neighborCoord[6] = new Coord(coord.x    , coord.y + 1);
                break;
            case 7:
                neighborCoord[0] = new Coord(coord.x - 1, coord.y - 1);
                neighborCoord[1] = new Coord(coord.x - 1, coord.y    );
                neighborCoord[2] = new Coord(coord.x + 1, coord.y    );
                neighborCoord[3] = new Coord(coord.x - 1, coord.y + 1);
                neighborCoord[4] = new Coord(coord.x    , coord.y + 1);
                neighborCoord[5] = new Coord(coord.x + 1, coord.y + 1);
                neighborCoord[6] = new Coord(coord.x    , coord.y + 2);
                break;
            case 8:
                neighborCoord[0] = new Coord(coord.x - 1, coord.y - 1);
                neighborCoord[1] = new Coord(coord.x    , coord.y - 1);
                neighborCoord[2] = new Coord(coord.x + 1, coord.y - 1);
                neighborCoord[3] = new Coord(coord.x + 1, coord.y    );
                neighborCoord[4] = new Coord(coord.x - 1, coord.y + 1);
                neighborCoord[5] = new Coord(coord.x    , coord.y + 1);
                break;
            case 9:
                neighborCoord[0] = new Coord(coord.x    , coord.y - 1);
                neighborCoord[1] = new Coord(coord.x - 1, coord.y    );
                neighborCoord[2] = new Coord(coord.x - 2, coord.y + 1);
                neighborCoord[3] = new Coord(coord.x - 1, coord.y + 1);
                neighborCoord[4] = new Coord(coord.x    , coord.y + 1);
                neighborCoord[5] = new Coord(coord.x    , coord.y + 2);
                break;
            case 10:
                neighborCoord[0] = new Coord(coord.x    , coord.y - 1);
                neighborCoord[1] = new Coord(coord.x + 1, coord.y - 1);
                neighborCoord[2] = new Coord(coord.x - 1, coord.y    );
                neighborCoord[3] = new Coord(coord.x - 1, coord.y + 1);
                neighborCoord[4] = new Coord(coord.x    , coord.y + 1);
                neighborCoord[5] = new Coord(coord.x + 1, coord.y + 1);
                break;
            case 11:
                neighborCoord[0] = new Coord(coord.x    , coord.y - 2);
                neighborCoord[1] = new Coord(coord.x    , coord.y - 1);
                neighborCoord[2] = new Coord(coord.x + 1, coord.y - 1);
                neighborCoord[3] = new Coord(coord.x + 2, coord.y - 1);
                neighborCoord[4] = new Coord(coord.x + 1, coord.y    );
                neighborCoord[5] = new Coord(coord.x    , coord.y + 1);
                break;
            }

            return neighborCoord;
        }

        private PointDouble getOffset() {
            var attr = Attr;
            var a = attr.A;

            var o = new PointDouble(0, 0);
            switch (direction) {
            case 0: case 6: case 8: case 9: case 10:         o.X = a * 2 + a * 6 * ((coord.x + 0) / 2); break;
            case 1: case 2: case 3: case 4: case 5 : case 7: o.X = a * 5 + a * 6 * ((coord.x + 0) / 2); break;
            case 11:                                         o.X = a * 2 + a * 6 * ((coord.x + 1) / 2); break;
            }
            switch (direction) {
            case 0:                           o.Y = a *  5 + a * 14 * (coord.y / 6); break;
            case 1:                           o.Y =          a * 14 * (coord.y / 6); break;
            case 2: case 3: case 4: case 5:   o.Y = a *  6 + a * 14 * (coord.y / 6); break;
            case 6:                           o.Y = a *  7 + a * 14 * (coord.y / 6); break;
            case 7:                           o.Y = a * 12 + a * 14 * (coord.y / 6); break;
            case 8: case 9: case 10: case 11: o.Y = a * 13 + a * 14 * (coord.y / 6); break;
            }
            return o;
        }

        protected override void CalcRegion() {
            var attr = Attr;
            var a = attr.A;

            var o = getOffset();

            switch (direction) {
            case 0:
            case 3:
            case 7:
            case 8:
                region.SetPoint(0, o.X + a    , o.Y - a * 3);
                region.SetPoint(1, o.X + a * 2, o.Y - a * 2);
                region.SetPoint(2, o.X        , o.Y        );
                region.SetPoint(3, o.X - a * 2, o.Y - a * 2);
                region.SetPoint(4, o.X - a    , o.Y - a * 3);
                break;
            case 1:
            case 4:
            case 6:
            case 10:
                region.SetPoint(0, o.X        , o.Y        );
                region.SetPoint(1, o.X + a * 2, o.Y + a * 2);
                region.SetPoint(2, o.X + a    , o.Y + a * 3);
                region.SetPoint(3, o.X - a    , o.Y + a * 3);
                region.SetPoint(4, o.X - a * 2, o.Y + a * 2);
                break;
            case 2:
            case 11:
                region.SetPoint(0, o.X - a * 2, o.Y - a * 2);
                region.SetPoint(1, o.X        , o.Y        );
                region.SetPoint(2, o.X - a * 2, o.Y + a * 2);
                region.SetPoint(3, o.X - a * 3, o.Y + a    );
                region.SetPoint(4, o.X - a * 3, o.Y - a    );
                break;
            case 5:
            case 9:
                region.SetPoint(0, o.X + a * 2, o.Y - a * 2);
                region.SetPoint(1, o.X + a * 3, o.Y - a    );
                region.SetPoint(2, o.X + a * 3, o.Y + a    );
                region.SetPoint(3, o.X + a * 2, o.Y + a * 2);
                region.SetPoint(4, o.X        , o.Y        );
                break;
            }
        }

        public override RectDouble GetRcInner(double borderWidth) {
            var attr = Attr;
            var sq = attr.GetSq(borderWidth);
            var sq2 = sq / 2;

            var center = new PointDouble(); // координата центра квадрата
            switch (direction) {
            case 0: case 3: case 7: case  8:                  center.X = region.GetPoint(2).X; center.Y = region.GetPoint(1).Y; break;
            case 1: case 4: case 6: case 10: case 2: case 11: center.X = region.GetPoint(0).X; center.Y = region.GetPoint(1).Y; break;
            case 5: case 9:                                   center.X = region.GetPoint(0).X; center.Y = region.GetPoint(4).Y; break;
            }

            return new RectDouble(
               center.X - sq2,
               center.Y - sq2,
               sq, sq);
        }

        public override int GetShiftPointBorderIndex() {
            switch (direction) {
            case 1: case 4: case 5: case 6: case 9: case 10:
                return 3;
            }
            return 2;
        }

        public override Color GetCellFillColor(int fillMode, Color defaultColor, Func<int, Color> repositoryColor) {
            if (fillMode == Attr.GetMaxCellFillModeValue()) {
                switch (GetDirection()) {
                case 2: case 3: case  4: case  5: return repositoryColor(0);
                case 8: case 9: case 10: case 11: return repositoryColor(1);
                case 1: case 7:                   return repositoryColor(2);
                case 0: case 6:                   return repositoryColor(3);
                //default:                        return repositoryColor(-1);
                }
            }
            return base.GetCellFillColor(fillMode, defaultColor, repositoryColor);
        }

    }

}
