package fmg.core.mosaic;

import java.beans.PropertyChangeEvent;

import fmg.common.Color;
import fmg.common.geom.BoundDouble;
import fmg.common.geom.SizeDouble;
import fmg.core.img.IImageModel;
import fmg.core.types.draw.ColorText;
import fmg.core.types.draw.FontInfo;
import fmg.core.types.draw.PenBorder;

/**
 * MVC: draw model of mosaic field.
 *
 * @param <TImageInner> platform specific view/image/picture or other display context/canvas/window/panel
 **/
public class MosaicDrawModel<TImageInner> extends MosaicGameModel implements IMosaicDrawModel<TImageInner> {

    /** Цвет заливки ячейки по-умолчанию. Зависит от текущего UI манагера. Переопределяется одним из MVC-наследником. */
    public static Color DefaultBkColor = Color.Gray().brighter();

    /** Fit the area/padding to the size.
     *
     * <ul>При autoFit = true
     * <li> При изменении Size, Padding меняется пропорционально Size
     * <li> При изменении Size / MosaicType / SizeField / Padding,
     *      Мозаика равномерно вписывается во внутреннюю область {@link #getInnerSize()}
     * <li> При изменении Area, нужно что бы {@link #getMosaicSize()} + {@link #getPadding()} будут определять новый {@link #getSize()}
     * </ul>
     *
     * <ul>При autoFit = false
     * <li> <ol>При изменении Size / MosaicType / SizeField:
     *      <li> Мозаика равномерно вписывается во вcю область {@link #getSize()}
     *      <li> при этом Padding заного перерасчитывается с нуля
     *      </ol>
     * <li> При изменении Offset меняется Padding так, чтобы InnerSize остался прежним
     * <li> При изменении Padding перерасчитывается Area, так что бы мозаика вписывалась внутрь нового InnerSize.
     * <li> При изменении Area: при этом Size и Offset не меняются, но при этом меняется Padding.left и Padding.bottom.
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
    private boolean lockChanging = false;

    public static final String PROPERTY_AUTO_FIT         = "AutoFit";
    public static final String PROPERTY_IMG_MINE         = "ImgMine";
    public static final String PROPERTY_IMG_FLAG         = "ImgFlag";
    public static final String PROPERTY_IMG_BCKGRND      = "ImgBckgrnd";
    public static final String PROPERTY_COLOR_TEXT       = "ColorText";
    public static final String PROPERTY_PEN_BORDER       = "PenBorder";
    public static final String PROPERTY_BACKGROUND_FILL  = "BackgroundFill";
    public static final String PROPERTY_FONT_INFO        = "FontInfo";
    public static final String PROPERTY_BACKGROUND_COLOR = "BackgroundColor";

    @Override
    public boolean getAutoFit() { return _autoFit; }
    @Override
    public void setAutoFit(boolean autoFit) {
        _notifier.setProperty(this._autoFit, autoFit, PROPERTY_AUTO_FIT);
    }

    /** get mosaic size in pixels */
    @Override
    public SizeDouble getMosaicSize() {
        return getCellAttr().getSize(getSizeField());
    }
    /** get inner size in pixels, куда равномерно вписана мозаика. Inner, т.к. снаружи есть ещё padding */
    private SizeDouble getInnerSize() {
        BoundDouble pad = getPadding();
        SizeDouble s = getSize();
        return new SizeDouble(s.width - pad.getLeftAndRight(), s.height - pad.getTopAndBottom());
    }
    /** common size in pixels */
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
    @Override
    public SizeDouble getMosaicOffset() {
        BoundDouble pad = getPadding();
        SizeDouble offset     = new SizeDouble(pad.left, pad.top);
        SizeDouble mosaicSize = getMosaicSize();
        SizeDouble innerSize  = getInnerSize();
        if (mosaicSize.equals(innerSize))
            return offset;
        double dx = innerSize.width  - mosaicSize.width;
        double dy = innerSize.height - mosaicSize.height;
        return new SizeDouble(offset.width + dx / 2, offset.height + dy / 2);
    }

    /** set offset to mosaic */
    @Override
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

        boolean locked = lockChanging;
        try {
            lockChanging = true;
            setPadding(padNew);
        } finally {
            if (!locked)
                lockChanging = false;
        }
    }

    @Override
    public TImageInner getImgMine() { return _imgMine; }
    public void setImgMine(TImageInner img) {
        _notifier.setProperty(this._imgMine, img, PROPERTY_IMG_MINE);
    }

    @Override
    public TImageInner getImgFlag() { return _imgFlag; }
    public void setImgFlag(TImageInner img) {
        _notifier.setProperty(this._imgFlag, img, PROPERTY_IMG_FLAG);
    }

    @Override
    public ColorText getColorText() {
        if (_colorText == null)
            setColorText(new ColorText());
        return _colorText;
    }
    @Override
    public void setColorText(ColorText colorText) {
        ColorText old = this._colorText;
        if (_notifier.setProperty(old, colorText, PROPERTY_COLOR_TEXT)) {
            if (old != null)
                old.removeListener(this::onColorTextPropertyChanged);
            if (colorText != null)
                colorText.addListener(this::onColorTextPropertyChanged);
        }
    }

    @Override
    public PenBorder getPenBorder() {
        if (_penBorder == null)
            setPenBorder(new PenBorder());
        return _penBorder;
    }
    @Override
    public void setPenBorder(PenBorder penBorder) {
        PenBorder old = this._penBorder;
        if (_notifier.setProperty(old, penBorder, PROPERTY_PEN_BORDER)) {
            if (old != null)
                old.removeListener(this::onPenBorderPropertyChanged);
            if (penBorder != null)
                penBorder.addListener(this::onPenBorderPropertyChanged);
        }
    }

    @Override
    public BackgroundFill getBackgroundFill() {
        if (_backgroundFill == null)
            setBackgroundFill(new BackgroundFill());
        return _backgroundFill;
    }
    @Override
    public void setBackgroundFill(BackgroundFill backgroundFill) {
        BackgroundFill old = this._backgroundFill;
        if (_notifier.setProperty(old, backgroundFill, PROPERTY_BACKGROUND_FILL)) {
            if (old != null)
                old.removeListener(this::onBackgroundFillPropertyChanged);
            if (backgroundFill != null)
                backgroundFill.addListener(this::onBackgroundFillPropertyChanged);
        }
    }

    @Override
    public FontInfo getFontInfo() {
        if (_fontInfo == null)
            setFontInfo(new FontInfo());
        return _fontInfo;
    }
    @Override
    public void setFontInfo(FontInfo fontInfo) {
        FontInfo old = this._fontInfo;
        if (_notifier.setProperty(old, fontInfo, PROPERTY_FONT_INFO)) {
            if (old != null)
                old.removeListener(this::onFontInfoPropertyChanged);
            if (fontInfo != null)
                fontInfo.addListener(this::onFontInfoPropertyChanged);
        }
    }

    @Override
    public Color getBackgroundColor() {
        if (_backgroundColor == null)
            setBackgroundColor(DefaultBkColor);
        return _backgroundColor;
    }

    @Override
    public void setBackgroundColor(Color color) {
        _notifier.setProperty(_backgroundColor, color, PROPERTY_BACKGROUND_COLOR);
    }

    @Override
    public TImageInner getImgBckgrnd() { return _imgBckgrnd; }
    public void setImgBckgrnd(TImageInner imgBckgrnd) {
        _notifier.setProperty(this._imgBckgrnd, imgBckgrnd, PROPERTY_IMG_BCKGRND);
    }

    private void onFontInfoPropertyChanged(PropertyChangeEvent ev) {
        _notifier.firePropertyChanged(null, ev.getSource(), PROPERTY_FONT_INFO);
    }
    private void onBackgroundFillPropertyChanged(PropertyChangeEvent ev) {
        _notifier.firePropertyChanged(null, ev.getSource(), PROPERTY_BACKGROUND_FILL);
    }
    private void onColorTextPropertyChanged(PropertyChangeEvent ev) {
        _notifier.firePropertyChanged(null, ev.getSource(), PROPERTY_COLOR_TEXT);
    }
    private void onPenBorderPropertyChanged(PropertyChangeEvent ev) {
        _notifier.firePropertyChanged(null, ev.getSource(), PROPERTY_PEN_BORDER);
    }

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
                    SizeDouble ms = getMosaicSize();
                    BoundDouble p = getPadding();
                    if (((ms.width  + p.getLeftAndRight()) <= 0) ||
                        ((ms.height + p.getTopAndBottom()) <= 0))
                    {
                        // reset padding
                        p = new BoundDouble(0);
                        setPadding(p);
                    }
                    setSize(new SizeDouble(ms.width + p.getLeftAndRight(), ms.height + p.getTopAndBottom()));
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
                if (PROPERTY_PADDING.equals(ev.getPropertyName()))
                    setArea(MosaicHelper.findAreaBySize(getMosaicType(), getSizeField(), getInnerSize(), new SizeDouble()));

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

            // change font size (пересчитать и установить новую высоту шрифта)
            switch (ev.getPropertyName()) {
            case MosaicGameModel.PROPERTY_MOSAIC_TYPE:
            case MosaicGameModel.PROPERTY_AREA:
            case MosaicDrawModel.PROPERTY_SIZE:
            case MosaicDrawModel.PROPERTY_PEN_BORDER:
                PenBorder penBorder = getPenBorder();
                getFontInfo().setSize(getCellAttr().getSq(penBorder.getWidth()));
                break;
            default:
                // none
            }
        } finally {
            lockChanging = false;
        }
    }

    @Override
    public void close() {
        super.close();
        if (_backgroundFill != null)
            getBackgroundFill().close();
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
