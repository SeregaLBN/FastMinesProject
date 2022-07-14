package fmg.core.img;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

/**
 * MVC: view of images with burger menu (where burger menu its secondary model)
 * @param <TImage> - platform specific image
 * @param <TImageModel> - general model of image (not burger menu model)
 */
@Deprecated
public abstract class WithBurgerMenuView<TImage, TImageModel extends AnimatedImageModel> extends ImageView<TImage, TImageModel> {

    /** the second model of image */
    private final BurgerMenuModel burgerMenuModel;
    private final PropertyChangeListener onPropertyBurgerMenuModelChangedListener = this::onPropertyBurgerMenuModelChanged;

    protected WithBurgerMenuView(TImageModel imageModel) {
        super(imageModel);
        this.burgerMenuModel = new BurgerMenuModel(imageModel);
        this.burgerMenuModel.addListener(onPropertyBurgerMenuModelChangedListener);
    }

    public BurgerMenuModel getBurgerMenuModel() { return burgerMenuModel; }

    protected void onPropertyBurgerMenuModelChanged(PropertyChangeEvent ev) {
        assert ev.getSource() == burgerMenuModel; // by reference
        invalidate();
    }

    @Override
    public void close() {
        burgerMenuModel.removeListener(onPropertyBurgerMenuModelChangedListener);
        burgerMenuModel.close();
        super.close();
    }

}
