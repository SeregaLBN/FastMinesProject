////////////////////////////////////////////////////////////////////////////////
//                               FastMines project
//                                      © Sergey Krivulya (KSerg, aka SeregaLBN)
// file name: "Square1.java"
//
// Описание класса Square1 - квадрат (классический вариант поля)
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
using fmg.common;
using fmg.common.geom;

namespace fmg.core.mosaic.cells {

/// <summary> Квадрат. Вариант 1 </summary>
public class Square1 : BaseCell {
	public class AttrSquare1 : BaseAttribute {
		public AttrSquare1(int area)
			: base(area)
      {}

		public override Size GetOwnerSize(Matrisize sizeField) {
			double a = A; // размер стороны квадрата
			Size result = new Size(
					(int)(sizeField.m * a),
					(int)(sizeField.n * a));
			return result;
		}

      public override int getNeighborNumber(bool max) { return 8; }
		public override int getNeighborNumber(int direction) { return 8; }
		public override int getVertexNumber(int direction) { return 4; }
		public override double getVertexIntersection() { return 4; }
		public override Size GetDirectionSizeField() { return new Size(1,1); }
		public override double A => Math.Sqrt(Area);
		public override double GetSq(int borderWidth) {
			double w = borderWidth/2.0;
			return A-2*w;
		}
	}

	public Square1(AttrSquare1 attr, Coord coord)
		: base(attr, coord, -1)
	{}

	private new AttrSquare1 Attr {
		get { return (AttrSquare1) base.Attr; }
	}

	protected override Coord?[] GetCoordsNeighbor() {
      var neighborCoord = new Coord?[Attr.getNeighborNumber(true)];

		// определяю координаты соседей
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

	public override bool PointInRegion(Point point) {
		if ((point.X < region.GetPoint(3).X) || (point.X >= region.GetPoint(0).X) ||
			(point.Y < region.GetPoint(0).Y) || (point.Y >= region.GetPoint(2).Y))
			return false;
		return true;
	}

	protected override void CalcRegion() {
		AttrSquare1 attr = Attr;
		double a = attr.A;

      int x1 = (int)(a * (coord.x + 0));
      int x2 = (int)(a * (coord.x + 1));
      int y1 = (int)(a * (coord.y + 0));
      int y2 = (int)(a * (coord.y + 1));

      region.SetPoint(0, x2, y1);
      region.SetPoint(1, x2, y2);
      region.SetPoint(2, x1, y2);
      region.SetPoint(3, x1, y1);
	}

	public override Rect getRcInner(int borderWidth) {
		AttrSquare1 attr = Attr;
		double sq = attr.GetSq(borderWidth);
		double w = borderWidth/2.0;

		Rect square = new Rect();
		square.X = (int) (region.GetPoint(3).X + w);
		square.Y = (int) (region.GetPoint(3).Y + w);
		square.Width = (int)sq;
		square.Height = (int)sq;
		return square;
	}

	public override int getShiftPointBorderIndex() { return 2; }

	public override Color getBackgroundFillColor(int fillMode, Color defaultColor, Func<int, Color> repositoryColor) {
		switch (fillMode) {
		default:
			return base.getBackgroundFillColor(fillMode, defaultColor, repositoryColor);
		case 1: // перекрываю базовый на основе direction
			int pos = (-getCoord().x + getCoord().y) % ((Attr.GetHashCode() & 0x3)+fillMode);
//			System.out.println(pos);
			return repositoryColor(pos);
		}
	}
}
}