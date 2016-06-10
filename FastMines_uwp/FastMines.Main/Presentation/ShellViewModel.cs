using System;
using System.Windows.Input;
using fmg.DataModel.DataSources;
using fmg.common.notyfier;

namespace fmg.common {

   /// <summary> ViewModel for main page </summary>
   public class ShellViewModel : NotifyPropertyChanged {
      private readonly MosaicGroupsDataSource _mosaicGroupDs = new MosaicGroupsDataSource();
      private readonly MosaicSkillsDataSource _mosaicSkillDs = new MosaicSkillsDataSource();
      private bool _isSplitViewPaneOpen;

      public ShellViewModel() {
         ToggleSplitViewPaneCommand = new Command(() => IsSplitViewPaneOpen = !IsSplitViewPaneOpen);

         //_mosaicGroupDs.PropertyChanged += (sender, args) => {
         //   if (args.PropertyName == "SelectedMenuItem") {
         //      // auto-close split view pane
         //      //this.IsSplitViewPaneOpen = false;
         //   }
         //};
      }

      public ICommand ToggleSplitViewPaneCommand { get; private set; }

      public bool IsSplitViewPaneOpen
      {
         get { return _isSplitViewPaneOpen; }
         set { SetProperty(ref _isSplitViewPaneOpen, value); }
      }

      public MosaicGroupsDataSource MosaicGroupDs => _mosaicGroupDs;
      public MosaicSkillsDataSource MosaicSkillDs => _mosaicSkillDs;

      public int ImageSize {
         get { return _mosaicGroupDs.ImageSize; }
         set {
            var old = ImageSize;
            _mosaicGroupDs.ImageSize = value;
            _mosaicSkillDs.ImageSize = value;
            if (old != value)
               OnPropertyChanged(this, new PropertyChangedExEventArgs<int>(value, old));
         }
      }

      protected override void Dispose(bool disposing) {
         if (Disposed)
            return;

         base.Dispose(disposing);

         _mosaicGroupDs.Dispose();
         _mosaicSkillDs.Dispose();
      }

   }
}
