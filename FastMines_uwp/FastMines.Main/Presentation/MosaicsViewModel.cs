using System;
using fmg.DataModel.DataSources;
using fmg.common.notyfier;

namespace fmg.common {

   /// <summary> ViewModel for SelectMosaicPage </summary>
   public class MosaicsViewModel : NotifyPropertyChanged {
      private readonly MosaicsDataSource _mosaicsDs = new MosaicsDataSource();

      public MosaicsViewModel() {
         //_mosaicsDs.ImageSize = 150; // TODO убрать, если будет зависимость ImageSize от размеров окна // see Shell.xaml.cs: Shell.OnSizeChanged

         //_mosaicsDs.PropertyChanged += (sender, args) => {
         //   if (args.PropertyName == "SelectedMenuItem") {
         //      // auto-close split view pane
         //      //this.IsSplitViewPaneOpen = false;
         //   }
         //};
      }

      public MosaicsDataSource MosaicsDs => _mosaicsDs;

      public int ImageSize {
         get { return _mosaicsDs.ImageSize; }
         set {
            var old = ImageSize;
            _mosaicsDs.ImageSize = value;
            if (old != value)
               OnPropertyChanged(this, new PropertyChangedExEventArgs<int>(value, old));
         }
      }

      protected override void Dispose(bool disposing) {
         if (Disposed)
            return;

         base.Dispose(disposing);

         _mosaicsDs.Dispose();
      }

   }

}
