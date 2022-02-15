////////////////////////////////////////////////////////////////////////////////
//                               FastMines project
// file name: "PentagonT24.cs"
//
// Реализация класса PentagonT24 - равносторонний 5-ти угольник, тип №2 и №4
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

    /// <summary> Пятиугольник. Тип №2 и №4 - равносторонний </summary>
    public class PentagonT24 : BaseCell {

        public PentagonT24(ShapePentagonT24 shape, Coord coord)
            : base(shape, coord,
                       ((coord.y & 1) << 1) + (coord.x & 1) // 0..3
                  )
        { }

        private new ShapePentagonT24 Shape => (ShapePentagonT24)base.Shape;

        public override IList<Coord> GetCoordsNeighbor() {
            var neighborCoord = new Coord[Shape.GetNeighborNumber(GetDirection())];

            // определяю координаты соседей
            switch (direction) {
            case 0:
                neighborCoord[0] = new Coord(coord.x - 1, coord.y - 1);
                neighborCoord[1] = new Coord(coord.x    , coord.y - 1);
                neighborCoord[2] = new Coord(coord.x - 1, coord.y    );
                neighborCoord[3] = new Coord(coord.x + 1, coord.y    );
                neighborCoord[4] = new Coord(coord.x - 1, coord.y + 1);
                neighborCoord[5] = new Coord(coord.x    , coord.y + 1);
                neighborCoord[6] = new Coord(coord.x + 1, coord.y + 1);
                break;
            case 1:
                neighborCoord[0] = new Coord(coord.x - 1, coord.y - 1);
                neighborCoord[1] = new Coord(coord.x    , coord.y - 1);
                neighborCoord[2] = new Coord(coord.x + 1, coord.y - 1);
                neighborCoord[3] = new Coord(coord.x - 1, coord.y    );
                neighborCoord[4] = new Coord(coord.x + 1, coord.y    );
                neighborCoord[5] = new Coord(coord.x - 1, coord.y + 1);
                neighborCoord[6] = new Coord(coord.x    , coord.y + 1);
                break;
            case 2:
                neighborCoord[0] = new Coord(coord.x    , coord.y - 1);
                neighborCoord[1] = new Coord(coord.x + 1, coord.y - 1);
                neighborCoord[2] = new Coord(coord.x - 1, coord.y    );
                neighborCoord[3] = new Coord(coord.x + 1, coord.y    );
                neighborCoord[4] = new Coord(coord.x - 1, coord.y + 1);
                neighborCoord[5] = new Coord(coord.x    , coord.y + 1);
                neighborCoord[6] = new Coord(coord.x + 1, coord.y + 1);
                break;
            case 3:
                neighborCoord[0] = new Coord(coord.x - 1, coord.y - 1);
                neighborCoord[1] = new Coord(coord.x    , coord.y - 1);
                neighborCoord[2] = new Coord(coord.x + 1, coord.y - 1);
                neighborCoord[3] = new Coord(coord.x - 1, coord.y    );
                neighborCoord[4] = new Coord(coord.x + 1, coord.y    );
                neighborCoord[5] = new Coord(coord.x    , coord.y + 1);
                neighborCoord[6] = new Coord(coord.x + 1, coord.y + 1);
                break;
            }

            return neighborCoord;
        }

        protected override void CalcRegion() {
            var shape = Shape;
            var a = shape.A;
            var b = shape.B;
            var c = shape.C;

            // определение координат точек фигуры
            var oX = a * ((coord.x >> 1) << 1); // offset X
            var oY = a * ((coord.y >> 1) << 1); // offset Y
            switch (direction) {
            case 0:
                region.SetPoint(0, oX +         a, oY + b        );
                region.SetPoint(1, oX + c +     a, oY + c +     a);
                region.SetPoint(2, oX + b        , oY + b +     a);
                region.SetPoint(3, oX            , oY +         a);
                region.SetPoint(4, oX + c        , oY + c        );
                break;
            case 1:
                region.SetPoint(0, oX + c + 2 * a, oY + c        );
                region.SetPoint(1, oX +     2 * a, oY +         a);
                region.SetPoint(2, oX + c +     a, oY + c +     a);
                region.SetPoint(3, oX +         a, oY + b        );
                region.SetPoint(4, oX + b +     a, oY            );
                break;
            case 2:
                region.SetPoint(0, oX + c +     a, oY + c +     a);
                region.SetPoint(1, oX + b +     a, oY +     2 * a);
                region.SetPoint(2, oX +         a, oY + b + 2 * a);
                region.SetPoint(3, oX + c        , oY + c + 2 * a);
                region.SetPoint(4, oX + b        , oY + b +     a);
                break;
            case 3:
                region.SetPoint(0, oX +     2 * a, oY +         a);
                region.SetPoint(1, oX + b + 2 * a, oY + b +     a);
                region.SetPoint(2, oX + c + 2 * a, oY + c + 2 * a);
                region.SetPoint(3, oX + b +     a, oY +     2 * a);
                region.SetPoint(4, oX + c +     a, oY + c +     a);
                break;
            }
        }

        public override RectDouble GetRcInner(double borderWidth) {
            var shape = Shape;
            var sq = shape.GetSq(borderWidth);
            var w = borderWidth / 2.0;
            var w2 = w / BaseShape.SQRT2;

            var square = new RectDouble();
            switch (direction) {
            case 0:
                square.X = region.GetPoint(4).X + w2;
                square.Y = region.GetPoint(1).Y - w2 - sq;
                break;
            case 1:
                square.X = region.GetPoint(2).X + w2;
                square.Y = region.GetPoint(0).Y + w2;
                break;
            case 2:
                square.X = region.GetPoint(0).X - w2 - sq;
                square.Y = region.GetPoint(3).Y - w2 - sq;
                break;
            case 3:
                square.X = region.GetPoint(2).X - w2 - sq;
                square.Y = region.GetPoint(4).Y + w2;
                break;
            }
            square.Width = sq;
            square.Height = sq;
            return square;
        }

        public override int GetShiftPointBorderIndex() {
            switch (direction) {
            case 0:
            case 1:
                return 2;
            }
            return 3;
        }

    }

}
