using System;
using System.ComponentModel;
using fmg.common.geom;
using fmg.common.notyfier;

namespace fmg.core.img {

   /// <summary> Model of the flag image </summary>
   public class FlagModel : IImageModel {

      private SizeDouble _size;
      public event PropertyChangedEventHandler PropertyChanged;
      protected readonly NotifyPropertyChanged _notifier;

      public FlagModel() {
         _size = new SizeDouble(40, 40);
         _notifier = new NotifyPropertyChanged(this, ev => PropertyChanged?.Invoke(this, ev));
      }

      /// <summary> width and height in pixel </summary>
      public SizeDouble Size {
         get { return _size; }
         set { _notifier.SetProperty(ref _size, value); }
      }
      public void SetSize(double widhtAndHeight) { Size = new SizeDouble(widhtAndHeight, widhtAndHeight); }

      public void Dispose() {
         _notifier.Dispose();
         GC.SuppressFinalize(this);
      }

   }

}
