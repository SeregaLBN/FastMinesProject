////////////////////////////////////////////////////////////////////////////////
//                               FMG project
// file name: "BaseCell.java"
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
package fmg.core.mosaic.cells;

import java.beans.PropertyChangeListener;
import java.util.List;
import java.util.function.IntFunction;

import fmg.common.Color;
import fmg.common.Logger;
import fmg.common.geom.*;
import fmg.common.notifier.INotifyPropertyChanged;
import fmg.common.notifier.NotifyPropertyChanged;
import fmg.core.types.EClose;
import fmg.core.types.EOpen;
import fmg.core.types.EState;
import fmg.core.types.Property;

/** Базовый класс фигуры-ячейки */
public abstract class BaseCell {

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

    /**
     * Cell model.
     * Контекст/метаданные, описывающий общие хар-ки для каждого из экземпляров BaseCell.
     * <br> (Полные данные о конкретной мозаике) <br>
     * Доопределаяется наследниками BaseCell
     */
    public abstract static class BaseAttribute implements INotifyPropertyChanged, AutoCloseable {

        public static final String PROPERTY_AREA = "Area";

        /** площадь ячейки/фигуры */
        @Property(PROPERTY_AREA)
        private double area = 500;

        protected NotifyPropertyChanged notifier = new NotifyPropertyChanged(this);

        /** площадь ячейки/фигуры */
        public void setArea(double area) {
            //setProperty(area, PROPERTY_AREA);
            double old = this.area;
            if (!DoubleExt.hasMinDiff(old, area)) {
                this.area = area;
                notifier.firePropertyChanged(old, area, PROPERTY_AREA);
            }
        }
        /** площадь ячейки/фигуры */
        public double getArea() {
            return area;
        }

        /** размер квадрата, вписанного в фигуру - область куда выводиться изображение/текст
         * на основе заданных параметров */
        public abstract double getSq(double borderWidth);

        /** значение A (базовая величина фигуры - обычно это размер одной из сторон фигуры) по заданной площади фигуры */
        protected abstract double getA();

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
         * (знает ф-ция {@link BaseCell#getCellFillColor} или её наследующая)
         * (Не считая режима заливки цветом фона по-умолчанию...)
         */
        public int getMaxCellFillModeValue() {
            return 19;
        }

        @Override
        public void addListener(PropertyChangeListener listener) {
            notifier.addListener(listener);
        }
        @Override
        public void removeListener(PropertyChangeListener listener) {
            notifier.removeListener(listener);
        }

        @Override
        public void close() {
            notifier.close();
        }

    }

    private final BaseAttribute attr;
    public BaseAttribute getAttr() {
        return attr;
    }

    protected Coord coord;
    /** направление - 'третья координата' ячейки */
    protected int direction;

    /** вписанный в фигуру квадрат - область в которую выводится изображение/текст */
    public abstract RectDouble getRcInner(double borderWidth);
    /** вернёт прямоугольник в который вписана фигура ячейки */
    public RectDouble getRcOuter() {
        return region.getBounds();
    }


    /** массив координат точек из которых состоит фигура */
    protected RegionDouble region;

    public static class StateCell {
        private EState status; // _Open, _Close
        private EOpen  open;   // _Nil, _1, ... _21, _Mine
        private EClose close;  // _Unknown, _Clear, _Flag
        /** Нажата? Не путать с open! - ячейка может быть нажата, но ещё не открыта. Важно только для ф-ции прорисовки */
        private boolean down;

        public void setDown(boolean bDown) { this.down = bDown; }
        public boolean isDown() { return this.down; }

        public void setStatus(EState status) { this.status = status; }
        public EState getStatus() { return status; }

        public void setOpen(EOpen open) { this.open = open; }
        public EOpen getOpen() { return this.open; }

        public void setClose(EClose close) { this.close = close; }
        public EClose getClose() { return this.close; }

        public StateCell() { reset(); }
        public void reset() {
            status = EState._Close;
            open   = EOpen._Nil;
            close  = EClose._Clear;
            down = false;
        }
    }
    private StateCell state;
    /** запретить установку мины на данную ячейку */
    private boolean lockMine;

    public void setMine() {
        if (this.lockMine || (state.open == EOpen._Mine))
            throw new IllegalStateException("Illegal usage");
        state.open = EOpen._Mine;
    }

    public StateCell getState() { return state; }
    public void setState(StateCell stateNew) {
        this.state = stateNew;
    }

    protected BaseCell(
        BaseAttribute attr,
        Coord coord,
        int iDirection)
    {
        this.attr = attr;
        this.coord = coord;
        this.direction = iDirection;
        this.region = new RegionDouble(attr.getVertexNumber(iDirection));

        this.state = new StateCell();
        reset();
    }

    public void init() {
        calcRegion();
    }

    /** координаты соседей */
    public abstract List<Coord> getCoordsNeighbor();

    public Coord getCoord() { return coord; }
    public int getDirection() { return direction; }
    /** координата центра фигуры (в пикселях) */
    public PointDouble getCenter() { return getRcInner(1).center(); }

    /** принадлежат ли эти экранные координаты ячейке */
    public boolean pointInRegion(PointDouble point) { return region.contains(point); }

    public RegionDouble getRegion() { return region; }

    /** определить координаты точек из которых состоит фигура */
    protected abstract void calcRegion();

    public void reset() {
        state.reset();
        lockMine = false;
    }

    /** Index where border change color */
    public abstract int getShiftPointBorderIndex();


    /** <ul> Вернуть цвет заливки ячеки в зависимости от
        * <li> режима заливки фона ячеек
        * <li> координаты ячейки
        * <li> направления ячейки
        * <li> ... - как придумает дочерний класс
        */
    public Color getCellFillColor(int fillMode, Color defaultColor, IntFunction<Color> getColor) {
        switch (fillMode) {
        default:
            Logger.error(getClass().getSimpleName()+".getBackgroundFillColor: fillMode="+fillMode+":  добавь цветовую обработку для этого режима!");
            // !!! no break
        case 0:
            if ((getState().getStatus() == EState._Open) && (getState().getOpen() == EOpen._Mine) && getState().isDown())
                return Color.Red().brighter(0.05); // game over: игра завершена - клик на мине
            if ((getState().getStatus() == EState._Open) && (getState().getOpen() != EOpen._Mine) && (getState().getClose() == EClose._Flag))
                return Color.Magenta().brighter(0.3); // game over: игра завершена - не верно проставлен флаг (на ячейке с цифрой)

            // для Down и Нажатого состояний делаю фон чуть и чуть-чуть темнее...
            if (getState().isDown())
                return defaultColor.darker((getState().getStatus() == EState._Close) ? 0.15 : 0.25);
            return defaultColor;

        case 1:
            return getColor.apply(getDirection());
        case 2:
            {
                // подсветить каждую i-тую строку c шагом div
                final int i = 2;
                final int div = 5;
                int tmp1 = getCoord().x % div;
                int tmp2 = (getCoord().y-tmp1) % div;
                return getColor.apply((((tmp1 + tmp2) % div) == i) ? 0 : 1 );
            }
        case 3:
            {
                // дуршлаг
                final int i = 3;
                final int div = 4;
                int tmp1 = getCoord().x % div;
                int tmp2 = (getCoord().y+tmp1) % div;
                return getColor.apply((((tmp1 + tmp2) % div) == i) ? 0 : 1 );
            }
        case 4:
            {
                // ход конём
                final int i = 3;
                final int div = 5;
                int tmp1 = getCoord().x % div;
                int tmp2 = (getCoord().y+tmp1) % div;
                return getColor.apply((((tmp1 + tmp2) % div) == i) ? 0 : 1 );
            }
        case 5:
            {
                // волны
                final int div = 15;
                int tmp1 = getCoord().x % div;
                int tmp2 = (getCoord().y+tmp1) % div;
                return getColor.apply((tmp1 + tmp2) % div);
            }
        case 6:
            {
                final int div = 4;
                return getColor.apply(((getCoord().x%div + getCoord().y%div) == div) ? 0 : 1);
            }
        case 7: case 8: case 9:
            return getColor.apply(getCoord().x % (-5+fillMode));
        case 10: case 11: case 12:
            return getColor.apply(getCoord().y % (-8+fillMode));
        case 13: case 14: case 15:
        case 16: case 17: case 18:
            return getColor.apply(getCoord().x % (-fillMode) - fillMode + getCoord().y % (+fillMode));
        case 19:
            // подсветить direction
            int zx = getCoord().x / getAttr().getDirectionSizeField().width +1;
            int zy = getCoord().y / getAttr().getDirectionSizeField().height +1;
            return getColor.apply(zx*zy);
        }
    }

    @Override
    public String toString() {
        return getClass().getSimpleName()+"{ x:"+coord.x+", y:"+coord.y+" }";
    }

}
