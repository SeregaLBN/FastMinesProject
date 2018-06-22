using System;
using fmg.common.ui;

namespace fmg.core.img {

   /// <summary> Transforming of rotate angle </summary>
   public class RotateTransformer : IModelTransformer {

      public void Execute(int currentFrame, int totalFrames, IImageModel model) {
         AnimatedImageModel am = model as AnimatedImageModel;
         if (am == null)
            throw new Exception("Illegal usage transformer");

         double rotateAngleDelta = 360.0 / totalFrames; // 360° / TotalFrames
         if (!am.AnimeDirection)
            rotateAngleDelta = -rotateAngleDelta;
         double rotateAngle = currentFrame * rotateAngleDelta;
         am.RotateAngle = rotateAngle;
      }

   }

}