package fmg.core.img;

import java.util.List;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import fmg.common.Color;
import fmg.common.geom.*;
import fmg.core.img.MosaicAnimatedModel.ERotateMode;
import fmg.core.mosaic.MosaicDrawModel;
import fmg.core.mosaic.MosaicGameModel;

public abstract class ATestDrawing {

   public Random getRandom() { return ThreadLocalRandom.current(); }
   public int r(int max) { return getRandom().nextInt(max); }
   public boolean bl() { return getRandom().nextBoolean(); } // random bool
   public int np() { return (bl() ? -1 : +1); } // negative or positive

   private final String titlePrefix;

   protected ATestDrawing(String titlePrefix) {
      this.titlePrefix = titlePrefix;
   }

   public void applySettings(IImageController<?,?,?> ctrller, boolean testTransparent) {

      ///////////////////////
      //                   //
      //  manual settings  //
      //                   //
      ///////////////////////

      IImageModel model = ctrller.getModel();
      //model.setSize(new SizeDouble(600, 600));

      if (model instanceof IAnimatedModel) {
         IAnimatedModel am = (IAnimatedModel)model;
         am.setAnimated(true);
         if (am.isAnimated()) {
            am.setAnimatePeriod(2000); // rotate period
            am.setTotalFrames(100); // animate iterations
         }
      }
      if (model instanceof AnimatedImageModel) {
         @SuppressWarnings("resource")
         AnimatedImageModel aim = (AnimatedImageModel)model;
         aim.setPadding(10);
         aim.setBorderWidth(0);
         aim.setBackgroundColor(testTransparent ? new Color(0xC8FFFFFF) : Color.White());
         aim.getForegroundColor().setA(200); // 0..255 - foreground alpha-chanel color
      }
      if (model instanceof BurgerMenuModel) {
         @SuppressWarnings("resource")
         BurgerMenuModel bmm = (BurgerMenuModel)model;
         bmm.setShow(true);
      }
      if (model instanceof LogoModel) {
         @SuppressWarnings("resource")
         LogoModel lm = (LogoModel)model;
         lm.setUseGradient(true);
      }

      if (ctrller instanceof AnimatedImgController<?, ?, ?>) {
         @SuppressWarnings("resource")
         AnimatedImgController<?, ?, ?> aic = (AnimatedImgController<?, ?, ?>)ctrller;
         aic.useRotateTransforming(true);
         aic.usePolarLightFgTransforming(true);
      }


      boolean useRandom = true;
      ///////////////////////
      //                   //
      //  random settings  //
      //                   //
      ///////////////////////
      if (!useRandom)
         return;

      testTransparent = testTransparent || bl();

      if (model instanceof IAnimatedModel) {
         IAnimatedModel am = (IAnimatedModel)model;
         am.setAnimated(bl() || bl());
         if (am.isAnimated()) {
            am.setAnimatePeriod(1000 + r(2000));
            am.setTotalFrames(40 + r(20));
         }
      }
      if (ctrller instanceof AnimatedImgController) {
         AnimatedImgController<?,?,?> aCtrller = (AnimatedImgController<?,?,?>)ctrller;
         if (aCtrller.getModel().isAnimated()) {
            aCtrller.useRotateTransforming(bl());
            aCtrller.usePolarLightFgTransforming(bl());
            if (bl())
               aCtrller.addModelTransformer(new PolarLightBkTransformer());
         }
      }

      Color bkClr = Color.RandomColor();
      if (testTransparent)
         bkClr.setA(50 + r(10));

      if (model instanceof AnimatedImageModel) {
         @SuppressWarnings("resource")
         AnimatedImageModel aim = (AnimatedImageModel)model;

         aim.setBorderWidth(r(3));

         double pad = Math.min(aim.getSize().height/3, aim.getSize().width/3);
         aim.setPadding(-pad/4 + r((int)pad));

         aim.setBackgroundColor(bkClr);

         aim.setForegroundColor(Color.RandomColor()/*.brighter()*/);
         if (testTransparent) {
            // test transparent
            Color clr = aim.getForegroundColor();
            if ((aim.getBorderWidth() > 0) && (r(4) == 0)) {
               clr.setA(Color.Transparent().getA());
            } else {
               clr.setA(150 + r(255-150));
            }
            aim.setForegroundColor(clr);
         }

         aim.setPolarLights(bl());
         aim.setAnimeDirection(bl());

         if (model instanceof LogoModel) {
            @SuppressWarnings("resource")
            LogoModel lm = (LogoModel)model;
            lm.setUseGradient(bl());
         }
      }
      if (model instanceof MosaicGameModel) {
         @SuppressWarnings("resource")
         MosaicGameModel mgm = (MosaicGameModel)model;
         mgm.setSizeField(new Matrisize(3+r(2), 3 + r(2)));

         if (model instanceof MosaicDrawModel<?>) {
            @SuppressWarnings("resource")
            MosaicDrawModel<?> mdm = (MosaicDrawModel<?>)model;
            mdm.setBackgroundColor(bkClr);

            mdm.getBackgroundFill().setMode(1 + r(mdm.getCellAttr().getMaxBackgroundFillModeValue()));

            mdm.getPenBorder().setWidth(r(3));
            SizeDouble size = mdm.getSize();
            double padLeftRight = r((int)(size.width /3));
            double padTopBottom = r((int)(size.height/3));
            mdm.setPadding(new BoundDouble(padLeftRight, padTopBottom, padLeftRight, padTopBottom));

            if (model instanceof MosaicAnimatedModel) {
               @SuppressWarnings("resource")
               MosaicAnimatedModel<?> mam = (MosaicAnimatedModel<?>)model;

               ERotateMode[] eRotateModes = ERotateMode.values();
               mam.setRotateMode(eRotateModes[r(eRotateModes.length)]);
            }
         }
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
      public SizeDouble imageSize;
      public Size tableSize;
      public Function<IImageController<?,?,?> /* imageControllers */, CellTilingInfo> itemCallback;
   }

   public CellTilingResult cellTiling(RectDouble rc, List<IImageController<?,?,?>> images, boolean testIntersection) {
      int len = images.size();

      // max tiles in one row
      Function<Integer, Integer> mtor = rowsTotal -> {
         return (int)Math.ceil(len / (double)rowsTotal);
      };

      // для предполагаемого кол-ва колонок нахожу макс кол-во плиток в строке
      // и возвращаю отношение меньшей стороны к большей
      Function<Integer, Double> f = rowsTotal -> {
         int mcnt = mtor.apply(rowsTotal);
         double tailW = rc.width / mcnt;
         double tailH = rc.height / rowsTotal;
         return (tailW < tailH)
               ? tailW/tailH
               : tailH/tailW;
      };

      int rowsOpt = 0;
      {
         double xToY = 0; // отношение меньшей стороны к большей
         // ищу оптимальное кол-во колонок для расположения плиток. Оптимальным считаю такое расположение,
         // при котором плитки будут наибольше похожими на квадрат (т.е. отношение меньшей стороны к большей будет максимальней)
         for (int i=1; i<=len; ++i) {
            double xy = f.apply(i);
            if (xy < xToY)
               break;
            rowsOpt = i;
            xToY = xy;
         }
      }

      int rows = rowsOpt;
      int cols = (int)Math.ceil(len/(double)rows);
      double dx = rc.width  / cols; // cell tile width
      double dy = rc.height / rows; // cell tile height

      int pad = 2; // cell padding
      double addonX = (cols==1) ? 0 : !testIntersection ? 0 : dx/4; // test intersection
      double addonY = (rows==1) ? 0 : !testIntersection ? 0 : dy/4; // test intersection
      SizeDouble imgSize = new SizeDouble(dx - 2*pad + addonX,  // dx - 2*pad;
                                          dy - 2*pad + addonY); // dy - 2*pad;

      Function<IImageController<?,?,?>, CellTilingInfo> itemCallback = item -> {
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

   public String getTitle(List<IImageController<?,?,?>> images) {
      return titlePrefix + " test paints: " + images.stream()
         .map(i -> i.getClass().getName())
         .map(n -> Stream.of(n.split("\\.")).reduce((first, second) -> second).get().replaceAll("\\$", ".") )
         .collect(Collectors.groupingBy(z -> z))
         .entrySet().stream()
         .map(x -> x.getKey())
         .collect(Collectors.joining(" & "));
   }

}
