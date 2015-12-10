using Windows.Foundation;
using Windows.UI.Core;

namespace FastMines.Common {
   public static class UiThreadExecutor {
      /// <summary> ExecuteOnUIThread </summary>
      public static IAsyncAction InvokeLaterAsync(this DispatchedHandler action, CoreDispatcherPriority priority) {
         return Windows.ApplicationModel.Core.CoreApplication.MainView.CoreWindow.Dispatcher.RunAsync(priority, action);
      }

      public static void InvokeLater(this DispatchedHandler action, CoreDispatcherPriority priority) {
#pragma warning disable CS4014 // Because this call is not awaited, execution of the current method continues before the call is completed
         InvokeLaterAsync(action, priority);
#pragma warning restore CS4014 // Because this call is not awaited, execution of the current method continues before the call is completed
      }

      //public static IAsyncAction InvokeLater(DispatchedHandler action, CoreDispatcherPriority priority = CoreDispatcherPriority.Normal, bool bAwait = false) {
      //   return bAwait
      //      ? Windows.System.Threading.ThreadPool.RunAsync(async delegate { await InvokeLater(action, priority); }, (Windows.System.Threading.WorkItemPriority)priority)
      //      : Windows.System.Threading.ThreadPool.RunAsync(delegate { InvokeLater(action, priority); }, (Windows.System.Threading.WorkItemPriority)priority);
      //}
   }
}
