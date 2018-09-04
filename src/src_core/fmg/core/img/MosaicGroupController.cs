namespace fmg.core.img {

   /// <summary> MVC controller of <see cref="EMosaicGroup"/> image </summary>
   /// <typeparam name="TImage">platform specific view/image/picture or other display context/canvas/window/panel</typeparam>
   /// <typeparam name="MosaicGroupModel">MVC view</typeparam>
   public abstract class MosaicGroupController<TImage, TImageView>
                       : AnimatedImgController<TImage, TImageView, MosaicGroupModel>
      where TImage : class
      where TImageView : WithBurgerMenuView<TImage, MosaicGroupModel>
   {

      protected MosaicGroupController(bool showBurgerMenu, TImageView imageView)
         : base(imageView)
      {
         View.BurgerMenuModel.Show = showBurgerMenu;

         AddModelTransformer(new MosaicGroupTransformer());
         UsePolarLightFgTransforming(true);
         UseRotateTransforming(true);
      }

   }

}
