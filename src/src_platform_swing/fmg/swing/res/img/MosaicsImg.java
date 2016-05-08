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
import java.util.function.Consumer;

import javax.swing.SwingUtilities;

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
import fmg.swing.draw.GraphicContext;
import fmg.swing.draw.mosaic.graphics.CellPaintGraphics;
import fmg.swing.draw.mosaic.graphics.PaintableGraphics;

/** картинка поля конкретной мозаики. Используется для меню, кнопок, etc...
 *  SWING impl
 **/
public abstract class MosaicsImg<TImage extends Object> extends fmg.core.img.MosaicsImg<PaintableGraphics, TImage> {

   static {
      StaticImg.DEFERR_INVOKER = doRun -> SwingUtilities.invokeLater(doRun);
      RotatedImg.TIMER_CREATOR = () -> new fmg.swing.ui.Timer();
   }

   public MosaicsImg(EMosaic mosaicType, Matrisize sizeField) { super(mosaicType, sizeField); }
   public MosaicsImg(EMosaic mosaicType, Matrisize sizeField, int widthAndHeight) { super(mosaicType, sizeField, widthAndHeight); }
   public MosaicsImg(EMosaic mosaicType, Matrisize sizeField, int widthAndHeight, int padding) { super(mosaicType, sizeField, widthAndHeight, padding); }
   public MosaicsImg(EMosaic mosaicType, Matrisize sizeField, Size sizeImage, Bound padding) { super(mosaicType, sizeField, sizeImage, padding); }

   @Override
   public ICellPaint<PaintableGraphics> getCellPaint() { return _extProperties.getCellPaint(); }

   private MosaicsImgExtProperty<TImage> _extProperties = new MosaicsImgExtProperty<>(
         this,
         propertyName -> onPropertyChanged(propertyName),
         () -> invalidate() );

   static class MosaicsImgExtProperty<TImage> {

      final fmg.core.img.MosaicsImg<PaintableGraphics, TImage> _self;
      final Consumer<String> _firePropertyChanged;
      final Runnable _invalidator;
      private static final boolean RandomCellBkColor = true;

      public MosaicsImgExtProperty(
            fmg.core.img.MosaicsImg<PaintableGraphics, TImage> owner,
            Consumer<String> firePropertyChanged,
            Runnable invalidator)
      {
         _self = owner;
         _firePropertyChanged = firePropertyChanged;
         _invalidator = invalidator;
      }

      private ICellPaint<PaintableGraphics> _cellPaint;
      public ICellPaint<PaintableGraphics> getCellPaint() {
         if (_cellPaint == null)
            setCellPaint(new CellPaintGraphics());
         return _cellPaint;
      }
      private void setCellPaint(ICellPaint<PaintableGraphics> value) {
         //if (_self.setProperty(_cellPaint, value, "CellPaint")) {
         if (_cellPaint != value) {
            _cellPaint = value;
            _firePropertyChanged.accept("CellPaint");
            dependency_GContext_CellPaint();
            _invalidator.run();
         }
      }

      private GraphicContext _gContext;
      protected GraphicContext getGContext() {
         if (_gContext == null)
            setGContext(new GraphicContext(null, true));
         return _gContext;
      }
      protected void setGContext(GraphicContext value) {
         //if (_self.setProperty(_gContext, value, "GContext")) {
         if (_gContext != value) {
            _gContext = value;
            _firePropertyChanged.accept("GContext");
            dependency_GContext_CellAttribute();
            dependency_GContext_PaddingFull();
            dependency_GContext_CellPaint();
            dependency_GContext_BorderWidth();
            dependency_GContext_BorderColor();
            _invalidator.run();
         }
      }

      protected void onPropertyChanged(Object oldValue, Object newValue, String propertyName) {
         switch (propertyName) {
         case "PaddingFull":
            dependency_GContext_PaddingFull();
            break;
         case "CellAttr":
            dependency_GContext_CellAttribute();
            break;
         case "BorderWidth":
            dependency_GContext_BorderWidth();
            break;
         case "BorderColor":
            dependency_GContext_BorderColor();
            break;
         }
      }

      ///////////// #region Dependencys
      void dependency_GContext_CellAttribute() {
         if (_gContext == null)
            return;
         if (RandomCellBkColor)
            getGContext().getBackgroundFill()
                  .setMode(1 + new Random(UUID.randomUUID().hashCode()).nextInt(_self.getCellAttr().getMaxBackgroundFillModeValue()));
      }

      void dependency_GContext_PaddingFull() {
         if (_gContext == null)
            return;
         getGContext().setPadding(_self.getPaddingFull());
      }

      void dependency_GContext_BorderWidth() {
         if (_gContext == null)
            return;
         getGContext().getPenBorder().setWidth(_self.getBorderWidth());
      }

      void dependency_GContext_BorderColor() {
         if (_gContext == null)
            return;
         PenBorder pb = getGContext().getPenBorder();
         pb.setColorShadow(_self.getBorderColor());
         pb.setColorLight(_self.getBorderColor());
      }

      void dependency_GContext_CellPaint() {
         if (_cellPaint == null)
            return;
         assert (getCellPaint() instanceof CellPaintGraphics);
         ((CellPaintGraphics) getCellPaint()).setGraphicContext(getGContext());
      }

      ////////////// #endregion
   }

   /** Return painted mosaic bitmap
    *  if (!OnlySyncDraw) {
    *    Сама картинка возвращается сразу.
    *    Но вот её отрисовка - в фоне.
    *    Т.к. WriteableBitmap есть DependencyObject, то его владелец может сам отслеживать отрисовку...
    *  }
    */
   protected void drawBody(Graphics g) { drawBody(g, this); }
   protected static <TImage> void drawBody(Graphics g, fmg.core.img.MosaicsImg<PaintableGraphics, TImage> self) {
      int w = self.getWidth();
      int h = self.getHeight();

      //g.clearRect(0, 0, w, h);

      Runnable funcFillBk = () -> {
         g.setColor(Cast.toColor(self.getBackgroundColor()));
         g.fillRect(0, 0, w, h);
      };

      List<BaseCell> matrix = self.getRotatedMatrix();
      PaintableGraphics paint = new PaintableGraphics(g);
      ICellPaint<PaintableGraphics> cp = self.getCellPaint();
      if (self.isOnlySyncDraw() || self.isLiveImage()) {
         // sync draw
         funcFillBk.run();
         for (BaseCell cell : matrix)
            cp.paint(cell, paint);
      } else {
         // async draw
         SwingUtilities.invokeLater(() ->  {
            funcFillBk.run();
            for (BaseCell cell : matrix) {
               BaseCell tmp = cell;
               SwingUtilities.invokeLater(() -> cp.paint(tmp, paint));
            }
         });
      }
   }

   @Override
   protected void onPropertyChanged(Object oldValue, Object newValue, String propertyName) {
      //LoggerSimple.Put("OnPropertyChanged: {0}: PropertyName={1}", Entity, ev.PropertyName);
      super.onPropertyChanged(oldValue, newValue, propertyName);
      _extProperties.onPropertyChanged(oldValue, newValue, propertyName);
   }

   /////////////////////////////////////////////////////////////////////////////////////////////////////
   //    custom implementations
   /////////////////////////////////////////////////////////////////////////////////////////////////////

   public static class Icon extends MosaicsImg<javax.swing.Icon> {
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
      public Image(EMosaic mosaicType, Matrisize sizeField, int widthAndHeight) { super(mosaicType, sizeField, widthAndHeight); }
      public Image(EMosaic mosaicType, Matrisize sizeField, int widthAndHeight, int padding) { super(mosaicType, sizeField, widthAndHeight, padding); }
      public Image(EMosaic mosaicType, Matrisize sizeField, Size sizeImage, Bound padding) { super(mosaicType, sizeField, sizeImage, padding); }

      @Override
      protected java.awt.Image createImage() {
         return new BufferedImage(getWidth(), getHeight(), BufferedImage.TYPE_INT_ARGB);
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
         MosaicsImg.Icon img1 = new MosaicsImg.Icon(eMosaic, new Matrisize(5+rnd.nextInt(5), 5 + rnd.nextInt(5)));

         eMosaic = EMosaic.fromOrdinal(rnd.nextInt(EMosaic.values().length));
         MosaicsImg.Image img2 = new MosaicsImg.Image(eMosaic, new Matrisize(5+rnd.nextInt(5), 5 + rnd.nextInt(5)));

         return new Pair<>(img1, img2);
      });
   }
   //////////////////////////////////
}
