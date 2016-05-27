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

      static PaintUwpContext() {
         try {
            var uiSettings = new UISettings();

            Color clrBtn, clrWin;
            try {
               // desktop
               clrBtn = uiSettings.UIElementColor(UIElementType.ButtonFace).ToFmColor();
               clrWin = uiSettings.UIElementColor(UIElementType.Window).ToFmColor();
            } catch (ArgumentException) {
               try {
                  // mobile
                  clrBtn = uiSettings.UIElementColor(1000 + UIElementType.ButtonFace).ToFmColor();
                  clrWin = uiSettings.UIElementColor(1000 + UIElementType.Window).ToFmColor();
               } catch (Exception) {
                  clrBtn = clrWin = Color.Gray; // wtf??
               }
            }
            DefaultBackgroundFillColor = clrBtn;
            DefaultBackgroundWindowColor = clrWin;
         } catch (Exception ex) {
            System.Diagnostics.Debug.Fail(ex.Message);
         }
      }

   }

}
