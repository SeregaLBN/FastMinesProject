package fmg.swing.geom;

import java.awt.Dimension;
import java.awt.Polygon;
import java.awt.Rectangle;

import fmg.common.geom.Rect;
import fmg.common.geom.Region;
import fmg.common.geom.Size;

/**
 * Приведение типов от платформонезависемых читых Java классов fmg.common.geom.* к библиотечным SWING/AWT классам java.awt.*\java.swing.*
 * @author SeregaLBN
 *
 */
public final class Cast {
	
	public static    java.awt.Point toPoint(fmg.common.geom.Point p) { return new    java.awt.Point(p.x, p.y); }
	public static fmg.common.geom.Point toPoint(   java.awt.Point p) { return new fmg.common.geom.Point(p.x, p.y); }

	public static java.awt.geom.Point2D.Double toPoint(fmg.common.geom.PointDouble    p) { return new java.awt.geom.Point2D.Double(p.x, p.y); }
	public static   fmg.common.geom.PointDouble    toPoint(java.awt.geom.Point2D.Double p) { return new fmg.common.geom.PointDouble(   p.x, p.y); }
	
	public static Rectangle toRect(Rect      rc) { return new Rectangle(rc.x, rc.y, rc.width, rc.height); }
	public static Rect      toRect(Rectangle rc) { return new Rect     (rc.x, rc.y, rc.width, rc.height); }
	
	public static Dimension toSize(Size       size) { return new Dimension(size.width, size.height); }
	public static Size      toSize(Dimension  size) { return new Size     (size.width, size.height); }

	public static Polygon   toPolygon(Region region) {
		Polygon polygon = new Polygon();
		for (int i=0; i<region.getCountPoints(); i++) {
			fmg.common.geom.Point p = region.getPoint(i);
			polygon.addPoint(p.x, p.y);
		}
		return polygon;
	}

	public static java.awt.Color toColor(fmg.common.Color   clr) { return new java.awt.Color( 0xFF & clr.getR() ,  0xFF & clr.getG() ,  0xFF & clr.getB() ,  0xFF & clr.getA() ); }
	public static   fmg.common.Color toColor(java.awt.Color clr) { return new   fmg.common.Color((byte) clr.getRed(), (byte)clr.getGreen(), (byte)clr.getBlue(), (byte)clr.getAlpha()); }
}