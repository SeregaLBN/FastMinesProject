package fmg.core.img;

import java.util.HashSet;
import java.util.Set;
import java.util.function.Supplier;

/**
 * {@link ImageProperties} with animated properties
 *
 * @param <TImage> plaform specific image
 */
public abstract class AAnimatedImgController<TImage,
                                             TImageView  extends IImageView<TImage, TImageModel>,
                                             TImageModel extends ImageProperties>
                extends AImageController<TImage, TImageView, TImageModel>
{

   /** Platform-dependent factory of {@link IAnimator}. Set from outside... */
   public static Supplier<IAnimator> GET_ANIMATOR;

   protected AAnimatedImgController(TImageView imageView) {
      super(imageView);
   }

   @SuppressWarnings("deprecation")
   protected <TI>boolean setProperty(TI storage, TI value, String propertyName) {
      return super.setProperty(value, propertyName);
   }

   public static final String PROPERTY_ANIMATED       = "Animated";
   public static final String PROPERTY_ANIMATE_PERIOD = "AnimatePeriod";
   public static final String PROPERTY_TOTAL_FRAMES   = "TotalFrames";
   public static final String PROPERTY_CURRENT_FRAME  = "CurrentFrame";

   private boolean _animated = false;
   public boolean isAnimated() { return _animated; }
   public void setAnimated(boolean value) {
      if (setProperty(_animated, value, PROPERTY_ANIMATED)) {
         //invalidate();
         if (value)
            GET_ANIMATOR.get().subscribe(this, timeFromStartSubscribe -> {
               long mod = timeFromStartSubscribe % _animatePeriod;
               long frame = mod * getTotalFrames() / _animatePeriod;
               setCurrentFrame((int)frame);
            });
         else
            GET_ANIMATOR.get().unsubscribe(this);
      }
   }

   private long _animatePeriod = 3000;
   /** Overall animation period (in milliseconds) */
   public long getAnimatePeriod() { return _animatePeriod; }
   /** Overall animation period (in milliseconds) */
   public void setAnimatePeriod(long value) {
      setProperty(_animatePeriod, value, PROPERTY_ANIMATE_PERIOD);
   }

   private int _totalFrames = 5;
   /** Total frames of the animated period */
   public int getTotalFrames() { return _totalFrames; }
   public void setTotalFrames(int value) {
      if (setProperty(_totalFrames, value, PROPERTY_TOTAL_FRAMES))
         setCurrentFrame(0);
   }

   private int _currentFrame = 0;
   protected int getCurrentFrame() { return _currentFrame; }
   protected void setCurrentFrame(int value) {
      if (setProperty(_currentFrame, value, PROPERTY_CURRENT_FRAME)) {
         _transformers.forEach(x -> x.execute(_currentFrame, _totalFrames, getModel()));
         getView().invalidate();
      }
   }

   private Set<IModelTransformer> _transformers = new HashSet<>();

   protected <TModelTransformer extends IModelTransformer> void useTransforming(boolean enable, Class<TModelTransformer> clazz, Supplier<TModelTransformer> newInstance) {
      if (enable) {
         if (!_transformers.stream().anyMatch(e -> e.getClass() == clazz))
            _transformers.add(newInstance.get());
      } else {
         IModelTransformer rt = _transformers.stream()
               .filter(e -> e.getClass() == clazz)
               .findAny()
               .orElse(null);
         if (rt != null)
            _transformers.remove(rt);
      }
   }

   public void useRotateTransforming(boolean enable) {
      useTransforming(enable, RotateTransformer.class, () -> new RotateTransformer());
   }

   public void usePolarLightTransforming(boolean enable) {
      useTransforming(enable, PolarLightFgTransformer.class, () -> new PolarLightFgTransformer());
   }

   @Override
   public void close() {
      setAnimated(false); // unsubscribe
      _transformers.clear();
      super.close();
   }

}
