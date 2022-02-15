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
using System.Collections.Generic;
using Fmg.Common.Geom;
using Fmg.Core.Mosaic.Shape;

namespace Fmg.Core.Mosaic.Cells {

    /// <summary> Квадрат. Вариант 2 - сдвинутые ряды </summary>
    public class Square2 : BaseCell {

        public Square2(ShapeSquare2 shape, Coord coord)
            : base(shape, coord,
                         coord.y & 1 // 0..1
                  )
        { }

        private new ShapeSquare2 Shape => (ShapeSquare2)base.Shape;

        public override IList<Coord> GetCoordsNeighbor() {
            var neighborCoord = new Coord[Shape.GetNeighborNumber(GetDirection())];

            // определяю координаты соседей
            neighborCoord[0] = new Coord(coord.x -  direction     , coord.y - 1);
            neighborCoord[1] = new Coord(coord.x + (direction ^ 1), coord.y - 1);
            neighborCoord[2] = new Coord(coord.x - 1              , coord.y    );
            neighborCoord[3] = new Coord(coord.x + 1              , coord.y    );
            neighborCoord[4] = new Coord(coord.x -  direction     , coord.y + 1);
            neighborCoord[5] = new Coord(coord.x + (direction ^ 1), coord.y + 1);

            return neighborCoord;
        }

        public override bool PointInRegion(PointDouble point) {
            if ((point.X < region.GetPoint(3).X) || (point.X >= region.GetPoint(0).X) ||
                (point.Y < region.GetPoint(0).Y) || (point.Y >= region.GetPoint(2).Y))
                return false;
            return true;
        }

        protected override void CalcRegion() {
            var shape = Shape;
            var a = shape.A;

            var x1 = a * (coord.x + 0) + ((direction != 0) ? 0 : a / 2);
            var x2 = a * (coord.x + 1) + ((direction != 0) ? 0 : a / 2);
            var y1 = a * (coord.y + 0);
            var y2 = a * (coord.y + 1);

            region.SetPoint(0, x2, y1);
            region.SetPoint(1, x2, y2);
            region.SetPoint(2, x1, y2);
            region.SetPoint(3, x1, y1);
        }

        public override RectDouble GetRcInner(double borderWidth) {
            var shape = Shape;
            var sq = shape.GetSq(borderWidth);
            var w = borderWidth / 2.0;

            return new RectDouble(
               region.GetPoint(3).X + w,
               region.GetPoint(3).Y + w,
               sq, sq);
        }

        public override int GetShiftPointBorderIndex() { return 2; }

    }

}
