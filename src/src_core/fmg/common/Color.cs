using System;

namespace fmg.common {

   public struct Color {
      #region Colors sorted by HEX Value: http://www.w3schools.com/colors/colors_hex.asp
      public static readonly Color Transparent          = new Color(0, 255,255,255);
      public static readonly Color Black                = new Color(0xFF000000);
      public static readonly Color Navy                 = new Color(0xFF000080);
      public static readonly Color DarkBlue             = new Color(0xFF00008B);
      public static readonly Color MediumBlue           = new Color(0xFF0000CD);
      public static readonly Color Blue                 = new Color(0xFF0000FF);
      public static readonly Color DarkGreen            = new Color(0xFF006400);
      public static readonly Color Green                = new Color(0xFF008000);
      public static readonly Color Teal                 = new Color(0xFF008080);
      public static readonly Color DarkCyan             = new Color(0xFF008B8B);
      public static readonly Color DeepSkyBlue          = new Color(0xFF00BFFF);
      public static readonly Color DarkTurquoise        = new Color(0xFF00CED1);
      public static readonly Color MediumSpringGreen    = new Color(0xFF00FA9A);
      public static readonly Color Lime                 = new Color(0xFF00FF00);
      public static readonly Color SpringGreen          = new Color(0xFF00FF7F);
      public static readonly Color Aqua                 = new Color(0xFF00FFFF);
      public static          Color Cyan                 => Aqua;
      public static readonly Color MidnightBlue         = new Color(0xFF191970);
      public static readonly Color DodgerBlue           = new Color(0xFF1E90FF);
      public static readonly Color LightSeaGreen        = new Color(0xFF20B2AA);
      public static readonly Color ForestGreen          = new Color(0xFF228B22);
      public static readonly Color SeaGreen             = new Color(0xFF2E8B57);
      public static readonly Color DarkSlateGray        = new Color(0xFF2F4F4F);
      public static          Color DarkSlateGrey        => DarkSlateGray;
      public static readonly Color LimeGreen            = new Color(0xFF32CD32);
      public static readonly Color MediumSeaGreen       = new Color(0xFF3CB371);
      public static readonly Color Turquoise            = new Color(0xFF40E0D0);
      public static readonly Color RoyalBlue            = new Color(0xFF4169E1);
      public static readonly Color SteelBlue            = new Color(0xFF4682B4);
      public static readonly Color DarkSlateBlue        = new Color(0xFF483D8B);
      public static readonly Color MediumTurquoise      = new Color(0xFF48D1CC);
      public static readonly Color Indigo               = new Color(0xFF4B0082);
      public static readonly Color DarkOliveGreen       = new Color(0xFF556B2F);
      public static readonly Color CadetBlue            = new Color(0xFF5F9EA0);
      public static readonly Color CornflowerBlue       = new Color(0xFF6495ED);
      public static readonly Color RebeccaPurple        = new Color(0xFF663399);
      public static readonly Color MediumAquaMarine     = new Color(0xFF66CDAA);
      public static readonly Color DimGray              = new Color(0xFF696969);
      public static          Color DimGrey              => DimGray;
      public static readonly Color SlateBlue            = new Color(0xFF6A5ACD);
      public static readonly Color OliveDrab            = new Color(0xFF6B8E23);
      public static readonly Color SlateGray            = new Color(0xFF708090);
      public static          Color SlateGrey            => SlateGray;
      public static readonly Color LightSlateGray       = new Color(0xFF778899);
      public static          Color LightSlateGrey       => LightSlateGray;
      public static readonly Color MediumSlateBlue      = new Color(0xFF7B68EE);
      public static readonly Color LawnGreen            = new Color(0xFF7CFC00);
      public static readonly Color Chartreuse           = new Color(0xFF7FFF00);
      public static readonly Color Aquamarine           = new Color(0xFF7FFFD4);
      public static readonly Color Maroon               = new Color(0xFF800000);
      public static readonly Color Purple               = new Color(0xFF800080);
      public static readonly Color Olive                = new Color(0xFF808000);
      public static readonly Color Gray                 = new Color(0xFF808080);
      public static          Color Grey                 => Gray;
      public static readonly Color SkyBlue              = new Color(0xFF87CEEB);
      public static readonly Color LightSkyBlue         = new Color(0xFF87CEFA);
      public static readonly Color BlueViolet           = new Color(0xFF8A2BE2);
      public static readonly Color DarkRed              = new Color(0xFF8B0000);
      public static readonly Color DarkMagenta          = new Color(0xFF8B008B);
      public static readonly Color SaddleBrown          = new Color(0xFF8B4513);
      public static readonly Color DarkSeaGreen         = new Color(0xFF8FBC8F);
      public static readonly Color LightGreen           = new Color(0xFF90EE90);
      public static readonly Color MediumPurple         = new Color(0xFF9370DB);
      public static readonly Color DarkViolet           = new Color(0xFF9400D3);
      public static readonly Color PaleGreen            = new Color(0xFF98FB98);
      public static readonly Color DarkOrchid           = new Color(0xFF9932CC);
      public static readonly Color YellowGreen          = new Color(0xFF9ACD32);
      public static readonly Color Sienna               = new Color(0xFFA0522D);
      public static readonly Color Brown                = new Color(0xFFA52A2A);
      public static readonly Color DarkGray             = new Color(0xFFA9A9A9);
      public static          Color DarkGrey             => DarkGray;
      public static readonly Color LightBlue            = new Color(0xFFADD8E6);
      public static readonly Color GreenYellow          = new Color(0xFFADFF2F);
      public static readonly Color PaleTurquoise        = new Color(0xFFAFEEEE);
      public static readonly Color LightSteelBlue       = new Color(0xFFB0C4DE);
      public static readonly Color PowderBlue           = new Color(0xFFB0E0E6);
      public static readonly Color FireBrick            = new Color(0xFFB22222);
      public static readonly Color DarkGoldenRod        = new Color(0xFFB8860B);
      public static readonly Color MediumOrchid         = new Color(0xFFBA55D3);
      public static readonly Color RosyBrown            = new Color(0xFFBC8F8F);
      public static readonly Color DarkKhaki            = new Color(0xFFBDB76B);
      public static readonly Color Silver               = new Color(0xFFC0C0C0);
      public static readonly Color MediumVioletRed      = new Color(0xFFC71585);
      public static readonly Color IndianRed            = new Color(0xFFCD5C5C);
      public static readonly Color Peru                 = new Color(0xFFCD853F);
      public static readonly Color Chocolate            = new Color(0xFFD2691E);
      public static readonly Color Tan                  = new Color(0xFFD2B48C);
      public static readonly Color LightGray            = new Color(0xFFD3D3D3);
      public static          Color LightGrey            => LightGray;
      public static readonly Color Thistle              = new Color(0xFFD8BFD8);
      public static readonly Color Orchid               = new Color(0xFFDA70D6);
      public static readonly Color GoldenRod            = new Color(0xFFDAA520);
      public static readonly Color PaleVioletRed        = new Color(0xFFDB7093);
      public static readonly Color Crimson              = new Color(0xFFDC143C);
      public static readonly Color Gainsboro            = new Color(0xFFDCDCDC);
      public static readonly Color Plum                 = new Color(0xFFDDA0DD);
      public static readonly Color BurlyWood            = new Color(0xFFDEB887);
      public static readonly Color LightCyan            = new Color(0xFFE0FFFF);
      public static readonly Color Lavender             = new Color(0xFFE6E6FA);
      public static readonly Color DarkSalmon           = new Color(0xFFE9967A);
      public static readonly Color Violet               = new Color(0xFFEE82EE);
      public static readonly Color PaleGoldenRod        = new Color(0xFFEEE8AA);
      public static readonly Color LightCoral           = new Color(0xFFF08080);
      public static readonly Color Khaki                = new Color(0xFFF0E68C);
      public static readonly Color AliceBlue            = new Color(0xFFF0F8FF);
      public static readonly Color HoneyDew             = new Color(0xFFF0FFF0);
      public static readonly Color Azure                = new Color(0xFFF0FFFF);
      public static readonly Color SandyBrown           = new Color(0xFFF4A460);
      public static readonly Color Wheat                = new Color(0xFFF5DEB3);
      public static readonly Color Beige                = new Color(0xFFF5F5DC);
      public static readonly Color WhiteSmoke           = new Color(0xFFF5F5F5);
      public static readonly Color MintCream            = new Color(0xFFF5FFFA);
      public static readonly Color GhostWhite           = new Color(0xFFF8F8FF);
      public static readonly Color Salmon               = new Color(0xFFFA8072);
      public static readonly Color AntiqueWhite         = new Color(0xFFFAEBD7);
      public static readonly Color Linen                = new Color(0xFFFAF0E6);
      public static readonly Color LightGoldenRodYellow = new Color(0xFFFAFAD2);
      public static readonly Color OldLace              = new Color(0xFFFDF5E6);
      public static readonly Color Red                  = new Color(0xFFFF0000);
      public static readonly Color Fuchsia              = new Color(0xFFFF00FF);
      public static          Color Magenta              => Fuchsia;
      public static readonly Color DeepPink             = new Color(0xFFFF1493);
      public static readonly Color OrangeRed            = new Color(0xFFFF4500);
      public static readonly Color Tomato               = new Color(0xFFFF6347);
      public static readonly Color HotPink              = new Color(0xFFFF69B4);
      public static readonly Color Coral                = new Color(0xFFFF7F50);
      public static readonly Color DarkOrange           = new Color(0xFFFF8C00);
      public static readonly Color LightSalmon          = new Color(0xFFFFA07A);
      public static readonly Color Orange               = new Color(0xFFFFA500);
      public static readonly Color LightPink            = new Color(0xFFFFB6C1);
      public static readonly Color Pink                 = new Color(0xFFFFC0CB);
      public static readonly Color Gold                 = new Color(0xFFFFD700);
      public static readonly Color PeachPuff            = new Color(0xFFFFDAB9);
      public static readonly Color NavajoWhite          = new Color(0xFFFFDEAD);
      public static readonly Color Moccasin             = new Color(0xFFFFE4B5);
      public static readonly Color Bisque               = new Color(0xFFFFE4C4);
      public static readonly Color MistyRose            = new Color(0xFFFFE4E1);
      public static readonly Color BlanchedAlmond       = new Color(0xFFFFEBCD);
      public static readonly Color PapayaWhip           = new Color(0xFFFFEFD5);
      public static readonly Color LavenderBlush        = new Color(0xFFFFF0F5);
      public static readonly Color SeaShell             = new Color(0xFFFFF5EE);
      public static readonly Color Cornsilk             = new Color(0xFFFFF8DC);
      public static readonly Color LemonChiffon         = new Color(0xFFFFFACD);
      public static readonly Color FloralWhite          = new Color(0xFFFFFAF0);
      public static readonly Color Snow                 = new Color(0xFFFFFAFA);
      public static readonly Color Yellow               = new Color(0xFFFFFF00);
      public static readonly Color LightYellow          = new Color(0xFFFFFFE0);
      public static readonly Color Ivory                = new Color(0xFFFFFFF0);
      public static readonly Color White                = new Color(0xFFFFFFFF);
      #endregion

      public byte R,G,B,A;

      public Color(Color copy)
         : this(copy.A, copy.R, copy.G, copy.B)
      { }
      public Color(byte a, byte r, byte g, byte b) { R = r; G = g; B = b; A = a; }
      public Color(byte r, byte g, byte b) { R = r; G = g; B = b; A = 0xFF; }
      public Color(ulong aarrggbb) {
         if (aarrggbb > 0xFFFFFFFF)
            throw new ArgumentException("Value incorrect");
         A = (byte)((aarrggbb & 0xFF000000) >> 24);
         R = (byte)((aarrggbb & 0xFF0000) >> 16);
         G = (byte)((aarrggbb & 0xFF00) >> 8);
         B = (byte)(aarrggbb & 0xFF);
      }

      public bool IsOpaque      => A == 255;
      public bool IsTransparent => A == 0;

      public static bool operator !=(Color c1, Color c2) { return (c1.A != c2.A) || (c1.R != c2.R) || (c1.G != c2.G) || (c1.B != c2.B); }
      public static bool operator ==(Color c1, Color c2) { return (c1.A == c2.A) && (c1.R == c2.R) && (c1.G == c2.G) && (c1.B == c2.B); }

      public override bool Equals(object o) {
         if (ReferenceEquals(null, o))
            return false;
         return (o is Color) && (this == (Color)o);
      }

      public override int GetHashCode() { return (A << 24) | (R << 16) | (G << 8) | B; }

      public override string ToString() {
         return $"argb={A:X2}{R:X2}{G:X2}{B:X2}";
      }

      public static Color RandomColor() {
         var next = ThreadLocalRandom.Current.Next();
         return new Color {
            R = (byte) ((next & 0xFF) >> 0),
            G = (byte) ((next & 0xFF00) >> 8),
            B = (byte) ((next & 0xFF0000) >> 16),
            A = 255};
      }

   }

   public static class ColorExt {

      /// <summary> Creates grayscale version of this Color </summary>
      public static Color Grayscale(this Color clr) { return new Color(clr.A, (byte)(clr.R * 0.2126), (byte)(clr.G * 0.7152), (byte)(clr.B * 0.0722)); }

      /// <summary> Creates brighter version of this Color </summary>
      /// <param name="clr">from</param>
      /// <param name="percent">0.0 - as is; 1 - WHITE</param>
      /// <returns></returns>
      public static Color Brighter(this Color clr, double percent = 0.7) {
         var tmp = new Color(clr.A, (byte)(0xFF - clr.R), (byte)(0xFF - clr.G), (byte)(0xFF - clr.B));
         tmp = tmp.Darker(percent);
         return new Color(tmp.A, (byte)(0xFF - tmp.R), (byte)(0xFF - tmp.G), (byte)(0xFF - tmp.B));
      }

      /// <summary> Creates darker version of this Color </summary>
      /// <param name="clr">from</param>
      /// <param name="percent">0.0 - as is; 1 - BLACK</param>
      /// <returns></returns>
      public static Color Darker(this Color clr, double percent = 0.7) {
         var tmp = 1 - Math.Min(1, Math.Max(0, percent));
         return new Color(clr.A,
            (byte)(clr.R*tmp),  // (byte)Math.Min(clr.R*tmp, byte.MaxValue),
            (byte)(clr.G*tmp),  // (byte)Math.Min(clr.G*tmp, byte.MaxValue),
            (byte)(clr.B*tmp)); // (byte)Math.Min(clr.B*tmp, byte.MaxValue),
      }

   }

}
