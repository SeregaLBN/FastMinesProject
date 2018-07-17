using System;
using fmg.common.ui;

namespace fmg.core.img {

   /// <summary> Transforming of rotate angle </summary>
   public class RotateTransformer : IModelTransformer {

      public void Execute(IAnimatedModel model) {
         if (!(model is AnimatedImageModel am))
            throw new Exception("Illegal usage transformer");

         double rotateAngleDelta = 360.0 / am.TotalFrames; // 360° / TotalFrames
         if (!am.AnimeDirection)
            rotateAngleDelta = -rotateAngleDelta;
         double rotateAngle = am.CurrentFrame * rotateAngleDelta;
         am.RotateAngle = rotateAngle;
      }

   }

}