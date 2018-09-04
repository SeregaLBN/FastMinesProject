package fmg.core.mosaic;

import java.util.Collection;

import fmg.core.img.IImageView;
import fmg.core.mosaic.cells.BaseCell;

/** MVC view interface of mosaic
 * @param <TImage> platform specific view/image/picture or other display context/canvas/window/panel
 * @param <TImageInner> platform specific view/image/picture or other display context/canvas/window/panel
 * @param <TMosaicModel> mosaic data model
 */
public interface IMosaicView<TImage, TImageInner, TMosaicModel extends MosaicDrawModel<TImageInner>>
       extends IImageView<TImage, TMosaicModel>
{

   /** Mark the cells needed for the repainting.
    * Performs a call to the draw method (synchronously or asynchronously or implicitly, depending on the implementation)
    * @param modifiedCells - cells to invalidate. null value - make to redraw all mosaic field cells */
   void invalidate(Collection<BaseCell> modifiedCells);

}
