package fmg.swing.res.img;

import java.awt.BasicStroke;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.util.List;
import java.util.stream.Collectors;

import javax.swing.SwingUtilities;

import fmg.common.Color;
import fmg.common.Pair;
import fmg.common.geom.PointDouble;
import fmg.core.img.RotatedImg;
import fmg.core.img.StaticImg;
import fmg.data.controller.types.ESkillLevel;
import fmg.swing.Cast;

/** representable fmg.data.controller.types.ESkillLevel as image
 *  SWING impl
 **/
public abstract class MosaicsSkillImg<TImage extends Object> extends fmg.core.img.MosaicsSkillImg<TImage> {

   static {
      StaticImg.DEFERR_INVOKER = doRun -> SwingUtilities.invokeLater(doRun);
      RotatedImg.TIMER_CREATOR = () -> new fmg.swing.ui.Timer();
   }

   public MosaicsSkillImg(ESkillLevel skill) { super(skill); }
   public MosaicsSkillImg(ESkillLevel skill, int widthAndHeight) { super(skill, widthAndHeight); }
   public MosaicsSkillImg(ESkillLevel skill, int widthAndHeight, int padding) { super(skill, widthAndHeight, padding); }

   protected void drawBody(Graphics g) {
      Graphics2D g2 = (Graphics2D) g;
      g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

      g.setColor(Cast.toColor(getBackgroundColor()));
      g.fillRect(0, 0, getWidth(), getHeight());

      getCoords()// .reverse()
            .forEach(coords -> {
               g.setColor(Cast.toColor(getForegroundColorAttenuate()));
               List<PointDouble> points = coords.collect(Collectors.toList());
               g.fillPolygon(Cast.toPolygon(points));

               // draw perimeter border
               Color clr = getBorderColor();
               if (clr.getA() != Color.Transparent.getA()) {
                  g.setColor(Cast.toColor(clr));
                  int bw = getBorderWidth();
                  g2.setStroke(new BasicStroke(bw));

                  for (int i = 0; i < points.size(); i++) {
                     PointDouble p1 = points.get(i);
                     PointDouble p2 = (i != (points.size() - 1)) ? points.get(i + 1) : points.get(0);
                     g.drawLine((int) p1.x, (int) p1.y, (int) p2.x, (int) p2.y);
                  }
               }
            });
   }

   public static class Icon extends MosaicsSkillImg<javax.swing.Icon> {
      public Icon(ESkillLevel skill) { super(skill); }
      public Icon(ESkillLevel skill, int widthAndHeight) { super(skill, widthAndHeight); }
      public Icon(ESkillLevel skill, int widthAndHeight, int padding) { super(skill, widthAndHeight, padding); }

      private BufferedImage buffImg;
      private Graphics gBuffImg;
      @Override
      protected javax.swing.Icon createImage() {
         if (gBuffImg != null)
            gBuffImg.dispose();

         buffImg = new BufferedImage(getWidth(), getHeight(), BufferedImage.TYPE_INT_ARGB);
         gBuffImg = buffImg.createGraphics();

         return new javax.swing.Icon() {
            @Override
            public int getIconWidth() { return Icon.this.getWidth(); }
            @Override
            public int getIconHeight() { return Icon.this.getHeight(); }
            @Override
            public void paintIcon(Component c, Graphics g, int x, int y) {
               g.drawImage(buffImg, x,y, null);
            }
         };
      }

      @Override
      protected void drawBody() { drawBody(gBuffImg); }

   }

   public static class Image extends MosaicsSkillImg<java.awt.Image> {
      public Image(ESkillLevel skill) { super(skill); }
      public Image(ESkillLevel skill, int widthAndHeight) { super(skill, widthAndHeight); }
      public Image(ESkillLevel skill, int widthAndHeight, Integer padding) { super(skill, widthAndHeight, padding); }

      @Override
      protected java.awt.Image createImage() {
         return new BufferedImage(getWidth(), getHeight(), BufferedImage.TYPE_INT_ARGB);
      }

      @Override
      protected void drawBody() {
         BufferedImage img = (BufferedImage) getImage();
         Graphics g = img.createGraphics();
         drawBody(g);
         g.dispose();
      }

   }

   ////////////// TEST //////////////
   public static void main(String[] args) {
      TestDrawing.<ESkillLevel>testApp(rnd -> {
         ESkillLevel skill = ESkillLevel.fromOrdinal(rnd.nextInt(ESkillLevel.values().length));
         MosaicsSkillImg.Icon img1 = new MosaicsSkillImg.Icon(skill);

         skill = ESkillLevel.fromOrdinal(rnd.nextInt(ESkillLevel.values().length));
         MosaicsSkillImg.Image img2 = new MosaicsSkillImg.Image(skill);

         return new Pair<>(img1, img2);
      });
   }
   //////////////////////////////////

}
