package fmg.swing.res.img;

import java.awt.*;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.swing.Icon;

import fmg.common.geom.PointDouble;
import fmg.swing.Cast;
import fmg.swing.utils.ImgUtils;

/** main logos image */
public class Logo extends fmg.core.img.Logo<Icon> implements Icon {

   public Logo(boolean useGradient) {
      super(useGradient);
   }

   @Override
   public int getIconWidth() { return (int)getWidth(); }

   @Override
   public int getIconHeight() { return (int)getHeight(); }

   @Override
   protected Icon createImage() {
      BufferedImage img = new BufferedImage(getIconWidth(), getIconHeight(), BufferedImage.TYPE_INT_ARGB);
      Graphics2D g = img.createGraphics();
      g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
      g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
      draw(g);
      g.dispose();
      return ImgUtils.toIco(img);
   }

   @Override
   protected void drawImage(Icon img) {
      // none... already drawed in createImage()
   }

   private void draw(Graphics2D g) {
      final int iPenWidth = 2;

      List<PointDouble> rays0 = new ArrayList<>();
      List<PointDouble> inn0 = new ArrayList<>();
      List<PointDouble> oct0 = new ArrayList<>();
      getCoords(rays0, inn0, oct0);

      Point2D.Double [] rays = rays0.stream().map(p -> Cast.toPoint(p)).toArray(size -> new Point2D.Double[size]);
      Point2D.Double [] inn  = inn0 .stream().map(p -> Cast.toPoint(p)).toArray(size -> new Point2D.Double[size]);
      Point2D.Double [] oct  = oct0 .stream().map(p -> Cast.toPoint(p)).toArray(size -> new Point2D.Double[size]);
      Point2D.Double center = new Point2D.Double(getWidth()/2, getHeight()/2);

      Color [] palette = Arrays.stream(Palette).map(clr -> Cast.toColor(clr)).toArray(size -> new Color[size]);

      // paint owner gradient rays
      for (int i=0; i<8; i++) {
         if (isUseGradient()) {
            // rectangle gragient
            g.setPaint(new GradientPaint(oct[(i+5)%8], palette[(i+0)%8], oct[i], palette[(i+3)%8]));
            g.fillPolygon(new int[] {
                  (int)rays[i].x,
                  (int)oct[i].x,
                  (int)inn[i].x,
                  (int)oct[(i+5)%8].x
               }, new int[] {
                  (int)rays[i].y,
                  (int)oct[i].y,
                  (int)inn[i].y,
                  (int)oct[(i+5)%8].y
               }, 4);

            // emulate triangle gradient (see BmpLogo.cpp C++ source code)
            Color clr = palette[(i+6)%8];
            clr = new Color(clr.getRed(), clr.getGreen(), clr.getBlue(), 0);
            g.setPaint(new GradientPaint(center, clr, inn[(i+6)%8], palette[(i+3)%8]));
            g.fillPolygon(new int[] {
                  (int)rays[i].x,
                  (int)oct[i].x,
                  (int)inn[i].x
               }, new int[] {
                  (int)rays[i].y,
                  (int)oct[i].y,
                  (int)inn[i].y
               }, 3);
            g.setPaint(new GradientPaint(center, clr, inn[(i+2)%8], palette[(i+0)%8]));
            g.fillPolygon(new int[] {
                  (int)rays[i].x,
                  (int)oct[(i+5)%8].x,
                  (int)inn[i].x
               }, new int[] {
                  (int)rays[i].y,
                  (int)oct[(i+5)%8].y,
                  (int)inn[i].y
               }, 3);
         } else {
            g.setColor(Cast.toColor(Palette[i].darker()));
            g.fillPolygon(new int [] {
                  (int)rays[i].x,
                  (int)oct[i].x,
                  (int)inn[i].x,
                  (int)oct[(i+5)%8].x
               }, new int [] {
                  (int)rays[i].y,
                  (int)oct[i].y,
                  (int)inn[i].y,
                  (int)oct[(i+5)%8].y
               }, 4);
         }
      }

      // paint star perimeter
      g.setStroke(new BasicStroke(iPenWidth));
      for (int i=0; i<8; i++) {
         Point2D.Double p1 = rays[(i + 7)%8];
         Point2D.Double p2 = rays[i];
         g.setColor(palette[i]);
         g.drawLine((int)p1.x, (int)p1.y, (int)p2.x, (int)p2.y);
      }

      // paint inner gradient triangles
      for (int i=0; i<8; i++) {
         if (isUseGradient())
            g.setPaint(new GradientPaint(
                  inn[i], palette[(i+6)%8],
                  center, ((i&1)==0) ? Color.BLACK : Color.WHITE));
         else
            g.setColor(((i & 1) == 0)
                  ? Cast.toColor(Palette[(i + 6)%8].brighter())
                  : Cast.toColor(Palette[(i + 6)%8].darker()));
         g.fillPolygon(new int [] {
               (int)inn[(i + 0)%8].x,
               (int)inn[(i + 3)%8].x,
               (int)center.x
            }, new int [] {
               (int)inn[(i + 0)%8].y,
               (int)inn[(i + 3)%8].y,
               (int)center.y
            }, 3);
      }
   }

   @Override
   public void paintIcon(Component c, Graphics g, int x, int y) {
      getImage().paintIcon(c, g, x, y);
   }

   public static void main(String[] args) {
      TestDrawing.testApp2(size -> ImgUtils.zoom(new Logo(true), size, size));
   }

}
