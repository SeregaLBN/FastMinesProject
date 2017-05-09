using System;
using System.Collections.Generic;
using fmg.common.geom;
using fmg.core.mosaic.cells;

namespace fmg.core.mosaic {

   /// <summary> MVC view interface of mosaic </summary>
   public interface IMosaicView : IDisposable {

      /// <summary> Size of the View in pixels </summary>
      SizeDouble Size { get; }

      /// <summary> Mark the cells needed for the repainting.
      /// Performs a call to the Repaint method (synchronously or asynchronously or implicitly, depending on the implementation) </summary>
      /// <param name="modifiedCells"> cells to invalidate. null value - make to redraw all mosaic field cells </param>
      void Invalidate(IEnumerable<BaseCell> modifiedCells = null);

      /// <summary> Redraw the required cells </summary>
      /// <param name="modifiedCells">Cells to be redrawn. NULL - redraw the full mosaic, or only those that are included in the clipRegion.</param>
      /// <param name="clipRegion">Region for redrawing. NULL - Redraw everything that is specified in modifiedCells</param>
      void Repaint(IEnumerable<BaseCell> modifiedCells = null, RectDouble? clipRegion = null);

   }

}
