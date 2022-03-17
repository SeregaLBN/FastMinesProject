package fmg.core.img;

import fmg.core.types.EMosaicGroup;

/** MVC controller of {@link EMosaicGroup} image
 * @param <TImage> platform specific view/image/picture or other display context/canvas/window/panel
 * @param <TImageView> MVC view */
public abstract class MosaicGroupController2<TImage,
                                            TView extends IImageView2<TImage>>
    extends ImageController2<TImage, MosaicGroupModel2, TView>
{

    protected MosaicGroupController2(boolean showBurgerMenu, TImageView imageView) {
        super(imageView);

        getBurgerMenuModel().setShow(showBurgerMenu);

        addModelTransformer(new MosaicGroupTransformer());
        usePolarLightFgTransforming(true);
        useRotateTransforming(true);
    }

    public BurgerMenuModel getBurgerMenuModel() { return getView().getBurgerMenuModel(); }

}
