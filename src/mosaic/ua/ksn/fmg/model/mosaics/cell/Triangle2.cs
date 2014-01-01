////////////////////////////////////////////////////////////////////////////////
//                               FastMines project
//                                      © Sergey Krivulya (KSerg, aka SeregaLBN)
// file name: "Triangle2.java"
//
// –еализаци€ класса Triangle2 - равносторонний треугольник (вариант пол€ є2 - Єлочкой)
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

/// <summary> “реугольник. ¬ариант 2 - равносторонний, Єлочкой </summary>
public class Triangle2 : BaseCell {
	public class AttrTriangle2 : BaseAttribute {
		public AttrTriangle2(int area)
			: base(area)
      {}

		public override Size CalcOwnerSize(Size sizeField, int area) {
			double b = CalcB(area);
			double h = CalcH(area);
			Size result = new Size(
					(int)(b * (sizeField.width+1)),
					(int)(h * (sizeField.height+0)));
			return result;
		}
	
		public override int getNeighborNumber() { return 8; }
		public override int getNeighborNumber(int direction) { return 8; }
		public override int getVertexNumber() { return 3; }
		public override int getVertexNumber(int direction) { return 3; }
		public override double getVertexIntersection() { return 3.75; } // (4+4+4+3)/4.
		public override Size GetDirectionSizeField() { return new Size(2, 1); }
		public override double CalcA(int area) { return CalcB(area) * 2.0f; } // размер стороны треугольника
		/// <summary> </summary> пол стороны треугольника */
		public double CalcB(int area) { return Math.Sqrt(area/SQRT3); }
		/// <summary> </summary> высота треугольника */
		public double CalcH(int area) { return CalcB(area) * SQRT3; }
		public override double CalcSq(int area, int borderWidth) {
			double w = borderWidth/2.0;
			return (CalcH(area)*2 - 6*w)/(SQRT3+2);
		}

		public override Size sizeIcoField(bool smallSize) {
			return new Size(3, smallSize ? 2 : 3);
		}
	}

	public Triangle2(AttrTriangle2 attr, Coord coord)
		: base(attr, coord,
				coord.x&1 // 0..1
			)
	{}

	private AttrTriangle2 Attr {
		get { return (AttrTriangle2) base.Attr; }
	}

	protected override Coord[] GetCoordsNeighbor() {
		Coord[] neighborCoord = new Coord[Attr.getNeighborNumber()];

		// определ€ю координаты соседей
    	switch (direction) {
    	case 0: case 3:
    		neighborCoord[ 0] = new Coord(coord.x  , coord.y-1);
    		neighborCoord[ 1] = new Coord(coord.x-2, coord.y  );
    		neighborCoord[ 2] = new Coord(coord.x-1, coord.y  );
    		neighborCoord[ 3] = new Coord(coord.x+1, coord.y  );
    		neighborCoord[ 4] = new Coord(coord.x+2, coord.y  );
    		neighborCoord[ 5] = new Coord(coord.x-1, coord.y+1);
    		neighborCoord[ 6] = new Coord(coord.x  , coord.y+1);
    		neighborCoord[ 7] = new Coord(coord.x+1, coord.y+1);
    		break;                                          
    	case 1: case 2:                                    
    		neighborCoord[ 0] = new Coord(coord.x-1, coord.y-1);
    		neighborCoord[ 1] = new Coord(coord.x  , coord.y-1);
    		neighborCoord[ 2] = new Coord(coord.x+1, coord.y-1);
    		neighborCoord[ 3] = new Coord(coord.x-2, coord.y  );
    		neighborCoord[ 4] = new Coord(coord.x-1, coord.y  );
    		neighborCoord[ 5] = new Coord(coord.x+1, coord.y  );
    		neighborCoord[ 6] = new Coord(coord.x+2, coord.y  );
    		neighborCoord[ 7] = new Coord(coord.x  , coord.y+1);
    		break;
    	}

		return neighborCoord;
	}

	protected override void CalcRegion() {
		AttrTriangle2 attr = Attr;
		int area = attr.Area;
		double a = attr.CalcA(area);
		double b = attr.CalcB(area);
		double h = attr.CalcH(area);

		double oX = a*(coord.x>>1); // offset X
		double oY = h* coord.y;     // offset Y

		switch (direction) {
		case 0:
			region.setPoint(0, (int)(oX +   b), (int)(oY    ));
			region.setPoint(1, (int)(oX + a  ), (int)(oY + h));
			region.setPoint(2, (int)(oX      ), (int)(oY + h));
			break;                                                              
		case 1:                                                                
			region.setPoint(0, (int)(oX + a+b), (int)(oY    ));
			region.setPoint(1, (int)(oX + a  ), (int)(oY + h));
			region.setPoint(2, (int)(oX +   b), (int)(oY    ));
			break;
		}
	}

	public override Rect getRcInner(int borderWidth) {
		AttrTriangle2 attr = Attr;
		int area = attr.Area;
		double b = attr.CalcB(area);
		double sq = attr.CalcSq(area, borderWidth);
		double w = borderWidth/2.0;

		PointDouble center = new PointDouble(); // координата вписанного в фигуру квадрата (не совпадает с центром фигуры)
		switch (direction) {
		case 0:
			center.x = region.getPoint(2).x + b;
			center.y = region.getPoint(2).y - sq/2 - w;
			break;
		case 1:
			center.x = region.getPoint(2).x + b;
			center.y = region.getPoint(2).y + sq/2 + w;
			break;
		}

		Rect square = new Rect();
		square.x = (int) (center.x - sq/2);
		square.y = (int) (center.y - sq/2);
		square.width =
		square.height = (int) sq;
		return square;
	}

	public override int getShiftPointBorderIndex() {
		switch (direction) {
		case 1: return 1;
		}
		return 2;
	}
}
}