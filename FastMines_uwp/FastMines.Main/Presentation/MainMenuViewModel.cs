using System.ComponentModel;
using System.Windows.Input;
using fmg.DataModel.DataSources;
using fmg.common.notyfier;

namespace fmg.common {

   /// <summary> ViewModel for <see cref="MainPage"/> </summary>
   public class MainMenuViewModel : NotifyPropertyChanged {

      private readonly MosaicGroupsDataSource _mosaicGroupDs = new MosaicGroupsDataSource();
      private readonly MosaicSkillsDataSource _mosaicSkillDs = new MosaicSkillsDataSource();
      private bool _isSplitViewPaneOpen;

      public MainMenuViewModel() {
         ToggleSplitViewPaneCommand = new Command(() => IsSplitViewPaneOpen = !IsSplitViewPaneOpen);

         _mosaicGroupDs.PropertyChanged += OnMosaicGroupDsPropertyChanged;
      }

      public ICommand ToggleSplitViewPaneCommand { get; private set; }

      public bool IsSplitViewPaneOpen {
         get { return _isSplitViewPaneOpen; }
         set { SetProperty(ref _isSplitViewPaneOpen, value); }
      }

      public MosaicGroupsDataSource MosaicGroupDs => _mosaicGroupDs;
      public MosaicSkillsDataSource MosaicSkillDs => _mosaicSkillDs;

      private void OnMosaicGroupDsPropertyChanged(object sender, PropertyChangedEventArgs ev) {
         System.Diagnostics.Debug.Assert(sender is MosaicGroupsDataSource);
         switch (ev.PropertyName) {
         case nameof(MosaicsDataSource.CurrentElement): {
               //// auto-close split view pane
               //this.IsSplitViewPaneOpen = false;
            }
            break;
         }
      }

      protected override void Dispose(bool disposing) {
         if (Disposed)
            return;

         base.Dispose(disposing);

         _mosaicGroupDs.PropertyChanged -= OnMosaicGroupDsPropertyChanged;
         _mosaicGroupDs.Dispose();
         _mosaicSkillDs.Dispose();
      }

   }
}
