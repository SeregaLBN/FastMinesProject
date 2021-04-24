using System;
using System.Runtime.CompilerServices;
using System.Threading.Tasks;

namespace Fmg.Common {

    /// <summary> Very simple logger </summary>
    public class Logger {

        /// <summary> may be rewrited </summary>
        public static Action<string>   ErrorWriter = s => System.Diagnostics.Debug.WriteLine(s); // s => System.Console.Error.WriteLine(s);
        public static Action<string> WarningWriter = s => System.Diagnostics.Debug.WriteLine(s); // s => System.Console.WriteLine(s);
        public static Action<string>    InfoWriter = s => System.Diagnostics.Debug.WriteLine(s); // s => System.Console.WriteLine(s);
        public static Action<string>   DebugWriter = s => System.Diagnostics.Debug.WriteLine(s);
        public static bool UseDatePrefix = true;

        private enum ELevel { error, warning, info, debug }

        private static void Write(ELevel level, string format, params object[] args) {
            Action<string> writer = null;
            switch (level) {
            case ELevel.error  : writer =   ErrorWriter; break;
            case ELevel.warning: writer = WarningWriter; break;
            case ELevel.info   : writer =    InfoWriter; break;
            case ELevel.debug  : writer =   DebugWriter; break;
            }
            if (writer == null)
                return;

            try {
                if (args.Length > 0)
                    format = string.Format(format, args);

                var thr = Task.CurrentId.HasValue
                    ? string.Format($"{Environment.CurrentManagedThreadId}-T{Task.CurrentId.Value}")
                    : Environment.CurrentManagedThreadId.ToString();

                var lev = level.ToString().ToUpper();
                if (UseDatePrefix)
                    writer($"[{DateTime.Now:HH:mm:ss.fff}]  {lev}  Th={thr}  {format}");
                else
                    writer($"{lev}  Th={thr}  {format}");

            } catch (Exception ex) {
                System.Diagnostics.Debug.WriteLine(ex);
                writer(format);
            }
        }

        public static void Error(string message) {
            Write(ELevel.error, message);
        }

        public static void Error(string message, Exception ex) {
            Write(ELevel.error, "{0}: {1}", message, ex);
        }

        public static void Warn(string format, params object[] args) {
            Write(ELevel.warning, format, args);
        }

        public static void Info(string format, params object[] args) {
            Write(ELevel.info, format, args);
        }
        public static void Debug(string format, params object[] args) {
#if DEBUG
            Write(ELevel.debug, format, args);
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
                Logger.Debug($"> {hint}");
            else
                Logger.Debug($"> {hint}: {ctorMessage}");
#endif
        }

        public void Dispose() {
#if DEBUG
            if (_disposeMessage == null)
                Logger.Debug($"< {_hint}");
            else
                Logger.Debug($"< {_hint}: {_disposeMessage()}");
#endif
        }

        public void Put(string format, params object[] args) {
#if DEBUG
            if (args.Length > 0)
                format = string.Format(format, args);
            Logger.Debug($"  {_hint}: {format}");
#endif
        }

    }

}
