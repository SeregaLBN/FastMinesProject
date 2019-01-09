package fmg.core.mosaic;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.HashMap;
import java.util.Map;

import fmg.common.Color;
import fmg.common.geom.BoundDouble;
import fmg.common.geom.SizeDouble;
import fmg.common.notyfier.INotifyPropertyChanged;
import fmg.common.notyfier.NotifyPropertyChanged;
import fmg.core.img.IImageModel;
import fmg.core.types.draw.ColorText;
import fmg.core.types.draw.FontInfo;
import fmg.core.types.draw.PenBorder;

/**
 * MVC: draw model of mosaic field.
 *
 * @param <TImageInner> platform specific view/image/picture or other display context/canvas/window/panel
 **/
public class MosaicDrawModel<TImageInner> extends MosaicGameModel implements IImageModel {

    /** Цвет заливки ячейки по-умолчанию. Зависит от текущего UI манагера. Переопределяется одним из MVC-наследником. */
    public static Color DefaultBkColor = Color.Gray().brighter();

    /** Fit the area/padding to the size.
     *
     * <ul>При autoFit = true
     * <li> При любом изменении Size, Padding меняется пропорционально Size
     * <li> При любом изменении Size / FieldType / FieldSize / Padding,
     *      Мозаика равномерно вписывается во внутреннюю область {@link #getInnerSize()}
     * <li> Area напрямую не устанавливается. А если устанавливается, то {@link #getMosaicSize()} + {@link #getPadding()}
     *      будут определять новый {@link #getSize()}
     * </ul>
     *
     * <ul>При autoFit = false
     * <li> <ol>При любом изменении Size / FieldType / FieldSize:
     *      <li> Мозаика равномерно вписывается во вcю область {@link #getSize()}
     *      <li> при этом Padding заного перерасчитывается с нуля
     *      </ol>
     * <li> при изменении Offset меняется Padding так, чтобы InnerSize остался прежним
     * <li> Padding напрямую не устанавливается (меняется через установку Offset).
     *      А если меняется, то перерасчитывается Area, так что бы мозаика вписывалась внутрь нового InnerSize.
     * <li> Area меняется явно. При этом Size и Offset не меняются, но при этом меняется Padding.left и Padding.bottom.
     * </ul>
     **/
    private boolean        _autoFit = true;
    private SizeDouble     _size;
    private BoundDouble    _padding = new BoundDouble(0);
    private TImageInner    _imgMine;
    private TImageInner    _imgFlag;
    private TImageInner    _imgBckgrnd;
    private ColorText      _colorText;
    private PenBorder      _penBorder;
    private FontInfo       _fontInfo;
    private BackgroundFill _backgroundFill;
    private Color          _backgroundColor;

    public MosaicDrawModel() {
        _notifier.addListener(this::onPropertyChanged);
    }

    public static final String PROPERTY_AUTO_FIT         = "AutoFit";
    public static final String PROPERTY_IMG_MINE         = "ImgMine";
    public static final String PROPERTY_IMG_FLAG         = "ImgFlag";
    public static final String PROPERTY_IMG_BCKGRND      = "ImgBckgrnd";
    public static final String PROPERTY_COLOR_TEXT       = "ColorText";
    public static final String PROPERTY_PEN_BORDER       = "PenBorder";
    public static final String PROPERTY_BACKGROUND_FILL  = "BackgroundFill";
    public static final String PROPERTY_FONT_INFO        = "FontInfo";
    public static final String PROPERTY_BACKGROUND_COLOR = "BackgroundColor";

    public boolean getAutoFit() { return _autoFit; }
    public void setAutoFit(boolean autoFit) {
        _notifier.setProperty(this._autoFit, autoFit, PROPERTY_AUTO_FIT);
    }

    /** размер в пикселях поля мозаики */
    public SizeDouble getMosaicSize() {
        return getCellAttr().getSize(getSizeField());
    }
    /** размер внутренней области в пикселях, куда равномерно вписана мозаика. Inner, т.к. снаружи есть ещё padding */
    private SizeDouble getInnerSize() {
        BoundDouble pad = getPadding();
        SizeDouble s = getSize();
        return new SizeDouble(s.width - pad.getLeftAndRight(), s.height - pad.getTopAndBottom());
    }
    /** общий размер в пискелях */
    @Override
    public SizeDouble getSize() {
        if ((_size == null) ||
            (_size.width <= 0) ||
            (_size.height <= 0))
        {
            SizeDouble s = getMosaicSize();
            BoundDouble p = getPadding();
            s.width  += p.getLeftAndRight();
            s.height += p.getTopAndBottom();
            setSize(s);
        }
        return _size;
    }
    @Override
    public void setSize(SizeDouble size) {
        IImageModel.checkSize(size);
        _notifier.setProperty(this._size, size, PROPERTY_SIZE);
    }

    @Override
    public BoundDouble getPadding() { return _padding; }
    @Override
    public void setPadding(BoundDouble padding) {
        IImageModel.checkPadding(this, padding);
        _notifier.setProperty(this._padding, new BoundDouble(padding), PROPERTY_PADDING);
    }

    /** Offset to mosaic.
     * Определяется Padding'ом  и, дополнительно, смещением к мозаике (т.к. мозаика равномерно вписана в InnerSize) */
    public SizeDouble getMosaicOffset() {
        BoundDouble pad = getPadding();
        SizeDouble offset     = new SizeDouble(pad.left, pad.top);
        SizeDouble mosaicSize = getMosaicSize();
        SizeDouble innerSize  = getInnerSize();
        if (mosaicSize.equals(innerSize))
            return offset;
        double dx = innerSize.width - mosaicSize.width;
        double dy = innerSize.width - mosaicSize.width;
        return new SizeDouble(offset.width + dx / 2, offset.height + dy / 2);
    }

    /** set offset to mosaic */
    public void setMosaicOffset(SizeDouble offset) {
        BoundDouble pad = getPadding();
        SizeDouble oldOffset = new SizeDouble(pad.left, pad.top);
        double dx = offset.width  - oldOffset.width;
        double dy = offset.height - oldOffset.height;
        BoundDouble padNew = new BoundDouble(pad);
        padNew.left   += dx;
        padNew.top    += dy;
        padNew.right  -= dx;
        padNew.bottom -= dy;
        setPadding(padNew);
    }

    public TImageInner getImgMine() { return _imgMine; }
    public void setImgMine(TImageInner img) {
        _notifier.setProperty(this._imgMine, img, PROPERTY_IMG_MINE);
    }

    public TImageInner getImgFlag() { return _imgFlag; }
    public void setImgFlag(TImageInner img) {
        _notifier.setProperty(this._imgFlag, img, PROPERTY_IMG_FLAG);
    }

    public ColorText getColorText() {
        if (_colorText == null)
            setColorText(new ColorText());
        return _colorText;
    }
    public void setColorText(ColorText colorText) {
        ColorText old = this._colorText;
        if (_notifier.setProperty(old, colorText, PROPERTY_COLOR_TEXT)) {
            if (old != null)
                old.removeListener(this::onColorTextPropertyChanged);
            if (colorText != null)
                colorText.addListener(this::onColorTextPropertyChanged);
        }
    }

    public PenBorder getPenBorder() {
        if (_penBorder == null)
            setPenBorder(new PenBorder());
        return _penBorder;
    }
    public void setPenBorder(PenBorder penBorder) {
        PenBorder old = this._penBorder;
        if (_notifier.setProperty(old, penBorder, PROPERTY_PEN_BORDER)) {
            if (old != null)
                old.removeListener(this::onPenBorderPropertyChanged);
            if (penBorder != null)
                penBorder.addListener(this::onPenBorderPropertyChanged);
        }
    }

    /** всё что относиться к заливке фоном ячееек */
    public static class BackgroundFill implements AutoCloseable, INotifyPropertyChanged {
        /** режим заливки фона ячеек */
        private int _mode = 0;
        /** кэшированные цвета фона ячеек
         * <br/> Нет цвета? - создасться с нужной интенсивностью! */
        private final Map<Integer, Color> _colors = new HashMap<Integer, Color>() {
            private static final long serialVersionUID = 1L;
            @Override
            public Color get(Object key) {
                assert key instanceof Integer;
                Color res = super.get(key);
                if (res == null) {
                    res = Color.RandomColor().brighter(0.45);
                    super.put((Integer)key, res);
                }
                return res;
            }
         };

        public static final String PROPERTY_MODE = "Mode";
        protected NotifyPropertyChanged _notifier = new NotifyPropertyChanged(this);

        /** режим заливки фона ячеек */
        public int getMode() { return _mode; }

        /** режим заливки фона ячеек
         * @param mode
         *  <li> 0 - цвет заливки фона по-умолчанию
         *  <li> not 0 - радуга %)
         */
        public void setMode(int newFillMode) {
            if (_notifier.setProperty(_mode, newFillMode, PROPERTY_MODE))
                _colors.clear();
        }

        /** кэшированные цвета фона ячеек
         * <br/> Нет цвета? - создасться с нужной интенсивностью! */
        public Map<Integer, Color> getColors() {
            return _colors;
        }

        /** off notifier */
        public AutoCloseable hold() {
            return _notifier.hold();
        }

        @Override
        public void close() {
            _notifier.close();
            _colors.clear();
        }

        @Override
        public void addListener(PropertyChangeListener listener) {
            _notifier.addListener(listener);
        }
        @Override
        public void removeListener(PropertyChangeListener listener) {
            _notifier.removeListener(listener);
        }

    }

    public BackgroundFill getBackgroundFill() {
        if (_backgroundFill == null)
            setBackgroundFill(new BackgroundFill());
        return _backgroundFill;
    }
    private void setBackgroundFill(BackgroundFill backgroundFill) {
        BackgroundFill old = this._backgroundFill;
        if (_notifier.setProperty(old, backgroundFill, PROPERTY_BACKGROUND_FILL)) {
            if (old != null)
                old.removeListener(this::onBackgroundFillPropertyChanged);
            if (backgroundFill != null)
                backgroundFill.addListener(this::onBackgroundFillPropertyChanged);
        }
    }

    public FontInfo getFontInfo() {
        if (_fontInfo == null)
            setFontInfo(new FontInfo());
        return _fontInfo;
    }
    public void setFontInfo(FontInfo fontInfo) {
        FontInfo old = this._fontInfo;
        if (_notifier.setProperty(old, fontInfo, PROPERTY_FONT_INFO)) {
            if (old != null)
                old.removeListener(this::onFontInfoPropertyChanged);
            if (fontInfo != null)
                fontInfo.addListener(this::onFontInfoPropertyChanged);
        }
    }

    public Color getBackgroundColor() {
        if (_backgroundColor == null)
            setBackgroundColor(DefaultBkColor);
        return _backgroundColor;
    }

    public void setBackgroundColor(Color color) {
        _notifier.setProperty(_backgroundColor, color, PROPERTY_BACKGROUND_COLOR);
    }

    public TImageInner getImgBckgrnd() { return _imgBckgrnd; }
    public void setImgBckgrnd(TImageInner imgBckgrnd) {
        _notifier.setProperty(this._imgBckgrnd, imgBckgrnd, PROPERTY_IMG_BCKGRND);
    }

    private void onFontInfoPropertyChanged(PropertyChangeEvent ev) {
        _notifier.onPropertyChanged(null, ev.getSource(), PROPERTY_FONT_INFO);
    }
    private void onBackgroundFillPropertyChanged(PropertyChangeEvent ev) {
        _notifier.onPropertyChanged(null, ev.getSource(), PROPERTY_BACKGROUND_FILL);
    }
    private void onColorTextPropertyChanged(PropertyChangeEvent ev) {
        _notifier.onPropertyChanged(null, ev.getSource(), PROPERTY_COLOR_TEXT);
    }
    private void onPenBorderPropertyChanged(PropertyChangeEvent ev) {
        _notifier.onPropertyChanged(null, ev.getSource(), PROPERTY_PEN_BORDER);
    }

    private boolean lockChanging = false;
    @Override
    protected void onPropertyChanged(PropertyChangeEvent ev) {
        super.onPropertyChanged(ev);

        if (lockChanging)
            return;

        lockChanging = true;

        // @see javadoc {@link #autoFit}
        try {
            if (getAutoFit()) {
                // recalc padding
                if (PROPERTY_SIZE.equals(ev.getPropertyName())) {
                    SizeDouble oldSize = (SizeDouble)ev.getOldValue();
                    if (oldSize != null)
                        setPadding(IImageModel.recalcPadding(getPadding(), getSize(), oldSize));
                }

                // recalc area
                switch (ev.getPropertyName()) {
                case PROPERTY_SIZE:
                case PROPERTY_SIZE_FIELD:
                case PROPERTY_MOSAIC_TYPE:
                case PROPERTY_PADDING:
                    setArea(MosaicHelper.findAreaBySize(getMosaicType(), getSizeField(), getInnerSize(), new SizeDouble()));
                    break;
                default:
                    // none
                }

                // recalc size
                if (PROPERTY_AREA.equals(ev.getPropertyName())) {
                    System.err.println("При autoFit==true, Area напрямую не устанавливается!");

                    SizeDouble sm = getMosaicSize();
                    BoundDouble p = getPadding();
                    setSize(new SizeDouble(sm.width + p.getLeftAndRight(), sm.height + p.getTopAndBottom()));
                }
            } else {
                // recalc area / padding
                switch (ev.getPropertyName()) {
                case PROPERTY_SIZE:
                case PROPERTY_SIZE_FIELD:
                case PROPERTY_MOSAIC_TYPE:
                    SizeDouble realInnerSize = new SizeDouble();
                    SizeDouble s = getSize();
                    setArea(MosaicHelper.findAreaBySize(getMosaicType(), getSizeField(), s, realInnerSize));
                    double padX = (s.width  - realInnerSize.width ) / 2;
                    double padY = (s.height - realInnerSize.height) / 2;
                    setPadding(new BoundDouble(padX, padY, padX, padY));
                    break;
                default:
                    // none
                }

                // recalc area
                if (PROPERTY_PADDING.equals(ev.getPropertyName())) {
                    System.err.println("При autoFit==false, Padding напрямую не устанавливается.");
                    setArea(MosaicHelper.findAreaBySize(getMosaicType(), getSizeField(), getInnerSize(), new SizeDouble()));
                }

                // recalc size
                if (PROPERTY_AREA.equals(ev.getPropertyName())) {
                    SizeDouble sm = getMosaicSize();
                    BoundDouble p = getPadding();
                    SizeDouble s = getSize();
                    setPadding(new BoundDouble(p.left, p.top,
                                               s.width  - sm.width  - p.left,
                                               s.height - sm.height - p.top));
                }
            }
        } finally {
            lockChanging = false;
        }
    }

    /** off notifier */
    @Override
    protected AutoCloseable hold() {
        AutoCloseable a0 = super.hold();
        AutoCloseable a1 = getColorText().hold();
        AutoCloseable a2 = getPenBorder().hold();
        AutoCloseable a3 = getFontInfo().hold();
        AutoCloseable a4 = getBackgroundFill().hold();
        return () -> {
            a0.close();
            a1.close();
            a2.close();
            a3.close();
            a4.close();
        };
    }

    @Override
    public void close() {
        _notifier.removeListener(this::onPropertyChanged);
        getBackgroundFill().close();
        super.close();
        // unsubscribe from local notifications
        setFontInfo(null);
        setBackgroundFill(null);
        setColorText(null);
        setPenBorder(null);

        setImgBckgrnd(null);
        setImgFlag(null);
        setImgMine(null);
    }

}
