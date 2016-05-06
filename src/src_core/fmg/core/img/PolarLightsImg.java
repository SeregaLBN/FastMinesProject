package fmg.core.img;

import fmg.common.HSV;

public abstract class PolarLightsImg<T, TImage extends Object> extends RotatedImg<T, TImage> {

   protected PolarLightsImg(T entity) { super(entity); }
   protected PolarLightsImg(T entity, int widthAndHeight) { super(entity, widthAndHeight); }
   protected PolarLightsImg(T entity, int widthAndHeight, Integer padding) { super(entity, widthAndHeight, padding); }

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
   protected boolean isLiveImage() {
      return isPolarLights() || super.isLiveImage();
   }

}
