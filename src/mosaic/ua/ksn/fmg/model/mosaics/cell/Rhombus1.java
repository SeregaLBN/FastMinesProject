////////////////////////////////////////////////////////////////////////////////
//                               FastMines project
//                                      © Sergey Krivulya (KSerg, aka SeregaLBN)
// file name: "Rhombus1.java"
//
// Реализация класса Rhombus1 - 3 ромба, составляющие равносторонний шестиугольник
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

import java.util.Map;

import ua.ksn.Color;
import ua.ksn.geom.Coord;
import ua.ksn.geom.PointDouble;
import ua.ksn.geom.Rect;
import ua.ksn.geom.Size;

/**
 * Rhombus1 - 3 ромба, составляющие равносторонний шестиугольник
 * @see BaseCell
 **/
public class Rhombus1 extends BaseCell {
	public static class AttrRhombus1 extends BaseAttribute {
		public AttrRhombus1(int area) {
			super(area);
		}

		@Override
		public Size CalcOwnerSize(Size sizeField, int area) {
			double a = CalcA(area);
			double r = CalcR(area);
			double c = CalcC(area);
			Size result = new Size(
					(int)(c+a   *((sizeField.width+2)/3) +
					       (a+c)*((sizeField.width+1)/3) +
					          c *((sizeField.width+0)/3)),
					(int)(    r * (sizeField.height+1)));

			if (sizeField.width == 1)
				result.height -= r;
			if (sizeField.height == 1)
				switch (sizeField.width % 3) {
				case 0: result.width -= a/2; break;
				case 2: result.width -= a; break;
				}

			return result;
		}
	
		@Override
		public int getNeighborNumber() { return 10; }
		@Override
		public int getNeighborNumber(int direction) { return 10; }
		@Override
		public int getVertexNumber() { return 4; }
		@Override
		public int getVertexNumber(int direction) { return 4; }
		@Override
		public double getVertexIntersection() { return 4.5; } // (3+3+6+6)/4.
		@Override
		public Size GetDirectionSizeField() { return new Size(3, 2); }
		@Override
		protected double CalcA(int area) { return Math.sqrt(area*2/SQRT3); }
		protected double CalcC(int area) { return CalcA(area)/2; }
		protected double CalcH(int area) { return CalcA(area)*SQRT3; }
		protected double CalcR(int area) { return CalcH(area)/2; }
		@Override
		public double CalcSq(int area, int borderWidth) {
			double w = borderWidth/2.;
			return (CalcA(area)*SQRT3 - w*4)/(SQRT3+1);
		}

		@Override
		public int getMaxBackgroundFillModeValue() {
			return super.getMaxBackgroundFillModeValue()+1;
		}
	}

	public Rhombus1(AttrRhombus1 attr, Coord coord) {
		super(attr, coord,
				(coord.y&1)*3+(coord.x%3) // 0..5
			);
	}

	@Override
	public AttrRhombus1 getAttr() {
		return (AttrRhombus1) super.getAttr();
	}

	@Override
	protected Coord[] GetCoordsNeighbor() {
		Coord[] neighborCoord = new Coord[getAttr().getNeighborNumber()];

		// определяю координаты соседей
    	switch (direction) {
    	case 0:
    		neighborCoord[ 0] = new Coord(coord.x+1, coord.y-2);
    		neighborCoord[ 1] = new Coord(coord.x+2, coord.y-2);
    		neighborCoord[ 2] = new Coord(coord.x-2, coord.y-1);
    		neighborCoord[ 3] = new Coord(coord.x-1, coord.y-1);
    		neighborCoord[ 4] = new Coord(coord.x  , coord.y-1);
    		neighborCoord[ 5] = new Coord(coord.x+1, coord.y-1);
    		neighborCoord[ 6] = new Coord(coord.x-1, coord.y  );
    		neighborCoord[ 7] = new Coord(coord.x+1, coord.y  );
    		neighborCoord[ 8] = new Coord(coord.x-1, coord.y+1);
    		neighborCoord[ 9] = new Coord(coord.x  , coord.y+1);
    		break;
    	case 1:
    		neighborCoord[ 0] = new Coord(coord.x  , coord.y-2);
    		neighborCoord[ 1] = new Coord(coord.x+1, coord.y-2);
    		neighborCoord[ 2] = new Coord(coord.x-1, coord.y-1);
    		neighborCoord[ 3] = new Coord(coord.x  , coord.y-1);
    		neighborCoord[ 4] = new Coord(coord.x-1, coord.y  );
    		neighborCoord[ 5] = new Coord(coord.x+1, coord.y  );
    		neighborCoord[ 6] = new Coord(coord.x-1, coord.y+1);
    		neighborCoord[ 7] = new Coord(coord.x  , coord.y+1);
    		neighborCoord[ 8] = new Coord(coord.x-1, coord.y+2);
    		neighborCoord[ 9] = new Coord(coord.x  , coord.y+2);
    		break;
    	case 2:
    		neighborCoord[ 0] = new Coord(coord.x-1, coord.y-1);
    		neighborCoord[ 1] = new Coord(coord.x  , coord.y-1);
    		neighborCoord[ 2] = new Coord(coord.x-1, coord.y  );
    		neighborCoord[ 3] = new Coord(coord.x+1, coord.y  );
    		neighborCoord[ 4] = new Coord(coord.x-2, coord.y+1);
    		neighborCoord[ 5] = new Coord(coord.x-1, coord.y+1);
    		neighborCoord[ 6] = new Coord(coord.x  , coord.y+1);
    		neighborCoord[ 7] = new Coord(coord.x+1, coord.y+1);
    		neighborCoord[ 8] = new Coord(coord.x-2, coord.y+2);
    		neighborCoord[ 9] = new Coord(coord.x-1, coord.y+2);
    		break;
    	case 3:
    		neighborCoord[ 0] = new Coord(coord.x-2, coord.y-2);
    		neighborCoord[ 1] = new Coord(coord.x-1, coord.y-2);
    		neighborCoord[ 2] = new Coord(coord.x-1, coord.y-1);
    		neighborCoord[ 3] = new Coord(coord.x  , coord.y-1);
    		neighborCoord[ 4] = new Coord(coord.x+1, coord.y-1);
    		neighborCoord[ 5] = new Coord(coord.x+2, coord.y-1);
    		neighborCoord[ 6] = new Coord(coord.x-1, coord.y  );
    		neighborCoord[ 7] = new Coord(coord.x+1, coord.y  );
    		neighborCoord[ 8] = new Coord(coord.x  , coord.y+1);
    		neighborCoord[ 9] = new Coord(coord.x+1, coord.y+1);
    		break;
    	case 4:
    		neighborCoord[ 0] = new Coord(coord.x  , coord.y-1);
    		neighborCoord[ 1] = new Coord(coord.x+1, coord.y-1);
    		neighborCoord[ 2] = new Coord(coord.x-1, coord.y  );
    		neighborCoord[ 3] = new Coord(coord.x+1, coord.y  );
    		neighborCoord[ 4] = new Coord(coord.x-1, coord.y+1);
    		neighborCoord[ 5] = new Coord(coord.x  , coord.y+1);
    		neighborCoord[ 6] = new Coord(coord.x+1, coord.y+1);
    		neighborCoord[ 7] = new Coord(coord.x+2, coord.y+1);
    		neighborCoord[ 8] = new Coord(coord.x+1, coord.y+2);
    		neighborCoord[ 9] = new Coord(coord.x+2, coord.y+2);
    		break;
    	case 5:
    		neighborCoord[ 0] = new Coord(coord.x-1, coord.y-2);
    		neighborCoord[ 1] = new Coord(coord.x  , coord.y-2);
    		neighborCoord[ 2] = new Coord(coord.x  , coord.y-1);
    		neighborCoord[ 3] = new Coord(coord.x+1, coord.y-1);
    		neighborCoord[ 4] = new Coord(coord.x-1, coord.y  );
    		neighborCoord[ 5] = new Coord(coord.x+1, coord.y  );
    		neighborCoord[ 6] = new Coord(coord.x  , coord.y+1);
    		neighborCoord[ 7] = new Coord(coord.x+1, coord.y+1);
    		neighborCoord[ 8] = new Coord(coord.x  , coord.y+2);
    		neighborCoord[ 9] = new Coord(coord.x+1, coord.y+2);
    		break;
    	}

		return neighborCoord;
	}

	@Override
	protected void CalcRegion() {
		AttrRhombus1 attr = getAttr();
		int area = attr.getArea();
		double a = attr.CalcA(area);
		double c = attr.CalcC(area);
		double h = attr.CalcH(area);
		double r = attr.CalcR(area);

		// определение координат точек фигуры
		double oX = a*(coord.x/3*3+1)+c; // offset X
		double oY = h*(coord.y/2)    +h; // offset Y

		switch (direction) {
		case 0:
			region.setPoint(0, (int)(oX      ), (int)(oY - h));
			region.setPoint(1, (int)(oX - c  ), (int)(oY - r));
			region.setPoint(2, (int)(oX - a-c), (int)(oY - r));
			region.setPoint(3, (int)(oX - a  ), (int)(oY - h));
			break;
		case 1:
			region.setPoint(0, (int)(oX      ), (int)(oY - h));
			region.setPoint(1, (int)(oX + c  ), (int)(oY - r));
			region.setPoint(2, (int)(oX      ), (int)(oY    ));
			region.setPoint(3, (int)(oX - c  ), (int)(oY - r));
			break;
		case 2:
			region.setPoint(0, (int)(oX + a+c), (int)(oY - r));
			region.setPoint(1, (int)(oX + a  ), (int)(oY    ));
			region.setPoint(2, (int)(oX      ), (int)(oY    ));
			region.setPoint(3, (int)(oX + c  ), (int)(oY - r));
			break;
		case 3:
			region.setPoint(0, (int)(oX - c  ), (int)(oY - r));
			region.setPoint(1, (int)(oX      ), (int)(oY    ));
			region.setPoint(2, (int)(oX - a  ), (int)(oY    ));
			region.setPoint(3, (int)(oX - a-c), (int)(oY - r));
			break;
		case 4:
			region.setPoint(0, (int)(oX + a  ), (int)(oY    ));
			region.setPoint(1, (int)(oX + a+c), (int)(oY + r));
			region.setPoint(2, (int)(oX + c  ), (int)(oY + r));
			region.setPoint(3, (int)(oX      ), (int)(oY    ));
			break;
		case 5:
			region.setPoint(0, (int)(oX + a+c), (int)(oY - r));
			region.setPoint(1, (int)(oX + a+a), (int)(oY    ));
			region.setPoint(2, (int)(oX + a+c), (int)(oY + r));
			region.setPoint(3, (int)(oX + a  ), (int)(oY    ));
			break;
		}
	}

	@Override
	public Rect getRcInner(int borderWidth) {
		AttrRhombus1 attr = getAttr();
		int area = attr.getArea();
		double a = attr.CalcA(area);
		double c = attr.CalcC(area);
		double h = attr.CalcH(area);
		double r = attr.CalcR(area);
//		double w = borderWidth/2.;
		double sq  = attr.CalcSq(area, borderWidth);
		double sq2 = sq/2;

		double oX = a*(coord.x/3*3+1)+c; // offset X
		double oY = h*(coord.y/2)    +h; // offset Y

		PointDouble center = new PointDouble(); // координата центра квадрата
		switch (direction) {
		case 0: center.x = oX - c*1.5; center.y = oY - r*1.5; break;
		case 1: center.x = oX;         center.y = oY - r;     break;
		case 2: center.x = oX + c*1.5; center.y = oY - r*0.5; break;
		case 3: center.x = oX - c*1.5; center.y = oY - r*0.5; break;
		case 4: center.x = oX + c*1.5; center.y = oY + r*0.5; break;
		case 5: center.x = oX + a+c;   center.y = oY;         break;
		}

		Rect square = new Rect();
		square.x = (int) (center.x - sq2);
		square.y = (int) (center.y - sq2);
		square.width =
		square.height = (int) sq;
		return square;
	}

	@Override
	public int getShiftPointBorderIndex() { return 2; }

	@Override
	public Color getBackgroundFillColor(int fillMode, Color defaultColor, Map<Integer, Color> repositoryColor) {
		if (fillMode == getAttr().getMaxBackgroundFillModeValue()) {
			switch ((getCoord().y%4)*3+(getCoord().x%3)) { // почти как вычисление direction...
			// подсвечиваю 4 группы, составляющие каждая шестигранник из 3х ромбов
			case 0: case  1: case  3: return repositoryColor.get(0);
			case 2: case  4: case  5: return repositoryColor.get(1);
			case 6: case  7: case  9: return repositoryColor.get(2);
			case 8: case 10: case 11: return repositoryColor.get(3);
			}
		}
		return super.getBackgroundFillColor(fillMode, defaultColor, repositoryColor);
	}
}