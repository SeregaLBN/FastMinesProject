package fmg.swing.draw.img;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import fmg.common.Color;
import fmg.common.Pair;
import fmg.common.geom.PointDouble;
import fmg.core.img.AMosaicsGroupImg;
import fmg.core.types.EMosaicGroup;
import fmg.swing.Cast;

/**
 * Representable {@link fmg.core.types.EMosaicGroup} as image
 * <br>
 * SWING impl
 *
 * @param <TImage> SWING specific image: {@link java.awt.Image} or {@link javax.swing.Icon})
 **/
public abstract class MosaicsGroupImg<TImage> extends AMosaicsGroupImg<TImage> {

   static {
      StaticRotateImgConsts.init();
   }

   /** @param group - may be null. if Null - representable image of EMosaicGroup.class */
   protected MosaicsGroupImg(EMosaicGroup group) { super(group); }

   protected void drawBody(Graphics2D g) {
      g.setColor(Cast.toColor(getBackgroundColor()));
      g.fillRect(0, 0, getWidth(), getHeight());

      Stream<Pair<Color, Stream<PointDouble>>> stars = getCoords();
      stars.forEach(pair -> {
         g.setColor(Cast.toColor(pair.first));
         List<PointDouble> points = pair.second.collect(Collectors.toList());
         g.fillPolygon(Cast.toPolygon(points));

         // draw perimeter border
         Color clr = getBorderColor();
         if (!clr.isTransparent()) {
            g.setColor(Cast.toColor(clr));
            int bw = getBorderWidth();
            g.setStroke(new BasicStroke(bw));

            for (int i = 0; i < points.size(); i++) {
               PointDouble p1 = points.get(i);
               PointDouble p2 = (i != (points.size() - 1)) ? points.get(i + 1) : points.get(0);
               g.drawLine((int) p1.x, (int) p1.y, (int) p2.x, (int) p2.y);
            }
         }
      });

      getCoordsBurgerMenu()
         .forEach(li -> {
            g.setStroke(new BasicStroke((float)li.penWidht));
            g.setColor(Cast.toColor(li.clr));
            g.drawLine((int)li.from.x, (int)li.from.y, (int)li.to.x, (int)li.to.y);
         });
   }

   /////////////////////////////////////////////////////////////////////////////////////////////////////
   //    custom implementations
   /////////////////////////////////////////////////////////////////////////////////////////////////////

   public static class Icon extends MosaicsGroupImg<javax.swing.Icon> {

      /** @param group - may be null. if Null - representable image of EMosaicGroup.class */
      public Icon(EMosaicGroup group) { super(group); }

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

   public static class Image extends MosaicsGroupImg<java.awt.Image> {

      /** @param group - may be null. if Null - representable image of EMosaicGroup.class */
      public Image(EMosaicGroup group) { super(group); }

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
      TestDrawing.testApp(rnd ->
         Stream.concat(Stream.of((EMosaicGroup)null),
                       Stream.of(EMosaicGroup.values()))
               .map(e -> new Pair<>(new MosaicsGroupImg.Icon (e),
                                    new MosaicsGroupImg.Image(e)))
               .flatMap(x -> Stream.of(x.first, x.second))
               .collect(Collectors.toList())
      );
   }
   //////////////////////////////////

}
