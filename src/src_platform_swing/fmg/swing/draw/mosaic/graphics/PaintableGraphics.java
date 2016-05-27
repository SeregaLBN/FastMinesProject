package fmg.swing.draw.mosaic.graphics;

import java.awt.Graphics;

import javax.swing.JComponent;

import fmg.core.mosaic.draw.IPaintable;

/** Container for {@link java.awt.Graphics} */
public class PaintableGraphics implements IPaintable {
   private final Graphics _graphics;
   private final JComponent _owner; // TODO: try exclude..

   public PaintableGraphics(JComponent owner, Graphics graphics) {
      _owner = owner;
      _graphics = graphics;
   }

   public JComponent getOwner() {
      return _owner;
   }

   public Graphics getGraphics() {
      return _graphics;
   }

}
