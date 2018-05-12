package fmg.core.img;

import fmg.core.types.EMosaicGroup;

/**
 * MVC controller of {@link EMosaicGroup} image
 *
 * @param <TImage> plaform specific view/image/picture or other display context/canvas/window/panel
 * @param <TImageView> MVC view
 */
public abstract class MosaicGroupController< TImage,
                                              TImageView extends WithBurgerMenuView<TImage, MosaicGroupModel>>
                extends AnimatedImgController<TImage, TImageView, MosaicGroupModel>
{

   protected MosaicGroupController(boolean showBurgerMenu, TImageView imageView) {
      super(imageView);

      getView().getBurgerMenuModel().setShow(showBurgerMenu);

      addModelTransformer(new MosaicGroupTransformer());
      usePolarLightFgTransforming(true);
      useRotateTransforming(true);
   }

}
