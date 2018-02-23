package fmg.core.img;

/** Transforming of rotate angle */
public class RotateTransformer implements IModelTransformer {

   @Override
   public void execute(int currentFrame, int totalFrames, IImageModel model) {
      if (!(model instanceof ImageProperties))
         return;
      ImageProperties ip = (ImageProperties)model;

      double rotateAngleDelta = 360.0 / totalFrames; // 360° / TotalFrames
      double rotateAngle = currentFrame * rotateAngleDelta;
      ip.setRotateAngle(rotateAngle);
   }

}
