package fmg.swing.img;

import java.util.Arrays;

import fmg.core.img.LogoModel;

/** Mine image on the playing field */
public final class Mine {

   /** Mine image controller implementation for {@link Logo.Icon} */
   public static class ControllerIcon extends Logo.ControllerIcon {
      public ControllerIcon() { LogoModel.toMineModel(getModel()); }
   }

   /** Mine image controller implementation for {@link Logo.Image} */
   public static class ControllerImage extends Logo.ControllerImage {
      public ControllerImage() { LogoModel.toMineModel(getModel()); }
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
