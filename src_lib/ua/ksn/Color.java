package ua.ksn;

public class Color {
	public static final Color Black  = new Color(0x00000000);
	public static final Color White  = new Color(0x00FFFFFF);
	public static final Color Navy   = new Color(0x00000080);
	public static final Color Green  = new Color(0x00008000);
	public static final Color Red    = new Color(0x00FF0000);
	public static final Color Maroon = new Color(0x00800000);
	public static final Color Blue   = new Color(0x000000FF);
	public static final Color Olive  = new Color(0x00808000);
	public static final Color Aqua   = new Color(0x0000FFFF);
	public static final Color Teal   = new Color(0x00008080);

	protected byte r,g,b,a;
	
	public Color() {
		this((byte)0, (byte)0, (byte)0, (byte)255);
	}
	public Color(byte r, byte g, byte b, byte a) {
		this.r=r;
		this.g=g;
		this.b=b;
		this.a=a;
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
		if (a==0)
			return String.format("rgb=%02X%02X%02X", r,g,b);
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
}