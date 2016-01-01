using System;
using System.Windows.Input;
using fmg.uwp.res.img;
using FastMines.DataModel.DataSources;
using FastMines.Presentation.Notyfier;

namespace FastMines.Presentation {

   /// <summary> ViewModel for main page </summary>
   public class ShellViewModel : NotifyPropertyChanged, IDisposable {
      private readonly MosaicGroupDataSource _mosaicGroupDs = new MosaicGroupDataSource();
      private readonly MosaicSkillDataSource _mosaicSkillDs = new MosaicSkillDataSource();
      private bool _isSplitViewPaneOpen;

      public ShellViewModel() {
         ToggleSplitViewPaneCommand = new Command(() => IsSplitViewPaneOpen = !IsSplitViewPaneOpen);

         _mosaicGroupDs.ImageSize = MosaicsGroupImg.DefaultImageSize / 2;
         _mosaicGroupDs.PropertyChanged += (sender, args) => {
            if (args.PropertyName == "SelectedMenuItem") {
               // auto-close split view pane
               //this.IsSplitViewPaneOpen = false;
            }
         };
      }

      public ICommand ToggleSplitViewPaneCommand { get; private set; }

      public bool IsSplitViewPaneOpen
      {
         get { return _isSplitViewPaneOpen; }
         set { SetProperty(ref _isSplitViewPaneOpen, value); }
      }

      public MosaicGroupDataSource MosaicGroupDs => _mosaicGroupDs;
      public MosaicSkillDataSource MosaicSkillDs => _mosaicSkillDs;

      public int ImageSize {
         get { return _mosaicGroupDs.ImageSize; }
         set {
            var old = ImageSize;
            _mosaicGroupDs.ImageSize = value;
            _mosaicSkillDs.ImageSize = value;
            if (old != value)
               OnPropertyChanged(this, new PropertyChangedExEventArgs<int>("ImageSize", value, old));
         }
      }

      public void Dispose()
      {
         _mosaicGroupDs.Dispose();
         _mosaicSkillDs.Dispose();
      }

   }
}
