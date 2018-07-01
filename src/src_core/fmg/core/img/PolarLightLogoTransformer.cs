using System;
using fmg.common;

namespace fmg.core.img {

   /// <summary> Transforming of logo palette </summary>
   public class PolarLightLogoTransformer : IModelTransformer {

      public void Execute(IAnimatedModel model) {
         LogoModel lm = model as LogoModel;
         if (lm == null)
            throw new Exception("Illegal usage transformer");

         if (!lm.PolarLights)
            return;

         double rotateAngleDelta = 360.0 / lm.TotalFrames; // 360° / TotalFrames
         if (!lm.AnimeDirection)
            rotateAngleDelta = -rotateAngleDelta;
         HSV[] palette = lm.Palette;
         for (int i = 0; i < palette.Length; ++i)
            palette[i].h += rotateAngleDelta;
      }

   }
}