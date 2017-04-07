package fmg.core.mosaic.draw;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Random;

import fmg.common.Color;
import fmg.common.geom.BoundDouble;
import fmg.common.notyfier.NotifyPropertyChanged;
import fmg.data.view.draw.ColorText;
import fmg.data.view.draw.FontInfo;
import fmg.data.view.draw.PenBorder;

/**
 * Information required for drawing the entire mosaic and cells
 *
 * @param <TImage> plaform specific image
 **/
public class PaintContext<TImage> extends NotifyPropertyChanged implements PropertyChangeListener, AutoCloseable {

   private TImage imgMine, imgFlag;
   private ColorText colorText;
   private PenBorder penBorder;
   private FontInfo  fontInfo;
   private boolean iconicMode;
   private BoundDouble padding = new BoundDouble(0, 0, 0, 0);
   private Color      backgroundColor;
   private boolean useBackgroundColor = true;
   private TImage imgBckgrnd;

   protected static Color _defaultBkColor = Color.Gray;
   /** Цвет заливки ячейки по-умолчанию. Зависит от текущего UI манагера. Переопределяется классом-наследником. */
   protected static Color getDefaultBackgroundColor() {
      return _defaultBkColor;
   }

   public static final String PROPERTY_PADDING               = "Padding";
   public static final String PROPERTY_IMG_MINE              = "ImgMine";
   public static final String PROPERTY_IMG_FLAG              = "ImgFlag";
   public static final String PROPERTY_COLOR_TEXT            = "ColorText";
   public static final String PROPERTY_PEN_BORDER            = "PenBorder";
   public static final String PROPERTY_BACKGROUND_FILL       = "BackgroundFill";
   public static final String PROPERTY_FONT_INFO             = "FontInfo";
   public static final String PROPERTY_BACKGROUND_COLOR      = "BackgroundColor";
   public static final String PROPERTY_USE_BACKGROUND_COLOR  = "UseBackgroundColor";
   public static final String PROPERTY_IMG_BCKGRND           = "ImgBckgrnd";
   public static final String PROPERTY_ICONIC_MODE           = "IconicMode";

   public TImage getImgMine() {
      return imgMine;
   }
   public void setImgMine(TImage img) {
      Object old = this.imgMine;
      if (old != img) { // references compare
         this.imgMine = img;
         onSelfPropertyChanged(old, img, PROPERTY_IMG_MINE);
      }
   }

   public TImage getImgFlag() {
      return imgFlag;
   }
   public void setImgFlag(TImage img) {
      Object old = this.imgFlag;
      if (old != img) { // references compare
         this.imgFlag = img;
         onSelfPropertyChanged(old, img, PROPERTY_IMG_FLAG);
      }
   }

   public ColorText getColorText() {
      if (colorText == null)
         setColorText(new ColorText());
      return colorText;
   }
   public void setColorText(ColorText colorText) {
      ColorText old = this.colorText;
      if (Objects.equals(colorText, old))
         return;

      if (old != null)
         old.removeListener(this);
      this.colorText = colorText;
      if (colorText != null)
         colorText.addListener(this);
      onSelfPropertyChanged(old, colorText, PROPERTY_COLOR_TEXT);
   }

   public PenBorder getPenBorder() {
      if (penBorder == null)
         setPenBorder(new PenBorder());
      return penBorder;
   }
   public void setPenBorder(PenBorder penBorder) {
      PenBorder old = this.penBorder;
      if (Objects.equals(penBorder, old))
         return;

      if (old != null)
         old.removeListener(this);
      this.penBorder = penBorder;
      if (penBorder != null)
         penBorder.addListener(this);
      onSelfPropertyChanged(old, penBorder, PROPERTY_PEN_BORDER);
   }

   /** всё что относиться к заливке фоном ячееек */
   public static class BackgroundFill extends NotifyPropertyChanged {
      /** режим заливки фона ячеек */
      private int mode = 0;
      /** кэшированные цвета фона ячеек */
      private Map<Integer, Color> colors;

      public static final String PROPERTY_MODE = "Mode";

      /** режим заливки фона ячеек */
      public int getMode() {
         return mode;
      }

      /**
      /* режим заливки фона ячеек
       * @param mode
       *  <li> 0 - цвет заливки фона по-умолчанию
       *  <li> not 0 - радуга %)
       */
      public void setMode(int newFillMode) {
         int old = this.mode;
         if (old != newFillMode) {
            this.mode = newFillMode;
            onSelfPropertyChanged(old, newFillMode, PROPERTY_MODE);
            getColors().clear();
         }
      }

      /** кэшированные цвета фона ячеек
      /** <br/> Нет цвета? - создасться с нужной интенсивностью! */
      public Map<Integer, Color> getColors() {
         if (colors == null)
            colors = new HashMap<Integer, Color>() {
               private static final long serialVersionUID = 1L;
               private Random rnd = new Random();

               @Override
               public Color get(Object key) {
                  Color res = super.get(key);
                  if (res == null) {
                     res = Color.RandomColor(rnd).brighter(0.45);
                     super.put((Integer)key, res);
                  }
                  return res;
               }
            };
         return colors;
      }
   }

   private BackgroundFill _backgroundFill;
   public BackgroundFill getBackgroundFill() {
      if (_backgroundFill == null)
         setBackgroundFill(new BackgroundFill());
      return _backgroundFill;
   }
   private void setBackgroundFill(BackgroundFill backgroundFill) {
      BackgroundFill oldBkFill = this._backgroundFill;
      if (oldBkFill == backgroundFill) // ref eq
         return;
      if (oldBkFill != null)
         oldBkFill.removeListener(this);
      this._backgroundFill = backgroundFill;
      if (backgroundFill != null)
         backgroundFill.addListener(this);
      onSelfPropertyChanged(oldBkFill, backgroundFill, PROPERTY_BACKGROUND_FILL);
   }

   public boolean isIconicMode() {
      return iconicMode;
   }
   public void setIconicMode(boolean iconicMode) {
      if (this.iconicMode == iconicMode)
         return;
      this.iconicMode = iconicMode;
      onSelfPropertyChanged(!iconicMode, iconicMode, PROPERTY_ICONIC_MODE);
   }

   public BoundDouble getPadding() {
      return padding;
   }
   public void setPadding(BoundDouble padding) {
      BoundDouble old = this.padding;
      if (!padding.equals(old)) {
         this.padding = padding;
         onSelfPropertyChanged(old, padding, PROPERTY_PADDING);
      }
   }

   public FontInfo getFontInfo() {
      if (fontInfo == null)
         setFontInfo(new FontInfo());
      return fontInfo;
   }
   public void setFontInfo(FontInfo fontInfo) {
      FontInfo oldFont = this.fontInfo;
      if ((oldFont == null) && (fontInfo == null))
         return;
      if ((oldFont != null) && oldFont.equals(fontInfo))
         return;
      if (oldFont != null)
         oldFont.removeListener(this);
      this.fontInfo = fontInfo;
      if (fontInfo != null)
         fontInfo.addListener(this);
      onSelfPropertyChanged(oldFont, fontInfo, PROPERTY_FONT_INFO);
   }

   public Color getBackgroundColor() {
      if (backgroundColor == null)
         setBackgroundColor(getDefaultBackgroundColor());
      return backgroundColor;
   }

   public void setBackgroundColor(Color color) {
      Color old = this.backgroundColor;
      if (color.equals(old))
         return;
      this.backgroundColor = color;
      onSelfPropertyChanged(old, color, PROPERTY_BACKGROUND_COLOR);
   }

   public boolean isUseBackgroundColor() {
      return useBackgroundColor;
   }
   public void setUseBackgroundColor(boolean useBackgroundColor) {
      if (this.useBackgroundColor == useBackgroundColor)
         return;
      this.useBackgroundColor = useBackgroundColor;
      onSelfPropertyChanged(!useBackgroundColor, useBackgroundColor, PROPERTY_BACKGROUND_COLOR);
   }

   public TImage getImgBckgrnd() {
      return imgBckgrnd;
   }

   public void setImgBckgrnd(TImage imgBckgrnd) {
      Object old = this.imgBckgrnd;
      if (old == imgBckgrnd) // references compare
         return;
      this.imgBckgrnd = imgBckgrnd;
      onSelfPropertyChanged(old, imgBckgrnd, PROPERTY_IMG_BCKGRND);
   }

   @Override
   public void propertyChange(PropertyChangeEvent ev) {
      Object source = ev.getSource();
      if (source instanceof FontInfo)
         onSelfPropertyChangedRethrow((FontInfo)ev.getSource(), ev, PROPERTY_FONT_INFO);
      if (source instanceof BackgroundFill)
         onSelfPropertyChangedRethrow((BackgroundFill)ev.getSource(), ev, PROPERTY_BACKGROUND_FILL);
      if (source instanceof ColorText)
         onSelfPropertyChangedRethrow((ColorText)ev.getSource(), ev, PROPERTY_COLOR_TEXT);
      if (source instanceof PenBorder)
         onSelfPropertyChangedRethrow((PenBorder)ev.getSource(), ev, PROPERTY_PEN_BORDER);
   }

   @Override
   public void close() {
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
