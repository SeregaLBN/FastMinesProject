package fmg.core.img;

import fmg.core.types.ESkillLevel;

/**
 * MVC controller of {@link ESkillLevel} image
 *
 * @param <TImage> plaform specific view/image/picture or other display context/canvas/window/panel
 * @param <TImageView> MVC view
 */
public abstract class MosaicSkillController<TImage,
                                            TImageView extends WithBurgerMenuView<TImage, MosaicsSkillModel>>
              extends AnimatedImgController<TImage, TImageView, MosaicsSkillModel>
{

   protected MosaicSkillController(boolean showBurgerMenu, TImageView imageView) {
      super(imageView);

      getView().getBurgerMenuModel().setShow(showBurgerMenu);

    //addModelTransformer(new MosaicsSkillTransformer());
      usePolarLightFgTransforming(true);
      useRotateTransforming(true);
   }

}
