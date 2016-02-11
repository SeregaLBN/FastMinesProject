////////////////////////////////////////////////////////////////////////////////
//                               FastMines project
//                                      © Sergey Krivulya (KSerg, aka SeregaLBN)
// file name: "Rhombus1.java"
//
// Реализация класса Rhombus1 - 3 ромба, составляющие равносторонний шестиугольник
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

/// <summary> Rhombus1 - 3 ромба, составляющие равносторонний шестиугольник </summary>
public class Rhombus1 : BaseCell {
	public class AttrRhombus1 : BaseAttribute {
		public AttrRhombus1(int area)
			: base(area)
      {}

		public override Size GetOwnerSize(Matrisize sizeField) {
			double a = A;
			double r = R;
			double c = C;
			Size result = new Size(
					(int)(c+a   *((sizeField.m+2)/3) +
					       (a+c)*((sizeField.m+1)/3) +
					          c *((sizeField.m+0)/3)),
					(int)(    r * (sizeField.n+1)));

			if (sizeField.m == 1)
				result.height -= (int)r;
			if (sizeField.n == 1)
				switch (sizeField.m % 3) {
				case 0: result.width -= (int)(a/2); break;
				case 2: result.width -= (int)a; break;
				}

			return result;
		}

      public override int getNeighborNumber(bool max) { return 10; }
		public override int getNeighborNumber(int direction) { return 10; }
		public override int getVertexNumber(int direction) { return 4; }
		public override double getVertexIntersection() { return 4.5; } // (3+3+6+6)/4.
		public override Size GetDirectionSizeField() { return new Size(3, 2); }
		public override double A => Math.Sqrt(Area*2/SQRT3);
		public double C => A / 2;
		public double H => A * SQRT3;
		public double R => H / 2;
		public override double GetSq(int borderWidth) {
			double w = borderWidth/2.0;
			return (A*SQRT3 - w*4)/(SQRT3+1);
		}

		public override int getMaxBackgroundFillModeValue() {
			return base.getMaxBackgroundFillModeValue()+1;
		}
	}

	public Rhombus1(AttrRhombus1 attr, Coord coord)
		: base(attr, coord,
				(coord.y&1)*3+(coord.x%3) // 0..5
			)
	{}

	private new AttrRhombus1 Attr {
		get { return (AttrRhombus1) base.Attr; }
	}

	protected override Coord?[] GetCoordsNeighbor() {
      var neighborCoord = new Coord?[Attr.getNeighborNumber(true)];

		// определяю координаты соседей
    	switch (direction) {
    	case 0:
    		neighborCoord[ 0] = new Coord(coord.x+1, coord.y-2);
    		neighborCoord[ 1] = new Coord(coord.x+2, coord.y-2);
    		neighborCoord[ 2] = new Coord(coord.x-2, coord.y-1);
    		neighborCoord[ 3] = new Coord(coord.x-1, coord.y-1);
    		neighborCoord[ 4] = new Coord(coord.x  , coord.y-1);
    		neighborCoord[ 5] = new Coord(coord.x+1, coord.y-1);
    		neighborCoord[ 6] = new Coord(coord.x-1, coord.y  );
    		neighborCoord[ 7] = new Coord(coord.x+1, coord.y  );
    		neighborCoord[ 8] = new Coord(coord.x-1, coord.y+1);
    		neighborCoord[ 9] = new Coord(coord.x  , coord.y+1);
    		break;
    	case 1:
    		neighborCoord[ 0] = new Coord(coord.x  , coord.y-2);
    		neighborCoord[ 1] = new Coord(coord.x+1, coord.y-2);
    		neighborCoord[ 2] = new Coord(coord.x-1, coord.y-1);
    		neighborCoord[ 3] = new Coord(coord.x  , coord.y-1);
    		neighborCoord[ 4] = new Coord(coord.x-1, coord.y  );
    		neighborCoord[ 5] = new Coord(coord.x+1, coord.y  );
    		neighborCoord[ 6] = new Coord(coord.x-1, coord.y+1);
    		neighborCoord[ 7] = new Coord(coord.x  , coord.y+1);
    		neighborCoord[ 8] = new Coord(coord.x-1, coord.y+2);
    		neighborCoord[ 9] = new Coord(coord.x  , coord.y+2);
    		break;
    	case 2:
    		neighborCoord[ 0] = new Coord(coord.x-1, coord.y-1);
    		neighborCoord[ 1] = new Coord(coord.x  , coord.y-1);
    		neighborCoord[ 2] = new Coord(coord.x-1, coord.y  );
    		neighborCoord[ 3] = new Coord(coord.x+1, coord.y  );
    		neighborCoord[ 4] = new Coord(coord.x-2, coord.y+1);
    		neighborCoord[ 5] = new Coord(coord.x-1, coord.y+1);
    		neighborCoord[ 6] = new Coord(coord.x  , coord.y+1);
    		neighborCoord[ 7] = new Coord(coord.x+1, coord.y+1);
    		neighborCoord[ 8] = new Coord(coord.x-2, coord.y+2);
    		neighborCoord[ 9] = new Coord(coord.x-1, coord.y+2);
    		break;
    	case 3:
    		neighborCoord[ 0] = new Coord(coord.x-2, coord.y-2);
    		neighborCoord[ 1] = new Coord(coord.x-1, coord.y-2);
    		neighborCoord[ 2] = new Coord(coord.x-1, coord.y-1);
    		neighborCoord[ 3] = new Coord(coord.x  , coord.y-1);
    		neighborCoord[ 4] = new Coord(coord.x+1, coord.y-1);
    		neighborCoord[ 5] = new Coord(coord.x+2, coord.y-1);
    		neighborCoord[ 6] = new Coord(coord.x-1, coord.y  );
    		neighborCoord[ 7] = new Coord(coord.x+1, coord.y  );
    		neighborCoord[ 8] = new Coord(coord.x  , coord.y+1);
    		neighborCoord[ 9] = new Coord(coord.x+1, coord.y+1);
    		break;
    	case 4:
    		neighborCoord[ 0] = new Coord(coord.x  , coord.y-1);
    		neighborCoord[ 1] = new Coord(coord.x+1, coord.y-1);
    		neighborCoord[ 2] = new Coord(coord.x-1, coord.y  );
    		neighborCoord[ 3] = new Coord(coord.x+1, coord.y  );
    		neighborCoord[ 4] = new Coord(coord.x-1, coord.y+1);
    		neighborCoord[ 5] = new Coord(coord.x  , coord.y+1);
    		neighborCoord[ 6] = new Coord(coord.x+1, coord.y+1);
    		neighborCoord[ 7] = new Coord(coord.x+2, coord.y+1);
    		neighborCoord[ 8] = new Coord(coord.x+1, coord.y+2);
    		neighborCoord[ 9] = new Coord(coord.x+2, coord.y+2);
    		break;
    	case 5:
    		neighborCoord[ 0] = new Coord(coord.x-1, coord.y-2);
    		neighborCoord[ 1] = new Coord(coord.x  , coord.y-2);
    		neighborCoord[ 2] = new Coord(coord.x  , coord.y-1);
    		neighborCoord[ 3] = new Coord(coord.x+1, coord.y-1);
    		neighborCoord[ 4] = new Coord(coord.x-1, coord.y  );
    		neighborCoord[ 5] = new Coord(coord.x+1, coord.y  );
    		neighborCoord[ 6] = new Coord(coord.x  , coord.y+1);
    		neighborCoord[ 7] = new Coord(coord.x+1, coord.y+1);
    		neighborCoord[ 8] = new Coord(coord.x  , coord.y+2);
    		neighborCoord[ 9] = new Coord(coord.x+1, coord.y+2);
    		break;
    	}

		return neighborCoord;
	}

	protected override void CalcRegion() {
		AttrRhombus1 attr = Attr;
		double a = attr.A;
		double c = attr.C;
		double h = attr.H;
		double r = attr.R;

		// определение координат точек фигуры
		double oX = a*(coord.x/3*3+1)+c; // offset X
		double oY = h*(coord.y/2)    +h; // offset Y

		switch (direction) {
		case 0:
			region.SetPoint(0, (int)(oX      ), (int)(oY - h));
			region.SetPoint(1, (int)(oX - c  ), (int)(oY - r));
			region.SetPoint(2, (int)(oX - a-c), (int)(oY - r));
			region.SetPoint(3, (int)(oX - a  ), (int)(oY - h));
			break;
		case 1:
			region.SetPoint(0, (int)(oX      ), (int)(oY - h));
			region.SetPoint(1, (int)(oX + c  ), (int)(oY - r));
			region.SetPoint(2, (int)(oX      ), (int)(oY    ));
			region.SetPoint(3, (int)(oX - c  ), (int)(oY - r));
			break;
		case 2:
			region.SetPoint(0, (int)(oX + a+c), (int)(oY - r));
			region.SetPoint(1, (int)(oX + a  ), (int)(oY    ));
			region.SetPoint(2, (int)(oX      ), (int)(oY    ));
			region.SetPoint(3, (int)(oX + c  ), (int)(oY - r));
			break;
		case 3:
			region.SetPoint(0, (int)(oX - c  ), (int)(oY - r));
			region.SetPoint(1, (int)(oX      ), (int)(oY    ));
			region.SetPoint(2, (int)(oX - a  ), (int)(oY    ));
			region.SetPoint(3, (int)(oX - a-c), (int)(oY - r));
			break;
		case 4:
			region.SetPoint(0, (int)(oX + a  ), (int)(oY    ));
			region.SetPoint(1, (int)(oX + a+c), (int)(oY + r));
			region.SetPoint(2, (int)(oX + c  ), (int)(oY + r));
			region.SetPoint(3, (int)(oX      ), (int)(oY    ));
			break;
		case 5:
			region.SetPoint(0, (int)(oX + a+c), (int)(oY - r));
			region.SetPoint(1, (int)(oX + a+a), (int)(oY    ));
			region.SetPoint(2, (int)(oX + a+c), (int)(oY + r));
			region.SetPoint(3, (int)(oX + a  ), (int)(oY    ));
			break;
		}
	}

	public override Rect getRcInner(int borderWidth) {
		AttrRhombus1 attr = Attr;
		double a = attr.A;
		double c = attr.C;
		double h = attr.H;
		double r = attr.R;
//		double w = borderWidth/2.0;
		double sq  = attr.GetSq(borderWidth);
		double sq2 = sq/2;

		double oX = a*(coord.x/3*3+1)+c; // offset X
		double oY = h*(coord.y/2)    +h; // offset Y

		PointDouble center = new PointDouble(); // координата центра квадрата
		switch (direction) {
		case 0: center.X = oX - c*1.5; center.Y = oY - r*1.5; break;
		case 1: center.X = oX;         center.Y = oY - r;     break;
		case 2: center.X = oX + c*1.5; center.Y = oY - r*0.5; break;
		case 3: center.X = oX - c*1.5; center.Y = oY - r*0.5; break;
		case 4: center.X = oX + c*1.5; center.Y = oY + r*0.5; break;
		case 5: center.X = oX + a+c;   center.Y = oY;         break;
		}

		Rect square = new Rect();
		square.X = (int) (center.X - sq2);
		square.Y = (int) (center.Y - sq2);
		square.Width =
		square.Height = (int) sq;
		return square;
	}

	public override int getShiftPointBorderIndex() { return 2; }

	public override Color getBackgroundFillColor(int fillMode, Color defaultColor, Func<int, Color> repositoryColor) {
		if (fillMode == Attr.getMaxBackgroundFillModeValue()) {
			switch ((getCoord().y%4)*3+(getCoord().x%3)) { // почти как вычисление direction...
			// подсвечиваю 4 группы, составляющие каждая шестигранник из 3х ромбов
			case 0: case  1: case  3: return repositoryColor(0);
			case 2: case  4: case  5: return repositoryColor(1);
			case 6: case  7: case  9: return repositoryColor(2);
			case 8: case 10: case 11: return repositoryColor(3);
			}
		}
		return base.getBackgroundFillColor(fillMode, defaultColor, repositoryColor);
	}
}
}