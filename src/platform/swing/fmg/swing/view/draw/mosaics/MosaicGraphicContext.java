package fmg.swing.view.draw.mosaics;

import java.awt.Color;

import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.UIManager;

import fmg.common.geom.Size;
import fmg.swing.view.draw.GraphicContext;

public class MosaicGraphicContext extends GraphicContext {
	public static final Color COLOR_BTNFACE = Color.GRAY;

	private Color  	  colorBk;
	private ImageIcon imgBckgrnd;

	public MosaicGraphicContext(JComponent owner) {
		super(owner, false, new Size(0,0));
	}

	public Color getColorBk() {
		if (colorBk == null) {
//			setColorBk(COLOR_BTNFACE);
//			setColorBk(Color.BLACK);
//			setColorBk(UIManager.getDefaults().getColor("Panel.background"));

			Color clr = UIManager.getDefaults().getColor("Panel.background");
			float perc = 0.40f; // делаю темнее
			int _r = (int) (clr.getRed  () - clr.getRed  () * perc);
			int _g = (int) (clr.getGreen() - clr.getGreen() * perc);
			int _b = (int) (clr.getBlue () - clr.getBlue () * perc);
			setColorBk(new Color(_r,_g,_b));
		}
		return colorBk;
//		return Color.white;
	}
	public void setColorBk(Color colorBk) {
		Object old = this.colorBk;
		this.colorBk = colorBk;
		propertyChanges.firePropertyChange("GraphicContext_colorBk", old, colorBk);
	}

	public ImageIcon getImgBckgrnd() {
		return imgBckgrnd;
	}
	public void setImgBckgrnd(ImageIcon imgBckgrnd) {
		Object old = this.imgBckgrnd;
		this.imgBckgrnd = imgBckgrnd;
		propertyChanges.firePropertyChange("GraphicContext_imgBckgrnd", old, imgBckgrnd);
	}
}
