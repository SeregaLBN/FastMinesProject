package fmg.core.img;

/**
 * MVC controller. Base animation controller.
 * @param <TImage> plaform specific view/image/picture or other display context/canvas/window/panel
 * @param <TImageView> MVC view
 * @param <TImageModel> MVC model
 */
public abstract class AnimatedImgController<TImage,
                                            TImageView  extends IImageView<TImage, TImageModel>,
                                            TImageModel extends IAnimatedModel>
             extends        ImageController<TImage, TImageView, TImageModel>
             implements IAnimatedController<TImage, TImageView, TImageModel>
{
   private final AnimatedInnerController<TImage, TImageView, TImageModel> _innerController;

   protected AnimatedImgController(TImageView imageView) {
      super(imageView);
      _innerController = new AnimatedInnerController<>(getModel());
   }

   @Override
   public void addModelTransformer(IModelTransformer transformer) {
      _innerController.addModelTransformer(transformer);
   }
   @Override
   public void removeModelTransformer(Class<? extends IModelTransformer> transformerClass) {
      _innerController.removeModelTransformer(transformerClass);
   }

   public void useRotateTransforming(boolean enable) {
      if (enable)
         addModelTransformer(new RotateTransformer());
      else
         removeModelTransformer(RotateTransformer.class);
   }

   public void usePolarLightFgTransforming(boolean enable) {
      if (enable)
         addModelTransformer(new PolarLightFgTransformer());
      else
         removeModelTransformer(PolarLightFgTransformer.class);
   }

   @Override
   public void close() {
      _innerController.close();
      super.close();
   }

}
