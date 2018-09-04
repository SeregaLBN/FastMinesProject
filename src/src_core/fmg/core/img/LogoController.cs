namespace fmg.core.img {

   /// <summary> MVC controlle of logo image </summary>
   /// <typeparam name="TImage">platform specific view/image/picture or other display context/canvas/window/panel</typeparam>
   /// <typeparam name="TImageView">MVC view</typeparam>
   public class LogoController<TImage, TImageView>
       : AnimatedImgController<TImage, TImageView, LogoModel>
      where TImage : class
      where TImageView : IImageView<TImage, LogoModel>
   {

      protected LogoController(TImageView imageView)
         : base(imageView)
      { }

      public override void UseRotateTransforming(bool enable) {
         base.UseRotateTransforming(enable);
         if (enable)
            AddModelTransformer(new RotateLogoTransformer());
         else
            RemoveModelTransformer(typeof(RotateLogoTransformer));
      }

      public override void UsePolarLightFgTransforming(bool enable) {
         //base.UsePolarLightFgTransforming(enable); // hide super implementation!
         if (enable)
            AddModelTransformer(new PolarLightLogoTransformer());
         else
            RemoveModelTransformer(typeof(PolarLightLogoTransformer));
      }

   }

}
