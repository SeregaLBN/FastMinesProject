using System;

namespace fmg.core.img {

   /// <summary> MVC controller. Base animation controller </summary>
   /// <typeparam name="TImage">plaform specific view/image/picture or other display context/canvas/window/panel</typeparam>
   /// <typeparam name="TImageView">MVC view</typeparam>
   /// <typeparam name="TImageModel">MVC model</typeparam>
   public abstract class AnimatedImgController<TImage, TImageView, TImageModel>
                             : ImageController<TImage, TImageView, TImageModel>
      where TImageView  : IImageView<TImage, TImageModel>
      where TImageModel : IImageModel
   {

      /// <summary> Image is animated? </summary>
      private bool? _animated = null;
      /// <summary> Overall animation period (in milliseconds) </summary>
      private long _animatePeriod = 3000;
      /// <summary> Total frames of the animated period </summary>
      private int _totalFrames = 30;
      private int _currentFrame = 0;

      private IDictionary<Type, IModelTransformer> _transformers = new Dictionary<Type, IModelTransformer>();

      protected AnimatedImgController(TImageView imageView)
         : base(imageView)
      { }

      public bool Animated {
         get { return (_animated == true); }
         set {
            if (_notifier.SetProperty(ref _animated, value)) {
               if (value)
                  Factory.GET_ANIMATOR().Subscribe(this, timeFromStartSubscribe => {
                     long mod = timeFromStartSubscribe % _animatePeriod;
                     long frame = mod * getTotalFrames() / _animatePeriod;
                     //System.out.println("ANIMATOR : " + getClass().getSimpleName() + ": "+ timeFromStartSubscribe);
                     CurrentFrame = (int)frame;
                  });
               else
                  Factory.GET_ANIMATOR().Pause(this);
            }
         }
      }

      /// <summary> Overall animation period (in milliseconds) </summary>
      public long AnimatePeriod {
         get { return _animatePeriod; }
         set { _notifier.SetProperty(ref _animatePeriod, value); }
      }

      /// <summary> Total frames of the animated period </summary>
      public int TotalFrames {
         get { return _totalFrames; }
         set {
            if (_notifier.SetProperty(ref _totalFrames, value))
               CurrentFrame = 0;
         }
      }

      protected int CurrentFrame {
         get { return _currentFrame; }
         get {
            if (_notifier.SetProperty(ref _currentFrame, value)) {
               _transformers.forEach((k,v) -> v.execute(_currentFrame, _totalFrames, getModel()));
               getView().invalidate();
            }
         }
      }

      public void RemoveModelTransformer(Type /* extends IModelTransformer */ transformerClass) {
         if (_transformers.ContainsKey(transformerClass))
            _transformers.Remove(transformerClass);
      }
      public void AddModelTransformer(IModelTransformer transformer) {
         if (!_transformers.ContainsKey(transformer.GetType()))
            _transformers.Add(transformer.GetType(), transformer);
      }

      public void UseRotateTransforming(bool enable) {
         if (enable)
            addModelTransformer(new RotateTransformer());
         else
            removeModelTransformer(typeof(RotateTransformer));
      }

      public void usePolarLightFgTransforming(bool enable) {
         if (enable)
            addModelTransformer(new PolarLightFgTransformer());
         else
            removeModelTransformer(typeof(PolarLightFgTransformer));
      }

      protected override void Disposing() {
         if (_animated != null)
            GET_ANIMATOR().Unsubscribe(this);
         _transformers.clear();
         base.Disposing();
      }

   }

}
