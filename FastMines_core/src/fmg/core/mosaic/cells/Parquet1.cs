////////////////////////////////////////////////////////////////////////////////
//                               FastMines project
// file name: "Parquet1.cs"
//
// Реализация класса Parquet1 - паркет в елку (herring-bone parquet)
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

    /// <summary> Паркет в елку </summary>
    public class Parquet1 : BaseCell {

        public Parquet1(ShapeParquet1 shape, Coord coord)
            : base(shape, coord,
                         coord.x & 1 // 0..1
                  )
        { }

        private new ShapeParquet1 Shape => (ShapeParquet1)base.Shape;

        public override IList<Coord> GetCoordsNeighbor() {
            var neighborCoord = new Coord[Shape.GetNeighborNumber(GetDirection())];

            // определяю координаты соседей
            bool bdir = (direction != 0);
            neighborCoord[0] = new Coord(bdir ? coord.x     : coord.x - 1,        coord.y - 1              );
            neighborCoord[1] = new Coord(bdir ? coord.x - 1 : coord.x    , bdir ? coord.y     : coord.y - 1);
            neighborCoord[2] = new Coord(       coord.x + 1              , bdir ? coord.y     : coord.y - 1);
            neighborCoord[3] = new Coord(       coord.x - 1              , bdir ? coord.y + 1 : coord.y    );
            neighborCoord[4] = new Coord(bdir ? coord.x     : coord.x + 1, bdir ? coord.y + 1 : coord.y    );
            neighborCoord[5] = new Coord(bdir ? coord.x + 1 : coord.x    ,        coord.y + 1              );

            return neighborCoord;
        }

        protected override void CalcRegion() {
            var shape = Shape;
            var a = shape.A;

            switch (direction) {
            case 0:
                region.SetPoint(0, a * (2 + 2 * coord.x), a * (0 + 2 * coord.y));
                region.SetPoint(1, a * (3 + 2 * coord.x), a * (1 + 2 * coord.y));
                region.SetPoint(2, a * (1 + 2 * coord.x), a * (3 + 2 * coord.y));
                region.SetPoint(3, a * (0 + 2 * coord.x), a * (2 + 2 * coord.y));
                break;
            case 1:
                region.SetPoint(0, a * (1 + 2 * coord.x), a * (1 + 2 * coord.y));
                region.SetPoint(1, a * (3 + 2 * coord.x), a * (3 + 2 * coord.y));
                region.SetPoint(2, a * (2 + 2 * coord.x), a * (4 + 2 * coord.y));
                region.SetPoint(3, a * (0 + 2 * coord.x), a * (2 + 2 * coord.y));
                break;
            }
        }

        public override RectDouble GetRcInner(double borderWidth) {
            var shape = Shape;
            var sq = shape.GetSq(borderWidth);
            var w = borderWidth / 2.0;
            var bdir = (direction != 0);

            return new RectDouble(
               (bdir ? region.GetPoint(0).X : region.GetPoint(2).X) + w / BaseShape.SQRT2,
               (bdir ? region.GetPoint(3).Y : region.GetPoint(1).Y) + w / BaseShape.SQRT2,
               sq, sq);
        }

        public override int GetShiftPointBorderIndex() { return 2; }

    }

}
