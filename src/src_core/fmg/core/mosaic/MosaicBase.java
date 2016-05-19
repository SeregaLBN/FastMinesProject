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
package fmg.core.mosaic;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import fmg.common.geom.Coord;
import fmg.common.geom.DoubleExt;
import fmg.common.geom.Matrisize;
import fmg.common.geom.SizeDouble;
import fmg.common.notyfier.NotifyPropertyChanged;
import fmg.core.mosaic.cells.BaseCell;
import fmg.core.types.EClose;
import fmg.core.types.EGameStatus;
import fmg.core.types.EMosaic;
import fmg.core.types.EOpen;
import fmg.core.types.EPlayInfo;
import fmg.core.types.EState;
import fmg.core.types.click.ClickCellResult;
import fmg.core.types.click.ClickResult;
import fmg.swing.draw.mosaic.graphics.PaintableGraphics;

/** Mosaic field: класс окна мозаики поля */
public abstract class MosaicBase extends NotifyPropertyChanged implements IMosaic<PaintableGraphics>, PropertyChangeListener
{
   public static final double AREA_MINIMUM = 230;

   /** матрица List &lt; List &lt; BaseCell &gt; &gt; , представленная(развёрнута) в виде вектора */
   private final List<BaseCell> _matrix = new ArrayList<BaseCell>(0);
   /** размер поля в ячейках */
   protected Matrisize _size = new Matrisize(0, 0);
   /** из каких фигур состоит мозаика поля */
   protected EMosaic _mosaicType = EMosaic.eMosaicSquare1;
   /** кол-во мин на поле */
   protected int _minesCount = 1;
   /** кол-во мин на поле до создания игры. Используется когда игра была создана, но ни одной мины не проставлено. */
   protected int _oldMinesCount = 1;

   private EGameStatus _gameStatus;
   private EPlayInfo _playInfo;
   private int _countClick;

   /** для load'a - координаты ячеек с минами */
   private List<Coord> _repositoryMines;

   /** использовать ли флажок на поле */
   private boolean _useUnknown = true;

   private BaseCell.BaseAttribute _cellAttr;
   private void setCellAttr(BaseCell.BaseAttribute newValue) {
      if (_cellAttr == null)
         return;
      if (newValue != null)
         throw new IllegalArgumentException("Bad argument - support only null value!");
      _cellAttr.removePropertyChangeListener(this);
      _cellAttr = null;
      onPropertyChanged("CellAttr");
   }
   @Override
   public BaseCell.BaseAttribute getCellAttr() {
      if (_cellAttr == null) {
         _cellAttr = MosaicHelper.createAttributeInstance(getMosaicType(), getArea());
         _cellAttr.addListener(this);
      }
      return _cellAttr;
   }

   @Override
   public List<BaseCell> getMatrix() {
      if (_matrix.isEmpty()) {
         BaseCell.BaseAttribute attr = getCellAttr();
         Matrisize size = getSizeField();
         EMosaic mosaicType = getMosaicType();
         //_matrix = new ArrayList<BaseCell>(size.width * size.height);
         for (int i=0; i < size.m; i++)
            for (int j=0; j < size.n; j++) {
               BaseCell cell = MosaicHelper.createCellInstance(attr, mosaicType, new Coord(i, j));
               _matrix.add(i*_size.n + j, cell);
            }
      }
      return _matrix;
   }

   /** размер поля в ячейках */
   @Override
   public Matrisize getSizeField() { return _size; }
   /** размер поля в ячейках */
   @Override
   public void setSizeField(Matrisize newSizeField) {
      Matrisize old = this._size;
      if (old.equals(newSizeField))
         return;

      setCellDown(null); // чтобы не было IndexOutOfBoundsException при уменьшении размера поля когда удерживается клик на поле...
      this._size = newSizeField;
      onPropertyChanged(old, newSizeField, "SizeField");

      _matrix.clear();
      onPropertyChanged("Matrix");

      GameNew();
   }

   /** узнать тип мозаики */
   @Override
   public EMosaic getMosaicType() { return _mosaicType; }
   @Override
   /** установить тип мозаики */
   public void setMosaicType(EMosaic newMosaicType) {
      EMosaic old = this._mosaicType;
      if (old == newMosaicType)
         return;

      this._mosaicType = newMosaicType;
      onPropertyChanged(old, newMosaicType, "MosaicType");

      double saveArea = getArea(); // save
      setCellAttr(null); // lost area

      _matrix.clear();
      onPropertyChanged("Matrix");

      setArea(saveArea); // restore

      GameNew();
   }

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
      onPropertyChanged(old, _minesCount, "MinesCount");
      onPropertyChanged(null, _minesCount, "CountMinesLeft");

      GameNew();
   }

   protected void OnError(String msg) {
      System.err.println(msg);
   }

   /** arrange Mines */
   public void setMines_LoadRepository(List<Coord> repository) {
      for (Coord c: repository) {
         boolean suc = getCell(c).getState().SetMine();
         if (!suc)
            OnError("Проблемы с установкой мин... :(");
      }
      // set other CellOpen and set all Caption
      getMatrix().forEach(cell -> cell.getState().calcOpenState(this));
   }

   /** arrange Mines - set random mines */
   public void setMines_random(BaseCell firstClickCell) {
      if (_minesCount == 0)
         _minesCount = _oldMinesCount;

      List<BaseCell> matrixClone = new ArrayList<BaseCell>(_matrix);
      matrixClone.remove(firstClickCell); // исключаю на которой кликал юзер
      matrixClone.removeAll(firstClickCell.getNeighbors(this)); // и их соседей
      int count = 0;
      Random rand = new Random();
      do {
         int len = matrixClone.size();
         if (len == 0) {
            OnError("ээээ..... лажа......\r\nЗахотели установить больше мин чем возможно");
            _minesCount = count;
            break;
         }
         int i = rand.nextInt(len);
         BaseCell cellToSetMines = matrixClone.get(i);
         if (cellToSetMines.getState().SetMine()) {
            count++;
            matrixClone.remove(cellToSetMines);
         } else
            OnError("Мины должны всегда устанавливаться...");
      } while (count < _minesCount);

      // set other CellOpen and set all Caption
      getMatrix().forEach(cell -> cell.getState().calcOpenState(this));
   }

   public int getCountOpen() {
      return (int)_matrix.stream()
         .filter(c -> c.getState().getStatus() == EState._Open)
         .count();
   }
   public int getCountFlag() {
      return (int)_matrix.stream()
            .filter(c -> c.getState().getStatus() == EState._Close)
            .filter(c -> c.getState().getClose() == EClose._Flag)
            .count();
   }
   public int getCountUnknown() {
      return (int)_matrix.stream()
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
         onPropertyChanged(old, clickCount, "CountClick");
      }
   }

   /** доступ к заданной ячейке */
   public BaseCell getCell(int x, int y) { return _matrix.get(x*_size.n + y); }
   /** доступ к заданной ячейке */
   @Override
   public BaseCell getCell(Coord coord) { return getCell(coord.x, coord.y); }

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
         onPropertyChanged(old, newStatus, "GameStatus");
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
      onPropertyChanged(old, newVal, "PlayInfo");
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
      onPropertyChanged("RepositoryMines");
      //setGameStatus(EGameStatus.eGSEnd);
      GameNew();
   }

   /** перерисовать ячейку; если null - перерисовать всё поле */
   protected abstract void Repaint(BaseCell cell);

   /** Начать игру, т.к. произошёл первый клик на поле */
   protected void GameBegin(BaseCell firstClickCell) {
      setGameStatus(EGameStatus.eGSPlay);

      // set mines
      if (!getRepositoryMines().isEmpty()) {
         setPlayInfo(EPlayInfo.ePlayIgnor);
         setMines_LoadRepository(getRepositoryMines());
      } else {
         setMines_random(firstClickCell);
      }

      Repaint(null);
   }

   /** Завершить игру */
   private void GameEnd(boolean victory) {
      if (getGameStatus() == EGameStatus.eGSEnd)
         return;

      // открыть оставшeеся
      for (BaseCell cell: _matrix)
         if (cell.getState().getStatus() == EState._Close) {
            if (victory) {
               if (cell.getState().getOpen() == EOpen._Mine)
               {
                  cell.getState().setClose(EClose._Flag);
               } else {
                  cell.getState().setStatus(EState._Open);
                  cell.getState().setDown(true);
               }
            } else {
               if ((cell.getState().getOpen() != EOpen._Mine) ||
                  (cell.getState().getClose() != EClose._Flag))
               {
                  cell.getState().setStatus(EState._Open);
               }
            }
            Repaint(cell);
         }

      setGameStatus(EGameStatus.eGSEnd);
      onPropertyChanged("CountMinesLeft");
      onPropertyChanged("CountFlag");
      onPropertyChanged("CountOpen");
   }

   private void VerifyFlag() {
      if (getGameStatus() == EGameStatus.eGSEnd) return;
      if (getMinesCount() == getCountFlag()) {
         for (BaseCell cell: _matrix)
            if ((cell.getState().getClose() == EClose._Flag) &&
               (cell.getState().getOpen() != EOpen._Mine))
               return; // неверно проставленный флажок - на выход
         GameEnd(true);
      } else {
         if (getMinesCount() == (getCountFlag() + getCountUnknown())) {
            for (BaseCell cell: _matrix)
               if (((cell.getState().getClose() == EClose._Unknown) ||
                  ( cell.getState().getClose() == EClose._Flag)) &&
                  ( cell.getState().getOpen() != EOpen._Mine))
                  return; // неверно проставленный флажок или '?'- на выход
            GameEnd(true);
         }
      }
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
         Repaint(cellLeftDown);
         result.modified.add(cellLeftDown);
      } else {
         ClickCellResult resultCell = cellLeftDown.LButtonDown(this);
         result.modified = resultCell.modified; // copy reference; TODO result.modified.addAll(resultCell.modified);
         result.modified.forEach(cell -> Repaint(cell));
      }
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
         if ((getGameStatus() == EGameStatus.eGSReady) && (cellDown == cellLeftUp))
         {
            GameBegin(cellDown);
         }
         ClickCellResult resultCell = cellDown.LButtonUp(cellDown == cellLeftUp, this);
         result.modified = resultCell.modified; // copy reference; TODO result.modified.addAll(resultCell.modified);
         result.modified.forEach(c -> Repaint(c));
         int countOpen = result.getCountOpen();
         int countFlag = result.getCountFlag();
         int countUnknown = result.getCountUnknown();
         boolean any = (countOpen > 0) || (countFlag > 0) || (countUnknown > 0); // клик со смыслом (были изменения на поле)
         if (any) {
            setCountClick(getCountClick()+1);
            setPlayInfo(EPlayInfo.ePlayerUser);  // юзер играл
            if (countOpen > 0)
               onPropertyChanged("CountOpen");
            if ((countFlag > 0) || (countUnknown > 0)) {
               onPropertyChanged("CountFlag");
               onPropertyChanged("CountMinesLeft");
               onPropertyChanged("CountUnknown");
            }
         }

         if (result.isAnyOpenMine()) {
            GameEnd(false);
         } else {
            Matrisize sizeField = getSizeField();
            if ((getCountOpen() + getMinesCount()) == sizeField.m*sizeField.n) {
               GameEnd(true);
            } else {
               VerifyFlag();
            }
         }
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
         result.modified.addAll(getMatrix()); // ??? TODO optimize
         return result;
      }
      if (getGameStatus() == EGameStatus.eGSReady)
         return result;
      if (getGameStatus() == EGameStatus.eGSCreateGame)
         return result;
      if (cellRightDown == null)
         return result;

      setCellDown(cellRightDown);
      ClickCellResult resultCell = cellRightDown.RButtonDown(cellRightDown.getState().getClose().nextState(getUseUnknown()));
      result.modified = resultCell.modified; // copy reference; TODO result.modified.addAll(resultCell.modified);
      result.modified.forEach(c -> Repaint(c));

      int countFlag = result.getCountFlag();
      int countUnknown = result.getCountUnknown();
      boolean any = (countFlag > 0) || (countUnknown > 0); // клик со смыслом (были изменения на поле)
      if (any) {
         setCountClick(getCountClick()+1);
         setPlayInfo(EPlayInfo.ePlayerUser); // то считаю что юзер играл
         onPropertyChanged("CountFlag");
         onPropertyChanged("CountMinesLeft");
         onPropertyChanged("CountUnknown");
      }

      VerifyFlag();
      if (getGameStatus() != EGameStatus.eGSEnd) {
         //...
      }
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

   protected boolean RequestToUser_RestoreLastGame() {
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
            if (RequestToUser_RestoreLastGame())
               getRepositoryMines().clear();
         }

      getMatrix().forEach(cell -> cell.Reset());

      setCountClick(0);

      setGameStatus(EGameStatus.eGSReady);
      setPlayInfo(EPlayInfo.ePlayerUnknown); // пока не знаю кто будет играть
      Repaint(null);

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

   /** площадь ячеек */
   @Override
   public double getArea() {
      if (_cellAttr == null)
         return AREA_MINIMUM;
      double area = getCellAttr().getArea();
      if (area < AREA_MINIMUM) {
         area = AREA_MINIMUM;
         getCellAttr().setArea(AREA_MINIMUM);
      }
      return area;
   }
   /** установить новую площадь ячеек */
   @Override
   public void setArea(double newArea)  {
      double oldArea = getCellAttr().getArea();
      newArea = Math.max(AREA_MINIMUM, newArea);
      if (DoubleExt.hasMinDiff(oldArea, newArea))
         return;
      getCellAttr().setArea(newArea);
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
         ? getCellAttr().getOwnerSize(sizeField)
         : MosaicHelper.getOwnerSize(getMosaicType(), area, sizeField);
   }
   /** размер в пикселях */
   public SizeDouble getWindowSize() { return getWindowSize(getSizeField(), getArea()); }
   /** узнать количество соседей для текущей мозаики */
   public int GetMaxNeighborNumber() { return getCellAttr().getNeighborNumber(true); }

   /** действительно лишь когда gameStatus == gsEnd */
   public boolean isVictory() {
      return (getGameStatus() == EGameStatus.eGSEnd) && (0 == getCountMinesLeft());
   }

   /** Mosaic field: класс окна мозаики поля */
   public MosaicBase() {
      initialize();
   }
   /** Mosaic field: класс окна мозаики поля */
   public MosaicBase(Matrisize sizeField, EMosaic mosaicType, int minesCount, double area) {
      initialize(sizeField, mosaicType, minesCount, area);
   }

   protected void initialize() {
      initialize(new Matrisize(10, 10),
            EMosaic.eMosaicSquare1,//EMosaic.eMosaicPenrousePeriodic1, //
            15, AREA_MINIMUM*10);
   }
   protected void initialize(Matrisize sizeField, EMosaic mosaicType, int minesCount, double area) {
      setSizeField(sizeField);
      setMosaicType(mosaicType);
      setMinesCount(minesCount);
      setArea(area); // ...провера на валидность есть только при установке из класса Main. Так что, не нуна тут задавать громадные велечины.
   }


   @Override
   public void propertyChange(PropertyChangeEvent ev) {
      if (ev.getSource() instanceof BaseCell.BaseAttribute)
         onCellAttributePropertyChanged((BaseCell.BaseAttribute)ev.getSource(), ev);
   }
   protected void onCellAttributePropertyChanged(BaseCell.BaseAttribute source, PropertyChangeEvent ev) {
      String propName = ev.getPropertyName();
      if ("Area".equals(propName)) {
         getMatrix().forEach(cell -> cell.Init());
         onPropertyChanged(ev.getOldValue(), ev.getNewValue(), propName); // ! rethrow event - notify parent class
         Repaint(null);
      }
      onPropertyChanged("CellAttr");
      onPropertyChanged("CellAttr." + propName);
   }

}
