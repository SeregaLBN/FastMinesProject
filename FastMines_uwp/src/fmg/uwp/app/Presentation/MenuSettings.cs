using Fmg.Common.Notifier;
using System;
using System.ComponentModel;

namespace Fmg.Uwp.App.Presentation {

    /// <summary> Main menu save data </summary>
    public class MenuSettings : INotifyPropertyChanged, IDisposable {

        public const bool DEFAULT_SPLIT_PANE_OPEN = true;

        private bool _splitPaneOpen = DEFAULT_SPLIT_PANE_OPEN;

        protected readonly NotifyPropertyChanged _notifier;
        public event PropertyChangedEventHandler PropertyChanged {
            add    { _notifier.PropertyChanged += value;  }
            remove { _notifier.PropertyChanged -= value;  }
        }

        public MenuSettings() {
            _notifier = new NotifyPropertyChanged(this, true);
        }

        public bool SplitPaneOpen {
            get { return _splitPaneOpen; }
            set {
                _notifier.SetProperty(ref _splitPaneOpen, value);
            }
        }

        public void Dispose() {
            _notifier.Dispose();
            GC.SuppressFinalize(this);
        }

    }

}
