package fmg.swing;

import javax.swing.JApplet;

import fmg.swing.mosaic.MosaicControllerSwing;

public class Applet extends JApplet {
   private static final long serialVersionUID = -8406501303115617115L;

   @Override
   public void init() {
      try (MosaicControllerSwing m = new MosaicControllerSwing()) {
         setContentPane(m.getView().getControl());
      }
   }

}
