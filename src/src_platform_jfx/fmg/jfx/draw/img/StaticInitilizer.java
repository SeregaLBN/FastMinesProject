package fmg.jfx.draw.img;

import fmg.core.img.AnimatedImgController;
import fmg.core.img.ImageController;
import javafx.application.Platform;

public final class StaticInitilizer {

   static {
      ImageController.DEFERR_INVOKER = doRun -> Platform.runLater(doRun);
      AnimatedImgController.GET_ANIMATOR = () -> Animator.getSingleton();
   }

   public static void init() {
      // implicit call static block
   }

}
