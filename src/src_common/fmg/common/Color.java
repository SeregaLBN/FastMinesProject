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
   static int checkA(int a) { return check(a, "ALPHA"); }
   static int checkR(int r) { return check(r, "RED"); }
   static int checkG(int g) { return check(g, "GREEN"); }
   static int checkB(int b) { return check(b, "BLUE"); }

   public Color(int a, int r, int g, int b) {
      this.a = checkA(a);
      this.r = checkR(r);
      this.g = checkG(g);
      this.b = checkB(b);
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
      return String.format("argb[%02X%02X%02X%02X]", a,r,g,b);
   }

   @Override
   public Color clone() { return new Color(a,r,g,b); }

   /** get RED chanel */
   public int getR() { return r; }
   /** set RED chanel */
   public void setR(int r) { this.r = checkR(r); }
   /** get GREEN chanel */
   public int getG() { return g; }
   /** set GREEN chanel */
   public void setG(int g) { this.g = checkG(g); }
   /** get BLUE chanel */
   public int getB() { return b; }
   /** set BLUE chanel */
   public void setB(int b) { this.b = checkB(b); }
   /** get ALPHA chanel */
   public int getA() { return a; }
   /** set ALPHA chanel */
   public void setA(int a) { this.a = checkA(a); }

   public static Color RandomColor(Random rnd) {
      return new Color(
         rnd.nextInt(256),
         rnd.nextInt(256),
         rnd.nextInt(256));
   }

   /** Creates grayscale version of this Color */
   public Color grayscale() { return new Color(a, (int)(r * 0.2126), (int)(g * 0.7152), (int)(b * 0.0722)); }

   /**
    * Creates brighter version of this Color
    * @param percent - 0.0 - as is; 1 - WHITE
    * @return
    */
   public Color brighter(double percent) {
      if ((percent < 0) || (percent > 1))
         throw new IllegalArgumentException("Bad 'percent' argument");
      Color tmp = new Color(a, 0xFF - r, 0xFF - g, 0xFF - b);
      tmp = tmp.darker(percent);
      return new Color(tmp.a, 0xFF - tmp.r, 0xFF - tmp.g, 0xFF - tmp.b);
      //HSV hsv = new HSV(this);
      //hsv.s *= 1 - percent;
      //hsv.v = 100 - hsv.v * ( 1 - percent);
      //return hsv.toColor();
   }
   public Color brighter() { return this.brighter(0.7); }

   /**
    * Creates darker version of this Color
    * @param percent - 0.0 - as is; 1 - BLACK
    * @return
    */
   public Color darker(double percent) {
      if ((percent < 0) || (percent > 1))
         throw new IllegalArgumentException("Bad 'percent' argument");
      double tmp = 1 - Math.min(1.0, Math.max(0, percent));
      return new Color(a,
         (int)(r * tmp),
         (int)(g * tmp),
         (int)(b * tmp));
      //HSV hsv = new HSV(this);
      //hsv.v *= 1 - percent;
      //return hsv.toColor();
   }
   public Color darker() { return darker(0.7); }

/*
   // test
   public static void main(String[] args) {
      Random rnd = new Random(java.util.UUID.randomUUID().hashCode());

      try {
         Color clr = Color.RandomColor(rnd);
         System.out.println("original: " + new HSV(clr));
         for (int i=0; i<=10; ++i) {
            double prcnt = 0.1 * i;
            Color darker = clr.darker(prcnt);
            Color brighter = clr.brighter(prcnt);
            HSV hsvD = new HSV(darker);
            HSV hsvB = new HSV(brighter);
            System.out.println(hsvD + "  <-D  " + String.format("%.2f", prcnt) + "  B->  " + hsvB);
         }
      } catch(Exception ex) {
         ex.printStackTrace(System.err);
      }

      try {
         Color clr = Color.RandomColor(rnd);

         Color black = clr.darker(1);
         System.out.println("Test darker max - black("+black+").equals(Black) is " + black.equals(Black));
         Color copy1 = clr.darker(0);
         System.out.println("Test darker min - copy("+copy1+").equals(original("+clr+")) is " + copy1.equals(clr));

         Color white = clr.brighter(1);
         System.out.println("Test brighter max - white("+white+").equals(White) is " + white.equals(White));
         Color copy2 = clr.brighter(0);
         System.out.println("Test brighter min - copy("+copy2+").equals(original("+clr+")) is " + copy2.equals(clr));
      } catch(Exception ex) {
         ex.printStackTrace(System.err);
      }
   }
*/

}
