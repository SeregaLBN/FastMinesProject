package fmg.swing.draw.img;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.CubicCurve2D;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;

import javax.swing.Icon;

import fmg.swing.utils.ImgUtils;

/** flag image */
public class Flag implements Icon {
   private static final double Zoom = 1.7;

   @Override
   public int getIconWidth() {
      return (int) (100 * Zoom);
   }

   @Override
   public int getIconHeight() {
      return (int) (100 * Zoom);
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
      // perimeter figure points
      Point2D.Double[] p = new Point2D.Double[] {
            new Point2D.Double(13.5 *Zoom, 90*Zoom),
            new Point2D.Double(17.44*Zoom, 51*Zoom),
            new Point2D.Double(21   *Zoom, 16*Zoom),
            new Point2D.Double(85   *Zoom, 15*Zoom),
            new Point2D.Double(81.45*Zoom, 50*Zoom)};

      BasicStroke penLine = new BasicStroke(15, BasicStroke.CAP_SQUARE, BasicStroke.JOIN_MITER);
      g.setStroke(penLine);
      g.setColor(Color.BLACK);
    //g.drawLine((int)p[0].x, (int)p[0].y, (int)p[2].x, (int)p[2].y);
      g.drawLine((int)p[0].x, (int)p[0].y, (int)p[1].x, (int)p[1].y);

      BasicStroke penCurve = new BasicStroke(12, BasicStroke.CAP_ROUND, BasicStroke.JOIN_MITER);
      g.setStroke(penCurve);
      g.setColor(Color.RED);
      CubicCurve2D curve = new CubicCurve2D.Double(
            p[2].x, p[2].y,
            95*Zoom, 0*Zoom,
            19.3*Zoom, 32*Zoom,
            p[3].x, p[3].y);
       g.draw(curve);
//       if (false) {
//         curve = new CubicCurve2D.Double(
//               p[1].x, p[1].y,
//               55.5*_zoom, 15*_zoom,
//               45*_zoom, 62.5*_zoom,
//               p[3].x, p[3].y);
//          g.draw(curve);
//       } else
       {
         curve = new CubicCurve2D.Double(
               p[4].x, p[4].y,
               15.83*Zoom, 67*Zoom,
               91.45*Zoom, 35*Zoom,
               p[1].x, p[1].y);
          g.draw(curve);
          curve = new CubicCurve2D.Double(
               p[3].x, p[3].y,
               77.8*Zoom, 32.89*Zoom,
               88.05*Zoom, 22.73*Zoom,
               p[4].x, p[4].y);
          g.draw(curve);
       }
       g.setStroke(penLine);
       g.drawLine((int)p[1].x, (int)p[1].y, (int)p[2].x, (int)p[2].y);
   }

   @Override
   public void paintIcon(Component c, Graphics g, int x, int y) {
      getIcon().paintIcon(c, g, x, y);
   }

   public static void main(String[] args) {
      TestDrawing.testApp2(size -> ImgUtils.zoom(new Flag(), size, size));
   }

}
