using System.Collections.Generic;
using fmg.core.img;
using fmg.core.mosaic.cells;

namespace fmg.core.mosaic {

    /// <summary> MVC view interface of mosaic </summary>
    /// <typeparam name="TImage">platform specific view/image/picture or other display context/canvas/window/panel</typeparam>
    /// <typeparam name="TImageInner">platform specific view/image/picture or other display context/canvas/window/panel</typeparam>
    /// <typeparam name="TMosaicModel">mosaic data model</typeparam>
    public interface IMosaicView<TImage, TImageInner, TMosaicModel>
                    : IImageView<TImage, TMosaicModel>
        where TImage : class
        where TImageInner : class
        where TMosaicModel : MosaicDrawModel<TImageInner>
    {
        /// <summary> Mark the cells needed for the repainting.
        /// Performs a call to the draw method (synchronously or asynchronously or implicitly, depending on the implementation) </summary>
        /// <param name="modifiedCells"> cells to invalidate. null value - make to redraw all mosaic field cells </param>
        void Invalidate(ICollection<BaseCell> modifiedCells);

    }

}
