using System;
using System.Collections.Generic;
using fmg.core.mosaic.cells;

namespace fmg.core.mosaic {

   /// <summary> MVC view interface of mosaic </summary>
   public interface IMosaicView : IDisposable {

      /// <summary> mark to redraw </summary>
      /// <param name="modifiedCells"> cells to invalidate. null value - make to redraw all mosaic field cells </param>
      void Invalidate(IEnumerable<BaseCell> modifiedCells = null);

   }

}
