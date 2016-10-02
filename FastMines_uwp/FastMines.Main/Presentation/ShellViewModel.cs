using System.ComponentModel;
using System.Windows.Input;
using fmg.DataModel.DataSources;
using fmg.common.notyfier;
using fmg.common.geom;

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

      public Size ImageSize {
         get { return _mosaicGroupDs.ImageSize; }
         set {
            _mosaicGroupDs.ImageSize = value;
            _mosaicSkillDs.ImageSize = value;
         }
      }
      public Size TopImageSize {
         get { return _mosaicGroupDs.TopImageSize; }
         set {
            _mosaicGroupDs.TopImageSize = value;
            _mosaicSkillDs.TopImageSize = value;
         }
      }

      private void OnMosaicSkillDsPropertyChanged(object sender, PropertyChangedEventArgs ev) {
         if (ev.PropertyName == nameof(MosaicsDataSource.ImageSize)) {
            var ev2 = ev as PropertyChangedExEventArgs<Size>;
            if (ev2 == null)
               OnSelfPropertyChanged(nameof(this.ImageSize));
            else
               OnSelfPropertyChanged(ev2.OldValue, ev2.NewValue, nameof(this.ImageSize));
         }
      }

      private void OnMosaicGroupDsPropertyChanged(object sender, PropertyChangedEventArgs ev) {
         System.Diagnostics.Debug.Assert(sender is MosaicGroupsDataSource);
         switch (ev.PropertyName) {
         case nameof(MosaicGroupsDataSource.ImageSize): {
               var ev2 = ev as PropertyChangedExEventArgs<Size>;
               if (ev2 == null)
                  OnSelfPropertyChanged(nameof(this.ImageSize));
               else
                  OnSelfPropertyChanged(ev2.OldValue, ev2.NewValue, nameof(this.ImageSize));
            }
            break;
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
         _mosaicSkillDs.PropertyChanged -= OnMosaicSkillDsPropertyChanged;
         _mosaicGroupDs.Dispose();
         _mosaicSkillDs.Dispose();
      }

   }
}
