using System;
using System.ComponentModel;
using fmg.common.geom;
using fmg.common.notyfier;

namespace fmg.core.img {

   /// <summary> Model of the flag image </summary>
   public class FlagModel : IImageModel {

      private SizeDouble _size;
      protected bool Disposed { get; private set; }
      protected NotifyPropertyChanged _notifier;
      public event PropertyChangedEventHandler PropertyChanged;

      public FlagModel() {
         _size = new SizeDouble(40, 40);
         _notifier = new NotifyPropertyChanged(this);
         _notifier.PropertyChanged += OnPropertyChanged;
      }

      /// <summary> width and height in pixel </summary>
      public SizeDouble Size {
         get { return _size; }
         set { _notifier.SetProperty(ref _size, value); }
      }
      public void SetSize(double widhtAndHeight) { Size = new SizeDouble(widhtAndHeight, widhtAndHeight); }

      private void OnPropertyChanged(object sender, PropertyChangedEventArgs ev) {
         System.Diagnostics.Debug.Assert(ReferenceEquals(sender, this));
         PropertyChanged?.Invoke(this, ev);
      }

      protected virtual void Dispose(bool disposing) {
         if (disposing) {
            // Dispose managed resources
            _notifier.PropertyChanged -= OnPropertyChanged;
            _notifier.Dispose();
         }

         // Dispose unmanaged resources
      }

      public void Dispose() {
         if (!Disposed) {
            Disposed = true;
            Dispose(true);
         }
         GC.SuppressFinalize(this);
      }

      ~FlagModel() {
         if (!Disposed) {
            Disposed = true;
            Dispose(false);
         }
      }

   }

}
