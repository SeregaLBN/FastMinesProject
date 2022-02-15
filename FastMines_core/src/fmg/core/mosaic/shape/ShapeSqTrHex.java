////////////////////////////////////////////////////////////////////////////////
//                               FastMines project
// file name: "SqTrHex.java"
//
// Реализация класса SqTrHex - мозаика из 6Square 4Triangle 2Hexagon
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

public class ShapeSqTrHex extends BaseShape {

    @Override
    public SizeDouble getSize(Matrisize sizeField) {
        double a = getA();
        double h = getH();
        SizeDouble result = new SizeDouble(
             a/2+h + a/2*((sizeField.m+2)/3) +
                     h * ((sizeField.m+1)/3) +
            (a/2+h)    * ((sizeField.m+0)/3),
             a/2   + h * ((sizeField.n+1)/2)+
             a*3/2*      ((sizeField.n+0)/2));

        if (sizeField.n < 4) {
            int x = sizeField.m % 3;
            switch (sizeField.n) {
            case 1:
                switch (x) { case 0: result.width -= h; case 1: case 2: result.width -= h; }
                break;
            case 2: case 3:
                switch (x) { case 0: case 1: result.width -= h; }
                break;
            }
        }
        if (sizeField.m < 3) {
            int y = sizeField.n % 4;
            switch (sizeField.m) {
            case 1:
                switch (y) { case 0: result.height -= a*1.5; break; case 2: case 3: result.height -= a/2; }
                break;
            case 2:
                if (y == 0) result.height -= a/2;
                break;
            }
        }

        return result;
    }

    @Override
    public int getNeighborNumber(int direction) {
        switch (direction) {
        case  0: case  2: case  6: case  7: return 6;
        case  1: case  3: case  5: case  8: case  9: case 10: return 8;
        case  4: case 11: return 12;
        default:
            throw new IllegalArgumentException("Invalid value direction="+direction);
        }
    }
    @Override
    public int getVertexNumber(int direction) {
        switch (direction) {
        case  0: case  2: case  6: case  7: return 3;
        case  1: case  3: case  5: case  8: case  9: case 10: return 4;
        case  4: case 11: return 6;
        default:
            throw new IllegalArgumentException("Invalid value direction="+direction);
        }
    }
    @Override
    public double getVertexIntersection() { return 4.; }
    @Override
    public Size getDirectionSizeField() { return new Size(3, 4); }
    @Override
    public double getA() { return Math.sqrt(getArea()/(0.5+1/SQRT3)); }
    public double getH() { return getA()*SQRT3/2; }
    @Override
    public double getSq(double borderWidth) {
        double w = borderWidth/2.;
        return (getA()*SQRT3 - w*6) / (2+SQRT3);
    }

}
