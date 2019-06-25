using System;
using System.Runtime.CompilerServices;
using System.Threading.Tasks;

namespace Fmg.Common {

    /// <summary> Very simple logger </summary>
    public class Logger {

        public static void Info(string format, params object[] args) {
#if DEBUG
            if (args.Length > 0)
                format = string.Format(format, args);
            var thr = Task.CurrentId.HasValue
                ? string.Format($"{Environment.CurrentManagedThreadId}-T{Task.CurrentId.Value}")
                : Environment.CurrentManagedThreadId.ToString();
            System.Diagnostics.Debug.WriteLine($"[{DateTime.Now.ToString("HH:mm:ss.fff")}]  Th={thr}  {format}");
#endif
        }
    }

    public class Tracer : IDisposable {
        private readonly string _hint;
        private readonly Func<string> _disposeMessage;

        public Tracer([CallerMemberName] string hint = null, string ctorMessage = null, Func<string> disposeMessage = null) {
            _hint = hint;
            _disposeMessage = disposeMessage;
#if DEBUG
            if (ctorMessage == null)
                Logger.Info($"> {hint}");
            else
                Logger.Info($"> {hint}: {ctorMessage}");
#endif
        }

        public void Dispose() {
#if DEBUG
            if (_disposeMessage == null)
                Logger.Info($"< {_hint}");
            else
                Logger.Info($"< {_hint}: {_disposeMessage()}");
#endif
        }

        public void Put(string format, params object[] args) {
#if DEBUG
            if (args.Length > 0)
                format = string.Format(format, args);
            Logger.Info($"  {_hint}: {format}");
#endif
        }

    }

}
