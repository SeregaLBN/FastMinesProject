package fmg.swing.res.img;

import java.awt.BasicStroke;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import fmg.common.Color;
import fmg.common.geom.PointDouble;
import fmg.common.geom.util.FigureHelper;
import fmg.data.controller.types.ESkillLevel;
import fmg.swing.Cast;

/** representable fmg.data.controller.types.ESkillLevel as image */
public abstract class MosaicsSkillImg<TImage extends Object> extends RotatedImg<ESkillLevel, TImage> {

   public MosaicsSkillImg(ESkillLevel group) { super(group); }
   public MosaicsSkillImg(ESkillLevel group, int widthAndHeight) { super(group, widthAndHeight); }
   public MosaicsSkillImg(ESkillLevel group, int widthAndHeight, int padding) { super(group, widthAndHeight, padding); }

   public ESkillLevel getMosaicSkill() { return getEntity(); }
   public void setMosaicSkill(ESkillLevel value) { setEntity(value); }

   protected Stream<Stream<PointDouble>> getCoords() {
      double sq = Math.min( // size inner square
            getWidth()  - getPadding().getLeftAndRight(),
            getHeight() - getPadding().getTopAndBottom());
      double r1 = sq/7; // external radius
      double r2 = sq/12; // internal radius
      int ordinal = getMosaicSkill().ordinal();
      int rays = 5 + ordinal; // rays count
      int stars = 4 + ordinal; // number of stars on the perimeter of the circle
      double[] angle = { getRotateAngle() };
      double starAngle = 360.0/stars;
      return IntStream.iterate(0, i -> ++i)
            .limit(stars)
            .mapToObj(st -> {
               Stream<PointDouble> points = (getMosaicSkill() == ESkillLevel.eCustom)
                     ? FigureHelper.getRegularPolygonCoords(3 + (st % 4), r1, -angle[0])
                     : FigureHelper.getRegularStarCoords(rays, r1, r2, -angle[0]);

               // (un)comment next line to view result changes...
               angle[0] = Math.sin(FigureHelper.toRadian(angle[0]/4))*angle[0]; // ускоряшка..

               // adding offset
               PointDouble offset = FigureHelper.getPointOnCircle(sq / 3, angle[0] + (st * starAngle));
               offset.x += getWidth() / 2.0;
               offset.x += getHeight() / 2.0;
               return points.map(p -> {
                  p.x += offset.x;
                  p.y += offset.y;
                  return p;
               });
            });
   }

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
      public Icon(ESkillLevel group) { super(group); }
      public Icon(ESkillLevel group, int widthAndHeight) { super(group, widthAndHeight); }
      public Icon(ESkillLevel group, int widthAndHeight, int padding) { super(group, widthAndHeight, padding); }

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

   public static class Image extends MosaicsSkillImg<java.awt.Image> {
      public Image(ESkillLevel group) { super(group); }
      public Image(ESkillLevel group, int widthAndHeight) { super(group, widthAndHeight); }
      public Image(ESkillLevel group, int widthAndHeight, Integer padding) { super(group, widthAndHeight, padding); }

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
