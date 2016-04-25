package fmg.swing.res.img;

import java.awt.BasicStroke;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;
import java.beans.PropertyChangeListener;
import java.util.List;
import java.util.Random;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.swing.JFrame;
import javax.swing.JPanel;

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

      private BufferedImage buffImg;
      private Graphics gBuffImg;
      @Override
      protected javax.swing.Icon createImage() {
         if (gBuffImg != null)
            gBuffImg.dispose();

         buffImg = new BufferedImage(getWidth(), getHeight(), BufferedImage.TYPE_INT_ARGB);
         gBuffImg = buffImg.createGraphics();

         return new javax.swing.Icon() {
            @Override
            public int getIconWidth() { return Icon.this.getWidth(); }
            @Override
            public int getIconHeight() { return Icon.this.getHeight(); }
            @Override
            public void paintIcon(Component c, Graphics g, int x, int y) {
               g.drawImage(buffImg, x,y, null);
            }
         };
      }

      @Override
      protected void drawBody() { drawBody(gBuffImg); }

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

   ////////////// TEST //////////////
   public static void main(String[] args) {
      new JFrame() {
         private static final long serialVersionUID = 1L;
         static final int SIZE = 700;
         {
             setSize(SIZE+30, SIZE+50);
             setLocationRelativeTo(null);
             setTitle("test paints MosaicsGroupImg.Image & MosaicsGroupImg.Icon");

             Random rnd = new Random(UUID.randomUUID().hashCode());
             EMosaicGroup eGroup = EMosaicGroup.fromOrdinal(rnd.nextInt(EMosaicGroup.values().length));
             MosaicsGroupImg.Icon img1 = new MosaicsGroupImg.Icon(eGroup, SIZE/2);

             eGroup = EMosaicGroup.fromOrdinal(rnd.nextInt(EMosaicGroup.values().length));
             MosaicsGroupImg.Image img2 = new MosaicsGroupImg.Image(eGroup, SIZE/2);

             JPanel jPanel = new JPanel() {
                private static final long serialVersionUID = 1L;
                {
                   setPreferredSize(new Dimension(SIZE, SIZE));
                }
                @Override
                public void paintComponent(Graphics g) {
                   super.paintComponent(g);
                   final int offset = 10;
                   //g.fillRect(offset, offset, SIZE-offset, SIZE-offset);
                   g.drawRect(offset, offset, SIZE-offset, SIZE-offset);

                   img1.getImage().paintIcon(this, g, 2*offset, 2*offset);
                   g.drawImage(img2.getImage(), SIZE/2-offset, SIZE/2-offset, null);
                }
             };
             add(jPanel);

             PropertyChangeListener l = evt -> {
                if ("Image".equals(evt.getPropertyName())) {
                   //jPanel.invalidate();
                   //jPanel.revalidate();
                   jPanel.repaint();
                }
             };
             img1.addListener(l);
             img2.addListener(l);

             final boolean testTransparent = !false;
             if (testTransparent) {  // test transparent
                Color bkClr = Color.RandomColor(rnd); bkClr.setA((byte)0x40); // Color.Transparent; //
                img1.setBackgroundColor(bkClr);
                bkClr = Color.RandomColor(rnd); bkClr.setA((byte)0x30); // Color.Transparent; //
                img2.setBackgroundColor(bkClr);
             } else {
                img1.setBackgroundColor(Cast.toColor(jPanel.getBackground()));
                img2.setBackgroundColor(Cast.toColor(jPanel.getBackground()));
             }
             img1.setRotateAngle(33.333);
             img2.setRotateAngle(-15);

             img1.setRotateAngleDelta( -img1.getRotateAngleDelta());
             img2.setRotateAngleDelta(3*img2.getRotateAngleDelta());
             img1.setRotate(true);
             img2.setRotate(true);

             //setDefaultCloseOperation(EXIT_ON_CLOSE);
             addWindowListener(new WindowAdapter() {
                @Override
                public void windowClosing(WindowEvent we) {
                   img1.close();
                   img2.close();
                   dispose();
                }
             });
             setVisible(true);
         }
      };
   }
   //////////////////////////////////

}
