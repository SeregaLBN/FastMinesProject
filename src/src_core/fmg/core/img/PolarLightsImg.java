package fmg.core.img;

import fmg.common.HSV;

/**
 * {@link RotatedImg} with hue rotation effect.
 * Alters the color of an image by rotating its hue values.
 *
 * @param <T> the entity of image
 * @param <TImage> plaform specific image
 */
public abstract class PolarLightsImg<T, TImage> extends RotatedImg<T, TImage> {

   protected PolarLightsImg(T entity) { super(entity); }

   private boolean _polarLights;
   /** shimmering filling */
   public boolean isPolarLights() { return _polarLights; }
   public void setPolarLights(boolean value) {
      if (setProperty(_polarLights, value, "PolarLights")) {
         if (value)
            startTimer();
         else
            stopTimer();
      }
   }

   private void nextForegroundColor() {
      HSV hsv = new HSV(getForegroundColor());
      hsv.h += getRotateAngleDelta();
      setForegroundColor(hsv.toColor());
   }

   @Override
   protected void onTimer() {
      if (isPolarLights())
         nextForegroundColor();
      super.onTimer();
   }

   @Override
   public boolean isLiveImage() {
      return isPolarLights() || super.isLiveImage();
   }

}
