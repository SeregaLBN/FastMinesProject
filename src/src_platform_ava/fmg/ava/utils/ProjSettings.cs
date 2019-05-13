using Avalonia.Threading;
using fmg.common.ui;
using fmg.ava.img;

namespace fmg.ava.utils {

    static class ProjSettings {

        static ProjSettings() {
            UiInvoker.Deferred = doRun => Dispatcher.UIThread.InvokeAsync(() => doRun(), DispatcherPriority.Normal);
            UiInvoker.Animator = () => Animator.Singleton;
            UiInvoker.TimerCreator = () => new Timer();
        }

        public static void Init() {
            // implicit call static block
        }

    }

}
