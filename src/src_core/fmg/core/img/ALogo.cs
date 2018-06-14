using System.Collections.Generic;
using System.ComponentModel;
using fmg.common;
using fmg.common.geom;
using fmg.common.geom.util;

namespace fmg.core.img {

   /// <summary> Abstract main logos image </summary>
   public abstract class ALogo<TImage> : PolarLightsImg<TImage>
      where TImage : class
   {

      public enum ERotateMode {
         Classic, // rotate image
         Color, // rotate color Palette
         Combi // color + classic
      }

      protected ALogo() {
         BackgroundColor = Color.Transparent;
      }

      public readonly HSV[] Palette = {
         new HSV(  0), new HSV( 45), new HSV( 90), new HSV(135),
         new HSV(180), new HSV(225), new HSV(270), new HSV(315) };

      private bool _useGradient;
      public bool UseGradient {
         get { return _useGradient; }
         set {
            if (SetProperty(ref _useGradient, value))
               Invalidate();
         }
      }

      private ERotateMode _rotateMode = ERotateMode.Combi;
      public ERotateMode RotateMode {
         get { return _rotateMode; }
         set { SetProperty(ref _rotateMode, value); }
      }

      protected double ZoomX => (Size.Width  - Padding.LeftAndRight) / 200.0;
      protected double ZoomY => (Size.Height - Padding.TopAndBottom) / 200.0;

      protected void GetCoords(IList<PointDouble> rays, IList<PointDouble> inn, IList<PointDouble> oct) {
         var pl = Padding.Left;
         var pt = Padding.Top;
         var zx = ZoomX;
         var zy = ZoomY;
         var center = new PointDouble(Size.Width / 2.0, Size.Height / 2.0);

         rays.Clear();
         rays.Add(new PointDouble(pl + 100.0000 * zx, pt + 200.0000 * zy));
         rays.Add(new PointDouble(pl + 170.7107 * zx, pt +  29.2893 * zy));
         rays.Add(new PointDouble(pl +   0.0000 * zx, pt + 100.0000 * zy));
         rays.Add(new PointDouble(pl + 170.7107 * zx, pt + 170.7107 * zy));
         rays.Add(new PointDouble(pl + 100.0000 * zx, pt +   0.0000 * zy));
         rays.Add(new PointDouble(pl +  29.2893 * zx, pt + 170.7107 * zy));
         rays.Add(new PointDouble(pl + 200.0000 * zx, pt + 100.0000 * zy));
         rays.Add(new PointDouble(pl +  29.2893 * zx, pt +  29.2893 * zy));

         inn.Clear();
         inn.Add(new PointDouble(pl + 100.0346 * zx, pt + 141.4070 * zy));
         inn.Add(new PointDouble(pl + 129.3408 * zx, pt +  70.7320 * zy));
         inn.Add(new PointDouble(pl +  58.5800 * zx, pt + 100.0000 * zy));
         inn.Add(new PointDouble(pl + 129.2500 * zx, pt + 129.2500 * zy));
         inn.Add(new PointDouble(pl +  99.9011 * zx, pt +  58.5377 * zy));
         inn.Add(new PointDouble(pl +  70.7233 * zx, pt + 129.3198 * zy));
         inn.Add(new PointDouble(pl + 141.4167 * zx, pt + 100.0000 * zy));
         inn.Add(new PointDouble(pl +  70.7500 * zx, pt +  70.7500 * zy));

         oct.Clear();
         oct.Add(new PointDouble(pl + 120.7053 * zx, pt + 149.9897 * zy));
         oct.Add(new PointDouble(pl + 120.7269 * zx, pt +  50.0007 * zy));
         oct.Add(new PointDouble(pl +  50.0034 * zx, pt + 120.7137 * zy));
         oct.Add(new PointDouble(pl + 150.0000 * zx, pt + 120.6950 * zy));
         oct.Add(new PointDouble(pl +  79.3120 * zx, pt +  50.0007 * zy));
         oct.Add(new PointDouble(pl +  79.2624 * zx, pt + 149.9727 * zy));
         oct.Add(new PointDouble(pl + 150.0000 * zx, pt +  79.2737 * zy));
         oct.Add(new PointDouble(pl +  50.0034 * zx, pt +  79.3093 * zy));

         if (RotateMode != ERotateMode.Color) {
            var ra = RotateAngle;
            rays.RotateList(ra, center);
            inn.RotateList(ra, center);
            oct.RotateList(ra, center);
         }
      }

      protected override void OnPropertyChanged(PropertyChangedEventArgs ev) {
         if ((RotateMode != ERotateMode.Classic) && (nameof(this.RotateAngle) == ev.PropertyName)) {
            var delta = RotateAngleDelta;
            for (var i = 0; i < Palette.Length; ++i) {
               Palette[i].h += delta;
            }
         }
         base.OnPropertyChanged(ev);
      }

   }

}
