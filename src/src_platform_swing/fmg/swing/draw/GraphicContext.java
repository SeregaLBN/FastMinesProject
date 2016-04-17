package fmg.swing.draw;

import java.awt.Font;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import javax.swing.Icon;
import javax.swing.JComponent;
import javax.swing.UIDefaults;
import javax.swing.UIManager;

import fmg.common.Color;
import fmg.common.geom.BoundDouble;
import fmg.common.notyfier.NotifyPropertyChanged;
import fmg.data.view.draw.ColorText;
import fmg.data.view.draw.PenBorder;
import fmg.swing.Cast;

public class GraphicContext extends NotifyPropertyChanged implements PropertyChangeListener {
   public static final Font DEFAULT_FONT = new Font("SansSerif", Font.PLAIN, 10);

   /** TODO: Mosaic field - нуна избавиться... */
   private JComponent owner;

   private Icon imgMine, imgFlag;
   private ColorText colorText;
   private PenBorder penBorder;
   private Font        font;
   private final boolean iconicMode;
   private BoundDouble padding;

   public GraphicContext(JComponent owner, boolean iconicMode) {
      this.owner = owner;
      this.iconicMode = iconicMode;
      this.padding = new BoundDouble(0, 0, 0, 0);
   }

   public Icon getImgMine() {
      return imgMine;
   }
   public void setImgMine(Icon img) {
      Object old = this.imgMine;
      if (old != img) { // references compare
         this.imgMine = img;
         onPropertyChanged(old, img, "ImgMine");
      }
   }

   public Icon getImgFlag() {
      return imgFlag;
   }
   public void setImgFlag(Icon img) {
      Object old = this.imgFlag;
      if (old != img) { // references compare
         this.imgFlag = img;
         onPropertyChanged(old, img, "ImgFlag");
      }
   }

   public ColorText getColorText() {
      if (colorText == null)
         setColorText(new ColorText());
      return colorText;
   }
   public void setColorText(ColorText colorText) {
      ColorText old = this.colorText;
      if (colorText.equals(old))
         return;

      if (old != null)
         old.removePropertyChangeListener(this);
      colorText.addListener(this);

      this.colorText = colorText;
      onPropertyChanged(old, colorText, "ColorText");
   }

   public PenBorder getPenBorder() {
      if (penBorder == null)
         setPenBorder(new PenBorder());
      return penBorder;
   }
   public void setPenBorder(PenBorder penBorder) {
      PenBorder old = this.penBorder;
      if (penBorder.equals(old))
         return;

      if (old != null)
         old.removePropertyChangeListener(this);
      penBorder.addListener(this);

      this.penBorder = penBorder;
      onPropertyChanged(old, penBorder, "PenBorder");
   }

   public JComponent getOwner() {
      return owner;
   }

   /** всё что относиться к заливке фоном ячееек */
   public static class BackgroundFill extends NotifyPropertyChanged {
      /** режим заливки фона ячеек */
      private int mode = 0;
      /** кэшированные цвета фона ячеек */
      private Map<Integer, Color> colors;

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
            onPropertyChanged(old, newFillMode, "Mode");
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
                     res = Color.RandomColor(rnd).attenuate();
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
      if (_backgroundFill == null) {
         _backgroundFill = new BackgroundFill();
         _backgroundFill.addListener(this);
         onPropertyChanged("BackgroundFill");
      }
      return _backgroundFill;
   }

   public boolean isIconicMode() {
      return iconicMode;
   }

   public BoundDouble getPadding() {
      return padding;
   }

   public void setPadding(BoundDouble padding) {
      BoundDouble old = this.padding;
      if (!padding.equals(old)) {
         this.padding = padding;
         onPropertyChanged(old, padding, "Padding");
      }
   }

   public Font getFont() {
      if (font == null)
         setFont(DEFAULT_FONT);
      return font;
   }
   private void setRawFont(Font font) {
      Object old = this.font;
      if (old != font) { // references compare
         this.font = font;
         onPropertyChanged(old, font, "Font");
      }
   }
   public void setFont(Font newFont) {
      if (font != null) {
         if (font.getName().equals(newFont.getName()) &&
            (font.getStyle() == newFont.getStyle()) &&
            (font.getSize() == newFont.getSize()))
            return;

         int heightNeed = font.getSize();
         int heightBad = newFont.getSize();
         if (heightNeed != heightBad)
            newFont = new Font(newFont.getName(), newFont.getStyle(), heightNeed);
      }
      setRawFont(newFont);
   }
   public void setFontSize(int size) {
//      size = 9; // debug
      Font fnt = getFont();
      if (fnt.getSize() == size)
         return;
      setRawFont(new Font(fnt.getName(), fnt.getStyle(), size));
   }

   private static fmg.common.Color _defaultBkColor;
   /** Цвет заливки ячейки по-умолчанию. Зависит от текущего UI манагера */
   public static fmg.common.Color getDefaultBackgroundFillColor() {
      return _defaultBkColor;
   }

   static {
      UIDefaults uiDef = UIManager.getDefaults();
      java.awt.Color clr = uiDef.getColor("Panel.background");
      if (clr == null)
         clr = java.awt.Color.GRAY;
      _defaultBkColor = Cast.toColor(clr);
      // ToggleButton.darkShadow : javax.swing.plaf.ColorUIResource[r=105,g=105,b=105]
      // ToggleButton.background : javax.swing.plaf.ColorUIResource[r=240,g=240,b=240]
      // ToggleButton.focus      : javax.swing.plaf.ColorUIResource[r=0,g=0,b=0]
      // ToggleButton.highlight  : javax.swing.plaf.ColorUIResource[r=255,g=255,b=255]
      // ToggleButton.light      : javax.swing.plaf.ColorUIResource[r=227,g=227,b=227]
      // ToggleButton.shadow     : javax.swing.plaf.ColorUIResource[r=160,g=160,b=160]
      // ToggleButton.foreground : javax.swing.plaf.ColorUIResource[r=0,g=0,b=0]
   }

   @Override
   public void propertyChange(PropertyChangeEvent ev) {
      //super.propertyChange(ev);
      Object source = ev.getSource();
      if (source instanceof BackgroundFill)
         onBackgroundFillPropertyChanged((BackgroundFill)ev.getSource(), ev);
      if (source instanceof ColorText)
         onColorTextPropertyChanged((ColorText)ev.getSource(), ev);
      if (source instanceof PenBorder)
         onPenBorderPropertyChanged((PenBorder)ev.getSource(), ev);
   }
   private void onBackgroundFillPropertyChanged(BackgroundFill source, PropertyChangeEvent ev) {
      onPropertyChanged("BackgroundFill");
      onPropertyChanged("BackgroundFill." + ev.getPropertyName());
   }
   private void onColorTextPropertyChanged(ColorText source, PropertyChangeEvent ev) {
      onPropertyChanged("ColorText");
      onPropertyChanged("ColorText." + ev.getPropertyName());
   }
   private void onPenBorderPropertyChanged(PenBorder source, PropertyChangeEvent ev) {
      onPropertyChanged("PenBorder");
      onPropertyChanged("PenBorder." + ev.getPropertyName());
   }

}