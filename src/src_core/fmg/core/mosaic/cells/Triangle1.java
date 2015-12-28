////////////////////////////////////////////////////////////////////////////////
//                               FastMines project
//                                      © Sergey Krivulya (KSerg, aka SeregaLBN)
// file name: "Triangle1.java"
//
// Реализация класса Triangle1 - равносторонний треугольник (вариант поля №1)
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
 * Треугольник. Вариант 1 - равносторонний, классика 
 * @see BaseCell
 **/
public class Triangle1 extends BaseCell {
	public static class AttrTriangle1 extends BaseAttribute {
		public AttrTriangle1(int area) {
			super(area);
		}

		@Override
		public Size getOwnerSize(Matrisize sizeField) {
			double b = getB();
			double h = getH();
			Size result = new Size(
					(int)(b * (sizeField.m+1)),
					(int)(h * (sizeField.n+0)));
			return result;
		}
	
		@Override
		public int getNeighborNumber(boolean max) { return 12; }
		@Override
		public int getNeighborNumber(int direction) { return 12; }
		@Override
		public int getVertexNumber(int direction) { return 3; }
		@Override
		public double getVertexIntersection() { return 6; }
		@Override
		public Size GetDirectionSizeField() { return new Size(2, 2); }
		@Override
		protected double getA() { return getB() * 2.f; } // размер стороны треугольника
		/** пол стороны треугольника */
		protected double getB() { return Math.sqrt(getArea()/SQRT3); }
		/** высота треугольника */
		protected double getH() { return getB() * SQRT3; }
		@Override
		public double getSq(int borderWidth) {
			double w = borderWidth/2.;
			return (getH()*2 - 6*w)/(SQRT3+2);
			//return (getA()*SQRT3 - 6*w)/(SQRT3+2);
		}
	}

	public Triangle1(AttrTriangle1 attr, Coord coord) {
		super(attr, coord,
				((coord.y&1)<<1)+(coord.x&1) // 0..3
			);
	}

	@Override
	public AttrTriangle1 getAttr() {
		return (AttrTriangle1) super.getAttr();
	}

	@Override
	protected Coord[] GetCoordsNeighbor() {
		Coord[] neighborCoord = new Coord[getAttr().getNeighborNumber(true)];

		// определяю координаты соседей
    	switch (direction) {
    	case 0: case 3:
    		neighborCoord[ 0] = new Coord(coord.x-1, coord.y-1);
    		neighborCoord[ 1] = new Coord(coord.x  , coord.y-1);
    		neighborCoord[ 2] = new Coord(coord.x+1, coord.y-1);
    		neighborCoord[ 3] = new Coord(coord.x-2, coord.y  );
    		neighborCoord[ 4] = new Coord(coord.x-1, coord.y  );
    		neighborCoord[ 5] = new Coord(coord.x+1, coord.y  );
    		neighborCoord[ 6] = new Coord(coord.x+2, coord.y  );
    		neighborCoord[ 7] = new Coord(coord.x-2, coord.y+1);
    		neighborCoord[ 8] = new Coord(coord.x-1, coord.y+1);
    		neighborCoord[ 9] = new Coord(coord.x  , coord.y+1);
    		neighborCoord[10] = new Coord(coord.x+1, coord.y+1);
    		neighborCoord[11] = new Coord(coord.x+2, coord.y+1);
    		break;
    	case 1: case 2:
    		neighborCoord[ 0] = new Coord(coord.x-2, coord.y-1);
    		neighborCoord[ 1] = new Coord(coord.x-1, coord.y-1);
    		neighborCoord[ 2] = new Coord(coord.x  , coord.y-1);
    		neighborCoord[ 3] = new Coord(coord.x+1, coord.y-1);
    		neighborCoord[ 4] = new Coord(coord.x+2, coord.y-1);
    		neighborCoord[ 5] = new Coord(coord.x-2, coord.y  );
    		neighborCoord[ 6] = new Coord(coord.x-1, coord.y  );
    		neighborCoord[ 7] = new Coord(coord.x+1, coord.y  );
    		neighborCoord[ 8] = new Coord(coord.x+2, coord.y  );
    		neighborCoord[ 9] = new Coord(coord.x-1, coord.y+1);
    		neighborCoord[10] = new Coord(coord.x  , coord.y+1);
    		neighborCoord[11] = new Coord(coord.x+1, coord.y+1);
    		break;
    	}

		return neighborCoord;
	}

	@Override
	protected void CalcRegion() {
		AttrTriangle1 attr = getAttr();
		double a = attr.getA();
		double b = attr.getB();
		double h = attr.getH();

		double oX = a*(coord.x>>1); // offset X
		double oY = h* coord.y;     // offset Y

		switch (direction) {
		case 0:
			region.setPoint(0, (int)(oX +   b), (int)(oY    ));
			region.setPoint(1, (int)(oX + a  ), (int)(oY + h));
			region.setPoint(2, (int)(oX      ), (int)(oY + h));
			break;
		case 1:
			region.setPoint(0, (int)(oX + a+b), (int)(oY    ));
			region.setPoint(1, (int)(oX + a  ), (int)(oY + h));
			region.setPoint(2, (int)(oX +   b), (int)(oY    ));
			break;
		case 2:
			region.setPoint(0, (int)(oX + a  ), (int)(oY    ));
			region.setPoint(1, (int)(oX +   b), (int)(oY + h));
			region.setPoint(2, (int)(oX      ), (int)(oY    ));
			break;
		case 3:
			region.setPoint(0, (int)(oX + a  ), (int)(oY    ));
			region.setPoint(1, (int)(oX + a+b), (int)(oY + h));
			region.setPoint(2, (int)(oX +   b), (int)(oY + h));
			break;
		}
	}

	@Override
	public Rect getRcInner(int borderWidth) {
		AttrTriangle1 attr = getAttr();
		double b = attr.getB();
		double sq = attr.getSq(borderWidth);
		double w = borderWidth/2.;

		PointDouble center = new PointDouble(); // координата вписанного в фигуру квадрата (не совпадает с центром фигуры)
		switch (direction) {
		case 0: case 3:
			center.x = region.getPoint(2).x + b;
			center.y = region.getPoint(2).y - sq/2 - w;
			break;
		case 1: case 2:
			center.x = region.getPoint(2).x + b;
			center.y = region.getPoint(2).y + sq/2 + w;
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
		case 0: case 3: return 2;
		}
		return 1;
	}
}