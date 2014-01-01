////////////////////////////////////////////////////////////////////////////////
//                               FastMines project
//                                      © Sergey Krivulya (KSerg, aka SeregaLBN)
// file name: "Trapezoid1.java"
//
// –еализаци€ класса Trapezoid1 - 3 трапеции, составл€ющие равносторонний треугольник
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

/// <summary> Trapezoid1 - 3 трапеции, составл€ющие равносторонний треугольник </summary>
public class Trapezoid1 : BaseCell {
	public class AttrTrapezoid1 : BaseAttribute {
		public AttrTrapezoid1(int area)
			: base(area)
      {}

		public override Size CalcOwnerSize(Size sizeField, int area) {
			double a = CalcA(area);
			double c = CalcC(area);
			double r = CalcRIn(area);
			double R = CalcROut(area);
			Size result = new Size(
					(int)( c + a *  (sizeField.width+1)),
					(int)( R     * ((sizeField.height+1)/2) +
					       r     * ((sizeField.height+0)/2)));

			if (sizeField.height < 4)
				if ((sizeField.width % 3) != 0)
					result.width -= (int)c;

			return result;
		}
	
		public override int getNeighborNumber() { return 8; }
		public override int getNeighborNumber(int direction) { return 8; }
		public override int getVertexNumber() { return 4; }
		public override int getVertexNumber(int direction) { return 4; }
		public override double getVertexIntersection() { return 3.6; } // (3+3+3+3+6)/5.
		public override Size GetDirectionSizeField() { return new Size(3, 4); }
		public override double CalcA   (int area) { return Math.Sqrt(area/SQRT27)*2; }
		public double CalcB   (int area) { return CalcA(area)*2; }
		public double CalcC   (int area) { return CalcA(area)/2; }
		public double CalcROut(int area) { return CalcA(area)*SQRT3; }
		public double CalcRIn (int area) { return CalcROut(area)/2; }
		public override double CalcSq(int area, int borderWidth) {
			double w = borderWidth/2.0;
			return (CalcA(area)*SQRT3 - w*4)/(SQRT3+1);
		}

		public override Size sizeIcoField(bool smallSize) {
			return new Size(2, smallSize ? 2 : 3);
		}
	}

	public Trapezoid1(AttrTrapezoid1 attr, Coord coord)
		: base(attr, coord,
				(coord.y&3)*3+(coord.x%3) // 0..11
			)
	{}

	private AttrTrapezoid1 Attr {
		get { return (AttrTrapezoid1) base.Attr; }
	}

	protected override Coord[] GetCoordsNeighbor() {
		Coord[] neighborCoord = new Coord[Attr.getNeighborNumber()];

		// определ€ю координаты соседей
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
		AttrTrapezoid1 attr = Attr;
		int area = attr.Area;
		double a = attr.CalcA(area);
		double b = attr.CalcB(area);
		double c = attr.CalcC(area);
		double R = attr.CalcROut(area);
		double r = attr.CalcRIn(area);

		// определение координат точек фигуры
		double oX = (a+b)*(coord.x/3) + b; // offset X
		double oY = (R+r)*(coord.y/4*2+1); // offset Y

		switch (direction) {
		case 0:
			region.setPoint(0, (int)(oX - c  ), (int)(oY - R-r));
			region.setPoint(1, (int)(oX      ), (int)(oY - R  ));
			region.setPoint(2, (int)(oX - c  ), (int)(oY - r  ));
			region.setPoint(3, (int)(oX - c-a), (int)(oY - r  ));
			break;
		case 1:
			region.setPoint(0, (int)(oX + c  ), (int)(oY - R-r));
			region.setPoint(1, (int)(oX + a  ), (int)(oY - R  ));
			region.setPoint(2, (int)(oX + c  ), (int)(oY - r  ));
			region.setPoint(3, (int)(oX - c  ), (int)(oY - R-r));
			break;
		case 2:
			region.setPoint(0, (int)(oX + b+c), (int)(oY - R-r));
			region.setPoint(1, (int)(oX + b  ), (int)(oY - R  ));
			region.setPoint(2, (int)(oX + a  ), (int)(oY - R  ));
			region.setPoint(3, (int)(oX + c  ), (int)(oY - R-r));
			break;
		case 3:
			region.setPoint(0, (int)(oX - c  ), (int)(oY - r  ));
			region.setPoint(1, (int)(oX      ), (int)(oY      ));
			region.setPoint(2, (int)(oX - b  ), (int)(oY      ));
			region.setPoint(3, (int)(oX - a-c), (int)(oY - r  ));
			break;
		case 4:
			region.setPoint(0, (int)(oX      ), (int)(oY - R  ));
			region.setPoint(1, (int)(oX + a  ), (int)(oY      ));
			region.setPoint(2, (int)(oX      ), (int)(oY      ));
			region.setPoint(3, (int)(oX - c  ), (int)(oY - r  ));
			break;
		case 5:
			region.setPoint(0, (int)(oX + b  ), (int)(oY - R  ));
			region.setPoint(1, (int)(oX + a  ), (int)(oY      ));
			region.setPoint(2, (int)(oX + c  ), (int)(oY - r  ));
			region.setPoint(3, (int)(oX + a  ), (int)(oY - R  ));
			break;
		case 6:
			region.setPoint(0, (int)(oX - a  ), (int)(oY      ));
			region.setPoint(1, (int)(oX - c  ), (int)(oY + r  ));
			region.setPoint(2, (int)(oX - a  ), (int)(oY + R  ));
			region.setPoint(3, (int)(oX - b  ), (int)(oY      ));
			break;
		case 7:
			region.setPoint(0, (int)(oX + a  ), (int)(oY      ));
			region.setPoint(1, (int)(oX + c  ), (int)(oY + r  ));
			region.setPoint(2, (int)(oX - c  ), (int)(oY + r  ));
			region.setPoint(3, (int)(oX - a  ), (int)(oY      ));
			break;
		case 8:
			region.setPoint(0, (int)(oX + a  ), (int)(oY      ));
			region.setPoint(1, (int)(oX + a+c), (int)(oY + r  ));
			region.setPoint(2, (int)(oX + a  ), (int)(oY + R  ));
			region.setPoint(3, (int)(oX      ), (int)(oY + R  ));
			break;
		case 9:
			region.setPoint(0, (int)(oX + c  ), (int)(oY + r  ));
			region.setPoint(1, (int)(oX - c  ), (int)(oY + R+r));
			region.setPoint(2, (int)(oX - a  ), (int)(oY + R  ));
			region.setPoint(3, (int)(oX - c  ), (int)(oY + r  ));
			break;
		case 10:
			region.setPoint(0, (int)(oX + a  ), (int)(oY + R  ));
			region.setPoint(1, (int)(oX + a+c), (int)(oY + R+r));
			region.setPoint(2, (int)(oX - c  ), (int)(oY + R+r));
			region.setPoint(3, (int)(oX      ), (int)(oY + R  ));
			break;
		case 11:
			region.setPoint(0, (int)(oX + a+c), (int)(oY + r  ));
			region.setPoint(1, (int)(oX + b+c), (int)(oY + R+r));
			region.setPoint(2, (int)(oX + a+c), (int)(oY + R+r));
			region.setPoint(3, (int)(oX + a  ), (int)(oY + R  ));
			break;
		}
	}

	public override Rect getRcInner(int borderWidth) {
		AttrTrapezoid1 attr = Attr;
		int area = attr.Area;
		double a = attr.CalcA(area);
		double b = attr.CalcB(area);
		double c = attr.CalcC(area);
		double R = attr.CalcROut(area);
		double r = attr.CalcRIn(area);
//		double w = borderWidth/2.0;
		double sq    = attr.CalcSq(area, borderWidth);
		double sq2   = sq/2;

		double oX = (a+b)*(coord.x/3) + b; // offset X
		double oY = (R+r)*(coord.y/4*2+1); // offset Y

		PointDouble center = new PointDouble(); // координата центра квадрата
		switch (direction) {
		case 0:  center.x = oX - c*1.25; center.y = oY - r*1.75; break;
		case 1:  center.x = oX + c*0.75; center.y = oY - r*2.25; break;
		case 2:  center.x = oX + c*3;    center.y = oY - r*2.50; break;
		case 3:  center.x = oX - a;      center.y = oY - r*0.50; break;
		case 4:  center.x = oX + c*0.25; center.y = oY - r*0.75; break;
		case 5:  center.x = oX + c*2.25; center.y = oY - r*1.25; break;
		case 6:  center.x = oX - c*2.25; center.y = oY + r*0.75; break;
		case 7:  center.x = oX;          center.y = oY + r*0.50; break;
		case 8:  center.x = oX + c*1.75; center.y = oY + r*1.25; break;
		case 9:  center.x = oX - c*0.75; center.y = oY + r*1.75; break;
		case 10: center.x = oX + c;      center.y = oY + r*2.50; break;
		case 11: center.x = oX + c*3.25; center.y = oY + r*2.25; break;
		}

		Rect square = new Rect();
		square.x = (int) (center.x - sq2);
		square.y = (int) (center.y - sq2);
		square.width =
		square.height = (int) sq;
		return square;
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