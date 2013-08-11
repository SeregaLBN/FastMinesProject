package ua.ksn.fmg.view.swing.draw;

import java.awt.Color;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;

/** ’арактеристики кисти у рамки €чейки */
public class PenBorder {
	private PropertyChangeSupport propertyChanges = new PropertyChangeSupport(this);
	/**  подписатьс€ на уведомлени€ изменений PenBorder */
	public void addPropertyChangeListener(PropertyChangeListener l) {
		propertyChanges.addPropertyChangeListener(l);
	}
	/**  отписатьс€ от уведомлений изменений PenBorder */
	public void removePropertyChangeListener(PropertyChangeListener l) {
		propertyChanges.removePropertyChangeListener(l);
	}

    private Color colorShadow, colorLight;
    private int width;

    public PenBorder() {
    	this(Color.BLACK, Color.WHITE, 3);
//    	this(Color.GREEN, Color.RED, 1);
    }
    public PenBorder(
    		Color colorShadow,
    		Color colorLight,
    		int iWidth)
    {
    	this.colorShadow = colorShadow;
    	this.colorLight  = colorLight;
    	this.width = iWidth;
    }

    public Color getColorShadow() {
		return colorShadow;
	}
	public void setColorShadow(Color colorShadow) {
		Color old = colorShadow;
		this.colorShadow = colorShadow;
		propertyChanges.firePropertyChange("PenBorder_colorShadow", old, colorShadow);
	}
	public Color getColorLight() {
		return colorLight;
	}
	public void setColorLight(Color colorLight) {
		Color old = colorLight;
		this.colorLight = colorLight;
		propertyChanges.firePropertyChange("PenBorder_colorLight", old, colorLight);
	}
	public int getWidth() {
		return width;
	}
	public void setWidth(int iWidth) {
		int old = iWidth;
		this.width = iWidth;
		propertyChanges.firePropertyChange("PenBorder_width", old, iWidth);
	}

	@Override
	public boolean equals(Object obj) {
		if (obj instanceof PenBorder) {
			PenBorder penObj = (PenBorder)obj;
			return (width == penObj.width)
				&& colorShadow.equals(penObj.colorShadow)
				&& colorLight.equals(penObj.colorLight);
		}
		//return super.equals(obj);
		return false;
	}
}
