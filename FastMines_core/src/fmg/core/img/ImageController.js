/**
 * Image MVC: controller
 * Base implementation of image controller (manipulations with the image).
 *
 * @param <TImage> plaform specific view/image/picture or other display context/canvas/window/panel
 * @param <TImageView> image view
 * @param <TImageModel> image model
 */
class ImageController {

    constructor(/*TImageView*/ imageView) {
        /** MVC: view */
        this._imageView = imageView;
    }

    get View()  { return this._imageView; }
    get Model() { return this.View.Model; }
    get image() { return this.View.image; }
    get size()  { return this.View.size; }

}
