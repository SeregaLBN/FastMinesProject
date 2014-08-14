package ua.ksn.fmg.view.swing.res.img;

import java.awt.Component;
import java.awt.Graphics;

import javax.swing.Icon;

import ua.ksn.swing.geom.Cast;

/** mine image */
public class Mine implements Icon {
	private Logo _logo;

	public Mine() {
		_logo = new Logo(false);
		_logo.setMargin(10);
		_logo.setZoomX(0.7);
		_logo.setZoomY(0.7);
		for (int i = 0; i<_logo.Palette.length; i++)
			_logo.Palette[i] = Cast.toColor(Cast.toColor(_logo.Palette[i]).darker(0.5));
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