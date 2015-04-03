using fmg.common.geom;
using fmg.core.model.mosaics;

namespace fmg.winrt.mosaic {
   public class MosaicPageInitParam {
      public EMosaic MosaicTypes { get; set; }
      public Size SizeField { get; set; }
      public int MinesCount { get; set; }
   }
}
