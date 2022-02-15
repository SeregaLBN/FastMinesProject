////////////////////////////////////////////////////////////////////////////////
//                               FastMines project
// file name: "TrSq1.cs"
//
// Реализация класса TrSq1 - мозаика из 4х треугольников и 2х квадратов
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
using Fmg.Core.Mosaic.Shape;

namespace Fmg.Core.Mosaic.Cells {

    /// <summary> Комбинация. Мозаика из 4х треугольников и 2х квадратов </summary>
    public class TrSq1 : BaseCell {

        public TrSq1(ShapeTrSq1 shape, Coord coord)
            : base(shape, coord,
                        (coord.y & 1) * 3 + (coord.x % 3) // 0..5
                  )
        { }

         private new ShapeTrSq1 Shape => (ShapeTrSq1)base.Shape;

         public override IList<Coord> GetCoordsNeighbor() {
            var neighborCoord = new Coord[Shape.GetNeighborNumber(GetDirection())];

            // определяю координаты соседей
            switch (direction) {
            case 0:
                neighborCoord[ 0] = new Coord(coord.x - 2, coord.y - 1);
                neighborCoord[ 1] = new Coord(coord.x - 1, coord.y - 1);
                neighborCoord[ 2] = new Coord(coord.x    , coord.y - 1);
                neighborCoord[ 3] = new Coord(coord.x + 1, coord.y - 1);
                neighborCoord[ 4] = new Coord(coord.x - 2, coord.y    );
                neighborCoord[ 5] = new Coord(coord.x - 1, coord.y    );
                neighborCoord[ 6] = new Coord(coord.x + 1, coord.y    );
                neighborCoord[ 7] = new Coord(coord.x + 2, coord.y    );
                neighborCoord[ 8] = new Coord(coord.x - 2, coord.y + 1);
                neighborCoord[ 9] = new Coord(coord.x - 1, coord.y + 1);
                neighborCoord[10] = new Coord(coord.x    , coord.y + 1);
                neighborCoord[11] = new Coord(coord.x + 1, coord.y + 1);
                break;
            case 1:
                neighborCoord[ 0] = new Coord(coord.x - 2, coord.y - 1);
                neighborCoord[ 1] = new Coord(coord.x - 1, coord.y - 1);
                neighborCoord[ 2] = new Coord(coord.x    , coord.y - 1);
                neighborCoord[ 3] = new Coord(coord.x + 1, coord.y - 1);
                neighborCoord[ 4] = new Coord(coord.x - 1, coord.y    );
                neighborCoord[ 5] = new Coord(coord.x + 1, coord.y    );
                neighborCoord[ 6] = new Coord(coord.x + 2, coord.y    );
                neighborCoord[ 7] = new Coord(coord.x - 1, coord.y + 1);
                neighborCoord[ 8] = new Coord(coord.x    , coord.y + 1);
                break;
            case 2:
                neighborCoord[ 0] = new Coord(coord.x - 1, coord.y - 1);
                neighborCoord[ 1] = new Coord(coord.x    , coord.y - 1);
                neighborCoord[ 2] = new Coord(coord.x - 2, coord.y    );
                neighborCoord[ 3] = new Coord(coord.x - 1, coord.y    );
                neighborCoord[ 4] = new Coord(coord.x + 1, coord.y    );
                neighborCoord[ 5] = new Coord(coord.x - 2, coord.y + 1);
                neighborCoord[ 6] = new Coord(coord.x - 1, coord.y + 1);
                neighborCoord[ 7] = new Coord(coord.x    , coord.y + 1);
                neighborCoord[ 8] = new Coord(coord.x + 1, coord.y + 1);
                break;
            case 3:
                neighborCoord[ 0] = new Coord(coord.x - 1, coord.y - 1);
                neighborCoord[ 1] = new Coord(coord.x    , coord.y - 1);
                neighborCoord[ 2] = new Coord(coord.x + 1, coord.y - 1);
                neighborCoord[ 3] = new Coord(coord.x + 2, coord.y - 1);
                neighborCoord[ 4] = new Coord(coord.x - 2, coord.y    );
                neighborCoord[ 5] = new Coord(coord.x - 1, coord.y    );
                neighborCoord[ 6] = new Coord(coord.x + 1, coord.y    );
                neighborCoord[ 7] = new Coord(coord.x    , coord.y + 1);
                neighborCoord[ 8] = new Coord(coord.x + 1, coord.y + 1);
                break;
            case 4:
                neighborCoord[ 0] = new Coord(coord.x - 1, coord.y - 1);
                neighborCoord[ 1] = new Coord(coord.x    , coord.y - 1);
                neighborCoord[ 2] = new Coord(coord.x + 1, coord.y - 1);
                neighborCoord[ 3] = new Coord(coord.x + 2, coord.y - 1);
                neighborCoord[ 4] = new Coord(coord.x - 2, coord.y    );
                neighborCoord[ 5] = new Coord(coord.x - 1, coord.y    );
                neighborCoord[ 6] = new Coord(coord.x + 1, coord.y    );
                neighborCoord[ 7] = new Coord(coord.x + 2, coord.y    );
                neighborCoord[ 8] = new Coord(coord.x - 1, coord.y + 1);
                neighborCoord[ 9] = new Coord(coord.x    , coord.y + 1);
                neighborCoord[10] = new Coord(coord.x + 1, coord.y + 1);
                neighborCoord[11] = new Coord(coord.x + 2, coord.y + 1);
                break;
            case 5:
                neighborCoord[ 0] = new Coord(coord.x    , coord.y - 1);
                neighborCoord[ 1] = new Coord(coord.x + 1, coord.y - 1);
                neighborCoord[ 2] = new Coord(coord.x - 1, coord.y    );
                neighborCoord[ 3] = new Coord(coord.x + 1, coord.y    );
                neighborCoord[ 4] = new Coord(coord.x + 2, coord.y    );
                neighborCoord[ 5] = new Coord(coord.x - 1, coord.y + 1);
                neighborCoord[ 6] = new Coord(coord.x    , coord.y + 1);
                neighborCoord[ 7] = new Coord(coord.x + 1, coord.y + 1);
                neighborCoord[ 8] = new Coord(coord.x + 2, coord.y + 1);
                break;
            }

            return neighborCoord;
        }

        protected override void CalcRegion() {
            var shape = Shape;
            var b = shape.B;
            var k = shape.K;
            var n = shape.N;
            var m = shape.M;

            var oX = b + n * (coord.x / 3 * 2); // offset X
            var oY = n + n * 2 * (coord.y / 2); // offset Y

            switch (direction) {
            case 0:
                region.SetPoint(0, oX - m  , oY - n);
                region.SetPoint(1, oX      , oY    );
                region.SetPoint(2, oX - n  , oY + m);
                region.SetPoint(3, oX - b  , oY - k);
                break;
            case 1:
                region.SetPoint(1, oX      , oY    );
                region.SetPoint(2, oX - m  , oY - n);
                region.SetPoint(0, oX + k  , oY - k);
                break;
            case 2:
                region.SetPoint(0, oX + k  , oY - k);
                region.SetPoint(1, oX + n  , oY + m);
                region.SetPoint(2, oX      , oY    );
                break;
            case 3:
                region.SetPoint(1, oX - m  , oY + n);
                region.SetPoint(2, oX - n  , oY + m);
                region.SetPoint(0, oX      , oY    );
                break;
            case 4:
                region.SetPoint(0, oX + n  , oY + m);
                region.SetPoint(3, oX      , oY    );
                region.SetPoint(2, oX - m  , oY + n);
                region.SetPoint(1, oX + k  , oY + b);
                break;
            case 5:
                region.SetPoint(0, oX + n  , oY + m);
                region.SetPoint(1, oX + n+k, oY + n);
                region.SetPoint(2, oX + k  , oY + b);
                break;
            }
        }

        public override RectDouble GetRcInner(double borderWidth) {
            var shape = Shape;
            var b = shape.B;
            var k = shape.K;
            var n = shape.N;
            var m = shape.M;
            var w = borderWidth/2.0;
            var sq = shape.GetSq(borderWidth);
            var sq2 = sq/2;

            var oX = b + n * (coord.x / 3 * 2); // offset X
            var oY = n + n * 2 * (coord.y / 2); // offset Y


            var ksw1 = k/2-sq2-w/BaseShape.SQRT2;
            var ksw2 = k/2+sq2+w/BaseShape.SQRT2;
            var center = new PointDouble(); // координата вписанного в фигуру квадрата (не совпадает с центром фигуры)
            switch (direction) {
            case 0:  center.X = oX - b/2;    center.Y = oY - k/2;    break;
            case 1:  center.X = oX + ksw1;   center.Y = oY - ksw2;   break;
            case 2:  center.X = oX + ksw2;   center.Y = oY - ksw1;   break;
            case 3:  center.X = oX + ksw2-n; center.Y = oY - ksw2+n; break;
            case 4:  center.X = oX + k/2;    center.Y = oY + b/2;    break;
            case 5:  center.X = oX + ksw1+n; center.Y = oY + ksw2+m; break;
            }

            return new RectDouble(
                center.X - sq2,
                center.Y - sq2,
                sq, sq);
        }

        public override int GetShiftPointBorderIndex() {
            switch (direction) {
            case 1: case 3: return 1;
            }
            return 2;
        }

    }

}
