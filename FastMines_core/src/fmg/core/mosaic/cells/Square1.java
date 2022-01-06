////////////////////////////////////////////////////////////////////////////////
//                               FastMines project
// file name: "Square1.java"
//
// Описание класса Square1 - квадрат (классический вариант поля)
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
import java.util.Map;

import fmg.common.Color;
import fmg.common.geom.*;

/**
 * Квадрат. Вариант 1
 * @see BaseCell
 **/
public class Square1 extends BaseCell {

    public static class AttrSquare1 extends BaseAttribute {

        @Override
        public SizeDouble getSize(Matrisize sizeField) {
            double a = getA(); // размер стороны квадрата
            return new SizeDouble(
                sizeField.m * a,
                sizeField.n * a);
        }

        @Override
        public int getNeighborNumber(int direction) { return 8; }
        @Override
        public int getVertexNumber(int direction) { return 4; }
        @Override
        public double getVertexIntersection() { return 4; }
        @Override
        public Size getDirectionSizeField() { return new Size(1,1); }
        @Override
        protected double getA() { return Math.sqrt(getArea()); }
        @Override
        public double getSq(double borderWidth) {
            double w = borderWidth/2.;
            return getA()-2*w;
        }
    }

    public Square1(AttrSquare1 attr, Coord coord) {
        super(attr, coord, -1);
    }

    @Override
    public AttrSquare1 getAttr() {
        return (AttrSquare1) super.getAttr();
    }

    @Override
    protected List<Coord> getCoordsNeighbor() {
        List<Coord> neighborCoord = new ArrayList<>(getAttr().getNeighborNumber(getDirection()));

        // определяю координаты соседей
        neighborCoord.add(new Coord(coord.x-1, coord.y-1));
        neighborCoord.add(new Coord(coord.x  , coord.y-1));
        neighborCoord.add(new Coord(coord.x+1, coord.y-1));
        neighborCoord.add(new Coord(coord.x-1, coord.y  ));
        neighborCoord.add(new Coord(coord.x+1, coord.y  ));
        neighborCoord.add(new Coord(coord.x-1, coord.y+1));
        neighborCoord.add(new Coord(coord.x  , coord.y+1));
        neighborCoord.add(new Coord(coord.x+1, coord.y+1));

        return neighborCoord;
    }

    @Override
    public boolean pointInRegion(PointDouble point) {
        if ((point.x < region.getPoint(3).x) || (point.x >= region.getPoint(0).x) ||
            (point.y < region.getPoint(0).y) || (point.y >= region.getPoint(2).y))
            return false;
        return true;
    }

    @Override
    protected void calcRegion() {
        AttrSquare1 attr = getAttr();
        double a = attr.getA();

        double x1 = a * (coord.x + 0);
        double x2 = a * (coord.x + 1);
        double y1 = a * (coord.y + 0);
        double y2 = a * (coord.y + 1);

        region.setPoint(0, x2, y1);
        region.setPoint(1, x2, y2);
        region.setPoint(2, x1, y2);
        region.setPoint(3, x1, y1);
    }

    @Override
    public RectDouble getRcInner(double borderWidth) {
        AttrSquare1 attr = getAttr();
        double sq = attr.getSq(borderWidth);
        double w = borderWidth/2.;

        return new RectDouble(
            region.getPoint(3).x + w,
            region.getPoint(3).y + w,
            sq, sq);
    }

    @Override
    public int getShiftPointBorderIndex() { return 2; }

    @Override
    public Color getBackgroundFillColor(int fillMode, Color defaultColor, Map<Integer, Color> repositoryColor) {
        switch (fillMode) {
        default:
            return super.getBackgroundFillColor(fillMode, defaultColor, repositoryColor);
        case 1: // перекрываю базовый на основе direction
            int pos = (-getCoord().x + getCoord().y) % ((getAttr().hashCode() & 0x3)+fillMode);
//          Logger.info(pos);
            return repositoryColor.get(pos);
        }
    }

}
