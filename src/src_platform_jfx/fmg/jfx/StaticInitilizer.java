package fmg.jfx;

import javafx.application.Platform;

import fmg.common.notyfier.NotifyPropertyChanged;
import fmg.core.img.AnimatedImgController;
import fmg.jfx.draw.img.Animator;

public final class StaticInitilizer {

   static {
      NotifyPropertyChanged.DEFERR_INVOKER = doRun -> Platform.runLater(doRun);
      AnimatedImgController.GET_ANIMATOR = () -> Animator.getSingleton();
   }

   public static void init() {
      // implicit call static block
   }

}
