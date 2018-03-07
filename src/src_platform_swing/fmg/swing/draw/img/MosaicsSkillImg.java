package fmg.swing.draw.img;

import java.util.stream.Collectors;
import java.util.stream.Stream;

import fmg.common.Color;
import fmg.common.Pair;
import fmg.common.geom.PointDouble;
import fmg.core.img.AAnimatedImgController;
import fmg.core.img.MosaicsSkillModel;
import fmg.core.types.ESkillLevel;

/**
 * Representable {@link fmg.core.types.ESkillLevel} as image
 * <br>
 * SWING impl
 *
 * @param <TImage> SWING specific image: {@link java.awt.Image} or {@link javax.swing.Icon})
 **/
public abstract class MosaicsSkillImg<TImage> extends MosaicsSkillOrGroupView<TImage, MosaicsSkillModel> {

   /** @param skill - may be null. if Null - representable image of ESkillLevel.class */
   protected MosaicsSkillImg(ESkillLevel skill) { super(new MosaicsSkillModel(skill)); }

   @Override
   protected Stream<Pair<Color, Stream<PointDouble>>> getCoords() { return getModel().getCoords(); }

   /////////////////////////////////////////////////////////////////////////////////////////////////////
   //    custom implementations
   /////////////////////////////////////////////////////////////////////////////////////////////////////

   /** MosaicsSkill image view implementation over {@link javax.swing.Icon} */
   static class Icon extends MosaicsSkillImg<javax.swing.Icon> {

      private IconSwing<MosaicsSkillModel> ico = new IconSwing<>(this);

      public Icon(ESkillLevel skill) { super(skill); }

      @Override
      protected javax.swing.Icon createImage() { return ico.createImage(); }

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
   static class Image extends MosaicsSkillImg<java.awt.Image> {

      private ImageAwt<MosaicsSkillModel> img = new ImageAwt<>(this);

      public Image(ESkillLevel skill) { super(skill); }

      @Override
      protected java.awt.Image createImage() { return img.createImage(); }

      @Override
      protected void drawBody() { img.draw(g -> draw(g)); }

   }

   /** MosaicsSkill image controller implementation for {@link Icon} */
   public static class ControllerIcon extends AAnimatedImgController<javax.swing.Icon, MosaicsSkillImg.Icon, MosaicsSkillModel> {
      public ControllerIcon(ESkillLevel skill) { super(new MosaicsSkillImg.Icon(skill)); }
   }

   /** MosaicsSkill image controller implementation for {@link Image} */
   public static class ControllerImage extends AAnimatedImgController<java.awt.Image, MosaicsSkillImg.Image, MosaicsSkillModel> {
      public ControllerImage(ESkillLevel skill) { super(new MosaicsSkillImg.Image(skill)); }
   }

   ////////////// TEST //////////////
   public static void main(String[] args) {
      TestDrawing.testApp(() ->
         Stream.concat(Stream.of((ESkillLevel)null),
                       Stream.of(ESkillLevel.values()))
               .map(e -> new Pair<>(new MosaicsSkillImg.ControllerIcon (e),
                                    new MosaicsSkillImg.ControllerImage(e)))
               .flatMap(x -> Stream.of(x.first, x.second))
               .collect(Collectors.toList())
      );
   }
   //////////////////////////////////

}
