////////////////////////////////////////////////////////////////////////////////
//                               FastMines project
//                                      © Sergey Krivulya (KSerg, aka SeregaLBN)
// file name: "Square2.java"
//
// Реализация класса Square2 - квадрат (перекошенный вариант поля)
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
import fmg.common.geom.Point;
import fmg.common.geom.Rect;
import fmg.common.geom.Size;

/**
 * Квадрат. Вариант 2 - сдвинутые ряды
 * @see BaseCell
 **/
public class Square2 extends BaseCell {
	public static class AttrSquare2 extends BaseAttribute {
		public AttrSquare2(int area) {
			super(area);
		}

		@Override
		public Size getOwnerSize(Matrisize sizeField) {
			double a = getA(); // размер стороны квадрата
			Size result = new Size(
					(int)(sizeField.m * a + a/2),
					(int)(sizeField.n * a));
			return result;
		}
	
		@Override
		public int getNeighborNumber(boolean max) { return 6; }
		@Override
		public int getNeighborNumber(int direction) { return 6; }
		@Override
		public int getVertexNumber(int direction) { return 4; }
		@Override
		public double getVertexIntersection() { return 3; }
		@Override
		public Size GetDirectionSizeField() { return new Size(1, 2); }
		@Override
		protected double getA() { return Math.sqrt(getArea()); }
		@Override
		public double getSq(int borderWidth) {
			double w = borderWidth/2.;
			return getA()-2*w;
		}
	}

	public Square2(AttrSquare2 attr, Coord coord) {
		super(attr, coord,
		           coord.y&1 // 0..1
				);
	}

	@Override
	public AttrSquare2 getAttr() {
		return (AttrSquare2) super.getAttr();
	}

	@Override
	protected Coord[] GetCoordsNeighbor() {
		Coord[] neighborCoord = new Coord[getAttr().getNeighborNumber(true)];

		// определяю координаты соседей
    	neighborCoord[0] = new Coord(coord.x- direction   , coord.y-1);
		neighborCoord[1] = new Coord(coord.x+(direction^1), coord.y-1);
		neighborCoord[2] = new Coord(coord.x-1            , coord.y);
		neighborCoord[3] = new Coord(coord.x+1            , coord.y);
		neighborCoord[4] = new Coord(coord.x- direction   , coord.y+1);
		neighborCoord[5] = new Coord(coord.x+(direction^1), coord.y+1);

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
		AttrSquare2 attr = getAttr();
		double a = attr.getA();

      int x1 = (int)(a * (coord.x + 0) + ((direction != 0) ? 0 : a / 2));
      int x2 = (int)(a * (coord.x + 1) + ((direction != 0) ? 0 : a / 2));
      int y1 = (int)(a * (coord.y + 0));
      int y2 = (int)(a * (coord.y + 1));

      region.setPoint(0, x2, y1);
      region.setPoint(1, x2, y2);
      region.setPoint(2, x1, y2);
      region.setPoint(3, x1, y1);
	}

	@Override
	public Rect getRcInner(int borderWidth) {
		AttrSquare2 attr = getAttr();
		double sq = attr.getSq(borderWidth);
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
}
