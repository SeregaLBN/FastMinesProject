using System;

namespace Fmg.Core.Img {

    /// <summary> Image MVC: animate controller </summary>
    /// <typeparam name="TImage">platform specific view/image/picture or other display context/canvas/window/panel</typeparam>
    /// <typeparam name="TImageView">image view</typeparam>
    /// <typeparam name="TImageModel">image model</typeparam>
    public interface IAnimatedController<out TImage, out TImageView, out TImageModel>
                      : IImageController<    TImage,     TImageView,     TImageModel>
        where TImage      : class
        where TImageView  : IImageView<TImage, TImageModel>
        where TImageModel : IAnimatedModel
    {

        void AddModelTransformer(IModelTransformer transformer);
        void RemoveModelTransformer(Type /** extends IModelTransformer */ transformerClass);

        void UseRotateTransforming(bool enable);

        void UsePolarLightFgTransforming(bool enable);

    }

}
