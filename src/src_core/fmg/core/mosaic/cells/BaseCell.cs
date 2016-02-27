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
using System;
using System.ComponentModel;
using System.Linq;
using fmg.common;
using fmg.common.geom;
using fmg.core.types;
using fmg.core.types.click;

namespace fmg.core.mosaic.cells {

/// <summary>Базовый класс фигуры-ячейки</summary>
public abstract class BaseCell {
   public const double PI = 3.14159265358979323846; // Math.PI;
   public static readonly double SQRT2   = Math.Sqrt(2.0);
   public static readonly double SQRT3   = Math.Sqrt(3.0);
   public static readonly double SQRT27  = Math.Sqrt(27.0);
   public static readonly double SQRT48  = Math.Sqrt(48.0);
   public static readonly double SQRT147 = Math.Sqrt(147.0);
   public static readonly double SIN15   = Math.Sin(PI/180.0*15.0);
   public static readonly double SIN18   = Math.Sin(PI/180.0*18.0);
   public static readonly double SIN36   = Math.Sin(PI/180.0*36.0);
   public static readonly double SIN54   = Math.Sin(PI/180.0*54.0);
   public static readonly double SIN72   = Math.Sin(PI/180.0*72.0);
   public static readonly double SIN75   = Math.Sin(PI/180.0*75.0);
   public static readonly double SIN99   = Math.Sin(PI/180.0*99.0);
   public static readonly double TAN15   = Math.Tan(PI/180.0*15.0);
   public static readonly double TAN45_2 = Math.Tan(PI/180.0*45.0/2);
   public static readonly double SIN135a = Math.Sin(PI/180.0*135.0-Math.Atan(8.0/3));

   /*
    * Контекст/метаданные, описывающий общие хар-ки для каждого из экземпляров BaseCell.
    * <br> (Полные данные о конкретной мозаике) <br>
    * Доопределаяется наследниками BaseCell
    */
   public abstract class BaseAttribute : FastMines.Presentation.Notyfier.NotifyPropertyChanged {
      /// На PropertyChanged это подписаны все наследники BaseCell: при изменении A - надо пересчить все координаты точек

      public BaseAttribute(double area) {
         Area = area;
      }

      /// <summary>площадь ячейки/фигуры</summary>
      private double _area;

      /// <summary>площадь ячейки/фигуры</summary>
      public double Area {
         get { return _area; }
         set { this.SetProperty(ref this._area, value); }
      }

      /// <summary>размер квадрата, вписанного в фигуру - область куда выводиться изображение/текст
      /// на основе заданных параметров</summary>
      public abstract double GetSq(int borderWidth);

      /// <summary>значение A (базовая величина фигуры - обычно это размер одной из сторон фигуры) по заданной площади фигуры</summary>
      public abstract double A { get; }

      /// <summary>get parent container (owner window) size in pixels</summary>
      public abstract SizeDouble GetOwnerSize(Matrisize sizeField);

      /// <summary>размер поля из группы ячеек состоящих из разных direction</summary>
      public abstract Size GetDirectionSizeField();
      /// <summary>кол-во direction'ов, которые знает данный тип мозаики</summary>
      public int GetDirectionCount() { Size s = GetDirectionSizeField(); return s.Width*s.Height; }

      /// <summary>кол-во соседей (максимум или минимум)</summary>
      public virtual int getNeighborNumber(bool max) {
         var str = Enumerable.Range(0, GetDirectionCount()).Select(getNeighborNumber);
         return max ? str.Max() : str.Min();
      }
      /// <summary>кол-во соседей у ячейки конкретной направленности</summary>
      public abstract int getNeighborNumber(int direction);
      /// <summary>из скольки точек/вершин состоит фигура конкретной направленности</summary>
      public abstract int getVertexNumber(int direction);
      /// <summary>сколько фигур пересекается в одной вершине (в среднем)</summary>
      public abstract double getVertexIntersection(); 

      /// <summary>макс кол-во режимов заливки фона, которые знает данный тип мозаики
      /// (знает ф-ция BaseCell::getBackgroundFillColor() или её наследующая)
      /// (Не считая режима заливки цветом фона по-умолчанию...)</summary>
      public virtual int getMaxBackgroundFillModeValue() {
         return 19;
      }

   }

   private readonly BaseAttribute attr;
   public BaseAttribute Attr { get { return attr; } }

   //CellContext cellContext;
   protected Coord coord;
   /// <summary>направление - 'третья координата' ячейки</summary>
   protected int direction;

   /// <summary>вписанный в фигуру квадрат - область в которую выводится изображение/текст</summary>
   public abstract RectDouble getRcInner(int borderWidth);
   /// <summary>вернёт прямоугольник в который вписана фигура ячейки</summary>
   public RectDouble getRcOuter() {
      var rcOuter = region.GetBounds();
      rcOuter.Height++; rcOuter.Width++; // чтобы при repaint'е захватило и крайние границы
      return rcOuter;
   }

   /// <summary>соседние ячейки - с которыми граничит this</summary>
   private BaseCell[] neighbors;
   public BaseCell[] Neighbors { get { return neighbors; } }

   /// <summary>массив координат точек из которых состоит фигура</summary>
   protected RegionDouble region;

   public class StateCell {
      private readonly BaseCell owner;
      // { union
      private EState status; // _Open, _Close
      private EOpen  open;   // _Nil, _1, ... _21, _Mine
      private EClose close;  // _Unknown, _Clear, _Flag
      // } end union
      /// <summary>Нажата? Не путать с open! - ячейка может быть нажата, но ещё не открыта. Важно только для ф-ции прорисовки</summary>
      public bool Down { get; set; }
      public void setStatus(EState status, ClickReportContext clickRepContext) {
         if (clickRepContext != null)
            if (status == EState._Open)
               if (this.open == EOpen._Nil)
                  clickRepContext.setOpenNil.Add(owner);
               else
                  clickRepContext.setOpen.Add(owner);
         this.status = status;
      }
      public EState Status { get { return status; } }
      public void CalcOpenState() {
          if (this.open == EOpen._Mine) return;
          // подсчитать у соседей число мин и установить значение
          int count = 0;
          for (int i=0; i<owner.neighbors.Length; i++) {
             if (owner.neighbors[i] == null) continue; // существует ли сосед?
             if (owner.neighbors[i].state.open == EOpen._Mine) count++;
          }
          this.open = EOpenEx.GetValues()[count];
       }
       public bool SetMine() {
          if (owner.lockMine || (this.open == EOpen._Mine)) return false;
          this.open = EOpen._Mine;
          return true;
       }
       public EOpen Open { get { return this.open; } }
       public void setClose(EClose close, ClickReportContext clickRepContext) {
          if (clickRepContext != null)
             if ((    close == EClose._Flag) || // если устанавливаю флажок
                (this.close == EClose._Flag))   // если снимаю флажок
             {
                clickRepContext.setFlag.Add(owner);
             }
          this.close = close;
       }
       public EClose Close { get { return this.close; } }

       public StateCell(BaseCell self) { owner = self; Reset(); }
       public void Reset() {
          status = EState._Close;
          open = EOpen._Nil;
          close = EClose._Clear;
          Down = false;
       }
   }
   private StateCell state;
   /// <summary>запретить установку мины на данную ячейку</summary>
   private bool lockMine;

   public void LockNeighborMines() {
      lockMine = true;
      // запретить установку мин у соседей,
      for (int i=0; i<neighbors.Length; i++) {
         if (neighbors[i] == null) continue; // существует ли сосед?
         neighbors[i].lockMine = true;
      }
   }

   public StateCell State { get { return state; } }

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

      this.state = new StateCell(this);
      Reset();
   }

   public void Init() {
      CalcRegion();
   }

   /// <summary>
   /// Coord[] neighborCoord = new Coord[attr.getNeighborNumber()];
   /// <br>... потомки должны определить координаты соседей
   /// </summary>
   protected abstract Coord?[] GetCoordsNeighbor();

   /// <summary>матрица ячеек поля мозаики</summary>
   public interface IMatrixCells {
      /// <summary>размер поля</summary>
      Matrisize SizeField { get; set; }

      /// <summary>доступ к заданной ячейке</summary>
      BaseCell getCell(Coord coord);
   }
   /// <summary>для this определить ячейки-соседей, и проверить валидность их координат
   /// вызывать после изменений размера поля или типа мозаики</summary>
   public void IdentifyNeighbors(IMatrixCells matrix) {
      // получаю координаты соседних ячеек
      var neighborCoord = GetCoordsNeighbor();
      if (neighborCoord.Length != attr.getNeighborNumber(true))
         throw new Exception("neighborCoord.Length != GetNeighborNumber()");

      // проверяю что они не вылезли за размеры
      for (var i=0; i<neighborCoord.Length; i++)
         if (neighborCoord[i] != null)
            if ((neighborCoord[i].Value.x >= matrix.SizeField.m) ||
               (neighborCoord[i].Value.y >= matrix.SizeField.n) ||
               (neighborCoord[i].Value.x < 0) ||
               (neighborCoord[i].Value.y < 0))
            {
               neighborCoord[i] = null;
            }
      // по координатам получаю множество соседних обьектов-ячеек
      neighbors = new BaseCell[attr.getNeighborNumber(true)];
      for (var i=0; i<neighborCoord.Length; i++)
         if (neighborCoord[i] != null)
            neighbors[i] = matrix.getCell(neighborCoord[i].Value);
   }

   public Coord getCoord() { return coord; }
   public int getDirection() { return direction; }
   /// <summary>координата центра фигуры</summary>
   public PointDouble getCenter() { return getRcInner(1).Center(); }

   /// <summary>принадлежат ли эти экранные координаты ячейке</summary>
   public virtual bool PointInRegion(PointDouble point) { return region.Contains(point); }

   public RegionDouble getRegion() { return region; }

   /// <summary>определить координаты точек из которых состоит фигура</summary>
   protected abstract void CalcRegion();

   public void Reset() {
      state.Reset();
      lockMine = false;
   }

   /// <summary>Index where border change color</summary>
   public abstract int getShiftPointBorderIndex();


   public LeftDownResult LButtonDown() {
      if (state.Close  == EClose._Flag) return null;
      if (state.Status == EState._Close) {
         state.Down = true;
         LeftDownResult result1 = new LeftDownResult();
         result1.needRepaint.Add(this);
         return result1;
      }

      LeftDownResult result = null;
      // эффект нажатости для неоткрытых соседей
      if ((state.Status == EState._Open) && (state.Open != EOpen._Nil))
         for (int i=0; i<neighbors.Length; i++) {
            if (neighbors[i] == null) continue; // существует ли сосед?
            if ((neighbors[i].state.Status == EState._Open) ||
               (neighbors[i].state.Close  == EClose._Flag)) continue;
            neighbors[i].state.Down = true;
            if (result == null)
               result = new LeftDownResult();
            result.needRepaint.Add(neighbors[i]);
         }
      return result;
   }
   public LeftUpResult LButtonUp(bool isMy, ClickReportContext clickRepContext) {
      LeftUpResult result = new LeftUpResult(0, 0, 0, false, false);

      if (state.Close == EClose._Flag) return result;
      // избавится от эффекта нажатости
      if ((state.Status == EState._Open) && (state.Open != EOpen._Nil))
         for (int i=0; i<neighbors.Length; i++) {
            if (neighbors[i] == null) continue; // существует ли сосед?
            if ((neighbors[i].state.Status == EState._Open) ||
               (neighbors[i].state.Close  == EClose._Flag)) continue;
            neighbors[i].state.Down = false;
            result.addToRepaint(neighbors[i]);
         }
      // Открыть закрытую ячейку на которой нажали
      if (state.Status == EState._Close)
         if (!isMy) {
            state.Down = false;
            result.addToRepaint(this);
            return result;
         } else {
            result.countUnknown += (state.Close == EClose._Unknown) ? -1 : 0;
            result.countOpen++;
            state.setStatus(EState._Open, clickRepContext);
            state.Down = true;
            result.addToRepaint(this);
         }

      // ! В этой точке ячейка уже открыта
      // Подсчитываю кол-во установленных вокруг флагов и не открытых ячеек
      int countFlags = 0;
      int countClear = 0;
      if (state.Open != EOpen._Nil)
         for (int i=0; i<neighbors.Length; i++) {
            if (neighbors[i] == null) continue; // существует ли сосед?
            if (neighbors[i].state.Status == EState._Open) continue;
            if (neighbors[i].state.Close  == EClose._Flag)
               countFlags++;
            else countClear++;
         }
      // оставшимся установить флаги
      if ((state.Open != EOpen._Nil) && ((countFlags+countClear) == state.Open.Ordinal()))
         for (int i=0; i<neighbors.Length; i++) {
            if (neighbors[i] == null) continue; // существует ли сосед?
            if ((neighbors[i].state.Status == EState._Open) ||
               (neighbors[i].state.Close  == EClose._Flag)) continue;
            result.countUnknown += (neighbors[i].state.Close == EClose._Unknown) ? -1 : 0;
            result.countFlag++;
            neighbors[i].state.setClose(EClose._Flag, clickRepContext);
            result.addToRepaint(neighbors[i]);
         }
      if (!isMy) return result;
      // открыть оставшиеся
      if ((countFlags+result.countFlag) == state.Open.Ordinal())
         for (int i=0; i<neighbors.Length; i++) {
            if (neighbors[i] == null) continue; // существует ли сосед?
            if ((neighbors[i].state.Status == EState._Open) ||
               (neighbors[i].state.Close  == EClose._Flag)) continue;
            result.countUnknown += (neighbors[i].state.Close == EClose._Unknown) ? -1 : 0;
            result.countOpen++;
            neighbors[i].state.Down = true;
            neighbors[i].state.setStatus(EState._Open, clickRepContext);
            result.addToRepaint(neighbors[i]);
            if (neighbors[i].state.Open == EOpen._Nil) {
               LeftUpResult result2 = neighbors[i].LButtonUp(true, clickRepContext);
               result.countFlag    += result2.countFlag;
               result.countOpen    += result2.countOpen;
               result.countUnknown += result2.countUnknown;
               if (result.endGame) {
                  result.endGame = result2.endGame;
                  result.victory = result2.victory;
               }
               if (result2.needRepaint != null)
                  foreach (BaseCell cellToRepaint in result2.needRepaint)
                     result.addToRepaint(cellToRepaint);
            }
            if (neighbors[i].state.Open == EOpen._Mine) {
               result.endGame = true;
               result.victory = false;
               return result;
            }
         }
      if (state.Open == EOpen._Mine) {
         result.endGame = true;
         result.victory = false;
      }
      return result;
   }
   public RightDownReturn RButtonDown(EClose close, ClickReportContext clickRepContext) {
      RightDownReturn result = new RightDownReturn(0, 0);

      if ((state.Status == EState._Open) || state.Down) return result;
      switch (state.Close) {
      case EClose._Clear:
         switch (close) {
         case EClose._Flag:    result.countFlag    = +1;  break;
         case EClose._Unknown: result.countUnknown = +1;  break;
         default: break;
         }
         if (state.Close != close)
            state.setClose(close, clickRepContext);
         break;
      case EClose._Flag:
         switch (close) {
         case EClose._Unknown: result.countUnknown = +1;
                               result.countFlag    = -1; break;
         case EClose._Clear:   result.countFlag    = -1; break;
         default: break;
         }
         if (state.Close != close)
            state.setClose(close, clickRepContext);
         break;
      case EClose._Unknown:
         switch (close) {
         case EClose._Flag:    result.countFlag    = +1;
                               result.countUnknown = -1; break;
         case EClose._Clear:   result.countUnknown = -1; break;
         default: break;
         }
         if (state.Close != close)
            state.setClose(close, clickRepContext);
         break;
      }
      result.needRepaint = true;
      return result;
   }

   /// <summary>Вернуть цвет заливки ячеки в зависимости от
   /// * режима заливки фона ячеек
   /// * координаты ячейки
   /// * направления ячейки
   /// * ... - как придумает дочерний класс </summary>
   public virtual Color getBackgroundFillColor(int fillMode, Color defaultColor, Func<int, Color> repositoryColor) {
      switch (fillMode) {
      default:
         System.Diagnostics.Debug.Assert(false,this.GetType()+".getBackgroundFillColor: fillMode="+fillMode+":  добавь цветовую обработку для этого режима!");
         //break;// !!! без break'а
         goto case 0;
      case 0:
         if ((state.Status == EState._Open) && (state.Open == EOpen._Mine) && state.Down)
            return Color.Red.Brighter(0.05); // game ower: игра завершена - клик на мине
         if ((state.Status == EState._Open) && (state.Open != EOpen._Mine) && (state.Close == EClose._Flag))
            return Color.Magenta.Brighter(0.3); // game ower: игра завершена - не верно проставлен флаг (на ячейке с цифрой)

          // для Down и Нажатого состояний делаю фон чуть и чуть-чуть темнее...
         if (state.Down)
            return defaultColor.Darker((state.Status == EState._Close) ? 0.15 : 0.25);
         return defaultColor;

      case 1:
         return repositoryColor(getDirection());
      case 2:
         {
            // подсветить каждую i-тую строку c шагом div
            int i = 2;
            int div = 5;
            int tmp1 = getCoord().x % div;
            int tmp2 = (getCoord().y-tmp1) % div;
            return repositoryColor((((tmp1 + tmp2) % div) == i) ? 0 : 1 );
         }
      case 3:
         {
            // дуршлаг
            int i = 3;
            int div = 4;
            int tmp1 = getCoord().x % div;
            int tmp2 = (getCoord().y+tmp1) % div;
            return repositoryColor((((tmp1 + tmp2) % div) == i) ? 0 : 1 );
         }
      case 4:
         {
            // ход конём
            int i = 3;
            int div = 5;
            int tmp1 = getCoord().x % div;
            int tmp2 = (getCoord().y+tmp1) % div;
            return repositoryColor((((tmp1 + tmp2) % div) == i) ? 0 : 1);
         }
      case 5:
         {
            // волны
            int div = 15;
            int tmp1 = getCoord().x % div;
            int tmp2 = (getCoord().y+tmp1) % div;
            return repositoryColor((tmp1 + tmp2) % div);
         }
      case 6:
         {
            int div = 4;
            return repositoryColor(((getCoord().x % div + getCoord().y % div) == div) ? 0 : 1);
         }
      case 7: case 8: case 9:
         return repositoryColor(getCoord().x % (-5 + fillMode));
      case 10: case 11: case 12:
         return repositoryColor(getCoord().y % (-8 + fillMode));
      case 13: case 14: case 15:
      case 16: case 17: case 18:
         return repositoryColor(getCoord().x % (-fillMode) - fillMode + getCoord().y % (+fillMode));
      case 19:
         // подсветить direction
         var zx = getCoord().x / Attr.GetDirectionSizeField().Width + 1;
         var zy = getCoord().y / Attr.GetDirectionSizeField().Height + 1;
         return repositoryColor(zx * zy);
      }
   }

   public void OnPropertyChanged(object sender, PropertyChangedEventArgs e) {
      if ("Area".Equals(e.PropertyName))
         CalcRegion();
   }
}
}