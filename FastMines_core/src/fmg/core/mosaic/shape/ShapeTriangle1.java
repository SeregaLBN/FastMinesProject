////////////////////////////////////////////////////////////////////////////////
//                               FastMines project
// file name: "Triangle1.java"
//
// Реализация класса Triangle1 - равносторонний треугольник (вариант поля №1)
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

public class ShapeTriangle1 extends BaseShape {

    @Override
    public SizeDouble getSize(Matrisize sizeField) {
        double b = getB();
        double h = getH();
        return new SizeDouble(
            b * (sizeField.m+1),
            h * (sizeField.n+0));
    }

    @Override
    public int getNeighborNumber(int direction) { return 12; }

    @Override
    public int getVertexNumber(int direction) { return 3; }

    @Override
    public double getVertexIntersection() { return 6; }

    @Override
    public Size getDirectionSizeField() { return new Size(2, 2); }

    @Override
    public double getA() { return getB() * 2.f; } // размер стороны треугольника

    /** пол стороны треугольника */
    public double getB() { return Math.sqrt(getArea()/SQRT3); }

    /** высота треугольника */
    public double getH() { return getB() * SQRT3; }

    @Override
    public double getSq(double borderWidth) {
        double w = borderWidth/2.;
        return (getH()*2 - 6*w)/(SQRT3+2);
        //return (getA()*SQRT3 - 6*w)/(SQRT3+2);
    }

}
