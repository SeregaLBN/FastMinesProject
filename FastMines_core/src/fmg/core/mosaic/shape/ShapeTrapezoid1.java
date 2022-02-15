////////////////////////////////////////////////////////////////////////////////
//                               FastMines project
// file name: "Trapezoid1.java"
//
// Реализация класса Trapezoid1 - 3 трапеции, составляющие равносторонний треугольник
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

public class ShapeTrapezoid1 extends BaseShape {

    @Override
    public SizeDouble getSize(Matrisize sizeField) {
        double a = getA();
        double c = getC();
        double r = getRIn();
        double R = getROut();
        SizeDouble result = new SizeDouble(
            c + a *  (sizeField.m+1),
            R     * ((sizeField.n+1)/2) +
            r     * ((sizeField.n+0)/2));

        if (sizeField.n < 4)
            if ((sizeField.m % 3) != 0)
                result.width -= c;

        return result;
    }

    @Override
    public int getNeighborNumber(int direction) { return 8; }

    @Override
    public int getVertexNumber(int direction) { return 4; }

    @Override
    public double getVertexIntersection() { return 3.6; } // (3+3+3+3+6)/5.

    @Override
    public Size getDirectionSizeField() { return new Size(3, 4); }

    @Override
    public double getA   () { return Math.sqrt(getArea()/SQRT27)*2; }
    public double getB   () { return getA()*2; }
    public double getC   () { return getA()/2; }
    public double getROut() { return getA()*SQRT3; }
    public double getRIn () { return getROut()/2; }

    @Override
    public double getSq(double borderWidth) {
        double w = borderWidth/2.;
        return (getA()*SQRT3 - w*4)/(SQRT3+1);
    }

}
