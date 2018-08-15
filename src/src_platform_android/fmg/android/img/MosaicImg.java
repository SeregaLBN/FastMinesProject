package fmg.android.img;

import java.util.Collection;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import fmg.core.img.IImageController;
import fmg.core.img.MosaicAnimatedModel;
import fmg.core.mosaic.MosaicImageController;
import fmg.core.mosaic.cells.BaseCell;
import fmg.core.types.EMosaic;
import fmg.android.mosaic.MosaicAndroidView;

/**
 * Representable {@link EMosaic} as image
 * <br>
 * base Android impl
 *
 * @param <TImage> Android specific image: {@link android.graphics.Bitmap}
 */
public abstract class MosaicImg<TImage>
                extends MosaicAndroidView<TImage, Void, MosaicAnimatedModel<Void>>
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
   //    custom implementations
   /////////////////////////////////////////////////////////////////////////////////////////////////////

   /** Moisac image view implementation over {@link android.graphics.Bitmap} */
   static class Bitmap extends MosaicImg<android.graphics.Bitmap> {

      private BmpCanvas wrap = new BmpCanvas();

      @Override
      protected android.graphics.Bitmap createImage() {
         return wrap.createImage(getModel().getSize());
      }

      @Override
      protected void drawModified(Collection<BaseCell> modifiedCells) {
         drawAndroid(wrap.getCanvas(), modifiedCells, null, _useBackgroundColor);
      }

      @Override
      public void close() {
         wrap.close();
      }

   }

   /** Mosaic image controller implementation for {@link Bitmap} */
   public static class ControllerBitmap extends MosaicImageController<android.graphics.Bitmap, Bitmap> {

      public ControllerBitmap() {
         super(new MosaicImg.Bitmap());
      }

      @Override
      public void close() {
         getView().close();
         super.close();
      }

   }

   ////////////// TEST //////////////
   public static List<IImageController<?,?,?>> testData() {
      return
//         // test single
//         Arrays.asList(new MosaicImg.ControllerImage() { { setMosaicType(EMosaic.eMosaicSquare1); }})

         // test all
         Stream.of(EMosaic.values())
               .map(e -> new MosaicImg.ControllerBitmap() { { setMosaicType(e); }})
               .collect(Collectors.toList());
   }
   //////////////////////////////////

}
