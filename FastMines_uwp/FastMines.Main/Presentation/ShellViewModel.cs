using System.ComponentModel;
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

         _mosaicGroupDs.PropertyChanged += OnMosaicGroupDsPropertyChanged;
         _mosaicSkillDs.PropertyChanged += OnMosaicSkillDsPropertyChanged;
      }

      public ICommand ToggleSplitViewPaneCommand { get; private set; }

      public bool IsSplitViewPaneOpen {
         get { return _isSplitViewPaneOpen; }
         set { SetProperty(ref _isSplitViewPaneOpen, value); }
      }

      public MosaicGroupsDataSource MosaicGroupDs => _mosaicGroupDs;
      public MosaicSkillsDataSource MosaicSkillDs => _mosaicSkillDs;

      public int ImageSize {
         get { return _mosaicGroupDs.ImageSize; }
         set {
            _mosaicGroupDs.ImageSize = value;
            _mosaicSkillDs.ImageSize = value;
         }
      }

      private void OnMosaicSkillDsPropertyChanged(object sender, PropertyChangedEventArgs ev) {
         if (ev.PropertyName == nameof(MosaicsDataSource.ImageSize)) {
            var evi = ev as PropertyChangedExEventArgs<int>;
            if (evi == null)
               OnSelfPropertyChanged(nameof(this.ImageSize));
            else
               OnSelfPropertyChanged(evi.OldValue, evi.NewValue, nameof(this.ImageSize));
         }
      }

      private void OnMosaicGroupDsPropertyChanged(object sender, PropertyChangedEventArgs ev) {
         if (ev.PropertyName == nameof(MosaicsDataSource.ImageSize)) {
            var evi = ev as PropertyChangedExEventArgs<int>;
            if (evi == null)
               OnSelfPropertyChanged(nameof(this.ImageSize));
            else
               OnSelfPropertyChanged(evi.OldValue, evi.NewValue, nameof(this.ImageSize));
         } else if (ev.PropertyName == nameof(MosaicsDataSource.CurrentElement)) {
            //// auto-close split view pane
            //this.IsSplitViewPaneOpen = false;
         }
      }

      protected override void Dispose(bool disposing) {
         if (Disposed)
            return;

         base.Dispose(disposing);

         _mosaicGroupDs.PropertyChanged -= OnMosaicGroupDsPropertyChanged;
         _mosaicSkillDs.PropertyChanged -= OnMosaicSkillDsPropertyChanged;
         _mosaicGroupDs.Dispose();
         _mosaicSkillDs.Dispose();
      }

   }
}
