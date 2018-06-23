using System;
using fmg.common;

namespace fmg.core.img {

   /// <summary> Transforming of foreground color (rotation of foreground color) </summary>
   public class PolarLightFgTransformer : IModelTransformer {

      public void Execute(int currentFrame, int totalFrames, IImageModel model) {
         var am = model as AnimatedImageModel;
         if (am == null)
            throw new Exception("Illegal usage transformer");

         if (!am.PolarLights)
            return;

         double rotateAngleDelta = 360.0 / totalFrames; // 360° / TotalFrames
         if (!am.AnimeDirection)
            rotateAngleDelta = -rotateAngleDelta;

         HSV hsv = new HSV(am.ForegroundColor);
         hsv.h += rotateAngleDelta;
         am.ForegroundColor = hsv.ToColor();
      }

   }

}
