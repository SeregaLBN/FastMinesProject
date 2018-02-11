package fmg.core.img;

import java.util.function.Supplier;

/**
 * {@link StaticImg} with animated properties
 *
 * @param <TImage> plaform specific image
 */
public abstract class AnimatedImg<TImage> extends StaticImg<TImage> {

   /** Platform-dependent factory of {@link IAnimator}. Set from outside... */
   public static Supplier<IAnimator> GET_ANIMATOR;

//   protected AnimatedImg() { super(); }

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
               long animatePeriod = getAnimatePeriod();
               long mod = timeFromStartSubscribe % animatePeriod;
               long frame = mod * getTotalFrames() / animatePeriod;
               setCurrentFrame((int)frame);
            });
         else
            GET_ANIMATOR.get().unsubscribe(this);
      }
   }

   private long _animatePeriod = 3000;
   /** Overall animation period (in milliseconds) */
   public long getAnimatePeriod() { return _animatePeriod; }
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
   public int getCurrentFrame() { return _currentFrame; }
   protected void setCurrentFrame(int value) {
      if (setProperty(_currentFrame, value, PROPERTY_CURRENT_FRAME))
         invalidate();
   }

   @Deprecated
   public boolean isLiveImage() { return isAnimated(); }

   @Override
   public void close() {
      setAnimated(false); // unsubscribe
      super.close();
   }

}
