using System;
using System.ComponentModel;
using fmg.common.notifier;
using fmg.DataModel.DataSources;
using fmg.common.geom;

namespace fmg.common {

    /// <summary> ViewModel for <see cref="SelectMosaicPage"/> </summary>
    public class MosaicsViewModel : INotifyPropertyChanged, IDisposable {

        private readonly MosaicDataSource mosaicDS = new MosaicDataSource();
        protected bool Disposed { get; private set; }
        public event PropertyChangedEventHandler PropertyChanged;
        protected readonly NotifyPropertyChanged notifier;

        public MosaicsViewModel() {
            mosaicDS.PropertyChanged += OnMosaicDsPropertyChanged;
            notifier = new NotifyPropertyChanged(this, false);
            notifier.PropertyChanged += OnNotifierPropertyChanged;
        }

        public MosaicDataSource MosaicDS => mosaicDS;

        public SizeDouble ImageSize {
            get { return mosaicDS.ImageSize; }
            set { mosaicDS.ImageSize = value; }
        }

        private void OnMosaicDsPropertyChanged(object sender, PropertyChangedEventArgs ev) {
            notifier.FirePropertyChanged(nameof(this.MosaicDS));
            if (ev.PropertyName == nameof(MosaicDataSource.ImageSize)) {
                // ! notify parent container
                notifier.FirePropertyChanged<SizeDouble>(ev, nameof(this.ImageSize));
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

            mosaicDS.PropertyChanged -= OnMosaicDsPropertyChanged;
            mosaicDS.Dispose();

            notifier.PropertyChanged -= OnNotifierPropertyChanged;
            notifier.Dispose();

            NotifyPropertyChanged.AssertCheckSubscribers(this);

            GC.SuppressFinalize(this);
        }

    }

}
