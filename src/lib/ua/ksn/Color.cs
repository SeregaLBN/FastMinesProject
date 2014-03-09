namespace ua.ksn {

   public struct Color {
      public static readonly Color TRANSPARENT = new Color(0,255,255,255);
      public static readonly Color BLACK   = new Color(0xFF000000);
      public static readonly Color WHITE   = new Color { A = 0xFF, R = 0xFF, G = 0xFF, B = 0xFF };
      public static readonly Color RED     = new Color { A = 0xFF, R = 0xFF };
      public static readonly Color BLUE    = new Color { A = 0xFF, B = 0xFF };
      public static readonly Color GREEN   = new Color { A = 0xFF, G = 0xFF };
      public static readonly Color NAVY    = new Color(0xFF000080);
      public static readonly Color MAROON  = new Color(0xFF800000);
      public static readonly Color OLIVE   = new Color(0xFF808000);
      public static readonly Color AQUA    = new Color(0xFF00FFFF);
      public static readonly Color TEAL    = new Color(0xFF008080);
      public static readonly Color MAGENTA = new Color(255, 0, 255); // FUCHSIA
      public static readonly Color GRAY    = new Color(128, 128, 128);

      public byte R,G,B,A;

      public Color(byte r, byte g, byte b, byte a) { R = r; G = g; B = b; A = a; }
      public Color(byte r, byte g, byte b) { R = r; G = g; B = b; A = 0xFF; }
      public Color(ulong aarrggbb) {
         if (aarrggbb > 0xFFFFFFFF)
            throw new System.ArgumentException("Value incorrect");
         A = (byte)((aarrggbb & 0xFF000000) >> 24);
         R = (byte)((aarrggbb & 0xFF0000) >> 16);
         G = (byte)((aarrggbb & 0xFF00) >> 8);
         B = (byte)(aarrggbb & 0xFF);
      }

      public static bool operator !=(Color c1, Color c2) { return (c1.A != c2.A) || (c1.R != c2.R) || (c1.G != c2.G) || (c1.B != c2.B); }
      public static bool operator ==(Color c1, Color c2) { return (c1.A == c2.A) && (c1.R == c2.R) && (c1.G == c2.G) && (c1.B == c2.B); }

      public override bool Equals(object o) {
         if (ReferenceEquals(null, o))
            return false;
         return (o is Color) && (this == (Color)o);
      }

      public override int GetHashCode() { return (A << 24) | (R << 16) | (G << 8) | B; }

      public override string ToString() {
         return string.Format("argb={0:X2}{1:X2}{2:X2}{3:X2}", A, R, G, B);
      }
   }

   public static class ColorExt {
#if WINDOWS_RT
      public static Color ToFmColor(this Windows.UI.Color self) { return new Color(self.R, self.G, self.B, self.A); }
      public static Windows.UI.Color ToWinColor(this Color self) { return new Windows.UI.Color { A = self.A, B = self.B, G = self.G, R = self.R }; }
#elif WINDOWS_FORMS
      ...
#endif
   }
}