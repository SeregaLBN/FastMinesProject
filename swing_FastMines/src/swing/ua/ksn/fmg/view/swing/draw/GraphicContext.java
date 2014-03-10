package ua.ksn.fmg.view.swing.draw;

import java.awt.Font;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import javax.swing.ImageIcon;
import javax.swing.JComponent;

import ua.ksn.Color;
import ua.ksn.geom.Size;
import ua.ksn.fmg.view.draw.ColorText;
import ua.ksn.fmg.view.draw.PenBorder;

public class GraphicContext  {
	public static final Font DEFAULT_FONT = new Font("SansSerif", Font.PLAIN, 10);

	protected PropertyChangeSupport propertyChanges = new PropertyChangeSupport(this);
	/**  подписатьс€ на уведомлени€ изменений свойств GraphicContext */
	public void addPropertyChangeListener(PropertyChangeListener l) {
		propertyChanges.addPropertyChangeListener(l);
	}
	/**  отписатьс€ от уведомлений изменений свойств GraphicContext */
	public void removePropertyChangeListener(PropertyChangeListener l) {
		propertyChanges.removePropertyChangeListener(l);
	}

	/** TODO: Mosaic field - нуна избавитьс€... */
	private JComponent owner;

	private ImageIcon imgMine, imgFlag;
	private ColorText colorText;
	protected PenBorder penBorder;
	private Font   	  font;
	private final boolean iconicMode;
	private final Size bound;

	public GraphicContext(JComponent owner, boolean iconicMode, Size bound) {
		this.owner = owner;
		this.iconicMode = iconicMode;
		this.bound = bound;
	}

	public ImageIcon getImgMine() {
		return imgMine;
	}
	public void setImgMine(ImageIcon img) {
		Object old = this.imgMine;
		this.imgMine = img;
		propertyChanges.firePropertyChange("GraphicContext_imgMine", old, img);
	}
	public ImageIcon getImgFlag() {
		return imgFlag;
	}
	public void setImgFlag(ImageIcon img) {
		Object old = this.imgFlag;
		this.imgFlag = img;
		propertyChanges.firePropertyChange("GraphicContext_imgFlag", old, img);
	}

	public ColorText getColorText() {
		if (colorText == null)
			setColorText(new ColorText());
		return colorText;
	}
	public void setColorText(ColorText colorText) {
		ColorText old = this.colorText;
		this.colorText = colorText;
		propertyChanges.firePropertyChange("GraphicContext_colorText", old, colorText);
	}

	public PenBorder getPenBorder() {
		if (penBorder == null)
			setPenBorder(new PenBorder());
		return penBorder;
	}
	public void setPenBorder(PenBorder penBorder) {
		PenBorder old = this.penBorder;
		this.penBorder = penBorder;
		propertyChanges.firePropertyChange("GraphicContext_penBorder", old, penBorder);
	}

	public JComponent getOwner() {
		return owner;
	}

	/** всЄ что относитьс€ к заливке фоном €чееек */
	public class BackgroundFill {
		/** режим заливки фона €чеек */
		private int mode = 0;
		/** кэшированные цвета фона €чеек */
		private Map<Integer, Color> colors;

		/** режим заливки фона €чеек */
		public int getMode() {
			return mode;
		}
		/**
		/* режим заливки фона €чеек
		 * @param mode
		 *  <li> 0 - цвет заливки фона по-умолчанию
		 *  <li> not 0 - радуга %)
		 */
		public void setMode(int newFillMode) {
			this.mode = newFillMode;
			getColors().clear();
		}

		/** кэшированные цвета фона €чеек
		/** <br/> Ќет цвета? - создастьс€ с нужной интенсивностью! */
		public Map<Integer, Color> getColors() {
			if (colors == null)
				colors = new HashMap<Integer, Color>() {
					private static final long serialVersionUID = 1L;

					@Override
					public Color get(Object key) {
						Color res = super.get(key);
						if (res == null) {
							final int base = 120; // от заданной границы светлости буду создавать новый цвет
							Random rand = new Random();
							int r = base + rand.nextInt(0xFF-base);
							int g = base + rand.nextInt(0xFF-base);
							int b = base + rand.nextInt(0xFF-base);
							res = new Color((byte)r,(byte)g,(byte)b);
							super.put((Integer)key, res);
						}
						return res;
					}
				};
			return colors;
		}
	}
	private BackgroundFill _backgroundFill;
	public BackgroundFill getBackgroundFill() {
		if (_backgroundFill == null)
			_backgroundFill = new BackgroundFill();
		return _backgroundFill;
	}

	public boolean isIconicMode() {
		return iconicMode;
	}

	public Size getBound() {
		return bound;
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
}