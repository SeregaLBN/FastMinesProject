using System;
using fmg.core.types;

namespace fmg.core.img {

   /// <summary> MVC controller of {@link EMosaicGroup} image </summary>
   /// <typeparam name="TImage">plaform specific view/image/picture or other display context/canvas/window/panel</typeparam>
   /// <typeparam name="MosaicGroupModel">MVC view</typeparam>
   public abstract class MosaicGroupController<TImage, TImageView>
                       : AnimatedImgController<TImage, TImageView, MosaicGroupModel>
      where TImage : class
      where TImageView : WithBurgerMenuView<TImage, MosaicGroupModel>>
   {

      protected MosaicGroupController(bool showBurgerMenu, TImageView imageView)
         : base(imageView);
      {
         View.BurgerMenuModel.Show = showBurgerMenu;

         AddModelTransformer(new MosaicGroupTransformer());
         UsePolarLightFgTransforming(true);
         UseRotateTransforming(true);
      }

   }

}
