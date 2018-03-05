package fmg.core.img;

/** Transforming of rotate angle */
public class RotateTransformer implements IModelTransformer {

   @Override
   public void execute(int currentFrame, int totalFrames, AnimatedImageModel model) {
      double rotateAngleDelta = 360.0 / totalFrames; // 360° / TotalFrames
      if (!model.getAnimeDirection())
         rotateAngleDelta = -rotateAngleDelta;
      double rotateAngle = currentFrame * rotateAngleDelta;
      model.setRotateAngle(rotateAngle);
   }

}
