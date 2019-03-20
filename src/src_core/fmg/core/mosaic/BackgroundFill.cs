using System;
using System.Collections.Generic;
using System.ComponentModel;
using fmg.common;
using fmg.common.geom;
using fmg.common.notifier;
using fmg.core.img;
using fmg.core.types.draw;

namespace fmg.core.mosaic {

    /// <summary> all that apply to the background fill of cells </summary>
    public class BackgroundFill : INotifyPropertyChanged, IDisposable {
        /// <summary> режим заливки фона ячеек </summary>
        private int _mode = 0;
        /// <summary> кэшированные цвета фона ячеек </summary>
        private readonly IDictionary<int, Color> _colors = new Dictionary<int, Color>();

        public event PropertyChangedEventHandler PropertyChanged;
        protected readonly NotifyPropertyChanged _notifier;

        public BackgroundFill() {
            _notifier = new NotifyPropertyChanged(this, ev => PropertyChanged?.Invoke(this, ev));
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
