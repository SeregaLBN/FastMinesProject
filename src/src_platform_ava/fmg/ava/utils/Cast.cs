using fmg.common;
using fmg.common.geom;

namespace fmg.uwp.utils {

   /// <summary>
   /// Приведение типов от платформо-независимых чистых C# классов fmg.common.* к библиотечным Avalonia классам
   /// </summary>
   public static class Cast {

      public static Size ToFmSize(this Avalonia.Size self) { return new Size((int)self.Width, (int)self.Height); }
      public static Avalonia.Size ToWinSize(this Size self) { return new Avalonia.Size(self.Width, self.Height); }

      public static SizeDouble ToFmSizeDouble(this Avalonia.Size self) { return new SizeDouble(self.Width, self.Height); }
      public static Avalonia.Size ToWinSize(this SizeDouble self) { return new Avalonia.Size(self.Width, self.Height); }

      public static Rect ToFmRect(this Avalonia.Rect self) { return new Rect((int)self.X, (int)self.Y, (int)self.Width, (int)self.Height); }
      public static Avalonia.Rect ToWinRect(this Rect self) { return new Avalonia.Rect(self.X, self.Y, self.Width, self.Height); }

      public static RectDouble ToFmRectDouble(this Avalonia.Rect self) { return new RectDouble(self.X, self.Y, self.Width, self.Height); }
      public static Avalonia.Rect ToWinRect(this RectDouble self) { return new Avalonia.Rect(self.X, self.Y, self.Width, self.Height); }

      public static Point ToFmPoint(this Avalonia.Point self) { return new Point((int)self.X, (int)self.Y); }
      public static Avalonia.Point ToWinPoint(this Point self) { return new Avalonia.Point(self.X, self.Y); }

      public static PointDouble ToFmPointDouble(this Avalonia.Point self) { return new PointDouble(self.X, self.Y); }
      public static Avalonia.Point ToWinPoint(this PointDouble self) { return new Avalonia.Point(self.X, self.Y); }

      public static Color ToFmColor(this Avalonia.Media.Color self) { return new Color(self.A, self.R, self.G, self.B); }
      public static Avalonia.Media.Color ToWinColor(this Color self) { return new Avalonia.Media.Color(self.A, self.B, self.G, self.R); }

      public static HSV ToHsvColor(this Avalonia.Media.Color self) { return new HSV(self.ToFmColor()); }
      public static Avalonia.Media.Color ToWinColor(this HSV self) { return self.ToColor().ToWinColor(); }

   }

}
