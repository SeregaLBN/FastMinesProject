package fmg.core.mosaic;

import java.util.Collection;

import fmg.core.mosaic.cells.BaseCell;

/** MVC view interface of mosaic */
public interface IMosaicView extends AutoCloseable {

   /** make to redraw all mosaic field cells */
   void invalidate();

   /** make to redraw
    * @param modifiedCells cells to invalidate */
   void invalidate(Collection<BaseCell> modifiedCells);

}
