////////////////////////////////////////////////////////////////////////////////
//                               FastMines project
// file name: "Hexagon1.java"
//
// Реализация класса Hexagon1 - правильный 6-ти угольник (сота)
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

public class ShapeHexagon1 extends BaseShape {

    @Override
    public SizeDouble getSize(Matrisize sizeField) {
        double a = getA();
        SizeDouble result = new SizeDouble(
                a * (sizeField.m    +0.5) * SQRT3,
                a * (sizeField.n*1.5+0.5));

        if (sizeField.n == 1)
            result.width -= getB()/2;

        return result;
    }

    @Override
    public int getNeighborNumber(int direction) { return 6; }
    @Override
    public int getVertexNumber(int direction) { return 6; }
    @Override
    public double getVertexIntersection() { return 3; }
    @Override
    public Size getDirectionSizeField() { return new Size(1, 2); }
    @Override
    public double getA() { return Math.sqrt(2*getArea()/SQRT27); }
    /** пол стороны треугольника */
    public double getB() { return getA()*SQRT3; }
    @Override
    public double getSq(double borderWidth) {
        double w = borderWidth/2.;
        return 2*(getB() - 2*w)/(SQRT3+1);
    }

}
