////////////////////////////////////////////////////////////////////////////////
//                               FastMines project
//                                      © Sergey Krivulya (KSerg, aka SeregaLBN)
// file name: "Trapezoid2.java"
//
// Реализация класса Trapezoid2 - 3 трапеции, составляющие равносторонний треугольник
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

package fmg.core.mosaic.cells;

import fmg.common.geom.Coord;
import fmg.common.geom.PointDouble;
import fmg.common.geom.Rect;
import fmg.common.geom.Size;

/**
 * Trapezoid2 - 3 трапеции, составляющие равносторонний треугольник
 * @see BaseCell
 **/
public class Trapezoid2 extends BaseCell {
	public static class AttrTrapezoid2 extends BaseAttribute {
		public AttrTrapezoid2(int area) {
			super(area);
		}

		@Override
		public Size getOwnerSize(Size sizeField) {
			double a = CalcA();
			double c = CalcC();
			double r = CalcRIn();
			double R = CalcROut();
			Size result = new Size(
					(int)(a+c+ c *((sizeField.width+2)/3)+
					       (a+ c)*((sizeField.width+1)/3)+
					           a *((sizeField.width+0)/3)),
					(int)(     R *((sizeField.height+1)/2)+
					           r *((sizeField.height+0)/2)));

			if (sizeField.width == 1)
				if ((sizeField.height % 4) == 3)
					result.height -= r;

			return result;
		}
	
		@Override
		public int getNeighborNumber(boolean max) { return 9; }
		@Override
		public int getNeighborNumber(int direction) { return 9; }
		@Override
		public int getVertexNumber(int direction) { return 4; }
		@Override
		public double getVertexIntersection() { return 4.25; } // (6+4+4+3)/4.
		@Override
		public Size GetDirectionSizeField() { return new Size(3, 4); }
		@Override
		protected double CalcA   () { return Math.sqrt(getArea()/SQRT27)*2; }
		protected double CalcB   () { return CalcA()*2; }
		protected double CalcC   () { return CalcA()/2; }
		protected double CalcROut() { return CalcA()*SQRT3; }
		protected double CalcRIn () { return CalcROut()/2; }
		@Override
		public double CalcSq(int borderWidth) {
			double w = borderWidth/2.;
			return (CalcA()*SQRT3 - w*4)/(SQRT3+1);
		}
	}

	public Trapezoid2(AttrTrapezoid2 attr, Coord coord) {
		super(attr, coord,
				(coord.y&3)*3+(coord.x%3) // 0..11
			);
	}

	@Override
	public AttrTrapezoid2 getAttr() {
		return (AttrTrapezoid2) super.getAttr();
	}

	@Override
	protected Coord[] GetCoordsNeighbor() {
		Coord[] neighborCoord = new Coord[getAttr().getNeighborNumber(true)];

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
    		neighborCoord[ 8] = new Coord(coord.x+2, coord.y+1);
    		break;                                          
    	case 1:                                            
    		neighborCoord[ 0] = new Coord(coord.x-2, coord.y-1);
    		neighborCoord[ 1] = new Coord(coord.x-1, coord.y-1);
    		neighborCoord[ 2] = new Coord(coord.x  , coord.y-1);
    		neighborCoord[ 3] = new Coord(coord.x+1, coord.y-1);
    		neighborCoord[ 4] = new Coord(coord.x-2, coord.y  );
    		neighborCoord[ 5] = new Coord(coord.x-1, coord.y  );
    		neighborCoord[ 6] = new Coord(coord.x+1, coord.y  );
    		neighborCoord[ 7] = new Coord(coord.x  , coord.y+1);
    		neighborCoord[ 8] = new Coord(coord.x+1, coord.y+1);
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
    		neighborCoord[ 8] = new Coord(coord.x+1, coord.y+1);
    		break;                                          
    	case 3:                                            
    		neighborCoord[ 0] = new Coord(coord.x-1, coord.y-1);
    		neighborCoord[ 1] = new Coord(coord.x  , coord.y-1);
    		neighborCoord[ 2] = new Coord(coord.x-2, coord.y  );
    		neighborCoord[ 3] = new Coord(coord.x-1, coord.y  );
    		neighborCoord[ 4] = new Coord(coord.x+1, coord.y  );
    		neighborCoord[ 5] = new Coord(coord.x-2, coord.y+1);
    		neighborCoord[ 6] = new Coord(coord.x-1, coord.y+1);
    		neighborCoord[ 7] = new Coord(coord.x  , coord.y+1);
    		neighborCoord[ 8] = new Coord(coord.x+1, coord.y+1);
    		break;                                          
    	case 4:                                            
    		neighborCoord[ 0] = new Coord(coord.x-1, coord.y-1);
    		neighborCoord[ 1] = new Coord(coord.x  , coord.y-1);
    		neighborCoord[ 2] = new Coord(coord.x-1, coord.y  );
    		neighborCoord[ 3] = new Coord(coord.x+1, coord.y  );
    		neighborCoord[ 4] = new Coord(coord.x+2, coord.y  );
    		neighborCoord[ 5] = new Coord(coord.x-1, coord.y+1);
    		neighborCoord[ 6] = new Coord(coord.x  , coord.y+1);
    		neighborCoord[ 7] = new Coord(coord.x+1, coord.y+1);
    		neighborCoord[ 8] = new Coord(coord.x+2, coord.y+1);
    		break;                                          
    	case 5:                                            
    		neighborCoord[ 0] = new Coord(coord.x-2, coord.y-1);
    		neighborCoord[ 1] = new Coord(coord.x-1, coord.y-1);
    		neighborCoord[ 2] = new Coord(coord.x  , coord.y-1);
    		neighborCoord[ 3] = new Coord(coord.x+1, coord.y-1);
    		neighborCoord[ 4] = new Coord(coord.x-1, coord.y  );
    		neighborCoord[ 5] = new Coord(coord.x+1, coord.y  );
    		neighborCoord[ 6] = new Coord(coord.x-1, coord.y+1);
    		neighborCoord[ 7] = new Coord(coord.x  , coord.y+1);
    		neighborCoord[ 8] = new Coord(coord.x+1, coord.y+1);
    		break;                                          
    	case 6:                                            
    		neighborCoord[ 0] = new Coord(coord.x-2, coord.y-1);
    		neighborCoord[ 1] = new Coord(coord.x-1, coord.y-1);
    		neighborCoord[ 2] = new Coord(coord.x  , coord.y-1);
    		neighborCoord[ 3] = new Coord(coord.x+1, coord.y-1);
    		neighborCoord[ 4] = new Coord(coord.x-2, coord.y  );
    		neighborCoord[ 5] = new Coord(coord.x-1, coord.y  );
    		neighborCoord[ 6] = new Coord(coord.x+1, coord.y  );
    		neighborCoord[ 7] = new Coord(coord.x-1, coord.y+1);
    		neighborCoord[ 8] = new Coord(coord.x  , coord.y+1);
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
    		neighborCoord[ 8] = new Coord(coord.x  , coord.y+1);
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
    		neighborCoord[ 8] = new Coord(coord.x+1, coord.y+1);
    		break;                                          
    	case 9:                                            
    		neighborCoord[ 0] = new Coord(coord.x-1, coord.y-1);
    		neighborCoord[ 1] = new Coord(coord.x  , coord.y-1);
    		neighborCoord[ 2] = new Coord(coord.x+1, coord.y-1);
    		neighborCoord[ 3] = new Coord(coord.x+2, coord.y-1);
    		neighborCoord[ 4] = new Coord(coord.x-1, coord.y  );
    		neighborCoord[ 5] = new Coord(coord.x+1, coord.y  );
    		neighborCoord[ 6] = new Coord(coord.x-1, coord.y+1);
    		neighborCoord[ 7] = new Coord(coord.x  , coord.y+1);
    		neighborCoord[ 8] = new Coord(coord.x+1, coord.y+1);
    		break;                                          
    	case 10:                                           
    		neighborCoord[ 0] = new Coord(coord.x  , coord.y-1);
    		neighborCoord[ 1] = new Coord(coord.x+1, coord.y-1);
    		neighborCoord[ 2] = new Coord(coord.x-2, coord.y  );
    		neighborCoord[ 3] = new Coord(coord.x-1, coord.y  );
    		neighborCoord[ 4] = new Coord(coord.x+1, coord.y  );
    		neighborCoord[ 5] = new Coord(coord.x-2, coord.y+1);
    		neighborCoord[ 6] = new Coord(coord.x-1, coord.y+1);
    		neighborCoord[ 7] = new Coord(coord.x  , coord.y+1);
    		neighborCoord[ 8] = new Coord(coord.x+1, coord.y+1);
    		break;                                          
    	case 11:                                           
    		neighborCoord[ 0] = new Coord(coord.x  , coord.y-1);
    		neighborCoord[ 1] = new Coord(coord.x+1, coord.y-1);
    		neighborCoord[ 2] = new Coord(coord.x-1, coord.y  );
    		neighborCoord[ 3] = new Coord(coord.x+1, coord.y  );
    		neighborCoord[ 4] = new Coord(coord.x+2, coord.y  );
    		neighborCoord[ 5] = new Coord(coord.x-1, coord.y+1);
    		neighborCoord[ 6] = new Coord(coord.x  , coord.y+1);
    		neighborCoord[ 7] = new Coord(coord.x+1, coord.y+1);
    		neighborCoord[ 8] = new Coord(coord.x+2, coord.y+1);
    		break;
    	}

		return neighborCoord;
	}

	@Override
	protected void CalcRegion() {
		AttrTrapezoid2 attr = getAttr();
		double a = attr.CalcA();
		double b = attr.CalcB();
		double c = attr.CalcC();
		double R = attr.CalcROut();
		double r = attr.CalcRIn();

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
			region.setPoint(0, (int)(oX + a+c), (int)(oY - R-r));
			region.setPoint(1, (int)(oX + a  ), (int)(oY - R  ));
			region.setPoint(2, (int)(oX      ), (int)(oY - R  ));
			region.setPoint(3, (int)(oX - c  ), (int)(oY - R-r));
			break;                                                            
		case 2:                                                              
			region.setPoint(0, (int)(oX + b+c), (int)(oY - R-r));
			region.setPoint(3, (int)(oX + a+c), (int)(oY - R-r));
			region.setPoint(2, (int)(oX + a  ), (int)(oY - R  ));
			region.setPoint(1, (int)(oX + a+c), (int)(oY - r  ));
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
			region.setPoint(0, (int)(oX + a  ), (int)(oY - R  ));
			region.setPoint(1, (int)(oX + a+c), (int)(oY - r  ));
			region.setPoint(2, (int)(oX + a  ), (int)(oY      ));
			region.setPoint(3, (int)(oX      ), (int)(oY - R  ));
			break;                                                            
		case 6:                                                              
			region.setPoint(0, (int)(oX      ), (int)(oY      ));
			region.setPoint(1, (int)(oX - c  ), (int)(oY + r  ));
			region.setPoint(2, (int)(oX - a-c), (int)(oY + r  ));
			region.setPoint(3, (int)(oX - b  ), (int)(oY      ));
			break;                                                            
		case 7:                                                              
			region.setPoint(0, (int)(oX + a  ), (int)(oY      ));
			region.setPoint(1, (int)(oX      ), (int)(oY + R  ));
			region.setPoint(2, (int)(oX - c  ), (int)(oY + r  ));
			region.setPoint(3, (int)(oX      ), (int)(oY      ));
			break;                                                            
		case 8:                                                              
			region.setPoint(0, (int)(oX + a  ), (int)(oY      ));
			region.setPoint(1, (int)(oX + a+c), (int)(oY + r  ));
			region.setPoint(2, (int)(oX + a  ), (int)(oY + R  ));
			region.setPoint(3, (int)(oX      ), (int)(oY + R  ));
			break;                                                            
		case 9:                                                              
			region.setPoint(0, (int)(oX - c  ), (int)(oY + r  ));
			region.setPoint(1, (int)(oX      ), (int)(oY + R  ));
			region.setPoint(2, (int)(oX - c  ), (int)(oY + R+r));
			region.setPoint(3, (int)(oX - a-c), (int)(oY + r  ));
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

	@Override
	public Rect getRcInner(int borderWidth) {
		AttrTrapezoid2 attr = getAttr();
		double a = attr.CalcA();
		double b = attr.CalcB();
		double c = attr.CalcC();
		double R = attr.CalcROut();
		double r = attr.CalcRIn();
//		double w = borderWidth/2.;
		double sq  = attr.CalcSq(borderWidth);
		double sq2 = sq/2;

		double oX = (a+b)*(coord.x/3) + b; // offset X
		double oY = (R+r)*(coord.y/4*2+1); // offset Y

		PointDouble center = new PointDouble(); // координата центра квадрата
		switch (direction) {
		case 0:  center.x = oX - c*1.25; center.y = oY - r*1.75; break;
		case 1:  center.x = oX + c;      center.y = oY - r*2.50; break;
		case 2:  center.x = oX + c*3.25; center.y = oY - r*2.25; break;
		case 3:  center.x = oX - a;      center.y = oY - r*0.50; break;
		case 4:  center.x = oX + c*0.25; center.y = oY - r*0.75; break;
		case 5:  center.x = oX + c*1.75; center.y = oY - r*1.25; break;
		case 6:  center.x = oX - a;      center.y = oY + r*0.50; break;
		case 7:  center.x = oX + c*0.25; center.y = oY + r*0.75; break;
		case 8:  center.x = oX + c*1.75; center.y = oY + r*1.25; break;
		case 9:  center.x = oX - c*1.25; center.y = oY + r*1.75; break;
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

	@Override
	public int getShiftPointBorderIndex() {
		switch (direction) {
		case 0: case 8: return 3;
		case 2: case 7: return 1;
		}
		return 2;
	}
}