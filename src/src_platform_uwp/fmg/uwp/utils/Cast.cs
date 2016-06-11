using fmg.common;
using fmg.common.geom;

namespace fmg.uwp.utils {

   /// <summary>
   /// Приведение типов от платформонезависемых чистых C# классов fmg.common.* к библиотечным Windows.Foundation классам
   /// </summary>
   public static class Cast {

      public static Size ToFmSize(this Windows.Foundation.Size self) { return new Size((int)self.Width, (int)self.Height); }
      public static Windows.Foundation.Size ToWinSize(this Size self) { return new Windows.Foundation.Size { Width = self.Width, Height = self.Height }; }

      public static SizeDouble ToFmSizeDouble(this Windows.Foundation.Size self) { return new SizeDouble(self.Width, self.Height); }
      public static Windows.Foundation.Size ToWinSize(this SizeDouble self) { return new Windows.Foundation.Size { Width = self.Width, Height = self.Height }; }

      public static Rect ToFmRect(this Windows.Foundation.Rect self) { return new Rect((int)self.X, (int)self.Y, (int)self.Width, (int)self.Height); }
      public static Windows.Foundation.Rect ToWinRect(this Rect self) { return new Windows.Foundation.Rect(self.X, self.Y, self.Width, self.Height); }

      public static RectDouble ToFmRectDouble(this Windows.Foundation.Rect self) { return new RectDouble(self.X, self.Y, self.Width, self.Height); }
      public static Windows.Foundation.Rect ToWinRect(this RectDouble self) { return new Windows.Foundation.Rect(self.X, self.Y, self.Width, self.Height); }

      public static Point ToFmPoint(this Windows.Foundation.Point self) { return new Point((int)self.X, (int)self.Y); }
      public static Windows.Foundation.Point ToWinPoint(this Point self) { return new Windows.Foundation.Point { X = self.X, Y = self.Y }; }

      public static PointDouble ToFmRectDouble(this Windows.Foundation.Point self) { return new PointDouble(self.X, self.Y); }
      public static Windows.Foundation.Point ToWinPoint(this PointDouble self) { return new Windows.Foundation.Point { X = self.X, Y = self.Y }; }

      public static Color ToFmColor(this Windows.UI.Color self) { return new Color(self.A, self.R, self.G, self.B); }
      public static Windows.UI.Color ToWinColor(this Color self) { return new Windows.UI.Color { A = self.A, B = self.B, G = self.G, R = self.R }; }

   }
}
