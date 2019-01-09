using System;
using System.Collections.Generic;
using System.ComponentModel;
using System.Linq;
using fmg.common;
using fmg.common.geom;
using fmg.common.notyfier;

namespace fmg.core.img {

    /// <summary> MVC: model of representable menu as horizontal or vertical lines </summary>
    public sealed class BurgerMenuModel : IAnimatedModel {

        private AnimatedImageModel _generalModel;
        private bool _show = true;
        private bool _horizontal = true;
        private int _layers = 3;
        private BoundDouble? _padding;

        private bool _disposed;
        public event PropertyChangedEventHandler PropertyChanged;
        private readonly NotifyPropertyChanged _notifier;

        /// <summary> ctor </summary>
        /// <param name="generalModel">another basic model</param>
        internal BurgerMenuModel(AnimatedImageModel generalModel) {
            _notifier = new NotifyPropertyChanged(this, ev => PropertyChanged?.Invoke(this, ev));
            _generalModel = generalModel;
            _generalModel.PropertyChanged += OnGeneralModelPropertyChanged;
        }

        /// <summary> image width and height in pixel </summary>
        public SizeDouble Size          { get => _generalModel.Size         ; set => _generalModel.Size          = value; }
        public bool       Animated      { get => _generalModel.Animated     ; set => _generalModel.Animated      = value; }
        public long       AnimatePeriod { get => _generalModel.AnimatePeriod; set => _generalModel.AnimatePeriod = value; }
        public int        TotalFrames   { get => _generalModel.TotalFrames  ; set => _generalModel.TotalFrames   = value; }
        public int        CurrentFrame  { get => _generalModel.CurrentFrame ; set => _generalModel.CurrentFrame  = value; }

        public bool Show {
            get { return _show; }
            set { _notifier.SetProperty(ref _show, value); }
        }

        public bool Horizontal {
            get { return _horizontal; }
            set { _notifier.SetProperty(ref _horizontal, value); }
        }

        public int Layers {
            get { return _layers; }
            set { _notifier.SetProperty(ref _layers, value); }
        }

        /// <summary> inside padding </summary>
        public BoundDouble Padding {
            get {
                if (_padding == null)
                    RecalcPadding(null);
                return _padding.Value;
            }
            set {
                this.CheckPadding(value);
                _notifier.SetProperty(ref _padding, value);
            }
        }
        private void RecalcPadding(SizeDouble? old) {
            SizeDouble size = Size;
            var paddingNew = (_padding == null)
                  ? new BoundDouble(size.Width / 2,
                                    size.Height / 2,
                                    _generalModel.Padding.Right,
                                    _generalModel.Padding.Bottom)
                  : this.RecalcPadding(_padding.Value, size, old.Value);
            _notifier.SetProperty(ref _padding, paddingNew, nameof(this.Padding));
        }

        internal struct LineInfo {
            public Color clr;
            public double penWidht;
            public PointDouble from; // start coord
            public PointDouble to;   // end   coord
        }

        /// <summary> get paint information of drawing burger menu model image </summary>
        internal IEnumerable<LineInfo> Coords {
            get {
                if (!Show)
                    return Enumerable.Empty<LineInfo>();

                bool horizontal = Horizontal;
                int layers = Layers;
                var pad = Padding;
                var rc = new RectDouble(pad.Left,
                                        pad.Top,
                                        Size.Width - pad.LeftAndRight,
                                        Size.Height - pad.TopAndBottom); ;
                double penWidth = Math.Max(1, (horizontal ? rc.Height : rc.Width) / (2.0 * layers));
                double rotateAngle = _generalModel.RotateAngle;
                double stepAngle = 360.0 / layers;

                return Enumerable.Range(0, layers)
                   .Select(layerNum => {
                       double layerAlignmentAngle = AnimatedImageModel.FixAngle(layerNum * stepAngle + rotateAngle);
                       double offsetTop = !horizontal ? 0 : layerAlignmentAngle * rc.Height / 360;
                       double offsetLeft = horizontal ? 0 : layerAlignmentAngle * rc.Width / 360;
                       var start = new PointDouble(rc.Left() + offsetLeft,
                                                   rc.Top() + offsetTop);
                       var end = new PointDouble((horizontal ? rc.Right() : rc.Left()) + offsetLeft,
                                                 (horizontal ? rc.Top() : rc.Bottom()) + offsetTop);

                       var hsv = new HSV(Color.Gray);
                       hsv.v *= Math.Sin(layerNum * stepAngle / layers);

                       var li = new LineInfo {
                           clr = hsv.ToColor(),
                           penWidht = penWidth,
                           from = start,
                           to = end
                       };
                       return li;
                   });
            }
        }

        private void OnGeneralModelPropertyChanged(object sender, PropertyChangedEventArgs ev) {
            System.Diagnostics.Debug.Assert(ReferenceEquals(sender, _generalModel));
            if (nameof(IImageModel.Size) == ev.PropertyName) {
                if (ev is PropertyChangedExEventArgs<SizeDouble> evEx)
                    RecalcPadding(evEx.OldValue);
                else
                    throw new Exception();
            }
        }

        public void Dispose() {
            if (_disposed)
                return;
            _disposed = true;

            _generalModel.PropertyChanged -= OnGeneralModelPropertyChanged;
            _notifier.Dispose();
            _generalModel = null;

            GC.SuppressFinalize(this);
        }

    }

}
