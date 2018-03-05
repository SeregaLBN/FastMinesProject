package fmg.core.img;

import fmg.common.HSV;

/** Transforming of foreground color (rotation of background color) */
public class PolarLightBkTransformer implements IModelTransformer {

   @Override
   public void execute(int currentFrame, int totalFrames, AnimatedImageModel model) {
      if (!model.isPolarLights())
         return;

      double rotateAngleDelta = 360.0 / totalFrames; // 360° / TotalFrames
      if (!model.getAnimeDirection())
         rotateAngleDelta = -rotateAngleDelta;

      HSV hsv = new HSV(model.getBackgroundColor());
      hsv.h += rotateAngleDelta;
      model.setBackgroundColor(hsv.toColor());
   }

}
