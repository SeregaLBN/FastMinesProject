package fmg.swing.draw.mosaic.graphics;

import java.awt.Graphics;

import fmg.core.mosaic.draw.IPaintable;

/** Container for {@link java.awt.Graphics} */
public class PaintableGraphics implements IPaintable {
   private final Graphics _graphics;

   public PaintableGraphics(Graphics graphics) { _graphics = graphics; }

   public Graphics getGraphics() {
      return _graphics;
   }

}
