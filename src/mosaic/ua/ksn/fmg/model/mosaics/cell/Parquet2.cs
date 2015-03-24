////////////////////////////////////////////////////////////////////////////////
//                               FastMines project
//                                      © Sergey Krivulya (KSerg, aka SeregaLBN)
// file name: "Parquet2.java"
//
// Реализация класса Parquet2 - ещё один паркет
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

using System;
using ua.ksn.geom;
 
namespace ua.ksn.fmg.model.mosaics.cell {

/// <summary> Паркет. Вариант №2 </summary>
public class Parquet2 : BaseCell {
	public class AttrParquet2 : BaseAttribute {
		public AttrParquet2(int area)
			: base(area)
      {}

		public override Size CalcOwnerSize(Size sizeField, int area) {
			double a = CalcA(area);
			Size result = new Size(
					(int)((sizeField.width*2+2) * a),
					(int)((sizeField.height*2+2) * a));

			if (sizeField.width == 1)
				result.height -= (int)a;

			return result;
		}

      public override int getNeighborNumber(bool max) { return 7; }
		public override int getNeighborNumber(int direction) { return 7; }
		public override int getVertexNumber(int direction) { return 4; }
		public override double getVertexIntersection() { return 3.5; } // (4+4+3+3) / 4
		public override Size GetDirectionSizeField() { return new Size(2, 2); }
		public override double CalcA(int area) { return Math.Sqrt(area)/2; }
		public override double CalcSq(int area, int borderWidth) {
			double w = borderWidth/2.0;
			return CalcA(area)-w*SQRT2;
		}
	}

	public Parquet2(AttrParquet2 attr, Coord coord)
		: base(attr, coord,
		           ((coord.y&1)<<1) + (coord.x&1) // 0..3
				)
	{}

	private AttrParquet2 Attr {
		get { return (AttrParquet2) base.Attr; }
	}

	protected override Coord?[] GetCoordsNeighbor() {
		var neighborCoord = new Coord?[Attr.getNeighborNumber(true)];

		// определяю координаты соседей
    	switch (direction) {
    	case 0:
    		neighborCoord[0] = new Coord(coord.x-1, coord.y-1);
    		neighborCoord[1] = new Coord(coord.x  , coord.y-1);
    		neighborCoord[2] = new Coord(coord.x+1, coord.y-1);
    		neighborCoord[3] = new Coord(coord.x-1, coord.y  );
    		neighborCoord[4] = new Coord(coord.x+1, coord.y  );
    		neighborCoord[5] = new Coord(coord.x  , coord.y+1);
    		neighborCoord[6] = new Coord(coord.x+1, coord.y+1);
    		break;
    	case 1:
    		neighborCoord[0] = new Coord(coord.x  , coord.y-1);
    		neighborCoord[1] = new Coord(coord.x+1, coord.y-1);
    		neighborCoord[2] = new Coord(coord.x-1, coord.y  );
    		neighborCoord[3] = new Coord(coord.x+1, coord.y  );
    		neighborCoord[4] = new Coord(coord.x-1, coord.y+1);
    		neighborCoord[5] = new Coord(coord.x  , coord.y+1);
    		neighborCoord[6] = new Coord(coord.x+1, coord.y+1);
    		break;
    	case 2:
    		neighborCoord[0] = new Coord(coord.x-1, coord.y-1);
    		neighborCoord[1] = new Coord(coord.x  , coord.y-1);
    		neighborCoord[2] = new Coord(coord.x+1, coord.y-1);
    		neighborCoord[3] = new Coord(coord.x-1, coord.y  );
    		neighborCoord[4] = new Coord(coord.x+1, coord.y  );
    		neighborCoord[5] = new Coord(coord.x-1, coord.y+1);
    		neighborCoord[6] = new Coord(coord.x  , coord.y+1);
    		break;
    	case 3:
    		neighborCoord[0] = new Coord(coord.x-1, coord.y-1);
    		neighborCoord[1] = new Coord(coord.x  , coord.y-1);
    		neighborCoord[2] = new Coord(coord.x-1, coord.y  );
    		neighborCoord[3] = new Coord(coord.x+1, coord.y  );
    		neighborCoord[4] = new Coord(coord.x-1, coord.y+1);
    		neighborCoord[5] = new Coord(coord.x  , coord.y+1);
    		neighborCoord[6] = new Coord(coord.x+1, coord.y+1);
    		break;
    	}

		return neighborCoord;
	}

	protected override void CalcRegion() {
		AttrParquet2 attr = Attr;
		double a = attr.CalcA(attr.Area);

		switch (direction) {
		case 0:
			region.setPoint(0, (int)((2 * coord.x + 2) * a), (int)((2 * coord.y + 0) * a));
			region.setPoint(1, (int)((2 * coord.x + 4) * a), (int)((2 * coord.y + 2) * a));
			region.setPoint(2, (int)((2 * coord.x + 3) * a), (int)((2 * coord.y + 3) * a));
			region.setPoint(3, (int)((2 * coord.x + 1) * a), (int)((2 * coord.y + 1) * a));
			break;
		case 1:
			region.setPoint(0, (int)((2 * coord.x + 3) * a), (int)((2 * coord.y + 1) * a));
			region.setPoint(1, (int)((2 * coord.x + 4) * a), (int)((2 * coord.y + 2) * a));
			region.setPoint(2, (int)((2 * coord.x + 2) * a), (int)((2 * coord.y + 4) * a));
			region.setPoint(3, (int)((2 * coord.x + 1) * a), (int)((2 * coord.y + 3) * a));
			break;
		case 2:
			region.setPoint(0, (int)((2 * coord.x + 2) * a), (int)((2 * coord.y + 0) * a));
			region.setPoint(1, (int)((2 * coord.x + 3) * a), (int)((2 * coord.y + 1) * a));
			region.setPoint(2, (int)((2 * coord.x + 1) * a), (int)((2 * coord.y + 3) * a));
			region.setPoint(3, (int)((2 * coord.x + 0) * a), (int)((2 * coord.y + 2) * a));
			break;
		case 3:
			region.setPoint(0, (int)((2 * coord.x + 1) * a), (int)((2 * coord.y + 1) * a));
			region.setPoint(1, (int)((2 * coord.x + 3) * a), (int)((2 * coord.y + 3) * a));
			region.setPoint(2, (int)((2 * coord.x + 2) * a), (int)((2 * coord.y + 4) * a));
			region.setPoint(3, (int)((2 * coord.x + 0) * a), (int)((2 * coord.y + 2) * a));
			break;
		}
	}

	public override Rect getRcInner(int borderWidth) {
		AttrParquet2 attr = Attr;
		double sq = attr.CalcSq(attr.Area, borderWidth);
		double w = borderWidth/2.0;

		Rect square = new Rect();
		switch (direction) {
		case 0: case 3:
			square.x = (int) (region.getPoint(0).x + w/SQRT2);
			square.y = (int) (region.getPoint(3).y + w/SQRT2);
			break;
		case 1: case 2:
			square.x = (int) (region.getPoint(2).x + w/SQRT2);
			square.y = (int) (region.getPoint(1).y + w/SQRT2);
			break;
		}
		square.width = (int)sq;
		square.height = (int)sq;
		return square;
	}

	public override int getShiftPointBorderIndex() { return 2; }
}
}
