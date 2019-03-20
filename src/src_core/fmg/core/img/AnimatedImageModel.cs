using System;
using System.ComponentModel;
using fmg.common;
using fmg.common.geom;
using fmg.common.notifier;

namespace fmg.core.img {

    public static class AnimatedImageModelConst {

        public static readonly Color DefaultBkColor = Color.DarkOrange; // Color.Coral; // 
        public static readonly Color DefaultForegroundColor = Color.LightSeaGreen; // Color.Orchid; // 
        public const int DefaultImageSize = 100;
        public const int DefaultPadding = (int)(DefaultImageSize * 0.05); // 5%

    }

    /// <summary> MVC: model. Common animated image characteristics. </summary>
    public abstract class AnimatedImageModel : IAnimatedModel {

        /// <summary> width and height in pixel </summary>
        private SizeDouble _size = new SizeDouble(AnimatedImageModelConst.DefaultImageSize, AnimatedImageModelConst.DefaultImageSize);
        /// <summary> inside padding </summary>
        private BoundDouble _padding = new BoundDouble(AnimatedImageModelConst.DefaultPadding);
        private Color _foregroundColor = AnimatedImageModelConst.DefaultForegroundColor;
        /// <summary> background fill color </summary>
        private Color _backgroundColor = AnimatedImageModelConst.DefaultBkColor;
        private Color _borderColor = Color.Maroon.Darker(0.5);
        private double _borderWidth = 3;
        /// <summary> 0° .. +360° </summary>
        private double _rotateAngle;

        /** animation of polar lights */
        private bool _polarLights = true;
        /** animation direction (example: clockwise or counterclockwise for simple rotation) */
        private bool _animeDirection = true;
        private readonly AnimatedInnerModel _innerModel = new AnimatedInnerModel();

        protected bool Disposed { get; private set; }
        public event PropertyChangedEventHandler PropertyChanged;
        protected readonly NotifyPropertyChanged _notifier;

        protected AnimatedImageModel() {
            _notifier = new NotifyPropertyChanged(this, ev => PropertyChanged?.Invoke(this, ev));
            _innerModel.PropertyChanged += OnInnerModelPropertyChanged;
        }

        /// <summary> width and height in pixel </summary>
        public SizeDouble Size {
            get { return _size; }
            set {
                this.CheckSize(value);
                var old = _size;
                if (_notifier.SetProperty(ref _size, value))
                    Padding = this.RecalcPadding(_padding, _size, old);
            }
        }

        /// <summary> inside padding </summary>
        public BoundDouble Padding {
            get { return _padding; }
            set {
                this.CheckPadding(value);
                _notifier.SetProperty(ref _padding, value);
            }
        }

        public Color ForegroundColor {
            get { return _foregroundColor; }
            set { _notifier.SetProperty(ref _foregroundColor, value); }
        }

        /// <summary> background fill color </summary>
        public Color BackgroundColor {
            get { return _backgroundColor; }
            set { _notifier.SetProperty(ref _backgroundColor, value); }
        }

        public Color BorderColor {
            get { return _borderColor; }
            set { _notifier.SetProperty(ref _borderColor, value); }
        }

        public double BorderWidth {
            get { return _borderWidth; }
            set {
                // _notifier.SetProperty(ref _borderWidth, value);
                if (!_borderWidth.HasMinDiff(value)) {
                    double old = _borderWidth;
                    _borderWidth = value;
                    _notifier.FirePropertyChanged<double>(old, value, nameof(this.BorderWidth));
                }
            }
        }

        /// <summary> 0° .. +360° </summary>
        public double RotateAngle {
            get { return _rotateAngle; }
            set { _notifier.SetProperty(ref _rotateAngle, FixAngle(value)); }
        }

        /// <summary> to diapason (0° .. +360°] </summary>
        internal static double FixAngle(double value) {
            return (value >= 360)
                 ? (value % 360)
                 : (value < 0)
                    ? (value % 360) + 360
                    :  value;
        }

        public bool Animated {
            get { return _innerModel.Animated; }
            set { _innerModel.Animated = value; }
        }

        //[Obsolete("oldest use")]
        //public void SetRIandRAD(int redrawInterval = 100, double rotateAngleDelta = 1.4) {
        //    double totalFrames = 360 / rotateAngleDelta;
        //    double animatePeriod = totalFrames * redrawInterval;
        //    TotalFrames = (int)totalFrames;
        //    AnimatePeriod = (long)animatePeriod;
        //}

        /// <summary> Overall animation period (in milliseconds) </summary>
        public long AnimatePeriod {
            get { return _innerModel.AnimatePeriod; }
            set { _innerModel.AnimatePeriod = value; }
        }

        /// <summary> Total frames of the animated period </summary>
        public int TotalFrames {
            get { return _innerModel.TotalFrames; }
            set { _innerModel.TotalFrames = value; }
        }

        public int CurrentFrame {
            get { return _innerModel.CurrentFrame; }
            set { _innerModel.CurrentFrame = value; }
        }

        public bool PolarLights {
            get { return _polarLights; }
            set { _notifier.SetProperty(ref _polarLights, value); }
        }

        public bool AnimeDirection {
            get { return _animeDirection; }
            set { _notifier.SetProperty(ref _animeDirection, value); }
        }

        protected void OnInnerModelPropertyChanged(object sender, PropertyChangedEventArgs ev) {
            // refire
            _notifier.FirePropertyChanged(ev);
        }

        // <summary>  Dispose managed resources </summary>/
        protected virtual void Disposing() {
            _innerModel.PropertyChanged -= OnInnerModelPropertyChanged;
            _notifier.Dispose();
        }

        public void Dispose() {
            if (Disposed)
                return;
            Disposed = true;
            Disposing();
            GC.SuppressFinalize(this);
        }

    }

}
