package fmg.core.mosaic;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Consumer;
import java.util.stream.IntStream;

import fmg.common.geom.*;
import fmg.common.notyfier.NotifyPropertyChanged;
import fmg.core.mosaic.cells.BaseCell;
import fmg.core.types.*;
import fmg.core.types.click.ClickCellResult;
import fmg.core.types.click.ClickResult;

/** MVC: controller. Base implementation */
public abstract class AMosaicController<TMosaicView extends IMosaicView>
                        extends NotifyPropertyChanged
                        implements PropertyChangeListener
{

   /** MVC: model */
   protected MosaicGameModel _mosaic;
   /** MVC: view */
   protected TMosaicView _view;

   /** get model */
   public MosaicGameModel getMosaic() {
      if (_mosaic == null)
         setMosaic(new MosaicGameModel());
      return _mosaic;
   }
   /** set model */
   protected void setMosaic(MosaicGameModel model) {
      if (_mosaic != null) {
         _mosaic.removeListener(this);
         _mosaic.close();
      }
      _mosaic = model;
      if (_mosaic != null) {
         _mosaic.addListener(this);
      }
   }

   /** get view */
   public abstract TMosaicView getView();
   /** set view */
   protected abstract void setView(TMosaicView view);


   /** кол-во мин на поле */
   protected int _minesCount = 10;
   /** кол-во мин на поле до создания игры. Используется когда игра была создана, но ни одной мины не проставлено. */
   protected int _oldMinesCount = 1;

   private EGameStatus _gameStatus = EGameStatus.eGSEnd;
   private EPlayInfo _playInfo = EPlayInfo.ePlayerUnknown;
   private int _countClick;

   /** для load'a - координаты ячеек с минами */
   private List<Coord> _repositoryMines;

   /** использовать ли флажок на поле */
   private boolean _useUnknown = true;

   public static final String PROPERTY_AREA              = MosaicGameModel.PROPERTY_AREA;
   public static final String PROPERTY_SIZE_FIELD        = MosaicGameModel.PROPERTY_SIZE_FIELD;
   public static final String PROPERTY_MOSAIC_TYPE       = MosaicGameModel.PROPERTY_MOSAIC_TYPE;
   public static final String PROPERTY_WINDOW_SIZE       = "WindowSize";
   public static final String PROPERTY_MINES_COUNT       = "MinesCount";
   public static final String PROPERTY_COUNT_MINES_LEFT  = "CountMinesLeft";
   public static final String PROPERTY_COUNT_UNKNOWN     = "CountUnknown";
   public static final String PROPERTY_COUNT_CLICK       = "CountClick";
   public static final String PROPERTY_COUNT_FLAG        = "CountFlag";
   public static final String PROPERTY_COUNT_OPEN        = "CountOpen";
   public static final String PROPERTY_PLAY_INFO         = "PlayInfo";
   public static final String PROPERTY_REPOSITORY_MINES  = "RepositoryMines";
   public static final String PROPERTY_GAME_STATUS       = "GameStatus";

   public List<BaseCell> getMatrix() {
      return getMosaic().getMatrix();
   }

   /** площадь ячеек */
   public double getArea() { return getMosaic().getArea(); }
   /** установить новую площадь ячеек */
   public void setArea(double newArea) { getMosaic().setArea(newArea); }

   /** размер поля в ячейках */
   public Matrisize getSizeField() { return getMosaic().getSizeField(); }
   /** размер поля в ячейках */
   public void setSizeField(Matrisize newSizeField) { getMosaic().setSizeField(newSizeField); }

   /** узнать тип мозаики
     * (из каких фигур состоит мозаика поля) */
   public EMosaic getMosaicType() { return getMosaic().getMosaicType(); }
   /** установить тип мозаики */
   public void setMosaicType(EMosaic newMosaicType) { getMosaic().setMosaicType(newMosaicType); }

   /** количество мин */
   public int getMinesCount() { return _minesCount; }
   /** количество мин */
   public void setMinesCount(int newMinesCount) {
      int old = getMinesCount();
      if (old == newMinesCount)
         return;

      if (newMinesCount == 0) // TODO  ?? to create field mode - EGameStatus.eGSCreateGame
         this._oldMinesCount = this._minesCount; // save

      _minesCount = Math.max(1, Math.min(newMinesCount, GetMaxMines(getSizeField())));
      onSelfPropertyChanged(old, _minesCount, PROPERTY_MINES_COUNT);
      onSelfPropertyChanged(null, _minesCount, PROPERTY_COUNT_MINES_LEFT);

      GameNew();
   }

   /** arrange Mines */
   public void setMines_LoadRepository(List<Coord> repository) {
      MosaicGameModel mosaic = getMosaic();
      for (Coord c: repository) {
         boolean suc = mosaic.getCell(c).getState().SetMine();
         if (!suc)
            System.err.println("Проблемы с установкой мин... :(");
      }
      // set other CellOpen and set all Caption
      for (BaseCell cell : getMatrix())
         cell.getState().calcOpenState(mosaic);
   }

   /** arrange Mines - set random mines */
   public void setMines_random(BaseCell firstClickCell) {
      if (_minesCount == 0)
         _minesCount = _oldMinesCount;

      MosaicGameModel mosaic = getMosaic();
      List<BaseCell> matrixClone = new ArrayList<BaseCell>(getMatrix());
      matrixClone.remove(firstClickCell); // исключаю на которой кликал юзер
      matrixClone.removeAll(firstClickCell.getNeighbors(mosaic)); // и их соседей
      int count = 0;
      Random rand = ThreadLocalRandom.current();
      do {
         int len = matrixClone.size();
         if (len == 0) {
            System.err.println("ээээ..... лажа......\r\nЗахотели установить больше мин чем возможно");
            _minesCount = count;
            break;
         }
         int i = rand.nextInt(len);
         BaseCell cellToSetMines = matrixClone.get(i);
         if (cellToSetMines.getState().SetMine()) {
            count++;
            matrixClone.remove(cellToSetMines);
         } else
            System.err.println("Мины должны всегда устанавливаться...");
      } while (count < _minesCount);

      // set other CellOpen and set all Caption
      for (BaseCell cell : getMatrix())
         cell.getState().calcOpenState(mosaic);
   }

   public int getCountOpen() {
      return (int)getMatrix().stream()
         .filter(c -> c.getState().getStatus() == EState._Open)
         .count();
   }
   public int getCountFlag() {
      return (int)getMatrix().stream()
            .filter(c -> c.getState().getStatus() == EState._Close)
            .filter(c -> c.getState().getClose() == EClose._Flag)
            .count();
   }
   public int getCountUnknown() {
      return (int)getMatrix().stream()
            .filter(c -> c.getState().getStatus() == EState._Close)
            .filter(c -> c.getState().getClose() == EClose._Unknown)
            .count();
   }

   /** сколько ещё осталось открыть мин */
   public int getCountMinesLeft() { return getMinesCount() - getCountFlag(); }
   public int getCountClick()  { return _countClick; }
   public void setCountClick(int clickCount)  {
      int old = _countClick;
      if (old != clickCount) {
         _countClick = clickCount;
         onSelfPropertyChanged(old, clickCount, PROPERTY_COUNT_CLICK);
      }
   }

   private BaseCell _cellDown;
   /** ячейка на которой было нажато (но не обязательно что отпущено) */
   public BaseCell getCellDown() { return _cellDown; }
   /** ячейка на которой было нажато (но не обязательно что отпущено) */
   public void setCellDown(BaseCell cellDown) { _cellDown = cellDown; }

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
   public EGameStatus getGameStatus() {
      if (_gameStatus == null)
         _gameStatus = EGameStatus.eGSEnd;
      return _gameStatus;
   }
   public void setGameStatus(EGameStatus newStatus) {
      EGameStatus old = _gameStatus;
      if (old != newStatus) {
         _gameStatus = newStatus;
         onSelfPropertyChanged(old, newStatus, PROPERTY_GAME_STATUS);
      }
   }

   public EPlayInfo getPlayInfo() {
      if (_playInfo == null)
         _playInfo = EPlayInfo.ePlayerUnknown;
      return _playInfo;
   }
   public void setPlayInfo(EPlayInfo newVal) {
      EPlayInfo old = _playInfo;
      newVal = EPlayInfo.setPlayInfo(_playInfo, newVal);
      if (old == newVal)
         return;
      _playInfo = newVal;
      onSelfPropertyChanged(old, newVal, PROPERTY_PLAY_INFO);
   }

   public List<Coord> getRepositoryMines() {
      if (_repositoryMines == null)
         _repositoryMines = new ArrayList<Coord>(0);
      return _repositoryMines;
   }
   public void setRepositoryMines(List<Coord> newMines) {
      List<Coord> current = getRepositoryMines();
      if (!current.equals(newMines)) {
         current.clear();
         if ((newMines != null) && !newMines.isEmpty())
            current.addAll(newMines);
      }
      onSelfPropertyChanged(PROPERTY_REPOSITORY_MINES);
      //setGameStatus(EGameStatus.eGSEnd);
      GameNew();
   }

   /** Начать игру, т.к. произошёл первый клик на поле */
   public void GameBegin(BaseCell firstClickCell) {
      setGameStatus(EGameStatus.eGSPlay);

      // set mines
      if (!getRepositoryMines().isEmpty()) {
         setPlayInfo(EPlayInfo.ePlayIgnor);
         setMines_LoadRepository(getRepositoryMines());
      } else {
         setMines_random(firstClickCell);
      }
   }

   /** Завершить игру */
   private Collection<BaseCell> GameEnd(boolean victory) {
      if (getGameStatus() == EGameStatus.eGSEnd)
         return Collections.emptySet();

      Set<BaseCell> toRepaint = new HashSet<>();
      // открыть оставшeеся
      for (BaseCell cell: getMatrix())
         if (cell.getState().getStatus() == EState._Close) {
            if (victory) {
               if (cell.getState().getOpen() == EOpen._Mine) {
                  cell.getState().setClose(EClose._Flag);
               } else {
                  cell.getState().setStatus(EState._Open);
                  cell.getState().setDown(true);
               }
               toRepaint.add(cell);
            } else {
               if ((cell.getState().getOpen()  != EOpen._Mine) ||
                   (cell.getState().getClose() != EClose._Flag))
               {
                  cell.getState().setStatus(EState._Open);
                  toRepaint.add(cell);
               }
            }
         }

      setGameStatus(EGameStatus.eGSEnd);
      onSelfPropertyChanged(PROPERTY_COUNT_MINES_LEFT);
      onSelfPropertyChanged(PROPERTY_COUNT_FLAG);
      onSelfPropertyChanged(PROPERTY_COUNT_OPEN);

      return toRepaint;
   }

   private Collection<BaseCell> VerifyFlag() {
      if (getGameStatus() == EGameStatus.eGSEnd) return Collections.emptySet();
      if (getMinesCount() == getCountFlag()) {
         for (BaseCell cell: getMatrix())
            if ((cell.getState().getClose() == EClose._Flag) &&
               (cell.getState().getOpen() != EOpen._Mine))
               return Collections.emptySet(); // неверно проставленный флажок - на выход
         return GameEnd(true);
      } else {
         if (getMinesCount() == (getCountFlag() + getCountUnknown())) {
            for (BaseCell cell: getMatrix())
               if (((cell.getState().getClose() == EClose._Unknown) ||
                  ( cell.getState().getClose() == EClose._Flag)) &&
                  ( cell.getState().getOpen() != EOpen._Mine))
                  return Collections.emptySet(); // неверно проставленный флажок или '?'- на выход
            return GameEnd(true);
         }
      }
      return Collections.emptySet();
   }

   protected ClickResult onLeftButtonDown(BaseCell cellLeftDown) {
      ClickResult result = new ClickResult(cellLeftDown, true, true);
      setCellDown(null);
      if (getGameStatus() == EGameStatus.eGSEnd)
         return result;
      if (cellLeftDown == null)
         return result;

      setCellDown(cellLeftDown);
      if (getGameStatus() == EGameStatus.eGSCreateGame) {
         if (cellLeftDown.getState().getOpen() != EOpen._Mine) {
            cellLeftDown.getState().setStatus(EState._Open);
            cellLeftDown.getState().SetMine();
            setMinesCount(getMinesCount()+1);
            getRepositoryMines().add(cellLeftDown.getCoord());
         } else {
            cellLeftDown.Reset();
            setMinesCount(getMinesCount()-1);
            getRepositoryMines().remove(cellLeftDown.getCoord());
         }
         result.modified.add(cellLeftDown);
      } else {
         ClickCellResult resultCell = cellLeftDown.leftButtonDown(getMosaic());
         result.modified = resultCell.modified; // copy reference; TODO result.modified.addAll(resultCell.modified);
      }
      onSelfModifiedCellsPropertyChanged(result.modified);
      return result;
   }

   protected ClickResult onLeftButtonUp(BaseCell cellLeftUp) {
      try {
         BaseCell cellDown = getCellDown();
         ClickResult result = new ClickResult(cellDown, true, false);
         if (getGameStatus() == EGameStatus.eGSEnd)
            return result;
         if (cellDown == null)
            return result;
         if (getGameStatus() == EGameStatus.eGSCreateGame)
            return result;

   //      System.out.println("OnLeftButtonUp: coordLUp="+coordLUp);
         boolean gameBegin = (getGameStatus() == EGameStatus.eGSReady) && (cellDown == cellLeftUp);
         if (gameBegin) {
            GameBegin(cellDown);
            result.modified.addAll(this.getMatrix());
         }
         ClickCellResult resultCell = cellDown.leftButtonUp(cellDown == cellLeftUp, getMosaic());
         if (!gameBegin)
            result.modified.addAll(resultCell.modified);
         int countOpen = result.getCountOpen();
         int countFlag = result.getCountFlag();
         int countUnknown = result.getCountUnknown();
         boolean any = (countOpen > 0) || (countFlag > 0) || (countUnknown > 0); // клик со смыслом (были изменения на поле)
         if (any) {
            setCountClick(getCountClick()+1);
            setPlayInfo(EPlayInfo.ePlayerUser);  // юзер играл
            if (countOpen > 0)
               onSelfPropertyChanged(PROPERTY_COUNT_OPEN);
            if ((countFlag > 0) || (countUnknown > 0)) {
               onSelfPropertyChanged(PROPERTY_COUNT_FLAG);
               onSelfPropertyChanged(PROPERTY_COUNT_MINES_LEFT);
               onSelfPropertyChanged(PROPERTY_COUNT_UNKNOWN);
            }
         }

         Collection<BaseCell> modified;
         if (result.isAnyOpenMine()) {
            modified = GameEnd(false);
         } else {
            Matrisize sizeField = getSizeField();
            if ((getCountOpen() + getMinesCount()) == sizeField.m*sizeField.n) {
               modified = GameEnd(true);
            } else {
               modified = VerifyFlag();
            }
         }

         if (!gameBegin)
            result.modified.addAll(modified);
         onSelfModifiedCellsPropertyChanged(result.modified);

         return result;
      } finally {
         setCellDown(null);
      }
   }

   protected ClickResult onRightButtonDown(BaseCell cellRightDown) {
      setCellDown(null);
      ClickResult result = new ClickResult(cellRightDown, false, true);
      if (getGameStatus() == EGameStatus.eGSEnd) {
         GameNew();
         return result;
      }
      if (getGameStatus() == EGameStatus.eGSReady)
         return result;
      if (getGameStatus() == EGameStatus.eGSCreateGame)
         return result;
      if (cellRightDown == null)
         return result;

      setCellDown(cellRightDown);
      ClickCellResult resultCell = cellRightDown.rightButtonDown(cellRightDown.getState().getClose().nextState(getUseUnknown()));
      result.modified = resultCell.modified; // copy reference; TODO modify to result.modified.addAll(resultCell.modified);

      int countFlag = result.getCountFlag();
      int countUnknown = result.getCountUnknown();
      boolean any = (countFlag > 0) || (countUnknown > 0); // клик со смыслом (были изменения на поле)
      if (any) {
         setCountClick(getCountClick()+1);
         setPlayInfo(EPlayInfo.ePlayerUser); // то считаю что юзер играл
         onSelfPropertyChanged(PROPERTY_COUNT_FLAG);
         onSelfPropertyChanged(PROPERTY_COUNT_MINES_LEFT);
         onSelfPropertyChanged(PROPERTY_COUNT_UNKNOWN);
      }

      result.modified.addAll(VerifyFlag());
      if (getGameStatus() != EGameStatus.eGSEnd) {
         //...
      }

      onSelfModifiedCellsPropertyChanged(result.modified);
      return result;
   }

   protected ClickResult onRightButtonUp(BaseCell cellRightUp) {
      try {
         BaseCell cellDown = getCellDown();
         return new ClickResult(cellDown, false, false);
      } finally {
         setCellDown(null);
      }
   }

   /** Request to user */
   protected boolean checkNeedRestoreLastGame() {
      //  need override in child class
      System.out.println("Restore last game?");
      return false;
   }

   /** Подготовиться к началу игры - сбросить все ячейки */
   public boolean GameNew() {
//      System.out.println("Mosaic::GameNew()");

      if (getGameStatus() == EGameStatus.eGSReady)
         return false;

      if (!getRepositoryMines().isEmpty())
         if (getGameStatus() == EGameStatus.eGSCreateGame) {
         } else {
            if (checkNeedRestoreLastGame())
               getRepositoryMines().clear();
         }

      for (BaseCell cell : getMatrix())
         cell.Reset();

      setCountClick(0);

      setGameStatus(EGameStatus.eGSReady);
      setPlayInfo(EPlayInfo.ePlayerUnknown); // пока не знаю кто будет играть

      onSelfModifiedCellsPropertyChanged(this.getMatrix());

      return true;
   }

   /** создать игру игроком - он сам расставит мины */
   public void GameCreate() {
      GameNew();
      if (getRepositoryMines().isEmpty()) {
         setMinesCount(0);
         setGameStatus(EGameStatus.eGSCreateGame);
      }
   }

   public void setUseUnknown(boolean val) { _useUnknown = val; }
   public boolean getUseUnknown() { return _useUnknown; }

   /** Максимальное кол-во мин при указанном размере поля */
   public int GetMaxMines(Matrisize sizeFld) {
      int iMustFreeCell = GetMaxNeighborNumber()+1;
      int iMaxMines = sizeFld.m*sizeFld.n-iMustFreeCell;
      return Math.max(1, iMaxMines);
   }
   /** Максимальное кол-во мин при  текущем  размере поля */
   public int GetMaxMines() { return GetMaxMines(getSizeField()); }
   /** размер в пикселях для указанных параметров */
   public SizeDouble getWindowSize(Matrisize sizeField, double area) {
      return (DoubleExt.hasMinDiff(area, getArea()))
         ? getMosaic().getCellAttr().getOwnerSize(sizeField)
         : MosaicHelper.getOwnerSize(getMosaicType(), area, sizeField);
   }
   /** размер в пикселях */
   public SizeDouble getWindowSize() {
      return getWindowSize(getSizeField(), getArea());
   }
   /** узнать max количество соседей для текущей мозаики */
   public int GetMaxNeighborNumber() {
      BaseCell.BaseAttribute attr = getMosaic().getCellAttr();
      return IntStream.range(0, attr.getDirectionCount())
            .map(i -> attr.getNeighborNumber(i))
            .max().getAsInt();
   }

   /** действительно лишь когда gameStatus == gsEnd */
   public boolean isVictory() {
      return (getGameStatus() == EGameStatus.eGSEnd) && (0 == getCountMinesLeft());
   }

   @Override
   public void propertyChange(PropertyChangeEvent ev) {
      if (ev.getSource() instanceof MosaicGameModel)
         onMosaicPropertyChanged((MosaicGameModel)ev.getSource(), ev);
   }

   protected void onMosaicPropertyChanged(MosaicGameModel source, PropertyChangeEvent ev) {
      String propertyName = ev.getPropertyName();
      switch (propertyName) {
      case MosaicGameModel.PROPERTY_SIZE_FIELD:
         setCellDown(null); // чтобы не было IndexOutOfBoundsException при уменьшении размера поля когда удерживается клик на поле...
         onSelfPropertyChanged(ev.getOldValue(), ev.getNewValue(), PROPERTY_SIZE_FIELD);
         onSelfPropertyChanged(PROPERTY_WINDOW_SIZE);
         GameNew();
         break;
      case MosaicGameModel.PROPERTY_MOSAIC_TYPE:
         onSelfPropertyChanged(ev.getOldValue(), ev.getNewValue(), PROPERTY_MOSAIC_TYPE);
         GameNew();
         break;
      case MosaicGameModel.PROPERTY_AREA:
         onSelfPropertyChanged(ev.getOldValue(), ev.getNewValue(), PROPERTY_AREA);
         onSelfPropertyChanged(PROPERTY_WINDOW_SIZE);
         onSelfModifiedCellsPropertyChanged(source.getMatrix());
         break;
      default:
         break;
      }
   }

   protected void onSelfModifiedCellsPropertyChanged(Collection<BaseCell> cells) {
      if (cells.isEmpty())
         return;

      // mark NULL if all mosaic is changed
      if (cells.size() == getMatrix().size())
         cells = null;
      if (cells == getMatrix()) // ReferenceEquals
         cells = null;

      getView().invalidate(cells);
   }


   /** преобразовать экранные координаты в ячейку поля мозаики */
   private BaseCell CursorPointToCell(PointDouble point) {
      if (point == null)
            return null;
      for (BaseCell cell: getMosaic(). getMatrix())
         //if (cell.getRcOuter().contains(point)) // пох.. - тормозов нет..  (измерить время на макс размерах поля...) в принципе, проверка не нужная...
            if (cell.pointInRegion(point))
               return cell;
      return null;
   }

   public ClickResult mousePressed(PointDouble clickPoint, boolean isLeftMouseButton) {
      ClickResult res = isLeftMouseButton
         ? this.onLeftButtonDown(CursorPointToCell(clickPoint))
         : this.onRightButtonDown(CursorPointToCell(clickPoint));
      acceptClickEvent(res);
      return res;
   }

   public ClickResult mouseReleased(PointDouble clickPoint, boolean isLeftMouseButton) {
      ClickResult res = isLeftMouseButton
         ? this.onLeftButtonUp(CursorPointToCell(clickPoint))
         : this.onRightButtonUp(CursorPointToCell(clickPoint));
      acceptClickEvent(res);
      return res;
   }

   public ClickResult mouseFocusLost() {
      BaseCell cellDown = this.getCellDown();
      if (cellDown == null)
         return null;
      boolean isLeft = cellDown.getState().isDown(); // hint: State.Down used only for the left click
      ClickResult res = isLeft
         ? this.onLeftButtonUp(null)
         : this.onRightButtonUp(null);
      acceptClickEvent(res);
      return res;
   }

   /** уведомление о том, что на мозаике был произведён клик */
   private Consumer<ClickResult> clickEvent;
   public void setOnClickEvent(Consumer<ClickResult> handler) {
      clickEvent = handler;
   }
   private void acceptClickEvent(ClickResult clickResult) {
      if (clickEvent != null)
         clickEvent.accept(clickResult);
   }

   @Override
   public void close() {
      setMosaic(null); // unsubscribe & close
      setView(null); // unsubscribe & close
   }

}
