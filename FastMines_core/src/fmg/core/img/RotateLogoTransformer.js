/** Transforming of logo rays */
class RotateLogoTransformer extends IModelTransformer {

    static get className() { return 'RotateLogoTransformer'; }
    get className() { return RotateLogoTransformer.className; }

    execute(/*IImageModel*/ model) {
        var lm = model;

        lm.rays.length = 0;
        lm.inn .length = 0;
        lm.oct .length = 0;

        var size = lm.size;
        var center = new Point(size.width/2.0, size.height/2.0);
        var ra = lm.rotateAngle;
        FigureHelper.rotateCollection(lm.rays, ra, center);
        FigureHelper.rotateCollection(lm.inn , ra, center);
        FigureHelper.rotateCollection(lm.oct , ra, center);
    }

}
