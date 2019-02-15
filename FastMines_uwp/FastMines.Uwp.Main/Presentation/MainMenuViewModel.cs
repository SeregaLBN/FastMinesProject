using System;
using System.ComponentModel;
using System.Windows.Input;
using fmg.DataModel.DataSources;
using fmg.common.notyfier;

namespace fmg.common {

    /// <summary> ViewModel for <see cref="MainPage"/> </summary>
    public class MainMenuViewModel : INotifyPropertyChanged, IDisposable {

        private readonly MosaicGroupDataSource mosaicGroupDS = new MosaicGroupDataSource();
        private readonly MosaicSkillDataSource mosaicSkillDS = new MosaicSkillDataSource();
        private bool isSplitViewPaneOpen;
        protected bool Disposed { get; private set; }
        public event PropertyChangedEventHandler PropertyChanged;
        protected readonly NotifyPropertyChanged notifier;

        public MainMenuViewModel() {
            ToggleSplitViewPaneCommand = new Command(() => IsSplitViewPaneOpen = !IsSplitViewPaneOpen);

            mosaicGroupDS.PropertyChanged += OnMosaicGroupDsPropertyChanged;
            notifier = new NotifyPropertyChanged(this, ev => PropertyChanged?.Invoke(this, ev), false);
        }

        public ICommand ToggleSplitViewPaneCommand { get; private set; }

        public bool IsSplitViewPaneOpen {
            get { return isSplitViewPaneOpen; }
            set { notifier.SetProperty(ref isSplitViewPaneOpen, value); }
        }

        public MosaicGroupDataSource MosaicGroupDS => mosaicGroupDS;
        public MosaicSkillDataSource MosaicSkillDS => mosaicSkillDS;

        private void OnMosaicGroupDsPropertyChanged(object sender, PropertyChangedEventArgs ev) {
            System.Diagnostics.Debug.Assert(sender is MosaicGroupDataSource);
            switch (ev.PropertyName) {
            case nameof(MosaicDataSource.CurrentItem): {
                    //// auto-close split view pane
                    //this.IsSplitViewPaneOpen = false;
                }
                break;
            }
        }

        public void Dispose() {
            if (Disposed)
                return;
            Disposed = true;

            mosaicGroupDS.PropertyChanged -= OnMosaicGroupDsPropertyChanged;
            mosaicGroupDS.Dispose();
            mosaicSkillDS.Dispose();

            notifier.Dispose();

            GC.SuppressFinalize(this);
        }

    }

}
