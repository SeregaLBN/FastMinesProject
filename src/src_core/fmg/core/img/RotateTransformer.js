/** Transforming of rotate angle */
class RotateTransformer extends IModelTransformer {

    static get className() { return 'RotateTransformer'; }
    get className() { return RotateTransformer.className; }

    execute(/*IImageModel*/ model) {
        var am = model;

        var rotateAngleDelta = 360.0 / am.totalFrames; // 360° / TotalFrames
        if (!am.animeDirection)
            rotateAngleDelta = -rotateAngleDelta;
        var rotateAngle = am.currentFrame * rotateAngleDelta;
        am.rotateAngle = rotateAngle;
    }

}
