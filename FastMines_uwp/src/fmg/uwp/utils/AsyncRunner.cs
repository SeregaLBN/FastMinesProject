using System;
using System.Threading.Tasks;
using Windows.Foundation;
using Windows.UI.Core;

namespace Fmg.Uwp.Utils {

    public static class AsyncRunner {

        /// <summary> send for execution on the UI thread </summary>
        public static IAsyncAction InvokeFromUiLaterAsync(this DispatchedHandler action, CoreDispatcherPriority priority = CoreDispatcherPriority.Normal) {
            return Windows.ApplicationModel.Core.CoreApplication.MainView.CoreWindow.Dispatcher.RunAsync(priority, action);
        }

        /// <summary> send for execution on the UI thread with result </summary>
        public static async Task<T> ExecuteFromUiLaterAsync<T>(this Func<T> action, CoreDispatcherPriority priority = CoreDispatcherPriority.Normal) {
            T t = default;
            await Windows.ApplicationModel.Core.CoreApplication.MainView.CoreWindow.Dispatcher.RunAsync(priority, () => t = action());
            return t;
        }

        /// <summary> send for execution on the UI thread, without waiting for the result </summary>
        public static void InvokeFromUiLater(this DispatchedHandler action, CoreDispatcherPriority priority = CoreDispatcherPriority.Normal) {
#pragma warning disable CS4014 // Because this call is not awaited, execution of the current method continues before the call is completed
            InvokeFromUiLaterAsync(action, priority);
#pragma warning restore CS4014 // Because this call is not awaited, execution of the current method continues before the call is completed
        }

        /// <summary> send to run asynchronously without waiting for the result </summary>
        public static void InvokeLater(Windows.System.Threading.WorkItemHandler handler, Windows.System.Threading.WorkItemPriority priority = Windows.System.Threading.WorkItemPriority.Normal) {
#pragma warning disable CS4014 // Because this call is not awaited, execution of the current method continues before the call is completed
            Windows.System.Threading.ThreadPool.RunAsync(handler, priority);
#pragma warning restore CS4014 // Because this call is not awaited, execution of the current method continues before the call is completed
        }

        public async static Task RunWithDelay(this Action run, TimeSpan delay) {
            await Task.Delay(delay);//.ContinueWith(t => run());
            run();
        }

        public static void RunWithDelayNoWait(this Action run, TimeSpan delay) {
#pragma warning disable CS4014 // Because this call is not awaited, execution of the current method continues before the call is completed
            RunWithDelay(run, delay);
#pragma warning restore CS4014 // Because this call is not awaited, execution of the current method continues before the call is completed
        }

        public static void Repeat(this Action run, TimeSpan delay, Func<bool> cancelation) {
            void run2() {
                if (cancelation())
                    return;
                run();
                ((Action)run2).RunWithDelayNoWait(delay); // repeat
            }
            run2(); // start
        }

    }

}
