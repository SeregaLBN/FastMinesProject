using System;
using Windows.UI;
using Windows.UI.Core;
using Windows.UI.ViewManagement;
using fmg.common.ui;
using fmg.core.mosaic;
using fmg.uwp.img;

namespace fmg.uwp.utils {

   public static class StaticInitializer {

      static StaticInitializer() {
         Factory.DEFERR_INVOKER = doRun => AsyncRunner.InvokeFromUiLater(() => doRun(), CoreDispatcherPriority.Normal);
         Factory.GET_ANIMATOR = () => Animator.Singleton;
         Factory.TIMER_CREATOR = () => new Timer();

         try {
            var uiSettings = new UISettings();

            Color clr;
            try {
               // desktop
               clr = uiSettings.UIElementColor(UIElementType.ButtonFace);
               //clr = uiSettings.UIElementColor(UIElementType.Window);
            } catch (ArgumentException) {
               try {
                  // mobile
                  const int magic = 1000;
                  clr = uiSettings.UIElementColor(magic + UIElementType.ButtonFace);
                  //clr = uiSettings.UIElementColor(magic + UIElementType.Window);
               } catch (Exception) {
                  clr = fmg.common.Color.Gray.ToWinColor(); // wtf??
               }
            }
            MosaicDrawModelConst.DefaultBkColor = clr.ToFmColor();
         } catch (Exception ex) {
            System.Diagnostics.Debug.WriteLine(ex.Message);
            System.Diagnostics.Debug.Assert(false, ex.Message);
         }
      }

      public static void Init() {
         // implicit call static block
      }

   }

}
