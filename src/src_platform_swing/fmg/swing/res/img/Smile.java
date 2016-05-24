package fmg.swing.res.img;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Component;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.geom.Arc2D;
import java.awt.geom.Area;
import java.awt.geom.Ellipse2D;
import java.awt.image.BufferedImage;

import javax.swing.Icon;

import fmg.common.HSV;
import fmg.swing.Cast;
import fmg.swing.utils.ImgUtils;

public class Smile implements Icon {

   private final double _size;


   public Smile(int size) { _size = size; }

   @Override
   public int getIconWidth() { return (int)_size; }
   @Override
   public int getIconHeight() { return (int)_size; }

   @Override
   public void paintIcon(Component c, Graphics g, int x, int y) {
      getIcon().paintIcon(c, g, x, y);
   }

   Icon _ico;
   private Icon getIcon() {
      if (_ico == null) {
         BufferedImage img = new BufferedImage(getIconWidth(), getIconHeight(), BufferedImage.TYPE_INT_ARGB);
         Graphics2D g = img.createGraphics();
         g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
         g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
         draw(g);
         g.dispose();
         _ico = ImgUtils.toIco(img);
      }
      return _ico;
   }

   private void draw(Graphics2D g) {
      Color oldColor = g.getColor();
      Shape oldClip = g.getClip();
      Paint oldPaint = g.getPaint();

      drawBody(g);
      drawEyes(g);
      drawMouth(g);

      // restore
      g.setColor(oldColor);
      g.setPaint(oldPaint);
      g.setClip(oldClip);
   }

   private void drawBody(Graphics2D g) {
      Color yellow = Color.YELLOW;//Cast.toColor(Cast.toColor(Color.YELLOW).brighter(0.1));
      HSV hsv = new HSV(Cast.toColor(yellow));
      hsv.h += 6;
      Color yellowBrighter =  Cast.toColor(Cast.toColor(yellow).brighter(0.5)); // Cast.toColor(hsv.toColor()); //
      hsv.h -= 12;
      Color yellowDarker = Cast.toColor(hsv.toColor()); // Cast.toColor(Cast.toColor(yellow).darker(0.3));

      { // рисую затемненный круг
         //{ // variant 1
         //   g.setColor(yellowDarker);
         //   g.fillOval(0, 0, (int)_size, (int)_size);
         //}
         { // variant 2: рисую не круг, а кольцо
            double wh = _size;
            Ellipse2D ellipse1 = new Ellipse2D.Double(0, 0, wh, wh);
            double pad = _size/30; // offset
            wh -= 2*pad;
            Ellipse2D ellipse2 = new Ellipse2D.Double(pad, pad, wh, wh);
            g.setColor(yellowDarker);
            g.fill(intersectExclude(ellipse1, ellipse2));
         }
      }
      { // поверх него, внутри - градиентный круг
         double pad = _size/30; // offset
         g.setPaint(new GradientPaint(0, 0, yellowBrighter, (float)_size, (float)_size, yellowDarker));
         //g.setPaint(new GradientPaint(0, 0, yellow, (float)_size, (float)_size, yellow));
         g.fillOval((int)pad, (int)pad, (int)(_size-pad*2), (int)(_size-pad*2));
      }
      { // верхний левый блик
         double pad = _size/30; // offset
         double wh = _size-pad*2;
         Ellipse2D ellipse1 = new Ellipse2D.Double(pad, pad, wh, wh);
         wh = 1.13*_size;
         Ellipse2D ellipse2 = new Ellipse2D.Double(pad, pad, wh, wh);
         g.setColor(yellowBrighter); // Color.DARK_GRAY
         g.fill(intersectExclude(ellipse1, ellipse2));
         //g.setColor(Color.BLACK);
         //g.draw(ellipse1);
         //g.draw(ellipse2);
      }
      { // нижний правый блик
         double pad = _size/30; // offset
         double wh1 = _size-pad*2;
         Ellipse2D ellipse1 = new Ellipse2D.Double(pad, pad, wh1, wh1);
         double wh2 = 1.13*_size;
         Ellipse2D ellipse2 = new Ellipse2D.Double(pad+wh1-wh2, pad+wh1-wh2, wh2, wh2);
         g.setColor(yellowDarker); // Color.DARK_GRAY
         g.fill(intersectExclude(ellipse1, ellipse2));
         //g.setColor(Color.BLACK);
         //g.draw(ellipse1);
         //g.draw(ellipse2);
      }
   }

   private void drawEyes(Graphics2D g) {
      g.setColor(Color.DARK_GRAY);
      g.fillOval((int)(0.330*_size), (int)(0.150*_size), (int)(0.098*_size), (int)(0.296*_size));
      g.fillOval((int)(0.570*_size), (int)(0.150*_size), (int)(0.098*_size), (int)(0.296*_size));
   }

   private void drawMouth(Graphics2D g) {
      // smile
      g.setColor(Color.DARK_GRAY);
//      Stroke stroke = g.getStroke();
      g.setStroke(new BasicStroke((float)Math.max(1, 0.014*_size), BasicStroke.CAP_ROUND, BasicStroke.JOIN_BEVEL));
      Arc2D arcSmile = new Arc2D.Double(0.103*_size, -0.133*_size, 0.795*_size, 1.003*_size, 207, 126, Arc2D.OPEN);
      g.draw(arcSmile); // g.drawArc((int)(0.103*_size), (int)(-0.133*_size), (int)(0.795*_size), (int)(1.003*_size), 207, 126);
//      arcSmile.setArcType(Arc2D.CHORD);
      Ellipse2D ellipse = new Ellipse2D.Double((int)(0.060*_size), (int)(0.477*_size), (int)(0.877*_size), (int)(0.330*_size));
      g.fill(intersectExclude(arcSmile, ellipse));

      // ямо на щк
      g.drawArc((int)(+0.020*_size), (int)(0.420*_size), (int)(0.180*_size), (int)(0.180*_size), 85+180, 57);
      g.drawArc((int)(+0.800*_size), (int)(0.420*_size), (int)(0.180*_size), (int)(0.180*_size), 38+180, 57);
   }

   private static Area intersectExclude(Shape s1, Shape s2) {
      Area outside = new Area(s1);
      outside.subtract(new Area(s2));
      return outside;
    }

   public static void main(String[] args) {
//      TestDrawing.testApp2(size -> ImgUtils.zoom(new Smile(size), size, size));
      TestDrawing.testApp2(size -> new Smile(size));
   }

}
