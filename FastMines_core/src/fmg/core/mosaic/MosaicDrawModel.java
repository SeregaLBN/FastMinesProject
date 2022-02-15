package fmg.core.mosaic;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import fmg.common.Color;
import fmg.common.geom.BoundDouble;
import fmg.common.geom.SizeDouble;
import fmg.core.img.IImageModel;
import fmg.core.types.Property;
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
    public static Color DefaultBkColor   = Color.LightSlateGray().brighter();
    public static Color DefaultCellColor = Color.LightGray();

    public static final String PROPERTY_AUTO_FIT         = "AutoFit";
    public static final String PROPERTY_IMG_MINE         = "ImgMine";
    public static final String PROPERTY_IMG_FLAG         = "ImgFlag";
    public static final String PROPERTY_IMG_BCKGRND      = "ImgBckgrnd";
    public static final String PROPERTY_COLOR_TEXT       = "ColorText";
    public static final String PROPERTY_CELL_FILL        = "CellFill";
    public static final String PROPERTY_CELL_COLOR       = "CellColor";
    public static final String PROPERTY_BACKGROUND_COLOR = "BackgroundColor";
    public static final String PROPERTY_PEN_BORDER       = "PenBorder";
    public static final String PROPERTY_FONT_INFO        = "FontInfo";

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
     * <li> При изменении Area: при этом Size и Offset не меняются, но при этом меняется Padding.left и Padding.bottom.
     * </ul>
     *
     * <ul>Одинаково
     * <li> При изменении Offset меняется Padding так, чтобы InnerSize остался прежним
     * <li> При изменении Padding перерасчитывается Area, так что бы мозаика вписывалась внутрь нового InnerSize.
     * </ul>
     **/
    @Property(PROPERTY_AUTO_FIT)
    private boolean autoFit = true;

    @Property(PROPERTY_SIZE)
    private SizeDouble size;

    @Property(PROPERTY_PADDING)
    private BoundDouble padding = new BoundDouble(0);

    @Property(PROPERTY_IMG_MINE)
    private TImageInner imgMine;

    @Property(PROPERTY_IMG_FLAG)
    private TImageInner imgFlag;

    @Property(PROPERTY_IMG_BCKGRND)
    private TImageInner imgBckgrnd;

    @Property(PROPERTY_COLOR_TEXT)
    private ColorText colorText;

    @Property(PROPERTY_PEN_BORDER)
    private PenBorder penBorder;

    @Property(PROPERTY_FONT_INFO)
    private FontInfo fontInfo;

    @Property(PROPERTY_CELL_FILL)
    private CellFill cellFill;

    @Property(PROPERTY_CELL_COLOR)
    private Color cellColor;

    @Property(PROPERTY_BACKGROUND_COLOR)
    private Color backgroundColor;

    private boolean lockChanging = false;

    private final PropertyChangeListener onColorTextPropertyChangedListener = this::onColorTextPropertyChanged;
    private final PropertyChangeListener onPenBorderPropertyChangedListener = this::onPenBorderPropertyChanged;
    private final PropertyChangeListener  onCellFillPropertyChangedListener = this::onCellFillPropertyChanged;
    private final PropertyChangeListener  onFontInfoPropertyChangedListener = this::onFontInfoPropertyChanged;

    @Override
    public boolean getAutoFit() { return autoFit; }
    @Override
    public void setAutoFit(boolean autoFit) {
        notifier.setProperty(this.autoFit, autoFit, PROPERTY_AUTO_FIT);
    }

    /** get mosaic size in pixels */
    @Override
    public SizeDouble getMosaicSize() {
        return getShape().getSize(getSizeField());
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
        if ((size == null) ||
            (size.width <= 0) ||
            (size.height <= 0))
        {
            SizeDouble s = getMosaicSize();
            BoundDouble p = getPadding();
            s.width  += p.getLeftAndRight();
            s.height += p.getTopAndBottom();
            setSize(s);
        }
        return size;
    }
    @Override
    public void setSize(SizeDouble size) {
        IImageModel.checkSize(size);
        notifier.setProperty(this.size, size, PROPERTY_SIZE);
    }

    @Override
    public BoundDouble getPadding() { return padding; }
    @Override
    public void setPadding(BoundDouble padding) {
        IImageModel.checkPadding(this, padding);
        notifier.setProperty(this.padding, new BoundDouble(padding), PROPERTY_PADDING);
    }

    /** Offset to mosaic.
     * Определяется Padding'ом  и, дополнительно, смещением к мозаике (т.к. мозаика равномерно вписана в InnerSize) */
    @Override
    public SizeDouble getMosaicOffset() {
        BoundDouble pad = getPadding();
        SizeDouble padLT = new SizeDouble(pad.left, pad.top);
        SizeDouble mosaicSize = getMosaicSize();
        SizeDouble innerSize  = getInnerSize();
        if (mosaicSize.equals(innerSize))
            return padLT;
        double dx = innerSize.width  - mosaicSize.width;
        double dy = innerSize.height - mosaicSize.height;
        return new SizeDouble(padLT.width + dx / 2, padLT.height + dy / 2);
    }

    /** set offset to mosaic */
    @Override
    public void setMosaicOffset(SizeDouble offset) {
        BoundDouble pad = getPadding();
        SizeDouble oldOffset = getMosaicOffset();
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
    public TImageInner getImgMine() { return imgMine; }
    public void setImgMine(TImageInner img) {
        notifier.setProperty(this.imgMine, img, PROPERTY_IMG_MINE);
    }

    @Override
    public TImageInner getImgFlag() { return imgFlag; }
    public void setImgFlag(TImageInner img) {
        notifier.setProperty(this.imgFlag, img, PROPERTY_IMG_FLAG);
    }

    @Override
    public ColorText getColorText() {
        if (colorText == null)
            setColorText(new ColorText());
        return colorText;
    }
    @Override
    public void setColorText(ColorText colorText) {
        ColorText old = this.colorText;
        if (notifier.setProperty(old, colorText, PROPERTY_COLOR_TEXT)) {
            if (old != null)
                old.removeListener(onColorTextPropertyChangedListener);
            if (colorText != null)
                colorText.addListener(onColorTextPropertyChangedListener);
        }
    }

    @Override
    public PenBorder getPenBorder() {
        if (penBorder == null)
            setPenBorder(new PenBorder());
        return penBorder;
    }
    @Override
    public void setPenBorder(PenBorder penBorder) {
        PenBorder old = this.penBorder;
        if (notifier.setProperty(old, penBorder, PROPERTY_PEN_BORDER)) {
            if (old != null)
                old.removeListener(onPenBorderPropertyChangedListener);
            if (penBorder != null)
                penBorder.addListener(onPenBorderPropertyChangedListener);
        }
    }

    @Override
    public CellFill getCellFill() {
        if (cellFill == null)
            setCellFill(new CellFill());
        return cellFill;
    }
    @Override
    public void setCellFill(CellFill cellFill) {
        CellFill old = this.cellFill;
        if (notifier.setProperty(old, cellFill, PROPERTY_CELL_FILL)) {
            if (old != null) {
                old.removeListener(onCellFillPropertyChangedListener);
                old.close();
            }
            if (cellFill != null)
                cellFill.addListener(onCellFillPropertyChangedListener);
        }
    }

    @Override
    public Color getCellColor() {
        if (cellColor == null)
            setCellColor(DefaultCellColor);
        return cellColor;
    }

    @Override
    public void setCellColor(Color color) {
        notifier.setProperty(cellColor, color, PROPERTY_CELL_COLOR);
    }

    @Override
    public Color getBackgroundColor() {
        if (backgroundColor == null)
            setBackgroundColor(DefaultBkColor);
        return backgroundColor;
    }

    @Override
    public void setBackgroundColor(Color color) {
        notifier.setProperty(backgroundColor, color, PROPERTY_BACKGROUND_COLOR);
    }

    @Override
    public FontInfo getFontInfo() {
        if (fontInfo == null)
            setFontInfo(new FontInfo());
        return fontInfo;
    }

    @Override
    public void setFontInfo(FontInfo fontInfo) {
        FontInfo old = this.fontInfo;
        if (notifier.setProperty(old, fontInfo, PROPERTY_FONT_INFO)) {
            if (old != null)
                old.removeListener(onFontInfoPropertyChangedListener);
            if (fontInfo != null)
                fontInfo.addListener(onFontInfoPropertyChangedListener);
        }
    }

    @Override
    public TImageInner getImgBckgrnd() { return imgBckgrnd; }
    public void setImgBckgrnd(TImageInner imgBckgrnd) {
        notifier.setProperty(this.imgBckgrnd, imgBckgrnd, PROPERTY_IMG_BCKGRND);
    }

    private void onFontInfoPropertyChanged(PropertyChangeEvent ev) {
        notifier.firePropertyChanged(null, ev.getSource(), PROPERTY_FONT_INFO);
    }
    private void onCellFillPropertyChanged(PropertyChangeEvent ev) {
        notifier.firePropertyChanged(null, ev.getSource(), PROPERTY_CELL_FILL);
    }
    private void onColorTextPropertyChanged(PropertyChangeEvent ev) {
        notifier.firePropertyChanged(null, ev.getSource(), PROPERTY_COLOR_TEXT);
    }
    private void onPenBorderPropertyChanged(PropertyChangeEvent ev) {
        notifier.firePropertyChanged(null, ev.getSource(), PROPERTY_PEN_BORDER);
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
                getFontInfo().setSize(getShape().getSq(penBorder.getWidth()));
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
        // unsubscribe from local notifications
        setFontInfo(null);
        setCellFill(null);
        setColorText(null);
        setPenBorder(null);

        setImgBckgrnd(null);
        setImgFlag(null);
        setImgMine(null);
    }

}
