////////////////////////////////////////////////////////////////////////////////
//                               FastMines project
// file name: "Rhombus1.java"
//
// Реализация класса Rhombus1 - 3 ромба, составляющие равносторонний шестиугольник
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

public class ShapeRhombus1 extends BaseShape {

    @Override
    public SizeDouble getSize(Matrisize sizeField) {
        double a = getA();
        double r = getR();
        double c = getC();
        SizeDouble result = new SizeDouble(
            c+a   *((sizeField.m+2)/3) +
             (a+c)*((sizeField.m+1)/3) +
                c *((sizeField.m+0)/3),
                r * (sizeField.n+1));

        if (sizeField.m == 1)
            result.height -= r;
        if (sizeField.n == 1)
            switch (sizeField.m % 3) {
            case 0: result.width -= a/2; break;
            case 2: result.width -= a; break;
            }

        return result;
    }

    @Override
    public int getNeighborNumber(int direction) { return 10; }
    @Override
    public int getVertexNumber(int direction) { return 4; }
    @Override
    public double getVertexIntersection() { return 4.5; } // (3+3+6+6)/4.
    @Override
    public Size getDirectionSizeField() { return new Size(3, 2); }
    @Override
    public double getA() { return Math.sqrt(getArea()*2/SQRT3); }
    public double getC() { return getA()/2; }
    public double getH() { return getA()*SQRT3; }
    public double getR() { return getH()/2; }
    @Override
    public double getSq(double borderWidth) {
        double w = borderWidth/2.;
        return (getA()*SQRT3 - w*4)/(SQRT3+1);
    }

    @Override
    public int getMaxCellFillModeValue() {
        return super.getMaxCellFillModeValue()+1;
    }

}
