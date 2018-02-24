package fmg.swing.draw.img;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import fmg.common.Color;
import fmg.common.geom.Matrisize;
import fmg.common.geom.Size;
import fmg.common.geom.SizeDouble;
import fmg.core.img.AMosaicsImg;
import fmg.core.mosaic.cells.BaseCell;
import fmg.core.mosaic.draw.ICellPaint;
import fmg.core.types.EMosaic;
import fmg.data.view.draw.PenBorder;
import fmg.swing.draw.mosaic.PaintSwingContext;
import fmg.swing.draw.mosaic.graphics.CellPaintGraphics;
import fmg.swing.draw.mosaic.graphics.PaintableGraphics;
import fmg.swing.mosaic.AMosaicViewSwing;

/**
 * Representable {@link fmg.core.types.EMosaic} as image
 * <br>
 * base SWING impl
 *
 * @param <TImage> SWING specific image: {@link java.awt.Image} or {@link javax.swing.Icon}
 */
public abstract class MosaicsImg<TImage> extends AMosaicsImg<TImage> {

   static {
      StaticInitilizer.init();
   }

   private static final boolean RandomCellBkColor = true;

   protected abstract class AMosaicImgView extends AMosaicViewSwing<TImage> {

      private CellPaintGraphics<TImage> _cellPaint;

      public abstract CellPaintGraphics<TImage> createCellPaint();

      @Override
      public ICellPaint<PaintableGraphics, TImage, PaintSwingContext<TImage>> getCellPaint() {
         if (_cellPaint == null)
            _cellPaint = createCellPaint();
         return _cellPaint;
      }

      @Override
      protected PaintSwingContext<TImage> createPaintContext() {
         PaintSwingContext<TImage> cntxt = new PaintSwingContext<>();
         cntxt.setIconicMode(true);
         if (RandomCellBkColor)
            cntxt.getBackgroundFill().setMode(1 + ThreadLocalRandom.current().nextInt(getMosaic().getCellAttr().getMaxBackgroundFillModeValue()));
         return cntxt;
      }

      @Override
      public void invalidate(Collection<BaseCell> modifiedCells) {
         repaint(modifiedCells, null);
      }

      @Override
      protected void changeSizeImagesMineFlag() {
         // none
      }

      @Override
      public SizeDouble getSize() {
         Size sz = MosaicsImg.this.getSize();
         return new SizeDouble(sz.width, sz.height);
      }

      @Override
      public void close() {
         super.close();
         _cellPaint = null;
      }

   }

   protected AMosaicImgView _view;

   protected abstract AMosaicImgView createView();

   /** get view */
   protected AMosaicImgView getView() {
      if (_view == null) {
         setView(createView());
      }
      return _view;
   }
   /** set view */
   protected void setView(AMosaicImgView view) {
      if (_view != null)
         _view.close();
      _view = view;
      if (_view != null)
         _view.setMosaic(getMosaic());
   }

   protected PaintSwingContext<TImage> getPaintContext() { return getView().getPaintContext(); }

   @Override
   protected void onSelfPropertyChanged(Object oldValue, Object newValue, String propertyName) {
      switch (propertyName) {
      case PROPERTY_PADDING_FULL:
         getPaintContext().setPadding(getPaddingFull());
         break;
      case PROPERTY_BORDER_WIDTH:
         getPaintContext().getPenBorder().setWidth(getBorderWidth());
         break;
      case PROPERTY_BORDER_COLOR:
         PenBorder pb = getPaintContext().getPenBorder();
         pb.setColorShadow(getBorderColor());
         pb.setColorLight(getBorderColor());
         break;
      case PROPERTY_BACKGROUND_COLOR:
         getPaintContext().setBackgroundColor(getBackgroundColor());
         break;
      }

      super.onSelfPropertyChanged(oldValue, newValue, propertyName);

      if (getRotateMode() == ERotateMode.someCells) {
         switch (propertyName) {
         case PROPERTY_SIZE:
            _imageCache = null;
            break;
         case PROPERTY_ROTATED_ELEMENTS:
         case PROPERTY_BACKGROUND_COLOR:
            _invalidateCache = true;
            break;
         }
      }
   }

   @Override
   protected void drawBody() {
      switch (getRotateMode()) {
      case fullMatrix:
         drawBodyFullMatrix();
         break;
      case someCells:
         drawBodySomeCells();
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
   private void drawBodyFullMatrix() {
      getView().getPaintContext().setUseBackgroundColor(true);
      getView().invalidate(getMatrix());
   }

   /** ///////////// ================= PART {@link ERotateMode#someCells} ======================= ///////////// */

   private static final boolean USE_CACHE = false; // true is not supported in SWING

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
         //Graphics save = getView().getPaintable();
         //getView().setPaintable(_imageCache as Graphics); // not implement in SWING :(
         drawCache();
         //getView().setPaintable(save); // restore
      }
      return _imageCache;
   }

   /** copy cached image to original */
   protected abstract void copyFromCache();

   protected void drawCache() { drawStaticPart(); }

   private void drawStaticPart() {
      getView().getPaintContext().setUseBackgroundColor(true);

      List<BaseCell> notRotated;
      if (_rotatedElements.isEmpty()) {
         notRotated = getMatrix();
      } else {
         List<BaseCell> matrix = getMatrix();
         List<Integer> indexes = _rotatedElements.stream().map(cntxt -> cntxt.index).collect(Collectors.toList());
         notRotated = new ArrayList<>(matrix.size() - indexes.size());
         int i = 0;
         for (BaseCell cell : matrix) {
            if (!indexes.contains(i))
               notRotated.add(cell);
            ++i;
         }
      }
      getView().invalidate(notRotated);
   }

   private void drawRotatedPart() {
      if (_rotatedElements.isEmpty())
         return;

      PenBorder pb = getPaintContext().getPenBorder();
      // save
      int borderWidth = getBorderWidth();
      Color borderColor = getBorderColor();
      // modify
      pb.setWidth(2 * borderWidth);
      pb.setColorLight(borderColor.darker(0.5));
      pb.setColorShadow(borderColor.darker(0.5));

      getView().getPaintContext().setUseBackgroundColor(false);
      List<BaseCell> matrix = getMatrix();
      List<BaseCell> rotatedCells = new ArrayList<>(_rotatedElements.size());
      for (RotatedCellContext cntxt : _rotatedElements)
         rotatedCells.add(matrix.get(cntxt.index));
      getView().invalidate(rotatedCells);

      // restore
      pb.setWidth(borderWidth); //BorderWidth = borderWidth;
      pb.setColorLight(borderColor); //BorderColor = borderColor;
      pb.setColorShadow(borderColor); //BorderColor = borderColor;
   }

   private void drawBodySomeCells() {
      if (USE_CACHE)
         copyFromCache();
      else
         drawStaticPart();
      drawRotatedPart();
   }

   @Override
   public void close() {
      super.close();
      setView(null);
   }

   /////////////////////////////////////////////////////////////////////////////////////////////////////
   //    custom implementations
   /////////////////////////////////////////////////////////////////////////////////////////////////////

   public static class Icon extends MosaicsImg<javax.swing.Icon> {

      private BufferedImage buffImg;
      private Graphics2D gBuffImg;
      @Override
      protected javax.swing.Icon createImage() {
         if (gBuffImg != null)
            gBuffImg.dispose();

         buffImg = new BufferedImage(getSize().width, getSize().height, BufferedImage.TYPE_INT_ARGB);
         gBuffImg = buffImg.createGraphics();
         gBuffImg.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC));
         gBuffImg.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
         gBuffImg.setClip(0, 0, getSize().width, getSize().height);
         getView().setPaintable(gBuffImg);

         return new javax.swing.Icon() {
            @Override
            public int getIconWidth() { return Icon.this.getSize().width; }
            @Override
            public int getIconHeight() { return Icon.this.getSize().height; }
            @Override
            public void paintIcon(Component c, Graphics g, int x, int y) {
               g.drawImage(buffImg, x,y, c);
            }
         };
      }

      @Override
      protected AMosaicImgView createView() {
         return new AMosaicImgView() {

            @Override
            public CellPaintGraphics<javax.swing.Icon> createCellPaint() {
               return new CellPaintGraphics.Icon();
            }

         };
      }

      @Override
      protected void copyFromCache() {
//       paintableGraphics.drawImage(getImageCache(), 0, 0, getSize().width, getSize().height, null);
         throw new UnsupportedOperationException("not implemented...");
      }

//      @Override
//      protected void drawBody() {
//         getView().setPaintable(gBuffImg);
//         super.drawBody();
//         getView().setPaintable(null);
//      }

      @Override
      public void close() {
         super.close();
         if (gBuffImg != null)
            gBuffImg.dispose();
         gBuffImg = null;
      }

   }

   public static class Image extends MosaicsImg<java.awt.Image> {

      @Override
      protected java.awt.Image createImage() {
         return new BufferedImage(getSize().width, getSize().height, BufferedImage.TYPE_INT_ARGB);
      }

      @Override
      protected AMosaicImgView createView() {
         return new AMosaicImgView() {

            @Override
            public CellPaintGraphics<java.awt.Image> createCellPaint() {
               return new CellPaintGraphics.Image();
            }

         };
      }

      @Override
      protected void copyFromCache() {
//         paintableGraphics.drawImage(getImageCache(), 0, 0, getSize().width, getSize().height, null);
         throw new UnsupportedOperationException("not implemented...");
      }

      @Override
      protected void drawBody() {
         BufferedImage img = (BufferedImage) getImage();
         Graphics2D g = img.createGraphics();
         g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC));
         g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
         g.setClip(0, 0, getSize().width, getSize().height);
         getView().setPaintable(g);
         super.drawBody();
         getView().setPaintable(null);
         g.dispose();
      }

   }

   ////////////// TEST //////////////
   public static void main(String[] args) {
      Random rnd = ThreadLocalRandom.current();
      TestDrawing.testApp(() ->
//       // test single
//       Arrays.asList(new MosaicsImg.Image() { { setMosaicType(EMosaic.eMosaicPentagonT24); setSizeField(new Matrisize(3, 7)); }})

         // test all
         Stream.of(EMosaic.values())

               // variant 1
//               .map(e -> new Pair<>(new MosaicsImg.Icon () { { setMosaicType(e); setSizeField(new Matrisize(3+rnd.nextInt(2), 3 + rnd.nextInt(2))); }},
//                                    new MosaicsImg.Image() { { setMosaicType(e); setSizeField(new Matrisize(3+rnd.nextInt(2), 3 + rnd.nextInt(2))); }}))
//               .flatMap(x -> Stream.of(x.first, x.second))

               // variant 2
               .map(e ->  rnd.nextBoolean()
                           ? new MosaicsImg.Icon () { { setMosaicType(e); setSizeField(new Matrisize(3+rnd.nextInt(2), 3 + rnd.nextInt(2))); }}
                           : new MosaicsImg.Image() { { setMosaicType(e); setSizeField(new Matrisize(3+rnd.nextInt(2), 3 + rnd.nextInt(2))); }})

               .collect(Collectors.toList())
      );
   }
   //////////////////////////////////

}
