////////////////////////////////////////////////////////////////////////////////
//                               FastMines project
//                                      © Sergey Krivulya (KSerg, aka SeregaLBN)
// file name: "TrSq1.java"
//
// Реализация класса TrSq1 - мозаика из 4х треугольников и 2х квадратов
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
 * Комбинация. Мозаика из 4х треугольников и 2х квадратов 
 * @see BaseCell
 **/
public class TrSq1 extends BaseCell {
	public static class AttrTrSq1 extends BaseAttribute {
		public AttrTrSq1(int area) {
			super(area);
		}

		@Override
		public Size CalcOwnerSize(Size sizeField, int area) {
			double b = CalcB(area);
			double k = CalcK(area);
			double n = CalcN(area);
			double m = CalcM(area);
			Size result = new Size(
					(int)(b+n*((sizeField.width-1+2)/3)+
					        k*((sizeField.width-1+1)/3)+
					        m*((sizeField.width-1+0)/3)),
					(int)(b+n* (sizeField.height-1)));

			if (sizeField.height == 1) {
				if ((sizeField.width % 3) == 2) result.width -= m;
				if ((sizeField.width % 3) == 0) result.width -= k;
			}
			if (sizeField.width == 1)
				if ((sizeField.height & 1) == 0)
					result.height -= m;

			return result;
		}
	
		@Override
		public int getNeighborNumber() { return 12; }
		@Override
		public int getNeighborNumber(int direction) {
	    	switch (direction) {
	    	case 1: case 2: case 3: case 5: return 9;
	    	case 0: case 4: return 12;
	    	default:
	    		throw new IllegalArgumentException("Invalid value direction=" + direction);
	    	}
		}
		@Override
		public int getVertexNumber(int direction) {
			switch (direction) {
			case 1: case 2: case 3: case 5: return 3;
			case 0: case 4: return 4;
			default:
				throw new IllegalArgumentException("Invalid value direction="+direction);
		 	}
		}
		@Override
		public double getVertexIntersection() { return 5.; }
		@Override
		public Size GetDirectionSizeField() { return new Size(3, 2); }
		@Override
		protected double CalcA(int area) { return Math.sqrt(3*area/(1+SQRT3/2)); }
		protected double CalcB(int area) { return CalcN(area)+CalcM(area); }
		protected double CalcK(int area) { return CalcN(area)-CalcM(area); }
		protected double CalcN(int area) { return CalcA(area)*SIN75; }
		protected double CalcM(int area) { return CalcA(area)*SIN15; }
		@Override
		public double CalcSq(int area, int borderWidth) {
			double w = borderWidth/2.;
			return (CalcA(area)*SQRT3 - w*6) / (4*SIN75); 
		}
	}

	public TrSq1(AttrTrSq1 attr, Coord coord) {
		super(attr, coord,
				(coord.y&1)*3+(coord.x%3) // 0..5
			);
	}

	@Override
	public AttrTrSq1 getAttr() {
		return (AttrTrSq1) super.getAttr();
	}

	@Override
	protected Coord[] GetCoordsNeighbor() {
		Coord[] neighborCoord = new Coord[getAttr().getNeighborNumber()];

		// определяю координаты соседей
    	switch (direction) {
    	case 0:
    		neighborCoord[ 0] = new Coord(coord.x-2, coord.y-1);
    		neighborCoord[ 1] = new Coord(coord.x-1, coord.y-1);
    		neighborCoord[ 2] = new Coord(coord.x  , coord.y-1);
    		neighborCoord[ 3] = new Coord(coord.x+1, coord.y-1);
    		neighborCoord[ 4] = new Coord(coord.x-2, coord.y  );
    		neighborCoord[ 5] = new Coord(coord.x-1, coord.y  );
    		neighborCoord[ 6] = new Coord(coord.x+1, coord.y  );
    		neighborCoord[ 7] = new Coord(coord.x+2, coord.y  );
    		neighborCoord[ 8] = new Coord(coord.x-2, coord.y+1);
    		neighborCoord[ 9] = new Coord(coord.x-1, coord.y+1);
    		neighborCoord[10] = new Coord(coord.x  , coord.y+1);
    		neighborCoord[11] = new Coord(coord.x+1, coord.y+1);
    		break;
    	case 1:
    		neighborCoord[ 0] = new Coord(coord.x-2, coord.y-1);
    		neighborCoord[ 1] = new Coord(coord.x-1, coord.y-1);
    		neighborCoord[ 2] = new Coord(coord.x  , coord.y-1);
    		neighborCoord[ 3] = new Coord(coord.x+1, coord.y-1);
    		neighborCoord[ 4] = new Coord(coord.x-1, coord.y  );
    		neighborCoord[ 5] = new Coord(coord.x+1, coord.y  );
    		neighborCoord[ 6] = new Coord(coord.x+2, coord.y  );
    		neighborCoord[ 7] = new Coord(coord.x-1, coord.y+1);
    		neighborCoord[ 8] = new Coord(coord.x  , coord.y+1);
    		neighborCoord[ 9] =
    		neighborCoord[10] =
    		neighborCoord[11] = null;
    		break;
    	case 2:
    		neighborCoord[ 0] = new Coord(coord.x-1, coord.y-1);
    		neighborCoord[ 1] = new Coord(coord.x  , coord.y-1);
    		neighborCoord[ 2] = new Coord(coord.x-2, coord.y  );
    		neighborCoord[ 3] = new Coord(coord.x-1, coord.y  );
    		neighborCoord[ 4] = new Coord(coord.x+1, coord.y  );
    		neighborCoord[ 5] = new Coord(coord.x-2, coord.y+1);
    		neighborCoord[ 6] = new Coord(coord.x-1, coord.y+1);
    		neighborCoord[ 7] = new Coord(coord.x  , coord.y+1);
    		neighborCoord[ 8] = new Coord(coord.x+1, coord.y+1);
    		neighborCoord[ 9] =  
    		neighborCoord[10] =
    		neighborCoord[11] = null;
    		break;
    	case 3:
    		neighborCoord[ 0] = new Coord(coord.x-1, coord.y-1);
    		neighborCoord[ 1] = new Coord(coord.x  , coord.y-1);
    		neighborCoord[ 2] = new Coord(coord.x+1, coord.y-1);
    		neighborCoord[ 3] = new Coord(coord.x+2, coord.y-1);
    		neighborCoord[ 4] = new Coord(coord.x-2, coord.y  );
    		neighborCoord[ 5] = new Coord(coord.x-1, coord.y  );
    		neighborCoord[ 6] = new Coord(coord.x+1, coord.y  );
    		neighborCoord[ 7] = new Coord(coord.x  , coord.y+1);
    		neighborCoord[ 8] = new Coord(coord.x+1, coord.y+1);
    		neighborCoord[ 9] =
    		neighborCoord[10] =
    		neighborCoord[11] = null;
    		break;
    	case 4:
    		neighborCoord[ 0] = new Coord(coord.x-1, coord.y-1);
    		neighborCoord[ 1] = new Coord(coord.x  , coord.y-1);
    		neighborCoord[ 2] = new Coord(coord.x+1, coord.y-1);
    		neighborCoord[ 3] = new Coord(coord.x+2, coord.y-1);
    		neighborCoord[ 4] = new Coord(coord.x-2, coord.y  );
    		neighborCoord[ 5] = new Coord(coord.x-1, coord.y  );
    		neighborCoord[ 6] = new Coord(coord.x+1, coord.y  );
    		neighborCoord[ 7] = new Coord(coord.x+2, coord.y  );
    		neighborCoord[ 8] = new Coord(coord.x-1, coord.y+1);
    		neighborCoord[ 9] = new Coord(coord.x  , coord.y+1);
    		neighborCoord[10] = new Coord(coord.x+1, coord.y+1);
    		neighborCoord[11] = new Coord(coord.x+2, coord.y+1);
    		break;
    	case 5:
    		neighborCoord[ 0] = new Coord(coord.x  , coord.y-1);
    		neighborCoord[ 1] = new Coord(coord.x+1, coord.y-1);
    		neighborCoord[ 2] = new Coord(coord.x-1, coord.y  );
    		neighborCoord[ 3] = new Coord(coord.x+1, coord.y  );
    		neighborCoord[ 4] = new Coord(coord.x+2, coord.y  );
    		neighborCoord[ 5] = new Coord(coord.x-1, coord.y+1);
    		neighborCoord[ 6] = new Coord(coord.x  , coord.y+1);
    		neighborCoord[ 7] = new Coord(coord.x+1, coord.y+1);
    		neighborCoord[ 8] = new Coord(coord.x+2, coord.y+1);
    		neighborCoord[ 9] =
    		neighborCoord[10] =
    		neighborCoord[11] = null;
    		break;
    	}

		return neighborCoord;
	}

	@Override
	protected void CalcRegion() {
		AttrTrSq1 attr = getAttr();
		int area = attr.getArea();
		double b = attr.CalcB(area);
		double k = attr.CalcK(area);
		double n = attr.CalcN(area);
		double m = attr.CalcM(area);

		double oX = b + n * (coord.x/3*2); // offset X
		double oY = n + n*2*(coord.y/2);   // offset Y

		switch (direction) {
		case 0:
			region.setPoint(0, (int)(oX - m  ), (int)(oY - n));
			region.setPoint(1, (int)(oX      ), (int)(oY    ));
			region.setPoint(2, (int)(oX - n  ), (int)(oY + m));
			region.setPoint(3, (int)(oX - b  ), (int)(oY - k));
			break;
		case 1:                            
			region.setPoint(1, (int)(oX      ), (int)(oY    ));
			region.setPoint(2, (int)(oX - m  ), (int)(oY - n));
			region.setPoint(0, (int)(oX + k  ), (int)(oY - k));
			break;
		case 2:                            
			region.setPoint(0, (int)(oX + k  ), (int)(oY - k));
			region.setPoint(1, (int)(oX + n  ), (int)(oY + m));
			region.setPoint(2, (int)(oX      ), (int)(oY    ));
			break;
		case 3:                            
			region.setPoint(1, (int)(oX - m  ), (int)(oY + n));
			region.setPoint(2, (int)(oX - n  ), (int)(oY + m));
			region.setPoint(0, (int)(oX      ), (int)(oY    ));
			break;
		case 4:                            
			region.setPoint(0, (int)(oX + n  ), (int)(oY + m));
			region.setPoint(3, (int)(oX      ), (int)(oY    ));
			region.setPoint(2, (int)(oX - m  ), (int)(oY + n));
			region.setPoint(1, (int)(oX + k  ), (int)(oY + b));
			break;
		case 5:                            
			region.setPoint(0, (int)(oX + n  ), (int)(oY + m));
			region.setPoint(1, (int)(oX + n+k), (int)(oY + n));
			region.setPoint(2, (int)(oX + k  ), (int)(oY + b));
			break;
		}
	}

	@Override
	public Rect getRcInner(int borderWidth) {
		AttrTrSq1 attr = getAttr();
		int area = attr.getArea();
		double b = attr.CalcB(area);
		double k = attr.CalcK(area);
		double n = attr.CalcN(area);
		double m = attr.CalcM(area);
		double w = borderWidth/2.;
		double sq = attr.CalcSq(area, borderWidth);
		double sq2 = sq/2;

		double oX = b + n * (coord.x/3*2); // offset X
		double oY = n + n*2*(coord.y/2);   // offset Y


		double ksw1 = k/2-sq2-w/SQRT2;
		double ksw2 = k/2+sq2+w/SQRT2;
		PointDouble center = new PointDouble(); // координата вписанного в фигуру квадрата (не совпадает с центром фигуры)
		switch (direction) {
		case 0:  center.x = oX - b/2;    center.y = oY - k/2;    break;
		case 1:  center.x = oX + ksw1;   center.y = oY - ksw2;   break;
		case 2:  center.x = oX + ksw2;   center.y = oY - ksw1;   break;
		case 3:  center.x = oX + ksw2-n; center.y = oY - ksw2+n; break;
		case 4:  center.x = oX + k/2;    center.y = oY + b/2;    break;
		case 5:  center.x = oX + ksw1+n; center.y = oY + ksw2+m; break;
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
		case 1: case 3: return 1;
		}
		return 2;
	}
}