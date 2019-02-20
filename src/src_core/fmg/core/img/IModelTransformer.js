/** Transforming image model data. Usage for image animations. */
class IModelTransformer {

    get className() { throw new Error('Abstract method'); }

    /** The handler for the frame change event */
    execute(/*IImageModel*/ model) { throw new Error('Abstract method'); }

}
