////////////////////////////////////////////////////////////////////////////////
//                               FastMines project
// file name: "TrSq2.java"
//
// Реализация класса TrSq2 - мозаика из 24х треугольников и 12х квадратов (на 1 квадрат приходится 2 треугольника)
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
 * Комбинация. мозаика из 24х треугольников и 12х квадратов (на 1 квадрат приходится 2 треугольника)
 * @see BaseCell
 **/
public class TrSq2 extends BaseCell {

    public static class AttrTrSq2 extends BaseAttribute {

        @Override
        public SizeDouble getSize(Matrisize sizeField) {
            double a = getA();
            double b = getB();
            double h = getH();
            SizeDouble result = new SizeDouble(
                b+h*((sizeField.m+2)/3)+
                  a*((sizeField.m+1)/3)+
                  b*((sizeField.m+0)/3),
                b+h*((sizeField.n+2)/3)+
                  a*((sizeField.n+1)/3)+
                  b*((sizeField.n+0)/3));

            if (sizeField.n < 5) {
                int x = sizeField.m % 6;
                switch (sizeField.n) {
                case 1:
                    switch (x) { case 0: case 2: case 5: result.width -= b; }
                    break;
                case 2: case 3: case 4:
                    if (x == 5) result.width -= b;
                    break;
                }
            }
            if (sizeField.m < 5) {
                int y = sizeField.n % 6;
                switch (sizeField.m) {
                case 1:
                    switch (y) { case 2: case 3: case 5: result.height -= b; }
                    break;
                case 2: case 3: case 4:
                    if (y == 2) result.height -= b;
                    break;
                }
            }

            return result;
        }

        @Override
        public int getNeighborNumber(int direction) {
            switch (direction) {
            case  1: case  2: case  4: case  5:
            case  6: case  8: case  9: case 11:
            case 12: case 13: case 15: case 16:
            case 19: case 20: case 22: case 23:
            case 24: case 26: case 27: case 29:
            case 30: case 31: case 33: case 34: return 9;
            case  0: case  3: case  7: case 10:
            case 14: case 17: case 18: case 21:
            case 25: case 28: case 32: case 35: return 12;
            default:
                throw new IllegalArgumentException("Invalid value direction="+direction);
            }
        }
        @Override
        public int getVertexNumber(int direction) {
            switch (direction) {
            case  0: case  3: case  7: case 10:
            case 14: case 17: case 18: case 21:
            case 25: case 28: case 32: case 35: return 4;
            case  1: case  2: case  4: case  5:
            case  6: case  8: case  9: case 11:
            case 12: case 13: case 15: case 16:
            case 19: case 20: case 22: case 23:
            case 24: case 26: case 27: case 29:
            case 30: case 31: case 33: case 34: return 3;
            default:
                throw new IllegalArgumentException("Invalid value direction="+direction);
            }
        }
        @Override
        public double getVertexIntersection() { return 5.; }
        @Override
        public Size getDirectionSizeField() { return new Size(6, 6); }
        /** размер стороны треугольника и квадрата */
        @Override
        protected double getA() { return Math.sqrt(6*getArea()/(2+SQRT3)); }
        protected double getB() { return getA()/2; }
        protected double getH() { return getB()*SQRT3; }
        @Override
        public double getSq(double borderWidth) {
            double w = borderWidth/2.;
            return (getA()*SQRT3 - w*6) / (2+SQRT3) - 1;
        }
    }

    public TrSq2(AttrTrSq2 attr, Coord coord) {
        super(attr, coord,
                   (coord.y%6)*6+(coord.x%6) // 0..35
             );
    }

    @Override
    public  AttrTrSq2 getAttr() {
        return (AttrTrSq2) super.getAttr();
    }

    @Override
    public List<Coord> getCoordsNeighbor() {
        List<Coord> neighborCoord = new ArrayList<>(getAttr().getNeighborNumber(getDirection()));

        // определяю координаты соседей
        switch (direction) {
        case 0:
        case 21:
            neighborCoord.add(new Coord(coord.x+1, coord.y-2));
            neighborCoord.add(new Coord(coord.x-1, coord.y-1));
            neighborCoord.add(new Coord(coord.x  , coord.y-1));
            neighborCoord.add(new Coord(coord.x+1, coord.y-1));
            neighborCoord.add(new Coord(coord.x-1, coord.y  ));
            neighborCoord.add(new Coord(coord.x+1, coord.y  ));
            neighborCoord.add(new Coord(coord.x-1, coord.y+1));
            neighborCoord.add(new Coord(coord.x  , coord.y+1));
            neighborCoord.add(new Coord(coord.x+1, coord.y+1));
            neighborCoord.add(new Coord(coord.x+2, coord.y+1));
            neighborCoord.add(new Coord(coord.x-1, coord.y+2));
            neighborCoord.add(new Coord(coord.x  , coord.y+2));
            break;
        case 1:
        case 22:
            neighborCoord.add(new Coord(coord.x  , coord.y-2));
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
        case 23:
            neighborCoord.add(new Coord(coord.x-1, coord.y-2));
            neighborCoord.add(new Coord(coord.x+1, coord.y-1));
            neighborCoord.add(new Coord(coord.x  , coord.y-1));
            neighborCoord.add(new Coord(coord.x+2, coord.y-1));
            neighborCoord.add(new Coord(coord.x-1, coord.y  ));
            neighborCoord.add(new Coord(coord.x+1, coord.y  ));
            neighborCoord.add(new Coord(coord.x-1, coord.y+1));
            neighborCoord.add(new Coord(coord.x  , coord.y+1));
            neighborCoord.add(new Coord(coord.x+1, coord.y+1));
            break;
        case 3:
        case 18:
            neighborCoord.add(new Coord(coord.x-1, coord.y-1));
            neighborCoord.add(new Coord(coord.x  , coord.y-1));
            neighborCoord.add(new Coord(coord.x+1, coord.y-1));
            neighborCoord.add(new Coord(coord.x+2, coord.y-1));
            neighborCoord.add(new Coord(coord.x-1, coord.y  ));
            neighborCoord.add(new Coord(coord.x+1, coord.y  ));
            neighborCoord.add(new Coord(coord.x+2, coord.y  ));
            neighborCoord.add(new Coord(coord.x-2, coord.y+1));
            neighborCoord.add(new Coord(coord.x-1, coord.y+1));
            neighborCoord.add(new Coord(coord.x  , coord.y+1));
            neighborCoord.add(new Coord(coord.x+1, coord.y+1));
            neighborCoord.add(new Coord(coord.x+1, coord.y+2));
            break;
        case 4:
        case 19:
            neighborCoord.add(new Coord(coord.x  , coord.y-1));
            neighborCoord.add(new Coord(coord.x+1, coord.y-1));
            neighborCoord.add(new Coord(coord.x-1, coord.y  ));
            neighborCoord.add(new Coord(coord.x+1, coord.y  ));
            neighborCoord.add(new Coord(coord.x-1, coord.y+1));
            neighborCoord.add(new Coord(coord.x  , coord.y+1));
            neighborCoord.add(new Coord(coord.x+1, coord.y+1));
            neighborCoord.add(new Coord(coord.x  , coord.y+2));
            neighborCoord.add(new Coord(coord.x+1, coord.y+2));
            break;
        case 5:
        case 20:
            neighborCoord.add(new Coord(coord.x-1, coord.y-1));
            neighborCoord.add(new Coord(coord.x  , coord.y-1));
            neighborCoord.add(new Coord(coord.x+1, coord.y-1));
            neighborCoord.add(new Coord(coord.x-2, coord.y  ));
            neighborCoord.add(new Coord(coord.x-1, coord.y  ));
            neighborCoord.add(new Coord(coord.x+1, coord.y  ));
            neighborCoord.add(new Coord(coord.x-1, coord.y+1));
            neighborCoord.add(new Coord(coord.x  , coord.y+1));
            neighborCoord.add(new Coord(coord.x  , coord.y+2));
            break;
        case 6:
        case 27:
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
        case 7:
        case 28:
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
            neighborCoord.add(new Coord(coord.x+3, coord.y+1));
            break;
        case 8:
        case 29:
            neighborCoord.add(new Coord(coord.x-1, coord.y-3));
            neighborCoord.add(new Coord(coord.x  , coord.y-2));
            neighborCoord.add(new Coord(coord.x-2, coord.y-1));
            neighborCoord.add(new Coord(coord.x-1, coord.y-1));
            neighborCoord.add(new Coord(coord.x  , coord.y-1));
            neighborCoord.add(new Coord(coord.x+1, coord.y-1));
            neighborCoord.add(new Coord(coord.x-2, coord.y  ));
            neighborCoord.add(new Coord(coord.x-1, coord.y  ));
            neighborCoord.add(new Coord(coord.x+1, coord.y  ));
            break;
        case 9:
        case 24:
            neighborCoord.add(new Coord(coord.x-1, coord.y-1));
            neighborCoord.add(new Coord(coord.x  , coord.y-1));
            neighborCoord.add(new Coord(coord.x+1, coord.y-1));
            neighborCoord.add(new Coord(coord.x-2, coord.y  ));
            neighborCoord.add(new Coord(coord.x-1, coord.y  ));
            neighborCoord.add(new Coord(coord.x+1, coord.y  ));
            neighborCoord.add(new Coord(coord.x-1, coord.y+1));
            neighborCoord.add(new Coord(coord.x  , coord.y+1));
            neighborCoord.add(new Coord(coord.x+1, coord.y+1));
            break;
        case 10:
        case 25:
            neighborCoord.add(new Coord(coord.x-1, coord.y-1));
            neighborCoord.add(new Coord(coord.x  , coord.y-1));
            neighborCoord.add(new Coord(coord.x+1, coord.y-1));
            neighborCoord.add(new Coord(coord.x-1, coord.y  ));
            neighborCoord.add(new Coord(coord.x+1, coord.y  ));
            neighborCoord.add(new Coord(coord.x-1, coord.y+1));
            neighborCoord.add(new Coord(coord.x  , coord.y+1));
            neighborCoord.add(new Coord(coord.x+1, coord.y+1));
            neighborCoord.add(new Coord(coord.x-1, coord.y+2));
            neighborCoord.add(new Coord(coord.x  , coord.y+2));
            neighborCoord.add(new Coord(coord.x+1, coord.y+2));
            neighborCoord.add(new Coord(coord.x+1, coord.y+3));
            break;
        case 11:
        case 26:
            neighborCoord.add(new Coord(coord.x  , coord.y-2));
            neighborCoord.add(new Coord(coord.x+1, coord.y-2));
            neighborCoord.add(new Coord(coord.x-1, coord.y-1));
            neighborCoord.add(new Coord(coord.x  , coord.y-1));
            neighborCoord.add(new Coord(coord.x+1, coord.y-1));
            neighborCoord.add(new Coord(coord.x-1, coord.y  ));
            neighborCoord.add(new Coord(coord.x+1, coord.y  ));
            neighborCoord.add(new Coord(coord.x  , coord.y+1));
            neighborCoord.add(new Coord(coord.x+1, coord.y+1));
            break;
        case 12:
        case 33:
            neighborCoord.add(new Coord(coord.x  , coord.y-2));
            neighborCoord.add(new Coord(coord.x-1, coord.y-1));
            neighborCoord.add(new Coord(coord.x  , coord.y-1));
            neighborCoord.add(new Coord(coord.x+1, coord.y-1));
            neighborCoord.add(new Coord(coord.x-1, coord.y  ));
            neighborCoord.add(new Coord(coord.x+1, coord.y  ));
            neighborCoord.add(new Coord(coord.x+2, coord.y  ));
            neighborCoord.add(new Coord(coord.x-1, coord.y+1));
            neighborCoord.add(new Coord(coord.x  , coord.y+1));
            break;
        case 13:
        case 34:
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
        case 14:
        case 35:
            neighborCoord.add(new Coord(coord.x-2, coord.y-1));
            neighborCoord.add(new Coord(coord.x-1, coord.y-1));
            neighborCoord.add(new Coord(coord.x+1, coord.y-1));
            neighborCoord.add(new Coord(coord.x-2, coord.y  ));
            neighborCoord.add(new Coord(coord.x-1, coord.y  ));
            neighborCoord.add(new Coord(coord.x+1, coord.y  ));
            neighborCoord.add(new Coord(coord.x+2, coord.y  ));
            neighborCoord.add(new Coord(coord.x-2, coord.y+1));
            neighborCoord.add(new Coord(coord.x-1, coord.y+1));
            neighborCoord.add(new Coord(coord.x  , coord.y+1));
            neighborCoord.add(new Coord(coord.x+1, coord.y+1));
            neighborCoord.add(new Coord(coord.x  , coord.y+2));
            break;
        case 15:
        case 30:
            neighborCoord.add(new Coord(coord.x-2, coord.y-1));
            neighborCoord.add(new Coord(coord.x  , coord.y-1));
            neighborCoord.add(new Coord(coord.x+1, coord.y-1));
            neighborCoord.add(new Coord(coord.x-1, coord.y  ));
            neighborCoord.add(new Coord(coord.x+1, coord.y  ));
            neighborCoord.add(new Coord(coord.x-1, coord.y+1));
            neighborCoord.add(new Coord(coord.x  , coord.y+1));
            neighborCoord.add(new Coord(coord.x+1, coord.y+1));
            neighborCoord.add(new Coord(coord.x-1, coord.y+2));
            break;
        case 16:
        case 31:
            neighborCoord.add(new Coord(coord.x-1, coord.y-2));
            neighborCoord.add(new Coord(coord.x  , coord.y-2));
            neighborCoord.add(new Coord(coord.x-3, coord.y-1));
            neighborCoord.add(new Coord(coord.x-1, coord.y-1));
            neighborCoord.add(new Coord(coord.x  , coord.y-1));
            neighborCoord.add(new Coord(coord.x-2, coord.y  ));
            neighborCoord.add(new Coord(coord.x-1, coord.y  ));
            neighborCoord.add(new Coord(coord.x-1, coord.y+1));
            neighborCoord.add(new Coord(coord.x  , coord.y+1));
            break;
        case 17:
        case 32:
            neighborCoord.add(new Coord(coord.x-1, coord.y-2));
            neighborCoord.add(new Coord(coord.x  , coord.y-2));
            neighborCoord.add(new Coord(coord.x+1, coord.y-2));
            neighborCoord.add(new Coord(coord.x-1, coord.y-1));
            neighborCoord.add(new Coord(coord.x  , coord.y-1));
            neighborCoord.add(new Coord(coord.x+1, coord.y-1));
            neighborCoord.add(new Coord(coord.x+1, coord.y  ));
            neighborCoord.add(new Coord(coord.x+2, coord.y  ));
            neighborCoord.add(new Coord(coord.x-1, coord.y+1));
            neighborCoord.add(new Coord(coord.x  , coord.y+1));
            neighborCoord.add(new Coord(coord.x+1, coord.y+1));
            neighborCoord.add(new Coord(coord.x  , coord.y+2));
            break;
        }

        return neighborCoord;
    }

    private PointDouble getOffset() {
        AttrTrSq2 attr = getAttr();
        double a = attr.getA();
        double b = attr.getB();
        double h = attr.getH();

        double oX = 0; // offset X
        double oY = 0; // offset Y

        switch (direction) {
        case  0: case  1: case  2: case  6: case  7: case  8: case 12: case 13: case 14: oX = (h*2+a*3)*(coord.x/6) + h+b;
                                                                                         oY = (h*2+a*3)*(coord.y/6) + h;       break;
        case  3: case  4: case  5: case  9: case 10: case 11: case 15: case 16: case 17: oX = (h*2+a*3)*(coord.x/6) + h*2+a+b;
                                                                                         oY = (h*2+a*3)*(coord.y/6) + h+b;     break;
        case 18: case 19: case 20: case 24: case 25: case 26: case 30: case 31: case 32: oX = (h*2+a*3)*(coord.x/6) + h;
                                                                                         oY = (h*2+a*3)*(coord.y/6) + h*2+a*2; break;
        case 21: case 22: case 23: case 27: case 28: case 29: case 33: case 34: case 35: oX = (h*2+a*3)*(coord.x/6) + h*2+a*2;
                                                                                         oY = (h*2+a*3)*(coord.y/6) + h*2+a+b; break;
        }
        return new PointDouble(oX, oY);
    }

    @Override
    protected void calcRegion() {
        AttrTrSq2 attr = getAttr();
        double a = attr.getA();
        double b = attr.getB();
        double h = attr.getH();

        PointDouble o = getOffset();
        switch (direction) {
        case 0: case 21:
            region.setPoint(0, o.x - b  , o.y - h  );
            region.setPoint(3, o.x - b-h, o.y + b-h);
            region.setPoint(2, o.x - h  , o.y + b  );
            region.setPoint(1, o.x      , o.y      );
            break;
        case 1: case 22:
            region.setPoint(0, o.x + b  , o.y - h  );
            region.setPoint(2, o.x - b  , o.y - h  );
            region.setPoint(1, o.x      , o.y      );
            break;
        case 2: case 23:
            region.setPoint(0, o.x + a+b, o.y - h  );
            region.setPoint(2, o.x + b  , o.y - h  );
            region.setPoint(1, o.x + a  , o.y      );
            break;
        case 3: case 18:
            region.setPoint(0, o.x - h+b, o.y - b-h);
            region.setPoint(3, o.x - h  , o.y - b  );
            region.setPoint(2, o.x      , o.y      );
            region.setPoint(1, o.x + b  , o.y - h  );
            break;
        case 4: case 19:
            region.setPoint(0, o.x + b  , o.y - h  );
            region.setPoint(1, o.x + a  , o.y      );
            region.setPoint(2, o.x      , o.y      );
            break;
        case 5: case 20:
            region.setPoint(0, o.x + a+b, o.y - h  );
            region.setPoint(2, o.x + b  , o.y - h  );
            region.setPoint(1, o.x + a  , o.y      );
            break;
        case 6: case 27:
            region.setPoint(0, o.x      , o.y      );
            region.setPoint(2, o.x - h  , o.y + b  );
            region.setPoint(1, o.x      , o.y + a  );
            break;
        case 7: case 28:
            region.setPoint(0, o.x + a  , o.y      );
            region.setPoint(3, o.x      , o.y      );
            region.setPoint(2, o.x      , o.y + a  );
            region.setPoint(1, o.x + a  , o.y + a  );
            break;
        case 8: case 29:
            region.setPoint(0, o.x + b  , o.y - h  );
            region.setPoint(1, o.x + a  , o.y      );
            region.setPoint(2, o.x      , o.y      );
            break;
        case 9: case 24:
            region.setPoint(0, o.x - h  , o.y - b  );
            region.setPoint(1, o.x      , o.y      );
            region.setPoint(2, o.x - h  , o.y + b  );
            break;
        case 10: case 25:
            region.setPoint(0, o.x + a  , o.y      );
            region.setPoint(3, o.x      , o.y      );
            region.setPoint(2, o.x      , o.y + a  );
            region.setPoint(1, o.x + a  , o.y + a  );
            break;
        case 11: case 26:
            region.setPoint(0, o.x + a+b, o.y - h  );
            region.setPoint(1, o.x + a+a, o.y      );
            region.setPoint(2, o.x + a  , o.y      );
            break;
        case 12: case 33:
            region.setPoint(0, o.x - h  , o.y + b  );
            region.setPoint(1, o.x      , o.y + a  );
            region.setPoint(2, o.x - h  , o.y + a+b);
            break;
        case 13: case 34:
            region.setPoint(0, o.x      , o.y + a  );
            region.setPoint(2, o.x - h  , o.y + a+b);
            region.setPoint(1, o.x      , o.y + a+a);
            break;
        case 14: case 35:
            region.setPoint(0, o.x + a  , o.y + a  );
            region.setPoint(3, o.x      , o.y + a  );
            region.setPoint(2, o.x      , o.y + a+a);
            region.setPoint(1, o.x + a  , o.y + a+a);
            break;
        case 15: case 30:
            region.setPoint(0, o.x - h  , o.y + b  );
            region.setPoint(1, o.x      , o.y + a  );
            region.setPoint(2, o.x - h  , o.y + a+b);
            break;
        case 16: case 31:
            region.setPoint(0, o.x      , o.y      );
            region.setPoint(2, o.x - h  , o.y + b  );
            region.setPoint(1, o.x      , o.y + a  );
            break;
        case 17: case 32:
            region.setPoint(0, o.x + a+a, o.y      );
            region.setPoint(3, o.x + a  , o.y      );
            region.setPoint(2, o.x + a  , o.y + a  );
            region.setPoint(1, o.x + a+a, o.y + a  );
            break;
        }
    }

    @Override
    public RectDouble getRcInner(double borderWidth) {
        AttrTrSq2 attr = getAttr();
        double a = attr.getA();
        double b = attr.getB();
        double h = attr.getH();
        double w = borderWidth/2.;
        double sq = attr.getSq(borderWidth);
        double sq2 = sq/2;
        double wsq2 = w+sq2;

        PointDouble o = getOffset();

        PointDouble center = new PointDouble(); // координата вписанного в фигуру квадрата (не совпадает с центром фигуры)
        switch (direction) {
        case  0: case 21: center.x = o.x -(b+h)/2; center.y = o.y + (b-h)/2; break;
        case  1: case 22: center.x = o.x;          center.y = o.y - h+wsq2;  break;
        case  2: case 23: center.x = o.x + a;      center.y = o.y - h+wsq2;  break;
        case  3: case 18: center.x = o.x +(b-h)/2; center.y = o.y - (b+h)/2; break;
        case  4: case 19: center.x = o.x + b;      center.y = o.y - wsq2;    break;
        case  5: case 20: center.x = o.x + a;      center.y = o.y - h+wsq2;  break;
        case  6: case 27: center.x = o.x - wsq2;   center.y = o.y + b;       break;
        case  7: case 28: center.x = o.x + b;      center.y = o.y + b;       break;
        case  8: case 29: center.x = o.x + b;      center.y = o.y - wsq2;    break;
        case  9: case 24: center.x = o.x - h+wsq2; center.y = o.y;           break;
        case 10: case 25: center.x = o.x + b;      center.y = o.y + b;       break;
        case 11: case 26: center.x = o.x + a+b;    center.y = o.y - wsq2;    break;
        case 12: case 33: center.x = o.x - h+wsq2; center.y = o.y + a;       break;
        case 13: case 34: center.x = o.x - wsq2;   center.y = o.y + a+b;     break;
        case 14: case 35: center.x = o.x + b;      center.y = o.y + a+b;     break;
        case 15: case 30: center.x = o.x - h+wsq2; center.y = o.y + a;       break;
        case 16: case 31: center.x = o.x - wsq2;   center.y = o.y + b;       break;
        case 17: case 32: center.x = o.x + a+b;    center.y = o.y + b;       break;
        }

        return new RectDouble(
            center.x - sq2,
            center.y - sq2,
            sq, sq);
    }

    @Override
    public int getShiftPointBorderIndex() {
        switch (direction) {
        case 1: case  2: case  5: case 20: case 22: case 23:
        case 6: case 13: case 16: case 27: case 31: case 34:
            return 1;
        }
        return 2;
    }

}