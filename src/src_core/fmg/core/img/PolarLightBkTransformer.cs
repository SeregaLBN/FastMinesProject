using System;
using fmg.common;

namespace fmg.core.img {

   /// <summary> Transforming of background color (rotation of background color) </summary>
   public class PolarLightBkTransformer : IModelTransformer {

      public void Execute(IAnimatedModel model) {
         AnimatedImageModel am = model as AnimatedImageModel;
         if (am == null)
            throw new Exception("Illegal usage transformer");

         if (!am.PolarLights)
            return;

         double rotateAngleDelta = 360.0 / am.TotalFrames; // 360° / TotalFrames
         if (!am.AnimeDirection)
            rotateAngleDelta = -rotateAngleDelta;

         HSV hsv = new HSV(am.BackgroundColor);
         hsv.h += rotateAngleDelta;
         am.BackgroundColor = hsv.ToColor();
      }

   }

}
