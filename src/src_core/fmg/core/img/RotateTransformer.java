package fmg.core.img;

/** Transforming of rotate angle */
public class RotateTransformer implements IModelTransformer {

   @Override
   public void execute(IAnimatedModel model) {
      if (!(model instanceof AnimatedImageModel))
         throw new RuntimeException("Illegal usage transformer");

      AnimatedImageModel am = (AnimatedImageModel)model;

      double rotateAngleDelta = 360.0 / am.getTotalFrames(); // 360° / TotalFrames
      if (!am.getAnimeDirection())
         rotateAngleDelta = -rotateAngleDelta;
      double rotateAngle = am.getCurrentFrame() * rotateAngleDelta;
      am.setRotateAngle(rotateAngle);
   }

}
