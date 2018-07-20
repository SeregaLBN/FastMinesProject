package fmg.swing.img;

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
 * SWING impl
 *
 * @param <TImage> SWING specific image: {@link java.awt.Image} or {@link javax.swing.Icon})
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

   /** MosaicsSkill image view implementation over {@link javax.swing.Icon} */
   static class Icon extends MosaicSkillImg<javax.swing.Icon> {

      private IconSwing ico = new IconSwing(this);

      public Icon(ESkillLevel skill) { super(skill); }

      @Override
      protected javax.swing.Icon createImage() { return ico.create(); }

      @Override
      protected void drawBody() { draw(ico.getGraphics()); }

      @Override
      public void close() {
         ico.close();
         super.close();
         ico = null;
      }

   }

   /** MosaicsSkill image view implementation over {@link java.awt.Image} */
   static class Image extends MosaicSkillImg<java.awt.Image> {

      private ImageAwt img = new ImageAwt(this);

      public Image(ESkillLevel skill) { super(skill); }

      @Override
      protected java.awt.Image createImage() { return img.create(); }

      @Override
      protected void drawBody() { img.draw(g -> draw(g)); }

   }

   /** MosaicsSkill image controller implementation for {@link Icon} */
   public static class ControllerIcon extends MosaicSkillController<javax.swing.Icon, MosaicSkillImg.Icon> {

      public ControllerIcon(ESkillLevel skill) {
         super(skill == null, new MosaicSkillImg.Icon(skill));
      }

      @Override
      public void close() {
         getView().close();
         super.close();
      }

   }

   /** MosaicsSkill image controller implementation for {@link Image} */
   public static class ControllerImage extends MosaicSkillController<java.awt.Image, MosaicSkillImg.Image> {

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
               .map(e -> new Pair<>(new MosaicSkillImg.ControllerIcon (e),
                                    new MosaicSkillImg.ControllerImage(e)))
               .flatMap(x -> Stream.of(x.first, x.second))
               .collect(Collectors.toList())
      );
   }
   //////////////////////////////////

}
