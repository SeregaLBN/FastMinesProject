using System;

namespace fmg.common {

   public struct Color {
      public static readonly Color Transparent = new Color(0,255,255,255);
      public static readonly Color Black   = new Color(0xFF000000);
      public static readonly Color White   = new Color(0xFFFFFFFF);
      public static readonly Color Navy    = new Color(0xFF000080);
      public static readonly Color Green   = new Color(0xFF008000);
      public static readonly Color Red     = new Color(0xFFFF0000);
      public static readonly Color Maroon  = new Color(0xFF800000);
      public static readonly Color Blue    = new Color(0xFF0000FF);
      public static readonly Color Olive   = new Color(0xFF808000);
      public static readonly Color Aqua    = new Color(0xFF00FFFF);
      public static readonly Color Teal    = new Color(0xFF008080);
      public static readonly Color Magenta = new Color(0xFFFF00FF); // Fuchsia
      public static readonly Color Gray    = new Color(0xFF808080);

      public byte R,G,B,A;

      public Color(byte r, byte g, byte b, byte a) { R = r; G = g; B = b; A = a; }
      public Color(byte r, byte g, byte b) { R = r; G = g; B = b; A = 0xFF; }
      public Color(ulong aarrggbb) {
         if (aarrggbb > 0xFFFFFFFF)
            throw new ArgumentException("Value incorrect");
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
      public static Color RandomColor(Random rnd) {
         var next = rnd.Next();
         return new Color {
            R = (byte) ((next & 0xFF) >> 0),
            G = (byte) ((next & 0xFF00) >> 8),
            B = (byte) ((next & 0xFF0000) >> 16),
            A = 255};
      }

      /// <summary> —м€гчить цвет </summary>
      /// <param name="clr"></param>
      /// <param name="basic"> от заданной границы светлости буду создавать новый цвет </param>
      /// <returns></returns>
      public static Color Attenuate(this Color clr, int basic = 120) {
         System.Diagnostics.Debug.Assert(basic >= 0 && basic < 0xFF);
         return new Color {
            R = (byte) (basic + clr.R%(0xFF - basic)),
            G = (byte) (basic + clr.G%(0xFF - basic)),
            B = (byte) (basic + clr.B%(0xFF - basic)),
            A = clr.A
         };
      }

      /// <summary> Creates brighter version of this Color </summary>
      /// <param name="clr">from</param>
      /// <param name="percent">0.0 - as is; 1 - WHITE</param>
      /// <returns></returns>
      public static Color Brighter(this Color clr, double percent = 0.7) {
         var tmp = new Color((byte)(0xFF - clr.R), (byte)(0xFF - clr.G), (byte)(0xFF - clr.B), clr.A);
         tmp = tmp.Darker(percent);
         return new Color((byte)(0xFF - tmp.R), (byte)(0xFF - tmp.G), (byte)(0xFF - tmp.B), tmp.A);
      }

      /// <summary> Creates darker version of this Color </summary>
      /// <param name="clr">from</param>
      /// <param name="percent">0.0 - as is; 1 - BLACK</param>
      /// <returns></returns>
      public static Color Darker(this Color clr, double percent = 0.7) {
         var tmp = 1 - Math.Min(1, Math.Max(0, percent));
         return new Color(
            (byte)(clr.R*tmp), // (byte)Math.Min(clr.R*tmp, byte.MaxValue),
            (byte)(clr.G*tmp), // (byte)Math.Min(clr.G*tmp, byte.MaxValue),
            (byte)(clr.B*tmp), // (byte)Math.Min(clr.B*tmp, byte.MaxValue),
            clr.A);
      }

#if WINDOWS_RT
      public static Color ToFmColor(this Windows.UI.Color self) { return new Color(self.R, self.G, self.B, self.A); }
      public static Windows.UI.Color ToWinColor(this Color self) { return new Windows.UI.Color { A = self.A, B = self.B, G = self.G, R = self.R }; }
#elif WINDOWS_FORMS
      ...
#else
      ...
#endif
   }
}