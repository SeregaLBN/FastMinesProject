////////////////////////////////////////////////////////////////////////////////
//                               FastMines project
//                                      © Sergey Krivulya (KSerg, aka SeregaLBN)
// file name: "Mosaic.java"
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
using System.Threading.Tasks;
using ua.ksn.geom;
using ua.ksn.fmg.model.mosaics.cell;
using ua.ksn.fmg.model.mosaics;
using ua.ksn.fmg.controller.Event;
using ua.ksn.fmg.controller.types;
using ua.ksn.fmg.Event.click;

namespace ua.ksn.fmg.controller {

/// <summary> Mosaic field: класс окна мозаики поля </summary>
public abstract class Mosaic {

   /// <summary> размер поля в ячейках </summary>
   /// <returns></returns>
   public Size SizeField { get {
      return Cells.Size;
   }}

   public const int AREA_MINIMUM = 230;
   /// <summary>использовать ли флажок на поле</summary>
   private bool _useUnknown = true;

   private BaseCell.BaseAttribute _cellAttr;
   public BaseCell.BaseAttribute CellAttr {
      private set {
         if (_cellAttr == null)
            return;
         if (value != null)
            throw new ArgumentException("Bad argument - support only null value!");
         _cellAttr = null;
      }
      get {
         if (_cellAttr == null)
            _cellAttr = CellFactory.CreateAttributeInstance(Cells.MosaicType, Area);
         return _cellAttr;
      }
   }

   /// <summary>матрица ячеек поля мозаики</summary>
   protected class MatrixCells : BaseCell.IMatrixCells {
      protected readonly Mosaic _mosaic;
      /// <summary>матрица List &lt; List &lt; BaseCell &gt; &gt; , представленная(развёрнута) в виде вектора</summary>
      protected IList<BaseCell> _matrix = new List<BaseCell>(0);
      /// <summary>размер поля в ячейках</summary>
      protected Size _size = new Size(0, 0);
      /// <summary>из каких фигур состоит мозаика поля</summary>
      protected EMosaic _mosaicType = EMosaic.eMosaicSquare1;
      /// <summary>кол-во мин на поле</summary>
      protected int _minesCount = 1;
      /// <summary>кол-во мин на поле до создания игры. Используется когда игра была создана, но ни одной мины не проставлено.</summary>
      protected int _oldMinesCount = 1;

      public MatrixCells(Mosaic owner) { _mosaic = owner; }

      /// <summary>размер поля в ячейках</summary>
      public Size Size { get { return _size; } set { setParams(value, null, null); } }
      /// <summary>тип мозаики</summary>
      public EMosaic MosaicType { get { return _mosaicType; } set { setParams(null, value, null); } }
      /// <summary>кол-во мин</summary>
      public int MinesCount { get { return _minesCount; } set { setParams(null, null, value); } }

      public virtual void setParams(Size? newSizeField, EMosaic? newMosaicType, int? newMinesCount) {
         var oldMosaicType = this._mosaicType;
         bool newMosaciType = (newMosaicType != this._mosaicType);
         bool recreateMatrix = 
            ((newSizeField  != null) && !newSizeField.Equals(this._size)) ||
            ((newMosaicType != null) && newMosaciType);

         var saveArea = _mosaic.Area;
         if (newSizeField != null)
            this._size = newSizeField.GetValueOrDefault();
         if (newMosaicType != null)
            if (this._mosaicType != newMosaicType) {
               this._mosaicType = newMosaicType.GetValueOrDefault();
               _mosaic.CellAttr = null;
            }
         if (newMinesCount != null) {
            if (newMinesCount == 0)
               this._oldMinesCount = this._minesCount;
            this._minesCount = newMinesCount.GetValueOrDefault();
         }
         _minesCount = Math.Max(1, Math.Min(_minesCount, _mosaic.GetMaxMines(this._size)));
         if (saveArea != _mosaic.Area)
            _mosaic.Area = saveArea;

         if (recreateMatrix) {
            BaseCell.BaseAttribute attr = _mosaic.CellAttr;

            // отписываю старые ячейки от уведомлений атрибута
            foreach (BaseCell cell in _matrix)
               attr.PropertyChanged -= cell.OnPropertyChanged;

            _matrix.Clear();
            _matrix = new List<BaseCell>(_size.width*_size.height);
            for (int i=0; i < _size.width; i++)
               for (int j=0; j < _size.height; j++) {
                  BaseCell cell = CellFactory.CreateCellInstance(attr, _mosaicType, new Coord(i, j));
                  _matrix.Add(/*i*size.height + j, */cell);
                  attr.PropertyChanged += cell.OnPropertyChanged; // подписываю новые ячейки на уведомления атрибута (изменение a -> перерасчёт координат)
               }

            foreach (BaseCell cell in _matrix)
               cell.IdentifyNeighbors(this);
         }

         _mosaic.fireOnChangeCounters();
         if (newMosaciType)
            _mosaic.fireOnChangeMosaicType(oldMosaicType);
      }

      protected virtual void OnError(String msg) {
         System.Diagnostics.Debug.WriteLine(msg);
#if WINDOWS_RT
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
         foreach (BaseCell cell in _matrix)
            cell.State.CalcOpenState();
      }
      /// <summary>arrange Mines - set random mines</summary>
      public void setMines_random(Coord firstClickCoord) {
         if (_minesCount == 0)
            _minesCount = _oldMinesCount;
         
         BaseCell firstClickCell = getCell(firstClickCoord);
         var firstClickNeighbors = firstClickCell.Neighbors;
         List<BaseCell> matrixClone = new List<BaseCell>(_matrix);
         matrixClone.Remove(firstClickCell); // исключаю на которой кликал юзер
         matrixClone.RemoveAll( x => firstClickNeighbors.Contains(x) ); // и их соседей
         int count = 0;
         Random rand = new Random();
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
         foreach (BaseCell cell in _matrix)
            cell.State.CalcOpenState();
      }

      public IList<BaseCell> All {
         get { return _matrix; }
      }

      public int CountOpen {
         get { return _matrix.Where(x => x.State.Status == EState._Open).Count(); }
      }
      public int CountFlag {
         get { return _matrix.Where(x => (x.State.Status == EState._Close) && (x.State.Close == EClose._Flag)).Count(); }
      }
      public int CountUnknown {
         get { return _matrix.Where(x => (x.State.Status == EState._Close) && (x.State.Close == EClose._Unknown)).Count(); }
      }
      
      /// <summary>доступ к заданной ячейке</summary>
      public BaseCell getCell(int x, int y) { return _matrix[x*_size.height + y]; }
      /// <summary>доступ к заданной ячейке</summary>
      public BaseCell getCell(Coord coord) { return getCell(coord.x, coord.y); }
   }
   protected MatrixCells _cells;
   /// <summary>матрица ячеек</summary>
   protected virtual MatrixCells Cells { get {
      if (_cells == null)
         _cells = new MatrixCells(this);
      return _cells;
   }}

   /// <summary>координаты ячейки на которой было нажато (но не обязательно что отпущено)</summary>
   private Coord _coordDown;
   protected Coord CoordDown {
      set { _coordDown = value; }
      get {
         if (_coordDown == null)
            _coordDown = Coord.INCORRECT_COORD;
         return _coordDown;
      }
   }

   /**
    *<br> Этапы игры:
    *<br>           GameNew()      GameBegin()     GameEnd()      GameNew()
    *<br>    time      |               |               |             |
    *<br>  -------->   | eGSCreateGame |               |             |
    *<br>              |  or eGSReady  |    eGSPlay    |   eGSEnd    |
    *<br>              \------ 1 -----/ \----- 2 -----/ \---- 3 ----/
    *<br>
    *<br> @see ua.ksn.fmg.types.EGameStatus
    *<br>
    *<br> PS: При этапе gsReady поле чисто - мин нет! Мины расставляются только после первого клика
    *<br>     Так сделал только лишь потому, чтобы первый клик выполнялся не на мине. Естественно
    *<br>     это не относится к случаю, когда игра была создана пользователем или считана из файла.
    */
   private EGameStatus _gameStatus = EGameStatus.eGSEnd;
   public EGameStatus GameStatus {
      get { return _gameStatus; }
      set { var old = _gameStatus; _gameStatus = value; fireOnChangeGameStatus(old); }
   }

   private EPlayInfo _playInfo = EPlayInfo.ePlayerUnknown;
   public EPlayInfo PlayInfo {
      get { return _playInfo; }
      set { _playInfo = EPlayInfoEx.setPlayInfo(_playInfo, value); }
   }

   private int _countClick;

   /// <summary>для load'a - координаты ячеек с минами</summary>
   private IList<Coord> _repositoryMines;
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

   public event OnClickEvent OnClick = delegate { };
   public event OnChangeCountersEvent OnChangeCounters = delegate { };
   public event OnChangeGameStatusEvent OnChangeGameStatus = delegate { };
   public event OnChangeAreaEvent OnChangeArea = delegate { };
   public event OnChangeMosaicTypeEvent OnChangeMosaicType = delegate { };

   /// <summary>уведомить о клике на мозаике</summary>
   private void fireOnClick(bool leftClick, bool down) { OnClick(this, leftClick, down); }
   private void fireOnChangeCounters() { OnChangeCounters(this); }
   /// <summary>уведомить об изменении статуса игры (новая игра, начало игры, конец игры)</summary>
   private void fireOnChangeGameStatus(EGameStatus oldValue) { OnChangeGameStatus(this, oldValue); }
   /// <summary>уведомить о изменении размера площади у ячейки</summary>
   private void fireOnChangeArea(int oldArea) { OnChangeArea(this, oldArea); }
   /// <summary>уведомить о изменении размера площади у ячейки</summary>
   private void fireOnChangeMosaicType(EMosaic oldMosaic) { OnChangeMosaicType(this, oldMosaic); }

   /// <summary>перерисовать ячейку; если null - перерисовать всё поле </summary>
   protected abstract void Repaint(BaseCell cell);
   
   /// <summary>Начать игру, т.к. произошёл первый клик на поле</summary>
   protected virtual void GameBegin(Coord firstClick) {
      Repaint(null);

      GameStatus = EGameStatus.eGSPlay;

      // set mines
      if (RepositoryMines.Count != 0) {
         PlayInfo = EPlayInfo.ePlayIgnor;
         Cells.setMines_LoadRepository(RepositoryMines);
      } else {
         Cells.setMines_random(firstClick);
      }
   }

   /// <summary>Завершить игру</summary>
   private void GameEnd(bool victory) {
      if (GameStatus == EGameStatus.eGSEnd) return;

      int realCountOpen = 0;
      { // открыть оставшeеся
//         ::SetCursor(::LoadCursor(NULL, IDC_WAIT));
         foreach (BaseCell cell in Cells.All)
            if (cell.State.Status == EState._Close) {
               if (victory) {
                  if (cell.State.Open == EOpen._Mine)
                  {
                     cell.State.setClose(EClose._Flag, null);
                  } else {
                     cell.State.setStatus(EState._Open, null);
                     cell.State.Down = true;
                  }
                  realCountOpen++;
               } else {
                  if ((cell.State.Open != EOpen._Mine) ||
                     (cell.State.Close != EClose._Flag))
                  {
                     cell.State.setStatus(EState._Open, null);
                  }
               }
               Repaint(cell);
            } else {
               realCountOpen++;
            }
//         ::SetCursor(::LoadCursor(NULL, IDC_ARROW));
      }
      //BeepSpeaker();

      GameStatus = EGameStatus.eGSEnd;
      fireOnChangeCounters();
   }

   private void VerifyFlag() {
      if (GameStatus == EGameStatus.eGSEnd) return;
      if (Cells.MinesCount == Cells.CountFlag) {
         foreach (BaseCell cell in Cells.All)
            if ((cell.State.Close == EClose._Flag) &&
               (cell.State.Open != EOpen._Mine))
               return; // неверно проставленный флажок - на выход
         GameEnd(true);
      } else
         if (Cells.MinesCount == (Cells.CountFlag + Cells.CountUnknown)) {
            foreach (BaseCell cell in Cells.All)
               if (((cell.State.Close == EClose._Unknown) ||
                  ( cell.State.Close == EClose._Flag)) &&
                  ( cell.State.Open != EOpen._Mine))
                  return; // неверно проставленный флажок или '?'- на выход
            GameEnd(true);
         }
      return;
   }

   protected void OnLeftButtonDown(Coord coordLDown) {
      CoordDown = Coord.INCORRECT_COORD;
      if (GameStatus == EGameStatus.eGSEnd)
         return;
      if (coordLDown.Equals(Coord.INCORRECT_COORD))
         return;
      fireOnClick(true, true);

      CoordDown = coordLDown;
      BaseCell cell = getCell(coordLDown);
      if (GameStatus == EGameStatus.eGSCreateGame) {
         if (cell.State.Open != EOpen._Mine) {
            cell.State.setStatus(EState._Open, null);
            cell.State.SetMine();
            Cells.MinesCount = Cells.MinesCount+1;
            RepositoryMines.Add(coordLDown);
         } else {
            cell.Reset();
            Cells.MinesCount = Cells.MinesCount-1;
            RepositoryMines.Remove(coordLDown);
         }
         Repaint(cell);
      } else {
         LeftDownResult result = cell.LButtonDown();
         if ((result != null) && (result.needRepaint != null))
            foreach (BaseCell cellToRepaint in result.needRepaint)
               Repaint(cellToRepaint);
      }
   }

   protected void OnLeftButtonUp(Coord coordLUp) {
      if (GameStatus == EGameStatus.eGSEnd)
         return;
      if (CoordDown.Equals(Coord.INCORRECT_COORD))
         return;
      fireOnClick(true, false);
      if (GameStatus == EGameStatus.eGSCreateGame)
         return;

//      System.out.println("OnLeftButtonUp: coordLUp="+coordLUp);
      if ((GameStatus == EGameStatus.eGSReady) && coordLUp.Equals(CoordDown))
      {
         GameBegin(CoordDown);
      }
      ClickReportContext clickReportContext = new ClickReportContext();
      LeftUpResult result = getCell(CoordDown).LButtonUp(coordLUp.Equals(CoordDown), clickReportContext);
      if (result.needRepaint != null)
         foreach (BaseCell cellToRepaint in result.needRepaint)
            Repaint(cellToRepaint);
      if ((result.countOpen > 0) || (result.countFlag > 0) || (result.countUnknown > 0)) { // клик со смыслом (были изменения на поле)
         IncrementCountClick();
         PlayInfo = EPlayInfo.ePlayerUser;  // юзер играл
         fireOnChangeCounters();
      }

      if (result.endGame) {
         GameEnd(result.victory);
      } else {
         Size sizeField = Cells.Size;
         if ((Cells.CountOpen + Cells.MinesCount) == sizeField.width*sizeField.height) {
            GameEnd(true);
         } else {
            VerifyFlag();
         }
      }
   }

   protected void OnRightButtonDown(Coord coordRDown) {
      if (GameStatus == EGameStatus.eGSEnd) {
         GameNew();
         return;
      }
      if (GameStatus == EGameStatus.eGSReady)
         return;
      if (GameStatus == EGameStatus.eGSCreateGame)
         return;
      if (coordRDown.Equals(Coord.INCORRECT_COORD))
         return;
      fireOnClick(false, true);

      EClose eClose;
      BaseCell cell = getCell(coordRDown);
      switch (cell.State.Close) {
      case EClose._Clear: eClose = EClose._Flag; break;
      case EClose._Flag : eClose = UseUnknown ? EClose._Unknown : EClose._Clear; break;
      default:
      //case EClose._Unknown:
         eClose = EClose._Clear;
         break;
      }
      ClickReportContext clickReportContext = new ClickReportContext();
      RightDownReturn result = cell.RButtonDown(eClose, clickReportContext);
      if (result.needRepaint)
         Repaint(cell);
      if ((result.countFlag>0) || (result.countUnknown>0)) { // клик со смыслом (были изменения на поле)
         IncrementCountClick();
         PlayInfo = EPlayInfo.ePlayerUser; // то считаю что юзер играл
         fireOnChangeCounters();
      }

      VerifyFlag();
      if (GameStatus != EGameStatus.eGSEnd) {
         //...
      }
   }

   protected void OnRightButtonUp(/*Coord coordRUp*/) {
      fireOnClick(false, false);
   }

   protected virtual async System.Threading.Tasks.Task<bool> RequestToUser_RestoreLastGame() {
      //  need override in child class
      var msg = "Restore last game?";
      System.Diagnostics.Debug.WriteLine(msg);
#if WINDOWS_RT
#elif WINDOWS_FORMS
         System.Console.WriteLine(msg);
#else
         ...
#endif

      return await new Task<bool>(() => false);
   }

   /// <summary>Подготовиться к началу игры - сбросить все ячейки</summary>
   public virtual async System.Threading.Tasks.Task GameNew() {
//      System.out.println("Mosaic::GameNew()");

      if (GameStatus == EGameStatus.eGSReady)
         return;

      if (RepositoryMines.Count != 0)
         if (GameStatus == EGameStatus.eGSCreateGame) {
         } else {
            if (await RequestToUser_RestoreLastGame())
               RepositoryMines.Clear();
         }

      foreach (BaseCell cell in Cells.All)
         cell.Reset();

      ResetCountClick();

      GameStatus = EGameStatus.eGSReady;
      PlayInfo = EPlayInfo.ePlayerUnknown; // пока не знаю кто будет играть
   }

   /// <summary>создать игру игроком - он сам расставит мины</summary>
   public void GameCreate() {
      GameNew();
      if (RepositoryMines.Count == 0) {
         Cells.MinesCount = 0;
         GameStatus = EGameStatus.eGSCreateGame;
         fireOnChangeCounters();
      }
   }

   /// <summary>установить мозаику заданного размера, типа и с определённым количеством мин</summary>
   public void setParams(Size sizeField, EMosaic mosaicType, int minesCount) {
      setParams(sizeField, mosaicType, minesCount, null);
   }

   /// <summary>установить мозаику заданного размера, типа  и с определённым количеством мин (координаты мин могут задаваться с помощью "Хранилища Мин")</summary>
   public void setParams(Size sizeField, EMosaic mosaicType, int minesCount, List<Coord> storageCoordMines)
   {
      //repositoryMines.Reset();
      if ((Cells.MosaicType == mosaicType) &&
         Cells.Size.Equals(sizeField) &&
         (Cells.MinesCount == minesCount))
      {
         GameNew();
         return;
      }

      CoordDown = Coord.INCORRECT_COORD; // чтобы небыло IndexOutOfBoundsException при уменьшении размера поля когда удерживается клик на поле...

      Cells.setParams(sizeField, mosaicType, minesCount);
      if ((storageCoordMines == null) || (storageCoordMines.Count == 0))
         RepositoryMines.Clear();
      else
         RepositoryMines = storageCoordMines;
      //GameStatus = EGameStatus.eGSEnd;
      GameNew();
   }

   /// <summary>доступ к заданной ячейке</summary>
   public BaseCell getCell(int x, int y) { return Cells.getCell(x, y); }
   /// <summary>доступ к заданной ячейке</summary>
   public BaseCell getCell(Coord coord) { return Cells.getCell(coord); }
//   private void setCell(Coord coord, BaseCell cell) { mosaic.get(coord.x).set(coord.y, cell); }

   /// <summary>количество мин</summary>
   public int MinesCount { get { return Cells.MinesCount; } }
   /// <summary>узнать тип мозаики</summary>
   public EMosaic MosaicType { get { return Cells.MosaicType; } }
   /// <summary>площадь ячеек</summary>
   public virtual int Area {
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
         if (oldArea == Math.Max(AREA_MINIMUM, value))
            return;
         CellAttr.Area = Math.Max(AREA_MINIMUM, value);
         fireOnChangeArea(oldArea);
      }
   }
   public bool UseUnknown {
      set { _useUnknown = value; }
      get { return _useUnknown; }
   }

   /// <summary> Максимальное кол-во мин при указанном размере поля </summary>
   public int GetMaxMines(Size sizeFld) {
      int iMustFreeCell = NeighborNumber+1;
      int iMaxMines = sizeFld.width*sizeFld.height-iMustFreeCell;
      return Math.Max(1, iMaxMines);
   }
   /// <summary> Максимальное кол-во мин при  текущем  размере поля </summary>
   public int GetMaxMines() { return GetMaxMines(SizeField); }
   /// <summary> размер в пикселях для указанных параметров </summary>
   public Size CalcWindowSize(Size sizeField, int area) { return CellAttr.CalcOwnerSize(sizeField, area); }
   /// <summary> размер в пикселях </summary>
   public Size WindowSize { get { return CalcWindowSize(SizeField, Area); }}
   /// <summary> узнать количество соседей для текущей мозаики </summary>
   public int NeighborNumber { get { return CellAttr.getNeighborNumber(); } }

   /// <summary>сколько ещё осталось открыть мин</summary>
   public int CountMinesLeft { get { return Cells.MinesCount - Cells.CountFlag; } }
   public int CountClick { get { return _countClick; } }
   private void ResetCountClick()     { _countClick=0; fireOnChangeCounters();}
   private void IncrementCountClick() { _countClick++; fireOnChangeCounters(); }
   public int CountOpen { get { return Cells.CountOpen; } }
   /// <summary>действительно лишь когда gameStatus == gsEnd</summary>
   public bool IsVictory {
      get {
         return (GameStatus == EGameStatus.eGSEnd) && (0 == CountMinesLeft);
      }
   }

   /// <summary>Mosaic field: класс окна мозаики поля</summary>
   public Mosaic() {
      initialize();
   }
   /// <summary>Mosaic field: класс окна мозаики поля</summary>
   public Mosaic(Size sizeField, EMosaic mosaicType, int minesCount, int area) {
      initialize(sizeField, mosaicType, minesCount, area);
   }

   public IList<Coord> StorageMines {
      get {
         IList<Coord> repositoryMines = new List<Coord>();
         foreach (BaseCell cell in Cells.All)
            if (cell.State.Open == EOpen._Mine)
               repositoryMines.Add(cell.getCoord());
         return repositoryMines;
      }
   }

   protected void initialize() {
      initialize(new Size(10, 10),
            EMosaic.eMosaicSquare1,//EMosaic.eMosaicPenrousePeriodic1, // 
            15, AREA_MINIMUM*10);
   }
   protected void initialize(Size sizeField, EMosaic mosaicType, int minesCount, int area) {
      setParams(sizeField, mosaicType, minesCount);
      Area = area; // ...провера на валидность есть только при установке из класса Main. Так что, не нуна тут задавать громадные велечины.
   }
}
}