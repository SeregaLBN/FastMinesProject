////////////////////////////////////////////////////////////////////////////////
//                               FastMines project
//                                      © Sergey Krivulya (KSerg, aka SeregaLBN)
// file name: "Square1.java"
//
// ќписание класса Square1 - квадрат (классический вариант пол€)
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
import ua.ksn.geom.Rect;
import ua.ksn.geom.Size;
import ua.ksn.geom.Point;

/**
 *  вадрат. ¬ариант 1
 * @see BaseCell
 **/
public class Square1 extends BaseCell {
	public static class AttrSquare1 extends BaseAttribute {
		public AttrSquare1(int area) {
			super(area);
		}

		@Override
		public Size CalcOwnerSize(Size sizeField, int area) {
			double a = CalcA(area); // размер стороны квадрата
			Size result = new Size(
					(int)(sizeField.width * a),
					(int)(sizeField.height * a));
			return result;
		}
	
		@Override
		public int getNeighborNumber() { return 8; }
		@Override
		public int getNeighborNumber(int direction) { return 8; }
		@Override
		public int getVertexNumber() { return 4; }
		@Override
		public int getVertexNumber(int direction) { return 4; }
		@Override
		public double getVertexIntersection() { return 4; }
		@Override
		public Size GetDirectionSizeField() { return new Size(1,1); }
		@Override
		protected double CalcA(int area) { return Math.sqrt(area); }
		@Override
		public double CalcSq(int area, int borderWidth) {
			double w = borderWidth/2.;
			return CalcA(area)-2*w;
		}

		@Override
		public Size sizeIcoField(boolean smallSize) {
			return new Size(smallSize ? 2 : 3, smallSize ? 2 : 3);
		}
	}

	public Square1(AttrSquare1 attr, Coord coord) {
		super(attr, coord, -1);
	}

	@Override
	public AttrSquare1 getAttr() {
		return (AttrSquare1) super.getAttr();
	}

	@Override
	protected Coord[] GetCoordsNeighbor() {
		Coord[] neighborCoord = new Coord[getAttr().getNeighborNumber()];

		// определ€ю координаты соседей
    	neighborCoord[0] = new Coord(coord.x-1, coord.y-1);
		neighborCoord[1] = new Coord(coord.x  , coord.y-1);
		neighborCoord[2] = new Coord(coord.x+1, coord.y-1);
		neighborCoord[3] = new Coord(coord.x-1, coord.y);
		neighborCoord[4] = new Coord(coord.x+1, coord.y);
		neighborCoord[5] = new Coord(coord.x-1, coord.y+1);
		neighborCoord[6] = new Coord(coord.x  , coord.y+1);
		neighborCoord[7] = new Coord(coord.x+1, coord.y+1);

		return neighborCoord;
	}

	@Override
	public boolean PointInRegion(Point point) {
		if ((point.x < region.getPoint(3).x) || (point.x >= region.getPoint(0).x) ||
			(point.y < region.getPoint(0).y) || (point.y >= region.getPoint(2).y))
			return false;
		return true;
	}

	@Override
	protected void CalcRegion() {
		AttrSquare1 attr = getAttr();
		double a = attr.CalcA(attr.getArea());

		region.getPoint(3).x = region.getPoint(2).x = (int) (a*(coord.x+0));
		region.getPoint(0).x = region.getPoint(1).x = (int) (a*(coord.x+1));

		region.getPoint(0).y = region.getPoint(3).y = (int) (a*(coord.y+0));
		region.getPoint(2).y = region.getPoint(1).y = (int) (a*(coord.y+1));
	}

	@Override
	public Rect getRcInner(int borderWidth) {
		AttrSquare1 attr = getAttr();
		double sq = attr.CalcSq(attr.getArea(), borderWidth);
		double w = borderWidth/2.;

		Rect square = new Rect();
		square.x = (int) (region.getPoint(3).x + w);
		square.y = (int) (region.getPoint(3).y + w);
		square.width = (int)sq;
		square.height = (int)sq;
		return square;
	}

	@Override
	public int getShiftPointBorderIndex() { return 2; }

	@Override
	public Color getBackgroundFillColor(int fillMode, Color defaultColor, Map<Integer, Color> repositoryColor) {
		switch (fillMode) {
		default:
			return super.getBackgroundFillColor(fillMode, defaultColor, repositoryColor);
		case 1: // перекрываю базовый на основе direction
			int pos = (-getCoord().x + getCoord().y) % ((getAttr().hashCode() & 0x3)+fillMode);
//			System.out.println(pos);
			return repositoryColor.get(pos);
		}
	}
}