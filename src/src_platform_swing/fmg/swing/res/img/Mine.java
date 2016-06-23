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
      try (Logo.Icon logo = new Logo.Icon()) {
         logo.setUseGradient(false);
         logo.setSize(150);
         logo.setPadding(10);
         for (int i = 0; i < logo.Palette.length; i++)
            logo.Palette[i].v = 75;
         logo.getImage().paintIcon(c, g, x, y);
      }
   }

}
