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
using System.Collections.Generic;
using Fmg.Common.Geom;
using Fmg.Core.Mosaic.Shape;

namespace Fmg.Core.Mosaic.Cells {

    /// <summary> Trapezoid3 - 8 трапеций, складывающихся в шестигранник </summary>
    public class Trapezoid3 : BaseCell {

        public Trapezoid3(ShapeTrapezoid3 shape, Coord coord)
            : base(shape, coord,
                       ((coord.y & 3) << 2) + (coord.x & 3) // 0..15
                  )
        { }

        private new ShapeTrapezoid3 Shape => (ShapeTrapezoid3)base.Shape;

        public override IList<Coord> GetCoordsNeighbor() {
            var neighborCoord = new Coord[Shape.GetNeighborNumber(GetDirection())];

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
            var shape = Shape;
            var a = shape.A;
            var b = shape.B;
            var c = shape.C;
            var R = shape.ROut;
            var r = shape.RIn;

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
            var shape = Shape;
            var a = shape.A;
            var b = shape.B;
            var c = shape.C;
            var R = shape.ROut;
            var r = shape.RIn;
            //var w = borderWidth / 2.0;
            var sq  = shape.GetSq(borderWidth);
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
