////////////////////////////////////////////////////////////////////////////////
//                               FastMines project
//                                      © Sergey Krivulya (KSerg, aka SeregaLBN)
// file name: "Square2.java"
//
// –еализаци€ класса Square2 - квадрат (перекошенный вариант пол€)
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

/// <summary>  вадрат. ¬ариант 2 - сдвинутые р€ды </summary>
public class Square2 : BaseCell {
	public class AttrSquare2 : BaseAttribute {
		public AttrSquare2(int area)
			: base(area)
      {}

		public override Size CalcOwnerSize(Size sizeField, int area) {
			double a = CalcA(area); // размер стороны квадрата
			Size result = new Size(
					(int)(sizeField.width * a + a/2),
					(int)(sizeField.height * a));
			return result;
		}
	
		public override int getNeighborNumber() { return 6; }
		public override int getNeighborNumber(int direction) { return 6; }
		public override int getVertexNumber() { return 4; }
		public override int getVertexNumber(int direction) { return 4; }
		public override double getVertexIntersection() { return 3; }
		public override Size GetDirectionSizeField() { return new Size(1, 2); }
		public override double CalcA(int area) { return Math.Sqrt(area); }
		public override double CalcSq(int area, int borderWidth) {
			double w = borderWidth/2.0;
			return CalcA(area)-2*w;
		}
	}

	public Square2(AttrSquare2 attr, Coord coord)
		: base(attr, coord,
		           coord.y&1 // 0..1
				)
	{}

	private AttrSquare2 Attr {
		get { return (AttrSquare2) base.Attr; }
	}

	protected override Coord?[] GetCoordsNeighbor() {
      var neighborCoord = new Coord?[Attr.getNeighborNumber()];

		// определ€ю координаты соседей
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
		double a = attr.CalcA(attr.Area);

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
		double sq = attr.CalcSq(attr.Area, borderWidth);
		double w = borderWidth/2.0;

		Rect square = new Rect();
		square.x = (int) (region.getPoint(3).x + w);
		square.y = (int) (region.getPoint(3).y + w);
		square.width = (int)sq;
		square.height = (int)sq;
		return square;
	}

	public override int getShiftPointBorderIndex() { return 2; }
}
}