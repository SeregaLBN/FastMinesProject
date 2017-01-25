using System;
using System.Runtime.CompilerServices;
using System.Threading.Tasks;

namespace fmg.common {

   public class LoggerSimple {

#if DEBUG
      public static void Put(string format, params object[] args) {
         if (args.Length > 0)
            format = string.Format(format, args);
         var thr = Task.CurrentId.HasValue
            ? string.Format($"{Environment.CurrentManagedThreadId}-T{Task.CurrentId.Value}")
            : Environment.CurrentManagedThreadId.ToString();
         System.Diagnostics.Debug.WriteLine($"[{DateTime.Now.ToString("HH:mm:ss.fff")}]  Th={thr}  {format}");
      }
#else 
      public static void Put(string format, params object[] args) {}
#endif
   }

   public class Tracer : Disposable {
      private readonly string _hint;
      private readonly Func<string> _disposeMessage;

      public Tracer([CallerMemberName] string hint = null) : this(hint, null, null) { }
      public Tracer(string hint, string ctorMessage) : this(hint, ctorMessage, null) { }
      public Tracer(string hint, Func<string> disposeMessage) : this(hint, null, disposeMessage) { }
      public Tracer(string hint, string ctorMessage, Func<string> disposeMessage) {
         _hint = hint;
         _disposeMessage = disposeMessage;
#if DEBUG
         if (ctorMessage == null)
            LoggerSimple.Put($"> {hint}");
         else
            LoggerSimple.Put($"> {hint}: {ctorMessage}");
#else
#endif
      }

      protected override void Dispose(bool disposing) {
         if (disposing) {
            // free managed resources
#if DEBUG
            if (_disposeMessage == null)
               LoggerSimple.Put($"< {_hint}");
            else
               LoggerSimple.Put($"< {_hint}: {_disposeMessage()}");
#else
#endif
         }
         // free native resources if there are any.
      }

#if DEBUG
      public void Put(string format, params object[] args) {
         if (args.Length > 0)
            format = string.Format(format, args);
         LoggerSimple.Put($"  {_hint}: {format}");
      }
#else
      public void Put(string format, params object[] args) {}
#endif
   }

}
