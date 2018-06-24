package fmg.jfx;

import fmg.common.ui.Factory;
import fmg.jfx.draw.img.Animator;
import fmg.jfx.utils.Timer;

public final class StaticInitializer {

   static {
      Factory.DEFERR_INVOKER = javafx.application.Platform::runLater;
      Factory.GET_ANIMATOR = Animator::getSingleton;
      Factory.TIMER_CREATOR = Timer::new;
   }

   public static void init() {
      // implicit call static block
   }

}
