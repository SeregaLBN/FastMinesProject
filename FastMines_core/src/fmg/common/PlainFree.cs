using System;

namespace Fmg.Common {

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
