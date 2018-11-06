package fmg.core.img;

import fmg.common.HSV;

/** Transforming of logo palette */
public class PolarLightLogoTransformer implements IModelTransformer {

    @Override
    public void execute(IAnimatedModel model) {
        if (!(model instanceof LogoModel))
            throw new RuntimeException("Illegal usage transformer");

        LogoModel lm = (LogoModel)model;

        if (!lm.isPolarLights())
            return;

        double rotateAngleDelta = 360.0 / lm.getTotalFrames(); // 360° / TotalFrames
        if (!lm.getAnimeDirection())
            rotateAngleDelta = -rotateAngleDelta;
        HSV[] palette = lm.getPalette();
        for (int i = 0; i < palette.length; ++i)
            palette[i].h += rotateAngleDelta;
    }

}
