﻿using System;
using System.Linq;
using System.ComponentModel;
using System.Collections.Generic;
using Fmg.Common;
using Fmg.Common.Geom;
using Fmg.Core.Types;
using Fmg.Core.Img;
using Fmg.Core.Mosaic.Cells;

namespace Fmg.Core.Mosaic {

    /// <summary> MVC: mosaic controller. Base implementation </summary>
    /// <typeparam name="TImage">platform specific view/image/picture or other display context/canvas/window/panel</typeparam>
    /// <typeparam name="TImageInner">image type of flag/mine into mosaic field</typeparam>
    /// <typeparam name="TMosaicView">mosaic view</typeparam>
    /// <typeparam name="TMosaicModel">mosaic model</typeparam>
    public abstract class MosaicController<TImage, TImageInner, TMosaicView, TMosaicModel>
                         : ImageController<TImage,              TMosaicView, TMosaicModel>,
                         IMosaicController<TImage, TImageInner, TMosaicView, TMosaicModel>
        where TImage : class
        where TImageInner : class
        where TMosaicView : IMosaicView<TImage, TImageInner, TMosaicModel>
        where TMosaicModel : IMosaicDrawModel<TImageInner>
    {

        /// <summary> кол-во мин на поле </summary>
        protected int _countMines = 10;
        /// <summary> кол-во мин на поле до создания игры. Используется когда игра была создана, но ни одной мины не проставлено </summary>
        protected int _oldCountMines = 1;

        private EGameStatus _gameStatus = EGameStatus.eGSReady;
        private EPlayInfo _playInfo = EPlayInfo.ePlayerUnknown;
        private int _countClick;

        /// <summary> ячейка на которой было нажато (но не обязательно что отпущено) </summary>
        public BaseCell CellDown { get; set; }

        /// <summary> для load'a - координаты ячеек с минами </summary>
        private IList<Coord> _repositoryMines;

        /// <summary> использовать ли флажок на поле </summary>
        private bool _useUnknown = true;


        protected MosaicController(TMosaicView mosaicView)
            : base(mosaicView)
        {
            Model.PropertyChanged += OnModelPropertyChanged;
        }


        public IList<BaseCell> Matrix => Model.Matrix;

        /// <summary> размер поля в ячейках </summary>
        public Matrisize SizeField {
            get { return Model.SizeField; }
            set { Model.SizeField = value; }
        }

        /// <summary> узнать тип мозаики (из каких фигур состоит мозаика поля) </summary>
        public EMosaic MosaicType {
            get { return Model.MosaicType; }
            set { Model.MosaicType = value; }
        }

        /// <summary> количество мин </summary>
        public int CountMines {
            get { return _countMines; }
            set {
                var max = GetMaxMines(SizeField);
                var newVal = Math.Max((GameStatus == EGameStatus.eGSCreateGame) ? 0 : 1, Math.Min(value, max));
                int oldVal = CountMines;
                if ((oldVal != value) &&
                    (newVal != value))
                {
                    Logger.Warn("Can`t set mines count to {0}; reset to {1}. Try set size field first?", value, newVal);
                }

                if (oldVal == newVal)
                    return;

                if (newVal == 0) // TODO  ?? to create field mode - EGameStatus.eGSCreateGame
                    this._oldCountMines = this._countMines; // save

                _countMines = newVal;
                _notifier.FirePropertyChanged(oldVal, _countMines, nameof(this.CountMines));
                _notifier.FirePropertyChanged( -1   , _countMines, nameof(this.CountMinesLeft));

                GameNew();
            }
        }

        private void RecheckCountMines() {
            CountMines = CountMines; //  implicitly call this setter
        }

        /// <summary> arrange Mines </summary>
        public void SetMines_LoadRepository(IList<Coord> repository) {
            var mosaic = Model;
            foreach (var c in repository) {
                bool suc = mosaic.GetCell(c).State.SetMine();
                System.Diagnostics.Debug.Assert(suc, "Проблемы с установкой мин... :(");
            }
            // set other CellOpen and set all Caption
            foreach (var cell in Matrix)
                cell.State.CalcOpenState(mosaic);
        }

        /// <summary> arrange Mines - set random mines </summary>
        public void SetMines_Random(BaseCell firstClickCell) {
            if (_countMines == 0)
                _countMines = _oldCountMines;

            var mosaic = Model;
            var matrixClone = new List<BaseCell>(Matrix);
            matrixClone.Remove(firstClickCell); // исключаю на которой кликал юзер
            var neighbors = firstClickCell.GetNeighbors(mosaic);
            foreach (var x in neighbors)
                matrixClone.Remove(x); // и их соседей
            if (!matrixClone.Any())
                matrixClone.Add(neighbors[ThreadLocalRandom.Current.Next(neighbors.Count)]);
            var count = 0;
            var rand = ThreadLocalRandom.Current;
            do {
                var len = matrixClone.Count;
                if (len == 0) {
                    System.Diagnostics.Debug.Assert(false, "ээээ..... лажа......\r\nЗахотели установить больше мин чем возможно");
                    _countMines = count;
                    break;
                }
                var i = rand.Next(len);
                var cellToSetMines = matrixClone[i];
                if (cellToSetMines.State.SetMine()) {
                    count++;
                    matrixClone.Remove(cellToSetMines);
                } else
                    System.Diagnostics.Debug.Assert(false, "Мины должны всегда устанавливаться...");
            } while (count < _countMines);

            // set other CellOpen and set all Caption
            foreach (var cell in Matrix)
                cell.State.CalcOpenState(mosaic);
        }

        public int CountOpen { get { return Matrix.Count(x => x.State.Status == EState._Open); } }
        public int CountFlag { get { return Matrix.Count(x => (x.State.Status == EState._Close) && (x.State.Close == EClose._Flag)); } }
        public int CountUnknown { get { return Matrix.Count(x => (x.State.Status == EState._Close) && (x.State.Close == EClose._Unknown)); } }

        /// <summary>сколько ещё осталось открыть мин</summary>
        public int CountMinesLeft { get { return CountMines - CountFlag; } }
        public int CountClick {
            get { return _countClick; }
            private set { _notifier.SetProperty(ref _countClick, value); }
        }

        /// <summary>
        // <br> Этапы игры:
        // <br>           GameNew()      GameBegin()     GameEnd()      GameNew()
        // <br>    time      |               |               |             |
        // <br>  -------->   | eGSCreateGame |               |             |
        // <br>              |  or eGSReady  |    eGSPlay    |   eGSEnd    |
        // <br>              \------ 1 -----/ \----- 2 -----/ \---- 3 ----/
        // <br>
        // <br> @see Fmg.Core.Types.EGameStatus
        // <br>
        // <br> PS: При этапе gsReady поле чисто - мин нет! Мины расставляются только после первого клика
        // <br>     Так сделал только лишь потому, чтобы первый клик выполнялся не на мине. Естественно
        // <br>     это не относится к случаю, когда игра была создана пользователем или считана из файла.
        /// </summary>
        public EGameStatus GameStatus {
            get { return _gameStatus; }
            set { _notifier.SetProperty(ref _gameStatus, value); }
        }

        public EPlayInfo PlayInfo {
            get { return _playInfo; }
            set { _notifier.SetProperty(ref _playInfo, EPlayInfoEx.setPlayInfo(_playInfo, value)); }
        }

        public IList<Coord> RepositoryMines {
            get { return _repositoryMines ?? (_repositoryMines = new List<Coord>(0)); }
            set {
                var current = RepositoryMines;
                var areEquivalent = (value != null) && (current.Count == value.Count) && !current.Except(value).Any();
                if (!areEquivalent) {
                    current.Clear();
                    if ((value != null) && value.Any())
                        (current as List<Coord>).AddRange(value);
                }
                _notifier.FirePropertyChanged();
                //GameStatus = EGameStatus.eGSEnd;
                GameNew();
            }
        }

        /// <summary> Начать игру, т.к. произошёл первый клик на поле </summary>
        public virtual void GameBegin(BaseCell firstClickCell) {
            Model.BkFill.Mode = 0;
            GameStatus = EGameStatus.eGSPlay;

            // set mines
            if (RepositoryMines.Any()) {
                PlayInfo = EPlayInfo.ePlayIgnor;
                SetMines_LoadRepository(RepositoryMines);
            } else {
                SetMines_Random(firstClickCell);
            }
        }

        /// <summary> Завершить игру </summary>
        public IEnumerable<BaseCell> GameEnd(bool victory) {
            if (GameStatus == EGameStatus.eGSEnd)
                return Enumerable.Empty<BaseCell>();

            var toRepaint = new HashSet<BaseCell>();
            // открыть оставшeеся
            foreach (var cell in Matrix)
                if (cell.State.Status == EState._Close) {
                    if (victory) {
                        if (cell.State.Open == EOpen._Mine) {
                            cell.State.Close = EClose._Flag;
                        } else {
                            cell.State.Status = EState._Open;
                            cell.State.Down = true;
                        }
                        toRepaint.Add(cell);
                    } else {
                        if ((cell.State.Open  != EOpen._Mine) ||
                            (cell.State.Close != EClose._Flag))
                        {
                            cell.State.Status = EState._Open;
                            toRepaint.Add(cell);
                        }
                    }
                }

            GameStatus = EGameStatus.eGSEnd;
            _notifier.FirePropertyChanged(nameof(this.CountMinesLeft));
            _notifier.FirePropertyChanged(nameof(this.CountFlag));
            _notifier.FirePropertyChanged(nameof(this.CountOpen));

            return toRepaint;
        }

        private IEnumerable<BaseCell> VerifyFlag() {
            if (GameStatus == EGameStatus.eGSEnd) return Enumerable.Empty<BaseCell>();
            if (CountMines == CountFlag) {
                foreach (var cell in Matrix)
                    if ((cell.State.Close == EClose._Flag) &&
                        (cell.State.Open != EOpen._Mine))
                        return Enumerable.Empty<BaseCell>(); // неверно проставленный флажок - на выход
                return GameEnd(true);
            } else {
                if (CountMines == (CountFlag + CountUnknown)) {
                    foreach (var cell in Matrix)
                        if (((cell.State.Close == EClose._Unknown) ||
                             (cell.State.Close == EClose._Flag)) &&
                             (cell.State.Open != EOpen._Mine))
                            return Enumerable.Empty<BaseCell>(); // неверно проставленный флажок или '?'- на выход
                    return GameEnd(true);
                }
            }
            return Enumerable.Empty<BaseCell>();
        }

        protected ClickResult OnLeftButtonDown(BaseCell cellLeftDown) {
            //using (new Fmg.Common.Tracer("Mosaic.OnLeftButtonDown"))
            {
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
                        CountMines++;
                        RepositoryMines.Add(cellLeftDown.GetCoord());
                    } else {
                        cellLeftDown.Reset();
                        CountMines = CountMines - 1;
                        RepositoryMines.Remove(cellLeftDown.GetCoord());
                    }
                    result.Modified.Add(cellLeftDown);
                } else {
                    var resultCell = cellLeftDown.LButtonDown(Model);
                    result.Modified = resultCell.Modified; // copy reference; TODO result.Modified.AddRange(resultCell.Modified);
                }
                InvalidateView(result.Modified);
                return result;
            }
        }

        protected ClickResult OnLeftButtonUp(BaseCell cellLeftUp) {
            //using (var tracer = new Fmg.Common.Tracer("Mosaic.OnLeftButtonUp", "coordLUp=" + cellLeftUp?.getCoord()))
            {
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
                    var resultCell = cellDown.LButtonUp(ReferenceEquals(cellDown, cellLeftUp), Model);
                    if (!gameBegin)
                        result.Modified.UnionWith(resultCell.Modified);
                    //tracer.Put(" result.Modified=" + result.Modified.Count);
                    var countOpen = result.CountOpen;
                    var countFlag = result.CountFlag;
                    var countUnknown = result.CountUnknown;
                    var any = (countOpen > 0) || (countFlag > 0) || (countUnknown > 0); // клик со смыслом (были изменения на поле)
                    if (any) {
                        CountClick++;
                        PlayInfo = EPlayInfo.ePlayerUser;  // юзер играл
                        if (countOpen > 0)
                            _notifier.FirePropertyChanged(nameof(this.CountOpen));
                        if ((countFlag > 0) || (countUnknown > 0)) {
                            _notifier.FirePropertyChanged(nameof(this.CountFlag));
                            _notifier.FirePropertyChanged(nameof(this.CountMinesLeft));
                            _notifier.FirePropertyChanged(nameof(this.CountUnknown));
                        }
                    }

                    IEnumerable<BaseCell> modified;
                    if (result.IsAnyOpenMine) {
                        modified = GameEnd(false);
                    } else {
                        var sizeField = SizeField;
                        if ((CountOpen + CountMines) == sizeField.m * sizeField.n)
                            modified = GameEnd(true);
                        else
                            modified = VerifyFlag();
                    }

                    if (!gameBegin)
                        result.Modified.UnionWith(modified);
                    InvalidateView(result.Modified);

                    return result;
                } finally {
                    CellDown = null;
                }
            }
        }

        protected ClickResult OnRightButtonDown(BaseCell cellRightDown) {
            //using (var tracer = new Fmg.Common.Tracer("Mosaic.OnRightButtonDown"))
            {
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
                    _notifier.FirePropertyChanged(nameof(this.CountFlag));
                    _notifier.FirePropertyChanged(nameof(this.CountMinesLeft));
                    _notifier.FirePropertyChanged(nameof(this.CountUnknown));
                }

                result.Modified.UnionWith(VerifyFlag());
                if (GameStatus != EGameStatus.eGSEnd) {
                    //...
                }
                //tracer.Put("any=" + any);

                InvalidateView(result.Modified);
                return result;
            }
        }

        protected ClickResult OnRightButtonUp(BaseCell cellRightUp) {
            //using (var tracer = new Fmg.Common.Tracer("Mosaic.OnRightButtonUp"))
            {
                try {
                    var cellDown = CellDown;
                    //tracer.Put("return");
                    return new ClickResult(cellDown, false, false);
                } finally {
                    CellDown = null;
                }
            }
        }

        /// <summary> Request to user </summary>
        protected virtual bool CheckNeedRestoreLastGame() { return false; }

        /// <summary> Подготовиться к началу игры - сбросить все ячейки </summary>
        public virtual bool GameNew() {
            //using (var tracer = new Fmg.Common.Tracer("Mosaic.GameNew"))

            var m = Model;
            m.BkFill.Mode =  1 + ThreadLocalRandom.Current.Next(
                            m.CellAttr // MosaicHelper.CreateAttributeInstance(m.MosaicType
                            .GetMaxBackgroundFillModeValue());

            if (GameStatus == EGameStatus.eGSReady)
                return false;

            if (RepositoryMines.Any())
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

            _notifier.FirePropertyChanged(nameof(this.CountMinesLeft));

            InvalidateView(this.Matrix);

            return true;
        }

        /// <summary> создать игру игроком - он сам расставит мины </summary>
        public void GameCreate() {
            GameNew();
            if (!RepositoryMines.Any()) {
                CountMines = 0;
                GameStatus = EGameStatus.eGSCreateGame;
            }
        }

        public bool UseUnknown {
            get { return _useUnknown; }
            set { _useUnknown = value; }
        }

        /// <summary> Максимальное кол-во мин при  текущем  размере поля </summary>
        public int MaxMines => GetMaxMines(SizeField);
        /// <summary> Максимальное кол-во мин при указанном размере поля </summary>
        public int GetMaxMines(Matrisize sizeFld) {
            var iMustFreeCell = MaxNeighborNumber + 1;
            var iMaxMines = sizeFld.m * sizeFld.n - iMustFreeCell;
            return Math.Max(1, iMaxMines);
        }

        /// <summary> размер мозаики в пикселях для указанных параметров </summary>
        public SizeDouble GetMosaicSize(Matrisize sizeField, double area) {
            var m = Model;
            return area.HasMinDiff(m.Area)
                ? m.CellAttr.GetSize(sizeField)
                : MosaicHelper.GetSize(MosaicType, area, sizeField);
        }
        /// <summary> размер мозаики в пикселях </summary>
        public SizeDouble MosaicSize => Model.MosaicSize;

        /// <summary> узнать max количество соседей для текущей мозаики </summary>
        public int MaxNeighborNumber {
            get {
                var attr = Model.CellAttr;
                return Enumerable.Range(0, attr.GetDirectionCount())
                        .Select(i => attr.GetNeighborNumber(i))
                        .Max();
            }
        }

        /// <summary> действительно лишь когда gameStatus == gsEnd </summary>
        public bool IsVictory => (GameStatus == EGameStatus.eGSEnd) && (0 == CountMinesLeft);

        protected virtual void OnModelPropertyChanged(object sender, PropertyChangedEventArgs ev) {
            switch (ev.PropertyName) {
            case nameof(Model.SizeField):
                CellDown = null; // чтобы не было IndexOutOfBoundsException при уменьшении размера поля когда удерживается клик на поле...
                RecheckCountMines();
                GameNew();
                break;
            case nameof(Model.MosaicType):
                RecheckCountMines();
                GameNew();
                break;
            //case nameof(Model.Area): // TODO при изменении модели итак все перерисовывается...
            //    InvalidateView(Model.Matrix);
            //    break;
            }
        }

        protected void InvalidateView(ICollection<BaseCell> cells) {
            if (!cells.Any())
                return;

            // mark NULL if all mosaic is changed
            if (cells.Count == Matrix.Count)
                cells = null;
            if (ReferenceEquals(cells, Matrix))
                cells = null;

            View.Invalidate(cells);
        }

        /// <summary> преобразовать экранные координаты в ячейку поля мозаики </summary>
        private BaseCell CursorPointToCell(PointDouble point) {
            //if (point == null)
            //    return null;
            var m = Model;
            var offset = m.MosaicOffset;
            point = new PointDouble(point.X - offset.Width, point.Y - offset.Height);
            return m.Matrix.FirstOrDefault(cell =>
                //cell.getRcOuter().Contains(point) && // пох.. - тормозов нет..  (измерить время на макс размерах поля...) в принципе, проверка не нужная...
                cell.PointInRegion(point));
        }

        public ClickResult MousePressed(PointDouble clickPoint, bool isLeftMouseButton) {
            //using (new Fmg.Common.Tracer(GetCallerName(), "clickPoint" + clickPoint + "; isLeftMouseButton=" + isLeftMouseButton))
            {
                var res = isLeftMouseButton
                    ? OnLeftButtonDown(CursorPointToCell(clickPoint))
                    : OnRightButtonDown(CursorPointToCell(clickPoint));
                AcceptClickEvent(res);
                return res;
            }
        }

        public ClickResult MouseReleased(PointDouble clickPoint, bool isLeftMouseButton) {
            //using (new Fmg.Common.Tracer(GetCallerName(), "isLeftMouseButton=" + isLeftMouseButton))
            {
                var res = isLeftMouseButton
                    ? OnLeftButtonUp(CursorPointToCell(clickPoint))
                    : OnRightButtonUp(CursorPointToCell(clickPoint));
                AcceptClickEvent(res);
                return res;
            }
        }

        public ClickResult MouseFocusLost() {
            var cellDown = this.CellDown;
            if (cellDown == null)
                return null;
            bool isLeft = cellDown.State.Down; // hint: State.Down used only for the left click
            //using (new Fmg.Common.Tracer(GetCallerName(), string.Format("CellDown.Coord={0}; isLeft={1}", CellDown.getCoord(), isLeft)))
            {
                var res = isLeft
                    ? OnLeftButtonUp(null)
                    : OnRightButtonUp(null);
                AcceptClickEvent(res);
                return res;
            }
        }

        /// <summary> уведомление о том, что на мозаике был произведён клик </summary>
        private Action<ClickResult> _clickEvent;
        public void SetOnClickEvent(Action<ClickResult> handler) {
            _clickEvent = handler;
        }
        private void AcceptClickEvent(ClickResult clickResult) {
            _clickEvent?.Invoke(clickResult);
        }

        protected override void Disposing() {
            View.Model.PropertyChanged -= OnModelPropertyChanged;
            base.Disposing();
        }

        protected string GetCallerName([System.Runtime.CompilerServices.CallerMemberName] string callerName = null) {
            return callerName;
        }

    }

}
