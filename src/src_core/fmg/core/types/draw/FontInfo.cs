using System;
using System.ComponentModel;
using fmg.common.geom;
using fmg.common.notyfier;

namespace fmg.core.types.draw {

    public class FontInfo : INotifyPropertyChanged, IDisposable {

        private string _name = "Arial"; // Times New Roman // Verdana // Courier New // SansSerif;
        private bool _bold = false;
        private double _size = 10;
        public event PropertyChangedEventHandler PropertyChanged;
        protected readonly NotifyPropertyChanged _notifier;

        public FontInfo() {
            _notifier = new NotifyPropertyChanged(this, ev => PropertyChanged?.Invoke(this, ev));
        }

        public string Name {
            get { return _name; }
            set { _notifier.SetProperty(ref _name, value); }
        }

        public bool Bold {
            get { return _bold; }
            set { _notifier.SetProperty(ref _bold, value); }
        }

        public double Size {
            get { return _size; }
            set {
                // _notifier.SetProperty(ref _size, value);
                double old = _size;
                if (_size.HasMinDiff(value))
                    return;
                _size = value;
                _notifier.OnPropertyChanged(old, value);
            }
        }

        protected bool Equals(FontInfo other) {
            return string.Equals(_name, other._name) && (_bold == other._bold) && (_size == other._size);
        }

        public override bool Equals(object obj) {
            if (ReferenceEquals(null, obj)) return false;
            if (ReferenceEquals(this, obj)) return true;
            return (GetType() == obj.GetType()) && Equals((FontInfo)obj);
        }

        public override int GetHashCode() {
            unchecked {
                var hashCode = _name?.GetHashCode() ?? 0;
                hashCode = (hashCode * 397) ^ _bold.GetHashCode();
                return (hashCode * 397) ^ _size.GetHashCode();
            }
        }

        public override string ToString() {
            return string.Format("FontInfo={{name={0}, bold={1}, size={2}}}", _name, _bold, _size);
        }

        /// <summary> off notifer </summary>
        public IDisposable Hold() {
            return _notifier.Hold();
        }

        public void Dispose() {
            _notifier.Dispose();
            GC.SuppressFinalize(this);
        }

    }

}
