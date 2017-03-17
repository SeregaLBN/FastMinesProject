package fmg.swing;

import javax.swing.JApplet;

import fmg.swing.mosaic.Mosaic;

public class Applet extends JApplet {
   private static final long serialVersionUID = -8406501303115617115L;

   @Override
   public void init() {
      try (Mosaic.MosaicController m = new Mosaic.MosaicController()) {
         setContentPane(m.getView().getControl());
      }
   }

}
