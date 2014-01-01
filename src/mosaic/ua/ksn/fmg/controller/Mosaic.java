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

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
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
public abstract class Mosaic {

	/** ������ ���� � ������� */
	public Size getSizeField() {
		return getCells().getSize();
	}

	/** ������� ����� ������ */
	protected int _area = -1;
	public static final int AREA_MINIMUM = 230;
	/** ������������ �� ������ �� ���� */
	private boolean _useUnknown = true;

	private BaseCell.BaseAttribute _cellAttr;
	private void setCellAttr(BaseCell.BaseAttribute newValue) {
		if (_cellAttr == null)
			return;
		if (newValue != null)
			throw new IllegalArgumentException("Bad argument - support only null value!");
		this.removePropertyChangeListener(_cellAttr);
		_cellAttr = null;
	}
	public BaseCell.BaseAttribute getCellAttr() {
		if (_cellAttr == null) {
			_cellAttr = CellFactory.createAttributeInstance(getCells().getMosaicType(), getArea());

			this.addPropertyChangeListener(_cellAttr); // ������� ������� �� ������� (��������� area -> ����������� attr.abcdeh)
		}
		return _cellAttr;
	}

	/** ������� ����� ���� ������� */
	protected class MatrixCells implements BaseCell.IMatrixCells {
		/** ������� List &lt; List &lt; BaseCell &gt; &gt; , ��������������(���������) � ���� ������� */
		protected List<BaseCell> matrix = new ArrayList<BaseCell>(0);
		/** ������ ���� � ������� */
		protected Size size = new Size(0, 0);
		/** �� ����� ����� ������� ������� ���� */
		protected EMosaic mosaicType = EMosaic.eMosaicSquare1;
		/** ���-�� ��� �� ���� */
		protected int minesCount = 1;
		/** ���-�� ��� �� ���� �� �������� ����. ������������ ����� ���� ���� �������, �� �� ����� ���� �� �����������. */
		protected int oldMinesCount = 1;

//		public void setSize(Size newSizeField          ) { setParams(newSizeField, null, null ); } 
//		public void setMosaicType(EMosaic newMosaicType) { setParams(null, newMosaicType, null); }
		public void setMinesCount(int newMinesCount    ) { setParams(null, null, newMinesCount); }
		public void setParams(Size newSizeField, EMosaic newMosaicType, Integer newMinesCount) {
			EMosaic oldMosaicType = this.mosaicType;
			boolean newMosaciType = (newMosaicType != this.mosaicType);
			boolean recreateMatrix =
				((newSizeField  != null) && !newSizeField.equals(this.size)) ||
				((newMosaicType != null) && newMosaciType);

			if (newSizeField != null)
				this.size = newSizeField;
			if (newMosaicType != null)
				if (this.mosaicType != newMosaicType) {
					this.mosaicType = newMosaicType;
					setCellAttr(null);
				}
			if (newMinesCount != null) {
				if (newMinesCount == 0)
					this.oldMinesCount = this.minesCount;
				this.minesCount = newMinesCount;
			}
			minesCount = Math.max(1, Math.min(minesCount, GetMaxMines(this.size)));

			if (recreateMatrix) {
				BaseCell.BaseAttribute attr = getCellAttr();

				// ��������� ������ ������ �� ����������� ��������
				for (BaseCell cell: matrix)
					attr.removePropertyChangeListener(cell);

				matrix.clear();
				matrix = new ArrayList<BaseCell>(size.width*size.height);
				for (int i=0; i < size.width; i++)
					for (int j=0; j < size.height; j++) {
						BaseCell cell = CellFactory.createCellInstance(attr, mosaicType, new Coord(i, j));
						matrix.add(i*size.height + j, cell);
						attr.addPropertyChangeListener(cell); // ���������� ����� ������ �� ����������� �������� (��������� a -> ���������� ���������)
					}

				for (BaseCell cell: matrix)
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
					OnError("�������� � ���������� ���... :(");
			}
			// set other CellOpen and set all Caption
			for (BaseCell cell: matrix)
				cell.getState().CalcOpenState();
		}
		/** arrange Mines - set random mines */
		public void setMines_random(Coord firstClickCoord) {
			if (minesCount == 0)
				minesCount = oldMinesCount;
			
			BaseCell firstClickCell = getCell(firstClickCoord);
			List<BaseCell> matrixClone = new ArrayList<BaseCell>(matrix);
			matrixClone.remove(firstClickCell); // �������� �� ������� ������ ����
			matrixClone.removeAll(Arrays.asList(firstClickCell.getNeighbors())); // � �� �������
			int count = 0;
			Random rand = new Random();
			do {
				int len = matrixClone.size();
				if (len == 0) {
					OnError("����..... ����......\r\n�������� ���������� ������ ��� ��� ��������");
					minesCount = count;
					break;
				}
				int i = rand.nextInt(len);
				BaseCell cellToSetMines = matrixClone.get(i);
				if (cellToSetMines.getState().SetMine()) {
					count++;
					matrixClone.remove(cellToSetMines);
				} else
					OnError("���� ������ ������ ���������������...");
			} while (count < minesCount);

			// set other CellOpen and set all Caption
			for (BaseCell cell: matrix)
				cell.getState().CalcOpenState();
		}

		/** ������ ���� � ������� */
		@Override
		public Size getSize() { return new Size(size); } // return clone
		/** ������ ��� ������� */
		public EMosaic getMosaicType() { return mosaicType; }
		/** ���-�� ��� */
		public int getMinesCount() { return minesCount; }

		public List<BaseCell> getAll() {
			return matrix;
		}

		public int getCountOpen() {
			int cnt = 0;
			for (BaseCell cell: matrix)
				if (cell.getState().getStatus() == EState._Open)
					cnt++;
			return cnt;
		}
		public int getCountFlag() {
			int cnt = 0;
			for (BaseCell cell: matrix)
				if ((cell.getState().getStatus() == EState._Close) &&
					(cell.getState().getClose() == EClose._Flag))
						cnt++;
			return cnt;
		}
		public int getCountUnknown() {
			int cnt = 0;
			for (BaseCell cell: matrix)
				if ((cell.getState().getStatus() == EState._Close) &&
					(cell.getState().getClose() == EClose._Unknown))
						cnt++;
			return cnt;
		}
		
		/** ������ � �������� ������ */
		public BaseCell getCell(int x, int y) { return matrix.get(x*size.height + y); }
		/** ������ � �������� ������ */
		@Override
		public BaseCell getCell(Coord coord) { return getCell(coord.x, coord.y); }
	}
	protected MatrixCells _cells;
	/** ������� ����� */
	protected MatrixCells getCells() {
		if (_cells == null)
			_cells = new MatrixCells();
		return _cells;
	}

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

	/** ��� load'a - ���������� ����� � ������ */
	private List<Coord> _repositoryMines;
	private List<Coord> getRepositoryMines() {
		if (_repositoryMines == null)
			_repositoryMines = new ArrayList<Coord>(0);
		return _repositoryMines;
	}
	private void setRepositoryMines(List<Coord> repositoryMines) {
		this._repositoryMines = repositoryMines;
	}

	private PropertyChangeSupport _propertyChanges;
	private PropertyChangeSupport getPropertyChangeSupport() {
		if (_propertyChanges == null)
			_propertyChanges = new PropertyChangeSupport(this);
		return _propertyChanges;
	}
	/**  ����������� �� ����������� ��������� ������� ������� */
	public void addPropertyChangeListener(PropertyChangeListener l) {
		getPropertyChangeSupport().addPropertyChangeListener(l);
	}
	/**  ���������� �� ����������� ��������� ������� ������� */
	public void removePropertyChangeListener(PropertyChangeListener l) {
		getPropertyChangeSupport().removePropertyChangeListener(l);
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
		public void fireOnChangeCounters(MosaicEvent.ChangeCountersEvent e) {
			for (MosaicListener l: getListeners())
				l.OnChangeCounters(e);
		}
		/** ��������� �� ��������� ������� ���� (����� ����, ������ ����, ����� ����) */
		public void fireOnChangeGameStatus(MosaicEvent.ChangeGameStatusEvent e) {
			for (MosaicListener l: getListeners())
				l.OnChangeGameStatus(e);
		}
		/** ��������� � ��������� ������� ������� � ������ */
		public void fireOnChangeArea(MosaicEvent.ChangeAreaEvent e) {
			for (MosaicListener l: getListeners())
				l.OnChangeArea(e);
		}
		/** ��������� � ��������� ������� ������� � ������ */
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
	/**  ����������� �� ����������� ��������� ������� */
	public void addMosaicListener(MosaicListener listener) {
		getMosaicListeners().add(listener);
	}
	/**  ���������� �� ����������� ��������� ������� */
	public void removeMosaicListener(MosaicListener listener) {
		getMosaicListeners().remove(listener);
	}

	/** ������������ ������; ���� null - ������������ �� ���� */
	protected abstract void NeedRepaint(BaseCell cell);
	
	/** ������ ����, �.�. ��������� ������ ���� �� ���� */
	protected void GameBegin(Coord firstClick) {
		NeedRepaint(null);

		setGameStatus(EGameStatus.eGSPlay);

		// set mines
		if (!getRepositoryMines().isEmpty()) {
			setPlayInfo(EPlayInfo.ePlayIgnor);
			getCells().setMines_LoadRepository(getRepositoryMines());
		} else {
			getCells().setMines_random(firstClick);
		}
	}

	/** ��������� ���� */
	private void GameEnd(boolean victory) {
		if (getGameStatus() == EGameStatus.eGSEnd) return;

		{ // ������� ������e���
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
					NeedRepaint(cell);
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
					return; // ������� ������������� ������ - �� �����
			GameEnd(true);
		} else
			if (getCells().getMinesCount() == (getCells().getCountFlag() + getCells().getCountUnknown())) {
				for (BaseCell cell: getCells().getAll())
					if (((cell.getState().getClose() == EClose._Unknown) ||
						( cell.getState().getClose() == EClose._Flag)) &&
						( cell.getState().getOpen() != EOpen._Mine))
						return; // ������� ������������� ������ ��� '?'- �� �����
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
			NeedRepaint(cell);
		} else {
			LeftDownResult result = cell.LButtonDown();
			if ((result != null) && (result.needRepaint != null))
				for (BaseCell cellToRepaint : result.needRepaint)
					NeedRepaint(cellToRepaint);
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
				NeedRepaint(cellToRepaint);
		if ((result.countOpen > 0) || (result.countFlag > 0) || (result.countUnknown > 0)) { // ���� �� ������� (���� ��������� �� ����)
			incrementCountClick();
			setPlayInfo(EPlayInfo.ePlayerUser);  // ���� �����
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
			NeedRepaint(cell);
		if ((result.countFlag>0) || (result.countUnknown>0)) { // ���� �� ������� (���� ��������� �� ����)
			incrementCountClick();
			setPlayInfo(EPlayInfo.ePlayerUser); // �� ������ ��� ���� �����
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

		for (BaseCell cell: getCells().getAll())
			cell.Reset();

		resetCountClick();

		setGameStatus(EGameStatus.eGSReady);
		setPlayInfo(EPlayInfo.ePlayerUnknown); // ���� �� ���� ��� ����� ������
	}

	/** ������� ���� ������� - �� ��� ��������� ���� */
	public void GameCreate() {
		GameNew();
		if (getRepositoryMines().isEmpty()) {
			getCells().setMinesCount(0);
			setGameStatus(EGameStatus.eGSCreateGame);
			getMosaicListeners().fireOnChangeCounters(new MosaicEvent.ChangeCountersEvent(this));
		}
	}

	/** ���������� ������� ��������� �������, ���� � � ����������� ����������� ��� */
	public void setParams(Size sizeField, EMosaic mosaicType, int minesCount) {
		setParams(sizeField, mosaicType, minesCount, null);
	}

	/** ���������� ������� ��������� �������, ����  � � ����������� ����������� ��� (���������� ��� ����� ���������� � ������� "��������� ���") */
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

		setCoordDown(Coord.INCORRECT_COORD); // ����� ������ IndexOutOfBoundsException ��� ���������� ������� ���� ����� ������������ ���� �� ����...

		getCells().setParams(sizeField, mosaicType, minesCount);
		if ((storageCoordMines == null) || storageCoordMines.isEmpty())
			getRepositoryMines().clear();
		else
			setRepositoryMines(storageCoordMines);
		//setGameStatus(EGameStatus.eGSEnd);
		GameNew();
	}

	/** ������ � �������� ������ */
	public BaseCell getCell(int x, int y) { return getCells().getCell(x, y); }
	public BaseCell getCell(Coord coord) { return getCells().getCell(coord); }
//	private void setCell(Coord coord, BaseCell cell) { mosaic.get(coord.x).set(coord.y, cell); }

	/** ���������� ��� */
	public int getMinesCount() { return getCells().getMinesCount(); }
	/** ������ ��� ������� */
	public EMosaic getMosaicType() { return getCells().getMosaicType(); }
	/** ������� ����� */
	public int getArea() {
		if (_area < AREA_MINIMUM)
			setArea(AREA_MINIMUM);
		return _area;
	}
	/** ���������� ����� ������� ����� */
	public void setArea(int newArea)  {
		if (this._area == Math.max(AREA_MINIMUM, newArea)) return;
		int oldArea = _area;
		this._area = Math.max(AREA_MINIMUM, newArea);

		getPropertyChangeSupport().firePropertyChange("Mosaic_area", oldArea, this._area);
		getMosaicListeners().fireOnChangeArea(new MosaicEvent.ChangeAreaEvent(this, oldArea));
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
	/** ������ � �������� */
	public Size CalcWindowSize(Size sizeField, int area) { return getCellAttr().CalcOwnerSize(sizeField, area); }
	/** ������ ���������� ������� ��� ������� ������� */
	public int GetNeighborNumber() { return getCellAttr().getNeighborNumber(); }

	/** ������� ��� �������� ������� ��� */
	public int getCountMinesLeft() { return getCells().getMinesCount() - getCells().getCountFlag(); }
	public int getCountClick()  { return _countClick; }
	private void resetCountClick()     { _countClick=0; getMosaicListeners().fireOnChangeCounters(new MosaicEvent.ChangeCountersEvent(this)); }
	private void incrementCountClick() { _countClick++; getMosaicListeners().fireOnChangeCounters(new MosaicEvent.ChangeCountersEvent(this)); }
	public int getCountOpen()  { return getCells().getCountOpen(); }
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
		setArea(area); // ...������� �� ���������� ���� ������ ��� ��������� �� ������ Main. ��� ���, �� ���� ��� �������� ��������� ��������.
	}
}
