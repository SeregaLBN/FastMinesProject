using fmg.common.notyfier;
using fmg.common.geom;

namespace fmg.DataModel.Items {

   /// <summary> Base item class for <see cref="MosaicDataItem"/> and <see cref="MosaicGroupDataItem"/> and <see cref="MosaicSkillDataItem"/> </summary>
   [Windows.Foundation.Metadata.WebHostHidden]
   public abstract class BaseData<T, TImage> : NotifyPropertyChanged
      where TImage : class
   {
      protected BaseData(T uniqueId) {
         UniqueId = uniqueId;
      }

      private T _uniqueId;
      public T UniqueId {
         get { return _uniqueId; }
         set { SetProperty(ref _uniqueId, value); }
      }

      private string _title = string.Empty;
      public string Title {
         get { return _title; }
         set { SetProperty(ref _title, value); }
      }

      public abstract TImage Image { get; protected set; }
      public abstract Size ImageSize { get; set; }

      public override string ToString() {
         return Title;
      }
   }
}