package fmg.swing.draw.img;

import javax.swing.SwingUtilities;

import fmg.core.img.AAnimatedImgController;
import fmg.core.img.AImageController;

final class StaticInitilizer {

   static {
      AImageController.DEFERR_INVOKER = doRun -> SwingUtilities.invokeLater(doRun);
      AAnimatedImgController.GET_ANIMATOR = () -> Animator.getSingleton();
   }

   public static void init() {
      // implicit call static block
   }

}
