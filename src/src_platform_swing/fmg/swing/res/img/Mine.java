package fmg.swing.res.img;

import java.awt.Component;
import java.awt.Graphics;

import javax.swing.Icon;

/** mine image */
public class Mine implements Icon {

   private final Logo _logo;
   {
      _logo = new Logo(false);
      _logo.setPadding(10);
      _logo.setZoom(0.7);
      for (int i = 0; i < _logo.Palette.length; i++)
         _logo.Palette[i] = _logo.Palette[i].darker(0.5);
   }

   @Override
   public int getIconWidth() { return _logo.getIconWidth(); }

   @Override
   public int getIconHeight() { return _logo.getIconHeight(); }

   @Override
   public void paintIcon(Component c, Graphics g, int x, int y) {
      _logo.paintIcon(c, g, x, y);
   }

}
