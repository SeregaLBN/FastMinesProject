////////////////////////////////////////////////////////////////////////////////
//                               FastMines project
//                                      � Sergey Krivulya (KSerg, aka SeregaLBN)
// file name: "Trapezoid3.java"
//
// ���������� ������ Trapezoid3 - 8 ��������, �������������� � ������������
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

package ua.ksn.fmg.model.mosaics.cell;

import ua.ksn.geom.Coord;
import ua.ksn.geom.PointDouble;
import ua.ksn.geom.Rect;
import ua.ksn.geom.Size;

/**
 * Trapezoid3 - 8 ��������, �������������� � ������������
 * @see BaseCell
 **/
public class Trapezoid3 extends BaseCell {
	public static class AttrTrapezoid3 extends BaseAttribute {
		public AttrTrapezoid3(int area) {
			super(area);
		}

		@Override
		public Size CalcOwnerSize(Size sizeField, int area) {
			double a = CalcA(area);
			double b = CalcB(area);
			double R = CalcROut(area);
			Size result = new Size(
					(int)(  R *((sizeField.width+1)/2)),
					(int)(a+b *((sizeField.height+1)/2)+
					        a *((sizeField.height+0)/2)));

			if (sizeField.width == 1)
				switch (sizeField.height % 4) {
				case 0: result.height -= a; break;
				case 3: result.height -= a*1.5; break;
				}
			if (sizeField.height == 1)
				if ((sizeField.width & 1) == 1)
					result.width -= CalcRIn(area);

			return result;
		}
	
		@Override
		public int getNeighborNumber() { return 11; }
		@Override
		public int getNeighborNumber(int direction) {
	    	switch (direction) {
	    	case  2: case  5: case 11: case 12: return 6;
	    	case  0: case  7: case  9: case 14: return 10;
	    	case  1: case  3: case  4: case  6:
	    	case  8: case 10: case 13: case 15: return 11;
	    	default:
	    		throw new IllegalArgumentException("Invalid value direction=" + direction);
	    	}
		}
		@Override
		public int getVertexNumber() { return 4; }
		@Override
		public int getVertexNumber(int direction) { return 4; }

		static double vertexIntersection = 0.;
		@Override
		public double getVertexIntersection() {
			if (vertexIntersection < 1) {
				final int cntDirection = GetDirectionCount(); // 0..11
				double sum = 0;
				for (int dir=0; dir<cntDirection; dir++)
					switch (dir) {
			    	case  2: case  5: case 11: case 12:
						sum += (4+4+3+3)/4.;
						break;
			    	case  0: case  7: case  9: case 14:
						sum += (6+6+3+3)/4.;
						break;
			    	case  1: case  3: case  4: case  6:
			    	case  8: case 10: case 13: case 15:
			    		sum += (6+6+4+3)/4.;
						break;
					default:
						throw new RuntimeException("����� case #" + dir);
					}
				vertexIntersection = sum / cntDirection;
//				System.out.println("Trapezoid3::getVertexNeighbor == " + vertexIntersection);
			}
			return vertexIntersection;
		}

		@Override
		public Size GetDirectionSizeField() { return new Size(4, 4); }
		@Override
		protected double CalcA   (int area) { return Math.sqrt(area/SQRT27)*2; }
		protected double CalcB   (int area) { return CalcA(area)*2; }
		protected double CalcC   (int area) { return CalcA(area)/2; }
		protected double CalcROut(int area) { return CalcA(area)*SQRT3; }
		protected double CalcRIn (int area) { return CalcROut(area)/2; }
		@Override
		public double CalcSq(int area, int borderWidth) {
			double w = borderWidth/2.;
			return (CalcA(area)*SQRT3 - w*4)/(SQRT3+1);
		}

		@Override
		public Size sizeIcoField(boolean smallSize) {
			return new Size(4, smallSize ? 2 : 3);
		}
	}

	public Trapezoid3(AttrTrapezoid3 attr, Coord coord) {
		super(attr, coord,
				((coord.y&3)<<2)+(coord.x&3) // 0..15
			);
	}

	@Override
	public AttrTrapezoid3 getAttr() {
		return (AttrTrapezoid3) super.getAttr();
	}

	@Override
	protected Coord[] GetCoordsNeighbor() {
		Coord[] neighborCoord = new Coord[getAttr().getNeighborNumber()];

		// ��������� ���������� �������
    	switch (direction) {
    	case 0:
    		neighborCoord[ 0] = new Coord(coord.x-1, coord.y-1);
    		neighborCoord[ 1] = new Coord(coord.x+1, coord.y-1);
    		neighborCoord[ 2] = new Coord(coord.x-1, coord.y  );
    		neighborCoord[ 3] = new Coord(coord.x+1, coord.y  );
    		neighborCoord[ 4] = new Coord(coord.x-2, coord.y+1);
    		neighborCoord[ 5] = new Coord(coord.x-1, coord.y+1);
    		neighborCoord[ 6] = new Coord(coord.x  , coord.y+1);
    		neighborCoord[ 7] = new Coord(coord.x+1, coord.y+1);
    		neighborCoord[ 8] = new Coord(coord.x-2, coord.y+2);
    		neighborCoord[ 9] = new Coord(coord.x  , coord.y+2);
    		neighborCoord[10] = Coord.INCORRECT_COORD;
    		break;
    	case 1:
    		neighborCoord[ 0] = new Coord(coord.x  , coord.y-2);
    		neighborCoord[ 1] = new Coord(coord.x-2, coord.y-1);
    		neighborCoord[ 2] = new Coord(coord.x  , coord.y-1);
    		neighborCoord[ 3] = new Coord(coord.x+1, coord.y-1);
    		neighborCoord[ 4] = new Coord(coord.x+2, coord.y-1);
    		neighborCoord[ 5] = new Coord(coord.x-2, coord.y  );
    		neighborCoord[ 6] = new Coord(coord.x-1, coord.y  );
    		neighborCoord[ 7] = new Coord(coord.x+1, coord.y  );
    		neighborCoord[ 8] = new Coord(coord.x+2, coord.y  );
    		neighborCoord[ 9] = new Coord(coord.x-2, coord.y+1);
    		neighborCoord[10] = new Coord(coord.x  , coord.y+1);
    		break;
    	case 2:
    		neighborCoord[ 0] = new Coord(coord.x-1, coord.y  );
    		neighborCoord[ 1] = new Coord(coord.x+1, coord.y  );
    		neighborCoord[ 2] = new Coord(coord.x-2, coord.y+1);
    		neighborCoord[ 3] = new Coord(coord.x-1, coord.y+1);
    		neighborCoord[ 4] = new Coord(coord.x  , coord.y+1);
    		neighborCoord[ 5] = new Coord(coord.x+1, coord.y+1);
    		neighborCoord[ 6] =
    		neighborCoord[ 7] =
    		neighborCoord[ 8] =
    		neighborCoord[ 9] =
    		neighborCoord[10] = Coord.INCORRECT_COORD;
    		break;
    	case 3:
    		neighborCoord[ 0] = new Coord(coord.x-2, coord.y-2);
    		neighborCoord[ 1] = new Coord(coord.x-2, coord.y-1);
    		neighborCoord[ 2] = new Coord(coord.x-1, coord.y-1);
    		neighborCoord[ 3] = new Coord(coord.x  , coord.y-1);
    		neighborCoord[ 4] = new Coord(coord.x+2, coord.y-1);
    		neighborCoord[ 5] = new Coord(coord.x-2, coord.y  );
    		neighborCoord[ 6] = new Coord(coord.x-1, coord.y  );
    		neighborCoord[ 7] = new Coord(coord.x+1, coord.y  );
    		neighborCoord[ 8] = new Coord(coord.x+2, coord.y  );
    		neighborCoord[ 9] = new Coord(coord.x-2, coord.y+1);
    		neighborCoord[10] = new Coord(coord.x  , coord.y+1);
    		break;
    	case 4:
    		neighborCoord[ 0] = new Coord(coord.x  , coord.y-1);
    		neighborCoord[ 1] = new Coord(coord.x+2, coord.y-1);
    		neighborCoord[ 2] = new Coord(coord.x-2, coord.y  );
    		neighborCoord[ 3] = new Coord(coord.x-1, coord.y  );
    		neighborCoord[ 4] = new Coord(coord.x+1, coord.y  );
    		neighborCoord[ 5] = new Coord(coord.x+2, coord.y  );
    		neighborCoord[ 6] = new Coord(coord.x-2, coord.y+1);
    		neighborCoord[ 7] = new Coord(coord.x  , coord.y+1);
    		neighborCoord[ 8] = new Coord(coord.x+1, coord.y+1);
    		neighborCoord[ 9] = new Coord(coord.x+2, coord.y+1);
    		neighborCoord[10] = new Coord(coord.x+2, coord.y+2);
    		break;
    	case 5:
    		neighborCoord[ 0] = new Coord(coord.x-1, coord.y-1);
    		neighborCoord[ 1] = new Coord(coord.x  , coord.y-1);
    		neighborCoord[ 2] = new Coord(coord.x+1, coord.y-1);
    		neighborCoord[ 3] = new Coord(coord.x+2, coord.y-1);
    		neighborCoord[ 4] = new Coord(coord.x-1, coord.y  );
    		neighborCoord[ 5] = new Coord(coord.x+1, coord.y  );
    		neighborCoord[ 6] =
    		neighborCoord[ 7] =
    		neighborCoord[ 8] =
    		neighborCoord[ 9] =
    		neighborCoord[10] = Coord.INCORRECT_COORD;
    		break;
    	case 6:
    		neighborCoord[ 0] = new Coord(coord.x  , coord.y-1);
    		neighborCoord[ 1] = new Coord(coord.x+2, coord.y-1);
    		neighborCoord[ 2] = new Coord(coord.x-2, coord.y  );
    		neighborCoord[ 3] = new Coord(coord.x-1, coord.y  );
    		neighborCoord[ 4] = new Coord(coord.x+1, coord.y  );
    		neighborCoord[ 5] = new Coord(coord.x+2, coord.y  );
    		neighborCoord[ 6] = new Coord(coord.x-2, coord.y+1);
    		neighborCoord[ 7] = new Coord(coord.x-1, coord.y+1);
    		neighborCoord[ 8] = new Coord(coord.x  , coord.y+1);
    		neighborCoord[ 9] = new Coord(coord.x+2, coord.y+1);
    		neighborCoord[10] = new Coord(coord.x  , coord.y+2);
    		break;
    	case 7:
    		neighborCoord[ 0] = new Coord(coord.x  , coord.y-2);
    		neighborCoord[ 1] = new Coord(coord.x+2, coord.y-2);
    		neighborCoord[ 2] = new Coord(coord.x-1, coord.y-1);
    		neighborCoord[ 3] = new Coord(coord.x  , coord.y-1);
    		neighborCoord[ 4] = new Coord(coord.x+1, coord.y-1);
    		neighborCoord[ 5] = new Coord(coord.x+2, coord.y-1);
    		neighborCoord[ 6] = new Coord(coord.x-1, coord.y  );
    		neighborCoord[ 7] = new Coord(coord.x+1, coord.y  );
    		neighborCoord[ 8] = new Coord(coord.x-1, coord.y+1);
    		neighborCoord[ 9] = new Coord(coord.x+1, coord.y+1);
    		neighborCoord[10] = Coord.INCORRECT_COORD;
    		break;
    	case 8:
    		neighborCoord[ 0] = new Coord(coord.x  , coord.y-2);
    		neighborCoord[ 1] = new Coord(coord.x-2, coord.y-1);
    		neighborCoord[ 2] = new Coord(coord.x-1, coord.y-1);
    		neighborCoord[ 3] = new Coord(coord.x  , coord.y-1);
    		neighborCoord[ 4] = new Coord(coord.x+2, coord.y-1);
    		neighborCoord[ 5] = new Coord(coord.x-2, coord.y  );
    		neighborCoord[ 6] = new Coord(coord.x-1, coord.y  );
    		neighborCoord[ 7] = new Coord(coord.x+1, coord.y  );
    		neighborCoord[ 8] = new Coord(coord.x+2, coord.y  );
    		neighborCoord[ 9] = new Coord(coord.x  , coord.y+1);
    		neighborCoord[10] = new Coord(coord.x+2, coord.y+1);
    		break;
    	case 9:
    		neighborCoord[ 0] = new Coord(coord.x-1, coord.y-1);
    		neighborCoord[ 1] = new Coord(coord.x+1, coord.y-1);
    		neighborCoord[ 2] = new Coord(coord.x-1, coord.y  );
    		neighborCoord[ 3] = new Coord(coord.x+1, coord.y  );
    		neighborCoord[ 4] = new Coord(coord.x-1, coord.y+1);
    		neighborCoord[ 5] = new Coord(coord.x  , coord.y+1);
    		neighborCoord[ 6] = new Coord(coord.x+1, coord.y+1);
    		neighborCoord[ 7] = new Coord(coord.x+2, coord.y+1);
    		neighborCoord[ 8] = new Coord(coord.x  , coord.y+2);
    		neighborCoord[ 9] = new Coord(coord.x+2, coord.y+2);
    		neighborCoord[10] = Coord.INCORRECT_COORD;
    		break;
    	case 10:
    		neighborCoord[ 0] = new Coord(coord.x+2, coord.y-2);
    		neighborCoord[ 1] = new Coord(coord.x-2, coord.y-1);
    		neighborCoord[ 2] = new Coord(coord.x  , coord.y-1);
    		neighborCoord[ 3] = new Coord(coord.x+1, coord.y-1);
    		neighborCoord[ 4] = new Coord(coord.x+2, coord.y-1);
    		neighborCoord[ 5] = new Coord(coord.x-2, coord.y  );
    		neighborCoord[ 6] = new Coord(coord.x-1, coord.y  );
    		neighborCoord[ 7] = new Coord(coord.x+1, coord.y  );
    		neighborCoord[ 8] = new Coord(coord.x+2, coord.y  );
    		neighborCoord[ 9] = new Coord(coord.x  , coord.y+1);
    		neighborCoord[10] = new Coord(coord.x+2, coord.y+1);
    		break;
    	case 11:
    		neighborCoord[ 0] = new Coord(coord.x-1, coord.y  );
    		neighborCoord[ 1] = new Coord(coord.x+1, coord.y  );
    		neighborCoord[ 2] = new Coord(coord.x-1, coord.y+1);
    		neighborCoord[ 3] = new Coord(coord.x  , coord.y+1);
    		neighborCoord[ 4] = new Coord(coord.x+1, coord.y+1);
    		neighborCoord[ 5] = new Coord(coord.x+2, coord.y+1);
    		neighborCoord[ 6] =
    		neighborCoord[ 7] =
    		neighborCoord[ 8] =
    		neighborCoord[ 9] =
    		neighborCoord[10] = Coord.INCORRECT_COORD;
    		break;
    	case 12:
    		neighborCoord[ 0] = new Coord(coord.x-2, coord.y-1);
    		neighborCoord[ 1] = new Coord(coord.x-1, coord.y-1);
    		neighborCoord[ 2] = new Coord(coord.x  , coord.y-1);
    		neighborCoord[ 3] = new Coord(coord.x+1, coord.y-1);
    		neighborCoord[ 4] = new Coord(coord.x-1, coord.y  );
    		neighborCoord[ 5] = new Coord(coord.x+1, coord.y  );
    		neighborCoord[ 6] =
    		neighborCoord[ 7] =
    		neighborCoord[ 8] =
    		neighborCoord[ 9] =
    		neighborCoord[10] = Coord.INCORRECT_COORD;
    		break;
    	case 13:
    		neighborCoord[ 0] = new Coord(coord.x-2, coord.y-1);
    		neighborCoord[ 1] = new Coord(coord.x  , coord.y-1);
    		neighborCoord[ 2] = new Coord(coord.x-2, coord.y  );
    		neighborCoord[ 3] = new Coord(coord.x-1, coord.y  );
    		neighborCoord[ 4] = new Coord(coord.x+1, coord.y  );
    		neighborCoord[ 5] = new Coord(coord.x+2, coord.y  );
    		neighborCoord[ 6] = new Coord(coord.x-2, coord.y+1);
    		neighborCoord[ 7] = new Coord(coord.x-1, coord.y+1);
    		neighborCoord[ 8] = new Coord(coord.x  , coord.y+1);
    		neighborCoord[ 9] = new Coord(coord.x+2, coord.y+1);
    		neighborCoord[10] = new Coord(coord.x-2, coord.y+2);
    		break;
    	case 14:
    		neighborCoord[ 0] = new Coord(coord.x-2, coord.y-2);
    		neighborCoord[ 1] = new Coord(coord.x  , coord.y-2);
    		neighborCoord[ 2] = new Coord(coord.x-2, coord.y-1);
    		neighborCoord[ 3] = new Coord(coord.x-1, coord.y-1);
    		neighborCoord[ 4] = new Coord(coord.x  , coord.y-1);
    		neighborCoord[ 5] = new Coord(coord.x+1, coord.y-1);
    		neighborCoord[ 6] = new Coord(coord.x-1, coord.y  );
    		neighborCoord[ 7] = new Coord(coord.x+1, coord.y  );
    		neighborCoord[ 8] = new Coord(coord.x-1, coord.y+1);
    		neighborCoord[ 9] = new Coord(coord.x+1, coord.y+1);
    		neighborCoord[10] = Coord.INCORRECT_COORD;
    		break;
    	case 15:
    		neighborCoord[ 0] = new Coord(coord.x-2, coord.y-1);
    		neighborCoord[ 1] = new Coord(coord.x  , coord.y-1);
    		neighborCoord[ 2] = new Coord(coord.x-2, coord.y  );
    		neighborCoord[ 3] = new Coord(coord.x-1, coord.y  );
    		neighborCoord[ 4] = new Coord(coord.x+1, coord.y  );
    		neighborCoord[ 5] = new Coord(coord.x+2, coord.y  );
    		neighborCoord[ 6] = new Coord(coord.x-2, coord.y+1);
    		neighborCoord[ 7] = new Coord(coord.x  , coord.y+1);
    		neighborCoord[ 8] = new Coord(coord.x+1, coord.y+1);
    		neighborCoord[ 9] = new Coord(coord.x+2, coord.y+1);
    		neighborCoord[10] = new Coord(coord.x  , coord.y+2);
    		break;
    	}

		return neighborCoord;
	}

	@Override
	protected void CalcRegion() {
		AttrTrapezoid3 attr = getAttr();
		int area = attr.getArea();
		double a = attr.CalcA(area);
		double b = attr.CalcB(area);
		double c = attr.CalcC(area);
		double R = attr.CalcROut(area);
		double r = attr.CalcRIn(area);

		// ����������� ��������� ����� ������
		double oX = (R*2)*(coord.x/4) + R; // offset X
		double oY = (a*6)*(coord.y/4) + a + b; // offset Y

		switch (direction) {
		case 0:
			region.getPoint(0).x = (int)(oX - r); region.getPoint(0).y = (int)(oY - a-c);
			region.getPoint(1).x = (int)(oX - r); region.getPoint(1).y = (int)(oY - c  );
			region.getPoint(2).x = (int)(oX - R); region.getPoint(2).y = (int)(oY      );
			region.getPoint(3).x = (int)(oX - R); region.getPoint(3).y = (int)(oY - b  );
			break;
		case 1:
			region.getPoint(0).x = (int)(oX    ); region.getPoint(0).y = (int)(oY - a-b);
			region.getPoint(1).x = (int)(oX    ); region.getPoint(1).y = (int)(oY - b  );
			region.getPoint(2).x = (int)(oX - r); region.getPoint(2).y = (int)(oY - a-c);
			region.getPoint(3).x = (int)(oX - R); region.getPoint(3).y = (int)(oY - b  );
			break;
		case 2:
			region.getPoint(0).x = (int)(oX + r); region.getPoint(0).y = (int)(oY - a-c);
			region.getPoint(1).x = (int)(oX + r); region.getPoint(1).y = (int)(oY - c  );
			region.getPoint(2).x = (int)(oX    ); region.getPoint(2).y = (int)(oY      );
			region.getPoint(3).x = (int)(oX    ); region.getPoint(3).y = (int)(oY - b  );
			break;
		case 3:
			region.getPoint(0).x = (int)(oX + R); region.getPoint(0).y = (int)(oY - b  );
			region.getPoint(1).x = (int)(oX + r); region.getPoint(1).y = (int)(oY - a-c);
			region.getPoint(2).x = (int)(oX    ); region.getPoint(2).y = (int)(oY - b  );
			region.getPoint(3).x = (int)(oX    ); region.getPoint(3).y = (int)(oY - a-b);
			break;
		case 4:
			region.getPoint(0).x = (int)(oX    ); region.getPoint(0).y = (int)(oY      );
			region.getPoint(1).x = (int)(oX    ); region.getPoint(1).y = (int)(oY + a  );
			region.getPoint(2).x = (int)(oX - R); region.getPoint(2).y = (int)(oY      );
			region.getPoint(3).x = (int)(oX - r); region.getPoint(3).y = (int)(oY - c  );
			break;
		case 5:
			region.getPoint(0).x = (int)(oX    ); region.getPoint(0).y = (int)(oY - b  );
			region.getPoint(1).x = (int)(oX    ); region.getPoint(1).y = (int)(oY      );
			region.getPoint(2).x = (int)(oX - r); region.getPoint(2).y = (int)(oY - c  );
			region.getPoint(3).x = (int)(oX - r); region.getPoint(3).y = (int)(oY - a-c);
			break;
		case 6:
			region.getPoint(0).x = (int)(oX + R); region.getPoint(0).y = (int)(oY      );
			region.getPoint(1).x = (int)(oX    ); region.getPoint(1).y = (int)(oY + a  );
			region.getPoint(2).x = (int)(oX    ); region.getPoint(2).y = (int)(oY      );
			region.getPoint(3).x = (int)(oX + r); region.getPoint(3).y = (int)(oY - c  );
			break;
		case 7:
			region.getPoint(0).x = (int)(oX + R); region.getPoint(0).y = (int)(oY - b  );
			region.getPoint(1).x = (int)(oX + R); region.getPoint(1).y = (int)(oY      );
			region.getPoint(2).x = (int)(oX + r); region.getPoint(2).y = (int)(oY - c  );
			region.getPoint(3).x = (int)(oX + r); region.getPoint(3).y = (int)(oY - a-c);
			break;
		case 8:
			region.getPoint(0).x = (int)(oX    ); region.getPoint(0).y = (int)(oY + a  );
			region.getPoint(1).x = (int)(oX - r); region.getPoint(1).y = (int)(oY + a+c);
			region.getPoint(2).x = (int)(oX - R); region.getPoint(2).y = (int)(oY + a  );
			region.getPoint(3).x = (int)(oX - R); region.getPoint(3).y = (int)(oY      );
			break;
		case 9:
			region.getPoint(0).x = (int)(oX    ); region.getPoint(0).y = (int)(oY + a  );
			region.getPoint(1).x = (int)(oX    ); region.getPoint(1).y = (int)(oY + a+b);
			region.getPoint(2).x = (int)(oX - r); region.getPoint(2).y = (int)(oY + b+c);
			region.getPoint(3).x = (int)(oX - r); region.getPoint(3).y = (int)(oY + a+c);
			break;
		case 10:
			region.getPoint(0).x = (int)(oX + R); region.getPoint(0).y = (int)(oY      );
			region.getPoint(1).x = (int)(oX + R); region.getPoint(1).y = (int)(oY + a  );
			region.getPoint(2).x = (int)(oX + r); region.getPoint(2).y = (int)(oY + a+c);
			region.getPoint(3).x = (int)(oX    ); region.getPoint(3).y = (int)(oY + a  );
			break;
		case 11:
			region.getPoint(0).x = (int)(oX + R); region.getPoint(0).y = (int)(oY + a  );
			region.getPoint(1).x = (int)(oX + R); region.getPoint(1).y = (int)(oY + a+b);
			region.getPoint(2).x = (int)(oX + r); region.getPoint(2).y = (int)(oY + b+c);
			region.getPoint(3).x = (int)(oX + r); region.getPoint(3).y = (int)(oY + a+c);
			break;
		case 12:
			region.getPoint(0).x = (int)(oX - r); region.getPoint(0).y = (int)(oY + a+c);
			region.getPoint(1).x = (int)(oX - r); region.getPoint(1).y = (int)(oY + b+c);
			region.getPoint(2).x = (int)(oX - R); region.getPoint(2).y = (int)(oY + a+b);
			region.getPoint(3).x = (int)(oX - R); region.getPoint(3).y = (int)(oY + a  );
			break;
		case 13:
			region.getPoint(0).x = (int)(oX    ); region.getPoint(0).y = (int)(oY + a+b);
			region.getPoint(1).x = (int)(oX - R); region.getPoint(1).y = (int)(oY + b*2);
			region.getPoint(2).x = (int)(oX - R); region.getPoint(2).y = (int)(oY + a+b);
			region.getPoint(3).x = (int)(oX - r); region.getPoint(3).y = (int)(oY + b+c);
			break;
		case 14:
			region.getPoint(0).x = (int)(oX + r); region.getPoint(0).y = (int)(oY + a+c);
			region.getPoint(1).x = (int)(oX + r); region.getPoint(1).y = (int)(oY + b+c);
			region.getPoint(2).x = (int)(oX    ); region.getPoint(2).y = (int)(oY + a+b);
			region.getPoint(3).x = (int)(oX    ); region.getPoint(3).y = (int)(oY + a  );
			break;
		case 15:
			region.getPoint(0).x = (int)(oX + R); region.getPoint(0).y = (int)(oY + a+b);
			region.getPoint(1).x = (int)(oX + R); region.getPoint(1).y = (int)(oY + b*2);
			region.getPoint(2).x = (int)(oX    ); region.getPoint(2).y = (int)(oY + a+b);
			region.getPoint(3).x = (int)(oX + r); region.getPoint(3).y = (int)(oY + b+c);
			break;
		}
	}

	@Override
	public Rect getRcInner(int borderWidth) {
		AttrTrapezoid3 attr = getAttr();
		int area = attr.getArea();
		double a = attr.CalcA(area);
		double b = attr.CalcB(area);
		double c = attr.CalcC(area);
		double R = attr.CalcROut(area);
		double r = attr.CalcRIn(area);
//		double w = borderWidth/2.;
		double sq  = attr.CalcSq(area, borderWidth);
		double sq2 = sq/2;

		double oX = (R*2)*(coord.x/4) + R; // offset X
		double oY = (a*6)*(coord.y/4) + a + b; // offset Y

		PointDouble center = new PointDouble(); // ���������� ������ ��������
		switch (direction) {
		case 0:  center.x = oX - r*1.50; center.y = oY - a;      break;
		case 1:  center.x = oX - r*0.75; center.y = oY - c*4.25; break;
		case 2:  center.x = oX + r*0.50; center.y = oY - a;      break;
		case 3:  center.x = oX + r*0.75; center.y = oY - c*4.25; break;
		case 4:  center.x = oX - r*0.75; center.y = oY + c*0.25; break;
		case 5:  center.x = oX - r*0.50; center.y = oY - a;      break;
		case 6:  center.x = oX + r*0.75; center.y = oY + c*0.25; break;
		case 7:  center.x = oX + r*1.50; center.y = oY - a;      break;
		case 8:  center.x = oX - r*1.25; center.y = oY + c*1.75; break;
		case 9:  center.x = oX - r*0.50; center.y = oY + b;      break;
		case 10: center.x = oX + r*1.25; center.y = oY + c*1.75; break;
		case 11: center.x = oX + r*1.50; center.y = oY + b;      break;
		case 12: center.x = oX - r*1.50; center.y = oY + b;      break;
		case 13: center.x = oX - r*1.25; center.y = oY + c*6.25; break;
		case 14: center.x = oX + r*0.50; center.y = oY + b;      break;
		case 15: center.x = oX + r*1.25; center.y = oY + c*6.25; break;
		}

		Rect square = new Rect();
		square.x = (int) (center.x - sq2);
		square.y = (int) (center.y - sq2);
		square.width =
		square.height = (int) sq;
		return square;
	}

	@Override
	public int getShiftPointBorderIndex() {
		switch (direction) {
		case 1: case 10: return 3;
		case 6: case 13: return 1;
		}
		return 2;
	}
}