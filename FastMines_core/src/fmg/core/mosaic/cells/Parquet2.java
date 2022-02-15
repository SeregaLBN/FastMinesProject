////////////////////////////////////////////////////////////////////////////////
//                               FastMines project
// file name: "Parquet2.java"
//
// Реализация класса Parquet2 - ещё один паркет
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
import fmg.core.mosaic.shape.ShapeParquet2;

/**
 * Паркет. Вариант №2
 * @see BaseCell
 **/
public class Parquet2 extends BaseCell {

    public Parquet2(ShapeParquet2 shape, Coord coord) {
        super(shape, coord,
                  ((coord.y&1)<<1) + (coord.x&1) // 0..3
             );
    }

    @Override
    public ShapeParquet2 getShape() {
        return (ShapeParquet2)super.getShape();
    }

    @Override
    public List<Coord> getCoordsNeighbor() {
        List<Coord> neighborCoord = new ArrayList<>(getShape().getNeighborNumber(getDirection()));

        // определяю координаты соседей
        switch (direction) {
        case 0:
            neighborCoord.add(new Coord(coord.x-1, coord.y-1));
            neighborCoord.add(new Coord(coord.x  , coord.y-1));
            neighborCoord.add(new Coord(coord.x+1, coord.y-1));
            neighborCoord.add(new Coord(coord.x-1, coord.y  ));
            neighborCoord.add(new Coord(coord.x+1, coord.y  ));
            neighborCoord.add(new Coord(coord.x  , coord.y+1));
            neighborCoord.add(new Coord(coord.x+1, coord.y+1));
            break;
        case 1:
            neighborCoord.add(new Coord(coord.x  , coord.y-1));
            neighborCoord.add(new Coord(coord.x+1, coord.y-1));
            neighborCoord.add(new Coord(coord.x-1, coord.y  ));
            neighborCoord.add(new Coord(coord.x+1, coord.y  ));
            neighborCoord.add(new Coord(coord.x-1, coord.y+1));
            neighborCoord.add(new Coord(coord.x  , coord.y+1));
            neighborCoord.add(new Coord(coord.x+1, coord.y+1));
            break;
        case 2:
            neighborCoord.add(new Coord(coord.x-1, coord.y-1));
            neighborCoord.add(new Coord(coord.x  , coord.y-1));
            neighborCoord.add(new Coord(coord.x+1, coord.y-1));
            neighborCoord.add(new Coord(coord.x-1, coord.y  ));
            neighborCoord.add(new Coord(coord.x+1, coord.y  ));
            neighborCoord.add(new Coord(coord.x-1, coord.y+1));
            neighborCoord.add(new Coord(coord.x  , coord.y+1));
            break;
        case 3:
            neighborCoord.add(new Coord(coord.x-1, coord.y-1));
            neighborCoord.add(new Coord(coord.x  , coord.y-1));
            neighborCoord.add(new Coord(coord.x-1, coord.y  ));
            neighborCoord.add(new Coord(coord.x+1, coord.y  ));
            neighborCoord.add(new Coord(coord.x-1, coord.y+1));
            neighborCoord.add(new Coord(coord.x  , coord.y+1));
            neighborCoord.add(new Coord(coord.x+1, coord.y+1));
            break;
        }

        return neighborCoord;
    }

    @Override
    protected void calcRegion() {
        ShapeParquet2 shape = getShape();
        double a = shape.getA();

        switch (direction) {
        case 0:
            region.setPoint(0, (2 * coord.x + 2) * a, (2 * coord.y + 0) * a);
            region.setPoint(1, (2 * coord.x + 4) * a, (2 * coord.y + 2) * a);
            region.setPoint(2, (2 * coord.x + 3) * a, (2 * coord.y + 3) * a);
            region.setPoint(3, (2 * coord.x + 1) * a, (2 * coord.y + 1) * a);
            break;
        case 1:
            region.setPoint(0, (2 * coord.x + 3) * a, (2 * coord.y + 1) * a);
            region.setPoint(1, (2 * coord.x + 4) * a, (2 * coord.y + 2) * a);
            region.setPoint(2, (2 * coord.x + 2) * a, (2 * coord.y + 4) * a);
            region.setPoint(3, (2 * coord.x + 1) * a, (2 * coord.y + 3) * a);
            break;
        case 2:
            region.setPoint(0, (2 * coord.x + 2) * a, (2 * coord.y + 0) * a);
            region.setPoint(1, (2 * coord.x + 3) * a, (2 * coord.y + 1) * a);
            region.setPoint(2, (2 * coord.x + 1) * a, (2 * coord.y + 3) * a);
            region.setPoint(3, (2 * coord.x + 0) * a, (2 * coord.y + 2) * a);
            break;
        case 3:
            region.setPoint(0, (2 * coord.x + 1) * a, (2 * coord.y + 1) * a);
            region.setPoint(1, (2 * coord.x + 3) * a, (2 * coord.y + 3) * a);
            region.setPoint(2, (2 * coord.x + 2) * a, (2 * coord.y + 4) * a);
            region.setPoint(3, (2 * coord.x + 0) * a, (2 * coord.y + 2) * a);
            break;
        }
    }

    @Override
    public RectDouble getRcInner(double borderWidth) {
        ShapeParquet2 shape = getShape();
        double sq = shape.getSq(borderWidth);
        double w = borderWidth/2.;

        RectDouble square = new RectDouble();
        switch (direction) {
        case 0: case 3:
            square.x = region.getPoint(0).x + w/BaseShape.SQRT2;
            square.y = region.getPoint(3).y + w/BaseShape.SQRT2;
            break;
        case 1: case 2:
            square.x = region.getPoint(2).x + w/BaseShape.SQRT2;
            square.y = region.getPoint(1).y + w/BaseShape.SQRT2;
            break;
        }
        square.width = sq;
        square.height = sq;
        return square;
    }

    @Override
    public int getShiftPointBorderIndex() { return 2; }

}
