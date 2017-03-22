////////////////////////////////////////////////////////////////////////////////
//                               FastMines project
//                                      © Sergey Krivulya (KSerg, aka SeregaLBN)
// file name: "Mosaic.cs"
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
using fmg.core.types.click;
using fmg.common.notyfier;

namespace fmg.core.mosaic
{

   /// <summary> MVC: model (mosaic field). Default implementation. </summary>
   public class Mosaic : NotifyPropertyChanged, IMosaic {

   #region Members

      public const double AREA_MINIMUM = 230;

      /// <summary>матрица List &lt; List &lt; BaseCell &gt; &gt; , представленная(развёрнута) в виде вектора</summary>
      private readonly List<BaseCell> _matrix = new List<BaseCell>(0);
      /// <summary>размер поля в ячейках</summary>
      protected Matrisize _size = new Matrisize(10, 10);
      /// <summary>из каких фигур состоит мозаика поля</summary>
      protected EMosaic _mosaicType = EMosaic.eMosaicSquare1;
      /// <summary>кол-во мин на поле</summary>
      protected int _minesCount = 10;
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

      public const string PROPERTY_MODIFIED_CELLS = "ToRepaint";

   #endregion

      public BaseCell.BaseAttribute CellAttr {
         private set {
            if (_cellAttr == null)
               return;
            if (value != null)
               throw new ArgumentException("Bad argument - support only null value!");
            _cellAttr.PropertyChanged -= OnCellAttributePropertyChanged;
            _cellAttr = null;
            OnSelfPropertyChanged();
         }
         get {
            if (_cellAttr == null) {
               _cellAttr = MosaicHelper.CreateAttributeInstance(MosaicType);
               _cellAttr.Area = 10 * AREA_MINIMUM;
               _cellAttr.PropertyChanged += OnCellAttributePropertyChanged;
            }
            return _cellAttr;
         }
      }

      /// <summary>площадь ячеек</summary>
      public virtual double Area {
         get {
            return CellAttr.Area;
         }
         set {
            CellAttr.Area = Math.Max(AREA_MINIMUM, value);
         }
      }

      public IList<BaseCell> Matrix { get {
            if (!_matrix.Any()) {
               var attr = CellAttr;
               var size = SizeField;
               var mosaicType = MosaicType;
               //_matrix = new ArrayList<BaseCell>(size.width * size.height);
               for (var i = 0; i < size.m; i++)
                  for (var j = 0; j < size.n; j++) {
                     var cell = MosaicHelper.CreateCellInstance(attr, mosaicType, new Coord(i, j));
                     _matrix.Add( /*i*size.height + j, */cell);
                  }
            }
            return _matrix;
         }
      }

      /// <summary> размер поля в ячейках </summary>
      public Matrisize SizeField {
         get { return _size; }
         set {
            if (!SetProperty(ref _size, value))
               return;

            CellDown = null; // чтобы не было IndexOutOfBoundsException при уменьшении размера поля когда удерживается клик на поле...

            _matrix.Clear();
            OnSelfPropertyChanged(nameof(this.Matrix));
            OnSelfPropertyChanged(nameof(this.WindowSize));

            GameNew();
         }
      }

      /// <summary> тип мозаики </summary>
      public EMosaic MosaicType {
         get { return _mosaicType; }
         set {
            if (_mosaicType == value)
               return;

            var saveArea = Area; // save

            CellAttr = null; //  // clean BaseCell.BaseAttribute before changing _mosaicType
            _matrix.Clear();

            SetProperty(ref _mosaicType, value);
            OnSelfPropertyChanged(nameof(this.Matrix));

            Area = saveArea; // restore
            GameNew();
         }
      }

      /// <summary> кол-во мин </summary>
      public int MinesCount {
         get { return _minesCount; }
         set {
            if (!SetProperty(ref _minesCount, value))
               return;

            if (value == 0) // TODO  ?? to create field mode - EGameStatus.eGSCreateGame
               _oldMinesCount = _minesCount; // save

            _minesCount = Math.Max(1, Math.Min(value, GetMaxMines(SizeField)));
            OnSelfPropertyChanged(-1, _minesCount, nameof(this.CountMinesLeft));

            GameNew();
         }
      }

      /// <summary>arrange Mines</summary>
      public void setMines_LoadRepository(IList<Coord> repository) {
         foreach (var c in repository) {
            var suc = getCell(c).State.SetMine();
            System.Diagnostics.Debug.Assert(suc, "Проблемы с установкой мин... :(");
         }
         // set other CellOpen and set all Caption
         foreach (var cell in Matrix)
            cell.State.CalcOpenState(this);
      }

      /// <summary>arrange Mines - set random mines</summary>
      public void setMines_random(BaseCell firstClickCell) {
         if (_minesCount == 0)
            _minesCount = _oldMinesCount;
         
         var matrixClone = new List<BaseCell>(Matrix);
         matrixClone.Remove(firstClickCell); // исключаю на которой кликал юзер
         foreach (var x in firstClickCell.GetNeighbors(this))
            matrixClone.Remove(x); // и их соседей
         var count = 0;
         var rand = new Random(Guid.NewGuid().GetHashCode());
         do {
            var len = matrixClone.Count;
            if (len == 0) {
               System.Diagnostics.Debug.Assert(false, "ээээ..... лажа......\r\nЗахотели установить больше мин чем возможно");
               _minesCount = count;
               break;
            }
            var i = rand.Next(len);
            var cellToSetMines = matrixClone[i];
            if (cellToSetMines.State.SetMine()) {
               count++;
               matrixClone.Remove(cellToSetMines);
            } else
               System.Diagnostics.Debug.Assert(false, "Мины должны всегда устанавливаться...");
         } while (count < _minesCount);

         // set other CellOpen and set all Caption
         foreach (var cell in Matrix)
            cell.State.CalcOpenState(this);
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
      public BaseCell CellDown { get; set; }

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
         set { SetProperty(ref _playInfo, EPlayInfoEx.setPlayInfo(_playInfo, value)); }
      }

      public IList<Coord> RepositoryMines {
         get { return _repositoryMines ?? (_repositoryMines = new List<Coord>(0)); }
         set {
            var current = RepositoryMines;
            var areEquivalent = (value != null) && (current.Count == value.Count) && !current.Except(value).Any();
            if (!areEquivalent) {
               current.Clear();
               if ((value != null) && value.Any())
                  foreach (var itm in value)
                     current.Add(itm);
            }
            OnSelfPropertyChanged();
            //setGameStatus(EGameStatus.eGSEnd);
            GameNew();
         }
      }

      /// <summary>Начать игру, т.к. произошёл первый клик на поле</summary>
      public virtual void GameBegin(BaseCell firstClickCell) {
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
      private IEnumerable<BaseCell> GameEnd(bool victory) {
         if (GameStatus == EGameStatus.eGSEnd)
            return Enumerable.Empty<BaseCell>();

         var toRepaint = new HashSet<BaseCell>();
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
                  toRepaint.Add(cell);
               } else {
                  if ((cell.State.Open != EOpen._Mine) ||
                     (cell.State.Close != EClose._Flag))
                  {
                     cell.State.Status = EState._Open;
                     toRepaint.Add(cell);
                  }
               }
            }

         GameStatus = EGameStatus.eGSEnd;
         OnSelfPropertyChanged(nameof(this.CountMinesLeft));
         OnSelfPropertyChanged(nameof(this.CountFlag));
         OnSelfPropertyChanged(nameof(this.CountOpen));

         return toRepaint;
      }

      private IEnumerable<BaseCell> VerifyFlag() {
         if (GameStatus == EGameStatus.eGSEnd) return Enumerable.Empty<BaseCell>();
         if (MinesCount == CountFlag) {
            foreach (BaseCell cell in Matrix)
               if ((cell.State.Close == EClose._Flag) &&
                  (cell.State.Open != EOpen._Mine))
                  return Enumerable.Empty<BaseCell>(); // неверно проставленный флажок - на выход
            return GameEnd(true);
         } else {
            if (MinesCount == (CountFlag + CountUnknown)) {
               foreach (BaseCell cell in Matrix)
                  if (((cell.State.Close == EClose._Unknown) ||
                     ( cell.State.Close == EClose._Flag)) &&
                     ( cell.State.Open != EOpen._Mine))
                     return Enumerable.Empty<BaseCell>(); // неверно проставленный флажок или '?'- на выход
               return GameEnd(true);
            }
         }
         return Enumerable.Empty<BaseCell>();
      }

      public ClickResult OnLeftButtonDown(BaseCell cellLeftDown) {
         using (new fmg.common.Tracer("Mosaic::OnLeftButtonDown")) {
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
               result.Modified.Add(cellLeftDown);
            } else {
               var resultCell = cellLeftDown.LButtonDown(this);
               result.Modified = resultCell.Modified; // copy reference; TODO result.Modified.AddRange(resultCell.Modified);
            }
            OnSelfModifiedCellsPropertyChanged(result.Modified);
            return result;
         }
      }

      public ClickResult OnLeftButtonUp(BaseCell cellLeftUp) {
         using (var tracer = new fmg.common.Tracer("Mosaic::OnLeftButtonUp", "coordLUp=" + cellLeftUp?.getCoord()))
         try {
            var cellDown = CellDown;
            var result = new ClickResult(cellDown, true, false);
            if (GameStatus == EGameStatus.eGSEnd)
               return result;
            if (cellDown == null)
               return result;
            if (GameStatus == EGameStatus.eGSCreateGame)
               return result;

            bool gameBegin = (GameStatus == EGameStatus.eGSReady) && ReferenceEquals(cellDown, cellLeftUp);
            if (gameBegin) {
               GameBegin(CellDown);
               result.Modified.UnionWith(this.Matrix);
            }
            var resultCell = cellDown.LButtonUp(ReferenceEquals(cellDown, cellLeftUp), this);
            if (!gameBegin)
               result.Modified.UnionWith(resultCell.Modified);
            tracer.Put(" result.Modified=" + result.Modified.Count);
            var countOpen = result.CountOpen;
            var countFlag = result.CountFlag;
            var countUnknown = result.CountUnknown;
            var any = (countOpen > 0) || (countFlag > 0) || (countUnknown > 0); // клик со смыслом (были изменения на поле)
            if (any) {
               CountClick++;
               PlayInfo = EPlayInfo.ePlayerUser;  // юзер играл
               if (countOpen > 0)
                  OnSelfPropertyChanged(nameof(this.CountOpen));
               if ((countFlag > 0) || (countUnknown > 0)) {
                  OnSelfPropertyChanged(nameof(this.CountFlag));
                  OnSelfPropertyChanged(nameof(this.CountMinesLeft));
                  OnSelfPropertyChanged(nameof(this.CountUnknown));
               }
            }

            IEnumerable<BaseCell> modified;
            if (result.IsAnyOpenMine) {
               modified = GameEnd(false);
            } else {
               var sizeField = SizeField;
               if ((CountOpen + MinesCount) == sizeField.m*sizeField.n) {
                  modified = GameEnd(true);
               } else {
                  modified = VerifyFlag();
               }
            }

            if (!gameBegin)
               result.Modified.UnionWith(modified);
            OnSelfModifiedCellsPropertyChanged(result.Modified);

            return result;
         } finally {
            CellDown = null;
         }
      }

      public ClickResult OnRightButtonDown(BaseCell cellRightDown) {
         using (var tracer = new fmg.common.Tracer("Mosaic::OnRightButtonDown")) {
            CellDown = null;
            var result = new ClickResult(cellRightDown, false, true);
            if (GameStatus == EGameStatus.eGSEnd) {
               GameNew();
               return result;
            }
            if (GameStatus == EGameStatus.eGSReady)
               return result;
            if (GameStatus == EGameStatus.eGSCreateGame)
               return result;
            if (cellRightDown == null)
               return result;

            CellDown = cellRightDown;
            var resultCell = cellRightDown.RButtonDown(cellRightDown.State.Close.NextState(UseUnknown));
            result.Modified = resultCell.Modified; // copy reference; TODO modify to result.Modified.Add(resultCell.Modified);

            var countFlag = result.CountFlag;
            var countUnknown = result.CountUnknown;
            var any = (countFlag != 0) || (countUnknown != 0); // клик со смыслом (были изменения на поле)
            if (any) {
               CountClick++;
               PlayInfo = EPlayInfo.ePlayerUser; // то считаю что юзер играл
               OnSelfPropertyChanged(nameof(this.CountFlag));
               OnSelfPropertyChanged(nameof(this.CountMinesLeft));
               OnSelfPropertyChanged(nameof(this.CountUnknown));
            }

            result.Modified.UnionWith(VerifyFlag());
            if (GameStatus != EGameStatus.eGSEnd) {
               //...
            }
            tracer.Put("any=" + any);

            OnSelfModifiedCellsPropertyChanged(result.Modified);
            return result;
         }
      }

      public ClickResult OnRightButtonUp(BaseCell cellRightUp) {
         using (var tracer = new fmg.common.Tracer("Mosaic::OnRightButtonUp"))
         try {
            var cellDown = CellDown;
            //tracer.Put("return");
            return new ClickResult(cellDown, false, false);
         } finally {
            CellDown = null;
         }
      }

      /// <summary> Request to user </summary>
      protected virtual bool CheckNeedRestoreLastGame() { return false; }

      /// <summary>Подготовиться к началу игры - сбросить все ячейки</summary>
      public virtual bool GameNew() {
         //System.out.println("Mosaic::GameNew()");

         if (GameStatus == EGameStatus.eGSReady)
            return false;

         if (RepositoryMines.Count != 0)
            if (GameStatus == EGameStatus.eGSCreateGame) {
            } else {
               if (CheckNeedRestoreLastGame())
                  RepositoryMines.Clear();
            }

         foreach (BaseCell cell in Matrix)
            cell.Reset();

         CountClick = 0;

         GameStatus = EGameStatus.eGSReady;
         PlayInfo = EPlayInfo.ePlayerUnknown; // пока не знаю кто будет играть

         OnSelfModifiedCellsPropertyChanged(this.Matrix);

         return true;
      }

         /// <summary>создать игру игроком - он сам расставит мины</summary>
      public void GameCreate() {
         GameNew();
         if (RepositoryMines.Count == 0) {
            MinesCount = 0;
            GameStatus = EGameStatus.eGSCreateGame;
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
      public int MaxNeighborNumber {
         get {
            var attr = CellAttr;
            return Enumerable.Range(0, attr.GetDirectionCount())
                  .Select(i => attr.getNeighborNumber(i))
                  .Max();
         }
      }

      /// <summary>действительно лишь когда gameStatus == gsEnd</summary>
      public bool IsVictory {
         get {
            return (GameStatus == EGameStatus.eGSEnd) && (0 == CountMinesLeft);
         }
      }

      protected virtual void OnCellAttributePropertyChanged(object sender, PropertyChangedEventArgs ev) {
         var pn = ev.PropertyName;
         if (pn == nameof(_cellAttr.Area)) {
            foreach (var cell in Matrix)
               cell.Init();

            OnSelfPropertyChanged(nameof(this.WindowSize));
            OnSelfPropertyChanged<double>(ev, nameof(this.Area));

            OnSelfModifiedCellsPropertyChanged(this.Matrix);
         }
         OnSelfPropertyChanged(nameof(this.CellAttr));
         OnSelfPropertyChanged(nameof(this.CellAttr) + "." + pn);
      }

      protected void OnSelfModifiedCellsPropertyChanged(ICollection<BaseCell> cells) {
         if (!cells.Any())
            return;

         // mark NULL if all mosaic is changed
         if (cells.Count == Matrix.Count)
            cells = null;
         if (ReferenceEquals(cells, Matrix))
            cells = null;
         OnSelfPropertyChanged(null, cells, PROPERTY_MODIFIED_CELLS);
      }

   }

}
