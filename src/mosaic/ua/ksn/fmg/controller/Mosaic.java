////////////////////////////////////////////////////////////////////////////////
//                               FastMines project
//                                      � Sergey Krivulya (KSerg, aka SeregaLBN)
// file name: "Mosaic.java"
//
// ���������� ��������� ������� ��������� �� �����
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

/** Mosaic field: ����� ���� ������� ���� */
public abstract class Mosaic implements BaseCell.IMatrixCells {

	public static final int AREA_MINIMUM = 230;

	/** ������� List &lt; List &lt; BaseCell &gt; &gt; , ��������������(���������) � ���� ������� */
	protected List<BaseCell> _matrix = new ArrayList<BaseCell>(0);
	/** ������ ���� � ������� */
	protected Size _size = new Size(0, 0);
	/** �� ����� ����� ������� ������� ���� */
	protected EMosaic _mosaicType = EMosaic.eMosaicSquare1;
	/** ���-�� ��� �� ���� */
	protected int _minesCount = 1;
	/** ���-�� ��� �� ���� �� �������� ����. ������������ ����� ���� ���� �������, �� �� ����� ���� �� �����������. */
	protected int _oldMinesCount = 1;

	private EGameStatus _gameStatus;
	private EPlayInfo _playInfo;
	private int _countClick;

	/** ��� load'a - ���������� ����� � ������ */
	private List<Coord> _repositoryMines;

	/** ������������ �� ������ �� ���� */
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
			_cellAttr = CellFactory.createAttributeInstance(getMosaicType(), getArea());
		return _cellAttr;
	}

	/** ������ ���� � ������� */
	@Override
	public Size getSizeField() { return new Size(_size); } // return clone
	/** ������ ��� ������� */
	public EMosaic getMosaicType() { return _mosaicType; }
	/** ���������� ��� */
	public int getMinesCount() { return _minesCount; }

//		public void setSize(Size newSizeField          ) { setParams(newSizeField, null, null ); } 
//		public void setMosaicType(EMosaic newMosaicType) { setParams(null, newMosaicType, null); }
	public void setMinesCount(int newMinesCount    ) { setParams(null, null, newMinesCount); }

	/** ���������� ������� ��������� �������, ����  � � ����������� ����������� ��� (���������� ��� ����� ���������� � ������� "��������� ���") */
	public void setParams(Size newSizeField, EMosaic newMosaicType, Integer newMinesCount, List<Coord> storageCoordMines) {
		try {
			//repositoryMines.Reset();
			if ((getMosaicType() == newMosaicType) &&
				getSizeField().equals(newSizeField) &&
				(getMinesCount() == newMinesCount))
			{
				return;
			}

			EMosaic oldMosaicType = this._mosaicType;
			Size oldMosaicSize = this._size;
			boolean isNewMosaic = (newMosaicType != null) && (newMosaicType != this._mosaicType);
			boolean isNewSizeFld = (newSizeField != null) && !newSizeField.equals(this._size);

			int saveArea = getArea();
			if (isNewSizeFld) {
				setCoordDown(Coord.INCORRECT_COORD); // ����� �� ���� IndexOutOfBoundsException ��� ���������� ������� ���� ����� ������������ ���� �� ����...
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
					// ��������� ������ ������ �� ����������� ��������
					attr.removePropertyChangeListener(cell);

				_matrix.clear();
				_matrix = new ArrayList<BaseCell>(_size.width*_size.height);
				for (int i=0; i < _size.width; i++)
					for (int j=0; j < _size.height; j++) {
						BaseCell cell = CellFactory.createCellInstance(attr, _mosaicType, new Coord(i, j));
						_matrix.add(i*_size.height + j, cell);
						attr.addPropertyChangeListener(cell); // ���������� ����� ������ �� ����������� �������� (��������� a -> ���������� ���������)
					}
	
				for (BaseCell cell: _matrix)
					cell.IdentifyNeighbors(this);
			}

			getMosaicListeners().fireOnChangedCounters(new MosaicEvent.ChangedCountersEvent(Mosaic.this));
			if (isNewMosaic)
				getMosaicListeners().fireOnChangedMosaicType(new MosaicEvent.ChangedMosaicTypeEvent(Mosaic.this, oldMosaicType));
			if (isNewSizeFld)
				getMosaicListeners().fireOnChangedMosaicSize(new MosaicEvent.ChangedMosaicSizeEvent(Mosaic.this, oldMosaicSize));
		} finally {
			if ((storageCoordMines == null) || storageCoordMines.isEmpty())
				getRepositoryMines().clear();
			else
				setRepositoryMines(storageCoordMines);
			//setGameStatus(EGameStatus.eGSEnd);
			GameNew();
		}
	}

	/** ���������� ������� ��������� �������, ���� � � ����������� ����������� ��� */
	public void setParams(Size sizeField, EMosaic mosaicType, Integer minesCount)
	{
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
				OnError("�������� � ���������� ���... :(");
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
		matrixClone.remove(firstClickCell); // �������� �� ������� ������ ����
		matrixClone.removeAll(Arrays.asList(firstClickCell.getNeighbors())); // � �� �������
		int count = 0;
		Random rand = new Random();
		do {
			int len = matrixClone.size();
			if (len == 0) {
				OnError("����..... ����......\r\n�������� ���������� ������ ��� ��� ��������");
				_minesCount = count;
				break;
			}
			int i = rand.nextInt(len);
			BaseCell cellToSetMines = matrixClone.get(i);
			if (cellToSetMines.getState().SetMine()) {
				count++;
				matrixClone.remove(cellToSetMines);
			} else
				OnError("���� ������ ������ ���������������...");
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
	
	/** ������� ��� �������� ������� ��� */
	public int getCountMinesLeft() { return getMinesCount() - getCountFlag(); }
	public int getCountClick()  { return _countClick; }
	public void setCountClick(int clickCount)  { _countClick=clickCount; getMosaicListeners().fireOnChangedCounters(new MosaicEvent.ChangedCountersEvent(this)); }

	/** ������ � �������� ������ */
	public BaseCell getCell(int x, int y) { return _matrix.get(x*_size.height + y); }
	/** ������ � �������� ������ */
	@Override
	public BaseCell getCell(Coord coord) { return getCell(coord.x, coord.y); }

	/** ���������� ������ �� ������� ���� ������ (�� �� ����������� ��� ��������) */
	private Coord _coordDown;
	protected void setCoordDown(Coord coord) { _coordDown = coord; }
	protected Coord getCoordDown() {
		if (_coordDown == null)
			_coordDown = Coord.INCORRECT_COORD;
		return _coordDown;
	}

	/**
	 *<br> ����� ����:
	 *<br>           GameNew()      GameBegin()     GameEnd()      GameNew()
	 *<br>    time      |               |               |             |
	 *<br>  -------->   | eGSCreateGame |               |             |
	 *<br>              |  or eGSReady  |    eGSPlay    |   eGSEnd    |
	 *<br>              \------ 1 -----/ \----- 2 -----/ \---- 3 ----/
	 *<br>
	 *<br> @see ua.ksn.fmg.types.EGameStatus
	 *<br>
	 *<br> PS: ��� ����� gsReady ���� ����� - ��� ���! ���� ������������� ������ ����� ������� �����
	 *<br>     ��� ������ ������ ���� ������, ����� ������ ���� ���������� �� �� ����. �����������
	 *<br>     ��� �� ��������� � ������, ����� ���� ���� ������� ������������� ��� ������� �� �����.
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

		/** ��������� � ����� �� ������� */
		public void fireOnClick(MosaicEvent.ClickEvent e) {
			for (MosaicListener l: getListeners())
				l.OnClick(e);
		}
		/** �����������: �������� ���-�� �������� ����� / ������ / ������ / ... */
		public void fireOnChangedCounters(MosaicEvent.ChangedCountersEvent e) {
			for (MosaicListener l: getListeners())
				l.OnChangedCounters(e);
		}
		/** ��������� �� ��������� ������� ���� (����� ����, ������ ����, ����� ����) */
		public void fireOnChangedGameStatus(MosaicEvent.ChangedGameStatusEvent e) {
			for (MosaicListener l: getListeners())
				l.OnChangedGameStatus(e);
		}
		/** ��������� �� ��������� ������� ������� � ������ */
		public void fireOnChangedArea(MosaicEvent.ChangedAreaEvent e) {
			for (MosaicListener l: getListeners())
				l.OnChangedArea(e);
		}
		/** ��������� �� ��������� ������� ������� � ������ */
		public void fireOnChangedMosaicType(MosaicEvent.ChangedMosaicTypeEvent e) {
			for (MosaicListener l: getListeners())
				l.OnChangedMosaicType(e);
		}
		/** ��������� �� ��������� ������� ������� */
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
	/**  ����������� �� ����������� ��������� ������� */
	public void addMosaicListener(MosaicListener listener) {
		getMosaicListeners().add(listener);
	}
	/**  ���������� �� ����������� ��������� ������� */
	public void removeMosaicListener(MosaicListener listener) {
		getMosaicListeners().remove(listener);
	}

	/** ������������ ������; ���� null - ������������ �� ���� */
	protected abstract void Repaint(BaseCell cell);
	
	/** ������ ����, �.�. ��������� ������ ���� �� ���� */
	protected void GameBegin(Coord firstClick) {
		Repaint(null);

		setGameStatus(EGameStatus.eGSPlay);

		// set mines
		if (!getRepositoryMines().isEmpty()) {
			setPlayInfo(EPlayInfo.ePlayIgnor);
			setMines_LoadRepository(getRepositoryMines());
		} else {
			setMines_random(firstClick);
		}
	}

	/** ��������� ���� */
	private void GameEnd(boolean victory) {
		if (getGameStatus() == EGameStatus.eGSEnd) return;

		{ // ������� ������e���
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
					return; // ������� ������������� ������ - �� �����
			GameEnd(true);
		} else
			if (getMinesCount() == (getCountFlag() + getCountUnknown())) {
				for (BaseCell cell: _matrix)
					if (((cell.getState().getClose() == EClose._Unknown) ||
						( cell.getState().getClose() == EClose._Flag)) &&
						( cell.getState().getOpen() != EOpen._Mine))
						return; // ������� ������������� ������ ��� '?'- �� �����
				GameEnd(true);
			}
		return;
	}

	protected boolean OnLeftButtonDown(Coord coordLDown) {
		setCoordDown(Coord.INCORRECT_COORD);
		if (getGameStatus() == EGameStatus.eGSEnd)
			return false;
		if (coordLDown.equals(Coord.INCORRECT_COORD))
			return false;

		setCoordDown(coordLDown);
		BaseCell cell = getCell(coordLDown);
		if (getGameStatus() == EGameStatus.eGSCreateGame) {
			if (cell.getState().getOpen() != EOpen._Mine) {
				cell.getState().setStatus(EState._Open, null);
				cell.getState().SetMine();
				setMinesCount(getMinesCount()+1);
				getRepositoryMines().add(coordLDown);
			} else {
				cell.Reset();
				setMinesCount(getMinesCount()-1);
				getRepositoryMines().remove(coordLDown);
			}
			Repaint(cell);
		} else {
			LeftDownResult result = cell.LButtonDown();
			if ((result != null) && (result.needRepaint != null))
				for (BaseCell cellToRepaint : result.needRepaint)
					Repaint(cellToRepaint);
		}
		getMosaicListeners().fireOnClick(new MosaicEvent.ClickEvent(this, cell, true, true));
		return true;
	}

	protected boolean OnLeftButtonUp(Coord coordLUp) {
		try {
			if (getGameStatus() == EGameStatus.eGSEnd)
				return false;
			if (getCoordDown().equals(Coord.INCORRECT_COORD))
				return false;
			if (getGameStatus() == EGameStatus.eGSCreateGame)
				return false;
	
	//		System.out.println("OnLeftButtonUp: coordLUp="+coordLUp);
			if ((getGameStatus() == EGameStatus.eGSReady) && coordLUp.equals(getCoordDown()))
			{
				GameBegin(getCoordDown());
			}
			boolean res = false;
			ClickReportContext clickReportContext = new ClickReportContext();
			BaseCell cell = getCell(getCoordDown());
			LeftUpResult result = cell.LButtonUp(coordLUp.equals(getCoordDown()), clickReportContext);
			if (result.needRepaint != null)
				for (BaseCell cellToRepaint : result.needRepaint)
					Repaint(cellToRepaint);
			res = (result.countOpen > 0) || (result.countFlag > 0) || (result.countUnknown > 0); // ���� �� ������� (���� ��������� �� ����) 
			if (res) {
				setCountClick(getCountClick()+1);
				setPlayInfo(EPlayInfo.ePlayerUser);  // ���� �����
				getMosaicListeners().fireOnChangedCounters(new MosaicEvent.ChangedCountersEvent(this));
			}
	
			if (result.endGame) {
				GameEnd(result.victory);
			} else {
				Size sizeField = getSizeField();
				if ((getCountOpen() + getMinesCount()) == sizeField.width*sizeField.height) {
					GameEnd(true);
				} else {
					VerifyFlag();
				}
			}
			getMosaicListeners().fireOnClick(new MosaicEvent.ClickEvent(this, cell, true, false));
			return res;
		} finally {
			setCoordDown(Coord.INCORRECT_COORD);
		}
	}

	protected boolean OnRightButtonDown(Coord coordRDown) {
		if (getGameStatus() == EGameStatus.eGSEnd) {
			GameNew();
			return true;
		}
		if (getGameStatus() == EGameStatus.eGSReady)
			return false;
		if (getGameStatus() == EGameStatus.eGSCreateGame)
			return false;
		if (coordRDown.equals(Coord.INCORRECT_COORD))
			return false;

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
		boolean res = (result.countFlag>0) || (result.countUnknown>0); // ���� �� ������� (���� ��������� �� ����)
		if (res) {
			setCountClick(getCountClick()+1);
			setPlayInfo(EPlayInfo.ePlayerUser); // �� ������ ��� ���� �����
			getMosaicListeners().fireOnChangedCounters(new MosaicEvent.ChangedCountersEvent(this));
		}

		VerifyFlag();
		if (getGameStatus() != EGameStatus.eGSEnd) {
			//...
		}
//		::BitBlt(m_GContext.m_hDCWnd, 0, 0, m_GContext.m_SizeBitmap.cx, m_GContext.m_SizeBitmap.cy,
//				m_GContext.m_hDCDst, 0, 0, SRCCOPY);
		getMosaicListeners().fireOnClick(new MosaicEvent.ClickEvent(this, cell, false, true));
		return res;
	}

	protected boolean OnRightButtonUp(/*Coord coordRUp*/) {
		getMosaicListeners().fireOnClick(new MosaicEvent.ClickEvent(this, null, false, false));
		return false;
	}

	protected boolean RequestToUser_RestoreLastGame() {
		//  need override in child class
		System.out.println("Restore last game?");
		return false;
	}
	/** ������������� � ������ ���� - �������� ��� ������ */
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
		setPlayInfo(EPlayInfo.ePlayerUnknown); // ���� �� ���� ��� ����� ������
	}

	/** ������� ���� ������� - �� ��� ��������� ���� */
	public void GameCreate() {
		GameNew();
		if (getRepositoryMines().isEmpty()) {
			setMinesCount(0);
			setGameStatus(EGameStatus.eGSCreateGame);
			getMosaicListeners().fireOnChangedCounters(new MosaicEvent.ChangedCountersEvent(this));
		}
	}

	/** ������� ����� */
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
	/** ���������� ����� ������� ����� */
	public void setArea(int newArea)  {
		int oldArea = getCellAttr().getArea();
		if (oldArea == Math.max(AREA_MINIMUM, newArea)) return;
		getCellAttr().setArea(Math.max(AREA_MINIMUM, newArea));

		getMosaicListeners().fireOnChangedArea(new MosaicEvent.ChangedAreaEvent(this, oldArea));
	}
	public void setUseUnknown(boolean val) { _useUnknown = val; }
	public boolean getUseUnknown() { return _useUnknown; }

	/** ������������ ���-�� ��� ��� ��������� ������� ���� */
	public int GetMaxMines(Size sizeFld) {
		int iMustFreeCell = GetNeighborNumber()+1;
		int iMaxMines = sizeFld.width*sizeFld.height-iMustFreeCell;
		return Math.max(1, iMaxMines);
	}
	/** ������������ ���-�� ��� ���  �������  ������� ���� */
	public int GetMaxMines() { return GetMaxMines(getSizeField()); }
	/** ������ � �������� ��� ��������� ���������� */
	public Size CalcWindowSize(Size sizeField, int area) { return getCellAttr().CalcOwnerSize(sizeField, area); }
	/** ������ � �������� */
	public Size getWindowSize() { return CalcWindowSize(getSizeField(), getArea()); }
	/** ������ ���������� ������� ��� ������� ������� */
	public int GetNeighborNumber() { return getCellAttr().getNeighborNumber(); }

	/** ������������� ���� ����� gameStatus == gsEnd */
	public boolean isVictory() {
		return (getGameStatus() == EGameStatus.eGSEnd) && (0 == getCountMinesLeft());
	}

	/** Mosaic field: ����� ���� ������� ���� */
	public Mosaic() {
		initialize();
	}
	/** Mosaic field: ����� ���� ������� ���� */
	public Mosaic(Size sizeField, EMosaic mosaicType, int minesCount, int area) {
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
		initialize(new Size(10, 10),
				EMosaic.eMosaicSquare1,//EMosaic.eMosaicPenrousePeriodic1, // 
				15, AREA_MINIMUM*10);
	}
	protected void initialize(Size sizeField, EMosaic mosaicType, int minesCount, int area) {
		setParams(sizeField, mosaicType, minesCount);
		setArea(area); // ...������� �� ���������� ���� ������ ��� ��������� �� ������ Main. ��� ���, �� ���� ��� �������� ��������� ��������.
	}
}
