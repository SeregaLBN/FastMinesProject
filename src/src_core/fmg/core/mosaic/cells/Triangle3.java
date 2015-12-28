////////////////////////////////////////////////////////////////////////////////
//                               FastMines project
//                                      © Sergey Krivulya (KSerg, aka SeregaLBN)
// file name: "Triangle3.java"
//
// Реализация класса Triangle3 - треугольник 45°-90°-45°
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

package fmg.core.mosaic.cells;

import fmg.common.geom.Coord;
import fmg.common.geom.Matrisize;
import fmg.common.geom.PointDouble;
import fmg.common.geom.Rect;
import fmg.common.geom.Size;

/**
 * Треугольник. Вариант 3 - треугольник 45°-90°-45°(квадрат разделённый на 4 части) 
 * @see BaseCell
 **/
public class Triangle3 extends BaseCell {
	public static class AttrTriangle3 extends BaseAttribute {
		public AttrTriangle3(int area) {
			super(area);
		}

		@Override
		public Size getOwnerSize(Matrisize sizeField) {
			double a = getA();
			Size result = new Size(
					(int)(a * ((sizeField.m+1)>>1)),
					(int)(a * ((sizeField.n+1)>>1)));

			if (sizeField.m == 1)
				if ((sizeField.n & 1) == 1)
					result.height -= a*0.5;

			return result;
		}
	
		@Override
		public int getNeighborNumber(boolean max) { return 14; }
		@Override
		public int getNeighborNumber(int direction) { return 14; }
		@Override
		public int getVertexNumber(int direction) { return 3; }
		@Override
		public double getVertexIntersection() { return 6.6666666666666666666666666666667; } // (8+8+4)/3.
		@Override
		public Size GetDirectionSizeField() { return new Size(2, 2); }
		@Override
		protected double getA() { return 2*getB(); }
		/** пол стороны треугольника */
		protected double getB() { return Math.sqrt(getArea()); }
		@Override
		public double getSq(int borderWidth) {
			double w = borderWidth/2.;
			return (getA() - w*2 / TAN45_2 ) / 3; 
		}
	}

	public Triangle3(AttrTriangle3 attr, Coord coord) {
		super(attr, coord,
				((coord.y&1)<<1)+(coord.x&1) // 0..3
			);
	}

	@Override
	public AttrTriangle3 getAttr() {
		return (AttrTriangle3) super.getAttr();
	}

	@Override
	protected Coord[] GetCoordsNeighbor() {
		Coord[] neighborCoord = new Coord[getAttr().getNeighborNumber(true)];

		// определяю координаты соседей
    	switch (direction) {
    	case 0:
    		neighborCoord[ 0] = new Coord(coord.x-1, coord.y-2);
    		neighborCoord[ 1] = new Coord(coord.x+1, coord.y-2);
    		neighborCoord[ 2] = new Coord(coord.x-1, coord.y-1);
    		neighborCoord[ 3] = new Coord(coord.x  , coord.y-1);
    		neighborCoord[ 4] = new Coord(coord.x+1, coord.y-1);
    		neighborCoord[ 5] = new Coord(coord.x+2, coord.y-1);
    		neighborCoord[ 6] = new Coord(coord.x+3, coord.y-1);
    		neighborCoord[ 7] = new Coord(coord.x-2, coord.y  );
    		neighborCoord[ 8] = new Coord(coord.x-1, coord.y  );
    		neighborCoord[ 9] = new Coord(coord.x+1, coord.y  );
    		neighborCoord[10] = new Coord(coord.x+2, coord.y  );
    		neighborCoord[11] = new Coord(coord.x  , coord.y+1);
    		neighborCoord[12] = new Coord(coord.x+1, coord.y+1);
    		neighborCoord[13] = new Coord(coord.x+2, coord.y+1);
    		break;
    	case 1:
    		neighborCoord[ 0] = new Coord(coord.x  , coord.y-2);
    		neighborCoord[ 1] = new Coord(coord.x  , coord.y-1);
    		neighborCoord[ 2] = new Coord(coord.x+1, coord.y-1);
    		neighborCoord[ 3] = new Coord(coord.x+2, coord.y-1);
    		neighborCoord[ 4] = new Coord(coord.x-1, coord.y  );
    		neighborCoord[ 5] = new Coord(coord.x+1, coord.y  );
    		neighborCoord[ 6] = new Coord(coord.x-1, coord.y+1);
    		neighborCoord[ 7] = new Coord(coord.x  , coord.y+1);
    		neighborCoord[ 8] = new Coord(coord.x+1, coord.y+1);
    		neighborCoord[ 9] = new Coord(coord.x+2, coord.y+1);
    		neighborCoord[10] = new Coord(coord.x-1, coord.y+2);
    		neighborCoord[11] = new Coord(coord.x  , coord.y+2);
    		neighborCoord[12] = new Coord(coord.x+1, coord.y+2);
    		neighborCoord[13] = new Coord(coord.x+1, coord.y+3);
    		break;
    	case 2:
    		neighborCoord[ 0] = new Coord(coord.x-1, coord.y-3);
    		neighborCoord[ 1] = new Coord(coord.x-1, coord.y-2);
    		neighborCoord[ 2] = new Coord(coord.x  , coord.y-2);
    		neighborCoord[ 3] = new Coord(coord.x+1, coord.y-2);
    		neighborCoord[ 4] = new Coord(coord.x-2, coord.y-1);
    		neighborCoord[ 5] = new Coord(coord.x-1, coord.y-1);
    		neighborCoord[ 6] = new Coord(coord.x  , coord.y-1);
    		neighborCoord[ 7] = new Coord(coord.x+1, coord.y-1);
    		neighborCoord[ 8] = new Coord(coord.x-1, coord.y  );
    		neighborCoord[ 9] = new Coord(coord.x+1, coord.y  );
    		neighborCoord[10] = new Coord(coord.x-2, coord.y+1);
    		neighborCoord[11] = new Coord(coord.x-1, coord.y+1);
    		neighborCoord[12] = new Coord(coord.x  , coord.y+1);
    		neighborCoord[13] = new Coord(coord.x  , coord.y+2);
    		break;
    	case 3:
    		neighborCoord[ 0] = new Coord(coord.x-2, coord.y-1);
    		neighborCoord[ 1] = new Coord(coord.x-1, coord.y-1);
    		neighborCoord[ 2] = new Coord(coord.x  , coord.y-1);
    		neighborCoord[ 3] = new Coord(coord.x-2, coord.y  );
    		neighborCoord[ 4] = new Coord(coord.x-1, coord.y  );
    		neighborCoord[ 5] = new Coord(coord.x+1, coord.y  );
    		neighborCoord[ 6] = new Coord(coord.x+2, coord.y  );
    		neighborCoord[ 7] = new Coord(coord.x-3, coord.y+1);
    		neighborCoord[ 8] = new Coord(coord.x-2, coord.y+1);
    		neighborCoord[ 9] = new Coord(coord.x-1, coord.y+1);
    		neighborCoord[10] = new Coord(coord.x  , coord.y+1);
    		neighborCoord[11] = new Coord(coord.x+1, coord.y+1);
    		neighborCoord[12] = new Coord(coord.x-1, coord.y+2);
    		neighborCoord[13] = new Coord(coord.x+1, coord.y+2);
    		break;
    	}

		return neighborCoord;
	}

	@Override
	protected void CalcRegion() {
		AttrTriangle3 attr = getAttr();
		double a = attr.getA();
		double b = attr.getB();

		double oX = a*(coord.x>>1); // offset X
		double oY = a*(coord.y>>1); // offset Y

		switch (direction) {
		case 0:
			region.setPoint(0, (int)(oX + a), (int)(oY    ));
			region.setPoint(2, (int)(oX    ), (int)(oY    ));
			region.setPoint(1, (int)(oX + b), (int)(oY + b));
			break;
		case 1:
			region.setPoint(0, (int)(oX + a), (int)(oY    ));
			region.setPoint(2, (int)(oX + b), (int)(oY + b));
			region.setPoint(1, (int)(oX + a), (int)(oY + a));
			break;
		case 2:
			region.setPoint(2, (int)(oX    ), (int)(oY + a));
			region.setPoint(1, (int)(oX + b), (int)(oY + b));
			region.setPoint(0, (int)(oX    ), (int)(oY    ));
			break;
		case 3:
			region.setPoint(2, (int)(oX    ), (int)(oY + a));
			region.setPoint(1, (int)(oX + a), (int)(oY + a));
			region.setPoint(0, (int)(oX + b), (int)(oY + b));
			break;
		}
	}

	@Override
	public Rect getRcInner(int borderWidth) {
		AttrTriangle3 attr = getAttr();
		double sq = attr.getSq(borderWidth);
		double w = borderWidth/2.;

		PointDouble center = new PointDouble(); // координата вписанного в фигуру квадрата (не совпадает с центром фигуры)
		switch (direction) {
		case 0:
			center.x = region.getPoint(1).x;
			center.y = region.getPoint(0).y + sq/2 + w;
			break;
		case 1:
			center.x = region.getPoint(0).x - sq/2 - w;
			center.y = region.getPoint(2).y;
			break;
		case 2:
			center.x = region.getPoint(0).x + sq/2 + w;
			center.y = region.getPoint(1).y;
			break;
		case 3:
			center.x = region.getPoint(0).x;
			center.y = region.getPoint(1).y - sq/2 - w;
			break;
		}

		Rect square = new Rect();
		square.x = (int) (center.x - sq/2);
		square.y = (int) (center.y - sq/2);
		square.width =
		square.height = (int) sq;
		return square;
	}

	@Override
	public int getShiftPointBorderIndex() {
		switch (direction) {
		case 0: case 1: return 1;
		}
		return 2;
	}
}