////////////////////////////////////////////////////////////////////////////////
//                               FMG project
//                                      © Sergey Krivulya (KSerg, aka SeregaLBN)
// file name: "BaseCell.java"
//
// Реализация базового класса BaseCell
// Copyright (C) 2010-2011 Sergey Krivulya
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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import fmg.common.Color;
import fmg.common.geom.*;
import fmg.common.notyfier.NotifyPropertyChanged;
import fmg.core.types.EClose;
import fmg.core.types.EOpen;
import fmg.core.types.EState;
import fmg.core.types.click.ClickCellResult;

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
   public static abstract class BaseAttribute extends NotifyPropertyChanged {

      public static final String PROPERTY_AREA = "Area";

      /** площадь ячейки/фигуры */
      private double area = 500;

      /** площадь ячейки/фигуры */
      public void setArea(double area) {
         //setProperty(area, PROPERTY_AREA);
         double old = this.area;
         if (!DoubleExt.hasMinDiff(old, area)) {
            this.area = area;
            onPropertyChanged(old, area, PROPERTY_AREA);
         }
      }
      /** площадь ячейки/фигуры */
      public double getArea() {
         return area;
      }

      /** размер квадрата, вписанного в фигуру - область куда выводиться изображение/текст
       * на основе заданных параметров */
      public abstract double getSq(int borderWidth);

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
       * (знает ф-ция BaseCell::getBackgroundFillColor() или её наследующая)
       * (Не считая режима заливки цветом фона по-умолчанию...)
       */
      public int getMaxBackgroundFillModeValue() {
         return 19;
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
   public abstract RectDouble getRcInner(int borderWidth);
   /** вернёт прямоугольник в который вписана фигура ячейки */
   public RectDouble getRcOuter() {
      RectDouble rcOuter = region.getBounds();
    //rcOuter.height++; rcOuter.width++; // чтобы при repaint'е захватило и крайние границы
      return rcOuter;
   }


   /** массив координат точек из которых состоит фигура */
   protected RegionDouble region;

   public class StateCell {
      // { union
      private EState status; // _Open, _Close
      private EOpen   open;   // _Nil, _1, ... _21, _Mine
      private EClose  close;  // _Unknown, _Clear, _Flag
      // } end union
      /** Нажата? Не путать с open! - ячейка может быть нажата, но ещё не открыта. Важно только для ф-ции прорисовки */
      private boolean down;

       public void setDown(boolean bDown) { this.down = bDown; }
       public boolean isDown() { return this.down; }

       public void setStatus(EState status) { this.status = status; }
       public EState getStatus() { return status; }

       public void calcOpenState(IMatrixCells matrix) {
          if (this.open == EOpen._Mine) return;
          // подсчитать у соседей число мин и установить значение
          int count = 0;
          List<BaseCell> neighbors = getNeighbors(matrix);
          for (BaseCell nCell : neighbors) {
             if (nCell == null) continue; // существует ли сосед?
             if (nCell.getState().getOpen() == EOpen._Mine) count++;
          }
          this.open = EOpen.class.getEnumConstants()[count];
       }
       public boolean SetMine() {
          if (lockMine || (this.open == EOpen._Mine)) return false;
          this.open = EOpen._Mine;
          return true;
       }
       public EOpen getOpen() { return this.open; }

       public void setClose(EClose close) { this.close = close; }
       public EClose getClose() { return this.close; }

       private StateCell() { Reset(); }
       public void Reset() {
          status = EState._Close;
          open = EOpen._Nil;
          close = EClose._Clear;
          down = false;
       }
   }
   private StateCell state;
   /** запретить установку мины на данную ячейку */
   private boolean lockMine;

   public void lockNeighborMines(IMatrixCells matrix) {
      lockMine = true;
      // запретить установку мин у соседей
      List<BaseCell> neighbors = getNeighbors(matrix);
      for (BaseCell nCell : neighbors) {
         if (nCell == null) continue; // существует ли сосед?
         nCell.lockMine = true;
      }
   }

   public StateCell getState() { return state; }

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
      Reset();
   }

   public void init() {
      calcRegion();
   }

   /** координаты соседей */
   protected abstract List<Coord> getCoordsNeighbor();

   /** матрица ячеек поля мозаики */
   public static interface IMatrixCells {
      /** размер поля */
      Matrisize getSizeField();
      void setSizeField(Matrisize size);

      /** доступ к заданной ячейке */
      BaseCell getCell(Coord coord);
   }

   /** соседние ячейки - с которыми граничит this */
   public List<BaseCell> getNeighbors(IMatrixCells matrix) {
      // получаю координаты соседних ячеек
      List<Coord> neighborCoord = getCoordsNeighbor();

      int m = matrix.getSizeField().m;
      int n = matrix.getSizeField().n;
      // по координатам получаю множество соседних обьектов-ячеек
      List<BaseCell> neighbors = new ArrayList<>(neighborCoord.size());
      for (Coord c : neighborCoord)
         // проверяю что они не вылезли за размеры
         if ((c.x >= 0) && (c.y >= 0) && (c.x < m) && (c.y < n))
             neighbors.add( matrix.getCell(c) );
      return neighbors;
   }

   public Coord getCoord() { return coord; }
   public int getDirection() { return direction; }
   /** координата центра фигуры (в пикселях) */
   public PointDouble getCenter() { return getRcInner(1).center(); }

   /** принадлежат ли эти экранные координаты ячейке */
   public boolean pointInRegion(PointDouble point) { return region.contains(point); }

   public RegionDouble getRegion() { return region; }

   /** определить координаты точек из которых состоит фигура */
   protected abstract void calcRegion();

   public void Reset() {
      state.Reset();
      lockMine = false;
   }

   /** Index where border change color */
   public abstract int getShiftPointBorderIndex();


   public ClickCellResult leftButtonDown(IMatrixCells matrix) {
      ClickCellResult result = new ClickCellResult();
      if (state.getClose() == EClose._Flag)
         return result;

      if (state.getStatus() == EState._Close) {
         state.setDown(true);
         result.modified.add(this);
         return result;
      }

      // эффект нажатости для неоткрытых соседей
      if ((state.getStatus() == EState._Open) && (state.getOpen() != EOpen._Nil)) {
         List<BaseCell> neighbors = getNeighbors(matrix);
         for (BaseCell nCell : neighbors) {
            if (nCell == null) continue; // существует ли сосед?
            if ((nCell.state.getStatus() == EState._Open) ||
                (nCell.state.getClose()  == EClose._Flag)) continue;
            nCell.state.setDown(true);
            result.modified.add(nCell);
         }
      }
      return result;
   }

   public ClickCellResult leftButtonUp(boolean isMy, IMatrixCells matrix) {
      ClickCellResult result = new ClickCellResult();

      if (state.getClose() == EClose._Flag)
         return result;

      // избавится от эффекта нажатости
      if ((state.getStatus() == EState._Open) && (state.getOpen() != EOpen._Nil)) {
         List<BaseCell> neighbors = getNeighbors(matrix);
         for (BaseCell nCell : neighbors) {
            if (nCell == null) continue; // существует ли сосед?
            if ((nCell.state.getStatus() == EState._Open) ||
                (nCell.state.getClose()  == EClose._Flag)) continue;
            nCell.state.setDown(false);
            result.modified.add(nCell);
         }
      }
      // Открыть закрытую ячейку на которой нажали
      if (state.getStatus() == EState._Close) {
         state.setDown(isMy);
         result.modified.add(this);
         if (!isMy)
            return result;

         getState().setStatus(EState._Open);
      }

      // ! В этой точке ячейка уже открыта
      // Подсчитываю кол-во установленных вокруг флагов и не открытых ячеек
      int countFlags = 0;
      int countClear = 0;
      List<BaseCell> neighbors = getNeighbors(matrix);
      if (state.getOpen() != EOpen._Nil)
         for (BaseCell nCell : neighbors) {
            if (nCell == null) continue; // существует ли сосед?
            if (nCell.state.getStatus() == EState._Open) continue;
            if (nCell.state.getClose()  == EClose._Flag)
               countFlags++;
            else countClear++;
         }
      // оставшимся установить флаги
      if ((state.getOpen() != EOpen._Nil) && ((countFlags+countClear) == state.getOpen().ordinal()))
         for (BaseCell nCell : neighbors) {
            if (nCell == null) continue; // существует ли сосед?
            if ((nCell.state.getStatus() == EState._Open) ||
                (nCell.state.getClose()  == EClose._Flag)) continue;
            nCell.state.setClose(EClose._Flag);
            result.modified.add(nCell);
         }
      if (!isMy)
         return result;

      // открыть оставшиеся
      if ((countFlags+result.getCountFlag()) == state.getOpen().ordinal())
         for (BaseCell nCell : neighbors) {
            if (nCell == null) continue; // существует ли сосед?
            if ((nCell.state.getStatus() == EState._Open) ||
                (nCell.state.getClose()  == EClose._Flag)) continue;
            nCell.state.setDown(true);
            nCell.state.setStatus(EState._Open);
            result.modified.add(nCell);
            if (nCell.state.getOpen() == EOpen._Nil) {
               ClickCellResult result2 = nCell.leftButtonUp(true, matrix); // TODO на больших размерах поля и при маленьком числе мин, приводит к StackOverflowException
               result.modified.addAll(result2.modified);
            }
            if (nCell.state.getOpen() == EOpen._Mine)
               return result;
         }
      return result;
   }

   public ClickCellResult rightButtonDown(EClose close) {
      ClickCellResult result = new ClickCellResult();
      if ((state.getStatus() == EState._Open) || state.isDown())
         return result;

      state.setClose(close);
      result.modified.add(this);
      return result;
   }

   /** <ul> Вернуть цвет заливки ячеки в зависимости от
    * <li> режима заливки фона ячеек
    * <li> координаты ячейки
    * <li> направления ячейки
    * <li> ... - как придумает дочерний класс
    */
   public Color getBackgroundFillColor(int fillMode, Color defaultColor, Map<Integer, Color> repositoryColor) {
      switch (fillMode) {
      default:
         System.err.println(getClass().getSimpleName()+".getBackgroundFillColor: fillMode="+fillMode+":  добавь цветовую обработку для этого режима!");
         //break;// !!! без break'а
      case 0:
         if ((getState().getStatus() == EState._Open) && (getState().getOpen() == EOpen._Mine) && getState().isDown())
            return Color.Red.brighter(0.05); // game ower: игра завершена - клик на мине
         if ((getState().getStatus() == EState._Open) && (getState().getOpen() != EOpen._Mine) && (getState().getClose() == EClose._Flag))
            return Color.Magenta.brighter(0.3); // game ower: игра завершена - не верно проставлен флаг (на ячейке с цифрой)

         // для Down и Нажатого состояний делаю фон чуть и чуть-чуть темнее...
         if (getState().isDown())
            return defaultColor.darker((getState().getStatus() == EState._Close) ? 0.15 : 0.25);
         return defaultColor;

      case 1:
         return repositoryColor.get(getDirection());
      case 2:
         {
            // подсветить каждую i-тую строку c шагом div
            final int i = 2;
            final int div = 5;
            int tmp1 = getCoord().x % div;
            int tmp2 = (getCoord().y-tmp1) % div;
            return repositoryColor.get((((tmp1 + tmp2) % div) == i) ? 0 : 1 );
         }
      case 3:
         {
            // дуршлаг
            final int i = 3;
            final int div = 4;
            int tmp1 = getCoord().x % div;
            int tmp2 = (getCoord().y+tmp1) % div;
            return repositoryColor.get((((tmp1 + tmp2) % div) == i) ? 0 : 1 );
         }
      case 4:
         {
            // ход конём
            final int i = 3;
            final int div = 5;
            int tmp1 = getCoord().x % div;
            int tmp2 = (getCoord().y+tmp1) % div;
            return repositoryColor.get((((tmp1 + tmp2) % div) == i) ? 0 : 1 );
         }
      case 5:
         {
            // волны
            final int div = 15;
            int tmp1 = getCoord().x % div;
            int tmp2 = (getCoord().y+tmp1) % div;
            return repositoryColor.get((tmp1 + tmp2) % div);
         }
      case 6:
         {
            final int div = 4;
            return repositoryColor.get(((getCoord().x%div + getCoord().y%div) == div) ? 0 : 1);
         }
      case 7: case 8: case 9:
         return repositoryColor.get(getCoord().x % (-5+fillMode));
      case 10: case 11: case 12:
         return repositoryColor.get(getCoord().y % (-8+fillMode));
      case 13: case 14: case 15:
      case 16: case 17: case 18:
         return repositoryColor.get(getCoord().x % (-fillMode) - fillMode + getCoord().y % (+fillMode));
      case 19:
         // подсветить direction
         int zx = getCoord().x / getAttr().getDirectionSizeField().width +1;
         int zy = getCoord().y / getAttr().getDirectionSizeField().height +1;
         return repositoryColor.get(zx*zy);
      }
   }

   @Override
   public String toString() {
      return getClass().getSimpleName()+"{ x:"+coord.x+", y:"+coord.y+" }";
   }
}
