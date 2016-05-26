package fmg.core.mosaic.draw;

import fmg.core.mosaic.cells.BaseCell;

/**
 * Interface for drawing cell
 *
 * @param <TPaintable> see {@link IPaintable}
 * @param <TImage> plaform specific image
 * @param <TPaintContext> see {@link PaintCellContext}
 */
public interface ICellPaint<TPaintable extends IPaintable, TImage, TPaintContext extends PaintCellContext<TImage>> {

   TPaintContext getPaintContext();
   void setPaintContext(TPaintContext paintContext);

   void paint(BaseCell cell, TPaintable p);

   void paintBorder(BaseCell cell, TPaintable p);

   /** draw border lines */
   void paintBorderLines(BaseCell cell, TPaintable p);

   void paintComponent(BaseCell cell, TPaintable p);

   /** залить ячейку нужным цветом */
   void paintComponentBackground(BaseCell cell, TPaintable p);
}
