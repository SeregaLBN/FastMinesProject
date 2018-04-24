package fmg.core.img;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

/**
 * MVC controller. Base animation controller.
 * @param <TImage> plaform specific view/image/picture or other display context/canvas/window/panel
 * @param <TImageView> MVC view
 * @param <TImageModel> MVC model
 */
public abstract class AnimatedImgController<TImage,
                                            TImageView  extends IImageView<TImage, TImageModel>,
                                            TImageModel extends IImageModel>
                extends ImageController<TImage, TImageView, TImageModel>
{

   /** Platform-dependent factory of {@link IAnimator}. Set from outside... */
   public static Supplier<IAnimator> GET_ANIMATOR;

   /** Image is animated? */
   private boolean _animated = false;
   /** Overall animation period (in milliseconds) */
   private long _animatePeriod = 3000;
   /** Total frames of the animated period */
   private int _totalFrames = 30;
   private int _currentFrame = 0;

   protected AnimatedImgController(TImageView imageView) {
      super(imageView);
   }

   public static final String PROPERTY_ANIMATED       = "Animated";
   public static final String PROPERTY_ANIMATE_PERIOD = "AnimatePeriod";
   public static final String PROPERTY_TOTAL_FRAMES   = "TotalFrames";
   public static final String PROPERTY_CURRENT_FRAME  = "CurrentFrame";

   public boolean isAnimated() { return _animated; }
   public void setAnimated(boolean value) {
      if (setProperty(_animated, value, PROPERTY_ANIMATED)) {
         //invalidate();
         if (value)
            GET_ANIMATOR.get().subscribe(this, timeFromStartSubscribe -> {
               long mod = timeFromStartSubscribe % _animatePeriod;
               long frame = mod * getTotalFrames() / _animatePeriod;
               //System.out.println("ANIMATOR : " + getClass().getSimpleName() + ": "+ timeFromStartSubscribe);
               setCurrentFrame((int)frame);
            });
         else
            GET_ANIMATOR.get().unsubscribe(this);
      }
   }

   /** Overall animation period (in milliseconds) */
   public long getAnimatePeriod() { return _animatePeriod; }
   /** Overall animation period (in milliseconds) */
   public void setAnimatePeriod(long value) {
      setProperty(_animatePeriod, value, PROPERTY_ANIMATE_PERIOD);
   }

   /** Total frames of the animated period */
   public int getTotalFrames() { return _totalFrames; }
   public void setTotalFrames(int value) {
      if (setProperty(_totalFrames, value, PROPERTY_TOTAL_FRAMES))
         setCurrentFrame(0);
   }

   protected int getCurrentFrame() { return _currentFrame; }
   protected void setCurrentFrame(int value) {
      if (setProperty(_currentFrame, value, PROPERTY_CURRENT_FRAME)) {
         _transformers.forEach((k,v) -> v.execute(_currentFrame, _totalFrames, getModel()));
         getView().invalidate();
      }
   }

   private Map<Class<? extends IModelTransformer>, IModelTransformer> _transformers = new HashMap<>();

   public void removeModelTransformer(Class<? extends IModelTransformer> transformerClass) {
      if (_transformers.keySet().contains(transformerClass))
         _transformers.remove(transformerClass);
   }
   public void addModelTransformer(IModelTransformer transformer) {
      if (!_transformers.keySet().contains(transformer.getClass()))
         _transformers.put(transformer.getClass(), transformer);
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
      setAnimated(false); // unsubscribe
      _transformers.clear();
      super.close();
   }

}
