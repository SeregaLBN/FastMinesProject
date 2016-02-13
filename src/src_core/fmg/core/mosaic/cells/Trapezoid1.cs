////////////////////////////////////////////////////////////////////////////////
//                               FastMines project
//                                      © Sergey Krivulya (KSerg, aka SeregaLBN)
// file name: "Trapezoid1.java"
//
// Реализация класса Trapezoid1 - 3 трапеции, составляющие равносторонний треугольник
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

/// <summary> Trapezoid1 - 3 трапеции, составляющие равносторонний треугольник </summary>
public class Trapezoid1 : BaseCell {
	public class AttrTrapezoid1 : BaseAttribute {
		public AttrTrapezoid1(double area)
			: base(area)
      {}

		public override SizeDouble GetOwnerSize(Matrisize sizeField) {
			var a = A;
			var c = C;
			var r = RIn;
			var R = ROut;
			var result = new SizeDouble(
					c + a *  (sizeField.m+1),
					R     * ((sizeField.n+1)/2.0) +
					r     * ((sizeField.n+0)/2.0));

			if (sizeField.n < 4)
				if ((sizeField.m % 3) != 0)
					result.Width -= c;

			return result;
		}

      public override int getNeighborNumber(bool max) { return 8; }
		public override int getNeighborNumber(int direction) { return 8; }
		public override int getVertexNumber(int direction) { return 4; }
		public override double getVertexIntersection() { return 3.6; } // (3+3+3+3+6)/5.
		public override Size GetDirectionSizeField() { return new Size(3, 4); }
		public override double A => Math.Sqrt(Area/SQRT27)*2;
		public double B => A * 2;
		public double C => A / 2;
		public double ROut => A * SQRT3;
		public double RIn => ROut / 2;
		public override double GetSq(int borderWidth) {
			var w = borderWidth/2.0;
			return (A*SQRT3 - w*4)/(SQRT3+1);
		}
	}

	public Trapezoid1(AttrTrapezoid1 attr, Coord coord)
		: base(attr, coord,
				(coord.y&3)*3+(coord.x%3) // 0..11
			)
	{}

	private new AttrTrapezoid1 Attr => (AttrTrapezoid1) base.Attr;

   protected override Coord?[] GetCoordsNeighbor() {
      var neighborCoord = new Coord?[Attr.getNeighborNumber(true)];

		// определяю координаты соседей
    	switch (direction) {
    	case 0:
    		neighborCoord[ 0] = new Coord(coord.x-1, coord.y-1);
    		neighborCoord[ 1] = new Coord(coord.x  , coord.y-1);
    		neighborCoord[ 2] = new Coord(coord.x+1, coord.y-1);
    		neighborCoord[ 3] = new Coord(coord.x-1, coord.y  );
    		neighborCoord[ 4] = new Coord(coord.x+1, coord.y  );
    		neighborCoord[ 5] = new Coord(coord.x-1, coord.y+1);
    		neighborCoord[ 6] = new Coord(coord.x  , coord.y+1);
    		neighborCoord[ 7] = new Coord(coord.x+1, coord.y+1);
    		break;
    	case 1:
    		neighborCoord[ 0] = new Coord(coord.x-2, coord.y-1);
    		neighborCoord[ 1] = new Coord(coord.x-1, coord.y-1);
    		neighborCoord[ 2] = new Coord(coord.x  , coord.y-1);
    		neighborCoord[ 3] = new Coord(coord.x-2, coord.y  );
    		neighborCoord[ 4] = new Coord(coord.x-1, coord.y  );
    		neighborCoord[ 5] = new Coord(coord.x+1, coord.y  );
    		neighborCoord[ 6] = new Coord(coord.x  , coord.y+1);
    		neighborCoord[ 7] = new Coord(coord.x+1, coord.y+1);
    		break;
    	case 2:
    		neighborCoord[ 0] = new Coord(coord.x-1, coord.y-1);
    		neighborCoord[ 1] = new Coord(coord.x  , coord.y-1);
    		neighborCoord[ 2] = new Coord(coord.x+1, coord.y-1);
    		neighborCoord[ 3] = new Coord(coord.x+2, coord.y-1);
    		neighborCoord[ 4] = new Coord(coord.x-1, coord.y  );
    		neighborCoord[ 5] = new Coord(coord.x+1, coord.y  );
    		neighborCoord[ 6] = new Coord(coord.x+2, coord.y  );
    		neighborCoord[ 7] = new Coord(coord.x  , coord.y+1);
    		break;
    	case 3:
    		neighborCoord[ 0] = new Coord(coord.x  , coord.y-1);
    		neighborCoord[ 1] = new Coord(coord.x-2, coord.y  );
    		neighborCoord[ 2] = new Coord(coord.x-1, coord.y  );
    		neighborCoord[ 3] = new Coord(coord.x+1, coord.y  );
    		neighborCoord[ 4] = new Coord(coord.x-2, coord.y+1);
    		neighborCoord[ 5] = new Coord(coord.x-1, coord.y+1);
    		neighborCoord[ 6] = new Coord(coord.x  , coord.y+1);
    		neighborCoord[ 7] = new Coord(coord.x+1, coord.y+1);
    		break;
    	case 4:
    		neighborCoord[ 0] = new Coord(coord.x-1, coord.y-1);
    		neighborCoord[ 1] = new Coord(coord.x  , coord.y-1);
    		neighborCoord[ 2] = new Coord(coord.x-1, coord.y  );
    		neighborCoord[ 3] = new Coord(coord.x+1, coord.y  );
    		neighborCoord[ 4] = new Coord(coord.x+2, coord.y  );
    		neighborCoord[ 5] = new Coord(coord.x  , coord.y+1);
    		neighborCoord[ 6] = new Coord(coord.x+1, coord.y+1);
    		neighborCoord[ 7] = new Coord(coord.x+2, coord.y+1);
    		break;
    	case 5:
    		neighborCoord[ 0] = new Coord(coord.x-1, coord.y-1);
    		neighborCoord[ 1] = new Coord(coord.x  , coord.y-1);
    		neighborCoord[ 2] = new Coord(coord.x+1, coord.y-1);
    		neighborCoord[ 3] = new Coord(coord.x-1, coord.y  );
    		neighborCoord[ 4] = new Coord(coord.x+1, coord.y  );
    		neighborCoord[ 5] = new Coord(coord.x-1, coord.y+1);
    		neighborCoord[ 6] = new Coord(coord.x  , coord.y+1);
    		neighborCoord[ 7] = new Coord(coord.x+1, coord.y+1);
    		break;
    	case 6:
    		neighborCoord[ 0] = new Coord(coord.x-2, coord.y-1);
    		neighborCoord[ 1] = new Coord(coord.x-1, coord.y-1);
    		neighborCoord[ 2] = new Coord(coord.x  , coord.y-1);
    		neighborCoord[ 3] = new Coord(coord.x-2, coord.y  );
    		neighborCoord[ 4] = new Coord(coord.x-1, coord.y  );
    		neighborCoord[ 5] = new Coord(coord.x+1, coord.y  );
    		neighborCoord[ 6] = new Coord(coord.x-1, coord.y+1);
    		neighborCoord[ 7] = new Coord(coord.x  , coord.y+1);
    		break;
    	case 7:
    		neighborCoord[ 0] = new Coord(coord.x-1, coord.y-1);
    		neighborCoord[ 1] = new Coord(coord.x  , coord.y-1);
    		neighborCoord[ 2] = new Coord(coord.x+1, coord.y-1);
    		neighborCoord[ 3] = new Coord(coord.x+2, coord.y-1);
    		neighborCoord[ 4] = new Coord(coord.x-1, coord.y  );
    		neighborCoord[ 5] = new Coord(coord.x+1, coord.y  );
    		neighborCoord[ 6] = new Coord(coord.x+2, coord.y  );
    		neighborCoord[ 7] = new Coord(coord.x-1, coord.y+1);
    		break;
    	case 8:
    		neighborCoord[ 0] = new Coord(coord.x-1, coord.y-1);
    		neighborCoord[ 1] = new Coord(coord.x  , coord.y-1);
    		neighborCoord[ 2] = new Coord(coord.x+1, coord.y-1);
    		neighborCoord[ 3] = new Coord(coord.x-1, coord.y  );
    		neighborCoord[ 4] = new Coord(coord.x+1, coord.y  );
    		neighborCoord[ 5] = new Coord(coord.x-2, coord.y+1);
    		neighborCoord[ 6] = new Coord(coord.x-1, coord.y+1);
    		neighborCoord[ 7] = new Coord(coord.x  , coord.y+1);
    		break;
    	case 9:
    		neighborCoord[ 0] = new Coord(coord.x  , coord.y-1);
    		neighborCoord[ 1] = new Coord(coord.x+1, coord.y-1);
    		neighborCoord[ 2] = new Coord(coord.x+2, coord.y-1);
    		neighborCoord[ 3] = new Coord(coord.x-1, coord.y  );
    		neighborCoord[ 4] = new Coord(coord.x+1, coord.y  );
    		neighborCoord[ 5] = new Coord(coord.x-1, coord.y+1);
    		neighborCoord[ 6] = new Coord(coord.x  , coord.y+1);
    		neighborCoord[ 7] = new Coord(coord.x+1, coord.y+1);
    		break;
    	case 10:
    		neighborCoord[ 0] = new Coord(coord.x+1, coord.y-1);
    		neighborCoord[ 1] = new Coord(coord.x-2, coord.y  );
    		neighborCoord[ 2] = new Coord(coord.x-1, coord.y  );
    		neighborCoord[ 3] = new Coord(coord.x+1, coord.y  );
    		neighborCoord[ 4] = new Coord(coord.x-2, coord.y+1);
    		neighborCoord[ 5] = new Coord(coord.x-1, coord.y+1);
    		neighborCoord[ 6] = new Coord(coord.x  , coord.y+1);
    		neighborCoord[ 7] = new Coord(coord.x+1, coord.y+1);
    		break;
    	case 11:
    		neighborCoord[ 0] = new Coord(coord.x  , coord.y-1);
    		neighborCoord[ 1] = new Coord(coord.x+1, coord.y-1);
    		neighborCoord[ 2] = new Coord(coord.x-1, coord.y  );
    		neighborCoord[ 3] = new Coord(coord.x+1, coord.y  );
    		neighborCoord[ 4] = new Coord(coord.x+2, coord.y  );
    		neighborCoord[ 5] = new Coord(coord.x  , coord.y+1);
    		neighborCoord[ 6] = new Coord(coord.x+1, coord.y+1);
    		neighborCoord[ 7] = new Coord(coord.x+2, coord.y+1);
    		break;
    	}

		return neighborCoord;
	}

	protected override void CalcRegion() {
		var attr = Attr;
		var a = attr.A;
		var b = attr.B;
		var c = attr.C;
		var R = attr.ROut;
		var r = attr.RIn;

		// определение координат точек фигуры
		var oX = (a+b)*(coord.x/3.0) + b; // offset X
		var oY = (R+r)*(coord.y/4.0*2+1); // offset Y

		switch (direction) {
		case 0:
			region.SetPoint(0, oX - c  , oY - R-r);
			region.SetPoint(1, oX      , oY - R  );
			region.SetPoint(2, oX - c  , oY - r  );
			region.SetPoint(3, oX - c-a, oY - r  );
			break;
		case 1:
			region.SetPoint(0, oX + c  , oY - R-r);
			region.SetPoint(1, oX + a  , oY - R  );
			region.SetPoint(2, oX + c  , oY - r  );
			region.SetPoint(3, oX - c  , oY - R-r);
			break;
		case 2:
			region.SetPoint(0, oX + b+c, oY - R-r);
			region.SetPoint(1, oX + b  , oY - R  );
			region.SetPoint(2, oX + a  , oY - R  );
			region.SetPoint(3, oX + c  , oY - R-r);
			break;
		case 3:
			region.SetPoint(0, oX - c  , oY - r  );
			region.SetPoint(1, oX      , oY      );
			region.SetPoint(2, oX - b  , oY      );
			region.SetPoint(3, oX - a-c, oY - r  );
			break;
		case 4:
			region.SetPoint(0, oX      , oY - R  );
			region.SetPoint(1, oX + a  , oY      );
			region.SetPoint(2, oX      , oY      );
			region.SetPoint(3, oX - c  , oY - r  );
			break;
		case 5:
			region.SetPoint(0, oX + b  , oY - R  );
			region.SetPoint(1, oX + a  , oY      );
			region.SetPoint(2, oX + c  , oY - r  );
			region.SetPoint(3, oX + a  , oY - R  );
			break;
		case 6:
			region.SetPoint(0, oX - a  , oY      );
			region.SetPoint(1, oX - c  , oY + r  );
			region.SetPoint(2, oX - a  , oY + R  );
			region.SetPoint(3, oX - b  , oY      );
			break;
		case 7:
			region.SetPoint(0, oX + a  , oY      );
			region.SetPoint(1, oX + c  , oY + r  );
			region.SetPoint(2, oX - c  , oY + r  );
			region.SetPoint(3, oX - a  , oY      );
			break;
		case 8:
			region.SetPoint(0, oX + a  , oY      );
			region.SetPoint(1, oX + a+c, oY + r  );
			region.SetPoint(2, oX + a  , oY + R  );
			region.SetPoint(3, oX      , oY + R  );
			break;
		case 9:
			region.SetPoint(0, oX + c  , oY + r  );
			region.SetPoint(1, oX - c  , oY + R+r);
			region.SetPoint(2, oX - a  , oY + R  );
			region.SetPoint(3, oX - c  , oY + r  );
			break;
		case 10:
			region.SetPoint(0, oX + a  , oY + R  );
			region.SetPoint(1, oX + a+c, oY + R+r);
			region.SetPoint(2, oX - c  , oY + R+r);
			region.SetPoint(3, oX      , oY + R  );
			break;
		case 11:
			region.SetPoint(0, oX + a+c, oY + r  );
			region.SetPoint(1, oX + b+c, oY + R+r);
			region.SetPoint(2, oX + a+c, oY + R+r);
			region.SetPoint(3, oX + a  , oY + R  );
			break;
		}
	}

	public override RectDouble getRcInner(int borderWidth) {
		var attr = Attr;
		var a = attr.A;
		var b = attr.B;
		var c = attr.C;
		var R = attr.ROut;
		var r = attr.RIn;
//		var w = borderWidth/2.0;
		var sq  = attr.GetSq(borderWidth);
		var sq2 = sq/2;

		var oX = (a+b)*(coord.x/3.0) + b; // offset X
		var oY = (R+r)*(coord.y/4.0*2+1); // offset Y

		var center = new PointDouble(); // координата центра квадрата
		switch (direction) {
		case 0:  center.X = oX - c*1.25; center.Y = oY - r*1.75; break;
		case 1:  center.X = oX + c*0.75; center.Y = oY - r*2.25; break;
		case 2:  center.X = oX + c*3;    center.Y = oY - r*2.50; break;
		case 3:  center.X = oX - a;      center.Y = oY - r*0.50; break;
		case 4:  center.X = oX + c*0.25; center.Y = oY - r*0.75; break;
		case 5:  center.X = oX + c*2.25; center.Y = oY - r*1.25; break;
		case 6:  center.X = oX - c*2.25; center.Y = oY + r*0.75; break;
		case 7:  center.X = oX;          center.Y = oY + r*0.50; break;
		case 8:  center.X = oX + c*1.75; center.Y = oY + r*1.25; break;
		case 9:  center.X = oX - c*0.75; center.Y = oY + r*1.75; break;
		case 10: center.X = oX + c;      center.Y = oY + r*2.50; break;
		case 11: center.X = oX + c*3.25; center.Y = oY + r*2.25; break;
		}

		return new RectDouble(
		   center.X - sq2,
		   center.Y - sq2,
		   sq, sq);
	}

	public override int getShiftPointBorderIndex() {
		switch (direction) {
		case 0: case 8: return 3;
		case 5: case 9: return 1;
		}
		return 2;
	}
}
}