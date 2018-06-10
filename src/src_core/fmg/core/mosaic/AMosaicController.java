package fmg.core.mosaic;

import java.beans.PropertyChangeListener;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Consumer;
import java.util.stream.IntStream;

import fmg.common.geom.*;
import fmg.core.img.AnimatedImgController;
import fmg.core.mosaic.cells.BaseCell;
import fmg.core.types.*;

/** MVC: mosaic controller. Base implementation
 * @param <TImage> plaform specific view/image/picture or other display context/canvas/window/panel
 * @param <TImage2> image type of flag/mine into mosaic field
 * @param <TMosaicView> mosaic view
 * @param <TMosaicModel> mosaic model
 */
public abstract class AMosaicController<TImage, TImage2,
                                        TMosaicView extends IMosaicView<TImage, TImage2, TMosaicModel>,
                                        TMosaicModel extends MosaicDrawModel<TImage2>>
                extends AnimatedImgController<TImage, TMosaicView, TMosaicModel>
{

   /** кол-во мин на поле */
   protected int _minesCount = 10;
   /** кол-во мин на поле до создания игры. Используется когда игра была создана, но ни одной мины не проставлено. */
   protected int _oldMinesCount = 1;

   private EGameStatus _gameStatus = EGameStatus.eGSReady;
   private EPlayInfo _playInfo = EPlayInfo.ePlayerUnknown;
   private int _countClick;

   /** для load'a - координаты ячеек с минами */
   private List<Coord> _repositoryMines;

   /** использовать ли флажок на поле */
   private boolean _useUnknown = true;

   private final PropertyChangeListener _mosaicModelListener = ev -> onMosaicModelPropertyChanged(ev.getOldValue(), ev.getNewValue(), ev.getPropertyName());


   protected AMosaicController(TMosaicView mosaicView) {
      super(mosaicView);
      mosaicView.getModel().addListener(_mosaicModelListener);
   }


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
      return getModel().getMatrix();
   }

   /** площадь ячеек */
   public double getArea() { return getModel().getArea(); }
   /** установить новую площадь ячеек */
   public void setArea(double newArea) { getModel().setArea(newArea); }

   /** размер поля в ячейках */
   public Matrisize getSizeField() { return getModel().getSizeField(); }
   /** размер поля в ячейках */
   public void setSizeField(Matrisize newSizeField) { getModel().setSizeField(newSizeField); }

   /** узнать тип мозаики
     * (из каких фигур состоит мозаика поля) */
   public EMosaic getMosaicType() { return getModel().getMosaicType(); }
   /** установить тип мозаики */
   public void setMosaicType(EMosaic newMosaicType) { getModel().setMosaicType(newMosaicType); }

   /** количество мин */
   public int getMinesCount() { return _minesCount; }
   /** количество мин */
   public void setMinesCount(int newMinesCount) {
      int old = getMinesCount();
      if (old == newMinesCount)
         return;

      if (newMinesCount == 0) // TODO  ?? to create field mode - EGameStatus.eGSCreateGame
         this._oldMinesCount = this._minesCount; // save

      _minesCount = Math.max(1, Math.min(newMinesCount, getMaxMines(getSizeField())));
      _notifier.onPropertyChanged(old, _minesCount, PROPERTY_MINES_COUNT);
      _notifier.onPropertyChanged(null, _minesCount, PROPERTY_COUNT_MINES_LEFT);

      gameNew();
   }

   /** arrange Mines */
   public void setMines_LoadRepository(List<Coord> repository) {
      MosaicGameModel mosaic = getModel();
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

      MosaicGameModel mosaic = getModel();
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
         _notifier.onPropertyChanged(old, clickCount, PROPERTY_COUNT_CLICK);
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
         _notifier.onPropertyChanged(old, newStatus, PROPERTY_GAME_STATUS);
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
      _notifier.onPropertyChanged(old, newVal, PROPERTY_PLAY_INFO);
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
      _notifier.onPropertyChanged(PROPERTY_REPOSITORY_MINES);
      //setGameStatus(EGameStatus.eGSEnd);
      gameNew();
   }

   /** Начать игру, т.к. произошёл первый клик на поле */
   public void gameBegin(BaseCell firstClickCell) {
      getModel().getBackgroundFill().setMode(0);
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
   private Collection<BaseCell> gameEnd(boolean victory) {
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
      _notifier.onPropertyChanged(PROPERTY_COUNT_MINES_LEFT);
      _notifier.onPropertyChanged(PROPERTY_COUNT_FLAG);
      _notifier.onPropertyChanged(PROPERTY_COUNT_OPEN);

      return toRepaint;
   }

   private Collection<BaseCell> verifyFlag() {
      if (getGameStatus() == EGameStatus.eGSEnd) return Collections.emptySet();
      if (getMinesCount() == getCountFlag()) {
         for (BaseCell cell: getMatrix())
            if ((cell.getState().getClose() == EClose._Flag) &&
               (cell.getState().getOpen() != EOpen._Mine))
               return Collections.emptySet(); // неверно проставленный флажок - на выход
         return gameEnd(true);
      } else {
         if (getMinesCount() == (getCountFlag() + getCountUnknown())) {
            for (BaseCell cell: getMatrix())
               if (((cell.getState().getClose() == EClose._Unknown) ||
                  ( cell.getState().getClose() == EClose._Flag)) &&
                  ( cell.getState().getOpen() != EOpen._Mine))
                  return Collections.emptySet(); // неверно проставленный флажок или '?'- на выход
            return gameEnd(true);
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
            cellLeftDown.reset();
            setMinesCount(getMinesCount()-1);
            getRepositoryMines().remove(cellLeftDown.getCoord());
         }
         result.modified.add(cellLeftDown);
      } else {
         ClickCellResult resultCell = cellLeftDown.leftButtonDown(getModel());
         result.modified = resultCell.modified; // copy reference; TODO result.modified.addAll(resultCell.modified);
      }
      invalidateView(result.modified);
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
            gameBegin(cellDown);
            result.modified.addAll(this.getMatrix());
         }
         ClickCellResult resultCell = cellDown.leftButtonUp(cellDown == cellLeftUp, getModel());
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
               _notifier.onPropertyChanged(PROPERTY_COUNT_OPEN);
            if ((countFlag > 0) || (countUnknown > 0)) {
               _notifier.onPropertyChanged(PROPERTY_COUNT_FLAG);
               _notifier.onPropertyChanged(PROPERTY_COUNT_MINES_LEFT);
               _notifier.onPropertyChanged(PROPERTY_COUNT_UNKNOWN);
            }
         }

         Collection<BaseCell> modified;
         if (result.isAnyOpenMine()) {
            modified = gameEnd(false);
         } else {
            Matrisize sizeField = getSizeField();
            if ((getCountOpen() + getMinesCount()) == sizeField.m*sizeField.n) {
               modified = gameEnd(true);
            } else {
               modified = verifyFlag();
            }
         }

         if (!gameBegin)
            result.modified.addAll(modified);
         invalidateView(result.modified);

         return result;
      } finally {
         setCellDown(null);
      }
   }

   protected ClickResult onRightButtonDown(BaseCell cellRightDown) {
      setCellDown(null);
      ClickResult result = new ClickResult(cellRightDown, false, true);
      if (getGameStatus() == EGameStatus.eGSEnd) {
         gameNew();
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
         _notifier.onPropertyChanged(PROPERTY_COUNT_FLAG);
         _notifier.onPropertyChanged(PROPERTY_COUNT_MINES_LEFT);
         _notifier.onPropertyChanged(PROPERTY_COUNT_UNKNOWN);
      }

      result.modified.addAll(verifyFlag());
      if (getGameStatus() != EGameStatus.eGSEnd) {
         //...
      }

      invalidateView(result.modified);
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
   public boolean gameNew() {
//      System.out.println("Mosaic::GameNew()");
      TMosaicModel m = getModel();
      m.getBackgroundFill().setMode(
            1 + ThreadLocalRandom.current().nextInt(
                      m.getCellAttr() // MosaicHelper.createAttributeInstance(m.getMosaicType())
                     .getMaxBackgroundFillModeValue()));

      if (getGameStatus() == EGameStatus.eGSReady)
         return false;

      if (!getRepositoryMines().isEmpty())
         if (getGameStatus() == EGameStatus.eGSCreateGame) {
         } else {
            if (checkNeedRestoreLastGame())
               getRepositoryMines().clear();
         }

      for (BaseCell cell : getMatrix())
         cell.reset();

      setCountClick(0);

      setGameStatus(EGameStatus.eGSReady);
      setPlayInfo(EPlayInfo.ePlayerUnknown); // пока не знаю кто будет играть

      invalidateView(this.getMatrix());

      return true;
   }

   /** создать игру игроком - он сам расставит мины */
   public void gameCreate() {
      gameNew();
      if (getRepositoryMines().isEmpty()) {
         setMinesCount(0);
         setGameStatus(EGameStatus.eGSCreateGame);
      }
   }

   public void setUseUnknown(boolean val) { _useUnknown = val; }
   public boolean getUseUnknown() { return _useUnknown; }

   /** Максимальное кол-во мин при указанном размере поля */
   public int getMaxMines(Matrisize sizeFld) {
      int iMustFreeCell = getMaxNeighborNumber()+1;
      int iMaxMines = sizeFld.m*sizeFld.n-iMustFreeCell;
      return Math.max(1, iMaxMines);
   }
   /** Максимальное кол-во мин при  текущем  размере поля */
   public int getMaxMines() { return getMaxMines(getSizeField()); }
   /** размер в пикселях для указанных параметров */
   public SizeDouble getInnerSize(Matrisize sizeField, double area) {
      return DoubleExt.hasMinDiff(area, getArea())
         ? getModel().getCellAttr().getSize(sizeField)
         : MosaicHelper.getSize(getMosaicType(), area, sizeField);
   }
   /** размер в пикселях */
   public SizeDouble getInnerSize() {
      return getModel().getInnerSize();
   }
   /** узнать max количество соседей для текущей мозаики */
   public int getMaxNeighborNumber() {
      BaseCell.BaseAttribute attr = getModel().getCellAttr();
      return IntStream.range(0, attr.getDirectionCount())
            .map(i -> attr.getNeighborNumber(i))
            .max().getAsInt();
   }

   /** действительно лишь когда gameStatus == gsEnd */
   public boolean isVictory() {
      return (getGameStatus() == EGameStatus.eGSEnd) && (0 == getCountMinesLeft());
   }

   protected void onMosaicModelPropertyChanged(Object oldValue, Object newValue, String propertyName) {
      switch (propertyName) {
      case MosaicGameModel.PROPERTY_SIZE_FIELD:
         setCellDown(null); // чтобы не было IndexOutOfBoundsException при уменьшении размера поля когда удерживается клик на поле...
         gameNew();
         break;
      case MosaicGameModel.PROPERTY_MOSAIC_TYPE:
         gameNew();
         break;
      case MosaicGameModel.PROPERTY_AREA:
         invalidateView(getModel().getMatrix());
         break;
      case MosaicDrawModel.PROPERTY_SIZE_DOUBLE:
         _notifier.onPropertyChanged(PROPERTY_SIZE);
         break;
      default:
         break;
      }
   }

   protected void invalidateView(Collection<BaseCell> cells) {
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
   private BaseCell cursorPointToCell(PointDouble point) {
      if (point == null)
            return null;
      for (BaseCell cell: getModel(). getMatrix())
         //if (cell.getRcOuter().contains(point)) // пох.. - тормозов нет..  (измерить время на макс размерах поля...) в принципе, проверка не нужная...
            if (cell.pointInRegion(point))
               return cell;
      return null;
   }

   public ClickResult mousePressed(PointDouble clickPoint, boolean isLeftMouseButton) {
      ClickResult res = isLeftMouseButton
         ? this.onLeftButtonDown(cursorPointToCell(clickPoint))
         : this.onRightButtonDown(cursorPointToCell(clickPoint));
      acceptClickEvent(res);
      return res;
   }

   public ClickResult mouseReleased(PointDouble clickPoint, boolean isLeftMouseButton) {
      ClickResult res = isLeftMouseButton
         ? this.onLeftButtonUp(cursorPointToCell(clickPoint))
         : this.onRightButtonUp(cursorPointToCell(clickPoint));
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
      getView().getModel().removeListener(_mosaicModelListener);
      super.close();
   }

}
