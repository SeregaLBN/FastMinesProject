////////////////////////////////////////////////////////////////////////////////
//                               FastMines project
// file name: "Hexagon1.cs"
//
// Реализация класса Hexagon1 - правильный 6-ти угольник (сота)
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

    /// <summary> Шестиугольник </summary>
    public class Hexagon1 : BaseCell {

        public Hexagon1(ShapeHexagon1 shape, Coord coord)
            : base(shape, coord,
                         coord.y & 1 // 0..1
                  )
        { }

        private new ShapeHexagon1 Shape => (ShapeHexagon1)base.Shape;

        public override IList<Coord> GetCoordsNeighbor() {
            var neighborCoord = new Coord[Shape.GetNeighborNumber(GetDirection())];

            // определяю координаты соседей
            neighborCoord[0] = new Coord(coord.x - (direction ^ 1), coord.y - 1);
            neighborCoord[1] = new Coord(coord.x +  direction     , coord.y - 1);
            neighborCoord[2] = new Coord(coord.x - 1              , coord.y);
            neighborCoord[3] = new Coord(coord.x + 1              , coord.y);
            neighborCoord[4] = new Coord(coord.x - (direction ^ 1), coord.y + 1);
            neighborCoord[5] = new Coord(coord.x +  direction     , coord.y + 1);

            return neighborCoord;
        }

        protected override void CalcRegion() {
            var shape = Shape;
            var a = shape.A;
            var b = shape.B;

            var oX = (coord.x + 1) * b;                 // offset X
            var oY = (coord.y + (direction ^ 1)) * a * 1.5; // offset Y

            switch (direction) {
            case 0:
                region.SetPoint(0, oX        , oY - a);
                region.SetPoint(1, oX        , oY);
                region.SetPoint(2, oX - b / 2, oY + a / 2);
                region.SetPoint(3, oX - b    , oY);
                region.SetPoint(4, oX - b    , oY - a);
                region.SetPoint(5, oX - b / 2, oY - a * 1.5);
                break;
            case 1:
                region.SetPoint(0, oX + b / 2, oY + a / 2);
                region.SetPoint(1, oX + b / 2, oY + a * 1.5);
                region.SetPoint(2, oX        , oY + a * 2);
                region.SetPoint(3, oX - b / 2, oY + a * 1.5);
                region.SetPoint(4, oX - b / 2, oY + a / 2);
                region.SetPoint(5, oX        , oY);
                break;
            }
        }

        public override RectDouble GetRcInner(double borderWidth) {
            var shape = Shape;
            var a = shape.A;
            var b = shape.B;
            var sq = shape.GetSq(borderWidth);

            var oX = (coord.x + 1) * b;               // offset X
            var oY = (coord.y + 1 - direction) * a * 1.5; // offset Y

            var center = new PointDouble(); // координата вписанного в фигуру квадрата (не совпадает с центром фигуры)
            switch (direction) {
            case 0: center.X = oX - b / 2; center.Y = oY - a / 2; break;
            case 1: center.X = oX; center.Y = oY + a; break;
            }

            return new RectDouble(
               center.X - sq / 2,
               center.Y - sq / 2,
               sq, sq);
        }

        public override int GetShiftPointBorderIndex() { return 3; }

    }

}
