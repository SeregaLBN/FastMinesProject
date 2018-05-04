package fmg.swing;

import fmg.swing.mosaic.MosaicControllerSwing;

@SuppressWarnings("deprecation")
public class Applet extends javax.swing.JApplet {
   private static final long serialVersionUID = 1;

   @Override
   public void init() {
      try (MosaicControllerSwing m = new MosaicControllerSwing()) {
         setContentPane(m.getViewPanel());
      }
   }

}
