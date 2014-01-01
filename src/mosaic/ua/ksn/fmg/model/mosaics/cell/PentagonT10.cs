////////////////////////////////////////////////////////////////////////////////
//                               FastMines project
//                                      © Sergey Krivulya (KSerg, aka SeregaLBN)
// file name: "PentagonT10.java"
//
// Реализация класса PentagonT10 - 5-ти угольник, тип №10
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
using System.Collections.Generic;
using ua.ksn.geom;
 
namespace ua.ksn.fmg.model.mosaics.cell {

/// <summary> Пятиугольник. Тип №10 </summary>
public class PentagonT10 : BaseCell {
	public class AttrPentagonT10 : BaseAttribute {
		public AttrPentagonT10(int area)
			: base(area)
      {}

		public override Size CalcOwnerSize(Size sizeField, int area) {
			double a = CalcA(area);
			Size result = new Size(
					(int)(2*a +
					      5*a*((sizeField.width+1)/2) +
					        a*((sizeField.width+0)/2)),
					(int)(2*a +
					      3*a*((sizeField.height+2)/3) +
					      3*a*((sizeField.height+1)/3) +
					        a*((sizeField.height+0)/3)));

			if (sizeField.height == 1)
				if ((sizeField.width & 1) == 1)
					result.width -= (int)(3*a);
				else
					result.width -= (int)a;
			if (sizeField.height == 2)
				if ((sizeField.width & 1) == 1)
					result.width -= (int)(2*a);
				else
					result.width -= (int)a;

			if (sizeField.width == 1)
				if (((sizeField.height % 6) == 4) ||
					((sizeField.height % 6) == 5))
					result.height -= (int)(2*a);

			return result;
		}
	
		public override int getNeighborNumber() { return 7; }
		public override int getNeighborNumber(int direction) {
			switch (direction) {
			case 0: case 1: case 6: case 7: return 7;                              
			case 2: case 3: case 4: case 5: case 8: case 9: case 10: case 11: return 6;
			default:
				throw new ArgumentException("Invalid value direction=" + direction);
			}
		}
		public override int getVertexNumber() { return 5; }
		public override int getVertexNumber(int direction) { return 5; }

		static double vertexIntersection = 0.0;
		public override double getVertexIntersection() {
			if (vertexIntersection < 1) {
				int cntDirection = GetDirectionCount(); // 0..11
				double sum = 0;
				for (int dir=0; dir<cntDirection; dir++)
					switch (dir) {
					case 0: case 1: case 6: case 7:                              
						sum += 3;
						break;
					case 2: case 3: case 4: case 5: case 8: case 9: case 10: case 11:
						sum += 16.0/5.0;
						break;
					default:
						throw new Exception("Забыл case #" + dir);
					}
				vertexIntersection = sum / cntDirection;
//				System.out.println("PentagonT10::getVertexNeighbor == " + vertexIntersection);
			}
			return vertexIntersection;
		}

		public override Size GetDirectionSizeField() { return new Size(2, 6); }
		public override double CalcA(int area) { return Math.Sqrt(area/7); }
		public override double CalcSq(int area, int borderWidth) {
			double w = borderWidth/2.0;
			return 2*(CalcA(area)-w);
		}

		public override int getMaxBackgroundFillModeValue() {
			return base.getMaxBackgroundFillModeValue() + 1;
//			return 1;
		}

		public override Size sizeIcoField(bool smallSize) {
			return new Size(3, smallSize ? 2 : 3);
		}
	}

	public PentagonT10(AttrPentagonT10 attr, Coord coord)
		: base(attr, coord,
				((coord.y%6)<<1) + (coord.x&1) // 0..11
			)
	{}

	private AttrPentagonT10 Attr {
		get { return (AttrPentagonT10) base.Attr; }
	}

	protected override Coord[] GetCoordsNeighbor() {
		Coord[] neighborCoord = new Coord[Attr.getNeighborNumber()];

		// определяю координаты соседей
		switch (direction) {
		case 0:
			neighborCoord[0] = new Coord(coord.x  , coord.y-1);
			neighborCoord[1] = new Coord(coord.x-1, coord.y  );
			neighborCoord[2] = new Coord(coord.x+1, coord.y  );
			neighborCoord[3] = new Coord(coord.x-1, coord.y+1);
			neighborCoord[4] = new Coord(coord.x  , coord.y+1);
			neighborCoord[5] = new Coord(coord.x+1, coord.y+1);
			neighborCoord[6] = new Coord(coord.x-1, coord.y+2);
			break;                              
		case 1:                                
			neighborCoord[0] = new Coord(coord.x  , coord.y-2);
			neighborCoord[1] = new Coord(coord.x-1, coord.y-1);
			neighborCoord[2] = new Coord(coord.x  , coord.y-1);
			neighborCoord[3] = new Coord(coord.x+1, coord.y-1);
			neighborCoord[4] = new Coord(coord.x-1, coord.y  );
			neighborCoord[5] = new Coord(coord.x+1, coord.y  );
			neighborCoord[6] = new Coord(coord.x  , coord.y+1);
			break;                              
		case 2:                                
			neighborCoord[0] = new Coord(coord.x  , coord.y-1);
			neighborCoord[1] = new Coord(coord.x+1, coord.y  );
			neighborCoord[2] = new Coord(coord.x-1, coord.y+1);
			neighborCoord[3] = new Coord(coord.x  , coord.y+1);
			neighborCoord[4] = new Coord(coord.x+1, coord.y+1);
			neighborCoord[5] = new Coord(coord.x  , coord.y+2);
			neighborCoord[6] = Coord.INCORRECT_COORD;
			break;                              
		case 3:                                
			neighborCoord[0] = new Coord(coord.x-1, coord.y-1);
			neighborCoord[1] = new Coord(coord.x  , coord.y-1);
			neighborCoord[2] = new Coord(coord.x+1, coord.y-1);
			neighborCoord[3] = new Coord(coord.x-1, coord.y  );
			neighborCoord[4] = new Coord(coord.x-1, coord.y+1);
			neighborCoord[5] = new Coord(coord.x  , coord.y+1);
			neighborCoord[6] = Coord.INCORRECT_COORD;
			break;                              
		case 4:                                
			neighborCoord[0] = new Coord(coord.x  , coord.y-1);
			neighborCoord[1] = new Coord(coord.x+1, coord.y-1);
			neighborCoord[2] = new Coord(coord.x+1, coord.y  );
			neighborCoord[3] = new Coord(coord.x  , coord.y+1);
			neighborCoord[4] = new Coord(coord.x+1, coord.y+1);
			neighborCoord[5] = new Coord(coord.x+2, coord.y+1);
			neighborCoord[6] = Coord.INCORRECT_COORD;
			break;                              
		case 5:                                
			neighborCoord[0] = new Coord(coord.x+1, coord.y-2);
			neighborCoord[1] = new Coord(coord.x-1, coord.y-1);
			neighborCoord[2] = new Coord(coord.x  , coord.y-1);
			neighborCoord[3] = new Coord(coord.x+1, coord.y-1);
			neighborCoord[4] = new Coord(coord.x-1, coord.y  );
			neighborCoord[5] = new Coord(coord.x+1, coord.y+1);
			neighborCoord[6] = Coord.INCORRECT_COORD;
			break;                              
		case 6:                                
			neighborCoord[0] = new Coord(coord.x  , coord.y-2);
			neighborCoord[1] = new Coord(coord.x-2, coord.y-1);
			neighborCoord[2] = new Coord(coord.x-1, coord.y-1);
			neighborCoord[3] = new Coord(coord.x  , coord.y-1);
			neighborCoord[4] = new Coord(coord.x-1, coord.y  );
			neighborCoord[5] = new Coord(coord.x+1, coord.y  );
			neighborCoord[6] = new Coord(coord.x  , coord.y+1);
			break;                              
		case 7:                                
			neighborCoord[0] = new Coord(coord.x-1, coord.y-1);
			neighborCoord[1] = new Coord(coord.x-1, coord.y  );
			neighborCoord[2] = new Coord(coord.x+1, coord.y  );
			neighborCoord[3] = new Coord(coord.x-1, coord.y+1);
			neighborCoord[4] = new Coord(coord.x  , coord.y+1);
			neighborCoord[5] = new Coord(coord.x+1, coord.y+1);
			neighborCoord[6] = new Coord(coord.x  , coord.y+2);
			break;                              
		case 8:                                
			neighborCoord[0] = new Coord(coord.x-1, coord.y-1);
			neighborCoord[1] = new Coord(coord.x  , coord.y-1);
			neighborCoord[2] = new Coord(coord.x+1, coord.y-1);
			neighborCoord[3] = new Coord(coord.x+1, coord.y  );
			neighborCoord[4] = new Coord(coord.x-1, coord.y+1);
			neighborCoord[5] = new Coord(coord.x  , coord.y+1);
			neighborCoord[6] = Coord.INCORRECT_COORD;
			break;                              
		case 9:                                
			neighborCoord[0] = new Coord(coord.x  , coord.y-1);
			neighborCoord[1] = new Coord(coord.x-1, coord.y  );
			neighborCoord[2] = new Coord(coord.x-2, coord.y+1);
			neighborCoord[3] = new Coord(coord.x-1, coord.y+1);
			neighborCoord[4] = new Coord(coord.x  , coord.y+1);
			neighborCoord[5] = new Coord(coord.x  , coord.y+2);
			neighborCoord[6] = Coord.INCORRECT_COORD;
			break;                              
		case 10:                               
			neighborCoord[0] = new Coord(coord.x  , coord.y-1);
			neighborCoord[1] = new Coord(coord.x+1, coord.y-1);
			neighborCoord[2] = new Coord(coord.x-1, coord.y  );
			neighborCoord[3] = new Coord(coord.x-1, coord.y+1);
			neighborCoord[4] = new Coord(coord.x  , coord.y+1);
			neighborCoord[5] = new Coord(coord.x+1, coord.y+1);
			neighborCoord[6] = Coord.INCORRECT_COORD;
			break;                              
		case 11:                               
			neighborCoord[0] = new Coord(coord.x  , coord.y-2);
			neighborCoord[1] = new Coord(coord.x  , coord.y-1);
			neighborCoord[2] = new Coord(coord.x+1, coord.y-1);
			neighborCoord[3] = new Coord(coord.x+2, coord.y-1);
			neighborCoord[4] = new Coord(coord.x+1, coord.y  );
			neighborCoord[5] = new Coord(coord.x  , coord.y+1);
			neighborCoord[6] = Coord.INCORRECT_COORD;
			break;
		}

		return neighborCoord;
	}

	private PointDouble getOffset() {
		AttrPentagonT10 attr = Attr;
		double a = attr.CalcA(attr.Area);

		PointDouble o = new PointDouble(0,0);
		switch (direction) {
		case 0: case 6: case  8: case 9: case 10:          o.x = a*2+a*6*((coord.x+0)/2); break;
		case 1: case 2: case  3: case 4: case 5: case 7:   o.x = a*5+a*6*((coord.x+0)/2); break;
		case 11:                                           o.x = a*2+a*6*((coord.x+1)/2); break;
		}
		switch (direction) {
		case 0:                                            o.y = a*5 +a*14*(coord.y/6);   break;
		case 1:                                            o.y =      a*14*(coord.y/6);   break;
		case 2: case 3: case  4: case 5:                   o.y = a*6 +a*14*(coord.y/6);   break;
		case 6:                                            o.y = a*7 +a*14*(coord.y/6);   break;
		case 7:                                            o.y = a*12+a*14*(coord.y/6);   break;
		case 8: case 9: case 10: case 11:                  o.y = a*13+a*14*(coord.y/6);   break;
		}
		return o;
	}

	protected override void CalcRegion() {
		AttrPentagonT10 attr = Attr;
		double a = attr.CalcA(attr.Area);

		PointDouble o = getOffset();

		switch (direction) {
		case 0: case 3: case 7: case 8:
			region.setPoint(0, (int)(o.x + a  ), (int)(o.y - a*3));
			region.setPoint(1, (int)(o.x + a*2), (int)(o.y - a*2));
			region.setPoint(2, (int)(o.x      ), (int)(o.y      ));
			region.setPoint(3, (int)(o.x - a*2), (int)(o.y - a*2));
			region.setPoint(4, (int)(o.x - a  ), (int)(o.y - a*3));
			break;
		case 1: case 4: case 6: case 10:
			region.setPoint(0, (int)(o.x      ), (int)(o.y      ));
			region.setPoint(1, (int)(o.x + a*2), (int)(o.y + a*2));
			region.setPoint(2, (int)(o.x + a  ), (int)(o.y + a*3));
			region.setPoint(3, (int)(o.x - a  ), (int)(o.y + a*3));
			region.setPoint(4, (int)(o.x - a*2), (int)(o.y + a*2));
			break;
		case 2: case 11:
			region.setPoint(0, (int)(o.x - a*2), (int)(o.y - a*2));
			region.setPoint(1, (int)(o.x      ), (int)(o.y      ));
			region.setPoint(2, (int)(o.x - a*2), (int)(o.y + a*2));
			region.setPoint(3, (int)(o.x - a*3), (int)(o.y + a  ));
			region.setPoint(4, (int)(o.x - a*3), (int)(o.y - a  ));
			break;
		case 5: case 9:
			region.setPoint(0, (int)(o.x + a*2), (int)(o.y - a*2));
			region.setPoint(1, (int)(o.x + a*3), (int)(o.y - a  ));
			region.setPoint(2, (int)(o.x + a*3), (int)(o.y + a  ));
			region.setPoint(3, (int)(o.x + a*2), (int)(o.y + a*2));
			region.setPoint(4, (int)(o.x      ), (int)(o.y      ));
			break;
		}
	}

	public override Rect getRcInner(int borderWidth) {
		AttrPentagonT10 attr = Attr;
		double sq = attr.CalcSq(attr.Area, borderWidth);
		double sq2 = sq/2;

		PointDouble center = new PointDouble(); // координата центра квадрата
		switch (direction) {
		case 0: case  3: case 7: case  8: center.x = region.getPoint(2).x; center.y = region.getPoint(1).y; break;
		case 1: case  4: case 6: case 10: 
		case 2: case 11:                  center.x = region.getPoint(0).x; center.y = region.getPoint(1).y; break;
		case 5: case  9:                  center.x = region.getPoint(0).x; center.y = region.getPoint(4).y; break; 
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
		case 1: case 4: case 5: case 6: case 9: case 10:
			return 3;
		}
		return 2;
	}

	public override Color getBackgroundFillColor(int fillMode, Color defaultColor, IDictionary<int, Color> repositoryColor) {
		if (fillMode == Attr.getMaxBackgroundFillModeValue())
		{
			switch (getDirection()) {
			case  2: case  3: case  4: case  5: return repositoryColor[0];
			case  8: case  9: case 10: case 11: return repositoryColor[1];
			case  1: case  7: return repositoryColor[2];
			case  0: case  6: return repositoryColor[3];
//			default:
//				return repositoryColor.get(-1);
			}
		}
		return base.getBackgroundFillColor(fillMode, defaultColor, repositoryColor);
	}
}
}