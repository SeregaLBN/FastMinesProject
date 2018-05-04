package fmg.swing.draw.img;

import java.util.stream.Collectors;
import java.util.stream.Stream;

import fmg.common.Color;
import fmg.common.Pair;
import fmg.common.geom.PointDouble;
import fmg.core.img.BurgerMenuModel;
import fmg.core.img.MosaicsGroupController;
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
   static class Image extends MosaicsGroupImg<java.awt.Image> {

      private ImageAwt img = new ImageAwt(this);

      public Image(EMosaicGroup group) { super(group); }

      @Override
      protected java.awt.Image createImage() { return img.create(); }

      @Override
      protected void drawBody() { img.draw(g -> draw(g)); }

   }

   /** MosaicsGroup image controller implementation for {@link Icon} */
   public static class ControllerIcon extends MosaicsGroupController<javax.swing.Icon, MosaicsGroupImg.Icon> {
      public ControllerIcon(EMosaicGroup group) {
         super(new MosaicsGroupImg.Icon(group));
         BurgerMenuModel bm = getView().getBurgerMenuModel();
         bm.setShow(group == null);
      }
   }

   /** MosaicsGroup image controller implementation for {@link Image} */
   public static class ControllerImage extends MosaicsGroupController<java.awt.Image, MosaicsGroupImg.Image> {
      public ControllerImage(EMosaicGroup group) {
         super(new MosaicsGroupImg.Image(group));
         BurgerMenuModel bm = getView().getBurgerMenuModel();
         bm.setShow(group == null);
      }
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
