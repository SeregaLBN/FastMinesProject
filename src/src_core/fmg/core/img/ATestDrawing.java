package fmg.core.img;

import java.util.List;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import fmg.common.Color;
import fmg.common.HSV;
import fmg.common.geom.PointDouble;
import fmg.common.geom.RectDouble;
import fmg.common.geom.Size;

public abstract class ATestDrawing {

   private final String titlePrefix;

   protected ATestDrawing(String titlePrefix) {
      this.titlePrefix = titlePrefix;
   }

   public Random getRandom() { return ThreadLocalRandom.current(); }
   public int r(int max) { return getRandom().nextInt(max); }
   public boolean bl() { return getRandom().nextBoolean(); } // random bool
   public int np() { return (bl() ? -1 : +1); } // negative or positive

   public void applyRandom(AImageController<?,?,?> cntrller, boolean testTransparent) {
      testTransparent = testTransparent || bl();

      if (cntrller instanceof AAnimatedImgController) {
         AAnimatedImgController<?,?,?> aCtrller = (AAnimatedImgController<?,?,?>)cntrller;
         aCtrller.setAnimated(bl() && bl()); // 25%
         aCtrller.setAnimatePeriod((1000 + r(2000)) * np());
         aCtrller.setTotalFrames(3 + r(50));

         aCtrller.usePolarLightTransforming(bl());
         aCtrller.useRotateTransforming(bl());

         if (testTransparent) {
            HSV bkClr = new HSV(Color.RandomColor(getRandom()));
            bkClr.a = 50 + r(10);
            double rotateAngleDelta = 360.0 / aCtrller.getTotalFrames(); // 360° / TotalFrames
            cntrller.addListener(ev -> {
               if (AAnimatedImgController.PROPERTY_CURRENT_FRAME.equals(ev.getPropertyName())) {
                  bkClr.h += rotateAngleDelta;
                  aCtrller.getModel().setBackgroundColor(bkClr.toColor());
               }
            });
         }
      }

      IImageModel model = cntrller.getModel();
      if (model instanceof ImageProperties) {
         @SuppressWarnings("resource")
         ImageProperties ip = (ImageProperties)model;
         ip.setBorderWidth(r(3));
         ip.setPadding(4);

         if (testTransparent) {
            // test transparent
            if ((ip.getBorderWidth() != 0) && (r(4) == 0)) {
               ip.setForegroundColor(Color.Transparent);
            } else {
               Color clr = ip.getForegroundColor();
               clr.setA(150 + r(255-150));
               ip.setForegroundColor(clr);
            }
         } else {
            ip.setBackgroundColor(Color.RandomColor(getRandom()).brighter());
         }
      }
      if (model instanceof LogoModel) {
         @SuppressWarnings("resource")
         LogoModel lm = (LogoModel)model;
         lm.setUseGradient(bl());
      }

//      if (img instanceof AMosaicsImg) {
//         AMosaicsImg<?> mosaicsImg = (AMosaicsImg<?>)img;
//         mosaicsImg.setRotateMode(AMosaicsImg.ERotateMode.values()[r(AMosaicsImg.ERotateMode.values().length)]);
//      }
   }

   public static class CellTilingInfo {
      /** index of column */
      public int i;
      /** index of row */
      public int j;
      public PointDouble imageOffset;
   }

   public static class CellTilingResult {
      public Size imageSize;
      public Size tableSize;
      public Function<? /* image */, CellTilingInfo> itemCallback;
   }

   public CellTilingResult cellTiling(RectDouble rc, List<?> images, boolean testTransparent)
   {
      int len = images.size();
      int cols = (int)Math.round( Math.sqrt(len)  + 0.4999999999); // columns
      int rows = (int)Math.round(len/(double)cols + 0.4999999999);
      double dx = rc.width  / cols; // cell tile width
      double dy = rc.height / rows; // cell tile height

      int pad = 2; // cell padding
      double addonX = (cols==1) ? 0 : !testTransparent ? 0 : dx/4; // test intersection
      double addonY = (rows==1) ? 0 : !testTransparent ? 0 : dy/4; // test intersection
      Size imgSize = new Size((int)(dx - 2*pad + addonX),  // dx - 2*pad;
                              (int)(dy - 2*pad + addonY)); // dy - 2*pad;

      Function<? /* image */, CellTilingInfo> itemCallback = item -> {
         if (item instanceof BurgerMenuImg) {
            BurgerMenuImg<?> brgrImg = (BurgerMenuImg<?>)item;
            brgrImg.resetPaddingBurgerMenu();
         }

         int pos = images.indexOf(item);
         if (pos == -1)
            throw new RuntimeException("Illegal usage...");

         int i = pos % cols;
         int j = pos / cols;
         PointDouble offset = new PointDouble(rc.x + i*dx + pad,
                                              rc.y + j*dy + pad);
         if (i == (cols-1))
            offset.x -= addonX;
         if (j == (rows-1))
            offset.y -= addonY;

         CellTilingInfo cti = new CellTilingInfo();
         cti.i = i;
         cti.j = j;
         cti.imageOffset = offset;
         return cti;
      };

      CellTilingResult ctr = new CellTilingResult();
      ctr.imageSize = imgSize;
      ctr.tableSize = new Size(cols, rows);
      ctr.itemCallback = itemCallback;
      return ctr;
   }

   public String getTitle(List<?> images) {
      return titlePrefix + " test paints: " + images.stream()
         .map(i -> i.getClass().getName())
         .map(n -> Stream.of(n.split("\\.")).reduce((first, second) -> second).get().replaceAll("\\$", ".") )
         .collect(Collectors.groupingBy(z -> z))
         .entrySet().stream()
         .map(x -> x.getKey())
         .collect(Collectors.joining(" & "));
   }

}
