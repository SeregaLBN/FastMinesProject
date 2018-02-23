package fmg.core.img;

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
      useTransforming(enable, RotateLogoTransformer.class, () -> new RotateLogoTransformer());
   }

   @Override
   public void usePolarLightTransforming(boolean enable) {
      useTransforming(enable, PolarLightLogoTransformer.class, () -> new PolarLightLogoTransformer());
   }

}
