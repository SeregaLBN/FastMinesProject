using System;

namespace fmg.common {

    /// <summary> Simple disposable </summary>
    public sealed class PlainFree : IDisposable {

        public Action _onDispose;

        public void Dispose() {
            _onDispose?.Invoke();
            _onDispose = null;
            GC.SuppressFinalize(this);
        }

    }

}
