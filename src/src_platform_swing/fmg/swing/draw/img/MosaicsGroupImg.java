package fmg.swing.draw.img;

import java.awt.AlphaComposite;
import java.awt.BasicStroke;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.util.List;
import java.util.stream.Collectors;

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

   public MosaicsGroupImg(EMosaicGroup group) { super(group); }

   protected void drawBody(Graphics g) {
      Graphics2D g2 = (Graphics2D) g;

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

   /////////////////////////////////////////////////////////////////////////////////////////////////////
   //    custom implementations
   /////////////////////////////////////////////////////////////////////////////////////////////////////

   public static class Icon extends MosaicsGroupImg<javax.swing.Icon> {

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
      TestDrawing.<EMosaicGroup>testApp(rnd -> {
         EMosaicGroup eGroup = EMosaicGroup.fromOrdinal(rnd.nextInt(EMosaicGroup.values().length));
         MosaicsGroupImg.Icon img1 = new MosaicsGroupImg.Icon(eGroup);

         eGroup = EMosaicGroup.fromOrdinal(rnd.nextInt(EMosaicGroup.values().length));
         MosaicsGroupImg.Image img2 = new MosaicsGroupImg.Image(eGroup);

         return new Pair<>(img1, img2);
      });
   }
   //////////////////////////////////

}
