////////////////////////////////////////////////////////////////////////////////
//                               FastMines project
// file name: "PentagonT10.java"
//
// Реализация класса PentagonT10 - 5-ти угольник, тип №10
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
import java.util.function.IntFunction;

import fmg.common.Color;
import fmg.common.geom.Coord;
import fmg.common.geom.PointDouble;
import fmg.common.geom.RectDouble;
import fmg.core.mosaic.shape.ShapePentagonT10;

/**
 * Пятиугольник. Тип №10
 * @see BaseCell
 **/
public class PentagonT10 extends BaseCell {

    public PentagonT10(ShapePentagonT10 shape, Coord coord) {
        super(shape, coord,
                  ((coord.y%6)<<1) + (coord.x&1) // 0..11
             );
    }

    @Override
    public ShapePentagonT10 getShape() {
        return (ShapePentagonT10)super.getShape();
    }

    @Override
    public List<Coord> getCoordsNeighbor() {
        List<Coord> neighborCoord = new ArrayList<>(getShape().getNeighborNumber(getDirection()));

        // определяю координаты соседей
        switch (direction) {
        case 0:
            neighborCoord.add(new Coord(coord.x  , coord.y-1));
            neighborCoord.add(new Coord(coord.x-1, coord.y  ));
            neighborCoord.add(new Coord(coord.x+1, coord.y  ));
            neighborCoord.add(new Coord(coord.x-1, coord.y+1));
            neighborCoord.add(new Coord(coord.x  , coord.y+1));
            neighborCoord.add(new Coord(coord.x+1, coord.y+1));
            neighborCoord.add(new Coord(coord.x-1, coord.y+2));
            break;
        case 1:
            neighborCoord.add(new Coord(coord.x  , coord.y-2));
            neighborCoord.add(new Coord(coord.x-1, coord.y-1));
            neighborCoord.add(new Coord(coord.x  , coord.y-1));
            neighborCoord.add(new Coord(coord.x+1, coord.y-1));
            neighborCoord.add(new Coord(coord.x-1, coord.y  ));
            neighborCoord.add(new Coord(coord.x+1, coord.y  ));
            neighborCoord.add(new Coord(coord.x  , coord.y+1));
            break;
        case 2:
            neighborCoord.add(new Coord(coord.x  , coord.y-1));
            neighborCoord.add(new Coord(coord.x+1, coord.y  ));
            neighborCoord.add(new Coord(coord.x-1, coord.y+1));
            neighborCoord.add(new Coord(coord.x  , coord.y+1));
            neighborCoord.add(new Coord(coord.x+1, coord.y+1));
            neighborCoord.add(new Coord(coord.x  , coord.y+2));
            break;
        case 3:
            neighborCoord.add(new Coord(coord.x-1, coord.y-1));
            neighborCoord.add(new Coord(coord.x  , coord.y-1));
            neighborCoord.add(new Coord(coord.x+1, coord.y-1));
            neighborCoord.add(new Coord(coord.x-1, coord.y  ));
            neighborCoord.add(new Coord(coord.x-1, coord.y+1));
            neighborCoord.add(new Coord(coord.x  , coord.y+1));
            break;
        case 4:
            neighborCoord.add(new Coord(coord.x  , coord.y-1));
            neighborCoord.add(new Coord(coord.x+1, coord.y-1));
            neighborCoord.add(new Coord(coord.x+1, coord.y  ));
            neighborCoord.add(new Coord(coord.x  , coord.y+1));
            neighborCoord.add(new Coord(coord.x+1, coord.y+1));
            neighborCoord.add(new Coord(coord.x+2, coord.y+1));
            break;
        case 5:
            neighborCoord.add(new Coord(coord.x+1, coord.y-2));
            neighborCoord.add(new Coord(coord.x-1, coord.y-1));
            neighborCoord.add(new Coord(coord.x  , coord.y-1));
            neighborCoord.add(new Coord(coord.x+1, coord.y-1));
            neighborCoord.add(new Coord(coord.x-1, coord.y  ));
            neighborCoord.add(new Coord(coord.x+1, coord.y+1));
            break;
        case 6:
            neighborCoord.add(new Coord(coord.x  , coord.y-2));
            neighborCoord.add(new Coord(coord.x-2, coord.y-1));
            neighborCoord.add(new Coord(coord.x-1, coord.y-1));
            neighborCoord.add(new Coord(coord.x  , coord.y-1));
            neighborCoord.add(new Coord(coord.x-1, coord.y  ));
            neighborCoord.add(new Coord(coord.x+1, coord.y  ));
            neighborCoord.add(new Coord(coord.x  , coord.y+1));
            break;
        case 7:
            neighborCoord.add(new Coord(coord.x-1, coord.y-1));
            neighborCoord.add(new Coord(coord.x-1, coord.y  ));
            neighborCoord.add(new Coord(coord.x+1, coord.y  ));
            neighborCoord.add(new Coord(coord.x-1, coord.y+1));
            neighborCoord.add(new Coord(coord.x  , coord.y+1));
            neighborCoord.add(new Coord(coord.x+1, coord.y+1));
            neighborCoord.add(new Coord(coord.x  , coord.y+2));
            break;
        case 8:
            neighborCoord.add(new Coord(coord.x-1, coord.y-1));
            neighborCoord.add(new Coord(coord.x  , coord.y-1));
            neighborCoord.add(new Coord(coord.x+1, coord.y-1));
            neighborCoord.add(new Coord(coord.x+1, coord.y  ));
            neighborCoord.add(new Coord(coord.x-1, coord.y+1));
            neighborCoord.add(new Coord(coord.x  , coord.y+1));
            break;
        case 9:
            neighborCoord.add(new Coord(coord.x  , coord.y-1));
            neighborCoord.add(new Coord(coord.x-1, coord.y  ));
            neighborCoord.add(new Coord(coord.x-2, coord.y+1));
            neighborCoord.add(new Coord(coord.x-1, coord.y+1));
            neighborCoord.add(new Coord(coord.x  , coord.y+1));
            neighborCoord.add(new Coord(coord.x  , coord.y+2));
            break;
        case 10:
            neighborCoord.add(new Coord(coord.x  , coord.y-1));
            neighborCoord.add(new Coord(coord.x+1, coord.y-1));
            neighborCoord.add(new Coord(coord.x-1, coord.y  ));
            neighborCoord.add(new Coord(coord.x-1, coord.y+1));
            neighborCoord.add(new Coord(coord.x  , coord.y+1));
            neighborCoord.add(new Coord(coord.x+1, coord.y+1));
            break;
        case 11:
            neighborCoord.add(new Coord(coord.x  , coord.y-2));
            neighborCoord.add(new Coord(coord.x  , coord.y-1));
            neighborCoord.add(new Coord(coord.x+1, coord.y-1));
            neighborCoord.add(new Coord(coord.x+2, coord.y-1));
            neighborCoord.add(new Coord(coord.x+1, coord.y  ));
            neighborCoord.add(new Coord(coord.x  , coord.y+1));
            break;
        }

        return neighborCoord;
    }

    private PointDouble getOffset() {
        ShapePentagonT10 shape = getShape();
        double a = shape.getA();

        PointDouble o = new PointDouble(0,0);
        switch (direction) {
        case 0: case 6: case  8: case 9: case 10:          o.x = a*2+a*6*((coord.x+0)/2); break;
        case 1: case 2: case  3: case 4: case 5: case 7:   o.x = a*5+a*6*((coord.x+0)/2); break;
        case 11:                                           o.x = a*2+a*6*((coord.x+1)/2); break;
        }
        switch (direction) {
        case 0:                                            o.y = a*5 +a*14*(coord.y/6);   break;
        case 1:                                            o.y =      a*14*(coord.y/6);   break;
        case 2: case 3: case  4: case 5:                   o.y = a*6 +a*14*(coord.y/6);   break;
        case 6:                                            o.y = a*7 +a*14*(coord.y/6);   break;
        case 7:                                            o.y = a*12+a*14*(coord.y/6);   break;
        case 8: case 9: case 10: case 11:                  o.y = a*13+a*14*(coord.y/6);   break;
        }
        return o;
    }

    @Override
    protected void calcRegion() {
        ShapePentagonT10 shape = getShape();
        double a = shape.getA();

        PointDouble o = getOffset();

        switch (direction) {
        case 0: case 3: case 7: case 8:
            region.setPoint(0, o.x + a  , o.y - a*3);
            region.setPoint(1, o.x + a*2, o.y - a*2);
            region.setPoint(2, o.x      , o.y      );
            region.setPoint(3, o.x - a*2, o.y - a*2);
            region.setPoint(4, o.x - a  , o.y - a*3);
            break;
        case 1: case 4: case 6: case 10:
            region.setPoint(0, o.x      , o.y      );
            region.setPoint(1, o.x + a*2, o.y + a*2);
            region.setPoint(2, o.x + a  , o.y + a*3);
            region.setPoint(3, o.x - a  , o.y + a*3);
            region.setPoint(4, o.x - a*2, o.y + a*2);
            break;
        case 2: case 11:
            region.setPoint(0, o.x - a*2, o.y - a*2);
            region.setPoint(1, o.x      , o.y      );
            region.setPoint(2, o.x - a*2, o.y + a*2);
            region.setPoint(3, o.x - a*3, o.y + a  );
            region.setPoint(4, o.x - a*3, o.y - a  );
            break;
        case 5: case 9:
            region.setPoint(0, o.x + a*2, o.y - a*2);
            region.setPoint(1, o.x + a*3, o.y - a  );
            region.setPoint(2, o.x + a*3, o.y + a  );
            region.setPoint(3, o.x + a*2, o.y + a*2);
            region.setPoint(4, o.x      , o.y      );
            break;
        }
    }

    @Override
    public RectDouble getRcInner(double borderWidth) {
        ShapePentagonT10 shape = getShape();
        double sq = shape.getSq(borderWidth);
        double sq2 = sq/2;

        PointDouble center = new PointDouble(); // координата центра квадрата
        switch (direction) {
        case 0: case  3: case 7: case  8: center.x = region.getPoint(2).x; center.y = region.getPoint(1).y; break;
        case 1: case  4: case 6: case 10:
        case 2: case 11:                  center.x = region.getPoint(0).x; center.y = region.getPoint(1).y; break;
        case 5: case  9:                  center.x = region.getPoint(0).x; center.y = region.getPoint(4).y; break;
        }

        return new RectDouble(
            center.x - sq2,
            center.y - sq2,
            sq, sq);
    }

    @Override
    public int getShiftPointBorderIndex() {
        switch (direction) {
        case 1: case 4: case 5: case 6: case 9: case 10:
            return 3;
        }
        return 2;
    }

    @Override
    public Color getCellFillColor(int fillMode, Color defaultColor, IntFunction<Color> getColor) {
        if (fillMode == getShape().getMaxCellFillModeValue()) {
            switch (getDirection()) {
            case  2: case  3: case  4: case  5: return getColor.apply(0);
            case  8: case  9: case 10: case 11: return getColor.apply(1);
            case  1: case  7: return getColor.apply(2);
            case  0: case  6: return getColor.apply(3);
//          default:
//              return getColor.apply(-1);
            }
        }
        return super.getCellFillColor(fillMode, defaultColor, getColor);
    }

}