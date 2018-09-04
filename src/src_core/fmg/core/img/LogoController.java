package fmg.core.img;

/**
 * MVC controller of logo image
 *
 * @param <TImage> platform specific view/image/picture or other display context/canvas/window/panel
 * @param <TImageView> MVC view
 */
public class LogoController<TImage,
                            TImageView extends IImageView<TImage, LogoModel>>
      extends AnimatedImgController<TImage, TImageView, LogoModel>
{

   protected LogoController(TImageView imageView) {
      super(imageView);
   }

   @Override
   public void useRotateTransforming(boolean enable) {
      super.useRotateTransforming(enable);
      if (enable)
         addModelTransformer(new RotateLogoTransformer());
      else
         removeModelTransformer(RotateLogoTransformer.class);
   }

   @Override
   public void usePolarLightFgTransforming(boolean enable) {
      //super.usePolarLightFgTransforming(enable); // hide super implementation!
      if (enable)
         addModelTransformer(new PolarLightLogoTransformer());
      else
         removeModelTransformer(PolarLightLogoTransformer.class);
   }

}
