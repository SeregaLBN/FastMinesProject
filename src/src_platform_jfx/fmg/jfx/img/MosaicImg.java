package fmg.jfx.img;

import java.util.Collection;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import fmg.core.img.MosaicAnimatedModel;
import fmg.core.mosaic.MosaicImageController;
import fmg.core.mosaic.cells.BaseCell;
import fmg.core.types.EMosaic;
import fmg.jfx.mosaic.MosaicJfxView;

/**
 * Representable {@link fmg.core.types.EMosaic} as image
 * <br>
 * base JavaFX impl
 *
 * @param <TImage> SWING specific image: {@link javafx.scene.image.Image} or {@link javafx.scene.canvas.Canvas}
 */
public abstract class MosaicImg<TImage>
      extends MosaicJfxView<TImage, Void, MosaicAnimatedModel<Void>> {

   protected boolean _useBackgroundColor = true;

   protected MosaicImg() {
      super(new MosaicAnimatedModel<Void>());
   }

   @Override
   protected void drawBody() {
      // super.drawBody(); // !hide super implementation

      MosaicAnimatedModel<Void> model = getModel();

      _useBackgroundColor = true;
      switch (model.getRotateMode()) {
      case fullMatrix:
         drawModified(model.getMatrix());
         break;
      case someCells:
         // draw static part
         drawModified(model.getNotRotatedCells());

         // draw rotated part
         _useBackgroundColor = false;
         model.getRotatedCells(rotatedCells -> drawModified(rotatedCells));
         break;
      }
   }

   @Override
   public void close() {
      getModel().close();
      super.close();
   }

   /////////////////////////////////////////////////////////////////////////////////////////////////////
   // custom implementations
   /////////////////////////////////////////////////////////////////////////////////////////////////////

   /** Moisac image view implementation over {@link javafx.scene.canvas.javafx.scene.canvas.Canvas} */
   static class Canvas extends MosaicImg<javafx.scene.canvas.Canvas> {

      private CanvasJfx canvas = new CanvasJfx(this);

      @Override
      protected javafx.scene.canvas.Canvas createImage() {
         return canvas.create();
      }

      @Override
      protected void drawModified(Collection<BaseCell> modifiedCells) {
         drawJfx(canvas.getGraphics(), modifiedCells, null, _useBackgroundColor);
      }

   }

   /** Mosaics image view implementation over {@link javafx.scene.image.Image} */
   static class Image extends MosaicImg<javafx.scene.image.Image> {

      private ImageJfx img = new ImageJfx(this);

      @Override
      protected javafx.scene.image.Image createImage() {
         img.createCanvas();
         return null; // img.createImage(); // fake empty image
      }

      @Override
      protected void drawModified(Collection<BaseCell> modifiedCells) {
         drawJfx(img.getGraphics(), modifiedCells, null, _useBackgroundColor);
         setImage(img.createImage()); // real image
      }

   }

   /** Mosaic image controller implementation for {@link Canvas} */
   public static class ControllerCanvas extends MosaicImageController<javafx.scene.canvas.Canvas, MosaicImg.Canvas> {

      public ControllerCanvas() {
         super(new MosaicImg.Canvas());
      }

      @Override
      public void close() {
         getView().close();
         super.close();
      }

   }

   /** Mosaic image controller implementation for {@link Image} */
   public static class ControllerImage extends MosaicImageController<javafx.scene.image.Image, MosaicImg.Image> {

      public ControllerImage() {
         super(new MosaicImg.Image());
      }

      @Override
      public void close() {
         getView().close();
         super.close();
      }

   }

   ////////////// TEST //////////////
   public static void main(String[] args) {
      TestDrawing.testApp(() ->
// // test single
// Arrays.asList(new MosaicImg.ControllerImage() { { setMosaicType(EMosaic.eMosaicSquare1); }})

      // test all
      Stream.of(EMosaic.values())

// // variant 1
// .map(e -> Stream.of(new MosaicImg.ControllerCanvas() { { setMosaicType(e); }},
// new MosaicImg.ControllerImage () { { setMosaicType(e); }}))
// .flatMap(x -> x)

            // variant 2
            .map(e -> ThreadLocalRandom.current().nextBoolean()
                  ? new MosaicImg.ControllerCanvas() {
                     {
                        setMosaicType(e);
                     }
                  }
                  : new MosaicImg.ControllerImage() {
                     {
                        setMosaicType(e);
                     }
                  })
            .collect(Collectors.toList()));
   }
   //////////////////////////////////

}
