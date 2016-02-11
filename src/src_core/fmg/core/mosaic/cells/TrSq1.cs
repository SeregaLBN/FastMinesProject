////////////////////////////////////////////////////////////////////////////////
//                               FastMines project
//                                      © Sergey Krivulya (KSerg, aka SeregaLBN)
// file name: "TrSq1.java"
//
// Реализация класса TrSq1 - мозаика из 4х треугольников и 2х квадратов
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

/// <summary> Комбинация. Мозаика из 4х треугольников и 2х квадратов </summary>
public class TrSq1 : BaseCell {
	public class AttrTrSq1 : BaseAttribute {
		public AttrTrSq1(int area)
			: base(area)
      {}

		public override Size GetOwnerSize(Matrisize sizeField) {
			double b = B;
			double k = K;
			double n = N;
			double m = M;
			Size result = new Size(
					(int)(b+n*((sizeField.m-1+2)/3)+
					        k*((sizeField.m-1+1)/3)+
					        m*((sizeField.m-1+0)/3)),
					(int)(b+n* (sizeField.n-1)));

			if (sizeField.n == 1) {
				if ((sizeField.m % 3) == 2) result.width -= (int)m;
				if ((sizeField.m % 3) == 0) result.width -= (int)k;
			}
			if (sizeField.m == 1)
				if ((sizeField.n & 1) == 0)
					result.height -= (int)m;

			return result;
		}
	
		public override int getNeighborNumber(bool max) { return max ? 12 : 9; }
		public override int getNeighborNumber(int direction) {
	    	switch (direction) {
	    	case 1: case 2: case 3: case 5: return 9;
	    	case 0: case 4: return 12;
	    	default:
	    		throw new ArgumentException("Invalid value direction=" + direction);
	    	}
		}
		public override int getVertexNumber(int direction) {
			switch (direction) {
			case 1: case 2: case 3: case 5: return 3;
			case 0: case 4: return 4;
			default:
				throw new ArgumentException("Invalid value direction="+direction);
		 	}
		}
		public override double getVertexIntersection() { return 5.0; }
		public override Size GetDirectionSizeField() { return new Size(3, 2); }
		public override double A => Math.Sqrt(3*Area/(1+SQRT3/2));
		public double B => N + M;
		public double K => N - M;
		public double N => A * SIN75;
		public double M => A * SIN15;
		public override double GetSq(int borderWidth) {
			double w = borderWidth/2.0;
			return (A*SQRT3 - w*6) / (4*SIN75); 
		}
	}

	public TrSq1(AttrTrSq1 attr, Coord coord)
		: base(attr, coord,
				(coord.y&1)*3+(coord.x%3) // 0..5
			)
	{}

	private new AttrTrSq1 Attr {
		get { return (AttrTrSq1) base.Attr; }
	}

	protected override Coord?[] GetCoordsNeighbor() {
      var neighborCoord = new Coord?[Attr.getNeighborNumber(true)];

		// определяю координаты соседей
    	switch (direction) {
    	case 0:
    		neighborCoord[ 0] = new Coord(coord.x-2, coord.y-1);
    		neighborCoord[ 1] = new Coord(coord.x-1, coord.y-1);
    		neighborCoord[ 2] = new Coord(coord.x  , coord.y-1);
    		neighborCoord[ 3] = new Coord(coord.x+1, coord.y-1);
    		neighborCoord[ 4] = new Coord(coord.x-2, coord.y  );
    		neighborCoord[ 5] = new Coord(coord.x-1, coord.y  );
    		neighborCoord[ 6] = new Coord(coord.x+1, coord.y  );
    		neighborCoord[ 7] = new Coord(coord.x+2, coord.y  );
    		neighborCoord[ 8] = new Coord(coord.x-2, coord.y+1);
    		neighborCoord[ 9] = new Coord(coord.x-1, coord.y+1);
    		neighborCoord[10] = new Coord(coord.x  , coord.y+1);
    		neighborCoord[11] = new Coord(coord.x+1, coord.y+1);
    		break;
    	case 1:
    		neighborCoord[ 0] = new Coord(coord.x-2, coord.y-1);
    		neighborCoord[ 1] = new Coord(coord.x-1, coord.y-1);
    		neighborCoord[ 2] = new Coord(coord.x  , coord.y-1);
    		neighborCoord[ 3] = new Coord(coord.x+1, coord.y-1);
    		neighborCoord[ 4] = new Coord(coord.x-1, coord.y  );
    		neighborCoord[ 5] = new Coord(coord.x+1, coord.y  );
    		neighborCoord[ 6] = new Coord(coord.x+2, coord.y  );
    		neighborCoord[ 7] = new Coord(coord.x-1, coord.y+1);
    		neighborCoord[ 8] = new Coord(coord.x  , coord.y+1);
    		neighborCoord[ 9] =
    		neighborCoord[10] =
    		neighborCoord[11] = null;
    		break;
    	case 2:
    		neighborCoord[ 0] = new Coord(coord.x-1, coord.y-1);
    		neighborCoord[ 1] = new Coord(coord.x  , coord.y-1);
    		neighborCoord[ 2] = new Coord(coord.x-2, coord.y  );
    		neighborCoord[ 3] = new Coord(coord.x-1, coord.y  );
    		neighborCoord[ 4] = new Coord(coord.x+1, coord.y  );
    		neighborCoord[ 5] = new Coord(coord.x-2, coord.y+1);
    		neighborCoord[ 6] = new Coord(coord.x-1, coord.y+1);
    		neighborCoord[ 7] = new Coord(coord.x  , coord.y+1);
    		neighborCoord[ 8] = new Coord(coord.x+1, coord.y+1);
    		neighborCoord[ 9] =  
    		neighborCoord[10] =
    		neighborCoord[11] = null;
    		break;
    	case 3:
    		neighborCoord[ 0] = new Coord(coord.x-1, coord.y-1);
    		neighborCoord[ 1] = new Coord(coord.x  , coord.y-1);
    		neighborCoord[ 2] = new Coord(coord.x+1, coord.y-1);
    		neighborCoord[ 3] = new Coord(coord.x+2, coord.y-1);
    		neighborCoord[ 4] = new Coord(coord.x-2, coord.y  );
    		neighborCoord[ 5] = new Coord(coord.x-1, coord.y  );
    		neighborCoord[ 6] = new Coord(coord.x+1, coord.y  );
    		neighborCoord[ 7] = new Coord(coord.x  , coord.y+1);
    		neighborCoord[ 8] = new Coord(coord.x+1, coord.y+1);
    		neighborCoord[ 9] =
    		neighborCoord[10] =
    		neighborCoord[11] = null;
    		break;
    	case 4:
    		neighborCoord[ 0] = new Coord(coord.x-1, coord.y-1);
    		neighborCoord[ 1] = new Coord(coord.x  , coord.y-1);
    		neighborCoord[ 2] = new Coord(coord.x+1, coord.y-1);
    		neighborCoord[ 3] = new Coord(coord.x+2, coord.y-1);
    		neighborCoord[ 4] = new Coord(coord.x-2, coord.y  );
    		neighborCoord[ 5] = new Coord(coord.x-1, coord.y  );
    		neighborCoord[ 6] = new Coord(coord.x+1, coord.y  );
    		neighborCoord[ 7] = new Coord(coord.x+2, coord.y  );
    		neighborCoord[ 8] = new Coord(coord.x-1, coord.y+1);
    		neighborCoord[ 9] = new Coord(coord.x  , coord.y+1);
    		neighborCoord[10] = new Coord(coord.x+1, coord.y+1);
    		neighborCoord[11] = new Coord(coord.x+2, coord.y+1);
    		break;
    	case 5:
    		neighborCoord[ 0] = new Coord(coord.x  , coord.y-1);
    		neighborCoord[ 1] = new Coord(coord.x+1, coord.y-1);
    		neighborCoord[ 2] = new Coord(coord.x-1, coord.y  );
    		neighborCoord[ 3] = new Coord(coord.x+1, coord.y  );
    		neighborCoord[ 4] = new Coord(coord.x+2, coord.y  );
    		neighborCoord[ 5] = new Coord(coord.x-1, coord.y+1);
    		neighborCoord[ 6] = new Coord(coord.x  , coord.y+1);
    		neighborCoord[ 7] = new Coord(coord.x+1, coord.y+1);
    		neighborCoord[ 8] = new Coord(coord.x+2, coord.y+1);
    		neighborCoord[ 9] =
    		neighborCoord[10] =
    		neighborCoord[11] = null;
    		break;
    	}

		return neighborCoord;
	}

	protected override void CalcRegion() {
		AttrTrSq1 attr = Attr;
		double b = attr.B;
		double k = attr.K;
		double n = attr.N;
		double m = attr.M;

		double oX = b + n * (coord.x/3*2); // offset X
		double oY = n + n*2*(coord.y/2);   // offset Y

		switch (direction) {
		case 0:
			region.SetPoint(0, (int)(oX - m  ), (int)(oY - n));
			region.SetPoint(1, (int)(oX      ), (int)(oY    ));
			region.SetPoint(2, (int)(oX - n  ), (int)(oY + m));
			region.SetPoint(3, (int)(oX - b  ), (int)(oY - k));
			break;
		case 1:                            
			region.SetPoint(1, (int)(oX      ), (int)(oY    ));
			region.SetPoint(2, (int)(oX - m  ), (int)(oY - n));
			region.SetPoint(0, (int)(oX + k  ), (int)(oY - k));
			break;
		case 2:                            
			region.SetPoint(0, (int)(oX + k  ), (int)(oY - k));
			region.SetPoint(1, (int)(oX + n  ), (int)(oY + m));
			region.SetPoint(2, (int)(oX      ), (int)(oY    ));
			break;
		case 3:                            
			region.SetPoint(1, (int)(oX - m  ), (int)(oY + n));
			region.SetPoint(2, (int)(oX - n  ), (int)(oY + m));
			region.SetPoint(0, (int)(oX      ), (int)(oY    ));
			break;
		case 4:                            
			region.SetPoint(0, (int)(oX + n  ), (int)(oY + m));
			region.SetPoint(3, (int)(oX      ), (int)(oY    ));
			region.SetPoint(2, (int)(oX - m  ), (int)(oY + n));
			region.SetPoint(1, (int)(oX + k  ), (int)(oY + b));
			break;
		case 5:                            
			region.SetPoint(0, (int)(oX + n  ), (int)(oY + m));
			region.SetPoint(1, (int)(oX + n+k), (int)(oY + n));
			region.SetPoint(2, (int)(oX + k  ), (int)(oY + b));
			break;
		}
	}

	public override Rect getRcInner(int borderWidth) {
		AttrTrSq1 attr = Attr;
		double b = attr.B;
		double k = attr.K;
		double n = attr.N;
		double m = attr.M;
		double w = borderWidth/2.0;
		double sq = attr.GetSq(borderWidth);
		double sq2 = sq/2;

		double oX = b + n * (coord.x/3*2); // offset X
		double oY = n + n*2*(coord.y/2);   // offset Y


		double ksw1 = k/2-sq2-w/SQRT2;
		double ksw2 = k/2+sq2+w/SQRT2;
		PointDouble center = new PointDouble(); // координата вписанного в фигуру квадрата (не совпадает с центром фигуры)
		switch (direction) {
		case 0:  center.X = oX - b/2;    center.Y = oY - k/2;    break;
		case 1:  center.X = oX + ksw1;   center.Y = oY - ksw2;   break;
		case 2:  center.X = oX + ksw2;   center.Y = oY - ksw1;   break;
		case 3:  center.X = oX + ksw2-n; center.Y = oY - ksw2+n; break;
		case 4:  center.X = oX + k/2;    center.Y = oY + b/2;    break;
		case 5:  center.X = oX + ksw1+n; center.Y = oY + ksw2+m; break;
		}

		Rect square = new Rect();
		square.X = (int) (center.X - sq2);
		square.Y = (int) (center.Y - sq2);
		square.Width =
		square.Height = (int) sq;
		return square;
	}

	public override int getShiftPointBorderIndex() {
		switch (direction) {
		case 1: case 3: return 1;
		}
		return 2;
	}
}
}