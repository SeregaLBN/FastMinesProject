////////////////////////////////////////////////////////////////////////////////
//                               FastMines project
// file name: "Triangle3.java"
//
// Реализация класса Triangle3 - треугольник 45°-90°-45°
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

public class ShapeTriangle3 extends BaseShape {

    @Override
    public SizeDouble getSize(Matrisize sizeField) {
        double a = getA();
        return new SizeDouble(a * ((sizeField.m + (sizeField.m & 1)) / 2),
                              a * ((sizeField.n + (sizeField.n & 1)) / 2));
    }

    @Override
    public int getNeighborNumber(int direction) { return 14; }
    @Override
    public int getVertexNumber(int direction) { return 3; }
    @Override
    public double getVertexIntersection() { return 6.6666666666666666666666666666667; } // (8+8+4)/3.
    @Override
    public Size getDirectionSizeField() { return new Size(2, 2); }
    @Override
    public double getA() { return 2*getB(); }
    /** пол стороны треугольника */
    public double getB() { return Math.sqrt(getArea()); }
    @Override
    public double getSq(double borderWidth) {
        double w = borderWidth/2.;
        return (getA() - w*2 / TAN45_2 ) / 3;
    }

}
