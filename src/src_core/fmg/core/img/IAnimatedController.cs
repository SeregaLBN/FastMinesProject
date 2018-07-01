using System;

namespace fmg.core.img {

   /// <summary> Image MVC: animate controller </summary>
   /// <typeparam name="TImage">plaform specific view/image/picture or other display context/canvas/window/panel</typeparam>
   /// <typeparam name="TImageView">image view</typeparam>
   /// <typeparam name="TImageModel">image model</typeparam>
   public interface IAnimatedController<TImage, TImageView, TImageModel>
                     : IImageController<TImage, TImageView, TImageModel>
      where TImage      : class
      where TImageView  : IImageView<TImage, TImageModel>
      where TImageModel :IAnimatedModel
   {

      void RemoveModelTransformer(Type /** extends IModelTransformer */ transformerClass);
      void AddModelTransformer(IModelTransformer transformer);

   }

}
