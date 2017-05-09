package fmg.core.mosaic;

import java.util.Collection;

import fmg.common.geom.RectDouble;
import fmg.common.geom.SizeDouble;
import fmg.core.mosaic.cells.BaseCell;

/** MVC view interface of mosaic */
public interface IMosaicView extends AutoCloseable {

   /** Size of the View in pixels */
   SizeDouble getSize();

   /** Mark the cells needed for the repainting.
    * Performs a call to the Repaint method (synchronously or asynchronously or implicitly, depending on the implementation)
    * @param modifiedCells - cells to invalidate. null value - make to redraw all mosaic field cells */
   void invalidate(Collection<BaseCell> modifiedCells);

   /**
    * Redraw the required cells
    * @param modifiedCells - Cells to be redrawn. NULL - redraw the full mosaic, or only those that are included in the clipRegion.
    * @param clipRegion - Region for redrawing. NULL - Redraw everything that is specified in modifiedCells
    */
   void repaint(Collection<BaseCell> modifiedCells, RectDouble clipRegion);

}
