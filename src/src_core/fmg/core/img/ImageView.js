/**
 * MVC: view.
 * Base implementation of image view.
 *
 * @param <TImage> plaform specific view/image/picture or other display context/canvas/window/panel
 * @param <TImageModel> model data for display
 **/
class ImageView {

    static get /* enum */ EInvalidate() {
        return {
            needRedraw: { ordinal: 0 },
            redrawing : { ordinal: 1 },
            redrawed  : { ordinal: 2 }
        };
    }

    /** @param imageModel base of #ImageModel */
    constructor(/*TImageModel*/ imageModel) {
        this._imageModel = imageModel;
        this._image = null;
        this._invalidate = ImageView.EInvalidate.needRedraw;
    }

    get Model() { return this._imageModel; }

    /** width and height in pixel */
    get size() { return this.Model.size; }
    set size(/*double or Size*/ value) { this.Model.size = value; }

    createImage() { throw new Error('Abstract method'); return null; }

    get image() {
        if (this._image == null) {
            this.image = this.createImage();
            this._invalidate = ImageView.EInvalidate.needRedraw;
        }
        if (this._invalidate.ordinal == ImageView.EInvalidate.needRedraw.ordinal)
            this.draw();
        return this._image;
    }
    set image(/*TImage*/ value) {
        this._image = value;
    }

    invalidate() {
        if (this._invalidate.ordinal == ImageView.EInvalidate.redrawing.ordinal)
            return;
        //if (this._invalidate.ordinal == ImageView.EInvalidate.needRedraw.ordinal)
        //   return;
        this._invalidate = ImageView.EInvalidate.needRedraw;
    }

    draw() {
        this.drawBegin();
        this.drawBody();
        this.drawEnd();
    }

    drawBegin() { this._invalidate = ImageView.EInvalidate.redrawing; }
    drawBody()  { throw new Error('Abstract method'); }
    drawEnd()   { this._invalidate = ImageView.EInvalidate.redrawed; }

}
