package fmg.swing.draw.img;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Component;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.geom.AffineTransform;
import java.awt.geom.Arc2D;
import java.awt.geom.Area;
import java.awt.geom.Ellipse2D;
import java.awt.geom.GeneralPath;
import java.awt.geom.Line2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.util.function.Consumer;

import javax.swing.Icon;

import fmg.common.geom.PointDouble;
import fmg.common.geom.Size;
import fmg.swing.utils.ImgUtils;

public class Smile implements Icon {

   /** @see http://unicode-table.com/blocks/emoticons/
    * <br>  http://unicode-table.com/blocks/miscellaneous-symbols-and-pictographs/
   */
   public enum EType {
      /** :) ☺ -  White Smiling Face (Незакрашенное улыбающееся лицо) U+263A */
      Face_WhiteSmiling,

      /** :( 😞 - Disappointed Face (Разочарованное лицо) U+1F61E */
      Face_Disappointed,

      /** 😀 - Grinning Face (Ухмыляющееся лицо) U+1F600 */
      Face_Grinning,

      /** 😎 - Smiling Face with Sunglasses (Улыбающееся лицо в солнечных очках) U+1F60E */
      Face_SmilingWithSunglasses,

      /** 😋 - Face Savouring Delicious Food (Лицо, смакующее деликатес) U+1F60B */
      Face_SavouringDeliciousFood,


      /** like as Professor: 🎓 - Graduation Cap (Выпускная шапочка) U+1F393 */
      Face_Assistant,

      /** 👀 - Eyes (Глаза) U+1F440 */
      Eyes_OpenDisabled,

      Eyes_ClosedDisabled,

      Face_EyesOpen,

      Face_WinkingEyeLeft,
      Face_WinkingEyeRight,

      Face_EyesClosed
   }

   private final Size _size;
   private final EType _type;

   public Smile(Size size, EType type) {
      _size = size;
      _type = type;
   }

   @Override
   public int getIconWidth() { return _size.width; }
   @Override
   public int getIconHeight() { return _size.height; }

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
      if (_type == EType.Eyes_OpenDisabled || _type == EType.Eyes_ClosedDisabled)
         return;

      Color yellowBody = new Color(0xFF, 0xCC, 0x00);
      Color yellowGlint = new Color(0xFF, 0xFF, 0x33);
      Color yellowBorder = new Color(0xFF, 0x6C, 0x0A);

      { // рисую затемненный круг
         g.setColor(yellowBorder);
         g.fillOval(0, 0, _size.width, _size.height);
      }
      { // поверх него, внутри - градиентный круг
         double padX = _size.width/30.0; // offset
         double padY = _size.height/30.0; // offset
         g.setPaint(new GradientPaint(0, 0, yellowBody, _size.width, _size.height, yellowBorder));
         g.fill(new Ellipse2D.Double(padX, padY, _size.width-padX*2, _size.height-padY*2));
      }
      { // верхний левый блик
         double padX = _size.width/30.0; // offset
         double padY = _size.height/30.0; // offset
         double w = _size.width-padX*2;
         double h = _size.height-padY*2;
         Ellipse2D ellipse1 = new Ellipse2D.Double(padX, padY, w, h);
         w = 1.13*_size.width;
         h = 1.13*_size.height;
         Ellipse2D ellipse2 = new Ellipse2D.Double(padX, padY, w, h);
         g.setColor(yellowGlint); // Color.DARK_GRAY
         g.fill(intersectExclude(ellipse1, ellipse2));

         // test
         //g.setColor(Color.BLACK);
         //g.draw(ellipse1);
         //g.draw(ellipse2);
      }
      { // нижний правый блик
         double padX = _size.width/30.0; // offset
         double padY = _size.height/30.0; // offset
         double w1 = _size.width-padX*2;
         double h1 = _size.height-padY*2;
         Ellipse2D ellipse1 = new Ellipse2D.Double(padX, padY, w1, h1);
         double w2 = 1.13*_size.width;
         double h2 = 1.13*_size.height;
         Ellipse2D ellipse2 = new Ellipse2D.Double(padX+w1-w2, padY+h1-h2, w2, h2);
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
      case Face_Assistant:
      case Face_SmilingWithSunglasses: {
            // glasses
            Stroke strokeNew = new BasicStroke((float)Math.max(1, 0.03*((_size.width+_size.height)/2.0)), BasicStroke.CAP_ROUND, BasicStroke.JOIN_BEVEL);
            g.setStroke(strokeNew);
            g.setColor(Color.BLACK);
            g.draw(new Ellipse2D.Double(0.200*_size.width, 0.100*_size.height, 0.290*_size.width, 0.440*_size.height));
            g.draw(new Ellipse2D.Double(0.510*_size.width, 0.100*_size.height, 0.290*_size.width, 0.440*_size.height));
            // дужки
            g.draw(new Line2D.Double(0.746*_size.width, 0.148*_size.height, 0.885*_size.width, 0.055*_size.height));
            g.draw(new  Arc2D.Double(0.864*_size.width, 0.047*_size.height, 0.100*_size.width, 0.100*_size.height, 0, 125, Arc2D.OPEN));
            g.draw(new Line2D.Double((1-0.746)*_size.width, 0.148*_size.height, (1-0.885)*_size.width, 0.055*_size.height));
            g.draw(new  Arc2D.Double((1-0.864-0.100)*_size.width, 0.047*_size.height, 0.100*_size.width, 0.100*_size.height, 55, 125, Arc2D.OPEN));
         }
         //break; // ! no break
      case Face_SavouringDeliciousFood:
      case Face_WhiteSmiling:
      case Face_Grinning: {
            g.setColor(Color.BLACK);
            g.fill(new Ellipse2D.Double(0.270*_size.width, 0.170*_size.height, 0.150*_size.width, 0.300*_size.height));
            g.fill(new Ellipse2D.Double(0.580*_size.width, 0.170*_size.height, 0.150*_size.width, 0.300*_size.height));
         }
         break;
      case Face_Disappointed: {
            Stroke strokeNew = new BasicStroke((float)Math.max(1, 0.02*((_size.width+_size.height)/2.0)), BasicStroke.CAP_ROUND, BasicStroke.JOIN_BEVEL);
            g.setStroke(strokeNew);

            Rectangle2D rcHalfLeft  = new Rectangle2D.Double(0, 0, _size.width/2.0, _size.height);
            Rectangle2D rcHalfRght = new Rectangle2D.Double(_size.width/2.0, 0, _size.width, _size.height);

            // глаз/eye
            Area areaLeft1 = intersectExclude(new Ellipse2D.Double(0.417*_size.width, 0.050*_size.height, 0.384*_size.width, 0.400*_size.height), rcHalfLeft);
            Area areaRght1 = intersectExclude(new Ellipse2D.Double(0.205*_size.width, 0.050*_size.height, 0.384*_size.width, 0.400*_size.height), rcHalfRght);
            g.setColor(Color.RED);
            g.fill(areaLeft1);
            g.fill(areaRght1);
            g.setColor(Color.BLACK);
            g.draw(areaLeft1);
            g.draw(areaRght1);

            // зрачок/pupil
            Area areaLeft2 = intersectExclude(new Ellipse2D.Double(0.550*_size.width, 0.200*_size.height, 0.172*_size.width, 0.180*_size.height), rcHalfLeft);
            Area areaRght2 = intersectExclude(new Ellipse2D.Double(0.282*_size.width, 0.200*_size.height, 0.172*_size.width, 0.180*_size.height), rcHalfRght);
            g.setColor(Color.BLUE);
            g.fill(areaLeft2);
            g.fill(areaRght2);
            g.setColor(Color.BLACK);
            g.draw(areaLeft2);
            g.draw(areaRght2);

            // веко/eyelid
            Area areaLeft3 = intersectExclude(rotate(new Ellipse2D.Double(0.441*_size.width, -0.236*_size.height, 0.436*_size.width, 0.560*_size.height),
                                                     new PointDouble     (0.441*_size.width, -0.236*_size.height), 30), rcHalfLeft);
            Area areaRght3 = intersectExclude(rotate(new Ellipse2D.Double(0.128*_size.width, -0.236*_size.height, 0.436*_size.width, 0.560*_size.height),
                                                     new PointDouble     (0.564*_size.width, -0.236*_size.height), -30), rcHalfRght);
            areaLeft3 = intersect(areaLeft1, areaLeft3);
            areaRght3 = intersect(areaRght1, areaRght3);
            g.setColor(Color.GREEN);
            g.fill(areaLeft3);
            g.fill(areaRght3);
            g.setColor(Color.BLACK);
            g.draw(areaLeft3);
            g.draw(areaRght3);

            // nose
            Ellipse2D nose = new Ellipse2D.Double(0.415*_size.width, 0.400*_size.height, 0.170*_size.width, 0.170*_size.height);
            g.setColor(Color.GREEN);
            g.fill(nose);
            g.setColor(Color.BLACK);
            g.draw(nose);
         }
         break;
      case Eyes_OpenDisabled:
         eyeOpened(g, true, true);
         eyeOpened(g, false, true);
         break;
      case Eyes_ClosedDisabled:
         eyeClosed(g, true, true);
         eyeClosed(g, false, true);
         break;
      case Face_EyesOpen:
         eyeOpened(g, true, false);
         eyeOpened(g, false, false);
         break;
      case Face_WinkingEyeLeft:
         eyeClosed(g, true, false);
         eyeOpened(g, false, false);
         break;
      case Face_WinkingEyeRight:
         eyeOpened(g, true, false);
         eyeClosed(g, false, false);
         break;
      case Face_EyesClosed:
         eyeClosed(g, true, false);
         eyeClosed(g, false, false);
         break;
      default:
         throw new UnsupportedOperationException("Not implemented");
      }
      g.setStroke(strokeOld);
   }

   private void drawMouth(Graphics2D g) {
      switch (_type) {
      case Face_Assistant:
      case Eyes_OpenDisabled:
      case Eyes_ClosedDisabled:
      case Face_EyesOpen:
      case Face_WinkingEyeLeft:
      case Face_WinkingEyeRight:
      case Face_EyesClosed:
         return;
      default:
      }

      Stroke strokeOld = g.getStroke();
      Stroke strokeNew = new BasicStroke((float)Math.max(1, 0.044*((_size.width+_size.height)/2.0)), BasicStroke.CAP_ROUND, BasicStroke.JOIN_BEVEL);
      g.setStroke(strokeNew);
      g.setColor(Color.BLACK);

      switch (_type) {
      case Face_SavouringDeliciousFood:
      case Face_SmilingWithSunglasses:
      case Face_WhiteSmiling: {
            // smile
            Arc2D arcSmile = new Arc2D.Double(0.103*_size.width, -0.133*_size.height, 0.795*_size.width, 1.003*_size.height, 207, 126, Arc2D.OPEN);
            g.draw(arcSmile);
            Ellipse2D lip = new Ellipse2D.Double(0.060*_size.width, 0.475*_size.height, 0.877*_size.width, 0.330*_size.height);
            g.fill(intersectExclude(arcSmile, lip));

            // test
            //g.setStroke(strokeOld);
            //g.setColor(Color.GREEN);
            //g.draw(ellipse);

            // dimples - ямочки на щеках
            g.setStroke(strokeNew);
            g.setColor(Color.BLACK);
            g.draw(new Arc2D.Double(+0.020*_size.width, 0.420*_size.height, 0.180*_size.width, 0.180*_size.height, 85+180, 57, Arc2D.OPEN));
            g.draw(new Arc2D.Double(+0.800*_size.width, 0.420*_size.height, 0.180*_size.width, 0.180*_size.height, 38+180, 57, Arc2D.OPEN));

            // tongue / язык
            if (_type == EType.Face_SavouringDeliciousFood) {
               Shape tongue = rotate(new Ellipse2D.Double(0.470*_size.width, 0.406*_size.height, 0.281*_size.width, 0.628*_size.height),
                                     new      PointDouble(0.470*_size.width, 0.406*_size.height), 40);
               g.setColor(Color.RED);
               Ellipse2D ellipseSmile = new Ellipse2D.Double(0.103*_size.width, -0.133*_size.height, 0.795*_size.width, 1.003*_size.height);
               g.fill(intersectExclude(tongue, ellipseSmile));
            }
         }
         break;
      case Face_Disappointed: {
            // smile
            Arc2D arcSmile = new Arc2D.Double(0.025*_size.width, 0.655*_size.height, 0.950*_size.width, 0.950*_size.height, 50, 80, Arc2D.OPEN);
            g.draw(arcSmile);
            arcSmile.setAngleStart(0); arcSmile.setAngleExtent(360); // arc as circle

            // tongue / язык
            Area tongue = intersectInclude(new   Ellipse2D.Double(0.338*_size.width, 0.637*_size.height, 0.325*_size.width, 0.325*_size.height),  // кончик языка
                                           new Rectangle2D.Double(0.338*_size.width, 0.594*_size.height, 0.325*_size.width, 0.206*_size.height)); // тело языка
            Area hole = intersectExclude(new Rectangle2D.Double(0, 0, _size.width, _size.height), arcSmile);
            tongue = intersectExclude(tongue, hole);
            g.setColor(Color.RED);
            g.fill(tongue);
            g.setColor(Color.BLACK);
            g.draw(tongue);
          //g.draw(intersectExclude(new Line2D.Double(_size.width/2.0, 0.637*_size.height, _size.width/2.0, 0.800*_size.height), hole)); // don't working
            g.draw(intersectExclude(new Rectangle2D.Double(_size.width/2.0, 0.637*_size.height, 0.0001, 0.200*_size.height), hole)); // its works

            // test
            //g.setStroke(new BasicStroke(1, BasicStroke.CAP_ROUND, BasicStroke.JOIN_BEVEL));
            //g.draw(arcSmile);
            //g.draw(hole);
         }
         break;
      case Face_Grinning: {
            Arc2D arcSmile = new Arc2D.Double(0.103*_size.width, -0.133*_size.height, 0.795*_size.width, 1.003*_size.height, 207, 126, Arc2D.CHORD);
            Paint paintOld = g.getPaint();
            g.setPaint(new GradientPaint(0, 0, Color.GRAY, (float)(_size.width/2.0), 0, Color.WHITE));
//            g.fill(new Rectangle2D.Double(0, 0, _size.width, _size.height)); // test
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

   private void eyeOpened(Graphics2D g, boolean right, boolean disabled) {
      Consumer<PointDouble> draw = offset -> {
         Area pupil = right
               ? intersectInclude(intersectInclude(
                          new Ellipse2D.Double((offset.x+0.273)*_size.width, (offset.y+0.166)*_size.height, 0.180*_size.width, 0.324*_size.height),
                   rotate(new Ellipse2D.Double((offset.x+0.320)*_size.width, (offset.y+0.124)*_size.height, 0.180*_size.width, 0.273*_size.height),
                          new      PointDouble((offset.x+0.320)*_size.width, (offset.y+0.124)*_size.height), 35)),
                   rotate(new Ellipse2D.Double((offset.x+0.163)*_size.width, (offset.y+0.313)*_size.height, 0.180*_size.width, 0.266*_size.height),
                          new      PointDouble((offset.x+0.163)*_size.width, (offset.y+0.313)*_size.height), -36))
               : intersectInclude(intersectInclude(
                          new Ellipse2D.Double((offset.x+0.500)*_size.width, (offset.y+0.166)*_size.height, 0.180*_size.width, 0.324*_size.height),
                   rotate(new Ellipse2D.Double((offset.x+0.486)*_size.width, (offset.y+0.227)*_size.height, 0.180*_size.width, 0.273*_size.height),
                          new      PointDouble((offset.x+0.486)*_size.width, (offset.y+0.227)*_size.height), -35)),
                   rotate(new Ellipse2D.Double((offset.x+0.646)*_size.width, (offset.y+0.211)*_size.height, 0.180*_size.width, 0.266*_size.height),
                          new      PointDouble((offset.x+0.646)*_size.width, (offset.y+0.211)*_size.height), 36));
         if (!disabled) {
            g.setColor(Color.BLACK);
            g.fill(pupil);
         }
         Shape hole = right
               ? rotate(new Ellipse2D.Double((offset.x+0.303*_size.width), (offset.y+0.209)*_size.height, 0.120*_size.width, 0.160*_size.height),
                        new      PointDouble((offset.x+0.303*_size.width), (offset.y+0.209)*_size.height), 25)
               : rotate(new Ellipse2D.Double((offset.x+0.610*_size.width), (offset.y+0.209)*_size.height, 0.120*_size.width, 0.160*_size.height),
                        new      PointDouble((offset.x+0.610*_size.width), (offset.y+0.209)*_size.height), 25);
         if (!disabled) {
            g.setColor(Color.WHITE);
            g.fill(hole);
         } else {
            g.fill(intersectExclude(pupil, hole));
         }
      };
      if (disabled) {
         g.setColor(Color.WHITE);
         draw.accept(new PointDouble(0.034, 0.027));
         g.setColor(Color.GRAY);
         draw.accept(new PointDouble());
      } else {
         draw.accept(new PointDouble());
      }
   }

   private void eyeClosed(Graphics2D g, boolean right, boolean disabled) {
      Consumer<PointDouble> eye = offset -> {
         if (disabled) {
            g.setColor(Color.WHITE);
            g.fill(intersectInclude(
                       new Ellipse2D.Double((offset.x+0.532)*_size.width, (offset.y+0.248)*_size.height, 0.313*_size.width, 0.068*_size.height),
                       new Ellipse2D.Double((offset.x+0.655)*_size.width, (offset.y+0.246)*_size.height, 0.205*_size.width, 0.130*_size.height)));
         }
         g.setColor(disabled ? Color.GRAY : Color.BLACK);
         g.fill(intersectInclude(
                    new Ellipse2D.Double((offset.x+0.517)*_size.width, (offset.y+0.248)*_size.height, 0.313*_size.width, 0.034*_size.height),
                    new Ellipse2D.Double((offset.x+0.640)*_size.width, (offset.y+0.246)*_size.height, 0.205*_size.width, 0.075*_size.height)));
      };
      eye.accept(right
                 ? new PointDouble(-0.410, 0)
                 : new PointDouble());
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
//      TestDrawing.testApp2(size -> ImgUtils.zoom(new Smile(new Size(size, size), EType.Face_SmilingWithSunglasses), 24, 24));
//      TestDrawing.testApp2(size -> new Smile(new Size(size, size), EType.Face_SavouringDeliciousFood));

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
                     Icon ico = ImgUtils.zoom(new Smile(new Size(size, size), EType.values()[pos]), wIco, hIco);
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
