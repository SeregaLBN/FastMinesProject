////////////////////////////////////////////////////////////////////////////////
//                               FastMines project
// file name: "Square1.java"
//
// Описание класса Square1 - квадрат (классический вариант поля)
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

public class ShapeSquare1 extends BaseShape {

    @Override
    public SizeDouble getSize(Matrisize sizeField) {
        double a = getA(); // размер стороны квадрата
        return new SizeDouble(
            sizeField.m * a,
            sizeField.n * a);
    }

    @Override
    public int getNeighborNumber(int direction) { return 8; }
    @Override
    public int getVertexNumber(int direction) { return 4; }
    @Override
    public double getVertexIntersection() { return 4; }
    @Override
    public Size getDirectionSizeField() { return new Size(1,1); }
    @Override
    public double getA() { return Math.sqrt(getArea()); }
    @Override
    public double getSq(double borderWidth) {
        double w = borderWidth/2.;
        return getA()-2*w;
    }

}
