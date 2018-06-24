package fmg.jfx.utils;

import fmg.common.ui.Factory;
import fmg.jfx.draw.img.Animator;

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
