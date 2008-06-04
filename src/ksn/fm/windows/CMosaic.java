package ksn.fm.windows;

import java.awt.Color;
import java.awt.Graphics;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JPanel;

public class CMosaic extends JPanel {
	private static final long serialVersionUID = -1;

	public void paintComponent(Graphics g) {
		super.paintComponent(g);

		ImageIcon ico = new ImageIcon("res/MinesWeeper.gif");
		int iW = getWidth();
		int iH = getHeight();

		g.drawImage(ico.getImage(), 0, 0, iW, iH, this);

		
//		g.setColor(new Color(244, 244, 244));
//		g.fillRect(0, 0, getWidth(), getHeight());
//
//		g.setColor(Color.WHITE);
//		g.drawLine(0, 0, getWidth() - 1, 0);
//		g.drawLine(0, 1, getWidth() - 2, 1);
//		g.drawLine(0, 2, getWidth() - 3, 2);
//
//		g.drawLine(0, 0, 0, getHeight() - 1);
//		g.drawLine(1, 0, 1, getHeight() - 2);
//		g.drawLine(2, 0, 2, getHeight() - 3);
//
//		g.setColor(Color.BLACK);
//		g.drawLine(1, getHeight() - 1, getWidth() - 1, getHeight() - 1);
//		g.drawLine(getWidth() - 1, 1, getWidth() - 1, getHeight() - 1);
//
//		g.setColor(new Color(204, 204, 204));
//		g.drawLine(2, getHeight() - 2, getWidth() - 2, getHeight() - 2);
//		g.drawLine(getWidth() - 2, 2, getWidth() - 2, getHeight() - 2);
//
//		g.setColor(new Color(224, 224, 224));
//		g.drawLine(3, getHeight() - 3, getWidth() - 3, getHeight() - 3);
//		g.drawLine(getWidth() - 3, 3, getWidth() - 3, getHeight() - 3);
//
////		 g.setColor(Color.BLACK);
////		 if (icon != null) g.drawImage(icon.getImage(), px + 1, py + 1, this);
////		 if (title != null && title.length() > 0) g.drawString(title, sx + 1,
////		 sy + 1);
	}
}
