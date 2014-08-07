package ua.ksn.fmg.view.swing.res.img;

import java.awt.Color;

import java.awt.BasicStroke;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;

import javax.swing.Icon;

/** картинка для фоновой паузы */
@Deprecated
public class BackgroundPauseOld implements Icon {
	public int getIconWidth() {
		return 1000;
	}

	public int getIconHeight() {
		return 1000;
	}

	public void paintIcon(Component c, Graphics g, int x, int y) {
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