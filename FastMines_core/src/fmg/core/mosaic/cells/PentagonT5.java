////////////////////////////////////////////////////////////////////////////////
//                               FastMines project
// file name: "PentagonT5.java"
//
// Реализация класса PentagonT5 - 5-ти угольник, тип №5
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
 * Пятиугольник. Тип №5
 * @see BaseCell
 **/
public class PentagonT5 extends BaseCell {

    public static class AttrPentagonT5 extends BaseAttribute {

        @Override
        public SizeDouble getSize(Matrisize sizeField) {
            double a = getA();
            double h = getH();
            SizeDouble result = new SizeDouble(
                a*3.5 +
                a*2.0*((sizeField.m+13)/14) +
                a    *((sizeField.m+12)/14) +
                a*1.5*((sizeField.m+11)/14) +
                a*2.0*((sizeField.m+10)/14) +
                a    *((sizeField.m+ 9)/14) +
                a*1.5*((sizeField.m+ 8)/14) +
                a*2.0*((sizeField.m+ 7)/14) +
                a    *((sizeField.m+ 6)/14) +
                a*1.5*((sizeField.m+ 5)/14) +
                a*2.0*((sizeField.m+ 4)/14) +
                a    *((sizeField.m+ 3)/14) +
                a*2.0*((sizeField.m+ 2)/14) +
                a    *((sizeField.m+ 1)/14) +
                a*1.5*((sizeField.m+ 0)/14),
                h*5  +
                h*2  *((sizeField.n+ 5)/ 6) +
                h*2  *((sizeField.n+ 4)/ 6) +
                h*2  *((sizeField.n+ 3)/ 6) +
                h*3  *((sizeField.n+ 2)/ 6) +
                h*2  *((sizeField.n+ 1)/ 6) +
                h*3  *((sizeField.n+ 0)/ 6));

            // когда размер поля мал...
            if (sizeField.m < 14) { // ...нужно вычислять не только по общей формуле, а и убрать остатки снизу..
                if ((sizeField.n & 1) == 0) {
                    if (sizeField.m < 11) result.height -= h;
                    if (sizeField.m <  8) result.height -= h;
                    if (sizeField.m <  5) result.height -= h;
                    if (sizeField.m <  2) result.height -= h;
                } else {
                    if (sizeField.m < 10) result.height -= h;
                    if (sizeField.m <  7) result.height -= h;
                    if (sizeField.m <  4) result.height -= h;
                }
                if ((sizeField.n+5)%6 == 0) // y == 1 7 13 ..
                    if (sizeField.m < 13) result.height -= h;
            }
            if (sizeField.n < 5) { // .. и справа
                switch (sizeField.n) {
                case 1:
                    switch (sizeField.m % 14) {
                    default: result.width -= 3*a;     break;
                    case 12: result.width -= 3*a+a/2; break;
                    case 13: result.width -= 3*a-a/2; break;
                    } break;
                case 2:
                    switch (sizeField.m % 14) {
                    default: result.width -= 3*a;     break;
                    case 12: result.width -= 3*a+a/2; break;
                    case 13: result.width -= 3*a-a/2; break;
                    case  0: result.width -= 1.5*a;   break;
                    } break;
                case 3:
                    switch (sizeField.m % 14) {
                    default: result.width -= 1.5*a; break;
                    case 12: result.width -=   2*a; break;
                    } break;
                case 4:
                    switch (sizeField.m % 14) {
                    default: result.width -= 1.5*a; break;
                    case 12: result.width -=   2*a; break;
                    case 13: result.width -=   1*a; break;
                    } break;
                }
            }

            return result;
        }

        @Override
        public int getNeighborNumber(int direction) { return 8; }
        @Override
        public int getVertexNumber(int direction) { return 5; }
        @Override
        public double getVertexIntersection() { return 3.6; } // (3+3+3+3+6)/5.
        @Override
        public Size getDirectionSizeField() { return new Size(14, 6); }
        @Override
        protected double getA() { return 2*Math.sqrt(getArea()/SQRT147); }
        protected double getH() { return getA()*SQRT3/2; }
        @Override
        public double getSq(double borderWidth) {
            double w = borderWidth/2.;
            return (getA()*2*SQRT3-4*w)/(SQRT3+1);
        }

        @Override
        public int getMaxBackgroundFillModeValue() {
            return super.getMaxBackgroundFillModeValue()+2;
//          return 1;
        }
    }

    public PentagonT5(AttrPentagonT5 attr, Coord coord) {
        super(attr, coord,
                   (coord.y%6)*14 + (coord.x%14) // 0..83
             );
    }

    @Override
    public AttrPentagonT5 getAttr() {
        return (AttrPentagonT5) super.getAttr();
    }

    @Override
    protected List<Coord> getCoordsNeighbor() {
        List<Coord> neighborCoord = new ArrayList<>(getAttr().getNeighborNumber(getDirection()));

        // определяю координаты соседей
        switch (direction) {
        case 0:
            neighborCoord.add(new Coord(coord.x-2, coord.y-2));
            neighborCoord.add(new Coord(coord.x-3, coord.y-1));
            neighborCoord.add(new Coord(coord.x-2, coord.y-1));
            neighborCoord.add(new Coord(coord.x+1, coord.y  ));
            neighborCoord.add(new Coord(coord.x+2, coord.y  ));
            neighborCoord.add(new Coord(coord.x  , coord.y+1));
            neighborCoord.add(new Coord(coord.x+1, coord.y+1));
            neighborCoord.add(new Coord(coord.x+2, coord.y+1));
            break;
        case 3: case 6: case 9:
            neighborCoord.add(new Coord(coord.x-2, coord.y-1));
            neighborCoord.add(new Coord(coord.x-1, coord.y  ));
            neighborCoord.add(new Coord(coord.x+1, coord.y  ));
            neighborCoord.add(new Coord(coord.x+2, coord.y  ));
            neighborCoord.add(new Coord(coord.x-1, coord.y+1));
            neighborCoord.add(new Coord(coord.x  , coord.y+1));
            neighborCoord.add(new Coord(coord.x+1, coord.y+1));
            neighborCoord.add(new Coord(coord.x+2, coord.y+1));
            break;
        case 12:
            neighborCoord.add(new Coord(coord.x-2, coord.y-1));
            neighborCoord.add(new Coord(coord.x-1, coord.y  ));
            neighborCoord.add(new Coord(coord.x+1, coord.y  ));
            neighborCoord.add(new Coord(coord.x-1, coord.y+1));
            neighborCoord.add(new Coord(coord.x  , coord.y+1));
            neighborCoord.add(new Coord(coord.x+1, coord.y+1));
            neighborCoord.add(new Coord(coord.x  , coord.y+2));
            neighborCoord.add(new Coord(coord.x+1, coord.y+2));
            break;
        case 28:
            neighborCoord.add(new Coord(coord.x-1, coord.y-1));
            neighborCoord.add(new Coord(coord.x+1, coord.y-1));
            neighborCoord.add(new Coord(coord.x-1, coord.y  ));
            neighborCoord.add(new Coord(coord.x+1, coord.y  ));
            neighborCoord.add(new Coord(coord.x+2, coord.y  ));
            neighborCoord.add(new Coord(coord.x  , coord.y+1));
            neighborCoord.add(new Coord(coord.x+1, coord.y+1));
            neighborCoord.add(new Coord(coord.x+2, coord.y+1));
            break;
        case 31: case 34: case 37: case 56: case 59: case 62:
            neighborCoord.add(new Coord(coord.x+1, coord.y-1));
            neighborCoord.add(new Coord(coord.x-1, coord.y  ));
            neighborCoord.add(new Coord(coord.x+1, coord.y  ));
            neighborCoord.add(new Coord(coord.x+2, coord.y  ));
            neighborCoord.add(new Coord(coord.x-1, coord.y+1));
            neighborCoord.add(new Coord(coord.x  , coord.y+1));
            neighborCoord.add(new Coord(coord.x+1, coord.y+1));
            neighborCoord.add(new Coord(coord.x+2, coord.y+1));
            break;
        case 54:
            neighborCoord.add(new Coord(coord.x-1, coord.y-1));
            neighborCoord.add(new Coord(coord.x  , coord.y-1));
            neighborCoord.add(new Coord(coord.x-1, coord.y  ));
            neighborCoord.add(new Coord(coord.x+1, coord.y  ));
            neighborCoord.add(new Coord(coord.x-1, coord.y+1));
            neighborCoord.add(new Coord(coord.x  , coord.y+1));
            neighborCoord.add(new Coord(coord.x+1, coord.y+1));
            neighborCoord.add(new Coord(coord.x+1, coord.y+2));
            break;
        case 65:
            neighborCoord.add(new Coord(coord.x+1, coord.y-1));
            neighborCoord.add(new Coord(coord.x-1, coord.y  ));
            neighborCoord.add(new Coord(coord.x+1, coord.y  ));
            neighborCoord.add(new Coord(coord.x-1, coord.y+1));
            neighborCoord.add(new Coord(coord.x  , coord.y+1));
            neighborCoord.add(new Coord(coord.x+1, coord.y+1));
            neighborCoord.add(new Coord(coord.x+2, coord.y+1));
            neighborCoord.add(new Coord(coord.x+3, coord.y+1));
            break;
        case 1:
            neighborCoord.add(new Coord(coord.x-3, coord.y-2));
            neighborCoord.add(new Coord(coord.x-2, coord.y-1));
            neighborCoord.add(new Coord(coord.x-1, coord.y-1));
            neighborCoord.add(new Coord(coord.x-1, coord.y  ));
            neighborCoord.add(new Coord(coord.x+1, coord.y  ));
            neighborCoord.add(new Coord(coord.x-1, coord.y+1));
            neighborCoord.add(new Coord(coord.x  , coord.y+1));
            neighborCoord.add(new Coord(coord.x+1, coord.y+1));
            break;
        case 4: case 7: case 10:
            neighborCoord.add(new Coord(coord.x-3, coord.y-1));
            neighborCoord.add(new Coord(coord.x-2, coord.y-1));
            neighborCoord.add(new Coord(coord.x-1, coord.y-1));
            neighborCoord.add(new Coord(coord.x-1, coord.y  ));
            neighborCoord.add(new Coord(coord.x+1, coord.y  ));
            neighborCoord.add(new Coord(coord.x-1, coord.y+1));
            neighborCoord.add(new Coord(coord.x  , coord.y+1));
            neighborCoord.add(new Coord(coord.x+1, coord.y+1));
            break;
        case 13:
            neighborCoord.add(new Coord(coord.x-3, coord.y-1));
            neighborCoord.add(new Coord(coord.x-2, coord.y-1));
            neighborCoord.add(new Coord(coord.x-1, coord.y  ));
            neighborCoord.add(new Coord(coord.x-1, coord.y+1));
            neighborCoord.add(new Coord(coord.x  , coord.y+1));
            neighborCoord.add(new Coord(coord.x+1, coord.y+1));
            neighborCoord.add(new Coord(coord.x-1, coord.y+2));
            neighborCoord.add(new Coord(coord.x  , coord.y+2));
            break;
        case 29: case 32: case 35: case 38: case 57: case 60: case 63:
            neighborCoord.add(new Coord(coord.x  , coord.y-1));
            neighborCoord.add(new Coord(coord.x+1, coord.y-1));
            neighborCoord.add(new Coord(coord.x+2, coord.y-1));
            neighborCoord.add(new Coord(coord.x-1, coord.y  ));
            neighborCoord.add(new Coord(coord.x+1, coord.y  ));
            neighborCoord.add(new Coord(coord.x-1, coord.y+1));
            neighborCoord.add(new Coord(coord.x  , coord.y+1));
            neighborCoord.add(new Coord(coord.x+1, coord.y+1));
            break;
        case 55:
            neighborCoord.add(new Coord(coord.x-1, coord.y-1));
            neighborCoord.add(new Coord(coord.x  , coord.y-1));
            neighborCoord.add(new Coord(coord.x-1, coord.y  ));
            neighborCoord.add(new Coord(coord.x+1, coord.y  ));
            neighborCoord.add(new Coord(coord.x-2, coord.y+1));
            neighborCoord.add(new Coord(coord.x-1, coord.y+1));
            neighborCoord.add(new Coord(coord.x  , coord.y+1));
            neighborCoord.add(new Coord(coord.x  , coord.y+2));
            break;
        case 66:
            neighborCoord.add(new Coord(coord.x  , coord.y-1));
            neighborCoord.add(new Coord(coord.x+1, coord.y-1));
            neighborCoord.add(new Coord(coord.x-1, coord.y  ));
            neighborCoord.add(new Coord(coord.x+1, coord.y  ));
            neighborCoord.add(new Coord(coord.x-1, coord.y+1));
            neighborCoord.add(new Coord(coord.x  , coord.y+1));
            neighborCoord.add(new Coord(coord.x+1, coord.y+1));
            neighborCoord.add(new Coord(coord.x+2, coord.y+1));
            break;
        case 2: case 5: case 8: case 11:
            neighborCoord.add(new Coord(coord.x-2, coord.y-1));
            neighborCoord.add(new Coord(coord.x-1, coord.y-1));
            neighborCoord.add(new Coord(coord.x-2, coord.y  ));
            neighborCoord.add(new Coord(coord.x-1, coord.y  ));
            neighborCoord.add(new Coord(coord.x+1, coord.y  ));
            neighborCoord.add(new Coord(coord.x-2, coord.y+1));
            neighborCoord.add(new Coord(coord.x-1, coord.y+1));
            neighborCoord.add(new Coord(coord.x  , coord.y+1));
            break;
        case 27:
            neighborCoord.add(new Coord(coord.x-1, coord.y-1));
            neighborCoord.add(new Coord(coord.x  , coord.y-1));
            neighborCoord.add(new Coord(coord.x-1, coord.y  ));
            neighborCoord.add(new Coord(coord.x+1, coord.y  ));
            neighborCoord.add(new Coord(coord.x+2, coord.y  ));
            neighborCoord.add(new Coord(coord.x-1, coord.y+1));
            neighborCoord.add(new Coord(coord.x  , coord.y+1));
            neighborCoord.add(new Coord(coord.x+1, coord.y+1));
            break;
        case 30: case 33: case 36: case 58: case 61: case 64:
            neighborCoord.add(new Coord(coord.x+1, coord.y-1));
            neighborCoord.add(new Coord(coord.x+2, coord.y-1));
            neighborCoord.add(new Coord(coord.x-2, coord.y  ));
            neighborCoord.add(new Coord(coord.x-1, coord.y  ));
            neighborCoord.add(new Coord(coord.x+1, coord.y  ));
            neighborCoord.add(new Coord(coord.x-2, coord.y+1));
            neighborCoord.add(new Coord(coord.x-1, coord.y+1));
            neighborCoord.add(new Coord(coord.x  , coord.y+1));
            break;
        case 39:
            neighborCoord.add(new Coord(coord.x+1, coord.y-1));
            neighborCoord.add(new Coord(coord.x-2, coord.y  ));
            neighborCoord.add(new Coord(coord.x-1, coord.y  ));
            neighborCoord.add(new Coord(coord.x+1, coord.y  ));
            neighborCoord.add(new Coord(coord.x-2, coord.y+1));
            neighborCoord.add(new Coord(coord.x-1, coord.y+1));
            neighborCoord.add(new Coord(coord.x  , coord.y+1));
            neighborCoord.add(new Coord(coord.x+1, coord.y+1));
            break;
        case 69:
            neighborCoord.add(new Coord(coord.x-1, coord.y-1));
            neighborCoord.add(new Coord(coord.x  , coord.y-1));
            neighborCoord.add(new Coord(coord.x+1, coord.y-1));
            neighborCoord.add(new Coord(coord.x+2, coord.y-1));
            neighborCoord.add(new Coord(coord.x-2, coord.y  ));
            neighborCoord.add(new Coord(coord.x-1, coord.y  ));
            neighborCoord.add(new Coord(coord.x+1, coord.y  ));
            neighborCoord.add(new Coord(coord.x  , coord.y+1));
            break;
        case 82:
            neighborCoord.add(new Coord(coord.x-3, coord.y-1));
            neighborCoord.add(new Coord(coord.x-2, coord.y-1));
            neighborCoord.add(new Coord(coord.x-1, coord.y-1));
            neighborCoord.add(new Coord(coord.x  , coord.y-1));
            neighborCoord.add(new Coord(coord.x-3, coord.y  ));
            neighborCoord.add(new Coord(coord.x-2, coord.y  ));
            neighborCoord.add(new Coord(coord.x-1, coord.y  ));
            neighborCoord.add(new Coord(coord.x+2, coord.y+1));
            break;
        case 14:
            neighborCoord.add(new Coord(coord.x-3, coord.y-2));
            neighborCoord.add(new Coord(coord.x-1, coord.y-1));
            neighborCoord.add(new Coord(coord.x  , coord.y-1));
            neighborCoord.add(new Coord(coord.x+1, coord.y-1));
            neighborCoord.add(new Coord(coord.x+2, coord.y-1));
            neighborCoord.add(new Coord(coord.x-1, coord.y  ));
            neighborCoord.add(new Coord(coord.x+1, coord.y  ));
            neighborCoord.add(new Coord(coord.x+2, coord.y  ));
            break;
        case 17: case 20: case 23: case 45: case 48: case 51:
            neighborCoord.add(new Coord(coord.x  , coord.y-1));
            neighborCoord.add(new Coord(coord.x+1, coord.y-1));
            neighborCoord.add(new Coord(coord.x+2, coord.y-1));
            neighborCoord.add(new Coord(coord.x-1, coord.y  ));
            neighborCoord.add(new Coord(coord.x+1, coord.y  ));
            neighborCoord.add(new Coord(coord.x+2, coord.y  ));
            neighborCoord.add(new Coord(coord.x-2, coord.y+1));
            neighborCoord.add(new Coord(coord.x-1, coord.y+1));
            break;
        case 26:
            neighborCoord.add(new Coord(coord.x  , coord.y-1));
            neighborCoord.add(new Coord(coord.x+1, coord.y-1));
            neighborCoord.add(new Coord(coord.x-1, coord.y  ));
            neighborCoord.add(new Coord(coord.x+1, coord.y  ));
            neighborCoord.add(new Coord(coord.x-2, coord.y+1));
            neighborCoord.add(new Coord(coord.x-1, coord.y+1));
            neighborCoord.add(new Coord(coord.x  , coord.y+1));
            neighborCoord.add(new Coord(coord.x+1, coord.y+1));
            break;
        case 42:
            neighborCoord.add(new Coord(coord.x-1, coord.y-1));
            neighborCoord.add(new Coord(coord.x  , coord.y-1));
            neighborCoord.add(new Coord(coord.x+1, coord.y-1));
            neighborCoord.add(new Coord(coord.x+2, coord.y-1));
            neighborCoord.add(new Coord(coord.x-1, coord.y  ));
            neighborCoord.add(new Coord(coord.x+1, coord.y  ));
            neighborCoord.add(new Coord(coord.x+2, coord.y  ));
            neighborCoord.add(new Coord(coord.x-1, coord.y+1));
            break;
        case 67: case 70: case 73: case 76:
            neighborCoord.add(new Coord(coord.x  , coord.y-1));
            neighborCoord.add(new Coord(coord.x+1, coord.y-1));
            neighborCoord.add(new Coord(coord.x+2, coord.y-1));
            neighborCoord.add(new Coord(coord.x-1, coord.y  ));
            neighborCoord.add(new Coord(coord.x+1, coord.y  ));
            neighborCoord.add(new Coord(coord.x+2, coord.y  ));
            neighborCoord.add(new Coord(coord.x+1, coord.y+1));
            neighborCoord.add(new Coord(coord.x+2, coord.y+1));
            break;
        case 79:
            neighborCoord.add(new Coord(coord.x  , coord.y-1));
            neighborCoord.add(new Coord(coord.x+1, coord.y-1));
            neighborCoord.add(new Coord(coord.x-1, coord.y  ));
            neighborCoord.add(new Coord(coord.x+1, coord.y  ));
            neighborCoord.add(new Coord(coord.x+2, coord.y  ));
            neighborCoord.add(new Coord(coord.x+3, coord.y  ));
            neighborCoord.add(new Coord(coord.x+1, coord.y+1));
            neighborCoord.add(new Coord(coord.x+2, coord.y+1));
            break;
        case 15:
            neighborCoord.add(new Coord(coord.x-1, coord.y-1));
            neighborCoord.add(new Coord(coord.x  , coord.y-1));
            neighborCoord.add(new Coord(coord.x+1, coord.y-1));
            neighborCoord.add(new Coord(coord.x-2, coord.y  ));
            neighborCoord.add(new Coord(coord.x-1, coord.y  ));
            neighborCoord.add(new Coord(coord.x+1, coord.y  ));
            neighborCoord.add(new Coord(coord.x-1, coord.y+1));
            neighborCoord.add(new Coord(coord.x  , coord.y+1));
            break;
        case 18: case 21: case 24: case 43: case 46: case 49: case 52:
            neighborCoord.add(new Coord(coord.x-1, coord.y-1));
            neighborCoord.add(new Coord(coord.x  , coord.y-1));
            neighborCoord.add(new Coord(coord.x+1, coord.y-1));
            neighborCoord.add(new Coord(coord.x-1, coord.y  ));
            neighborCoord.add(new Coord(coord.x+1, coord.y  ));
            neighborCoord.add(new Coord(coord.x-2, coord.y+1));
            neighborCoord.add(new Coord(coord.x-1, coord.y+1));
            neighborCoord.add(new Coord(coord.x  , coord.y+1));
            break;
        case 40:
            neighborCoord.add(new Coord(coord.x  , coord.y-2));
            neighborCoord.add(new Coord(coord.x+1, coord.y-2));
            neighborCoord.add(new Coord(coord.x  , coord.y-1));
            neighborCoord.add(new Coord(coord.x+1, coord.y-1));
            neighborCoord.add(new Coord(coord.x-1, coord.y  ));
            neighborCoord.add(new Coord(coord.x+1, coord.y  ));
            neighborCoord.add(new Coord(coord.x  , coord.y+1));
            neighborCoord.add(new Coord(coord.x+1, coord.y+1));
            break;
        case 68:
            neighborCoord.add(new Coord(coord.x  , coord.y-1));
            neighborCoord.add(new Coord(coord.x+1, coord.y-1));
            neighborCoord.add(new Coord(coord.x-1, coord.y  ));
            neighborCoord.add(new Coord(coord.x+1, coord.y  ));
            neighborCoord.add(new Coord(coord.x  , coord.y+1));
            neighborCoord.add(new Coord(coord.x+1, coord.y+1));
            neighborCoord.add(new Coord(coord.x+2, coord.y+2));
            neighborCoord.add(new Coord(coord.x+3, coord.y+2));
            break;
        case 71: case 74: case 77:
            neighborCoord.add(new Coord(coord.x-1, coord.y-1));
            neighborCoord.add(new Coord(coord.x  , coord.y-1));
            neighborCoord.add(new Coord(coord.x+1, coord.y-1));
            neighborCoord.add(new Coord(coord.x-1, coord.y  ));
            neighborCoord.add(new Coord(coord.x+1, coord.y  ));
            neighborCoord.add(new Coord(coord.x+1, coord.y+1));
            neighborCoord.add(new Coord(coord.x+2, coord.y+1));
            neighborCoord.add(new Coord(coord.x+3, coord.y+1));
            break;
        case 80:
            neighborCoord.add(new Coord(coord.x-1, coord.y-1));
            neighborCoord.add(new Coord(coord.x  , coord.y-1));
            neighborCoord.add(new Coord(coord.x-1, coord.y  ));
            neighborCoord.add(new Coord(coord.x+1, coord.y  ));
            neighborCoord.add(new Coord(coord.x+2, coord.y  ));
            neighborCoord.add(new Coord(coord.x+1, coord.y+1));
            neighborCoord.add(new Coord(coord.x+2, coord.y+1));
            neighborCoord.add(new Coord(coord.x+3, coord.y+1));
            break;
        case 16: case 19: case 22: case 25: case 44: case 47: case 50:
            neighborCoord.add(new Coord(coord.x-2, coord.y-1));
            neighborCoord.add(new Coord(coord.x-1, coord.y-1));
            neighborCoord.add(new Coord(coord.x  , coord.y-1));
            neighborCoord.add(new Coord(coord.x+1, coord.y-1));
            neighborCoord.add(new Coord(coord.x-2, coord.y  ));
            neighborCoord.add(new Coord(coord.x-1, coord.y  ));
            neighborCoord.add(new Coord(coord.x+1, coord.y  ));
            neighborCoord.add(new Coord(coord.x-1, coord.y+1));
            break;
        case 41:
            neighborCoord.add(new Coord(coord.x-1, coord.y-2));
            neighborCoord.add(new Coord(coord.x  , coord.y-2));
            neighborCoord.add(new Coord(coord.x-1, coord.y-1));
            neighborCoord.add(new Coord(coord.x  , coord.y-1));
            neighborCoord.add(new Coord(coord.x-1, coord.y  ));
            neighborCoord.add(new Coord(coord.x+1, coord.y  ));
            neighborCoord.add(new Coord(coord.x  , coord.y+1));
            neighborCoord.add(new Coord(coord.x+1, coord.y+1));
            break;
        case 53:
            neighborCoord.add(new Coord(coord.x-2, coord.y-1));
            neighborCoord.add(new Coord(coord.x-1, coord.y-1));
            neighborCoord.add(new Coord(coord.x  , coord.y-1));
            neighborCoord.add(new Coord(coord.x-2, coord.y  ));
            neighborCoord.add(new Coord(coord.x-1, coord.y  ));
            neighborCoord.add(new Coord(coord.x+1, coord.y  ));
            neighborCoord.add(new Coord(coord.x-1, coord.y+1));
            neighborCoord.add(new Coord(coord.x  , coord.y+1));
            break;
        case 83:
            neighborCoord.add(new Coord(coord.x-1, coord.y-2));
            neighborCoord.add(new Coord(coord.x  , coord.y-2));
            neighborCoord.add(new Coord(coord.x-2, coord.y-1));
            neighborCoord.add(new Coord(coord.x-1, coord.y-1));
            neighborCoord.add(new Coord(coord.x  , coord.y-1));
            neighborCoord.add(new Coord(coord.x+1, coord.y-1));
            neighborCoord.add(new Coord(coord.x+1, coord.y  ));
            neighborCoord.add(new Coord(coord.x+2, coord.y+1));
            break;
        case 72: case 75: case 78:
            neighborCoord.add(new Coord(coord.x-2, coord.y-1));
            neighborCoord.add(new Coord(coord.x-1, coord.y-1));
            neighborCoord.add(new Coord(coord.x  , coord.y-1));
            neighborCoord.add(new Coord(coord.x+1, coord.y-1));
            neighborCoord.add(new Coord(coord.x-2, coord.y  ));
            neighborCoord.add(new Coord(coord.x-1, coord.y  ));
            neighborCoord.add(new Coord(coord.x+1, coord.y  ));
            neighborCoord.add(new Coord(coord.x+2, coord.y+1));
            break;
        case 81:
            neighborCoord.add(new Coord(coord.x-2, coord.y-1));
            neighborCoord.add(new Coord(coord.x-1, coord.y-1));
            neighborCoord.add(new Coord(coord.x-2, coord.y  ));
            neighborCoord.add(new Coord(coord.x-1, coord.y  ));
            neighborCoord.add(new Coord(coord.x+1, coord.y  ));
            neighborCoord.add(new Coord(coord.x+2, coord.y+1));
            neighborCoord.add(new Coord(coord.x+3, coord.y+1));
            neighborCoord.add(new Coord(coord.x+3, coord.y+2));
            break;
        }

        return neighborCoord;
    }

    @Override
    protected void calcRegion() {
        AttrPentagonT5 attr = getAttr();
        double a = attr.getA();
        double h = attr.getH();

        // определение координат точек фигуры
        double oX = a*21*(coord.x/14); // offset X
        double oY = h*14*(coord.y/6);  // offset Y
        switch (direction) {
        case  0: case  1: case  2: case 14: case 15: case 16: oX += a* 2.5; oY += h* 3; break;
        case  3: case  4: case  5: case 17: case 18: case 19: oX += a* 7.0; oY += h* 4; break;
        case  6: case  7: case  8: case 20: case 21: case 22: oX += a*11.5; oY += h* 5; break;
        case  9: case 10: case 11: case 23: case 24: case 25: oX += a*16.0; oY += h* 6; break;
        case 12: case 13: case 27: case 26: case 40: case 41: oX += a*20.5; oY += h* 7; break;
        case 28: case 29: case 30: case 42: case 43: case 44: oX += a* 4.0; oY += h* 8; break;
        case 31: case 32: case 33: case 45: case 46: case 47: oX += a* 8.5; oY += h* 9; break;
        case 34: case 35: case 36: case 48: case 49: case 50: oX += a*13.0; oY += h*10; break;
        case 37: case 38: case 39: case 51: case 52: case 53: oX += a*17.5; oY += h*11; break;
        case 54: case 55: case 69: case 67: case 68: case 83: oX += a*22.0; oY += h*12; break;
        case 56: case 57: case 58: case 70: case 71: case 72: oX += a* 5.5; oY += h*13; break;
        case 59: case 60: case 61: case 73: case 74: case 75: oX += a*10.0; oY += h*14; break;
        case 62: case 63: case 64: case 76: case 77: case 78: oX += a*14.5; oY += h*15; break;
        case 65: case 66: case 82: case 79: case 80: case 81: oX += a*19.0; oY += h*16; break;
        }
        switch (direction) {
        case  0: case  3: case  6: case  9: case 12: case 28: case 31: case 34: case 37: case 54: case 56: case 59: case 62: case 65:
            region.setPoint(0, oX - a    , oY - h*2);
            region.setPoint(1, oX        , oY      );
            region.setPoint(2, oX - a*2  , oY      );
            region.setPoint(3, oX - a*2.5, oY - h  );
            region.setPoint(4, oX - a*2  , oY - h*2);
            break;
        case  1: case  4: case  7: case 10: case 13: case 29: case 32: case 35: case 38: case 55: case 57: case 60: case 63: case 66:
            region.setPoint(0, oX + a*0.5, oY - h*3);
            region.setPoint(1, oX + a    , oY - h*2);
            region.setPoint(2, oX        , oY      );
            region.setPoint(3, oX - a    , oY - h*2);
            region.setPoint(4, oX - a*0.5, oY - h*3);
            break;
        case  2: case  5: case  8: case 11: case 27: case 30: case 33: case 36: case 39: case 69: case 58: case 61: case 64: case 82:
            region.setPoint(0, oX + a*2  , oY - h*2);
            region.setPoint(1, oX + a*2.5, oY - h  );
            region.setPoint(2, oX + a*2  , oY      );
            region.setPoint(3, oX        , oY      );
            region.setPoint(4, oX + a    , oY - h*2);
            break;
        case 14: case 17: case 20: case 23: case 26: case 42: case 45: case 48: case 51: case 67: case 70: case 73: case 76: case 79:
            region.setPoint(0, oX        , oY      );
            region.setPoint(1, oX - a    , oY + h*2);
            region.setPoint(2, oX - a*2  , oY + h*2);
            region.setPoint(3, oX - a*2.5, oY + h  );
            region.setPoint(4, oX - a*2  , oY      );
            break;
        case 15: case 18: case 21: case 24: case 40: case 43: case 46: case 49: case 52: case 68: case 71: case 74: case 77: case 80:
            region.setPoint(0, oX        , oY      );
            region.setPoint(1, oX + a    , oY + h*2);
            region.setPoint(2, oX + a*0.5, oY + h*3);
            region.setPoint(3, oX - a*0.5, oY + h*3);
            region.setPoint(4, oX - a    , oY + h*2);
            break;
        case 16: case 19: case 22: case 25: case 41: case 44: case 47: case 50: case 53: case 83: case 72: case 75: case 78: case 81:
            region.setPoint(0, oX + a*2  , oY      );
            region.setPoint(1, oX + a*2.5, oY + h  );
            region.setPoint(2, oX + a*2  , oY + h*2);
            region.setPoint(3, oX + a    , oY + h*2);
            region.setPoint(4, oX        , oY      );
            break;
        }
    }

    @Override
    public RectDouble getRcInner(double borderWidth) {
        AttrPentagonT5 attr = getAttr();
        double a = attr.getA();
        double h = attr.getH();
//      double w = borderWidth/2.;
        double sq  = getAttr().getSq(borderWidth);
        double sq2 = sq/2;

        // определение координат точек фигуры
        double oX = a*21*(coord.x/14); // offset X
        double oY = h*14*(coord.y/6);  // offset Y
        switch (direction) {
        case  0: case  1: case  2: case 14: case 15: case 16: oX += a* 2.5; oY += h* 3; break;
        case  3: case  4: case  5: case 17: case 18: case 19: oX += a* 7.0; oY += h* 4; break;
        case  6: case  7: case  8: case 20: case 21: case 22: oX += a*11.5; oY += h* 5; break;
        case  9: case 10: case 11: case 23: case 24: case 25: oX += a*16.0; oY += h* 6; break;
        case 12: case 13: case 27: case 26: case 40: case 41: oX += a*20.5; oY += h* 7; break;
        case 28: case 29: case 30: case 42: case 43: case 44: oX += a* 4.0; oY += h* 8; break;
        case 31: case 32: case 33: case 45: case 46: case 47: oX += a* 8.5; oY += h* 9; break;
        case 34: case 35: case 36: case 48: case 49: case 50: oX += a*13.0; oY += h*10; break;
        case 37: case 38: case 39: case 51: case 52: case 53: oX += a*17.5; oY += h*11; break;
        case 54: case 55: case 69: case 67: case 68: case 83: oX += a*22.0; oY += h*12; break;
        case 56: case 57: case 58: case 70: case 71: case 72: oX += a* 5.5; oY += h*13; break;
        case 59: case 60: case 61: case 73: case 74: case 75: oX += a*10.0; oY += h*14; break;
        case 62: case 63: case 64: case 76: case 77: case 78: oX += a*14.5; oY += h*15; break;
        case 65: case 66: case 82: case 79: case 80: case 81: oX += a*19.0; oY += h*16; break;
        }

        PointDouble center = new PointDouble(); // координата центра квадрата
        switch (direction) {
        case  0: case  3: case  6: case  9: case 12: case 28: case 31:
        case 34: case 37: case 54: case 56: case 59: case 62: case 65: center.x = oX - a*1.5;  center.y = oY - h;   break;
        case  1: case  4: case  7: case 10: case 13: case 29: case 32:
        case 35: case 38: case 55: case 57: case 60: case 63: case 66: center.x = oX;          center.y = oY - h*2; break;
        case  2: case  5: case  8: case 11: case 27: case 30: case 33:
        case 36: case 39: case 69: case 58: case 61: case 64: case 82: center.x = oX + a*1.5;  center.y = oY - h;   break;
        case 14: case 17: case 20: case 23: case 26: case 42: case 45:
        case 48: case 51: case 67: case 70: case 73: case 76: case 79: center.x = oX - a*1.5;  center.y = oY + h;   break;
        case 15: case 18: case 21: case 24: case 40: case 43: case 46:
        case 49: case 52: case 68: case 71: case 74: case 77: case 80: center.x = oX;          center.y = oY + h*2; break;
        case 16: case 19: case 22: case 25: case 41: case 44: case 47:
        case 50: case 53: case 83: case 72: case 75: case 78: case 81: center.x = oX + a*1.5;  center.y = oY + h;   break;
        }

        return new RectDouble(
            center.x - sq2,
            center.y - sq2,
            sq, sq);
    }

    @Override
    public int getShiftPointBorderIndex() {
        switch (direction) {
        case  0: case  3: case  6: case  9: case 12: case 28: case 31: case 34: case 37: case 54: case 56: case 59: case 62: case 65:
        case  1: case  4: case  7: case 10: case 13: case 29: case 32: case 35: case 38: case 55: case 57: case 60: case 63: case 66:
        case 14: case 17: case 20: case 23: case 26: case 42: case 45: case 48: case 51: case 67: case 70: case 73: case 76: case 79:
            return 2;
        }
        return 3;
    }

    @Override
    public Color getBackgroundFillColor(int fillMode, Color defaultColor, Map<Integer, Color> repositoryColor) {
        if (fillMode == getAttr().getMaxBackgroundFillModeValue())
        {
            // подсвечиваю 'ромашку'
            switch (getDirection()) {
            case  0: case  1: case  2: case 14: case 15: case 16: return repositoryColor.get(0);
            case  3: case  4: case  5: case 17: case 18: case 19: return repositoryColor.get(1);
            case  6: case  7: case  8: case 20: case 21: case 22: return repositoryColor.get(2);
            case  9: case 10: case 11: case 23: case 24: case 25: return repositoryColor.get(3);
            case 12: case 13: case 27: case 26: case 40: case 41: return repositoryColor.get(4);
            case 28: case 29: case 30: case 42: case 43: case 44: return repositoryColor.get(5);
            case 31: case 32: case 33: case 45: case 46: case 47: return repositoryColor.get(6);
            case 34: case 35: case 36: case 48: case 49: case 50: return repositoryColor.get(7);
            case 37: case 38: case 39: case 51: case 52: case 53: return repositoryColor.get(8);
            case 54: case 55: case 69: case 67: case 68: case 83: return repositoryColor.get(9);
            case 56: case 57: case 58: case 70: case 71: case 72: return repositoryColor.get(10);
            case 59: case 60: case 61: case 73: case 74: case 75: return repositoryColor.get(11);
            case 62: case 63: case 64: case 76: case 77: case 78: return repositoryColor.get(12);
            case 65: case 66: case 82: case 79: case 80: case 81: return repositoryColor.get(13);
//          default:
//              return repositoryColor.get(-1);
            }
        } else
        if (fillMode == (getAttr().getMaxBackgroundFillModeValue()-1))
        {
            // подсвечиваю обратную 'диагональку'
            switch (getDirection()) {
            case  1: case  0: case 14:
            case 13: case 12: case 26: case 38: case 37: case 51: case 63: case 62: case 76:
            case  7: case  6: case 20: case 32: case 31: case 45: case 57: case 56: case 70:
                return repositoryColor.get(0);
            case  2: case 16: case 15:
            case 27: case 41: case 40: case 39: case 53: case 52: case 64: case 78: case 77:
            case  8: case 22: case 21: case 33: case 47: case 46: case 58: case 72: case 71:
                return repositoryColor.get(1);
            case  4: case  3: case 17:
            case 29: case 28: case 42: case 55: case 54: case 67: case 66: case 65: case 79:
            case 10: case  9: case 23: case 35: case 34: case 48: case 60: case 59: case 73:
                return repositoryColor.get(2);
            case  5: case 19: case 18:
            case 30: case 44: case 43: case 69: case 83: case 68: case 82: case 81: case 80:
            case 11: case 25: case 24: case 36: case 50: case 49: case 61: case 75: case 74:
                return repositoryColor.get(3);
//          default:
//              return repositoryColor.get(-1);
            }
        }
        return super.getBackgroundFillColor(fillMode, defaultColor, repositoryColor);
    }

}