////////////////////////////////////////////////////////////////////////////////
//                               FastMines project
// file name: "ShapeQuadrangle1.java"
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

package fmg.core.mosaic.shape;

import fmg.common.geom.Matrisize;
import fmg.common.geom.Size;
import fmg.common.geom.SizeDouble;

public class ShapeQuadrangle1 extends BaseShape {

    @Override
    public SizeDouble getSize(Matrisize sizeField) {
        double a = getA();
        double b = getB();
        double h = getH();
        double m = getM();
        SizeDouble result = new SizeDouble(
            m + m*((sizeField.m+2)/3)+
                h*((sizeField.m+1)/3)+
                m*((sizeField.m+0)/3),
            b + b*((sizeField.n+1)/2)+
                a*((sizeField.n+0)/2));

        if (sizeField.m == 1)
            if ((sizeField.n & 1) == 0)
                result.height -= a/4;
        if (sizeField.m == 2)
            if ((sizeField.n % 4) == 0)
                result.height -= a/4;
        if ((sizeField.n == 1) || (sizeField.n == 2)) {
            if ((sizeField.m % 3) == 2)
                result.width -= m;
            if ((sizeField.m % 3) == 0)
                result.width -= m;
        }

        return result;
    }

    @Override
    public int getNeighborNumber(int direction) { return 9; }
    @Override
    public int getVertexNumber(int direction) { return 4; }
    @Override
    public double getVertexIntersection() { return 4.25; } // (3+4+4+6)/4.
    @Override
    public Size getDirectionSizeField() { return new Size(3, 4); }
    @Override
    public double getA() { return Math.sqrt(getArea()/SQRT3)*2; }
    public double getB() { return getA()/2; }
    public double getH() { return getB()*SQRT3; }
    public double getN() { return getA()*0.75; }
    public double getM() { return getH()/2; }
    public double getZ() { return getA()/(1+SQRT3); }
    public double getZx() { return getZ()*SQRT3/2; }
    public double getZy() { return getZ()/2; }
    @Override
    public double getSq(double borderWidth) {
        double w = borderWidth/2.;
        return (getA()*SQRT3 - w*2*(1+SQRT3))/(SQRT3+2);
    }

}
