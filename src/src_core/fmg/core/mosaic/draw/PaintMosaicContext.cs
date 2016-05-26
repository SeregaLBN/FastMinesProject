using Windows.UI.Xaml.Media.Imaging;
using fmg.common;

namespace fmg.uwp.draw.mosaic {

   public class MosaicGraphicContext : GraphicContext {
      private Color _colorBk;
      private WriteableBitmap _imgBckgrnd;

      public MosaicGraphicContext() :
         base(false) {
         _colorBk = DefaultBackgroundWindowColor.Darker(0.4);
      }

      public Color ColorBk {
         get { return _colorBk; }
         set { SetProperty(ref _colorBk, value); }
      }

      public WriteableBitmap ImgBckgrnd {
         get { return _imgBckgrnd; }
         set { SetProperty(ref _imgBckgrnd, value); }
      }
   }
}