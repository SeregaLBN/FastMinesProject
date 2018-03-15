package fmg.swing.draw.img;

import java.util.Arrays;

import fmg.common.HSV;
import fmg.core.img.LogoModel;

/** Mine image on the playing field */
public final class Mine {

   /** Mine image controller implementation for {@link Logo.Icon} */
   public static class ControllerIcon extends Logo.ControllerIcon {
      public ControllerIcon() { updateModel(getModel()); }
   }

   /** Mine image controller implementation for {@link Logo.Image} */
   public static class ControllerImage extends Logo.ControllerImage {
      public ControllerImage() { updateModel(getModel()); }
   }

   private static void updateModel(LogoModel m) {
      m.setUseGradient(false);
      m.setSize(150);
      m.setPadding(50);
      for (HSV item : m.getPalette())
         //item.v = 75;
         item.grayscale();
   }

   ////////////// TEST //////////////
   public static void main(String[] args) {
      TestDrawing.testApp(() -> Arrays.asList(new Mine.ControllerIcon()
                                            , new Mine.ControllerImage()
                                            , new Mine.ControllerIcon()
                                            , new Mine.ControllerImage()
                         ));
   }
   //////////////////////////////////

}
