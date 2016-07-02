using Windows.UI.Xaml.Media.Imaging;
using fmg.core.mosaic.draw;

namespace fmg.uwp.draw.mosaic.wbmp {

   /// <summary> Container for <see cref="WriteableBitmap"/> </summary>
   public class PaintableWBmp : IPaintable {

      public PaintableWBmp(WriteableBitmap bmp) { Bmp = bmp; }

      public WriteableBitmap Bmp { get; private set; }

   }

}
