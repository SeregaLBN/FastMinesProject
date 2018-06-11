using Avalonia.Threading;
using fmg.core.img;
using fmg.ava.utils;

namespace fmg.ava.draw.img {

   static class StaticRotateImgConsts {

      static StaticRotateImgConsts() {
         ImageModelConsts.DeferrInvoker = doRun => Dispatcher.UIThread.InvokeTaskAsync(() => doRun(), DispatcherPriority.Normal);
         RotatedImgConst.TimerCreator = () => new Timer();
      }

      public static void Init() {
         // implicit call static block
      }

   }

}
