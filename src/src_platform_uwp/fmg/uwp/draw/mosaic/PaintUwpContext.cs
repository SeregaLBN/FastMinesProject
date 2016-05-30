using System;
using Windows.UI.ViewManagement;
using fmg.common;
using fmg.core.mosaic.draw;
using fmg.uwp.utils;

namespace fmg.uwp.draw.mosaic {

   /// <summary>Information required for drawing the entire mosaic and cells.
   /// UWP impl
   /// </summary>
   /// <typeparam name="TImage">UWP specific image: <see cref="Windows.UI.Xaml.Media.Imaging.WriteableBitmap"/> or <see cref="Windows.UI.Xaml.Media.ImageSource"/>, etc... </typeparam>
   public class PaintUwpContext<TImage> : PaintContext<TImage>
      where TImage : class
   {
      public PaintUwpContext(bool iconicMode) :
         base(iconicMode)
      {
      }

      public new static Color DefaultBackgroundColor => PaintContext<TImage>.DefaultBackgroundColor;

      static PaintUwpContext() {
         try {
            var uiSettings = new UISettings();

            Color clr;
            try {
               // desktop
               clr = uiSettings.UIElementColor(UIElementType.ButtonFace).ToFmColor();
               //clr = uiSettings.UIElementColor(UIElementType.Window).ToFmColor();
            } catch (ArgumentException) {
               try {
                  // mobile
                  clr = uiSettings.UIElementColor(1000 + UIElementType.ButtonFace).ToFmColor();
                  //clr = uiSettings.UIElementColor(1000 + UIElementType.Window).ToFmColor();
               } catch (Exception) {
                  clr = Color.Gray; // wtf??
               }
            }
            PaintContext<TImage>.DefaultBackgroundColor = clr;
         } catch (Exception ex) {
            System.Diagnostics.Debug.WriteLine(ex.Message);
         }
      }

   }

}
