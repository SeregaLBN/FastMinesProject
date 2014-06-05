////////////////////////////////////////////////////////////////////////////////
//                               FastMines project
//                                      © Sergey Krivulya (KSerg, aka SeregaLBN)
// file name: "Triangle1.java"
//
// –еализаци€ класса Triangle1 - равносторонний треугольник (вариант пол€ є1)
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

/// <summary> “реугольник. ¬ариант 1 - равносторонний, классика </summary>
public class Triangle1 : BaseCell {
	public class AttrTriangle1 : BaseAttribute {
		public AttrTriangle1(int area)
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
	
		public override int getNeighborNumber() { return 12; }
		public override int getNeighborNumber(int direction) { return 12; }
		public override int getVertexNumber() { return 3; }
		public override int getVertexNumber(int direction) { return 3; }
		public override double getVertexIntersection() { return 6; }
		public override Size GetDirectionSizeField() { return new Size(2, 2); }
		public override double CalcA(int area) { return CalcB(area) * 2.0f; } // размер стороны треугольника
		/// <summary> </summary> пол стороны треугольника */
		public double CalcB(int area) { return Math.Sqrt(area/SQRT3); }
		/// <summary> </summary> высота треугольника */
		public double CalcH(int area) { return CalcB(area) * SQRT3; }
		public override double CalcSq(int area, int borderWidth) {
			double w = borderWidth/2.0;
			return (CalcH(area)*2 - 6*w)/(SQRT3+2);
			//return (CalcA(area)*SQRT3 - 6*w)/(SQRT3+2);
		}
	}

	public Triangle1(AttrTriangle1 attr, Coord coord)
		: base(attr, coord,
				((coord.y&1)<<1)+(coord.x&1) // 0..3
			)
	{}

	private AttrTriangle1 Attr {
		get { return (AttrTriangle1) base.Attr; }
	}

	protected override Coord[] GetCoordsNeighbor() {
		Coord[] neighborCoord = new Coord[Attr.getNeighborNumber()];

		// определ€ю координаты соседей
    	switch (direction) {
    	case 0: case 3:
    		neighborCoord[ 0] = new Coord(coord.x-1, coord.y-1);
    		neighborCoord[ 1] = new Coord(coord.x  , coord.y-1);
    		neighborCoord[ 2] = new Coord(coord.x+1, coord.y-1);
    		neighborCoord[ 3] = new Coord(coord.x-2, coord.y  );
    		neighborCoord[ 4] = new Coord(coord.x-1, coord.y  );
    		neighborCoord[ 5] = new Coord(coord.x+1, coord.y  );
    		neighborCoord[ 6] = new Coord(coord.x+2, coord.y  );
    		neighborCoord[ 7] = new Coord(coord.x-2, coord.y+1);
    		neighborCoord[ 8] = new Coord(coord.x-1, coord.y+1);
    		neighborCoord[ 9] = new Coord(coord.x  , coord.y+1);
    		neighborCoord[10] = new Coord(coord.x+1, coord.y+1);
    		neighborCoord[11] = new Coord(coord.x+2, coord.y+1);
    		break;
    	case 1: case 2:
    		neighborCoord[ 0] = new Coord(coord.x-2, coord.y-1);
    		neighborCoord[ 1] = new Coord(coord.x-1, coord.y-1);
    		neighborCoord[ 2] = new Coord(coord.x  , coord.y-1);
    		neighborCoord[ 3] = new Coord(coord.x+1, coord.y-1);
    		neighborCoord[ 4] = new Coord(coord.x+2, coord.y-1);
    		neighborCoord[ 5] = new Coord(coord.x-2, coord.y  );
    		neighborCoord[ 6] = new Coord(coord.x-1, coord.y  );
    		neighborCoord[ 7] = new Coord(coord.x+1, coord.y  );
    		neighborCoord[ 8] = new Coord(coord.x+2, coord.y  );
    		neighborCoord[ 9] = new Coord(coord.x-1, coord.y+1);
    		neighborCoord[10] = new Coord(coord.x  , coord.y+1);
    		neighborCoord[11] = new Coord(coord.x+1, coord.y+1);
    		break;
    	}

		return neighborCoord;
	}

	protected override void CalcRegion() {
		AttrTriangle1 attr = Attr;
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
		case 2:
			region.setPoint(0, (int)(oX + a  ), (int)(oY    ));
			region.setPoint(1, (int)(oX +   b), (int)(oY + h));
			region.setPoint(2, (int)(oX      ), (int)(oY    ));
			break;
		case 3:
			region.setPoint(0, (int)(oX + a  ), (int)(oY    ));
			region.setPoint(1, (int)(oX + a+b), (int)(oY + h));
			region.setPoint(2, (int)(oX +   b), (int)(oY + h));
			break;
		}
	}

	public override Rect getRcInner(int borderWidth) {
		AttrTriangle1 attr = Attr;
		int area = attr.Area;
		double b = attr.CalcB(area);
		double sq = attr.CalcSq(area, borderWidth);
		double w = borderWidth/2.0;

		PointDouble center = new PointDouble(); // координата вписанного в фигуру квадрата (не совпадает с центром фигуры)
		switch (direction) {
		case 0: case 3:
			center.x = region.getPoint(2).x + b;
			center.y = region.getPoint(2).y - sq/2 - w;
			break;
		case 1: case 2:
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
		case 0: case 3: return 2;
		}
		return 1;
	}
}
}