////////////////////////////////////////////////////////////////////////////////
//                               FastMines project
//                                      © Sergey Krivulya (KSerg, aka SeregaLBN)
// file name: "Quadrangle1.java"
//
// –еализаци€ класса Quadrangle1 - четырЄхугольник 120∞-90∞-60∞-90∞
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

/// <summary> Quadrangle1 - четырЄхугольник 120∞-90∞-60∞-90∞ </summary>
public class Quadrangle1 : BaseCell {
	public class AttrQuadrangle1 : BaseAttribute {
		public AttrQuadrangle1(int area)
			: base(area)
      {}

		public override Size CalcOwnerSize(Size sizeField, int area) {
			double a = CalcA(area);
			double b = CalcB(area);
			double h = CalcH(area);
			double m = CalcM(area);
			Size result = new Size(
					(int)(m + m*((sizeField.width+2)/3)+
					          h*((sizeField.width+1)/3)+
					          m*((sizeField.width+0)/3)),
					(int)(b + b*((sizeField.height+1)/2)+
					          a*((sizeField.height+0)/2)));

			if (sizeField.width == 1)
				if ((sizeField.height & 1) == 0)
					result.height -= (int)(a/4);
			if (sizeField.width == 2)
				if ((sizeField.height % 4) == 0)
					result.height -= (int)(a/4);
			if ((sizeField.height == 1) || (sizeField.height == 2)) {
				if ((sizeField.width % 3) == 2)
					result.width -= (int)m;
				if ((sizeField.width % 3) == 0)
					result.width -= (int)m;
			}

			return result;
		}
	
		public override int getNeighborNumber() { return 9; }
		public override int getNeighborNumber(int direction) { return 9; }
		public override int getVertexNumber() { return 4; }
		public override int getVertexNumber(int direction) { return 4; }
		public override double getVertexIntersection() { return 4.25; } // (3+4+4+6)/4.
		public override Size GetDirectionSizeField() { return new Size(3, 4); }
		public override double CalcA(int area) { return Math.Sqrt(area/SQRT3)*2; }
		public double CalcB(int area) { return CalcA(area)/2; }
		public double CalcH(int area) { return CalcB(area)*SQRT3; }
		public double CalcN(int area) { return CalcA(area)*0.75; }
		public double CalcM(int area) { return CalcH(area)/2; }
		public double CalcZ(int area) { return CalcA(area)/(1+SQRT3); }
		public double CalcZx(int area) { return CalcZ(area)*SQRT3/2; }
		public double CalcZy(int area) { return CalcZ(area)/2; }
		public override double CalcSq(int area, int borderWidth) {
			double w = borderWidth/2.0;
			return (CalcA(area)*SQRT3 - w*2*(1+SQRT3))/(SQRT3+2);
		}

		public override Size sizeIcoField(bool smallSize) {
			return new Size(3, smallSize ? 2 : 3);
		}
	}

	public Quadrangle1(AttrQuadrangle1 attr, Coord coord)
		: base(attr, coord,
				(coord.y&3)*3 + (coord.x%3) // 0..11
			)
   {}

	private AttrQuadrangle1 Attr {
		get { return (AttrQuadrangle1) base.Attr; }
	}

	protected override Coord[] GetCoordsNeighbor() {
		Coord[] neighborCoord = new Coord[Attr.getNeighborNumber()];

		// определ€ю координаты соседей
    	switch (direction) {
    	case 0:
    		neighborCoord[0] = new Coord(coord.x-1, coord.y-1);
    		neighborCoord[1] = new Coord(coord.x  , coord.y-1);
    		neighborCoord[2] = new Coord(coord.x-1, coord.y  );
    		neighborCoord[3] = new Coord(coord.x+1, coord.y  );
    		neighborCoord[4] = new Coord(coord.x+2, coord.y  );
    		neighborCoord[5] = new Coord(coord.x-1, coord.y+1);
    		neighborCoord[6] = new Coord(coord.x  , coord.y+1);
    		neighborCoord[7] = new Coord(coord.x+1, coord.y+1);
    		neighborCoord[8] = new Coord(coord.x+2, coord.y+1);
    		break;
    	case 1:
    		neighborCoord[0] = new Coord(coord.x-2, coord.y-1);
    		neighborCoord[1] = new Coord(coord.x-1, coord.y-1);
    		neighborCoord[2] = new Coord(coord.x  , coord.y-1);
    		neighborCoord[3] = new Coord(coord.x+1, coord.y-1);
    		neighborCoord[4] = new Coord(coord.x-1, coord.y  );
    		neighborCoord[5] = new Coord(coord.x+1, coord.y  );
    		neighborCoord[6] = new Coord(coord.x-1, coord.y+1);
    		neighborCoord[7] = new Coord(coord.x  , coord.y+1);
    		neighborCoord[8] = new Coord(coord.x+1, coord.y+1);
    		break;
    	case 2:
    		neighborCoord[0] = new Coord(coord.x-1, coord.y-1);
    		neighborCoord[1] = new Coord(coord.x  , coord.y-1);
    		neighborCoord[2] = new Coord(coord.x-2, coord.y  );
    		neighborCoord[3] = new Coord(coord.x-1, coord.y  );
    		neighborCoord[4] = new Coord(coord.x+1, coord.y  );
    		neighborCoord[5] = new Coord(coord.x-2, coord.y+1);
    		neighborCoord[6] = new Coord(coord.x-1, coord.y+1);
    		neighborCoord[7] = new Coord(coord.x  , coord.y+1);
    		neighborCoord[8] = new Coord(coord.x+1, coord.y+1);
    		break;
    	case 3:
    		neighborCoord[0] = new Coord(coord.x-1, coord.y-1);
    		neighborCoord[1] = new Coord(coord.x  , coord.y-1);
    		neighborCoord[2] = new Coord(coord.x+1, coord.y-1);
    		neighborCoord[3] = new Coord(coord.x+2, coord.y-1);
    		neighborCoord[4] = new Coord(coord.x-1, coord.y  );
    		neighborCoord[5] = new Coord(coord.x+1, coord.y  );
    		neighborCoord[6] = new Coord(coord.x+2, coord.y  );
    		neighborCoord[7] = new Coord(coord.x-1, coord.y+1);
    		neighborCoord[8] = new Coord(coord.x  , coord.y+1);
    		break;
    	case 4:
    		neighborCoord[0] = new Coord(coord.x-1, coord.y-1);
    		neighborCoord[1] = new Coord(coord.x  , coord.y-1);
    		neighborCoord[2] = new Coord(coord.x+1, coord.y-1);
    		neighborCoord[3] = new Coord(coord.x-1, coord.y  );
    		neighborCoord[4] = new Coord(coord.x+1, coord.y  );
    		neighborCoord[5] = new Coord(coord.x-2, coord.y+1);
    		neighborCoord[6] = new Coord(coord.x-1, coord.y+1);
    		neighborCoord[7] = new Coord(coord.x  , coord.y+1);
    		neighborCoord[8] = new Coord(coord.x+1, coord.y+1);
    		break;
    	case 5:
    		neighborCoord[0] = new Coord(coord.x-2, coord.y-1);
    		neighborCoord[1] = new Coord(coord.x-1, coord.y-1);
    		neighborCoord[2] = new Coord(coord.x  , coord.y-1);
    		neighborCoord[3] = new Coord(coord.x+1, coord.y-1);
    		neighborCoord[4] = new Coord(coord.x-2, coord.y  );
    		neighborCoord[5] = new Coord(coord.x-1, coord.y  );
    		neighborCoord[6] = new Coord(coord.x+1, coord.y  );
    		neighborCoord[7] = new Coord(coord.x-1, coord.y+1);
    		neighborCoord[8] = new Coord(coord.x  , coord.y+1);
    		break;
    	case 6:
    		neighborCoord[0] = new Coord(coord.x  , coord.y-1);
    		neighborCoord[1] = new Coord(coord.x+1, coord.y-1);
    		neighborCoord[2] = new Coord(coord.x-2, coord.y  );
    		neighborCoord[3] = new Coord(coord.x-1, coord.y  );
    		neighborCoord[4] = new Coord(coord.x+1, coord.y  );
    		neighborCoord[5] = new Coord(coord.x-2, coord.y+1);
    		neighborCoord[6] = new Coord(coord.x-1, coord.y+1);
    		neighborCoord[7] = new Coord(coord.x  , coord.y+1);
    		neighborCoord[8] = new Coord(coord.x+1, coord.y+1);
    		break;
    	case 7:
    		neighborCoord[0] = new Coord(coord.x  , coord.y-1);
    		neighborCoord[1] = new Coord(coord.x+1, coord.y-1);
    		neighborCoord[2] = new Coord(coord.x-1, coord.y  );
    		neighborCoord[3] = new Coord(coord.x+1, coord.y  );
    		neighborCoord[4] = new Coord(coord.x+2, coord.y  );
    		neighborCoord[5] = new Coord(coord.x-1, coord.y+1);
    		neighborCoord[6] = new Coord(coord.x  , coord.y+1);
    		neighborCoord[7] = new Coord(coord.x+1, coord.y+1);
    		neighborCoord[8] = new Coord(coord.x+2, coord.y+1);
    		break;
    	case 8:
    		neighborCoord[0] = new Coord(coord.x-1, coord.y-1);
    		neighborCoord[1] = new Coord(coord.x  , coord.y-1);
    		neighborCoord[2] = new Coord(coord.x+1, coord.y-1);
    		neighborCoord[3] = new Coord(coord.x+2, coord.y-1);
    		neighborCoord[4] = new Coord(coord.x-1, coord.y  );
    		neighborCoord[5] = new Coord(coord.x+1, coord.y  );
    		neighborCoord[6] = new Coord(coord.x-1, coord.y+1);
    		neighborCoord[7] = new Coord(coord.x  , coord.y+1);
    		neighborCoord[8] = new Coord(coord.x+1, coord.y+1);
    		break;
    	case 9:
    		neighborCoord[0] = new Coord(coord.x-2, coord.y-1);
    		neighborCoord[1] = new Coord(coord.x-1, coord.y-1);
    		neighborCoord[2] = new Coord(coord.x  , coord.y-1);
    		neighborCoord[3] = new Coord(coord.x+1, coord.y-1);
    		neighborCoord[4] = new Coord(coord.x-2, coord.y  );
    		neighborCoord[5] = new Coord(coord.x-1, coord.y  );
    		neighborCoord[6] = new Coord(coord.x+1, coord.y  );
    		neighborCoord[7] = new Coord(coord.x  , coord.y+1);
    		neighborCoord[8] = new Coord(coord.x+1, coord.y+1);
    		break;
    	case 10:
    		neighborCoord[0] = new Coord(coord.x-1, coord.y-1);
    		neighborCoord[1] = new Coord(coord.x  , coord.y-1);
    		neighborCoord[2] = new Coord(coord.x+1, coord.y-1);
    		neighborCoord[3] = new Coord(coord.x+2, coord.y-1);
    		neighborCoord[4] = new Coord(coord.x-1, coord.y  );
    		neighborCoord[5] = new Coord(coord.x+1, coord.y  );
    		neighborCoord[6] = new Coord(coord.x+2, coord.y  );
    		neighborCoord[7] = new Coord(coord.x  , coord.y+1);
    		neighborCoord[8] = new Coord(coord.x+1, coord.y+1);
    		break;
    	case 11:
    		neighborCoord[0] = new Coord(coord.x-1, coord.y-1);
    		neighborCoord[1] = new Coord(coord.x  , coord.y-1);
    		neighborCoord[2] = new Coord(coord.x+1, coord.y-1);
    		neighborCoord[3] = new Coord(coord.x-1, coord.y  );
    		neighborCoord[4] = new Coord(coord.x+1, coord.y  );
    		neighborCoord[5] = new Coord(coord.x-1, coord.y+1);
    		neighborCoord[6] = new Coord(coord.x  , coord.y+1);
    		neighborCoord[7] = new Coord(coord.x+1, coord.y+1);
    		neighborCoord[8] = new Coord(coord.x+2, coord.y+1);
    		break;
    	}

		return neighborCoord;
	}

	protected override void CalcRegion() {
		AttrQuadrangle1 attr = Attr;
		int area = attr.Area;
		double a = attr.CalcA(area);
		double b = attr.CalcB(area);
		double h = attr.CalcH(area);
		double n = attr.CalcN(area);
		double m = attr.CalcM(area);

		// определение координат точек фигуры
		double oX = (h*2)*(coord.x/3) + h+m; // offset X
		double oY = (a*3)*(coord.y/4) + a+n; // offset Y

		switch (direction) {
		case 0:
			region.setPoint(0, (int)(oX - h  ), (int)(oY - n-n));
			region.setPoint(1, (int)(oX - m  ), (int)(oY - n  ));
			region.setPoint(2, (int)(oX - h-m), (int)(oY - n  ));
			region.setPoint(3, (int)(oX - h-m), (int)(oY - n-b));
			break;
		case 1:
			region.setPoint(0, (int)(oX      ), (int)(oY - n-n));
			region.setPoint(1, (int)(oX - m  ), (int)(oY - n  ));
			region.setPoint(2, (int)(oX - h  ), (int)(oY - n-n));
			region.setPoint(3, (int)(oX - m  ), (int)(oY - n-a));
			break;
		case 2:
			region.setPoint(0, (int)(oX + m  ), (int)(oY - n-b));
			region.setPoint(1, (int)(oX + m  ), (int)(oY - n  ));
			region.setPoint(2, (int)(oX - m  ), (int)(oY - n  ));
			region.setPoint(3, (int)(oX      ), (int)(oY - n-n));
			break;
		case 3:
			region.setPoint(0, (int)(oX - m  ), (int)(oY - n  ));
			region.setPoint(1, (int)(oX - h  ), (int)(oY      ));
			region.setPoint(2, (int)(oX - h-m), (int)(oY - n+b));
			region.setPoint(3, (int)(oX - h-m), (int)(oY - n  ));
			break;
		case 4:
			region.setPoint(0, (int)(oX - m  ), (int)(oY - n  ));
			region.setPoint(1, (int)(oX      ), (int)(oY      ));
			region.setPoint(2, (int)(oX - m  ), (int)(oY - n+a));
			region.setPoint(3, (int)(oX - h  ), (int)(oY      ));
			break;
		case 5:
			region.setPoint(0, (int)(oX + m  ), (int)(oY - n  ));
			region.setPoint(1, (int)(oX + m  ), (int)(oY - n+b));
			region.setPoint(2, (int)(oX      ), (int)(oY      ));
			region.setPoint(3, (int)(oX - m  ), (int)(oY - n  ));
			break;
		case 6:
			region.setPoint(0, (int)(oX - m  ), (int)(oY + n-b));
			region.setPoint(1, (int)(oX - m  ), (int)(oY + n  ));
			region.setPoint(2, (int)(oX - h-m), (int)(oY + n  ));
			region.setPoint(3, (int)(oX - h  ), (int)(oY      ));
			break;
		case 7:
			region.setPoint(0, (int)(oX      ), (int)(oY      ));
			region.setPoint(1, (int)(oX + m  ), (int)(oY + n  ));
			region.setPoint(2, (int)(oX - m  ), (int)(oY + n  ));
			region.setPoint(3, (int)(oX - m  ), (int)(oY + n-b));
			break;
		case 8:
			region.setPoint(0, (int)(oX + h  ), (int)(oY      ));
			region.setPoint(1, (int)(oX + m  ), (int)(oY + n  ));
			region.setPoint(2, (int)(oX      ), (int)(oY      ));
			region.setPoint(3, (int)(oX + m  ), (int)(oY + n-a));
			break;
		case 9:
			region.setPoint(0, (int)(oX - m  ), (int)(oY + n  ));
			region.setPoint(1, (int)(oX - m  ), (int)(oY + n+b));
			region.setPoint(2, (int)(oX - h  ), (int)(oY + n+n));
			region.setPoint(3, (int)(oX - h-m), (int)(oY + n  ));
			break;
		case 10:
			region.setPoint(0, (int)(oX + m  ), (int)(oY + n  ));
			region.setPoint(1, (int)(oX      ), (int)(oY + n+n));
			region.setPoint(2, (int)(oX - m  ), (int)(oY + n+b));
			region.setPoint(3, (int)(oX - m  ), (int)(oY + n  ));
			break;
		case 11:
			region.setPoint(0, (int)(oX + m  ), (int)(oY + n  ));
			region.setPoint(1, (int)(oX + h  ), (int)(oY + n+n));
			region.setPoint(2, (int)(oX + m  ), (int)(oY + n+a));
			region.setPoint(3, (int)(oX      ), (int)(oY + n+n));
			break;
		}
	}

	public override Rect getRcInner(int borderWidth) {
		AttrQuadrangle1 attr = Attr;
		int area = attr.Area;
		double a = attr.CalcA(area);
		double b = attr.CalcB(area);
		double h = attr.CalcH(area);
		double n = attr.CalcN(area);
		double m = attr.CalcM(area);
		double z = attr.CalcZ(area);
		double zx = attr.CalcZx(area);
		double zy = attr.CalcZy(area);
//		double w = borderWidth/2.0;
		double sq    = attr.CalcSq(area, borderWidth);
		double sq2   = sq/2;

		double oX = (h*2)*(coord.x/3) + h+m; // offset X
		double oY = (a*3)*(coord.y/4) + a+n; // offset Y

		PointDouble center = new PointDouble(); // координата центра квадрата
		switch (direction) {
		case 0:  center.x = oX - h-m+zx; center.y = oY - n-b+zy; break;
		case 1:  center.x = oX - m;      center.y = oY - n-a+z ; break;
		case 2:  center.x = oX + m  -zx; center.y = oY - n-b+zy; break;
		case 3:  center.x = oX - h-m+zx; center.y = oY - n+b-zy; break;
		case 4:  center.x = oX - m;      center.y = oY - n+a-z ; break;
		case 5:  center.x = oX + m  -zx; center.y = oY - n+b-zy; break;
		case 6:  center.x = oX - m  -zx; center.y = oY + n-b+zy; break;
		case 7:  center.x = oX - m  +zx; center.y = oY + n-b+zy; break;
		case 8:  center.x = oX + m;      center.y = oY + n-a+z ; break;
		case 9:  center.x = oX - m  -zx; center.y = oY + n+b-zy; break;
		case 10: center.x = oX - m  +zx; center.y = oY + n+b-zy; break;
		case 11: center.x = oX + m;      center.y = oY + n+a-z ; break;
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
		case 1: case  8: return 1;
		case 4: case 11: return 3;
		}
		return 2;
	}
}
}