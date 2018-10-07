////////////////////////////////////////////////////////////////////////////////
//                               FastMines project
// file name: "Parquet2.cs"
//
// Реализация класса Parquet2 - ещё один паркет
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
using fmg.common.geom;

namespace fmg.core.mosaic.cells {

    /// <summary> Паркет. Вариант №2 </summary>
    public class Parquet2 : BaseCell {

        public class AttrParquet2 : BaseAttribute {

            public override SizeDouble GetSize(Matrisize sizeField) {
                var a = A;
                var result = new SizeDouble(
                      (sizeField.m * 2 + 2) * a,
                      (sizeField.n * 2 + 2) * a);

                if (sizeField.m == 1)
                    result.Height -= a;

                return result;
            }

            public override int GetNeighborNumber(int direction) { return 7; }
            public override int GetVertexNumber(int direction) { return 4; }
            public override double GetVertexIntersection() { return 3.5; } // (4+4+3+3) / 4
            public override Size GetDirectionSizeField() { return new Size(2, 2); }
            public override double A => Math.Sqrt(Area) / 2;
            public override double GetSq(double borderWidth) {
                var w = borderWidth / 2.0;
                return A - w * SQRT2;
            }
        }

        public Parquet2(AttrParquet2 attr, Coord coord)
            : base(attr, coord,
                       ((coord.y & 1) << 1) + (coord.x & 1) // 0..3
                  )
        { }

        private new AttrParquet2 Attr => (AttrParquet2)base.Attr;

        protected override IList<Coord> GetCoordsNeighbor() {
            var neighborCoord = new Coord[Attr.GetNeighborNumber(GetDirection())];

            // определяю координаты соседей
            switch (direction) {
            case 0:
                neighborCoord[0] = new Coord(coord.x - 1, coord.y - 1);
                neighborCoord[1] = new Coord(coord.x    , coord.y - 1);
                neighborCoord[2] = new Coord(coord.x + 1, coord.y - 1);
                neighborCoord[3] = new Coord(coord.x - 1, coord.y);
                neighborCoord[4] = new Coord(coord.x + 1, coord.y);
                neighborCoord[5] = new Coord(coord.x    , coord.y + 1);
                neighborCoord[6] = new Coord(coord.x + 1, coord.y + 1);
                break;
            case 1:
                neighborCoord[0] = new Coord(coord.x    , coord.y - 1);
                neighborCoord[1] = new Coord(coord.x + 1, coord.y - 1);
                neighborCoord[2] = new Coord(coord.x - 1, coord.y);
                neighborCoord[3] = new Coord(coord.x + 1, coord.y);
                neighborCoord[4] = new Coord(coord.x - 1, coord.y + 1);
                neighborCoord[5] = new Coord(coord.x    , coord.y + 1);
                neighborCoord[6] = new Coord(coord.x + 1, coord.y + 1);
                break;
            case 2:
                neighborCoord[0] = new Coord(coord.x - 1, coord.y - 1);
                neighborCoord[1] = new Coord(coord.x    , coord.y - 1);
                neighborCoord[2] = new Coord(coord.x + 1, coord.y - 1);
                neighborCoord[3] = new Coord(coord.x - 1, coord.y);
                neighborCoord[4] = new Coord(coord.x + 1, coord.y);
                neighborCoord[5] = new Coord(coord.x - 1, coord.y + 1);
                neighborCoord[6] = new Coord(coord.x    , coord.y + 1);
                break;
            case 3:
                neighborCoord[0] = new Coord(coord.x - 1, coord.y - 1);
                neighborCoord[1] = new Coord(coord.x    , coord.y - 1);
                neighborCoord[2] = new Coord(coord.x - 1, coord.y);
                neighborCoord[3] = new Coord(coord.x + 1, coord.y);
                neighborCoord[4] = new Coord(coord.x - 1, coord.y + 1);
                neighborCoord[5] = new Coord(coord.x    , coord.y + 1);
                neighborCoord[6] = new Coord(coord.x + 1, coord.y + 1);
                break;
            }

            return neighborCoord;
        }

        protected override void CalcRegion() {
            var attr = Attr;
            var a = attr.A;

            switch (direction) {
            case 0:
                region.SetPoint(0, (2 * coord.x + 2) * a, (2 * coord.y + 0) * a);
                region.SetPoint(1, (2 * coord.x + 4) * a, (2 * coord.y + 2) * a);
                region.SetPoint(2, (2 * coord.x + 3) * a, (2 * coord.y + 3) * a);
                region.SetPoint(3, (2 * coord.x + 1) * a, (2 * coord.y + 1) * a);
                break;
            case 1:
                region.SetPoint(0, (2 * coord.x + 3) * a, (2 * coord.y + 1) * a);
                region.SetPoint(1, (2 * coord.x + 4) * a, (2 * coord.y + 2) * a);
                region.SetPoint(2, (2 * coord.x + 2) * a, (2 * coord.y + 4) * a);
                region.SetPoint(3, (2 * coord.x + 1) * a, (2 * coord.y + 3) * a);
                break;
            case 2:
                region.SetPoint(0, (2 * coord.x + 2) * a, (2 * coord.y + 0) * a);
                region.SetPoint(1, (2 * coord.x + 3) * a, (2 * coord.y + 1) * a);
                region.SetPoint(2, (2 * coord.x + 1) * a, (2 * coord.y + 3) * a);
                region.SetPoint(3, (2 * coord.x + 0) * a, (2 * coord.y + 2) * a);
                break;
            case 3:
                region.SetPoint(0, (2 * coord.x + 1) * a, (2 * coord.y + 1) * a);
                region.SetPoint(1, (2 * coord.x + 3) * a, (2 * coord.y + 3) * a);
                region.SetPoint(2, (2 * coord.x + 2) * a, (2 * coord.y + 4) * a);
                region.SetPoint(3, (2 * coord.x + 0) * a, (2 * coord.y + 2) * a);
                break;
            }
        }

        public override RectDouble GetRcInner(double borderWidth) {
            var attr = Attr;
            var sq = attr.GetSq(borderWidth);
            var w = borderWidth / 2.0;

            var square = new RectDouble();
            switch (direction) {
            case 0:
            case 3:
                square.X = region.GetPoint(0).X + w / SQRT2;
                square.Y = region.GetPoint(3).Y + w / SQRT2;
                break;
            case 1:
            case 2:
                square.X = region.GetPoint(2).X + w / SQRT2;
                square.Y = region.GetPoint(1).Y + w / SQRT2;
                break;
            }
            square.Width = sq;
            square.Height = sq;
            return square;
        }

        public override int GetShiftPointBorderIndex() { return 2; }

    }

}
