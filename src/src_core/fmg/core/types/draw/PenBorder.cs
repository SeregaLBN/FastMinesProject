using System;
using System.ComponentModel;
using Fmg.Common;
using Fmg.Common.Geom;
using Fmg.Common.Notifier;

namespace Fmg.Core.Types.Draw {

    /// <summary> Характеристики кисти у рамки ячейки</summary>
    public class PenBorder : INotifyPropertyChanged, IDisposable {

        private Color _colorShadow, _colorLight;
        private double _width;
        public event PropertyChangedEventHandler PropertyChanged {
            add    { _notifier.PropertyChanged += value;  }
            remove { _notifier.PropertyChanged -= value;  }
        }
        protected readonly NotifyPropertyChanged _notifier;

        public PenBorder() :
           this(Color.Black, Color.White, 3)
        //this(Color.GREEN, Color.RED, 1)
        {
            _notifier = new NotifyPropertyChanged(this);
        }

        public PenBorder(
              Color colorShadow,
              Color colorLight,
              double iWidth)
        {
            _colorShadow = colorShadow;
            _colorLight = colorLight;
            _width = iWidth;
        }

        public Color ColorShadow {
            get { return _colorShadow; }
            set { _notifier.SetProperty(ref _colorShadow, value); }
        }

        public Color ColorLight {
            get { return _colorLight; }
            set { _notifier.SetProperty(ref _colorLight, value); }
        }

        public double Width {
            get { return _width; }
            set {
                // _notifier.SetProperty(ref _width, value);
                double old = _width;
                if (_width.HasMinDiff(value))
                    return;
                _width = value;
                _notifier.FirePropertyChanged(old, value);
            }
        }

        public override int GetHashCode() {
            unchecked {
                var hashCode = _colorShadow.GetHashCode();
                hashCode = (hashCode * 397) ^ _colorLight.GetHashCode();
                hashCode = (hashCode * 397) ^ _width.GetHashCode();
                return hashCode;
            }
        }

        protected bool Equals(PenBorder other) {
            return (_width == other._width)
                  && _colorShadow.Equals(other._colorShadow)
                  && _colorLight .Equals(other._colorLight);
        }

        public override bool Equals(object other) {
            return (other is PenBorder penObj) && Equals(penObj);
        }

        public void Dispose() {
            _notifier.Dispose();
            GC.SuppressFinalize(this);
        }

    }

}
