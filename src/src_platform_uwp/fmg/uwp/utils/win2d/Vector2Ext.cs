using fmg.common.geom;

namespace fmg.uwp.utils.win2d {

   public static class Vector2Ext {

      public static PointDouble ToFmPointDouble(this System.Numerics.Vector2 vector) { return new PointDouble(vector.X, vector.Y); }
      public static System.Numerics.Vector2 ToVector2(this PointDouble point) { return new System.Numerics.Vector2((float)point.X, (float)point.Y); }

      public static SizeDouble ToFmSizeDouble(this System.Numerics.Vector2 vector) { return new SizeDouble(vector.X, vector.Y); }
      public static System.Numerics.Vector2 ToVector2(this SizeDouble size) { return new System.Numerics.Vector2((float)size.Width, (float)size.Height); }

   }

}
