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

using System;
using fmg.common.geom;

namespace fmg.core.mosaic.cells {

/// <summary> Квадрат. Вариант 2 - сдвинутые ряды </summary>
public class Square2 : BaseCell {
	public class AttrSquare2 : BaseAttribute {
		public AttrSquare2(int area)
			: base(area)
      {}

		public override Size GetOwnerSize(Matrisize sizeField) {
			double a = A; // размер стороны квадрата
			Size result = new Size(
					(int)(sizeField.m * a + a/2),
					(int)(sizeField.n * a));
			return result;
		}

      public override int getNeighborNumber(bool max) { return 6; }
		public override int getNeighborNumber(int direction) { return 6; }
		public override int getVertexNumber(int direction) { return 4; }
		public override double getVertexIntersection() { return 3; }
		public override Size GetDirectionSizeField() { return new Size(1, 2); }
		public override double A => Math.Sqrt(Area);
		public override double GetSq(int borderWidth) {
			double w = borderWidth/2.0;
			return A-2*w;
		}
	}

	public Square2(AttrSquare2 attr, Coord coord)
		: base(attr, coord,
		           coord.y&1 // 0..1
				)
	{}

	private new AttrSquare2 Attr {
		get { return (AttrSquare2) base.Attr; }
	}

	protected override Coord?[] GetCoordsNeighbor() {
      var neighborCoord = new Coord?[Attr.getNeighborNumber(true)];

		// определяю координаты соседей
    	neighborCoord[0] = new Coord(coord.x- direction   , coord.y-1);
		neighborCoord[1] = new Coord(coord.x+(direction^1), coord.y-1);
		neighborCoord[2] = new Coord(coord.x-1            , coord.y);
		neighborCoord[3] = new Coord(coord.x+1            , coord.y);
		neighborCoord[4] = new Coord(coord.x- direction   , coord.y+1);
		neighborCoord[5] = new Coord(coord.x+(direction^1), coord.y+1);

		return neighborCoord;
	}

	public override bool PointInRegion(Point point) {
		if ((point.x < region.getPoint(3).x) || (point.x >= region.getPoint(0).x) ||
			(point.y < region.getPoint(0).y) || (point.y >= region.getPoint(2).y))
			return false;
		return true;
	}

	protected override void CalcRegion() {
		AttrSquare2 attr = Attr;
		double a = attr.A;

      int x1 = (int)(a * (coord.x + 0) + ((direction != 0) ? 0 : a / 2));
      int x2 = (int)(a * (coord.x + 1) + ((direction != 0) ? 0 : a / 2));
      int y1 = (int)(a * (coord.y + 0));
      int y2 = (int)(a * (coord.y + 1));

      region.setPoint(0, x2, y1);
      region.setPoint(1, x2, y2);
      region.setPoint(2, x1, y2);
      region.setPoint(3, x1, y1);
	}

	public override Rect getRcInner(int borderWidth) {
		AttrSquare2 attr = Attr;
		double sq = attr.GetSq(borderWidth);
		double w = borderWidth/2.0;

		Rect square = new Rect();
		square.X = (int) (region.getPoint(3).x + w);
		square.Y = (int) (region.getPoint(3).y + w);
		square.Width = (int)sq;
		square.Height = (int)sq;
		return square;
	}

	public override int getShiftPointBorderIndex() { return 2; }
}
}