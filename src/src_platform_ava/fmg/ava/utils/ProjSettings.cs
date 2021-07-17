using Avalonia.Threading;
using Fmg.Common;
using Fmg.Common.UI;
using Fmg.Ava.Img;

namespace Fmg.Ava.Utils {

    class ProjSettings : AProjSettings {

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
