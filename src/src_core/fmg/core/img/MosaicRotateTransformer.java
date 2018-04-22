package fmg.core.img;

/** Representable {@link fmg.core.types.EMosaic} as animated image */
public class MosaicRotateTransformer implements IModelTransformer {

   @Override
   public void execute(int currentFrame, int totalFrames, IImageModel model) {
      if (!(model instanceof MosaicsAnimatedModel<?>))
         throw new RuntimeException("Illegal usage transformer");

      MosaicsAnimatedModel<?> mam = (MosaicsAnimatedModel<?>)model;

      double rotateAngleDelta = 360.0 / totalFrames; // 360° / TotalFrames
//    //if (!mam.getAnimeDirection())
//    //   rotateAngleDelta = -rotateAngleDelta;
      double rotateAngle = currentFrame * rotateAngleDelta;
      mam.setRotateAngle(rotateAngle);

      switch (mam.getRotateMode()) {
      case fullMatrix:
         mam.rotateMatrix();
         break;
      case someCells:
         mam.updateAnglesOffsets(rotateAngleDelta);
         mam.rotateCells();
         break;
      }
   }

}
