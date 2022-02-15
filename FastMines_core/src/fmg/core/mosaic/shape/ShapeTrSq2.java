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

package fmg.core.mosaic.shape;

import fmg.common.geom.Matrisize;
import fmg.common.geom.Size;
import fmg.common.geom.SizeDouble;

/** Triangle Square v2 */
public class ShapeTrSq2 extends BaseShape {

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
    public double getA() { return Math.sqrt(6*getArea()/(2+SQRT3)); }
    public double getB() { return getA()/2; }
    public double getH() { return getB()*SQRT3; }

    @Override
    public double getSq(double borderWidth) {
        double w = borderWidth/2.;
        return (getA()*SQRT3 - w*6) / (2+SQRT3) - 1;
    }

}
