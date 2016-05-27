package fmg.swing.draw.mosaic;

import fmg.core.mosaic.cells.BaseCell;
import fmg.core.mosaic.draw.ICellPaint;
import fmg.core.mosaic.draw.IPaintable;
import fmg.swing.Cast;

/**
 * Class for drawing cell
 *
 * @param <TPaintable> see {@link IPaintable}
 * @param <TImage> SWING specific image: {@link java.awt.Image} or {@link javax.swing.Icon}
 */
public abstract class CellPaint<TPaintable extends IPaintable, TImage> implements ICellPaint<TPaintable, TImage, PaintSwingContext<TImage>> {
   protected PaintSwingContext<TImage> paintContext;

   @Override
   public PaintSwingContext<TImage> getPaintContext() {
      return paintContext;
   }
   @Override
   public void setPaintContext(PaintSwingContext<TImage> paintContext) {
      this.paintContext = paintContext;
   }

   protected void repaint(BaseCell cell) {
//       gContext.getOwner().paintImmediately(Cast.toRect(cell.getRcOuter()));
      paintContext.getOwner().repaint(Cast.toRect(cell.getRcOuter()));
   }

   @Override
   public abstract void paint(BaseCell cell, TPaintable p);

   @Override
   public abstract void paintBorder(BaseCell cell, TPaintable p);

   /** draw border lines */
   @Override
   public abstract void paintBorderLines(BaseCell cell, TPaintable p);

   @Override
   public abstract void paintComponent(BaseCell cell, TPaintable p);

   /** залить ячейку нужным цветом */
   @Override
   public abstract void paintComponentBackground(BaseCell cell, TPaintable p);

}