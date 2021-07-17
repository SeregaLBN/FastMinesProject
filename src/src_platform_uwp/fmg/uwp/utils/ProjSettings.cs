using System;
using Windows.UI;
using Windows.UI.Core;
using Windows.UI.ViewManagement;
using Fmg.Common.UI;
using Fmg.Core.Mosaic;
using Fmg.Uwp.Img;

namespace Fmg.Uwp.Utils {

    public class ProjSettings : Fmg.Common.AProjSettings {

        /// <summary> Mobile (true) or Desktop (false) </summary>
        public static bool IsMobile { get; }

        static ProjSettings() {
            UiInvoker.Deferred = doRun => AsyncRunner.InvokeFromUiLater(() => doRun(), CoreDispatcherPriority.Normal);
            UiInvoker.Animator = () => Animator.Singleton;
            UiInvoker.TimerCreator = () => new Timer();

            try {
                var uiSettings = new UISettings();

                Color clr;
                try {
                    // desktop
                    clr = uiSettings.UIElementColor(UIElementType.ButtonFace);
                    //clr = uiSettings.UIElementColor(UIElementType.Window);
                    IsMobile = false;
                } catch (ArgumentException) {
                    IsMobile = true;
                    try {
                        // mobile
                        const int magic = 1000;
                        clr = uiSettings.UIElementColor(magic + UIElementType.ButtonFace);
                        //clr = uiSettings.UIElementColor(magic + UIElementType.Window);
                    } catch (Exception) {
                        clr = Fmg.Common.Color.Gray.ToWinColor(); // wtf??
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
