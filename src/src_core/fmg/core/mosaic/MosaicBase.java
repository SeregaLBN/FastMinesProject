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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

import fmg.common.geom.Coord;
import fmg.common.geom.Matrisize;
import fmg.common.geom.Size;
import fmg.core.mosaic.cells.BaseCell;
import fmg.core.types.EClose;
import fmg.core.types.EGameStatus;
import fmg.core.types.EMosaic;
import fmg.core.types.EOpen;
import fmg.core.types.EPlayInfo;
import fmg.core.types.EState;
import fmg.core.types.click.ClickReportContext;
import fmg.core.types.click.LeftDownResult;
import fmg.core.types.click.LeftUpResult;
import fmg.core.types.click.RightDownReturn;
import fmg.core.types.event.MosaicEvent;
import fmg.core.types.event.MosaicListener;
import fmg.swing.draw.mosaic.graphics.PaintableGraphics;

/** Mosaic field: класс окна мозаики поля */
public abstract class MosaicBase implements IMosaic<PaintableGraphics> {

	public static final int AREA_MINIMUM = 230;

	/** матрица List &lt; List &lt; BaseCell &gt; &gt; , представленная(развёрнута) в виде вектора */
	protected List<BaseCell> _matrix = new ArrayList<BaseCell>(0);
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
		_cellAttr = null;
	}
	public BaseCell.BaseAttribute getCellAttr() {
		if (_cellAttr == null)
			_cellAttr = MosaicHelper.createAttributeInstance(getMosaicType(), getArea());
		return _cellAttr;
	}

	public List<BaseCell> getMatrix() {
		return _matrix;
	}

	/** размер поля в ячейках */
	@Override
	public Matrisize getSizeField() { return new Matrisize(_size); } // return clone
	/** размер поля в ячейках */
	@Override
	public void setSizeField(Matrisize newSizeField) { setParams(newSizeField, null, null ); } 

	/** узнать тип мозаики */
	@Override
	public EMosaic getMosaicType() { return _mosaicType; }
	@Override
	public void setMosaicType(EMosaic newMosaicType) { setParams(null, newMosaicType, null); }
	/** количество мин */
	public int getMinesCount() { return _minesCount; }
	public void setMinesCount(int newMinesCount    ) { setParams(null, null, newMinesCount); }

	/** установить мозаику заданного размера, типа  и с определённым количеством мин (координаты мин могут задаваться с помощью "Хранилища Мин") */
	public void setParams(Matrisize newSizeField, EMosaic newMosaicType, Integer newMinesCount, List<Coord> storageCoordMines) {
		try {
			//repositoryMines.Reset();
			if ((getMosaicType() == newMosaicType) &&
				getSizeField().equals(newSizeField) &&
				(getMinesCount() == newMinesCount))
			{
				return;
			}

			EMosaic oldMosaicType = this._mosaicType;
			Matrisize oldMosaicSize = this._size;
			boolean isNewMosaic = (newMosaicType != null) && (newMosaicType != this._mosaicType);
			boolean isNewSizeFld = (newSizeField != null) && !newSizeField.equals(this._size);

			int saveArea = getArea();
			if (isNewSizeFld) {
				setCellDown(null); // чтобы не было IndexOutOfBoundsException при уменьшении размера поля когда удерживается клик на поле...
				this._size = newSizeField;
			}
			if (isNewMosaic) {
				this._mosaicType = newMosaicType;
				setCellAttr(null);
			}
			if (newMinesCount != null) {
				if (newMinesCount == 0)
					this._oldMinesCount = this._minesCount;
				this._minesCount = newMinesCount;
			}
			_minesCount = Math.max(1, Math.min(_minesCount, GetMaxMines(this._size)));
			if (saveArea != getArea())
				setArea(saveArea);
	
			if (isNewSizeFld || isNewMosaic) {
				BaseCell.BaseAttribute attr = getCellAttr();

				for (BaseCell cell: _matrix)
					// отписываю старые ячейки от уведомлений атрибута
					attr.removePropertyChangeListener(cell);

				_matrix.clear();
				//_matrix = new ArrayList<BaseCell>(_size.width*_size.height);
				for (int i=0; i < _size.m; i++)
					for (int j=0; j < _size.n; j++) {
						BaseCell cell = MosaicHelper.createCellInstance(attr, _mosaicType, new Coord(i, j));
						_matrix.add(i*_size.n + j, cell);
						attr.addPropertyChangeListener(cell); // подписываю новые ячейки на уведомления атрибута (изменение a -> перерасчёт координат)
					}
	
				for (BaseCell cell: _matrix)
					cell.IdentifyNeighbors(this);
			}

			getMosaicListeners().fireOnChangedCounters(new MosaicEvent.ChangedCountersEvent(MosaicBase.this));
			if (isNewMosaic)
				getMosaicListeners().fireOnChangedMosaicType(new MosaicEvent.ChangedMosaicTypeEvent(MosaicBase.this, oldMosaicType));
			if (isNewSizeFld)
				getMosaicListeners().fireOnChangedMosaicSize(new MosaicEvent.ChangedMosaicSizeEvent(MosaicBase.this, oldMosaicSize));
		} finally {
			if ((storageCoordMines == null) || storageCoordMines.isEmpty())
				getRepositoryMines().clear();
			else
				setRepositoryMines(storageCoordMines);
			//setGameStatus(EGameStatus.eGSEnd);
			GameNew();
		}
	}

	/** установить мозаику заданного размера, типа и с определённым количеством мин */
	public void setParams(Matrisize sizeField, EMosaic mosaicType, Integer minesCount) {
		setParams(sizeField, mosaicType, minesCount, null);
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
		for (BaseCell cell: _matrix)
			cell.getState().CalcOpenState();
	}
	/** arrange Mines - set random mines */
	public void setMines_random(BaseCell firstClickCell) {
		if (_minesCount == 0)
			_minesCount = _oldMinesCount;
		
		List<BaseCell> matrixClone = new ArrayList<BaseCell>(_matrix);
		matrixClone.remove(firstClickCell); // исключаю на которой кликал юзер
		matrixClone.removeAll(Arrays.asList(firstClickCell.getNeighbors())); // и их соседей
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
		for (BaseCell cell: _matrix)
			cell.getState().CalcOpenState();
	}

	public int getCountOpen() {
		int cnt = 0;
		for (BaseCell cell: _matrix)
			if (cell.getState().getStatus() == EState._Open)
				cnt++;
		return cnt;
	}
	public int getCountFlag() {
		int cnt = 0;
		for (BaseCell cell: _matrix)
			if ((cell.getState().getStatus() == EState._Close) &&
				(cell.getState().getClose() == EClose._Flag))
					cnt++;
		return cnt;
	}
	public int getCountUnknown() {
		int cnt = 0;
		for (BaseCell cell: _matrix)
			if ((cell.getState().getStatus() == EState._Close) &&
				(cell.getState().getClose() == EClose._Unknown))
					cnt++;
		return cnt;
	}
	
	/** сколько ещё осталось открыть мин */
	public int getCountMinesLeft() { return getMinesCount() - getCountFlag(); }
	public int getCountClick()  { return _countClick; }
	public void setCountClick(int clickCount)  { _countClick=clickCount; getMosaicListeners().fireOnChangedCounters(new MosaicEvent.ChangedCountersEvent(this)); }

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
		_gameStatus = newStatus;
		getMosaicListeners().fireOnChangedGameStatus(new MosaicEvent.ChangedGameStatusEvent(this, old));
	}

	public EPlayInfo getPlayInfo() {
		if (_playInfo == null)
			_playInfo = EPlayInfo.ePlayerUnknown;
		return _playInfo;
	}
	public void setPlayInfo(EPlayInfo newVal) {
		_playInfo = EPlayInfo.setPlayInfo(getPlayInfo(), newVal);
	}

	private List<Coord> getRepositoryMines() {
		if (_repositoryMines == null)
			_repositoryMines = new ArrayList<Coord>(0);
		return _repositoryMines;
	}
	private void setRepositoryMines(List<Coord> repositoryMines) {
		this._repositoryMines = repositoryMines;
	}

	protected static class MosaicListeners {
		private List<MosaicListener> listeners1; // variant 1
//		private EventListenerList listeners2; // variant 2
		
		public List<MosaicListener> getListeners() {
			if (listeners1 == null)
				listeners1 = new ArrayList<MosaicListener>(1); // java.util.Collections.synchronizedList(new ArrayList<MosaicListener>(1));
			return listeners1;
//			if (listeners2 == null)
//				listeners2 = new EventListenerList();
//			return Arrays.asList(listeners2.getListeners(MosaicListener.class));
		}
		public void add(MosaicListener l) {
			listeners1.add(l);			
//			listeners2.add(MosaicListener.class, l);
		}
		public void remove(MosaicListener l) {
			listeners1.remove(l);
//			listeners2.remove(MosaicListener.class, l);
		}

		/** уведомить о клике на мозаике */
		public void fireOnClick(MosaicEvent.ClickEvent e) {
			for (MosaicListener l: getListeners())
				l.OnClick(e);
		}
		/** уведомление: изменено кол-во открытых ячеек / флагов / кликов / ... */
		public void fireOnChangedCounters(MosaicEvent.ChangedCountersEvent e) {
			for (MosaicListener l: getListeners())
				l.OnChangedCounters(e);
		}
		/** уведомить об изменении статуса игры (новая игра, начало игры, конец игры) */
		public void fireOnChangedGameStatus(MosaicEvent.ChangedGameStatusEvent e) {
			for (MosaicListener l: getListeners())
				l.OnChangedGameStatus(e);
		}
		/** уведомить об изменении размера площади у ячейки */
		public void fireOnChangedArea(MosaicEvent.ChangedAreaEvent e) {
			for (MosaicListener l: getListeners())
				l.OnChangedArea(e);
		}
		/** уведомить об изменении размера площади у ячейки */
		public void fireOnChangedMosaicType(MosaicEvent.ChangedMosaicTypeEvent e) {
			for (MosaicListener l: getListeners())
				l.OnChangedMosaicType(e);
		}
		/** уведомить об изменении размера мозаики */
		public void fireOnChangedMosaicSize(MosaicEvent.ChangedMosaicSizeEvent e) {
			for (MosaicListener l: getListeners())
				l.OnChangedMosaicSize(e);
		}
	}
	private MosaicListeners _mosaicListeners;
	protected MosaicListeners getMosaicListeners() {
		if (_mosaicListeners  == null)
			_mosaicListeners = new MosaicListeners();
		return _mosaicListeners;
	}
	/**  подписаться на уведомления изменений мозаики */
	public void addMosaicListener(MosaicListener listener) {
		getMosaicListeners().add(listener);
	}
	/**  отписаться от уведомлений изменений мозаики */
	public void removeMosaicListener(MosaicListener listener) {
		getMosaicListeners().remove(listener);
	}

	/** перерисовать ячейку; если null - перерисовать всё поле */
	protected abstract void Repaint(BaseCell cell);
	
	/** Начать игру, т.к. произошёл первый клик на поле */
	protected void GameBegin(BaseCell firstClickCell) {
		Repaint(null);

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
	private void GameEnd(boolean victory) {
		if (getGameStatus() == EGameStatus.eGSEnd) return;

		{ // открыть оставшeеся
//			::SetCursor(::LoadCursor(NULL, IDC_WAIT));
			for (BaseCell cell: _matrix)
				if (cell.getState().getStatus() == EState._Close) {
					if (victory) {
						if (cell.getState().getOpen() == EOpen._Mine)
						{
							cell.getState().setClose(EClose._Flag, null);
						} else {
							cell.getState().setStatus(EState._Open, null);
							cell.getState().setDown(true);
						}
					} else {
						if ((cell.getState().getOpen() != EOpen._Mine) ||
							(cell.getState().getClose() != EClose._Flag))
						{
							cell.getState().setStatus(EState._Open, null);
						}
					}
					Repaint(cell);
				}
//			::SetCursor(::LoadCursor(NULL, IDC_ARROW));
		}
		//BeepSpeaker();

		setGameStatus(EGameStatus.eGSEnd);
		getMosaicListeners().fireOnChangedCounters(new MosaicEvent.ChangedCountersEvent(this));
	}

	private void VerifyFlag() {
		if (getGameStatus() == EGameStatus.eGSEnd) return;
		if (getMinesCount() == getCountFlag()) {
			for (BaseCell cell: _matrix)
				if ((cell.getState().getClose() == EClose._Flag) &&
					(cell.getState().getOpen() != EOpen._Mine))
					return; // неверно проставленный флажок - на выход
			GameEnd(true);
		} else
			if (getMinesCount() == (getCountFlag() + getCountUnknown())) {
				for (BaseCell cell: _matrix)
					if (((cell.getState().getClose() == EClose._Unknown) ||
						( cell.getState().getClose() == EClose._Flag)) &&
						( cell.getState().getOpen() != EOpen._Mine))
						return; // неверно проставленный флажок или '?'- на выход
				GameEnd(true);
			}
		return;
	}

	protected boolean OnLeftButtonDown(BaseCell cellLeftDown) {
		setCellDown(null);
		if (getGameStatus() == EGameStatus.eGSEnd)
			return false;
		if (cellLeftDown == null)
			return false;

		setCellDown(cellLeftDown);
		if (getGameStatus() == EGameStatus.eGSCreateGame) {
			if (cellLeftDown.getState().getOpen() != EOpen._Mine) {
				cellLeftDown.getState().setStatus(EState._Open, null);
				cellLeftDown.getState().SetMine();
				setMinesCount(getMinesCount()+1);
				getRepositoryMines().add(cellLeftDown.getCoord());
			} else {
				cellLeftDown.Reset();
				setMinesCount(getMinesCount()-1);
				getRepositoryMines().remove(cellLeftDown.getCoord());
			}
			Repaint(cellLeftDown);
		} else {
			LeftDownResult result = cellLeftDown.LButtonDown();
			if ((result != null) && (result.needRepaint != null))
				for (BaseCell cellToRepaint : result.needRepaint)
					Repaint(cellToRepaint);
		}
		getMosaicListeners().fireOnClick(new MosaicEvent.ClickEvent(this, cellLeftDown, true, true));
		return true;
	}

	protected boolean OnLeftButtonUp(BaseCell cellLeftUp) {
		try {
			if (getGameStatus() == EGameStatus.eGSEnd)
				return false;
			BaseCell cell = getCellDown(); 
			if (cell == null)
				return false;
			if (getGameStatus() == EGameStatus.eGSCreateGame)
				return false;
	
	//		System.out.println("OnLeftButtonUp: coordLUp="+coordLUp);
			if ((getGameStatus() == EGameStatus.eGSReady) && (cell == cellLeftUp))
			{
				GameBegin(cell);
			}
			ClickReportContext clickReportContext = new ClickReportContext();
			LeftUpResult result = cell.LButtonUp(cell == cellLeftUp, clickReportContext);
			if (result.needRepaint != null)
				for (BaseCell cellToRepaint : result.needRepaint)
					Repaint(cellToRepaint);
			boolean res = (result.countOpen > 0) || (result.countFlag > 0) || (result.countUnknown > 0); // клик со смыслом (были изменения на поле) 
			if (res) {
				setCountClick(getCountClick()+1);
				setPlayInfo(EPlayInfo.ePlayerUser);  // юзер играл
				getMosaicListeners().fireOnChangedCounters(new MosaicEvent.ChangedCountersEvent(this));
			}
	
			if (result.endGame) {
				GameEnd(result.victory);
			} else {
				Matrisize sizeField = getSizeField();
				if ((getCountOpen() + getMinesCount()) == sizeField.m*sizeField.n) {
					GameEnd(true);
				} else {
					VerifyFlag();
				}
			}
			getMosaicListeners().fireOnClick(new MosaicEvent.ClickEvent(this, cell, true, false));
			return res;
		} finally {
			setCellDown(null);
		}
	}

	protected boolean OnRightButtonDown(BaseCell cellRightDown) {
		setCellDown(null);
		if (getGameStatus() == EGameStatus.eGSEnd) {
			GameNew();
			return true;
		}
		if (getGameStatus() == EGameStatus.eGSReady)
			return false;
		if (getGameStatus() == EGameStatus.eGSCreateGame)
			return false;
		if (cellRightDown == null)
			return false;

		setCellDown(cellRightDown);
		EClose eClose;
		switch (cellRightDown.getState().getClose()) {
		case _Clear  : eClose = EClose._Flag; break;
		case _Flag   : eClose = getUseUnknown() ? EClose._Unknown : EClose._Clear; break;
		default: // fix: The local variable eClose may not have been initialized
		//case _Unknown:
			eClose = EClose._Clear;
			break;
		}
		ClickReportContext clickReportContext = new ClickReportContext();
		RightDownReturn result = cellRightDown.RButtonDown(eClose, clickReportContext);
		if (result.needRepaint)
			Repaint(cellRightDown);
		boolean res = (result.countFlag>0) || (result.countUnknown>0); // клик со смыслом (были изменения на поле)
		if (res) {
			setCountClick(getCountClick()+1);
			setPlayInfo(EPlayInfo.ePlayerUser); // то считаю что юзер играл
			getMosaicListeners().fireOnChangedCounters(new MosaicEvent.ChangedCountersEvent(this));
		}

		VerifyFlag();
		if (getGameStatus() != EGameStatus.eGSEnd) {
			//...
		}
		getMosaicListeners().fireOnClick(new MosaicEvent.ClickEvent(this, cellRightDown, false, true));
		return res;
	}

	protected boolean OnRightButtonUp(/*BaseCell cellRightUp*/) {
		try {
			BaseCell cell = getCellDown(); 
			if (cell == null)
				return false;
			getMosaicListeners().fireOnClick(new MosaicEvent.ClickEvent(this, cell, false, false));
			return true;
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
	public void GameNew() {
//		System.out.println("Mosaic::GameNew()");

		if (getGameStatus() == EGameStatus.eGSReady)
			return;

		if (!getRepositoryMines().isEmpty())
			if (getGameStatus() == EGameStatus.eGSCreateGame) {
			} else {
				if (RequestToUser_RestoreLastGame())
					getRepositoryMines().clear();
			}

		for (BaseCell cell: _matrix)
			cell.Reset();

		setCountClick(0);

		setGameStatus(EGameStatus.eGSReady);
		setPlayInfo(EPlayInfo.ePlayerUnknown); // пока не знаю кто будет играть
	}

	/** создать игру игроком - он сам расставит мины */
	public void GameCreate() {
		GameNew();
		if (getRepositoryMines().isEmpty()) {
			setMinesCount(0);
			setGameStatus(EGameStatus.eGSCreateGame);
			getMosaicListeners().fireOnChangedCounters(new MosaicEvent.ChangedCountersEvent(this));
		}
	}

	/** площадь ячеек */
	public int getArea() {
		if (_cellAttr == null)
			return AREA_MINIMUM;
		int area = getCellAttr().getArea();
		if (area < AREA_MINIMUM) {
			area = AREA_MINIMUM;
			getCellAttr().setArea(AREA_MINIMUM);
		}
		return area;
	}
	/** установить новую площадь ячеек */
	public void setArea(int newArea)  {
		int oldArea = getCellAttr().getArea();
		if (oldArea == Math.max(AREA_MINIMUM, newArea)) return;
		getCellAttr().setArea(Math.max(AREA_MINIMUM, newArea));

		getMosaicListeners().fireOnChangedArea(new MosaicEvent.ChangedAreaEvent(this, oldArea));
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
	public Size getWindowSize(Matrisize sizeField, int area) {
		return (area == getArea())
			? getCellAttr().getOwnerSize(sizeField)
			: MosaicHelper.getOwnerSize(getMosaicType(), area, sizeField);
	}
	/** размер в пикселях */
	public Size getWindowSize() { return getWindowSize(getSizeField(), getArea()); }
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
	public MosaicBase(Matrisize sizeField, EMosaic mosaicType, int minesCount, int area) {
		initialize(sizeField, mosaicType, minesCount, area);
	}

	public List<Coord> getStorageMines() {
		List<Coord> repositoryMines = new ArrayList<Coord>();
		for (BaseCell cell: _matrix)
			if (cell.getState().getOpen() == EOpen._Mine)
				repositoryMines.add(cell.getCoord());
		return repositoryMines;
	}

	protected void initialize() {
		initialize(new Matrisize(10, 10),
				EMosaic.eMosaicSquare1,//EMosaic.eMosaicPenrousePeriodic1, // 
				15, AREA_MINIMUM*10);
	}
	protected void initialize(Matrisize sizeField, EMosaic mosaicType, int minesCount, int area) {
		setParams(sizeField, mosaicType, minesCount);
		setArea(area); // ...провера на валидность есть только при установке из класса Main. Так что, не нуна тут задавать громадные велечины.
	}
}
