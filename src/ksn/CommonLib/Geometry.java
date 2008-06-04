package ksn.CommonLib;

import ksn.utils.Types.GraphPrimitives.Point;

public final class Geometry {

	public static boolean PointInPolygon(Point point, Point [] polygon, int size) {
		float x = point.x+0.01f;
		float y = point.y+0.01f;
		int count = 0;
		for (int i=0; i<size; i++) {
			int j = (i+1)%size;
			if (polygon[i].y == polygon[j].y) continue;
			if (polygon[i].y > y && polygon[j].y > y) continue;
			if (polygon[i].y < y && polygon[j].y < y) continue;
			if (Math.max(polygon[i].y, polygon[j].y) == y) count++;
			else
				if (Math.min(polygon[i].y, polygon[j].y) == y) continue;
				else {
					float t = (float)(y-polygon[i].y)/(float)(polygon[j].y-polygon[i].y);
					if (t>0 && t<1 && polygon[i].x+t*(polygon[j].x-polygon[i].x) >= x) count++;
				}
		}
		return (count%2) == 1;
	}
}
