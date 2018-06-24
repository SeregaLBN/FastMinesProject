using fmg.ava.utils;
using fmg.common.ui;
using fmg.jfx.draw.img;

namespace fmg.ava {

   static class StaticInitializer {

      static StaticInitializer() {
         Factory.DEFERR_INVOKER = doRun => Avalonia.Threading.Dispatcher.UIThread.InvokeAsync(() => doRun(),
                                                                                              Avalonia.Threading.DispatcherPriority.Normal);
         Factory.GET_ANIMATOR = Animator.getSingleton;
         Factory.TIMER_CREATOR = () => new Timer();
      }

      public static void Init() {
         // implicit call static block
      }

   }

}
