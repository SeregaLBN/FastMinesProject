package fmg.core.img;

/**
 * Image MVC: animate controller
 *
 * @param <TImage> platform specific view/image/picture or other display context/canvas/window/panel
 * @param <TImageView> image view
 * @param <TImageModel> image model
 */
public interface IAnimatedController<TImage,
                                     TImageView  extends IImageView<TImage, TImageModel>,
                                     TImageModel extends IAnimatedModel>
                 extends IImageController<TImage, TImageView, TImageModel>
{

    void addModelTransformer(IModelTransformer transformer);
    void removeModelTransformer(Class<? extends IModelTransformer> transformerClass);

    void useRotateTransforming(boolean enable);
    void usePolarLightFgTransforming(boolean enable);

}
