package fmg.swing.mosaic;

import java.awt.Graphics;
import java.awt.Rectangle;
import java.util.Collection;

import fmg.common.geom.RectDouble;
import fmg.core.mosaic.AMosaicView;
import fmg.core.mosaic.cells.BaseCell;
import fmg.core.mosaic.draw.ICellPaint;
import fmg.swing.Cast;
import fmg.swing.draw.mosaic.PaintSwingContext;
import fmg.swing.draw.mosaic.graphics.PaintableGraphics;

/** MVC: view. Abstract SWING implementation */
public abstract class AMosaicViewSwing<TImage> extends AMosaicView<PaintableGraphics, TImage, PaintSwingContext<TImage>> {

   protected PaintableGraphics createPaintableGraphics(Graphics g) {
      return new PaintableGraphics(null, g);
   }

   private Graphics _graphics;
   public Graphics getGraphics() {
      return _graphics;
   }
   public void setGraphics(Graphics graphics) {
      this._graphics = graphics;
   }

   protected boolean _alreadyPainted = false;
   protected void repaint(Collection<BaseCell> modifiedCells) {
      Graphics g = getGraphics();
      if (g == null)
         return;

      assert !_alreadyPainted;

      _alreadyPainted = true;
      try {
         PaintSwingContext<TImage> pc = getPaintContext();

         Rectangle rcFill = g.getClipBounds();
         if (pc.isUseBackgroundColor()) {
            // background color
            g.setColor(Cast.toColor(pc.getBackgroundColor().darker(0.2)));
            g.fillRect(rcFill.x, rcFill.y, rcFill.width, rcFill.height);
         }

         if (modifiedCells == null)
            modifiedCells = getMosaic().getMatrix(); // check to redraw all mosaic cells

         // paint cells
         g.setFont(pc.getFont());
         PaintableGraphics p = createPaintableGraphics(g);
         RectDouble clipBounds = Cast.toRectDouble(rcFill);
         ICellPaint<PaintableGraphics, TImage, PaintSwingContext<TImage>> cellPaint = getCellPaint();
         for (BaseCell cell: modifiedCells)
            if (cell.getRcOuter().Intersects(clipBounds)) // redraw only when needed - when the cells and update region intersect
               cellPaint.paint(cell, p, pc);
      } finally {
         _alreadyPainted = false;
      }
   }

}
