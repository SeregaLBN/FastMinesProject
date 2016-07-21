package fmg.core.img;

import fmg.common.HSV;

/**
 * {@link RotatedImg} with hue rotation effect.
 * Alters the color of an image by rotating its hue values.
 *
 * @param <TImage> plaform specific image
 */
public abstract class PolarLightsImg<TImage> extends RotatedImg<TImage> {

//   protected PolarLightsImg() { super(); }

   public static final String PROPERTY_POLAR_LIGHTS = "PolarLights";

   private boolean _polarLights;
   /** shimmering filling */
   public boolean isPolarLights() { return _polarLights; }
   public void setPolarLights(boolean value) {
      if (setProperty(_polarLights, value, PROPERTY_POLAR_LIGHTS)) {
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
