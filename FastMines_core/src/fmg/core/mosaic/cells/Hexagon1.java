////////////////////////////////////////////////////////////////////////////////
//                               FastMines project
// file name: "Hexagon1.java"
//
// Реализация класса Hexagon1 - правильный 6-ти угольник (сота)
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

import fmg.common.geom.*;

/**
 * Шестиугольник
 * @see BaseCell
 **/
public class Hexagon1 extends BaseCell {

    public static class AttrHexagon1 extends BaseAttribute {

        @Override
        public SizeDouble getSize(Matrisize sizeField) {
            double a = getA();
            SizeDouble result = new SizeDouble(
                    a * (sizeField.m    +0.5) * SQRT3,
                    a * (sizeField.n*1.5+0.5));

            if (sizeField.n == 1)
                result.width -= getB()/2;

            return result;
        }

        @Override
        public int getNeighborNumber(int direction) { return 6; }
        @Override
        public int getVertexNumber(int direction) { return 6; }
        @Override
        public double getVertexIntersection() { return 3; }
        @Override
        public Size getDirectionSizeField() { return new Size(1, 2); }
        @Override
        protected double getA() { return Math.sqrt(2*getArea()/SQRT27); }
        /** пол стороны треугольника */
        protected double getB() { return getA()*SQRT3; }
        @Override
        public double getSq(double borderWidth) {
            double w = borderWidth/2.;
            return 2*(getB() - 2*w)/(SQRT3+1);
        }

    }

    public Hexagon1(AttrHexagon1 attr, Coord coord) {
        super(attr, coord,
                    coord.y&1 // 0..1
             );
    }

    @Override
    public AttrHexagon1 getAttr() {
        return (AttrHexagon1) super.getAttr();
    }

    @Override
    public List<Coord> getCoordsNeighbor() {
        List<Coord> neighborCoord = new ArrayList<>(getAttr().getNeighborNumber(getDirection()));

        // определяю координаты соседей
        neighborCoord.add(new Coord(coord.x-(direction^1), coord.y-1));
        neighborCoord.add(new Coord(coord.x+ direction   , coord.y-1));
        neighborCoord.add(new Coord(coord.x-1            , coord.y  ));
        neighborCoord.add(new Coord(coord.x+1            , coord.y  ));
        neighborCoord.add(new Coord(coord.x-(direction^1), coord.y+1));
        neighborCoord.add(new Coord(coord.x+ direction   , coord.y+1));

        return neighborCoord;
    }

    @Override
    protected void calcRegion() {
        AttrHexagon1 attr = getAttr();
        double a = attr.getA();
        double b = attr.getB();

        double oX = (coord.x+1)*b;                 // offset X
        double oY = (coord.y+(direction^1))*a*1.5; // offset Y

        switch (direction) {
        case 0:
            region.setPoint(0, oX      , oY - a    );
            region.setPoint(1, oX      , oY        );
            region.setPoint(2, oX - b/2, oY + a/2  );
            region.setPoint(3, oX - b  , oY        );
            region.setPoint(4, oX - b  , oY - a    );
            region.setPoint(5, oX - b/2, oY - a*1.5);
            break;
        case 1:
            region.setPoint(0, oX + b/2, oY + a/2  );
            region.setPoint(1, oX + b/2, oY + a*1.5);
            region.setPoint(2, oX      , oY + a*2  );
            region.setPoint(3, oX - b/2, oY + a*1.5);
            region.setPoint(4, oX - b/2, oY + a/2  );
            region.setPoint(5, oX      , oY        );
            break;
        }
    }

    @Override
    public RectDouble getRcInner(double borderWidth) {
        AttrHexagon1 attr = getAttr();
        double a = attr.getA();
        double b = attr.getB();
        double sq = attr.getSq(borderWidth);

        double oX = (coord.x+1)*b;      // offset X
        double oY = (coord.y+1-direction)*a*1.5; // offset Y

        PointDouble center = new PointDouble(); // координата вписанного в фигуру квадрата (не совпадает с центром фигуры)
        switch (direction) {
        case 0: center.x = oX - b/2; center.y = oY - a/2; break;
        case 1: center.x = oX;       center.y = oY + a;   break;
        }

        return new RectDouble(
            center.x - sq/2,
            center.y - sq/2,
            sq, sq);
    }

    @Override
    public int getShiftPointBorderIndex() { return 3; }

}
