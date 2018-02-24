package fmg.core.img;

import fmg.common.HSV;

/** Transforming of foreground color (rotation of background color) */
public class PolarLightBkTransformer implements IModelTransformer {

   @Override
   public void execute(int currentFrame, int totalFrames, IImageModel model) {
      if (!(model instanceof ImageProperties))
         throw new RuntimeException("Illegal usage transformer");

      ImageProperties ip = (ImageProperties)model;

      double rotateAngleDelta = 360.0 / totalFrames; // 360° / TotalFrames

      HSV hsv = new HSV(ip.getBackgroundColor());
      hsv.h += rotateAngleDelta;
      ip.setBackgroundColor(hsv.toColor());
   }

}
