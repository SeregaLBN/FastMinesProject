package fmg.swing.draw.img;

import javax.swing.SwingUtilities;

import fmg.core.img.RotatedImg;
import fmg.core.img.ImageProperties;

final class StaticRotateImgConsts {

   static {
      ImageProperties.DEFERR_INVOKER = doRun -> SwingUtilities.invokeLater(doRun);
      RotatedImg.TIMER_CREATOR = () -> new fmg.swing.utils.Timer();
   }

   public static void init() {
      // implicit call static block
   }

}
