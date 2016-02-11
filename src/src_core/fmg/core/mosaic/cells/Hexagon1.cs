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

using System;
using fmg.common.geom;

namespace fmg.core.mosaic.cells {

/// <summary> Шестиугольник </summary>
public class Hexagon1 : BaseCell {
	public class AttrHexagon1 : BaseAttribute {
		public AttrHexagon1(int area)
			: base(area)
      {}

		public override Size GetOwnerSize(Matrisize sizeField) {
			double a = A;
			Size result = new Size(
					(int)(a * (sizeField.m    +0.5) * SQRT3),
					(int)(a * (sizeField.n*1.5+0.5)));

			if (sizeField.n == 1)
				result.width -= (int)(B/2);

			return result;
		}
	
		public override int getNeighborNumber(bool max) { return 6; }
		public override int getNeighborNumber(int direction) { return 6; }
		public override int getVertexNumber(int direction) { return 6; }
		public override double getVertexIntersection() { return 3; }
		public override Size GetDirectionSizeField() { return new Size(1, 2); }
		public override double A => Math.Sqrt(2*Area/SQRT27);
		/// <summary> пол стороны треугольника </summary>
		public double B => A*SQRT3;
		public override double GetSq(int borderWidth) {
			double w = borderWidth/2.0;
			return 2*(B - 2*w)/(SQRT3+1);
		}
	}

	public Hexagon1(AttrHexagon1 attr, Coord coord)
		: base(attr, coord,
				coord.y&1 // 0..1
			)
	{}

	private new AttrHexagon1 Attr {
		get { return (AttrHexagon1) base.Attr; }
	}

	protected override Coord?[] GetCoordsNeighbor() {
		var neighborCoord = new Coord?[Attr.getNeighborNumber(true)];

		// определяю координаты соседей
		neighborCoord[0] = new Coord(coord.x-(direction^1), coord.y-1);
		neighborCoord[1] = new Coord(coord.x+ direction   , coord.y-1);
		neighborCoord[2] = new Coord(coord.x-1            , coord.y);
		neighborCoord[3] = new Coord(coord.x+1            , coord.y);
		neighborCoord[4] = new Coord(coord.x-(direction^1), coord.y+1);
		neighborCoord[5] = new Coord(coord.x+ direction   , coord.y+1);

		return neighborCoord;
	}

	protected override void CalcRegion() {
		AttrHexagon1 attr = Attr;
		double a = attr.A;
		double b = attr.B;

		double oX = (coord.x+1)*b;                 // offset X
		double oY = (coord.y+(direction^1))*a*1.5; // offset Y

		switch (direction) {
		case 0:
			region.SetPoint(0, (int)(oX      ), (int)(oY - a  ));
			region.SetPoint(1, (int)(oX      ), (int)(oY      ));
			region.SetPoint(2, (int)(oX - b/2), (int)(oY + a/2));
			region.SetPoint(3, (int)(oX - b  ), (int)(oY      ));
			region.SetPoint(4, (int)(oX - b  ), (int)(oY - a  ));
			region.SetPoint(5, (int)(oX - b/2), (int)(oY - a*1.5));
			break;
		case 1:
			region.SetPoint(0, (int)(oX + b/2), (int)(oY + a/2  ));
			region.SetPoint(1, (int)(oX + b/2), (int)(oY + a*1.5));
			region.SetPoint(2, (int)(oX      ), (int)(oY + a*2  ));
			region.SetPoint(3, (int)(oX - b/2), (int)(oY + a*1.5));
			region.SetPoint(4, (int)(oX - b/2), (int)(oY + a/2  ));
			region.SetPoint(5, (int)(oX      ), (int)(oY        ));
			break;
		}
	}

	public override Rect getRcInner(int borderWidth) {
		AttrHexagon1 attr = Attr;
		double a = attr.A;
		double b = attr.B;
		double sq = attr.GetSq(borderWidth);

		double oX = (coord.x+1)*b;      // offset X
		double oY = (coord.y+1-direction)*a*1.5; // offset Y

		PointDouble center = new PointDouble(); // координата вписанного в фигуру квадрата (не совпадает с центром фигуры)
		switch (direction) {
		case 0: center.X = oX - b/2; center.Y = oY - a/2; break;
		case 1: center.X = oX;       center.Y = oY + a;   break;
		}

		Rect square = new Rect();
		square.X = (int) (center.X - sq/2);
		square.Y = (int) (center.Y - sq/2);
		square.Width =
		square.Height = (int) sq;
		return square;
	}

	public override int getShiftPointBorderIndex() { return 3; }
}
}
