package fmg.swing.draw.img;

import java.util.stream.Collectors;
import java.util.stream.Stream;

import fmg.common.Color;
import fmg.common.Pair;
import fmg.common.geom.PointDouble;
import fmg.core.img.AImageController;
import fmg.core.img.MosaicsGroupModel;
import fmg.core.types.EMosaicGroup;

/**
 * Representable {@link fmg.core.types.EMosaicGroup} as image
 * <br>
 * SWING impl
 *
 * @param <TImage> SWING specific image: {@link java.awt.Image} or {@link javax.swing.Icon})
 **/
public abstract class MosaicsGroupImg<TImage> extends MosaicsSkillOrGroupView<TImage, MosaicsGroupModel> {

   /** @param group - may be null. if Null - representable image of EMosaicGroup.class */
   protected MosaicsGroupImg(EMosaicGroup group) { super(new MosaicsGroupModel(group)); }

   @Override
   protected Stream<Pair<Color, Stream<PointDouble>>> getCoords() { return getModel().getCoords(); }

   /////////////////////////////////////////////////////////////////////////////////////////////////////
   //    custom implementations
   /////////////////////////////////////////////////////////////////////////////////////////////////////

   /** MosaicsGroup image view implementation over {@link javax.swing.Icon} */
   static class Icon extends MosaicsGroupImg<javax.swing.Icon> {

      private IconSwing<MosaicsGroupModel> ico = new IconSwing<>(this);

      public Icon(EMosaicGroup group) { super(group); }

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

   /** MosaicsGroup image view implementation over {@link java.awt.Image} */
   static class Image extends MosaicsGroupImg<java.awt.Image> {

      private ImageAwt<MosaicsGroupModel> img = new ImageAwt<>(this);

      public Image(EMosaicGroup group) { super(group); }

      @Override
      protected java.awt.Image createImage() { return img.createImage(); }

      @Override
      protected void drawBody() { img.draw(g -> draw(g)); }

   }

   /** MosaicsGroup image controller implementation for {@link Icon} */
   public static class ControllerIcon extends AImageController<javax.swing.Icon, MosaicsGroupImg.Icon, MosaicsGroupModel> {
      public ControllerIcon(EMosaicGroup group) { super(new MosaicsGroupImg.Icon(group)); }
   }

   /** MosaicsGroup image controller implementation for {@link Image} */
   public static class ControllerImage extends AImageController<java.awt.Image, MosaicsGroupImg.Image, MosaicsGroupModel> {
      public ControllerImage(EMosaicGroup group) { super(new MosaicsGroupImg.Image(group)); }
   }

   ////////////// TEST //////////////
   public static void main(String[] args) {
      TestDrawing.testApp(() ->
         Stream.concat(Stream.of((EMosaicGroup)null),
                       Stream.of(EMosaicGroup.values()))
               .map(e -> new Pair<>(new MosaicsGroupImg.ControllerIcon (e),
                                    new MosaicsGroupImg.ControllerImage(e)))
               .flatMap(x -> Stream.of(x.first, x.second))
               .collect(Collectors.toList())
      );
   }
   //////////////////////////////////

}
