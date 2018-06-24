using Windows.UI.Core;
using fmg.common.ui;
using fmg.core.img;
using fmg.uwp.draw.img;

namespace fmg.uwp.utils {

   public static class StaticInitilizer {

      static StaticInitilizer() {
         Factory.DEFERR_INVOKER = doRun => AsyncRunner.InvokeFromUiLater(() => doRun(), CoreDispatcherPriority.Normal);
         Factory.GET_ANIMATOR = Animator.getSingleton;
         Factory.TIMER_CREATOR = () => new Timer();
      }

      public static void Init() {
         // implicit call static block
      }

   }

}
