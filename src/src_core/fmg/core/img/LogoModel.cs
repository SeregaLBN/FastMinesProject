using System.Collections.Generic;
using System.ComponentModel;
using fmg.common;
using fmg.common.geom;
using fmg.common.geom.util;

namespace fmg.core.img {

   /// <summary> MVC: model for FastMines logo image </summary>
   public abstract class LogoModel : AnimatedImageModel {

      public enum ERotateMode {
         /// <summary> rotate image </summary>
         Classic,
         /// <summary> rotate color Palette </summary>
         Color,
         /// <summary> color + classic </summary>
         Combi
      }


      public readonly HSV[] _palette = { new HSV(  0), new HSV( 45), new HSV( 90), new HSV(135),
                                        new HSV(180), new HSV(225), new HSV(270), new HSV(315) };
      private bool _useGradient;
      private ERotateMode _rotateMode = ERotateMode.Combi;
      /** owner rays points */
      private readonly IList<PointDouble> _rays = new List<PointDouble>();
      /** inner octahedron */
      private readonly IList<PointDouble> _inn = new List<PointDouble>();
      /** central octahedron */
      private readonly IList<PointDouble> _oct = new List<PointDouble>();


      protected ALogo() {
         BackgroundColor = Color.Transparent;
         PropertyChanged += OnPropertyChanged;
      }

      public HSV[] Palette => _palette;

      public static void toMineModel(LogoModel m) {
         m.UseGradient = false;
         for (HSV item : m.Palette)
            //item.v = 75;
            item.Grayscale();
      }

      public bool UseGradient {
         get { return _useGradient; }
         set { _notifier.SetProperty(ref _useGradient, value); }
      }

      public ERotateMode RotateMode {
         get { return _rotateMode; }
         set { _notifier.SetProperty(ref _rotateMode, value); }
      }

      public double ZoomX => (Size.Width  - Padding.LeftAndRight) / 200.0;
      public double ZoomY => (Size.Height - Padding.TopAndBottom) / 200.0;

      public IList<PointDouble> Rays { get {
         if (_rays.Empty) {
            var pl = Padding.Left;
            var pt = Padding.Top;
            var zx = ZoomX;
            var zy = ZoomY;

            _rays.Add(new PointDouble(pl + 100.0000 * zx, pt + 200.0000 * zy));
            _rays.Add(new PointDouble(pl + 170.7107 * zx, pt +  29.2893 * zy));
            _rays.Add(new PointDouble(pl +   0.0000 * zx, pt + 100.0000 * zy));
            _rays.Add(new PointDouble(pl + 170.7107 * zx, pt + 170.7107 * zy));
            _rays.Add(new PointDouble(pl + 100.0000 * zx, pt +   0.0000 * zy));
            _rays.Add(new PointDouble(pl +  29.2893 * zx, pt + 170.7107 * zy));
            _rays.Add(new PointDouble(pl + 200.0000 * zx, pt + 100.0000 * zy));
            _rays.Add(new PointDouble(pl +  29.2893 * zx, pt +  29.2893 * zy));
         }
         return _rays;
      }}

      public IList<PointDouble> Inn { get {
         if (_inn.Empty) {
            var pl = Padding.Left;
            var pt = Padding.Top;
            var zx = ZoomX;
            var zy = ZoomY;

            _inn.Add(new PointDouble(pl + 100.0346 * zx, pt + 141.4070 * zy));
            _inn.Add(new PointDouble(pl + 129.3408 * zx, pt +  70.7320 * zy));
            _inn.Add(new PointDouble(pl +  58.5800 * zx, pt + 100.0000 * zy));
            _inn.Add(new PointDouble(pl + 129.2500 * zx, pt + 129.2500 * zy));
            _inn.Add(new PointDouble(pl +  99.9011 * zx, pt +  58.5377 * zy));
            _inn.Add(new PointDouble(pl +  70.7233 * zx, pt + 129.3198 * zy));
            _inn.Add(new PointDouble(pl + 141.4167 * zx, pt + 100.0000 * zy));
            _inn.Add(new PointDouble(pl +  70.7500 * zx, pt +  70.7500 * zy));
         }
         return _inn;
      }}

      public IList<PointDouble> Oct { get {
         if (_oct.Empty) {
            var pl = Padding.Left;
            var pt = Padding.Top;
            var zx = ZoomX;
            var zy = ZoomY;

            _oct.Add(new PointDouble(pl + 120.7053 * zx, pt + 149.9897 * zy));
            _oct.Add(new PointDouble(pl + 120.7269 * zx, pt +  50.0007 * zy));
            _oct.Add(new PointDouble(pl +  50.0034 * zx, pt + 120.7137 * zy));
            _oct.Add(new PointDouble(pl + 150.0000 * zx, pt + 120.6950 * zy));
            _oct.Add(new PointDouble(pl +  79.3120 * zx, pt +  50.0007 * zy));
            _oct.Add(new PointDouble(pl +  79.2624 * zx, pt + 149.9727 * zy));
            _oct.Add(new PointDouble(pl + 150.0000 * zx, pt +  79.2737 * zy));
            _oct.Add(new PointDouble(pl +  50.0034 * zx, pt +  79.3093 * zy));
         }
         return _oct;
      }}

      protected void OnPropertyChanged(object sender, PropertyChangedEventArgs ev) {
         switch (ev.PropertyName) {
         case nameof(IImageMode.Size):
         case nameof(ImageMode.Padding):
            _rays.clear();
            _inn.clear();
            _oct.clear();
            break;
         }
      }

      protected override void Dispose(bool disposing) {
         if (disposing) {
            PropertyChanged -= OnPropertyChanged;
         }
      }

      /*
      {
         if (RotateMode != ERotateMode.Color) {
            var ra = RotateAngle;
            rays.RotateList(ra, center);
            inn.RotateList(ra, center);
            oct.RotateList(ra, center);
         }
      }
      {
         if ((RotateMode != ERotateMode.Classic) && (nameof(this.RotateAngle) == ev.PropertyName)) {
            var delta = RotateAngleDelta;
            for (var i = 0; i < Palette.Length; ++i) {
               Palette[i].h += delta;
            }
         }
      }
      */

   }

}
