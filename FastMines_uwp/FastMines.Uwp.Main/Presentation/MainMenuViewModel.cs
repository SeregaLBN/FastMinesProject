using System;
using System.ComponentModel;
using System.Windows.Input;
using fmg.DataModel.DataSources;
using fmg.common.notifier;
using fmg.common.Converters;

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
            mosaicSkillDS.PropertyChanged += OnMosaicSkillDsPropertyChanged;
            notifier = new NotifyPropertyChanged(this, false);
            notifier.PropertyChanged += OnNotifierPropertyChanged;
        }

        public ICommand ToggleSplitViewPaneCommand { get; private set; }

        public bool IsSplitViewPaneOpen {
            get { return isSplitViewPaneOpen; }
            set { notifier.SetProperty(ref isSplitViewPaneOpen, value); }
        }

        public MosaicGroupDataSource MosaicGroupDS => mosaicGroupDS;
        public MosaicSkillDataSource MosaicSkillDS => mosaicSkillDS;

        public DipWrapper MenuGroupPaddingInDip => new DipWrapper(0);

        private void OnMosaicSkillDsPropertyChanged(object sender, PropertyChangedEventArgs ev) {
            System.Diagnostics.Debug.Assert(sender is MosaicSkillDataSource);
            notifier.FirePropertyChanged(nameof(this.MosaicSkillDS));
        }

        private void OnMosaicGroupDsPropertyChanged(object sender, PropertyChangedEventArgs ev) {
            System.Diagnostics.Debug.Assert(sender is MosaicGroupDataSource);
            notifier.FirePropertyChanged(nameof(this.MosaicGroupDS));
            switch (ev.PropertyName) {
            case nameof(MosaicDataSource.CurrentItem): {
                    //// auto-close split view pane
                    //this.IsSplitViewPaneOpen = false;
                }
                break;
            }
        }

        private void OnNotifierPropertyChanged(object sender, PropertyChangedEventArgs ev) {
            System.Diagnostics.Debug.Assert(ReferenceEquals(sender, notifier));
            PropertyChanged?.Invoke(this, ev);
        }

        public void Dispose() {
            if (Disposed)
                return;
            Disposed = true;

            mosaicGroupDS.PropertyChanged -= OnMosaicGroupDsPropertyChanged;
            mosaicSkillDS.PropertyChanged -= OnMosaicSkillDsPropertyChanged;
            mosaicGroupDS.Dispose();
            mosaicSkillDS.Dispose();

            notifier.PropertyChanged -= OnNotifierPropertyChanged;
            notifier.Dispose();

            NotifyPropertyChanged.AssertCheckSubscribers(this);

            GC.SuppressFinalize(this);
        }

    }

}
