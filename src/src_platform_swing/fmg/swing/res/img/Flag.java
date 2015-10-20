package fmg.swing.res.img;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Paint;
import java.awt.RenderingHints;
import java.awt.Stroke;
import java.awt.Toolkit;
import java.awt.geom.CubicCurve2D;
import java.awt.geom.Point2D;
import java.awt.image.MemoryImageSource;

import javax.swing.Icon;

import fmg.swing.utils.ImgUtils;

/** flag image */
public class Flag extends ImageWrapper {
	public Flag() {
		super(ImgUtils.toImg(createIcon()));
	}

	@Deprecated
	public Flag(boolean newFlagImage) {
		super(newFlagImage
				? ImgUtils.toImg(createIcon())
				: createImageOld());
	}
	
	public static Icon createIcon() {
		return new Icon() {
			private double _zoom = 1.7;

			public int getIconWidth() {
				return (int) (100 * _zoom);
			}

			public int getIconHeight() {
				return (int) (100 * _zoom);
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
//				g.drawLine((int)p[0].x, (int)p[0].y, (int)p[2].x, (int)p[2].y);
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
//			    if (false) {
//					curve = new CubicCurve2D.Double(
//							p[1].x, p[1].y,
//							55.5*_zoom, 15*_zoom,
//							45*_zoom, 62.5*_zoom,
//							p[3].x, p[3].y);
//				    g2.draw(curve);
//			    } else
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
		};
	}

	@Deprecated
	public static Image createImageOld() {
		int w = 11, h = 11;
		int pixels[] = new int[w*h];

		// fill background to transparent color
		for (int i=0; i<pixels.length; i++)
			pixels[i] = 0x00112233; // aarrggbb

		// paint image

		// центральная стойка
		for (int y=5; y<10; y++)
				pixels[y*w+6] = 0xFF000000;

		// поддон
		for (int x=4; x<9; x++)
				pixels[10*w+x] = 0xFF000000;
		pixels[10*w+3] =
		pixels[9*w+5] =
		pixels[9*w+7] =
		pixels[10*w+9] = 0x7F000000;

		// флаг
		int mX = 6;
		for (int y=1; y<5; y++, mX--)
			for (int x=2; x<mX; x++)
				pixels[y*w+x] = 0xFFFF0000;
		mX = 6;
		for (int y=1; y<5; y++, mX--)
			for (int x=mX; x<7; x++)
				pixels[y*w+x] = 0xFF800000;

		return Toolkit.getDefaultToolkit().createImage(new MemoryImageSource(w,h, pixels, 0,w));
	}
}
