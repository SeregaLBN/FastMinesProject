using System;
using System.Collections.Generic;
using System.ComponentModel;
using Fmg.Common;
using Fmg.Common.Geom;
using Fmg.Common.Notifier;
using Fmg.Core.Img;
using Fmg.Core.Types.Draw;

namespace Fmg.Core.Mosaic {

    public static class MosaicDrawModelConst {

        /// <summary> Цвет заливки ячейки по-умолчанию. Зависит от текущего UI манагера. Переопределяется одним из MVC-наследником </summary>
        public static Color DefaultBkColor = Color.Gray.Brighter();

    }

    /// <summary> MVC: draw model of mosaic field. </summary>
    /// <typeparam name="TImageInner">platform specific view/image/picture or other display context/canvas/window/panel</typeparam>
    public class MosaicDrawModel<TImageInner> : MosaicGameModel, IMosaicDrawModel<TImageInner>
        where TImageInner : class
    {

        /// <summary> Fit the area/padding to the size.
        /// <br/>
        /// При autoFit = true:
        /// <list type="number">
        /// <item><description>При любом изменении Size, Padding меняется пропорционально Size</description></item>
        /// <item><description>При любом изменении Size / MosaicType / SizeField / Padding,
        ///                    Мозаика равномерно вписывается во внутреннюю область {@link #getInnerSize()}</description></item>
        /// <item><description>При изменении Area, нужно что бы {@link #getMosaicSize()} + {@link #getPadding()}
        ///                    будут определять новый {@link #getSize()}</description></item>
        /// </list>
        ///
        /// <br/>
        /// При autoFit = false:
        /// <list type="number">
        /// <item><description>При изменении Size / MosaicType / SizeField:
        ///     <list type="bullet">
        ///     <item><description>Мозаика равномерно вписывается во вcю область {@link #getSize()} </description></item>
        ///     <item><description>при этом Padding заного перерасчитывается с нуля </description></item>
        ///     </list>
        /// </description></item>
        /// <item> <description>При изменении Offset меняется Padding так, чтобы InnerSize остался прежним </description></item>
        /// <item> <description>При изменении Padding перерасчитывается Area, так что бы мозаика вписывалась внутрь нового InnerSize. </description></item>
        /// <item> <description>При изменении Area: при этом Size и Offset не меняются, но при этом меняется Padding.left и Padding.bottom. </description></item>
        /// </list>
        /// </summary>
        private bool           _autoFit = true;
        private SizeDouble     _size;
        private BoundDouble    _padding = new BoundDouble(0);
        private TImageInner    _imgMine, _imgFlag;
        private ColorText      _colorText;
        private PenBorder      _penBorder;
        private FontInfo       _fontInfo;
        private BackgroundFill _backgroundFill;
        private Color          _backgroundColor = MosaicDrawModelConst.DefaultBkColor;
        private TImageInner    _imgBckgrnd;
        private bool lockChanging = false;

        public bool AutoFit {
            get => _autoFit;
            set { _notifier.SetProperty(ref this._autoFit, value); }
        }

        /// <summary> get mosaic size in pixels </summary>
        public SizeDouble MosaicSize => CellAttr.GetSize(SizeField);

        /// <summary> get inner size in pixels, куда равномерно вписана мозаика. Inner, т.к. снаружи есть ещё padding </summary>
        private SizeDouble InnerSize {
            get {
                var pad = Padding;
                var s = Size;
                return new SizeDouble(s.Width - pad.LeftAndRight, s.Height - pad.TopAndBottom);
            }
        }

        /// <summary> common size in pixels </summary>
        public SizeDouble Size {
            get {
                if ((_size.Width <= 0) || (_size.Height <= 0)) {
                    var s = MosaicSize;
                    var p = Padding;
                    s.Width  += p.LeftAndRight;
                    s.Height += p.TopAndBottom;
                    Size = s;
                }
                return _size;
            }
            set {
                this.CheckSize(value);
                _notifier.SetProperty(ref this._size, value);
            }
        }

        public BoundDouble Padding {
            get => _padding;
            set {
                this.CheckPadding(value);
                _notifier.SetProperty(ref this._padding, value);
            }
        }

        /// <summary> Offset to mosaic.
        /// Определяется Padding'ом  и, дополнительно, смещением к мозаике (т.к. мозаика равномерно вписана в InnerSize) </summary>
        public SizeDouble MosaicOffset {
            get {
                var pad = Padding;
                var offset     = new SizeDouble(pad.Left, pad.Top);
                var mosaicSize = MosaicSize;
                var innerSize  = InnerSize;
                if (mosaicSize == innerSize)
                    return offset;
                var dx = innerSize.Width  - mosaicSize.Width;
                var dy = innerSize.Height - mosaicSize.Height;
                return new SizeDouble(offset.Width + dx / 2, offset.Height + dy / 2);
            }
            set {
                var pad = Padding;
                var oldOffset = new SizeDouble(pad.Left, pad.Top);
                var dx = value.Width  - oldOffset.Width;
                var dy = value.Height - oldOffset.Height;
                var padNew = new BoundDouble(pad);
                padNew.Left   += dx;
                padNew.Top    += dy;
                padNew.Right  -= dx;
                padNew.Bottom -= dy;

                bool locked = lockChanging;
                try {
                    lockChanging = true;
                    Padding = padNew;
                } finally {
                    if (!locked)
                        lockChanging = false;
                }
            }
        }


        public TImageInner ImgMine {
            get => _imgMine;
            set => _notifier.SetProperty(ref _imgMine, value);
        }

        public TImageInner ImgFlag {
            get => _imgFlag;
            set =>_notifier.SetProperty(ref _imgFlag, value);
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
                    if (old != null) {
                        old.PropertyChanged -= OnBackgroundFillPropertyChanged;
                        old.Dispose();
                    }
                    if (value != null)
                        value.PropertyChanged += OnBackgroundFillPropertyChanged;
                }
            }
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
            get => _imgBckgrnd;
            set => _notifier.SetProperty(ref _imgBckgrnd, value);
        }

        private void OnFontInfoPropertyChanged(object sender, PropertyChangedEventArgs ev) {
            _notifier.FirePropertyChanged(nameof(this.FontInfo));
        }
        private void OnBackgroundFillPropertyChanged(object sender, PropertyChangedEventArgs ev) {
            _notifier.FirePropertyChanged(nameof(this.BkFill));
        }
        private void OnColorTextPropertyChanged(object sender, PropertyChangedEventArgs ev) {
            _notifier.FirePropertyChanged(nameof(this.ColorText));
        }
        private void OnPenBorderPropertyChanged(object sender, PropertyChangedEventArgs ev) {
            _notifier.FirePropertyChanged(nameof(this.PenBorder));
        }

        protected override void OnPropertyChanged(object sender, PropertyChangedEventArgs ev) {
            base.OnPropertyChanged(sender, ev);

            if (lockChanging)
                return;

            lockChanging = true;

            // see doc for member autoFit
            try {
                if (AutoFit) {
                    // recalc padding
                    if (ev.PropertyName == nameof(this.Size)) {
                        var evEx = ev as PropertyChangedExEventArgs<SizeDouble>;
                        var oldSize = evEx.OldValue;
                        if (oldSize != default(SizeDouble))
                            Padding = this.RecalcPadding(Padding, Size, oldSize);
                    }

                    // recalc area
                    switch (ev.PropertyName) {
                    case nameof(this.Size):
                    case nameof(this.SizeField):
                    case nameof(this.MosaicType):
                    case nameof(this.Padding):
                        var innerSize = InnerSize;
                        Area = MosaicHelper.FindAreaBySize(MosaicType, SizeField, ref innerSize);
                        break;
                    }

                    // recalc size
                    if (ev.PropertyName == nameof(this.Area)) {
                        var ms = MosaicSize;
                        var p = Padding;
                        if (((ms.Width  + p.LeftAndRight) <= 0) ||
                            ((ms.Height + p.TopAndBottom) <= 0))
                        {
                            // reset padding
                            p = new BoundDouble(0);
                            Padding = p;
                        }
                        Size = new SizeDouble(ms.Width + p.LeftAndRight, ms.Height + p.TopAndBottom);
                    }
                } else {
                    // recalc area / padding
                    switch (ev.PropertyName) {
                    case nameof(this.Size):
                    case nameof(this.SizeField):
                    case nameof(this.MosaicType):
                        var s = Size;
                        var realInnerSize = s;
                        Area = MosaicHelper.FindAreaBySize(MosaicType, SizeField, ref realInnerSize);
                        var padX = (s.Width  - realInnerSize.Width ) / 2;
                        var padY = (s.Height - realInnerSize.Height) / 2;
                        Padding = new BoundDouble(padX, padY, padX, padY);
                        break;
                    }

                    // recalc area
                    if (ev.PropertyName == nameof(this.Padding)) {
                        var innerSize = InnerSize;
                        Area = MosaicHelper.FindAreaBySize(MosaicType, SizeField, ref innerSize);
                    }

                    // recalc size
                    if (ev.PropertyName == nameof(this.Area)) {
                        var sm = MosaicSize;
                        var p = Padding;
                        var s = Size;
                        Padding = new BoundDouble(p.Left, p.Top,
                                                s.Width  - sm.Width  - p.Left,
                                                s.Height - sm.Height - p.Top);
                    }
                }

                switch (ev.PropertyName) {
                case nameof(this.MosaicType):
                case nameof(this.Area):
                case nameof(this.Size):
                case nameof(this.PenBorder):
                    var penBorder = PenBorder;
                    FontInfo.Size = CellAttr.GetSq(penBorder.Width);
                    break;
                }
            } finally {
                lockChanging = false;
            }
        }

        protected override void Disposing() {
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

}

