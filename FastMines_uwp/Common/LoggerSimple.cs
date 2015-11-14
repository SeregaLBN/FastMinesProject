using System;
using System.Threading.Tasks;

namespace FastMines.Common {
   public class LoggerSimple {
#if DEBUG
      //[System.Runtime.InteropServices.DllImport("Kernel32.dll")]
      //public static extern uint GetCurrentThreadId();
      public static void Put(string format, params object[] args) {
         if (args.Length > 0)
            format = string.Format(format, args);
         var thr = Task.CurrentId.HasValue
            ? string.Format("{0}-T{1}", Environment.CurrentManagedThreadId, Task.CurrentId.Value)
            : Environment.CurrentManagedThreadId.ToString();
         //GetCurrentThreadId()
         System.Diagnostics.Debug.WriteLine("[{0}]  Th={1}  {2}", DateTime.Now.ToString("HH:mm:ss.fff"), thr, format);
      }
#else 
      public static void Log(string format, params object[] args) {}
#endif
   }

   public class Tracer : Disposable {
      private readonly string _hint;
      private readonly Func<string> _disposeMessage;

      public Tracer(string hint) : this(hint, null, null) { }
      public Tracer(string hint, string ctorMessage) : this(hint, ctorMessage, null) { }
      public Tracer(string hint, Func<string> disposeMessage) : this(hint, null, disposeMessage) { }

      public Tracer(string hint, string ctorMessage, Func<string> disposeMessage) {
         _hint = hint;
         _disposeMessage = disposeMessage;
#if DEBUG
         if (ctorMessage == null)
            LoggerSimple.Put("> {0}", hint);
         else
            LoggerSimple.Put("> {0}: {1}", hint, ctorMessage);
#else
#endif
      }

      protected override void Dispose(bool disposing)
      {
         if (disposing) {
            // free managed resources
#if DEBUG
            if (_disposeMessage == null)
               LoggerSimple.Put("< {0}", _hint);
            else
               LoggerSimple.Put("< {0}: {1}", _hint, _disposeMessage());
#else
#endif
         }
         // free native resources if there are any.
      }

#if DEBUG
      public void Put(string format, params object[] args) {
         if (args.Length > 0)
            format = string.Format(format, args);
         LoggerSimple.Put("  {0}: {1}", _hint, format);
      }
#else
      public static void Put(string format, params object[] args) {}
#endif
   }
}