package fmg.jfx.draw.img;

import fmg.core.img.AnimatedImg;
import fmg.core.img.StaticImg;
import javafx.application.Platform;

final class StaticRotateImgConsts {

   static {
      StaticImg.DEFERR_INVOKER = doRun -> Platform.runLater(doRun);
      AnimatedImg.TIMER_CREATOR = () -> new fmg.jfx.utils.Timer();
   }

   public static void init() {
      // implicit call static block
   }

}
