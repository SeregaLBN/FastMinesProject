////////////////////////////////////////////////////////////////////////////////
//                               FastMines project
// file name: "ShapePentagonT5.java"
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

package fmg.core.mosaic.shape;

import fmg.common.geom.Matrisize;
import fmg.common.geom.Size;
import fmg.common.geom.SizeDouble;

public class ShapePentagonT5 extends BaseShape {

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
    public double getA() { return 2*Math.sqrt(getArea()/SQRT147); }
    public double getH() { return getA()*SQRT3/2; }
    @Override
    public double getSq(double borderWidth) {
        double w = borderWidth/2.;
        return (getA()*2*SQRT3-4*w)/(SQRT3+1);
    }

    @Override
    public int getMaxCellFillModeValue() {
        return super.getMaxCellFillModeValue()+2;
//      return 1;
    }
}
