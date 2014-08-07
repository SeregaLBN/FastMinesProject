package ua.ksn.fmg.view.swing.res.img;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.RenderingHints;
import java.awt.Stroke;
import java.awt.geom.CubicCurve2D;
import java.awt.geom.Point2D;

import javax.swing.Icon;

/** flag image */
public class Flag implements Icon {
	public static final int DefaultWidht = 100;
	public static final int DefaultHeight = 100;
	private double _zoom = 1.7;

	public int getIconWidth() {
		return (int) (DefaultWidht * _zoom);
	}

	public int getIconHeight() {
		return (int) (DefaultHeight * _zoom);
	}

	public void paintIcon(Component c, Graphics g, int x, int y) {
		Color oldColor = g.getColor();
		Graphics2D g2 = (Graphics2D)g;
		Stroke oldStroke = g2.getStroke();
		Paint oldGPaint = g2.getPaint();
		Object oldValAntialiasing = g2.getRenderingHint(RenderingHints.KEY_ANTIALIASING);

		g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

		// perimeter figure points
		Point2D.Double[] p = new Point2D.Double[] {
				new Point2D.Double(13.5 *_zoom, 90*_zoom),
				new Point2D.Double(17.44*_zoom, 51*_zoom),
				new Point2D.Double(21   *_zoom, 16*_zoom),
				new Point2D.Double(85   *_zoom, 15*_zoom),
				new Point2D.Double(81.45*_zoom, 50*_zoom)};

		BasicStroke penLine = new BasicStroke(15, BasicStroke.CAP_SQUARE, BasicStroke.JOIN_MITER);
		g2.setStroke(penLine);
		g.setColor(Color.BLACK);
//		g.drawLine((int)p[0].x, (int)p[0].y, (int)p[2].x, (int)p[2].y);
		g.drawLine((int)p[0].x, (int)p[0].y, (int)p[1].x, (int)p[1].y);

		BasicStroke penCurve = new BasicStroke(12, BasicStroke.CAP_ROUND, BasicStroke.JOIN_MITER);
		g2.setStroke(penCurve);
		g.setColor(Color.RED);
		CubicCurve2D curve = new CubicCurve2D.Double(
				p[2].x, p[2].y,
				95*_zoom, 0*_zoom,
				19.3*_zoom, 32*_zoom,
				p[3].x, p[3].y);
	    g2.draw(curve);
//	    if (false) {
//			curve = new CubicCurve2D.Double(
//					p[1].x, p[1].y,
//					55.5*_zoom, 15*_zoom,
//					45*_zoom, 62.5*_zoom,
//					p[3].x, p[3].y);
//		    g2.draw(curve);
//	    } else
	    {
			curve = new CubicCurve2D.Double(
					p[1].x, p[1].y,
					91.45*_zoom, 35*_zoom,
					15.83*_zoom, 67*_zoom,
					p[4].x, p[4].y);
		    g2.draw(curve);
		    curve = new CubicCurve2D.Double(
					p[3].x, p[3].y,
					77.8*_zoom, 32.89*_zoom,
					88.05*_zoom, 22.73*_zoom,
					p[4].x, p[4].y);
		    g2.draw(curve);
	    }
	    g2.setStroke(penLine);
	    g.drawLine((int)p[1].x, (int)p[1].y, (int)p[2].x, (int)p[2].y);

		// restore
		g.setColor(oldColor);
		g2.setStroke(oldStroke);
		g2.setPaint(oldGPaint);
		g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, oldValAntialiasing);
	}
}