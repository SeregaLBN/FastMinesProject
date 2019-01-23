package fmg.core.img;

import java.beans.PropertyChangeEvent;

/**
 * MVC: view of images with burger menu (where burger menu its secondary model)
 * @param <TImage> - platform specific image
 * @param <TImageModel> - general model of image (not burger menu model)
 */
public abstract class WithBurgerMenuView<TImage, TImageModel extends AnimatedImageModel> extends ImageView<TImage, TImageModel> {

    /** the second model of image */
    private final BurgerMenuModel _burgerMenuModel;

    protected WithBurgerMenuView(TImageModel imageModel) {
        super(imageModel);
        _burgerMenuModel = new BurgerMenuModel(imageModel);
        _burgerMenuModel.addListener(this::onPropertyBurgerMenuModelChanged);
    }

    public BurgerMenuModel getBurgerMenuModel() { return _burgerMenuModel; }

    protected void onPropertyBurgerMenuModelChanged(PropertyChangeEvent ev) {
        assert ev.getSource() == _burgerMenuModel; // by reference
        invalidate();
    }

    @Override
    public void close() {
        _burgerMenuModel.removeListener(this::onPropertyBurgerMenuModelChanged);
        _burgerMenuModel.close();
        super.close();
    }

}
