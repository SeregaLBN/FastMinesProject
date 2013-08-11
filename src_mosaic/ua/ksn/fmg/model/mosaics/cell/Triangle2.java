////////////////////////////////////////////////////////////////////////////////
//                               FastMines project
//                                      © Sergey Krivulya (KSerg, aka SeregaLBN)
// file name: "Triangle2.java"
//
// –еализаци€ класса Triangle2 - равносторонний треугольник (вариант пол€ є2 - Єлочкой)
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
 * “реугольник. ¬ариант 2 - равносторонний, Єлочкой 
 * @see BaseCell
 **/
public class Triangle2 extends BaseCell {
	public static class AttrTriangle2 extends BaseAttribute {
		public AttrTriangle2(int area) {
			super(area);
		}

		@Override
		public Size CalcOwnerSize(Size sizeField, int area) {
			double b = CalcB(area);
			double h = CalcH(area);
			Size result = new Size(
					(int)(b * (sizeField.width+1)),
					(int)(h * (sizeField.height+0)));
			return result;
		}
	
		@Override
		public int getNeighborNumber() { return 8; }
		@Override
		public int getNeighborNumber(int direction) { return 8; }
		@Override
		public int getVertexNumber() { return 3; }
		@Override
		public int getVertexNumber(int direction) { return 3; }
		@Override
		public double getVertexIntersection() { return 3.75; } // (4+4+4+3)/4.
		@Override
		public Size GetDirectionSizeField() { return new Size(2, 1); }
		@Override
		protected double CalcA(int area) { return CalcB(area) * 2.f; } // размер стороны треугольника
		/** пол стороны треугольника */
		protected double CalcB(int area) { return Math.sqrt(area/SQRT3); }
		/** высота треугольника */
		protected double CalcH(int area) { return CalcB(area) * SQRT3; }
		@Override
		public double CalcSq(int area, int borderWidth) {
			double w = borderWidth/2.;
			return (CalcH(area)*2 - 6*w)/(SQRT3+2);
		}

		@Override
		public Size sizeIcoField(boolean smallSize) {
			return new Size(3, smallSize ? 2 : 3);
		}
	}

	public Triangle2(AttrTriangle2 attr, Coord coord) {
		super(attr, coord,
				coord.x&1 // 0..1
			);
	}

	@Override
	public AttrTriangle2 getAttr() {
		return (AttrTriangle2) super.getAttr();
	}

	@Override
	protected Coord[] GetCoordsNeighbor() {
		Coord[] neighborCoord = new Coord[getAttr().getNeighborNumber()];

		// определ€ю координаты соседей
    	switch (direction) {
    	case 0: case 3:
    		neighborCoord[ 0] = new Coord(coord.x  , coord.y-1);
    		neighborCoord[ 1] = new Coord(coord.x-2, coord.y  );
    		neighborCoord[ 2] = new Coord(coord.x-1, coord.y  );
    		neighborCoord[ 3] = new Coord(coord.x+1, coord.y  );
    		neighborCoord[ 4] = new Coord(coord.x+2, coord.y  );
    		neighborCoord[ 5] = new Coord(coord.x-1, coord.y+1);
    		neighborCoord[ 6] = new Coord(coord.x  , coord.y+1);
    		neighborCoord[ 7] = new Coord(coord.x+1, coord.y+1);
    		break;                                          
    	case 1: case 2:                                    
    		neighborCoord[ 0] = new Coord(coord.x-1, coord.y-1);
    		neighborCoord[ 1] = new Coord(coord.x  , coord.y-1);
    		neighborCoord[ 2] = new Coord(coord.x+1, coord.y-1);
    		neighborCoord[ 3] = new Coord(coord.x-2, coord.y  );
    		neighborCoord[ 4] = new Coord(coord.x-1, coord.y  );
    		neighborCoord[ 5] = new Coord(coord.x+1, coord.y  );
    		neighborCoord[ 6] = new Coord(coord.x+2, coord.y  );
    		neighborCoord[ 7] = new Coord(coord.x  , coord.y+1);
    		break;
    	}

		return neighborCoord;
	}

	@Override
	protected void CalcRegion() {
		AttrTriangle2 attr = getAttr();
		int area = attr.getArea();
		double a = attr.CalcA(area);
		double b = attr.CalcB(area);
		double h = attr.CalcH(area);

		double oX = a*(coord.x>>1); // offset X
		double oY = h* coord.y;     // offset Y

		switch (direction) {
		case 0:
			region.getPoint(0).x = (int)(oX +   b); region.getPoint(0).y = (int)(oY    );
			region.getPoint(1).x = (int)(oX + a  ); region.getPoint(1).y = (int)(oY + h);
			region.getPoint(2).x = (int)(oX      ); region.getPoint(2).y = (int)(oY + h);
			break;                                                              
		case 1:                                                                
			region.getPoint(0).x = (int)(oX + a+b); region.getPoint(0).y = (int)(oY    );
			region.getPoint(1).x = (int)(oX + a  ); region.getPoint(1).y = (int)(oY + h);
			region.getPoint(2).x = (int)(oX +   b); region.getPoint(2).y = (int)(oY    );
			break;
		}
	}

	@Override
	public Rect getRcInner(int borderWidth) {
		AttrTriangle2 attr = getAttr();
		int area = attr.getArea();
		double b = attr.CalcB(area);
		double sq = attr.CalcSq(area, borderWidth);
		double w = borderWidth/2.;

		PointDouble center = new PointDouble(); // координата вписанного в фигуру квадрата (не совпадает с центром фигуры)
		switch (direction) {
		case 0:
			center.x = region.getPoint(2).x + b;
			center.y = region.getPoint(2).y - sq/2 - w;
			break;
		case 1:
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
		case 1: return 1;
		}
		return 2;
	}
}