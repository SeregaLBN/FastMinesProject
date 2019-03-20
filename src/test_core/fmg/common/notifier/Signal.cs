using System;
using System.Threading;
using System.Threading.Tasks;

namespace fmg.common.notifier {

    public class Signal : IDisposable {
        private readonly SemaphoreSlim signal = new SemaphoreSlim(0, 1);
        /// <summary> set signal </summary>
        public void Set() { signal.Release(); }
        ///// <summary> unset signal </summary>
        //public void Reset() { signal.Dispose(); signal = new SemaphoreSlim(0, 1); }
        /// <summary> wait for signal </summary>
        public async Task<bool> Wait(TimeSpan ts) { return await signal.WaitAsync(ts); }
        public void Dispose() { signal.Dispose(); }
    }

}
