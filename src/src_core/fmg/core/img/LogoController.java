package fmg.core.img;

/**
 * MVC controlle of logo image
 *
 * @param <TImage> plaform specific image
 * @param <TImageView> MVC view
 */
public class LogoController<TImage,
                            TImageView extends IImageView<TImage, LogoModel>>
      extends AAnimatedImgController<TImage, TImageView, LogoModel>
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
   public void usePolarLightTransforming(boolean enable) {
      if (enable)
         addModelTransformer(new PolarLightLogoTransformer());
      else
         removeModelTransformer(PolarLightLogoTransformer.class);
   }

}
