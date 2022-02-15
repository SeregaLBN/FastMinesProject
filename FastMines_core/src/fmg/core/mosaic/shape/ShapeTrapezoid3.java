////////////////////////////////////////////////////////////////////////////////
//                               FastMines project
// file name: "ShapeTrapezoid3.java"
//
// Реализация класса Trapezoid3 - 8 трапеций, складывающихся в шестигранник
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

public class ShapeTrapezoid3 extends BaseShape {

    @Override
    public SizeDouble getSize(Matrisize sizeField) {
        double a = getA();
        double b = getB();
        double R = getROut();
        SizeDouble result = new SizeDouble(
              R *((sizeField.m+1)/2),
            a+b *((sizeField.n+1)/2)+
              a *((sizeField.n+0)/2));

        if (sizeField.m == 1)
            switch (sizeField.n % 4) {
            case 0: result.height -= a; break;
            case 3: result.height -= a*1.5; break;
            }
        if (sizeField.n == 1)
            if ((sizeField.m & 1) == 1)
                result.width -= getRIn();

        return result;
    }

    @Override
    public int getNeighborNumber(int direction) {
        switch (direction) {
        case  2: case  5: case 11: case 12: return 6;
        case  0: case  7: case  9: case 14: return 10;
        case  1: case  3: case  4: case  6:
        case  8: case 10: case 13: case 15: return 11;
        default:
            throw new IllegalArgumentException("Invalid value direction=" + direction);
        }
    }
    @Override
    public int getVertexNumber(int direction) { return 4; }

    static double vertexIntersection = 0.;
    @Override
    public double getVertexIntersection() {
        if (vertexIntersection < 1) {
            final int cntDirection = getDirectionCount(); // 0..11
            double sum = 0;
            for (int dir=0; dir<cntDirection; dir++)
                switch (dir) {
                case  2: case  5: case 11: case 12:
                    sum += (4+4+3+3)/4.;
                    break;
                case  0: case  7: case  9: case 14:
                    sum += (6+6+3+3)/4.;
                    break;
                case  1: case  3: case  4: case  6:
                case  8: case 10: case 13: case 15:
                    sum += (6+6+4+3)/4.;
                    break;
                default:
                    throw new RuntimeException("Забыл case #" + dir);
                }
            vertexIntersection = sum / cntDirection;
//              Logger.info("Trapezoid3::getVertexNeighbor == " + vertexIntersection);
        }
        return vertexIntersection;
    }

    @Override
    public Size getDirectionSizeField() { return new Size(4, 4); }

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
