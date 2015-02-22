package ua.ksn;

import java.util.Random;

public class Color {
	public static final Color Black   = new Color(0xFF000000);
	public static final Color White   = new Color(0xFFFFFFFF);
	public static final Color Navy    = new Color(0xFF000080);
	public static final Color Green   = new Color(0xFF008000);
	public static final Color Red     = new Color(0xFFFF0000);
	public static final Color Maroon  = new Color(0xFF800000);
	public static final Color Blue    = new Color(0xFF0000FF);
	public static final Color Olive   = new Color(0xFF808000);
	public static final Color Aqua    = new Color(0xFF00FFFF);
	public static final Color Teal    = new Color(0xFF008080);
	public static final Color Magenta = new Color(0xFFFF00FF); // Fuchsia
	public static final Color Gray    = new Color(0xFF808080);

	protected byte r,g,b,a;
	
	public Color() {
		this((byte)0, (byte)0, (byte)0, (byte)255);
	}
	private static byte int_to_byte(int v, String name) {
		if (v<0 || v>255) throw new IllegalArgumentException("Bad "+name+" argument");
		return (byte)(v&0xFF);
	}
	public Color(int r, int g, int b, int a) {
		this(int_to_byte(r, "RED"), int_to_byte(g, "GREEN"), int_to_byte(b, "BLUE"), int_to_byte(a, "ALPHA"));
	}
	public Color(byte r, byte g, byte b, byte a) {
		this.r=r;
		this.g=g;
		this.b=b;
		this.a=a;
	}
	public Color(int r, int g, int b) {
		this(r, g, b, 255);
	}
	public Color(byte r, byte g, byte b) {
		this(r, g, b, (byte)255);
	}
	public Color(int OxAARRGGBB) {
		this((byte)((OxAARRGGBB >> 16) &  0xFF),
			(byte)((OxAARRGGBB >> 8) &  0xFF),
			(byte)((OxAARRGGBB >> 0) &  0xFF),
			(byte)((OxAARRGGBB >> 24) &  0xFF));
	}

	@Override
	public int hashCode() {
		return ((a & 0xFF)<<24) | ((r & 0xFF)<<16) | ((g & 0xFF)<<8) | (b & 0xFF);
	}
	@Override
	public boolean equals(Object obj) {
		if (obj == this)
			return true;
		if (!(obj instanceof Color))
			return false;
		Color clr = (Color)obj;
		return (clr.a==a) &&(clr.r==r) &&(clr.g==g) &&(clr.b==b); 
	}
	@Override
	public String toString() {
		return String.format("argb=%02X%02X%02X%02X", a,r,g,b);
	}

	public byte getR() { return r; }
	public void setR(byte r) { this.r = r; }
	public byte getG() { return g; }
	public void setG(byte g) { this.g = g; }
	public byte getB() { return b; 	}
	public void setB(byte b) { this.b = b; }
	public byte getA() { return a; }
	public void setA(byte a) { this.a = a; }

	public static Color RandomColor(Random rnd) {
		return new Color(
			(byte)rnd.nextInt(0xFF),
			(byte)rnd.nextInt(0xFF),
			(byte)rnd.nextInt(0xFF));
	}

	/**
	 * Смягчить цвет
	 * @param clr
	 * @param basic - от заданной границы светлости буду создавать новый цвет
	 * @param withAlphaChanel
	 * @return
	 */
	public Color attenuate(int basic /* = 120 */) {
		if (basic < 0 || basic >= 0xFF)
			throw new IllegalArgumentException();
		return new Color(
			(byte) (basic + (0xFF & this.r)%(0xFF - basic)),
			(byte) (basic + (0xFF & this.g)%(0xFF - basic)),
			(byte) (basic + (0xFF & this.b)%(0xFF - basic)),
			this.a
	);
	}
	public Color attenuate() { return this.attenuate(120); }

	/**
	 * Creates brighter version of this Color
	 * @param percent - 0.0 - as is; 1 - WHITE
	 * @return
	 */
	public Color brighter(double percent) {
		Color tmp = new Color((byte)(0xFF - (0xFF & this.r)), (byte)(0xFF - (0xFF & this.g)), (byte)(0xFF - (0xFF & this.b)), this.a);
		tmp = tmp.darker(percent);
		return new Color((byte)(0xFF - (0xFF & tmp.r)), (byte)(0xFF - (0xFF & tmp.g)), (byte)(0xFF - (0xFF & tmp.b)), tmp.a);
	}
	public Color brighter() { return this.brighter(0.7); }

	/**
	 * Creates darker version of this Color
	 * @param percent - 0.0 - as is; 1 - BLACK
	 * @return
	 */
	public Color darker(double percent) {
		double tmp = 1 - Math.min(1, Math.max(0, percent));
		return new Color(
			(byte)((0xFF & this.r)*tmp),
			(byte)((0xFF & this.g)*tmp),
			(byte)((0xFF & this.b)*tmp),
			this.a);
	}
	public Color darker() { return darker(0.7); }
}