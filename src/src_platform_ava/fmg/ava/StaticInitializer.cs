using fmg.core;
using fmg.core.img;
using fmg.ava.utils;

namespace fmg.ava {

   static class StaticInitilizer {

      static StaticInitilizer() {
         Factory.DEFERR_INVOKER = doRun => Avalonia.Threading.Dispatcher.UIThread.InvokeAsync(() => doRun(), Avalonia.Threading.DispatcherPriority.Normal);
         Factory.GET_ANIMATOR = () => Animator.getSingleton();
         Factory.TIMER_CREATOR = () => new Timer();
      }

      public static void Init() {
         // implicit call static block
      }

   }

}
