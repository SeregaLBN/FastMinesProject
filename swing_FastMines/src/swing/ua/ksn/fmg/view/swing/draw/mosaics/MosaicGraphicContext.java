package ua.ksn.fmg.view.swing.draw.mosaics;

import java.awt.Color;
import java.awt.Font;

import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.UIManager;

import ua.ksn.fmg.view.swing.draw.GraphicContext;

public class MosaicGraphicContext extends GraphicContext {
	public static final Font DEFAULT_FONT = new Font("SansSerif", Font.PLAIN, 10);
	public static final Color COLOR_BTNFACE = Color.GRAY;

	private Font   	  font;
	private Color  	  colorBk;
	private ImageIcon imgBckgrnd;

	public MosaicGraphicContext(JComponent owner) {
		super(owner, false);
	}

	public Font getFont() {
		if (font == null)
			setFont(DEFAULT_FONT);
		return font;
	}
	private void setRawFont(Font font) {
		Object old = this.font;
		this.font = font;
		propertyChanges.firePropertyChange("GraphicContext_font", old, font);
	}
	public void setFont(Font newFont) {
		if (font != null) {
			if (font.getName().equals(newFont.getName()) &&
				(font.getStyle() == newFont.getStyle()) &&
				(font.getSize() == newFont.getSize()))
				return;
	
			int heightNeed = font.getSize();
			int heightBad = newFont.getSize();
			if (heightNeed != heightBad)
				newFont = new Font(newFont.getName(), newFont.getStyle(), heightNeed);
		}
		setRawFont(newFont);
	}
	public void setFontSize(int size) {
//		size = 9; // debug
		Font fnt = getFont();
		if (fnt.getSize() == size)
			return;
		setRawFont(new Font(fnt.getName(), fnt.getStyle(), size));
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
