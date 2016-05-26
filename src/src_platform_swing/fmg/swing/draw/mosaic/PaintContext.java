package fmg.swing.draw.mosaic;

import java.awt.Font;

import javax.swing.JComponent;
import javax.swing.UIDefaults;
import javax.swing.UIManager;

import fmg.data.view.draw.FontInfo;
import fmg.swing.Cast;

/**
 * Information required for drawing the entire mosaic and cells. <br>
 * SWING impl
 *
 * @param <TImage> SWING specific image: {@link java.awt.Image} or {@link javax.swing.Icon}
 */
public class PaintContext<TImage> extends fmg.core.mosaic.draw.PaintContext<TImage> {

   public static final Font DEFAULT_FONT = new Font("SansSerif", Font.PLAIN, 10);

   /** TODO: Mosaic field - нуна избавиться... */
   private final JComponent owner;

   private Font font;

   public PaintContext(JComponent owner, boolean iconicMode) {
      super(iconicMode);
      this.owner = owner;
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

   public JComponent getOwner() {
      return owner;
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
   protected void onPropertyChanged(Object oldValue, Object newValue, String propertyName) {
      super.onPropertyChanged(oldValue, newValue, propertyName);
      if ("FontInfo".equals(propertyName)) {
         font = null;
         onPropertyChanged("Font");
      }
   }

}
