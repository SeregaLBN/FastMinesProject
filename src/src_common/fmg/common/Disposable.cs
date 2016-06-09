using System;

namespace fmg.common {

    public abstract class Disposable : IDisposable {

        protected bool Disposed { get; private set; }

        protected virtual void Dispose(bool disposing) {
            Disposed = true;
            if (disposing) {
                // Dispose managed resources
            }

            // Dispose unmanaged resources
        }

        public void Dispose() {
            Dispose(true);
            GC.SuppressFinalize(this);
        }

        ~Disposable() {
            Dispose(false);
        }

   }

}
