////////////////////////////////////////////////////////////////////////////////
//                               FMG project
// file name: "BaseShape.java"
//
// Реализация базового класса BaseCell
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

import fmg.common.geom.DoubleExt;
import fmg.common.geom.Matrisize;
import fmg.common.geom.Size;
import fmg.common.geom.SizeDouble;

/** Base shape */
public abstract class BaseShape {

    public static final double SQRT2   = java.lang.Math.sqrt(2.);
    public static final double SQRT3   = java.lang.Math.sqrt(3.);
    public static final double SQRT27  = java.lang.Math.sqrt(27.);
    public static final double SQRT48  = java.lang.Math.sqrt(48.);
    public static final double SQRT147 = java.lang.Math.sqrt(147.);
    public static final double SIN15   = java.lang.Math.sin(java.lang.Math.PI/180.*15.);
    public static final double SIN18   = java.lang.Math.sin(java.lang.Math.PI/180.*18.);
    public static final double SIN36   = java.lang.Math.sin(java.lang.Math.PI/180.*36.);
    public static final double SIN54   = java.lang.Math.sin(java.lang.Math.PI/180.*54.);
    public static final double SIN72   = java.lang.Math.sin(java.lang.Math.PI/180.*72.);
    public static final double SIN75   = java.lang.Math.sin(java.lang.Math.PI/180.*75.);
    public static final double SIN99   = java.lang.Math.sin(java.lang.Math.PI/180.*99.);
    public static final double TAN15   = java.lang.Math.tan(java.lang.Math.PI/180.*15.);
    public static final double TAN45_2 = java.lang.Math.tan(java.lang.Math.PI/180.*45./2);
    public static final double SIN135a = java.lang.Math.sin(java.lang.Math.PI/180.*135.-java.lang.Math.atan(8./3));

    public static final String PROPERTY_AREA = "Area";

    /** площадь ячейки/фигуры */
    private double area = 500;

    /** площадь ячейки/фигуры */
    public void setArea(double area) {
        //setProperty(area, PROPERTY_AREA);
        double old = this.area;
        if (!DoubleExt.almostEquals(old, area))
            this.area = area;
    }
    /** площадь ячейки/фигуры */
    public double getArea() {
        return area;
    }

    /** размер квадрата, вписанного в фигуру - область куда выводиться изображение/текст
     * на основе заданных параметров */
    public abstract double getSq(double borderWidth);

    /** значение A (базовая величина фигуры - обычно это размер одной из сторон фигуры) по заданной площади фигуры */
    public abstract double getA();

    /** The size in pixels where to place the matrix */
    public abstract SizeDouble getSize(Matrisize sizeField);

    /** размер поля из группы ячеек состоящих из разных direction */
    public abstract Size getDirectionSizeField();
    /** кол-во direction'ов, которые знает данный тип мозаики */
    public int getDirectionCount() { Size s = getDirectionSizeField(); return s.width*s.height; }

    /** кол-во соседей у ячейки конкретной направленности */
    public abstract int getNeighborNumber(int direction);
    /** из скольки точек/вершин состоит фигура конкретной направленности */
    public abstract int getVertexNumber(int direction);
    /** сколько фигур пересекается в одной вершине (в среднем) */
    public abstract double getVertexIntersection();

    /** макс кол-во режимов заливки фона, которые знает данный тип мозаики
     * (знает ф-ция {@link BaseShape#getCellFillColor} или её наследующая)
     * (Не считая режима заливки цветом фона по-умолчанию...)
     */
    public int getMaxCellFillModeValue() {
        return 19;
    }

}
