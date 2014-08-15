package ua.ksn.swing.geom;

import java.awt.Dimension;
import java.awt.Polygon;
import java.awt.Rectangle;

import ua.ksn.geom.Rect;
import ua.ksn.geom.Region;
import ua.ksn.geom.Size;

/**
 * Приведение типов от платформонезависемых читых Java классов ua.ksn.geom.* к библиотечным SWING/AWT классам java.awt.*\java.swing.*
 * @author SeregaLBN
 *
 */
public final class Cast {
	
	public static    java.awt.Point toPoint(ua.ksn.geom.Point p) { return new    java.awt.Point(p.x, p.y); }
	public static ua.ksn.geom.Point toPoint(   java.awt.Point p) { return new ua.ksn.geom.Point(p.x, p.y); }

	public static java.awt.geom.Point2D.Double toPoint(ua.ksn  .geom.PointDouble    p) { return new java.awt.geom.Point2D.Double(p.x, p.y); }
	public static   ua.ksn.geom.PointDouble    toPoint(java.awt.geom.Point2D.Double p) { return new ua.ksn  .geom.PointDouble(   p.x, p.y); }
	
	public static Rectangle toRect(Rect      rc) { return new Rectangle(rc.x, rc.y, rc.width, rc.height); }
	public static Rect      toRect(Rectangle rc) { return new Rect     (rc.x, rc.y, rc.width, rc.height); }
	
	public static Dimension toSize(Size       size) { return new Dimension(size.width, size.height); }
	public static Size      toSize(Dimension  size) { return new Size     (size.width, size.height); }

	public static Polygon   toPolygon(Region region) {
		Polygon polygon = new Polygon();
		for (int i=0; i<region.getCountPoints(); i++) {
			ua.ksn.geom.Point p = region.getPoint(i);
			polygon.addPoint(p.x, p.y);
		}
		return polygon;
	}

	public static java.awt.Color toColor(ua.ksn.Color   clr) { return new java.awt.Color( 0xFF & clr.getR() ,  0xFF & clr.getG() ,  0xFF & clr.getB() ,  0xFF & clr.getA() ); }
	public static   ua.ksn.Color toColor(java.awt.Color clr) { return new   ua.ksn.Color((byte) clr.getRed(), (byte)clr.getGreen(), (byte)clr.getBlue(), (byte)clr.getAlpha()); }
}