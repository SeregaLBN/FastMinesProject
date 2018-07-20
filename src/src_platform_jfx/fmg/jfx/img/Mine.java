package fmg.jfx.img;

import java.util.Arrays;

import fmg.core.img.LogoModel;

/** Mine image on the playing field */
public final class Mine {

   /** Mine image controller implementation for {@link Logo.Canvas} */
   public static class ControllerCanvas extends Logo.ControllerCanvas {
      public ControllerCanvas() { LogoModel.toMineModel(getModel()); }
   }

   /** Mine image controller implementation for {@link Logo.Image} */
   public static class ControllerImage extends Logo.ControllerImage {
      public ControllerImage() { LogoModel.toMineModel(getModel()); }
   }

   ////////////// TEST //////////////
   public static void main(String[] args) {
      TestDrawing.testApp(() -> Arrays.asList(new Mine.ControllerCanvas()
                                            , new Mine.ControllerImage()
                                            , new Mine.ControllerCanvas()
                                            , new Mine.ControllerImage()
                         ));
   }
   //////////////////////////////////

}
