////////////////////////////////////////////////////////////////////////////////
//                               FastMines project
//                                      © Sergey Krivulya (KSerg, aka SeregaLBN)
// file name: "SqTrHex.java"
//
// Реализация класса SqTrHex - мозаика из 6Square 4Triangle 2Hexagon
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
import ua.ksn.geom.PointDouble;
import ua.ksn.geom.Rect;
import ua.ksn.geom.Size;

/**
 * Комбинация. мозаика из 6Square 4Triangle 2Hexagon
 * @see BaseCell
 **/
public class SqTrHex extends BaseCell {
	public static class AttrSqTrHex extends BaseAttribute {
		public AttrSqTrHex(int area) {
			super(area);
		}

		@Override
		public Size CalcOwnerSize(Size sizeField, int area) {
			double a = CalcA(area);
			double h = CalcH(area);
			Size result = new Size(
					(int)(a/2+h + a/2*((sizeField.width+2)/3) +
					              h * ((sizeField.width+1)/3) +
					     (a/2+h)    * ((sizeField.width+0)/3)),
					(int)(a/2   + h * ((sizeField.height+1)/2)+
					      a*3/2*      ((sizeField.height+0)/2)));

			if (sizeField.height < 4) {
				int x = sizeField.width % 3;
				switch (sizeField.height) {
				case 1:
					switch (x) { case 0: result.width -= h; case 1: case 2: result.width -= h; }
					break;
				case 2: case 3:
					switch (x) { case 0: case 1: result.width -= h; }
					break;
				}
			}
			if (sizeField.width < 3) {
				int y = sizeField.height % 4;
				switch (sizeField.width) {
				case 1:
					switch (y) { case 0: result.height -= a*1.5; break; case 2: case 3: result.height -= a/2; }
					break;
				case 2:
					if (y == 0) result.height -= a/2;
					break;
				}
			}

			return result;
		}
	
		@Override
		public int getNeighborNumber() { return 12; }
		@Override
		public int getNeighborNumber(int direction) {
			switch (direction) {
			case  0: case  2: case  6: case  7: return 6;
			case  1: case  3: case  5: case  8: case  9: case 10: return 8;
			case  4: case 11: return 12;
			default:
				throw new IllegalArgumentException("Invalid value direction="+direction);
		 	}
		}
		@Override
		public int getVertexNumber() { return 6; }
		@Override
		public int getVertexNumber(int direction) {
			switch (direction) {
			case  0: case  2: case  6: case  7: return 3;
			case  1: case  3: case  5: case  8: case  9: case 10: return 4;
			case  4: case 11: return 6;
			default:
				throw new IllegalArgumentException("Invalid value direction="+direction);
		 	}
		}
		@Override
		public double getVertexIntersection() { return 4.; }
		@Override
		public Size GetDirectionSizeField() { return new Size(3, 4); }
		@Override
		protected double CalcA(int area) { return Math.sqrt(area/(0.5+1/SQRT3)); }
		protected double CalcH(int area) { return CalcA(area)*SQRT3/2; }
		@Override
		public double CalcSq(int area, int borderWidth) {
			double w = borderWidth/2.;
			return (CalcA(area)*SQRT3 - w*6) / (2+SQRT3); 
		}

		@Override
		public Size sizeIcoField(boolean smallSize) {
			return new Size(4, smallSize ? 4 : 5);
		}
	}

	public SqTrHex(AttrSqTrHex attr, Coord coord) {
		super(attr, coord,
				(coord.y&3)*3+(coord.x%3) // 0..11
			);
	}

	@Override
	public AttrSqTrHex getAttr() {
		return (AttrSqTrHex) super.getAttr();
	}

	@Override
	protected Coord[] GetCoordsNeighbor() {
		Coord[] neighborCoord = new Coord[getAttr().getNeighborNumber()];

		// определяю координаты соседей
		switch (direction) {
		case 0:
			neighborCoord[ 0] = new Coord(coord.x-1, coord.y-1);
			neighborCoord[ 1] = new Coord(coord.x+1, coord.y  );
			neighborCoord[ 2] = new Coord(coord.x-2, coord.y+1);
			neighborCoord[ 3] = new Coord(coord.x-1, coord.y+1);
			neighborCoord[ 4] = new Coord(coord.x  , coord.y+1);
			neighborCoord[ 5] = new Coord(coord.x+1, coord.y+1);
			neighborCoord[ 6] =
			neighborCoord[ 7] =
			neighborCoord[ 8] =
			neighborCoord[ 9] =
			neighborCoord[10] =
			neighborCoord[11] = Coord.INCORRECT_COORD;
			break;
		case 1:
			neighborCoord[ 0] = new Coord(coord.x-2, coord.y-1);
			neighborCoord[ 1] = new Coord(coord.x  , coord.y-1);
			neighborCoord[ 2] = new Coord(coord.x-1, coord.y  );
			neighborCoord[ 3] = new Coord(coord.x+1, coord.y  );
			neighborCoord[ 4] = new Coord(coord.x-2, coord.y+1);
			neighborCoord[ 5] = new Coord(coord.x-1, coord.y+1);
			neighborCoord[ 6] = new Coord(coord.x  , coord.y+1);
			neighborCoord[ 7] = new Coord(coord.x+1, coord.y+1);
			neighborCoord[ 8] =
			neighborCoord[ 9] =
			neighborCoord[10] =
			neighborCoord[11] = Coord.INCORRECT_COORD;
			break;
		case 2:
			neighborCoord[ 0] = new Coord(coord.x-3, coord.y-1);
			neighborCoord[ 1] = new Coord(coord.x-1, coord.y-1);
			neighborCoord[ 2] = new Coord(coord.x  , coord.y-1);
			neighborCoord[ 3] = new Coord(coord.x-1, coord.y  );
			neighborCoord[ 4] = new Coord(coord.x-1, coord.y+1);
			neighborCoord[ 5] = new Coord(coord.x  , coord.y+1);
			neighborCoord[ 6] =
			neighborCoord[ 7] =
			neighborCoord[ 8] =
			neighborCoord[ 9] =
			neighborCoord[10] =
			neighborCoord[11] = Coord.INCORRECT_COORD;
			break;
		case 3:
			neighborCoord[ 0] = new Coord(coord.x  , coord.y-1);
			neighborCoord[ 1] = new Coord(coord.x+1, coord.y-1);
			neighborCoord[ 2] = new Coord(coord.x-2, coord.y  );
			neighborCoord[ 3] = new Coord(coord.x-1, coord.y  );
			neighborCoord[ 4] = new Coord(coord.x+1, coord.y  );
			neighborCoord[ 5] = new Coord(coord.x-1, coord.y+1);
			neighborCoord[ 6] = new Coord(coord.x  , coord.y+1);
			neighborCoord[ 7] = new Coord(coord.x  , coord.y+2);
			neighborCoord[ 8] =
			neighborCoord[ 9] =
			neighborCoord[10] =
			neighborCoord[11] = Coord.INCORRECT_COORD;
			break;
		case 4:
			neighborCoord[ 0] = new Coord(coord.x-1, coord.y-1);
			neighborCoord[ 1] = new Coord(coord.x  , coord.y-1);
			neighborCoord[ 2] = new Coord(coord.x+1, coord.y-1);
			neighborCoord[ 3] = new Coord(coord.x+2, coord.y-1);
			neighborCoord[ 4] = new Coord(coord.x-1, coord.y  );
			neighborCoord[ 5] = new Coord(coord.x+1, coord.y  );
			neighborCoord[ 6] = new Coord(coord.x+2, coord.y  );
			neighborCoord[ 7] = new Coord(coord.x-1, coord.y+1);
			neighborCoord[ 8] = new Coord(coord.x  , coord.y+1);
			neighborCoord[ 9] = new Coord(coord.x+1, coord.y+1);
			neighborCoord[10] = new Coord(coord.x+2, coord.y+1);
			neighborCoord[11] = new Coord(coord.x-1, coord.y+2);
			break;
		case 5:
			neighborCoord[ 0] = new Coord(coord.x-1, coord.y-2);
			neighborCoord[ 1] = new Coord(coord.x  , coord.y-2);
			neighborCoord[ 2] = new Coord(coord.x-1, coord.y-1);
			neighborCoord[ 3] = new Coord(coord.x  , coord.y-1);
			neighborCoord[ 4] = new Coord(coord.x+1, coord.y-1);
			neighborCoord[ 5] = new Coord(coord.x+2, coord.y-1);
			neighborCoord[ 6] = new Coord(coord.x-1, coord.y  );
			neighborCoord[ 7] = new Coord(coord.x+1, coord.y  );
			neighborCoord[ 8] =
			neighborCoord[ 9] =
			neighborCoord[10] =
			neighborCoord[11] = Coord.INCORRECT_COORD;
			break;
		case 6:
			neighborCoord[ 0] = new Coord(coord.x-2, coord.y-1);
			neighborCoord[ 1] = new Coord(coord.x  , coord.y-1);
			neighborCoord[ 2] = new Coord(coord.x+1, coord.y-1);
			neighborCoord[ 3] = new Coord(coord.x-1, coord.y  );
			neighborCoord[ 4] = new Coord(coord.x-1, coord.y+1);
			neighborCoord[ 5] = new Coord(coord.x  , coord.y+1);
			neighborCoord[ 6] =
			neighborCoord[ 7] =
			neighborCoord[ 8] =
			neighborCoord[ 9] =
			neighborCoord[10] =
			neighborCoord[11] = Coord.INCORRECT_COORD;
			break;
		case 7:
			neighborCoord[ 0] = new Coord(coord.x  , coord.y-1);
			neighborCoord[ 1] = new Coord(coord.x+1, coord.y  );
			neighborCoord[ 2] = new Coord(coord.x-2, coord.y+1);
			neighborCoord[ 3] = new Coord(coord.x-1, coord.y+1);
			neighborCoord[ 4] = new Coord(coord.x  , coord.y+1);
			neighborCoord[ 5] = new Coord(coord.x+1, coord.y+1);
			neighborCoord[ 6] =
			neighborCoord[ 7] =
			neighborCoord[ 8] =
			neighborCoord[ 9] =
			neighborCoord[10] =
			neighborCoord[11] = Coord.INCORRECT_COORD;
			break;
		case 8:
			neighborCoord[ 0] = new Coord(coord.x-1, coord.y-1);
			neighborCoord[ 1] = new Coord(coord.x+1, coord.y-1);
			neighborCoord[ 2] = new Coord(coord.x-1, coord.y  );
			neighborCoord[ 3] = new Coord(coord.x+1, coord.y  );
			neighborCoord[ 4] = new Coord(coord.x-2, coord.y+1);
			neighborCoord[ 5] = new Coord(coord.x-1, coord.y+1);
			neighborCoord[ 6] = new Coord(coord.x  , coord.y+1);
			neighborCoord[ 7] = new Coord(coord.x+1, coord.y+1);
			neighborCoord[ 8] =
			neighborCoord[ 9] =
			neighborCoord[10] =
			neighborCoord[11] = Coord.INCORRECT_COORD;
			break;
		case 9:
			neighborCoord[ 0] = new Coord(coord.x  , coord.y-2);
			neighborCoord[ 1] = new Coord(coord.x+1, coord.y-2);
			neighborCoord[ 2] = new Coord(coord.x-1, coord.y-1);
			neighborCoord[ 3] = new Coord(coord.x  , coord.y-1);
			neighborCoord[ 4] = new Coord(coord.x+1, coord.y-1);
			neighborCoord[ 5] = new Coord(coord.x+2, coord.y-1);
			neighborCoord[ 6] = new Coord(coord.x-1, coord.y  );
			neighborCoord[ 7] = new Coord(coord.x+1, coord.y  );
			neighborCoord[ 8] =
			neighborCoord[ 9] =
			neighborCoord[10] =
			neighborCoord[11] = Coord.INCORRECT_COORD;
			break;
		case 10:
			neighborCoord[ 0] = new Coord(coord.x  , coord.y-1);
			neighborCoord[ 1] = new Coord(coord.x+1, coord.y-1);
			neighborCoord[ 2] = new Coord(coord.x-2, coord.y  );
			neighborCoord[ 3] = new Coord(coord.x-1, coord.y  );
			neighborCoord[ 4] = new Coord(coord.x+1, coord.y  );
			neighborCoord[ 5] = new Coord(coord.x  , coord.y+1);
			neighborCoord[ 6] = new Coord(coord.x+1, coord.y+1);
			neighborCoord[ 7] = new Coord(coord.x+1, coord.y+2);
			neighborCoord[ 8] =
			neighborCoord[ 9] =
			neighborCoord[10] =
			neighborCoord[11] = Coord.INCORRECT_COORD;
			break;
		case 11:
			neighborCoord[ 0] = new Coord(coord.x-1, coord.y-1);
			neighborCoord[ 1] = new Coord(coord.x  , coord.y-1);
			neighborCoord[ 2] = new Coord(coord.x+1, coord.y-1);
			neighborCoord[ 3] = new Coord(coord.x+2, coord.y-1);
			neighborCoord[ 4] = new Coord(coord.x-1, coord.y  );
			neighborCoord[ 5] = new Coord(coord.x+1, coord.y  );
			neighborCoord[ 6] = new Coord(coord.x+2, coord.y  );
			neighborCoord[ 7] = new Coord(coord.x  , coord.y+1);
			neighborCoord[ 8] = new Coord(coord.x+1, coord.y+1);
			neighborCoord[ 9] = new Coord(coord.x+2, coord.y+1);
			neighborCoord[10] = new Coord(coord.x+3, coord.y+1);
			neighborCoord[11] = new Coord(coord.x  , coord.y+2);
			break;
	 	}

		return neighborCoord;
	}

	private PointDouble getOffest() {
		AttrSqTrHex attr = getAttr();
		int area = attr.getArea();
		double a = attr.CalcA(area);
		double h = attr.CalcH(area);

		return new PointDouble(
				(h*2+a  )*(coord.x/3) + a+h,
				(h*2+a*3)*(coord.y/4) + a*2+h);
	}
	
	@Override
	protected void CalcRegion() {
		AttrSqTrHex attr = getAttr();
		int area = attr.getArea();
		double a = attr.CalcA(area);
		double b = a/2;
		double h = attr.CalcH(area);

		PointDouble o = getOffest();
		switch (direction) {
		case 0:
			region.getPoint(0).x = (int)(o.x - b-h  );  region.getPoint(0).y = (int)(o.y - a-b-h);
			region.getPoint(1).x = (int)(o.x - h    );  region.getPoint(1).y = (int)(o.y - a-b  );
			region.getPoint(2).x = (int)(o.x - h-a  );  region.getPoint(2).y = (int)(o.y - a-b  );
			break;
		case 1:
			region.getPoint(0).x = (int)(o.x - b    );  region.getPoint(0).y = (int)(o.y - a-a-h);
			region.getPoint(1).x = (int)(o.x        );  region.getPoint(1).y = (int)(o.y - a-a  );
			region.getPoint(2).x = (int)(o.x - h    );  region.getPoint(2).y = (int)(o.y - a-b  );
			region.getPoint(3).x = (int)(o.x - b-h  );  region.getPoint(3).y = (int)(o.y - a-b-h);
			break;
		case 2:
			region.getPoint(0).x = (int)(o.x + b    );  region.getPoint(0).y = (int)(o.y - a-a-h);
			region.getPoint(1).x = (int)(o.x        );  region.getPoint(1).y = (int)(o.y - a-a  );
			region.getPoint(2).x = (int)(o.x - b    );  region.getPoint(2).y = (int)(o.y - a-a-h);
			break;
		case 3:
			region.getPoint(0).x = (int)(o.x - h    );  region.getPoint(0).y = (int)(o.y - a-b  );
			region.getPoint(1).x = (int)(o.x - h    );  region.getPoint(1).y = (int)(o.y - b    );
			region.getPoint(2).x = (int)(o.x - a-h  );  region.getPoint(2).y = (int)(o.y - b    );
			region.getPoint(3).x = (int)(o.x - a-h  );  region.getPoint(3).y = (int)(o.y - a-b  );
			break;
		case 4:
			region.getPoint(0).x = (int)(o.x        );  region.getPoint(0).y = (int)(o.y - a-a  );
			region.getPoint(1).x = (int)(o.x + h    );  region.getPoint(1).y = (int)(o.y - a-b  );
			region.getPoint(2).x = (int)(o.x + h    );  region.getPoint(2).y = (int)(o.y - b    );
			region.getPoint(3).x = (int)(o.x        );  region.getPoint(3).y = (int)(o.y        );
			region.getPoint(4).x = (int)(o.x - h    );  region.getPoint(4).y = (int)(o.y - b    );
			region.getPoint(5).x = (int)(o.x - h    );  region.getPoint(5).y = (int)(o.y - a-b  );
			break;
		case 5:
			region.getPoint(0).x = (int)(o.x + b    );  region.getPoint(0).y = (int)(o.y - a-a-h);
			region.getPoint(1).x = (int)(o.x + b+h  );  region.getPoint(1).y = (int)(o.y - a-b-h);
			region.getPoint(2).x = (int)(o.x + h    );  region.getPoint(2).y = (int)(o.y - a-b  );
			region.getPoint(3).x = (int)(o.x        );  region.getPoint(3).y = (int)(o.y - a-a  );
			break;
		case 6:
			region.getPoint(0).x = (int)(o.x - h    );  region.getPoint(0).y = (int)(o.y - b    );
			region.getPoint(1).x = (int)(o.x - b-h  );  region.getPoint(1).y = (int)(o.y - b+h  );
			region.getPoint(2).x = (int)(o.x - a-h  );  region.getPoint(2).y = (int)(o.y - b    );
			break;
		case 7:
			region.getPoint(0).x = (int)(o.x        );  region.getPoint(0).y = (int)(o.y        );
			region.getPoint(1).x = (int)(o.x + b    );  region.getPoint(1).y = (int)(o.y + h    );
			region.getPoint(2).x = (int)(o.x - b    );  region.getPoint(2).y = (int)(o.y + h    );
			break;
		case 8:
			region.getPoint(0).x = (int)(o.x + h    );  region.getPoint(0).y = (int)(o.y - b    );
			region.getPoint(1).x = (int)(o.x + h+b  );  region.getPoint(1).y = (int)(o.y - b+h  );
			region.getPoint(2).x = (int)(o.x + b    );  region.getPoint(2).y = (int)(o.y + h    );
			region.getPoint(3).x = (int)(o.x        );  region.getPoint(3).y = (int)(o.y        );
			break;
		case 9:
			region.getPoint(0).x = (int)(o.x - h    );  region.getPoint(0).y = (int)(o.y - b    );
			region.getPoint(1).x = (int)(o.x        );  region.getPoint(1).y = (int)(o.y        );
			region.getPoint(2).x = (int)(o.x - b    );  region.getPoint(2).y = (int)(o.y + h    );
			region.getPoint(3).x = (int)(o.x - b-h  );  region.getPoint(3).y = (int)(o.y - b+h  );
			break;
		case 10:
			region.getPoint(0).x = (int)(o.x + b    );  region.getPoint(0).y = (int)(o.y + h    );
			region.getPoint(1).x = (int)(o.x + b    );  region.getPoint(1).y = (int)(o.y + a+h  );
			region.getPoint(2).x = (int)(o.x - b    );  region.getPoint(2).y = (int)(o.y + a+h  );
			region.getPoint(3).x = (int)(o.x - b    );  region.getPoint(3).y = (int)(o.y + h    );
			break;
		case 11:
			region.getPoint(0).x = (int)(o.x + b+h  );  region.getPoint(0).y = (int)(o.y + h-b  );
			region.getPoint(1).x = (int)(o.x + b+h+h);  region.getPoint(1).y = (int)(o.y + h    );
			region.getPoint(2).x = (int)(o.x + b+h+h);  region.getPoint(2).y = (int)(o.y + a+h  );
			region.getPoint(3).x = (int)(o.x + b+h  );  region.getPoint(3).y = (int)(o.y + a+b+h);
			region.getPoint(4).x = (int)(o.x + b    );  region.getPoint(4).y = (int)(o.y + a+h  );
			region.getPoint(5).x = (int)(o.x + b    );  region.getPoint(5).y = (int)(o.y + h    );
			break;
		}
	}

	@Override
	public Rect getRcInner(int borderWidth) {
		AttrSqTrHex attr = getAttr();
		int area = attr.getArea();
		double a = attr.CalcA(area);
		double b = a/2;
		double h = attr.CalcH(area);
		double w = borderWidth/2.;
		double sq = getAttr().CalcSq(area, borderWidth);
		double sq2 = sq/2;

		PointDouble o = getOffest();

		PointDouble center = new PointDouble(); // координата центра вписанного в фигуру квадрата
		switch (direction) {
		case  0: center.x = o.x -  b-h;    center.y = o.y - a-b-w-sq2;   break;
		case  1: center.x = o.x - (b+h)/2; center.y = o.y - a-b-(b+h)/2; break;
		case  2: center.x = o.x;           center.y = o.y - a-a-h+w+sq2; break;
		case  3: center.x = o.x -  b-h;    center.y = o.y - a;           break;
		case  4: center.x = o.x;           center.y = o.y - a;           break;
		case  5: center.x = o.x + (b+h)/2; center.y = o.y - a-b-(b+h)/2; break;
		case  6: center.x = o.x -  b-h;    center.y = o.y - b+w+sq2;     break;
		case  7: center.x = o.x;           center.y = o.y + h-w-sq2;     break;
		case  8: center.x = o.x + (b+h)/2; center.y = o.y - b+(b+h)/2;   break;
		case  9: center.x = o.x - (b+h)/2; center.y = o.y - b+(b+h)/2;   break;
		case 10: center.x = o.x;           center.y = o.y + b+h;         break;
		case 11: center.x = o.x +  b+h;    center.y = o.y + b+h;         break;
		}

		Rect square = new Rect();
		square.x = (int) (center.x - sq2);
		square.y = (int) (center.y - sq2);
		square.width =
		square.height = (int) sq;
		return square;
	}

	@Override
	public int getShiftPointBorderIndex() {
		switch (direction) {
		case 2: case  6: return 1;
		case 4: case 11: return 3;
		}
		return 2;
	}
}
