package fmg.core.img;

import fmg.core.types.ESkillLevel;

/**
 * MVC controller of {@link ESkillLevel} image
 *
 * @param <TImage> platform specific view/image/picture or other display context/canvas/window/panel
 * @param <TImageView> MVC view
 */
public abstract class MosaicSkillController<TImage,
                                            TImageView extends WithBurgerMenuView<TImage, MosaicSkillModel>>
              extends AnimatedImgController<TImage, TImageView, MosaicSkillModel>
{

   protected MosaicSkillController(boolean showBurgerMenu, TImageView imageView) {
      super(imageView);

      getView().getBurgerMenuModel().setShow(showBurgerMenu);

      usePolarLightFgTransforming(true);
      useRotateTransforming(true);
   }

}
