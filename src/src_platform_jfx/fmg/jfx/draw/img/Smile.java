package fmg.jfx.draw.img;

import java.util.Arrays;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.*;
import javafx.scene.shape.*;

import fmg.common.geom.PointDouble;
import fmg.common.geom.Size;
import fmg.core.img.ImageController;
import fmg.core.img.ImageView;
import fmg.core.img.SmileModel;
import fmg.core.img.SmileModel.EFaceType;
import fmg.jfx.Cast;
import fmg.jfx.StaticInitilizer;
import fmg.jfx.utils.ShapeConverter;

public abstract class Smile<TImage> extends ImageView<TImage, SmileModel> {

   protected Smile(EFaceType faceType) {
      super(new SmileModel(faceType));
   }

   static {
      StaticInitilizer.init();
   }

   protected void draw(GraphicsContext g) {
      // save
      Paint oldFill = g.getFill();
      Paint oldStroke = g.getStroke();

      drawBody(g);
      drawEyes(g);
      drawMouth(g);

      // restore
      g.setFill(oldFill);
      g.setStroke(oldStroke);
   }

   private void drawBody(GraphicsContext g) {
      Size size = getSize();
      g.clearRect(0,0, size.width, size.height);

      SmileModel sm = this.getModel();
      SmileModel.EFaceType type = sm.getFaceType();
      int width = sm.getSize().width;
      int height = sm.getSize().height;

      if (type == EFaceType.Eyes_OpenDisabled || type == EFaceType.Eyes_ClosedDisabled)
         return;

      Color yellowBody   = Color.rgb(0xFF, 0xCC, 0x00);
      Color yellowGlint  = Color.rgb(0xFF, 0xFF, 0x33);
      Color yellowBorder = Color.rgb(0xFF, 0x6C, 0x0A);

      { // рисую затемненный круг
         g.setFill(yellowBorder);
         g.fillOval(0, 0, width, height);
      }

      double padX = 0.033 * width;
      double padY = 0.033 * height;
      double wInt = width - 2 * padX;
      double hInt = height - 2 * padY;
      double wExt = 1.133 * width;
      double hExt = 1.133 * height;
      Ellipse ellipseInternal = newEllipse(padX, padY, width-padX*2, height-padY*2);
      { // поверх него, внутри - градиентный круг
         g.setFill(new LinearGradient(0, 0, width, height, false, CycleMethod.NO_CYCLE, new Stop[] { new Stop(0, yellowBody), new Stop(1, yellowBorder)}));
         g.fillOval(padX, padY, width-padX*2, height-padY*2);
      }
      { // верхний левый блик
         Ellipse ellipseExternal = newEllipse(padX, padY, wExt, hExt);
         g.beginPath();
         g.appendSVGPath(ShapeConverter.toSvg(intersectExclude(ellipseInternal, ellipseExternal)));
         g.closePath();
         g.setFill(yellowGlint); // Color.DARK_GRAY
         g.fill();

         // test
         //g.setColor(Color.BLACK);
         //g.draw(ellipseInternal);
         //g.draw(ellipseExternal);
      }
      { // нижний правый блик
         Ellipse ellipseExternal = newEllipse(padX + wInt - wExt, padY + hInt - hExt, wExt, hExt);
         g.beginPath();
         g.appendSVGPath(ShapeConverter.toSvg(intersectExclude(ellipseInternal, ellipseExternal)));
         g.closePath();
         g.setFill(Cast.toColor(Cast.toColor(yellowBorder).darker(0.4)));
         g.fill();

         // test
         //g.setColor(Color.BLACK);
         //g.draw(ellipseInternal);
         //g.draw(ellipseExternal);
      }
   }

   private void drawEyes(GraphicsContext g) {
      SmileModel sm = this.getModel();
      SmileModel.EFaceType type = sm.getFaceType();
      int width = sm.getSize().width;
      int height = sm.getSize().height;

      Paint fillOld = g.getFill();
      Paint strokeOld = g.getStroke();
      double lwOld = g.getLineWidth();
      StrokeLineCap slcOld = g.getLineCap();
      StrokeLineJoin sljOld = g.getLineJoin();

      switch (type) {
      case Face_Assistant:
      case Face_SmilingWithSunglasses: {
            // glasses
            g.setLineWidth(Math.max(1, 0.03*((width+height)/2.0)));
            g.setLineCap(StrokeLineCap.ROUND);
            g.setLineJoin(StrokeLineJoin.BEVEL);
            g.setStroke(Color.BLACK);
            g.strokeOval(0.200*width, 0.100*height, 0.290*width, 0.440*height);
            g.strokeOval(0.510*width, 0.100*height, 0.290*width, 0.440*height);
            // дужки
            g.strokeLine(   0.746 *width, 0.148*height,   0.885 *width, 0.055*height);
            g.strokeLine((1-0.746)*width, 0.148*height, (1-0.885)*width, 0.055*height);
            g.strokeArc(   0.864       *width, 0.047*height, 0.100*width, 0.100*height,  0, 125, ArcType.OPEN);
            g.strokeArc((1-0.864-0.100)*width, 0.047*height, 0.100*width, 0.100*height, 55, 125, ArcType.OPEN);
         }
         //break; // ! no break
      case Face_SavouringDeliciousFood:
      case Face_WhiteSmiling:
      case Face_Grinning: {
            g.setFill(Color.BLACK);
            g.fillOval(0.270*width, 0.170*height, 0.150*width, 0.300*height);
            g.fillOval(0.580*width, 0.170*height, 0.150*width, 0.300*height);
         }
         break;
      case Face_Disappointed: {
            g.setLineWidth(Math.max(1, 0.02*((width+height)/2.0)));
            g.setLineCap(StrokeLineCap.ROUND);
            g.setLineJoin(StrokeLineJoin.BEVEL);

            Rectangle rcHalfLeft = new Rectangle(0, 0, width/2.0, height);
            Rectangle rcHalfRght = new Rectangle(width/2.0, 0, width, height);

            // глаз/eye
            Shape areaLeft1 = intersectExclude(newEllipse(0.417*width, 0.050*height, 0.384*width, 0.400*height), rcHalfLeft);
            Shape areaRght1 = intersectExclude(newEllipse(0.205*width, 0.050*height, 0.384*width, 0.400*height), rcHalfRght);
            g.setFill(Color.RED);
            g.setStroke(Color.BLACK);

            g.beginPath();
            g.appendSVGPath(ShapeConverter.toSvg(areaLeft1));
            g.closePath();
            g.fill();
            g.stroke();

            g.beginPath();
            g.appendSVGPath(ShapeConverter.toSvg(areaRght1));
            g.closePath();
            g.fill();
            g.stroke();

            // зрачок/pupil
            Shape areaLeft2 = intersectExclude(newEllipse(0.550*width, 0.200*height, 0.172*width, 0.180*height), rcHalfLeft);
            Shape areaRght2 = intersectExclude(newEllipse(0.282*width, 0.200*height, 0.172*width, 0.180*height), rcHalfRght);
            g.setFill(Color.BLUE);
            g.setStroke(Color.BLACK);

            g.beginPath();
            g.appendSVGPath(ShapeConverter.toSvg(areaLeft2));
            g.closePath();
            g.fill();
            g.stroke();

            g.beginPath();
            g.appendSVGPath(ShapeConverter.toSvg(areaRght2));
            g.closePath();
            g.fill();
            g.stroke();

            // веко/eyelid
            Shape areaLeft3 = intersectExclude(rotate(newEllipse     (0.441*width, -0.236*height, 0.436*width, 0.560*height),
                                                      new PointDouble(0.441*width, -0.236*height), 30), rcHalfLeft);
            Shape areaRght3 = intersectExclude(rotate(newEllipse     (0.128*width, -0.236*height, 0.436*width, 0.560*height),
                                                      new PointDouble(0.564*width, -0.236*height), -30), rcHalfRght);
            areaLeft3 = intersect(areaLeft1, areaLeft3);
            areaRght3 = intersect(areaRght1, areaRght3);
            g.setFill(Color.LIME);
            g.setStroke(Color.BLACK);

            g.beginPath();
            g.appendSVGPath(ShapeConverter.toSvg(areaLeft3));
            g.closePath();
            g.fill();
            g.stroke();

            g.beginPath();
            g.appendSVGPath(ShapeConverter.toSvg(areaRght3));
            g.closePath();
            g.fill();
            g.stroke();

            // nose
            Ellipse nose = newEllipse(0.415*width, 0.400*height, 0.170*width, 0.170*height);
            g.setFill(Color.LIME);
            g.setStroke(Color.BLACK);
            g.beginPath();
            g.appendSVGPath(ShapeConverter.toSvg(nose));
            g.closePath();
            g.fill();
            g.stroke();
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

      g.setLineWidth(lwOld);
      g.setLineCap(slcOld);
      g.setLineJoin(sljOld);
      g.setStroke(strokeOld);
      g.setFill(fillOld);
   }

   private void drawMouth(GraphicsContext g) {
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


      Paint fillOld = g.getFill();
      Paint strokeOld = g.getStroke();
      double lwOld = g.getLineWidth();
      StrokeLineCap slcOld = g.getLineCap();
      StrokeLineJoin sljOld = g.getLineJoin();

      g.setLineWidth(Math.max(1, 0.044*((width+height)/2.0)));
      g.setLineCap(StrokeLineCap.ROUND);
      g.setLineJoin(StrokeLineJoin.BEVEL);
      g.setStroke(Color.BLACK);
      g.setFill(Color.BLACK);

      switch (type) {
      case Face_SavouringDeliciousFood:
      case Face_SmilingWithSunglasses:
      case Face_WhiteSmiling: {
            // smile
            g.strokeArc          (0.103*width, -0.133*height, 0.795*width, 1.003*height, 207, 126, ArcType.OPEN);
            Arc arcSmile = newArc(0.103*width, -0.133*height, 0.795*width, 1.003*height, 207, 126);
            Ellipse lip = newEllipse(0.060*width, 0.475*height, 0.877*width, 0.330*height);
            g.beginPath();
            g.appendSVGPath(ShapeConverter.toSvg(intersectExclude(arcSmile, lip)));
            g.closePath();
            g.fill();

            // test
            //g.setStroke(strokeOld);
            //g.setColor(Color.LIME);
            //g.draw(lip);

            // dimples - ямочки на щеках
//            g.setStroke(strokeNew);
//            g.setColor(Color.BLACK);
            g.strokeArc(+0.020*width, 0.420*height, 0.180*width, 0.180*height, 85+180, 57, ArcType.OPEN);
            g.strokeArc(+0.800*width, 0.420*height, 0.180*width, 0.180*height, 38+180, 57, ArcType.OPEN);

            // tongue / язык
            if (type == EFaceType.Face_SavouringDeliciousFood) {
               Shape tongue = rotate(newEllipse     (0.470*width, 0.406*height, 0.281*width, 0.628*height),
                                     new PointDouble(0.470*width, 0.406*height), 40);
               g.setFill(Color.RED);
               Ellipse ellipseSmile = newEllipse(0.103*width, -0.133*height, 0.795*width, 1.003*height);
               g.beginPath();
               g.appendSVGPath(ShapeConverter.toSvg(intersectExclude(tongue, ellipseSmile)));
               g.closePath();
               g.fill();
            }
         }
         break;
      case Face_Disappointed: {
            // smile
            g.strokeArc          (0.025*width, 0.655*height, 0.950*width, 0.950*height, 50, 80, ArcType.OPEN);
            Arc arcSmile = newArc(0.025*width, 0.655*height, 0.950*width, 0.950*height, 0, 360); // arc as ellipse

            // tongue / язык
            Shape tongue = intersectInclude( newEllipse(0.338*width, 0.637*height, 0.325*width, 0.325*height),  // кончик языка
                                          new Rectangle(0.338*width, 0.594*height, 0.325*width, 0.206*height)); // тело языка
            Shape hole = intersectExclude(new Rectangle(0, 0, width, height), arcSmile);
            tongue = intersectExclude(tongue, hole);
            g.setFill(Color.RED);
            g.beginPath();
            g.appendSVGPath(ShapeConverter.toSvg(tongue));
            g.closePath();
            g.fill();
            g.setStroke(Color.BLACK);
            g.stroke();

            g.beginPath();
          //g.appendSVGPath(ShapeConverter.toSvg(intersectExclude(new Line(width/2.0, 0.637*height, width/2.0, 0.800*height), hole))); // don't working
            g.appendSVGPath(ShapeConverter.toSvg(intersectExclude(new Rectangle(width/2.0, 0.637*height, 0.0001, 0.200*height), hole))); // its works
            g.closePath();
            g.stroke();

            // test
            //g.setStroke(new java.awt.BasicStroke(1, java.awt.BasicStroke.CAP_ROUND, java.awt.BasicStroke.JOIN_BEVEL));
            //g.draw(arcSmile);
            //g.draw(hole);
         }
         break;
      case Face_Grinning: {
            g.setFill(new LinearGradient(0, 0, width/2.0, 0, false, CycleMethod.NO_CYCLE, new Stop[] { new Stop(0, Color.GRAY), new Stop(1, Color.WHITE)}));
            g.fillArc(0.103*width, -0.133*height, 0.795*width, 1.003*height, 207, 126, ArcType.CHORD);
            g.setStroke(Color.BLACK);
            g.strokeArc(0.103*width, -0.133*height, 0.795*width, 1.003*height, 207, 126, ArcType.CHORD);
         }
         break;
      default:
         throw new UnsupportedOperationException("Not implemented");
      }

      g.setLineWidth(lwOld);
      g.setLineCap(slcOld);
      g.setLineJoin(sljOld);
      g.setStroke(strokeOld);
      g.setFill(fillOld);
   }

   private void eyeOpened(GraphicsContext g, boolean right, boolean disabled) {
      SmileModel sm = this.getModel();
      int width = sm.getSize().width;
      int height = sm.getSize().height;

      Consumer<PointDouble> draw = offset -> {
         Shape pupil = right
               ? intersectInclude(intersectInclude(
                          newEllipse     ((offset.x+0.273)*width, (offset.y+0.166)*height, 0.180*width, 0.324*height),
                   rotate(newEllipse     ((offset.x+0.320)*width, (offset.y+0.124)*height, 0.180*width, 0.273*height),
                          new PointDouble((offset.x+0.320)*width, (offset.y+0.124)*height), 35)),
                   rotate(newEllipse     ((offset.x+0.163)*width, (offset.y+0.313)*height, 0.180*width, 0.266*height),
                          new PointDouble((offset.x+0.163)*width, (offset.y+0.313)*height), -36))
               : intersectInclude(intersectInclude(
                          newEllipse     ((offset.x+0.500)*width, (offset.y+0.166)*height, 0.180*width, 0.324*height),
                   rotate(newEllipse     ((offset.x+0.486)*width, (offset.y+0.227)*height, 0.180*width, 0.273*height),
                          new PointDouble((offset.x+0.486)*width, (offset.y+0.227)*height), -35)),
                   rotate(newEllipse     ((offset.x+0.646)*width, (offset.y+0.211)*height, 0.180*width, 0.266*height),
                          new PointDouble((offset.x+0.646)*width, (offset.y+0.211)*height), 36));
         if (!disabled) {
            g.setFill(Color.BLACK);
            g.beginPath();
            g.appendSVGPath(ShapeConverter.toSvg(pupil));
            g.closePath();
            g.fill();
         }
         Shape hole = rotate(     newEllipse((offset.x+(right?0.303:0.610))*width, (offset.y+0.209)*height, 0.120*width, 0.160*height),
                             new PointDouble((offset.x+(right?0.303:0.610))*width, (offset.y+0.209)*height), 25);
         if (!disabled) {
            g.setFill(Color.WHITE);
            g.beginPath();
          //g.appendSVGPath(ShapeConverter.toSvg(hole)); // hm.... not forked
            g.appendSVGPath(ShapeConverter.toSvg(intersectExclude(hole, new Rectangle(0,0,0,0)))); // ..must usage over dummy rect
            g.closePath();
            g.fill();
         } else {
            g.beginPath();
            g.appendSVGPath(ShapeConverter.toSvg(intersectExclude(pupil, hole)));
            g.closePath();
            g.fill();
         }
      };
      if (disabled) {
         g.setFill(Color.WHITE);
         draw.accept(new PointDouble(0.034, 0.027));
         g.setFill(Color.GRAY);
         draw.accept(new PointDouble());
      } else {
         draw.accept(new PointDouble());
      }
   }

   private void eyeClosed(GraphicsContext g, boolean right, boolean disabled) {
      SmileModel sm = this.getModel();
      int width = sm.getSize().width;
      int height = sm.getSize().height;

      Consumer<Boolean> eye = isFat -> {
         g.fillOval(((right ? 0.107 : 0.517)+(isFat?0.015:0))*width, 0.248*height, 0.313*width, 0.034*(isFat?2:1)*height);
         g.fillOval(((right ? 0.230 : 0.640)+(isFat?0.015:0))*width, 0.246*height, 0.205*width, 0.065*(isFat?2:1)*height);
      };
      if (disabled) {
         g.setFill(Color.WHITE);
         eye.accept(true);
      }
      g.setFill(disabled ? Color.GRAY : Color.BLACK);
      eye.accept(false);
   }

   private static Shape rotate(Shape shape, PointDouble rotatePoint, double angle) {
      shape.getTransforms().add(new javafx.scene.transform.Rotate(angle, rotatePoint.x, rotatePoint.y));
      return shape;
   }

   private static Shape intersect(Shape s1, Shape s2) {
      return Shape.intersect(s1, s2);
   }

   private static Shape intersectExclude(Shape s1, Shape s2) {
      return Shape.subtract(s1, s2);
   }

   private static Shape intersectInclude(Shape s1, Shape s2) {
      return Shape.union(s1, s2);
   }

   private static Ellipse newEllipse(double x, double y, double w, double h) {
      return new Ellipse(x+w/2, y+h/2, w/2, h/2);
   }
   private static Arc newArc(double x, double y, double w, double h, double start, double extent) {
       return new Arc(x+w/2, y+h/2, w/2, h/2, start, extent);
   }

   /////////////////////////////////////////////////////////////////////////////////////////////////////
   //    custom implementations
   /////////////////////////////////////////////////////////////////////////////////////////////////////

   /** Smile image view implementation over {@link javafx.scene.canvas.Canvas} */
   static class Canvas extends Smile<javafx.scene.canvas.Canvas> {

       private CanvasJfx canvas = new CanvasJfx(this);

       public Canvas(EFaceType faceType) { super(faceType); }

       @Override
       protected javafx.scene.canvas.Canvas createImage() { return canvas.create(); }

       @Override
       protected void drawBody() { draw(canvas.getGraphics()); }

   }

   /** Smile image view implementation over {@link javafx.scene.image.Image} */
   static class Image extends Smile<javafx.scene.image.Image> {

       private ImageJfx img = new ImageJfx(this);

       public Image(EFaceType faceType) { super(faceType); }

       @Override
       protected javafx.scene.image.Image createImage() {
          img.createCanvas();
          return null; // img.createImage(); // fake empty image
       }

       @Override
       protected void drawBody() {
          draw(img.getGraphics());
          setImage(img.createImage()); // real image
       }

   }

   /** Smile image controller implementation for {@link Canvas} */
   public static class ControllerCanvas extends ImageController<javafx.scene.canvas.Canvas, Smile.Canvas, SmileModel> {
      public ControllerCanvas(EFaceType faceType) { super(new Smile.Canvas(faceType)); }
   }

   /** Smile image controller implementation for {@link Image} */
   public static class ControllerImage extends ImageController<javafx.scene.image.Image, Smile.Image, SmileModel> {
      public ControllerImage(EFaceType faceType) { super(new Smile.Image(faceType)); }
   }

   ////////////// TEST //////////////
   public static void main(String[] args) {
      TestDrawing.testApp(() -> {
            return Arrays.asList(EFaceType.values()).stream()
                  .map(e -> Stream.of(new Smile.ControllerCanvas(e),
                                      new Smile.ControllerImage(e)))
                  .flatMap(x -> x)
                  .collect(Collectors.toList());
         }
      );
   }
   //////////////////////////////////

}
