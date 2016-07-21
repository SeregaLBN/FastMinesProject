using System;
using fmg.common.ui;

namespace fmg.core.img {

   public abstract class RotatedImgConst {

      public static Func<ITimer> TimerCreator;

   }

   /// <summary> <see cref="StaticImg{TImage}"/> with rotated properties </summary>
   /// <typeparam name="TImage">plaform specific image</typeparam>
   public abstract class RotatedImg<TImage> : StaticImg<TImage>
      where TImage : class
   {
      //protected RotatedImg() : base() { }

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
            _timer = RotatedImgConst.TimerCreator();
            _timer.Interval = RedrawInterval;
         }
         _timer.Callback = OnTimer; //  start
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