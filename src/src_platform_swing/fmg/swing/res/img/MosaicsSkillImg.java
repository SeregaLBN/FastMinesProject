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
import java.util.stream.IntStream;
import java.util.stream.Stream;

import javax.swing.JFrame;
import javax.swing.JPanel;

import fmg.common.Color;
import fmg.common.geom.PointDouble;
import fmg.common.geom.util.FigureHelper;
import fmg.data.controller.types.ESkillLevel;
import fmg.swing.Cast;

/** representable fmg.data.controller.types.ESkillLevel as image */
public abstract class MosaicsSkillImg<TImage extends Object> extends RotatedImg<ESkillLevel, TImage> {

   public MosaicsSkillImg(ESkillLevel skill) { super(skill); }
   public MosaicsSkillImg(ESkillLevel skill, int widthAndHeight) { super(skill, widthAndHeight); }
   public MosaicsSkillImg(ESkillLevel skill, int widthAndHeight, int padding) { super(skill, widthAndHeight, padding); }

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
      public Icon(ESkillLevel skill) { super(skill); }
      public Icon(ESkillLevel skill, int widthAndHeight) { super(skill, widthAndHeight); }
      public Icon(ESkillLevel skill, int widthAndHeight, int padding) { super(skill, widthAndHeight, padding); }

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

   public static class Image extends MosaicsSkillImg<java.awt.Image> {
      public Image(ESkillLevel skill) { super(skill); }
      public Image(ESkillLevel skill, int widthAndHeight) { super(skill, widthAndHeight); }
      public Image(ESkillLevel skill, int widthAndHeight, Integer padding) { super(skill, widthAndHeight, padding); }

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
             setTitle("test paints MosaicsSkillImg.Image & MosaicsSkillImg.Icon");

             Random rnd = new Random(UUID.randomUUID().hashCode());
             ESkillLevel skill = ESkillLevel.fromOrdinal(rnd.nextInt(ESkillLevel.values().length));
             MosaicsSkillImg.Icon img1 = new MosaicsSkillImg.Icon(skill, SIZE/2);

             skill = ESkillLevel.fromOrdinal(rnd.nextInt(ESkillLevel.values().length));
             MosaicsSkillImg.Image img2 = new MosaicsSkillImg.Image(skill, SIZE/2);

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
