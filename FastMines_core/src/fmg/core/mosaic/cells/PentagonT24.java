////////////////////////////////////////////////////////////////////////////////
//                               FastMines project
// file name: "PentagonT24.java"
//
// Реализация класса PentagonT24 - равносторонний 5-ти угольник, тип №2 и №4
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
import fmg.common.geom.RectDouble;
import fmg.core.mosaic.shape.BaseShape;
import fmg.core.mosaic.shape.ShapePentagonT24;

/**
 * Пятиугольник. Тип №2 и №4 - равносторонний
 * @see BaseCell
 **/
public class PentagonT24 extends BaseCell {

    public PentagonT24(ShapePentagonT24 shape, Coord coord) {
        super(shape, coord,
                  ((coord.y&1)<<1) + (coord.x&1) // 0..3
             );
    }

    @Override
    public ShapePentagonT24 getShape() {
        return (ShapePentagonT24)super.getShape();
    }

    @Override
    public List<Coord> getCoordsNeighbor() {
        List<Coord> neighborCoord = new ArrayList<>(getShape().getNeighborNumber(getDirection()));

        // определяю координаты соседей
        switch (direction) {
        case 0:
            neighborCoord.add(new Coord(coord.x-1, coord.y-1));
            neighborCoord.add(new Coord(coord.x  , coord.y-1));
            neighborCoord.add(new Coord(coord.x-1, coord.y  ));
            neighborCoord.add(new Coord(coord.x+1, coord.y  ));
            neighborCoord.add(new Coord(coord.x-1, coord.y+1));
            neighborCoord.add(new Coord(coord.x  , coord.y+1));
            neighborCoord.add(new Coord(coord.x+1, coord.y+1));
            break;
        case 1:
            neighborCoord.add(new Coord(coord.x-1, coord.y-1));
            neighborCoord.add(new Coord(coord.x  , coord.y-1));
            neighborCoord.add(new Coord(coord.x+1, coord.y-1));
            neighborCoord.add(new Coord(coord.x-1, coord.y  ));
            neighborCoord.add(new Coord(coord.x+1, coord.y  ));
            neighborCoord.add(new Coord(coord.x-1, coord.y+1));
            neighborCoord.add(new Coord(coord.x  , coord.y+1));
            break;
        case 2:
            neighborCoord.add(new Coord(coord.x  , coord.y-1));
            neighborCoord.add(new Coord(coord.x+1, coord.y-1));
            neighborCoord.add(new Coord(coord.x-1, coord.y  ));
            neighborCoord.add(new Coord(coord.x+1, coord.y  ));
            neighborCoord.add(new Coord(coord.x-1, coord.y+1));
            neighborCoord.add(new Coord(coord.x  , coord.y+1));
            neighborCoord.add(new Coord(coord.x+1, coord.y+1));
            break;
        case 3:
            neighborCoord.add(new Coord(coord.x-1, coord.y-1));
            neighborCoord.add(new Coord(coord.x  , coord.y-1));
            neighborCoord.add(new Coord(coord.x+1, coord.y-1));
            neighborCoord.add(new Coord(coord.x-1, coord.y  ));
            neighborCoord.add(new Coord(coord.x+1, coord.y  ));
            neighborCoord.add(new Coord(coord.x  , coord.y+1));
            neighborCoord.add(new Coord(coord.x+1, coord.y+1));
            break;
        }

        return neighborCoord;
    }

    @Override
    protected void calcRegion() {
        ShapePentagonT24 shape = getShape();
        double a = shape.getA();
        double b = shape.getB();
        double c = shape.getC();

        // определение координат точек фигуры
        double oX = a*((coord.x>>1)<<1); // offset X
        double oY = a*((coord.y>>1)<<1); // offset Y
        switch (direction) {
        case 0:
            region.setPoint(0, oX +       a, oY + b      );
            region.setPoint(1, oX + c +   a, oY + c +   a);
            region.setPoint(2, oX + b      , oY + b +   a);
            region.setPoint(3, oX          , oY +       a);
            region.setPoint(4, oX + c      , oY + c      );
            break;
        case 1:
            region.setPoint(0, oX + c + 2*a, oY + c      );
            region.setPoint(1, oX +     2*a, oY +       a);
            region.setPoint(2, oX + c +   a, oY + c +   a);
            region.setPoint(3, oX +       a, oY + b      );
            region.setPoint(4, oX + b +   a, oY          );
            break;
        case 2:
            region.setPoint(0, oX + c +   a, oY + c +   a);
            region.setPoint(1, oX + b +   a, oY +     2*a);
            region.setPoint(2, oX +       a, oY + b + 2*a);
            region.setPoint(3, oX + c      , oY + c + 2*a);
            region.setPoint(4, oX + b      , oY + b +   a);
            break;
        case 3:
            region.setPoint(0, oX +     2*a, oY +       a);
            region.setPoint(1, oX + b + 2*a, oY + b +   a);
            region.setPoint(2, oX + c + 2*a, oY + c + 2*a);
            region.setPoint(3, oX + b +   a, oY +     2*a);
            region.setPoint(4, oX + c +   a, oY + c +   a);
            break;
        }
    }

    @Override
    public RectDouble getRcInner(double borderWidth) {
        ShapePentagonT24 shape = getShape();
        double sq = shape.getSq(borderWidth);
        double w = borderWidth/2.;
        double w2 = w/BaseShape.SQRT2;

        RectDouble square = new RectDouble();
        switch (direction) {
        case 0:
            square.x = region.getPoint(4).x+w2;
            square.y = region.getPoint(1).y-w2 - sq;
            break;
        case 1:
            square.x = region.getPoint(2).x+w2;
            square.y = region.getPoint(0).y+w2;
            break;
        case 2:
            square.x = region.getPoint(0).x-w2 - sq;
            square.y = region.getPoint(3).y-w2 - sq;
            break;
        case 3:
            square.x = region.getPoint(2).x-w2 - sq;
            square.y = region.getPoint(4).y+w2;
            break;
        }
        square.width = sq;
        square.height = sq;
        return square;
    }

    @Override
    public int getShiftPointBorderIndex() {
        switch (direction) {
        case 0: case 1:
            return 2;
        }
        return 3;
    }

}
