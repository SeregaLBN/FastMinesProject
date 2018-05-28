package fmg.swing;

import fmg.swing.mosaic.MosaicJPanelController;

@SuppressWarnings("deprecation")
public class Applet extends javax.swing.JApplet {
   private static final long serialVersionUID = 1;

   @Override
   public void init() {
      try (MosaicJPanelController m = new MosaicJPanelController()) {
         setContentPane(m.getViewPanel());
      }
   }

}
