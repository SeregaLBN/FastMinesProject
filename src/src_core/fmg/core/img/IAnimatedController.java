package fmg.core.img;

/**
 * Image MVC: animate controller
 *
 * @param <TImage> plaform specific view/image/picture or other display context/canvas/window/panel
 * @param <TImageView> image view
 * @param <TImageModel> image model
 */
public interface IAnimatedController<TImage,
                                  TImageView  extends IImageView<TImage, TImageModel>,
                                  TImageModel extends IAnimatedModel>
                 extends IImageController<TImage, TImageView, TImageModel>
{

   void removeModelTransformer(Class<? extends IModelTransformer> transformerClass);
   void addModelTransformer(IModelTransformer transformer);

}
