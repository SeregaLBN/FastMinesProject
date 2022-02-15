////////////////////////////////////////////////////////////////////////////////
//                               FastMines project
// file name: "Triangle1.java"
//
// Реализация класса Triangle1 - равносторонний треугольник (вариант поля №1)
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
import fmg.core.mosaic.shape.ShapeTriangle1;

/**
 * Треугольник. Вариант 1 - равносторонний, классика
 * @see BaseCell
 **/
public class Triangle1 extends BaseCell {

    public Triangle1(ShapeTriangle1 shape, Coord coord) {
        super(shape, coord,
                  ((coord.y&1)<<1)+(coord.x&1) // 0..3
             );
    }

    @Override
    public ShapeTriangle1 getShape() {
        return (ShapeTriangle1)super.getShape();
    }

    @Override
    public List<Coord> getCoordsNeighbor() {
        List<Coord> neighborCoord = new ArrayList<>(getShape().getNeighborNumber(getDirection()));

        // определяю координаты соседей
        switch (direction) {
        case 0: case 3:
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
            break;
        case 1: case 2:
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
            break;
        }

        return neighborCoord;
    }

    @Override
    protected void calcRegion() {
        ShapeTriangle1 shape = getShape();
        double a = shape.getA();
        double b = shape.getB();
        double h = shape.getH();

        double oX = a*(coord.x>>1); // offset X
        double oY = h* coord.y;     // offset Y

        switch (direction) {
        case 0:
            region.setPoint(0, oX +   b, oY    );
            region.setPoint(1, oX + a  , oY + h);
            region.setPoint(2, oX      , oY + h);
            break;
        case 1:
            region.setPoint(0, oX + a+b, oY    );
            region.setPoint(1, oX + a  , oY + h);
            region.setPoint(2, oX +   b, oY    );
            break;
        case 2:
            region.setPoint(0, oX + a  , oY    );
            region.setPoint(1, oX +   b, oY + h);
            region.setPoint(2, oX      , oY    );
            break;
        case 3:
            region.setPoint(0, oX + a  , oY    );
            region.setPoint(1, oX + a+b, oY + h);
            region.setPoint(2, oX +   b, oY + h);
            break;
        }
    }

    @Override
    public RectDouble getRcInner(double borderWidth) {
        ShapeTriangle1 shape = getShape();
        double b = shape.getB();
        double sq = shape.getSq(borderWidth);
        double w = borderWidth/2.;

        PointDouble center = new PointDouble(); // координата вписанного в фигуру квадрата (не совпадает с центром фигуры)
        switch (direction) {
        case 0: case 3:
            center.x = region.getPoint(2).x + b;
            center.y = region.getPoint(2).y - sq/2 - w;
            break;
        case 1: case 2:
            center.x = region.getPoint(2).x + b;
            center.y = region.getPoint(2).y + sq/2 + w;
            break;
        }

        return new RectDouble(
            center.x - sq/2,
            center.y - sq/2,
            sq, sq);
    }

    @Override
    public int getShiftPointBorderIndex() {
        switch (direction) {
        case 0: case 3: return 2;
        }
        return 1;
    }

}
