package fmg.core.img;

/**
 * Representable {@link fmg.core.types.EMosaic} as animated image
 */
public abstract class MosaicRotateTransformer  implements IModelTransformer {

   @Override
   public void execute(int currentFrame, int totalFrames, IImageModel model) {
      if (!(model instanceof MosaicsAnimatedModel<?>))
         throw new RuntimeException("Illegal usage transformer");

      MosaicsAnimatedModel<?> mam = (MosaicsAnimatedModel<?>)model;

      double rotateAngleDelta = 360.0 / totalFrames; // 360° / TotalFrames
//    //if (!mam.getAnimeDirection())
//    //   rotateAngleDelta = -rotateAngleDelta;
      double rotateAnglePrevious = (currentFrame-1) * rotateAngleDelta;

      switch (mam.getRotateMode()) {
      case fullMatrix:
         mam.rotateMatrix();
         break;
      case someCells:
         mam.updateAnglesOffsets(rotateAnglePrevious, rotateAngleDelta);
         mam.rotateCells();
         break;
      }
   }

}
