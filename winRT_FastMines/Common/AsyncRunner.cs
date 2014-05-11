using System;
using Windows.Foundation;
using Windows.System.Threading;
using Windows.UI.Core;

namespace FastMines.Common {
   public static class AsyncRunner {
      public static IAsyncAction ExecuteOnUIThread(DispatchedHandler action, CoreDispatcherPriority priority) {
         return Windows.ApplicationModel.Core.CoreApplication.MainView.CoreWindow.Dispatcher.RunAsync(priority, action);
      }

      public static IAsyncAction InvokeLater(DispatchedHandler action, CoreDispatcherPriority priority = CoreDispatcherPriority.Normal, bool bAwait = false) {
         return bAwait
            ? ThreadPool.RunAsync(async delegate { await ExecuteOnUIThread(action, priority); }, (WorkItemPriority)priority)
            : ThreadPool.RunAsync(delegate { ExecuteOnUIThread(action, priority); }, (WorkItemPriority)priority);
      }
   }
}
