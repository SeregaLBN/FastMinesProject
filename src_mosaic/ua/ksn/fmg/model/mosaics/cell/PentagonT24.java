////////////////////////////////////////////////////////////////////////////////
//                               FastMines project
//                                      © Sergey Krivulya (KSerg, aka SeregaLBN)
// file name: "PentagonT24.java"
//
// –еализаци€ класса PentagonT24 - равносторонний 5-ти угольник, тип є2 и є4
// Copyright (C) 2002-2011 Sergey Krivulya
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

import ua.ksn.geom.Coord;
import ua.ksn.geom.Rect;
import ua.ksn.geom.Size;

/**
 * ѕ€тиугольник. “ип є2 и є4 - равносторонний
 * @see BaseCell
 **/
public class PentagonT24 extends BaseCell {
	public static class AttrPentagonT24 extends BaseAttribute {
		public AttrPentagonT24(int area) {
			super(area);
		}

		@Override
		public Size CalcOwnerSize(Size sizeField, int area) {
			double a = CalcA(area);
			double b = CalcB(area);
			Size result = new Size(
					(int)(b + sizeField.width * a),
					(int)(b + sizeField.height * a));

			if (sizeField.height == 1)
				result.width -= CalcC(area);

				return result;
		}
	
		@Override
		public int getNeighborNumber() { return 7; }
		@Override
		public int getNeighborNumber(int direction) { return 7; }
		@Override
		public int getVertexNumber() { return 5; }
		@Override
		public int getVertexNumber(int direction) { return 5; }
		@Override
		public double getVertexIntersection() { return 3.4; } // (3+3+3+4+4)/5.
		@Override
		public Size GetDirectionSizeField() { return new Size(2, 2); }
		@Override
		protected double CalcA(int area) { return Math.sqrt(area); }
		protected double CalcB(int area) { return CalcA(area)*6/11; }
		protected double CalcC(int area) { return CalcB(area)/2; }
		@Override
		public double CalcSq(int area, int borderWidth) {
			double w = borderWidth/2.;
			return CalcA(area)*8/11-(w+w/SIN135a) / SQRT2;
		}

		@Override
		public Size sizeIcoField(boolean smallSize) {
			return new Size(2, smallSize ? 2 : 3);
		}
	}

	public PentagonT24(AttrPentagonT24 attr, Coord coord) {
		super(attr, coord,
		           ((coord.y&1)<<1) + (coord.x&1) // 0..3
				);
	}

	@Override
	public AttrPentagonT24 getAttr() {
		return (AttrPentagonT24) super.getAttr();
	}

	@Override
	protected Coord[] GetCoordsNeighbor() {
		Coord[] neighborCoord = new Coord[getAttr().getNeighborNumber()];

		// определ€ю координаты соседей
		switch (direction) {
		case 0:
			neighborCoord[0] = new Coord(coord.x-1, coord.y-1);
			neighborCoord[1] = new Coord(coord.x  , coord.y-1);
			neighborCoord[2] = new Coord(coord.x-1, coord.y  );
			neighborCoord[3] = new Coord(coord.x+1, coord.y  );
			neighborCoord[4] = new Coord(coord.x-1, coord.y+1);
			neighborCoord[5] = new Coord(coord.x  , coord.y+1);
			neighborCoord[6] = new Coord(coord.x+1, coord.y+1);
			break;
		case 1:
			neighborCoord[0] = new Coord(coord.x-1, coord.y-1);
			neighborCoord[1] = new Coord(coord.x  , coord.y-1);
			neighborCoord[2] = new Coord(coord.x+1, coord.y-1);
			neighborCoord[3] = new Coord(coord.x-1, coord.y  );
			neighborCoord[4] = new Coord(coord.x+1, coord.y  );
			neighborCoord[5] = new Coord(coord.x-1, coord.y+1);
			neighborCoord[6] = new Coord(coord.x  , coord.y+1);
			break;
		case 2:
			neighborCoord[0] = new Coord(coord.x  , coord.y-1);
			neighborCoord[1] = new Coord(coord.x+1, coord.y-1);
			neighborCoord[2] = new Coord(coord.x-1, coord.y  );
			neighborCoord[3] = new Coord(coord.x+1, coord.y  );
			neighborCoord[4] = new Coord(coord.x-1, coord.y+1);
			neighborCoord[5] = new Coord(coord.x  , coord.y+1);
			neighborCoord[6] = new Coord(coord.x+1, coord.y+1);
			break;
		case 3:
			neighborCoord[0] = new Coord(coord.x-1, coord.y-1);
			neighborCoord[1] = new Coord(coord.x  , coord.y-1);
			neighborCoord[2] = new Coord(coord.x+1, coord.y-1);
			neighborCoord[3] = new Coord(coord.x-1, coord.y  );
			neighborCoord[4] = new Coord(coord.x+1, coord.y  );
			neighborCoord[5] = new Coord(coord.x  , coord.y+1);
			neighborCoord[6] = new Coord(coord.x+1, coord.y+1);
			break;
		}

		return neighborCoord;
	}

	@Override
	protected void CalcRegion() {
		AttrPentagonT24 attr = getAttr();
		int area = attr.getArea();
		double a = attr.CalcA(area);
		double b = attr.CalcB(area);
		double c = attr.CalcC(area);

		// определение координат точек фигуры
		double oX = a*((coord.x>>1)<<1); // offset X
		double oY = a*((coord.y>>1)<<1); // offset Y
		switch (direction) {
		case 0:
			region.getPoint(0).x = (int) (oX +       a); region.getPoint(0).y = (int) (oY + b      );
			region.getPoint(1).x = (int) (oX + c +   a); region.getPoint(1).y = (int) (oY + c +   a);
			region.getPoint(2).x = (int) (oX + b      ); region.getPoint(2).y = (int) (oY + b +   a);
			region.getPoint(3).x = (int) (oX          ); region.getPoint(3).y = (int) (oY +       a);
			region.getPoint(4).x = (int) (oX + c      ); region.getPoint(4).y = (int) (oY + c      );
			break;
		case 1:
			region.getPoint(0).x = (int) (oX + c + 2*a); region.getPoint(0).y = (int) (oY + c      );
			region.getPoint(1).x = (int) (oX +     2*a); region.getPoint(1).y = (int) (oY +       a);
			region.getPoint(2).x = (int) (oX + c +   a); region.getPoint(2).y = (int) (oY + c +   a);
			region.getPoint(3).x = (int) (oX +       a); region.getPoint(3).y = (int) (oY + b      );
			region.getPoint(4).x = (int) (oX + b +   a); region.getPoint(4).y = (int) (oY          );
			break;
		case 2:
			region.getPoint(0).x = (int) (oX + c +   a); region.getPoint(0).y = (int) (oY + c +   a);
			region.getPoint(1).x = (int) (oX + b +   a); region.getPoint(1).y = (int) (oY +     2*a);
			region.getPoint(2).x = (int) (oX +       a); region.getPoint(2).y = (int) (oY + b + 2*a);
			region.getPoint(3).x = (int) (oX + c      ); region.getPoint(3).y = (int) (oY + c + 2*a);
			region.getPoint(4).x = (int) (oX + b      ); region.getPoint(4).y = (int) (oY + b +   a);
			break;
		case 3:
			region.getPoint(0).x = (int) (oX +     2*a); region.getPoint(0).y = (int) (oY +       a);
			region.getPoint(1).x = (int) (oX + b + 2*a); region.getPoint(1).y = (int) (oY + b +   a);
			region.getPoint(2).x = (int) (oX + c + 2*a); region.getPoint(2).y = (int) (oY + c + 2*a);
			region.getPoint(3).x = (int) (oX + b +   a); region.getPoint(3).y = (int) (oY +     2*a);
			region.getPoint(4).x = (int) (oX + c +   a); region.getPoint(4).y = (int) (oY + c +   a);
			break;
		}
	}

	@Override
	public Rect getRcInner(int borderWidth) {
		AttrPentagonT24 attr = getAttr();
		double sq = attr.CalcSq(attr.getArea(), borderWidth);
		double w = borderWidth/2.;
		double w2 = w/SQRT2;

		Rect square = new Rect();
		switch (direction) {
		case 0:
			square.x = (int) (region.getPoint(4).x+w2);
			square.y = (int) (region.getPoint(1).y-w2 - sq);
			break;
		case 1:
			square.x = (int) (region.getPoint(2).x+w2);
			square.y = (int) (region.getPoint(0).y+w2);
			break;
		case 2:
			square.x = (int) (region.getPoint(0).x-w2 - sq);
			square.y = (int) (region.getPoint(3).y-w2 - sq);
			break;
		case 3:
			square.x = (int) (region.getPoint(2).x-w2 - sq);
			square.y = (int) (region.getPoint(4).y+w2);
			break;
		}
		square.width = (int)sq;
		square.height = (int)sq;
		return square;
	}

	@Override
	public int getShiftPointBorderIndex() {
		switch (direction) {
		case 0: case 1:
			return 2;
		}
		return 3;
	}
}
