/**
 * MVC controller. Base animation controller.
 * @param <TImage> plaform specific view/image/picture or other display context/canvas/window/panel
 * @param <TImageView> MVC view
 * @param <TImageModel> MVC model
 */
class AnimatedInnerController {

    constructor(/*TImageModel*/ model) {
        this._model = model;
        this._transformers = new Map();
    }

    addModelTransformer(/*IModelTransformer*/ transformer) {
        if (!this._transformers.has(transformer.className))
            this._transformers.set(transformer.className, transformer);
    }
    removeModelTransformer(/*string*/ transformerClassName) {
        if (this._transformers.has(transformerClassName))
            this._transformers.delete(transformerClassName);
    }

    executeTransformers() {
        var self = this;
        this._transformers.forEach(function(v,k) {
            v.execute(self._model);
        });
    }

}
