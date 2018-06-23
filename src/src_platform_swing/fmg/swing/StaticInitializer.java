package fmg.swing;

import fmg.common.ui.Factory;
import fmg.swing.draw.img.Animator;
import fmg.swing.utils.Timer;

public final class StaticInitializer {

   static {
      Factory.DEFERR_INVOKER = doRun -> javax.swing.SwingUtilities.invokeLater(doRun);
      Factory.GET_ANIMATOR = () -> Animator.getSingleton();
      Factory.TIMER_CREATOR = () -> new Timer();
   }

   public static void init() {
      // implicit call static block
   }

}
