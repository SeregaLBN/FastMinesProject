package fmg.android.img;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import fmg.common.Color;
import fmg.common.Pair;
import fmg.common.geom.PointDouble;
import fmg.core.img.IImageController;
import fmg.core.img.MosaicGroupController;
import fmg.core.img.MosaicGroupModel;
import fmg.core.types.EMosaicGroup;

/**
 * Representable {@link EMosaicGroup} as image
 * <br>
 * Android impl
 *
 * @param <TImage> Android specific image: {@link android.graphics.Bitmap})
 **/
public abstract class MosaicGroupImg<TImage> extends MosaicSkillOrGroupView<TImage, MosaicGroupModel> {

   /** @param group - may be null. if Null - representable image of EMosaicGroup.class */
   protected MosaicGroupImg(EMosaicGroup group) {
      super(new MosaicGroupModel(group));
   }

   @Override
   protected Stream<Pair<Color, Stream<PointDouble>>> getCoords() { return getModel().getCoords(); }

   @Override
   public void close() {
      getModel().close();
      super.close();
   }

   /////////////////////////////////////////////////////////////////////////////////////////////////////
   //    custom implementations
   /////////////////////////////////////////////////////////////////////////////////////////////////////

   /** MosaicsGroup image view implementation over {@link android.graphics.Bitmap} */
   static class Bitmap extends MosaicGroupImg<android.graphics.Bitmap> {

      private BmpCanvas wrap = new BmpCanvas();

      public Bitmap(EMosaicGroup group) { super(group); }

      @Override
      protected android.graphics.Bitmap createImage() {
         return wrap.createImage(getModel().getSize());
      }

      @Override
      protected void drawBody() {
         draw(wrap.getCanvas());
      }

      @Override
      public void close() {
         wrap.close();
      }

   }

   /** MosaicsGroup image controller implementation for {@link Bitmap} */
   public static class ControllerBitmap extends MosaicGroupController<android.graphics.Bitmap, Bitmap> {

      public ControllerBitmap(EMosaicGroup group) {
         super(group==null, new MosaicGroupImg.Bitmap(group));
      }

      @Override
      public void close() {
         getView().close();
         super.close();
      }

   }


   ////////////// TEST //////////////
   public static List<IImageController<?,?,?>> getTestData() {
      return Stream.concat(Stream.of((EMosaicGroup)null),
                       Stream.of(EMosaicGroup.values()))
               .map(e -> new Pair<>(new MosaicGroupImg.ControllerBitmap (e),
                                    new MosaicGroupImg.ControllerBitmap(e)))
               .flatMap(x -> Stream.of(x.first, x.second))
               .collect(Collectors.toList());
   }
   //////////////////////////////////

}
