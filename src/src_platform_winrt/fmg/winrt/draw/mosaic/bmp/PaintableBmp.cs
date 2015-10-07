using Windows.UI.Xaml.Media.Imaging;

namespace fmg.winrt.draw.mosaic.bmp
{
   public class PaintableBmp : IPaintable
   {
      public PaintableBmp(WriteableBitmap bmp) { Bmp = bmp; }

      public WriteableBitmap Bmp { get; private set; }
   }
}