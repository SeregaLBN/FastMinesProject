using fmg.core.types;

namespace fmg.core.img {

   /// <summary> MVC controller of <see cref="ESkillLevel"/> image </summary>
   /// <typeparam name="TImage">plaform specific view/image/picture or other display context/canvas/window/panel</typeparam>
   /// <typeparam name="MosaicSkillModel">MVC view</typeparam>
   public abstract class MosaicSkillController<TImage, TImageView>
                       : AnimatedImgController<TImage, TImageView, MosaicSkillModel>
      where TImage : class
      where TImageView : WithBurgerMenuView<TImage, MosaicSkillModel>
   {

      protected MosaicSkillController(bool showBurgerMenu, TImageView imageView)
         : base(imageView)
      {
         View.BurgerMenuModel.Show = showBurgerMenu;

         UsePolarLightFgTransforming(true);
         UseRotateTransforming(true);
      }

   }

}
