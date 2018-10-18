package fmg.common;

import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

/** RGBA color */
public class Color {

    /** {@link #White()} with zero Alpha chanel */
    public static Color Transparent         () { return new Color(0, 255,255,255); }

    // region Colors sorted by HEX Value: http://www.w3schools.com/colors/colors_hex.asp
    public static Color Black               () { return new Color(0xFF000000); }
    public static Color Navy                () { return new Color(0xFF000080); }
    public static Color DarkBlue            () { return new Color(0xFF00008B); }
    public static Color MediumBlue          () { return new Color(0xFF0000CD); }
    public static Color Blue                () { return new Color(0xFF0000FF); }
    public static Color DarkGreen           () { return new Color(0xFF006400); }
    public static Color Green               () { return new Color(0xFF008000); }
    public static Color Teal                () { return new Color(0xFF008080); }
    public static Color DarkCyan            () { return new Color(0xFF008B8B); }
    public static Color DeepSkyBlue         () { return new Color(0xFF00BFFF); }
    public static Color DarkTurquoise       () { return new Color(0xFF00CED1); }
    public static Color MediumSpringGreen   () { return new Color(0xFF00FA9A); }
    public static Color Lime                () { return new Color(0xFF00FF00); }
    public static Color SpringGreen         () { return new Color(0xFF00FF7F); }
    public static Color Aqua                () { return new Color(0xFF00FFFF); }
    public static Color Cyan                () { return Aqua();                }
    public static Color MidnightBlue        () { return new Color(0xFF191970); }
    public static Color DodgerBlue          () { return new Color(0xFF1E90FF); }
    public static Color LightSeaGreen       () { return new Color(0xFF20B2AA); }
    public static Color ForestGreen         () { return new Color(0xFF228B22); }
    public static Color SeaGreen            () { return new Color(0xFF2E8B57); }
    public static Color DarkSlateGray       () { return new Color(0xFF2F4F4F); }
    public static Color DarkSlateGrey       () { return DarkSlateGray();       }
    public static Color LimeGreen           () { return new Color(0xFF32CD32); }
    public static Color MediumSeaGreen      () { return new Color(0xFF3CB371); }
    public static Color Turquoise           () { return new Color(0xFF40E0D0); }
    public static Color RoyalBlue           () { return new Color(0xFF4169E1); }
    public static Color SteelBlue           () { return new Color(0xFF4682B4); }
    public static Color DarkSlateBlue       () { return new Color(0xFF483D8B); }
    public static Color MediumTurquoise     () { return new Color(0xFF48D1CC); }
    public static Color Indigo              () { return new Color(0xFF4B0082); }
    public static Color DarkOliveGreen      () { return new Color(0xFF556B2F); }
    public static Color CadetBlue           () { return new Color(0xFF5F9EA0); }
    public static Color CornflowerBlue      () { return new Color(0xFF6495ED); }
    public static Color RebeccaPurple       () { return new Color(0xFF663399); }
    public static Color MediumAquaMarine    () { return new Color(0xFF66CDAA); }
    public static Color DimGray             () { return new Color(0xFF696969); }
    public static Color DimGrey             () { return DimGray();             }
    public static Color SlateBlue           () { return new Color(0xFF6A5ACD); }
    public static Color OliveDrab           () { return new Color(0xFF6B8E23); }
    public static Color SlateGray           () { return new Color(0xFF708090); }
    public static Color SlateGrey           () { return SlateGray();           }
    public static Color LightSlateGray      () { return new Color(0xFF778899); }
    public static Color LightSlateGrey      () { return LightSlateGray();      }
    public static Color MediumSlateBlue     () { return new Color(0xFF7B68EE); }
    public static Color LawnGreen           () { return new Color(0xFF7CFC00); }
    public static Color Chartreuse          () { return new Color(0xFF7FFF00); }
    public static Color Aquamarine          () { return new Color(0xFF7FFFD4); }
    public static Color Maroon              () { return new Color(0xFF800000); }
    public static Color Purple              () { return new Color(0xFF800080); }
    public static Color Olive               () { return new Color(0xFF808000); }
    public static Color Gray                () { return new Color(0xFF808080); }
    public static Color Grey                () { return Gray();                }
    public static Color SkyBlue             () { return new Color(0xFF87CEEB); }
    public static Color LightSkyBlue        () { return new Color(0xFF87CEFA); }
    public static Color BlueViolet          () { return new Color(0xFF8A2BE2); }
    public static Color DarkRed             () { return new Color(0xFF8B0000); }
    public static Color DarkMagenta         () { return new Color(0xFF8B008B); }
    public static Color SaddleBrown         () { return new Color(0xFF8B4513); }
    public static Color DarkSeaGreen        () { return new Color(0xFF8FBC8F); }
    public static Color LightGreen          () { return new Color(0xFF90EE90); }
    public static Color MediumPurple        () { return new Color(0xFF9370DB); }
    public static Color DarkViolet          () { return new Color(0xFF9400D3); }
    public static Color PaleGreen           () { return new Color(0xFF98FB98); }
    public static Color DarkOrchid          () { return new Color(0xFF9932CC); }
    public static Color YellowGreen         () { return new Color(0xFF9ACD32); }
    public static Color Sienna              () { return new Color(0xFFA0522D); }
    public static Color Brown               () { return new Color(0xFFA52A2A); }
    public static Color DarkGray            () { return new Color(0xFFA9A9A9); }
    public static Color DarkGrey            () { return DarkGray();            }
    public static Color LightBlue           () { return new Color(0xFFADD8E6); }
    public static Color GreenYellow         () { return new Color(0xFFADFF2F); }
    public static Color PaleTurquoise       () { return new Color(0xFFAFEEEE); }
    public static Color LightSteelBlue      () { return new Color(0xFFB0C4DE); }
    public static Color PowderBlue          () { return new Color(0xFFB0E0E6); }
    public static Color FireBrick           () { return new Color(0xFFB22222); }
    public static Color DarkGoldenRod       () { return new Color(0xFFB8860B); }
    public static Color MediumOrchid        () { return new Color(0xFFBA55D3); }
    public static Color RosyBrown           () { return new Color(0xFFBC8F8F); }
    public static Color DarkKhaki           () { return new Color(0xFFBDB76B); }
    public static Color Silver              () { return new Color(0xFFC0C0C0); }
    public static Color MediumVioletRed     () { return new Color(0xFFC71585); }
    public static Color IndianRed           () { return new Color(0xFFCD5C5C); }
    public static Color Peru                () { return new Color(0xFFCD853F); }
    public static Color Chocolate           () { return new Color(0xFFD2691E); }
    public static Color Tan                 () { return new Color(0xFFD2B48C); }
    public static Color LightGray           () { return new Color(0xFFD3D3D3); }
    public static Color LightGrey           () { return LightGray();           }
    public static Color Thistle             () { return new Color(0xFFD8BFD8); }
    public static Color Orchid              () { return new Color(0xFFDA70D6); }
    public static Color GoldenRod           () { return new Color(0xFFDAA520); }
    public static Color PaleVioletRed       () { return new Color(0xFFDB7093); }
    public static Color Crimson             () { return new Color(0xFFDC143C); }
    public static Color Gainsboro           () { return new Color(0xFFDCDCDC); }
    public static Color Plum                () { return new Color(0xFFDDA0DD); }
    public static Color BurlyWood           () { return new Color(0xFFDEB887); }
    public static Color LightCyan           () { return new Color(0xFFE0FFFF); }
    public static Color Lavender            () { return new Color(0xFFE6E6FA); }
    public static Color DarkSalmon          () { return new Color(0xFFE9967A); }
    public static Color Violet              () { return new Color(0xFFEE82EE); }
    public static Color PaleGoldenRod       () { return new Color(0xFFEEE8AA); }
    public static Color LightCoral          () { return new Color(0xFFF08080); }
    public static Color Khaki               () { return new Color(0xFFF0E68C); }
    public static Color AliceBlue           () { return new Color(0xFFF0F8FF); }
    public static Color HoneyDew            () { return new Color(0xFFF0FFF0); }
    public static Color Azure               () { return new Color(0xFFF0FFFF); }
    public static Color SandyBrown          () { return new Color(0xFFF4A460); }
    public static Color Wheat               () { return new Color(0xFFF5DEB3); }
    public static Color Beige               () { return new Color(0xFFF5F5DC); }
    public static Color WhiteSmoke          () { return new Color(0xFFF5F5F5); }
    public static Color MintCream           () { return new Color(0xFFF5FFFA); }
    public static Color GhostWhite          () { return new Color(0xFFF8F8FF); }
    public static Color Salmon              () { return new Color(0xFFFA8072); }
    public static Color AntiqueWhite        () { return new Color(0xFFFAEBD7); }
    public static Color Linen               () { return new Color(0xFFFAF0E6); }
    public static Color LightGoldenRodYellow() { return new Color(0xFFFAFAD2); }
    public static Color OldLace             () { return new Color(0xFFFDF5E6); }
    public static Color Red                 () { return new Color(0xFFFF0000); }
    public static Color Fuchsia             () { return new Color(0xFFFF00FF); }
    public static Color Magenta             () { return Fuchsia();             }
    public static Color DeepPink            () { return new Color(0xFFFF1493); }
    public static Color OrangeRed           () { return new Color(0xFFFF4500); }
    public static Color Tomato              () { return new Color(0xFFFF6347); }
    public static Color HotPink             () { return new Color(0xFFFF69B4); }
    public static Color Coral               () { return new Color(0xFFFF7F50); }
    public static Color DarkOrange          () { return new Color(0xFFFF8C00); }
    public static Color LightSalmon         () { return new Color(0xFFFFA07A); }
    public static Color Orange              () { return new Color(0xFFFFA500); }
    public static Color LightPink           () { return new Color(0xFFFFB6C1); }
    public static Color Pink                () { return new Color(0xFFFFC0CB); }
    public static Color Gold                () { return new Color(0xFFFFD700); }
    public static Color PeachPuff           () { return new Color(0xFFFFDAB9); }
    public static Color NavajoWhite         () { return new Color(0xFFFFDEAD); }
    public static Color Moccasin            () { return new Color(0xFFFFE4B5); }
    public static Color Bisque              () { return new Color(0xFFFFE4C4); }
    public static Color MistyRose           () { return new Color(0xFFFFE4E1); }
    public static Color BlanchedAlmond      () { return new Color(0xFFFFEBCD); }
    public static Color PapayaWhip          () { return new Color(0xFFFFEFD5); }
    public static Color LavenderBlush       () { return new Color(0xFFFFF0F5); }
    public static Color SeaShell            () { return new Color(0xFFFFF5EE); }
    public static Color Cornsilk            () { return new Color(0xFFFFF8DC); }
    public static Color LemonChiffon        () { return new Color(0xFFFFFACD); }
    public static Color FloralWhite         () { return new Color(0xFFFFFAF0); }
    public static Color Snow                () { return new Color(0xFFFFFAFA); }
    public static Color Yellow              () { return new Color(0xFFFFFF00); }
    public static Color LightYellow         () { return new Color(0xFFFFFFE0); }
    public static Color Ivory               () { return new Color(0xFFFFFFF0); }
    public static Color White               () { return new Color(0xFFFFFFFF); }
    // endregion

    private final int r;
    private final int g;
    private final int b;
    private final int a;

    private static int check(int v, String name) {
        if (v<0 || v>255) throw new IllegalArgumentException("Bad "+name+" argument");
        return v;
    }
    static int checkA(int a) { return check(a, "ALPHA"); }
    static int checkR(int r) { return check(r, "RED"); }
    static int checkG(int g) { return check(g, "GREEN"); }
    static int checkB(int b) { return check(b, "BLUE"); }

    public Color(Color copy) {
        this(copy.a, copy.r, copy.g, copy.b);
    }
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
    /** update RED chanel. Returned new Color */
    public Color updateR(int r) { return new Color(this.a, r, this.g, this.b); }
    /** get GREEN chanel */
    public int getG() { return g; }
    /** update GREEN chanel. Returned new Color */
    public Color updateG(int g) { return new Color(this.a, this.r, g, this.b); }
    /** get BLUE chanel */
    public int getB() { return b; }
    /** update BLUE chanel. Returned new Color */
    public Color updateB(int b) { return new Color(this.a, this.r, this.g, b); }
    /** get ALPHA chanel */
    public int getA() { return a; }
    /** update ALPHA chanel. Returned new Color */
    public Color updateA(int a) { return new Color(a, this.r, this.g, this.b); }

    public boolean isOpaque()      { return this.a == 255; }
    public boolean isTransparent() { return this.a == 0; }

    public static Color RandomColor() {
        Random rnd = ThreadLocalRandom.current();
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

}
