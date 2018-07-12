using System;
using System.Collections.Generic;
using fmg.common.geom;
using fmg.core.img;
using fmg.core.mosaic.cells;

namespace fmg.core.mosaic {

   /// <summary> MVC view interface of mosaic </summary>
   /// <typeparam name="TImage">plaform specific view/image/picture or other display context/canvas/window/panel</typeparam>
   /// <typeparam name="TImageInner">plaform specific view/image/picture or other display context/canvas/window/panel</typeparam>
   /// <typeparam name="TMosaicModel">mosaic data model</typeparam>
   public interface IMosaicView<TImage, TImageInner, TMosaicModel>
                   : IImageView<TImage, TMosaicModel>
      where TImage : class
      where TImageInner : class
      where TMosaicModel : MosaicDrawModel<TImageInner>
   {

      /// <summary> Mark the cells needed for the repainting.
      /// Performs a call to the {@link #draw} method (synchronously or asynchronously or implicitly, depending on the implementation) </summary>
      /// <param name="modifiedCells"> cells to invalidate. null value - make to redraw all mosaic field cells </param>
      void Invalidate(IEnumerable<BaseCell> modifiedCells);

      /// <summary> Redraw the required cells </summary>
      /// <param name="modifiedCells">Cells to be redrawn. NULL - redraw the full mosaic, or only those that are included in the clipRegion.</param>
      void Draw(IEnumerable<BaseCell> modifiedCells);

   }

}
