using System;

namespace FastMines.Common
{
    public abstract class Disposable : IDisposable
    {
        //public Disposable()
        //{
        //    // Constructor
        //}

        protected virtual void Dispose(bool disposing)
        {
            if (disposing)
            {
                // Dispose managed resources
            }

            // Dispose unmanaged resources
        }

        public void Dispose()
        {
            Dispose(true);
            GC.SuppressFinalize(this);
        }

        ~Disposable()
        {
            Dispose(false);
        }
    }
}