using Windows.UI.ViewManagement;
using Windows.UI.Xaml.Media.Imaging;
using ua.ksn.geom;

namespace ua.ksn.fmg.view.win_rt.draw.mosaics {

   public class MosaicGraphicContext : GraphicContext {
      public static readonly Color COLOR_BTNFACE = Color.GRAY;

      private Color _colorBk;
      private WriteableBitmap _imgBckgrnd;

      public MosaicGraphicContext() :
         base(false, new Size(0, 0)) {
         //_colorBk = COLOR_BTNFACE;
         //_colorBk = Color.BLACK;
         //_colorBk = new UISettings().UIElementColor(UIElementType.Window).Cast(); // Panel background

         var clr = new UISettings().UIElementColor(UIElementType.Window).ToFmColor(); // Panel background
         const float perc = 0.40f; // делаю темнее
         var r = (int) (clr.R - clr.R*perc);
         var g = (int) (clr.G - clr.G*perc);
         var b = (int) (clr.B - clr.B*perc);
         _colorBk = new Color((byte)r, (byte)g, (byte)b);
      }

      public Color ColorBk {
         get {
            return _colorBk;
            //return Color.WHITE;
         }
         set { SetProperty(ref _colorBk, value); }
      }

      public WriteableBitmap ImgBckgrnd {
         get { return _imgBckgrnd; }
         set { SetProperty(ref _imgBckgrnd, value); }
      }
   }
}