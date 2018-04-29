package fmg.core.mosaic;

import java.util.Collection;

import fmg.core.img.IImageView;
import fmg.core.mosaic.cells.BaseCell;
import fmg.core.mosaic.draw.MosaicDrawModel;

/** MVC view interface of mosaic
 * @param <TImage> plaform specific view/image/picture or other display context/canvas/window/panel
 * @param <TImage2> plaform specific view/image/picture or other display context/canvas/window/panel
 * @param <TMosaicModel> mosaic data model
 */
public interface IMosaicView<TImage, TImage2, TMosaicModel extends MosaicDrawModel<TImage2>>
       extends IImageView<TImage, TMosaicModel>
{

   /** Mark the cells needed for the repainting.
    * Performs a call to the {@link #draw} method (synchronously or asynchronously or implicitly, depending on the implementation)
    * @param modifiedCells - cells to invalidate. null value - make to redraw all mosaic field cells */
   void invalidate(Collection<BaseCell> modifiedCells);

   /** Redraw the required cells
    * @param modifiedCells - Cells to be redrawn. NULL - redraw the full mosaic, or only those that are included in the clipRegion.
    */
   void draw(Collection<BaseCell> modifiedCells);

}
