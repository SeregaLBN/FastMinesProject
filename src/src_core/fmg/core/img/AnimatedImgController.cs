using System;

namespace fmg.core.img {

   /// <summary> MVC controller. Base animation controller </summary>
   /// <typeparam name="TImage">platform specific view/image/picture or other display context/canvas/window/panel</typeparam>
   /// <typeparam name="TImageView">MVC view</typeparam>
   /// <typeparam name="TImageModel">MVC model</typeparam>
   public abstract class AnimatedImgController<TImage, TImageView, TImageModel>
                             : ImageController<TImage, TImageView, TImageModel>,
                           IAnimatedController<TImage, TImageView, TImageModel>
      where TImage      : class
      where TImageView  : IImageView<TImage, TImageModel>
      where TImageModel : IAnimatedModel
   {

      private readonly AnimatedInnerController<TImage, TImageView, TImageModel> _innerController;

      protected AnimatedImgController(TImageView imageView)
         : base(imageView)
      {
         _innerController = new AnimatedInnerController<TImage, TImageView, TImageModel>(Model);
      }

      public void AddModelTransformer(IModelTransformer transformer) {
         _innerController.AddModelTransformer(transformer);
      }
      public void RemoveModelTransformer(Type /* extends IModelTransformer */ transformerClass) {
         _innerController.RemoveModelTransformer(transformerClass);
      }

      public virtual void UseRotateTransforming(bool enable) {
         if (enable)
            AddModelTransformer(new RotateTransformer());
         else
            RemoveModelTransformer(typeof(RotateTransformer));
      }

      public virtual void UsePolarLightFgTransforming(bool enable) {
         if (enable)
            AddModelTransformer(new PolarLightFgTransformer());
         else
            RemoveModelTransformer(typeof(PolarLightFgTransformer));
      }

      protected override void Disposing() {
         _innerController.Dispose();
         base.Disposing();
      }

   }

}
