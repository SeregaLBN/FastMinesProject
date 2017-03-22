package fmg.swing;

import javax.swing.JApplet;

import fmg.swing.mosaic.MosaicController;

public class Applet extends JApplet {
   private static final long serialVersionUID = -8406501303115617115L;

   @Override
   public void init() {
      try (MosaicController m = new MosaicController()) {
         setContentPane(m.getView().getControl());
      }
   }

}
