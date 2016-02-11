////////////////////////////////////////////////////////////////////////////////
//                               FastMines project
//                                      © Sergey Krivulya (KSerg, aka SeregaLBN)
// file name: "PentagonT5.java"
//
// Реализация класса PentagonT5 - 5-ти угольник, тип №5
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

/// <summary> Пятиугольник. Тип №5 </summary>
public class PentagonT5 : BaseCell {
	public class AttrPentagonT5 : BaseAttribute {
		public AttrPentagonT5(int area)
			: base(area)
      {}

		public override Size GetOwnerSize(Matrisize sizeField) {
			double a = A;
			double h = H;
			Size result = new Size(
					(int)(a*3.5 +
					      a*2.0*((sizeField.m+13)/14) +
					      a    *((sizeField.m+12)/14) +
					      a*1.5*((sizeField.m+11)/14) +
					      a*2.0*((sizeField.m+10)/14) +
					      a    *((sizeField.m+ 9)/14) +
					      a*1.5*((sizeField.m+ 8)/14) +
					      a*2.0*((sizeField.m+ 7)/14) +
					      a    *((sizeField.m+ 6)/14) +
					      a*1.5*((sizeField.m+ 5)/14) +
					      a*2.0*((sizeField.m+ 4)/14) +
					      a    *((sizeField.m+ 3)/14) +
					      a*2.0*((sizeField.m+ 2)/14) +
					      a    *((sizeField.m+ 1)/14) +
					      a*1.5*((sizeField.m+ 0)/14)),
					(int)(h*5  +
					      h*2  *((sizeField.n+ 5)/ 6) +
					      h*2  *((sizeField.n+ 4)/ 6) +
					      h*2  *((sizeField.n+ 3)/ 6) +
					      h*3  *((sizeField.n+ 2)/ 6) +
					      h*2  *((sizeField.n+ 1)/ 6) +
					      h*3  *((sizeField.n+ 0)/ 6)));

			// когда размер поля мал...
			if (sizeField.m < 14) { // ...нужно вычислять не только по общей формуле, а и убрать остатки снизу..
				if ((sizeField.n & 1) == 0) {
					if (sizeField.m < 11) result.height -= (int)h;
					if (sizeField.m <  8) result.height -= (int)h;
					if (sizeField.m <  5) result.height -= (int)h;
					if (sizeField.m <  2) result.height -= (int)h;
				} else {
					if (sizeField.m < 10) result.height -= (int)h;
					if (sizeField.m <  7) result.height -= (int)h;
					if (sizeField.m <  4) result.height -= (int)h;
				}
				if ((sizeField.n+5)%6 == 0) // y == 1 7 13 ..
					if (sizeField.m < 13) result.height -= (int)h;
			}
			if (sizeField.n < 5) { // .. и справа
				switch (sizeField.n) {
				case 1:
					switch (sizeField.m % 14) {
					default: result.width -= (int)(3*a); 	  break;
					case 12: result.width -= (int)(3*a+a/2); break;
					case 13: result.width -= (int)(3*a-a/2); break;
					} break;
				case 2:
					switch (sizeField.m % 14) {
					default: result.width -= (int)(3*a);     break;
					case 12: result.width -= (int)(3*a+a/2); break;
					case 13: result.width -= (int)(3*a-a/2); break;
					case  0: result.width -= (int)(1.5*a);   break;
					} break;
				case 3:
					switch (sizeField.m % 14) {
					default: result.width -= (int)(1.5*a); break;
					case 12: result.width -= (int)(  2*a); break;
					} break;
				case 4:
					switch (sizeField.m % 14) {
					default: result.width -= (int)(1.5*a); break;
					case 12: result.width -= (int)(  2*a); break;
					case 13: result.width -= (int)(  1*a); break;
					} break;
				}
			}

			return result;
		}

      public override int getNeighborNumber(bool max) { return 8; }
		public override int getNeighborNumber(int direction) { return 8; }
		public override int getVertexNumber(int direction) { return 5; }
		public override double getVertexIntersection() { return 3.6; } // (3+3+3+3+6)/5.
		public override Size GetDirectionSizeField() { return new Size(14, 6); }
		public override double A => 2 * Math.Sqrt(Area/SQRT147);
		public double H => A * SQRT3/2;
		public override double GetSq(int borderWidth) {
			double w = borderWidth/2.0;
			return (A*2*SQRT3-4*w)/(SQRT3+1);
		}

		public override int getMaxBackgroundFillModeValue() {
			return base.getMaxBackgroundFillModeValue()+2;
//			return 1;
		}
	}

	public PentagonT5(AttrPentagonT5 attr, Coord coord)
		: base(attr, coord,
				(coord.y%6)*14 + (coord.x%14) // 0..83
			)
	{}

	private new AttrPentagonT5 Attr {
		get { return (AttrPentagonT5) base.Attr; }
	}

	protected override Coord?[] GetCoordsNeighbor() {
      var neighborCoord = new Coord?[Attr.getNeighborNumber(true)];

		// определяю координаты соседей
		switch (direction) {
		case 0:
			neighborCoord[0] = new Coord(coord.x-2, coord.y-2);
			neighborCoord[1] = new Coord(coord.x-3, coord.y-1);
			neighborCoord[2] = new Coord(coord.x-2, coord.y-1);
			neighborCoord[3] = new Coord(coord.x+1, coord.y  );
			neighborCoord[4] = new Coord(coord.x+2, coord.y  );
			neighborCoord[5] = new Coord(coord.x  , coord.y+1);
			neighborCoord[6] = new Coord(coord.x+1, coord.y+1);
			neighborCoord[7] = new Coord(coord.x+2, coord.y+1);
			break;
		case 3: case 6: case 9:
			neighborCoord[0] = new Coord(coord.x-2, coord.y-1);
			neighborCoord[1] = new Coord(coord.x-1, coord.y  );
			neighborCoord[2] = new Coord(coord.x+1, coord.y  );
			neighborCoord[3] = new Coord(coord.x+2, coord.y  );
			neighborCoord[4] = new Coord(coord.x-1, coord.y+1);
			neighborCoord[5] = new Coord(coord.x  , coord.y+1);  
			neighborCoord[6] = new Coord(coord.x+1, coord.y+1);
			neighborCoord[7] = new Coord(coord.x+2, coord.y+1);
			break;
		case 12:
			neighborCoord[0] = new Coord(coord.x-2, coord.y-1);
			neighborCoord[1] = new Coord(coord.x-1, coord.y  );
			neighborCoord[2] = new Coord(coord.x+1, coord.y  );
			neighborCoord[3] = new Coord(coord.x-1, coord.y+1);
			neighborCoord[4] = new Coord(coord.x  , coord.y+1);
			neighborCoord[5] = new Coord(coord.x+1, coord.y+1);
			neighborCoord[6] = new Coord(coord.x  , coord.y+2);
			neighborCoord[7] = new Coord(coord.x+1, coord.y+2);
			break;
		case 28:
			neighborCoord[0] = new Coord(coord.x-1, coord.y-1);
			neighborCoord[1] = new Coord(coord.x+1, coord.y-1);
			neighborCoord[2] = new Coord(coord.x-1, coord.y  );
			neighborCoord[3] = new Coord(coord.x+1, coord.y  );
			neighborCoord[4] = new Coord(coord.x+2, coord.y  );
			neighborCoord[5] = new Coord(coord.x  , coord.y+1);
			neighborCoord[6] = new Coord(coord.x+1, coord.y+1);
			neighborCoord[7] = new Coord(coord.x+2, coord.y+1);
			break;
		case 31: case 34: case 37: case 56: case 59: case 62:
			neighborCoord[0] = new Coord(coord.x+1, coord.y-1);
			neighborCoord[1] = new Coord(coord.x-1, coord.y  );
			neighborCoord[2] = new Coord(coord.x+1, coord.y  );
			neighborCoord[3] = new Coord(coord.x+2, coord.y  );
			neighborCoord[4] = new Coord(coord.x-1, coord.y+1);
			neighborCoord[5] = new Coord(coord.x  , coord.y+1);
			neighborCoord[6] = new Coord(coord.x+1, coord.y+1);
			neighborCoord[7] = new Coord(coord.x+2, coord.y+1);
			break;
		case 54:
			neighborCoord[0] = new Coord(coord.x-1, coord.y-1);
			neighborCoord[1] = new Coord(coord.x  , coord.y-1);
			neighborCoord[2] = new Coord(coord.x-1, coord.y  );
			neighborCoord[3] = new Coord(coord.x+1, coord.y  );
			neighborCoord[4] = new Coord(coord.x-1, coord.y+1);
			neighborCoord[5] = new Coord(coord.x  , coord.y+1);
			neighborCoord[6] = new Coord(coord.x+1, coord.y+1);
			neighborCoord[7] = new Coord(coord.x+1, coord.y+2);
			break;
		case 65:
			neighborCoord[0] = new Coord(coord.x+1, coord.y-1);
			neighborCoord[1] = new Coord(coord.x-1, coord.y  );
			neighborCoord[2] = new Coord(coord.x+1, coord.y  );
			neighborCoord[3] = new Coord(coord.x-1, coord.y+1);
			neighborCoord[4] = new Coord(coord.x  , coord.y+1);
			neighborCoord[5] = new Coord(coord.x+1, coord.y+1);
			neighborCoord[6] = new Coord(coord.x+2, coord.y+1);
			neighborCoord[7] = new Coord(coord.x+3, coord.y+1);
			break;
		case 1:
			neighborCoord[0] = new Coord(coord.x-3, coord.y-2);
			neighborCoord[1] = new Coord(coord.x-2, coord.y-1);
			neighborCoord[2] = new Coord(coord.x-1, coord.y-1);
			neighborCoord[3] = new Coord(coord.x-1, coord.y  );
			neighborCoord[4] = new Coord(coord.x+1, coord.y  );
			neighborCoord[5] = new Coord(coord.x-1, coord.y+1);
			neighborCoord[6] = new Coord(coord.x  , coord.y+1);
			neighborCoord[7] = new Coord(coord.x+1, coord.y+1);
			break;
		case 4: case 7: case 10:
			neighborCoord[0] = new Coord(coord.x-3, coord.y-1);
			neighborCoord[1] = new Coord(coord.x-2, coord.y-1);
			neighborCoord[2] = new Coord(coord.x-1, coord.y-1);
			neighborCoord[3] = new Coord(coord.x-1, coord.y  );
			neighborCoord[4] = new Coord(coord.x+1, coord.y  );
			neighborCoord[5] = new Coord(coord.x-1, coord.y+1);
			neighborCoord[6] = new Coord(coord.x  , coord.y+1);
			neighborCoord[7] = new Coord(coord.x+1, coord.y+1);
			break;
		case 13:
			neighborCoord[0] = new Coord(coord.x-3, coord.y-1);
			neighborCoord[1] = new Coord(coord.x-2, coord.y-1);
			neighborCoord[2] = new Coord(coord.x-1, coord.y  );
			neighborCoord[3] = new Coord(coord.x-1, coord.y+1);
			neighborCoord[4] = new Coord(coord.x  , coord.y+1);
			neighborCoord[5] = new Coord(coord.x+1, coord.y+1);
			neighborCoord[6] = new Coord(coord.x-1, coord.y+2);
			neighborCoord[7] = new Coord(coord.x  , coord.y+2);
			break;
		case 29: case 32: case 35: case 38: case 57: case 60: case 63:
			neighborCoord[0] = new Coord(coord.x  , coord.y-1);
			neighborCoord[1] = new Coord(coord.x+1, coord.y-1);
			neighborCoord[2] = new Coord(coord.x+2, coord.y-1);
			neighborCoord[3] = new Coord(coord.x-1, coord.y  );
			neighborCoord[4] = new Coord(coord.x+1, coord.y  );
			neighborCoord[5] = new Coord(coord.x-1, coord.y+1);
			neighborCoord[6] = new Coord(coord.x  , coord.y+1);
			neighborCoord[7] = new Coord(coord.x+1, coord.y+1);
			break;
		case 55:
			neighborCoord[0] = new Coord(coord.x-1, coord.y-1);
			neighborCoord[1] = new Coord(coord.x  , coord.y-1);
			neighborCoord[2] = new Coord(coord.x-1, coord.y  );
			neighborCoord[3] = new Coord(coord.x+1, coord.y  );
			neighborCoord[4] = new Coord(coord.x-2, coord.y+1);
			neighborCoord[5] = new Coord(coord.x-1, coord.y+1);
			neighborCoord[6] = new Coord(coord.x  , coord.y+1);
			neighborCoord[7] = new Coord(coord.x  , coord.y+2);
			break;
		case 66:
			neighborCoord[0] = new Coord(coord.x  , coord.y-1);
			neighborCoord[1] = new Coord(coord.x+1, coord.y-1);
			neighborCoord[2] = new Coord(coord.x-1, coord.y  );
			neighborCoord[3] = new Coord(coord.x+1, coord.y  );
			neighborCoord[4] = new Coord(coord.x-1, coord.y+1);
			neighborCoord[5] = new Coord(coord.x  , coord.y+1);
			neighborCoord[6] = new Coord(coord.x+1, coord.y+1);
			neighborCoord[7] = new Coord(coord.x+2, coord.y+1);
			break;
		case 2: case 5: case 8: case 11:
			neighborCoord[0] = new Coord(coord.x-2, coord.y-1);
			neighborCoord[1] = new Coord(coord.x-1, coord.y-1);
			neighborCoord[2] = new Coord(coord.x-2, coord.y  );
			neighborCoord[3] = new Coord(coord.x-1, coord.y  );
			neighborCoord[4] = new Coord(coord.x+1, coord.y  );
			neighborCoord[5] = new Coord(coord.x-2, coord.y+1);
			neighborCoord[6] = new Coord(coord.x-1, coord.y+1);
			neighborCoord[7] = new Coord(coord.x  , coord.y+1);
			break;
		case 27:
			neighborCoord[0] = new Coord(coord.x-1, coord.y-1);
			neighborCoord[1] = new Coord(coord.x  , coord.y-1);
			neighborCoord[2] = new Coord(coord.x-1, coord.y  );
			neighborCoord[3] = new Coord(coord.x+1, coord.y  );
			neighborCoord[4] = new Coord(coord.x+2, coord.y  );
			neighborCoord[5] = new Coord(coord.x-1, coord.y+1);
			neighborCoord[6] = new Coord(coord.x  , coord.y+1);
			neighborCoord[7] = new Coord(coord.x+1, coord.y+1);
			break;
		case 30: case 33: case 36: case 58: case 61: case 64:
			neighborCoord[0] = new Coord(coord.x+1, coord.y-1);
			neighborCoord[1] = new Coord(coord.x+2, coord.y-1);
			neighborCoord[2] = new Coord(coord.x-2, coord.y  );
			neighborCoord[3] = new Coord(coord.x-1, coord.y  );
			neighborCoord[4] = new Coord(coord.x+1, coord.y  );
			neighborCoord[5] = new Coord(coord.x-2, coord.y+1);
			neighborCoord[6] = new Coord(coord.x-1, coord.y+1);
			neighborCoord[7] = new Coord(coord.x  , coord.y+1);
			break;
		case 39:
			neighborCoord[0] = new Coord(coord.x+1, coord.y-1);
			neighborCoord[1] = new Coord(coord.x-2, coord.y  );
			neighborCoord[2] = new Coord(coord.x-1, coord.y  );
			neighborCoord[3] = new Coord(coord.x+1, coord.y  );
			neighborCoord[4] = new Coord(coord.x-2, coord.y+1);
			neighborCoord[5] = new Coord(coord.x-1, coord.y+1);
			neighborCoord[6] = new Coord(coord.x  , coord.y+1);
			neighborCoord[7] = new Coord(coord.x+1, coord.y+1);
			break;
		case 69:
			neighborCoord[0] = new Coord(coord.x-1, coord.y-1);
			neighborCoord[1] = new Coord(coord.x  , coord.y-1);
			neighborCoord[2] = new Coord(coord.x+1, coord.y-1);
			neighborCoord[3] = new Coord(coord.x+2, coord.y-1);
			neighborCoord[4] = new Coord(coord.x-2, coord.y  );
			neighborCoord[5] = new Coord(coord.x-1, coord.y  );
			neighborCoord[6] = new Coord(coord.x+1, coord.y  );
			neighborCoord[7] = new Coord(coord.x  , coord.y+1);
			break;
		case 82:
			neighborCoord[0] = new Coord(coord.x-3, coord.y-1);
			neighborCoord[1] = new Coord(coord.x-2, coord.y-1);
			neighborCoord[2] = new Coord(coord.x-1, coord.y-1);
			neighborCoord[3] = new Coord(coord.x  , coord.y-1);
			neighborCoord[4] = new Coord(coord.x-3, coord.y  );
			neighborCoord[5] = new Coord(coord.x-2, coord.y  );
			neighborCoord[6] = new Coord(coord.x-1, coord.y  );
			neighborCoord[7] = new Coord(coord.x+2, coord.y+1);
			break;
		case 14:
			neighborCoord[0] = new Coord(coord.x-3, coord.y-2);
			neighborCoord[1] = new Coord(coord.x-1, coord.y-1);
			neighborCoord[2] = new Coord(coord.x  , coord.y-1);
			neighborCoord[3] = new Coord(coord.x+1, coord.y-1);
			neighborCoord[4] = new Coord(coord.x+2, coord.y-1);
			neighborCoord[5] = new Coord(coord.x-1, coord.y  );
			neighborCoord[6] = new Coord(coord.x+1, coord.y  );
			neighborCoord[7] = new Coord(coord.x+2, coord.y  );
			break;
		case 17: case 20: case 23: case 45: case 48: case 51:
			neighborCoord[0] = new Coord(coord.x  , coord.y-1);
			neighborCoord[1] = new Coord(coord.x+1, coord.y-1);
			neighborCoord[2] = new Coord(coord.x+2, coord.y-1);
			neighborCoord[3] = new Coord(coord.x-1, coord.y  );
			neighborCoord[4] = new Coord(coord.x+1, coord.y  );
			neighborCoord[5] = new Coord(coord.x+2, coord.y  );
			neighborCoord[6] = new Coord(coord.x-2, coord.y+1);
			neighborCoord[7] = new Coord(coord.x-1, coord.y+1);
			break;
		case 26:
			neighborCoord[0] = new Coord(coord.x  , coord.y-1);
			neighborCoord[1] = new Coord(coord.x+1, coord.y-1);
			neighborCoord[2] = new Coord(coord.x-1, coord.y  );
			neighborCoord[3] = new Coord(coord.x+1, coord.y  );
			neighborCoord[4] = new Coord(coord.x-2, coord.y+1);
			neighborCoord[5] = new Coord(coord.x-1, coord.y+1);
			neighborCoord[6] = new Coord(coord.x  , coord.y+1);
			neighborCoord[7] = new Coord(coord.x+1, coord.y+1);
			break;
		case 42:
			neighborCoord[0] = new Coord(coord.x-1, coord.y-1);
			neighborCoord[1] = new Coord(coord.x  , coord.y-1);
			neighborCoord[2] = new Coord(coord.x+1, coord.y-1);
			neighborCoord[3] = new Coord(coord.x+2, coord.y-1);
			neighborCoord[4] = new Coord(coord.x-1, coord.y  );
			neighborCoord[5] = new Coord(coord.x+1, coord.y  );
			neighborCoord[6] = new Coord(coord.x+2, coord.y  );
			neighborCoord[7] = new Coord(coord.x-1, coord.y+1);
			break;
		case 67: case 70: case 73: case 76:
			neighborCoord[0] = new Coord(coord.x  , coord.y-1);
			neighborCoord[1] = new Coord(coord.x+1, coord.y-1);
			neighborCoord[2] = new Coord(coord.x+2, coord.y-1);
			neighborCoord[3] = new Coord(coord.x-1, coord.y  );
			neighborCoord[4] = new Coord(coord.x+1, coord.y  );
			neighborCoord[5] = new Coord(coord.x+2, coord.y  );
			neighborCoord[6] = new Coord(coord.x+1, coord.y+1);
			neighborCoord[7] = new Coord(coord.x+2, coord.y+1);
			break;
		case 79:
			neighborCoord[0] = new Coord(coord.x  , coord.y-1);
			neighborCoord[1] = new Coord(coord.x+1, coord.y-1);
			neighborCoord[2] = new Coord(coord.x-1, coord.y  );
			neighborCoord[3] = new Coord(coord.x+1, coord.y  );
			neighborCoord[4] = new Coord(coord.x+2, coord.y  );
			neighborCoord[5] = new Coord(coord.x+3, coord.y  );
			neighborCoord[6] = new Coord(coord.x+1, coord.y+1);
			neighborCoord[7] = new Coord(coord.x+2, coord.y+1);
			break;
		case 15:
			neighborCoord[0] = new Coord(coord.x-1, coord.y-1);
			neighborCoord[1] = new Coord(coord.x  , coord.y-1);
			neighborCoord[2] = new Coord(coord.x+1, coord.y-1);
			neighborCoord[3] = new Coord(coord.x-2, coord.y  );
			neighborCoord[4] = new Coord(coord.x-1, coord.y  );
			neighborCoord[5] = new Coord(coord.x+1, coord.y  );
			neighborCoord[6] = new Coord(coord.x-1, coord.y+1);
			neighborCoord[7] = new Coord(coord.x  , coord.y+1);
			break;
		case 18: case 21: case 24: case 43: case 46: case 49: case 52:
			neighborCoord[0] = new Coord(coord.x-1, coord.y-1);
			neighborCoord[1] = new Coord(coord.x  , coord.y-1);
			neighborCoord[2] = new Coord(coord.x+1, coord.y-1);
			neighborCoord[3] = new Coord(coord.x-1, coord.y  );
			neighborCoord[4] = new Coord(coord.x+1, coord.y  );
			neighborCoord[5] = new Coord(coord.x-2, coord.y+1);
			neighborCoord[6] = new Coord(coord.x-1, coord.y+1);
			neighborCoord[7] = new Coord(coord.x  , coord.y+1);
			break;
		case 40:
			neighborCoord[0] = new Coord(coord.x  , coord.y-2);
			neighborCoord[1] = new Coord(coord.x+1, coord.y-2);
			neighborCoord[2] = new Coord(coord.x  , coord.y-1);
			neighborCoord[3] = new Coord(coord.x+1, coord.y-1);
			neighborCoord[4] = new Coord(coord.x-1, coord.y  );
			neighborCoord[5] = new Coord(coord.x+1, coord.y  );
			neighborCoord[6] = new Coord(coord.x  , coord.y+1);
			neighborCoord[7] = new Coord(coord.x+1, coord.y+1);
			break;
		case 68:
			neighborCoord[0] = new Coord(coord.x  , coord.y-1);
			neighborCoord[1] = new Coord(coord.x+1, coord.y-1);
			neighborCoord[2] = new Coord(coord.x-1, coord.y  );
			neighborCoord[3] = new Coord(coord.x+1, coord.y  );
			neighborCoord[4] = new Coord(coord.x  , coord.y+1);
			neighborCoord[5] = new Coord(coord.x+1, coord.y+1);
			neighborCoord[6] = new Coord(coord.x+2, coord.y+2);
			neighborCoord[7] = new Coord(coord.x+3, coord.y+2);
			break;
		case 71: case 74: case 77:
			neighborCoord[0] = new Coord(coord.x-1, coord.y-1);
			neighborCoord[1] = new Coord(coord.x  , coord.y-1);
			neighborCoord[2] = new Coord(coord.x+1, coord.y-1);
			neighborCoord[3] = new Coord(coord.x-1, coord.y  );
			neighborCoord[4] = new Coord(coord.x+1, coord.y  );
			neighborCoord[5] = new Coord(coord.x+1, coord.y+1);
			neighborCoord[6] = new Coord(coord.x+2, coord.y+1);
			neighborCoord[7] = new Coord(coord.x+3, coord.y+1);
			break;
		case 80:
			neighborCoord[0] = new Coord(coord.x-1, coord.y-1);
			neighborCoord[1] = new Coord(coord.x  , coord.y-1);
			neighborCoord[2] = new Coord(coord.x-1, coord.y  );
			neighborCoord[3] = new Coord(coord.x+1, coord.y  );
			neighborCoord[4] = new Coord(coord.x+2, coord.y  );
			neighborCoord[5] = new Coord(coord.x+1, coord.y+1);
			neighborCoord[6] = new Coord(coord.x+2, coord.y+1);
			neighborCoord[7] = new Coord(coord.x+3, coord.y+1);
			break;
		case 16: case 19: case 22: case 25: case 44: case 47: case 50:
			neighborCoord[0] = new Coord(coord.x-2, coord.y-1);
			neighborCoord[1] = new Coord(coord.x-1, coord.y-1);
			neighborCoord[2] = new Coord(coord.x  , coord.y-1);
			neighborCoord[3] = new Coord(coord.x+1, coord.y-1);
			neighborCoord[4] = new Coord(coord.x-2, coord.y  );
			neighborCoord[5] = new Coord(coord.x-1, coord.y  );
			neighborCoord[6] = new Coord(coord.x+1, coord.y  );
			neighborCoord[7] = new Coord(coord.x-1, coord.y+1);
			break;
		case 41:
			neighborCoord[0] = new Coord(coord.x-1, coord.y-2);
			neighborCoord[1] = new Coord(coord.x  , coord.y-2);
			neighborCoord[2] = new Coord(coord.x-1, coord.y-1);
			neighborCoord[3] = new Coord(coord.x  , coord.y-1);
			neighborCoord[4] = new Coord(coord.x-1, coord.y  );
			neighborCoord[5] = new Coord(coord.x+1, coord.y  );
			neighborCoord[6] = new Coord(coord.x  , coord.y+1);
			neighborCoord[7] = new Coord(coord.x+1, coord.y+1);
			break;
		case 53:
			neighborCoord[0] = new Coord(coord.x-2, coord.y-1);
			neighborCoord[1] = new Coord(coord.x-1, coord.y-1);
			neighborCoord[2] = new Coord(coord.x  , coord.y-1);
			neighborCoord[3] = new Coord(coord.x-2, coord.y  );
			neighborCoord[4] = new Coord(coord.x-1, coord.y  );
			neighborCoord[5] = new Coord(coord.x+1, coord.y  );
			neighborCoord[6] = new Coord(coord.x-1, coord.y+1);
			neighborCoord[7] = new Coord(coord.x  , coord.y+1);
			break;
		case 83:
			neighborCoord[0] = new Coord(coord.x-1, coord.y-2);
			neighborCoord[1] = new Coord(coord.x  , coord.y-2);
			neighborCoord[2] = new Coord(coord.x-2, coord.y-1);
			neighborCoord[3] = new Coord(coord.x-1, coord.y-1);
			neighborCoord[4] = new Coord(coord.x  , coord.y-1);
			neighborCoord[5] = new Coord(coord.x+1, coord.y-1);
			neighborCoord[6] = new Coord(coord.x+1, coord.y  );
			neighborCoord[7] = new Coord(coord.x+2, coord.y+1);
			break;
		case 72: case 75: case 78:
			neighborCoord[0] = new Coord(coord.x-2, coord.y-1);
			neighborCoord[1] = new Coord(coord.x-1, coord.y-1);
			neighborCoord[2] = new Coord(coord.x  , coord.y-1);
			neighborCoord[3] = new Coord(coord.x+1, coord.y-1);
			neighborCoord[4] = new Coord(coord.x-2, coord.y  );
			neighborCoord[5] = new Coord(coord.x-1, coord.y  );
			neighborCoord[6] = new Coord(coord.x+1, coord.y  );
			neighborCoord[7] = new Coord(coord.x+2, coord.y+1);
			break;
		case 81:
			neighborCoord[0] = new Coord(coord.x-2, coord.y-1);
			neighborCoord[1] = new Coord(coord.x-1, coord.y-1);
			neighborCoord[2] = new Coord(coord.x-2, coord.y  );
			neighborCoord[3] = new Coord(coord.x-1, coord.y  );
			neighborCoord[4] = new Coord(coord.x+1, coord.y  );
			neighborCoord[5] = new Coord(coord.x+2, coord.y+1);
			neighborCoord[6] = new Coord(coord.x+3, coord.y+1);
			neighborCoord[7] = new Coord(coord.x+3, coord.y+2);
		 		break;
		 	}

		return neighborCoord;
	}

	protected override void CalcRegion() {
		AttrPentagonT5 attr = Attr;
		double a = attr.A;
		double h = attr.H;

		// определение координат точек фигуры
		double oX = a*21*(coord.x/14); // offset X
		double oY = h*14*(coord.y/6);  // offset Y
		switch (direction) {
		case  0: case  1: case  2: case 14: case 15: case 16: oX += a* 2.5; oY += h* 3; break;
		case  3: case  4: case  5: case 17: case 18: case 19: oX += a* 7.0; oY += h* 4; break;
		case  6: case  7: case  8: case 20: case 21: case 22: oX += a*11.5; oY += h* 5; break;
		case  9: case 10: case 11: case 23: case 24: case 25: oX += a*16.0; oY += h* 6; break;
		case 12: case 13: case 27: case 26: case 40: case 41: oX += a*20.5; oY += h* 7; break;
		case 28: case 29: case 30: case 42: case 43: case 44: oX += a* 4.0; oY += h* 8; break;
		case 31: case 32: case 33: case 45: case 46: case 47: oX += a* 8.5; oY += h* 9; break;
		case 34: case 35: case 36: case 48: case 49: case 50: oX += a*13.0; oY += h*10; break;
		case 37: case 38: case 39: case 51: case 52: case 53: oX += a*17.5; oY += h*11; break;
		case 54: case 55: case 69: case 67: case 68: case 83: oX += a*22.0; oY += h*12; break;
		case 56: case 57: case 58: case 70: case 71: case 72: oX += a* 5.5; oY += h*13; break;
		case 59: case 60: case 61: case 73: case 74: case 75: oX += a*10.0; oY += h*14; break;
		case 62: case 63: case 64: case 76: case 77: case 78: oX += a*14.5; oY += h*15; break;
		case 65: case 66: case 82: case 79: case 80: case 81: oX += a*19.0; oY += h*16; break;
		}
		switch (direction) {
		case  0: case  3: case  6: case  9: case 12: case 28: case 31: case 34: case 37: case 54: case 56: case 59: case 62: case 65:
			region.setPoint(0, (int)(oX - a    ), (int)(oY - h*2));
			region.setPoint(1, (int)(oX        ), (int)(oY      ));
			region.setPoint(2, (int)(oX - a*2  ), (int)(oY      ));
			region.setPoint(3, (int)(oX - a*2.5), (int)(oY - h  ));
			region.setPoint(4, (int)(oX - a*2  ), (int)(oY - h*2));
			break;
		case  1: case  4: case  7: case 10: case 13: case 29: case 32: case 35: case 38: case 55: case 57: case 60: case 63: case 66:
			region.setPoint(0, (int)(oX + a*0.5), (int)(oY - h*3));
			region.setPoint(1, (int)(oX + a    ), (int)(oY - h*2));
			region.setPoint(2, (int)(oX        ), (int)(oY      ));
			region.setPoint(3, (int)(oX - a    ), (int)(oY - h*2));
			region.setPoint(4, (int)(oX - a*0.5), (int)(oY - h*3));
			break;
		case  2: case  5: case  8: case 11: case 27: case 30: case 33: case 36: case 39: case 69: case 58: case 61: case 64: case 82:
			region.setPoint(0, (int)(oX + a*2  ), (int)(oY - h*2));
			region.setPoint(1, (int)(oX + a*2.5), (int)(oY - h  ));
			region.setPoint(2, (int)(oX + a*2  ), (int)(oY      ));
			region.setPoint(3, (int)(oX        ), (int)(oY      ));
			region.setPoint(4, (int)(oX + a    ), (int)(oY - h*2));
			break;
		case 14: case 17: case 20: case 23: case 26: case 42: case 45: case 48: case 51: case 67: case 70: case 73: case 76: case 79:
			region.setPoint(0, (int)(oX        ), (int)(oY      ));
			region.setPoint(1, (int)(oX - a    ), (int)(oY + h*2));
			region.setPoint(2, (int)(oX - a*2  ), (int)(oY + h*2));
			region.setPoint(3, (int)(oX - a*2.5), (int)(oY + h  ));
			region.setPoint(4, (int)(oX - a*2  ), (int)(oY      ));
			break;
		case 15: case 18: case 21: case 24: case 40: case 43: case 46: case 49: case 52: case 68: case 71: case 74: case 77: case 80:
			region.setPoint(0, (int)(oX        ), (int)(oY      ));
			region.setPoint(1, (int)(oX + a    ), (int)(oY + h*2));
			region.setPoint(2, (int)(oX + a*0.5), (int)(oY + h*3));
			region.setPoint(3, (int)(oX - a*0.5), (int)(oY + h*3));
			region.setPoint(4, (int)(oX - a    ), (int)(oY + h*2));
			break;
		case 16: case 19: case 22: case 25: case 41: case 44: case 47: case 50: case 53: case 83: case 72: case 75: case 78: case 81:
			region.setPoint(0, (int)(oX + a*2  ), (int)(oY      ));
			region.setPoint(1, (int)(oX + a*2.5), (int)(oY + h  ));
			region.setPoint(2, (int)(oX + a*2  ), (int)(oY + h*2));
			region.setPoint(3, (int)(oX + a    ), (int)(oY + h*2));
			region.setPoint(4, (int)(oX        ), (int)(oY      ));
			break;
		}
	}

	public override Rect getRcInner(int borderWidth) {
		AttrPentagonT5 attr = Attr;
		double a = attr.A;
		double h = attr.H;
//		double w = borderWidth/2.0;
		double sq  = Attr.GetSq(borderWidth);
		double sq2 = sq/2;

		// определение координат точек фигуры
		double oX = a*21*(coord.x/14); // offset X
		double oY = h*14*(coord.y/6);  // offset Y
		switch (direction) {
		case  0: case  1: case  2: case 14: case 15: case 16: oX += a* 2.5; oY += h* 3; break;
		case  3: case  4: case  5: case 17: case 18: case 19: oX += a* 7.0; oY += h* 4; break;
		case  6: case  7: case  8: case 20: case 21: case 22: oX += a*11.5; oY += h* 5; break;
		case  9: case 10: case 11: case 23: case 24: case 25: oX += a*16.0; oY += h* 6; break;
		case 12: case 13: case 27: case 26: case 40: case 41: oX += a*20.5; oY += h* 7; break;
		case 28: case 29: case 30: case 42: case 43: case 44: oX += a* 4.0; oY += h* 8; break;
		case 31: case 32: case 33: case 45: case 46: case 47: oX += a* 8.5; oY += h* 9; break;
		case 34: case 35: case 36: case 48: case 49: case 50: oX += a*13.0; oY += h*10; break;
		case 37: case 38: case 39: case 51: case 52: case 53: oX += a*17.5; oY += h*11; break;
		case 54: case 55: case 69: case 67: case 68: case 83: oX += a*22.0; oY += h*12; break;
		case 56: case 57: case 58: case 70: case 71: case 72: oX += a* 5.5; oY += h*13; break;
		case 59: case 60: case 61: case 73: case 74: case 75: oX += a*10.0; oY += h*14; break;
		case 62: case 63: case 64: case 76: case 77: case 78: oX += a*14.5; oY += h*15; break;
		case 65: case 66: case 82: case 79: case 80: case 81: oX += a*19.0; oY += h*16; break;
		}

		PointDouble center = new PointDouble(); // координата центра квадрата
		switch (direction) {
		case  0: case  3: case  6: case  9: case 12: case 28: case 31:
		case 34: case 37: case 54: case 56: case 59: case 62: case 65: center.x = oX - a*1.5;  center.y = oY - h;   break;
		case  1: case  4: case  7: case 10: case 13: case 29: case 32:
		case 35: case 38: case 55: case 57: case 60: case 63: case 66: center.x = oX;          center.y = oY - h*2; break;
		case  2: case  5: case  8: case 11: case 27: case 30: case 33:
		case 36: case 39: case 69: case 58: case 61: case 64: case 82: center.x = oX + a*1.5;  center.y = oY - h;   break;
		case 14: case 17: case 20: case 23: case 26: case 42: case 45:
		case 48: case 51: case 67: case 70: case 73: case 76: case 79: center.x = oX - a*1.5;  center.y = oY + h;   break;
		case 15: case 18: case 21: case 24: case 40: case 43: case 46:
		case 49: case 52: case 68: case 71: case 74: case 77: case 80: center.x = oX;          center.y = oY + h*2; break;
		case 16: case 19: case 22: case 25: case 41: case 44: case 47:
		case 50: case 53: case 83: case 72: case 75: case 78: case 81: center.x = oX + a*1.5;  center.y = oY + h;   break;
		}

		Rect square = new Rect();
		square.X = (int) (center.x - sq2);
		square.Y = (int) (center.y - sq2);
		square.Width =
		square.Height = (int) sq;
		return square;
	}

	public override int getShiftPointBorderIndex() {
		switch (direction) {
		case  0: case  3: case  6: case  9: case 12: case 28: case 31: case 34: case 37: case 54: case 56: case 59: case 62: case 65:
		case  1: case  4: case  7: case 10: case 13: case 29: case 32: case 35: case 38: case 55: case 57: case 60: case 63: case 66:
		case 14: case 17: case 20: case 23: case 26: case 42: case 45: case 48: case 51: case 67: case 70: case 73: case 76: case 79:
			return 2;
		}
		return 3;
	}

	public override Color getBackgroundFillColor(int fillMode, Color defaultColor, Func<int, Color> repositoryColor) {
		if (fillMode == Attr.getMaxBackgroundFillModeValue())
		{
			// подсвечиваю 'ромашку'
			switch (getDirection()) {
			case  0: case  1: case  2: case 14: case 15: case 16: return repositoryColor(0);
			case  3: case  4: case  5: case 17: case 18: case 19: return repositoryColor(1);
			case  6: case  7: case  8: case 20: case 21: case 22: return repositoryColor(2);
			case  9: case 10: case 11: case 23: case 24: case 25: return repositoryColor(3);
			case 12: case 13: case 27: case 26: case 40: case 41: return repositoryColor(4);
			case 28: case 29: case 30: case 42: case 43: case 44: return repositoryColor(5);
			case 31: case 32: case 33: case 45: case 46: case 47: return repositoryColor(6);
			case 34: case 35: case 36: case 48: case 49: case 50: return repositoryColor(7);
			case 37: case 38: case 39: case 51: case 52: case 53: return repositoryColor(8);
			case 54: case 55: case 69: case 67: case 68: case 83: return repositoryColor(9);
			case 56: case 57: case 58: case 70: case 71: case 72: return repositoryColor(10);
			case 59: case 60: case 61: case 73: case 74: case 75: return repositoryColor(11);
			case 62: case 63: case 64: case 76: case 77: case 78: return repositoryColor(12);
			case 65: case 66: case 82: case 79: case 80: case 81: return repositoryColor(13);
//			default:
//				return repositoryColor.get(-1);
			}
		} else
		if (fillMode == (Attr.getMaxBackgroundFillModeValue()-1))
		{
			// подсвечиваю обратную 'диагональку'
			switch (getDirection()) {
			case  1: case  0: case 14:
			case 13: case 12: case 26: case 38: case 37: case 51: case 63: case 62: case 76:
			case  7: case  6: case 20: case 32: case 31: case 45: case 57: case 56: case 70:
				return repositoryColor(0);
			case  2: case 16: case 15:
			case 27: case 41: case 40: case 39: case 53: case 52: case 64: case 78: case 77:
			case  8: case 22: case 21: case 33: case 47: case 46: case 58: case 72: case 71:
				return repositoryColor(1);
			case  4: case  3: case 17:
			case 29: case 28: case 42: case 55: case 54: case 67: case 66: case 65: case 79:
			case 10: case  9: case 23: case 35: case 34: case 48: case 60: case 59: case 73:
				return repositoryColor(2);
			case  5: case 19: case 18:
			case 30: case 44: case 43: case 69: case 83: case 68: case 82: case 81: case 80:
			case 11: case 25: case 24: case 36: case 50: case 49: case 61: case 75: case 74:
				return repositoryColor(3);
//			default:
//				return repositoryColor(-1);
			}
		}
		return base.getBackgroundFillColor(fillMode, defaultColor, repositoryColor);
	}
}
}