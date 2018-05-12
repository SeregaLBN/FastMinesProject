package fmg.core.img;

import fmg.core.types.EMosaicGroup;

/**
 * MVC controller of {@link EMosaicGroup} image
 *
 * @param <TImage> plaform specific view/image/picture or other display context/canvas/window/panel
 * @param <TImageView> MVC view
 */
public abstract class MosaicsGroupController< TImage,
                                              TImageView extends WithBurgerMenuView<TImage, MosaicsGroupModel>>
                extends AnimatedImgController<TImage, TImageView, MosaicsGroupModel>
{

   protected MosaicsGroupController(boolean showBurgerMenu, TImageView imageView) {
      super(imageView);

      getView().getBurgerMenuModel().setShow(showBurgerMenu);

      addModelTransformer(new MosaicsGroupTransformer());
      usePolarLightFgTransforming(true);
      useRotateTransforming(true);
   }

}
