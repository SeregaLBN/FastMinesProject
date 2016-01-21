using System;
using Windows.UI.Core;
using fmg.uwp.res.img;
using FastMines.Common;
using FastMines.DataModel.DataSources;
using FastMines.Presentation.Notyfier;

namespace FastMines.Presentation {

   /// <summary> ViewModel for SelectMosaicPage </summary>
   public class MosaicsViewModel : NotifyPropertyChanged, IDisposable {
      private readonly MosaicsDataSource _mosaicsDs = new MosaicsDataSource();

      public MosaicsViewModel() {
         if (Windows.ApplicationModel.DesignMode.DesignModeEnabled)
            _mosaicsDs.ImageSize = MosaicsGroupImg.DefaultImageSize/2;
         else
            AsyncRunner.InvokeFromUiLater(() => _mosaicsDs.ImageSize = MosaicsGroupImg.DefaultImageSize/2,
               CoreDispatcherPriority.Low);
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

      public void Dispose() {
         _mosaicsDs.Dispose();
      }

   }
}
