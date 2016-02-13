////////////////////////////////////////////////////////////////////////////////
//                               FastMines project
//                                      © Sergey Krivulya (KSerg, aka SeregaLBN)
// file name: "TrSq2.java"
//
// Реализация класса TrSq2 - мозаика из 24х треугольников и 12х квадратов (на 1 квадрат приходится 2 треугольника)
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

/// <summary> Комбинация. мозаика из 24х треугольников и 12х квадратов (на 1 квадрат приходится 2 треугольника) </summary>
public class TrSq2 : BaseCell {
	public class AttrTrSq2 : BaseAttribute {
		public AttrTrSq2(double area)
			: base(area)
      {}

		public override SizeDouble GetOwnerSize(Matrisize sizeField) {
			var a = A;
			var b = B;
			var h = H;
			var result = new SizeDouble(
					b+h*((sizeField.m+2)/3)+
					  a*((sizeField.m+1)/3)+
					  b*((sizeField.m+0)/3),
					b+h*((sizeField.n+2)/3)+
					  a*((sizeField.n+1)/3)+
					  b*((sizeField.n+0)/3));

			if (sizeField.n < 5) {
				var x = sizeField.m % 6;
				switch (sizeField.n) {
				case 1:
               switch (x) { case 0: case 2: case 5: result.Width -= b; break; }
					break;
				case 2: case 3: case 4:
					if (x == 5) result.Width -= b;
					break;
				}
			}
			if (sizeField.m < 5) {
				var y = sizeField.n % 6;
				switch (sizeField.m) {
				case 1:
               switch (y) { case 2: case 3: case 5: result.Height -= b; break; }
					break;
				case 2: case 3: case 4:
					if (y == 2) result.Height -= b;
					break;
				}
			}

			return result;
		}

      public override int getNeighborNumber(bool max) { return max ? 12 : 9; }
		public override int getNeighborNumber(int direction) {
			switch (direction) {
			case  1: case  2: case  4: case  5:
			case  6: case  8: case  9: case 11:
			case 12: case 13: case 15: case 16:
			case 19: case 20: case 22: case 23:
			case 24: case 26: case 27: case 29:
			case 30: case 31: case 33: case 34: return 9;
			case  0: case  3: case  7: case 10:
			case 14: case 17: case 18: case 21:
			case 25: case 28: case 32: case 35: return 12;
			default:
				throw new ArgumentException("Invalid value direction="+direction);
			}
		}
		public override int getVertexNumber(int direction) {
			switch (direction) {
			case  0: case  3: case  7: case 10:
			case 14: case 17: case 18: case 21:
			case 25: case 28: case 32: case 35: return 4;
			case  1: case  2: case  4: case  5:
			case  6: case  8: case  9: case 11:
			case 12: case 13: case 15: case 16:
			case 19: case 20: case 22: case 23:
			case 24: case 26: case 27: case 29:
			case 30: case 31: case 33: case 34: return 3;
			default:
				throw new ArgumentException("Invalid value direction="+direction);
			}
		}
		public override double getVertexIntersection() { return 5.0; }
		public override Size GetDirectionSizeField() { return new Size(6, 6); }
		/// <summary> </summary> размер стороны треугольника и квадрата */
		public override double A => Math.Sqrt(6*Area/(2+SQRT3));
		public double B => A / 2;
		public double H => B * SQRT3;
		public override double GetSq(int borderWidth) {
			var w = borderWidth/2.0;
			return (A*SQRT3 - w*6) / (2+SQRT3) - 1; 
		}
	}

	public TrSq2(AttrTrSq2 attr, Coord coord)
		: base(attr, coord,
				(coord.y%6)*6+(coord.x%6) // 0..35
			)
	{}

	private new AttrTrSq2 Attr => (AttrTrSq2) base.Attr;

   protected override Coord?[] GetCoordsNeighbor() {
      var neighborCoord = new Coord?[Attr.getNeighborNumber(true)];

		// определяю координаты соседей
		switch (direction) {
		case 0:
		case 21:
			neighborCoord[ 0] = new Coord(coord.x+1, coord.y-2);
			neighborCoord[ 1] = new Coord(coord.x-1, coord.y-1);
			neighborCoord[ 2] = new Coord(coord.x  , coord.y-1);
			neighborCoord[ 3] = new Coord(coord.x+1, coord.y-1);
			neighborCoord[ 4] = new Coord(coord.x-1, coord.y  );
			neighborCoord[ 5] = new Coord(coord.x+1, coord.y  );
			neighborCoord[ 6] = new Coord(coord.x-1, coord.y+1);
			neighborCoord[ 7] = new Coord(coord.x  , coord.y+1);
			neighborCoord[ 8] = new Coord(coord.x+1, coord.y+1);
			neighborCoord[ 9] = new Coord(coord.x+2, coord.y+1);
			neighborCoord[10] = new Coord(coord.x-1, coord.y+2);
			neighborCoord[11] = new Coord(coord.x  , coord.y+2);
			break;
		case 1:
		case 22:
			neighborCoord[ 0] = new Coord(coord.x  , coord.y-2);
			neighborCoord[ 1] = new Coord(coord.x-1, coord.y-1);
			neighborCoord[ 2] = new Coord(coord.x  , coord.y-1);
			neighborCoord[ 3] = new Coord(coord.x+1, coord.y-1);
			neighborCoord[ 4] = new Coord(coord.x-1, coord.y  );
			neighborCoord[ 5] = new Coord(coord.x+1, coord.y  );
			neighborCoord[ 6] = new Coord(coord.x-1, coord.y+1);
			neighborCoord[ 7] = new Coord(coord.x  , coord.y+1);
			neighborCoord[ 8] = new Coord(coord.x+1, coord.y+1);
			neighborCoord[ 9] =
			neighborCoord[10] =
			neighborCoord[11] = null;
			break;
		case 2:
		case 23:
			neighborCoord[ 0] = new Coord(coord.x-1, coord.y-2);
			neighborCoord[ 1] = new Coord(coord.x+1, coord.y-1);
			neighborCoord[ 2] = new Coord(coord.x  , coord.y-1);
			neighborCoord[ 3] = new Coord(coord.x+2, coord.y-1);
			neighborCoord[ 4] = new Coord(coord.x-1, coord.y  );
			neighborCoord[ 5] = new Coord(coord.x+1, coord.y  );
			neighborCoord[ 6] = new Coord(coord.x-1, coord.y+1);
			neighborCoord[ 7] = new Coord(coord.x  , coord.y+1);
			neighborCoord[ 8] = new Coord(coord.x+1, coord.y+1);
			neighborCoord[ 9] =
			neighborCoord[10] =
			neighborCoord[11] = null;
			break;
		case 3:
		case 18:
			neighborCoord[ 0] = new Coord(coord.x-1, coord.y-1);
			neighborCoord[ 1] = new Coord(coord.x  , coord.y-1);
			neighborCoord[ 2] = new Coord(coord.x+1, coord.y-1);
			neighborCoord[ 3] = new Coord(coord.x+2, coord.y-1);
			neighborCoord[ 4] = new Coord(coord.x-1, coord.y  );
			neighborCoord[ 5] = new Coord(coord.x+1, coord.y  );
			neighborCoord[ 6] = new Coord(coord.x+2, coord.y  );
			neighborCoord[ 7] = new Coord(coord.x-2, coord.y+1);
			neighborCoord[ 8] = new Coord(coord.x-1, coord.y+1);
			neighborCoord[ 9] = new Coord(coord.x  , coord.y+1);
			neighborCoord[10] = new Coord(coord.x+1, coord.y+1);
			neighborCoord[11] = new Coord(coord.x+1, coord.y+2);
			break;
		case 4:
		case 19:
			neighborCoord[ 0] = new Coord(coord.x  , coord.y-1);
			neighborCoord[ 1] = new Coord(coord.x+1, coord.y-1);
			neighborCoord[ 2] = new Coord(coord.x-1, coord.y  );
			neighborCoord[ 3] = new Coord(coord.x+1, coord.y  );
			neighborCoord[ 4] = new Coord(coord.x-1, coord.y+1);
			neighborCoord[ 5] = new Coord(coord.x  , coord.y+1);
			neighborCoord[ 6] = new Coord(coord.x+1, coord.y+1);
			neighborCoord[ 7] = new Coord(coord.x  , coord.y+2);
			neighborCoord[ 8] = new Coord(coord.x+1, coord.y+2);
			neighborCoord[ 9] =
			neighborCoord[10] =
			neighborCoord[11] = null;
			break;
		case 5:
		case 20:
			neighborCoord[ 0] = new Coord(coord.x-1, coord.y-1);
			neighborCoord[ 1] = new Coord(coord.x  , coord.y-1);
			neighborCoord[ 2] = new Coord(coord.x+1, coord.y-1);
			neighborCoord[ 3] = new Coord(coord.x-2, coord.y  );
			neighborCoord[ 4] = new Coord(coord.x-1, coord.y  );
			neighborCoord[ 5] = new Coord(coord.x+1, coord.y  );
			neighborCoord[ 6] = new Coord(coord.x-1, coord.y+1);
			neighborCoord[ 7] = new Coord(coord.x  , coord.y+1);
			neighborCoord[ 8] = new Coord(coord.x  , coord.y+2);
			neighborCoord[ 9] =
			neighborCoord[10] =
			neighborCoord[11] = null;
			break;
		case 6:
		case 27:
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
		case 7:
		case 28:
			neighborCoord[ 0] = new Coord(coord.x-1, coord.y-1);
			neighborCoord[ 1] = new Coord(coord.x  , coord.y-1);
			neighborCoord[ 2] = new Coord(coord.x+1, coord.y-1);
			neighborCoord[ 3] = new Coord(coord.x+2, coord.y-1);
			neighborCoord[ 4] = new Coord(coord.x-1, coord.y  );
			neighborCoord[ 5] = new Coord(coord.x+1, coord.y  );
			neighborCoord[ 6] = new Coord(coord.x+2, coord.y  );
			neighborCoord[ 7] = new Coord(coord.x-1, coord.y+1);
			neighborCoord[ 8] = new Coord(coord.x  , coord.y+1);
			neighborCoord[ 9] = new Coord(coord.x+1, coord.y+1);
			neighborCoord[10] = new Coord(coord.x+2, coord.y+1);
			neighborCoord[11] = new Coord(coord.x+3, coord.y+1);
			break;
		case 8:
		case 29:
			neighborCoord[ 0] = new Coord(coord.x-1, coord.y-3);
			neighborCoord[ 1] = new Coord(coord.x  , coord.y-2);
			neighborCoord[ 2] = new Coord(coord.x-2, coord.y-1);
			neighborCoord[ 3] = new Coord(coord.x-1, coord.y-1);
			neighborCoord[ 4] = new Coord(coord.x  , coord.y-1);
			neighborCoord[ 5] = new Coord(coord.x+1, coord.y-1);
			neighborCoord[ 6] = new Coord(coord.x-2, coord.y  );
			neighborCoord[ 7] = new Coord(coord.x-1, coord.y  );
			neighborCoord[ 8] = new Coord(coord.x+1, coord.y  );
			neighborCoord[ 9] =
			neighborCoord[10] =
			neighborCoord[11] = null;
			break;
		case 9:
		case 24:
			neighborCoord[ 0] = new Coord(coord.x-1, coord.y-1);
			neighborCoord[ 1] = new Coord(coord.x  , coord.y-1);
			neighborCoord[ 2] = new Coord(coord.x+1, coord.y-1);
			neighborCoord[ 3] = new Coord(coord.x-2, coord.y  );
			neighborCoord[ 4] = new Coord(coord.x-1, coord.y  );
			neighborCoord[ 5] = new Coord(coord.x+1, coord.y  );
			neighborCoord[ 6] = new Coord(coord.x-1, coord.y+1);
			neighborCoord[ 7] = new Coord(coord.x  , coord.y+1);
			neighborCoord[ 8] = new Coord(coord.x+1, coord.y+1);
			neighborCoord[ 9] =
			neighborCoord[10] =
			neighborCoord[11] = null;
			break;
		case 10:
		case 25:
			neighborCoord[ 0] = new Coord(coord.x-1, coord.y-1);
			neighborCoord[ 1] = new Coord(coord.x  , coord.y-1);
			neighborCoord[ 2] = new Coord(coord.x+1, coord.y-1);
			neighborCoord[ 3] = new Coord(coord.x-1, coord.y  );
			neighborCoord[ 4] = new Coord(coord.x+1, coord.y  );
			neighborCoord[ 5] = new Coord(coord.x-1, coord.y+1);
			neighborCoord[ 6] = new Coord(coord.x  , coord.y+1);
			neighborCoord[ 7] = new Coord(coord.x+1, coord.y+1);
			neighborCoord[ 8] = new Coord(coord.x-1, coord.y+2);
			neighborCoord[ 9] = new Coord(coord.x  , coord.y+2);
			neighborCoord[10] = new Coord(coord.x+1, coord.y+2);
			neighborCoord[11] = new Coord(coord.x+1, coord.y+3);
			break;
		case 11:
		case 26:
			neighborCoord[ 0] = new Coord(coord.x  , coord.y-2);
			neighborCoord[ 1] = new Coord(coord.x+1, coord.y-2);
			neighborCoord[ 2] = new Coord(coord.x-1, coord.y-1);
			neighborCoord[ 3] = new Coord(coord.x  , coord.y-1);
			neighborCoord[ 4] = new Coord(coord.x+1, coord.y-1);
			neighborCoord[ 5] = new Coord(coord.x-1, coord.y  );
			neighborCoord[ 6] = new Coord(coord.x+1, coord.y  );
			neighborCoord[ 7] = new Coord(coord.x  , coord.y+1);
			neighborCoord[ 8] = new Coord(coord.x+1, coord.y+1);
			neighborCoord[ 9] =
			neighborCoord[10] =
			neighborCoord[11] = null;
			break;
		case 12:
		case 33:
			neighborCoord[ 0] = new Coord(coord.x  , coord.y-2);
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
		case 13:
		case 34:
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
		case 14:
		case 35:
			neighborCoord[ 0] = new Coord(coord.x-2, coord.y-1);
			neighborCoord[ 1] = new Coord(coord.x-1, coord.y-1);
			neighborCoord[ 2] = new Coord(coord.x+1, coord.y-1);
			neighborCoord[ 3] = new Coord(coord.x-2, coord.y  );
			neighborCoord[ 4] = new Coord(coord.x-1, coord.y  );
			neighborCoord[ 5] = new Coord(coord.x+1, coord.y  );
			neighborCoord[ 6] = new Coord(coord.x+2, coord.y  );
			neighborCoord[ 7] = new Coord(coord.x-2, coord.y+1);
			neighborCoord[ 8] = new Coord(coord.x-1, coord.y+1);
			neighborCoord[ 9] = new Coord(coord.x  , coord.y+1);
			neighborCoord[10] = new Coord(coord.x+1, coord.y+1);
			neighborCoord[11] = new Coord(coord.x  , coord.y+2);
			break;
		case 15:
		case 30:
			neighborCoord[ 0] = new Coord(coord.x-2, coord.y-1);
			neighborCoord[ 1] = new Coord(coord.x  , coord.y-1);
			neighborCoord[ 2] = new Coord(coord.x+1, coord.y-1);
			neighborCoord[ 3] = new Coord(coord.x-1, coord.y  );
			neighborCoord[ 4] = new Coord(coord.x+1, coord.y  );
			neighborCoord[ 5] = new Coord(coord.x-1, coord.y+1);
			neighborCoord[ 6] = new Coord(coord.x  , coord.y+1);
			neighborCoord[ 7] = new Coord(coord.x+1, coord.y+1);
			neighborCoord[ 8] = new Coord(coord.x-1, coord.y+2);
			neighborCoord[ 9] =
			neighborCoord[10] =
			neighborCoord[11] = null;
			break;
		case 16:
		case 31:
			neighborCoord[ 0] = new Coord(coord.x-1, coord.y-2);
			neighborCoord[ 1] = new Coord(coord.x  , coord.y-2);
			neighborCoord[ 2] = new Coord(coord.x-3, coord.y-1);
			neighborCoord[ 3] = new Coord(coord.x-1, coord.y-1);
			neighborCoord[ 4] = new Coord(coord.x  , coord.y-1);
			neighborCoord[ 5] = new Coord(coord.x-2, coord.y  );
			neighborCoord[ 6] = new Coord(coord.x-1, coord.y  );
			neighborCoord[ 7] = new Coord(coord.x-1, coord.y+1);
			neighborCoord[ 8] = new Coord(coord.x  , coord.y+1);
			neighborCoord[ 9] =
			neighborCoord[10] =
			neighborCoord[11] = null;
			break;
		case 17:
		case 32:
			neighborCoord[ 0] = new Coord(coord.x-1, coord.y-2);
			neighborCoord[ 1] = new Coord(coord.x  , coord.y-2);
			neighborCoord[ 2] = new Coord(coord.x+1, coord.y-2);
			neighborCoord[ 3] = new Coord(coord.x-1, coord.y-1);
			neighborCoord[ 4] = new Coord(coord.x  , coord.y-1);
			neighborCoord[ 5] = new Coord(coord.x+1, coord.y-1);
			neighborCoord[ 6] = new Coord(coord.x+1, coord.y  );
			neighborCoord[ 7] = new Coord(coord.x+2, coord.y  );
			neighborCoord[ 8] = new Coord(coord.x-1, coord.y+1);
			neighborCoord[ 9] = new Coord(coord.x  , coord.y+1);
			neighborCoord[10] = new Coord(coord.x+1, coord.y+1);
			neighborCoord[11] = new Coord(coord.x  , coord.y+2);
			break;
	 	}

		return neighborCoord;
	}

	private PointDouble getOffset() {
		var attr = Attr;
		var a = attr.A;
		var b = attr.B;
		var h = attr.H;

		double oX = 0; // offset X
		double oY = 0; // offset Y

		switch (direction) {
		case  0: case  1: case  2: case  6: case  7: case  8: case 12: case 13: case 14: oX = (h*2+a*3)*(coord.x/6) + h+b;
		                                                                                 oY = (h*2+a*3)*(coord.y/6) + h;       break;
		case  3: case  4: case  5: case  9: case 10: case 11: case 15: case 16: case 17: oX = (h*2+a*3)*(coord.x/6) + h*2+a+b;
		                                                                                 oY = (h*2+a*3)*(coord.y/6) + h+b;     break;
		case 18: case 19: case 20: case 24: case 25: case 26: case 30: case 31: case 32: oX = (h*2+a*3)*(coord.x/6) + h;
		                                                                                 oY = (h*2+a*3)*(coord.y/6) + h*2+a*2; break;
		case 21: case 22: case 23: case 27: case 28: case 29: case 33: case 34: case 35: oX = (h*2+a*3)*(coord.x/6) + h*2+a*2;
		                                                                                 oY = (h*2+a*3)*(coord.y/6) + h*2+a+b; break;
		}
		return new PointDouble(oX, oY);
	}
	
	protected override void CalcRegion() {
		var attr = Attr;
		var a = attr.A;
		var b = attr.B;
		var h = attr.H;

		var o = getOffset();
		switch (direction) {
		case 0: case 21:
			region.SetPoint(0, o.X - b  , o.Y - h  );
			region.SetPoint(3, o.X - b-h, o.Y + b-h);
			region.SetPoint(2, o.X - h  , o.Y + b  );
			region.SetPoint(1, o.X      , o.Y      );
			break;
		case 1: case 22:
			region.SetPoint(0, o.X + b  , o.Y - h  );
			region.SetPoint(2, o.X - b  , o.Y - h  );
			region.SetPoint(1, o.X      , o.Y      );
			break;
		case 2: case 23:
			region.SetPoint(0, o.X + a+b, o.Y - h  );
			region.SetPoint(2, o.X + b  , o.Y - h  );
			region.SetPoint(1, o.X + a  , o.Y      );
			break;
		case 3: case 18:
			region.SetPoint(0, o.X - h+b, o.Y - b-h);
			region.SetPoint(3, o.X - h  , o.Y - b  );
			region.SetPoint(2, o.X      , o.Y      );
			region.SetPoint(1, o.X + b  , o.Y - h  );
			break;
		case 4: case 19:
			region.SetPoint(0, o.X + b  , o.Y - h  );
			region.SetPoint(1, o.X + a  , o.Y      );
			region.SetPoint(2, o.X      , o.Y      );
			break;
		case 5: case 20:
			region.SetPoint(0, o.X + a+b, o.Y - h  );
			region.SetPoint(2, o.X + b  , o.Y - h  );
			region.SetPoint(1, o.X + a  , o.Y      );
			break;
		case 6: case 27:
			region.SetPoint(0, o.X      , o.Y      );
			region.SetPoint(2, o.X - h  , o.Y + b  );
			region.SetPoint(1, o.X      , o.Y + a  );
			break;
		case 7: case 28:
			region.SetPoint(0, o.X + a  , o.Y      );
			region.SetPoint(3, o.X      , o.Y      );
			region.SetPoint(2, o.X      , o.Y + a  );
			region.SetPoint(1, o.X + a  , o.Y + a  );
			break;
		case 8: case 29:
			region.SetPoint(0, o.X + b  , o.Y - h  );
			region.SetPoint(1, o.X + a  , o.Y      );
			region.SetPoint(2, o.X      , o.Y      );
			break;
		case 9: case 24:
			region.SetPoint(0, o.X - h  , o.Y - b  );
			region.SetPoint(1, o.X      , o.Y      );
			region.SetPoint(2, o.X - h  , o.Y + b  );
			break;
		case 10: case 25:
			region.SetPoint(0, o.X + a  , o.Y      );
			region.SetPoint(3, o.X      , o.Y      );
			region.SetPoint(2, o.X      , o.Y + a  );
			region.SetPoint(1, o.X + a  , o.Y + a  );
			break;
		case 11: case 26:
			region.SetPoint(0, o.X + a+b, o.Y - h  );
			region.SetPoint(1, o.X + a+a, o.Y      );
			region.SetPoint(2, o.X + a  , o.Y      );
			break;
		case 12: case 33:
			region.SetPoint(0, o.X - h  , o.Y + b  );
			region.SetPoint(1, o.X      , o.Y + a  );
			region.SetPoint(2, o.X - h  , o.Y + a+b);
			break;
		case 13: case 34:
			region.SetPoint(0, o.X      , o.Y + a  );
			region.SetPoint(2, o.X - h  , o.Y + a+b);
			region.SetPoint(1, o.X      , o.Y + a+a);
			break;
		case 14: case 35:
			region.SetPoint(0, o.X + a  , o.Y + a  );
			region.SetPoint(3, o.X      , o.Y + a  );
			region.SetPoint(2, o.X      , o.Y + a+a);
			region.SetPoint(1, o.X + a  , o.Y + a+a);
			break;
		case 15: case 30:
			region.SetPoint(0, o.X - h  , o.Y + b  );
			region.SetPoint(1, o.X      , o.Y + a  );
			region.SetPoint(2, o.X - h  , o.Y + a+b);
			break;
		case 16: case 31:
			region.SetPoint(0, o.X      , o.Y      );
			region.SetPoint(2, o.X - h  , o.Y + b  );
			region.SetPoint(1, o.X      , o.Y + a  );
			break;
		case 17: case 32:
			region.SetPoint(0, o.X + a+a, o.Y      );
			region.SetPoint(3, o.X + a  , o.Y      );
			region.SetPoint(2, o.X + a  , o.Y + a  );
			region.SetPoint(1, o.X + a+a, o.Y + a  );
			break;
		}
	}

	public override RectDouble getRcInner(int borderWidth) {
		var attr = Attr;
		var a = attr.A;
		var b = attr.B;
		var h = attr.H;
		var w = borderWidth/2.0;
		var sq = attr.GetSq(borderWidth);
		var sq2 = sq/2;
		var wsq2 = w+sq2;

		var o = getOffset();

		var center = new PointDouble(); // координата вписанного в фигуру квадрата (не совпадает с центром фигуры)
		switch (direction) {
		case  0: case 21: center.X = o.X -(b+h)/2; center.Y = o.Y + (b-h)/2; break;
		case  1: case 22: center.X = o.X;          center.Y = o.Y - h+wsq2;  break;
		case  2: case 23: center.X = o.X + a;      center.Y = o.Y - h+wsq2;  break;
		case  3: case 18: center.X = o.X +(b-h)/2; center.Y = o.Y - (b+h)/2; break;
		case  4: case 19: center.X = o.X + b;      center.Y = o.Y - wsq2;    break;
		case  5: case 20: center.X = o.X + a;      center.Y = o.Y - h+wsq2;  break;
		case  6: case 27: center.X = o.X - wsq2;   center.Y = o.Y + b;       break;
		case  7: case 28: center.X = o.X + b;      center.Y = o.Y + b;       break;
		case  8: case 29: center.X = o.X + b;      center.Y = o.Y - wsq2;    break;
		case  9: case 24: center.X = o.X - h+wsq2; center.Y = o.Y;           break;
		case 10: case 25: center.X = o.X + b;      center.Y = o.Y + b;       break;
		case 11: case 26: center.X = o.X + a+b;    center.Y = o.Y - wsq2;    break;
		case 12: case 33: center.X = o.X - h+wsq2; center.Y = o.Y + a;       break;
		case 13: case 34: center.X = o.X - wsq2;   center.Y = o.Y + a+b;     break;
		case 14: case 35: center.X = o.X + b;      center.Y = o.Y + a+b;     break;
		case 15: case 30: center.X = o.X - h+wsq2; center.Y = o.Y + a;       break;
		case 16: case 31: center.X = o.X - wsq2;   center.Y = o.Y + b;       break; 
		case 17: case 32: center.X = o.X + a+b;    center.Y = o.Y + b;       break;
		}

		return new RectDouble(
		   center.X - sq2,
		   center.Y - sq2,
		   sq, sq);
	}

	public override int getShiftPointBorderIndex() {
		switch (direction) {
		case 1: case  2: case  5: case 20: case 22: case 23:
		case 6: case 13: case 16: case 27: case 31: case 34:
			return 1;
		}
		return 2;
	}
}
}