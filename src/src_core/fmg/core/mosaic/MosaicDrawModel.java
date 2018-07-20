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
 * @param <TImage> plaform specific view/image/picture or other display context/canvas/window/panel
 **/
public class MosaicDrawModel<TImage> extends MosaicGameModel implements IImageModel {

   /** Цвет заливки ячейки по-умолчанию. Зависит от текущего UI манагера. Переопределяется одним из MVC-наследником. */
   public static Color DefaultBkColor = Color.Gray().brighter();

   private TImage         _imgMine, _imgFlag;
   private ColorText      _colorText;
   private PenBorder      _penBorder;
   private FontInfo       _fontInfo;
   /** автоматически регулирую при явной установке размера */
   private BoundDouble    _margin  = new BoundDouble(0, 0, 0, 0);
   private BoundDouble    _padding = new BoundDouble(0, 0, 0, 0);
   private BackgroundFill _backgroundFill;
   private Color          _backgroundColor;
   private TImage         _imgBckgrnd;

   private final PropertyChangeListener           _selfListener = ev ->               onPropertyChanged(ev.getOldValue(), ev.getNewValue(), ev.getPropertyName());
   private final PropertyChangeListener       _fontInfoListener = ev ->       onFontInfoPropertyChanged(ev);
   private final PropertyChangeListener _backgroundFillListener = ev -> onBackgroundFillPropertyChanged(ev);
   private final PropertyChangeListener      _colorTextListener = ev ->      onColorTextPropertyChanged(ev);
   private final PropertyChangeListener      _penBorderListener = ev ->      onPenBorderPropertyChanged(ev);

   public MosaicDrawModel() {
      _notifier.addListener(_selfListener);
   }

   public static final String PROPERTY_SIZE             = "Size";
   public static final String PROPERTY_MARGIN           = "Margin";
   public static final String PROPERTY_PADDING          = "Padding";
   public static final String PROPERTY_IMG_MINE         = "ImgMine";
   public static final String PROPERTY_IMG_FLAG         = "ImgFlag";
   public static final String PROPERTY_COLOR_TEXT       = "ColorText";
   public static final String PROPERTY_PEN_BORDER       = "PenBorder";
   public static final String PROPERTY_BACKGROUND_FILL  = "BackgroundFill";
   public static final String PROPERTY_FONT_INFO        = "FontInfo";
   public static final String PROPERTY_BACKGROUND_COLOR = "BackgroundColor";
   public static final String PROPERTY_IMG_BCKGRND      = "ImgBckgrnd";

   /** размер в пикселях поля мозаики. Inner, т.к. снаружи есть ещё padding и margin */
   public SizeDouble getInnerSize() {
      return getCellAttr().getSize(getSizeField());
   }
   /** общий размер в пискелях */
   @Override
   public SizeDouble getSize() {
      SizeDouble size = getInnerSize();
      BoundDouble m = getMargin();
      BoundDouble p = getPadding();
      size.width  += m.getLeftAndRight() + p.getLeftAndRight();
      size.height += m.getTopAndBottom() + p.getTopAndBottom();
      return size;
   }
   @Override
   public void setSize(SizeDouble size) {
      if (size.width < 1)
         throw new IllegalArgumentException("Size value widht must be > 1");
      if (size.height < 1)
         throw new IllegalArgumentException("Size value height must be > 1");

      SizeDouble oldSize = getSize();
      BoundDouble oldPadding = getPadding();
      BoundDouble newPadding = new BoundDouble(oldPadding.left   * size.width  / oldSize.width,
                                               oldPadding.top    * size.height / oldSize.height,
                                               oldPadding.right  * size.width  / oldSize.width,
                                               oldPadding.bottom * size.height / oldSize.height);
      SizeDouble toCalc = new SizeDouble(size.width  - newPadding.getLeftAndRight(),
                                         size.height - newPadding.getTopAndBottom());
      SizeDouble out = new SizeDouble();
      double area = MosaicHelper.findAreaBySize(getMosaicType(), getSizeField(), toCalc, out);
      BoundDouble margin = new BoundDouble(0);
      margin.left = margin.right  = (size.width  - newPadding.getLeftAndRight() - out.width ) / 2;
      margin.top  = margin.bottom = (size.height - newPadding.getTopAndBottom() - out.height) / 2;

      setArea(area);
      setMargin(margin);
      setPaddingInternal(newPadding);
   }

   public TImage getImgMine() {
      return _imgMine;
   }
   public void setImgMine(TImage img) {
      Object old = this._imgMine;
      if (old != img) { // references compare
         this._imgMine = img;
         onPropertyChanged(old, img, PROPERTY_IMG_MINE);
      }
   }

   public TImage getImgFlag() {
      return _imgFlag;
   }
   public void setImgFlag(TImage img) {
      Object old = this._imgFlag;
      if (old != img) { // references compare
         this._imgFlag = img;
         onPropertyChanged(old, img, PROPERTY_IMG_FLAG);
      }
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
            old.removeListener(_colorTextListener);
         if (colorText != null)
            colorText.addListener(_colorTextListener);
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
            old.removeListener(_penBorderListener);
         if (penBorder != null)
            penBorder.addListener(_penBorderListener);
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
            old.removeListener(_backgroundFillListener);
         if (backgroundFill != null)
            backgroundFill.addListener(_backgroundFillListener);
      }
   }

   public BoundDouble getMargin() {
      return _margin;
   }
   /** is only set when resizing. */
   private void setMargin(BoundDouble margin) {
      if (margin.left < 0)
         throw new IllegalArgumentException("Margin left value must be > 0");
      if (margin.top < 0)
         throw new IllegalArgumentException("Margin top value must be > 0");
      if (margin.right < 0)
         throw new IllegalArgumentException("Margin right value must be > 0");
      if (margin.bottom < 0)
         throw new IllegalArgumentException("Margin bottom value must be > 0");

      _notifier.setProperty(_margin, margin, PROPERTY_MARGIN);
   }

   public BoundDouble getPadding() {
      return _padding;
   }
   public void setPadding(double bound) { setPadding(new BoundDouble(bound)); }
   public void setPadding(BoundDouble padding) {
      if (padding.left < 0)
         throw new IllegalArgumentException("Padding left value must be > 0");
      if (padding.top < 0)
         throw new IllegalArgumentException("Padding top value must be > 0");
      if (padding.right < 0)
         throw new IllegalArgumentException("Padding right value must be > 0");
      if (padding.bottom < 0)
         throw new IllegalArgumentException("Padding bottom value must be > 0");

      SizeDouble size = getSize();
      if ((size.width - padding.getLeftAndRight()) < 1)
         throw new IllegalArgumentException("The left and right padding are very large");
      if ((size.height - padding.getTopAndBottom()) < 1)
         throw new IllegalArgumentException("The top and bottom padding are very large");

      SizeDouble toCalc = new SizeDouble(size.width  - padding.getLeftAndRight(),
                                         size.height - padding.getTopAndBottom());
      SizeDouble out = new SizeDouble();
      double area = MosaicHelper.findAreaBySize(getMosaicType(), getSizeField(), toCalc, out);
      BoundDouble margin = new BoundDouble(0);
      margin.left = margin.right  = (size.width  - padding.getLeftAndRight() - out.width ) / 2;
      margin.top  = margin.bottom = (size.height - padding.getTopAndBottom() - out.height) / 2;

      setArea(area);
      setMargin(margin);
      setPaddingInternal(padding);
   }
   private void setPaddingInternal(BoundDouble padding) {
      //String stack = Stream.of(new Exception().getStackTrace())
      //      .skip(1)
      //      .map(st -> st.getLineNumber()
      //                 + " " + Stream.of(st.getClassName().split("\\.")).reduce((first, second) -> second).get()
      //                 + "." + st.getMethodName())
      //      .limit(3)
      //      .collect(Collectors.joining("\n\t "));
      //System.out.println("setPaddingInternal(" + padding + ") call from: " + stack);
      _notifier.setProperty(_padding, padding, PROPERTY_PADDING);
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
            old.removeListener(_fontInfoListener);
         if (fontInfo != null)
            fontInfo.addListener(_fontInfoListener);
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

   public TImage getImgBckgrnd() {
      return _imgBckgrnd;
   }

   public void setImgBckgrnd(TImage imgBckgrnd) {
      Object old = this._imgBckgrnd;
      if (old == imgBckgrnd) // references compare
         return;
      this._imgBckgrnd = imgBckgrnd;
      onPropertyChanged(old, imgBckgrnd, PROPERTY_IMG_BCKGRND);
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

   protected void onPropertyChanged(Object oldValue, Object newValue, String propertyName) {
      switch (propertyName) {
      case PROPERTY_AREA:
      case PROPERTY_SIZE_FIELD:
      case PROPERTY_MOSAIC_TYPE:
      case PROPERTY_PADDING:
      case PROPERTY_MARGIN:
         _notifier.onPropertyChanged(PROPERTY_SIZE);
         break;
      }
   }

   @Override
   public void close() {
      _notifier.removeListener(_selfListener);
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
