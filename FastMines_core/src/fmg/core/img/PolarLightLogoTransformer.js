/** Transforming of logo palette */
class PolarLightLogoTransformer extends IModelTransformer {

    static get className() { return 'PolarLightLogoTransformer'; }
    get className() { return PolarLightLogoTransformer.className; }

    execute(/*IImageModel*/ model) {
        var lm = model;

        if (!lm.polarLights)
            return;

        var rotateAngleDelta = 360.0 / lm.totalFrames; // 360° / TotalFrames
        if (!lm.animeDirection)
            rotateAngleDelta = -rotateAngleDelta;
        var palette = lm.palette;
        for (var i = 0; i < palette.length; ++i)
            palette[i].h += rotateAngleDelta;
    }

}
