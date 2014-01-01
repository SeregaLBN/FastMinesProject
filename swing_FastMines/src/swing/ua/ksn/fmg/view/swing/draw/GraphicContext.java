package ua.ksn.fmg.view.swing.draw;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import javax.swing.ImageIcon;
import javax.swing.JComponent;

import ua.ksn.Color;
import ua.ksn.fmg.view.draw.ColorText;
import ua.ksn.fmg.view.draw.PenBorder;

public class GraphicContext  {
	protected PropertyChangeSupport propertyChanges = new PropertyChangeSupport(this);
	/**  ����������� �� ����������� ��������� ������� GraphicContext */
	public void addPropertyChangeListener(PropertyChangeListener l) {
		propertyChanges.addPropertyChangeListener(l);
	}
	/**  ���������� �� ����������� ��������� ������� GraphicContext */
	public void removePropertyChangeListener(PropertyChangeListener l) {
		propertyChanges.removePropertyChangeListener(l);
	}

	private ImageIcon imgMine, imgFlag;
	
	/** TODO: Mosaic field - ���� ����������... */
	private JComponent owner;

	private ColorText colorText;
	protected PenBorder penBorder;
	private boolean iconicMode;

	public GraphicContext(JComponent owner) {
		this.owner = owner;
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

	/** �� ��� ���������� � ������� ����� ������ */
	public class BackgroundFill {
		/** ����� ������� ���� ����� */
		private int mode = 0;
		/** ������������ ����� ���� ����� */
		private Map<Integer, Color> colors;

		/** ����� ������� ���� ����� */
		public int getMode() {
			return mode;
		}
		/**
		/* ����� ������� ���� �����
		 * @param mode
		 *  <li> 0 - ���� ������� ���� ��-���������
		 *  <li> not 0 - ������ %)
		 */
		public void setMode(int newFillMode) {
			this.mode = newFillMode;
			getColors().clear();
		}

		/** ������������ ����� ���� �����
		/** <br/> ��� �����? - ���������� � ������ ��������������! */
		public Map<Integer, Color> getColors() {
			if (colors == null)
				colors = new HashMap<Integer, Color>() {
					private static final long serialVersionUID = 1L;

					@Override
					public Color get(Object key) {
						Color res = super.get(key);
						if (res == null) {
							final int base = 120; // �� �������� ������� ��������� ���� ��������� ����� ����
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
	public void setIconicMode(boolean iconicMode) {
		this.iconicMode = iconicMode;
	}
}