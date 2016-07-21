using fmg.common;

namespace fmg.core.img {

   /// <summary> <see cref="RotatedImg{TImage}"/> with hue rotation effect.
   /// Alters the color of an image by rotating its hue values.
   /// </summary>
   /// <typeparam name="TImage">plaform specific image</typeparam>
   public abstract class PolarLightsImg<TImage> : RotatedImg<TImage>
      where TImage : class
   {
      //protected PolarLightsImg() : base() { }

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
         var hsv = new HSV(ForegroundColor);
         hsv.h += RotateAngleDelta;
         ForegroundColor = hsv.ToColor();
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
