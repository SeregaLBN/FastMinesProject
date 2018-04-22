package fmg.core.img;

import java.util.List;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import fmg.common.Color;
import fmg.common.geom.PointDouble;
import fmg.common.geom.RectDouble;
import fmg.common.geom.Size;
import fmg.core.img.MosaicsAnimatedModel.ERotateMode;
import fmg.core.mosaic.draw.MosaicDrawModel;

public abstract class ATestDrawing {

   private final String titlePrefix;

   protected ATestDrawing(String titlePrefix) {
      this.titlePrefix = titlePrefix;
   }

   public Random getRandom() { return ThreadLocalRandom.current(); }
   public int r(int max) { return getRandom().nextInt(max); }
   public boolean bl() { return getRandom().nextBoolean(); } // random bool
   public int np() { return (bl() ? -1 : +1); } // negative or positive

   public void applyRandom(ImageController<?,?,?> ctrller, boolean testTransparent) {
      testTransparent = testTransparent || bl();

      if (ctrller instanceof AnimatedImgController) {
         AnimatedImgController<?,?,?> aCtrller = (AnimatedImgController<?,?,?>)ctrller;
         aCtrller.setAnimated(bl() || bl());
         if (aCtrller.isAnimated()) {
            aCtrller.setAnimatePeriod(1000 + r(2000));
            aCtrller.setTotalFrames(40 + r(20));

            IImageModel im = aCtrller.getModel();
            if (im instanceof AnimatedImageModel) {
               aCtrller.usePolarLightTransforming(bl());
               aCtrller.useRotateTransforming(bl());
            }

            if (testTransparent) {
               // Rotate the transparent background color
               Color clr = Color.RandomColor();
               clr.setA(50 + r(10));
               if (im instanceof ImageProperties)
                  ((ImageProperties)im).setBackgroundColor(clr);
               else if (im instanceof MosaicDrawModel<?>)
                  ((MosaicDrawModel<?>)im).setBackgroundColor(clr);
               if (im instanceof AnimatedImageModel)
                  aCtrller.addModelTransformer(new PolarLightBkTransformer());
            }
         }
      }

      IImageModel model = ctrller.getModel();
      if (model instanceof ImageProperties) {
         @SuppressWarnings("resource")
         ImageProperties ip = (ImageProperties)model;

         ip.setBorderWidth(r(3));

         int pad = Math.min(ip.getSize().height/3, ip.getSize().width/3);
         ip.setPadding(-pad/4 + r(pad));

         if (testTransparent) {
            // test transparent
            Color clr = ip.getForegroundColor();
            if ((ip.getBorderWidth() != 0) && (r(4) == 0)) {
               clr.setA(Color.Transparent.getA());
            } else {
               clr.setA(150 + r(255-150));
            }
            ip.setForegroundColor(clr);
         } else {
            ip.setBackgroundColor(Color.RandomColor().brighter());
         }
      }
      if (model instanceof AnimatedImageModel) {
         @SuppressWarnings("resource")
         AnimatedImageModel aim = (AnimatedImageModel)model;
         aim.setPolarLights(bl());
         aim.setAnimeDirection(bl());
      }
      if (model instanceof LogoModel) {
         @SuppressWarnings("resource")
         LogoModel lm = (LogoModel)model;
         lm.setUseGradient(bl());
      }
      if (model instanceof MosaicsAnimatedModel) {
         @SuppressWarnings("resource")
         MosaicsAnimatedModel<?> mam = (MosaicsAnimatedModel<?>)model;

         mam.getBackgroundFill().setMode(1 + r(mam.getCellAttr().getMaxBackgroundFillModeValue()));

         ERotateMode[] eRotateModes = ERotateMode.values();
         mam.setRotateMode(eRotateModes[r(eRotateModes.length)]);

         mam.getPenBorder().setWidth(r(3));
         double pad = Math.min(mam.getSizeDouble().height/3, mam.getSizeDouble().width/3);
         mam.setPadding(-pad/4 + r((int)pad));
      }
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
      public Function<ImageController<?,?,?> /* image */, CellTilingInfo> itemCallback;
   }

   public CellTilingResult cellTiling(RectDouble rc, List<ImageController<?,?,?>> images, boolean testIntersection)
   {
      int len = images.size();
      int cols = (int)Math.round( Math.sqrt(len)  + 0.4999999999); // columns
      int rows = (int)Math.round(len/(double)cols + 0.4999999999);
      double dx = rc.width  / cols; // cell tile width
      double dy = rc.height / rows; // cell tile height

      int pad = 2; // cell padding
      double addonX = (cols==1) ? 0 : !testIntersection ? 0 : dx/4; // test intersection
      double addonY = (rows==1) ? 0 : !testIntersection ? 0 : dy/4; // test intersection
      Size imgSize = new Size((int)(dx - 2*pad + addonX),  // dx - 2*pad;
                              (int)(dy - 2*pad + addonY)); // dy - 2*pad;

      Function<ImageController<?,?,?>, CellTilingInfo> itemCallback = item -> {
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

   public String getTitle(List<ImageController<?,?,?>> images) {
      return titlePrefix + " test paints: " + images.stream()
         .map(i -> i.getClass().getName())
         .map(n -> Stream.of(n.split("\\.")).reduce((first, second) -> second).get().replaceAll("\\$", ".") )
         .collect(Collectors.groupingBy(z -> z))
         .entrySet().stream()
         .map(x -> x.getKey())
         .collect(Collectors.joining(" & "));
   }

}
