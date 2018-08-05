package fmg.swing.img;

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
 * SWING impl
 *
 * @param <TImage> SWING specific image: {@link java.awt.Image} or {@link javax.swing.Icon})
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

   /** MosaicsGroup image view implementation over {@link javax.swing.Icon} */
   static class Icon extends MosaicGroupImg<javax.swing.Icon> {

      private IconSwing ico = new IconSwing(this);

      public Icon(EMosaicGroup group) { super(group); }

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

   /** MosaicsGroup image view implementation over {@link java.awt.Image} */
   static class Image extends MosaicGroupImg<java.awt.Image> {

      private ImageAwt img = new ImageAwt(this);

      public Image(EMosaicGroup group) { super(group); }

      @Override
      protected java.awt.Image createImage() { return img.create(); }

      @Override
      protected void drawBody() { img.drawWrapper(g -> draw(g)); }

   }

   /** MosaicsGroup image controller implementation for {@link Icon} */
   public static class ControllerIcon extends MosaicGroupController<javax.swing.Icon, MosaicGroupImg.Icon> {

      public ControllerIcon(EMosaicGroup group) {
         super(group==null, new MosaicGroupImg.Icon(group));
      }

      @Override
      public void close() {
         getView().close();
         super.close();
      }

   }

   /** MosaicsGroup image controller implementation for {@link Image} */
   public static class ControllerImage extends MosaicGroupController<java.awt.Image, MosaicGroupImg.Image> {

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
               .map(e -> new Pair<>(new MosaicGroupImg.ControllerIcon (e),
                                    new MosaicGroupImg.ControllerImage(e)))
               .flatMap(x -> Stream.of(x.first, x.second))
               .collect(Collectors.toList())
      );
   }
   //////////////////////////////////

}
