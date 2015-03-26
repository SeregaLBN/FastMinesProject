package ua.ksn.fmg.view.swing.res.img;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Component;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.RenderingHints;
import java.awt.Stroke;
import java.awt.geom.Point2D;

import javax.swing.Icon;

import ua.ksn.swing.geom.Cast;

/** main logos image */
public class Logo implements Icon {
	public static final int DefaultWidht = 200;
	public static final int DefaultHeight = 200;

	private double _zoomX;
	private double _zoomY;
	private int _margin;
	private final boolean _useGradient;

	public Logo(boolean useGradient) {
		_zoomX = 1;
		_zoomY = 1;
		_margin = 3;
		_useGradient = useGradient;
	}

	public final Color[] Palette = {
			new Color(0xFF0000), new Color(0xFFD800), new Color(0x4CFF00), new Color(0x00FF90),
			new Color(0x0094FF), new Color(0x4800FF), new Color(0xB200FF), new Color(0xFF006E) };

	public static double CalcZoom(int desiredLogoWidhtHeight, int margin) {
		// desiredLogoWidhtHeight = DefaultHeight*zoom+2*margin
		return (desiredLogoWidhtHeight - 2.0 * margin) / DefaultHeight;
	}

	public void MixLoopColor(int loop) {
//		ua.ksn.Color[] copy = Palette.clone();
//		for (int i = 0; i < Palette.length; i++)
//			Palette[i] = copy[(i + loop) % 8];
	}

	public int getIconWidth() {
		return (int) (DefaultWidht * _zoomX + 2 * _margin);
	}

	public int getIconHeight() {
		return (int) (DefaultHeight * _zoomY + 2 * _margin);
	}

	public void paintIcon(Component c, Graphics g, int x, int y) {
		Color oldColor = g.getColor();
		Graphics2D g2 = (Graphics2D)g;
		Object oldValAntialiasing = g2.getRenderingHint(RenderingHints.KEY_ANTIALIASING);
		Stroke oldStroke = g2.getStroke();
		Paint oldGPaint = g2.getPaint();

		g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

		final int iPenWidth = 2;

		// draw star
		Point2D.Double[] rays = new Point2D.Double[] { // owner rays points
			new Point2D.Double(getMargin()+100.0000*getZoomX(), getMargin()+200.0000*getZoomY()),
			new Point2D.Double(getMargin()+170.7107*getZoomX(), getMargin()+ 29.2893*getZoomY()),
			new Point2D.Double(getMargin()+  0.0000*getZoomX(), getMargin()+100.0000*getZoomY()),
			new Point2D.Double(getMargin()+170.7107*getZoomX(), getMargin()+170.7107*getZoomY()),
			new Point2D.Double(getMargin()+100.0000*getZoomX(), getMargin()+  0.0000*getZoomY()),
			new Point2D.Double(getMargin()+ 29.2893*getZoomX(), getMargin()+170.7107*getZoomY()),
			new Point2D.Double(getMargin()+200.0000*getZoomX(), getMargin()+100.0000*getZoomY()),
			new Point2D.Double(getMargin()+ 29.2893*getZoomX(), getMargin()+ 29.2893*getZoomY())};
		Point2D.Double[] inn = new Point2D.Double[] { // inner octahedron
			new Point2D.Double(getMargin()+100.0346*getZoomX(), getMargin()+141.4070*getZoomY()),
			new Point2D.Double(getMargin()+129.3408*getZoomX(), getMargin()+ 70.7320*getZoomY()),
			new Point2D.Double(getMargin()+ 58.5800*getZoomX(), getMargin()+100.0000*getZoomY()),
			new Point2D.Double(getMargin()+129.2500*getZoomX(), getMargin()+129.2500*getZoomY()),
			new Point2D.Double(getMargin()+ 99.9011*getZoomX(), getMargin()+ 58.5377*getZoomY()),
			new Point2D.Double(getMargin()+ 70.7233*getZoomX(), getMargin()+129.3198*getZoomY()),
			new Point2D.Double(getMargin()+141.4167*getZoomX(), getMargin()+100.0000*getZoomY()),
			new Point2D.Double(getMargin()+ 70.7500*getZoomX(), getMargin()+ 70.7500*getZoomY())};
		Point2D.Double[] oct = new Point2D.Double[] { // central octahedron
			new Point2D.Double(getMargin()+120.7053*getZoomX(), getMargin()+149.9897*getZoomY()),
			new Point2D.Double(getMargin()+120.7269*getZoomX(), getMargin()+ 50.0007*getZoomY()),
			new Point2D.Double(getMargin()+ 50.0034*getZoomX(), getMargin()+120.7137*getZoomY()),
			new Point2D.Double(getMargin()+150.0000*getZoomX(), getMargin()+120.6950*getZoomY()),
			new Point2D.Double(getMargin()+ 79.3120*getZoomX(), getMargin()+ 50.0007*getZoomY()),
			new Point2D.Double(getMargin()+ 79.2624*getZoomX(), getMargin()+149.9727*getZoomY()),
			new Point2D.Double(getMargin()+150.0000*getZoomX(), getMargin()+ 79.2737*getZoomY()),
			new Point2D.Double(getMargin()+ 50.0034*getZoomX(), getMargin()+ 79.3093*getZoomY())};

		Point2D.Double center = new Point2D.Double(getIconWidth()/2, getIconHeight()/2);

		// paint owner gradient rays
		for (int i=0; i<8; i++) {
			if (_useGradient) {
				// rectangle gragient
				g2.setPaint(new GradientPaint(oct[(i+5)%8], Palette[(i+0)%8], oct[i], Palette[(i+3)%8]));
				g2.fillPolygon(new int[] {
						(int)rays[i].x,
						(int)oct[i].x,
						(int)inn[i].x,
						(int)oct[(i+5)%8].x
					}, new int[] {
						(int)rays[i].y,
						(int)oct[i].y,
						(int)inn[i].y,
						(int)oct[(i+5)%8].y
					}, 4);

				// emulate triangle gradient (see BmpLogo.cpp Ñ++ source code)
//				Color clr = Palette[(i+6)%8];
//				clr = new Color(clr.getRed(), clr.getGreen(), clr.getBlue(), 0);
//				g2.setPaint(new GradientPaint(center, clr, inn[(i+6)%8], Palette[(i+3)%8]));
//				g2.fillPolygon(new int[] {
//						(int)rays[i].x,
//						(int)oct[i].x,
//						(int)inn[i].x
//					}, new int[] {
//						(int)rays[i].y,
//						(int)oct[i].y,
//						(int)inn[i].y
//					}, 3);
//				g2.setPaint(new GradientPaint(center, clr, inn[(i+2)%8], Palette[(i+0)%8]));
//				g2.fillPolygon(new int[] {
//						(int)rays[i].x,
//						(int)oct[(i+5)%8].x,
//						(int)inn[i].x
//					}, new int[] {
//						(int)rays[i].y,
//						(int)oct[(i+5)%8].y,
//						(int)inn[i].y
//					}, 3);
			} else {
				g.setColor(Cast.toColor(Cast.toColor(Palette[i]).darker()));
				g.fillPolygon(new int [] {
						(int)rays[i].x,
						(int)oct[i].x,
						(int)inn[i].x,
						(int)oct[(i+5)%8].x
					}, new int [] {
						(int)rays[i].y,
						(int)oct[i].y,
						(int)inn[i].y,
						(int)oct[(i+5)%8].y
					}, 4);
			}
		}

		// paint star perimeter
		g2.setStroke(new BasicStroke(iPenWidth));
		for (int i=0; i<8; i++) {
			Point2D.Double p1 = rays[(i + 7)%8];
			Point2D.Double p2 = rays[i];
			g.setColor(Palette[i]);
			g.drawLine((int)p1.x, (int)p1.y, (int)p2.x, (int)p2.y);
		}

		// paint inner gradient triangles
		for (int i=0; i<8; i++) {
			if (_useGradient)
				g2.setPaint(new GradientPaint(
						inn[i], Palette[(i+6)%8],
						center, ((i&1)==0) ? Color.BLACK : Color.WHITE));
			else
				g.setColor(((i & 1) == 0)
						? Cast.toColor(Cast.toColor(Palette[(i + 6)%8]).brighter())
						: Cast.toColor(Cast.toColor(Palette[(i + 6)%8]).darker()));
			g.fillPolygon(new int [] {
					(int)inn[(i + 0)%8].x,
					(int)inn[(i + 3)%8].x,
					(int)center.x
				}, new int [] {
					(int)inn[(i + 0)%8].y,
					(int)inn[(i + 3)%8].y,
					(int)center.y
				}, 3);
		}

		// restore
		g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, oldValAntialiasing);
		g2.setStroke(oldStroke);
		g2.setPaint(oldGPaint);
		g.setColor(oldColor);
	}

	public double getZoomX() {
		return _zoomX;
	}

	public void setZoomX(double zoomX) {
		this._zoomX = zoomX;
	}

	public double getZoomY() {
		return _zoomY;
	}

	public void setZoomY(double zoomY) {
		this._zoomY = zoomY;
	}

	public int getMargin() {
		return _margin;
	}

	public void setMargin(int margin) {
		this._margin = margin;
	}

}