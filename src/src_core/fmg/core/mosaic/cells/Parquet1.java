////////////////////////////////////////////////////////////////////////////////
//                               FastMines project
//                                      © Sergey Krivulya (KSerg, aka SeregaLBN)
// file name: "Parquet1.java"
//
// Реализация класса Parquet1 - паркет в елку (herring-bone parquet)
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
import fmg.common.geom.Rect;
import fmg.common.geom.Size;

/**
 * Паркет в елку
 * @see BaseCell
 **/
public class Parquet1 extends BaseCell {
	public static class AttrParquet1 extends BaseAttribute {
		public AttrParquet1(int area) {
			super(area);
		}

		@Override
		public Size CalcOwnerSize(Size sizeField, int area) {
			double a = CalcA(area);
			Size result = new Size(
					(int)((sizeField.width*2+1) * a),
					(int)((sizeField.height*2+2) * a));

			if (sizeField.width == 1)
				result.height -= a;

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
		public Size GetDirectionSizeField() { return new Size(2, 1); }
		@Override
		protected double CalcA(int area) { return Math.sqrt(area)/2; }
		@Override
		public double CalcSq(int area, int borderWidth) {
			double w = borderWidth/2.;
			return CalcA(area)-w*SQRT2;
		}
	}

	public Parquet1(AttrParquet1 attr, Coord coord) {
		super(attr, coord,
		           coord.x&1 // 0..1
				);
	}

	@Override
	public AttrParquet1 getAttr() {
		return (AttrParquet1) super.getAttr();
	}

	@Override
	protected Coord[] GetCoordsNeighbor() {
		Coord[] neighborCoord = new Coord[getAttr().getNeighborNumber(true)];

		// определяю координаты соседей
    	boolean bdir = (direction != 0);
    	neighborCoord[0] = new Coord(bdir ? coord.x  : coord.x-1, coord.y-1);
		neighborCoord[1] = new Coord(bdir ? coord.x-1: coord.x  , bdir ? coord.y  : coord.y-1);
		neighborCoord[2] = new Coord(       coord.x+1           , bdir ? coord.y  : coord.y-1);
		neighborCoord[3] = new Coord(       coord.x-1           , bdir ? coord.y+1: coord.y);
		neighborCoord[4] = new Coord(bdir ? coord.x  : coord.x+1, bdir ? coord.y+1: coord.y);
		neighborCoord[5] = new Coord(bdir ? coord.x+1: coord.x  , coord.y+1);

		return neighborCoord;
	}

	@Override
	protected void CalcRegion() {
		AttrParquet1 attr = getAttr();
		double a = attr.CalcA(attr.getArea());

		switch (direction) {
		case 0:
			region.setPoint(0, (int)(a * (2 + 2 * coord.x)), (int)(a * (0 + 2 * coord.y)));
			region.setPoint(1, (int)(a * (3 + 2 * coord.x)), (int)(a * (1 + 2 * coord.y)));
			region.setPoint(2, (int)(a * (1 + 2 * coord.x)), (int)(a * (3 + 2 * coord.y)));
			region.setPoint(3, (int)(a * (0 + 2 * coord.x)), (int)(a * (2 + 2 * coord.y)));
			break;
		case 1:
			region.setPoint(0, (int)(a * (1 + 2 * coord.x)), (int)(a * (1 + 2 * coord.y)));
			region.setPoint(1, (int)(a * (3 + 2 * coord.x)), (int)(a * (3 + 2 * coord.y)));
			region.setPoint(2, (int)(a * (2 + 2 * coord.x)), (int)(a * (4 + 2 * coord.y)));
			region.setPoint(3, (int)(a * (0 + 2 * coord.x)), (int)(a * (2 + 2 * coord.y)));
			break;
		}
	}

	@Override
	public Rect getRcInner(int borderWidth) {
		AttrParquet1 attr = getAttr();
		double sq = attr.CalcSq(attr.getArea(), borderWidth);
		double w = borderWidth/2.;
		boolean bdir = (direction != 0);

		Rect square = new Rect();
		square.x = (int) ((bdir ? region.getPoint(0).x: region.getPoint(2).x) + w / SQRT2);
		square.y = (int) ((bdir ? region.getPoint(3).y: region.getPoint(1).y) + w / SQRT2);
		square.width = (int)sq;
		square.height = (int)sq;
		return square;
	}

	@Override
	public int getShiftPointBorderIndex() { return 2; }
}
