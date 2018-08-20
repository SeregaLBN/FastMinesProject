package fmg.jfx.img;

import java.util.stream.Collectors;
import java.util.stream.Stream;

import fmg.common.Color;
import fmg.common.Pair;
import fmg.common.geom.PointDouble;
import fmg.core.img.MosaicSkillController;
import fmg.core.img.MosaicSkillModel;
import fmg.core.types.ESkillLevel;

/**
 * Representable {@link fmg.core.types.ESkillLevel} as image
 * <br>
 * JFX impl
 *
 * @param <TImage> JFX specific image: {@link javafx.scene.image.Image} or {@link javafx.scene.canvas.Canvas}
 **/
public abstract class MosaicSkillImg<TImage> extends MosaicSkillOrGroupView<TImage, MosaicSkillModel> {

   /** @param skill - may be null. if Null - representable image of ESkillLevel.class */
   protected MosaicSkillImg(ESkillLevel skill) {
      super(new MosaicSkillModel(skill));
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

   /** MosaicsSkill image view implementation over {@javafx.scene.canvas.Canvas} */
   public static class Canvas extends MosaicSkillImg<javafx.scene.canvas.Canvas> {

      private CanvasJfx canvas = new CanvasJfx(this);

      /** @param skill - may be null. if Null - representable image of ESkillLevel.class */
      public Canvas(ESkillLevel skill) { super(skill); }

      @Override
      protected javafx.scene.canvas.Canvas createImage() { return canvas.create(); }

      @Override
      protected void drawBody() { draw(canvas.getGraphics()); }

   }

   /** MosaicsSkill image view implementation over {@link javafx.scene.image.Image} */
   public static class Image extends MosaicSkillImg<javafx.scene.image.Image> {

      private ImageJfx img = new ImageJfx(this);

      /** @param skill - may be null. if Null - representable image of ESkillLevel.class */
      public Image(ESkillLevel skill) { super(skill); }

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

   /** MosaicsSkill image controller implementation for {@link Canvas} */
   public static class ControllerCanvas extends MosaicSkillController<javafx.scene.canvas.Canvas, MosaicSkillImg.Canvas> {

      public ControllerCanvas(ESkillLevel skill) {
         super(skill == null, new MosaicSkillImg.Canvas(skill));
      }

      @Override
      public void close() {
         getView().close();
         super.close();
      }

   }

   /** MosaicsSkill image controller implementation for {@link Image} */
   public static class ControllerImage extends MosaicSkillController<javafx.scene.image.Image, MosaicSkillImg.Image> {

      public ControllerImage(ESkillLevel skill) {
         super(skill == null, new MosaicSkillImg.Image(skill));
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
         Stream.concat(Stream.of((ESkillLevel)null),
                       Stream.of(ESkillLevel.values()))
               .map(e -> new Pair<>(new MosaicSkillImg.ControllerCanvas(e),
                                    new MosaicSkillImg.ControllerImage(e)))
               .flatMap(x -> Stream.of(x.first, x.second))
               .collect(Collectors.toList())
      );
   }
   //////////////////////////////////

}
