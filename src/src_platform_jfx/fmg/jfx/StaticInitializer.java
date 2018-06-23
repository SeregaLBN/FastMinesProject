package fmg.jfx;

import fmg.common.ui.Factory;
import fmg.jfx.draw.img.Animator;
import fmg.jfx.utils.Timer;

public final class StaticInitializer {

   static {
      Factory.DEFERR_INVOKER = doRun -> javafx.application.Platform.runLater(doRun);
      Factory.GET_ANIMATOR = () -> Animator.getSingleton();
      Factory.TIMER_CREATOR = () -> new Timer();
   }

   public static void init() {
      // implicit call static block
   }

}
