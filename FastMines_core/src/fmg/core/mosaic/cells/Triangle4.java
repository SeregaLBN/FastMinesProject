////////////////////////////////////////////////////////////////////////////////
//                               FastMines project
// file name: "Triangle4.java"
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

package fmg.core.mosaic.cells;

import java.util.ArrayList;
import java.util.List;

import fmg.common.geom.Coord;
import fmg.common.geom.PointDouble;
import fmg.common.geom.RectDouble;
import fmg.core.mosaic.shape.ShapeTriangle4;
import fmg.core.mosaic.shape.ShapeTriangle4.ComplexityMode;

/**
 * Треугольник. Вариант 4 - треугольник 30°-30°-120°
 * @see BaseCell
 **/
public class Triangle4 extends BaseCell {

    public Triangle4(ShapeTriangle4 shape, Coord coord) {
        super(shape, coord,
                   (coord.y&3)*3+(coord.x%3) // 0..11
             );
    }

    @Override
    public ShapeTriangle4 getShape() {
        return (ShapeTriangle4)super.getShape();
    }

    @Override
    public List<Coord> getCoordsNeighbor() {
        List<Coord> neighborCoord = new ArrayList<>(getShape().getNeighborNumber(getDirection()));

        // определяю координаты соседей
        switch (ShapeTriangle4.Mode) {
        case eUnrealMode:
            switch (direction) {
            case 0:
                neighborCoord.add(new Coord(coord.x-1, coord.y-2));
                neighborCoord.add(new Coord(coord.x+1, coord.y-2));
                neighborCoord.add(new Coord(coord.x-1, coord.y-1));
                neighborCoord.add(new Coord(coord.x  , coord.y-1));
                neighborCoord.add(new Coord(coord.x+1, coord.y-1));
                neighborCoord.add(new Coord(coord.x+2, coord.y-1));
                neighborCoord.add(new Coord(coord.x-2, coord.y  ));
                neighborCoord.add(new Coord(coord.x-1, coord.y  ));
                neighborCoord.add(new Coord(coord.x+1, coord.y  ));
                neighborCoord.add(new Coord(coord.x+2, coord.y  ));
                neighborCoord.add(new Coord(coord.x-3, coord.y+1));
                neighborCoord.add(new Coord(coord.x-2, coord.y+1));
                neighborCoord.add(new Coord(coord.x-1, coord.y+1));
                neighborCoord.add(new Coord(coord.x  , coord.y+1));
                neighborCoord.add(new Coord(coord.x+1, coord.y+1));
                neighborCoord.add(new Coord(coord.x-3, coord.y+2));
                neighborCoord.add(new Coord(coord.x-2, coord.y+2));
                neighborCoord.add(new Coord(coord.x-1, coord.y+2));
                neighborCoord.add(new Coord(coord.x  , coord.y+2));
                neighborCoord.add(new Coord(coord.x-2, coord.y+3));
                neighborCoord.add(new Coord(coord.x  , coord.y+3));
                break;
            case 1:
                neighborCoord.add(new Coord(coord.x-2, coord.y-2));
                neighborCoord.add(new Coord(coord.x  , coord.y-2));
                neighborCoord.add(new Coord(coord.x-2, coord.y-1));
                neighborCoord.add(new Coord(coord.x-1, coord.y-1));
                neighborCoord.add(new Coord(coord.x  , coord.y-1));
                neighborCoord.add(new Coord(coord.x+1, coord.y-1));
                neighborCoord.add(new Coord(coord.x-2, coord.y  ));
                neighborCoord.add(new Coord(coord.x-1, coord.y  ));
                neighborCoord.add(new Coord(coord.x+1, coord.y  ));
                neighborCoord.add(new Coord(coord.x+2, coord.y  ));
                neighborCoord.add(new Coord(coord.x-2, coord.y+1));
                neighborCoord.add(new Coord(coord.x-1, coord.y+1));
                neighborCoord.add(new Coord(coord.x  , coord.y+1));
                neighborCoord.add(new Coord(coord.x+1, coord.y+1));
                neighborCoord.add(new Coord(coord.x+2, coord.y+1));
                neighborCoord.add(new Coord(coord.x-1, coord.y+2));
                neighborCoord.add(new Coord(coord.x  , coord.y+2));
                neighborCoord.add(new Coord(coord.x+1, coord.y+2));
                neighborCoord.add(new Coord(coord.x+2, coord.y+2));
                neighborCoord.add(new Coord(coord.x  , coord.y+3));
                neighborCoord.add(new Coord(coord.x+2, coord.y+3));
                break;
            case 2:
                neighborCoord.add(new Coord(coord.x-3, coord.y-2));
                neighborCoord.add(new Coord(coord.x-1, coord.y-2));
                neighborCoord.add(new Coord(coord.x  , coord.y-2));
                neighborCoord.add(new Coord(coord.x+2, coord.y-2));
                neighborCoord.add(new Coord(coord.x-3, coord.y-1));
                neighborCoord.add(new Coord(coord.x-2, coord.y-1));
                neighborCoord.add(new Coord(coord.x-1, coord.y-1));
                neighborCoord.add(new Coord(coord.x  , coord.y-1));
                neighborCoord.add(new Coord(coord.x+1, coord.y-1));
                neighborCoord.add(new Coord(coord.x+2, coord.y-1));
                neighborCoord.add(new Coord(coord.x+3, coord.y-1));
                neighborCoord.add(new Coord(coord.x-3, coord.y  ));
                neighborCoord.add(new Coord(coord.x-2, coord.y  ));
                neighborCoord.add(new Coord(coord.x-1, coord.y  ));
                neighborCoord.add(new Coord(coord.x+1, coord.y  ));
                neighborCoord.add(new Coord(coord.x+2, coord.y  ));
                neighborCoord.add(new Coord(coord.x+3, coord.y  ));
                neighborCoord.add(new Coord(coord.x-3, coord.y+1));
                neighborCoord.add(new Coord(coord.x-1, coord.y+1));
                neighborCoord.add(new Coord(coord.x  , coord.y+1));
                neighborCoord.add(new Coord(coord.x+2, coord.y+1));
                break;
            case 3:
                neighborCoord.add(new Coord(coord.x-2, coord.y-1));
                neighborCoord.add(new Coord(coord.x  , coord.y-1));
                neighborCoord.add(new Coord(coord.x+1, coord.y-1));
                neighborCoord.add(new Coord(coord.x+3, coord.y-1));
                neighborCoord.add(new Coord(coord.x-3, coord.y  ));
                neighborCoord.add(new Coord(coord.x-2, coord.y  ));
                neighborCoord.add(new Coord(coord.x-1, coord.y  ));
                neighborCoord.add(new Coord(coord.x+1, coord.y  ));
                neighborCoord.add(new Coord(coord.x+2, coord.y  ));
                neighborCoord.add(new Coord(coord.x+3, coord.y  ));
                neighborCoord.add(new Coord(coord.x-3, coord.y+1));
                neighborCoord.add(new Coord(coord.x-2, coord.y+1));
                neighborCoord.add(new Coord(coord.x-1, coord.y+1));
                neighborCoord.add(new Coord(coord.x  , coord.y+1));
                neighborCoord.add(new Coord(coord.x+1, coord.y+1));
                neighborCoord.add(new Coord(coord.x+2, coord.y+1));
                neighborCoord.add(new Coord(coord.x+3, coord.y+1));
                neighborCoord.add(new Coord(coord.x-2, coord.y+2));
                neighborCoord.add(new Coord(coord.x  , coord.y+2));
                neighborCoord.add(new Coord(coord.x+1, coord.y+2));
                neighborCoord.add(new Coord(coord.x+3, coord.y+2));
                break;
            case 4:
                neighborCoord.add(new Coord(coord.x-2, coord.y-3));
                neighborCoord.add(new Coord(coord.x  , coord.y-3));
                neighborCoord.add(new Coord(coord.x-2, coord.y-2));
                neighborCoord.add(new Coord(coord.x-1, coord.y-2));
                neighborCoord.add(new Coord(coord.x  , coord.y-2));
                neighborCoord.add(new Coord(coord.x+1, coord.y-2));
                neighborCoord.add(new Coord(coord.x-2, coord.y-1));
                neighborCoord.add(new Coord(coord.x-1, coord.y-1));
                neighborCoord.add(new Coord(coord.x  , coord.y-1));
                neighborCoord.add(new Coord(coord.x+1, coord.y-1));
                neighborCoord.add(new Coord(coord.x+2, coord.y-1));
                neighborCoord.add(new Coord(coord.x-2, coord.y  ));
                neighborCoord.add(new Coord(coord.x-1, coord.y  ));
                neighborCoord.add(new Coord(coord.x+1, coord.y  ));
                neighborCoord.add(new Coord(coord.x+2, coord.y  ));
                neighborCoord.add(new Coord(coord.x-1, coord.y+1));
                neighborCoord.add(new Coord(coord.x  , coord.y+1));
                neighborCoord.add(new Coord(coord.x+1, coord.y+1));
                neighborCoord.add(new Coord(coord.x+2, coord.y+1));
                neighborCoord.add(new Coord(coord.x  , coord.y+2));
                neighborCoord.add(new Coord(coord.x+2, coord.y+2));
                break;
            case 5:
                neighborCoord.add(new Coord(coord.x  , coord.y-3));
                neighborCoord.add(new Coord(coord.x+2, coord.y-3));
                neighborCoord.add(new Coord(coord.x  , coord.y-2));
                neighborCoord.add(new Coord(coord.x+1, coord.y-2));
                neighborCoord.add(new Coord(coord.x+2, coord.y-2));
                neighborCoord.add(new Coord(coord.x+3, coord.y-2));
                neighborCoord.add(new Coord(coord.x-1, coord.y-1));
                neighborCoord.add(new Coord(coord.x  , coord.y-1));
                neighborCoord.add(new Coord(coord.x+1, coord.y-1));
                neighborCoord.add(new Coord(coord.x+2, coord.y-1));
                neighborCoord.add(new Coord(coord.x+3, coord.y-1));
                neighborCoord.add(new Coord(coord.x-2, coord.y  ));
                neighborCoord.add(new Coord(coord.x-1, coord.y  ));
                neighborCoord.add(new Coord(coord.x+1, coord.y  ));
                neighborCoord.add(new Coord(coord.x+2, coord.y  ));
                neighborCoord.add(new Coord(coord.x-2, coord.y+1));
                neighborCoord.add(new Coord(coord.x-1, coord.y+1));
                neighborCoord.add(new Coord(coord.x  , coord.y+1));
                neighborCoord.add(new Coord(coord.x+1, coord.y+1));
                neighborCoord.add(new Coord(coord.x-1, coord.y+2));
                neighborCoord.add(new Coord(coord.x+1, coord.y+2));
                break;
            case 6:
                neighborCoord.add(new Coord(coord.x-2, coord.y-2));
                neighborCoord.add(new Coord(coord.x  , coord.y-2));
                neighborCoord.add(new Coord(coord.x+1, coord.y-2));
                neighborCoord.add(new Coord(coord.x+3, coord.y-2));
                neighborCoord.add(new Coord(coord.x-3, coord.y-1));
                neighborCoord.add(new Coord(coord.x-2, coord.y-1));
                neighborCoord.add(new Coord(coord.x-1, coord.y-1));
                neighborCoord.add(new Coord(coord.x  , coord.y-1));
                neighborCoord.add(new Coord(coord.x+1, coord.y-1));
                neighborCoord.add(new Coord(coord.x+2, coord.y-1));
                neighborCoord.add(new Coord(coord.x+3, coord.y-1));
                neighborCoord.add(new Coord(coord.x-3, coord.y  ));
                neighborCoord.add(new Coord(coord.x-2, coord.y  ));
                neighborCoord.add(new Coord(coord.x-1, coord.y  ));
                neighborCoord.add(new Coord(coord.x+1, coord.y  ));
                neighborCoord.add(new Coord(coord.x+2, coord.y  ));
                neighborCoord.add(new Coord(coord.x+3, coord.y  ));
                neighborCoord.add(new Coord(coord.x-2, coord.y+1));
                neighborCoord.add(new Coord(coord.x  , coord.y+1));
                neighborCoord.add(new Coord(coord.x+1, coord.y+1));
                neighborCoord.add(new Coord(coord.x+3, coord.y+1));
                break;
            case 7:
                neighborCoord.add(new Coord(coord.x  , coord.y-2));
                neighborCoord.add(new Coord(coord.x+2, coord.y-2));
                neighborCoord.add(new Coord(coord.x-1, coord.y-1));
                neighborCoord.add(new Coord(coord.x  , coord.y-1));
                neighborCoord.add(new Coord(coord.x+1, coord.y-1));
                neighborCoord.add(new Coord(coord.x+2, coord.y-1));
                neighborCoord.add(new Coord(coord.x-2, coord.y  ));
                neighborCoord.add(new Coord(coord.x-1, coord.y  ));
                neighborCoord.add(new Coord(coord.x+1, coord.y  ));
                neighborCoord.add(new Coord(coord.x+2, coord.y  ));
                neighborCoord.add(new Coord(coord.x-2, coord.y+1));
                neighborCoord.add(new Coord(coord.x-1, coord.y+1));
                neighborCoord.add(new Coord(coord.x  , coord.y+1));
                neighborCoord.add(new Coord(coord.x+1, coord.y+1));
                neighborCoord.add(new Coord(coord.x+2, coord.y+1));
                neighborCoord.add(new Coord(coord.x-2, coord.y+2));
                neighborCoord.add(new Coord(coord.x-1, coord.y+2));
                neighborCoord.add(new Coord(coord.x  , coord.y+2));
                neighborCoord.add(new Coord(coord.x+1, coord.y+2));
                neighborCoord.add(new Coord(coord.x-2, coord.y+3));
                neighborCoord.add(new Coord(coord.x  , coord.y+3));
                break;
            case 8:
                neighborCoord.add(new Coord(coord.x-1, coord.y-2));
                neighborCoord.add(new Coord(coord.x+1, coord.y-2));
                neighborCoord.add(new Coord(coord.x-2, coord.y-1));
                neighborCoord.add(new Coord(coord.x-1, coord.y-1));
                neighborCoord.add(new Coord(coord.x  , coord.y-1));
                neighborCoord.add(new Coord(coord.x+1, coord.y-1));
                neighborCoord.add(new Coord(coord.x-2, coord.y  ));
                neighborCoord.add(new Coord(coord.x-1, coord.y  ));
                neighborCoord.add(new Coord(coord.x+1, coord.y  ));
                neighborCoord.add(new Coord(coord.x+2, coord.y  ));
                neighborCoord.add(new Coord(coord.x-1, coord.y+1));
                neighborCoord.add(new Coord(coord.x  , coord.y+1));
                neighborCoord.add(new Coord(coord.x+1, coord.y+1));
                neighborCoord.add(new Coord(coord.x+2, coord.y+1));
                neighborCoord.add(new Coord(coord.x+3, coord.y+1));
                neighborCoord.add(new Coord(coord.x  , coord.y+2));
                neighborCoord.add(new Coord(coord.x+1, coord.y+2));
                neighborCoord.add(new Coord(coord.x+2, coord.y+2));
                neighborCoord.add(new Coord(coord.x+3, coord.y+2));
                neighborCoord.add(new Coord(coord.x  , coord.y+3));
                neighborCoord.add(new Coord(coord.x+2, coord.y+3));
                break;
            case 9:
                neighborCoord.add(new Coord(coord.x-2, coord.y-3));
                neighborCoord.add(new Coord(coord.x  , coord.y-3));
                neighborCoord.add(new Coord(coord.x-3, coord.y-2));
                neighborCoord.add(new Coord(coord.x-2, coord.y-2));
                neighborCoord.add(new Coord(coord.x-1, coord.y-2));
                neighborCoord.add(new Coord(coord.x  , coord.y-2));
                neighborCoord.add(new Coord(coord.x-3, coord.y-1));
                neighborCoord.add(new Coord(coord.x-2, coord.y-1));
                neighborCoord.add(new Coord(coord.x-1, coord.y-1));
                neighborCoord.add(new Coord(coord.x  , coord.y-1));
                neighborCoord.add(new Coord(coord.x+1, coord.y-1));
                neighborCoord.add(new Coord(coord.x-2, coord.y  ));
                neighborCoord.add(new Coord(coord.x-1, coord.y  ));
                neighborCoord.add(new Coord(coord.x+1, coord.y  ));
                neighborCoord.add(new Coord(coord.x+2, coord.y  ));
                neighborCoord.add(new Coord(coord.x-1, coord.y+1));
                neighborCoord.add(new Coord(coord.x  , coord.y+1));
                neighborCoord.add(new Coord(coord.x+1, coord.y+1));
                neighborCoord.add(new Coord(coord.x+2, coord.y+1));
                neighborCoord.add(new Coord(coord.x-1, coord.y+2));
                neighborCoord.add(new Coord(coord.x+1, coord.y+2));
                break;
            case 10:
                neighborCoord.add(new Coord(coord.x  , coord.y-3));
                neighborCoord.add(new Coord(coord.x+2, coord.y-3));
                neighborCoord.add(new Coord(coord.x-1, coord.y-2));
                neighborCoord.add(new Coord(coord.x  , coord.y-2));
                neighborCoord.add(new Coord(coord.x+1, coord.y-2));
                neighborCoord.add(new Coord(coord.x+2, coord.y-2));
                neighborCoord.add(new Coord(coord.x-2, coord.y-1));
                neighborCoord.add(new Coord(coord.x-1, coord.y-1));
                neighborCoord.add(new Coord(coord.x  , coord.y-1));
                neighborCoord.add(new Coord(coord.x+1, coord.y-1));
                neighborCoord.add(new Coord(coord.x+2, coord.y-1));
                neighborCoord.add(new Coord(coord.x-2, coord.y  ));
                neighborCoord.add(new Coord(coord.x-1, coord.y  ));
                neighborCoord.add(new Coord(coord.x+1, coord.y  ));
                neighborCoord.add(new Coord(coord.x+2, coord.y  ));
                neighborCoord.add(new Coord(coord.x-2, coord.y+1));
                neighborCoord.add(new Coord(coord.x-1, coord.y+1));
                neighborCoord.add(new Coord(coord.x  , coord.y+1));
                neighborCoord.add(new Coord(coord.x+1, coord.y+1));
                neighborCoord.add(new Coord(coord.x-2, coord.y+2));
                neighborCoord.add(new Coord(coord.x  , coord.y+2));
                break;
            case 11:
                neighborCoord.add(new Coord(coord.x-3, coord.y-1));
                neighborCoord.add(new Coord(coord.x-1, coord.y-1));
                neighborCoord.add(new Coord(coord.x  , coord.y-1));
                neighborCoord.add(new Coord(coord.x+2, coord.y-1));
                neighborCoord.add(new Coord(coord.x-3, coord.y  ));
                neighborCoord.add(new Coord(coord.x-2, coord.y  ));
                neighborCoord.add(new Coord(coord.x-1, coord.y  ));
                neighborCoord.add(new Coord(coord.x+1, coord.y  ));
                neighborCoord.add(new Coord(coord.x+2, coord.y  ));
                neighborCoord.add(new Coord(coord.x+3, coord.y  ));
                neighborCoord.add(new Coord(coord.x-3, coord.y+1));
                neighborCoord.add(new Coord(coord.x-2, coord.y+1));
                neighborCoord.add(new Coord(coord.x-1, coord.y+1));
                neighborCoord.add(new Coord(coord.x  , coord.y+1));
                neighborCoord.add(new Coord(coord.x+1, coord.y+1));
                neighborCoord.add(new Coord(coord.x+2, coord.y+1));
                neighborCoord.add(new Coord(coord.x+3, coord.y+1));
                neighborCoord.add(new Coord(coord.x-3, coord.y+2));
                neighborCoord.add(new Coord(coord.x-1, coord.y+2));
                neighborCoord.add(new Coord(coord.x  , coord.y+2));
                neighborCoord.add(new Coord(coord.x+2, coord.y+2));
                break;
            }
            break;
        case eMeanMode:
            switch (direction) {
            case 0:
                neighborCoord.add(new Coord(coord.x-1, coord.y+1));
                neighborCoord.add(new Coord(coord.x  , coord.y+1));
                neighborCoord.add(new Coord(coord.x+1, coord.y+1));
                break;
            case 1:
                neighborCoord.add(new Coord(coord.x+1, coord.y  ));
                neighborCoord.add(new Coord(coord.x  , coord.y+1));
                neighborCoord.add(new Coord(coord.x+1, coord.y+1));
                neighborCoord.add(new Coord(coord.x+1, coord.y+2));
                neighborCoord.add(new Coord(coord.x  , coord.y+3));
                break;
            case 2:
                neighborCoord.add(new Coord(coord.x-3, coord.y-1));
                neighborCoord.add(new Coord(coord.x  , coord.y-1));
                neighborCoord.add(new Coord(coord.x+3, coord.y-1));
                neighborCoord.add(new Coord(coord.x-3, coord.y  ));
                neighborCoord.add(new Coord(coord.x-1, coord.y  ));
                neighborCoord.add(new Coord(coord.x+3, coord.y  ));
                neighborCoord.add(new Coord(coord.x  , coord.y+1));
                break;
            case 3:
                neighborCoord.add(new Coord(coord.x  , coord.y-1));
                neighborCoord.add(new Coord(coord.x+1, coord.y  ));
                neighborCoord.add(new Coord(coord.x  , coord.y+1));
                break;
            case 4:
                neighborCoord.add(new Coord(coord.x-1, coord.y-1));
                neighborCoord.add(new Coord(coord.x  , coord.y-1));
                neighborCoord.add(new Coord(coord.x-1, coord.y  ));
                break;
            case 5:
                neighborCoord.add(new Coord(coord.x-1, coord.y-1));
                neighborCoord.add(new Coord(coord.x  , coord.y-1));
                neighborCoord.add(new Coord(coord.x+1, coord.y-1));
                neighborCoord.add(new Coord(coord.x  , coord.y+1));
                neighborCoord.add(new Coord(coord.x-1, coord.y+2));
                break;
            case 6:
                neighborCoord.add(new Coord(coord.x  , coord.y-1));
                neighborCoord.add(new Coord(coord.x+1, coord.y  ));
                neighborCoord.add(new Coord(coord.x  , coord.y+1));
                break;
            case 7:
                neighborCoord.add(new Coord(coord.x-1, coord.y  ));
                neighborCoord.add(new Coord(coord.x-1, coord.y+1));
                neighborCoord.add(new Coord(coord.x  , coord.y+1));
                break;
            case 8:
                neighborCoord.add(new Coord(coord.x-1, coord.y-2));
                neighborCoord.add(new Coord(coord.x  , coord.y-1));
                neighborCoord.add(new Coord(coord.x-1, coord.y+1));
                neighborCoord.add(new Coord(coord.x  , coord.y+1));
                neighborCoord.add(new Coord(coord.x+1, coord.y+1));
                break;
            case 9:
                neighborCoord.add(new Coord(coord.x-1, coord.y-1));
                neighborCoord.add(new Coord(coord.x  , coord.y-1));
                neighborCoord.add(new Coord(coord.x+1, coord.y-1));
                break;
            case 10:
                neighborCoord.add(new Coord(coord.x  , coord.y-3));
                neighborCoord.add(new Coord(coord.x+1, coord.y-2));
                neighborCoord.add(new Coord(coord.x  , coord.y-1));
                neighborCoord.add(new Coord(coord.x+1, coord.y-1));
                neighborCoord.add(new Coord(coord.x+1, coord.y  ));
                break;
            case 11:
                neighborCoord.add(new Coord(coord.x  , coord.y-1));
                neighborCoord.add(new Coord(coord.x-3, coord.y  ));
                neighborCoord.add(new Coord(coord.x-1, coord.y  ));
                neighborCoord.add(new Coord(coord.x+3, coord.y  ));
                neighborCoord.add(new Coord(coord.x-3, coord.y+1));
                neighborCoord.add(new Coord(coord.x  , coord.y+1));
                neighborCoord.add(new Coord(coord.x+3, coord.y+1));
                break;
            }
            break;
        case eOptimalMode:
            switch (direction) {
            case 0:
                neighborCoord.add(new Coord(coord.x+1, coord.y-2));
                neighborCoord.add(new Coord(coord.x+1, coord.y-1));
                neighborCoord.add(new Coord(coord.x+2, coord.y-1));
                neighborCoord.add(new Coord(coord.x-1, coord.y  ));
                neighborCoord.add(new Coord(coord.x-1, coord.y+1));
                neighborCoord.add(new Coord(coord.x  , coord.y+1));
                neighborCoord.add(new Coord(coord.x+1, coord.y+1));
                break;
            case 1:
                neighborCoord.add(new Coord(coord.x+1, coord.y  ));
                neighborCoord.add(new Coord(coord.x-1, coord.y+1));
                neighborCoord.add(new Coord(coord.x  , coord.y+1));
                neighborCoord.add(new Coord(coord.x+1, coord.y+1));
                neighborCoord.add(new Coord(coord.x+1, coord.y+2));
                neighborCoord.add(new Coord(coord.x+2, coord.y+2));
                neighborCoord.add(new Coord(coord.x+2, coord.y+3));
                break;
            case 2:
                neighborCoord.add(new Coord(coord.x+2, coord.y-2));
                neighborCoord.add(new Coord(coord.x  , coord.y-1));
                neighborCoord.add(new Coord(coord.x+2, coord.y-1));
                neighborCoord.add(new Coord(coord.x+3, coord.y-1));
                neighborCoord.add(new Coord(coord.x-1, coord.y  ));
                neighborCoord.add(new Coord(coord.x+1, coord.y  ));
                neighborCoord.add(new Coord(coord.x  , coord.y+1));
                break;
            case 3:
                neighborCoord.add(new Coord(coord.x  , coord.y-1));
                neighborCoord.add(new Coord(coord.x+1, coord.y-1));
                neighborCoord.add(new Coord(coord.x+1, coord.y  ));
                neighborCoord.add(new Coord(coord.x  , coord.y+1));
                neighborCoord.add(new Coord(coord.x+2, coord.y+1));
                neighborCoord.add(new Coord(coord.x+3, coord.y+1));
                neighborCoord.add(new Coord(coord.x+3, coord.y+2));
                break;
            case 4:
                neighborCoord.add(new Coord(coord.x-1, coord.y-1));
                neighborCoord.add(new Coord(coord.x  , coord.y-1));
                neighborCoord.add(new Coord(coord.x-1, coord.y  ));
                neighborCoord.add(new Coord(coord.x+1, coord.y+1));
                neighborCoord.add(new Coord(coord.x+2, coord.y+1));
                neighborCoord.add(new Coord(coord.x+2, coord.y+2));
                break;
            case 5:
                neighborCoord.add(new Coord(coord.x+2, coord.y-3));
                neighborCoord.add(new Coord(coord.x+2, coord.y-2));
                neighborCoord.add(new Coord(coord.x+3, coord.y-2));
                neighborCoord.add(new Coord(coord.x-1, coord.y-1));
                neighborCoord.add(new Coord(coord.x  , coord.y-1));
                neighborCoord.add(new Coord(coord.x+1, coord.y-1));
                break;
            case 6:
                neighborCoord.add(new Coord(coord.x-2, coord.y-2));
                neighborCoord.add(new Coord(coord.x-3, coord.y-1));
                neighborCoord.add(new Coord(coord.x-2, coord.y-1));
                neighborCoord.add(new Coord(coord.x  , coord.y-1));
                neighborCoord.add(new Coord(coord.x-1, coord.y  ));
                neighborCoord.add(new Coord(coord.x+1, coord.y  ));
                neighborCoord.add(new Coord(coord.x  , coord.y+1));
                break;
            case 7:
                neighborCoord.add(new Coord(coord.x-1, coord.y  ));
                neighborCoord.add(new Coord(coord.x-1, coord.y+1));
                neighborCoord.add(new Coord(coord.x  , coord.y+1));
                neighborCoord.add(new Coord(coord.x+1, coord.y+1));
                neighborCoord.add(new Coord(coord.x-2, coord.y+2));
                neighborCoord.add(new Coord(coord.x-1, coord.y+2));
                neighborCoord.add(new Coord(coord.x-2, coord.y+3));
                break;
            case 8:
                neighborCoord.add(new Coord(coord.x-1, coord.y-2));
                neighborCoord.add(new Coord(coord.x-2, coord.y-1));
                neighborCoord.add(new Coord(coord.x-1, coord.y-1));
                neighborCoord.add(new Coord(coord.x+1, coord.y  ));
                neighborCoord.add(new Coord(coord.x-1, coord.y+1));
                neighborCoord.add(new Coord(coord.x  , coord.y+1));
                neighborCoord.add(new Coord(coord.x+1, coord.y+1));
                break;
            case 9:
                neighborCoord.add(new Coord(coord.x-2, coord.y-3));
                neighborCoord.add(new Coord(coord.x-3, coord.y-2));
                neighborCoord.add(new Coord(coord.x-2, coord.y-2));
                neighborCoord.add(new Coord(coord.x-1, coord.y-1));
                neighborCoord.add(new Coord(coord.x  , coord.y-1));
                neighborCoord.add(new Coord(coord.x+1, coord.y-1));
                break;
            case 10:
                neighborCoord.add(new Coord(coord.x  , coord.y-1));
                neighborCoord.add(new Coord(coord.x+1, coord.y-1));
                neighborCoord.add(new Coord(coord.x+1, coord.y  ));
                neighborCoord.add(new Coord(coord.x-2, coord.y+1));
                neighborCoord.add(new Coord(coord.x-1, coord.y+1));
                neighborCoord.add(new Coord(coord.x-2, coord.y+2));
                break;
            case 11:
                neighborCoord.add(new Coord(coord.x-1, coord.y-1));
                neighborCoord.add(new Coord(coord.x  , coord.y-1));
                neighborCoord.add(new Coord(coord.x-1, coord.y  ));
                neighborCoord.add(new Coord(coord.x-3, coord.y+1));
                neighborCoord.add(new Coord(coord.x-2, coord.y+1));
                neighborCoord.add(new Coord(coord.x  , coord.y+1));
                neighborCoord.add(new Coord(coord.x-3, coord.y+2));
                break;
            }
            break;
        case eSimpeMode:
            switch (direction) {
            case 0:
                neighborCoord.add(new Coord(coord.x-1, coord.y+1));
                neighborCoord.add(new Coord(coord.x  , coord.y+1));
                neighborCoord.add(new Coord(coord.x+1, coord.y+1));
                break;
            case 1:
                neighborCoord.add(new Coord(coord.x+1, coord.y  ));
                neighborCoord.add(new Coord(coord.x  , coord.y+1));
                neighborCoord.add(new Coord(coord.x+1, coord.y+1));
                break;
            case 2:
                neighborCoord.add(new Coord(coord.x  , coord.y-1));
                neighborCoord.add(new Coord(coord.x-1, coord.y  ));
                neighborCoord.add(new Coord(coord.x  , coord.y+1));
                break;
            case 3:
                neighborCoord.add(new Coord(coord.x  , coord.y-1));
                neighborCoord.add(new Coord(coord.x+1, coord.y  ));
                neighborCoord.add(new Coord(coord.x  , coord.y+1));
                break;
            case 4:
                neighborCoord.add(new Coord(coord.x-1, coord.y-1));
                neighborCoord.add(new Coord(coord.x  , coord.y-1));
                neighborCoord.add(new Coord(coord.x-1, coord.y  ));
                break;
            case 5:
                neighborCoord.add(new Coord(coord.x-1, coord.y-1));
                neighborCoord.add(new Coord(coord.x  , coord.y-1));
                neighborCoord.add(new Coord(coord.x+1, coord.y-1));
                break;
            case 6:
                neighborCoord.add(new Coord(coord.x  , coord.y-1));
                neighborCoord.add(new Coord(coord.x+1, coord.y  ));
                neighborCoord.add(new Coord(coord.x  , coord.y+1));
                break;
            case 7:
                neighborCoord.add(new Coord(coord.x-1, coord.y  ));
                neighborCoord.add(new Coord(coord.x-1, coord.y+1));
                neighborCoord.add(new Coord(coord.x  , coord.y+1));
                break;
            case 8:
                neighborCoord.add(new Coord(coord.x-1, coord.y+1));
                neighborCoord.add(new Coord(coord.x  , coord.y+1));
                neighborCoord.add(new Coord(coord.x+1, coord.y+1));
                break;
            case 9:
                neighborCoord.add(new Coord(coord.x-1, coord.y-1));
                neighborCoord.add(new Coord(coord.x  , coord.y-1));
                neighborCoord.add(new Coord(coord.x+1, coord.y-1));
                break;
            case 10:
                neighborCoord.add(new Coord(coord.x  , coord.y-1));
                neighborCoord.add(new Coord(coord.x+1, coord.y-1));
                neighborCoord.add(new Coord(coord.x+1, coord.y  ));
                break;
            case 11:
                neighborCoord.add(new Coord(coord.x  , coord.y-1));
                neighborCoord.add(new Coord(coord.x-1, coord.y  ));
                neighborCoord.add(new Coord(coord.x  , coord.y+1));
                break;
            }
            break;
        }

        return neighborCoord;
    }

    @Override
    protected void calcRegion() {
        ShapeTriangle4 shape = getShape();
        double a = shape.getA();
        double b = shape.getB();
        double R = shape.getROut();
        double r = shape.getRIn();
        double s = (ShapeTriangle4.Mode != ComplexityMode.eUnrealMode) ? shape.getSnip() : 0;
        double c = (ShapeTriangle4.Mode != ComplexityMode.eUnrealMode) ? s/2*SQRT3 : 0; // s * cos30
        double u = (ShapeTriangle4.Mode != ComplexityMode.eUnrealMode) ? s/2 : 0; // s * cos60

        // определение координат точек фигуры
        double oX =  (coord.x/3)*a + b;      // offset X
        double oY = ((coord.y/4)*2+1)*(R+r); // offset Y
        switch (ShapeTriangle4.Mode) {
        case eUnrealMode:
        case eOptimalMode:
            break;
        case eMeanMode:
        case eSimpeMode:
            oX -= u;
            break;
        }

        switch (ShapeTriangle4.Mode) {
        case eUnrealMode:
            switch (direction) {
            case 0:
                region.setPoint(0, oX    , oY - R-r);
                region.setPoint(1, oX    , oY - r  );
                region.setPoint(2, oX - b, oY      );
                break;
            case 1:
                region.setPoint(0, oX + b, oY - R  );
                region.setPoint(1, oX + b, oY      );
                region.setPoint(2, oX    , oY - R-r);
                break;
            case 2:
                region.setPoint(0, oX + a, oY - R-r);
                region.setPoint(1, oX + b, oY - R  );
                region.setPoint(2, oX    , oY - R-r);
                break;
            case 3:
                region.setPoint(0, oX + b, oY      );
                region.setPoint(1, oX - b, oY      );
                region.setPoint(2, oX    , oY - r  );
                break;
            case 4:
                region.setPoint(0, oX    , oY - R-r);
                region.setPoint(1, oX + b, oY      );
                region.setPoint(2, oX    , oY - r  );
                break;
            case 5:
                region.setPoint(0, oX + a, oY - R-r);
                region.setPoint(1, oX + b, oY      );
                region.setPoint(2, oX + b, oY - R  );
                break;
            case 6:
                region.setPoint(0, oX + b, oY      );
                region.setPoint(1, oX    , oY + r  );
                region.setPoint(2, oX - b, oY      );
                break;
            case 7:
                region.setPoint(0, oX + b, oY      );
                region.setPoint(1, oX    , oY + R+r);
                region.setPoint(2, oX    , oY + r  );
                break;
            case 8:
                region.setPoint(0, oX + b, oY      );
                region.setPoint(1, oX + a, oY + R+r);
                region.setPoint(2, oX + b, oY + R  );
                break;
            case 9:
                region.setPoint(0, oX    , oY + r  );
                region.setPoint(1, oX    , oY + R+r);
                region.setPoint(2, oX - b, oY      );
                break;
            case 10:
                region.setPoint(0, oX + b, oY      );
                region.setPoint(1, oX + b, oY + R  );
                region.setPoint(2, oX    , oY + R+r);
                break;
            case 11:
                region.setPoint(0, oX + a, oY + R+r);
                region.setPoint(1, oX    , oY + R+r);
                region.setPoint(2, oX + b, oY + R  );
                break;
            }
            break;
        case eMeanMode:
            switch (direction) {
            case 0:
                region.setPoint(0, oX    , oY-R-r+s);
                region.setPoint(1, oX    , oY - r  );
                region.setPoint(2, oX-b+c, oY - u  );
                region.setPoint(3, oX-b+u, oY - c  );
                region.setPoint(4, oX - u, oY-R-r+c);
                break;
            case 1:
                region.setPoint(0, oX + b, oY - R  );
                region.setPoint(1, oX + b, oY      );
                region.setPoint(2, oX + u, oY-R-r+c);
                region.setPoint(3, oX + c, oY-R-r+u);
                break;
            case 2:
                region.setPoint(0, oX + a, oY - R-r);
                region.setPoint(1, oX + b, oY - R  );
                region.setPoint(2, oX    , oY - R-r);
                break;
            case 3:
                region.setPoint(0, oX+b-s, oY      );
                region.setPoint(1, oX-b+s, oY      );
                region.setPoint(2, oX-b+c, oY - u  );
                region.setPoint(3, oX    , oY - r  );
                region.setPoint(4, oX+b-c, oY - u  );
                break;
            case 4:
                region.setPoint(0, oX + u, oY-R-r+c);
                region.setPoint(1, oX+b-u, oY - c  );
                region.setPoint(2, oX+b-c, oY - u  );
                region.setPoint(3, oX    , oY - r  );
                region.setPoint(4, oX    , oY-R-r+s);
                break;
            case 5:
                region.setPoint(0, oX+a-u, oY-R-r+c);
                region.setPoint(1, oX + b, oY      );
                region.setPoint(2, oX + b, oY - R  );
                region.setPoint(3, oX+a-c, oY-R-r+u);
                break;
            case 6:
                region.setPoint(0, oX+b-c, oY + u  );
                region.setPoint(1, oX    , oY + r  );
                region.setPoint(2, oX-b+c, oY + u  );
                region.setPoint(3, oX-b+s, oY      );
                region.setPoint(4, oX+b-s, oY      );
                break;
            case 7:
                region.setPoint(0, oX+b-u, oY + c  );
                region.setPoint(1, oX + u, oY+R+r-c);
                region.setPoint(2, oX    , oY+R+r-s);
                region.setPoint(3, oX    , oY + r  );
                region.setPoint(4, oX+b-c, oY + u  );
                break;
            case 8:
                region.setPoint(0, oX + b, oY      );
                region.setPoint(1, oX+a-u, oY+R+r-c);
                region.setPoint(2, oX+a-c, oY+R+r-u);
                region.setPoint(3, oX + b, oY + R  );
                break;
            case 9:
                region.setPoint(0, oX    , oY + r  );
                region.setPoint(1, oX    , oY+R+r-s);
                region.setPoint(2, oX - u, oY+R+r-c);
                region.setPoint(3, oX-b+u, oY + c  );
                region.setPoint(4, oX-b+c, oY + u  );
                break;
            case 10:
                region.setPoint(0, oX + b, oY      );
                region.setPoint(1, oX + b, oY + R  );
                region.setPoint(2, oX + c, oY+R+r-u);
                region.setPoint(3, oX + u, oY+R+r-c);
                break;
            case 11:
                region.setPoint(0, oX + a, oY + R+r);
                region.setPoint(2, oX + b, oY + R  );
                region.setPoint(1, oX    , oY + R+r);
                break;
            }
            break;
        case eOptimalMode:
            switch (direction) {
            case 0:
                region.setPoint(0, oX    , oY - R-r);
                region.setPoint(1, oX    , oY - r  );
                region.setPoint(2, oX-b+c, oY - u  );
                region.setPoint(3, oX-b+u, oY - c  );
                break;
            case 1:
                region.setPoint(0, oX + b, oY - R  );
                region.setPoint(1, oX + b, oY      );
                region.setPoint(2, oX + u, oY-R-r+c);
                region.setPoint(3, oX + c, oY-R-r+u);
                break;
            case 2:
                region.setPoint(0, oX + a, oY -R -r);
                region.setPoint(1, oX + b, oY - R  );
                region.setPoint(2, oX + c, oY-R-r+u);
                region.setPoint(3, oX + s, oY -R -r);
                break;
            case 3:
                region.setPoint(0, oX+b  , oY      );
                region.setPoint(1, oX-b+s, oY      );
                region.setPoint(2, oX-b+c, oY - u  );
                region.setPoint(3, oX    , oY - r  );
                break;
            case 4:
                region.setPoint(0, oX + u, oY-R-r+c);
                region.setPoint(1, oX + b, oY      );
                region.setPoint(2, oX    , oY - r  );
                region.setPoint(3, oX    , oY-R-r+s);
                break;
            case 5:
                region.setPoint(0, oX+a  , oY - R-r);
                region.setPoint(1, oX+b+u, oY - c  );
                region.setPoint(2, oX + b, oY - s  );
                region.setPoint(3, oX + b, oY - R  );
                break;
            case 6:
                region.setPoint(0, oX+b-c, oY + u  );
                region.setPoint(1, oX    , oY + r  );
                region.setPoint(2, oX-b  , oY      );
                region.setPoint(3, oX+b-s, oY      );
                break;
            case 7:
                region.setPoint(0, oX+b-u, oY + c  );
                region.setPoint(1, oX    , oY + R+r);
                region.setPoint(2, oX    , oY + r  );
                region.setPoint(3, oX+b-c, oY + u  );
                break;
            case 8:
                region.setPoint(0, oX + b, oY      );
                region.setPoint(1, oX+a-u, oY+R+r-c);
                region.setPoint(2, oX+a-c, oY+R+r-u);
                region.setPoint(3, oX + b, oY + R  );
                break;
            case 9:
                region.setPoint(0, oX    , oY + r  );
                region.setPoint(1, oX    , oY+R+r-s);
                region.setPoint(2, oX - u, oY+R+r-c);
                region.setPoint(3, oX - b, oY      );
                break;
            case 10:
                region.setPoint(0, oX + b, oY + s  );
                region.setPoint(1, oX + b, oY + R  );
                region.setPoint(2, oX    , oY+R+r  );
                region.setPoint(3, oX+b-u, oY + c  );
                break;
            case 11:
                region.setPoint(0, oX+a-c, oY+R+r-u);
                region.setPoint(1, oX+a-s, oY + R+r);
                region.setPoint(2, oX    , oY + R+r);
                region.setPoint(3, oX + b, oY + R  );
                break;
            }
            break;
        case eSimpeMode:
            switch (direction) {
            case 0:
                region.setPoint(0, oX    , oY-R-r+s);
                region.setPoint(1, oX    , oY - r  );
                region.setPoint(2, oX-b+c, oY - u  );
                region.setPoint(3, oX-b+u, oY - c  );
                region.setPoint(4, oX - u, oY-R-r+c);
                break;
            case 1:
                region.setPoint(0, oX + b, oY - R  );
                region.setPoint(1, oX + b, oY - s  );
                region.setPoint(2, oX+b-u, oY - c  );
                region.setPoint(3, oX + u, oY-R-r+c);
                region.setPoint(4, oX + c, oY-R-r+u);
                break;
            case 2:
                region.setPoint(0, oX+a-c, oY-R-r+u);
                region.setPoint(1, oX + b, oY - R  );
                region.setPoint(2, oX + c, oY-R-r+u);
                region.setPoint(3, oX + s, oY -R -r);
                region.setPoint(4, oX+a-s, oY -R -r);
                break;
            case 3:
                region.setPoint(0, oX+b-s, oY      );
                region.setPoint(1, oX-b+s, oY      );
                region.setPoint(2, oX-b+c, oY - u  );
                region.setPoint(3, oX    , oY - r  );
                region.setPoint(4, oX+b-c, oY - u  );
                break;
            case 4:
                region.setPoint(0, oX + u, oY-R-r+c);
                region.setPoint(1, oX+b-u, oY - c  );
                region.setPoint(2, oX+b-c, oY - u  );
                region.setPoint(3, oX    , oY - r  );
                region.setPoint(4, oX    , oY-R-r+s);
                break;
            case 5:
                region.setPoint(0, oX+a-u, oY-R-r+c);
                region.setPoint(1, oX+b+u, oY - c  );
                region.setPoint(2, oX + b, oY - s  );
                region.setPoint(3, oX + b, oY - R  );
                region.setPoint(4, oX+a-c, oY-R-r+u);
                break;
            case 6:
                region.setPoint(0, oX+b-c, oY + u  );
                region.setPoint(1, oX    , oY + r  );
                region.setPoint(2, oX-b+c, oY + u  );
                region.setPoint(3, oX-b+s, oY      );
                region.setPoint(4, oX+b-s, oY      );
                break;
            case 7:
                region.setPoint(0, oX+b-u, oY + c  );
                region.setPoint(1, oX + u, oY+R+r-c);
                region.setPoint(2, oX    , oY+R+r-s);
                region.setPoint(3, oX    , oY + r  );
                region.setPoint(4, oX+b-c, oY + u  );
                break;
            case 8:
                region.setPoint(0, oX+b+u, oY + c  );
                region.setPoint(1, oX+a-u, oY+R+r-c);
                region.setPoint(2, oX+a-c, oY+R+r-u);
                region.setPoint(3, oX + b, oY + R  );
                region.setPoint(4, oX + b, oY + s  );
                break;
            case 9:
                region.setPoint(0, oX    , oY + r  );
                region.setPoint(1, oX    , oY+R+r-s);
                region.setPoint(2, oX - u, oY+R+r-c);
                region.setPoint(3, oX-b+u, oY + c  );
                region.setPoint(4, oX-b+c, oY + u  );
                break;
            case 10:
                region.setPoint(0, oX + b, oY + s  );
                region.setPoint(1, oX + b, oY + R  );
                region.setPoint(2, oX + c, oY+R+r-u);
                region.setPoint(3, oX + u, oY+R+r-c);
                region.setPoint(4, oX+b-u, oY + c  );
                break;
            case 11:
                region.setPoint(0, oX+a-s, oY + R+r);
                region.setPoint(1, oX + s, oY + R+r);
                region.setPoint(2, oX + c, oY+R+r-u);
                region.setPoint(3, oX + b, oY + R  );
                region.setPoint(4, oX+a-c, oY+R+r-u);
                break;
            }
            break;
        }
    }

    @Override
    public RectDouble getRcInner(double borderWidth) {
        ShapeTriangle4 shape = getShape();
        double w = borderWidth/2.;
        double sq    = shape.getSq(borderWidth);
        double sq2   = sq/2;
        double sq2w  = sq2+w;
        double sq2w3 = sq2+w/SQRT3;

        PointDouble center = new PointDouble(); // координата центра квадрата
        switch (direction) {
        case 0: case 10:
            center.x = region.getPoint(1).x - sq2w;
            center.y = region.getPoint(1).y - sq2w3;
            break;
        case 1: case 9:
            center.x = region.getPoint(0).x - sq2w;
            center.y = region.getPoint(0).y + sq2w3;
            break;
        case 2: case 6:
            center.x = region.getPoint(1).x;
            switch (ShapeTriangle4.Mode) {
            case eUnrealMode : center.y = region.getPoint(                 0    ).y + sq2w; break;
            case eMeanMode   : center.y = region.getPoint((direction==2) ? 0 : 4).y + sq2w; break;
            case eOptimalMode: center.y = region.getPoint(                   3  ).y + sq2w; break;
            case eSimpeMode  : center.y = region.getPoint(                     4).y + sq2w; break;
            }
            break;
        case 3: case 11:
            switch (ShapeTriangle4.Mode) {
            case eUnrealMode : center.x = region.getPoint(                     2).x; break;
            case eMeanMode   : center.x = region.getPoint((direction==3) ? 3 : 2).x; break;
            case eOptimalMode: center.x = region.getPoint(                 3    ).x; break;
            case eSimpeMode  : center.x = region.getPoint(                 3    ).x; break;
            }
            center.y = region.getPoint(1).y - sq2w;
            break;
        case 4: case 8:
            switch (ShapeTriangle4.Mode) {
            case eUnrealMode:
                center.x = region.getPoint(2).x + sq2w;
                center.y = region.getPoint(2).y - sq2w3;
                break;
            case eOptimalMode:
                center.x = region.getPoint(3).x + sq2w;
                center.y = region.getPoint((direction==4) ? 2 : 3).y - sq2w3;
                break;
            case eMeanMode  :
            case eSimpeMode :
                center.x = region.getPoint(3).x + sq2w;
                center.y = region.getPoint(3).y - sq2w3;
                break;
            }
            break;
        case 5: case 7:
            switch (ShapeTriangle4.Mode) {
            case eUnrealMode : center.x = region.getPoint(                 2    ).x + sq2w;
                               center.y = region.getPoint(                 2    ).y + sq2w3; break;
            case eMeanMode   : center.x = region.getPoint((direction==5) ? 2 : 3).x + sq2w;
                               center.y = region.getPoint((direction==5) ? 2 : 3).y + sq2w3; break;
            case eOptimalMode: center.x = region.getPoint((direction!=5) ? 2 : 3).x + sq2w;
                               center.y = region.getPoint((direction!=5) ? 2 : 3).y + sq2w3; break;
            case eSimpeMode  : center.x = region.getPoint(                     3).x + sq2w;
                               center.y = region.getPoint(                     3).y + sq2w3; break;
            }
            break;
        }

        return new RectDouble(
            center.x - sq2,
            center.y - sq2,
            sq, sq);
    }

    @Override
    public int getShiftPointBorderIndex() {
        switch (ShapeTriangle4.Mode) {
        case eUnrealMode:
            switch (direction) {
            case 1: case 3: case 5: case 7: case 9: case 11: return 1;
            default: return 2;
            }
        case eMeanMode:
            switch (direction) {
            case 1: case 3: case 5: case 7: case 9: case 11: return 1;
            case 4: case 8: return 3;
            default: return 2;
            }
        case eOptimalMode:
            switch (direction) {
            case 1: case 3: case 5: case 7: return 1;
            case 8: return 3;
            default: return 2;
            }
        case eSimpeMode:
            switch (direction) {
            case 1: case 2: case 3: case 5: case 7: case 9: case 11: return 2;
            default: return 3;
            }
        default: throw new RuntimeException("Unknown Mode==" + ShapeTriangle4.Mode);
        }
    }

}
