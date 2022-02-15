////////////////////////////////////////////////////////////////////////////////
//                               FastMines project
// file name: "PentagonT24.java"
//
// Реализация класса PentagonT24 - равносторонний 5-ти угольник, тип №2 и №4
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

public class ShapePentagonT24 extends BaseShape {

    @Override
    public SizeDouble getSize(Matrisize sizeField) {
        double a = getA();
        double b = getB();
        SizeDouble result = new SizeDouble(
            b + sizeField.m * a,
            b + sizeField.n * a);

        if (sizeField.n == 1)
            result.width -= getC();

        return result;
    }

    @Override
    public int getNeighborNumber(int direction) { return 7; }
    @Override
    public int getVertexNumber(int direction) { return 5; }
    @Override
    public double getVertexIntersection() { return 3.4; } // (3+3+3+4+4)/5.
    @Override
    public Size getDirectionSizeField() { return new Size(2, 2); }
    @Override
    public double getA() { return Math.sqrt(getArea()); }
    public double getB() { return getA()*6/11; }
    public double getC() { return getB()/2; }
    @Override
    public double getSq(double borderWidth) {
        double w = borderWidth/2.;
        return getA()*8/11-(w+w/SIN135a) / SQRT2;
    }

}
