using System;

namespace fmg.common {

   [Obsolete]
   public abstract class Disposable : IDisposable {

      protected bool Disposed { get; private set; }

      protected virtual void Dispose(bool disposing) {
   #if false && EXAMPLE
         if (Disposed)
            return;

         if (disposing) {
            // Dispose managed resources
         }

         // Dispose unmanaged resources
   #endif

         Disposed = true;
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
