using System;
using Windows.UI.Xaml;

namespace fmg.uwp.res.img
{
   public abstract class RotatedImg<T, TImage> : StaticImg<T, TImage>, IDisposable
      where TImage : class
   {
      protected RotatedImg(T entity, int widthAndHeight = DefaultImageSize, int? padding = null)
         : base(entity, widthAndHeight, padding)
      { }

      /// <summary> frequency of redrawing (in milliseconds) </summary>
      public double RedrawInterval { get; set; } = 100;

      private DispatcherTimer _timer;

      private bool _rotate;
      public bool Rotate {
         get { return _rotate; }
         set {
            if (SetProperty(ref _rotate, value) && value)
               DrawAsync();
         }
      }

      private double _rotateAngleDelta = 1.4;
      public double RotateAngleDelta {
         get { return _rotateAngleDelta; }
         set {
            if (SetProperty(ref _rotateAngleDelta, value) && Rotate)
               DrawAsync();
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
      }

      private void OnTick(object sender, object e) {
         OnTimer();
      }

      protected virtual void OnTimer() {
         RotateStep();
         DrawAsync();
      }

      protected virtual bool LiveImage() {
         return Rotate;
      }

      private void RotateStep() {
         if (!Rotate)
            return;

         var rotateAngle = RotateAngle + RotateAngleDelta;
         if (RotateAngleDelta > 0) {
            if (rotateAngle >= 360)
               rotateAngle -= 360;
         } else {
            if (rotateAngle <= -360)
               rotateAngle += 360;
         }
         RotateAngle = rotateAngle;
      }

      public void Dispose() {
         Dispose(true);
      }

      protected virtual void Dispose(bool disposing) {
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