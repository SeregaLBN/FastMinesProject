using System;
using Windows.UI.ViewManagement;
using fmg.common;
using fmg.core.mosaic.draw;
using fmg.uwp.utils;

namespace fmg.uwp.draw.mosaic {

   public class PaintContext<TImage> : PaintCellContext<TImage>
      where TImage: class
   {
      public PaintContext(bool iconicMode) :
         base(iconicMode)
      {
      }

      static PaintContext() {
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
