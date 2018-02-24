package fmg.core.img;

import fmg.common.HSV;

/** Transforming of logo palette */
public class PolarLightLogoTransformer implements IModelTransformer {

   @Override
   public void execute(int currentFrame, int totalFrames, IImageModel model) {
      if (!(model instanceof LogoModel))
         throw new RuntimeException("Illegal usage transformer");

      LogoModel lm = (LogoModel)model;

      double rotateAngleDelta = 360.0 / totalFrames; // 360° / TotalFrames
      HSV[] palette = lm.getPalette();
      for (int i = 0; i < palette.length; ++i)
         palette[i].h += rotateAngleDelta;
   }

}
