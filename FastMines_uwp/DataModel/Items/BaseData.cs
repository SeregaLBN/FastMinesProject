using System;
using Windows.UI.Xaml.Media;
using FastMines.Presentation.Notyfier;

namespace FastMines.DataModel.Items {

   /// <summary> Base item class for <see cref="MosaicDataItem"/> and <see cref="MosaicGroupDataItem"/> and <see cref="MosaicSkillDataItem"/> </summary>
   [Windows.Foundation.Metadata.WebHostHidden]
   public abstract class BaseData<T> : NotifyPropertyChanged, IDisposable {
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

      public abstract void Dispose();

      public override string ToString() {
         return Title;
      }
   }
}