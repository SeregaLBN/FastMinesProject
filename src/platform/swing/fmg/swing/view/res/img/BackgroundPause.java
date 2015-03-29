package fmg.swing.view.res.img;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;

import javax.swing.Icon;

/** картинка для фоновой паузы */
public class BackgroundPause implements Icon {
	private Logo _logo;

	public BackgroundPause() {
		_logo = new Logo(true);
		_logo.setMargin(10);
		_logo.setZoomX(2.7);
		_logo.setZoomY(2.7);
	}

	@Deprecated
	public BackgroundPause(boolean newLogo) {
		super();
		if (!newLogo)
			_logo = null;
	}

	public int getIconWidth() {
		return (_logo == null) ? 1000 : _logo.getIconWidth();
	}

	public int getIconHeight() {
		return (_logo == null) ? 1000 : _logo.getIconHeight();
	}

	public void paintIcon(Component c, Graphics g, int x, int y) {
		if (_logo != null) {
			_logo.paintIcon(c, g, x, y);
			return;
		}

		Color oldColor = g.getColor();
		Graphics2D g2 = (Graphics2D)g;
		Object oldValAntialiasing = g2.getRenderingHint(RenderingHints.KEY_ANTIALIASING);

		g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

//		// fill background (only transparent color)
//		g.setColor(new Color(0x00123456, true));
//		g.fillRect(0, 0, getIconWidth(), getIconHeight());

		// тело смайла
		g.setColor(new Color(0x00FFE600));
		g.fillOval(5, 5, getIconWidth()-10, getIconHeight()-10);

		// глаза
		g.setColor(new Color(0x00000000));
		g.fillOval(330, 150, 98, 296);
		g.fillOval(570, 150, 98, 296);

		// smile
		g2.setStroke(new BasicStroke(14, BasicStroke.CAP_ROUND, BasicStroke.JOIN_BEVEL));
		g.drawArc(103, -133, 795, 1003, 207, 126);

		// ямочки на щеках
		g.drawArc(90, 580, 180, 180, 85, 57);
		g.drawArc(730, 580, 180, 180, 38, 57);

		// restore
		g.setColor(oldColor);
		g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, oldValAntialiasing);
	}
}