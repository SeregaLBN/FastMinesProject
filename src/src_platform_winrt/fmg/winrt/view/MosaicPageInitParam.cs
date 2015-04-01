using fmg.common.geom;
using fmg.core.model.mosaics;

namespace fmg.winrt.view {
   public class MosaicPageInitParam {
      public EMosaic MosaicTypes { get; set; }
      public Size SizeField { get; set; }
      public int MinesCount { get; set; }
   }
}
