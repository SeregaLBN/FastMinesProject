////////////////////////////////////////////////////////////////////////////////
//                               FastMines project
// file name: "ShapeParquet1.java"
//
// Реализация класса Parquet1 - паркет в елку (herring-bone parquet)
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

public class ShapeParquet1 extends BaseShape {

    @Override
    public SizeDouble getSize(Matrisize sizeField) {
        double a = getA();
        SizeDouble result = new SizeDouble(
            (sizeField.m*2+1) * a,
            (sizeField.n*2+2) * a);

        if (sizeField.m == 1)
            result.height -= a;

        return result;
    }

    @Override
    public int getNeighborNumber(int direction) { return 6; }
    @Override
    public int getVertexNumber(int direction) { return 4; }
    @Override
    public double getVertexIntersection() { return 3; }
    @Override
    public Size getDirectionSizeField() { return new Size(2, 1); }
    @Override
    public double getA() { return Math.sqrt(getArea())/2; }
    @Override
    public double getSq(double borderWidth) {
        double w = borderWidth/2.;
        return getA()-w*SQRT2;
    }

}
