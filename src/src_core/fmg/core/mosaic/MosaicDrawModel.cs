using System;
using System.Collections.Generic;
using System.ComponentModel;
using fmg.common;
using fmg.common.geom;
using fmg.common.notyfier;
using fmg.core.img;
using fmg.core.types.draw;

namespace fmg.core.mosaic {

    public static class MosaicDrawModelConst {

        /// <summary> Цвет заливки ячейки по-умолчанию. Зависит от текущего UI манагера. Переопределяется одним из MVC-наследником </summary>
        public static Color DefaultBkColor = Color.Gray.Brighter();

    }

    /// <summary> MVC: draw model of mosaic field. </summary>
    /// <typeparam name="TImageInner">platform specific view/image/picture or other display context/canvas/window/panel</typeparam>
    public class MosaicDrawModel<TImageInner> : MosaicGameModel, IImageModel
        where TImageInner : class
    {

        private TImageInner    _imgMine, _imgFlag;
        private ColorText      _colorText;
        private PenBorder      _penBorder;
        private FontInfo       _fontInfo;
        /// <summary> автоматически регулирую при явной установке размера </summary>
        private BoundDouble    _margin  = new BoundDouble(0, 0, 0, 0);
        private BoundDouble    _padding = new BoundDouble(0, 0, 0, 0);
        private BackgroundFill _backgroundFill;
        private Color          _backgroundColor = MosaicDrawModelConst.DefaultBkColor;
        private TImageInner    _imgBckgrnd;

        public MosaicDrawModel() {
            this.PropertyChanged += OnPropertyChanged;
        }

        /// <summary> размер в пикселях поля мозаики. Inner, т.к. снаружи есть ещё padding и margin </summary>
        public SizeDouble InnerSize => CellAttr.GetSize(SizeField);

        /// <summary> общий размер в пискелях </summary>
        public SizeDouble Size {
            get {
                var size = InnerSize;
                var m = Margin;
                var p = Padding;
                size.Width  += m.LeftAndRight + p.LeftAndRight;
                size.Height += m.TopAndBottom + p.TopAndBottom;
                return size;
            }
            set {
                if (value.Width < 1)
                    throw new ArgumentException("Size value widht must be > 1");
                if (value.Height < 1)
                    throw new ArgumentException("Size value height must be > 1");

                var oldSize = Size;
                var oldPadding = Padding;
                var newPadding = new BoundDouble(oldPadding.Left   * value.Width  / oldSize.Width,
                                                 oldPadding.Top    * value.Height / oldSize.Height,
                                                 oldPadding.Right  * value.Width  / oldSize.Width,
                                                 oldPadding.Bottom * value.Height / oldSize.Height);
                var toCalc = new SizeDouble(value.Width  - newPadding.LeftAndRight,
                                            value.Height - newPadding.TopAndBottom);
                var area = MosaicHelper.FindAreaBySize(MosaicType, SizeField, ref toCalc);
                BoundDouble margin = new BoundDouble(0);
                margin.Left = margin.Right  = (value.Width  - newPadding.LeftAndRight - toCalc.Width ) / 2;
                margin.Top  = margin.Bottom = (value.Height - newPadding.TopAndBottom - toCalc.Height) / 2;

                Area = area;
                Margin = margin;
                SetPaddingInternal(newPadding);
            }
        }

        public TImageInner ImgMine {
            get { return _imgMine; }
            set {
                object old = this._imgMine;
                if (old != value) { // references compare
                    this._imgMine = value;
                    _notifier.OnPropertyChanged(old, value);
                }
            }
        }

        public TImageInner ImgFlag {
            get { return _imgFlag; }
            set {
                object old = this._imgFlag;
                if (!ReferenceEquals(old, value)) { // references compare
                    this._imgFlag = value;
                    _notifier.OnPropertyChanged(old, value);
                }
            }
        }

        public ColorText ColorText {
            get {
                if (_colorText == null)
                    ColorText = new ColorText();
                return _colorText;
            }
            set {
                ColorText old = this._colorText;
                if (_notifier.SetProperty(ref _colorText, value)) {
                    if (old != null)
                        old.PropertyChanged -= OnColorTextPropertyChanged;
                    if (value != null)
                        value.PropertyChanged += OnColorTextPropertyChanged;
                }
            }
        }

        public PenBorder PenBorder {
            get {
                if (_penBorder == null)
                    PenBorder = new PenBorder();
                return _penBorder;
            }
            set {
                PenBorder old = this._penBorder;
                if (_notifier.SetProperty(ref _penBorder, value)) {
                    if (old != null)
                        old.PropertyChanged -= OnPenBorderPropertyChanged;
                    if (value != null)
                        value.PropertyChanged += OnPenBorderPropertyChanged;
                }
            }
        }

        public BackgroundFill BkFill {
            get {
                if (_backgroundFill == null)
                    BkFill = new BackgroundFill();
                return _backgroundFill;
            }
            set {
                var old = this._backgroundFill;
                if (_notifier.SetProperty(ref _backgroundFill, value)) {
                    if (old != null)
                        old.PropertyChanged -= OnBackgroundFillPropertyChanged;
                    if (value != null)
                        value.PropertyChanged += OnBackgroundFillPropertyChanged;
                }
            }
        }

        public BoundDouble Margin {
            get { return _margin; }
            /// <summary> is only set when resizing. </summary>
            private set {
                if (value.Left < 0)
                    throw new ArgumentException("Margin left value must be > 0");
                if (value.Top < 0)
                    throw new ArgumentException("Margin top value must be > 0");
                if (value.Right < 0)
                    throw new ArgumentException("Margin right value must be > 0");
                if (value.Bottom < 0)
                    throw new ArgumentException("Margin bottom value must be > 0");

                _notifier.SetProperty(ref _margin, value);
            }
        }

        public BoundDouble Padding {
            get { return _padding; }
            set {
                if (value.Left < 0)
                    throw new ArgumentException("Padding left value must be > 0");
                if (value.Top < 0)
                    throw new ArgumentException("Padding top value must be > 0");
                if (value.Right < 0)
                    throw new ArgumentException("Padding right value must be > 0");
                if (value.Bottom < 0)
                    throw new ArgumentException("Padding bottom value must be > 0");

                var size = Size;
                if ((size.Width - value.LeftAndRight) < 1)
                    throw new ArgumentException("The left and right padding are very large");
                if ((size.Height - value.TopAndBottom) < 1)
                    throw new ArgumentException("The top and bottom padding are very large");

                var toCalc = new SizeDouble(size.Width  - value.LeftAndRight,
                                            size.Height - value.TopAndBottom);
                var area = MosaicHelper.FindAreaBySize(MosaicType, SizeField, ref toCalc);
                BoundDouble margin = new BoundDouble(0);
                margin.Left = margin.Right  = (size.Width  - value.LeftAndRight - toCalc.Width ) / 2;
                margin.Top  = margin.Bottom = (size.Height - value.TopAndBottom - toCalc.Height) / 2;

                Area = area;
                Margin = margin;
                SetPaddingInternal(value);
            }
        }
        public void SetPadding(double bound) { Padding = new BoundDouble(bound); }
        private void SetPaddingInternal(BoundDouble newBound) {
            _notifier.SetProperty(ref _padding, newBound, nameof(this.Padding));
        }

        public FontInfo FontInfo {
            get {
                if (_fontInfo == null)
                    FontInfo = new FontInfo();
                return _fontInfo;
            }
            set {
                FontInfo old = this._fontInfo;
                if (_notifier.SetProperty(ref _fontInfo, value)) {
                    if (old != null)
                        old.PropertyChanged -= OnFontInfoPropertyChanged;
                    if (value != null)
                        value.PropertyChanged += OnFontInfoPropertyChanged;
                }
            }
        }

        public Color BackgroundColor {
            get {
                return _backgroundColor;
            }
            set {
                _notifier.SetProperty(ref _backgroundColor, value);
            }
        }

        public TImageInner ImgBckgrnd {
            get { return _imgBckgrnd; }
            set {
                object old = this._imgBckgrnd;
                if (ReferenceEquals(old, value))
                    return;
                this._imgBckgrnd = value;
                _notifier.OnPropertyChanged(old, value);
            }
        }

        private void OnFontInfoPropertyChanged(object sender, PropertyChangedEventArgs ev) {
            _notifier.OnPropertyChanged(nameof(this.FontInfo));
        }
        private void OnBackgroundFillPropertyChanged(object sender, PropertyChangedEventArgs ev) {
            _notifier.OnPropertyChanged(nameof(this.BkFill));
        }
        private void OnColorTextPropertyChanged(object sender, PropertyChangedEventArgs ev) {
            _notifier.OnPropertyChanged(nameof(this.ColorText));
        }
        private void OnPenBorderPropertyChanged(object sender, PropertyChangedEventArgs ev) {
            _notifier.OnPropertyChanged(nameof(this.PenBorder));
        }

        protected virtual void OnPropertyChanged(object sender, PropertyChangedEventArgs ev) {
            switch (ev.PropertyName) {
            case nameof(this.Area):
            case nameof(this.SizeField):
            case nameof(this.MosaicType):
            case nameof(this.Padding):
            case nameof(this.Margin):
                _notifier.OnPropertyChanged(nameof(this.Size));
                break;
            }
        }

         /** off notifier */
        protected override IDisposable Hold() {
            var a0 = base.Hold();
            var a1 = ColorText.Hold();
            var a2 = PenBorder.Hold();
            var a3 = FontInfo.Hold();
            var a4 = BkFill.Hold();
            return new PlainFree() {
                _onDispose = () => {
                    a0.Dispose();
                    a1.Dispose();
                    a2.Dispose();
                    a3.Dispose();
                    a4.Dispose();
                }
            };
        }

        protected override void Disposing() {
            this.PropertyChanged -= OnPropertyChanged;
            BkFill.Dispose();
            base.Disposing();
            // unsubscribe from local notifications
            FontInfo = null;
            BkFill = null;
            ColorText = null;
            PenBorder = null;

            ImgBckgrnd= null;
            ImgFlag = null;
            ImgMine = null;
        }

    }

    /// <summary> всё что относиться к заливке фоном ячееек </summary>
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

        /// <summary> off notifer </summary>
        public IDisposable Hold() {
            return _notifier.Hold();
        }

        public void Dispose() {
            _notifier.Dispose();
            _colors.Clear();
        }

    }

}
