package fmg.core.mosaic;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import fmg.common.Logger;
import fmg.common.geom.*;
import fmg.common.ui.UiInvoker;
import fmg.core.app.model.MosaicBackupData;
import fmg.core.app.model.MosaicInitData;
import fmg.core.img.ImageController;
import fmg.core.mosaic.cells.BaseCell;
import fmg.core.types.*;

/** MVC: mosaic controller. Base implementation
 * @param <TImage> platform specific view/image/picture or other display context/canvas/window/panel
 * @param <TImageInner> image type of flag/mine into mosaic field
 * @param <TMosaicView> mosaic view
 * @param <TMosaicModel> mosaic model
 */
public abstract class MosaicController<TImage, TImageInner,
                                       TMosaicView extends IMosaicView<TImage, TImageInner, TMosaicModel>,
                                       TMosaicModel extends IMosaicDrawModel<TImageInner>>
               extends ImageController<TImage, TMosaicView, TMosaicModel>
          implements IMosaicController<TImage, TImageInner, TMosaicView, TMosaicModel>
{
    public static final String PROPERTY_COUNT_MINES       = "CountMines";
    public static final String PROPERTY_COUNT_MINES_LEFT  = "CountMinesLeft";
    public static final String PROPERTY_COUNT_UNKNOWN     = "CountUnknown";
    public static final String PROPERTY_COUNT_CLICK       = "CountClick";
    public static final String PROPERTY_COUNT_FLAG        = "CountFlag";
    public static final String PROPERTY_COUNT_OPEN        = "CountOpen";
    public static final String PROPERTY_PLAY_INFO         = "PlayInfo";
    public static final String PROPERTY_REPOSITORY_MINES  = "RepositoryMines";
    public static final String PROPERTY_GAME_STATUS       = "GameStatus";

    /** кол-во мин на поле */
    @Property(PROPERTY_COUNT_MINES)
    protected int countMines = 10;

    @Property(PROPERTY_GAME_STATUS)
    private EGameStatus gameStatus = EGameStatus.eGSReady;

    @Property(PROPERTY_PLAY_INFO)
    private EPlayInfo playInfo = EPlayInfo.ePlayerUnknown;

    @Property(PROPERTY_COUNT_CLICK)
    private int countClick;

    /** ячейка на которой было нажато (но не обязательно что отпущено) */
    private BaseCell cellDown;

    /** для load'a - координаты ячеек с минами */
    @Property(PROPERTY_REPOSITORY_MINES)
    private List<Coord> repositoryMines;

    /** использовать ли флажок на поле */
    private boolean useUnknown = true;

    private boolean ignoreModelChanges = false;

    private final PropertyChangeListener onModelPropertyChangedListener = this::onModelPropertyChanged;

    protected MosaicController(TMosaicView mosaicView) {
        super(mosaicView);
        getModel().addListener(onModelPropertyChangedListener);
    }


    public List<BaseCell> getMatrix() {
        return getModel().getMatrix();
    }

    /** размер поля в ячейках */
    @Override
    public Matrisize getSizeField() { return getModel().getSizeField(); }
    /** размер поля в ячейках */
    @Override
    public void setSizeField(Matrisize newSizeField) { getModel().setSizeField(newSizeField); }

    /** узнать тип мозаики
     * (из каких фигур состоит мозаика поля) */
    @Override
    public EMosaic getMosaicType() { return getModel().getMosaicType(); }
    /** установить тип мозаики */
    @Override
    public void setMosaicType(EMosaic newMosaicType) { getModel().setMosaicType(newMosaicType); }

    /** количество мин */
    @Override
    public int getCountMines() { return countMines; }
    /** количество мин */
    @Override
    public void setCountMines(int newCountMines) {
        int newVal = Math.max((getGameStatus() == EGameStatus.eGSCreateGame) ? 0 : 1, Math.min(newCountMines, getMaxMines(getSizeField())));
        int oldVal = getCountMines();
        if ((oldVal != newCountMines) &&
            (newVal != newCountMines))
        {
            Logger.warn("Can`t set mines count to {0}; reset to {1}. Try set size field first?", newCountMines, newVal);
        }

        if (oldVal == newVal)
            return;

        countMines = newVal;
        notifier.firePropertyChanged(oldVal, countMines, PROPERTY_COUNT_MINES);
        notifier.firePropertyChanged(null, countMines, PROPERTY_COUNT_MINES_LEFT);

        gameNew();
    }

    private void recheckCountMines() {
        setCountMines(getCountMines());
    }

    /** arrange Mines */
    public void setMines_LoadRepository(List<Coord> repository) {
        IMosaicDrawModel<TImageInner> mosaic = getModel();
        for (Coord c: repository)
            mosaic.getCell(c).setMine();

        // set other CellOpen and set all Caption
        for (BaseCell cell : getMatrix())
            cell.calcOpenState(mosaic);
    }

    /** arrange Mines - set random mines */
    private void setMines_random(BaseCell firstClickCell) {
        if (countMines <= 0) {
            Logger.error("Illegal count of mines " + countMines);
            return;
        }

        IMosaicDrawModel<TImageInner> mosaic = getModel();
        List<BaseCell> matrixClone = new ArrayList<>(getMatrix());
        matrixClone.remove(firstClickCell); // исключаю на которой кликал юзер
        List<BaseCell> neighbors = firstClickCell.getNeighbors(mosaic);
        matrixClone.removeAll(neighbors); // и их соседей
        if (matrixClone.isEmpty())
            matrixClone.add(neighbors.get(ThreadLocalRandom.current().nextInt(neighbors.size())));
        int count = 0;
        Random rand = ThreadLocalRandom.current();
        do {
            int len = matrixClone.size();
            if (len == 0) {
                Logger.error("ээээ..... лажа......\r\nЗахотели установить больше мин чем возможно");
                countMines = count;
                break;
            }
            int i = rand.nextInt(len);
            BaseCell cellToSetMines = matrixClone.get(i);
            cellToSetMines.setMine();
            count++;
            matrixClone.remove(cellToSetMines);
        } while (count < countMines);

        // set other CellOpen and set all Caption
        for (BaseCell cell : getMatrix())
            cell.calcOpenState(mosaic);
    }

    @Override
    public int getCountOpen() {
        return (int)getMatrix().stream()
            .filter(c -> c.getState().getStatus() == EState._Open)
            .count();
    }
    @Override
    public int getCountFlag() {
        return (int)getMatrix().stream()
            .filter(c -> c.getState().getStatus() == EState._Close)
            .filter(c -> c.getState().getClose() == EClose._Flag)
            .count();
    }
    @Override
    public int getCountUnknown() {
        return (int)getMatrix().stream()
            .filter(c -> c.getState().getStatus() == EState._Close)
            .filter(c -> c.getState().getClose() == EClose._Unknown)
            .count();
    }

    /** сколько ещё осталось открыть мин */
    @Override
    public int getCountMinesLeft() { return getCountMines() - getCountFlag(); }
    @Override
    public int getCountClick()  { return countClick; }
    public void setCountClick(int clickCount) {
        int oldVal = this.countClick;
        int newVal = clickCount;
        notifier.setProperty(oldVal, newVal, PROPERTY_COUNT_CLICK);
    }

    /** ячейка на которой было нажато (но не обязательно что отпущено) */
    @Override
    public BaseCell getCellDown() { return cellDown; }
    /** ячейка на которой было нажато (но не обязательно что отпущено) */
    @Override
    public void setCellDown(BaseCell cellDown) { this.cellDown = cellDown; }

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
    @Override
    public EGameStatus getGameStatus() {
        if (gameStatus == null)
            gameStatus = EGameStatus.eGSEnd;
        return gameStatus;
    }
    public void setGameStatus(EGameStatus newStatus) {
        notifier.setProperty(gameStatus, newStatus, PROPERTY_GAME_STATUS);
    }

    public EPlayInfo getPlayInfo() {
        if (playInfo == null)
            playInfo = EPlayInfo.ePlayerUnknown;
        return playInfo;
    }
    public void setPlayInfo(EPlayInfo newVal) {
        notifier.setProperty(playInfo, EPlayInfo.setPlayInfo(playInfo, newVal), PROPERTY_PLAY_INFO);
    }

    public List<Coord> getRepositoryMines() {
        if (repositoryMines == null)
            repositoryMines = new ArrayList<>(0);
        return repositoryMines;
    }
    public void setRepositoryMines(List<Coord> newMines) {
        List<Coord> current = getRepositoryMines();
        if (!current.equals(newMines)) {
            current.clear();
            if ((newMines != null) && !newMines.isEmpty())
                current.addAll(newMines);
        }
        notifier.firePropertyChanged(PROPERTY_REPOSITORY_MINES);
        //setGameStatus(EGameStatus.eGSEnd);
        gameNew();
    }

    /** Начать игру, т.к. произошёл первый клик на поле */
    @Override
    public void gameBegin(BaseCell firstClickCell) {
        getModel().getCellFill().setMode(0);
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
    @Override
    public Collection<BaseCell> gameEnd(boolean victory) {
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
        notifier.firePropertyChanged(PROPERTY_COUNT_MINES_LEFT);
        notifier.firePropertyChanged(PROPERTY_COUNT_FLAG);
        notifier.firePropertyChanged(PROPERTY_COUNT_OPEN);

        return toRepaint;
    }

    private Collection<BaseCell> verifyFlag() {
        if (getGameStatus() == EGameStatus.eGSEnd) return Collections.emptySet();
        if (getCountMines() == getCountFlag()) {
            for (BaseCell cell: getMatrix())
                if ((cell.getState().getClose() == EClose._Flag) &&
                    (cell.getState().getOpen() != EOpen._Mine))
                    return Collections.emptySet(); // неверно проставленный флажок - на выход
            return gameEnd(true);
        } else {
            if (getCountMines() == (getCountFlag() + getCountUnknown())) {
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
                cellLeftDown.setMine();
                setCountMines(getCountMines()+1);
                getRepositoryMines().add(cellLeftDown.getCoord());
            } else {
                cellLeftDown.reset();
                setCountMines(getCountMines()-1);
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

//          Logger.info("OnLeftButtonUp: coordLUp="+coordLUp);
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
                    notifier.firePropertyChanged(PROPERTY_COUNT_OPEN);
                if ((countFlag > 0) || (countUnknown > 0)) {
                    notifier.firePropertyChanged(PROPERTY_COUNT_FLAG);
                    notifier.firePropertyChanged(PROPERTY_COUNT_MINES_LEFT);
                    notifier.firePropertyChanged(PROPERTY_COUNT_UNKNOWN);
                }
            }

            Collection<BaseCell> modified;
            if (result.isAnyOpenMine()) {
                modified = gameEnd(false);
            } else {
                Matrisize sizeField = getSizeField();
                if ((getCountOpen() + getCountMines()) == sizeField.m*sizeField.n) {
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
            notifier.firePropertyChanged(PROPERTY_COUNT_FLAG);
            notifier.firePropertyChanged(PROPERTY_COUNT_MINES_LEFT);
            notifier.firePropertyChanged(PROPERTY_COUNT_UNKNOWN);
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
        Logger.info("Restore last game?");
        return false;
    }

    public MosaicBackupData gameBackup() {
        MosaicBackupData backup = new MosaicBackupData();
        backup.mosaicType = getMosaicType();
        backup.sizeField = new Matrisize(getSizeField());
        backup.cellStates = getMatrix()
                .stream()
                .map(c -> {
                    BaseCell.StateCell state = c.getState();
                    BaseCell.StateCell copy = new BaseCell.StateCell();
                    copy.setStatus(state.getStatus());
                    copy.setOpen(  state.getOpen());
                    copy.setClose( state.getClose());
                    copy.setDown(  state.isDown());
                    return copy;
                })
                .collect(Collectors.toList());
        backup.clickCount = getCountClick();
        backup.area = getModel().getArea();
        return backup;
    }

    public void gameRestore(MosaicBackupData backup) {
        if (backup == null)
            return;
        if (backup.mosaicType == null)
            return;
        if (backup.sizeField == null)
            return;
        if (backup.cellStates == null)
            return;
        if (backup.cellStates.isEmpty())
            return;
        if ((backup.sizeField.m * backup.sizeField.n) != backup.cellStates.size()) {
            Logger.warn("Can`t apply cellStates.size=" + backup.cellStates.size() + " for field " + backup.sizeField);
            return;
        }
        if (backup.clickCount < 0)
            backup.clickCount = 0;
        if (backup.area < MosaicInitData.AREA_MINIMUM)
            backup.area = MosaicInitData.AREA_MINIMUM;

        try {
            ignoreModelChanges = true;
            setMosaicType(backup.mosaicType);
            setSizeField(backup.sizeField);
            getModel().setArea(backup.area);
            setCountClick(backup.clickCount);

            countMines = 0;
            int i = 0;
            boolean anyOpen = false;
            for (BaseCell cell : getMatrix()) {
                BaseCell.StateCell stateNew = backup.cellStates.get(i++);
                cell.setState(stateNew);

                if (stateNew.getStatus() == EState._Open)
                    anyOpen = true;

                if (stateNew.getOpen() == EOpen._Mine)
                    countMines++;
            }
            assert countMines > 0;

            setGameStatus(anyOpen ? EGameStatus.eGSPlay : EGameStatus.eGSReady);
            setPlayInfo(anyOpen ? EPlayInfo.ePlayerUser : EPlayInfo.ePlayerUnknown); // TODO ?

            notifier.firePropertyChanged(null, countMines, PROPERTY_COUNT_MINES);
            notifier.firePropertyChanged(null, countMines, PROPERTY_COUNT_MINES_LEFT);

            invalidateView(this.getMatrix());
        } finally {
            UiInvoker.DEFERRED.accept(() -> ignoreModelChanges = false );
        }
    }

    /** Подготовиться к началу игры - сбросить все ячейки */
    @Override
    public boolean gameNew() {
//        Logger.info("Mosaic::GameNew()");
        TMosaicModel m = getModel();
        m.getCellFill().setMode(
                1 + ThreadLocalRandom.current().nextInt(
                            m.getCellAttr() // MosaicHelper.createAttributeInstance(m.getMosaicType())
                            .getMaxCellFillModeValue()));

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

        notifier.firePropertyChanged(PROPERTY_COUNT_MINES_LEFT);

        invalidateView(this.getMatrix());

        return true;
    }

    /** создать игру игроком - он сам расставит мины */
    public void gameCreate() {
        gameNew();
        if (getRepositoryMines().isEmpty()) {
            setCountMines(0);
            setGameStatus(EGameStatus.eGSCreateGame);
        }
    }

    public void setUseUnknown(boolean val) { useUnknown = val; }
    public boolean getUseUnknown() { return useUnknown; }

    /** Максимальное кол-во мин при  текущем  размере поля */
    public int getMaxMines() { return getMaxMines(getSizeField()); }
    /** Максимальное кол-во мин при указанном размере поля */
    public int getMaxMines(Matrisize sizeFld) {
        int iMustFreeCell = getMaxNeighborNumber()+1;
        int iMaxMines = sizeFld.m*sizeFld.n-iMustFreeCell;
        return Math.max(1, iMaxMines);
    }
    /** размер мозаики в пикселях для указанных параметров */
    public SizeDouble getMosaicSize(Matrisize sizeField, double area) {
        TMosaicModel m = getModel();
        return DoubleExt.hasMinDiff(area, m.getArea())
            ? m.getCellAttr().getSize(sizeField)
            : MosaicHelper.getSize(getMosaicType(), area, sizeField);
    }
    /** размер мозаики в пикселях */
    public SizeDouble getMosaicSize() {
        return getModel().getMosaicSize();
    }
    /** узнать max количество соседей для текущей мозаики */
    public int getMaxNeighborNumber() {
        BaseCell.BaseAttribute attr = getModel().getCellAttr();
        return IntStream.range(0, attr.getDirectionCount())
            .map(i -> attr.getNeighborNumber(i))
            .max().getAsInt();
    }

    /** действительно лишь когда gameStatus == gsEnd */
    @Override
    public boolean isVictory() {
        return (getGameStatus() == EGameStatus.eGSEnd) && (0 == getCountMinesLeft());
    }

    protected void onModelPropertyChanged(PropertyChangeEvent ev) {
        if (ignoreModelChanges)
            return;
        switch (ev.getPropertyName()) {
        case MosaicGameModel.PROPERTY_SIZE_FIELD:
            setCellDown(null); // чтобы не было IndexOutOfBoundsException при уменьшении размера поля когда удерживается клик на поле...
            recheckCountMines();
            gameNew();
            break;
        case MosaicGameModel.PROPERTY_MOSAIC_TYPE:
            recheckCountMines();
            gameNew();
            break;
      //case MosaicGameModel.PROPERTY_AREA: // TODO при изменении модели итак все перерисовывается...
      //    invalidateView(getModel().getMatrix());
      //    break;
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
        TMosaicModel m = getModel();
        SizeDouble offset = m.getMosaicOffset();
        point = new PointDouble(point.x - offset.width, point.y - offset.height);
        for (BaseCell cell: m.getMatrix())
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
        getModel().removeListener(onModelPropertyChangedListener);
        super.close();
    }

}
