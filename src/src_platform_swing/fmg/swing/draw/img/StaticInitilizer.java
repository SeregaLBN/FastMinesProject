package fmg.swing.draw.img;

import javax.swing.SwingUtilities;

import fmg.common.notyfier.NotifyPropertyChanged;
import fmg.core.img.AnimatedImgController;

public final class StaticInitilizer {

   static {
      NotifyPropertyChanged.DEFERR_INVOKER = doRun -> SwingUtilities.invokeLater(doRun);
      AnimatedImgController.GET_ANIMATOR = () -> Animator.getSingleton();
   }

   public static void init() {
      // implicit call static block
   }

}
