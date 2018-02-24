package fmg.core.img;

import fmg.common.HSV;

/** Transforming of foreground color (rotation of foreground color) */
public class PolarLightFgTransformer implements IModelTransformer {

   @Override
   public void execute(int currentFrame, int totalFrames, IImageModel model) {
      if (!(model instanceof ImageProperties))
         throw new RuntimeException("Illegal usage transformer");

      ImageProperties ip = (ImageProperties)model;

      double rotateAngleDelta = 360.0 / totalFrames; // 360° / TotalFrames

      HSV hsv = new HSV(ip.getForegroundColor());
      hsv.h += rotateAngleDelta;
      ip.setForegroundColor(hsv.toColor());
   }

}
