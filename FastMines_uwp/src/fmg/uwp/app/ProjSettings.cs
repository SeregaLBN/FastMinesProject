using System;
using Windows.UI;
using Windows.UI.Core;
using Windows.UI.ViewManagement;
using Fmg.Common.UI;
using Fmg.Core.Mosaic;
using Fmg.Uwp.Img;
using Fmg.Uwp.Utils;

namespace Fmg.Uwp.App {

    public class ProjSettings : Fmg.Core.App.AProjSettings {

        /// <summary> Mobile (true) or Desktop (false) </summary>
        public static bool IsMobile { get; }
        public static double MinTouchSize => Cast.DpToPx(48); // android recommended size

        static ProjSettings() {
            UiInvoker.Deferred = doRun => AsyncRunner.InvokeFromUi(() => doRun(), CoreDispatcherPriority.Normal);
            UiInvoker.Animator = () => Animator.Singleton;
            UiInvoker.TimerCreator = () => new Timer();

            try {
                var uiSettings = new UISettings();

                Color clrCell;
                Color clrBk;
                UIElementType forCell = UIElementType.ButtonFace; // UIElementType.Window
                UIElementType forBkrd = UIElementType.PageBackground;
                try {
                    // desktop
                    clrCell = uiSettings.UIElementColor(forCell);
                    clrBk   = uiSettings.UIElementColor(forBkrd);
                    IsMobile = false;
                } catch (ArgumentException) {
                    IsMobile = true;
                    try {
                        // mobile
                        const int magic = 1000;
                        clrCell = uiSettings.UIElementColor(magic + forCell);
                        clrCell = uiSettings.UIElementColor(magic + forBkrd);
                    }
                    catch (Exception) {
                        // wtf??
                        clrCell = MosaicDrawModelConst.DefaultCellColor.ToWinColor();
                        clrCell = MosaicDrawModelConst.DefaultBkColor  .ToWinColor();
                    }
                }
                MosaicDrawModelConst.DefaultCellColor = clrCell.ToFmColor();
                MosaicDrawModelConst.DefaultBkColor   = clrBk  .ToFmColor();
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
