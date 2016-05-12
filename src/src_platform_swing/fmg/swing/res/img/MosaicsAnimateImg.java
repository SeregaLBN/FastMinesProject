package fmg.swing.res.img;

import java.awt.AlphaComposite;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.util.List;

import javax.swing.SwingUtilities;

import fmg.common.Color;
import fmg.common.Pair;
import fmg.common.geom.Bound;
import fmg.common.geom.Matrisize;
import fmg.common.geom.Size;
import fmg.core.img.RotatedImg;
import fmg.core.img.StaticImg;
import fmg.core.mosaic.cells.BaseCell;
import fmg.core.mosaic.draw.ICellPaint;
import fmg.core.types.EMosaic;
import fmg.data.view.draw.PenBorder;
import fmg.swing.Cast;
import fmg.swing.draw.mosaic.graphics.PaintableGraphics;
import fmg.swing.res.img.MosaicsImg.MosaicsImgExtProperty;

/** картинка поля конкретной мозаики, где могут вращаться отдельный ячейки
 *  SWING impl
 **/
public abstract class MosaicsAnimateImg<TImage extends Object> extends fmg.core.img.MosaicsAnimateImg<PaintableGraphics, TImage> {

   static {
      StaticImg.DEFERR_INVOKER = doRun -> SwingUtilities.invokeLater(doRun);
      RotatedImg.TIMER_CREATOR = () -> new fmg.swing.ui.Timer();
   }

   public MosaicsAnimateImg(EMosaic mosaicType, Matrisize sizeField) { super(mosaicType, sizeField); }
   public MosaicsAnimateImg(EMosaic mosaicType, Matrisize sizeField, int widthAndHeight) { super(mosaicType, sizeField, widthAndHeight); }
   public MosaicsAnimateImg(EMosaic mosaicType, Matrisize sizeField, int widthAndHeight, int padding) { super(mosaicType, sizeField, widthAndHeight, padding); }
   public MosaicsAnimateImg(EMosaic mosaicType, Matrisize sizeField, Size sizeImage, Bound padding) { super(mosaicType, sizeField, sizeImage, padding); }

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

   protected abstract void drawCache();

   @Override
   public ICellPaint<PaintableGraphics> getCellPaint() { return _extProperties.getCellPaint(); }

   private MosaicsImgExtProperty<TImage> _extProperties = new MosaicsImgExtProperty<>(
         this,
         propertyName -> onPropertyChanged(propertyName),
         () -> invalidate() );

   /** copy cached image to original */
   protected abstract void copyFromCache();

   protected void drawCache(Graphics g) { drawStaticPartReal(g); }

   private void drawStaticPartReal(Graphics g) {
      int w = getWidth();
      int h = getHeight();

      g.setColor(Cast.toColor(getBackgroundColor()));
      //g.clearRect(0, 0, w, h);
      g.fillRect(0, 0, w, h);

      PaintableGraphics paint = new PaintableGraphics(g);
      List<BaseCell> matrix = getMatrix();
      for (int i = 0; i < matrix.size(); ++i)
         if (!_rotatedElements.containsKey(i))
            getCellPaint().paint(matrix.get(i), paint);
   }

   private void drawStaticPart(Graphics g) {
      if (USE_CACHE)
         copyFromCache();
      else
         drawStaticPartReal(g);
   }

   private void drawRotatedPart(Graphics g) {
      if (_rotatedElements.isEmpty())
         return;

      PaintableGraphics paint = new PaintableGraphics(g);
      PenBorder pb = _extProperties.getGContext().getPenBorder();
      // save
      int borderWidth = getBorderWidth();
      Color borderColor = getBorderColor();
      // modify
      pb.setWidth(2 * borderWidth);
      pb.setColorLight(borderColor.darker(0.5));
      pb.setColorShadow(borderColor.darker(0.5));

      List<BaseCell> matrix = getMatrix();
      for (int i = 0; i < matrix.size(); ++i)
         if (_rotatedElements.containsKey(i))
            getCellPaint().paint(matrix.get(i), paint);

      // restore
      pb.setWidth(borderWidth); //BorderWidth = borderWidth;
      pb.setColorLight(borderColor); //BorderColor = borderColor;
      pb.setColorShadow(borderColor); //BorderColor = borderColor;
   }

   protected void drawBody(Graphics g) {
      if (isOnlySyncDraw() || isLiveImage()) {
         // sync draw
         drawStaticPart(g);
         drawRotatedPart(g);
      } else {
         // async draw
         MosaicsImg.drawBody(g, this);
      }
   }

   @Override
   protected void onPropertyChanged(Object oldValue, Object newValue, String propertyName) {
      //LoggerSimple.Put("OnPropertyChanged: {0}: PropertyName={1}", Entity, ev.PropertyName);
      super.onPropertyChanged(oldValue, newValue, propertyName);
      switch (propertyName) {
      case "Size":
         //_invalidateCache = true;
         _imageCache = null;
         break;
      case "RotatedElements":
         _invalidateCache = true;
         break;
      }
      _extProperties.onPropertyChanged(oldValue, newValue, propertyName);
   }

   /////////////////////////////////////////////////////////////////////////////////////////////////////
   //    custom implementations
   /////////////////////////////////////////////////////////////////////////////////////////////////////

   public static class Icon extends MosaicsAnimateImg<javax.swing.Icon> {
      public Icon(EMosaic mosaicType, Matrisize sizeField) { super(mosaicType, sizeField); }
      public Icon(EMosaic mosaicType, Matrisize sizeField, int widthAndHeight) { super(mosaicType, sizeField, widthAndHeight); }
      public Icon(EMosaic mosaicType, Matrisize sizeField, int widthAndHeight, int padding) { super(mosaicType, sizeField, widthAndHeight, padding); }
      public Icon(EMosaic mosaicType, Matrisize sizeField, Size sizeImage, Bound padding) { super(mosaicType, sizeField, sizeImage, padding); }

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
               g.drawImage(buffImg, x,y, null);
            }
         };
      }

      @Override
      protected void copyFromCache() {
      }

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

   public static class Image extends MosaicsAnimateImg<java.awt.Image> {
      public Image(EMosaic mosaicType, Matrisize sizeField) { super(mosaicType, sizeField); }
      public Image(EMosaic mosaicType, Matrisize sizeField, int widthAndHeight) { super(mosaicType, sizeField, widthAndHeight); }
      public Image(EMosaic mosaicType, Matrisize sizeField, int widthAndHeight, int padding) { super(mosaicType, sizeField, widthAndHeight, padding); }
      public Image(EMosaic mosaicType, Matrisize sizeField, Size sizeImage, Bound padding) { super(mosaicType, sizeField, sizeImage, padding); }

      @Override
      protected java.awt.Image createImage() {
         return new BufferedImage(getWidth(), getHeight(), BufferedImage.TYPE_INT_ARGB);
      }

      @Override
      protected void copyFromCache() {
         BufferedImage img = (BufferedImage) getImage();
         Graphics2D g = img.createGraphics();
//         g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC));
//         g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
         g.drawImage(getImageCache(), 0, 0, getWidth(), getHeight(), null);
         g.dispose();
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
         MosaicsAnimateImg.Icon img1 = new MosaicsAnimateImg.Icon(eMosaic, new Matrisize(3+rnd.nextInt(3), 3 + rnd.nextInt(3)));

         eMosaic = EMosaic.fromOrdinal(rnd.nextInt(EMosaic.values().length));
         MosaicsAnimateImg.Image img2 = new MosaicsAnimateImg.Image(eMosaic, new Matrisize(3+rnd.nextInt(3), 3 + rnd.nextInt(3)));

         return new Pair<>(img1, img2);
      });
   }
   //////////////////////////////////
}
