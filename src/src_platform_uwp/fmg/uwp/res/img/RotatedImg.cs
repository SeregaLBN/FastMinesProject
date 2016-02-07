using System;
using Windows.UI.Xaml;
using fmg.common.geom;

namespace fmg.uwp.res.img
{
   public abstract class RotatedImg<T, TImage> : StaticImg<T, TImage>
      where TImage : class
   {
      protected RotatedImg(T entity, int widthAndHeight = DefaultImageSize, int? padding = null)
         : base(entity, widthAndHeight, padding)
      { }

      protected RotatedImg(T entity, Size sizeImage, Bound padding)
         : base(entity, sizeImage, padding)
      { }

      /// <summary> frequency of redrawing (in milliseconds) </summary>
      public double RedrawInterval { get; set; } = 100;

      private DispatcherTimer _timer;

      private bool _rotate;
      public bool Rotate {
         get { return _rotate; }
         set {
            if (SetProperty(ref _rotate, value) && value)
               Redraw();
         }
      }

      private double _rotateAngleDelta = 1.4;
      public double RotateAngleDelta {
         get { return _rotateAngleDelta; }
         set {
            if (SetProperty(ref _rotateAngleDelta, value) && Rotate)
               Redraw();
         }
      }

      protected override void DrawEnd() {
         if (LiveImage()) {
            if (_timer == null) {
               _timer = new DispatcherTimer { Interval = TimeSpan.FromMilliseconds(RedrawInterval) };
               _timer.Tick += OnTick;
            }
            _timer.Start();
         } else {
            _timer?.Stop();
         }
         base.DrawEnd();
      }

      private void OnTick(object sender, object e) {
         OnTimer();
      }

      protected virtual void OnTimer() {
         RotateStep();
      }

      protected virtual bool LiveImage() {
         return Rotate;
      }

      private void RotateStep() {
         if (!Rotate)
            return;

         var rotateAngle = RotateAngle + RotateAngleDelta;
         if (rotateAngle >= 360) {
            rotateAngle -= 360;
         } else {
            if (rotateAngle < 0)
               rotateAngle += 360;
         }
         RotateAngle = rotateAngle;
         System.Diagnostics.Debug.Assert((rotateAngle >= 0) && (rotateAngle < 360));
      }

      protected override void Dispose(bool disposing) {
         if (disposing) {
            // free managed resources
            var t = _timer;
            if (t != null) {
               t.Tick -= OnTick;
               t.Stop();
            }
         }
         // free native resources if there are any.
      }

   }
}