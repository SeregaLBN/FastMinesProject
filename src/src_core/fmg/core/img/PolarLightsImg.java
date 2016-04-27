package fmg.core.img;

import java.util.Random;
import java.util.UUID;
import java.util.function.Function;

import fmg.common.Color;

public abstract class PolarLightsImg<T, TImage extends Object> extends RotatedImg<T, TImage> {

   protected PolarLightsImg(T entity) {
      super(entity);
   }
   protected PolarLightsImg(T entity, int widthAndHeight) {
      super(entity, widthAndHeight);
   }
   protected PolarLightsImg(T entity, int widthAndHeight, Integer padding) {
      super(entity, widthAndHeight, padding);
   }

   private boolean _polarLights;
   /** shimmering filling */
   public boolean isPolarLights() { return _polarLights; }
   public void setPolarLights(boolean value) {
      if (setProperty(_polarLights, value, "PolarLights"))
         invalidate();
   }

   private final Random _random = new Random(UUID.randomUUID().hashCode());

   private void NextForegroundColor() {
      if (isPolarLights()) {
         Function<Byte, Byte> funcAddRandomBit = val -> (byte) ((((_random.nextInt() & 1) == 1) ? 0x00 : 0x80) | (val >> 1));
         Color f = getForegroundColor();
         switch (_random.nextInt() % 3) {
         case 0: f.setR( funcAddRandomBit.apply(f.getR()) ); break;
         case 1: f.setG( funcAddRandomBit.apply(f.getG()) ); break;
         case 2: f.setB( funcAddRandomBit.apply(f.getB()) ); break;
         }
         setForegroundColor(f);
      }
   }

   @Override
   protected void onTimer() {
      NextForegroundColor();
      super.onTimer();
   }

   @Override
   protected boolean isLiveImage() {
      return isPolarLights() || super.isLiveImage();
   }

}
