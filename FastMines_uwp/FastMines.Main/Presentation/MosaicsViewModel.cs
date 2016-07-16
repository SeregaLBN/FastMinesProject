using System.ComponentModel;
using fmg.common.notyfier;
using fmg.DataModel.DataSources;

namespace fmg.common {

   /// <summary> ViewModel for SelectMosaicPage </summary>
   public class MosaicsViewModel : NotifyPropertyChanged {
      private readonly MosaicsDataSource _mosaicsDs = new MosaicsDataSource();

      public MosaicsViewModel() {
         //_mosaicsDs.ImageSize = 150; // TODO убрать, если будет зависимость ImageSize от размеров окна // see Shell.xaml.cs: Shell.OnSizeChanged
         _mosaicsDs.PropertyChanged += OnMosaicsDsPropertyChanged;
      }

      public MosaicsDataSource MosaicsDs => _mosaicsDs;

      public int ImageSize {
         get { return _mosaicsDs.ImageSize; }
         set { _mosaicsDs.ImageSize = value; }
      }

      private void OnMosaicsDsPropertyChanged(object sender, PropertyChangedEventArgs ev) {
         if (ev.PropertyName == nameof(MosaicsDataSource.ImageSize)) {
            var evi = ev as PropertyChangedExEventArgs<int>;
            if (evi == null)
               OnSelfPropertyChanged(nameof(this.ImageSize));
            else
               OnSelfPropertyChanged(evi.OldValue, evi.NewValue, nameof(this.ImageSize));
         }
      }

      protected override void Dispose(bool disposing) {
         if (Disposed)
            return;

         base.Dispose(disposing);

         _mosaicsDs.PropertyChanged -= OnMosaicsDsPropertyChanged;
         _mosaicsDs.Dispose();
      }

   }

}
