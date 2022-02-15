////////////////////////////////////////////////////////////////////////////////
//                               FastMines project
// file name: "Quadrangle1.cs"
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
using System.Collections.Generic;
using Fmg.Common.Geom;
using Fmg.Core.Mosaic.Shape;

namespace Fmg.Core.Mosaic.Cells {

    /// <summary> Quadrangle1 - четырёхугольник 120°-90°-60°-90° </summary>
    public class Quadrangle1 : BaseCell {

        public Quadrangle1(ShapeQuadrangle1 shape, Coord coord)
            : base(shape, coord,
                        (coord.y & 3) * 3 + (coord.x % 3) // 0..11
                  )
        { }

        private new ShapeQuadrangle1 Shape => (ShapeQuadrangle1)base.Shape;

        public override IList<Coord> GetCoordsNeighbor() {
            var neighborCoord = new Coord[Shape.GetNeighborNumber(GetDirection())];

            // определяю координаты соседей
            switch (direction) {
            case 0:
                neighborCoord[0] = new Coord(coord.x - 1, coord.y - 1);
                neighborCoord[1] = new Coord(coord.x    , coord.y - 1);
                neighborCoord[2] = new Coord(coord.x - 1, coord.y    );
                neighborCoord[3] = new Coord(coord.x + 1, coord.y    );
                neighborCoord[4] = new Coord(coord.x + 2, coord.y    );
                neighborCoord[5] = new Coord(coord.x - 1, coord.y + 1);
                neighborCoord[6] = new Coord(coord.x    , coord.y + 1);
                neighborCoord[7] = new Coord(coord.x + 1, coord.y + 1);
                neighborCoord[8] = new Coord(coord.x + 2, coord.y + 1);
                break;
            case 1:
                neighborCoord[0] = new Coord(coord.x - 2, coord.y - 1);
                neighborCoord[1] = new Coord(coord.x - 1, coord.y - 1);
                neighborCoord[2] = new Coord(coord.x    , coord.y - 1);
                neighborCoord[3] = new Coord(coord.x + 1, coord.y - 1);
                neighborCoord[4] = new Coord(coord.x - 1, coord.y    );
                neighborCoord[5] = new Coord(coord.x + 1, coord.y    );
                neighborCoord[6] = new Coord(coord.x - 1, coord.y + 1);
                neighborCoord[7] = new Coord(coord.x    , coord.y + 1);
                neighborCoord[8] = new Coord(coord.x + 1, coord.y + 1);
                break;
            case 2:
                neighborCoord[0] = new Coord(coord.x - 1, coord.y - 1);
                neighborCoord[1] = new Coord(coord.x    , coord.y - 1);
                neighborCoord[2] = new Coord(coord.x - 2, coord.y    );
                neighborCoord[3] = new Coord(coord.x - 1, coord.y    );
                neighborCoord[4] = new Coord(coord.x + 1, coord.y    );
                neighborCoord[5] = new Coord(coord.x - 2, coord.y + 1);
                neighborCoord[6] = new Coord(coord.x - 1, coord.y + 1);
                neighborCoord[7] = new Coord(coord.x    , coord.y + 1);
                neighborCoord[8] = new Coord(coord.x + 1, coord.y + 1);
                break;
            case 3:
                neighborCoord[0] = new Coord(coord.x - 1, coord.y - 1);
                neighborCoord[1] = new Coord(coord.x    , coord.y - 1);
                neighborCoord[2] = new Coord(coord.x + 1, coord.y - 1);
                neighborCoord[3] = new Coord(coord.x + 2, coord.y - 1);
                neighborCoord[4] = new Coord(coord.x - 1, coord.y    );
                neighborCoord[5] = new Coord(coord.x + 1, coord.y    );
                neighborCoord[6] = new Coord(coord.x + 2, coord.y    );
                neighborCoord[7] = new Coord(coord.x - 1, coord.y + 1);
                neighborCoord[8] = new Coord(coord.x    , coord.y + 1);
                break;
            case 4:
                neighborCoord[0] = new Coord(coord.x - 1, coord.y - 1);
                neighborCoord[1] = new Coord(coord.x    , coord.y - 1);
                neighborCoord[2] = new Coord(coord.x + 1, coord.y - 1);
                neighborCoord[3] = new Coord(coord.x - 1, coord.y    );
                neighborCoord[4] = new Coord(coord.x + 1, coord.y    );
                neighborCoord[5] = new Coord(coord.x - 2, coord.y + 1);
                neighborCoord[6] = new Coord(coord.x - 1, coord.y + 1);
                neighborCoord[7] = new Coord(coord.x    , coord.y + 1);
                neighborCoord[8] = new Coord(coord.x + 1, coord.y + 1);
                break;
            case 5:
                neighborCoord[0] = new Coord(coord.x - 2, coord.y - 1);
                neighborCoord[1] = new Coord(coord.x - 1, coord.y - 1);
                neighborCoord[2] = new Coord(coord.x    , coord.y - 1);
                neighborCoord[3] = new Coord(coord.x + 1, coord.y - 1);
                neighborCoord[4] = new Coord(coord.x - 2, coord.y    );
                neighborCoord[5] = new Coord(coord.x - 1, coord.y    );
                neighborCoord[6] = new Coord(coord.x + 1, coord.y    );
                neighborCoord[7] = new Coord(coord.x - 1, coord.y + 1);
                neighborCoord[8] = new Coord(coord.x    , coord.y + 1);
                break;
            case 6:
                neighborCoord[0] = new Coord(coord.x    , coord.y - 1);
                neighborCoord[1] = new Coord(coord.x + 1, coord.y - 1);
                neighborCoord[2] = new Coord(coord.x - 2, coord.y    );
                neighborCoord[3] = new Coord(coord.x - 1, coord.y    );
                neighborCoord[4] = new Coord(coord.x + 1, coord.y    );
                neighborCoord[5] = new Coord(coord.x - 2, coord.y + 1);
                neighborCoord[6] = new Coord(coord.x - 1, coord.y + 1);
                neighborCoord[7] = new Coord(coord.x    , coord.y + 1);
                neighborCoord[8] = new Coord(coord.x + 1, coord.y + 1);
                break;
            case 7:
                neighborCoord[0] = new Coord(coord.x    , coord.y - 1);
                neighborCoord[1] = new Coord(coord.x + 1, coord.y - 1);
                neighborCoord[2] = new Coord(coord.x - 1, coord.y    );
                neighborCoord[3] = new Coord(coord.x + 1, coord.y    );
                neighborCoord[4] = new Coord(coord.x + 2, coord.y    );
                neighborCoord[5] = new Coord(coord.x - 1, coord.y + 1);
                neighborCoord[6] = new Coord(coord.x    , coord.y + 1);
                neighborCoord[7] = new Coord(coord.x + 1, coord.y + 1);
                neighborCoord[8] = new Coord(coord.x + 2, coord.y + 1);
                break;
            case 8:
                neighborCoord[0] = new Coord(coord.x - 1, coord.y - 1);
                neighborCoord[1] = new Coord(coord.x    , coord.y - 1);
                neighborCoord[2] = new Coord(coord.x + 1, coord.y - 1);
                neighborCoord[3] = new Coord(coord.x + 2, coord.y - 1);
                neighborCoord[4] = new Coord(coord.x - 1, coord.y    );
                neighborCoord[5] = new Coord(coord.x + 1, coord.y    );
                neighborCoord[6] = new Coord(coord.x - 1, coord.y + 1);
                neighborCoord[7] = new Coord(coord.x    , coord.y + 1);
                neighborCoord[8] = new Coord(coord.x + 1, coord.y + 1);
                break;
            case 9:
                neighborCoord[0] = new Coord(coord.x - 2, coord.y - 1);
                neighborCoord[1] = new Coord(coord.x - 1, coord.y - 1);
                neighborCoord[2] = new Coord(coord.x    , coord.y - 1);
                neighborCoord[3] = new Coord(coord.x + 1, coord.y - 1);
                neighborCoord[4] = new Coord(coord.x - 2, coord.y    );
                neighborCoord[5] = new Coord(coord.x - 1, coord.y    );
                neighborCoord[6] = new Coord(coord.x + 1, coord.y    );
                neighborCoord[7] = new Coord(coord.x    , coord.y + 1);
                neighborCoord[8] = new Coord(coord.x + 1, coord.y + 1);
                break;
            case 10:
                neighborCoord[0] = new Coord(coord.x - 1, coord.y - 1);
                neighborCoord[1] = new Coord(coord.x    , coord.y - 1);
                neighborCoord[2] = new Coord(coord.x + 1, coord.y - 1);
                neighborCoord[3] = new Coord(coord.x + 2, coord.y - 1);
                neighborCoord[4] = new Coord(coord.x - 1, coord.y    );
                neighborCoord[5] = new Coord(coord.x + 1, coord.y    );
                neighborCoord[6] = new Coord(coord.x + 2, coord.y    );
                neighborCoord[7] = new Coord(coord.x    , coord.y + 1);
                neighborCoord[8] = new Coord(coord.x + 1, coord.y + 1);
                break;
            case 11:
                neighborCoord[0] = new Coord(coord.x - 1, coord.y - 1);
                neighborCoord[1] = new Coord(coord.x    , coord.y - 1);
                neighborCoord[2] = new Coord(coord.x + 1, coord.y - 1);
                neighborCoord[3] = new Coord(coord.x - 1, coord.y    );
                neighborCoord[4] = new Coord(coord.x + 1, coord.y    );
                neighborCoord[5] = new Coord(coord.x - 1, coord.y + 1);
                neighborCoord[6] = new Coord(coord.x    , coord.y + 1);
                neighborCoord[7] = new Coord(coord.x + 1, coord.y + 1);
                neighborCoord[8] = new Coord(coord.x + 2, coord.y + 1);
                break;
            }

           return neighborCoord;
        }

        protected override void CalcRegion() {
            var shape = Shape;
            var a = shape.A;
            var b = shape.B;
            var h = shape.H;
            var n = shape.N;
            var m = shape.M;

            // определение координат точек фигуры
            var oX = (h * 2) * (coord.x / 3) + h + m; // offset X
            var oY = (a * 3) * (coord.y / 4) + a + n; // offset Y

            switch (direction) {
            case 0:
                region.SetPoint(0, oX - h    , oY - n - n);
                region.SetPoint(1, oX - m    , oY - n    );
                region.SetPoint(2, oX - h - m, oY - n    );
                region.SetPoint(3, oX - h - m, oY - n - b);
                break;
            case 1:
                region.SetPoint(0, oX        , oY - n - n);
                region.SetPoint(1, oX - m    , oY - n    );
                region.SetPoint(2, oX - h    , oY - n - n);
                region.SetPoint(3, oX - m    , oY - n - a);
                break;
            case 2:
                region.SetPoint(0, oX + m    , oY - n - b);
                region.SetPoint(1, oX + m    , oY - n    );
                region.SetPoint(2, oX - m    , oY - n    );
                region.SetPoint(3, oX        , oY - n - n);
                break;
            case 3:
                region.SetPoint(0, oX - m    , oY - n    );
                region.SetPoint(1, oX - h    , oY        );
                region.SetPoint(2, oX - h - m, oY - n + b);
                region.SetPoint(3, oX - h - m, oY - n    );
                break;
            case 4:
                region.SetPoint(0, oX - m    , oY - n    );
                region.SetPoint(1, oX        , oY        );
                region.SetPoint(2, oX - m    , oY - n + a);
                region.SetPoint(3, oX - h    , oY        );
                break;
            case 5:
                region.SetPoint(0, oX + m    , oY - n    );
                region.SetPoint(1, oX + m    , oY - n + b);
                region.SetPoint(2, oX        , oY        );
                region.SetPoint(3, oX - m    , oY - n    );
                break;
            case 6:
                region.SetPoint(0, oX - m    , oY + n - b);
                region.SetPoint(1, oX - m    , oY + n    );
                region.SetPoint(2, oX - h - m, oY + n    );
                region.SetPoint(3, oX - h    , oY        );
                break;
            case 7:
                region.SetPoint(0, oX        , oY        );
                region.SetPoint(1, oX + m    , oY + n    );
                region.SetPoint(2, oX - m    , oY + n    );
                region.SetPoint(3, oX - m    , oY + n - b);
                break;
            case 8:
                region.SetPoint(0, oX + h    , oY        );
                region.SetPoint(1, oX + m    , oY + n    );
                region.SetPoint(2, oX        , oY        );
                region.SetPoint(3, oX + m    , oY + n - a);
                break;
            case 9:
                region.SetPoint(0, oX - m    , oY + n    );
                region.SetPoint(1, oX - m    , oY + n + b);
                region.SetPoint(2, oX - h    , oY + n + n);
                region.SetPoint(3, oX - h - m, oY + n    );
                break;
            case 10:
                region.SetPoint(0, oX + m    , oY + n    );
                region.SetPoint(1, oX        , oY + n + n);
                region.SetPoint(2, oX - m    , oY + n + b);
                region.SetPoint(3, oX - m    , oY + n    );
                break;
            case 11:
                region.SetPoint(0, oX + m    , oY + n    );
                region.SetPoint(1, oX + h    , oY + n + n);
                region.SetPoint(2, oX + m    , oY + n + a);
                region.SetPoint(3, oX        , oY + n + n);
                break;
            }
        }

        public override RectDouble GetRcInner(double borderWidth) {
            var shape = Shape;
            var a = shape.A;
            var b = shape.B;
            var h = shape.H;
            var n = shape.N;
            var m = shape.M;
            var z = shape.Z;
            var zx = shape.Zx;
            var zy = shape.Zy;
            //var w = borderWidth / 2.0;
            var sq    = shape.GetSq(borderWidth);
            var sq2   = sq/2;

            var oX = (h * 2) * (coord.x / 3) + h + m; // offset X
            var oY = (a * 3) * (coord.y / 4) + a + n; // offset Y

            var center = new PointDouble(); // координата центра квадрата
            switch (direction) {
            case 0:  center.X = oX - h -m + zx; center.Y = oY - n - b + zy; break;
            case 1:  center.X = oX - m;         center.Y = oY - n - a + z ; break;
            case 2:  center.X = oX + m    - zx; center.Y = oY - n - b + zy; break;
            case 3:  center.X = oX - h -m + zx; center.Y = oY - n + b - zy; break;
            case 4:  center.X = oX - m;         center.Y = oY - n + a - z ; break;
            case 5:  center.X = oX + m    - zx; center.Y = oY - n + b - zy; break;
            case 6:  center.X = oX - m    - zx; center.Y = oY + n - b + zy; break;
            case 7:  center.X = oX - m    + zx; center.Y = oY + n - b + zy; break;
            case 8:  center.X = oX + m;         center.Y = oY + n - a + z ; break;
            case 9:  center.X = oX - m    - zx; center.Y = oY + n + b - zy; break;
            case 10: center.X = oX - m    + zx; center.Y = oY + n + b - zy; break;
            case 11: center.X = oX + m;         center.Y = oY + n + a - z ; break;
            }

            return new RectDouble(
                center.X - sq2,
                center.Y - sq2,
                sq, sq);
        }

        public override int GetShiftPointBorderIndex() {
           switch (direction) {
           case 1: case  8: return 1;
           case 4: case 11: return 3;
           }
           return 2;
        }

    }

}
