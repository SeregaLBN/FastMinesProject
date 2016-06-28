package fmg.swing.res.img;

import java.awt.AlphaComposite;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.util.List;
import java.util.Random;
import java.util.UUID;
import java.util.stream.Collectors;

import javax.swing.SwingUtilities;

import fmg.common.Color;
import fmg.common.Pair;
import fmg.common.geom.Matrisize;
import fmg.core.img.AMosaicsImg;
import fmg.core.mosaic.cells.BaseCell;
import fmg.core.mosaic.draw.ICellPaint;
import fmg.core.types.EMosaic;
import fmg.data.view.draw.PenBorder;
import fmg.swing.Cast;
import fmg.swing.draw.mosaic.PaintSwingContext;
import fmg.swing.draw.mosaic.graphics.CellPaintGraphics;
import fmg.swing.draw.mosaic.graphics.PaintableGraphics;

/**
 * Representable {@link fmg.core.types.EMosaic} as image
 * <br>
 * SWING impl
 *
 * @param <TImage> SWING specific image: {@link java.awt.Image} or {@link javax.swing.Icon}
 */
public abstract class MosaicsImg<TImage> extends AMosaicsImg<PaintableGraphics, TImage, PaintSwingContext<TImage>> {

   static {
      if (DEFERR_INVOKER == null)
         DEFERR_INVOKER = doRun -> SwingUtilities.invokeLater(doRun);
      if (TIMER_CREATOR == null)
         TIMER_CREATOR = () -> new fmg.swing.ui.Timer();
   }

   private static final boolean RandomCellBkColor = true;

   public MosaicsImg(EMosaic mosaicType, Matrisize sizeField) { super(mosaicType, sizeField); }

   private PaintSwingContext<TImage> _paintContext;
   protected PaintSwingContext<TImage> getPaintContext() {
      if (_paintContext == null)
         setPaintContext(new PaintSwingContext<>(true));
      return _paintContext;
   }
   protected void setPaintContext(PaintSwingContext<TImage> paintContext) {
      if (setProperty(_paintContext, paintContext, "PaintContext")) {
         dependency_PContext_CellAttribute();
         dependency_PContext_PaddingFull();
         dependency_PContext_BorderWidth();
         dependency_PContext_BorderColor();
         dependency_PContext_BkColor();
         invalidate();
      }
   }

   @Override
   protected void onPropertyChanged(Object oldValue, Object newValue, String propertyName) {
      super.onPropertyChanged(oldValue, newValue, propertyName);
      switch (propertyName) {
      case "PaddingFull":
         dependency_PContext_PaddingFull();
         break;
      case "CellAttr":
         dependency_PContext_CellAttribute();
         break;
      case "BorderWidth":
         dependency_PContext_BorderWidth();
         break;
      case "BorderColor":
         dependency_PContext_BorderColor();
         break;
      case "BackgroundColor":
         dependency_PContext_BkColor();
         break;
      }

      if (getRotateMode() == ERotateMode.someCells) {
         switch (propertyName) {
         case "Size":
            _imageCache = null;
            break;
         case "RotatedElements":
         case "BackgroundColor":
            _invalidateCache = true;
            break;
         }
      }
   }

   ///////////// #region Dependencys
   void dependency_PContext_CellAttribute() {
      if (_paintContext == null)
         return;
      if (RandomCellBkColor)
         getPaintContext().getBackgroundFill()
               .setMode(1 + new Random(UUID.randomUUID().hashCode()).nextInt(getCellAttr().getMaxBackgroundFillModeValue()));
   }

   void dependency_PContext_PaddingFull() {
      if (_paintContext == null)
         return;
      getPaintContext().setPadding(getPaddingFull());
   }

   void dependency_PContext_BorderWidth() {
      if (_paintContext == null)
         return;
      getPaintContext().getPenBorder().setWidth(getBorderWidth());
   }

   void dependency_PContext_BorderColor() {
      if (_paintContext == null)
         return;
      PenBorder pb = getPaintContext().getPenBorder();
      pb.setColorShadow(getBorderColor());
      pb.setColorLight(getBorderColor());
   }

   void dependency_PContext_BkColor() {
      if (_paintContext == null)
         return;
      getPaintContext().setBackgroundColor(getBackgroundColor());
   }

   ////////////// #endregion

   protected void drawBody(Graphics g) {
      switch (getRotateMode()) {
      case fullMatrix:
         drawBodyFullMatrix(g);
         break;
      case someCells:
         drawBodySomeCells(g);
         break;
      }
   }

   /** ///////////// ================= PART {@link ERotateMode#fullMatrix} ======================= ///////////// */

   /** Return painted mosaic bitmap
    *  if (!OnlySyncDraw) {
    *    Сама картинка возвращается сразу.
    *    Но вот её отрисовка - в фоне.
    *    Т.к. WriteableBitmap есть DependencyObject, то его владелец может сам отслеживать отрисовку...
    *  }
    */
   private void drawBodyFullMatrix(Graphics g) {
      int w = getWidth();
      int h = getHeight();

      //g.clearRect(0, 0, w, h);

      Runnable funcFillBk = () -> {
         g.setColor(Cast.toColor(getBackgroundColor()));
         g.fillRect(0, 0, w, h);
      };

      List<BaseCell> matrix = getMatrix();
      PaintableGraphics paint = new PaintableGraphics(null, g);
      PaintSwingContext<TImage> paintContext = getPaintContext();
      ICellPaint<PaintableGraphics, TImage, PaintSwingContext<TImage>> cp = getCellPaint();
      if (isSyncDraw() || isLiveImage()) {
         // sync draw
         funcFillBk.run();
         for (BaseCell cell : matrix)
            cp.paint(cell, paint, paintContext);
      } else {
         // async draw
         SwingUtilities.invokeLater(() ->  {
            funcFillBk.run();
            for (BaseCell cell : matrix) {
               BaseCell tmp = cell;
               SwingUtilities.invokeLater(() -> cp.paint(tmp, paint, paintContext));
            }
         });
      }
   }

   /** ///////////// ================= PART {@link ERotateMode#someCells} ======================= ///////////// */

   private static final boolean USE_CACHE = false;

   /** need redraw the static part of the cache */
   private boolean _invalidateCache = true;
   /**
    * Cached static part of the picture.
    * ! Recreated only when changing the original image size (minimizing CreateImage calls).
    **/
   private TImage _imageCache;
   protected TImage getImageCache() {
      if (_imageCache == null) {
         _imageCache = createImage();
         _invalidateCache = true;
      }
      if (_invalidateCache) {
         _invalidateCache = false;
         drawCache();
      }
      return _imageCache;
   }

   /** copy cached image to original */
   protected abstract void copyFromCache();

   protected abstract void drawCache();

   protected void drawCache(Graphics g) { drawStaticPart(g); }

   private void drawStaticPart(Graphics g) {
      int w = getWidth();
      int h = getHeight();

      g.setColor(Cast.toColor(getBackgroundColor()));
      //g.clearRect(0, 0, w, h);
      g.fillRect(0, 0, w, h);

      PaintableGraphics paint = new PaintableGraphics(null, g);
      PaintSwingContext<TImage> paintContext = getPaintContext();
      List<BaseCell> matrix = getMatrix();
      List<Integer> indexes = _rotatedElements.stream().map(cntxt -> cntxt.index).collect(Collectors.toList());
      for (int i = 0; i < matrix.size(); ++i)
         if (!indexes.contains(i))
            getCellPaint().paint(matrix.get(i), paint, paintContext);
   }

   private void drawRotatedPart(Graphics g) {
      if (_rotatedElements.isEmpty())
         return;

      PaintableGraphics paint = new PaintableGraphics(null, g);
      PaintSwingContext<TImage> paintContext = getPaintContext();
      PenBorder pb = getPaintContext().getPenBorder();
      // save
      int borderWidth = getBorderWidth();
      Color borderColor = getBorderColor();
      // modify
      pb.setWidth(2 * borderWidth);
      pb.setColorLight(borderColor.darker(0.5));
      pb.setColorShadow(borderColor.darker(0.5));

      List<BaseCell> matrix = getMatrix();
      _rotatedElements.forEach(cntxt -> getCellPaint().paint(matrix.get(cntxt.index), paint, paintContext));

      // restore
      pb.setWidth(borderWidth); //BorderWidth = borderWidth;
      pb.setColorLight(borderColor); //BorderColor = borderColor;
      pb.setColorShadow(borderColor); //BorderColor = borderColor;
   }

   private void drawBodySomeCells(Graphics g) {
      if (isSyncDraw() || isLiveImage()) {
         // sync draw
         if (USE_CACHE)
            copyFromCache();
         else
            drawStaticPart(g);
         drawRotatedPart(g);
      } else {
         // async draw
         drawBodyFullMatrix(g);
      }
   }

   @Override
   public void close() {
      super.close();
      getPaintContext().close();
   }

   /////////////////////////////////////////////////////////////////////////////////////////////////////
   //    custom implementations
   /////////////////////////////////////////////////////////////////////////////////////////////////////

   public static class Icon extends MosaicsImg<javax.swing.Icon> {

      public Icon(EMosaic mosaicType, Matrisize sizeField) { super(mosaicType, sizeField); }

      private ICellPaint<PaintableGraphics, javax.swing.Icon, PaintSwingContext<javax.swing.Icon>> _cellPaint;
      @Override
      public ICellPaint<PaintableGraphics, javax.swing.Icon, PaintSwingContext<javax.swing.Icon>> getCellPaint() {
         if (_cellPaint == null)
            _cellPaint = new CellPaintGraphics.Icon();
         return _cellPaint;
      }

      private BufferedImage buffImg;
      private Graphics2D gBuffImg;
      @Override
      protected javax.swing.Icon createImage() {
         if (gBuffImg != null)
            gBuffImg.dispose();

         buffImg = new BufferedImage(getWidth(), getHeight(), BufferedImage.TYPE_INT_ARGB);
         gBuffImg = buffImg.createGraphics();
         gBuffImg.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC));
         gBuffImg.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

         return new javax.swing.Icon() {
            @Override
            public int getIconWidth() { return Icon.this.getWidth(); }
            @Override
            public int getIconHeight() { return Icon.this.getHeight(); }
            @Override
            public void paintIcon(Component c, Graphics g, int x, int y) {
               g.drawImage(buffImg, x,y, c);
            }
         };
      }

      @Override
      protected void copyFromCache() { throw new UnsupportedOperationException("not implemented..."); }

      @Override
      protected void drawCache() { drawCache(gBuffImg); }

      @Override
      protected void drawBody() { drawBody(gBuffImg); }

      @Override
      public void close() {
         super.close();
         if (gBuffImg != null)
            gBuffImg.dispose();
         gBuffImg = null;
      }

   }

   public static class Image extends MosaicsImg<java.awt.Image> {

      public Image(EMosaic mosaicType, Matrisize sizeField) { super(mosaicType, sizeField); }

      @Override
      protected java.awt.Image createImage() {
         return new BufferedImage(getWidth(), getHeight(), BufferedImage.TYPE_INT_ARGB);
      }

      private ICellPaint<PaintableGraphics, java.awt.Image, PaintSwingContext<java.awt.Image>> _cellPaint;
      @Override
      public ICellPaint<PaintableGraphics, java.awt.Image, PaintSwingContext<java.awt.Image>> getCellPaint() {
         if (_cellPaint == null)
            _cellPaint = new CellPaintGraphics.Image();
         return _cellPaint;
      }

      @Override
      protected void copyFromCache() {
         throw new UnsupportedOperationException("not implemented...");
//         BufferedImage img = (BufferedImage) getImage();
//         Graphics2D g = img.createGraphics();
////         g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC));
////         g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
//         g.drawImage(getImageCache(), 0, 0, getWidth(), getHeight(), null);
//         g.dispose();
      }

      @Override
      protected void drawCache() {
         BufferedImage img = (BufferedImage) getImageCache();
         Graphics2D g = img.createGraphics();
         g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC));
         g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
         drawCache(g);
         g.dispose();
      }

      @Override
      protected void drawBody() {
         BufferedImage img = (BufferedImage) getImage();
         Graphics2D g = img.createGraphics();
         g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC));
         g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
         drawBody(g);
         g.dispose();
      }

   }

   ////////////// TEST //////////////
   public static void main(String[] args) {
      TestDrawing.<EMosaic>testApp(rnd -> {
         EMosaic eMosaic = EMosaic.fromOrdinal(rnd.nextInt(EMosaic.values().length));
         MosaicsImg.Icon img1 = new MosaicsImg.Icon(eMosaic, new Matrisize(3+rnd.nextInt(2), 3 + rnd.nextInt(2)));
         img1.setRotateMode(ERotateMode.values()[rnd.nextInt(ERotateMode.values().length)]);

         eMosaic = EMosaic.fromOrdinal(rnd.nextInt(EMosaic.values().length));
         MosaicsImg.Image img2 = new MosaicsImg.Image(eMosaic, new Matrisize(3+rnd.nextInt(3), 3 + rnd.nextInt(3)));
         img2.setRotateMode(ERotateMode.values()[rnd.nextInt(ERotateMode.values().length)]);

         return new Pair<>(img1, img2);
      });
   }
   //////////////////////////////////

}
