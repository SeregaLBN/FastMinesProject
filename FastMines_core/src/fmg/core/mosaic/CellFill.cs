using System;
using System.Collections.Generic;
using System.ComponentModel;
using Fmg.Common;
using Fmg.Common.Notifier;

namespace Fmg.Core.Mosaic {

    /// <summary> all that apply to the background fill of cells </summary>
    public class CellFill : INotifyPropertyChanged, IDisposable {
        /// <summary> режим заливки фона ячеек </summary>
        private int _mode = 0;
        /// <summary> кэшированные цвета фона ячеек </summary>
        private readonly IDictionary<int, Color> _colors = new Dictionary<int, Color>();

        public event PropertyChangedEventHandler PropertyChanged {
            add    { _notifier.PropertyChanged += value;  }
            remove { _notifier.PropertyChanged -= value;  }
        }
        protected readonly NotifyPropertyChanged _notifier;

        public CellFill() {
            _notifier = new NotifyPropertyChanged(this);
        }

        /// <summary> режим заливки фона ячеек
        ///  @param mode
        ///   <li> 0 - цвет заливки фона по-умолчанию
        ///   <li> not 0 - радуга %)
        /// </summary>
        public int Mode {
            get { return _mode; }
            set {
                if (_notifier.SetProperty(ref _mode, value))
                    _colors.Clear();
            }
        }

        /// <summary> кэшированные цвета фона ячеек
        /// Нет цвета? - создасться с нужной интенсивностью! */
        /// </summary>
        public Color GetColor(int index) {
            if (_colors.ContainsKey(index))
                return _colors[index];

            var res = Color.RandomColor().Brighter(0.45);
            _colors.Add(index, res);
            return res;
        }

        public void Dispose() {
            _notifier.Dispose();
            _colors.Clear();
        }

    }

}
