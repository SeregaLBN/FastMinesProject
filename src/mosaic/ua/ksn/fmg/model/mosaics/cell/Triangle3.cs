////////////////////////////////////////////////////////////////////////////////
//                               FastMines project
//                                      © Sergey Krivulya (KSerg, aka SeregaLBN)
// file name: "Triangle3.java"
//
// Реализация класса Triangle3 - треугольник 45°-90°-45°
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

/// <summary> Треугольник. Вариант 3 - треугольник 45°-90°-45°(квадрат разделённый на 4 части) </summary>
public class Triangle3 : BaseCell {
	public class AttrTriangle3 : BaseAttribute {
		public AttrTriangle3(int area)
			: base(area)
      {}

		public override Size CalcOwnerSize(Size sizeField, int area) {
			double a = CalcA(area);
			Size result = new Size(
					(int)(a * ((sizeField.width+1)>>1)),
					(int)(a * ((sizeField.height+1)>>1)));

			if (sizeField.width == 1)
				if ((sizeField.height & 1) == 1)
					result.height -= (int)(a*0.5);

			return result;
		}

      public override int getNeighborNumber(bool max) { return 14; }
		public override int getNeighborNumber(int direction) { return 14; }
		public override int getVertexNumber(int direction) { return 3; }
		public override double getVertexIntersection() { return 6.6666666666666666666666666666667; } // (8+8+4)/3.
		public override Size GetDirectionSizeField() { return new Size(2, 2); }
		public override double CalcA(int area) { return 2*CalcB(area); }
		/// <summary> пол стороны треугольника </summary>
		public double CalcB(int area) { return Math.Sqrt(area); }
		public override double CalcSq(int area, int borderWidth) {
			double w = borderWidth/2.0;
			return (CalcA(area) - w*2 / TAN45_2 ) / 3; 
		}
	}

	public Triangle3(AttrTriangle3 attr, Coord coord)
		: base(attr, coord,
				((coord.y&1)<<1)+(coord.x&1) // 0..3
			)
	{}

	private AttrTriangle3 Attr {
		get { return (AttrTriangle3) base.Attr; }
	}

	protected override Coord?[] GetCoordsNeighbor() {
		var neighborCoord = new Coord?[Attr.getNeighborNumber(true)];

		// определяю координаты соседей
    	switch (direction) {
    	case 0:
    		neighborCoord[ 0] = new Coord(coord.x-1, coord.y-2);
    		neighborCoord[ 1] = new Coord(coord.x+1, coord.y-2);
    		neighborCoord[ 2] = new Coord(coord.x-1, coord.y-1);
    		neighborCoord[ 3] = new Coord(coord.x  , coord.y-1);
    		neighborCoord[ 4] = new Coord(coord.x+1, coord.y-1);
    		neighborCoord[ 5] = new Coord(coord.x+2, coord.y-1);
    		neighborCoord[ 6] = new Coord(coord.x+3, coord.y-1);
    		neighborCoord[ 7] = new Coord(coord.x-2, coord.y  );
    		neighborCoord[ 8] = new Coord(coord.x-1, coord.y  );
    		neighborCoord[ 9] = new Coord(coord.x+1, coord.y  );
    		neighborCoord[10] = new Coord(coord.x+2, coord.y  );
    		neighborCoord[11] = new Coord(coord.x  , coord.y+1);
    		neighborCoord[12] = new Coord(coord.x+1, coord.y+1);
    		neighborCoord[13] = new Coord(coord.x+2, coord.y+1);
    		break;
    	case 1:
    		neighborCoord[ 0] = new Coord(coord.x  , coord.y-2);
    		neighborCoord[ 1] = new Coord(coord.x  , coord.y-1);
    		neighborCoord[ 2] = new Coord(coord.x+1, coord.y-1);
    		neighborCoord[ 3] = new Coord(coord.x+2, coord.y-1);
    		neighborCoord[ 4] = new Coord(coord.x-1, coord.y  );
    		neighborCoord[ 5] = new Coord(coord.x+1, coord.y  );
    		neighborCoord[ 6] = new Coord(coord.x-1, coord.y+1);
    		neighborCoord[ 7] = new Coord(coord.x  , coord.y+1);
    		neighborCoord[ 8] = new Coord(coord.x+1, coord.y+1);
    		neighborCoord[ 9] = new Coord(coord.x+2, coord.y+1);
    		neighborCoord[10] = new Coord(coord.x-1, coord.y+2);
    		neighborCoord[11] = new Coord(coord.x  , coord.y+2);
    		neighborCoord[12] = new Coord(coord.x+1, coord.y+2);
    		neighborCoord[13] = new Coord(coord.x+1, coord.y+3);
    		break;
    	case 2:
    		neighborCoord[ 0] = new Coord(coord.x-1, coord.y-3);
    		neighborCoord[ 1] = new Coord(coord.x-1, coord.y-2);
    		neighborCoord[ 2] = new Coord(coord.x  , coord.y-2);
    		neighborCoord[ 3] = new Coord(coord.x+1, coord.y-2);
    		neighborCoord[ 4] = new Coord(coord.x-2, coord.y-1);
    		neighborCoord[ 5] = new Coord(coord.x-1, coord.y-1);
    		neighborCoord[ 6] = new Coord(coord.x  , coord.y-1);
    		neighborCoord[ 7] = new Coord(coord.x+1, coord.y-1);
    		neighborCoord[ 8] = new Coord(coord.x-1, coord.y  );
    		neighborCoord[ 9] = new Coord(coord.x+1, coord.y  );
    		neighborCoord[10] = new Coord(coord.x-2, coord.y+1);
    		neighborCoord[11] = new Coord(coord.x-1, coord.y+1);
    		neighborCoord[12] = new Coord(coord.x  , coord.y+1);
    		neighborCoord[13] = new Coord(coord.x  , coord.y+2);
    		break;
    	case 3:
    		neighborCoord[ 0] = new Coord(coord.x-2, coord.y-1);
    		neighborCoord[ 1] = new Coord(coord.x-1, coord.y-1);
    		neighborCoord[ 2] = new Coord(coord.x  , coord.y-1);
    		neighborCoord[ 3] = new Coord(coord.x-2, coord.y  );
    		neighborCoord[ 4] = new Coord(coord.x-1, coord.y  );
    		neighborCoord[ 5] = new Coord(coord.x+1, coord.y  );
    		neighborCoord[ 6] = new Coord(coord.x+2, coord.y  );
    		neighborCoord[ 7] = new Coord(coord.x-3, coord.y+1);
    		neighborCoord[ 8] = new Coord(coord.x-2, coord.y+1);
    		neighborCoord[ 9] = new Coord(coord.x-1, coord.y+1);
    		neighborCoord[10] = new Coord(coord.x  , coord.y+1);
    		neighborCoord[11] = new Coord(coord.x+1, coord.y+1);
    		neighborCoord[12] = new Coord(coord.x-1, coord.y+2);
    		neighborCoord[13] = new Coord(coord.x+1, coord.y+2);
    		break;
    	}

		return neighborCoord;
	}

	protected override void CalcRegion() {
		AttrTriangle3 attr = Attr;
		int area = attr.Area;
		double a = attr.CalcA(area);
		double b = attr.CalcB(area);

		double oX = a*(coord.x>>1); // offset X
		double oY = a*(coord.y>>1); // offset Y

		switch (direction) {
		case 0:
			region.setPoint(0, (int)(oX + a), (int)(oY    ));
			region.setPoint(2, (int)(oX    ), (int)(oY    ));
			region.setPoint(1, (int)(oX + b), (int)(oY + b));
			break;
		case 1:
			region.setPoint(0, (int)(oX + a), (int)(oY    ));
			region.setPoint(2, (int)(oX + b), (int)(oY + b));
			region.setPoint(1, (int)(oX + a), (int)(oY + a));
			break;
		case 2:
			region.setPoint(2, (int)(oX    ), (int)(oY + a));
			region.setPoint(1, (int)(oX + b), (int)(oY + b));
			region.setPoint(0, (int)(oX    ), (int)(oY    ));
			break;
		case 3:
			region.setPoint(2, (int)(oX    ), (int)(oY + a));
			region.setPoint(1, (int)(oX + a), (int)(oY + a));
			region.setPoint(0, (int)(oX + b), (int)(oY + b));
			break;
		}
	}

	public override Rect getRcInner(int borderWidth) {
		AttrTriangle3 attr = Attr;
		int area = attr.Area;
		double sq = attr.CalcSq(area, borderWidth);
		double w = borderWidth/2.0;

		PointDouble center = new PointDouble(); // координата вписанного в фигуру квадрата (не совпадает с центром фигуры)
		switch (direction) {
		case 0:
			center.x = region.getPoint(1).x;
			center.y = region.getPoint(0).y + sq/2 + w;
			break;
		case 1:
			center.x = region.getPoint(0).x - sq/2 - w;
			center.y = region.getPoint(2).y;
			break;
		case 2:
			center.x = region.getPoint(0).x + sq/2 + w;
			center.y = region.getPoint(1).y;
			break;
		case 3:
			center.x = region.getPoint(0).x;
			center.y = region.getPoint(1).y - sq/2 - w;
			break;
		}

		Rect square = new Rect();
		square.x = (int) (center.x - sq/2);
		square.y = (int) (center.y - sq/2);
		square.width =
		square.height = (int) sq;
		return square;
	}

	public override int getShiftPointBorderIndex() {
		switch (direction) {
		case 0: case 1: return 1;
		}
		return 2;
	}
}
}