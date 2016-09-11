using System;
using fmg.common.geom;

namespace fmg.common
{

   /// <summary>
   /// Сylindrical-coordinate representations of points in an RGB color model
   /// <p>
   /// HSV - Hue, Saturation, Value(тон, насыщенность, значение) <br>
   ///   it also<br>
   /// HSB - Hue, Saturation, Brightness(тон, насыщенность, яркость)
   /// </p>
   ///
   /// White: h=any; s=0; v=100 <br>
   /// Black: h=any; s=any; v=0
   /// </summary>
   public struct HSV {

      /// <summary>Hue — цветовой тон (цвет/оттенок): <ul> 0°..360°
      /// <li> 0° - red
      /// <li> 60° - yellow
      /// <li> 120° - green
      /// <li> 180° - aqua
      /// <li> 240° - blue
      /// <li> 300° - fuchsia
      /// <li> 360° - red
      /// </summary>
      public double h;

      /// <summary>Saturation — насыщенность: 0%..100% (white(gray)..color) </summary>
      public double s;

      /// <summary>Value or Brightness — значение цвета или яркость: 0%..100% (black..color) </summary>
      public double v;

      /// <summary>Alpha chanel: 0..255 </summary>
      public byte a;

      public HSV(int h, int s = 100, int v = 100, byte a = 0xFF) {
         this.h = h;
         this.s = s;
         this.v = v;
         this.a = a;

         Fix();
      }

      public HSV(Color rgba)
         : this()
      {
         a = rgba.A;
         FromColorDouble(rgba.R, rgba.G, rgba.B);
      }

      public HSV AddHue(double addonH) {
         h += addonH;
         Fix();
         return this;
      }

      private void FromColorDouble(double r, double g, double b) {
         double max = Math.Max(Math.Max(r, g), b);
         double min = Math.Min(Math.Min(r, g), b);

         { // calc H
            if (max.HasMinDiff(min))
               h = 0;
            else if (max.HasMinDiff(r))
               h = 60 * (g - b) / (max - min) + ((g < b) ? 360 : 0);
            else if (max.HasMinDiff(g))
               h = 60 * (b - r) / (max - min) + 120;
            else if (max.HasMinDiff(b))
               h = 60 * (r - g) / (max - min) + 240;
            else
               throw new Exception();
         }
         s = max.HasMinDiff(0) ? 0 : 100 * (1 - min / max);
         v = max * 100 / 255;

         Fix();
      }

      public Color ToColor() {
         double r, g, b;
         ToColorDouble(out r, out g, out b);
         return new Color(a, (byte)(r * 255 / 100), (byte)(g * 255 / 100), (byte)(b * 255 / 100));
      }

      private void ToColorDouble(out double r, out double g, out double b) {
         Fix();

         var vMin = (100 - s) * v / 100;
         var delta = (v - vMin) * ((h % 60) / 60.0);
         var vInc = vMin + delta;
         var vDec = v - delta;

         switch (((int)(h / 60)) % 6) {
         case 0:
            r = v; g = vInc; b = vMin;
            break;
         case 1:
            r = vDec; g = v; b = vMin;
            break;
         case 2:
            r = vMin; g = v; b = vInc;
            break;
         case 3:
            r = vMin; g = vDec; b = v;
            break;
         case 4:
            r = vInc; g = vMin; b = v;
            break;
         case 5:
            r = v; g = vMin; b = vDec;
            break;
         default:
            throw new Exception();
         }
      }

      /// <summary> Update HSV to grayscale </summary>
      public void Grayscale() {
         double r, g, b;
         ToColorDouble(out r, out g, out b);
         r *= 0.2126;
         g *= 0.7152;
         b *= 0.0722;
         FromColorDouble(r, g, b);
      }

      private void Fix() {
         if (h < 0) {
            h %= 360;
            h += 360;
         } else {
            if (h >= 360)
               h %= 360;
         }

         if (s < 0) {
            s = 0;
         } else {
            if (s > 100)
               s = 100;
         }

         if (v < 0) {
            v = 0;
         } else {
            if (v >= 100)
               v = 100;
         }

         //if (a < 0) {
         //   a = 0;
         //} else {
         //   if (a > 255)
         //      a = 255;
         //}
      }


      public static bool operator !=(HSV c1, HSV c2) {
         return (c1.a != c2.a) ||
                (BitConverter.DoubleToInt64Bits(c1.h) != BitConverter.DoubleToInt64Bits(c2.h)) ||
                (BitConverter.DoubleToInt64Bits(c1.s) != BitConverter.DoubleToInt64Bits(c2.s)) ||
                (BitConverter.DoubleToInt64Bits(c1.v) != BitConverter.DoubleToInt64Bits(c2.v));
      }
      public static bool operator ==(HSV c1, HSV c2) {
         return (c1.a == c2.a) &&
                (BitConverter.DoubleToInt64Bits(c1.h) == BitConverter.DoubleToInt64Bits(c2.h)) &&
                (BitConverter.DoubleToInt64Bits(c1.s) == BitConverter.DoubleToInt64Bits(c2.s)) &&
                (BitConverter.DoubleToInt64Bits(c1.v) == BitConverter.DoubleToInt64Bits(c2.v));
      }

      public override bool Equals(object o) {
         if (ReferenceEquals(null, o))
            return false;
         return (o is HSV) && (this == (HSV)o);
      }

      public override int GetHashCode() {
         var result = 31 + a;
         var temp = BitConverter.DoubleToInt64Bits(h);
         result = 31 * result + (int)(temp ^ (temp >> 32));
         temp = BitConverter.DoubleToInt64Bits(s);
         result = 31 * result + (int)(temp ^ (temp >> 32));
         temp = BitConverter.DoubleToInt64Bits(v);
         return 31 * result + (int)(temp ^ (temp >> 32));
      }


      public override string ToString() {
         return (a == 255)
               ? $"HSV[h={h:0.00}, s={s:0.00}, v={v:0.00}]"
               : $"HSV[h={h:0.00}, s={s:0.00}, v={v:0.00}, a={a:X2}]";
      }

   }
}
