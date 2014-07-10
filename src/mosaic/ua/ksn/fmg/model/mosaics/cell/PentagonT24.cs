////////////////////////////////////////////////////////////////////////////////
//                               FastMines project
//                                      © Sergey Krivulya (KSerg, aka SeregaLBN)
// file name: "PentagonT24.java"
//
// –еализаци€ класса PentagonT24 - равносторонний 5-ти угольник, тип є2 и є4
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

/// <summary> ѕ€тиугольник. “ип є2 и є4 - равносторонний </summary>
public class PentagonT24 : BaseCell {
	public class AttrPentagonT24 : BaseAttribute {
		public AttrPentagonT24(int area)
			: base(area)
      {}

		public override Size CalcOwnerSize(Size sizeField, int area) {
			double a = CalcA(area);
			double b = CalcB(area);
			Size result = new Size(
					(int)(b + sizeField.width * a),
					(int)(b + sizeField.height * a));

			if (sizeField.height == 1)
				result.width -= (int)(CalcC(area));

				return result;
		}
	
		public override int getNeighborNumber() { return 7; }
		public override int getNeighborNumber(int direction) { return 7; }
		public override int getVertexNumber() { return 5; }
		public override int getVertexNumber(int direction) { return 5; }
		public override double getVertexIntersection() { return 3.4; } // (3+3+3+4+4)/5.
		public override Size GetDirectionSizeField() { return new Size(2, 2); }
		public override double CalcA(int area) { return Math.Sqrt(area); }
		public double CalcB(int area) { return CalcA(area)*6/11; }
		public double CalcC(int area) { return CalcB(area)/2; }
		public override double CalcSq(int area, int borderWidth) {
			double w = borderWidth/2.0;
			return CalcA(area)*8/11-(w+w/SIN135a) / SQRT2;
		}
	}

	public PentagonT24(AttrPentagonT24 attr, Coord coord)
		: base(attr, coord,
		           ((coord.y&1)<<1) + (coord.x&1) // 0..3
				)
	{}

	private AttrPentagonT24 Attr {
		get { return (AttrPentagonT24) base.Attr; }
	}

	protected override Coord?[] GetCoordsNeighbor() {
		var neighborCoord = new Coord?[Attr.getNeighborNumber()];

		// определ€ю координаты соседей
		switch (direction) {
		case 0:
			neighborCoord[0] = new Coord(coord.x-1, coord.y-1);
			neighborCoord[1] = new Coord(coord.x  , coord.y-1);
			neighborCoord[2] = new Coord(coord.x-1, coord.y  );
			neighborCoord[3] = new Coord(coord.x+1, coord.y  );
			neighborCoord[4] = new Coord(coord.x-1, coord.y+1);
			neighborCoord[5] = new Coord(coord.x  , coord.y+1);
			neighborCoord[6] = new Coord(coord.x+1, coord.y+1);
			break;
		case 1:
			neighborCoord[0] = new Coord(coord.x-1, coord.y-1);
			neighborCoord[1] = new Coord(coord.x  , coord.y-1);
			neighborCoord[2] = new Coord(coord.x+1, coord.y-1);
			neighborCoord[3] = new Coord(coord.x-1, coord.y  );
			neighborCoord[4] = new Coord(coord.x+1, coord.y  );
			neighborCoord[5] = new Coord(coord.x-1, coord.y+1);
			neighborCoord[6] = new Coord(coord.x  , coord.y+1);
			break;
		case 2:
			neighborCoord[0] = new Coord(coord.x  , coord.y-1);
			neighborCoord[1] = new Coord(coord.x+1, coord.y-1);
			neighborCoord[2] = new Coord(coord.x-1, coord.y  );
			neighborCoord[3] = new Coord(coord.x+1, coord.y  );
			neighborCoord[4] = new Coord(coord.x-1, coord.y+1);
			neighborCoord[5] = new Coord(coord.x  , coord.y+1);
			neighborCoord[6] = new Coord(coord.x+1, coord.y+1);
			break;
		case 3:
			neighborCoord[0] = new Coord(coord.x-1, coord.y-1);
			neighborCoord[1] = new Coord(coord.x  , coord.y-1);
			neighborCoord[2] = new Coord(coord.x+1, coord.y-1);
			neighborCoord[3] = new Coord(coord.x-1, coord.y  );
			neighborCoord[4] = new Coord(coord.x+1, coord.y  );
			neighborCoord[5] = new Coord(coord.x  , coord.y+1);
			neighborCoord[6] = new Coord(coord.x+1, coord.y+1);
			break;
		}

		return neighborCoord;
	}

	protected override void CalcRegion() {
		AttrPentagonT24 attr = Attr;
		int area = attr.Area;
		double a = attr.CalcA(area);
		double b = attr.CalcB(area);
		double c = attr.CalcC(area);

		// определение координат точек фигуры
		double oX = a*((coord.x>>1)<<1); // offset X
		double oY = a*((coord.y>>1)<<1); // offset Y
		switch (direction) {
		case 0:
			region.setPoint(0, (int)(oX +       a), (int)(oY + b      ));
			region.setPoint(1, (int)(oX + c +   a), (int)(oY + c +   a));
			region.setPoint(2, (int)(oX + b      ), (int)(oY + b +   a));
			region.setPoint(3, (int)(oX          ), (int)(oY +       a));
			region.setPoint(4, (int)(oX + c      ), (int)(oY + c      ));
			break;
		case 1:
			region.setPoint(0, (int)(oX + c + 2*a), (int)(oY + c      ));
			region.setPoint(1, (int)(oX +     2*a), (int)(oY +       a));
			region.setPoint(2, (int)(oX + c +   a), (int)(oY + c +   a));
			region.setPoint(3, (int)(oX +       a), (int)(oY + b      ));
			region.setPoint(4, (int)(oX + b +   a), (int)(oY          ));
			break;
		case 2:
			region.setPoint(0, (int)(oX + c +   a), (int)(oY + c +   a));
			region.setPoint(1, (int)(oX + b +   a), (int)(oY +     2*a));
			region.setPoint(2, (int)(oX +       a), (int)(oY + b + 2*a));
			region.setPoint(3, (int)(oX + c      ), (int)(oY + c + 2*a));
			region.setPoint(4, (int)(oX + b      ), (int)(oY + b +   a));
			break;
		case 3:
			region.setPoint(0, (int)(oX +     2*a), (int)(oY +       a));
			region.setPoint(1, (int)(oX + b + 2*a), (int)(oY + b +   a));
			region.setPoint(2, (int)(oX + c + 2*a), (int)(oY + c + 2*a));
			region.setPoint(3, (int)(oX + b +   a), (int)(oY +     2*a));
			region.setPoint(4, (int)(oX + c +   a), (int)(oY + c +   a));
			break;
		}
	}

	public override Rect getRcInner(int borderWidth) {
		AttrPentagonT24 attr = Attr;
		double sq = attr.CalcSq(attr.Area, borderWidth);
		double w = borderWidth/2.0;
		double w2 = w/SQRT2;

		Rect square = new Rect();
		switch (direction) {
		case 0:
			square.x = (int) (region.getPoint(4).x+w2);
			square.y = (int) (region.getPoint(1).y-w2 - sq);
			break;
		case 1:
			square.x = (int) (region.getPoint(2).x+w2);
			square.y = (int) (region.getPoint(0).y+w2);
			break;
		case 2:
			square.x = (int) (region.getPoint(0).x-w2 - sq);
			square.y = (int) (region.getPoint(3).y-w2 - sq);
			break;
		case 3:
			square.x = (int) (region.getPoint(2).x-w2 - sq);
			square.y = (int) (region.getPoint(4).y+w2);
			break;
		}
		square.width = (int)sq;
		square.height = (int)sq;
		return square;
	}

	public override int getShiftPointBorderIndex() {
		switch (direction) {
		case 0: case 1:
			return 2;
		}
		return 3;
	}
}
}