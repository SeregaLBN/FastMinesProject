package fmg.core.mosaic.draw;

import fmg.core.mosaic.cells.BaseCell;

/**
 * Interface for drawing cell
 *
 * @param <TPaintable> see {@link IPaintable}
 * @param <TImage> plaform specific view/image/picture or other display context/canvas/window/panel
 * @param <TPaintContext> see {@link MosaicDrawModel}
 */
public interface ICellPaint<TPaintable extends IPaintable, TImage, TPaintContext extends MosaicDrawModel<TImage>> {

   void paint(BaseCell cell, TPaintable p, TPaintContext paintContext);

}
