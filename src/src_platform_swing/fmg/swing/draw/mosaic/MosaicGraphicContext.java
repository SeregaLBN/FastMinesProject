package fmg.swing.draw.mosaic;

import java.awt.Color;

import javax.swing.ImageIcon;
import javax.swing.JComponent;

import fmg.swing.Cast;
import fmg.swing.draw.GraphicContext;

public class MosaicGraphicContext extends GraphicContext {
	private Color  	  colorBk;
	private ImageIcon imgBckgrnd;

	public MosaicGraphicContext(JComponent owner) {
		super(owner, false);
	}

	public Color getColorBk() {
		if (colorBk == null) {
			fmg.common.Color clr = GraphicContext.getDefaultBackgroundFillColor();
			setColorBk(Cast.toColor(clr.darker(0.4)));
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
