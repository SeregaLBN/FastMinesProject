package fmg.common;

import java.util.Random;

/** RGBA color */
public class Color {
   public static final Color Transparent = new Color(0, 255,255,255);
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

   protected int r,g,b,a;

   private static int check(int v, String name) {
      if (v<0 || v>255) throw new IllegalArgumentException("Bad "+name+" argument");
      return v;
   }
   public Color(int a, int r, int g, int b) {
      this.a = check(a, "ALPHA");
      this.r = check(r, "RED");
      this.g = check(g, "GREEN");
      this.b = check(b, "BLUE");
   }
   public Color(int r, int g, int b) {
      this(255, r, g, b);
   }
   public Color(int OxAARRGGBB) {
      this((OxAARRGGBB >> 24) & 0xFF,
           (OxAARRGGBB >> 16) & 0xFF,
           (OxAARRGGBB >>  8) & 0xFF,
           (OxAARRGGBB >>  0) & 0xFF);
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
      return (clr.a==a) && (clr.r==r) && (clr.g==g) && (clr.b==b);
   }
   @Override
   public String toString() {
      return String.format("argb=%02X%02X%02X%02X", a,r,g,b);
   }

   @Override
   public Color clone() { return new Color(a,r,g,b); }

   /** get RED chanel */
   public int getR() { return r; }
   /** set RED chanel */
   public void setR(int r) { this.r = check(r, "RED"); }
   /** get GREEN chanel */
   public int getG() { return g; }
   /** set GREEN chanel */
   public void setG(int g) { this.g = check(g, "GREEN"); }
   /** get BLUE chanel */
   public int getB() { return b; }
   /** set BLUE chanel */
   public void setB(int b) { this.b = check(b, "BLUE"); }
   /** get ALPHA chanel */
   public int getA() { return a; }
   /** set ALPHA chanel */
   public void setA(int a) { this.a = check(a, "ALPHA"); }

   public static Color RandomColor(Random rnd) {
      return new Color(
         rnd.nextInt(256),
         rnd.nextInt(256),
         rnd.nextInt(256));
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
      return new Color(a,
         basic + r % (0xFF - basic),
         basic + g % (0xFF - basic),
         basic + b % (0xFF - basic));
   }
   public Color attenuate() { return this.attenuate(120); }

   /**
    * Creates brighter version of this Color
    * @param percent - 0.0 - as is; 1 - WHITE
    * @return
    */
   public Color brighter(double percent) {
      Color tmp = new Color(a, 0xFF - r, 0xFF - g, 0xFF - b);
      tmp = tmp.darker(percent);
      return new Color(tmp.a, 0xFF - tmp.r, 0xFF - tmp.g, 0xFF - tmp.b);
   }
   public Color brighter() { return this.brighter(0.7); }

   /**
    * Creates darker version of this Color
    * @param percent - 0.0 - as is; 1 - BLACK
    * @return
    */
   public Color darker(double percent) {
      double tmp = 1 - Math.min(1.0, Math.max(0, percent));
      return new Color(a,
         (int)(r * tmp),
         (int)(g * tmp),
         (int)(b * tmp));
   }
   public Color darker() { return darker(0.7); }
}