////////////////////////////////////////////////////////////////////////////////
//                               FastMines project
// file name: "SqTrHex.cs"
//
// Реализация класса SqTrHex - мозаика из 6Square 4Triangle 2Hexagon
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

    /// <summary> Комбинация. мозаика из 6Square 4Triangle 2Hexagon </summary>
    public class SqTrHex : BaseCell {

        public SqTrHex(ShapeSqTrHex shape, Coord coord)
            : base(shape, coord,
                        (coord.y & 3) * 3 + (coord.x % 3) // 0..11
                  )
        { }

        private new ShapeSqTrHex Shape => (ShapeSqTrHex)base.Shape;

        public override IList<Coord> GetCoordsNeighbor() {
            var neighborCoord = new Coord[Shape.GetNeighborNumber(GetDirection())];

            // определяю координаты соседей
            switch (direction) {
            case 0:
                neighborCoord[ 0] = new Coord(coord.x - 1, coord.y - 1);
                neighborCoord[ 1] = new Coord(coord.x + 1, coord.y    );
                neighborCoord[ 2] = new Coord(coord.x - 2, coord.y + 1);
                neighborCoord[ 3] = new Coord(coord.x - 1, coord.y + 1);
                neighborCoord[ 4] = new Coord(coord.x    , coord.y + 1);
                neighborCoord[ 5] = new Coord(coord.x + 1, coord.y + 1);
                break;
            case 1:
                neighborCoord[ 0] = new Coord(coord.x - 2, coord.y - 1);
                neighborCoord[ 1] = new Coord(coord.x    , coord.y - 1);
                neighborCoord[ 2] = new Coord(coord.x - 1, coord.y    );
                neighborCoord[ 3] = new Coord(coord.x + 1, coord.y    );
                neighborCoord[ 4] = new Coord(coord.x - 2, coord.y + 1);
                neighborCoord[ 5] = new Coord(coord.x - 1, coord.y + 1);
                neighborCoord[ 6] = new Coord(coord.x    , coord.y + 1);
                neighborCoord[ 7] = new Coord(coord.x + 1, coord.y + 1);
                break;
            case 2:
                neighborCoord[ 0] = new Coord(coord.x - 3, coord.y - 1);
                neighborCoord[ 1] = new Coord(coord.x - 1, coord.y - 1);
                neighborCoord[ 2] = new Coord(coord.x    , coord.y - 1);
                neighborCoord[ 3] = new Coord(coord.x - 1, coord.y    );
                neighborCoord[ 4] = new Coord(coord.x - 1, coord.y + 1);
                neighborCoord[ 5] = new Coord(coord.x    , coord.y + 1);
                break;
            case 3:
                neighborCoord[ 0] = new Coord(coord.x    , coord.y - 1);
                neighborCoord[ 1] = new Coord(coord.x + 1, coord.y - 1);
                neighborCoord[ 2] = new Coord(coord.x - 2, coord.y    );
                neighborCoord[ 3] = new Coord(coord.x - 1, coord.y    );
                neighborCoord[ 4] = new Coord(coord.x + 1, coord.y    );
                neighborCoord[ 5] = new Coord(coord.x - 1, coord.y + 1);
                neighborCoord[ 6] = new Coord(coord.x    , coord.y + 1);
                neighborCoord[ 7] = new Coord(coord.x    , coord.y + 2);
                break;
            case 4:
                neighborCoord[ 0] = new Coord(coord.x - 1, coord.y - 1);
                neighborCoord[ 1] = new Coord(coord.x    , coord.y - 1);
                neighborCoord[ 2] = new Coord(coord.x + 1, coord.y - 1);
                neighborCoord[ 3] = new Coord(coord.x + 2, coord.y - 1);
                neighborCoord[ 4] = new Coord(coord.x - 1, coord.y    );
                neighborCoord[ 5] = new Coord(coord.x + 1, coord.y    );
                neighborCoord[ 6] = new Coord(coord.x + 2, coord.y    );
                neighborCoord[ 7] = new Coord(coord.x - 1, coord.y + 1);
                neighborCoord[ 8] = new Coord(coord.x    , coord.y + 1);
                neighborCoord[ 9] = new Coord(coord.x + 1, coord.y + 1);
                neighborCoord[10] = new Coord(coord.x + 2, coord.y + 1);
                neighborCoord[11] = new Coord(coord.x - 1, coord.y + 2);
                break;
            case 5:
                neighborCoord[ 0] = new Coord(coord.x - 1, coord.y - 2);
                neighborCoord[ 1] = new Coord(coord.x    , coord.y - 2);
                neighborCoord[ 2] = new Coord(coord.x - 1, coord.y - 1);
                neighborCoord[ 3] = new Coord(coord.x    , coord.y - 1);
                neighborCoord[ 4] = new Coord(coord.x + 1, coord.y - 1);
                neighborCoord[ 5] = new Coord(coord.x + 2, coord.y - 1);
                neighborCoord[ 6] = new Coord(coord.x - 1, coord.y    );
                neighborCoord[ 7] = new Coord(coord.x + 1, coord.y    );
                break;
            case 6:
                neighborCoord[ 0] = new Coord(coord.x - 2, coord.y - 1);
                neighborCoord[ 1] = new Coord(coord.x    , coord.y - 1);
                neighborCoord[ 2] = new Coord(coord.x + 1, coord.y - 1);
                neighborCoord[ 3] = new Coord(coord.x - 1, coord.y    );
                neighborCoord[ 4] = new Coord(coord.x - 1, coord.y + 1);
                neighborCoord[ 5] = new Coord(coord.x    , coord.y + 1);
                break;
            case 7:
                neighborCoord[ 0] = new Coord(coord.x    , coord.y - 1);
                neighborCoord[ 1] = new Coord(coord.x + 1, coord.y    );
                neighborCoord[ 2] = new Coord(coord.x - 2, coord.y + 1);
                neighborCoord[ 3] = new Coord(coord.x - 1, coord.y + 1);
                neighborCoord[ 4] = new Coord(coord.x    , coord.y + 1);
                neighborCoord[ 5] = new Coord(coord.x + 1, coord.y + 1);
                break;
            case 8:
                neighborCoord[ 0] = new Coord(coord.x - 1, coord.y - 1);
                neighborCoord[ 1] = new Coord(coord.x + 1, coord.y - 1);
                neighborCoord[ 2] = new Coord(coord.x - 1, coord.y    );
                neighborCoord[ 3] = new Coord(coord.x + 1, coord.y    );
                neighborCoord[ 4] = new Coord(coord.x - 2, coord.y + 1);
                neighborCoord[ 5] = new Coord(coord.x - 1, coord.y + 1);
                neighborCoord[ 6] = new Coord(coord.x    , coord.y + 1);
                neighborCoord[ 7] = new Coord(coord.x + 1, coord.y + 1);
                break;
            case 9:
                neighborCoord[ 0] = new Coord(coord.x    , coord.y - 2);
                neighborCoord[ 1] = new Coord(coord.x + 1, coord.y - 2);
                neighborCoord[ 2] = new Coord(coord.x - 1, coord.y - 1);
                neighborCoord[ 3] = new Coord(coord.x    , coord.y - 1);
                neighborCoord[ 4] = new Coord(coord.x + 1, coord.y - 1);
                neighborCoord[ 5] = new Coord(coord.x + 2, coord.y - 1);
                neighborCoord[ 6] = new Coord(coord.x - 1, coord.y    );
                neighborCoord[ 7] = new Coord(coord.x + 1, coord.y    );
                break;
            case 10:
                neighborCoord[ 0] = new Coord(coord.x    , coord.y - 1);
                neighborCoord[ 1] = new Coord(coord.x + 1, coord.y - 1);
                neighborCoord[ 2] = new Coord(coord.x - 2, coord.y    );
                neighborCoord[ 3] = new Coord(coord.x - 1, coord.y    );
                neighborCoord[ 4] = new Coord(coord.x + 1, coord.y    );
                neighborCoord[ 5] = new Coord(coord.x    , coord.y + 1);
                neighborCoord[ 6] = new Coord(coord.x + 1, coord.y + 1);
                neighborCoord[ 7] = new Coord(coord.x + 1, coord.y + 2);
                break;
            case 11:
                neighborCoord[ 0] = new Coord(coord.x - 1, coord.y - 1);
                neighborCoord[ 1] = new Coord(coord.x    , coord.y - 1);
                neighborCoord[ 2] = new Coord(coord.x + 1, coord.y - 1);
                neighborCoord[ 3] = new Coord(coord.x + 2, coord.y - 1);
                neighborCoord[ 4] = new Coord(coord.x - 1, coord.y    );
                neighborCoord[ 5] = new Coord(coord.x + 1, coord.y    );
                neighborCoord[ 6] = new Coord(coord.x + 2, coord.y    );
                neighborCoord[ 7] = new Coord(coord.x    , coord.y + 1);
                neighborCoord[ 8] = new Coord(coord.x + 1, coord.y + 1);
                neighborCoord[ 9] = new Coord(coord.x + 2, coord.y + 1);
                neighborCoord[10] = new Coord(coord.x + 3, coord.y + 1);
                neighborCoord[11] = new Coord(coord.x    , coord.y + 2);
                break;
            }

            return neighborCoord;
        }

        private PointDouble getOffset() {
            var shape = Shape;
            var a = shape.A;
            var h = shape.H;

            return new PointDouble(
                  (h * 2 + a    ) * (coord.x / 3) + a     + h,
                  (h * 2 + a * 3) * (coord.y / 4) + a * 2 + h);
        }

        protected override void CalcRegion() {
            var shape = Shape;
            var a = shape.A;
            var b = a / 2;
            var h = shape.H;

            PointDouble o = getOffset();
            switch (direction) {
            case 0:
               region.SetPoint(0, o.X - b - h    , o.Y - a - b - h);
               region.SetPoint(1, o.X - h        , o.Y - a - b    );
               region.SetPoint(2, o.X - h - a    , o.Y - a - b    );
               break;
            case 1:
               region.SetPoint(0, o.X - b        , o.Y - a - a - h);
               region.SetPoint(1, o.X            , o.Y - a - a    );
               region.SetPoint(2, o.X - h        , o.Y - a - b    );
               region.SetPoint(3, o.X - b - h    , o.Y - a - b - h);
               break;
            case 2:
               region.SetPoint(0, o.X + b        , o.Y - a - a - h);
               region.SetPoint(1, o.X            , o.Y - a - a    );
               region.SetPoint(2, o.X - b        , o.Y - a - a - h);
               break;
            case 3:
               region.SetPoint(0, o.X - h        , o.Y - a - b    );
               region.SetPoint(1, o.X - h        , o.Y - b        );
               region.SetPoint(2, o.X - a - h    , o.Y - b        );
               region.SetPoint(3, o.X - a - h    , o.Y - a - b    );
               break;
            case 4:
               region.SetPoint(0, o.X            , o.Y - a - a    );
               region.SetPoint(1, o.X + h        , o.Y - a - b    );
               region.SetPoint(2, o.X + h        , o.Y - b        );
               region.SetPoint(3, o.X            , o.Y            );
               region.SetPoint(4, o.X - h        , o.Y - b        );
               region.SetPoint(5, o.X - h        , o.Y - a - b    );
               break;
            case 5:
               region.SetPoint(0, o.X + b        , o.Y - a - a - h);
               region.SetPoint(1, o.X + b + h    , o.Y - a - b - h);
               region.SetPoint(2, o.X + h        , o.Y - a - b    );
               region.SetPoint(3, o.X            , o.Y - a - a    );
               break;
            case 6:
               region.SetPoint(0, o.X - h        , o.Y - b        );
               region.SetPoint(1, o.X - b - h    , o.Y - b + h    );
               region.SetPoint(2, o.X - a - h    , o.Y - b        );
               break;
            case 7:
               region.SetPoint(0, o.X            , o.Y            );
               region.SetPoint(1, o.X + b        , o.Y + h        );
               region.SetPoint(2, o.X - b        , o.Y + h        );
               break;
            case 8:
               region.SetPoint(0, o.X + h        , o.Y - b        );
               region.SetPoint(1, o.X + h + b    , o.Y - b + h    );
               region.SetPoint(2, o.X + b        , o.Y + h        );
               region.SetPoint(3, o.X            , o.Y            );
               break;
            case 9:
               region.SetPoint(0, o.X - h        , o.Y - b        );
               region.SetPoint(1, o.X            , o.Y            );
               region.SetPoint(2, o.X - b        , o.Y + h        );
               region.SetPoint(3, o.X - b - h    , o.Y - b + h    );
               break;
            case 10:
               region.SetPoint(0, o.X + b        , o.Y + h        );
               region.SetPoint(1, o.X + b        , o.Y + a + h    );
               region.SetPoint(2, o.X - b        , o.Y + a + h    );
               region.SetPoint(3, o.X - b        , o.Y + h        );
               break;
            case 11:
               region.SetPoint(0, o.X + b + h    , o.Y + h - b    );
               region.SetPoint(1, o.X + b + h + h, o.Y + h        );
               region.SetPoint(2, o.X + b + h + h, o.Y + a + h    );
               region.SetPoint(3, o.X + b + h    , o.Y + a + b + h);
               region.SetPoint(4, o.X + b        , o.Y + a + h    );
               region.SetPoint(5, o.X + b        , o.Y + h        );
               break;
            }
        }

        public override RectDouble GetRcInner(double borderWidth) {
            var shape = Shape;
            var a = shape.A;
            var b = a / 2;
            var h = shape.H;
            var w = borderWidth / 2.0;
            var sq = shape.GetSq(borderWidth);
            var sq2 = sq / 2;

            var o = getOffset();

            var center = new PointDouble(); // координата центра вписанного в фигуру квадрата
            switch (direction) {
            case  0: center.X = o.X -  b-h;    center.Y = o.Y - a-b-w-sq2;   break;
            case  1: center.X = o.X - (b+h)/2; center.Y = o.Y - a-b-(b+h)/2; break;
            case  2: center.X = o.X;           center.Y = o.Y - a-a-h+w+sq2; break;
            case  3: center.X = o.X -  b-h;    center.Y = o.Y - a;           break;
            case  4: center.X = o.X;           center.Y = o.Y - a;           break;
            case  5: center.X = o.X + (b+h)/2; center.Y = o.Y - a-b-(b+h)/2; break;
            case  6: center.X = o.X -  b-h;    center.Y = o.Y - b+w+sq2;     break;
            case  7: center.X = o.X;           center.Y = o.Y + h-w-sq2;     break;
            case  8: center.X = o.X + (b+h)/2; center.Y = o.Y - b+(b+h)/2;   break;
            case  9: center.X = o.X - (b+h)/2; center.Y = o.Y - b+(b+h)/2;   break;
            case 10: center.X = o.X;           center.Y = o.Y + b+h;         break;
            case 11: center.X = o.X +  b+h;    center.Y = o.Y + b+h;         break;
            }

            return new RectDouble(
                center.X - sq2,
                center.Y - sq2,
                sq, sq);
        }

        public override int GetShiftPointBorderIndex() {
            switch (direction) {
            case 2: case  6: return 1;
            case 4: case 11: return 3;
            }
            return 2;
        }

    }

}
