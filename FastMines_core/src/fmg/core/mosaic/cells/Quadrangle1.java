////////////////////////////////////////////////////////////////////////////////
//                               FastMines project
// file name: "Quadrangle1.java"
//
// Реализация класса Quadrangle1 - четырёхугольник 120°-90°-60°-90°
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
import fmg.core.mosaic.shape.ShapeQuadrangle1;

/**
 * Quadrangle1 - четырёхугольник 120°-90°-60°-90°
 * @see BaseCell
 **/
public class Quadrangle1 extends BaseCell {

    public Quadrangle1(ShapeQuadrangle1 shape, Coord coord) {
        super(shape, coord,
                   (coord.y&3)*3 + (coord.x%3) // 0..11
             );
    }

    @Override
    public ShapeQuadrangle1 getShape() {
        return (ShapeQuadrangle1)super.getShape();
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
            neighborCoord.add(new Coord(coord.x+2, coord.y  ));
            neighborCoord.add(new Coord(coord.x-1, coord.y+1));
            neighborCoord.add(new Coord(coord.x  , coord.y+1));
            neighborCoord.add(new Coord(coord.x+1, coord.y+1));
            neighborCoord.add(new Coord(coord.x+2, coord.y+1));
            break;
        case 1:
            neighborCoord.add(new Coord(coord.x-2, coord.y-1));
            neighborCoord.add(new Coord(coord.x-1, coord.y-1));
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
            neighborCoord.add(new Coord(coord.x-2, coord.y  ));
            neighborCoord.add(new Coord(coord.x-1, coord.y  ));
            neighborCoord.add(new Coord(coord.x+1, coord.y  ));
            neighborCoord.add(new Coord(coord.x-2, coord.y+1));
            neighborCoord.add(new Coord(coord.x-1, coord.y+1));
            neighborCoord.add(new Coord(coord.x  , coord.y+1));
            neighborCoord.add(new Coord(coord.x+1, coord.y+1));
            break;
        case 3:
            neighborCoord.add(new Coord(coord.x-1, coord.y-1));
            neighborCoord.add(new Coord(coord.x  , coord.y-1));
            neighborCoord.add(new Coord(coord.x+1, coord.y-1));
            neighborCoord.add(new Coord(coord.x+2, coord.y-1));
            neighborCoord.add(new Coord(coord.x-1, coord.y  ));
            neighborCoord.add(new Coord(coord.x+1, coord.y  ));
            neighborCoord.add(new Coord(coord.x+2, coord.y  ));
            neighborCoord.add(new Coord(coord.x-1, coord.y+1));
            neighborCoord.add(new Coord(coord.x  , coord.y+1));
            break;
        case 4:
            neighborCoord.add(new Coord(coord.x-1, coord.y-1));
            neighborCoord.add(new Coord(coord.x  , coord.y-1));
            neighborCoord.add(new Coord(coord.x+1, coord.y-1));
            neighborCoord.add(new Coord(coord.x-1, coord.y  ));
            neighborCoord.add(new Coord(coord.x+1, coord.y  ));
            neighborCoord.add(new Coord(coord.x-2, coord.y+1));
            neighborCoord.add(new Coord(coord.x-1, coord.y+1));
            neighborCoord.add(new Coord(coord.x  , coord.y+1));
            neighborCoord.add(new Coord(coord.x+1, coord.y+1));
            break;
        case 5:
            neighborCoord.add(new Coord(coord.x-2, coord.y-1));
            neighborCoord.add(new Coord(coord.x-1, coord.y-1));
            neighborCoord.add(new Coord(coord.x  , coord.y-1));
            neighborCoord.add(new Coord(coord.x+1, coord.y-1));
            neighborCoord.add(new Coord(coord.x-2, coord.y  ));
            neighborCoord.add(new Coord(coord.x-1, coord.y  ));
            neighborCoord.add(new Coord(coord.x+1, coord.y  ));
            neighborCoord.add(new Coord(coord.x-1, coord.y+1));
            neighborCoord.add(new Coord(coord.x  , coord.y+1));
            break;
        case 6:
            neighborCoord.add(new Coord(coord.x  , coord.y-1));
            neighborCoord.add(new Coord(coord.x+1, coord.y-1));
            neighborCoord.add(new Coord(coord.x-2, coord.y  ));
            neighborCoord.add(new Coord(coord.x-1, coord.y  ));
            neighborCoord.add(new Coord(coord.x+1, coord.y  ));
            neighborCoord.add(new Coord(coord.x-2, coord.y+1));
            neighborCoord.add(new Coord(coord.x-1, coord.y+1));
            neighborCoord.add(new Coord(coord.x  , coord.y+1));
            neighborCoord.add(new Coord(coord.x+1, coord.y+1));
            break;
        case 7:
            neighborCoord.add(new Coord(coord.x  , coord.y-1));
            neighborCoord.add(new Coord(coord.x+1, coord.y-1));
            neighborCoord.add(new Coord(coord.x-1, coord.y  ));
            neighborCoord.add(new Coord(coord.x+1, coord.y  ));
            neighborCoord.add(new Coord(coord.x+2, coord.y  ));
            neighborCoord.add(new Coord(coord.x-1, coord.y+1));
            neighborCoord.add(new Coord(coord.x  , coord.y+1));
            neighborCoord.add(new Coord(coord.x+1, coord.y+1));
            neighborCoord.add(new Coord(coord.x+2, coord.y+1));
            break;
        case 8:
            neighborCoord.add(new Coord(coord.x-1, coord.y-1));
            neighborCoord.add(new Coord(coord.x  , coord.y-1));
            neighborCoord.add(new Coord(coord.x+1, coord.y-1));
            neighborCoord.add(new Coord(coord.x+2, coord.y-1));
            neighborCoord.add(new Coord(coord.x-1, coord.y  ));
            neighborCoord.add(new Coord(coord.x+1, coord.y  ));
            neighborCoord.add(new Coord(coord.x-1, coord.y+1));
            neighborCoord.add(new Coord(coord.x  , coord.y+1));
            neighborCoord.add(new Coord(coord.x+1, coord.y+1));
            break;
        case 9:
            neighborCoord.add(new Coord(coord.x-2, coord.y-1));
            neighborCoord.add(new Coord(coord.x-1, coord.y-1));
            neighborCoord.add(new Coord(coord.x  , coord.y-1));
            neighborCoord.add(new Coord(coord.x+1, coord.y-1));
            neighborCoord.add(new Coord(coord.x-2, coord.y  ));
            neighborCoord.add(new Coord(coord.x-1, coord.y  ));
            neighborCoord.add(new Coord(coord.x+1, coord.y  ));
            neighborCoord.add(new Coord(coord.x  , coord.y+1));
            neighborCoord.add(new Coord(coord.x+1, coord.y+1));
            break;
        case 10:
            neighborCoord.add(new Coord(coord.x-1, coord.y-1));
            neighborCoord.add(new Coord(coord.x  , coord.y-1));
            neighborCoord.add(new Coord(coord.x+1, coord.y-1));
            neighborCoord.add(new Coord(coord.x+2, coord.y-1));
            neighborCoord.add(new Coord(coord.x-1, coord.y  ));
            neighborCoord.add(new Coord(coord.x+1, coord.y  ));
            neighborCoord.add(new Coord(coord.x+2, coord.y  ));
            neighborCoord.add(new Coord(coord.x  , coord.y+1));
            neighborCoord.add(new Coord(coord.x+1, coord.y+1));
            break;
        case 11:
            neighborCoord.add(new Coord(coord.x-1, coord.y-1));
            neighborCoord.add(new Coord(coord.x  , coord.y-1));
            neighborCoord.add(new Coord(coord.x+1, coord.y-1));
            neighborCoord.add(new Coord(coord.x-1, coord.y  ));
            neighborCoord.add(new Coord(coord.x+1, coord.y  ));
            neighborCoord.add(new Coord(coord.x-1, coord.y+1));
            neighborCoord.add(new Coord(coord.x  , coord.y+1));
            neighborCoord.add(new Coord(coord.x+1, coord.y+1));
            neighborCoord.add(new Coord(coord.x+2, coord.y+1));
            break;
        }

        return neighborCoord;
    }

    @Override
    protected void calcRegion() {
        ShapeQuadrangle1 shape = getShape();
        double a = shape.getA();
        double b = shape.getB();
        double h = shape.getH();
        double n = shape.getN();
        double m = shape.getM();

        // определение координат точек фигуры
        double oX = (h*2)*(coord.x/3) + h+m; // offset X
        double oY = (a*3)*(coord.y/4) + a+n; // offset Y

        switch (direction) {
        case 0:
            region.setPoint(0, oX - h  , oY - n-n);
            region.setPoint(1, oX - m  , oY - n  );
            region.setPoint(2, oX - h-m, oY - n  );
            region.setPoint(3, oX - h-m, oY - n-b);
            break;
        case 1:
            region.setPoint(0, oX      , oY - n-n);
            region.setPoint(1, oX - m  , oY - n  );
            region.setPoint(2, oX - h  , oY - n-n);
            region.setPoint(3, oX - m  , oY - n-a);
            break;
        case 2:
            region.setPoint(0, oX + m  , oY - n-b);
            region.setPoint(1, oX + m  , oY - n  );
            region.setPoint(2, oX - m  , oY - n  );
            region.setPoint(3, oX      , oY - n-n);
            break;
        case 3:
            region.setPoint(0, oX - m  , oY - n  );
            region.setPoint(1, oX - h  , oY      );
            region.setPoint(2, oX - h-m, oY - n+b);
            region.setPoint(3, oX - h-m, oY - n  );
            break;
        case 4:
            region.setPoint(0, oX - m  , oY - n  );
            region.setPoint(1, oX      , oY      );
            region.setPoint(2, oX - m  , oY - n+a);
            region.setPoint(3, oX - h  , oY      );
            break;
        case 5:
            region.setPoint(0, oX + m  , oY - n  );
            region.setPoint(1, oX + m  , oY - n+b);
            region.setPoint(2, oX      , oY      );
            region.setPoint(3, oX - m  , oY - n  );
            break;
        case 6:
            region.setPoint(0, oX - m  , oY + n-b);
            region.setPoint(1, oX - m  , oY + n  );
            region.setPoint(2, oX - h-m, oY + n  );
            region.setPoint(3, oX - h  , oY      );
            break;
        case 7:
            region.setPoint(0, oX      , oY      );
            region.setPoint(1, oX + m  , oY + n  );
            region.setPoint(2, oX - m  , oY + n  );
            region.setPoint(3, oX - m  , oY + n-b);
            break;
        case 8:
            region.setPoint(0, oX + h  , oY      );
            region.setPoint(1, oX + m  , oY + n  );
            region.setPoint(2, oX      , oY      );
            region.setPoint(3, oX + m  , oY + n-a);
            break;
        case 9:
            region.setPoint(0, oX - m  , oY + n  );
            region.setPoint(1, oX - m  , oY + n+b);
            region.setPoint(2, oX - h  , oY + n+n);
            region.setPoint(3, oX - h-m, oY + n  );
            break;
        case 10:
            region.setPoint(0, oX + m  , oY + n  );
            region.setPoint(1, oX      , oY + n+n);
            region.setPoint(2, oX - m  , oY + n+b);
            region.setPoint(3, oX - m  , oY + n  );
            break;
        case 11:
            region.setPoint(0, oX + m  , oY + n  );
            region.setPoint(1, oX + h  , oY + n+n);
            region.setPoint(2, oX + m  , oY + n+a);
            region.setPoint(3, oX      , oY + n+n);
            break;
        }
    }

    @Override
    public RectDouble getRcInner(double borderWidth) {
        ShapeQuadrangle1 shape = getShape();
        double a = shape.getA();
        double b = shape.getB();
        double h = shape.getH();
        double n = shape.getN();
        double m = shape.getM();
        double z = shape.getZ();
        double zx = shape.getZx();
        double zy = shape.getZy();
//      double w = borderWidth/2.;
        double sq    = shape.getSq(borderWidth);
        double sq2   = sq/2;

        double oX = (h*2)*(coord.x/3) + h+m; // offset X
        double oY = (a*3)*(coord.y/4) + a+n; // offset Y

        PointDouble center = new PointDouble(); // координата центра квадрата
        switch (direction) {
        case 0:  center.x = oX - h-m+zx; center.y = oY - n-b+zy; break;
        case 1:  center.x = oX - m;      center.y = oY - n-a+z ; break;
        case 2:  center.x = oX + m  -zx; center.y = oY - n-b+zy; break;
        case 3:  center.x = oX - h-m+zx; center.y = oY - n+b-zy; break;
        case 4:  center.x = oX - m;      center.y = oY - n+a-z ; break;
        case 5:  center.x = oX + m  -zx; center.y = oY - n+b-zy; break;
        case 6:  center.x = oX - m  -zx; center.y = oY + n-b+zy; break;
        case 7:  center.x = oX - m  +zx; center.y = oY + n-b+zy; break;
        case 8:  center.x = oX + m;      center.y = oY + n-a+z ; break;
        case 9:  center.x = oX - m  -zx; center.y = oY + n+b-zy; break;
        case 10: center.x = oX - m  +zx; center.y = oY + n+b-zy; break;
        case 11: center.x = oX + m;      center.y = oY + n+a-z ; break;
        }

        return new RectDouble(
            center.x - sq2,
            center.y - sq2,
            sq, sq);
    }

    @Override
    public int getShiftPointBorderIndex() {
        switch (direction) {
        case 1: case  8: return 1;
        case 4: case 11: return 3;
        }
        return 2;
    }

}
