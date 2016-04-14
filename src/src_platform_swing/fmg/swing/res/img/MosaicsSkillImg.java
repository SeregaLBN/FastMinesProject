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

import javax.swing.Icon;

import fmg.common.Color;
import fmg.common.geom.PointDouble;
import fmg.common.geom.util.FigureHelper;
import fmg.data.controller.types.ESkillLevel;
import fmg.swing.Cast;

/** representable fmg.data.controller.types.ESkillLevel as image */
public class MosaicsSkillImg extends RotatedImg<ESkillLevel, Icon> {

   public MosaicsSkillImg(ESkillLevel group) {
      super(group);
   }
   public MosaicsSkillImg(ESkillLevel group, int widthAndHeight) {
      super(group, widthAndHeight);
   }
   public MosaicsSkillImg(ESkillLevel group, int widthAndHeight, Integer padding) {
      super(group, widthAndHeight, padding);
   }

   public ESkillLevel getMosaicSkill() { return getEntity(); }
   public void setMosaicSkill(ESkillLevel value) { setEntity(value); }

   private Stream<Stream<PointDouble>> getCoords() {
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

   @Override
   protected Icon createImage() {
      return new Icon() {
         @Override
         public int getIconWidth() { return MosaicsSkillImg.this.getWidth(); }
         @Override
         public int getIconHeight() { return MosaicsSkillImg.this.getHeight(); }
         @Override
         public void paintIcon(Component c, Graphics g, int x, int y) {
            g.setColor(Cast.toColor(getBackgroundColor()));
            g.fillRect(0,0, getIconWidth(), getIconHeight());

            getCoords()//.reverse()
               .forEach(coords -> {
                  g.setColor(Cast.toColor(getForegroundColorAttenuate()));
                  List<PointDouble> points = coords.collect(Collectors.toList());
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
               });
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
