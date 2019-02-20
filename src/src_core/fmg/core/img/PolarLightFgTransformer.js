/** Transforming of foreground color (rotation of foreground color) */
class PolarLightFgTransformer extends IModelTransformer {

    static get className() { return 'PolarLightFgTransformer'; }
    get className() { return PolarLightFgTransformer.className; }

    execute(/*IImageModel*/ model) {
        var am = model;
        if (!am.polarLights)
            return;

        var rotateAngleDelta = 360.0 / am.totalFrames; // 360° / TotalFrames
        if (!am.animeDirection)
            rotateAngleDelta = -rotateAngleDelta;

        var hsv = new HSV(am.foregroundColor);
        hsv.h += rotateAngleDelta;
        am.foregroundColor = hsv.toColor();
    }

}
