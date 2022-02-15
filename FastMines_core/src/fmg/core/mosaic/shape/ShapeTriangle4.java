////////////////////////////////////////////////////////////////////////////////
//                               FastMines project
// file name: "Triangle4.java"
//
// Реализация класса Triangle4 - треугольник 30°-30°-120°
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

public class ShapeTriangle4 extends BaseShape {

    public enum ComplexityMode {
        /** original */
        eUnrealMode,
        eMeanMode,
        eOptimalMode,
        eSimpeMode
    }

    public final static ComplexityMode Mode = ComplexityMode.eOptimalMode; // TODO: check others to view...

    @Override
    public SizeDouble getSize(Matrisize sizeField) {
        double b = getB();
        double r = getRIn();
        double R = getROut();
        SizeDouble result = new SizeDouble(
                b+b *((sizeField.m+2)/3) +
                  b *((sizeField.m+0)/3),
               (R+r)*((sizeField.n+1)/2));

        switch (Mode) {
        case eUnrealMode:
        case eOptimalMode:
            // none  ...
            break;
        case eMeanMode:
        case eSimpeMode:
            {
                double u = getSnip()/2; // Snip * cos60
                double c = u*SQRT3; // Snip * cos30
                switch (sizeField.m%3) {
                case 0: result.width -= u+u; break;
                case 1: result.width -= u+c; break;
                case 2: result.width -= u; break;
                }
                if (Mode == ComplexityMode.eMeanMode) {
                    if ((sizeField.n % 4) == 3)
                        result.height -= u;
                } else {
                    if ((sizeField.n & 1) == 1)
                        result.height -= u;
                }
            }
            break;
        }

        if (sizeField.m == 1)
            if ((sizeField.n % 4) == 3)
                result.height -= R;
        if (sizeField.n == 1)
            if ((sizeField.m % 3) == 1)
                result.width -= b;

        return result;
    }

    @Override
    public int getNeighborNumber(int direction) {
        switch(Mode) {
        case eUnrealMode : return 21;
        case eMeanMode:
            switch(direction) {
            case 2: case 11:                                 return 7;
            case 1: case 5: case 8: case 10:                 return 5;
            case 0: case 3: case 4: case 6 : case 7: case 9: return 3;
            default: throw new RuntimeException("Unknown direction==" + direction);
            }
        case eOptimalMode:
            switch(direction) {
            case 4: case 5: case 9: case 10:  return 6;
            case 0: case 1: case 2: case 3:
            case 6: case 7: case 8: case 11:  return 7;
            default: throw new RuntimeException("Unknown direction==" + direction);
            }
        case eSimpeMode  : return 3;
        default: throw new RuntimeException("Unknown Mode==" + Mode);
        }
    }
    @Override
    public int getVertexNumber(int direction) {
        switch(Mode) {
        case eUnrealMode: return 3;
        case eMeanMode:
            switch(direction) {
            case 0: case 3: case 4: case 6: case 7: case 9: return 5;
            case 1: case 5: case 8: case 10:                return 4;
            case 2: case 11:                                return 3;
            default: throw new RuntimeException("Unknown direction==" + direction);
            }
        case eOptimalMode: return 4;
        case eSimpeMode  : return 5;
        default: throw new RuntimeException("Unknown Mode==" + Mode);
        }
    }
    @Override
    public double getVertexIntersection() {
        switch(Mode) {
        case eUnrealMode : return 9.;   // (12+12+3)/3.
        case eSimpeMode  : return 2.2;  // (2+2+2+2+3)/5.
        case eOptimalMode: return 3.25; // (6+3+2+2)/4.
        case eMeanMode   : return 2.62777777778;
            // ( (2+2+2+2+3)/ getVertexNumber(0 or 3 or 4 or 6 or 7 or 9) * 6шт  +
            //   ( 2+2+3+4 )/ getVertexNumber(1 or 5 or 8 or 10) * 4шт  +
            //   (  3+4+4  )/ getVertexNumber(2 or 11) * 2шт ) / 12шт
            //
            // ((2+2+2+2+3)/5.*6 + (2+2+3+4)/4.*4 + (3+4+4)/3.*2 ) / 12
            // (11/5.*6 + 11/4.*4 + 11/3.*2) / 12
            // 11*2*(1/5.*3 + 1/4.*2 + 1/3.) / 12
            // 11*2*(3/5. + 1/2. + 1/3.) / 12
            // 11*(3/5. + 1/2. + 1/3.) / 6
            // 11*(3/30. + 1/12. + 1/18.)
            // http://www.google.com/search?q=11*%283/30.+%2B+1/12.+%2B+1/18.%29
            //  2.62777777778
        default: throw new RuntimeException("Unknown Mode==" + Mode);
        }
    }

    @Override
    public Size getDirectionSizeField() { return new Size(3, 4); }

    @Override
    public double getA   () { return Math.sqrt(getArea()*SQRT48); }
    public double getB   () { return getA()/2; }
    public double getROut() { return getA()/SQRT3; }
    public double getRIn () { return getROut()/2; }
    //private double __snip  = 2.3456789 + new java.util.Random(java.util.UUID.randomUUID().hashCode()).nextInt(15);
    public double getSnip() { return getA()/(/*12*/6.789012345 /*__snip*/); }

    @Override
    public double getSq(double borderWidth) {
        double w = borderWidth/2.;
        return (getA()-w*2/TAN15)/(SQRT3+3);
    }

}
