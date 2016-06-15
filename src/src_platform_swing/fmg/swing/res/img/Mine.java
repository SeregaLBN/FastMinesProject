package fmg.swing.res.img;

import java.awt.Component;
import java.awt.Graphics;

import javax.swing.Icon;

/** mine image */
public class Mine implements Icon {

   @Override
   public int getIconWidth() { return 150; }

   @Override
   public int getIconHeight() { return 150; }

   @Override
   public void paintIcon(Component c, Graphics g, int x, int y) {
      try (Logo.Icon logo = new Logo.Icon(false, 150, 10)) {
         for (int i = 0; i < logo.Palette.length; i++)
            logo.Palette[i] = logo.Palette[i].darker(0.5);
         logo.getImage().paintIcon(c, g, x, y);
      }
   }

}
