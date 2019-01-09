using System;
using System.ComponentModel;
using fmg.common.geom;
using fmg.common.notyfier;

namespace fmg.core.img {

    /// <summary> Model of the flag image </summary>
    public class FlagModel : IImageModel {

        private SizeDouble _size = new SizeDouble(AnimatedImageModelConsts.DefaultImageSize, AnimatedImageModelConsts.DefaultImageSize);
        private BoundDouble _padding = new BoundDouble(AnimatedImageModelConsts.DefaultPadding);
        public event PropertyChangedEventHandler PropertyChanged;
        protected readonly NotifyPropertyChanged _notifier;

        /// <summary> width and height in pixel </summary>
        public SizeDouble Size {
            get { return _size; }
            set {
                this.CheckSize(value);
                SizeDouble old = _size;
                if (_notifier.SetProperty(ref _size, value))
                    Padding = this.RecalcPadding(Padding, _size, old);
            }
        }

        public BoundDouble Padding {
            get => _padding;
            set {
                this.CheckPadding(value);
                _notifier.SetProperty(ref this._padding, value);
            }
        }

        public void Dispose() {
            _notifier.Dispose();
            GC.SuppressFinalize(this);
        }

    }

}
