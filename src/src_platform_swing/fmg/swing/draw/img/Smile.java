package fmg.swing.draw.img;

import java.awt.*;
import java.awt.geom.*;
import java.awt.image.BufferedImage;

import javax.swing.Icon;

import fmg.common.geom.PointDouble;
import fmg.swing.utils.ImgUtils;

public class Smile implements Icon {

   // http://unicode-table.com/blocks/emoticons/
   // http://unicode-table.com/blocks/miscellaneous-symbols-and-pictographs/
   public enum EType {
      /** :) ☺ -  Незакрашенное улыбающееся лицо U+263A */
      Face_WhiteSmiling,             // newNormal

      /** :( 😞 - Разочарованное лицо U+1F61E */
      Face_Disappointed,             // newNormalLoss

      /** 😀 - Ухмыляющееся лицо U+1F600 */
      Face_Grinning,                 // newNormalMosaic

      /** 😎 - Улыбающееся лицо в солнечных очках U+1F60E */
      Face_SmilingWithSunglasses,    // newNormalWin

      /** 😋 - Лицо, смакующее деликатес U+1F60B */
      Face_SavouringDeliciousFood,   // newPressed


      Face_Assistant,         // pauseAssistant

      /** 👀 - Глаза U+1F440 */
      Eyes_OpenDisabled,      // pauseDisabled

      Eyes_ClosedDisabled,    // pauseDisabledSelected

      Face_EyesOpen,          // pauseNormal

      Face_WinkingEye,        // pausePressed

      Face_EyesClosed         // pauseSelected
   }

   private final double _size;
   private final EType _type;

   public Smile(int size, EType type) {
      _size = size;
      _type = type;
   }

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
      Color yellowBody = new Color(0xFF, 0xCC, 0x00);
      Color yellowGlint = new Color(0xFF, 0xFF, 0x33);
      Color yellowBorder = new Color(0xFF, 0x6C, 0x0A);

      { // рисую затемненный круг
         { // variant 1
            g.setColor(yellowBorder);
            g.fillOval(0, 0, (int)_size, (int)_size);
         }
         //{ // variant 2: рисую не круг, а кольцо
         //   double wh = _size;
         //   Ellipse2D ellipse1 = new Ellipse2D.Double(0, 0, wh, wh);
         //   double pad = _size/30; // offset
         //   wh -= 2*pad;
         //   Ellipse2D ellipse2 = new Ellipse2D.Double(pad, pad, wh, wh);
         //   g.setColor(yellowBorder);
         //   g.fill(intersectExclude(ellipse1, ellipse2));
         //}
      }
      { // поверх него, внутри - градиентный круг
         double pad = _size/30; // offset
         g.setPaint(new GradientPaint(0, 0, yellowBody, (float)_size, (float)_size, yellowBorder));
         g.fill(new Ellipse2D.Double(pad, pad, _size-pad*2, _size-pad*2));
      }
      { // верхний левый блик
         double pad = _size/30; // offset
         double wh = _size-pad*2;
         Ellipse2D ellipse1 = new Ellipse2D.Double(pad, pad, wh, wh);
         wh = 1.13*_size;
         Ellipse2D ellipse2 = new Ellipse2D.Double(pad, pad, wh, wh);
         g.setColor(yellowGlint); // Color.DARK_GRAY
         g.fill(intersectExclude(ellipse1, ellipse2));

         // test
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
         g.setColor(yellowBorder.darker());
         g.fill(intersectExclude(ellipse1, ellipse2));

         // test
         //g.setColor(Color.BLACK);
         //g.draw(ellipse1);
         //g.draw(ellipse2);
      }
   }

   private void drawEyes(Graphics2D g) {
      Stroke strokeOld = g.getStroke();
      switch (_type) {
      case Face_SmilingWithSunglasses: {
            // glasses
            Stroke strokeNew = new BasicStroke((float)Math.max(1, 0.03*_size), BasicStroke.CAP_ROUND, BasicStroke.JOIN_BEVEL);
            g.setStroke(strokeNew);
            g.setColor(Color.BLACK);
            g.draw(new Ellipse2D.Double(0.200*_size, 0.100*_size, 0.290*_size, 0.440*_size));
            g.draw(new Ellipse2D.Double(0.510*_size, 0.100*_size, 0.290*_size, 0.440*_size));
            // дужки
            g.draw(new Line2D.Double(0.746*_size, 0.148*_size, 0.885*_size, 0.055*_size));
            g.draw(new Arc2D.Double(0.864*_size, 0.047*_size, 0.100*_size, 0.100*_size, 0, 125, Arc2D.OPEN));
            g.draw(new Line2D.Double((1-0.746)*_size, 0.148*_size, (1-0.885)*_size, 0.055*_size));
            g.draw(new Arc2D.Double((1-0.864-0.100)*_size, 0.047*_size, 0.100*_size, 0.100*_size, 55, 125, Arc2D.OPEN));
         }
         //break; // ! no break
      case Face_WhiteSmiling:
      case Face_Grinning: {
            g.setColor(Color.BLACK);
            g.fill(new Ellipse2D.Double(0.270*_size, 0.170*_size, 0.150*_size, 0.300*_size));
            g.fill(new Ellipse2D.Double(0.580*_size, 0.170*_size, 0.150*_size, 0.300*_size));
         }
         break;
      case Face_Disappointed: {
            Stroke strokeNew = new BasicStroke((float)Math.max(1, 0.02*_size), BasicStroke.CAP_ROUND, BasicStroke.JOIN_BEVEL);
            g.setStroke(strokeNew);

            Rectangle2D rcHalfLeft  = new Rectangle2D.Double(0, 0, _size/2.0, _size);
            Rectangle2D rcHalfRght = new Rectangle2D.Double(_size/2.0, 0, _size, _size);

            // глаз/eye
            Area areaLeft1 = intersectExclude(new Ellipse2D.Double(0.417*_size, 0.050*_size, 0.384*_size, 0.400*_size), rcHalfLeft);
            Area areaRght1 = intersectExclude(new Ellipse2D.Double(0.205*_size, 0.050*_size, 0.384*_size, 0.400*_size), rcHalfRght);
            g.setColor(Color.RED);
            g.fill(areaLeft1);
            g.fill(areaRght1);
            g.setColor(Color.BLACK);
            g.draw(areaLeft1);
            g.draw(areaRght1);

            // зрачок/pupil
            Area areaLeft2 = intersectExclude(new Ellipse2D.Double(0.550*_size, 0.200*_size, 0.172*_size, 0.180*_size), rcHalfLeft);
            Area areaRght2 = intersectExclude(new Ellipse2D.Double(0.282*_size, 0.200*_size, 0.172*_size, 0.180*_size), rcHalfRght);
            g.setColor(Color.BLUE);
            g.fill(areaLeft2);
            g.fill(areaRght2);
            g.setColor(Color.BLACK);
            g.draw(areaLeft2);
            g.draw(areaRght2);

            // веко/eyelid
            Area areaLeft3 = intersectExclude(rotate(new Ellipse2D.Double(0.441*_size, -0.236*_size, 0.436*_size, 0.560*_size),
                                                     new PointDouble     (0.441*_size, -0.236*_size), 30), rcHalfLeft);
            Area areaRght3 = intersectExclude(rotate(new Ellipse2D.Double(0.128*_size, -0.236*_size, 0.436*_size, 0.560*_size),
                                                     new PointDouble     (0.564*_size, -0.236*_size), -30), rcHalfRght);
            areaLeft3 = intersect(areaLeft1, areaLeft3);
            areaRght3 = intersect(areaRght1, areaRght3);
            g.setColor(Color.GREEN);
            g.fill(areaLeft3);
            g.fill(areaRght3);
            g.setColor(Color.BLACK);
            g.draw(areaLeft3);
            g.draw(areaRght3);

            // nose
            Ellipse2D nose = new Ellipse2D.Double(0.415*_size, 0.400*_size, 0.170*_size, 0.170*_size);
            g.setColor(Color.GREEN);
            g.fill(nose);
            g.setColor(Color.BLACK);
            g.draw(nose);
         }
         break;
      default:
         throw new UnsupportedOperationException("Not implemented");
      }
      g.setStroke(strokeOld);
   }

   private void drawMouth(Graphics2D g) {
      Stroke strokeOld = g.getStroke();
      Stroke strokeNew = new BasicStroke((float)Math.max(1, 0.044*_size), BasicStroke.CAP_ROUND, BasicStroke.JOIN_BEVEL);
      g.setStroke(strokeNew);
      g.setColor(Color.BLACK);

      switch (_type) {
      case Face_SmilingWithSunglasses:
      case Face_WhiteSmiling: {
            // smile
            Arc2D arcSmile = new Arc2D.Double(0.103*_size, -0.133*_size, 0.795*_size, 1.003*_size, 207, 126, Arc2D.OPEN);
            g.draw(arcSmile);
            Ellipse2D ellipse = new Ellipse2D.Double(0.060*_size, 0.475*_size, 0.877*_size, 0.330*_size);
            g.fill(intersectExclude(arcSmile, ellipse));

            // test
            //g.setStroke(strokeOld);
            //g.setColor(Color.GREEN);
            //g.draw(ellipse);

            // dimples - ямочки на щеках
            g.setStroke(strokeNew);
            g.setColor(Color.BLACK);
            g.draw(new Arc2D.Double(+0.020*_size, 0.420*_size, 0.180*_size, 0.180*_size, 85+180, 57, Arc2D.OPEN));
            g.draw(new Arc2D.Double(+0.800*_size, 0.420*_size, 0.180*_size, 0.180*_size, 38+180, 57, Arc2D.OPEN));
         }
         break;
      case Face_Disappointed: {
            // smile
            Arc2D arcSmile = new Arc2D.Double(0.025*_size, 0.655*_size, 0.950*_size, 0.950*_size, 50, 80, Arc2D.OPEN);
            g.draw(arcSmile);
            arcSmile.setAngleStart(0); arcSmile.setAngleExtent(360); // arc as circle

            // tongue / язык
            Area area = intersectInclude(new Ellipse2D.Double(0.338*_size, 0.637*_size, 0.325*_size, 0.325*_size),    // кончик языка
                                         new Rectangle2D.Double(0.338*_size, 0.594*_size, 0.325*_size, 0.206*_size)); // тело языка
            Area hole = intersectExclude(new Rectangle2D.Double(0, 0, _size, _size), arcSmile);
            area = intersectExclude(area, hole);
            g.setColor(Color.RED);
            g.fill(area);
            g.setColor(Color.BLACK);
            g.draw(area);
          //g.draw(intersectExclude(new Line2D.Double(_size/2.0, 0.637*_size, _size/2.0, 0.800*_size), hole)); // don't working
            g.draw(intersectExclude(new Rectangle2D.Double(_size/2.0, 0.637*_size, 0.0001, 0.200*_size), hole)); // its works

            // test
            //g.setStroke(new BasicStroke(1, BasicStroke.CAP_ROUND, BasicStroke.JOIN_BEVEL));
            //g.draw(arcSmile);
            //g.draw(hole);
         }
         break;
      case Face_Grinning: {
            Arc2D arcSmile = new Arc2D.Double(0.103*_size, -0.133*_size, 0.795*_size, 1.003*_size, 207, 126, Arc2D.CHORD);
            Paint paintOld = g.getPaint();
            g.setPaint(new GradientPaint(0, 0, Color.GRAY, (float)(_size/2.0), 0, Color.WHITE));
//            g.fill(new Rectangle2D.Double(0, 0, _size, _size)); // test
            g.fill(arcSmile);
            g.setPaint(paintOld);
            g.setStroke(strokeNew);
            g.draw(arcSmile);
         }
         break;
      default:
         throw new UnsupportedOperationException("Not implemented");
      }

      g.setStroke(strokeOld);
   }

   private static Shape rotate(Shape shape, PointDouble rotatePoint, double angle) {
      AffineTransform tx = new AffineTransform();
      tx.rotate(Math.toRadians(angle), rotatePoint.x, rotatePoint.y);
      GeneralPath path = new GeneralPath();
      path.append(tx.createTransformedShape(shape), false);
      return path;
   }

   private static Area intersect(Shape s1, Shape s2) {
      Area outside = new Area(s1);
      outside.intersect(new Area(s2));
      return outside;
    }

   private static Area intersectExclude(Shape s1, Shape s2) {
      Area outside = new Area(s1);
      outside.subtract(new Area(s2));
      return outside;
   }

   private static Area intersectInclude(Shape s1, Shape s2) {
      Area outside = new Area(s1);
      outside.add(new Area(s2));
      return outside;
   }

   // test
   public static void main(String[] args) {
//      TestDrawing.testApp2(size -> ImgUtils.zoom(new Smile(size, EType.Face_SmilingWithSunglasses), 24, 24));
//      TestDrawing.testApp2(size -> new Smile(size, EType.Face_SmilingWithSunglasses));

      TestDrawing.testApp2(size -> new Icon(){
         @Override
         public void paintIcon(Component c, Graphics g, int x, int y) {
            int valCnt = EType.values().length;
            int maxI = (int)Math.round(Math.sqrt(valCnt) + 0.5);
            int maxJ = (int)Math.round(Math.sqrt(valCnt) - 0.5);
            int dx = size / maxI;
            int dy = size / maxJ;

            int icoPadding = 5;
            int wIco = Math.min(dx, dy) - 2*icoPadding;
            int hIco = Math.min(dx, dy) - 2*icoPadding;
            for (int i=0; i<maxI; ++i)
               for (int j=0; j<maxJ; ++j) {
                  int pos = maxI*j+i;
                  if (pos >= valCnt)
                     break;
                  try {
                     Icon ico = ImgUtils.zoom(new Smile(size, EType.values()[pos]), wIco, hIco);
                     ico.paintIcon(c, g, x+i*dx+icoPadding, y+j*dy+icoPadding);
                  } catch (UnsupportedOperationException ex) {
                     continue;
                  }
               }
         }
         @Override
         public int getIconWidth() { return size; }
         @Override
         public int getIconHeight() { return size; }
      });
/**/
   }

}
