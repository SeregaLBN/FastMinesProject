using System.ComponentModel;
using System.Windows.Input;
using fmg.DataModel.DataSources;
using fmg.common.notyfier;

namespace fmg.common {

    /// <summary> ViewModel for <see cref="MainPage"/> </summary>
    public class MainMenuViewModel : NotifyPropertyChanged {

        private readonly MosaicGroupDataSource _mosaicGroupDs = new MosaicGroupDataSource();
        private readonly MosaicSkillDataSource _mosaicSkillDs = new MosaicSkillDataSource();
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

        public MosaicGroupDataSource MosaicGroupDs => _mosaicGroupDs;
        public MosaicSkillDataSource MosaicSkillDs => _mosaicSkillDs;

        private void OnMosaicGroupDsPropertyChanged(object sender, PropertyChangedEventArgs ev) {
            System.Diagnostics.Debug.Assert(sender is MosaicGroupDataSource);
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
