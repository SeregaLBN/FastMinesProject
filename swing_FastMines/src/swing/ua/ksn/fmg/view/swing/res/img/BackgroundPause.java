package ua.ksn.fmg.view.swing.res.img;

import java.awt.Component;
import java.awt.Graphics;

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

	public int getIconWidth() {
		return _logo.getIconWidth();
	}

	public int getIconHeight() {
		return _logo.getIconHeight();
	}

	public void paintIcon(Component c, Graphics g, int x, int y) {
		_logo.paintIcon(c, g, x, y);
	}
}