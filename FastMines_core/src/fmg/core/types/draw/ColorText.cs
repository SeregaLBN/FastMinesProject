using Fmg.Common;
using Fmg.Common.Notifier;
using System;
using System.ComponentModel;

namespace Fmg.Core.Types.Draw {

    public class ColorText : INotifyPropertyChanged, IDisposable {

        private readonly Color[] _colorOpen;
        private readonly Color[] _colorClose;
        public event PropertyChangedEventHandler PropertyChanged {
            add    { _notifier.PropertyChanged += value;  }
            remove { _notifier.PropertyChanged -= value;  }
        }
        protected readonly NotifyPropertyChanged _notifier;

        public ColorText() {
            _notifier = new NotifyPropertyChanged(this);
            _colorOpen = new Color[EOpenEx.GetValues().Length];
            _colorClose = new Color[ECloseEx.GetValues().Length];

            foreach (var eOpen in EOpenEx.GetValues())
                switch (eOpen) {
                case EOpen._Nil : _colorOpen[eOpen.Ordinal()] = Color.Black;  break;
                case EOpen._1   : _colorOpen[eOpen.Ordinal()] = Color.Navy;   break;
                case EOpen._2   : _colorOpen[eOpen.Ordinal()] = Color.Green;  break;
                case EOpen._3   : _colorOpen[eOpen.Ordinal()] = Color.Red;    break;
                case EOpen._4   : _colorOpen[eOpen.Ordinal()] = Color.Maroon; break;
                case EOpen._5   : _colorOpen[eOpen.Ordinal()] = Color.Blue;   break;
                case EOpen._6   : _colorOpen[eOpen.Ordinal()] = Color.Black;  break;
                case EOpen._7   : _colorOpen[eOpen.Ordinal()] = Color.Olive;  break;
                case EOpen._8   : _colorOpen[eOpen.Ordinal()] = Color.Aqua;   break;
                case EOpen._9   : _colorOpen[eOpen.Ordinal()] = Color.Navy;   break;
                case EOpen._10  : _colorOpen[eOpen.Ordinal()] = Color.Green;  break;
                case EOpen._11  : _colorOpen[eOpen.Ordinal()] = Color.Red;    break;
                case EOpen._12  : _colorOpen[eOpen.Ordinal()] = Color.Maroon; break;
                case EOpen._13  : _colorOpen[eOpen.Ordinal()] = Color.Navy;   break;
                case EOpen._14  : _colorOpen[eOpen.Ordinal()] = Color.Green;  break;
                case EOpen._15  : _colorOpen[eOpen.Ordinal()] = Color.Red;    break;
                case EOpen._16  : _colorOpen[eOpen.Ordinal()] = Color.Maroon; break;
                case EOpen._17  : _colorOpen[eOpen.Ordinal()] = Color.Blue;   break;
                case EOpen._18  : _colorOpen[eOpen.Ordinal()] = Color.Black;  break;
                case EOpen._19  : _colorOpen[eOpen.Ordinal()] = Color.Olive;  break;
                case EOpen._20  : _colorOpen[eOpen.Ordinal()] = Color.Aqua;   break;
                case EOpen._21  : _colorOpen[eOpen.Ordinal()] = Color.Navy;   break;
                case EOpen._Mine: _colorOpen[eOpen.Ordinal()] = Color.Black;  break;
                default: throw new Exception("add EOpen value");
                }

            foreach (var eClose in ECloseEx.GetValues())
                switch (eClose) {
                case EClose._Unknown: _colorClose[eClose.Ordinal()] = Color.Teal;  break;
                case EClose._Clear  : _colorClose[eClose.Ordinal()] = Color.Black; break;
                case EClose._Flag   : _colorClose[eClose.Ordinal()] = Color.Red;   break;
                default: throw new Exception("add EClose value");
                }
        }

        public Color GetColorOpen(int i) {
            return _colorOpen[i];
        }

        public void SetColorOpen(int i, Color colorOpen) {
            _notifier.SetProperty(ref _colorOpen[i], colorOpen, "ColorOpen..#" + i);
        }

        public Color GetColorClose(int i) {
            return _colorClose[i];
        }

        public void SetColorClose(int i, Color colorClose) {
            _notifier.SetProperty(ref _colorClose[i], colorClose, "ColorClose.#" + i);
        }

        public void Dispose() {
            _notifier.Dispose();
            GC.SuppressFinalize(this);
        }

    }

}
