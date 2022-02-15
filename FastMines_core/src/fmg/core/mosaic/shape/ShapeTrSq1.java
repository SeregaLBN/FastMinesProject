////////////////////////////////////////////////////////////////////////////////
//                               FastMines project
// file name: "ShapeTrSq1.java"
//
// Реализация класса TrSq1 - мозаика из 4х треугольников и 2х квадратов
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
public class ShapeTrSq1 extends BaseShape {

    @Override
    public SizeDouble getSize(Matrisize sizeField) {
        double b = getB();
        double k = getK();
        double n = getN();
        double m = getM();
        SizeDouble result = new SizeDouble(
            b+n*((sizeField.m-1+2)/3)+
              k*((sizeField.m-1+1)/3)+
              m*((sizeField.m-1+0)/3),
            b+n* (sizeField.n-1));

        if (sizeField.n == 1) {
            if ((sizeField.m % 3) == 2) result.width -= m;
            if ((sizeField.m % 3) == 0) result.width -= k;
        }
        if (sizeField.m == 1)
            if ((sizeField.n & 1) == 0)
                result.height -= m;

        return result;
    }

    @Override
    public int getNeighborNumber(int direction) {
        switch (direction) {
        case 1: case 2: case 3: case 5: return 9;
        case 0: case 4: return 12;
        default:
            throw new IllegalArgumentException("Invalid value direction=" + direction);
        }
    }

    @Override
    public int getVertexNumber(int direction) {
        switch (direction) {
        case 1: case 2: case 3: case 5: return 3;
        case 0: case 4: return 4;
        default:
            throw new IllegalArgumentException("Invalid value direction="+direction);
        }
    }

    @Override
    public double getVertexIntersection() { return 5.; }

    @Override
    public Size getDirectionSizeField() { return new Size(3, 2); }

    @Override
    public double getA() { return Math.sqrt(3*getArea()/(1+SQRT3/2)); }
    public double getB() { return getN()+getM(); }
    public double getK() { return getN()-getM(); }
    public double getN() { return getA()*SIN75; }
    public double getM() { return getA()*SIN15; }

    @Override
    public double getSq(double borderWidth) {
        double w = borderWidth/2.;
        return (getA()*SQRT3 - w*6) / (4*SIN75);
    }

}
