using Windows.UI.Xaml.Media.Imaging;
using fmg.core.mosaic.draw;

namespace fmg.uwp.draw.mosaic.bmp {

   /// <summary> Container for <see cref="WriteableBitmap"/> </summary>
   public class PaintableBmp : IPaintable {

      public PaintableBmp(WriteableBitmap bmp) { Bmp = bmp; }

      public WriteableBitmap Bmp { get; private set; }

   }

}
