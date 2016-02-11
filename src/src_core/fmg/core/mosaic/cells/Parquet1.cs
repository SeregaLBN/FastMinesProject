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

using System;
using fmg.common.geom;

namespace fmg.core.mosaic.cells {

/// <summary> Паркет в елку </summary>
public class Parquet1 : BaseCell {
	public class AttrParquet1 : BaseAttribute {
		public AttrParquet1(int area)
			: base(area)
      {}

		public override Size GetOwnerSize(Matrisize sizeField) {
			double a = A;
			Size result = new Size(
					(int)((sizeField.m*2+1) * a),
					(int)((sizeField.n*2+2) * a));

			if (sizeField.m == 1)
				result.height -= (int)a;

			return result;
		}

      public override int getNeighborNumber(bool max) { return 6; }
		public override int getNeighborNumber(int direction) { return 6; }
		public override int getVertexNumber(int direction) { return 4; }
		public override double getVertexIntersection() { return 3; }
		public override Size GetDirectionSizeField() { return new Size(2, 1); }
		public override double A => Math.Sqrt(Area)/2;
		public override double GetSq(int borderWidth) {
			double w = borderWidth/2.0;
			return A-w*SQRT2;
		}
	}

	public Parquet1(AttrParquet1 attr, Coord coord)
		: base(attr, coord,
		           coord.x&1 // 0..1
				)
	{}

	private new AttrParquet1 Attr {
		get { return (AttrParquet1) base.Attr; }
	}

	protected override Coord?[] GetCoordsNeighbor() {
		var neighborCoord = new Coord?[Attr.getNeighborNumber(true)];

		// определяю координаты соседей
    	bool bdir = (direction != 0);
    	neighborCoord[0] = new Coord(bdir ? coord.x  : coord.x-1, coord.y-1);
		neighborCoord[1] = new Coord(bdir ? coord.x-1: coord.x  , bdir ? coord.y  : coord.y-1);
		neighborCoord[2] = new Coord(       coord.x+1           , bdir ? coord.y  : coord.y-1);
		neighborCoord[3] = new Coord(       coord.x-1           , bdir ? coord.y+1: coord.y);
		neighborCoord[4] = new Coord(bdir ? coord.x  : coord.x+1, bdir ? coord.y+1: coord.y);
		neighborCoord[5] = new Coord(bdir ? coord.x+1: coord.x  , coord.y+1);

		return neighborCoord;
	}

	protected override void CalcRegion() {
		AttrParquet1 attr = Attr;
		double a = attr.A;

		switch (direction) {
		case 0:
			region.SetPoint(0, (int)(a * (2 + 2 * coord.x)), (int)(a * (0 + 2 * coord.y)));
			region.SetPoint(1, (int)(a * (3 + 2 * coord.x)), (int)(a * (1 + 2 * coord.y)));
			region.SetPoint(2, (int)(a * (1 + 2 * coord.x)), (int)(a * (3 + 2 * coord.y)));
			region.SetPoint(3, (int)(a * (0 + 2 * coord.x)), (int)(a * (2 + 2 * coord.y)));
			break;
		case 1:
			region.SetPoint(0, (int)(a * (1 + 2 * coord.x)), (int)(a * (1 + 2 * coord.y)));
			region.SetPoint(1, (int)(a * (3 + 2 * coord.x)), (int)(a * (3 + 2 * coord.y)));
			region.SetPoint(2, (int)(a * (2 + 2 * coord.x)), (int)(a * (4 + 2 * coord.y)));
			region.SetPoint(3, (int)(a * (0 + 2 * coord.x)), (int)(a * (2 + 2 * coord.y)));
			break;
		}
	}

	public override Rect getRcInner(int borderWidth) {
		AttrParquet1 attr = Attr;
		double sq = attr.GetSq(borderWidth);
		double w = borderWidth/2.0;
		bool bdir = (direction != 0);

		Rect square = new Rect();
		square.X = (int) ((bdir ? region.GetPoint(0).X: region.GetPoint(2).X) + w / SQRT2);
		square.Y = (int) ((bdir ? region.GetPoint(3).Y: region.GetPoint(1).Y) + w / SQRT2);
		square.Width = (int)sq;
		square.Height = (int)sq;
		return square;
	}

	public override int getShiftPointBorderIndex() { return 2; }
}
}