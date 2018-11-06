using Avalonia.Threading;
using fmg.common.ui;
using fmg.ava.img;

namespace fmg.ava.utils {

    static class StaticInitializer {

        static StaticInitializer() {
            Factory.DEFERR_INVOKER = doRun => Dispatcher.UIThread.InvokeAsync(() => doRun(), DispatcherPriority.Normal);
            Factory.GET_ANIMATOR = () => Animator.Singleton;
            Factory.TIMER_CREATOR = () => new Timer();
        }

        public static void Init() {
            // implicit call static block
        }

    }

}
