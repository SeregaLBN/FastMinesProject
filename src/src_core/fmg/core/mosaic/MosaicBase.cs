////////////////////////////////////////////////////////////////////////////////
//                               FastMines project
//                                      © Sergey Krivulya (KSerg, aka SeregaLBN)
// file name: "MosaicBase.cs"
//
// реализация алгоритма Мозаики состоящей из ячеек
// Copyright (C) 2011 Sergey Krivulya
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
using System.Linq;
using System.Collections.Generic;
using System.ComponentModel;
using fmg.common.geom;
using fmg.core.types;
using fmg.core.mosaic.cells;
using fmg.core.mosaic.draw;
using fmg.core.types.click;
using FastMines.Presentation.Notyfier;

namespace fmg.core.mosaic {

/// <summary> Mosaic field: класс окна мозаики поля </summary>
public abstract class MosaicBase<TPaintable> : NotifyPropertyChanged, IMosaic<TPaintable> where TPaintable : IPaintable {

#region Members

   public const double AREA_MINIMUM = 230;

   /// <summary>матрица List &lt; List &lt; BaseCell &gt; &gt; , представленная(развёрнута) в виде вектора</summary>
   public IList<BaseCell> Matrix { get; protected set; } = new List<BaseCell>(0);
   /// <summary>размер поля в ячейках</summary>
   protected Matrisize _size = new Matrisize(0, 0);
   /// <summary>из каких фигур состоит мозаика поля</summary>
   protected EMosaic _mosaicType = EMosaic.eMosaicSquare1;
   /// <summary>кол-во мин на поле</summary>
   protected int _minesCount = 1;
   /// <summary>кол-во мин на поле до создания игры. Используется когда игра была создана, но ни одной мины не проставлено.</summary>
   protected int _oldMinesCount = 1;

   private EGameStatus _gameStatus = EGameStatus.eGSEnd;
   private EPlayInfo _playInfo = EPlayInfo.ePlayerUnknown;
   private int _countClick;

   /// <summary>для load'a - координаты ячеек с минами</summary>
   private IList<Coord> _repositoryMines;

   /// <summary>использовать ли флажок на поле</summary>
   private bool _useUnknown = true;

   private BaseCell.BaseAttribute _cellAttr;
#endregion

   public BaseCell.BaseAttribute CellAttr {
      private set {
         if (_cellAttr == null)
            return;
         if (value != null)
            throw new ArgumentException("Bad argument - support only null value!");
         _cellAttr = null;
      }
      get {
         if (_cellAttr == null) {
            _cellAttr = MosaicHelper.CreateAttributeInstance(MosaicType, Area);
            _cellAttr.PropertyChanged += OnCellAttributePropertyChanged;
         }
         return _cellAttr;
      }
   }

   public abstract ICellPaint<TPaintable> CellPaint { get; }

   /// <summary> размер поля в ячейках </summary>
   public Matrisize SizeField { get { return _size; } set { SetParams(value, null, null); } }

   /// <summary> тип мозаики </summary>
   public EMosaic MosaicType { get { return _mosaicType; } set { SetParams(null, value, null); } }

   /// <summary> кол-во мин </summary>
   public int MinesCount { get { return _minesCount; } set { SetParams(null, null, value); } }

   /// <summary> установить мозаику заданного размера, типа  и с определённым количеством мин (координаты мин могут задаваться с помощью "Хранилища Мин") </summary>
   public virtual void SetParams(Matrisize? newSizeField, EMosaic? newMosaicType, int? newMinesCount, List<Coord> storageCoordMines) {
      //repositoryMines.Reset();
      var oldMinesCount = MinesCount;
      var res = (MosaicType != newMosaicType) || !SizeField.Equals(newSizeField) || (oldMinesCount != newMinesCount);
      if (res)
      {
         var oldMosaicType = this._mosaicType;
         var oldMosaicSize = this._size;
         var isNewMosaic = (newMosaicType != null) && (newMosaicType != this._mosaicType);
         var isNewSizeFld = ((newSizeField != null) && !this._size.Equals(newSizeField));

         var saveArea = Area;
         if (isNewSizeFld) {
            CellDown = null; // чтобы небыло IndexOutOfBoundsException при уменьшении размера поля когда удерживается клик на поле...
            this._size = newSizeField.GetValueOrDefault();
         }
         if (isNewMosaic) {
            this._mosaicType = newMosaicType.GetValueOrDefault();
            CellAttr = null;
         }
         if (newMinesCount != null) {
            if (newMinesCount == 0)
               this._oldMinesCount = this._minesCount;
            this._minesCount = newMinesCount.GetValueOrDefault();
         }
         _minesCount = Math.Max(1, Math.Min(_minesCount, GetMaxMines(this._size)));
         if (!saveArea.HasMinDiff(Area))
            Area = saveArea;

         if (isNewMosaic || isNewSizeFld) {
            var attr = CellAttr;

            foreach (var cell in Matrix)
               // отписываю старые ячейки от уведомлений атрибута
               attr.PropertyChanged -= cell.OnPropertyChanged;

            Matrix.Clear();
            (Matrix as List<BaseCell>).Capacity = _size.m*_size.n;
            for (var i = 0; i < _size.m; i++)
               for (var j = 0; j < _size.n; j++) {
                  var cell = MosaicHelper.CreateCellInstance(attr, _mosaicType, new Coord(i, j));
                  Matrix.Add( /*i*size.height + j, */cell);

                  // подписываю новые ячейки на уведомления атрибута (изменение a -> перерасчёт координат)
                  attr.PropertyChanged += cell.OnPropertyChanged;
               }

            foreach (var cell in Matrix)
               cell.IdentifyNeighbors(this);
         }

         OnPropertyChanged(oldMinesCount, _minesCount, "MinesCount");
         OnPropertyChanged(-1, _minesCount, "CountMinesLeft");
         if (isNewMosaic)
            OnPropertyChanged(oldMosaicType, newMosaicType, "MosaicType");
         if (isNewSizeFld)
            OnPropertyChanged(oldMosaicSize, newSizeField, "SizeField");
         }
      if ((storageCoordMines == null) || (storageCoordMines.Count == 0))
         RepositoryMines.Clear();
      else
         RepositoryMines = storageCoordMines;
      //GameStatus = EGameStatus.eGSEnd;
      GameNew();
   }

   /// <summary>установить мозаику заданного размера, типа и с определённым количеством мин</summary>
   public virtual void SetParams(Matrisize? newSizeField, EMosaic? newMosaicType, int? newMinesCount) {
      SetParams(newSizeField, newMosaicType, newMinesCount, null);
   }

   protected virtual void OnError(String msg) {
      System.Diagnostics.Debug.WriteLine(msg);
#if WINDOWS_RT || WINDOWS_UWP
#elif WINDOWS_FORMS
      System.Console.Error(msg);
#else
      ...
#endif
   }

   /// <summary>arrange Mines</summary>
   public void setMines_LoadRepository(IList<Coord> repository) {
      foreach (Coord c in repository) {
         bool suc = getCell(c).State.SetMine();
         if (!suc)
            OnError("Проблемы с установкой мин... :(");
      }
      // set other CellOpen and set all Caption
      foreach (BaseCell cell in Matrix)
         cell.State.CalcOpenState();
   }
   /// <summary>arrange Mines - set random mines</summary>
   public void setMines_random(BaseCell firstClickCell) {
      if (_minesCount == 0)
         _minesCount = _oldMinesCount;
         
      var firstClickNeighbors = firstClickCell.Neighbors;
      List<BaseCell> matrixClone = new List<BaseCell>(Matrix);
      matrixClone.Remove(firstClickCell); // исключаю на которой кликал юзер
      matrixClone.RemoveAll( x => firstClickNeighbors.Contains(x) ); // и их соседей
      int count = 0;
      Random rand = new Random(Guid.NewGuid().GetHashCode());
      do {
         int len = matrixClone.Count;
         if (len == 0) {
            OnError("ээээ..... лажа......\r\nЗахотели установить больше мин чем возможно");
            _minesCount = count;
            break;
         }
         int i = rand.Next(len);
         BaseCell cellToSetMines = matrixClone[i];
         if (cellToSetMines.State.SetMine()) {
            count++;
            matrixClone.Remove(cellToSetMines);
         } else
            OnError("Мины должны всегда устанавливаться...");
      } while (count < _minesCount);

      // set other CellOpen and set all Caption
      foreach (BaseCell cell in Matrix)
         cell.State.CalcOpenState();
   }

   public int CountOpen { get { return Matrix.Count(x => x.State.Status == EState._Open); } }
   public int CountFlag { get { return Matrix.Count(x => (x.State.Status == EState._Close) && (x.State.Close == EClose._Flag)); } }
   public int CountUnknown { get { return Matrix.Count(x => (x.State.Status == EState._Close) && (x.State.Close == EClose._Unknown)); } }

   /// <summary>сколько ещё осталось открыть мин</summary>
   public int CountMinesLeft { get { return MinesCount - CountFlag; } }
   public int CountClick {
      get { return _countClick; }
      private set { SetProperty(ref _countClick, value); }
   }
      
   /// <summary> доступ к заданной ячейке </summary>
   public BaseCell getCell(int x, int y) { return Matrix[x*_size.n + y]; }
   /// <summary> доступ к заданной ячейке </summary>
   public BaseCell getCell(Coord coord) { return getCell(coord.x, coord.y); }

   /// <summary> ячейка на которой было нажато (но не обязательно что отпущено) </summary>
   protected BaseCell CellDown { get; set; }

   /**
    *<br> Этапы игры:
    *<br>           GameNew()      GameBegin()     GameEnd()      GameNew()
    *<br>    time      |               |               |             |
    *<br>  -------->   | eGSCreateGame |               |             |
    *<br>              |  or eGSReady  |    eGSPlay    |   eGSEnd    |
    *<br>              \------ 1 -----/ \----- 2 -----/ \---- 3 ----/
    *<br>
    *<br> @see fmg.core.types.EGameStatus
    *<br>
    *<br> PS: При этапе gsReady поле чисто - мин нет! Мины расставляются только после первого клика
    *<br>     Так сделал только лишь потому, чтобы первый клик выполнялся не на мине. Естественно
    *<br>     это не относится к случаю, когда игра была создана пользователем или считана из файла.
    */
   public EGameStatus GameStatus {
      get { return _gameStatus; }
      set { SetProperty(ref _gameStatus, value); }
   }

   public EPlayInfo PlayInfo {
      get { return _playInfo; }
      set { _playInfo = EPlayInfoEx.setPlayInfo(_playInfo, value); }
   }

   private IList<Coord> RepositoryMines {
      get {
         if (_repositoryMines == null)
            _repositoryMines = new List<Coord>(0);
         return _repositoryMines;
      }
      set {
         this._repositoryMines = value;
      }
   }

   /// <summary>перерисовать ячейку; если null - перерисовать всё поле </summary>
   protected abstract void Repaint(BaseCell cell);
   
   /// <summary>Начать игру, т.к. произошёл первый клик на поле</summary>
   protected virtual void GameBegin(BaseCell firstClickCell) {
      Repaint(null);

      GameStatus = EGameStatus.eGSPlay;

      // set mines
      if (RepositoryMines.Count != 0) {
         PlayInfo = EPlayInfo.ePlayIgnor;
         setMines_LoadRepository(RepositoryMines);
      } else {
         setMines_random(firstClickCell);
      }
   }

   /// <summary>Завершить игру</summary>
   private void GameEnd(bool victory) {
      if (GameStatus == EGameStatus.eGSEnd) return;

      // открыть оставшeеся
      foreach (var cell in Matrix)
         if (cell.State.Status == EState._Close) {
            if (victory) {
               if (cell.State.Open == EOpen._Mine)
               {
                  cell.State.Close = EClose._Flag;
               } else {
                  cell.State.Status = EState._Open;
                  cell.State.Down = true;
               }
            } else {
               if ((cell.State.Open != EOpen._Mine) ||
                  (cell.State.Close != EClose._Flag))
               {
                  cell.State.Status = EState._Open;
               }
            }
            Repaint(cell);
         }

      GameStatus = EGameStatus.eGSEnd;
      OnPropertyChanged("CountMinesLeft");
      OnPropertyChanged("CountFlag");
      OnPropertyChanged("CountOpen");
   }

   private void VerifyFlag() {
      if (GameStatus == EGameStatus.eGSEnd) return;
      if (MinesCount == CountFlag) {
         foreach (BaseCell cell in Matrix)
            if ((cell.State.Close == EClose._Flag) &&
               (cell.State.Open != EOpen._Mine))
               return; // неверно проставленный флажок - на выход
         GameEnd(true);
      } else {
         if (MinesCount == (CountFlag + CountUnknown)) {
            foreach (BaseCell cell in Matrix)
               if (((cell.State.Close == EClose._Unknown) ||
                  ( cell.State.Close == EClose._Flag)) &&
                  ( cell.State.Open != EOpen._Mine))
                  return; // неверно проставленный флажок или '?'- на выход
            GameEnd(true);
         }
      }
   }

   protected ClickResult OnLeftButtonDown(BaseCell cellLeftDown) {
      using (new FastMines.Common.Tracer("Mosaic::OnLeftButtonDown")) {
         var result = new ClickResult(cellLeftDown, true, true);
         CellDown = null;
         if (GameStatus == EGameStatus.eGSEnd)
            return result;
         if (cellLeftDown == null)
            return result;

         CellDown = cellLeftDown;
         if (GameStatus == EGameStatus.eGSCreateGame) {
            if (cellLeftDown.State.Open != EOpen._Mine) {
               cellLeftDown.State.Status = EState._Open;
               cellLeftDown.State.SetMine();
               MinesCount = MinesCount + 1;
               RepositoryMines.Add(cellLeftDown.getCoord());
            } else {
               cellLeftDown.Reset();
               MinesCount = MinesCount - 1;
               RepositoryMines.Remove(cellLeftDown.getCoord());
            }
            Repaint(cellLeftDown);
            result.Modified.Add(cellLeftDown);
         } else {
            var resultCell = cellLeftDown.LButtonDown();
            result.Modified = resultCell.Modified; // copy reference; TODO result.Modified.AddRange(resultCell.Modified);
            result.Modified.ForEach(Repaint);
         }
         return result;
      }
   }

   protected ClickResult OnLeftButtonUp(BaseCell cellLeftUp) {
      using (var tracer = new FastMines.Common.Tracer("Mosaic::OnLeftButtonUp"))
      try {
         var cellDown = CellDown;
         var result = new ClickResult(cellDown, true, false);
         if (GameStatus == EGameStatus.eGSEnd)
            return result;
         if (cellDown == null)
            return result;
         if (GameStatus == EGameStatus.eGSCreateGame)
            return result;

   //      System.out.println("OnLeftButtonUp: coordLUp="+coordLUp);
         if ((GameStatus == EGameStatus.eGSReady) && ReferenceEquals(cellDown, cellLeftUp))
         {
            GameBegin(CellDown);
         }
         var resultCell = cellDown.LButtonUp(ReferenceEquals(cellDown, cellLeftUp));
         result.Modified = resultCell.Modified; // copy reference; TODO result.Modified.AddRange(resultCell.Modified);
         tracer.Put(" result.Modified=" + result.Modified.Count);
         result.Modified.ForEach(Repaint);
         var countOpen = result.CountOpen;
         var countFlag = result.CountFlag;
         var countUnknown = result.CountUnknown;
         var any = (countOpen > 0) || (countFlag > 0) || (countUnknown > 0); // клик со смыслом (были изменения на поле)
         if (any) {
            CountClick++;
            PlayInfo = EPlayInfo.ePlayerUser;  // юзер играл
            if (countOpen > 0)
               OnPropertyChanged("CountOpen");
            if ((countFlag > 0) || (countUnknown > 0)) {
               OnPropertyChanged("CountFlag");
               OnPropertyChanged("CountMinesLeft");
               OnPropertyChanged("CountUnknown");
            }
         }

         if (result.IsAnyOpenMine) {
            GameEnd(false);
         } else {
            var sizeField = SizeField;
            if ((CountOpen + MinesCount) == sizeField.m*sizeField.n) {
               GameEnd(true);
            } else {
               VerifyFlag();
            }
         }
         return result;
      } finally {
         CellDown = null;
      }
   }

   protected ClickResult OnRightButtonDown(BaseCell cellRightDown) {
      using (var tracer = new FastMines.Common.Tracer("Mosaic::OnRightButtonDown")) {
         CellDown = null;
         var result = new ClickResult(cellRightDown, false, true); ;
         if (GameStatus == EGameStatus.eGSEnd) {
            GameNew();
            result.Modified.AddRange(Matrix); // ??? TODO optimize
            return result;
         }
         if (GameStatus == EGameStatus.eGSReady)
            return null;
         if (GameStatus == EGameStatus.eGSCreateGame)
            return null;
         if (cellRightDown == null)
            return null;

         CellDown = cellRightDown;
         var resultCell = cellRightDown.RButtonDown(cellRightDown.State.Close.NextState(UseUnknown));
         result.Modified = resultCell.Modified; // copy reference; TODO result.Modified.AddRange(resultCell.Modified);
         result.Modified.ForEach(Repaint);

         var countFlag = result.CountFlag;
         var countUnknown = result.CountUnknown;
         var any = (countFlag != 0) || (countUnknown != 0); // клик со смыслом (были изменения на поле)
         if (any) {
            CountClick++;
            PlayInfo = EPlayInfo.ePlayerUser; // то считаю что юзер играл
            OnPropertyChanged("CountFlag");
            OnPropertyChanged("CountMinesLeft");
            OnPropertyChanged("CountUnknown");
         }

         VerifyFlag();
         if (GameStatus != EGameStatus.eGSEnd) {
            //...
         }
         tracer.Put("any=" + any);
         return result;
      }
   }

   protected ClickResult OnRightButtonUp(BaseCell cellRightUp) {
      using (var tracer = new FastMines.Common.Tracer("Mosaic::OnRightButtonUp"))
      try {
         var cellDown = CellDown;
         //tracer.Put("return");
         return new ClickResult(cellDown, false, false);
      } finally {
         CellDown = null;
      }
   }

      /// <summary> Request to user </summary>
      public Func<bool> CheckNeedRestoreLastGame { get; set; }

   /// <summary>Подготовиться к началу игры - сбросить все ячейки</summary>
   public virtual void GameNew() {
//      System.out.println("Mosaic::GameNew()");

      if (GameStatus == EGameStatus.eGSReady)
         return;

      if (RepositoryMines.Count != 0)
         if (GameStatus == EGameStatus.eGSCreateGame) {
         } else {
            var func = CheckNeedRestoreLastGame;
            if ((func!= null) && func())
               RepositoryMines.Clear();
         }

      foreach (BaseCell cell in Matrix)
         cell.Reset();

      CountClick = 0;

      GameStatus = EGameStatus.eGSReady;
      PlayInfo = EPlayInfo.ePlayerUnknown; // пока не знаю кто будет играть
   }

   /// <summary>создать игру игроком - он сам расставит мины</summary>
   public void GameCreate() {
      GameNew();
      if (RepositoryMines.Count == 0) {
         MinesCount = 0;
         GameStatus = EGameStatus.eGSCreateGame;
      }
   }

   /// <summary>площадь ячеек</summary>
   public virtual double Area {
      get {
         if (_cellAttr == null)
            return AREA_MINIMUM;
         var area = CellAttr.Area;
         if (area < AREA_MINIMUM) {
            area = AREA_MINIMUM;
            CellAttr.Area = AREA_MINIMUM;
         }
         return area;
      }
      set {
         var oldArea = CellAttr.Area;
         value = Math.Max(AREA_MINIMUM, value);
         if (oldArea.HasMinDiff(value))
            return;
         CellAttr.Area = value;
      }
   }
   public bool UseUnknown {
      set { _useUnknown = value; }
      get { return _useUnknown; }
   }

   /// <summary> Максимальное кол-во мин при указанном размере поля </summary>
   public int GetMaxMines(Matrisize sizeFld) {
      var iMustFreeCell = MaxNeighborNumber+1;
      var iMaxMines = sizeFld.m*sizeFld.n-iMustFreeCell;
      return Math.Max(1, iMaxMines);
   }
   /// <summary> Максимальное кол-во мин при  текущем  размере поля </summary>
   public int GetMaxMines() { return GetMaxMines(SizeField); }
   /// <summary> размер в пикселях для указанных параметров </summary>
   public SizeDouble GetWindowSize(Matrisize sizeField, double area) {
      return area.HasMinDiff(Area)
         ? CellAttr.GetOwnerSize(sizeField)
         : MosaicHelper.GetOwnerSize(MosaicType, area, sizeField);
   }
   /// <summary> размер в пикселях </summary>
   public SizeDouble WindowSize { get { return GetWindowSize(SizeField, Area); }}
   /// <summary> узнать количество соседей для текущей мозаики </summary>
   public int MaxNeighborNumber { get { return CellAttr.getNeighborNumber(true); } }

   /// <summary>действительно лишь когда gameStatus == gsEnd</summary>
   public bool IsVictory {
      get {
         return (GameStatus == EGameStatus.eGSEnd) && (0 == CountMinesLeft);
      }
   }

   /// <summary>Mosaic field: класс окна мозаики поля</summary>
   public MosaicBase() {
      Initialize();
   }
   /// <summary>Mosaic field: класс окна мозаики поля</summary>
   public MosaicBase(Matrisize sizeField, EMosaic mosaicType, int minesCount, double area) {
      Initialize(sizeField, mosaicType, minesCount, area);
   }

   public IList<Coord> StorageMines => (from cell in Matrix
                                        where cell.State.Open == EOpen._Mine
                                        select cell.getCoord()).ToList();

   protected void Initialize() {
      Initialize(new Matrisize(5, 5),
            EMosaic.eMosaicPenrousePeriodic1, 
            1, AREA_MINIMUM);
   }

   protected void Initialize(Matrisize sizeField, EMosaic mosaicType, int minesCount, double area) {
      SetParams(sizeField, mosaicType, minesCount);
      Area = area; // ...провера на валидность есть только при установке из класса Main. Так что, не нуна тут задавать громадные велечины.
   }

   private void OnCellAttributePropertyChanged(object sender, PropertyChangedEventArgs ev) {
      var pn = ev.PropertyName;
      if (pn == "Area") {
         Matrix.ToList().ForEach(cell => cell.Init());
         OnPropertyChanged(this, ev); // ! rethrow event - notify parent class
      }
   }
}
}