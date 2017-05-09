package fmg.swing.mosaic;

import java.awt.Graphics;
import java.awt.Rectangle;
import java.util.Collection;

import fmg.common.geom.RectDouble;
import fmg.common.geom.SizeDouble;
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

   private Graphics _paintable;
   public Graphics getPaintable() {
      return _paintable;
   }
   public void setPaintable(Graphics paintable) {
      this._paintable = paintable;
   }

   protected boolean _alreadyPainted = false;
   @Override
   public void repaint(Collection<BaseCell> modifiedCells, RectDouble clipRegion) {
      Graphics g = getPaintable();
      if (g == null)
         return;

      assert !_alreadyPainted;

      _alreadyPainted = true;
      try {
         PaintSwingContext<TImage> pc = getPaintContext();

         if (pc.isUseBackgroundColor()) {
            // background color
            g.setColor(Cast.toColor(pc.getBackgroundColor()));
            if (clipRegion == null) {
               Rectangle rcBounds = g.getClipBounds();
               if (rcBounds != null) {
                  g.fillRect(rcBounds.x, rcBounds.y, rcBounds.width, rcBounds.height);
               } else {
                  SizeDouble sz = getSize();
                  g.fillRect(0, 0, (int)sz.width, (int)sz.height);
               }
            } else {
               g.fillRect((int)clipRegion.x, (int)clipRegion.y, (int)clipRegion.width, (int)clipRegion.height);
            }
         }

         if (modifiedCells == null)
            modifiedCells = getMosaic().getMatrix(); // check to redraw all mosaic cells

         // paint cells
         g.setFont(pc.getFont());
         PaintableGraphics p = createPaintableGraphics(g);
         ICellPaint<PaintableGraphics, TImage, PaintSwingContext<TImage>> cellPaint = getCellPaint();
         double padX = pc.getPadding().left, padY = pc.getPadding().top;
         for (BaseCell cell: modifiedCells) {
            if ((clipRegion == null) || cell.getRcOuter().moveXY(padX, padY).intersection(clipRegion)) // redraw only when needed - when the cells and update region intersect
               cellPaint.paint(cell, p, pc);
         }
      } finally {
         _alreadyPainted = false;
      }
   }

   @Override
   public void close() {
      super.close();
      setPaintable(null);
   }

}
