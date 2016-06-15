package fmg.swing.res.img;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;

import javax.swing.Icon;

import fmg.swing.utils.ImgUtils;

/** картинка для фоновой паузы */
public class BackgroundPause implements Icon {
   final boolean newLogo;

   public BackgroundPause() {
      newLogo = true;
   }

   @Deprecated
   public BackgroundPause(boolean newLogo) {
      this.newLogo = newLogo;
   }

   @Override
   public int getIconWidth() {
      return !newLogo ? 1000 : 550;
   }

   @Override
   public int getIconHeight() {
      return !newLogo ? 1000 : 550;
   }

   private Icon _icoOld;
   private Icon getIcon() {
      if (_icoOld == null) {
         BufferedImage img = new BufferedImage(getIconWidth(), getIconHeight(), BufferedImage.TYPE_INT_ARGB);
         Graphics2D g = img.createGraphics();
         g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
         g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
         draw(g);
         g.dispose();
         _icoOld = ImgUtils.toIco(img);
      }
      return _icoOld;
   }

   private void draw(Graphics2D g) {
//      // fill background (only transparent color)
//      g.setColor(new Color(0x00123456, true));
//      g.fillRect(0, 0, getIconWidth(), getIconHeight());

      // тело смайла
      g.setColor(new Color(0x00FFE600));
      g.fillOval(5, 5, getIconWidth()-10, getIconHeight()-10);

      // глаза
      g.setColor(new Color(0x00000000));
      g.fillOval(330, 150, 98, 296);
      g.fillOval(570, 150, 98, 296);

      // smile
      g.setStroke(new BasicStroke(14, BasicStroke.CAP_ROUND, BasicStroke.JOIN_BEVEL));
      g.drawArc(103, -133, 795, 1003, 207, 126);

      // ямочки на щеках
      g.drawArc(90, 580, 180, 180, 85, 57);
      g.drawArc(730, 580, 180, 180, 38, 57);
   }

   @Override
   public void paintIcon(Component c, Graphics g, int x, int y) {
      if (newLogo) {
         try (Logo.Icon logo = new Logo.Icon(true, 550, 10)) {
            logo.getImage().paintIcon(c, g, x, y);
         }
      } else {
         getIcon().paintIcon(c, g, x, y);
      }
   }

   public static void main(String[] args) {
      TestDrawing.testApp2(size -> ImgUtils.zoom(new BackgroundPause(), size, size));
   }

}
