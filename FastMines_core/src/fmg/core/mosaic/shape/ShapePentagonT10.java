////////////////////////////////////////////////////////////////////////////////
//                               FastMines project
// file name: "ShapePentagonT10.java"
//
// Реализация класса PentagonT10 - 5-ти угольник, тип №10
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

public class ShapePentagonT10 extends BaseShape {

    @Override
    public SizeDouble getSize(Matrisize sizeField) {
        double a = getA();
        SizeDouble result = new SizeDouble(
            2*a +
            5*a*((sizeField.m+1)/2) +
                a*((sizeField.m+0)/2),
            2*a +
            3*a*((sizeField.n+2)/3) +
            3*a*((sizeField.n+1)/3) +
                a*((sizeField.n+0)/3));

        if (sizeField.n == 1)
            if ((sizeField.m & 1) == 1)
                result.width -= 3*a;
            else
                result.width -= a;
        if (sizeField.n == 2)
            if ((sizeField.m & 1) == 1)
                result.width -= 2*a;
            else
                result.width -= a;

        if (sizeField.m == 1)
            if (((sizeField.n % 6) == 4) ||
                ((sizeField.n % 6) == 5))
                result.height -= 2*a;

        return result;
    }

    @Override
    public int getNeighborNumber(int direction) {
        switch (direction) {
        case 0: case 1: case 6: case 7: return 7;
        case 2: case 3: case 4: case 5: case 8: case 9: case 10: case 11: return 6;
        default:
            throw new IllegalArgumentException("Invalid value direction=" + direction);
        }
    }
    @Override
    public int getVertexNumber(int direction) { return 5; }

    static double vertexIntersection = 0.;
    @Override
    public double getVertexIntersection() {
        if (vertexIntersection < 1) {
            final int cntDirection = getDirectionCount(); // 0..11
            double sum = 0;
            for (int dir=0; dir<cntDirection; dir++)
                switch (dir) {
                case 0: case 1: case 6: case 7:
                    sum += 3;
                    break;
                case 2: case 3: case 4: case 5: case 8: case 9: case 10: case 11:
                    sum += 16./5.;
                    break;
                default:
                    throw new RuntimeException("Забыл case #" + dir);
                }
            vertexIntersection = sum / cntDirection;
//          Logger.info("PentagonT10::getVertexNeighbor == " + vertexIntersection);
        }
        return vertexIntersection;
    }

    @Override
    public Size getDirectionSizeField() { return new Size(2, 6); }
    @Override
    public double getA() { return Math.sqrt(getArea()/7); }
    @Override
    public double getSq(double borderWidth) {
        double w = borderWidth/2.;
        return 2*(getA()-w);
    }

    @Override
    public int getMaxCellFillModeValue() {
        return super.getMaxCellFillModeValue() + 1;
//      return 1;
    }
}
