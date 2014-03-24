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
package ua.ksn.fmg.controller;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

import ua.ksn.fmg.controller.event.MosaicEvent;
import ua.ksn.fmg.controller.event.MosaicListener;
import ua.ksn.fmg.controller.types.EGameStatus;
import ua.ksn.fmg.controller.types.EPlayInfo;
import ua.ksn.fmg.event.click.ClickReportContext;
import ua.ksn.fmg.event.click.LeftDownResult;
import ua.ksn.fmg.event.click.LeftUpResult;
import ua.ksn.fmg.event.click.RightDownReturn;
import ua.ksn.fmg.model.mosaics.CellFactory;
import ua.ksn.fmg.model.mosaics.EClose;
import ua.ksn.fmg.model.mosaics.EMosaic;
import ua.ksn.fmg.model.mosaics.EOpen;
import ua.ksn.fmg.model.mosaics.EState;
import ua.ksn.fmg.model.mosaics.cell.BaseCell;
import ua.ksn.geom.Coord;
import ua.ksn.geom.Size;

/** Mosaic field: класс окна мозаики поля */
public abstract class Mosaic {

	/** размер поля в ячейках */
	public Size getSizeField() {
		return getCells().getSize();
	}

	public static final int AREA_MINIMUM = 230;
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
			_cellAttr = CellFactory.createAttributeInstance(getCells().getMosaicType(), getArea());
		return _cellAttr;
	}

	/** матрица ячеек поля мозаики */
	protected class MatrixCells implements BaseCell.IMatrixCells {
		/** матрица List &lt; List &lt; BaseCell &gt; &gt; , представленная(развёрнута) в виде вектора */
		protected List<BaseCell> _matrix = new ArrayList<BaseCell>(0);
		/** размер поля в ячейках */
		protected Size _size = new Size(0, 0);
		/** из каких фигур состоит мозаика поля */
		protected EMosaic _mosaicType = EMosaic.eMosaicSquare1;
		/** кол-во мин на поле */
		protected int _minesCount = 1;
		/** кол-во мин на поле до создания игры. Используется когда игра была создана, но ни одной мины не проставлено. */
		protected int _oldMinesCount = 1;

//		public void setSize(Size newSizeField          ) { setParams(newSizeField, null, null ); } 
//		public void setMosaicType(EMosaic newMosaicType) { setParams(null, newMosaicType, null); }
		public void setMinesCount(int newMinesCount    ) { setParams(null, null, newMinesCount); }
		public void setParams(Size newSizeField, EMosaic newMosaicType, Integer newMinesCount) {
			EMosaic oldMosaicType = this._mosaicType;
			boolean newMosaciType = (newMosaicType != this._mosaicType);
			boolean recreateMatrix =
				((newSizeField  != null) && !newSizeField.equals(this._size)) ||
				((newMosaicType != null) && newMosaciType);

			int saveArea = getArea();
			if (newSizeField != null)
				this._size = newSizeField;
			if (newMosaicType != null)
				if (this._mosaicType != newMosaicType) {
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

			if (recreateMatrix) {
				BaseCell.BaseAttribute attr = getCellAttr();

				// отписываю старые ячейки от уведомлений атрибута
				for (BaseCell cell: _matrix)
					attr.removePropertyChangeListener(cell);

				_matrix.clear();
				_matrix = new ArrayList<BaseCell>(_size.width*_size.height);
				for (int i=0; i < _size.width; i++)
					for (int j=0; j < _size.height; j++) {
						BaseCell cell = CellFactory.createCellInstance(attr, _mosaicType, new Coord(i, j));
						_matrix.add(i*_size.height + j, cell);
						attr.addPropertyChangeListener(cell); // подписываю новые ячейки на уведомления атрибута (изменение a -> перерасчёт координат)
					}

				for (BaseCell cell: _matrix)
					cell.IdentifyNeighbors(this);
			}

			getMosaicListeners().fireOnChangeCounters(new MosaicEvent.ChangeCountersEvent(Mosaic.this));
			if (newMosaciType)
				getMosaicListeners().fireOnChangeMosaicType(new MosaicEvent.ChangeMosaicTypeEvent(Mosaic.this, oldMosaicType));
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
		public void setMines_random(Coord firstClickCoord) {
			if (_minesCount == 0)
				_minesCount = _oldMinesCount;
			
			BaseCell firstClickCell = getCell(firstClickCoord);
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

		/** размер поля в ячейках */
		@Override
		public Size getSize() { return new Size(_size); } // return clone
		/** узнать тип мозаики */
		public EMosaic getMosaicType() { return _mosaicType; }
		/** кол-во мин */
		public int getMinesCount() { return _minesCount; }

		public List<BaseCell> getAll() {
			return _matrix;
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
		
		/** доступ к заданной ячейке */
		public BaseCell getCell(int x, int y) { return _matrix.get(x*_size.height + y); }
		/** доступ к заданной ячейке */
		@Override
		public BaseCell getCell(Coord coord) { return getCell(coord.x, coord.y); }
	}
	protected MatrixCells _cells;
	/** матрица ячеек */
	protected MatrixCells getCells() {
		if (_cells == null)
			_cells = new MatrixCells();
		return _cells;
	}

	/** координаты ячейки на которой было нажато (но не обязательно что отпущено) */
	private Coord _coordDown;
	protected void setCoordDown(Coord coord) { _coordDown = coord; }
	protected Coord getCoordDown() {
		if (_coordDown == null)
			_coordDown = Coord.INCORRECT_COORD;
		return _coordDown;
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
	private EGameStatus _gameStatus;
	public EGameStatus getGameStatus() {
		if (_gameStatus == null)
			_gameStatus = EGameStatus.eGSEnd;
		return _gameStatus;
	}
	public void setGameStatus(EGameStatus newStatus) {
		EGameStatus old = _gameStatus; 
		_gameStatus = newStatus;
		getMosaicListeners().fireOnChangeGameStatus(new MosaicEvent.ChangeGameStatusEvent(this, old));
	}

	private EPlayInfo _playInfo;
	public EPlayInfo getPlayInfo() {
		if (_playInfo == null)
			_playInfo = EPlayInfo.ePlayerUnknown;
		return _playInfo;
	}
	public void setPlayInfo(EPlayInfo newVal) {
		_playInfo = EPlayInfo.setPlayInfo(getPlayInfo(), newVal);
	}

	private int _countClick;

	/** для load'a - координаты ячеек с минами */
	private List<Coord> _repositoryMines;
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
		public void fireOnChangeCounters(MosaicEvent.ChangeCountersEvent e) {
			for (MosaicListener l: getListeners())
				l.OnChangeCounters(e);
		}
		/** уведомить об изменении статуса игры (новая игра, начало игры, конец игры) */
		public void fireOnChangeGameStatus(MosaicEvent.ChangeGameStatusEvent e) {
			for (MosaicListener l: getListeners())
				l.OnChangeGameStatus(e);
		}
		/** уведомить о изменении размера площади у ячейки */
		public void fireOnChangeArea(MosaicEvent.ChangeAreaEvent e) {
			for (MosaicListener l: getListeners())
				l.OnChangeArea(e);
		}
		/** уведомить о изменении размера площади у ячейки */
		public void fireOnChangeMosaicType(MosaicEvent.ChangeMosaicTypeEvent e) {
			for (MosaicListener l: getListeners())
				l.OnChangeMosaicType(e);
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
	protected void GameBegin(Coord firstClick) {
		Repaint(null);

		setGameStatus(EGameStatus.eGSPlay);

		// set mines
		if (!getRepositoryMines().isEmpty()) {
			setPlayInfo(EPlayInfo.ePlayIgnor);
			getCells().setMines_LoadRepository(getRepositoryMines());
		} else {
			getCells().setMines_random(firstClick);
		}
	}

	/** Завершить игру */
	private void GameEnd(boolean victory) {
		if (getGameStatus() == EGameStatus.eGSEnd) return;

		{ // открыть оставшeеся
//			::SetCursor(::LoadCursor(NULL, IDC_WAIT));
			for (BaseCell cell: getCells().getAll())
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
		getMosaicListeners().fireOnChangeCounters(new MosaicEvent.ChangeCountersEvent(this));
	}

	private void VerifyFlag() {
		if (getGameStatus() == EGameStatus.eGSEnd) return;
		if (getCells().getMinesCount() == getCells().getCountFlag()) {
			for (BaseCell cell: getCells().getAll())
				if ((cell.getState().getClose() == EClose._Flag) &&
					(cell.getState().getOpen() != EOpen._Mine))
					return; // неверно проставленный флажок - на выход
			GameEnd(true);
		} else
			if (getCells().getMinesCount() == (getCells().getCountFlag() + getCells().getCountUnknown())) {
				for (BaseCell cell: getCells().getAll())
					if (((cell.getState().getClose() == EClose._Unknown) ||
						( cell.getState().getClose() == EClose._Flag)) &&
						( cell.getState().getOpen() != EOpen._Mine))
						return; // неверно проставленный флажок или '?'- на выход
				GameEnd(true);
			}
		return;
	}

	protected void OnLeftButtonDown(Coord coordLDown) {
		setCoordDown(Coord.INCORRECT_COORD);
		if (getGameStatus() == EGameStatus.eGSEnd)
			return;
		if (coordLDown.equals(Coord.INCORRECT_COORD))
			return;
		getMosaicListeners().fireOnClick(new MosaicEvent.ClickEvent(this, true, true));

		setCoordDown(coordLDown);
		BaseCell cell = getCell(coordLDown);
		if (getGameStatus() == EGameStatus.eGSCreateGame) {
			if (cell.getState().getOpen() != EOpen._Mine) {
				cell.getState().setStatus(EState._Open, null);
				cell.getState().SetMine();
				getCells().setMinesCount(getCells().getMinesCount()+1);
				getRepositoryMines().add(coordLDown);
			} else {
				cell.Reset();
				getCells().setMinesCount(getCells().getMinesCount()-1);
				getRepositoryMines().remove(coordLDown);
			}
			Repaint(cell);
		} else {
			LeftDownResult result = cell.LButtonDown();
			if ((result != null) && (result.needRepaint != null))
				for (BaseCell cellToRepaint : result.needRepaint)
					Repaint(cellToRepaint);
		}
	}

	protected void OnLeftButtonUp(Coord coordLUp) {
		if (getGameStatus() == EGameStatus.eGSEnd)
			return;
		if (getCoordDown().equals(Coord.INCORRECT_COORD))
			return;
		getMosaicListeners().fireOnClick(new MosaicEvent.ClickEvent(this, true, false));
		if (getGameStatus() == EGameStatus.eGSCreateGame)
			return;

//		System.out.println("OnLeftButtonUp: coordLUp="+coordLUp);
		if ((getGameStatus() == EGameStatus.eGSReady) && coordLUp.equals(getCoordDown()))
		{
			GameBegin(getCoordDown());
		}
		ClickReportContext clickReportContext = new ClickReportContext();
		LeftUpResult result = getCell(getCoordDown()).LButtonUp(coordLUp.equals(getCoordDown()), clickReportContext);
		if (result.needRepaint != null)
			for (BaseCell cellToRepaint : result.needRepaint)
				Repaint(cellToRepaint);
		if ((result.countOpen > 0) || (result.countFlag > 0) || (result.countUnknown > 0)) { // клик со смыслом (были изменения на поле)
			incrementCountClick();
			setPlayInfo(EPlayInfo.ePlayerUser);  // юзер играл
			getMosaicListeners().fireOnChangeCounters(new MosaicEvent.ChangeCountersEvent(this));
		}

		if (result.endGame) {
			GameEnd(result.victory);
		} else {
			Size sizeField = getCells().getSize();
			if ((getCells().getCountOpen() + getCells().getMinesCount()) == sizeField.width*sizeField.height) {
				GameEnd(true);
			} else {
				VerifyFlag();
			}
		}
	}

	protected void OnRightButtonDown(Coord coordRDown) {
		if (getGameStatus() == EGameStatus.eGSEnd) {
			GameNew();
			return;
		}
		if (getGameStatus() == EGameStatus.eGSReady)
			return;
		if (getGameStatus() == EGameStatus.eGSCreateGame)
			return;
		if (coordRDown.equals(Coord.INCORRECT_COORD))
			return;
		getMosaicListeners().fireOnClick(new MosaicEvent.ClickEvent(this, false, true));

		EClose eClose;
		BaseCell cell = getCell(coordRDown);
		switch (cell.getState().getClose()) {
		case _Clear  : eClose = EClose._Flag; break;
		case _Flag   : eClose = getUseUnknown() ? EClose._Unknown : EClose._Clear; break;
		default: // fix: The local variable eClose may not have been initialized
		//case _Unknown:
			eClose = EClose._Clear;
			break;
		}
		ClickReportContext clickReportContext = new ClickReportContext();
		RightDownReturn result = cell.RButtonDown(eClose, clickReportContext);
		if (result.needRepaint)
			Repaint(cell);
		if ((result.countFlag>0) || (result.countUnknown>0)) { // клик со смыслом (были изменения на поле)
			incrementCountClick();
			setPlayInfo(EPlayInfo.ePlayerUser); // то считаю что юзер играл
			getMosaicListeners().fireOnChangeCounters(new MosaicEvent.ChangeCountersEvent(this));
		}

		VerifyFlag();
		if (getGameStatus() != EGameStatus.eGSEnd) {
			//...
		}
//		::BitBlt(m_GContext.m_hDCWnd, 0, 0, m_GContext.m_SizeBitmap.cx, m_GContext.m_SizeBitmap.cy,
//				m_GContext.m_hDCDst, 0, 0, SRCCOPY);

	}

	protected void OnRightButtonUp(/*Coord coordRUp*/) {
		getMosaicListeners().fireOnClick(new MosaicEvent.ClickEvent(this, false, false));
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

		for (BaseCell cell: getCells().getAll())
			cell.Reset();

		resetCountClick();

		setGameStatus(EGameStatus.eGSReady);
		setPlayInfo(EPlayInfo.ePlayerUnknown); // пока не знаю кто будет играть
	}

	/** создать игру игроком - он сам расставит мины */
	public void GameCreate() {
		GameNew();
		if (getRepositoryMines().isEmpty()) {
			getCells().setMinesCount(0);
			setGameStatus(EGameStatus.eGSCreateGame);
			getMosaicListeners().fireOnChangeCounters(new MosaicEvent.ChangeCountersEvent(this));
		}
	}

	/** установить мозаику заданного размера, типа и с определённым количеством мин */
	public void setParams(Size sizeField, EMosaic mosaicType, int minesCount) {
		setParams(sizeField, mosaicType, minesCount, null);
	}

	/** установить мозаику заданного размера, типа  и с определённым количеством мин (координаты мин могут задаваться с помощью "Хранилища Мин") */
	public void setParams(Size sizeField, EMosaic mosaicType, int minesCount, List<Coord> storageCoordMines)
	{
		//repositoryMines.Reset();
		if ((getCells().getMosaicType() == mosaicType) &&
			getCells().getSize().equals(sizeField) &&
			(getCells().getMinesCount() == minesCount))
		{
			GameNew();
			return;
		}

		setCoordDown(Coord.INCORRECT_COORD); // чтобы небыло IndexOutOfBoundsException при уменьшении размера поля когда удерживается клик на поле...

		getCells().setParams(sizeField, mosaicType, minesCount);
		if ((storageCoordMines == null) || storageCoordMines.isEmpty())
			getRepositoryMines().clear();
		else
			setRepositoryMines(storageCoordMines);
		//setGameStatus(EGameStatus.eGSEnd);
		GameNew();
	}

	/** доступ к заданной ячейке */
	public BaseCell getCell(int x, int y) { return getCells().getCell(x, y); }
	public BaseCell getCell(Coord coord) { return getCells().getCell(coord); }
//	private void setCell(Coord coord, BaseCell cell) { mosaic.get(coord.x).set(coord.y, cell); }

	/** количество мин */
	public int getMinesCount() { return getCells().getMinesCount(); }
	/** узнать тип мозаики */
	public EMosaic getMosaicType() { return getCells().getMosaicType(); }
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

		getMosaicListeners().fireOnChangeArea(new MosaicEvent.ChangeAreaEvent(this, oldArea));
	}
	public void setUseUnknown(boolean val) { _useUnknown = val; }
	public boolean getUseUnknown() { return _useUnknown; }

	/** Максимальное кол-во мин при указанном размере поля */
	public int GetMaxMines(Size sizeFld) {
		int iMustFreeCell = GetNeighborNumber()+1;
		int iMaxMines = sizeFld.width*sizeFld.height-iMustFreeCell;
		return Math.max(1, iMaxMines);
	}
	/** Максимальное кол-во мин при  текущем  размере поля */
	public int GetMaxMines() { return GetMaxMines(getSizeField()); }
	/** размер в пикселях для указанных параметров */
	public Size CalcWindowSize(Size sizeField, int area) { return getCellAttr().CalcOwnerSize(sizeField, area); }
	/** размер в пикселях */
	public Size getWindowSize() { return CalcWindowSize(getSizeField(), getArea()); }
	/** узнать количество соседей для текущей мозаики */
	public int GetNeighborNumber() { return getCellAttr().getNeighborNumber(); }

	/** сколько ещё осталось открыть мин */
	public int getCountMinesLeft() { return getCells().getMinesCount() - getCells().getCountFlag(); }
	public int getCountClick()  { return _countClick; }
	private void resetCountClick()     { _countClick=0; getMosaicListeners().fireOnChangeCounters(new MosaicEvent.ChangeCountersEvent(this)); }
	private void incrementCountClick() { _countClick++; getMosaicListeners().fireOnChangeCounters(new MosaicEvent.ChangeCountersEvent(this)); }
	public int getCountOpen()  { return getCells().getCountOpen(); }
	/** действительно лишь когда gameStatus == gsEnd */
	public boolean isVictory() {
		return (getGameStatus() == EGameStatus.eGSEnd) && (0 == getCountMinesLeft());
	}

	/** Mosaic field: класс окна мозаики поля */
	public Mosaic() {
		initialize();
	}
	/** Mosaic field: класс окна мозаики поля */
	public Mosaic(Size sizeField, EMosaic mosaicType, int minesCount, int area) {
		initialize(sizeField, mosaicType, minesCount, area);
	}

	public List<Coord> getStorageMines() {
		List<Coord> repositoryMines = new ArrayList<Coord>();
		for (BaseCell cell: getCells().getAll())
			if (cell.getState().getOpen() == EOpen._Mine)
				repositoryMines.add(cell.getCoord());
		return repositoryMines;
	}

	protected void initialize() {
		initialize(new Size(10, 10),
				EMosaic.eMosaicSquare1,//EMosaic.eMosaicPenrousePeriodic1, // 
				15, AREA_MINIMUM*10);
	}
	protected void initialize(Size sizeField, EMosaic mosaicType, int minesCount, int area) {
		setParams(sizeField, mosaicType, minesCount);
		setArea(area); // ...провера на валидность есть только при установке из класса Main. Так что, не нуна тут задавать громадные велечины.
	}
}
