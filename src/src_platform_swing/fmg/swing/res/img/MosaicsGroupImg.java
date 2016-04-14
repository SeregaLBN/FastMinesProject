package fmg.swing.res.img;

import java.awt.BasicStroke;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.swing.Icon;

import fmg.common.Color;
import fmg.common.geom.PointDouble;
import fmg.common.geom.util.FigureHelper;
import fmg.core.types.EMosaicGroup;
import fmg.swing.Cast;

/** representable fmg.core.types.EMosaicGroup as image */
public class MosaicsGroupImg extends PolarLightsImg<EMosaicGroup, Icon> {

   public MosaicsGroupImg(EMosaicGroup group) {
      super(group);
   }
   public MosaicsGroupImg(EMosaicGroup group, int widthAndHeight) {
      super(group, widthAndHeight);
   }
   public MosaicsGroupImg(EMosaicGroup group, int widthAndHeight, Integer padding) {
      super(group, widthAndHeight, padding);
   }

   public EMosaicGroup getMosaicGroup() { return getEntity(); }
   public void setMosaicGroup(EMosaicGroup value) { setEntity(value); }

   private Stream<PointDouble> getCoords() {
      double sq = Math.min( // size inner square
            getWidth()  - getPadding().getLeftAndRight(),
            getHeight() - getPadding().getTopAndBottom());
      int vertices = 3 + getMosaicGroup().ordinal(); // vertices count
      Stream<PointDouble> points = (getMosaicGroup() != EMosaicGroup.eOthers)
            ? FigureHelper.getRegularPolygonCoords(vertices, sq/2, getRotateAngle())
            : FigureHelper.getRegularStarCoords(4, sq/2, sq/5, getRotateAngle());

      // adding offset
      double offsetX = getWidth() / 2.0;
      double offsetY = getHeight() / 2.0;
      return points.map(p -> {
         p.x += offsetX;
         p.y += offsetY;
         return p;
      });
   }

   @Override
   protected Icon createImage() {
      return new Icon() {
         @Override
         public int getIconWidth() { return MosaicsGroupImg.this.getWidth(); }
         @Override
         public int getIconHeight() { return MosaicsGroupImg.this.getHeight(); }
         @Override
         public void paintIcon(Component c, Graphics g, int x, int y) {
            g.setColor(Cast.toColor(getBackgroundColor()));
            g.fillRect(0,0, getIconWidth(), getIconHeight());

            g.setColor(Cast.toColor(getForegroundColorAttenuate()));
            List<PointDouble> points = getCoords().collect(Collectors.toList());
            g.fillPolygon(Cast.toPolygon(points));

            // draw perimeter border
            Color clr = getBorderColor();
            if (clr.getA() != Color.Transparent.getA()) {
               g.setColor(Cast.toColor(clr));
               int bw = getBorderWidth();
               ((Graphics2D)g).setStroke(new BasicStroke(bw));

               for (int i = 0; i < points.size(); i++) {
                  PointDouble p1 = points.get(i);
                  PointDouble p2 = (i != (points.size() - 1)) ? points.get(i + 1) : points.get(0);
                  g.drawLine((int) p1.x, (int) p1.y, (int) p2.x, (int) p2.y);
               }
            }
         }
      };
   }

   @Override
   protected void drawBody() {
      Icon ico = getImage();
      BufferedImage img = new BufferedImage(ico.getIconWidth(), ico.getIconHeight(), BufferedImage.TYPE_INT_ARGB);
      Graphics2D g = img.createGraphics();
      g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
      ico.paintIcon(null, g, 0, 0);
      g.dispose();
   }

}
