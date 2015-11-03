using Windows.UI.Xaml.Media;
using FastMines.Presentation.Notyfier;

namespace FastMines.DataModel.Items {

   /// <summary> Base item class for <see cref="MosaicGroupDataItem"/> and <see cref="MosaicDataItem"/> </summary>
   [Windows.Foundation.Metadata.WebHostHidden]
   public abstract class BaseData<T> : NotifyPropertyChanged {
      protected BaseData(T uniqueId) {
         UniqueId = uniqueId;
      }

      public T UniqueId { get; }

      private string _title = string.Empty;
      public string Title {
         get { return _title; }
         set { SetProperty(ref _title, value); }
      }

      public abstract ImageSource Image { get; }
      public abstract int ImageSize { get; set; }

      public override string ToString() {
         return Title;
      }
   }
}