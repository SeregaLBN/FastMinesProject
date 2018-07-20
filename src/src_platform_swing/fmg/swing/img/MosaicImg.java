package fmg.swing.img;

import java.util.Collection;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import fmg.core.img.MosaicAnimatedModel;
import fmg.core.mosaic.AMosaicImageController;
import fmg.core.mosaic.cells.BaseCell;
import fmg.core.types.EMosaic;
import fmg.swing.mosaic.AMosaicViewSwing;

/**
 * Representable {@link fmg.core.types.EMosaic} as image
 * <br>
 * base SWING impl
 *
 * @param <TImage> SWING specific image: {@link java.awt.Image} or {@link javax.swing.Icon}
 */
public abstract class MosaicImg<TImage>
                extends AMosaicViewSwing<TImage, Void, MosaicAnimatedModel<Void>>
{

   protected boolean _useBackgroundColor = true;

   protected MosaicImg() {
      super(new MosaicAnimatedModel<Void>());
   }

   @Override
   protected void drawBody() {
      //super.drawBody(); // !hide super implementation

      MosaicAnimatedModel<Void> model = getModel();

      _useBackgroundColor = true;
      switch (model.getRotateMode()) {
      case fullMatrix:
         draw(model.getMatrix());
         break;
      case someCells:
         // draw static part
         draw(model.getNotRotatedCells());

         // draw rotated part
         _useBackgroundColor = false;
         model.getRotatedCells(rotatedCells -> draw(rotatedCells));
         break;
      }
   }

   @Override
   public void close() {
      getModel().close();
      super.close();
   }

   /////////////////////////////////////////////////////////////////////////////////////////////////////
   //    custom implementations
   /////////////////////////////////////////////////////////////////////////////////////////////////////

   /** Moisac image view implementation over {@link javax.swing.Icon} */
   static class Icon extends MosaicImg<javax.swing.Icon> {

      private IconSwing ico = new IconSwing(this);

      @Override
      protected javax.swing.Icon createImage() { return ico.create(); }

      @Override
      public void draw(Collection<BaseCell> modifiedCells) {
         draw(ico.getGraphics(), modifiedCells, null, _useBackgroundColor);
      }

      @Override
      public void close() {
         ico.close();
         super.close();
         ico = null;
      }

   }

   /** Mosaics image view implementation over {@link java.awt.Image} */
   static class Image extends MosaicImg<java.awt.Image> {

      private ImageAwt img = new ImageAwt(this);

      @Override
      protected java.awt.Image createImage() { return img.create(); }

      @Override
      public void draw(Collection<BaseCell> modifiedCells) {
         img.draw(g -> draw(g, modifiedCells, null, _useBackgroundColor));
      }

   }

   /** Mosaic image controller implementation for {@link Icon} */
   public static class ControllerIcon extends AMosaicImageController<javax.swing.Icon, MosaicImg.Icon> {

      public ControllerIcon() {
         super(new MosaicImg.Icon());
      }

      @Override
      public void close() {
         getView().close();
         super.close();
      }

   }

   /** Mosaic image controller implementation for {@link Image} */
   public static class ControllerImage extends AMosaicImageController<java.awt.Image, MosaicImg.Image> {

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
//         // test single
//         Arrays.asList(new MosaicImg.ControllerImage() { { setMosaicType(EMosaic.eMosaicSquare1); }})

         // test all
         Stream.of(EMosaic.values())

//               // variant 1
//               .map(e -> Stream.of(new MosaicImg.ControllerIcon () { { setMosaicType(e); }},
//                                   new MosaicImg.ControllerImage() { { setMosaicType(e); }}))
//               .flatMap(x -> x)

               // variant 2
               .map(e -> ThreadLocalRandom.current().nextBoolean()
                           ? new MosaicImg.ControllerIcon () { { setMosaicType(e); }}
                           : new MosaicImg.ControllerImage() { { setMosaicType(e); }}
                   )
               .collect(Collectors.toList())
      );
   }
   //////////////////////////////////

}
