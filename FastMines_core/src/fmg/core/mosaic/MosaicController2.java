package fmg.core.mosaic;

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
import fmg.core.img.ImageController2;
import fmg.core.mosaic.cells.BaseCell;
import fmg.core.mosaic.shape.BaseShape;
import fmg.core.types.*;

/** MVC: mosaic controller. Base implementation
 * @param <TImage> platform specific view/image/picture or other display context/canvas/window/panel
   @param <TView> mosaic view */
public abstract class MosaicController2<TImage,
                                       TView extends IMosaicView2<TImage>>
              extends ImageController2<TImage, MosaicModel2, TView>
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
    protected int countMines = 10;

    private EGameStatus gameStatus = EGameStatus.eGSReady;

    private EPlayInfo playInfo = EPlayInfo.ePlayerUnknown;

    private int countClick;

    /** ячейка на которой было нажато (но не обязательно что отпущено) */
    private BaseCell cellDown;

    /** для load'a - координаты ячеек с минами */
    private List<Coord> repositoryMines;

    /** использовать ли флажок на поле */
    private boolean useUnknown = true;

    private boolean ignoreModelChanges = false;


    @Override
    protected void init(MosaicModel2 model, TView view) {
        super.init(model, view);
        onChangeCellSquareSize();
        subscribeToViewControl();
    }

    /** количество мин */
    public int getCountMines() { return countMines; }
    /** количество мин */
    public void setCountMines(int newCountMines) {
        int newVal = Math.max((getGameStatus() == EGameStatus.eGSCreateGame)
                                  ? 0
                                  : 1,
                              Math.min(newCountMines,
                                       getMaxMines(model.getSizeField())));
        int oldVal = getCountMines();
        if ((oldVal != newCountMines) &&
            (newVal != newCountMines))
        {
            Logger.warn("Can`t set mines count to {0}; reset to {1}. Try set size field first?", newCountMines, newVal);
        }

        if (oldVal == newVal)
            return;

        countMines = newVal;
        if (changedCallback != null) {
            //changedCallback.accept(PROPERTY_COUNT_MINES);
            changedCallback.accept(PROPERTY_COUNT_MINES_LEFT);
        }

        gameNew();
    }

    private void recheckCountMines() {
        setCountMines(getCountMines());
    }

    /** arrange Mines */
    public void setMinesLoadRepository(List<Coord> repository) {
        for (Coord c: repository)
            model.getCell(c).setMine();

        // set other CellOpen and set all Caption
        for (BaseCell cell : model.getMatrix())
            calcOpenState(cell);
    }

    /** соседние ячейки */
    private List<BaseCell> getNeighbors(BaseCell cell) {
        // получаю координаты соседних ячеек
        List<Coord> neighborCoord = cell.getCoordsNeighbor();

        int m = model.getSizeField().m;
        int n = model.getSizeField().n;
        // по координатам получаю множество соседних обьектов-ячеек
        List<BaseCell> neighbors = new ArrayList<>(neighborCoord.size());
        for (Coord c : neighborCoord)
            // проверяю что они не вылезли за размеры
            if ((c.x >= 0) && (c.y >= 0) && (c.x < m) && (c.y < n))
                neighbors.add( model.getCell(c) );
        return neighbors;
    }

    /** arrange Mines - set random mines */
    private void setMinesRandom(BaseCell firstClickCell) {
        if (countMines <= 0) {
            Logger.error("Illegal count of mines " + countMines);
            return;
        }

        List<BaseCell> matrixClone = new ArrayList<>(model.getMatrix());
        matrixClone.remove(firstClickCell); // исключаю на которой кликал юзер
        List<BaseCell> neighbors = getNeighbors(firstClickCell);
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
        for (BaseCell cell : model.getMatrix())
            calcOpenState(cell);
    }

    private void calcOpenState(BaseCell cell) {
        var state = cell.getState();
        if (state.getOpen() == EOpen._Mine)
            return;

        // подсчитать у соседей число мин и установить значение
        int count = 0;
        List<BaseCell> neighbors = getNeighbors(cell);
        for (BaseCell nCell : neighbors) {
            if (nCell == null) continue; // существует ли сосед?
            if (nCell.getState().getOpen() == EOpen._Mine) count++;
        }
        state.setOpen(EOpen.class.getEnumConstants()[count]);
    }

    public int getCountOpen() {
        return (int)model.getMatrix().stream()
            .filter(c -> c.getState().getStatus() == EState._Open)
            .count();
    }

    public int getCountFlag() {
        return (int)model.getMatrix().stream()
            .filter(c -> c.getState().getStatus() == EState._Close)
            .filter(c -> c.getState().getClose() == EClose._Flag)
            .count();
    }

    public int getCountUnknown() {
        return (int)model.getMatrix().stream()
            .filter(c -> c.getState().getStatus() == EState._Close)
            .filter(c -> c.getState().getClose() == EClose._Unknown)
            .count();
    }

    /** сколько ещё осталось открыть мин */
    public int getCountMinesLeft() { return getCountMines() - getCountFlag(); }

    public int getCountClick()  { return countClick; }
    public void setCountClick(int clickCount) {
        if (this.countClick == clickCount)
            return;
        this.countClick = clickCount;
        if (changedCallback != null)
            changedCallback.accept(PROPERTY_COUNT_CLICK);
    }

    /** ячейка на которой было нажато (но не обязательно что отпущено) */
    public BaseCell getCellDown() { return cellDown; }
    /** ячейка на которой было нажато (но не обязательно что отпущено) */
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
    public EGameStatus getGameStatus() {
        if (gameStatus == null)
            gameStatus = EGameStatus.eGSEnd;
        return gameStatus;
    }
    public void setGameStatus(EGameStatus newStatus) {
        if (this.gameStatus == newStatus)
            return;
        this.gameStatus = newStatus;
        if (changedCallback != null)
            changedCallback.accept(PROPERTY_GAME_STATUS);
    }

    public EPlayInfo getPlayInfo() {
        if (playInfo == null)
            playInfo = EPlayInfo.ePlayerUnknown;
        return playInfo;
    }
    public void setPlayInfo(EPlayInfo newVal) {
        newVal = EPlayInfo.setPlayInfo(this.playInfo, newVal);
        if (this.playInfo == newVal)
            return;
        this.playInfo = newVal;
        if (changedCallback != null)
            changedCallback.accept(PROPERTY_PLAY_INFO);
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
        if (changedCallback != null)
            changedCallback.accept(PROPERTY_REPOSITORY_MINES);
        //setGameStatus(EGameStatus.eGSEnd);
        gameNew();
    }

    /** Начать игру, т.к. произошёл первый клик на поле */
    public void gameBegin(BaseCell firstClickCell) {
        model.setFillMode(0);
        setGameStatus(EGameStatus.eGSPlay);

        // set mines
        if (!getRepositoryMines().isEmpty()) {
            setPlayInfo(EPlayInfo.ePlayIgnor);
            setMinesLoadRepository(getRepositoryMines());
        } else {
            setMinesRandom(firstClickCell);
        }
    }

    /** Завершить игру */
    public Collection<BaseCell> gameEnd(boolean victory) {
        if (getGameStatus() == EGameStatus.eGSEnd)
            return Collections.emptySet();

        Set<BaseCell> toRepaint = new HashSet<>();
        // открыть оставшeеся
        for (BaseCell cell: model.getMatrix())
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
        if (changedCallback != null) {
            changedCallback.accept(PROPERTY_COUNT_MINES_LEFT);
            changedCallback.accept(PROPERTY_COUNT_FLAG);
            changedCallback.accept(PROPERTY_COUNT_OPEN);
        }

        return toRepaint;
    }

    private Collection<BaseCell> verifyFlag() {
        if (getGameStatus() == EGameStatus.eGSEnd) return Collections.emptySet();
        if (getCountMines() == getCountFlag()) {
            for (BaseCell cell: model.getMatrix())
                if ((cell.getState().getClose() == EClose._Flag) &&
                    (cell.getState().getOpen() != EOpen._Mine))
                    return Collections.emptySet(); // неверно проставленный флажок - на выход
            return gameEnd(true);
        } else {
            if (getCountMines() == (getCountFlag() + getCountUnknown())) {
                for (BaseCell cell: model.getMatrix())
                    if (((cell.getState().getClose() == EClose._Unknown) ||
                        ( cell.getState().getClose() == EClose._Flag)) &&
                        ( cell.getState().getOpen() != EOpen._Mine))
                        return Collections.emptySet(); // неверно проставленный флажок или '?'- на выход
                return gameEnd(true);
            }
        }
        return Collections.emptySet();
    }

    private ClickCellResult leftButtonDown(BaseCell cell) {
        ClickCellResult result = new ClickCellResult();
        var state = cell.getState();
        if (state.getClose() == EClose._Flag)
            return result;

        if (state.getStatus() == EState._Close) {
            state.setDown(true);
            result.modified.add(cell);
            return result;
        }

        // эффект нажатости для неоткрытых соседей
        if ((state.getStatus() == EState._Open) && (state.getOpen() != EOpen._Nil)) {
            List<BaseCell> neighbors = getNeighbors(cell);
            for (BaseCell nCell : neighbors) {
                if (nCell == null) // существует ли сосед?
                    continue;

                var nState = nCell.getState();
                if ((nState.getStatus() == EState._Open) ||
                    (nState.getClose()  == EClose._Flag))
                    continue;

                nState.setDown(true);
                result.modified.add(nCell);
            }
        }
        return result;
    }

    private ClickCellResult leftButtonUp(BaseCell cell, boolean isMy) {
        ClickCellResult result = new ClickCellResult();

        var state = cell.getState();
        if (state.getClose() == EClose._Flag)
            return result;

        // избавится от эффекта нажатости
        List<BaseCell> neighbors = getNeighbors(cell);
        if ((state.getStatus() == EState._Open) && (state.getOpen() != EOpen._Nil)) {
            for (BaseCell nCell : neighbors) {
                if (nCell == null) // существует ли сосед?
                    continue;

                var nState = nCell.getState();
                if ((nState.getStatus() == EState._Open) ||
                    (nState.getClose()  == EClose._Flag))
                    continue;

                nState.setDown(false);
                result.modified.add(nCell);
            }
        }

        // Открыть закрытую ячейку на которой нажали
        if (state.getStatus() == EState._Close) {
            state.setDown(isMy);
            result.modified.add(cell);
            if (!isMy)
                return result;

            state.setStatus(EState._Open);
        }

        // ! В этой точке ячейка уже открыта
        // Подсчитываю кол-во установленных вокруг флагов и не открытых ячеек
        int countFlags = 0;
        int countClear = 0;
        if (state.getOpen() != EOpen._Nil)
            for (BaseCell nCell : neighbors) {
                if (nCell == null) // существует ли сосед?
                    continue;

                var nState = nCell.getState();
                if (nState.getStatus() == EState._Open)
                    continue;

                if (nState.getClose()  == EClose._Flag)
                    countFlags++;
                else
                    countClear++;
            }

        // оставшимся установить флаги
        if ((state.getOpen() != EOpen._Nil) && ((countFlags+countClear) == state.getOpen().ordinal()))
            for (BaseCell nCell : neighbors) {
                if (nCell == null) // существует ли сосед?
                    continue;

                var nState = nCell.getState();
                if ((nState.getStatus() == EState._Open) ||
                    (nState.getClose()  == EClose._Flag))
                    continue;

                nState.setClose(EClose._Flag);
                result.modified.add(nCell);
            }

        if (!isMy)
            return result;

        // открыть оставшиеся
        if ((countFlags+result.getCountFlag()) == state.getOpen().ordinal())
            for (BaseCell nCell : neighbors) {
                if (nCell == null) // существует ли сосед?
                    continue;

                var nState = nCell.getState();
                if ((nState.getStatus() == EState._Open) ||
                    (nState.getClose()  == EClose._Flag))
                    continue;

                nState.setDown(true);
                nState.setStatus(EState._Open);
                result.modified.add(nCell);
                if (nState.getOpen() == EOpen._Nil) {
                    ClickCellResult result2 = leftButtonUp(nCell, true); // TODO на больших размерах поля и при маленьком числе мин, приводит к StackOverflowException
                    result.modified.addAll(result2.modified);
                }
                if (nState.getOpen() == EOpen._Mine)
                    return result;
            }

        return result;
    }

    private ClickCellResult rightButtonDown(BaseCell cell) {
        ClickCellResult result = new ClickCellResult();
        var state = cell.getState();
        if ((state.getStatus() == EState._Open) || state.isDown())
            return result;

        state.setClose(state.getClose().nextState(getUseUnknown()));
        result.modified.add(cell);
        return result;
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
                setCountMines(getCountMines() + 1);
                getRepositoryMines().add(cellLeftDown.getCoord());
            } else {
                cellLeftDown.reset();
                setCountMines(getCountMines() - 1);
                getRepositoryMines().remove(cellLeftDown.getCoord());
            }
            result.modified.add(cellLeftDown);
        } else {
            ClickCellResult resultCell = leftButtonDown(cellLeftDown);
            result.modified = resultCell.modified; // copy reference; TODO result.modified.addAll(resultCell.modified);
        }
        if (!result.modified.isEmpty())
            view.invalidate(result.modified);
        return result;
    }

    protected ClickResult onLeftButtonUp(BaseCell cellLeftUp) {
        try {
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
                result.modified.addAll(model.getMatrix());
            }
            ClickCellResult resultCell = leftButtonUp(cellDown, cellDown == cellLeftUp);
            if (!gameBegin)
                result.modified.addAll(resultCell.modified);
            int countOpen = result.getCountOpen();
            int countFlag = result.getCountFlag();
            int countUnknown = result.getCountUnknown();
            boolean any = (countOpen > 0) || (countFlag > 0) || (countUnknown > 0); // клик со смыслом (были изменения на поле)
            if (any) {
                setCountClick(getCountClick()+1);
                setPlayInfo(EPlayInfo.ePlayerUser);  // юзер играл
                if (countOpen > 0) {
                    if (changedCallback != null)
                        changedCallback.accept(PROPERTY_COUNT_OPEN);
                }
                if ((countFlag > 0) || (countUnknown > 0)) {
                    if (changedCallback != null) {
                        changedCallback.accept(PROPERTY_COUNT_FLAG);
                        changedCallback.accept(PROPERTY_COUNT_MINES_LEFT);
                        changedCallback.accept(PROPERTY_COUNT_UNKNOWN);
                    }
                }
            }

            Collection<BaseCell> modified;
            if (result.isAnyOpenMine()) {
                modified = gameEnd(false);
            } else {
                Matrisize sizeField = model.getSizeField();
                if ((getCountOpen() + getCountMines()) == sizeField.m*sizeField.n) {
                    modified = gameEnd(true);
                } else {
                    modified = verifyFlag();
                }
            }

            if (!gameBegin)
                result.modified.addAll(modified);

            if (!result.modified.isEmpty())
                view.invalidate(result.modified);

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
        ClickCellResult resultCell = rightButtonDown(cellRightDown);
        result.modified = resultCell.modified; // copy reference; TODO modify to result.modified.addAll(resultCell.modified);

        int countFlag = result.getCountFlag();
        int countUnknown = result.getCountUnknown();
        boolean any = (countFlag > 0) || (countUnknown > 0); // клик со смыслом (были изменения на поле)
        if (any) {
            setCountClick(getCountClick()+1);
            setPlayInfo(EPlayInfo.ePlayerUser); // то считаю что юзер играл
            if (changedCallback != null) {
                changedCallback.accept(PROPERTY_COUNT_FLAG);
                changedCallback.accept(PROPERTY_COUNT_MINES_LEFT);
                changedCallback.accept(PROPERTY_COUNT_UNKNOWN);
            }
        }

        result.modified.addAll(verifyFlag());
        if (getGameStatus() != EGameStatus.eGSEnd) {
            //...
        }

        if (!result.modified.isEmpty())
            view.invalidate(result.modified);

        return result;
    }

    protected ClickResult onRightButtonUp(BaseCell cellRightUp) {
        try {
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
        backup.mosaicType = model.getMosaicType();
        backup.sizeField = new Matrisize(model.getSizeField());
        backup.cellStates = model.getMatrix()
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
        backup.area = model.getArea();
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
            model.setMosaicType(backup.mosaicType);
            model.setSizeField(backup.sizeField);
            model.setArea(backup.area);
            setCountClick(backup.clickCount);

            countMines = 0;
            int i = 0;
            boolean anyOpen = false;
            for (BaseCell cell : model.getMatrix()) {
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

            if (changedCallback != null) {
                changedCallback.accept(PROPERTY_COUNT_MINES);
                changedCallback.accept(PROPERTY_COUNT_MINES_LEFT);
            }

            view.invalidate();
        } finally {
            UiInvoker.Deferred.accept(() -> ignoreModelChanges = false );
        }
    }

    /** Подготовиться к началу игры - сбросить все ячейки */
    public boolean gameNew() {
//        Logger.info("Mosaic::GameNew()");
        model.setFillMode(
                1 + ThreadLocalRandom.current().nextInt(
                            model.getShape() // MosaicHelper.createShapeInstance(m.getMosaicType())
                            .getMaxCellFillModeValue()));

        if (getGameStatus() == EGameStatus.eGSReady)
            return false;

        if (!getRepositoryMines().isEmpty())
            if (getGameStatus() == EGameStatus.eGSCreateGame) {
            } else {
                if (checkNeedRestoreLastGame())
                    getRepositoryMines().clear();
            }

        for (BaseCell cell : model.getMatrix())
            cell.reset();

        setCountClick(0);

        setGameStatus(EGameStatus.eGSReady);
        setPlayInfo(EPlayInfo.ePlayerUnknown); // пока не знаю кто будет играть

        if (changedCallback != null)
            changedCallback.accept(PROPERTY_COUNT_MINES_LEFT);

        view.invalidate();

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
    public int getMaxMines() {
        return getMaxMines(model.getSizeField());
    }
    /** Максимальное кол-во мин при указанном размере поля */
    public int getMaxMines(Matrisize sizeFld) {
        int iMustFreeCell = getMaxNeighborNumber()+1;
        int iMaxMines = sizeFld.m*sizeFld.n-iMustFreeCell;
        return Math.max(1, iMaxMines);
    }
    /** размер мозаики в пикселях для указанных параметров */
    public SizeDouble getMosaicSize(Matrisize sizeField, double area) {
        return DoubleExt.almostEquals(area, model.getArea())
            ? model.getShape().getSize(sizeField)
            : MosaicHelper.getSize(model.getMosaicType(), area, sizeField);
    }
    /** размер мозаики в пикселях */
    public SizeDouble getMosaicSize() {
        return model.getSizeMosaic();
    }
    /** узнать max количество соседей для текущей мозаики */
    public int getMaxNeighborNumber() {
        BaseShape shape = model.getShape();
        return IntStream.range(0, shape.getDirectionCount())
            .map(shape::getNeighborNumber)
            .max().getAsInt();
    }

    /** действительно лишь когда gameStatus == gsEnd */
    public boolean isVictory() {
        return (getGameStatus() == EGameStatus.eGSEnd) && (0 == getCountMinesLeft());
    }

    @Override
    protected void onModelChanged(String propertyName) {
        super.onModelChanged(propertyName);
        if (ignoreModelChanges)
            return;
        switch (propertyName) {
        case MosaicModel2.PROPERTY_SIZE_FIELD:
            setCellDown(null); // чтобы не было IndexOutOfBoundsException при уменьшении размера поля когда удерживается клик на поле...
            recheckCountMines();
            gameNew();
            break;
        case MosaicModel2.PROPERTY_MOSAIC_TYPE:
            recheckCountMines();
            gameNew();
            break;
      //case MosaicModel2.PROPERTY_AREA: // TODO при изменении модели итак все перерисовывается...
      //    view.invalidate();
      //    break;
        default:
            break;
        }

        switch (propertyName) {
        case MosaicModel2.PROPERTY_MOSAIC_TYPE:
        case MosaicModel2.PROPERTY_AREA:
            onChangeCellSquareSize();
            break;
        default:
            // none
        }
    }

    /** преобразовать экранные координаты в ячейку поля мозаики */
    private BaseCell cursorPointToCell(PointDouble point) {
        if (point == null)
            return null;
        SizeDouble offset = model.getMosaicOffset();
        point = new PointDouble(point.x - offset.width, point.y - offset.height);
        for (BaseCell cell: model.getMatrix())
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

    /** переустанавливаю заного размер мины/флага для мозаики */
    protected abstract void onChangeCellSquareSize();
    protected abstract void subscribeToViewControl();
    protected abstract void unsubscribeToViewControl();

    @Override
    public void close() {
        unsubscribeToViewControl();
        super.close();
    }

}
