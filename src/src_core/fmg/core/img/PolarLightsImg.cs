using fmg.common;

namespace fmg.core.img {

   public abstract class PolarLightsImg<T, TImage> : RotatedImg<T, TImage>
      where TImage : class
   {

      protected PolarLightsImg(T entity, int widthAndHeight = DefaultImageSize, int? padding = null)
         : base(entity, widthAndHeight, padding)
      { }

      private bool _polarLights;
      /// <summary> shimmering filling </summary>
      public bool PolarLights {
         get { return _polarLights; }
         set {
            if (SetProperty(ref _polarLights, value))
               if (value)
                  StartTimer();
               else
                  StopTimer();
         }
      }

      private void NextForegroundColor() {
         HSV hsv = new HSV(ForegroundColor);
         hsv.h += RotateAngleDelta;
         ForegroundColor = hsv.toColor();
      }

      protected override void OnTimer() {
         if (PolarLights)
            NextForegroundColor();
         base.OnTimer();
      }

      protected override bool LiveImage() {
         return PolarLights || base.LiveImage();
      }

   }
}
