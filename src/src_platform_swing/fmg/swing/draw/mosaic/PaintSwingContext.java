package fmg.swing.draw.mosaic;

import java.awt.Font;

import javax.swing.UIDefaults;
import javax.swing.UIManager;

import fmg.common.Color;
import fmg.core.mosaic.draw.PaintContext;
import fmg.data.view.draw.FontInfo;
import fmg.swing.Cast;

/**
 * Information required for drawing the entire mosaic and cells. <br>
 * SWING impl
 *
 * @param <TImage> SWING specific image: {@link java.awt.Image} or {@link javax.swing.Icon}
 */
public class PaintSwingContext<TImage> extends PaintContext<TImage> {

   //public static final Font DEFAULT_FONT = new Font("SansSerif", Font.PLAIN, 10);

   private Font font;

   public PaintSwingContext(boolean iconicMode) {
      super(iconicMode);
   }

   public static final String PROPERTY_FONT = "Font";

   public static Color getDefaultBackgroundColor() {
      return PaintContext.getDefaultBackgroundColor();
   }

   static {
      UIDefaults uiDef = UIManager.getDefaults();
      java.awt.Color clr = uiDef.getColor("Panel.background");
      if (clr == null)
         clr = java.awt.Color.GRAY;
      _defaultBkColor = Cast.toColor(clr);
   }

   public Font getFont() {
      if (font == null) {
         //setFont(DEFAULT_FONT);
         FontInfo fi = getFontInfo();
         font = new Font(fi.getName(), fi.isBold() ? Font.BOLD : Font.PLAIN, fi.getSize());
      }
      return font;
   }

   public void setFont(Font newFont) {
      if (font == newFont) // ref eq
         return;
      if (font != null) {
         if (font.getName().equals(newFont.getName()) &&
            (font.getStyle() == newFont.getStyle()) &&
            (font.getSize() == newFont.getSize()))
            return;
      }
      FontInfo fi = getFontInfo();
      fi.setName(newFont.getName());
      fi.setBold((newFont.getStyle() & Font.BOLD) != 0);
      //fi.setSize(newFont.getSize()); // ! don't change original font size !
   }

   @Override
   protected void onSelfPropertyChanged(Object oldValue, Object newValue, String propertyName) {
      super.onSelfPropertyChanged(oldValue, newValue, propertyName);
      if (PaintContext.PROPERTY_FONT_INFO.equals(propertyName)) {
         font = null;
         onSelfPropertyChanged(PROPERTY_FONT);
      }
   }

}
