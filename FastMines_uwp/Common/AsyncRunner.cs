using Windows.Foundation;
using Windows.UI.Core;

namespace FastMines.Common {
   public static class AsyncRunner {
      /// <summary> ExecuteOnUIThread </summary>
      public static IAsyncAction InvokeLater(DispatchedHandler action, CoreDispatcherPriority priority) {
         return Windows.ApplicationModel.Core.CoreApplication.MainView.CoreWindow.Dispatcher.RunAsync(priority, action);
      }

      //public static IAsyncAction InvokeLater(DispatchedHandler action, CoreDispatcherPriority priority = CoreDispatcherPriority.Normal, bool bAwait = false) {
      //   return bAwait
      //      ? Windows.System.Threading.ThreadPool.RunAsync(async delegate { await InvokeLater(action, priority); }, (Windows.System.Threading.WorkItemPriority)priority)
      //      : Windows.System.Threading.ThreadPool.RunAsync(delegate { InvokeLater(action, priority); }, (Windows.System.Threading.WorkItemPriority)priority);
      //}
   }
}
