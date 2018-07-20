package fmg.jfx.img;

import java.util.stream.Collectors;
import java.util.stream.Stream;

import fmg.common.Color;
import fmg.common.Pair;
import fmg.common.geom.PointDouble;
import fmg.core.img.MosaicGroupController;
import fmg.core.img.MosaicGroupModel;
import fmg.core.types.EMosaicGroup;

/**
 * Representable {@link fmg.core.types.EMosaicGroup} as image
 * <br>
 * JFX impl
 *
 * @param <TImage> JFX specific image: {@link javafx.scene.image.Image} or {@link javafx.scene.canvas.Canvas})
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

   /** MosaicsGroup image view implementation over {@link javafx.scene.canvas.Canvas} */
   static class Canvas extends MosaicGroupImg<javafx.scene.canvas.Canvas> {

      private CanvasJfx canvas = new CanvasJfx(this);

      /** @param skill - may be null. if Null - representable image of EMosaicGroup.class */
      public Canvas(EMosaicGroup group) { super(group); }

      @Override
      protected javafx.scene.canvas.Canvas createImage() { return canvas.create(); }

      @Override
      protected void drawBody() { draw(canvas.getGraphics()); }

   }

   /** MosaicsGroup image view implementation over {@link javafx.scene.image.Image} */
   static class Image extends MosaicGroupImg<javafx.scene.image.Image> {

      private ImageJfx img = new ImageJfx(this);

      /** @param skill - may be null. if Null - representable image of EMosaicGroup.class */
      public Image(EMosaicGroup group) { super(group); }

      @Override
      protected javafx.scene.image.Image createImage() {
         img.createCanvas();
         return null; // img.createImage(); // fake empty image
      }

      @Override
      protected void drawBody() {
         draw(img.getGraphics());
         setImage(img.createImage()); // real image
      }

   }

   /** MosaicsGroup image controller implementation for {@link Canvas} */
   public static class ControllerCanvas extends MosaicGroupController<javafx.scene.canvas.Canvas, MosaicGroupImg.Canvas> {

      public ControllerCanvas(EMosaicGroup group) {
         super(group==null, new MosaicGroupImg.Canvas(group));
      }

      @Override
      public void close() {
         getView().close();
         super.close();
      }

   }

   /** MosaicsGroup image controller implementation for {@link Image} */
   public static class ControllerImage extends MosaicGroupController<javafx.scene.image.Image, MosaicGroupImg.Image> {

      public ControllerImage(EMosaicGroup group) {
         super(group==null, new MosaicGroupImg.Image(group));
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
         Stream.concat(Stream.of((EMosaicGroup)null),
                       Stream.of(EMosaicGroup.values()))
               .map(e -> new Pair<>(new MosaicGroupImg.ControllerCanvas (e),
                                    new MosaicGroupImg.ControllerImage(e)))
               .flatMap(x -> Stream.of(x.first, x.second))
               .collect(Collectors.toList())
      );
   }
   //////////////////////////////////

}
