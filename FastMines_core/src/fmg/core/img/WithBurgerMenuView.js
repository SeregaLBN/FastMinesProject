/**
 * MVC: view of images with burger menu (where burger menu its secondary model)
 * @param <TImage> - platform specific image
 * @param <TImageModel> - general model of image (not burger menu model)
 */
class WithBurgerMenuView extends ImageView {

    constructor(/*TImageModel*/ imageModel) {
        super(imageModel);
        this._burgerMenuModel = new BurgerMenuModel(imageModel);
    }

    get burgerMenuModel() { return this._burgerMenuModel; }

}
