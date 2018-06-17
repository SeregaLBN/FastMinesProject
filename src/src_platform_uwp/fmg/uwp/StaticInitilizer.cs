using Windows.UI.Core;
using fmg.core.img;
using fmg.uwp.utils;

namespace fmg.uwp {

   public static class StaticInitilizer {

      static StaticInitilizer() {
         ImageModelConsts.DeferrInvoker = doRun => AsyncRunner.InvokeFromUiLater(() => doRun(), CoreDispatcherPriority.Normal);
         RotatedImgConst.TimerCreator = () => new Timer();
      }

      public static void Init() {
         // implicit call static block
      }

   }

}
