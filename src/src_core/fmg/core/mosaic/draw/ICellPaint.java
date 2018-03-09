package fmg.core.mosaic.draw;

import fmg.core.mosaic.cells.BaseCell;

/**
 * Interface for drawing cell
 *
 * @param <TPaintable> see {@link IPaintable}
 * @param <TImage> plaform specific image or picture or other display context/canvas/window/panel
 * @param <TPaintContext> see {@link PaintContext}
 */
public interface ICellPaint<TPaintable extends IPaintable, TImage, TPaintContext extends PaintContext<TImage>> {

   void paint(BaseCell cell, TPaintable p, TPaintContext paintContext);

}
