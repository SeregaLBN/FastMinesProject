/**
 * MVC controller. Base animation controller.
 * @param <TImage> plaform specific view/image/picture or other display context/canvas/window/panel
 * @param <TImageView> MVC view
 * @param <TImageModel> MVC model
 */
class AnimatedImgController extends ImageController {

    constructor(/*TImageView*/ imageView) {
        super(imageView);
        this._innerController = new AnimatedInnerController(this.Model);
    }

    addModelTransformer(/*IModelTransformer*/ transformer) {
        this._innerController.addModelTransformer(transformer);
    }
    removeModelTransformer(/*string*/ transformerClassName) {
        this._innerController.removeModelTransformer(transformerClassName);
    }
    executeTransformers() {
        this._innerController.executeTransformers();
    }

    useRotateTransforming(/*bool*/ enable) {
        if (enable)
            this.addModelTransformer(new RotateTransformer());
        else
            this.removeModelTransformer(RotateTransformer.className);
    }

    usePolarLightFgTransforming(/*bool*/ enable) {
        if (enable)
            this.addModelTransformer(new PolarLightFgTransformer());
        else
            this.removeModelTransformer(PolarLightFgTransformer.className);
    }

}
