package fmg.swing.draw.img;

import javax.swing.SwingUtilities;

import fmg.core.img.AnimatedImgController;
import fmg.core.img.ImageController;

public final class StaticInitilizer {

   static {
      ImageController.DEFERR_INVOKER = doRun -> SwingUtilities.invokeLater(doRun);
      AnimatedImgController.GET_ANIMATOR = () -> Animator.getSingleton();
   }

   public static void init() {
      // implicit call static block
   }

}
