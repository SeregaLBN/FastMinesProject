package ksn.fm.Mosaic.Types;

/**
 * Создал только ради default constructor
 * @see java.awt.Color  
 * @author Serega
 */
public class Color extends java.awt.Color {
	private static final long serialVersionUID = 3801143233046986494L;

	/**
	 * @return java.awt.Color.BLACK
	 */
	public Color() {
		super(0,0,0);
	}
	public Color(Color c) {
		super(c.getRed(), c.getGreen(), c.getBlue(), c.getAlpha());
	}
	public Color(java.awt.Color c) {
		super(c.getRed(), c.getGreen(), c.getBlue(), c.getAlpha());
	}
	public Color(int r, int g, int b) {
		super(r, g, b);
	}
	public Color(int rgb) {
		super(rgb);
	}
}
