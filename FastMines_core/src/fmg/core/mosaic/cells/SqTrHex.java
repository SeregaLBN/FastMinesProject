////////////////////////////////////////////////////////////////////////////////
//                               FastMines project
// file name: "SqTrHex.java"
//
// Реализация класса SqTrHex - мозаика из 6Square 4Triangle 2Hexagon
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
import fmg.core.mosaic.shape.ShapeSqTrHex;

/**
 * Комбинация. мозаика из 6Square 4Triangle 2Hexagon
 * @see BaseCell
 **/
public class SqTrHex extends BaseCell {

    public SqTrHex(ShapeSqTrHex shape, Coord coord) {
        super(shape, coord,
                   (coord.y&3)*3+(coord.x%3) // 0..11
             );
    }

    @Override
    public ShapeSqTrHex getShape() {
        return (ShapeSqTrHex)super.getShape();
    }

    @Override
    public List<Coord> getCoordsNeighbor() {
        List<Coord> neighborCoord = new ArrayList<>(getShape().getNeighborNumber(getDirection()));

        // определяю координаты соседей
        switch (direction) {
        case 0:
            neighborCoord.add(new Coord(coord.x-1, coord.y-1));
            neighborCoord.add(new Coord(coord.x+1, coord.y  ));
            neighborCoord.add(new Coord(coord.x-2, coord.y+1));
            neighborCoord.add(new Coord(coord.x-1, coord.y+1));
            neighborCoord.add(new Coord(coord.x  , coord.y+1));
            neighborCoord.add(new Coord(coord.x+1, coord.y+1));
            break;
        case 1:
            neighborCoord.add(new Coord(coord.x-2, coord.y-1));
            neighborCoord.add(new Coord(coord.x  , coord.y-1));
            neighborCoord.add(new Coord(coord.x-1, coord.y  ));
            neighborCoord.add(new Coord(coord.x+1, coord.y  ));
            neighborCoord.add(new Coord(coord.x-2, coord.y+1));
            neighborCoord.add(new Coord(coord.x-1, coord.y+1));
            neighborCoord.add(new Coord(coord.x  , coord.y+1));
            neighborCoord.add(new Coord(coord.x+1, coord.y+1));
            break;
        case 2:
            neighborCoord.add(new Coord(coord.x-3, coord.y-1));
            neighborCoord.add(new Coord(coord.x-1, coord.y-1));
            neighborCoord.add(new Coord(coord.x  , coord.y-1));
            neighborCoord.add(new Coord(coord.x-1, coord.y  ));
            neighborCoord.add(new Coord(coord.x-1, coord.y+1));
            neighborCoord.add(new Coord(coord.x  , coord.y+1));
            break;
        case 3:
            neighborCoord.add(new Coord(coord.x  , coord.y-1));
            neighborCoord.add(new Coord(coord.x+1, coord.y-1));
            neighborCoord.add(new Coord(coord.x-2, coord.y  ));
            neighborCoord.add(new Coord(coord.x-1, coord.y  ));
            neighborCoord.add(new Coord(coord.x+1, coord.y  ));
            neighborCoord.add(new Coord(coord.x-1, coord.y+1));
            neighborCoord.add(new Coord(coord.x  , coord.y+1));
            neighborCoord.add(new Coord(coord.x  , coord.y+2));
            break;
        case 4:
            neighborCoord.add(new Coord(coord.x-1, coord.y-1));
            neighborCoord.add(new Coord(coord.x  , coord.y-1));
            neighborCoord.add(new Coord(coord.x+1, coord.y-1));
            neighborCoord.add(new Coord(coord.x+2, coord.y-1));
            neighborCoord.add(new Coord(coord.x-1, coord.y  ));
            neighborCoord.add(new Coord(coord.x+1, coord.y  ));
            neighborCoord.add(new Coord(coord.x+2, coord.y  ));
            neighborCoord.add(new Coord(coord.x-1, coord.y+1));
            neighborCoord.add(new Coord(coord.x  , coord.y+1));
            neighborCoord.add(new Coord(coord.x+1, coord.y+1));
            neighborCoord.add(new Coord(coord.x+2, coord.y+1));
            neighborCoord.add(new Coord(coord.x-1, coord.y+2));
            break;
        case 5:
            neighborCoord.add(new Coord(coord.x-1, coord.y-2));
            neighborCoord.add(new Coord(coord.x  , coord.y-2));
            neighborCoord.add(new Coord(coord.x-1, coord.y-1));
            neighborCoord.add(new Coord(coord.x  , coord.y-1));
            neighborCoord.add(new Coord(coord.x+1, coord.y-1));
            neighborCoord.add(new Coord(coord.x+2, coord.y-1));
            neighborCoord.add(new Coord(coord.x-1, coord.y  ));
            neighborCoord.add(new Coord(coord.x+1, coord.y  ));
            break;
        case 6:
            neighborCoord.add(new Coord(coord.x-2, coord.y-1));
            neighborCoord.add(new Coord(coord.x  , coord.y-1));
            neighborCoord.add(new Coord(coord.x+1, coord.y-1));
            neighborCoord.add(new Coord(coord.x-1, coord.y  ));
            neighborCoord.add(new Coord(coord.x-1, coord.y+1));
            neighborCoord.add(new Coord(coord.x  , coord.y+1));
            break;
        case 7:
            neighborCoord.add(new Coord(coord.x  , coord.y-1));
            neighborCoord.add(new Coord(coord.x+1, coord.y  ));
            neighborCoord.add(new Coord(coord.x-2, coord.y+1));
            neighborCoord.add(new Coord(coord.x-1, coord.y+1));
            neighborCoord.add(new Coord(coord.x  , coord.y+1));
            neighborCoord.add(new Coord(coord.x+1, coord.y+1));
            break;
        case 8:
            neighborCoord.add(new Coord(coord.x-1, coord.y-1));
            neighborCoord.add(new Coord(coord.x+1, coord.y-1));
            neighborCoord.add(new Coord(coord.x-1, coord.y  ));
            neighborCoord.add(new Coord(coord.x+1, coord.y  ));
            neighborCoord.add(new Coord(coord.x-2, coord.y+1));
            neighborCoord.add(new Coord(coord.x-1, coord.y+1));
            neighborCoord.add(new Coord(coord.x  , coord.y+1));
            neighborCoord.add(new Coord(coord.x+1, coord.y+1));
            break;
        case 9:
            neighborCoord.add(new Coord(coord.x  , coord.y-2));
            neighborCoord.add(new Coord(coord.x+1, coord.y-2));
            neighborCoord.add(new Coord(coord.x-1, coord.y-1));
            neighborCoord.add(new Coord(coord.x  , coord.y-1));
            neighborCoord.add(new Coord(coord.x+1, coord.y-1));
            neighborCoord.add(new Coord(coord.x+2, coord.y-1));
            neighborCoord.add(new Coord(coord.x-1, coord.y  ));
            neighborCoord.add(new Coord(coord.x+1, coord.y  ));
            break;
        case 10:
            neighborCoord.add(new Coord(coord.x  , coord.y-1));
            neighborCoord.add(new Coord(coord.x+1, coord.y-1));
            neighborCoord.add(new Coord(coord.x-2, coord.y  ));
            neighborCoord.add(new Coord(coord.x-1, coord.y  ));
            neighborCoord.add(new Coord(coord.x+1, coord.y  ));
            neighborCoord.add(new Coord(coord.x  , coord.y+1));
            neighborCoord.add(new Coord(coord.x+1, coord.y+1));
            neighborCoord.add(new Coord(coord.x+1, coord.y+2));
            break;
        case 11:
            neighborCoord.add(new Coord(coord.x-1, coord.y-1));
            neighborCoord.add(new Coord(coord.x  , coord.y-1));
            neighborCoord.add(new Coord(coord.x+1, coord.y-1));
            neighborCoord.add(new Coord(coord.x+2, coord.y-1));
            neighborCoord.add(new Coord(coord.x-1, coord.y  ));
            neighborCoord.add(new Coord(coord.x+1, coord.y  ));
            neighborCoord.add(new Coord(coord.x+2, coord.y  ));
            neighborCoord.add(new Coord(coord.x  , coord.y+1));
            neighborCoord.add(new Coord(coord.x+1, coord.y+1));
            neighborCoord.add(new Coord(coord.x+2, coord.y+1));
            neighborCoord.add(new Coord(coord.x+3, coord.y+1));
            neighborCoord.add(new Coord(coord.x  , coord.y+2));
            break;
        }

        return neighborCoord;
    }

    private PointDouble getOffset() {
        ShapeSqTrHex shape = getShape();
        double a = shape.getA();
        double h = shape.getH();

        return new PointDouble(
                (h*2+a  )*(coord.x/3) + a+h,
                (h*2+a*3)*(coord.y/4) + a*2+h);
    }

    @Override
    protected void calcRegion() {
        ShapeSqTrHex shape = getShape();
        double a = shape.getA();
        double b = a/2;
        double h = shape.getH();

        PointDouble o = getOffset();
        switch (direction) {
        case 0:
            region.setPoint(0, o.x - b-h  , o.y - a-b-h);
            region.setPoint(1, o.x - h    , o.y - a-b  );
            region.setPoint(2, o.x - h-a  , o.y - a-b  );
            break;
        case 1:
            region.setPoint(0, o.x - b    , o.y - a-a-h);
            region.setPoint(1, o.x        , o.y - a-a  );
            region.setPoint(2, o.x - h    , o.y - a-b  );
            region.setPoint(3, o.x - b-h  , o.y - a-b-h);
            break;
        case 2:
            region.setPoint(0, o.x + b    , o.y - a-a-h);
            region.setPoint(1, o.x        , o.y - a-a  );
            region.setPoint(2, o.x - b    , o.y - a-a-h);
            break;
        case 3:
            region.setPoint(0, o.x - h    , o.y - a-b  );
            region.setPoint(1, o.x - h    , o.y - b    );
            region.setPoint(2, o.x - a-h  , o.y - b    );
            region.setPoint(3, o.x - a-h  , o.y - a-b  );
            break;
        case 4:
            region.setPoint(0, o.x        , o.y - a-a  );
            region.setPoint(1, o.x + h    , o.y - a-b  );
            region.setPoint(2, o.x + h    , o.y - b    );
            region.setPoint(3, o.x        , o.y        );
            region.setPoint(4, o.x - h    , o.y - b    );
            region.setPoint(5, o.x - h    , o.y - a-b  );
            break;
        case 5:
            region.setPoint(0, o.x + b    , o.y - a-a-h);
            region.setPoint(1, o.x + b+h  , o.y - a-b-h);
            region.setPoint(2, o.x + h    , o.y - a-b  );
            region.setPoint(3, o.x        , o.y - a-a  );
            break;
        case 6:
            region.setPoint(0, o.x - h    , o.y - b    );
            region.setPoint(1, o.x - b-h  , o.y - b+h  );
            region.setPoint(2, o.x - a-h  , o.y - b    );
            break;
        case 7:
            region.setPoint(0, o.x        , o.y        );
            region.setPoint(1, o.x + b    , o.y + h    );
            region.setPoint(2, o.x - b    , o.y + h    );
            break;
        case 8:
            region.setPoint(0, o.x + h    , o.y - b    );
            region.setPoint(1, o.x + h+b  , o.y - b+h  );
            region.setPoint(2, o.x + b    , o.y + h    );
            region.setPoint(3, o.x        , o.y        );
            break;
        case 9:
            region.setPoint(0, o.x - h    , o.y - b    );
            region.setPoint(1, o.x        , o.y        );
            region.setPoint(2, o.x - b    , o.y + h    );
            region.setPoint(3, o.x - b-h  , o.y - b+h  );
            break;
        case 10:
            region.setPoint(0, o.x + b    , o.y + h    );
            region.setPoint(1, o.x + b    , o.y + a+h  );
            region.setPoint(2, o.x - b    , o.y + a+h  );
            region.setPoint(3, o.x - b    , o.y + h    );
            break;
        case 11:
            region.setPoint(0, o.x + b+h  , o.y + h-b  );
            region.setPoint(1, o.x + b+h+h, o.y + h    );
            region.setPoint(2, o.x + b+h+h, o.y + a+h  );
            region.setPoint(3, o.x + b+h  , o.y + a+b+h);
            region.setPoint(4, o.x + b    , o.y + a+h  );
            region.setPoint(5, o.x + b    , o.y + h    );
            break;
        }
    }

    @Override
    public RectDouble getRcInner(double borderWidth) {
        ShapeSqTrHex shape = getShape();
        double a = shape.getA();
        double b = a/2;
        double h = shape.getH();
        double w = borderWidth/2.;
        double sq = shape.getSq(borderWidth);
        double sq2 = sq/2;

        PointDouble o = getOffset();

        PointDouble center = new PointDouble(); // координата центра вписанного в фигуру квадрата
        switch (direction) {
        case  0: center.x = o.x -  b-h;    center.y = o.y - a-b-w-sq2;   break;
        case  1: center.x = o.x - (b+h)/2; center.y = o.y - a-b-(b+h)/2; break;
        case  2: center.x = o.x;           center.y = o.y - a-a-h+w+sq2; break;
        case  3: center.x = o.x -  b-h;    center.y = o.y - a;           break;
        case  4: center.x = o.x;           center.y = o.y - a;           break;
        case  5: center.x = o.x + (b+h)/2; center.y = o.y - a-b-(b+h)/2; break;
        case  6: center.x = o.x -  b-h;    center.y = o.y - b+w+sq2;     break;
        case  7: center.x = o.x;           center.y = o.y + h-w-sq2;     break;
        case  8: center.x = o.x + (b+h)/2; center.y = o.y - b+(b+h)/2;   break;
        case  9: center.x = o.x - (b+h)/2; center.y = o.y - b+(b+h)/2;   break;
        case 10: center.x = o.x;           center.y = o.y + b+h;         break;
        case 11: center.x = o.x +  b+h;    center.y = o.y + b+h;         break;
        }

        return new RectDouble(
            center.x - sq2,
            center.y - sq2,
            sq, sq);
    }

    @Override
    public int getShiftPointBorderIndex() {
        switch (direction) {
        case 2: case  6: return 1;
        case 4: case 11: return 3;
        }
        return 2;
    }

}
