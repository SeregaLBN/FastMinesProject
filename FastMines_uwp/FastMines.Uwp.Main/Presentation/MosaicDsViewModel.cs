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
        public event PropertyChangedEventHandler PropertyChanged {
            add    { _notifier.PropertyChanged += value;  }
            remove { _notifier.PropertyChanged -= value;  }
        }
        protected readonly NotifyPropertyChanged _notifier;

        public MosaicsViewModel() {
            mosaicDS.PropertyChanged += OnMosaicDsPropertyChanged;
            _notifier = new NotifyPropertyChanged(this, false);
        }

        public MosaicDataSource MosaicDS => mosaicDS;

        public SizeDouble ImageSize {
            get { return mosaicDS.ImageSize; }
            set { mosaicDS.ImageSize = value; }
        }

        private void OnMosaicDsPropertyChanged(object sender, PropertyChangedEventArgs ev) {
            _notifier.FirePropertyChanged(nameof(this.MosaicDS));
            if (ev.PropertyName == nameof(MosaicDataSource.ImageSize)) {
                // ! notify parent container
                _notifier.FirePropertyChanged<SizeDouble>(ev, nameof(this.ImageSize));
            }
        }

        public void Dispose() {
            if (Disposed)
                return;
            Disposed = true;

            mosaicDS.PropertyChanged -= OnMosaicDsPropertyChanged;
            mosaicDS.Dispose();

            _notifier.Dispose();

            GC.SuppressFinalize(this);
        }

    }

}
