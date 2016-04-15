package fmg.swing.res.img;

import java.awt.Component;
import java.awt.Graphics;

import javax.swing.Icon;

import fmg.swing.Cast;

/** mine image */
public class Mine implements Icon {

   private final Logo _logo;
   {
      _logo = new Logo(false);
      _logo.setMargin(10);
      _logo.setZoomX(0.7);
      _logo.setZoomY(0.7);
      for (int i = 0; i < _logo.Palette.length; i++)
         _logo.Palette[i] = Cast.toColor(Cast.toColor(_logo.Palette[i]).darker(0.5));
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
