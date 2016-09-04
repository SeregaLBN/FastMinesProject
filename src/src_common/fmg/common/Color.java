package fmg.common;

import java.util.Random;

/** RGBA color */
public class Color {
   // region Colors sorted by HEX Value: http://www.w3schools.com/colors/colors_hex.asp
   public static final Color Transparent          = new Color(0, 255,255,255);
   public static final Color Black                = new Color(0xFF000000);
   public static final Color Navy                 = new Color(0xFF000080);
   public static final Color DarkBlue             = new Color(0xFF00008B);
   public static final Color MediumBlue           = new Color(0xFF0000CD);
   public static final Color Blue                 = new Color(0xFF0000FF);
   public static final Color DarkGreen            = new Color(0xFF006400);
   public static final Color Green                = new Color(0xFF008000);
   public static final Color Teal                 = new Color(0xFF008080);
   public static final Color DarkCyan             = new Color(0xFF008B8B);
   public static final Color DeepSkyBlue          = new Color(0xFF00BFFF);
   public static final Color DarkTurquoise        = new Color(0xFF00CED1);
   public static final Color MediumSpringGreen    = new Color(0xFF00FA9A);
   public static final Color Lime                 = new Color(0xFF00FF00);
   public static final Color SpringGreen          = new Color(0xFF00FF7F);
   public static final Color Aqua                 = new Color(0xFF00FFFF);
   public static final Color Cyan                 = Aqua;
   public static final Color MidnightBlue         = new Color(0xFF191970);
   public static final Color DodgerBlue           = new Color(0xFF1E90FF);
   public static final Color LightSeaGreen        = new Color(0xFF20B2AA);
   public static final Color ForestGreen          = new Color(0xFF228B22);
   public static final Color SeaGreen             = new Color(0xFF2E8B57);
   public static final Color DarkSlateGray        = new Color(0xFF2F4F4F);
   public static final Color DarkSlateGrey        = DarkSlateGray;
   public static final Color LimeGreen            = new Color(0xFF32CD32);
   public static final Color MediumSeaGreen       = new Color(0xFF3CB371);
   public static final Color Turquoise            = new Color(0xFF40E0D0);
   public static final Color RoyalBlue            = new Color(0xFF4169E1);
   public static final Color SteelBlue            = new Color(0xFF4682B4);
   public static final Color DarkSlateBlue        = new Color(0xFF483D8B);
   public static final Color MediumTurquoise      = new Color(0xFF48D1CC);
   public static final Color Indigo               = new Color(0xFF4B0082);
   public static final Color DarkOliveGreen       = new Color(0xFF556B2F);
   public static final Color CadetBlue            = new Color(0xFF5F9EA0);
   public static final Color CornflowerBlue       = new Color(0xFF6495ED);
   public static final Color RebeccaPurple        = new Color(0xFF663399);
   public static final Color MediumAquaMarine     = new Color(0xFF66CDAA);
   public static final Color DimGray              = new Color(0xFF696969);
   public static final Color DimGrey              = DimGray;
   public static final Color SlateBlue            = new Color(0xFF6A5ACD);
   public static final Color OliveDrab            = new Color(0xFF6B8E23);
   public static final Color SlateGray            = new Color(0xFF708090);
   public static final Color SlateGrey            = SlateGray;
   public static final Color LightSlateGray       = new Color(0xFF778899);
   public static final Color LightSlateGrey       = LightSlateGray;
   public static final Color MediumSlateBlue      = new Color(0xFF7B68EE);
   public static final Color LawnGreen            = new Color(0xFF7CFC00);
   public static final Color Chartreuse           = new Color(0xFF7FFF00);
   public static final Color Aquamarine           = new Color(0xFF7FFFD4);
   public static final Color Maroon               = new Color(0xFF800000);
   public static final Color Purple               = new Color(0xFF800080);
   public static final Color Olive                = new Color(0xFF808000);
   public static final Color Gray                 = new Color(0xFF808080);
   public static final Color Grey                 = Gray;
   public static final Color SkyBlue              = new Color(0xFF87CEEB);
   public static final Color LightSkyBlue         = new Color(0xFF87CEFA);
   public static final Color BlueViolet           = new Color(0xFF8A2BE2);
   public static final Color DarkRed              = new Color(0xFF8B0000);
   public static final Color DarkMagenta          = new Color(0xFF8B008B);
   public static final Color SaddleBrown          = new Color(0xFF8B4513);
   public static final Color DarkSeaGreen         = new Color(0xFF8FBC8F);
   public static final Color LightGreen           = new Color(0xFF90EE90);
   public static final Color MediumPurple         = new Color(0xFF9370DB);
   public static final Color DarkViolet           = new Color(0xFF9400D3);
   public static final Color PaleGreen            = new Color(0xFF98FB98);
   public static final Color DarkOrchid           = new Color(0xFF9932CC);
   public static final Color YellowGreen          = new Color(0xFF9ACD32);
   public static final Color Sienna               = new Color(0xFFA0522D);
   public static final Color Brown                = new Color(0xFFA52A2A);
   public static final Color DarkGray             = new Color(0xFFA9A9A9);
   public static final Color DarkGrey             = DarkGray;
   public static final Color LightBlue            = new Color(0xFFADD8E6);
   public static final Color GreenYellow          = new Color(0xFFADFF2F);
   public static final Color PaleTurquoise        = new Color(0xFFAFEEEE);
   public static final Color LightSteelBlue       = new Color(0xFFB0C4DE);
   public static final Color PowderBlue           = new Color(0xFFB0E0E6);
   public static final Color FireBrick            = new Color(0xFFB22222);
   public static final Color DarkGoldenRod        = new Color(0xFFB8860B);
   public static final Color MediumOrchid         = new Color(0xFFBA55D3);
   public static final Color RosyBrown            = new Color(0xFFBC8F8F);
   public static final Color DarkKhaki            = new Color(0xFFBDB76B);
   public static final Color Silver               = new Color(0xFFC0C0C0);
   public static final Color MediumVioletRed      = new Color(0xFFC71585);
   public static final Color IndianRed            = new Color(0xFFCD5C5C);
   public static final Color Peru                 = new Color(0xFFCD853F);
   public static final Color Chocolate            = new Color(0xFFD2691E);
   public static final Color Tan                  = new Color(0xFFD2B48C);
   public static final Color LightGray            = new Color(0xFFD3D3D3);
   public static final Color LightGrey            = LightGray;
   public static final Color Thistle              = new Color(0xFFD8BFD8);
   public static final Color Orchid               = new Color(0xFFDA70D6);
   public static final Color GoldenRod            = new Color(0xFFDAA520);
   public static final Color PaleVioletRed        = new Color(0xFFDB7093);
   public static final Color Crimson              = new Color(0xFFDC143C);
   public static final Color Gainsboro            = new Color(0xFFDCDCDC);
   public static final Color Plum                 = new Color(0xFFDDA0DD);
   public static final Color BurlyWood            = new Color(0xFFDEB887);
   public static final Color LightCyan            = new Color(0xFFE0FFFF);
   public static final Color Lavender             = new Color(0xFFE6E6FA);
   public static final Color DarkSalmon           = new Color(0xFFE9967A);
   public static final Color Violet               = new Color(0xFFEE82EE);
   public static final Color PaleGoldenRod        = new Color(0xFFEEE8AA);
   public static final Color LightCoral           = new Color(0xFFF08080);
   public static final Color Khaki                = new Color(0xFFF0E68C);
   public static final Color AliceBlue            = new Color(0xFFF0F8FF);
   public static final Color HoneyDew             = new Color(0xFFF0FFF0);
   public static final Color Azure                = new Color(0xFFF0FFFF);
   public static final Color SandyBrown           = new Color(0xFFF4A460);
   public static final Color Wheat                = new Color(0xFFF5DEB3);
   public static final Color Beige                = new Color(0xFFF5F5DC);
   public static final Color WhiteSmoke           = new Color(0xFFF5F5F5);
   public static final Color MintCream            = new Color(0xFFF5FFFA);
   public static final Color GhostWhite           = new Color(0xFFF8F8FF);
   public static final Color Salmon               = new Color(0xFFFA8072);
   public static final Color AntiqueWhite         = new Color(0xFFFAEBD7);
   public static final Color Linen                = new Color(0xFFFAF0E6);
   public static final Color LightGoldenRodYellow = new Color(0xFFFAFAD2);
   public static final Color OldLace              = new Color(0xFFFDF5E6);
   public static final Color Red                  = new Color(0xFFFF0000);
   public static final Color Fuchsia              = new Color(0xFFFF00FF);
   public static final Color Magenta              = Fuchsia;
   public static final Color DeepPink             = new Color(0xFFFF1493);
   public static final Color OrangeRed            = new Color(0xFFFF4500);
   public static final Color Tomato               = new Color(0xFFFF6347);
   public static final Color HotPink              = new Color(0xFFFF69B4);
   public static final Color Coral                = new Color(0xFFFF7F50);
   public static final Color DarkOrange           = new Color(0xFFFF8C00);
   public static final Color LightSalmon          = new Color(0xFFFFA07A);
   public static final Color Orange               = new Color(0xFFFFA500);
   public static final Color LightPink            = new Color(0xFFFFB6C1);
   public static final Color Pink                 = new Color(0xFFFFC0CB);
   public static final Color Gold                 = new Color(0xFFFFD700);
   public static final Color PeachPuff            = new Color(0xFFFFDAB9);
   public static final Color NavajoWhite          = new Color(0xFFFFDEAD);
   public static final Color Moccasin             = new Color(0xFFFFE4B5);
   public static final Color Bisque               = new Color(0xFFFFE4C4);
   public static final Color MistyRose            = new Color(0xFFFFE4E1);
   public static final Color BlanchedAlmond       = new Color(0xFFFFEBCD);
   public static final Color PapayaWhip           = new Color(0xFFFFEFD5);
   public static final Color LavenderBlush        = new Color(0xFFFFF0F5);
   public static final Color SeaShell             = new Color(0xFFFFF5EE);
   public static final Color Cornsilk             = new Color(0xFFFFF8DC);
   public static final Color LemonChiffon         = new Color(0xFFFFFACD);
   public static final Color FloralWhite          = new Color(0xFFFFFAF0);
   public static final Color Snow                 = new Color(0xFFFFFAFA);
   public static final Color Yellow               = new Color(0xFFFFFF00);
   public static final Color LightYellow          = new Color(0xFFFFFFE0);
   public static final Color Ivory                = new Color(0xFFFFFFF0);
   public static final Color White                = new Color(0xFFFFFFFF);
   // endregion

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
