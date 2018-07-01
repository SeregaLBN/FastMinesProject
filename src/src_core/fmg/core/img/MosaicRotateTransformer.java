package fmg.core.img;

/** Representable {@link fmg.core.types.EMosaic} as animated image */
public class MosaicRotateTransformer implements IModelTransformer {

   @Override
   public void execute(IAnimatedModel model) {
      if (!(model instanceof MosaicAnimatedModel<?>))
         throw new RuntimeException("Illegal usage transformer");

      MosaicAnimatedModel<?> mam = (MosaicAnimatedModel<?>)model;

      double rotateAngleDelta = 360.0 / model.getTotalFrames(); // 360° / TotalFrames
//    //if (!mam.getAnimeDirection())
//    //   rotateAngleDelta = -rotateAngleDelta;
      double rotateAngle = mam.getCurrentFrame() * rotateAngleDelta;
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
