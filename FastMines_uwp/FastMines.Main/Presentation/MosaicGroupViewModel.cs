using System.ComponentModel;
using fmg.common.notyfier;
using fmg.DataModel.DataSources;
using fmg.common.geom;

namespace fmg.common {

   /// <summary> ViewModel for <see cref="SelectMosaicPage"/> </summary>
   public class MosaicGroupViewModel : NotifyPropertyChanged {

      private readonly MosaicsDataSource _mosaicsDs = new MosaicsDataSource();

      public MosaicGroupViewModel() {
         _mosaicsDs.PropertyChanged += OnMosaicsDsPropertyChanged;
      }

      public MosaicsDataSource MosaicsDs => _mosaicsDs;

      public Size ImageSize {
         get { return _mosaicsDs.ImageSize; }
         set { _mosaicsDs.ImageSize = value; }
      }

      private void OnMosaicsDsPropertyChanged(object sender, PropertyChangedEventArgs ev) {
         if (ev.PropertyName == nameof(MosaicsDataSource.ImageSize)) {
            // ! notify parent container
            OnSelfPropertyChanged<Size>(ev, nameof(this.ImageSize));
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
