package fmg.swing.draw;

import java.awt.Font;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.UIDefaults;
import javax.swing.UIManager;

import fmg.common.Color;
import fmg.common.geom.BoundDouble;
import fmg.data.view.draw.ColorText;
import fmg.data.view.draw.PenBorder;
import fmg.swing.Cast;

public class GraphicContext  {
   public static final Font DEFAULT_FONT = new Font("SansSerif", Font.PLAIN, 10);

   protected PropertyChangeSupport propertyChanges = new PropertyChangeSupport(this);
   /**  подписаться на уведомления изменений свойств GraphicContext */
   public void addPropertyChangeListener(PropertyChangeListener l) {
      propertyChanges.addPropertyChangeListener(l);
   }
   /**  отписаться от уведомлений изменений свойств GraphicContext */
   public void removePropertyChangeListener(PropertyChangeListener l) {
      propertyChanges.removePropertyChangeListener(l);
   }

   /** TODO: Mosaic field - нуна избавиться... */
   private JComponent owner;

   private ImageIcon imgMine, imgFlag;
   private ColorText colorText;
   protected PenBorder penBorder;
   private Font        font;
   private final boolean iconicMode;
   private BoundDouble padding;

   public GraphicContext(JComponent owner, boolean iconicMode) {
      this.owner = owner;
      this.iconicMode = iconicMode;
      this.padding = new BoundDouble(0, 0, 0, 0);
   }

   public ImageIcon getImgMine() {
      return imgMine;
   }
   public void setImgMine(ImageIcon img) {
      Object old = this.imgMine;
      this.imgMine = img;
      propertyChanges.firePropertyChange("GraphicContext_imgMine", old, img);
   }
   public ImageIcon getImgFlag() {
      return imgFlag;
   }
   public void setImgFlag(ImageIcon img) {
      Object old = this.imgFlag;
      this.imgFlag = img;
      propertyChanges.firePropertyChange("GraphicContext_imgFlag", old, img);
   }

   public ColorText getColorText() {
      if (colorText == null)
         setColorText(new ColorText());
      return colorText;
   }
   public void setColorText(ColorText colorText) {
      ColorText old = this.colorText;
      this.colorText = colorText;
      propertyChanges.firePropertyChange("GraphicContext_colorText", old, colorText);
   }

   public PenBorder getPenBorder() {
      if (penBorder == null)
         setPenBorder(new PenBorder());
      return penBorder;
   }
   public void setPenBorder(PenBorder penBorder) {
      PenBorder old = this.penBorder;
      this.penBorder = penBorder;
      propertyChanges.firePropertyChange("GraphicContext_penBorder", old, penBorder);
   }

   public JComponent getOwner() {
      return owner;
   }

   /** всё что относиться к заливке фоном ячееек */
   public class BackgroundFill {
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
         this.mode = newFillMode;
         getColors().clear();
      }

      /** кэшированные цвета фона ячеек
      /** <br/> Нет цвета? - создасться с нужной интенсивностью! */
      public Map<Integer, Color> getColors() {
         if (colors == null)
            colors = new HashMap<Integer, Color>() {
               private static final long serialVersionUID = 1L;

               @Override
               public Color get(Object key) {
                  Color res = super.get(key);
                  if (res == null) {
                     res = Color.RandomColor(new Random()).attenuate();
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
         _backgroundFill = new BackgroundFill();
      return _backgroundFill;
   }

   public boolean isIconicMode() {
      return iconicMode;
   }

   public BoundDouble getPadding() {
      return padding;
   }

   public void setPadding(BoundDouble padding) {
      this.padding = padding;
   }

   public Font getFont() {
      if (font == null)
         setFont(DEFAULT_FONT);
      return font;
   }
   private void setRawFont(Font font) {
      Object old = this.font;
      this.font = font;
      propertyChanges.firePropertyChange("GraphicContext_font", old, font);
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

}