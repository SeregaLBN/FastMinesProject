using System;
using System.Threading;
using System.Threading.Tasks;

namespace Fmg.Common.Notifier {

    public class Signal : IDisposable {
        private readonly SemaphoreSlim _signal = new SemaphoreSlim(0, 1);
        /// <summary> set signal </summary>
        public void Set() { _signal.Release(); }
        ///// <summary> unset signal </summary>
        //public void Reset() { signal.Dispose(); signal = new SemaphoreSlim(0, 1); }
        /// <summary> wait for signal </summary>
        public async Task<bool> Wait(TimeSpan ts) { return await _signal.WaitAsync(ts); }
        public void Dispose() { _signal.Dispose(); }
    }

}
