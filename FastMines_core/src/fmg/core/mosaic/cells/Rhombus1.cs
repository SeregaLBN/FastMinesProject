////////////////////////////////////////////////////////////////////////////////
//                               FastMines project
// file name: "Rhombus1.cs"
//
// Реализация класса Rhombus1 - 3 ромба, составляющие равносторонний шестиугольник
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
using Fmg.Core.Mosaic.Shape;

namespace Fmg.Core.Mosaic.Cells {

    /// <summary> Rhombus1 - 3 ромба, составляющие равносторонний шестиугольник </summary>
    public class Rhombus1 : BaseCell {

        public Rhombus1(ShapeRhombus1 shape, Coord coord)
            : base(shape, coord,
                        (coord.y & 1) * 3 + (coord.x % 3) // 0..5
                  )
        { }

        private new ShapeRhombus1 Shape => (ShapeRhombus1)base.Shape;

        public override IList<Coord> GetCoordsNeighbor() {
           var neighborCoord = new Coord[Shape.GetNeighborNumber(GetDirection())];

           // определяю координаты соседей
            switch (direction) {
            case 0:
               neighborCoord[ 0] = new Coord(coord.x + 1, coord.y - 2);
               neighborCoord[ 1] = new Coord(coord.x + 2, coord.y - 2);
               neighborCoord[ 2] = new Coord(coord.x - 2, coord.y - 1);
               neighborCoord[ 3] = new Coord(coord.x - 1, coord.y - 1);
               neighborCoord[ 4] = new Coord(coord.x    , coord.y - 1);
               neighborCoord[ 5] = new Coord(coord.x + 1, coord.y - 1);
               neighborCoord[ 6] = new Coord(coord.x - 1, coord.y    );
               neighborCoord[ 7] = new Coord(coord.x + 1, coord.y    );
               neighborCoord[ 8] = new Coord(coord.x - 1, coord.y + 1);
               neighborCoord[ 9] = new Coord(coord.x    , coord.y + 1);
               break;
            case 1:
               neighborCoord[ 0] = new Coord(coord.x    , coord.y - 2);
               neighborCoord[ 1] = new Coord(coord.x + 1, coord.y - 2);
               neighborCoord[ 2] = new Coord(coord.x - 1, coord.y - 1);
               neighborCoord[ 3] = new Coord(coord.x    , coord.y - 1);
               neighborCoord[ 4] = new Coord(coord.x - 1, coord.y    );
               neighborCoord[ 5] = new Coord(coord.x + 1, coord.y    );
               neighborCoord[ 6] = new Coord(coord.x - 1, coord.y + 1);
               neighborCoord[ 7] = new Coord(coord.x    , coord.y + 1);
               neighborCoord[ 8] = new Coord(coord.x - 1, coord.y + 2);
               neighborCoord[ 9] = new Coord(coord.x    , coord.y + 2);
               break;
            case 2:
               neighborCoord[ 0] = new Coord(coord.x - 1, coord.y - 1);
               neighborCoord[ 1] = new Coord(coord.x    , coord.y - 1);
               neighborCoord[ 2] = new Coord(coord.x - 1, coord.y    );
               neighborCoord[ 3] = new Coord(coord.x + 1, coord.y    );
               neighborCoord[ 4] = new Coord(coord.x - 2, coord.y + 1);
               neighborCoord[ 5] = new Coord(coord.x - 1, coord.y + 1);
               neighborCoord[ 6] = new Coord(coord.x    , coord.y + 1);
               neighborCoord[ 7] = new Coord(coord.x + 1, coord.y + 1);
               neighborCoord[ 8] = new Coord(coord.x - 2, coord.y + 2);
               neighborCoord[ 9] = new Coord(coord.x - 1, coord.y + 2);
               break;
            case 3:
               neighborCoord[ 0] = new Coord(coord.x - 2, coord.y - 2);
               neighborCoord[ 1] = new Coord(coord.x - 1, coord.y - 2);
               neighborCoord[ 2] = new Coord(coord.x - 1, coord.y - 1);
               neighborCoord[ 3] = new Coord(coord.x    , coord.y - 1);
               neighborCoord[ 4] = new Coord(coord.x + 1, coord.y - 1);
               neighborCoord[ 5] = new Coord(coord.x + 2, coord.y - 1);
               neighborCoord[ 6] = new Coord(coord.x - 1, coord.y    );
               neighborCoord[ 7] = new Coord(coord.x + 1, coord.y    );
               neighborCoord[ 8] = new Coord(coord.x    , coord.y + 1);
               neighborCoord[ 9] = new Coord(coord.x + 1, coord.y + 1);
               break;
            case 4:
               neighborCoord[ 0] = new Coord(coord.x    , coord.y - 1);
               neighborCoord[ 1] = new Coord(coord.x + 1, coord.y - 1);
               neighborCoord[ 2] = new Coord(coord.x - 1, coord.y    );
               neighborCoord[ 3] = new Coord(coord.x + 1, coord.y    );
               neighborCoord[ 4] = new Coord(coord.x - 1, coord.y + 1);
               neighborCoord[ 5] = new Coord(coord.x    , coord.y + 1);
               neighborCoord[ 6] = new Coord(coord.x + 1, coord.y + 1);
               neighborCoord[ 7] = new Coord(coord.x + 2, coord.y + 1);
               neighborCoord[ 8] = new Coord(coord.x + 1, coord.y + 2);
               neighborCoord[ 9] = new Coord(coord.x + 2, coord.y + 2);
               break;
            case 5:
               neighborCoord[ 0] = new Coord(coord.x - 1, coord.y - 2);
               neighborCoord[ 1] = new Coord(coord.x    , coord.y - 2);
               neighborCoord[ 2] = new Coord(coord.x    , coord.y - 1);
               neighborCoord[ 3] = new Coord(coord.x + 1, coord.y - 1);
               neighborCoord[ 4] = new Coord(coord.x - 1, coord.y    );
               neighborCoord[ 5] = new Coord(coord.x + 1, coord.y    );
               neighborCoord[ 6] = new Coord(coord.x    , coord.y + 1);
               neighborCoord[ 7] = new Coord(coord.x + 1, coord.y + 1);
               neighborCoord[ 8] = new Coord(coord.x    , coord.y + 2);
               neighborCoord[ 9] = new Coord(coord.x + 1, coord.y + 2);
               break;
            }

           return neighborCoord;
        }

        protected override void CalcRegion() {
            var shape = Shape;
            var a = shape.A;
            var c = shape.C;
            var h = shape.H;
            var r = shape.R;

            // определение координат точек фигуры
            var oX = a * (coord.x / 3 * 3 + 1) + c; // offset X
            var oY = h * (coord.y / 2) + h; // offset Y

            switch (direction) {
            case 0:
               region.SetPoint(0, oX        , oY - h);
               region.SetPoint(1, oX - c    , oY - r);
               region.SetPoint(2, oX - a - c, oY - r);
               region.SetPoint(3, oX - a    , oY - h);
               break;
            case 1:
               region.SetPoint(0, oX        , oY - h);
               region.SetPoint(1, oX + c    , oY - r);
               region.SetPoint(2, oX        , oY    );
               region.SetPoint(3, oX - c    , oY - r);
               break;
            case 2:
               region.SetPoint(0, oX + a + c, oY - r);
               region.SetPoint(1, oX + a    , oY    );
               region.SetPoint(2, oX        , oY    );
               region.SetPoint(3, oX + c    , oY - r);
               break;
            case 3:
               region.SetPoint(0, oX - c    , oY - r);
               region.SetPoint(1, oX        , oY    );
               region.SetPoint(2, oX - a    , oY    );
               region.SetPoint(3, oX - a - c, oY - r);
               break;
            case 4:
               region.SetPoint(0, oX + a    , oY    );
               region.SetPoint(1, oX + a + c, oY + r);
               region.SetPoint(2, oX + c    , oY + r);
               region.SetPoint(3, oX        , oY    );
               break;
            case 5:
               region.SetPoint(0, oX + a + c, oY - r);
               region.SetPoint(1, oX + a + a, oY    );
               region.SetPoint(2, oX + a + c, oY + r);
               region.SetPoint(3, oX + a    , oY    );
               break;
            }
        }

        public override RectDouble GetRcInner(double borderWidth) {
            var shape = Shape;
            var a = shape.A;
            var c = shape.C;
            var h = shape.H;
            var r = shape.R;
          //var w = borderWidth / 2.0;
            var sq  = shape.GetSq(borderWidth);
            var sq2 = sq / 2;

            var oX = a * (coord.x / 3 * 3 + 1) + c; // offset X
            var oY = h * (coord.y / 2) + h; // offset Y

            var center = new PointDouble(); // координата центра квадрата
            switch (direction) {
            case 0: center.X = oX - c * 1.5; center.Y = oY - r * 1.5; break;
            case 1: center.X = oX;           center.Y = oY - r;       break;
            case 2: center.X = oX + c * 1.5; center.Y = oY - r * 0.5; break;
            case 3: center.X = oX - c * 1.5; center.Y = oY - r * 0.5; break;
            case 4: center.X = oX + c * 1.5; center.Y = oY + r * 0.5; break;
            case 5: center.X = oX + a + c;   center.Y = oY;           break;
            }

            return new RectDouble(
               center.X - sq2,
               center.Y - sq2,
               sq, sq);
        }

        public override int GetShiftPointBorderIndex() { return 2; }

        public override Color GetCellFillColor(int fillMode, Color defaultColor, Func<int, Color> repositoryColor) {
            if (fillMode == Shape.GetMaxCellFillModeValue()) {
                var c = GetCoord();
                switch ((c.y % 4) * 3 + (c.x % 3)) { // почти как вычисление direction...
                                                     // подсвечиваю 4 группы, составляющие каждая шестигранник из 3х ромбов
                case 0: case  1: case  3: return repositoryColor(0);
                case 2: case  4: case  5: return repositoryColor(1);
                case 6: case  7: case  9: return repositoryColor(2);
                case 8: case 10: case 11: return repositoryColor(3);
                }
            }
            return base.GetCellFillColor(fillMode, defaultColor, repositoryColor);
        }

    }

}
