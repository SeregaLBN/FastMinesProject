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

import java.util.Map;
import java.util.stream.IntStream;

import fmg.common.Color;
import fmg.common.geom.Coord;
import fmg.common.geom.DoubleExt;
import fmg.common.geom.Matrisize;
import fmg.common.geom.PointDouble;
import fmg.common.geom.RectDouble;
import fmg.common.geom.RegionDouble;
import fmg.common.geom.Size;
import fmg.common.geom.SizeDouble;
import fmg.common.notyfier.NotifyPropertyChanged;
import fmg.core.types.EClose;
import fmg.core.types.EOpen;
import fmg.core.types.EState;
import fmg.core.types.click.ClickReportContext;
import fmg.core.types.click.LeftDownResult;
import fmg.core.types.click.LeftUpResult;
import fmg.core.types.click.RightDownReturn;

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
    * Контекст/метаданные, описывающий общие хар-ки для каждого из экземпляров BaseCell.
    * <br> (Полные данные о конкретной мозаике) <br>
    * Доопределаяется наследниками BaseCell
    */
   public static abstract class BaseAttribute extends NotifyPropertyChanged {

      public BaseAttribute(double area) {
         super();
         this.area = area;
      }

      /** площадь ячейки/фигуры */
      private double area;

      /** площадь ячейки/фигуры */
      public void setArea(double area) {
         //setProperty(area, "Area");
         double old = this.area;
         if (!DoubleExt.hasMinDiff(old, area)) {
            this.area = area;
            onPropertyChanged(old, area, "Area");
         }
      }
      /** площадь ячейки/фигуры */
      public double getArea() { return area; }

      /** размер квадрата, вписанного в фигуру - область куда выводиться изображение/текст
       * на основе заданных параметров */
      public abstract double getSq(int borderWidth);

      /** значение A (базовая величина фигуры - обычно это размер одной из сторон фигуры) по заданной площади фигуры */
      protected abstract double getA();

      /** get parent container (owner window) size in pixels */
      public abstract SizeDouble getOwnerSize(Matrisize sizeField);

      /** размер поля из группы ячеек состоящих из разных direction */
      public abstract Size GetDirectionSizeField();
      /** кол-во direction'ов, которые знает данный тип мозаики */
      public int GetDirectionCount() { Size s = GetDirectionSizeField(); return s.width*s.height; }

      /** кол-во соседей (максимум или минимум) */
      public int getNeighborNumber(boolean max) {
         IntStream str = IntStream.range(0, GetDirectionCount()).map(d -> getNeighborNumber(d));
         return (max ? str.max() : str.min()).getAsInt();
      }
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

//    CellContext cellContext;
   protected Coord coord;
   /** направление - 'третья координата' ячейки */
   protected int direction;

   /** вписанный в фигуру квадрат - область в которую выводится изображение/текст */
   public abstract RectDouble getRcInner(int borderWidth);
   /** вернёт прямоугольник в который вписана фигура ячейки */
   public RectDouble getRcOuter() {
      RectDouble rcOuter = region.getBounds();
      rcOuter.height++; rcOuter.width++; // чтобы при repaint'е захватило и крайние границы
      return rcOuter;
   }

   /** соседние ячейки - с которыми граничит this */
   private BaseCell[] neighbors;
   public BaseCell[] getNeighbors() { return neighbors; }
   
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
       public void setStatus(EState status, ClickReportContext clickRepContext) {
          if (clickRepContext != null)
             if (status == EState._Open)
                if (this.open == EOpen._Nil)
                   clickRepContext.setOpenNil.add(BaseCell.this);
                 else
                    clickRepContext.setOpen.add(BaseCell.this);
          this.status = status;
       }
       public EState getStatus() { return status; }
       public void CalcOpenState() {
          if (this.open == EOpen._Mine) return;
          // подсчитать у соседей число мин и установить значение
          int count = 0;
          for (int i=0; i<neighbors.length; i++) {
             if (neighbors[i] == null) continue; // существует ли сосед?
             if (neighbors[i].getState().getOpen() == EOpen._Mine) count++;
          }
          this.open = EOpen.class.getEnumConstants()[count];
       }
       public boolean SetMine() {
          if (lockMine || (this.open == EOpen._Mine)) return false;
          this.open = EOpen._Mine;
          return true;
       }
       public EOpen getOpen() { return this.open; }
       public void setClose(EClose close, ClickReportContext clickRepContext) {
          if (clickRepContext != null)
             if ((     close == EClose._Flag) || // если устанавливаю флажок
                (this.close == EClose._Flag))   // если снимаю флажок
             {
                clickRepContext.setFlag.add(BaseCell.this);
             }
          this.close = close;
       }
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

   public void LockNeighborMines() {
      lockMine = true;
      // запретить установку мин у соседей,
      for (int i=0; i<neighbors.length; i++) {
         if (neighbors[i] == null) continue; // существует ли сосед?
         neighbors[i].lockMine = true;
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
      this.neighbors = null;

      this.state = new StateCell();
      Reset();
   }

   public void Init() {
      CalcRegion();
   }

   /**
    * Coord[] neighborCoord = new Coord[attr.getNeighborNumber()];
    * <br>... потомки должны определить координаты соседей
    * @return neighborCoord
    */
   protected abstract Coord[] GetCoordsNeighbor();

   /** матрица ячеек поля мозаики */
   public static interface IMatrixCells {
      /** размер поля */
      Matrisize getSizeField();
      void setSizeField(Matrisize size);

      /** доступ к заданной ячейке */
      BaseCell getCell(Coord coord);
   }
   /** для this определить ячейки-соседей, и проверить валидность их координат
    * <br> вызывать после изменений размера поля или типа мозаики
    **/
   public final void IdentifyNeighbors(IMatrixCells matrix) {
      // получаю координаты соседних ячеек
      Coord[] neighborCoord = GetCoordsNeighbor();
      if (neighborCoord.length != attr.getNeighborNumber(true))
         throw new RuntimeException("neighborCoord.length != GetNeighborNumber()");

      // проверяю что они не вылезли за размеры
      for (int i=0; i<neighborCoord.length; i++)
         if (neighborCoord[i] != null)
            if ((neighborCoord[i].x >= matrix.getSizeField().m) ||
               (neighborCoord[i].y >= matrix.getSizeField().n) ||
               (neighborCoord[i].x < 0) ||
               (neighborCoord[i].y < 0))
            {
               neighborCoord[i] = null;
            }
      // по координатам получаю множество соседних обьектов-ячеек
      neighbors = new BaseCell[attr.getNeighborNumber(true)];
      for (int i=0; i<neighborCoord.length; i++)
         if (neighborCoord[i] != null)
            neighbors[i] = matrix.getCell(neighborCoord[i]);
   }

   public Coord getCoord() { return coord; }
   public int getDirection() { return direction; }
   /** координата центра фигуры (в пикселях) */
   public PointDouble getCenter() { return getRcInner(1).center(); }

   /** принадлежат ли эти экранные координаты ячейке */
   public boolean PointInRegion(PointDouble point) { return region.Contains(point); }

   public RegionDouble getRegion() { return region; }

   /** определить координаты точек из которых состоит фигура */
   protected abstract void CalcRegion();

   public void Reset() {
      state.Reset();
      lockMine = false;
   }

   /** Index where border change color */
   public abstract int getShiftPointBorderIndex();


   public LeftDownResult LButtonDown() {
      if (state.getClose()  == EClose._Flag) return null;
      if (state.getStatus() == EState._Close) {
         state.setDown(true);
         LeftDownResult result = new LeftDownResult();
         result.needRepaint.add(this);
         return result;
      }

      LeftDownResult result = null;
      // эффект нажатости для неоткрытых соседей
      if ((state.getStatus() == EState._Open) && (state.getOpen() != EOpen._Nil))
         for (int i=0; i<neighbors.length; i++) {
            if (neighbors[i] == null) continue; // существует ли сосед?
            if ((neighbors[i].state.getStatus() == EState._Open) ||
               (neighbors[i].state.getClose()  == EClose._Flag)) continue;
            neighbors[i].state.setDown(true);
            if (result == null)
               result = new LeftDownResult();
            result.needRepaint.add(neighbors[i]);
         }
      return result;
   }
   public LeftUpResult LButtonUp(boolean isMy, ClickReportContext clickRepContext) {
      LeftUpResult result = new LeftUpResult(0, 0, 0, false, false);

      if (state.getClose() == EClose._Flag) return result;
      // избавится от эффекта нажатости
      if ((state.getStatus() == EState._Open) && (state.getOpen() != EOpen._Nil))
         for (int i=0; i<neighbors.length; i++) {
            if (neighbors[i] == null) continue; // существует ли сосед?
            if ((neighbors[i].state.getStatus() == EState._Open) ||
               (neighbors[i].state.getClose()  == EClose._Flag)) continue;
            neighbors[i].state.setDown(false);
            result.addToRepaint(neighbors[i]);
         }
      // Открыть закрытую ячейку на которой нажали
      if (state.getStatus() == EState._Close)
         if (!isMy) {
            state.setDown(false);
            result.addToRepaint(this);
            return result;
         } else {
            result.countUnknown += (state.getClose() == EClose._Unknown) ? -1 : 0;
            result.countOpen++;
            getState().setStatus(EState._Open, clickRepContext);
            getState().setDown(true);
            result.addToRepaint(this);
         }

      // ! В этой точке ячейка уже открыта
      // Подсчитываю кол-во установленных вокруг флагов и не открытых ячеек
      int countFlags = 0;
      int countClear = 0;
      if (state.getOpen() != EOpen._Nil)
         for (int i=0; i<neighbors.length; i++) {
            if (neighbors[i] == null) continue; // существует ли сосед?
            if (neighbors[i].state.getStatus() == EState._Open) continue;
            if (neighbors[i].state.getClose()  == EClose._Flag)
               countFlags++;
            else countClear++;
         }
      // оставшимся установить флаги
      if ((state.getOpen() != EOpen._Nil) && ((countFlags+countClear) == state.getOpen().ordinal()))
         for (int i=0; i<neighbors.length; i++) {
            if (neighbors[i] == null) continue; // существует ли сосед?
            if ((neighbors[i].state.getStatus() == EState._Open) ||
               (neighbors[i].state.getClose()  == EClose._Flag)) continue;
            result.countUnknown += (neighbors[i].state.getClose() == EClose._Unknown) ? -1 : 0;
            result.countFlag++;
            neighbors[i].state.setClose(EClose._Flag, clickRepContext);
            result.addToRepaint(neighbors[i]);
         }
      if (!isMy) return result;
      // открыть оставшиеся
      if ((countFlags+result.countFlag) == state.getOpen().ordinal())
         for (int i=0; i<neighbors.length; i++) {
            if (neighbors[i] == null) continue; // существует ли сосед?
            if ((neighbors[i].state.getStatus() == EState._Open) ||
               (neighbors[i].state.getClose()  == EClose._Flag)) continue;
            result.countUnknown += (neighbors[i].state.getClose() == EClose._Unknown) ? -1 : 0;
            result.countOpen++;
            neighbors[i].state.setDown(true);
            neighbors[i].state.setStatus(EState._Open, clickRepContext);
            result.addToRepaint(neighbors[i]);
            if (neighbors[i].state.getOpen() == EOpen._Nil) {
               LeftUpResult result2 = neighbors[i].LButtonUp(true, clickRepContext);
               result.countFlag    += result2.countFlag;
               result.countOpen    += result2.countOpen;
               result.countUnknown += result2.countUnknown;
               if (result.endGame) {
                  result.endGame = result2.endGame;
                  result.victory = result2.victory;
               }
               if (result2.needRepaint != null)
                  for (BaseCell cellToRepaint : result2.needRepaint)
                     result.addToRepaint(cellToRepaint);
            }
            if (neighbors[i].state.getOpen() == EOpen._Mine) {
               result.endGame = true;
               result.victory = false;
               return result;
            }
         }
      if (state.getOpen() == EOpen._Mine) {
         result.endGame = true;
         result.victory = false;
      }
      return result;
   }
   public RightDownReturn RButtonDown(EClose close, ClickReportContext clickRepContext) {
      RightDownReturn result = new RightDownReturn(0, 0);

      if ((state.getStatus() == EState._Open) || state.isDown()) return result;
      switch (state.getClose()) {
      case _Clear:
         switch (close) {
         case _Flag:    result.countFlag    = +1;  break;
         case _Unknown: result.countUnknown = +1;
         default: break;
         }
         if (state.getClose() != close)
            state.setClose(close, clickRepContext);
         break;
      case _Flag:
         switch (close) {
         case _Unknown: result.countUnknown = +1;
         case _Clear:   result.countFlag    = -1;
         default: break;
         }
         if (state.getClose() != close)
            state.setClose(close, clickRepContext);
         break;
      case _Unknown:
         switch (close) {
         case _Flag:    result.countFlag    = +1;
         case _Clear:   result.countUnknown = -1;
         default: break;
         }
         if (state.getClose() != close)
            state.setClose(close, clickRepContext);
      }
      result.needRepaint = true;
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
         int zx = getCoord().x / getAttr().GetDirectionSizeField().width +1;
         int zy = getCoord().y / getAttr().GetDirectionSizeField().height +1;
         return repositoryColor.get(zx*zy);
      }
   }

}
