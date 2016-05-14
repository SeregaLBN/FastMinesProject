using System;
using fmg.common.geom;
using fmg.common.ui;

namespace fmg.core.img
{
   public abstract class RotatedImg<T, TImage> : StaticImg<T, TImage>
      where TImage : class
   {
      public static Func<ITimer> TimerCreator;

      protected RotatedImg(T entity, int widthAndHeight = DefaultImageSize, int? padding = null)
         : base(entity, widthAndHeight, padding)
      { }

      protected RotatedImg(T entity, Size sizeImage, Bound padding)
         : base(entity, sizeImage, padding)
      { }

      private long _redrawInterval = 100;
      /// <summary> frequency of redrawing (in milliseconds) </summary>
      public long RedrawInterval {
         get { return _redrawInterval; }
         set {
            if (SetProperty(ref _redrawInterval, value) && (_timer != null))
               _timer.Interval  = _redrawInterval;
         }
      }

      private ITimer _timer;

      private bool _rotate;
      public bool Rotate {
         get { return _rotate; }
         set {
            if (SetProperty(ref _rotate, value))
               if (value)
                  StartTimer();
               else
                  StopTimer();
         }
      }

      private double _rotateAngleDelta = 1.4;
      public double RotateAngleDelta {
         get { return _rotateAngleDelta; }
         set {
            if (SetProperty(ref _rotateAngleDelta, value) && Rotate)
               Invalidate();
         }
      }

      protected void StartTimer() {
         if (_timer == null) {
            _timer = TimerCreator();
            _timer.Interval = RedrawInterval;
         }
         _timer.Callback = () => OnTimer(); //  start
      }

      protected void StopTimer() {
         if ((_timer != null) && !LiveImage())
            _timer.Callback = null; // stop
      }

      protected virtual void OnTimer() {
         if (Rotate)
            RotateStep();
      }

      protected virtual bool LiveImage() {
         return Rotate;
      }

      private void RotateStep() {
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
            _timer?.Dispose();
            _timer = null;
         }
         // free native resources if there are any.
      }

   }
}