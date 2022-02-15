////////////////////////////////////////////////////////////////////////////////
//                               FastMines project
// file name: "Triangle4.cs"
//
// Реализация класса Triangle4 - треугольник 30°-30°-120°
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
using ComplexityMode = Fmg.Core.Mosaic.Shape.ShapeTriangle4.ComplexityMode;

namespace Fmg.Core.Mosaic.Cells {

    /// <summary> Треугольник. Вариант 4 - треугольник 30°-30°-120° </summary>
    public class Triangle4 : BaseCell {

        public Triangle4(ShapeTriangle4 shape, Coord coord)
            : base(shape, coord,
                        (coord.y & 3) * 3 + (coord.x % 3) // 0..11
                  )
        { }

        private new ShapeTriangle4 Shape => (ShapeTriangle4)base.Shape;

        public override IList<Coord> GetCoordsNeighbor() {
            var neighborCoord = new Coord[Shape.GetNeighborNumber(GetDirection())];

            // определяю координаты соседей
            switch (ShapeTriangle4.Mode) {
            case ComplexityMode.eUnrealMode:
                #region
                switch (direction) {
                case 0:
                    neighborCoord[ 0] = new Coord(coord.x-1, coord.y-2);
                    neighborCoord[ 1] = new Coord(coord.x+1, coord.y-2);
                    neighborCoord[ 2] = new Coord(coord.x-1, coord.y-1);
                    neighborCoord[ 3] = new Coord(coord.x  , coord.y-1);
                    neighborCoord[ 4] = new Coord(coord.x+1, coord.y-1);
                    neighborCoord[ 5] = new Coord(coord.x+2, coord.y-1);
                    neighborCoord[ 6] = new Coord(coord.x-2, coord.y  );
                    neighborCoord[ 7] = new Coord(coord.x-1, coord.y  );
                    neighborCoord[ 8] = new Coord(coord.x+1, coord.y  );
                    neighborCoord[ 9] = new Coord(coord.x+2, coord.y  );
                    neighborCoord[10] = new Coord(coord.x-3, coord.y+1);
                    neighborCoord[11] = new Coord(coord.x-2, coord.y+1);
                    neighborCoord[12] = new Coord(coord.x-1, coord.y+1);
                    neighborCoord[13] = new Coord(coord.x  , coord.y+1);
                    neighborCoord[14] = new Coord(coord.x+1, coord.y+1);
                    neighborCoord[15] = new Coord(coord.x-3, coord.y+2);
                    neighborCoord[16] = new Coord(coord.x-2, coord.y+2);
                    neighborCoord[17] = new Coord(coord.x-1, coord.y+2);
                    neighborCoord[18] = new Coord(coord.x  , coord.y+2);
                    neighborCoord[19] = new Coord(coord.x-2, coord.y+3);
                    neighborCoord[20] = new Coord(coord.x  , coord.y+3);
                    break;
                case 1:
                    neighborCoord[ 0] = new Coord(coord.x-2, coord.y-2);
                    neighborCoord[ 1] = new Coord(coord.x  , coord.y-2);
                    neighborCoord[ 2] = new Coord(coord.x-2, coord.y-1);
                    neighborCoord[ 3] = new Coord(coord.x-1, coord.y-1);
                    neighborCoord[ 4] = new Coord(coord.x  , coord.y-1);
                    neighborCoord[ 5] = new Coord(coord.x+1, coord.y-1);
                    neighborCoord[ 6] = new Coord(coord.x-2, coord.y  );
                    neighborCoord[ 7] = new Coord(coord.x-1, coord.y  );
                    neighborCoord[ 8] = new Coord(coord.x+1, coord.y  );
                    neighborCoord[ 9] = new Coord(coord.x+2, coord.y  );
                    neighborCoord[10] = new Coord(coord.x-2, coord.y+1);
                    neighborCoord[11] = new Coord(coord.x-1, coord.y+1);
                    neighborCoord[12] = new Coord(coord.x  , coord.y+1);
                    neighborCoord[13] = new Coord(coord.x+1, coord.y+1);
                    neighborCoord[14] = new Coord(coord.x+2, coord.y+1);
                    neighborCoord[15] = new Coord(coord.x-1, coord.y+2);
                    neighborCoord[16] = new Coord(coord.x  , coord.y+2);
                    neighborCoord[17] = new Coord(coord.x+1, coord.y+2);
                    neighborCoord[18] = new Coord(coord.x+2, coord.y+2);
                    neighborCoord[19] = new Coord(coord.x  , coord.y+3);
                    neighborCoord[20] = new Coord(coord.x+2, coord.y+3);
                    break;
                case 2:
                    neighborCoord[ 0] = new Coord(coord.x-3, coord.y-2);
                    neighborCoord[ 1] = new Coord(coord.x-1, coord.y-2);
                    neighborCoord[ 2] = new Coord(coord.x  , coord.y-2);
                    neighborCoord[ 3] = new Coord(coord.x+2, coord.y-2);
                    neighborCoord[ 4] = new Coord(coord.x-3, coord.y-1);
                    neighborCoord[ 5] = new Coord(coord.x-2, coord.y-1);
                    neighborCoord[ 6] = new Coord(coord.x-1, coord.y-1);
                    neighborCoord[ 7] = new Coord(coord.x  , coord.y-1);
                    neighborCoord[ 8] = new Coord(coord.x+1, coord.y-1);
                    neighborCoord[ 9] = new Coord(coord.x+2, coord.y-1);
                    neighborCoord[10] = new Coord(coord.x+3, coord.y-1);
                    neighborCoord[11] = new Coord(coord.x-3, coord.y  );
                    neighborCoord[12] = new Coord(coord.x-2, coord.y  );
                    neighborCoord[13] = new Coord(coord.x-1, coord.y  );
                    neighborCoord[14] = new Coord(coord.x+1, coord.y  );
                    neighborCoord[15] = new Coord(coord.x+2, coord.y  );
                    neighborCoord[16] = new Coord(coord.x+3, coord.y  );
                    neighborCoord[17] = new Coord(coord.x-3, coord.y+1);
                    neighborCoord[18] = new Coord(coord.x-1, coord.y+1);
                    neighborCoord[19] = new Coord(coord.x  , coord.y+1);
                    neighborCoord[20] = new Coord(coord.x+2, coord.y+1);
                    break;
                case 3:
                    neighborCoord[ 0] = new Coord(coord.x-2, coord.y-1);
                    neighborCoord[ 1] = new Coord(coord.x  , coord.y-1);
                    neighborCoord[ 2] = new Coord(coord.x+1, coord.y-1);
                    neighborCoord[ 3] = new Coord(coord.x+3, coord.y-1);
                    neighborCoord[ 4] = new Coord(coord.x-3, coord.y  );
                    neighborCoord[ 5] = new Coord(coord.x-2, coord.y  );
                    neighborCoord[ 6] = new Coord(coord.x-1, coord.y  );
                    neighborCoord[ 7] = new Coord(coord.x+1, coord.y  );
                    neighborCoord[ 8] = new Coord(coord.x+2, coord.y  );
                    neighborCoord[ 9] = new Coord(coord.x+3, coord.y  );
                    neighborCoord[10] = new Coord(coord.x-3, coord.y+1);
                    neighborCoord[11] = new Coord(coord.x-2, coord.y+1);
                    neighborCoord[12] = new Coord(coord.x-1, coord.y+1);
                    neighborCoord[13] = new Coord(coord.x  , coord.y+1);
                    neighborCoord[14] = new Coord(coord.x+1, coord.y+1);
                    neighborCoord[15] = new Coord(coord.x+2, coord.y+1);
                    neighborCoord[16] = new Coord(coord.x+3, coord.y+1);
                    neighborCoord[17] = new Coord(coord.x-2, coord.y+2);
                    neighborCoord[18] = new Coord(coord.x  , coord.y+2);
                    neighborCoord[19] = new Coord(coord.x+1, coord.y+2);
                    neighborCoord[20] = new Coord(coord.x+3, coord.y+2);
                    break;
               case 4:
                    neighborCoord[ 0] = new Coord(coord.x-2, coord.y-3);
                    neighborCoord[ 1] = new Coord(coord.x  , coord.y-3);
                    neighborCoord[ 2] = new Coord(coord.x-2, coord.y-2);
                    neighborCoord[ 3] = new Coord(coord.x-1, coord.y-2);
                    neighborCoord[ 4] = new Coord(coord.x  , coord.y-2);
                    neighborCoord[ 5] = new Coord(coord.x+1, coord.y-2);
                    neighborCoord[ 6] = new Coord(coord.x-2, coord.y-1);
                    neighborCoord[ 7] = new Coord(coord.x-1, coord.y-1);
                    neighborCoord[ 8] = new Coord(coord.x  , coord.y-1);
                    neighborCoord[ 9] = new Coord(coord.x+1, coord.y-1);
                    neighborCoord[10] = new Coord(coord.x+2, coord.y-1);
                    neighborCoord[11] = new Coord(coord.x-2, coord.y  );
                    neighborCoord[12] = new Coord(coord.x-1, coord.y  );
                    neighborCoord[13] = new Coord(coord.x+1, coord.y  );
                    neighborCoord[14] = new Coord(coord.x+2, coord.y  );
                    neighborCoord[15] = new Coord(coord.x-1, coord.y+1);
                    neighborCoord[16] = new Coord(coord.x  , coord.y+1);
                    neighborCoord[17] = new Coord(coord.x+1, coord.y+1);
                    neighborCoord[18] = new Coord(coord.x+2, coord.y+1);
                    neighborCoord[19] = new Coord(coord.x  , coord.y+2);
                    neighborCoord[20] = new Coord(coord.x+2, coord.y+2);
                    break;
                case 5:
                    neighborCoord[ 0] = new Coord(coord.x  , coord.y-3);
                    neighborCoord[ 1] = new Coord(coord.x+2, coord.y-3);
                    neighborCoord[ 2] = new Coord(coord.x  , coord.y-2);
                    neighborCoord[ 3] = new Coord(coord.x+1, coord.y-2);
                    neighborCoord[ 4] = new Coord(coord.x+2, coord.y-2);
                    neighborCoord[ 5] = new Coord(coord.x+3, coord.y-2);
                    neighborCoord[ 6] = new Coord(coord.x-1, coord.y-1);
                    neighborCoord[ 7] = new Coord(coord.x  , coord.y-1);
                    neighborCoord[ 8] = new Coord(coord.x+1, coord.y-1);
                    neighborCoord[ 9] = new Coord(coord.x+2, coord.y-1);
                    neighborCoord[10] = new Coord(coord.x+3, coord.y-1);
                    neighborCoord[11] = new Coord(coord.x-2, coord.y  );
                    neighborCoord[12] = new Coord(coord.x-1, coord.y  );
                    neighborCoord[13] = new Coord(coord.x+1, coord.y  );
                    neighborCoord[14] = new Coord(coord.x+2, coord.y  );
                    neighborCoord[15] = new Coord(coord.x-2, coord.y+1);
                    neighborCoord[16] = new Coord(coord.x-1, coord.y+1);
                    neighborCoord[17] = new Coord(coord.x  , coord.y+1);
                    neighborCoord[18] = new Coord(coord.x+1, coord.y+1);
                    neighborCoord[19] = new Coord(coord.x-1, coord.y+2);
                    neighborCoord[20] = new Coord(coord.x+1, coord.y+2);
                    break;
                case 6:
                    neighborCoord[ 0] = new Coord(coord.x-2, coord.y-2);
                    neighborCoord[ 1] = new Coord(coord.x  , coord.y-2);
                    neighborCoord[ 2] = new Coord(coord.x+1, coord.y-2);
                    neighborCoord[ 3] = new Coord(coord.x+3, coord.y-2);
                    neighborCoord[ 4] = new Coord(coord.x-3, coord.y-1);
                    neighborCoord[ 5] = new Coord(coord.x-2, coord.y-1);
                    neighborCoord[ 6] = new Coord(coord.x-1, coord.y-1);
                    neighborCoord[ 7] = new Coord(coord.x  , coord.y-1);
                    neighborCoord[ 8] = new Coord(coord.x+1, coord.y-1);
                    neighborCoord[ 9] = new Coord(coord.x+2, coord.y-1);
                    neighborCoord[10] = new Coord(coord.x+3, coord.y-1);
                    neighborCoord[11] = new Coord(coord.x-3, coord.y  );
                    neighborCoord[12] = new Coord(coord.x-2, coord.y  );
                    neighborCoord[13] = new Coord(coord.x-1, coord.y  );
                    neighborCoord[14] = new Coord(coord.x+1, coord.y  );
                    neighborCoord[15] = new Coord(coord.x+2, coord.y  );
                    neighborCoord[16] = new Coord(coord.x+3, coord.y  );
                    neighborCoord[17] = new Coord(coord.x-2, coord.y+1);
                    neighborCoord[18] = new Coord(coord.x  , coord.y+1);
                    neighborCoord[19] = new Coord(coord.x+1, coord.y+1);
                    neighborCoord[20] = new Coord(coord.x+3, coord.y+1);
                    break;
                case 7:
                    neighborCoord[ 0] = new Coord(coord.x  , coord.y-2);
                    neighborCoord[ 1] = new Coord(coord.x+2, coord.y-2);
                    neighborCoord[ 2] = new Coord(coord.x-1, coord.y-1);
                    neighborCoord[ 3] = new Coord(coord.x  , coord.y-1);
                    neighborCoord[ 4] = new Coord(coord.x+1, coord.y-1);
                    neighborCoord[ 5] = new Coord(coord.x+2, coord.y-1);
                    neighborCoord[ 6] = new Coord(coord.x-2, coord.y  );
                    neighborCoord[ 7] = new Coord(coord.x-1, coord.y  );
                    neighborCoord[ 8] = new Coord(coord.x+1, coord.y  );
                    neighborCoord[ 9] = new Coord(coord.x+2, coord.y  );
                    neighborCoord[10] = new Coord(coord.x-2, coord.y+1);
                    neighborCoord[11] = new Coord(coord.x-1, coord.y+1);
                    neighborCoord[12] = new Coord(coord.x  , coord.y+1);
                    neighborCoord[13] = new Coord(coord.x+1, coord.y+1);
                    neighborCoord[14] = new Coord(coord.x+2, coord.y+1);
                    neighborCoord[15] = new Coord(coord.x-2, coord.y+2);
                    neighborCoord[16] = new Coord(coord.x-1, coord.y+2);
                    neighborCoord[17] = new Coord(coord.x  , coord.y+2);
                    neighborCoord[18] = new Coord(coord.x+1, coord.y+2);
                    neighborCoord[19] = new Coord(coord.x-2, coord.y+3);
                    neighborCoord[20] = new Coord(coord.x  , coord.y+3);
                    break;
                case 8:
                    neighborCoord[ 0] = new Coord(coord.x-1, coord.y-2);
                    neighborCoord[ 1] = new Coord(coord.x+1, coord.y-2);
                    neighborCoord[ 2] = new Coord(coord.x-2, coord.y-1);
                    neighborCoord[ 3] = new Coord(coord.x-1, coord.y-1);
                    neighborCoord[ 4] = new Coord(coord.x  , coord.y-1);
                    neighborCoord[ 5] = new Coord(coord.x+1, coord.y-1);
                    neighborCoord[ 6] = new Coord(coord.x-2, coord.y  );
                    neighborCoord[ 7] = new Coord(coord.x-1, coord.y  );
                    neighborCoord[ 8] = new Coord(coord.x+1, coord.y  );
                    neighborCoord[ 9] = new Coord(coord.x+2, coord.y  );
                    neighborCoord[10] = new Coord(coord.x-1, coord.y+1);
                    neighborCoord[11] = new Coord(coord.x  , coord.y+1);
                    neighborCoord[12] = new Coord(coord.x+1, coord.y+1);
                    neighborCoord[13] = new Coord(coord.x+2, coord.y+1);
                    neighborCoord[14] = new Coord(coord.x+3, coord.y+1);
                    neighborCoord[15] = new Coord(coord.x  , coord.y+2);
                    neighborCoord[16] = new Coord(coord.x+1, coord.y+2);
                    neighborCoord[17] = new Coord(coord.x+2, coord.y+2);
                    neighborCoord[18] = new Coord(coord.x+3, coord.y+2);
                    neighborCoord[19] = new Coord(coord.x  , coord.y+3);
                    neighborCoord[20] = new Coord(coord.x+2, coord.y+3);
                    break;
                case 9:
                    neighborCoord[ 0] = new Coord(coord.x-2, coord.y-3);
                    neighborCoord[ 1] = new Coord(coord.x  , coord.y-3);
                    neighborCoord[ 2] = new Coord(coord.x-3, coord.y-2);
                    neighborCoord[ 3] = new Coord(coord.x-2, coord.y-2);
                    neighborCoord[ 4] = new Coord(coord.x-1, coord.y-2);
                    neighborCoord[ 5] = new Coord(coord.x  , coord.y-2);
                    neighborCoord[ 6] = new Coord(coord.x-3, coord.y-1);
                    neighborCoord[ 7] = new Coord(coord.x-2, coord.y-1);
                    neighborCoord[ 8] = new Coord(coord.x-1, coord.y-1);
                    neighborCoord[ 9] = new Coord(coord.x  , coord.y-1);
                    neighborCoord[10] = new Coord(coord.x+1, coord.y-1);
                    neighborCoord[11] = new Coord(coord.x-2, coord.y  );
                    neighborCoord[12] = new Coord(coord.x-1, coord.y  );
                    neighborCoord[13] = new Coord(coord.x+1, coord.y  );
                    neighborCoord[14] = new Coord(coord.x+2, coord.y  );
                    neighborCoord[15] = new Coord(coord.x-1, coord.y+1);
                    neighborCoord[16] = new Coord(coord.x  , coord.y+1);
                    neighborCoord[17] = new Coord(coord.x+1, coord.y+1);
                    neighborCoord[18] = new Coord(coord.x+2, coord.y+1);
                    neighborCoord[19] = new Coord(coord.x-1, coord.y+2);
                    neighborCoord[20] = new Coord(coord.x+1, coord.y+2);
                    break;
                case 10:
                    neighborCoord[ 0] = new Coord(coord.x  , coord.y-3);
                    neighborCoord[ 1] = new Coord(coord.x+2, coord.y-3);
                    neighborCoord[ 2] = new Coord(coord.x-1, coord.y-2);
                    neighborCoord[ 3] = new Coord(coord.x  , coord.y-2);
                    neighborCoord[ 4] = new Coord(coord.x+1, coord.y-2);
                    neighborCoord[ 5] = new Coord(coord.x+2, coord.y-2);
                    neighborCoord[ 6] = new Coord(coord.x-2, coord.y-1);
                    neighborCoord[ 7] = new Coord(coord.x-1, coord.y-1);
                    neighborCoord[ 8] = new Coord(coord.x  , coord.y-1);
                    neighborCoord[ 9] = new Coord(coord.x+1, coord.y-1);
                    neighborCoord[10] = new Coord(coord.x+2, coord.y-1);
                    neighborCoord[11] = new Coord(coord.x-2, coord.y  );
                    neighborCoord[12] = new Coord(coord.x-1, coord.y  );
                    neighborCoord[13] = new Coord(coord.x+1, coord.y  );
                    neighborCoord[14] = new Coord(coord.x+2, coord.y  );
                    neighborCoord[15] = new Coord(coord.x-2, coord.y+1);
                    neighborCoord[16] = new Coord(coord.x-1, coord.y+1);
                    neighborCoord[17] = new Coord(coord.x  , coord.y+1);
                    neighborCoord[18] = new Coord(coord.x+1, coord.y+1);
                    neighborCoord[19] = new Coord(coord.x-2, coord.y+2);
                    neighborCoord[20] = new Coord(coord.x  , coord.y+2);
                    break;
                case 11:
                    neighborCoord[ 0] = new Coord(coord.x-3, coord.y-1);
                    neighborCoord[ 1] = new Coord(coord.x-1, coord.y-1);
                    neighborCoord[ 2] = new Coord(coord.x  , coord.y-1);
                    neighborCoord[ 3] = new Coord(coord.x+2, coord.y-1);
                    neighborCoord[ 4] = new Coord(coord.x-3, coord.y  );
                    neighborCoord[ 5] = new Coord(coord.x-2, coord.y  );
                    neighborCoord[ 6] = new Coord(coord.x-1, coord.y  );
                    neighborCoord[ 7] = new Coord(coord.x+1, coord.y  );
                    neighborCoord[ 8] = new Coord(coord.x+2, coord.y  );
                    neighborCoord[ 9] = new Coord(coord.x+3, coord.y  );
                    neighborCoord[10] = new Coord(coord.x-3, coord.y+1);
                    neighborCoord[11] = new Coord(coord.x-2, coord.y+1);
                    neighborCoord[12] = new Coord(coord.x-1, coord.y+1);
                    neighborCoord[13] = new Coord(coord.x  , coord.y+1);
                    neighborCoord[14] = new Coord(coord.x+1, coord.y+1);
                    neighborCoord[15] = new Coord(coord.x+2, coord.y+1);
                    neighborCoord[16] = new Coord(coord.x+3, coord.y+1);
                    neighborCoord[17] = new Coord(coord.x-3, coord.y+2);
                    neighborCoord[18] = new Coord(coord.x-1, coord.y+2);
                    neighborCoord[19] = new Coord(coord.x  , coord.y+2);
                    neighborCoord[20] = new Coord(coord.x+2, coord.y+2);
                    break;
                }
                #endregion
                break;
            case ComplexityMode.eMeanMode:
                #region
                switch (direction) {
                case 0:
                    neighborCoord[0] = new Coord(coord.x-1, coord.y+1);
                    neighborCoord[1] = new Coord(coord.x  , coord.y+1);
                    neighborCoord[2] = new Coord(coord.x+1, coord.y+1);
                    break;
                case 1:
                    neighborCoord[0] = new Coord(coord.x+1, coord.y  );
                    neighborCoord[1] = new Coord(coord.x  , coord.y+1);
                    neighborCoord[2] = new Coord(coord.x+1, coord.y+1);
                    neighborCoord[3] = new Coord(coord.x+1, coord.y+2);
                    neighborCoord[4] = new Coord(coord.x  , coord.y+3);
                    break;
                case 2:
                    neighborCoord[0] = new Coord(coord.x-3, coord.y-1);
                    neighborCoord[1] = new Coord(coord.x  , coord.y-1);
                    neighborCoord[2] = new Coord(coord.x+3, coord.y-1);
                    neighborCoord[3] = new Coord(coord.x-3, coord.y  );
                    neighborCoord[4] = new Coord(coord.x-1, coord.y  );
                    neighborCoord[5] = new Coord(coord.x+3, coord.y  );
                    neighborCoord[6] = new Coord(coord.x  , coord.y+1);
                    break;
                case 3:
                    neighborCoord[0] = new Coord(coord.x  , coord.y-1);
                    neighborCoord[1] = new Coord(coord.x+1, coord.y  );
                    neighborCoord[2] = new Coord(coord.x  , coord.y+1);
                    break;
                case 4:
                    neighborCoord[0] = new Coord(coord.x-1, coord.y-1);
                    neighborCoord[1] = new Coord(coord.x  , coord.y-1);
                    neighborCoord[2] = new Coord(coord.x-1, coord.y  );
                    break;
                case 5:
                    neighborCoord[0] = new Coord(coord.x-1, coord.y-1);
                    neighborCoord[1] = new Coord(coord.x  , coord.y-1);
                    neighborCoord[2] = new Coord(coord.x+1, coord.y-1);
                    neighborCoord[3] = new Coord(coord.x  , coord.y+1);
                    neighborCoord[4] = new Coord(coord.x-1, coord.y+2);
                    break;
                case 6:
                    neighborCoord[0] = new Coord(coord.x  , coord.y-1);
                    neighborCoord[1] = new Coord(coord.x+1, coord.y  );
                    neighborCoord[2] = new Coord(coord.x  , coord.y+1);
                    break;
                case 7:
                    neighborCoord[0] = new Coord(coord.x-1, coord.y  );
                    neighborCoord[1] = new Coord(coord.x-1, coord.y+1);
                    neighborCoord[2] = new Coord(coord.x  , coord.y+1);
                    break;
                case 8:
                    neighborCoord[0] = new Coord(coord.x-1, coord.y-2);
                    neighborCoord[1] = new Coord(coord.x  , coord.y-1);
                    neighborCoord[2] = new Coord(coord.x-1, coord.y+1);
                    neighborCoord[3] = new Coord(coord.x  , coord.y+1);
                    neighborCoord[4] = new Coord(coord.x+1, coord.y+1);
                    break;
                case 9:
                    neighborCoord[0] = new Coord(coord.x-1, coord.y-1);
                    neighborCoord[1] = new Coord(coord.x  , coord.y-1);
                    neighborCoord[2] = new Coord(coord.x+1, coord.y-1);
                    break;
                case 10:
                    neighborCoord[0] = new Coord(coord.x  , coord.y-3);
                    neighborCoord[1] = new Coord(coord.x+1, coord.y-2);
                    neighborCoord[2] = new Coord(coord.x  , coord.y-1);
                    neighborCoord[3] = new Coord(coord.x+1, coord.y-1);
                    neighborCoord[4] = new Coord(coord.x+1, coord.y  );
                    break;
                case 11:
                    neighborCoord[0] = new Coord(coord.x  , coord.y-1);
                    neighborCoord[1] = new Coord(coord.x-3, coord.y  );
                    neighborCoord[2] = new Coord(coord.x-1, coord.y  );
                    neighborCoord[3] = new Coord(coord.x+3, coord.y  );
                    neighborCoord[4] = new Coord(coord.x-3, coord.y+1);
                    neighborCoord[5] = new Coord(coord.x  , coord.y+1);
                    neighborCoord[6] = new Coord(coord.x+3, coord.y+1);
                    break;
                }
                #endregion
                break;
            case ComplexityMode.eOptimalMode:
                #region
                switch (direction) {
                case 0:
                    neighborCoord[0] = new Coord(coord.x+1, coord.y-2);
                    neighborCoord[1] = new Coord(coord.x+1, coord.y-1);
                    neighborCoord[2] = new Coord(coord.x+2, coord.y-1);
                    neighborCoord[3] = new Coord(coord.x-1, coord.y  );
                    neighborCoord[4] = new Coord(coord.x-1, coord.y+1);
                    neighborCoord[5] = new Coord(coord.x  , coord.y+1);
                    neighborCoord[6] = new Coord(coord.x+1, coord.y+1);
                    break;
                case 1:
                    neighborCoord[0] = new Coord(coord.x+1, coord.y  );
                    neighborCoord[1] = new Coord(coord.x-1, coord.y+1);
                    neighborCoord[2] = new Coord(coord.x  , coord.y+1);
                    neighborCoord[3] = new Coord(coord.x+1, coord.y+1);
                    neighborCoord[4] = new Coord(coord.x+1, coord.y+2);
                    neighborCoord[5] = new Coord(coord.x+2, coord.y+2);
                    neighborCoord[6] = new Coord(coord.x+2, coord.y+3);
                    break;
                case 2:
                    neighborCoord[0] = new Coord(coord.x+2, coord.y-2);
                    neighborCoord[1] = new Coord(coord.x  , coord.y-1);
                    neighborCoord[2] = new Coord(coord.x+2, coord.y-1);
                    neighborCoord[3] = new Coord(coord.x+3, coord.y-1);
                    neighborCoord[4] = new Coord(coord.x-1, coord.y  );
                    neighborCoord[5] = new Coord(coord.x+1, coord.y  );
                    neighborCoord[6] = new Coord(coord.x  , coord.y+1);
                    break;
                case 3:
                    neighborCoord[0] = new Coord(coord.x  , coord.y-1);
                    neighborCoord[1] = new Coord(coord.x+1, coord.y-1);
                    neighborCoord[2] = new Coord(coord.x+1, coord.y  );
                    neighborCoord[3] = new Coord(coord.x  , coord.y+1);
                    neighborCoord[4] = new Coord(coord.x+2, coord.y+1);
                    neighborCoord[5] = new Coord(coord.x+3, coord.y+1);
                    neighborCoord[6] = new Coord(coord.x+3, coord.y+2);
                    break;
                case 4:
                    neighborCoord[0] = new Coord(coord.x-1, coord.y-1);
                    neighborCoord[1] = new Coord(coord.x  , coord.y-1);
                    neighborCoord[2] = new Coord(coord.x-1, coord.y  );
                    neighborCoord[3] = new Coord(coord.x+1, coord.y+1);
                    neighborCoord[4] = new Coord(coord.x+2, coord.y+1);
                    neighborCoord[5] = new Coord(coord.x+2, coord.y+2);
                    break;
                case 5:
                    neighborCoord[0] = new Coord(coord.x+2, coord.y-3);
                    neighborCoord[1] = new Coord(coord.x+2, coord.y-2);
                    neighborCoord[2] = new Coord(coord.x+3, coord.y-2);
                    neighborCoord[3] = new Coord(coord.x-1, coord.y-1);
                    neighborCoord[4] = new Coord(coord.x  , coord.y-1);
                    neighborCoord[5] = new Coord(coord.x+1, coord.y-1);
                    break;
                case 6:
                    neighborCoord[0] = new Coord(coord.x-2, coord.y-2);
                    neighborCoord[1] = new Coord(coord.x-3, coord.y-1);
                    neighborCoord[2] = new Coord(coord.x-2, coord.y-1);
                    neighborCoord[3] = new Coord(coord.x  , coord.y-1);
                    neighborCoord[4] = new Coord(coord.x-1, coord.y  );
                    neighborCoord[5] = new Coord(coord.x+1, coord.y  );
                    neighborCoord[6] = new Coord(coord.x  , coord.y+1);
                    break;
                case 7:
                    neighborCoord[0] = new Coord(coord.x-1, coord.y  );
                    neighborCoord[1] = new Coord(coord.x-1, coord.y+1);
                    neighborCoord[2] = new Coord(coord.x  , coord.y+1);
                    neighborCoord[3] = new Coord(coord.x+1, coord.y+1);
                    neighborCoord[4] = new Coord(coord.x-2, coord.y+2);
                    neighborCoord[5] = new Coord(coord.x-1, coord.y+2);
                    neighborCoord[6] = new Coord(coord.x-2, coord.y+3);
                    break;
                case 8:
                    neighborCoord[0] = new Coord(coord.x-1, coord.y-2);
                    neighborCoord[1] = new Coord(coord.x-2, coord.y-1);
                    neighborCoord[2] = new Coord(coord.x-1, coord.y-1);
                    neighborCoord[3] = new Coord(coord.x+1, coord.y  );
                    neighborCoord[4] = new Coord(coord.x-1, coord.y+1);
                    neighborCoord[5] = new Coord(coord.x  , coord.y+1);
                    neighborCoord[6] = new Coord(coord.x+1, coord.y+1);
                    break;
                case 9:
                    neighborCoord[0] = new Coord(coord.x-2, coord.y-3);
                    neighborCoord[1] = new Coord(coord.x-3, coord.y-2);
                    neighborCoord[2] = new Coord(coord.x-2, coord.y-2);
                    neighborCoord[3] = new Coord(coord.x-1, coord.y-1);
                    neighborCoord[4] = new Coord(coord.x  , coord.y-1);
                    neighborCoord[5] = new Coord(coord.x+1, coord.y-1);
                    break;
                case 10:
                    neighborCoord[0] = new Coord(coord.x  , coord.y-1);
                    neighborCoord[1] = new Coord(coord.x+1, coord.y-1);
                    neighborCoord[2] = new Coord(coord.x+1, coord.y  );
                    neighborCoord[3] = new Coord(coord.x-2, coord.y+1);
                    neighborCoord[4] = new Coord(coord.x-1, coord.y+1);
                    neighborCoord[5] = new Coord(coord.x-2, coord.y+2);
                    break;
                case 11:
                    neighborCoord[0] = new Coord(coord.x-1, coord.y-1);
                    neighborCoord[1] = new Coord(coord.x  , coord.y-1);
                    neighborCoord[2] = new Coord(coord.x-1, coord.y  );
                    neighborCoord[3] = new Coord(coord.x-3, coord.y+1);
                    neighborCoord[4] = new Coord(coord.x-2, coord.y+1);
                    neighborCoord[5] = new Coord(coord.x  , coord.y+1);
                    neighborCoord[6] = new Coord(coord.x-3, coord.y+2);
                    break;
                }
                #endregion
                break;
            case ComplexityMode.eSimpeMode:
                #region
                switch (direction) {
                case 0:
                    neighborCoord[0] = new Coord(coord.x-1, coord.y+1);
                    neighborCoord[1] = new Coord(coord.x  , coord.y+1);
                    neighborCoord[2] = new Coord(coord.x+1, coord.y+1);
                    break;
                case 1:
                    neighborCoord[0] = new Coord(coord.x+1, coord.y  );
                    neighborCoord[1] = new Coord(coord.x  , coord.y+1);
                    neighborCoord[2] = new Coord(coord.x+1, coord.y+1);
                    break;
                case 2:
                    neighborCoord[0] = new Coord(coord.x  , coord.y-1);
                    neighborCoord[1] = new Coord(coord.x-1, coord.y  );
                    neighborCoord[2] = new Coord(coord.x  , coord.y+1);
                    break;
                case 3:
                    neighborCoord[0] = new Coord(coord.x  , coord.y-1);
                    neighborCoord[1] = new Coord(coord.x+1, coord.y  );
                    neighborCoord[2] = new Coord(coord.x  , coord.y+1);
                    break;
                case 4:
                    neighborCoord[0] = new Coord(coord.x-1, coord.y-1);
                    neighborCoord[1] = new Coord(coord.x  , coord.y-1);
                    neighborCoord[2] = new Coord(coord.x-1, coord.y  );
                    break;
                case 5:
                    neighborCoord[0] = new Coord(coord.x-1, coord.y-1);
                    neighborCoord[1] = new Coord(coord.x  , coord.y-1);
                    neighborCoord[2] = new Coord(coord.x+1, coord.y-1);
                    break;
                case 6:
                    neighborCoord[0] = new Coord(coord.x  , coord.y-1);
                    neighborCoord[1] = new Coord(coord.x+1, coord.y  );
                    neighborCoord[2] = new Coord(coord.x  , coord.y+1);
                    break;
                case 7:
                    neighborCoord[0] = new Coord(coord.x-1, coord.y  );
                    neighborCoord[1] = new Coord(coord.x-1, coord.y+1);
                    neighborCoord[2] = new Coord(coord.x  , coord.y+1);
                    break;
                case 8:
                    neighborCoord[0] = new Coord(coord.x-1, coord.y+1);
                    neighborCoord[1] = new Coord(coord.x  , coord.y+1);
                    neighborCoord[2] = new Coord(coord.x+1, coord.y+1);
                    break;
                case 9:
                    neighborCoord[0] = new Coord(coord.x-1, coord.y-1);
                    neighborCoord[1] = new Coord(coord.x  , coord.y-1);
                    neighborCoord[2] = new Coord(coord.x+1, coord.y-1);
                    break;
                case 10:
                    neighborCoord[0] = new Coord(coord.x  , coord.y-1);
                    neighborCoord[1] = new Coord(coord.x+1, coord.y-1);
                    neighborCoord[2] = new Coord(coord.x+1, coord.y  );
                    break;
                case 11:
                    neighborCoord[0] = new Coord(coord.x  , coord.y-1);
                    neighborCoord[1] = new Coord(coord.x-1, coord.y  );
                    neighborCoord[2] = new Coord(coord.x  , coord.y+1);
                    break;
                }
                #endregion
                break;
            }
            return neighborCoord;
        }

        protected override void CalcRegion() {
            var shape = Shape;
            var a = shape.A;
            var b = shape.B;
            var R = shape.ROut;
            var r = shape.RIn;
            var s = (ShapeTriangle4.Mode != ComplexityMode.eUnrealMode) ? shape.CalcSnip() : 0;
            var c = (ShapeTriangle4.Mode != ComplexityMode.eUnrealMode) ? s/2*BaseShape.SQRT3 : 0; // s * cos30
            var u = (ShapeTriangle4.Mode != ComplexityMode.eUnrealMode) ? s/2 : 0; // s * cos60

            // определение координат точек фигуры
            var oX =  (coord.x/3)*a + b;      // offset X
            var oY = ((coord.y/4)*2+1)*(R+r); // offset Y
            switch (ShapeTriangle4.Mode) {
            case ComplexityMode.eUnrealMode:
            case ComplexityMode.eOptimalMode:
                break;
            case ComplexityMode.eMeanMode:
            case ComplexityMode.eSimpeMode:
                oX -= u;
                break;
            }

            switch (ShapeTriangle4.Mode) {
            case ComplexityMode.eUnrealMode:
                #region
                switch (direction) {
                case 0:
                    region.SetPoint(0, oX    , oY - R-r);
                    region.SetPoint(1, oX    , oY - r  );
                    region.SetPoint(2, oX - b, oY      );
                    break;
                case 1:
                    region.SetPoint(0, oX + b, oY - R  );
                    region.SetPoint(1, oX + b, oY      );
                    region.SetPoint(2, oX    , oY - R-r);
                    break;
                case 2:
                    region.SetPoint(0, oX + a, oY - R-r);
                    region.SetPoint(1, oX + b, oY - R  );
                    region.SetPoint(2, oX    , oY - R-r);
                    break;
                case 3:
                    region.SetPoint(0, oX + b, oY      );
                    region.SetPoint(1, oX - b, oY      );
                    region.SetPoint(2, oX    , oY - r  );
                    break;
                case 4:
                    region.SetPoint(0, oX    , oY - R-r);
                    region.SetPoint(1, oX + b, oY      );
                    region.SetPoint(2, oX    , oY - r  );
                    break;
                case 5:
                    region.SetPoint(0, oX + a, oY - R-r);
                    region.SetPoint(1, oX + b, oY      );
                    region.SetPoint(2, oX + b, oY - R  );
                    break;
                case 6:
                    region.SetPoint(0, oX + b, oY      );
                    region.SetPoint(1, oX    , oY + r  );
                    region.SetPoint(2, oX - b, oY      );
                    break;
                case 7:
                    region.SetPoint(0, oX + b, oY      );
                    region.SetPoint(1, oX    , oY + R+r);
                    region.SetPoint(2, oX    , oY + r  );
                    break;
                case 8:
                    region.SetPoint(0, oX + b, oY      );
                    region.SetPoint(1, oX + a, oY + R+r);
                    region.SetPoint(2, oX + b, oY + R  );
                    break;
                case 9:
                    region.SetPoint(0, oX    , oY + r  );
                    region.SetPoint(1, oX    , oY + R+r);
                    region.SetPoint(2, oX - b, oY      );
                    break;
                case 10:
                    region.SetPoint(0, oX + b, oY      );
                    region.SetPoint(1, oX + b, oY + R  );
                    region.SetPoint(2, oX    , oY + R+r);
                    break;
                case 11:
                    region.SetPoint(0, oX + a, oY + R+r);
                    region.SetPoint(1, oX    , oY + R+r);
                    region.SetPoint(2, oX + b, oY + R  );
                    break;
                }
                #endregion
                break;
            case ComplexityMode.eMeanMode:
                #region
                switch (direction) {
                case 0:
                    region.SetPoint(0, oX    , oY-R-r+s);
                    region.SetPoint(1, oX    , oY - r  );
                    region.SetPoint(2, oX-b+c, oY - u  );
                    region.SetPoint(3, oX-b+u, oY - c  );
                    region.SetPoint(4, oX - u, oY-R-r+c);
                    break;
                case 1:
                    region.SetPoint(0, oX + b, oY - R  );
                    region.SetPoint(1, oX + b, oY      );
                    region.SetPoint(2, oX + u, oY-R-r+c);
                    region.SetPoint(3, oX + c, oY-R-r+u);
                    break;
                case 2:
                    region.SetPoint(0, oX + a, oY - R-r);
                    region.SetPoint(1, oX + b, oY - R  );
                    region.SetPoint(2, oX    , oY - R-r);
                    break;
                case 3:
                    region.SetPoint(0, oX+b-s, oY      );
                    region.SetPoint(1, oX-b+s, oY      );
                    region.SetPoint(2, oX-b+c, oY - u  );
                    region.SetPoint(3, oX    , oY - r  );
                    region.SetPoint(4, oX+b-c, oY - u  );
                    break;
                case 4:
                    region.SetPoint(0, oX + u, oY-R-r+c);
                    region.SetPoint(1, oX+b-u, oY - c  );
                    region.SetPoint(2, oX+b-c, oY - u  );
                    region.SetPoint(3, oX    , oY - r  );
                    region.SetPoint(4, oX    , oY-R-r+s);
                    break;
                case 5:
                    region.SetPoint(0, oX+a-u, oY-R-r+c);
                    region.SetPoint(1, oX + b, oY      );
                    region.SetPoint(2, oX + b, oY - R  );
                    region.SetPoint(3, oX+a-c, oY-R-r+u);
                    break;
                case 6:
                    region.SetPoint(0, oX+b-c, oY + u  );
                    region.SetPoint(1, oX    , oY + r  );
                    region.SetPoint(2, oX-b+c, oY + u  );
                    region.SetPoint(3, oX-b+s, oY      );
                    region.SetPoint(4, oX+b-s, oY      );
                    break;
                case 7:
                    region.SetPoint(0, oX+b-u, oY + c  );
                    region.SetPoint(1, oX + u, oY+R+r-c);
                    region.SetPoint(2, oX    , oY+R+r-s);
                    region.SetPoint(3, oX    , oY + r  );
                    region.SetPoint(4, oX+b-c, oY + u  );
                    break;
                case 8:
                    region.SetPoint(0, oX + b, oY      );
                    region.SetPoint(1, oX+a-u, oY+R+r-c);
                    region.SetPoint(2, oX+a-c, oY+R+r-u);
                    region.SetPoint(3, oX + b, oY + R  );
                    break;
                case 9:
                    region.SetPoint(0, oX    , oY + r  );
                    region.SetPoint(1, oX    , oY+R+r-s);
                    region.SetPoint(2, oX - u, oY+R+r-c);
                    region.SetPoint(3, oX-b+u, oY + c  );
                    region.SetPoint(4, oX-b+c, oY + u  );
                    break;
                case 10:
                    region.SetPoint(0, oX + b, oY      );
                    region.SetPoint(1, oX + b, oY + R  );
                    region.SetPoint(2, oX + c, oY+R+r-u);
                    region.SetPoint(3, oX + u, oY+R+r-c);
                    break;
                case 11:
                    region.SetPoint(0, oX + a, oY + R+r);
                    region.SetPoint(2, oX + b, oY + R  );
                    region.SetPoint(1, oX    , oY + R+r);
                    break;
                }
                #endregion
                break;
            case ComplexityMode.eOptimalMode:
                #region
                switch (direction) {
                case 0:
                    region.SetPoint(0, oX    , oY - R-r);
                    region.SetPoint(1, oX    , oY - r  );
                    region.SetPoint(2, oX-b+c, oY - u  );
                    region.SetPoint(3, oX-b+u, oY - c  );
                    break;
                case 1:
                    region.SetPoint(0, oX + b, oY - R  );
                    region.SetPoint(1, oX + b, oY      );
                    region.SetPoint(2, oX + u, oY-R-r+c);
                    region.SetPoint(3, oX + c, oY-R-r+u);
                    break;
                case 2:
                    region.SetPoint(0, oX + a, oY -R -r);
                    region.SetPoint(1, oX + b, oY - R  );
                    region.SetPoint(2, oX + c, oY-R-r+u);
                    region.SetPoint(3, oX + s, oY -R -r);
                    break;
                case 3:
                    region.SetPoint(0, oX+b  , oY      );
                    region.SetPoint(1, oX-b+s, oY      );
                    region.SetPoint(2, oX-b+c, oY - u  );
                    region.SetPoint(3, oX    , oY - r  );
                    break;
                case 4:
                    region.SetPoint(0, oX + u, oY-R-r+c);
                    region.SetPoint(1, oX + b, oY      );
                    region.SetPoint(2, oX    , oY - r  );
                    region.SetPoint(3, oX    , oY-R-r+s);
                    break;
                case 5:
                    region.SetPoint(0, oX+a  , oY - R-r);
                    region.SetPoint(1, oX+b+u, oY - c  );
                    region.SetPoint(2, oX + b, oY - s  );
                    region.SetPoint(3, oX + b, oY - R  );
                    break;
                case 6:
                    region.SetPoint(0, oX+b-c, oY + u  );
                    region.SetPoint(1, oX    , oY + r  );
                    region.SetPoint(2, oX-b  , oY      );
                    region.SetPoint(3, oX+b-s, oY      );
                    break;
                case 7:
                    region.SetPoint(0, oX+b-u, oY + c  );
                    region.SetPoint(1, oX    , oY + R+r);
                    region.SetPoint(2, oX    , oY + r  );
                    region.SetPoint(3, oX+b-c, oY + u  );
                    break;
                case 8:
                    region.SetPoint(0, oX + b, oY      );
                    region.SetPoint(1, oX+a-u, oY+R+r-c);
                    region.SetPoint(2, oX+a-c, oY+R+r-u);
                    region.SetPoint(3, oX + b, oY + R  );
                    break;
                case 9:
                    region.SetPoint(0, oX    , oY + r  );
                    region.SetPoint(1, oX    , oY+R+r-s);
                    region.SetPoint(2, oX - u, oY+R+r-c);
                    region.SetPoint(3, oX - b, oY      );
                    break;
                case 10:
                    region.SetPoint(0, oX + b, oY + s  );
                    region.SetPoint(1, oX + b, oY + R  );
                    region.SetPoint(2, oX    , oY+R+r  );
                    region.SetPoint(3, oX+b-u, oY + c  );
                    break;
                case 11:
                    region.SetPoint(0, oX+a-c, oY+R+r-u);
                    region.SetPoint(1, oX+a-s, oY + R+r);
                    region.SetPoint(2, oX    , oY + R+r);
                    region.SetPoint(3, oX + b, oY + R  );
                    break;
                }
                #endregion
                break;
            case ComplexityMode.eSimpeMode:
                #region
                switch (direction) {
                case 0:
                    region.SetPoint(0, oX    , oY-R-r+s);
                    region.SetPoint(1, oX    , oY - r  );
                    region.SetPoint(2, oX-b+c, oY - u  );
                    region.SetPoint(3, oX-b+u, oY - c  );
                    region.SetPoint(4, oX - u, oY-R-r+c);
                    break;
                case 1:
                    region.SetPoint(0, oX + b, oY - R  );
                    region.SetPoint(1, oX + b, oY - s  );
                    region.SetPoint(2, oX+b-u, oY - c  );
                    region.SetPoint(3, oX + u, oY-R-r+c);
                    region.SetPoint(4, oX + c, oY-R-r+u);
                    break;
                case 2:
                    region.SetPoint(0, oX+a-c, oY-R-r+u);
                    region.SetPoint(1, oX + b, oY - R  );
                    region.SetPoint(2, oX + c, oY-R-r+u);
                    region.SetPoint(3, oX + s, oY -R -r);
                    region.SetPoint(4, oX+a-s, oY -R -r);
                    break;
                case 3:
                    region.SetPoint(0, oX+b-s, oY      );
                    region.SetPoint(1, oX-b+s, oY      );
                    region.SetPoint(2, oX-b+c, oY - u  );
                    region.SetPoint(3, oX    , oY - r  );
                    region.SetPoint(4, oX+b-c, oY - u  );
                    break;
                case 4:
                    region.SetPoint(0, oX + u, oY-R-r+c);
                    region.SetPoint(1, oX+b-u, oY - c  );
                    region.SetPoint(2, oX+b-c, oY - u  );
                    region.SetPoint(3, oX    , oY - r  );
                    region.SetPoint(4, oX    , oY-R-r+s);
                    break;
                case 5:
                    region.SetPoint(0, oX+a-u, oY-R-r+c);
                    region.SetPoint(1, oX+b+u, oY - c  );
                    region.SetPoint(2, oX + b, oY - s  );
                    region.SetPoint(3, oX + b, oY - R  );
                    region.SetPoint(4, oX+a-c, oY-R-r+u);
                    break;
                case 6:
                    region.SetPoint(0, oX+b-c, oY + u  );
                    region.SetPoint(1, oX    , oY + r  );
                    region.SetPoint(2, oX-b+c, oY + u  );
                    region.SetPoint(3, oX-b+s, oY      );
                    region.SetPoint(4, oX+b-s, oY      );
                    break;
                case 7:
                    region.SetPoint(0, oX+b-u, oY + c  );
                    region.SetPoint(1, oX + u, oY+R+r-c);
                    region.SetPoint(2, oX    , oY+R+r-s);
                    region.SetPoint(3, oX    , oY + r  );
                    region.SetPoint(4, oX+b-c, oY + u  );
                    break;
                case 8:
                    region.SetPoint(0, oX+b+u, oY + c  );
                    region.SetPoint(1, oX+a-u, oY+R+r-c);
                    region.SetPoint(2, oX+a-c, oY+R+r-u);
                    region.SetPoint(3, oX + b, oY + R  );
                    region.SetPoint(4, oX + b, oY + s  );
                    break;
                case 9:
                    region.SetPoint(0, oX    , oY + r  );
                    region.SetPoint(1, oX    , oY+R+r-s);
                    region.SetPoint(2, oX - u, oY+R+r-c);
                    region.SetPoint(3, oX-b+u, oY + c  );
                    region.SetPoint(4, oX-b+c, oY + u  );
                    break;
                case 10:
                    region.SetPoint(0, oX + b, oY + s  );
                    region.SetPoint(1, oX + b, oY + R  );
                    region.SetPoint(2, oX + c, oY+R+r-u);
                    region.SetPoint(3, oX + u, oY+R+r-c);
                    region.SetPoint(4, oX+b-u, oY + c  );
                    break;
                case 11:
                    region.SetPoint(0, oX+a-s, oY + R+r);
                    region.SetPoint(1, oX + s, oY + R+r);
                    region.SetPoint(2, oX + c, oY+R+r-u);
                    region.SetPoint(3, oX + b, oY + R  );
                    region.SetPoint(4, oX+a-c, oY+R+r-u);
                    break;
                }
                #endregion
                break;
            }
         }

         public override RectDouble GetRcInner(double borderWidth) {
            var shape = Shape;
            var w = borderWidth/2.0;
            var sq    = shape.GetSq(borderWidth);
            var sq2   = sq/2;
            var sq2w  = sq2+w;
            var sq2w3 = sq2+w/BaseShape.SQRT3;

            var center = new PointDouble(); // координата центра квадрата
            switch (direction) {
            case 0: case 10:
                center.X = region.GetPoint(1).X - sq2w;
                center.Y = region.GetPoint(1).Y - sq2w3;
                break;
            case 1: case 9:
                center.X = region.GetPoint(0).X - sq2w;
                center.Y = region.GetPoint(0).Y + sq2w3;
                break;
            case 2: case 6:
                center.X = region.GetPoint(1).X;
                switch (ShapeTriangle4.Mode) {
                case ComplexityMode.eUnrealMode : center.Y = region.GetPoint(                 0    ).Y + sq2w; break;
                case ComplexityMode.eMeanMode   : center.Y = region.GetPoint((direction==2) ? 0 : 4).Y + sq2w; break;
                case ComplexityMode.eOptimalMode: center.Y = region.GetPoint(                   3  ).Y + sq2w; break;
                case ComplexityMode.eSimpeMode  : center.Y = region.GetPoint(                     4).Y + sq2w; break;
                }
                break;
            case 3: case 11:
                switch (ShapeTriangle4.Mode) {
                case ComplexityMode.eUnrealMode : center.X = region.GetPoint(                     2).X; break;
                case ComplexityMode.eMeanMode   : center.X = region.GetPoint((direction==3) ? 3 : 2).X; break;
                case ComplexityMode.eOptimalMode: center.X = region.GetPoint(                 3    ).X; break;
                case ComplexityMode.eSimpeMode  : center.X = region.GetPoint(                 3    ).X; break;
                }
                center.Y = region.GetPoint(1).Y - sq2w;
                break;
            case 4: case 8:
                switch (ShapeTriangle4.Mode) {
                case ComplexityMode.eUnrealMode:
                    center.X = region.GetPoint(2).X + sq2w;
                    center.Y = region.GetPoint(2).Y - sq2w3;
                    break;
                case ComplexityMode.eOptimalMode:
                    center.X = region.GetPoint(3).X + sq2w;
                    center.Y = region.GetPoint((direction==4) ? 2 : 3).Y - sq2w3;
                    break;
                case ComplexityMode.eMeanMode  :
                case ComplexityMode.eSimpeMode :
                    center.X = region.GetPoint(3).X + sq2w;
                    center.Y = region.GetPoint(3).Y - sq2w3;
                    break;
                }
                break;
            case 5: case 7:
                switch (ShapeTriangle4.Mode) {
                case ComplexityMode.eUnrealMode : center.X = region.GetPoint(                 2    ).X + sq2w;
                                                  center.Y = region.GetPoint(                 2    ).Y + sq2w3; break;
                case ComplexityMode.eMeanMode   : center.X = region.GetPoint((direction==5) ? 2 : 3).X + sq2w;
                                                  center.Y = region.GetPoint((direction==5) ? 2 : 3).Y + sq2w3; break;
                case ComplexityMode.eOptimalMode: center.X = region.GetPoint((direction!=5) ? 2 : 3).X + sq2w;
                                                  center.Y = region.GetPoint((direction!=5) ? 2 : 3).Y + sq2w3; break;
                case ComplexityMode.eSimpeMode  : center.X = region.GetPoint(                     3).X + sq2w;
                                                  center.Y = region.GetPoint(                     3).Y + sq2w3; break;
                }
                break;
            }

            return new RectDouble(
                center.X - sq2,
                center.Y - sq2,
                sq, sq);
        }

        public override int GetShiftPointBorderIndex() {
            switch (ShapeTriangle4.Mode) {
            case ComplexityMode.eUnrealMode:
                switch (direction) {
                case 1: case 3: case 5: case 7: case 9: case 11: return 1;
                default: return 2;
                }
            case ComplexityMode.eMeanMode:
                switch (direction) {
                case 1: case 3: case 5: case 7: case 9: case 11: return 1;
                case 4: case 8: return 3;
                default: return 2;
                }
            case ComplexityMode.eOptimalMode:
                switch (direction) {
                case 1: case 3: case 5: case 7: return 1;
                case 8: return 3;
                default: return 2;
                }
            case ComplexityMode.eSimpeMode:
                switch (direction) {
                case 1: case 2: case 3: case 5: case 7: case 9: case 11: return 2;
                default: return 3;
                }
            default: throw new InvalidOperationException("Unknown Mode==" + ShapeTriangle4.Mode);
            }
        }

    }

}
