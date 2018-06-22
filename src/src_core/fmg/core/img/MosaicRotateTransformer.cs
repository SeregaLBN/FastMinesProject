using System;

namespace fmg.core.img {

   /// <summary> Representable {@link fmg.core.types.EMosaic} as animated image </summary>
   public class MosaicRotateTransformer<TImage> implements IModelTransformer
      where TImage : class
   {

      public void Execute(int currentFrame, int totalFrames, IImageModel model) {
         MosaicAnimatedModel<TImage> mam = model as MosaicAnimatedModel<TImage>;

         if (mam == null)
            throw new Exception("Illegal usage transformer");

         double rotateAngleDelta = 360.0 / totalFrames; // 360° / TotalFrames
   //    //if (!mam.AnimeDirection)
   //    //   rotateAngleDelta = -rotateAngleDelta;
         double rotateAngle = currentFrame * rotateAngleDelta;
         mam.RotateAngle = rotateAngle;

         switch (mam.RotateMode) {
         case fullMatrix:
            mam.RotateMatrix();
            break;
         case someCells:
            mam.UpdateAnglesOffsets(rotateAngleDelta);
            mam.RotateCells();
            break;
         }
      }

   }

}
