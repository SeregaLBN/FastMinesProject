package fmg.core.img;

import java.util.List;
import java.util.Random;
import java.util.function.Function;

import fmg.common.Color;
import fmg.common.HSV;
import fmg.common.Pair;
import fmg.common.geom.PointDouble;
import fmg.common.geom.Rect;
import fmg.common.geom.Size;

public interface ITestDrawing {

   Random getRandom();
   default int r(int max) { return getRandom().nextInt(max); }
   default boolean bl() { return getRandom().nextBoolean(); } // random bool
   default int np() { return (bl() ? -1 : +1); } // negative or positive

   default void applyRandom(StaticImg<?> img, boolean testTransparent) {
      if (img instanceof RotatedImg) {
         RotatedImg<?> rImg = (RotatedImg<?>)img;
         rImg.setRotate(true);
         rImg.setRotateAngleDelta((3 + r(5)) * np());
         rImg.setRedrawInterval(50);
         rImg.setBorderWidth(bl() ? 1 : 2);
         rImg.setPadding(4);
      }

      if (img instanceof PolarLightsImg) {
         PolarLightsImg<?> plImg = (PolarLightsImg<?>)img;
         plImg.setPolarLights(true);
      }

      if (img instanceof ALogo) {
         ALogo<?> logoImg = (ALogo<?>)img;
         logoImg.setRotateMode(ALogo.ERotateMode.values()[r(ALogo.ERotateMode.values().length)]);
         logoImg.setUseGradient(bl());
      }

      if (img instanceof AMosaicsImg) {
         AMosaicsImg<?,?,?,?> mosaicsImg = (AMosaicsImg<?,?,?,?>)img;
         mosaicsImg.setRotateMode(AMosaicsImg.ERotateMode.values()[r(AMosaicsImg.ERotateMode.values().length)]);
      }

      if (testTransparent || bl()) {
         // test transparent
         HSV bkClr = new HSV(Color.RandomColor(getRandom()));
         bkClr.a = 50 + r(10);
         img.addListener(ev -> {
            if (RotatedImg.PROPERTY_ROTATE_ANGLE.equals(ev.getPropertyName())) {
               bkClr.h = img.getRotateAngle();
               img.setBackgroundColor(bkClr.toColor());
            }
         });
      } else {
         img.setBackgroundColor(Color.RandomColor(getRandom()).brighter());
      }
   }

   default Pair<Size, // image size
               Function<? /* image */, PointDouble /* image offset */>> // Stream mapper
           cellTiling(Rect rc, List<?> images, boolean testTransparent)
   {
      int len = images.size();
      int cols = (int)Math.round( Math.sqrt(len)  + 0.4999999999); // columns
      int rows = (int)Math.round(len/(double)cols + 0.4999999999);
      double dx = rc.width  / (double)cols; // cell tile width
      double dy = rc.height / (double)rows; // cell tile height

      int pad = 2; // cell padding
      double addonX = (cols==1) ? 0 : !testTransparent ? 0 : dx/4; // test intersection
      double addonY = (rows==1) ? 0 : !testTransparent ? 0 : dy/4; // test intersection
      Size imgSize = new Size((int)(dx - 2*pad + addonX),  // dx - 2*pad;
                              (int)(dy - 2*pad + addonY)); // dy - 2*pad;

      Function<? /* image */, PointDouble /* image offset */> mapper = item -> {
         int pos = images.indexOf(item);
         if (pos == -1)
            throw new RuntimeException("Illegal usage...");

         int i = pos / rows;
         int j = pos % rows;
         PointDouble offset = new PointDouble(rc.x + i*dx + pad,
                                              rc.y + j*dy + pad);
         if (i == (cols-1))
            offset.x -= addonX;
         if (j == (rows-1))
            offset.y -= addonY;

         return offset;
      };

      return new Pair<>(imgSize, mapper);
   }

}
