////////////////////////////////////////////////////////////////////////////////
//                               FMG project
//                                      � Sergey Krivulya (KSerg, aka SeregaLBN)
// file name: "BaseCell.java"
//
// ���������� �������� ������ BaseCell
// Copyright (C) 2010-2011 Sergey Krivulya
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
package ua.ksn.fmg.model.mosaics.cell;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.Map;

import ua.ksn.Color;
import ua.ksn.fmg.event.click.ClickReportContext;
import ua.ksn.fmg.event.click.LeftDownResult;
import ua.ksn.fmg.event.click.LeftUpResult;
import ua.ksn.fmg.event.click.RightDownReturn;
import ua.ksn.fmg.model.mosaics.EClose;
import ua.ksn.fmg.model.mosaics.EOpen;
import ua.ksn.fmg.model.mosaics.EState;
import ua.ksn.geom.Coord;
import ua.ksn.geom.Rect;
import ua.ksn.geom.Region;
import ua.ksn.geom.Size;
import ua.ksn.geom.Point;

/** ������� ����� ������-������ */
public abstract class BaseCell implements PropertyChangeListener {
	public static final double SQRT2   = java.lang.Math.sqrt(2.);
	public static final double SQRT3   = java.lang.Math.sqrt(3.);
	public static final double SQRT27  = java.lang.Math.sqrt(27.);
	public static final double SQRT48  = java.lang.Math.sqrt(48.);
	public static final double SQRT147 = java.lang.Math.sqrt(147.);
	public static final double SIN15   = java.lang.Math.sin(java.lang.Math.PI/180.*15.);
	public static final double SIN18   = java.lang.Math.sin(java.lang.Math.PI/180.*18.);
	public static final double SIN36   = java.lang.Math.sin(java.lang.Math.PI/180.*36.);
	public static final double SIN54   = java.lang.Math.sin(java.lang.Math.PI/180.*54.);
	public static final double SIN72   = java.lang.Math.sin(java.lang.Math.PI/180.*72.);
	public static final double SIN75   = java.lang.Math.sin(java.lang.Math.PI/180.*75.);
	public static final double SIN99   = java.lang.Math.sin(java.lang.Math.PI/180.*99.);
	public static final double TAN15   = java.lang.Math.tan(java.lang.Math.PI/180.*15.);
	public static final double TAN45_2 = java.lang.Math.tan(java.lang.Math.PI/180.*45./2);
	public static final double SIN135a = java.lang.Math.sin(java.lang.Math.PI/180.*135.-java.lang.Math.atan(8./3));

	/**
	 * ��������/����������, ����������� ����� ���-�� ��� ������� �� ����������� BaseCell.
	 * <br> (������ ������ � ���������� �������) <br>
	 * ��������������� ������������ BaseCell
	 */
	public static abstract class BaseAttribute {
		/**
		 * �� ��� ���������:
		 *  <li> ��� ���������� BaseCell: ��� ��������� A - ���� ��������� ��� ���������� �����
		 */
		private PropertyChangeSupport propertyChanges = new PropertyChangeSupport(this);
		/**  ����������� �� ����������� ��������� �������� */
		public void addPropertyChangeListener(PropertyChangeListener l) {
			propertyChanges.addPropertyChangeListener(l);
		}
		/**  ���������� �� ����������� ��������� �������� */
		public void removePropertyChangeListener(PropertyChangeListener l) {
			propertyChanges.removePropertyChangeListener(l);
		}

		public BaseAttribute(int area) {
			super();
			setArea(area);
		}

		/** ������� ������/������ */
		private int area;

		/** ������� ������/������ */
		public void setArea(int area) {
			int old = this.area;
			this.area = area;
			propertyChanges.firePropertyChange("Area", old, area);
		}
		/** ������� ������/������ */
		public int getArea() { return area; }

		/** ����������� ������ ��������, ���������� � ������ - ������� ���� ���������� �����������/�����
		 * �� ������ �������� ���������� */
		public abstract double CalcSq(int area, int borderWidth);

		/** ����������� �������� A (������� �������� ������ - ������ ��� ������ ����� �� ������ ������) �� �������� ������� ������ */
		protected abstract double CalcA(int area);

		/** get parent container (owner window) size in pixels */
		public abstract Size CalcOwnerSize(Size sizeField, int area);

		/** ������ ���� �� ������ ����� ��������� �� ������ direction */
		public abstract Size GetDirectionSizeField();
		/** ���-�� direction'��, ������� ����� ������ ��� ������� */
		public int GetDirectionCount() { Size s = GetDirectionSizeField(); return s.width*s.height; }

		/** ���-�� ������� (��������) */
		public abstract int getNeighborNumber();
		/** ���-�� ������� � ������ ���������� �������������� */
		public abstract int getNeighborNumber(int direction);
		/** �� ������� �����/������ ������� ������ (��������) */
		public abstract int getVertexNumber();
		/** �� ������� �����/������ ������� ������ ���������� �������������� */
		public abstract int getVertexNumber(int direction);
		/** ������� ����� ������������ � ����� ����� (� �������) */
		public abstract double getVertexIntersection(); 

		/** ���� ���-�� ������� ������� ����, ������� ����� ������ ��� �������
		 * (����� �-��� BaseCell::getBackgroundFillColor() ��� � �����������)
		 * (�� ������ ������ ������� ������ ���� ��-���������...)
		 */
		public int getMaxBackgroundFillModeValue() {
			return 18;
		}

		/** ��� ��������� ������: ����������� ������ ����, �� �������� ����� ��������� ����, ��� ��� �� �������... */
		public abstract Size sizeIcoField(boolean smallSize);
	}

	private final BaseAttribute attr;
	public BaseAttribute getAttr() {
		return attr;
	}

//    private boolean bPresumeFlag;

//    CellContext cellContext;
	protected Coord coord;
	/** ����������� - '������ ����������' ������ */
	protected int direction;

	/** ��������� � ������ ������� - ������� � ������� ��������� �����������/����� */
	public abstract Rect getRcInner(int borderWidth);
	/** ����� ������������� � ������� ������� ������ ������ */
	public Rect getRcOuter() {
		Rect rcOuter = region.getBounds();
		rcOuter.height++; rcOuter.width++; // ����� ��� repaint'� ��������� � ������� �������
		return rcOuter;
	}

	/** �������� ������ - � �������� �������� this */
	private BaseCell[] neighbors;
	public BaseCell[] getNeighbors() { return neighbors; }
	
	/** ������ ��������� ����� �� ������� ������� ������ */
	protected Region region;

	public class StateCell {
		// { union
		private EState status; // _Open, _Close
		private EOpen   open;   // _Nil, _1, ... _21, _Mine
		private EClose  close;  // _Unknown, _Clear, _Flag
		// } end union
		/** ������? �� ������ � open! - ������ ����� ���� ������, �� ��� �� �������. ����� ������ ��� �-��� ���������� */
		private boolean down;

	    public void setDown(boolean bDown) { this.down = bDown; }
	    public boolean isDown() { return this.down; }
	    public void setStatus(EState status, ClickReportContext clickRepContext) {
	    	if (clickRepContext != null)
	    		if (status == EState._Open)
	    			if (this.open == EOpen._Nil)
	    				clickRepContext.setOpenNil.add(BaseCell.this);
	    		    else
	    		    	clickRepContext.setOpen.add(BaseCell.this);
	    	this.status = status;
	    }
	    public EState getStatus() { return status; }
	    public void CalcOpenState() {
	    	if (this.open == EOpen._Mine) return;
	    	// ���������� � ������� ����� ��� � ���������� ��������
	    	int count = 0;
	    	for (int i=0; i<neighbors.length; i++) {
	    		if (neighbors[i] == null) continue; // ���������� �� �����?
	    		if (neighbors[i].getState().getOpen() == EOpen._Mine) count++;
	    	}
	    	this.open = EOpen.class.getEnumConstants()[count];
	    }
	    public boolean SetMine() {
	    	if (lockMine || (this.open == EOpen._Mine)) return false;
	    	this.open = EOpen._Mine;
	    	return true;
	    }
	    public EOpen getOpen() { return this.open; }
	    public void setClose(EClose close, ClickReportContext clickRepContext) {
	    	if (clickRepContext != null)
	    		if ((     close == EClose._Flag) || // ���� ������������ ������
	    			(this.close == EClose._Flag))   // ���� ������ ������
	    		{
	    			clickRepContext.setFlag.add(BaseCell.this);
	    		}
	    	this.close = close;
	    }
	    public EClose getClose() { return this.close; }

	    private StateCell() { Reset(); }
	    public void Reset() {
	    	status = EState._Close;
	    	open = EOpen._Nil;
	    	close = EClose._Clear;
	    	down = false;
	    }
	}
	private StateCell state;
	/** ��������� ��������� ���� �� ������ ������ */
	private boolean lockMine;

	public void LockNeighborMines() {
		lockMine = true;
		// ��������� ��������� ��� � �������,
		for (int i=0; i<neighbors.length; i++) {
			if (neighbors[i] == null) continue; // ���������� �� �����?
			neighbors[i].lockMine = true;
		}
	}

	public StateCell getState() { return state; }

	protected BaseCell(
			BaseAttribute attr,
			Coord coord,
			int iDirection)
	{
		this.attr = attr;
		this.coord = coord;
		this.direction = iDirection;
		this.region = new Region(attr.getVertexNumber(iDirection));
		this.neighbors = null;

		this.state = new StateCell();
		Reset();
		CalcRegion();
	}

	/**
	 * Coord[] neighborCoord = new Coord[attr.getNeighborNumber()];
	 * <br>... ������� ������ ���������� ���������� �������
	 * @return neighborCoord
	 */
	protected abstract Coord[] GetCoordsNeighbor();

	/** ������� ����� ���� ������� */
	public static interface IMatrixCells {
		/** ������ ���� */
		Size getSize();

		/** ������ � �������� ������ */
		BaseCell getCell(Coord coord);
	}
	/** ��� this ���������� ������-�������, � ��������� ���������� �� ���������
	 * <br> �������� ����� ��������� ������� ���� ��� ���� �������
	 **/
	public final void IdentifyNeighbors(IMatrixCells matrix) {
		// ������� ���������� �������� �����
		Coord[] neighborCoord = GetCoordsNeighbor();
		if (neighborCoord.length != attr.getNeighborNumber())
			throw new RuntimeException("neighborCoord.length != GetNeighborNumber()");

		// �������� ��� ��� �� ������� �� �������
		for (int i=0; i<neighborCoord.length; i++)
			if (neighborCoord[i] != Coord.INCORRECT_COORD)
				if ((neighborCoord[i].x >= matrix.getSize().width) ||
					(neighborCoord[i].y >= matrix.getSize().height) ||
					(neighborCoord[i].x < 0) ||
					(neighborCoord[i].y < 0))
				{
					neighborCoord[i] = Coord.INCORRECT_COORD;
				}
		// �� ����������� ������� ��������� �������� ��������-�����
		neighbors = new BaseCell[attr.getNeighborNumber()];
		for (int i=0; i<neighborCoord.length; i++)
			if (neighborCoord[i] != Coord.INCORRECT_COORD)
				neighbors[i] = matrix.getCell(neighborCoord[i]);
	}

	public Coord getCoord() { return coord; }
	public int getDirection() { return direction; }
	/** ���������� ������ ������ (� ��������) */
	public Point getCenter() { return getRcInner(1).center(); }

	/** ����������� �� ��� �������� ���������� ������ */
	public boolean PointInRegion(Point point) { return region.Contains(point); }

	public Region getRegion() { return region; }

	/** ���������� ���������� ����� �� ������� ������� ������ */
	protected abstract void CalcRegion();

	public void Reset() {
		state.Reset();
		lockMine = false;
	}

	/** Index where border change color */
	public abstract int getShiftPointBorderIndex();


	public LeftDownResult LButtonDown() {
		if (state.getClose()  == EClose._Flag) return null;
		if (state.getStatus() == EState._Close) {
			state.setDown(true);
			LeftDownResult result = new LeftDownResult();
			result.needRepaint.add(this);
			return result;
		}

		LeftDownResult result = null;
		// ������ ��������� ��� ���������� �������
		if ((state.getStatus() == EState._Open) && (state.getOpen() != EOpen._Nil))
			for (int i=0; i<neighbors.length; i++) {
				if (neighbors[i] == null) continue; // ���������� �� �����?
				if ((neighbors[i].state.getStatus() == EState._Open) ||
					(neighbors[i].state.getClose()  == EClose._Flag)) continue;
				neighbors[i].state.setDown(true);
				if (result == null)
					result = new LeftDownResult();
				result.needRepaint.add(neighbors[i]);
			}
		return result;
	}
	public LeftUpResult LButtonUp(boolean isMy, ClickReportContext clickRepContext) {
		LeftUpResult result = new LeftUpResult(0, 0, 0, false, false);

		if (state.getClose() == EClose._Flag) return result;
		// ��������� �� ������� ���������
		if ((state.getStatus() == EState._Open) && (state.getOpen() != EOpen._Nil))
			for (int i=0; i<neighbors.length; i++) {
				if (neighbors[i] == null) continue; // ���������� �� �����?
				if ((neighbors[i].state.getStatus() == EState._Open) ||
					(neighbors[i].state.getClose()  == EClose._Flag)) continue;
				neighbors[i].state.setDown(false);
				result.addToRepaint(neighbors[i]);
			}
		// ������� �������� ������ �� ������� ������
		if (state.getStatus() == EState._Close)
			if (!isMy) {
				state.setDown(false);
				result.addToRepaint(this);
				return result;
			} else {
				result.countUnknown += (state.getClose() == EClose._Unknown) ? -1 : 0;
				result.countOpen++;
				getState().setStatus(EState._Open, clickRepContext);
				getState().setDown(true);
				result.addToRepaint(this);
			}

		// ! � ���� ����� ������ ��� �������
		// ����������� ���-�� ������������� ������ ������ � �� �������� �����
		int countFlags = 0;
		int countClear = 0;
		if (state.getOpen() != EOpen._Nil)
			for (int i=0; i<neighbors.length; i++) {
				if (neighbors[i] == null) continue; // ���������� �� �����?
				if (neighbors[i].state.getStatus() == EState._Open) continue;
				if (neighbors[i].state.getClose()  == EClose._Flag)
					countFlags++;
				else countClear++;
			}
		// ���������� ���������� �����
		if ((state.getOpen() != EOpen._Nil) && ((countFlags+countClear) == state.getOpen().ordinal()))
			for (int i=0; i<neighbors.length; i++) {
				if (neighbors[i] == null) continue; // ���������� �� �����?
				if ((neighbors[i].state.getStatus() == EState._Open) ||
					(neighbors[i].state.getClose()  == EClose._Flag)) continue;
				result.countUnknown += (neighbors[i].state.getClose() == EClose._Unknown) ? -1 : 0;
				result.countFlag++;
				neighbors[i].state.setClose(EClose._Flag, clickRepContext);
				result.addToRepaint(neighbors[i]);
			}
		if (!isMy) return result;
		// ������� ����������
		if ((countFlags+result.countFlag) == state.getOpen().ordinal())
			for (int i=0; i<neighbors.length; i++) {
				if (neighbors[i] == null) continue; // ���������� �� �����?
				if ((neighbors[i].state.getStatus() == EState._Open) ||
					(neighbors[i].state.getClose()  == EClose._Flag)) continue;
				result.countUnknown += (neighbors[i].state.getClose() == EClose._Unknown) ? -1 : 0;
				result.countOpen++;
				neighbors[i].state.setDown(true);
				neighbors[i].state.setStatus(EState._Open, clickRepContext);
				result.addToRepaint(neighbors[i]);
				if (neighbors[i].state.getOpen() == EOpen._Nil) {
					LeftUpResult result2 = neighbors[i].LButtonUp(true, clickRepContext);
					result.countFlag    += result2.countFlag;
					result.countOpen    += result2.countOpen;
					result.countUnknown += result2.countUnknown;
					if (result.endGame) {
						result.endGame = result2.endGame;
						result.victory = result2.victory;
					}
					if (result2.needRepaint != null)
						for (BaseCell cellToRepaint : result2.needRepaint)
							result.addToRepaint(cellToRepaint);
				}
				if (neighbors[i].state.getOpen() == EOpen._Mine) {
					result.endGame = true;
					result.victory = false;
					return result;
				}
			}
		if (state.getOpen() == EOpen._Mine) {
			result.endGame = true;
			result.victory = false;
		}
		return result;
	}
	public RightDownReturn RButtonDown(EClose close, ClickReportContext clickRepContext) {
		RightDownReturn result = new RightDownReturn(0, 0);

		if ((state.getStatus() == EState._Open) || state.isDown()) return result;
		switch (state.getClose()) {
		case _Clear:
			switch (close) {
			case _Flag:    result.countFlag    = +1;  break;
			case _Unknown: result.countUnknown = +1;
			default: break;
			}
			if (state.getClose() != close)
				state.setClose(close, clickRepContext);
			break;
		case _Flag:
			switch (close) {
			case _Unknown: result.countUnknown = +1;
			case _Clear:   result.countFlag    = -1;
			default: break;
			}
			if (state.getClose() != close)
				state.setClose(close, clickRepContext);
			break;
		case _Unknown:
			switch (close) {
			case _Flag:    result.countFlag    = +1;
			case _Clear:   result.countUnknown = -1;
			default: break;
			}
			if (state.getClose() != close)
				state.setClose(close, clickRepContext);
		}
		result.needRepaint = true;
		return result;
	}

	/** <ul> ������� ���� ������� ����� � ����������� ��
	 * <li> ������ ������� ���� �����
	 * <li> ���������� ������
	 * <li> ����������� ������
	 * <li> ... - ��� ��������� �������� �����
	 */
	public Color getBackgroundFillColor(int fillMode, Color defaultColor, Map<Integer, Color> repositoryColor) {
		switch (fillMode) {
		default:
			System.err.println(getClass().getSimpleName()+".getBackgroundFillColor: fillMode="+fillMode+":  ������ �������� ��������� ��� ����� ������!");
			//break;// !!! ��� break'�
		case 0:
			Color clr = defaultColor;
			{ // ��� Down � �������� ��������� ����� ��� ���� � ����-���� ������...
				float perc;
				boolean failGame = false;

				if (getState().getStatus() == EState._Close)
					if (getState().isDown())
						perc = .15f;
					else
						perc = 0.f;
				else {
					failGame = (getState().getOpen() == EOpen._Mine) && getState().isDown();
					perc = getState().isDown() ? .25f : 0.f;
				}
	
				if (failGame)
					return Color.Red;

				byte _r = (byte) (clr.getR() - clr.getR() * perc);
				byte _g = (byte) (clr.getG() - clr.getG() * perc);
				byte _b = (byte) (clr.getB() - clr.getB() * perc);
				return new Color(_r,_g,_b);
			}
		case 1:
			return repositoryColor.get(getDirection());
		case 2:
			{
				// ���������� ������ i-��� ������ c ����� div
				final int i = 2;
				final int div = 5;
				int tmp1 = getCoord().x % div;
				int tmp2 = (getCoord().y-tmp1) % div;
				return repositoryColor.get((((tmp1 + tmp2) % div) == i) ? 0 : 1 );
			}
		case 3:
			{
				// �������
				final int i = 3;
				final int div = 4;
				int tmp1 = getCoord().x % div;
				int tmp2 = (getCoord().y+tmp1) % div;
				return repositoryColor.get((((tmp1 + tmp2) % div) == i) ? 0 : 1 );
			}
		case 4:
			{
				// ��� ����
				final int i = 3;
				final int div = 5;
				int tmp1 = getCoord().x % div;
				int tmp2 = (getCoord().y+tmp1) % div;
				return repositoryColor.get((((tmp1 + tmp2) % div) == i) ? 0 : 1 );
			}
		case 5:
			{
				// �����
				final int div = 15;
				int tmp1 = getCoord().x % div;
				int tmp2 = (getCoord().y+tmp1) % div;
				return repositoryColor.get((tmp1 + tmp2) % div);
			}
		case 6:
			{
				final int div = 4;
				return repositoryColor.get(((getCoord().x%div + getCoord().y%div) == div) ? 0 : 1);
			}
		case 7: case 8: case 9:
			return repositoryColor.get(getCoord().x % (-5+fillMode));
		case 10: case 11: case 12:
			return repositoryColor.get(getCoord().y % (-8+fillMode));
		case 13: case 14: case 15:
		case 16: case 17: case 18:
			return repositoryColor.get(getCoord().x % (-fillMode) - fillMode + getCoord().y % (+fillMode));
		}
	}

//    public void    setPresumeFlag(boolean bValue) { bPresumeFlag = bValue; }
//    public boolean getPresumeFlag()               { return bPresumeFlag; }

	@Override
	public void propertyChange(PropertyChangeEvent evt) {
		if ("Area".equals(evt.getPropertyName()))
		{
			CalcRegion();

//			region.invalidate(); // ��������� region.bounds, ��� ���� ����� � ���� getRcOuter() ���
//			// �������� AWT'����� region.getBounds(). �����, ����� ��������� �������,
//			// ����� getRcOuter() (����� ����� region.getBounds() ) ����� ����������
//			// ������������� ��������, � ��� ��������� - ����� ��� ����������� Mosaic (����� ��������� �������)...
		}
	}
}
