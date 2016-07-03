package fmg.swing.draw.img;

import java.awt.AlphaComposite;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Component;
import java.awt.Composite;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import fmg.common.Pair;
import fmg.common.geom.PointDouble;
import fmg.core.img.ALogo;
import fmg.swing.Cast;

/** Main logos image */
public abstract class Logo<TImage> extends ALogo<TImage> {

   static {
      StaticRotateImgConsts.init();
   }

   protected void drawBody(Graphics2D g) {
      { // fill background
         fmg.common.Color bkClr = getBackgroundColor();
         //if (bkClr.getA() != fmg.common.Color.Transparent.getA())
         {
            g.setColor(Cast.toColor(bkClr));
            g.fillRect(0, 0, getWidth(), getHeight());
         }
      }

      List<PointDouble> rays0 = new ArrayList<>();
      List<PointDouble> inn0 = new ArrayList<>();
      List<PointDouble> oct0 = new ArrayList<>();
      getCoords(rays0, inn0, oct0);

      Point2D.Double [] rays = rays0.stream().map(p -> Cast.toPoint(p)).toArray(size -> new Point2D.Double[size]);
      Point2D.Double [] inn  = inn0 .stream().map(p -> Cast.toPoint(p)).toArray(size -> new Point2D.Double[size]);
      Point2D.Double [] oct  = oct0 .stream().map(p -> Cast.toPoint(p)).toArray(size -> new Point2D.Double[size]);
      Point2D.Double center = new Point2D.Double(getWidth()/2.0, getHeight()/2.0);

      Color [] palette = Arrays.stream(Palette)
         .map(hsv -> Cast.toColor(hsv.toColor()))
         .toArray(size -> new Color[size]);

      // paint owner gradient rays
      for (int i=0; i<8; i++) {
         if (isUseGradient()) {
            // rectangle gragient
            g.setPaint(new GradientPaint(oct[(i+5)%8], palette[(i+0)%8], oct[i], palette[(i+3)%8]));
            fillPolygon(g, rays[i], oct[i], inn[i], oct[(i+5)%8]);

            // emulate triangle gradient (see BmpLogo.cpp C++ source code)
            Composite composite = g.getComposite();
            g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER));
            Color clr = palette[(i+6)%8];
            clr = new Color(clr.getRed(), clr.getGreen(), clr.getBlue(), 0);
            g.setPaint(new GradientPaint(center, clr, inn[(i+6)%8], palette[(i+3)%8]));
            fillPolygon(g, rays[i], oct[i], inn[i]);
            g.setPaint(new GradientPaint(center, clr, inn[(i+2)%8], palette[(i+0)%8]));
            fillPolygon(g, rays[i], oct[(i+5)%8], inn[i]);
            g.setComposite(composite);
         } else {
            g.setColor(Cast.toColor(Palette[i].toColor().darker()));
            fillPolygon(g, rays[i], oct[i], inn[i], oct[(i+5)%8]);
         }
      }

      // paint star perimeter
      double zoomAverage = (getZoomX() + getZoomY())/2;
      final double penWidth = Math.max(1, 2 * zoomAverage);
      g.setStroke(new BasicStroke((float)penWidth));
      for (int i=0; i<8; i++) {
         Point2D.Double p1 = rays[(i + 7)%8];
         Point2D.Double p2 = rays[i];
         g.setColor(palette[i].darker());
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
                  ? Cast.toColor(Palette[(i + 6)%8].toColor().brighter())
                  : Cast.toColor(Palette[(i + 6)%8].toColor().darker()));
         fillPolygon(g, inn[(i + 0)%8], inn[(i + 3)%8], center);
      }
   }

   private static void fillPolygon(Graphics2D g, Point2D.Double... p) {
      g.fillPolygon(
         Arrays.stream(p).mapToInt(s -> (int)s.x).toArray(),
         Arrays.stream(p).mapToInt(s -> (int)s.y).toArray(),
         p.length);
   }

   /////////////////////////////////////////////////////////////////////////////////////////////////////
   //    custom implementations
   /////////////////////////////////////////////////////////////////////////////////////////////////////

   public static class Icon extends Logo<javax.swing.Icon> {

      private BufferedImage buffImg;
      private Graphics2D gBuffImg;
      @Override
      protected javax.swing.Icon createImage() {
         if (gBuffImg != null)
            gBuffImg.dispose();

         buffImg = new BufferedImage(getWidth(), getHeight(), BufferedImage.TYPE_INT_ARGB);
         gBuffImg = buffImg.createGraphics();
         gBuffImg.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC));
         gBuffImg.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
         gBuffImg.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);

         return new javax.swing.Icon() {
            @Override
            public int getIconWidth() { return Icon.this.getWidth(); }
            @Override
            public int getIconHeight() { return Icon.this.getHeight(); }
            @Override
            public void paintIcon(Component c, Graphics g, int x, int y) {
               g.drawImage(buffImg, x,y, c);
            }
         };
      }

      @Override
      protected void drawBody() { drawBody(gBuffImg); }

      @Override
      public void close() {
         super.close();
         if (gBuffImg != null)
            gBuffImg.dispose();
         gBuffImg = null;
      }

   }

   public static class Image extends Logo<java.awt.Image> {

      @Override
      protected java.awt.Image createImage() {
         return new BufferedImage(getWidth(), getHeight(), BufferedImage.TYPE_INT_ARGB);
      }

      @Override
      protected void drawBody() {
         BufferedImage img = (BufferedImage) getImage();
         Graphics2D g = img.createGraphics();
         g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC));
         g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
         g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
         drawBody(g);
         g.dispose();
      }

   }

   ////////////// TEST //////////////
   public static void main(String[] args) {
      TestDrawing.<Object>testApp(rnd ->
         new Pair<>(new Logo.Icon(), new Logo.Image())
      );
   }

//   public static void main(String[] args) {
//      TestDrawing.testApp2(size -> new Logo.Icon(true, size).getImage());
//      //TestDrawing.testApp2(size -> ImgUtils.zoom((new Logo.Icon(true, size)).getImage(), size, size));
//   }

   //////////////////////////////////

}
