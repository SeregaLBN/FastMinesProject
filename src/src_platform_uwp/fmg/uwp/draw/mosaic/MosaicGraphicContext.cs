using System;
using Windows.UI.ViewManagement;
using Windows.UI.Xaml.Media.Imaging;
using fmg.common;

namespace fmg.uwp.draw.mosaic {

   public class MosaicGraphicContext : GraphicContext {
      public static readonly Color COLOR_BTNFACE = Color.Gray;

      private Color _colorBk;
      private WriteableBitmap _imgBckgrnd;
      private static readonly UISettings UiSettings = new UISettings();

      public MosaicGraphicContext() :
         base(false) {

         Color clr;
         try {
            clr = UiSettings.UIElementColor(UIElementType.Window).ToFmColor(); // desktop
         } catch (ArgumentException) {
            try {
               clr = UiSettings.UIElementColor(1000 + UIElementType.Window).ToFmColor(); // mobile
            } catch (Exception) {
               clr = COLOR_BTNFACE; // hz
            }
         }
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