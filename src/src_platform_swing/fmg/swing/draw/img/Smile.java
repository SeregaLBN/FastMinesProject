package fmg.swing.draw.img;

import java.awt.*;
import java.awt.geom.*;
import java.util.Arrays;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import fmg.common.geom.PointDouble;
import fmg.core.img.ImageController;
import fmg.core.img.ImageView;
import fmg.core.img.SmileModel;
import fmg.core.img.SmileModel.EFaceType;
import fmg.swing.Cast;

public abstract class Smile<TImage> extends ImageView<TImage, SmileModel> {

   protected Smile(EFaceType faceType) {
      super(new SmileModel(faceType));
   }

   static {
      StaticInitilizer.init();
   }

   protected void draw(Graphics2D g) {
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
      SmileModel sm = this.getModel();
      SmileModel.EFaceType type = sm.getFaceType();
      int width = sm.getSize().width;
      int height = sm.getSize().height;

      if (type == EFaceType.Eyes_OpenDisabled || type == EFaceType.Eyes_ClosedDisabled)
         return;

      Color yellowBody = new Color(0xFF, 0xCC, 0x00);
      Color yellowGlint = new Color(0xFF, 0xFF, 0x33);
      Color yellowBorder = new Color(0xFF, 0x6C, 0x0A);

      { // рисую затемненный круг
         g.setColor(yellowBorder);
         g.fillOval(0, 0, width, height);
      }

      double padX = 0.033 * width;
      double padY = 0.033 * height;
      double wInt = width - 2 * padX;
      double hInt = height - 2 * padY;
      double wExt = 1.133 * width;
      double hExt = 1.133 * height;
      Ellipse2D ellipseInternal = new Ellipse2D.Double(padX, padY, width-padX*2, height-padY*2);
      { // поверх него, внутри - градиентный круг
         g.setPaint(new GradientPaint(0, 0, yellowBody, width, height, yellowBorder));
         g.fill(ellipseInternal);
      }
      { // верхний левый блик
         Ellipse2D ellipseExternal = new Ellipse2D.Double(padX, padY, wExt, hExt);
         g.setColor(yellowGlint); // Color.DARK_GRAY
         g.fill(intersectExclude(ellipseInternal, ellipseExternal));

         // test
         //g.setColor(Color.BLACK);
         //g.draw(ellipseInternal);
         //g.draw(ellipseExternal);
      }
      { // нижний правый блик
         Ellipse2D ellipseExternal = new Ellipse2D.Double(padX + wInt - wExt, padY + hInt - hExt, wExt, hExt);
         g.setColor(Cast.toColor(Cast.toColor(yellowBorder).darker(0.4)));
         g.fill(intersectExclude(ellipseInternal, ellipseExternal));

         // test
         //g.setColor(Color.BLACK);
         //g.draw(ellipseInternal);
         //g.draw(ellipseExternal);
      }
   }

   private void drawEyes(Graphics2D g) {
      SmileModel sm = this.getModel();
      SmileModel.EFaceType type = sm.getFaceType();
      int width = sm.getSize().width;
      int height = sm.getSize().height;

      Stroke strokeOld = g.getStroke();
      switch (type) {
      case Face_Assistant:
      case Face_SmilingWithSunglasses: {
            // glasses
            Stroke strokeNew = new BasicStroke((float)Math.max(1, 0.03*((width+height)/2.0)), BasicStroke.CAP_ROUND, BasicStroke.JOIN_BEVEL);
            g.setStroke(strokeNew);
            g.setColor(Color.BLACK);
            g.draw(new Ellipse2D.Double(0.200*width, 0.100*height, 0.290*width, 0.440*height));
            g.draw(new Ellipse2D.Double(0.510*width, 0.100*height, 0.290*width, 0.440*height));
            // дужки
            g.draw(new Line2D.Double(   0.746 *width, 0.148*height,    0.885 *width, 0.055*height));
            g.draw(new Line2D.Double((1-0.746)*width, 0.148*height, (1-0.885)*width, 0.055*height));
            g.draw(new  Arc2D.Double(   0.864       *width, 0.047*height, 0.100*width, 0.100*height,  0, 125, Arc2D.OPEN));
            g.draw(new  Arc2D.Double((1-0.864-0.100)*width, 0.047*height, 0.100*width, 0.100*height, 55, 125, Arc2D.OPEN));
         }
         //break; // ! no break
      case Face_SavouringDeliciousFood:
      case Face_WhiteSmiling:
      case Face_Grinning: {
            g.setColor(Color.BLACK);
            g.fill(new Ellipse2D.Double(0.270*width, 0.170*height, 0.150*width, 0.300*height));
            g.fill(new Ellipse2D.Double(0.580*width, 0.170*height, 0.150*width, 0.300*height));
         }
         break;
      case Face_Disappointed: {
            Stroke strokeNew = new BasicStroke((float)Math.max(1, 0.02*((width+height)/2.0)), BasicStroke.CAP_ROUND, BasicStroke.JOIN_BEVEL);
            g.setStroke(strokeNew);

            Rectangle2D rcHalfLeft = new Rectangle2D.Double(0, 0, width/2.0, height);
            Rectangle2D rcHalfRght = new Rectangle2D.Double(width/2.0, 0, width, height);

            // глаз/eye
            Area areaLeft1 = intersectExclude(new Ellipse2D.Double(0.417*width, 0.050*height, 0.384*width, 0.400*height), rcHalfLeft);
            Area areaRght1 = intersectExclude(new Ellipse2D.Double(0.205*width, 0.050*height, 0.384*width, 0.400*height), rcHalfRght);
            g.setColor(Color.RED);
            g.fill(areaLeft1);
            g.fill(areaRght1);
            g.setColor(Color.BLACK);
            g.draw(areaLeft1);
            g.draw(areaRght1);

            // зрачок/pupil
            Area areaLeft2 = intersectExclude(new Ellipse2D.Double(0.550*width, 0.200*height, 0.172*width, 0.180*height), rcHalfLeft);
            Area areaRght2 = intersectExclude(new Ellipse2D.Double(0.282*width, 0.200*height, 0.172*width, 0.180*height), rcHalfRght);
            g.setColor(Color.BLUE);
            g.fill(areaLeft2);
            g.fill(areaRght2);
            g.setColor(Color.BLACK);
            g.draw(areaLeft2);
            g.draw(areaRght2);

            // веко/eyelid
            Area areaLeft3 = intersectExclude(rotate(new Ellipse2D.Double(0.441*width, -0.236*height, 0.436*width, 0.560*height),
                                                     new PointDouble     (0.441*width, -0.236*height), 30), rcHalfLeft);
            Area areaRght3 = intersectExclude(rotate(new Ellipse2D.Double(0.128*width, -0.236*height, 0.436*width, 0.560*height),
                                                     new PointDouble     (0.564*width, -0.236*height), -30), rcHalfRght);
            areaLeft3 = intersect(areaLeft1, areaLeft3);
            areaRght3 = intersect(areaRght1, areaRght3);
            g.setColor(Color.GREEN);
            g.fill(areaLeft3);
            g.fill(areaRght3);
            g.setColor(Color.BLACK);
            g.draw(areaLeft3);
            g.draw(areaRght3);

            // nose
            Ellipse2D nose = new Ellipse2D.Double(0.415*width, 0.400*height, 0.170*width, 0.170*height);
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
      SmileModel sm = this.getModel();
      SmileModel.EFaceType type = sm.getFaceType();
      int width = sm.getSize().width;
      int height = sm.getSize().height;

      switch (type) {
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
      Stroke strokeNew = new BasicStroke((float)Math.max(1, 0.044*((width+height)/2.0)), BasicStroke.CAP_ROUND, BasicStroke.JOIN_BEVEL);
      g.setStroke(strokeNew);
      g.setColor(Color.BLACK);

      switch (type) {
      case Face_SavouringDeliciousFood:
      case Face_SmilingWithSunglasses:
      case Face_WhiteSmiling: {
            // smile
            Arc2D arcSmile = new Arc2D.Double(0.103*width, -0.133*height, 0.795*width, 1.003*height, 207, 126, Arc2D.OPEN);
            g.draw(arcSmile);
            Ellipse2D lip = new Ellipse2D.Double(0.060*width, 0.475*height, 0.877*width, 0.330*height);
            g.fill(intersectExclude(arcSmile, lip));

            // test
            //g.setStroke(strokeOld);
            //g.setColor(Color.GREEN);
            //g.draw(lip);

            // dimples - ямочки на щеках
            g.setStroke(strokeNew);
            g.setColor(Color.BLACK);
            g.draw(new Arc2D.Double(+0.020*width, 0.420*height, 0.180*width, 0.180*height, 85+180, 57, Arc2D.OPEN));
            g.draw(new Arc2D.Double(+0.800*width, 0.420*height, 0.180*width, 0.180*height, 38+180, 57, Arc2D.OPEN));

            // tongue / язык
            if (type == EFaceType.Face_SavouringDeliciousFood) {
               Shape tongue = rotate(new Ellipse2D.Double(0.470*width, 0.406*height, 0.281*width, 0.628*height),
                                     new      PointDouble(0.470*width, 0.406*height), 40);
               g.setColor(Color.RED);
               Ellipse2D ellipseSmile = new Ellipse2D.Double(0.103*width, -0.133*height, 0.795*width, 1.003*height);
               g.fill(intersectExclude(tongue, ellipseSmile));
            }
         }
         break;
      case Face_Disappointed: {
            // smile
            Arc2D arcSmile = new Arc2D.Double(0.025*width, 0.655*height, 0.950*width, 0.950*height, 50, 80, Arc2D.OPEN);
            g.draw(arcSmile);
            arcSmile.setAngleStart(0); arcSmile.setAngleExtent(360); // arc as ellipse

            // tongue / язык
            Area tongue = intersectInclude(new   Ellipse2D.Double(0.338*width, 0.637*height, 0.325*width, 0.325*height),  // кончик языка
                                           new Rectangle2D.Double(0.338*width, 0.594*height, 0.325*width, 0.206*height)); // тело языка
            Area hole = intersectExclude(new Rectangle2D.Double(0, 0, width, height), arcSmile);
            tongue = intersectExclude(tongue, hole);
            g.setColor(Color.RED);
            g.fill(tongue);
            g.setColor(Color.BLACK);
            g.draw(tongue);
          //g.draw(intersectExclude(new Line2D.Double(width/2.0, 0.637*height, width/2.0, 0.800*height), hole)); // don't working
            g.draw(intersectExclude(new Rectangle2D.Double(width/2.0, 0.637*height, 0.0001, 0.200*height), hole)); // its works

            // test
            //g.setStroke(new BasicStroke(1, BasicStroke.CAP_ROUND, BasicStroke.JOIN_BEVEL));
            //g.draw(arcSmile);
            //g.draw(hole);
         }
         break;
      case Face_Grinning: {
            Arc2D arcSmile = new Arc2D.Double(0.103*width, -0.133*height, 0.795*width, 1.003*height, 207, 126, Arc2D.CHORD);
            Paint paintOld = g.getPaint();
            g.setPaint(new GradientPaint(0, 0, Color.GRAY, (float)(width/2.0), 0, Color.WHITE));
          //g.fill(new Rectangle2D.Double(0, 0, width, height)); // test
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
      SmileModel sm = this.getModel();
      int width = sm.getSize().width;
      int height = sm.getSize().height;

      Consumer<PointDouble> draw = offset -> {
         Area pupil = right
               ? intersectInclude(intersectInclude(
                          new Ellipse2D.Double((offset.x+0.273)*width, (offset.y+0.166)*height, 0.180*width, 0.324*height),
                   rotate(new Ellipse2D.Double((offset.x+0.320)*width, (offset.y+0.124)*height, 0.180*width, 0.273*height),
                          new      PointDouble((offset.x+0.320)*width, (offset.y+0.124)*height), 35)),
                   rotate(new Ellipse2D.Double((offset.x+0.163)*width, (offset.y+0.313)*height, 0.180*width, 0.266*height),
                          new      PointDouble((offset.x+0.163)*width, (offset.y+0.313)*height), -36))
               : intersectInclude(intersectInclude(
                          new Ellipse2D.Double((offset.x+0.500)*width, (offset.y+0.166)*height, 0.180*width, 0.324*height),
                   rotate(new Ellipse2D.Double((offset.x+0.486)*width, (offset.y+0.227)*height, 0.180*width, 0.273*height),
                          new      PointDouble((offset.x+0.486)*width, (offset.y+0.227)*height), -35)),
                   rotate(new Ellipse2D.Double((offset.x+0.646)*width, (offset.y+0.211)*height, 0.180*width, 0.266*height),
                          new      PointDouble((offset.x+0.646)*width, (offset.y+0.211)*height), 36));
         if (!disabled) {
            g.setColor(Color.BLACK);
            g.fill(pupil);
         }
         Shape hole = rotate(new Ellipse2D.Double((offset.x+(right?0.303:0.610))*width, (offset.y+0.209)*height, 0.120*width, 0.160*height),
                             new      PointDouble((offset.x+(right?0.303:0.610))*width, (offset.y+0.209)*height), 25);
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
      SmileModel sm = this.getModel();
      int width = sm.getSize().width;
      int height = sm.getSize().height;

      Consumer<Boolean> eye = increased -> {
         g.fill(new Ellipse2D.Double(((right ? 0.107 : 0.517)+(increased?0.015:0))*width, 0.248*height, 0.313*width, 0.034*(increased?2:1)*height));
         g.fill(new Ellipse2D.Double(((right ? 0.230 : 0.640)+(increased?0.015:0))*width, 0.246*height, 0.205*width, 0.065*(increased?2:1)*height));
      };
      if (disabled) {
         g.setColor(Color.WHITE);
         eye.accept(true);
      }
      g.setColor(disabled ? Color.GRAY : Color.BLACK);
      eye.accept(false);
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

   /////////////////////////////////////////////////////////////////////////////////////////////////////
   //    custom implementations
   /////////////////////////////////////////////////////////////////////////////////////////////////////

   /** Smile image view implementation over {@link javax.swing.Icon} */
   static class Icon extends Smile<javax.swing.Icon> {

      private IconSwing ico = new IconSwing(this);

      public Icon(EFaceType faceType) { super(faceType); }

      @Override
      protected javax.swing.Icon createImage() { return ico.create(); }

      @Override
      protected void drawBody() { draw(ico.getGraphics()); }

      @Override
      public void close() {
         ico.close();
         super.close();
         ico = null;
      }

   }

   /** Smile image view implementation over {@link java.awt.Image} */
   static class Image extends Smile<java.awt.Image> {

      private ImageAwt img = new ImageAwt(this);

      public Image(EFaceType faceType) { super(faceType); }

      @Override
      protected java.awt.Image createImage() { return img.create(); }

      @Override
      protected void drawBody() { img.draw(g -> draw(g)); }

   }

   /** Smile image controller implementation for {@link Icon} */
   public static class ControllerIcon extends ImageController<javax.swing.Icon, Smile.Icon, SmileModel> {
      public ControllerIcon(EFaceType faceType) { super(new Smile.Icon(faceType)); }
   }

   /** Smile image controller implementation for {@link Image} */
   public static class ControllerImage extends ImageController<java.awt.Image, Smile.Image, SmileModel> {
      public ControllerImage(EFaceType faceType) { super(new Smile.Image(faceType)); }
   }

   ////////////// TEST //////////////
   public static void main(String[] args) {
      TestDrawing.testApp(() -> {
            return Arrays.asList(EFaceType.values()).stream()
                  .map(e -> Stream.of(new Smile.ControllerIcon(e),
                                      new Smile.ControllerImage(e)))
                  .flatMap(x -> x)
                  .collect(Collectors.toList());
         }
      );
   }
   //////////////////////////////////

}
