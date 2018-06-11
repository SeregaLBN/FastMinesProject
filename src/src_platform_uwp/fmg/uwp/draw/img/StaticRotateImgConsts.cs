using Windows.UI.Core;
using fmg.core.img;
using fmg.uwp.utils;

namespace fmg.uwp.draw.img {

   static class StaticRotateImgConsts {

      static StaticRotateImgConsts() {
         ImageModelConsts.DeferrInvoker = doRun => AsyncRunner.InvokeFromUiLater(() => doRun(), CoreDispatcherPriority.Normal);
         RotatedImgConst.TimerCreator = () => new Timer();
      }

      public static void Init() {
         // implicit call static block
      }

   }

}
