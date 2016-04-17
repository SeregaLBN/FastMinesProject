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

import fmg.common.Color;
import fmg.common.geom.PointDouble;
import fmg.common.geom.util.FigureHelper;
import fmg.core.types.EMosaicGroup;
import fmg.swing.Cast;

/** representable fmg.core.types.EMosaicGroup as image */
public abstract class MosaicsGroupImg<TImage extends Object> extends PolarLightsImg<EMosaicGroup, TImage> {

   public MosaicsGroupImg(EMosaicGroup group) { super(group); }
   public MosaicsGroupImg(EMosaicGroup group, int widthAndHeight) { super(group, widthAndHeight); }
   public MosaicsGroupImg(EMosaicGroup group, int widthAndHeight, Integer padding) { super(group, widthAndHeight, padding); }

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

   public void drawBody(Graphics g) {
      Graphics2D g2 = (Graphics2D) g;
      g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

      g.setColor(Cast.toColor(getBackgroundColor()));
      g.fillRect(0, 0, getWidth(), getHeight());

      g.setColor(Cast.toColor(getForegroundColorAttenuate()));
      List<PointDouble> points = getCoords().collect(Collectors.toList());
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
   }

   public static class Icon extends MosaicsGroupImg<javax.swing.Icon> {
      public Icon(EMosaicGroup group) { super(group); }
      public Icon(EMosaicGroup group, int widthAndHeight) { super(group, widthAndHeight); }
      public Icon(EMosaicGroup group, int widthAndHeight, Integer padding) { super(group, widthAndHeight, padding); }

      @Override
      protected javax.swing.Icon createImage() {
         return new javax.swing.Icon() {
            @Override
            public int getIconWidth() { return Icon.this.getWidth(); }
            @Override
            public int getIconHeight() { return Icon.this.getHeight(); }
            @Override
            public void paintIcon(Component c, Graphics g, int x, int y) { drawBody(g); }
         };
      }

      @Override
      protected void drawBody() {
         javax.swing.Icon ico = getImage();
         BufferedImage img = new BufferedImage(getWidth(), getHeight(), BufferedImage.TYPE_INT_ARGB);
         Graphics g = img.createGraphics();
         ico.paintIcon(null, g, 0, 0);
         g.dispose();
      }

   }

   public static class Image extends MosaicsGroupImg<java.awt.Image> {
      public Image(EMosaicGroup group) { super(group); }
      public Image(EMosaicGroup group, int widthAndHeight) { super(group, widthAndHeight); }
      public Image(EMosaicGroup group, int widthAndHeight, Integer padding) { super(group, widthAndHeight, padding); }

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

}
