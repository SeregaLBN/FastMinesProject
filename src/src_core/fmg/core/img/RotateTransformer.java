package fmg.core.img;

/** Transforming of rotate angle */
public class RotateTransformer implements IModelTransformer {

   @Override
   public void execute(int currentFrame, int totalFrames, IImageModel model) {
      if (!(model instanceof AnimatedImageModel))
         throw new RuntimeException("Illegal usage transformer");

      AnimatedImageModel am = (AnimatedImageModel)model;

      double rotateAngleDelta = 360.0 / totalFrames; // 360° / TotalFrames
      if (!am.getAnimeDirection())
         rotateAngleDelta = -rotateAngleDelta;
      double rotateAngle = currentFrame * rotateAngleDelta;
      am.setRotateAngle(rotateAngle);
   }

}
