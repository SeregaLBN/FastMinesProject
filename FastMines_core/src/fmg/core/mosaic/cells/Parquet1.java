////////////////////////////////////////////////////////////////////////////////
//                               FastMines project
// file name: "Parquet1.java"
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

package fmg.core.mosaic.cells;

import java.util.ArrayList;
import java.util.List;

import fmg.common.geom.Coord;
import fmg.common.geom.RectDouble;
import fmg.core.mosaic.shape.BaseShape;
import fmg.core.mosaic.shape.ShapeParquet1;

/**
 * Паркет в елку
 * @see BaseCell
 **/
public class Parquet1 extends BaseCell {

    public Parquet1(ShapeParquet1 shape, Coord coord) {
        super(shape, coord,
                    coord.x&1 // 0..1
             );
    }

    @Override
    public ShapeParquet1 getShape() {
        return (ShapeParquet1)super.getShape();
    }

    @Override
    public List<Coord> getCoordsNeighbor() {
        List<Coord> neighborCoord = new ArrayList<>(getShape().getNeighborNumber(getDirection()));

        // определяю координаты соседей
        boolean bdir = (direction != 0);
        neighborCoord.add(new Coord(bdir ? coord.x  : coord.x-1, coord.y-1));
        neighborCoord.add(new Coord(bdir ? coord.x-1: coord.x  , bdir ? coord.y  : coord.y-1));
        neighborCoord.add(new Coord(       coord.x+1           , bdir ? coord.y  : coord.y-1));
        neighborCoord.add(new Coord(       coord.x-1           , bdir ? coord.y+1: coord.y));
        neighborCoord.add(new Coord(bdir ? coord.x  : coord.x+1, bdir ? coord.y+1: coord.y));
        neighborCoord.add(new Coord(bdir ? coord.x+1: coord.x  , coord.y+1));

        return neighborCoord;
    }

    @Override
    protected void calcRegion() {
        ShapeParquet1 shape = getShape();
        double a = shape.getA();

        switch (direction) {
        case 0:
            region.setPoint(0, a * (2 + 2 * coord.x), a * (0 + 2 * coord.y));
            region.setPoint(1, a * (3 + 2 * coord.x), a * (1 + 2 * coord.y));
            region.setPoint(2, a * (1 + 2 * coord.x), a * (3 + 2 * coord.y));
            region.setPoint(3, a * (0 + 2 * coord.x), a * (2 + 2 * coord.y));
            break;
        case 1:
            region.setPoint(0, a * (1 + 2 * coord.x), a * (1 + 2 * coord.y));
            region.setPoint(1, a * (3 + 2 * coord.x), a * (3 + 2 * coord.y));
            region.setPoint(2, a * (2 + 2 * coord.x), a * (4 + 2 * coord.y));
            region.setPoint(3, a * (0 + 2 * coord.x), a * (2 + 2 * coord.y));
            break;
        }
    }

    @Override
    public RectDouble getRcInner(double borderWidth) {
        ShapeParquet1 shape = getShape();
        double sq = shape.getSq(borderWidth);
        double w = borderWidth/2.;
        boolean bdir = (direction != 0);

        return new RectDouble(
            (bdir ? region.getPoint(0).x: region.getPoint(2).x) + w / BaseShape.SQRT2,
            (bdir ? region.getPoint(3).y: region.getPoint(1).y) + w / BaseShape.SQRT2,
            sq, sq);
    }

    @Override
    public int getShiftPointBorderIndex() { return 2; }

}
