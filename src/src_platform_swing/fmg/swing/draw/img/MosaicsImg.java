package fmg.swing.draw.img;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import fmg.common.Color;
import fmg.core.img.MosaicRotateTransformer;
import fmg.core.img.MosaicsAnimatedModel;
import fmg.core.img.MosaicsAnimatedModel.ERotateMode;
import fmg.core.img.MosaicsAnimatedModel.RotatedCellContext;
import fmg.core.mosaic.AMosaicController;
import fmg.core.mosaic.cells.BaseCell;
import fmg.core.mosaic.draw.MosaicDrawModel;
import fmg.core.types.EMosaic;
import fmg.data.view.draw.PenBorder;
import fmg.swing.mosaic.AMosaicViewSwing;

/**
 * Representable {@link fmg.core.types.EMosaic} as image
 * <br>
 * base SWING impl
 *
 * @param <TImage> SWING specific image: {@link java.awt.Image} or {@link javax.swing.Icon}
 */
public abstract class MosaicsImg<TImage>
                extends AMosaicViewSwing<TImage, Void, MosaicsAnimatedModel<Void>>
{

   private static final boolean RandomCellBkColor = true;

   protected MosaicsImg() {
      super(new MosaicsAnimatedModel<Void>());

      MosaicsAnimatedModel<Void> model = getModel();
      PenBorder pen = model.getPenBorder();
      pen.setColorLight(pen.getColorShadow());
      if (RandomCellBkColor)
         model.getBackgroundFill().setMode(1 + ThreadLocalRandom.current().nextInt(model.getCellAttr().getMaxBackgroundFillModeValue()));
   }

   @Override
   protected void onPropertyModelChanged(Object oldValue, Object newValue, String propertyName) {
      super.onPropertyModelChanged(oldValue, newValue, propertyName);

      MosaicsAnimatedModel<Void> model = getModel();
      /*
      switch (propertyName) {
      case MosaicGameModel.PROPERTY_CELL_ATTR:
         if (RandomCellBkColor)
            model.getBackgroundFill().setMode(1 + ThreadLocalRandom.current().nextInt(model.getCellAttr().getMaxBackgroundFillModeValue()));
         break;
      default:
         break;
      }
      */

      if (model.getRotateMode() == ERotateMode.someCells) {
         switch (propertyName) {
         case MosaicDrawModel.PROPERTY_SIZE:
            _imageCache = null;
            break;
         case MosaicsAnimatedModel.PROPERTY_ROTATED_ELEMENTS:
         case MosaicsAnimatedModel.PROPERTY_BACKGROUND_COLOR:
            _invalidateCache = true;
            break;
         }
      }
   }

   @Override
   protected void drawBody() {
      //super.drawBody(); // !override super implementtation
      switch (getModel().getRotateMode()) {
      case fullMatrix:
         drawBodyFullMatrix();
         break;
      case someCells:
         drawBodySomeCells();
         break;
      }
   }


   protected boolean _useBackgroundColor = true;
   protected boolean _drawOnCache = true;

   /** ///////////// ================= PART {@link ERotateMode#fullMatrix} ======================= ///////////// */

   private void drawBodyFullMatrix() {
      _drawOnCache = false;
      _useBackgroundColor = true;
      draw(getModel().getMatrix());
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
         //Graphics save = get...();
         //getView().setPaintable(_imageCache as Graphics); // not implement in SWING :(
         drawCache();
         //set...(save); // restore
      }
      return _imageCache;
   }

   /** copy cached image to original */
   protected abstract void copyFromCache();

   protected void drawCache() { drawStaticPart(); }

   private void drawStaticPart() {
      MosaicsAnimatedModel<Void> model = getModel();

      List<BaseCell> notRotated;
      if (model.getRotatedElements().isEmpty()) {
         notRotated = model.getMatrix();
      } else {
         List<BaseCell> matrix = model.getMatrix();
         List<Integer> indexes = model.getRotatedElements().stream().map(cntxt -> cntxt.index).collect(Collectors.toList());
         notRotated = new ArrayList<>(matrix.size() - indexes.size());
         int i = 0;
         for (BaseCell cell : matrix) {
            if (!indexes.contains(i))
               notRotated.add(cell);
            ++i;
         }
      }
      draw(notRotated);
   }

   private void drawRotatedPart() {
      MosaicsAnimatedModel<Void> model = getModel();

      if (model.getRotatedElements().isEmpty())
         return;

      PenBorder pb = model.getPenBorder();
      // save
      int borderWidth = pb.getWidth();
      Color colorLight  = pb.getColorLight();
      Color colorShadow = pb.getColorShadow();
      // modify
      pb.setWidth(2 * borderWidth);
      pb.setColorLight(colorLight.darker(0.5));
      pb.setColorShadow(colorShadow.darker(0.5));

      List<BaseCell> matrix = model.getMatrix();
      List<BaseCell> rotatedCells = new ArrayList<>(model.getRotatedElements().size());
      for (RotatedCellContext cntxt : model.getRotatedElements())
         rotatedCells.add(matrix.get(cntxt.index));
      draw(rotatedCells);

      // restore
      pb.setWidth(borderWidth);
      pb.setColorLight(colorLight);
      pb.setColorShadow(colorShadow);
   }

   private void drawBodySomeCells() {
      _drawOnCache = USE_CACHE;
      _useBackgroundColor = true;
      if (USE_CACHE)
         copyFromCache();
      else
         drawStaticPart();

      _drawOnCache = false;
      _useBackgroundColor = false;
      drawRotatedPart();
   }

   /////////////////////////////////////////////////////////////////////////////////////////////////////
   //    custom implementations
   /////////////////////////////////////////////////////////////////////////////////////////////////////

   /** Moisac image view implementation over {@link javax.swing.Icon} */
   static class Icon extends MosaicsImg<javax.swing.Icon> {

      private IconSwing ico = new IconSwing(this);

      @Override
      protected javax.swing.Icon createImage() { return ico.create(); }

      @Override
      public void draw(Collection<BaseCell> modifiedCells) {
         draw(ico.getGraphics(), modifiedCells, null, _useBackgroundColor);
      }

      @Override
      protected void copyFromCache() {
       //drawImage(getImageCache(), 0, 0, getSize().width, getSize().height, null);
         throw new UnsupportedOperationException("not implemented...");
      }

      @Override
      public void close() {
         ico.close();
         super.close();
         ico = null;
      }

   }

   /** Mosaics image view implementation over {@link java.awt.Image} */
   static class Image extends MosaicsImg<java.awt.Image> {

      private ImageAwt img = new ImageAwt(this);

      @Override
      protected java.awt.Image createImage() { return img.create(); }

      @Override
      public void draw(Collection<BaseCell> modifiedCells) {
         img.draw(g -> draw(g, modifiedCells, null, _useBackgroundColor));
     }

      @Override
      protected void copyFromCache() {
       //drawImage(getImageCache(), 0, 0, getSize().width, getSize().height, null);
         throw new UnsupportedOperationException("not implemented...");
      }

   }

   /** Mosaics image controller implementation for {@link Icon} */
   public static class ControllerIcon extends AMosaicController<javax.swing.Icon, Void, MosaicsImg.Icon, MosaicsAnimatedModel<Void>> {
      public ControllerIcon() {
         super(new MosaicsImg.Icon());
         addModelTransformer(new MosaicRotateTransformer());
      }
   }

   /** Mosaics image controller implementation for {@link Image} */
   public static class ControllerImage extends AMosaicController<java.awt.Image, Void, MosaicsImg.Image, MosaicsAnimatedModel<Void>> {
      public ControllerImage() {
         super(new MosaicsImg.Image());
         addModelTransformer(new MosaicRotateTransformer());
      }
   }

   ////////////// TEST //////////////
   public static void main(String[] args) {
      Random rnd = ThreadLocalRandom.current();
      TestDrawing.testApp(() ->
//      // test single
//      Arrays.asList(new MosaicsImg.ControllerImage() { { setMosaicType(EMosaic.eMosaicSquare1); }})

         // test all
         Stream.of(EMosaic.values())

//               // variant 1
//               .map(e -> Stream.of(new MosaicsImg.ControllerIcon () { { setMosaicType(e); }},
//                                   new MosaicsImg.ControllerImage() { { setMosaicType(e); }}))
//               .flatMap(x -> x)

               // variant 2
               .map(e ->  rnd.nextBoolean()
                           ? new MosaicsImg.ControllerIcon () { { setMosaicType(e); }}
                           : new MosaicsImg.ControllerImage() { { setMosaicType(e); }})

               .collect(Collectors.toList())
      );
   }
   //////////////////////////////////

}
