////////////////////////////////////////////////////////////////////////////////
//                               FastMines project
//                                      © Sergey Krivulya (KSerg, aka SeregaLBN)
// file name: "Triangle4.java"
//
// Реализация класса Triangle4 - треугольник 30°-30°-120°
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
 * Треугольник. Вариант 4 - треугольник 30°-30°-120°
 * @see BaseCell
 **/
public class Triangle4 extends BaseCell {
	public static class AttrTriangle4 extends BaseAttribute {
		public AttrTriangle4(int area) {
			super(area);
		}

		@Override
		public Size CalcOwnerSize(Size sizeField, int area) {
			double b = CalcB(area);
			double r = CalcRIn(area);
			double R = CalcROut(area);
			Size result = new Size(
					(int)( b+b *((sizeField.width+2)/3) +
					         b *((sizeField.width+0)/3)),
					(int)((R+r)*((sizeField.height+1)/2)));

			if (sizeField.width == 1)
				if ((sizeField.height % 4) == 3)
					result.height -= R;
			if (sizeField.height == 1)
				if ((sizeField.width % 3) == 1)
					result.width -= b;

			return result;
		}
	
		@Override
		public int getNeighborNumber() { return 21; }
		@Override
		public int getNeighborNumber(int direction) { return 21; }
		@Override
		public int getVertexNumber() { return 3; }
		@Override
		public int getVertexNumber(int direction) { return 3; }
		@Override
		public double getVertexIntersection() { return 9.; } // (12+12+3)/3.
		@Override
		public Size GetDirectionSizeField() { return new Size(3, 4); }
		@Override
		protected double CalcA   (int area) { return Math.sqrt(area*SQRT48); }
		protected double CalcB   (int area) { return CalcA(area)/2; }
		protected double CalcROut(int area) { return CalcA(area)/SQRT3; }
		protected double CalcRIn (int area) { return CalcROut(area)/2; }
		@Override
		public double CalcSq(int area, int borderWidth) {
			double w = borderWidth/2.;
			return (CalcA(area)-w*2/TAN15)/(SQRT3+3);
		}

//		@Override
//		public int getMaxBackgroundFillModeValue() {
//			return super.getMaxBackgroundFillModeValue()+1;
//		}
	}

	public Triangle4(AttrTriangle4 attr, Coord coord) {
		super(attr, coord,
				(coord.y&3)*3+(coord.x%3) // 0..11
			);
	}

	@Override
	public AttrTriangle4 getAttr() {
		return (AttrTriangle4) super.getAttr();
	}

	@Override
	protected Coord[] GetCoordsNeighbor() {
		Coord[] neighborCoord = new Coord[getAttr().getNeighborNumber()];

		// определяю координаты соседей
		switch (direction) {
		case 0:
			neighborCoord[ 0] = new Coord(coord.x-1, coord.y-2);
			neighborCoord[ 1] = new Coord(coord.x+1, coord.y-2);
			neighborCoord[ 2] = new Coord(coord.x-1, coord.y-1);
			neighborCoord[ 3] = new Coord(coord.x  , coord.y-1);
			neighborCoord[ 4] = new Coord(coord.x+1, coord.y-1);
			neighborCoord[ 5] = new Coord(coord.x+2, coord.y-1);
			neighborCoord[ 6] = new Coord(coord.x-2, coord.y  );
			neighborCoord[ 7] = new Coord(coord.x-1, coord.y  );
			neighborCoord[ 8] = new Coord(coord.x+1, coord.y  );
			neighborCoord[ 9] = new Coord(coord.x+2, coord.y  );
			neighborCoord[10] = new Coord(coord.x-3, coord.y+1);
			neighborCoord[11] = new Coord(coord.x-2, coord.y+1);
			neighborCoord[12] = new Coord(coord.x-1, coord.y+1);
			neighborCoord[13] = new Coord(coord.x  , coord.y+1);
			neighborCoord[14] = new Coord(coord.x+1, coord.y+1);
			neighborCoord[15] = new Coord(coord.x-3, coord.y+2);
			neighborCoord[16] = new Coord(coord.x-2, coord.y+2);
			neighborCoord[17] = new Coord(coord.x-1, coord.y+2);
			neighborCoord[18] = new Coord(coord.x  , coord.y+2);
			neighborCoord[19] = new Coord(coord.x-2, coord.y+3);
			neighborCoord[20] = new Coord(coord.x  , coord.y+3);
			break;
		case 1:
			neighborCoord[ 0] = new Coord(coord.x-2, coord.y-2);
			neighborCoord[ 1] = new Coord(coord.x  , coord.y-2);
			neighborCoord[ 2] = new Coord(coord.x-2, coord.y-1);
			neighborCoord[ 3] = new Coord(coord.x-1, coord.y-1);
			neighborCoord[ 4] = new Coord(coord.x  , coord.y-1);
			neighborCoord[ 5] = new Coord(coord.x+1, coord.y-1);
			neighborCoord[ 6] = new Coord(coord.x-2, coord.y  );
			neighborCoord[ 7] = new Coord(coord.x-1, coord.y  );
			neighborCoord[ 8] = new Coord(coord.x+1, coord.y  );
			neighborCoord[ 9] = new Coord(coord.x+2, coord.y  );
			neighborCoord[10] = new Coord(coord.x-2, coord.y+1);
			neighborCoord[11] = new Coord(coord.x-1, coord.y+1);
			neighborCoord[12] = new Coord(coord.x  , coord.y+1);
			neighborCoord[13] = new Coord(coord.x+1, coord.y+1);
			neighborCoord[14] = new Coord(coord.x+2, coord.y+1);
			neighborCoord[15] = new Coord(coord.x-1, coord.y+2);
			neighborCoord[16] = new Coord(coord.x  , coord.y+2);
			neighborCoord[17] = new Coord(coord.x+1, coord.y+2);
			neighborCoord[18] = new Coord(coord.x+2, coord.y+2);
			neighborCoord[19] = new Coord(coord.x  , coord.y+3);
			neighborCoord[20] = new Coord(coord.x+2, coord.y+3);
			break;
		case 2:
			neighborCoord[ 0] = new Coord(coord.x-3, coord.y-2);
			neighborCoord[ 1] = new Coord(coord.x-1, coord.y-2);
			neighborCoord[ 2] = new Coord(coord.x  , coord.y-2);
			neighborCoord[ 3] = new Coord(coord.x+2, coord.y-2);
			neighborCoord[ 4] = new Coord(coord.x-3, coord.y-1);
			neighborCoord[ 5] = new Coord(coord.x-2, coord.y-1);
			neighborCoord[ 6] = new Coord(coord.x-1, coord.y-1);
			neighborCoord[ 7] = new Coord(coord.x  , coord.y-1);
			neighborCoord[ 8] = new Coord(coord.x+1, coord.y-1);
			neighborCoord[ 9] = new Coord(coord.x+2, coord.y-1);
			neighborCoord[10] = new Coord(coord.x+3, coord.y-1);
			neighborCoord[11] = new Coord(coord.x-3, coord.y  );
			neighborCoord[12] = new Coord(coord.x-2, coord.y  );
			neighborCoord[13] = new Coord(coord.x-1, coord.y  );
			neighborCoord[14] = new Coord(coord.x+1, coord.y  );
			neighborCoord[15] = new Coord(coord.x+2, coord.y  );
			neighborCoord[16] = new Coord(coord.x+3, coord.y  );
			neighborCoord[17] = new Coord(coord.x-3, coord.y+1);
			neighborCoord[18] = new Coord(coord.x-1, coord.y+1);
			neighborCoord[19] = new Coord(coord.x  , coord.y+1);
			neighborCoord[20] = new Coord(coord.x+2, coord.y+1);
			break;
		case 3:
			neighborCoord[ 0] = new Coord(coord.x-2, coord.y-1);
			neighborCoord[ 1] = new Coord(coord.x  , coord.y-1);
			neighborCoord[ 2] = new Coord(coord.x+1, coord.y-1);
			neighborCoord[ 3] = new Coord(coord.x+3, coord.y-1);
			neighborCoord[ 4] = new Coord(coord.x-3, coord.y  );
			neighborCoord[ 5] = new Coord(coord.x-2, coord.y  );
			neighborCoord[ 6] = new Coord(coord.x-1, coord.y  );
			neighborCoord[ 7] = new Coord(coord.x+1, coord.y  );
			neighborCoord[ 8] = new Coord(coord.x+2, coord.y  );
			neighborCoord[ 9] = new Coord(coord.x+3, coord.y  );
			neighborCoord[10] = new Coord(coord.x-3, coord.y+1);
			neighborCoord[11] = new Coord(coord.x-2, coord.y+1);
			neighborCoord[12] = new Coord(coord.x-1, coord.y+1);
			neighborCoord[13] = new Coord(coord.x  , coord.y+1);
			neighborCoord[14] = new Coord(coord.x+1, coord.y+1);
			neighborCoord[15] = new Coord(coord.x+2, coord.y+1);
			neighborCoord[16] = new Coord(coord.x+3, coord.y+1);
			neighborCoord[17] = new Coord(coord.x-2, coord.y+2);
			neighborCoord[18] = new Coord(coord.x  , coord.y+2);
			neighborCoord[19] = new Coord(coord.x+1, coord.y+2);
			neighborCoord[20] = new Coord(coord.x+3, coord.y+2);
			break;
		case 4:
			neighborCoord[ 0] = new Coord(coord.x-2, coord.y-3);
			neighborCoord[ 1] = new Coord(coord.x  , coord.y-3);
			neighborCoord[ 2] = new Coord(coord.x-2, coord.y-2);
			neighborCoord[ 3] = new Coord(coord.x-1, coord.y-2);
			neighborCoord[ 4] = new Coord(coord.x  , coord.y-2);
			neighborCoord[ 5] = new Coord(coord.x+1, coord.y-2);
			neighborCoord[ 6] = new Coord(coord.x-2, coord.y-1);
			neighborCoord[ 7] = new Coord(coord.x-1, coord.y-1);
			neighborCoord[ 8] = new Coord(coord.x  , coord.y-1);
			neighborCoord[ 9] = new Coord(coord.x+1, coord.y-1);
			neighborCoord[10] = new Coord(coord.x+2, coord.y-1);
			neighborCoord[11] = new Coord(coord.x-2, coord.y  );
			neighborCoord[12] = new Coord(coord.x-1, coord.y  );
			neighborCoord[13] = new Coord(coord.x+1, coord.y  );
			neighborCoord[14] = new Coord(coord.x+2, coord.y  );
			neighborCoord[15] = new Coord(coord.x-1, coord.y+1);
			neighborCoord[16] = new Coord(coord.x  , coord.y+1);
			neighborCoord[17] = new Coord(coord.x+1, coord.y+1);
			neighborCoord[18] = new Coord(coord.x+2, coord.y+1);
			neighborCoord[19] = new Coord(coord.x  , coord.y+2);
			neighborCoord[20] = new Coord(coord.x+2, coord.y+2);
			break;
		case 5:
			neighborCoord[ 0] = new Coord(coord.x  , coord.y-3);
			neighborCoord[ 1] = new Coord(coord.x+2, coord.y-3);
			neighborCoord[ 2] = new Coord(coord.x  , coord.y-2);
			neighborCoord[ 3] = new Coord(coord.x+1, coord.y-2);
			neighborCoord[ 4] = new Coord(coord.x+2, coord.y-2);
			neighborCoord[ 5] = new Coord(coord.x+3, coord.y-2);
			neighborCoord[ 6] = new Coord(coord.x-1, coord.y-1);
			neighborCoord[ 7] = new Coord(coord.x  , coord.y-1);
			neighborCoord[ 8] = new Coord(coord.x+1, coord.y-1);
			neighborCoord[ 9] = new Coord(coord.x+2, coord.y-1);
			neighborCoord[10] = new Coord(coord.x+3, coord.y-1);
			neighborCoord[11] = new Coord(coord.x-2, coord.y  );
			neighborCoord[12] = new Coord(coord.x-1, coord.y  );
			neighborCoord[13] = new Coord(coord.x+1, coord.y  );
			neighborCoord[14] = new Coord(coord.x+2, coord.y  );
			neighborCoord[15] = new Coord(coord.x-2, coord.y+1);
			neighborCoord[16] = new Coord(coord.x-1, coord.y+1);
			neighborCoord[17] = new Coord(coord.x  , coord.y+1);
			neighborCoord[18] = new Coord(coord.x+1, coord.y+1);
			neighborCoord[19] = new Coord(coord.x-1, coord.y+2);
			neighborCoord[20] = new Coord(coord.x+1, coord.y+2);
			break;
		case 6:
			neighborCoord[ 0] = new Coord(coord.x-2, coord.y-2);
			neighborCoord[ 1] = new Coord(coord.x  , coord.y-2);
			neighborCoord[ 2] = new Coord(coord.x+1, coord.y-2);
			neighborCoord[ 3] = new Coord(coord.x+3, coord.y-2);
			neighborCoord[ 4] = new Coord(coord.x-3, coord.y-1);
			neighborCoord[ 5] = new Coord(coord.x-2, coord.y-1);
			neighborCoord[ 6] = new Coord(coord.x-1, coord.y-1);
			neighborCoord[ 7] = new Coord(coord.x  , coord.y-1);
			neighborCoord[ 8] = new Coord(coord.x+1, coord.y-1);
			neighborCoord[ 9] = new Coord(coord.x+2, coord.y-1);
			neighborCoord[10] = new Coord(coord.x+3, coord.y-1);
			neighborCoord[11] = new Coord(coord.x-3, coord.y  );
			neighborCoord[12] = new Coord(coord.x-2, coord.y  );
			neighborCoord[13] = new Coord(coord.x-1, coord.y  );
			neighborCoord[14] = new Coord(coord.x+1, coord.y  );
			neighborCoord[15] = new Coord(coord.x+2, coord.y  );
			neighborCoord[16] = new Coord(coord.x+3, coord.y  );
			neighborCoord[17] = new Coord(coord.x-2, coord.y+1);
			neighborCoord[18] = new Coord(coord.x  , coord.y+1);
			neighborCoord[19] = new Coord(coord.x+1, coord.y+1);
			neighborCoord[20] = new Coord(coord.x+3, coord.y+1);
			break;
		case 7:
			neighborCoord[ 0] = new Coord(coord.x  , coord.y-2);
			neighborCoord[ 1] = new Coord(coord.x+2, coord.y-2);
			neighborCoord[ 2] = new Coord(coord.x-1, coord.y-1);
			neighborCoord[ 3] = new Coord(coord.x  , coord.y-1);
			neighborCoord[ 4] = new Coord(coord.x+1, coord.y-1);
			neighborCoord[ 5] = new Coord(coord.x+2, coord.y-1);
			neighborCoord[ 6] = new Coord(coord.x-2, coord.y  );
			neighborCoord[ 7] = new Coord(coord.x-1, coord.y  );
			neighborCoord[ 8] = new Coord(coord.x+1, coord.y  );
			neighborCoord[ 9] = new Coord(coord.x+2, coord.y  );
			neighborCoord[10] = new Coord(coord.x-2, coord.y+1);
			neighborCoord[11] = new Coord(coord.x-1, coord.y+1);
			neighborCoord[12] = new Coord(coord.x  , coord.y+1);
			neighborCoord[13] = new Coord(coord.x+1, coord.y+1);
			neighborCoord[14] = new Coord(coord.x+2, coord.y+1);
			neighborCoord[15] = new Coord(coord.x-2, coord.y+2);
			neighborCoord[16] = new Coord(coord.x-1, coord.y+2);
			neighborCoord[17] = new Coord(coord.x  , coord.y+2);
			neighborCoord[18] = new Coord(coord.x+1, coord.y+2);
			neighborCoord[19] = new Coord(coord.x-2, coord.y+3);
			neighborCoord[20] = new Coord(coord.x  , coord.y+3);
			break;
		case 8:
			neighborCoord[ 0] = new Coord(coord.x-1, coord.y-2);
			neighborCoord[ 1] = new Coord(coord.x+1, coord.y-2);
			neighborCoord[ 2] = new Coord(coord.x-2, coord.y-1);
			neighborCoord[ 3] = new Coord(coord.x-1, coord.y-1);
			neighborCoord[ 4] = new Coord(coord.x  , coord.y-1);
			neighborCoord[ 5] = new Coord(coord.x+1, coord.y-1);
			neighborCoord[ 6] = new Coord(coord.x-2, coord.y  );
			neighborCoord[ 7] = new Coord(coord.x-1, coord.y  );
			neighborCoord[ 8] = new Coord(coord.x+1, coord.y  );
			neighborCoord[ 9] = new Coord(coord.x+2, coord.y  );
			neighborCoord[10] = new Coord(coord.x-1, coord.y+1);
			neighborCoord[11] = new Coord(coord.x  , coord.y+1);
			neighborCoord[12] = new Coord(coord.x+1, coord.y+1);
			neighborCoord[13] = new Coord(coord.x+2, coord.y+1);
			neighborCoord[14] = new Coord(coord.x+3, coord.y+1);
			neighborCoord[15] = new Coord(coord.x  , coord.y+2);
			neighborCoord[16] = new Coord(coord.x+1, coord.y+2);
			neighborCoord[17] = new Coord(coord.x+2, coord.y+2);
			neighborCoord[18] = new Coord(coord.x+3, coord.y+2);
			neighborCoord[19] = new Coord(coord.x  , coord.y+3);
			neighborCoord[20] = new Coord(coord.x+2, coord.y+3);
			break;
		case 9:
			neighborCoord[ 0] = new Coord(coord.x-2, coord.y-3);
			neighborCoord[ 1] = new Coord(coord.x  , coord.y-3);
			neighborCoord[ 2] = new Coord(coord.x-3, coord.y-2);
			neighborCoord[ 3] = new Coord(coord.x-2, coord.y-2);
			neighborCoord[ 4] = new Coord(coord.x-1, coord.y-2);
			neighborCoord[ 5] = new Coord(coord.x  , coord.y-2);
			neighborCoord[ 6] = new Coord(coord.x-3, coord.y-1);
			neighborCoord[ 7] = new Coord(coord.x-2, coord.y-1);
			neighborCoord[ 8] = new Coord(coord.x-1, coord.y-1);
			neighborCoord[ 9] = new Coord(coord.x  , coord.y-1);
			neighborCoord[10] = new Coord(coord.x+1, coord.y-1);
			neighborCoord[11] = new Coord(coord.x-2, coord.y  );
			neighborCoord[12] = new Coord(coord.x-1, coord.y  );
			neighborCoord[13] = new Coord(coord.x+1, coord.y  );
			neighborCoord[14] = new Coord(coord.x+2, coord.y  );
			neighborCoord[15] = new Coord(coord.x-1, coord.y+1);
			neighborCoord[16] = new Coord(coord.x  , coord.y+1);
			neighborCoord[17] = new Coord(coord.x+1, coord.y+1);
			neighborCoord[18] = new Coord(coord.x+2, coord.y+1);
			neighborCoord[19] = new Coord(coord.x-1, coord.y+2);
			neighborCoord[20] = new Coord(coord.x+1, coord.y+2);
			break;
		case 10:
			neighborCoord[ 0] = new Coord(coord.x  , coord.y-3);
			neighborCoord[ 1] = new Coord(coord.x+2, coord.y-3);
			neighborCoord[ 2] = new Coord(coord.x-1, coord.y-2);
			neighborCoord[ 3] = new Coord(coord.x  , coord.y-2);
			neighborCoord[ 4] = new Coord(coord.x+1, coord.y-2);
			neighborCoord[ 5] = new Coord(coord.x+2, coord.y-2);
			neighborCoord[ 6] = new Coord(coord.x-2, coord.y-1);
			neighborCoord[ 7] = new Coord(coord.x-1, coord.y-1);
			neighborCoord[ 8] = new Coord(coord.x  , coord.y-1);
			neighborCoord[ 9] = new Coord(coord.x+1, coord.y-1);
			neighborCoord[10] = new Coord(coord.x+2, coord.y-1);
			neighborCoord[11] = new Coord(coord.x-2, coord.y  );
			neighborCoord[12] = new Coord(coord.x-1, coord.y  );
			neighborCoord[13] = new Coord(coord.x+1, coord.y  );
			neighborCoord[14] = new Coord(coord.x+2, coord.y  );
			neighborCoord[15] = new Coord(coord.x-2, coord.y+1);
			neighborCoord[16] = new Coord(coord.x-1, coord.y+1);
			neighborCoord[17] = new Coord(coord.x  , coord.y+1);
			neighborCoord[18] = new Coord(coord.x+1, coord.y+1);
			neighborCoord[19] = new Coord(coord.x-2, coord.y+2);
			neighborCoord[20] = new Coord(coord.x  , coord.y+2);
			break;
		case 11:
			neighborCoord[ 0] = new Coord(coord.x-3, coord.y-1);
			neighborCoord[ 1] = new Coord(coord.x-1, coord.y-1);
			neighborCoord[ 2] = new Coord(coord.x  , coord.y-1);
			neighborCoord[ 3] = new Coord(coord.x+2, coord.y-1);
			neighborCoord[ 4] = new Coord(coord.x-3, coord.y  );
			neighborCoord[ 5] = new Coord(coord.x-2, coord.y  );
			neighborCoord[ 6] = new Coord(coord.x-1, coord.y  );
			neighborCoord[ 7] = new Coord(coord.x+1, coord.y  );
			neighborCoord[ 8] = new Coord(coord.x+2, coord.y  );
			neighborCoord[ 9] = new Coord(coord.x+3, coord.y  );
			neighborCoord[10] = new Coord(coord.x-3, coord.y+1);
			neighborCoord[11] = new Coord(coord.x-2, coord.y+1);
			neighborCoord[12] = new Coord(coord.x-1, coord.y+1);
			neighborCoord[13] = new Coord(coord.x  , coord.y+1);
			neighborCoord[14] = new Coord(coord.x+1, coord.y+1);
			neighborCoord[15] = new Coord(coord.x+2, coord.y+1);
			neighborCoord[16] = new Coord(coord.x+3, coord.y+1);
			neighborCoord[17] = new Coord(coord.x-3, coord.y+2);
			neighborCoord[18] = new Coord(coord.x-1, coord.y+2);
			neighborCoord[19] = new Coord(coord.x  , coord.y+2);
			neighborCoord[20] = new Coord(coord.x+2, coord.y+2);
			break;
		}

		return neighborCoord;
	}

	@Override
	protected void CalcRegion() {
		AttrTriangle4 attr = getAttr();
		int area = attr.getArea();
		double a = attr.CalcA(area);
		double b = attr.CalcB(area);
		double R = attr.CalcROut(area);
		double r = attr.CalcRIn(area);

		// определение координат точек фигуры
		double oX =  (coord.x/3)*a + b;      // offset X
		double oY = ((coord.y/4)*2+1)*(R+r); // offset Y

		switch (direction) {
		case 0:
			region.setPoint(2, (int)(oX - b), (int)(oY      ));
			region.setPoint(1, (int)(oX    ), (int)(oY - r  ));
			region.setPoint(0, (int)(oX    ), (int)(oY - R-r));
			break;
		case 1:
			region.setPoint(0, (int)(oX + b), (int)(oY - R  ));
			region.setPoint(2, (int)(oX    ), (int)(oY - R-r));
			region.setPoint(1, (int)(oX + b), (int)(oY      ));
			break;
		case 2:
			region.setPoint(2, (int)(oX    ), (int)(oY - R-r));
			region.setPoint(1, (int)(oX + b), (int)(oY - R  ));
			region.setPoint(0, (int)(oX + a), (int)(oY - R-r));
			break;
		case 3:
			region.setPoint(0, (int)(oX + b), (int)(oY      ));
			region.setPoint(2, (int)(oX    ), (int)(oY - r  ));
			region.setPoint(1, (int)(oX - b), (int)(oY      ));
			break;
		case 4:
			region.setPoint(2, (int)(oX    ), (int)(oY - r  ));
			region.setPoint(1, (int)(oX + b), (int)(oY      ));
			region.setPoint(0, (int)(oX    ), (int)(oY - R-r));
			break;
		case 5:
			region.setPoint(0, (int)(oX + a), (int)(oY - R-r));
			region.setPoint(2, (int)(oX + b), (int)(oY - R  ));
			region.setPoint(1, (int)(oX + b), (int)(oY      ));
			break;
		case 6:
			region.setPoint(2, (int)(oX - b), (int)(oY      ));
			region.setPoint(1, (int)(oX    ), (int)(oY + r  ));
			region.setPoint(0, (int)(oX + b), (int)(oY      ));
			break;
		case 7:
			region.setPoint(0, (int)(oX + b), (int)(oY      ));
			region.setPoint(2, (int)(oX    ), (int)(oY + r  ));
			region.setPoint(1, (int)(oX    ), (int)(oY + R+r));
			break;
		case 8:
			region.setPoint(2, (int)(oX + b), (int)(oY + R  ));
			region.setPoint(1, (int)(oX + a), (int)(oY + R+r));
			region.setPoint(0, (int)(oX + b), (int)(oY      ));
			break;
		case 9:
			region.setPoint(0, (int)(oX    ), (int)(oY + r  ));
			region.setPoint(2, (int)(oX - b), (int)(oY      ));
			region.setPoint(1, (int)(oX    ), (int)(oY + R+r));
			break;
		case 10:
			region.setPoint(2, (int)(oX    ), (int)(oY + R+r));
			region.setPoint(1, (int)(oX + b), (int)(oY + R  ));
			region.setPoint(0, (int)(oX + b), (int)(oY      ));
			break;
		case 11:
			region.setPoint(0, (int)(oX + a), (int)(oY + R+r));
			region.setPoint(2, (int)(oX + b), (int)(oY + R  ));
			region.setPoint(1, (int)(oX    ), (int)(oY + R+r));
			break;
		}
	}

	@Override
	public Rect getRcInner(int borderWidth) {
		AttrTriangle4 attr = getAttr();
		int area = attr.getArea();
		double w = borderWidth/2.;
		double sq    = attr.CalcSq(area, borderWidth);
		double sq2   = sq/2;
		double sq2w  = sq2+w;
		double sq2w3 = sq2+w/SQRT3;

		PointDouble center = new PointDouble(); // координата центра квадрата
		switch (direction) {
		case 0: case 10:
			center.x = region.getPoint(1).x - sq2w;
			center.y = region.getPoint(1).y - sq2w3;
			break;
		case 1: case 9:
			center.x = region.getPoint(0).x - sq2w;
			center.y = region.getPoint(0).y + sq2w3;
			break;
		case 2: case 6:
			center.x = region.getPoint(1).x;
			center.y = region.getPoint(0).y + sq2w;
			break;
		case 3: case 11:
			center.x = region.getPoint(2).x;
			center.y = region.getPoint(0).y - sq2w;
			break;
		case 4: case 8:
			center.x = region.getPoint(2).x + sq2w;
			center.y = region.getPoint(2).y - sq2w3;
			break;
		case 5: case 7:
			center.x = region.getPoint(2).x + sq2w;
			center.y = region.getPoint(2).y + sq2w3;
			break;
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
		case 1: case 3: case 5: case 7: case 9: case 11: return 1;
		}
		return 2;
	}

//	@Override
//	protected Color getBackgroundFillColor(int fillMode, Color defaultColor, Map<Integer, Color> repositoryColor) {
////		if (fillMode == getAttr().getMaxBackgroundFillModeValue())
//		{
//			switch ((getCoord().y&3)*3+(getCoord().x%6)) { // почти как вычисление direction...
//			// подсвечиваю звёзду
//			case 1: case  3: case  4: case  5:
//				return repositoryColor.get(0);
//			case 6: return Color.red;
//			default:
//				return repositoryColor.get(1);
//			}
//		}
//	}
}