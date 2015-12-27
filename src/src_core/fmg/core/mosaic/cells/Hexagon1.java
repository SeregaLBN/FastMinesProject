////////////////////////////////////////////////////////////////////////////////
//                               FastMines project
//                                      © Sergey Krivulya (KSerg, aka SeregaLBN)
// file name: "Hexagon1.java"
//
// Реализация класса Hexagon1 - правильный 6-ти угольник (сота)
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
import fmg.common.geom.PointDouble;
import fmg.common.geom.Rect;
import fmg.common.geom.Size;

/**
 * Шестиугольник 
 * @see BaseCell
 **/
public class Hexagon1 extends BaseCell {
	public static class AttrHexagon1 extends BaseAttribute {
		public AttrHexagon1(int area) {
			super(area);
		}

		@Override
		public Size getOwnerSize(Size sizeField) {
			double a = CalcA();
			Size result = new Size(
					(int)(a * (sizeField.width    +0.5) * SQRT3),
					(int)(a * (sizeField.height*1.5+0.5)));

			if (sizeField.height == 1)
				result.width -= CalcB()/2;

			return result;
		}
	
		@Override
		public int getNeighborNumber(boolean max) { return 6; }
		@Override
		public int getNeighborNumber(int direction) { return 6; }
		@Override
		public int getVertexNumber(int direction) { return 6; }
		@Override
		public double getVertexIntersection() { return 3; }
		@Override
		public Size GetDirectionSizeField() { return new Size(1, 2); }
		@Override
		protected double CalcA() { return Math.sqrt(2*getArea()/SQRT27); }
		/** пол стороны треугольника */
		protected double CalcB() { return CalcA()*SQRT3; }
		@Override
		public double CalcSq(int borderWidth) {
			double w = borderWidth/2.;
			return 2*(CalcB() - 2*w)/(SQRT3+1);
		}
	}

	public Hexagon1(AttrHexagon1 attr, Coord coord) {
		super(attr, coord,
				coord.y&1 // 0..1
			);
	}

	@Override
	public AttrHexagon1 getAttr() {
		return (AttrHexagon1) super.getAttr();
	}

	@Override
	protected Coord[] GetCoordsNeighbor() {
		Coord[] neighborCoord = new Coord[getAttr().getNeighborNumber(true)];

		// определяю координаты соседей
		neighborCoord[0] = new Coord(coord.x-(direction^1), coord.y-1);
		neighborCoord[1] = new Coord(coord.x+ direction   , coord.y-1);
		neighborCoord[2] = new Coord(coord.x-1            , coord.y);
		neighborCoord[3] = new Coord(coord.x+1            , coord.y);
		neighborCoord[4] = new Coord(coord.x-(direction^1), coord.y+1);
		neighborCoord[5] = new Coord(coord.x+ direction   , coord.y+1);

		return neighborCoord;
	}

	@Override
	protected void CalcRegion() {
		AttrHexagon1 attr = getAttr();
		double a = attr.CalcA();
		double b = attr.CalcB();

		double oX = (coord.x+1)*b;                 // offset X
		double oY = (coord.y+(direction^1))*a*1.5; // offset Y

		switch (direction) {
		case 0:
			region.setPoint(0, (int)(oX      ), (int)(oY - a  ));
			region.setPoint(1, (int)(oX      ), (int)(oY      ));
			region.setPoint(2, (int)(oX - b/2), (int)(oY + a/2));
			region.setPoint(3, (int)(oX - b  ), (int)(oY      ));
			region.setPoint(4, (int)(oX - b  ), (int)(oY - a  ));
			region.setPoint(5, (int)(oX - b/2), (int)(oY - a*1.5));
			break;
		case 1:
			region.setPoint(0, (int)(oX + b/2), (int)(oY + a/2  ));
			region.setPoint(1, (int)(oX + b/2), (int)(oY + a*1.5));
			region.setPoint(2, (int)(oX      ), (int)(oY + a*2  ));
			region.setPoint(3, (int)(oX - b/2), (int)(oY + a*1.5));
			region.setPoint(4, (int)(oX - b/2), (int)(oY + a/2  ));
			region.setPoint(5, (int)(oX      ), (int)(oY        ));
			break;
		}
	}

	@Override
	public Rect getRcInner(int borderWidth) {
		AttrHexagon1 attr = getAttr();
		double a = attr.CalcA();
		double b = attr.CalcB();
		double sq = getAttr().CalcSq(borderWidth);

		double oX = (coord.x+1)*b;      // offset X
		double oY = (coord.y+1-direction)*a*1.5; // offset Y

		PointDouble center = new PointDouble(); // координата вписанного в фигуру квадрата (не совпадает с центром фигуры)
		switch (direction) {
		case 0: center.x = oX - b/2; center.y = oY - a/2; break;
		case 1: center.x = oX;       center.y = oY + a;   break;
		}

		Rect square = new Rect();
		square.x = (int) (center.x - sq/2);
		square.y = (int) (center.y - sq/2);
		square.width =
		square.height = (int) sq;
		return square;
	}

	@Override
	public int getShiftPointBorderIndex() { return 3; }
}
