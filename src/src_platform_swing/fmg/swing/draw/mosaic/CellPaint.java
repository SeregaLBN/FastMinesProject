package fmg.swing.draw.mosaic;

import fmg.core.mosaic.cells.BaseCell;
import fmg.core.mosaic.draw.ICellPaint;
import fmg.core.mosaic.draw.IPaintable;

/**
 * Class for drawing cell
 *
 * @param <TPaintable> see {@link IPaintable}
 * @param <TImage> SWING specific image: {@link java.awt.Image} or {@link javax.swing.Icon}
 */
public abstract class CellPaint<TPaintable extends IPaintable, TImage> implements ICellPaint<TPaintable, TImage, PaintSwingContext<TImage>> {

   @Override
   public abstract void paint(BaseCell cell, TPaintable p, PaintSwingContext<TImage> paintContext);

   protected abstract void paintBorder(BaseCell cell, TPaintable p, PaintSwingContext<TImage> paintContext);

   /** draw border lines */
   protected abstract void paintBorderLines(BaseCell cell, TPaintable p, PaintSwingContext<TImage> paintContext);

   protected abstract void paintComponent(BaseCell cell, TPaintable p, PaintSwingContext<TImage> paintContext);

   /** залить ячейку нужным цветом */
   protected abstract void paintComponentBackground(BaseCell cell, TPaintable p, PaintSwingContext<TImage> paintContext);

   /** draw image Flag or image Mine */
   protected abstract void paintImage(BaseCell cell, TPaintable p, PaintSwingContext<TImage> paintContext, TImage img);

}