package fmg.core.img;

import fmg.common.HSV;

/** Transforming of foreground color (rotation of foreground color) */
public class PolarLightFgTransformer implements IModelTransformer {

   @Override
   public void execute(int currentFrame, int totalFrames, IImageModel model) {
      if (!(model instanceof AnimatedImageModel))
         throw new RuntimeException("Illegal usage transformer");

      AnimatedImageModel am = (AnimatedImageModel)model;
      if (!am.isPolarLights())
         return;

      double rotateAngleDelta = 360.0 / totalFrames; // 360° / TotalFrames
      if (!am.getAnimeDirection())
         rotateAngleDelta = -rotateAngleDelta;

      HSV hsv = new HSV(am.getForegroundColor());
      hsv.h += rotateAngleDelta;
      am.setForegroundColor(hsv.toColor());
   }

}
