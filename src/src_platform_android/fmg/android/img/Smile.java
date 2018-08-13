package fmg.android.img;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PointF;
import android.graphics.Shader;

import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import fmg.common.geom.SizeDouble;
import fmg.core.img.SmileModel;
import fmg.core.img.IImageController;
import fmg.core.img.ImageController;
import fmg.core.img.ImageView;
import fmg.core.img.SmileModel.EFaceType;
import fmg.android.utils.Cast;
import fmg.android.utils.StaticInitializer;

public abstract class Smile<TImage> extends ImageView<TImage, SmileModel> {

   protected Smile(EFaceType faceType) {
      super(new SmileModel(faceType));
   }

   static {
      StaticInitializer.init();
   }

   protected void draw(Canvas g) {
      g.save();

      drawBody(g);
      drawEyes(g);
      drawMouth(g);

      g.restore();
   }

   private void drawBody(Canvas g) {
      SizeDouble size = getSize();

      SmileModel sm = this.getModel();
      SmileModel.EFaceType type = sm.getFaceType();
      float width  = (float)size.width;
      float height = (float)size.height;

      if (type == EFaceType.Eyes_OpenDisabled || type == EFaceType.Eyes_ClosedDisabled)
         return;

      int yellowBody   = Color.rgb(0xFF, 0xCC, 0x00);
      int yellowGlint  = Color.rgb(0xFF, 0xFF, 0x33);
      int yellowBorder = Color.rgb(0xFF, 0x6C, 0x0A);

      Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
      paint.setStyle(Paint.Style.FILL);

      { // рисую затемненный круг
         paint.setColor(yellowBorder);
         g.drawOval(0, 0, width, height, paint);
      }
      float padX = 0.033f * width;
      float padY = 0.033f * height;
      float wInt = width - 2 * padX;
      float hInt = height - 2 * padY;
      float wExt = 1.133f * width;
      float hExt = 1.133f * height;
      Path ellipseInternal = newEllipse(padX, padY, width-padX*2, height-padY*2);
      { // поверх него, внутри - градиентный круг
         paint.setShader(makeLinearGradient(0, 0, yellowBody, width, height, yellowBorder));
         g.drawPath(ellipseInternal, paint);
         paint.setShader(null);
      }
      { // верхний левый блик
         Path ellipseExternal = newEllipse(padX, padY, wExt, hExt);
         paint.setColor(yellowGlint); // Color.DARK_GRAY
         g.drawPath(intersectExclude(ellipseInternal, ellipseExternal), paint);

         // test
         //paint.setColor(Color.BLACK);
         //paint.setStyle(Paint.Style.STROKE);
         //g.drawPath(ellipseInternal, paint);
         //g.drawPath(ellipseExternal, paint);
         //paint.setStyle(Paint.Style.FILL);
      }
      { // нижний правый блик
         Path ellipseExternal = newEllipse(padX + wInt - wExt, padY + hInt - hExt, wExt, hExt);
         paint.setColor(Cast.toColor(Cast.toColor(yellowBorder).darker(0.4)));
         g.drawPath(intersectExclude(ellipseInternal, ellipseExternal), paint);

         // test
         //paint.setColor(Color.BLACK);
         //paint.setStyle(Paint.Style.STROKE);
         //g.drawPath(ellipseInternal, paint);
         //g.drawPath(ellipseExternal, paint);
         //paint.setStyle(Paint.Style.FILL);
      }
   }

   private void drawEyes(Canvas g) {
      SmileModel sm = this.getModel();
      SmileModel.EFaceType type = sm.getFaceType();
      float width  = (float)sm.getSize().width;
      float height = (float)sm.getSize().height;

      Paint paintStroke = new Paint(Paint.ANTI_ALIAS_FLAG); paintStroke.setStyle(Paint.Style.STROKE);
      Paint paintFill   = new Paint(Paint.ANTI_ALIAS_FLAG); paintFill  .setStyle(Paint.Style.FILL);
      switch (type) {
      case Face_Assistant:
      case Face_SmilingWithSunglasses: {
            // glasses
            paintStroke.setStrokeWidth((float)Math.max(1, 0.03*((width+height)/2.0)));
            paintStroke.setStrokeCap(Paint.Cap.ROUND);
            paintStroke.setStrokeJoin(Paint.Join.BEVEL);
            paintStroke.setColor(Color.BLACK);
            g.drawPath(newEllipse(0.200f*width, 0.100f*height, 0.290f*width, 0.440f*height), paintStroke);
            g.drawPath(newEllipse(0.510f*width, 0.100f*height, 0.290f*width, 0.440f*height), paintStroke);
            // дужки
            g.drawLine(   0.746f *width, 0.148f*height,    0.885f *width, 0.055f*height, paintStroke);
            g.drawLine((1-0.746f)*width, 0.148f*height, (1-0.885f)*width, 0.055f*height, paintStroke);
            g.drawPath(newArc(   0.864f        *width, 0.047f*height, 0.100f*width, 0.100f*height,  0, 125), paintStroke);
            g.drawPath(newArc((1-0.864f-0.100f)*width, 0.047f*height, 0.100f*width, 0.100f*height, 55, 125), paintStroke);
         }
         //break; // ! no break
      case Face_SavouringDeliciousFood:
      case Face_WhiteSmiling:
      case Face_Grinning: {
            paintFill.setColor(Color.BLACK);
            g.drawPath(newEllipse(0.270f*width, 0.170f*height, 0.150f*width, 0.300f*height), paintFill);
            g.drawPath(newEllipse(0.580f*width, 0.170f*height, 0.150f*width, 0.300f*height), paintFill);
         }
         break;
      case Face_Disappointed: {
            paintStroke.setStrokeWidth((float)Math.max(1, 0.02*((width+height)/2.0)));
            paintStroke.setStrokeCap(Paint.Cap.ROUND);
            paintStroke.setStrokeJoin(Paint.Join.BEVEL);

            Path rcHalfLeft = newRectangle(0, 0, width/2.0f, height);
            Path rcHalfRght = newRectangle(width/2.0f, 0, width, height);

            // глаз/eye
            Path areaLeft1 = intersectExclude(newEllipse(0.417f*width, 0.050f*height, 0.384f*width, 0.400f*height), rcHalfLeft);
            Path areaRght1 = intersectExclude(newEllipse(0.205f*width, 0.050f*height, 0.384f*width, 0.400f*height), rcHalfRght);
            paintFill  .setColor(Color.RED);
            paintStroke.setColor(Color.BLACK);

            g.drawPath(areaLeft1, paintFill);
            g.drawPath(areaRght1, paintFill);
            g.drawPath(areaLeft1, paintStroke);
            g.drawPath(areaRght1, paintStroke);

            // зрачок/pupil
            Path areaLeft2 = intersectExclude(newEllipse(0.550f*width, 0.200f*height, 0.172f*width, 0.180f*height), rcHalfLeft);
            Path areaRght2 = intersectExclude(newEllipse(0.282f*width, 0.200f*height, 0.172f*width, 0.180f*height), rcHalfRght);
            paintFill  .setColor(Color.BLUE);
            paintStroke.setColor(Color.BLACK);

            g.drawPath(areaLeft2, paintFill);
            g.drawPath(areaRght2, paintFill);
            g.drawPath(areaLeft2, paintStroke);
            g.drawPath(areaRght2, paintStroke);

            // веко/eyelid
            Path areaLeft3 = intersectExclude(rotate(newEllipse(0.441f*width, -0.236f*height, 0.436f*width, 0.560f*height),
                                                     new PointF(0.441f*width, -0.236f*height), 30), rcHalfLeft);
            Path areaRght3 = intersectExclude(rotate(newEllipse(0.128f*width, -0.236f*height, 0.436f*width, 0.560f*height),
                                                     new PointF(0.564f*width, -0.236f*height), -30), rcHalfRght);
            areaLeft3 = intersect(areaLeft1, areaLeft3);
            areaRght3 = intersect(areaRght1, areaRght3);
            paintFill  .setColor(Color.GREEN);
            paintStroke.setColor(Color.BLACK);

            g.drawPath(areaLeft3, paintFill);
            g.drawPath(areaRght3, paintFill);
            g.drawPath(areaLeft3, paintStroke);
            g.drawPath(areaRght3, paintStroke);

            // nose
            Path nose = newEllipse(0.415f*width, 0.400f*height, 0.170f*width, 0.170f*height);
            paintFill  .setColor(Color.GREEN);
            paintStroke.setColor(Color.BLACK);
            g.drawPath(nose, paintFill);
            g.drawPath(nose, paintStroke);
         }
         break;
      case Eyes_OpenDisabled:
         eyeOpened(g,  true, true, paintFill);
         eyeOpened(g, false, true, paintFill);
         break;
      case Eyes_ClosedDisabled:
         eyeClosed(g,  true, true, paintFill);
         eyeClosed(g, false, true, paintFill);
         break;
      case Face_EyesOpen:
         eyeOpened(g,  true, false, paintFill);
         eyeOpened(g, false, false, paintFill);
         break;
      case Face_WinkingEyeLeft:
         eyeClosed(g,  true, false, paintFill);
         eyeOpened(g, false, false, paintFill);
         break;
      case Face_WinkingEyeRight:
         eyeOpened(g,  true, false, paintFill);
         eyeClosed(g, false, false, paintFill);
         break;
      case Face_EyesClosed:
         eyeClosed(g,  true, false, paintFill);
         eyeClosed(g, false, false, paintFill);
         break;
      default:
         throw new UnsupportedOperationException("Not implemented");
      }
   }

   private void drawMouth(Canvas g) {
      SmileModel sm = this.getModel();
      SmileModel.EFaceType type = sm.getFaceType();
      float width  = (float)sm.getSize().width;
      float height = (float)sm.getSize().height;

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


      Paint paintStroke = new Paint(Paint.ANTI_ALIAS_FLAG); paintStroke.setStyle(Paint.Style.STROKE);
      Paint paintFill   = new Paint(Paint.ANTI_ALIAS_FLAG); paintFill  .setStyle(Paint.Style.FILL);
      paintStroke.setStrokeWidth((float)Math.max(1, 0.044*((width+height)/2.0)));
      paintStroke.setStrokeCap(Paint.Cap.ROUND);
      paintStroke.setStrokeJoin(Paint.Join.BEVEL);
      paintStroke.setColor(Color.BLACK);
      paintFill  .setColor(Color.BLACK);

      switch (type) {
      case Face_SavouringDeliciousFood:
      case Face_SmilingWithSunglasses:
      case Face_WhiteSmiling: {
            // smile
            Path arcSmile = newArc(0.103f*width, -0.133f*height, 0.795f*width, 1.003f*height, 207, 126);
            g.drawPath(arcSmile, paintStroke);
            Path lip = newEllipse(0.060f*width, 0.475f*height, 0.877f*width, 0.330f*height);
            g.drawPath(intersectExclude(arcSmile, lip), paintFill);

            // test
//            paintStroke.setColor(Color.GREEN);
//            g.drawPath(lip, paintStroke);

            // dimples - ямочки на щеках
//            g.setStroke(strokeNew);
//            g.setColor(Color.BLACK);
            g.drawPath(newArc(+0.020f*width, 0.420f*height, 0.180f*width, 0.180f*height, 85+180, 57), paintStroke);
            g.drawPath(newArc(+0.800f*width, 0.420f*height, 0.180f*width, 0.180f*height, 38+180, 57), paintStroke);

            // tongue / язык
            if (type == EFaceType.Face_SavouringDeliciousFood) {
               Path tongue = rotate(newEllipse(0.470f*width, 0.406f*height, 0.281f*width, 0.628f*height),
                                    new PointF(0.470f*width, 0.406f*height), 40);
               paintFill.setColor(Color.RED);
               Path ellipseSmile = newEllipse(0.103f*width, -0.133f*height, 0.795f*width, 1.003f*height);
               g.drawPath(intersectExclude(tongue, ellipseSmile), paintFill);
            }
         }
         break;
      case Face_Disappointed: {
            // smile
            Path arcSmile = newArc(0.025f*width, 0.655f*height, 0.950f*width, 0.950f*height, 50, 80); // arc as ellipse
            g.drawPath(arcSmile, paintStroke);
            arcSmile = newArc(0.025f*width, 0.655f*height, 0.950f*width, 0.950f*height, 0, 360); // arc as ellipse

            // tongue / язык
            Path tongue = intersectInclude( newEllipse(0.338f*width, 0.637f*height, 0.325f*width, 0.325f*height),  // кончик языка
                                          newRectangle(0.338f*width, 0.594f*height, 0.325f*width, 0.206f*height)); // тело языка
            Path hole = intersectExclude(newRectangle(0, 0, width, height), arcSmile);
            tongue = intersectExclude(tongue, hole);
            paintFill.setColor(Color.RED);
            g.drawPath(tongue, paintFill);
            paintStroke.setColor(Color.BLACK);
            g.drawPath(tongue, paintStroke);

          //g.drawPath(intersectExclude(newLine(width/2.0f, 0.637f*height, width/2.0f, 0.800f*height), hole), paintStroke); // don't working
            g.drawPath(intersectExclude(newRectangle(width/2.0f, 0.637f*height, 0.001f, 0.200f*height), hole), paintStroke); // its works

            // test
            //paintStroke.setStrokeWidth(1);
            //paintStroke.setStrokeCap(Paint.Cap.ROUND);
            //paintStroke.setStrokeJoin(Paint.Join.BEVEL);
            //paintStroke.setColor(Color.BLACK);
            //paintStroke = new Paint(Paint.ANTI_ALIAS_FLAG); paintStroke.setStyle(Paint.Style.STROKE);
            //g.drawPath(arcSmile, paintStroke);
            //g.drawPath(hole, paintStroke);
         }
         break;
      case Face_Grinning: {
            paintFill.setShader(makeLinearGradient(0, 0, Color.GRAY, width/2.0f, 0, Color.WHITE));
            Path arcSmile = newArc(0.103f*width, -0.133f*height, 0.795f*width, 1.003f*height, 207, 126);
            arcSmile.close();
            g.drawPath(arcSmile, paintFill);
            paintFill.setShader(null);
            paintStroke.setColor(Color.BLACK);
            g.drawPath(arcSmile, paintStroke);
         }
         break;
      default:
         throw new UnsupportedOperationException("Not implemented");
      }
   }

   private void eyeOpened(Canvas g, boolean right, boolean disabled, Paint paintFill) {
      SmileModel sm = this.getModel();
      float width  = (float)sm.getSize().width;
      float height = (float)sm.getSize().height;

      Consumer<PointF> draw = offset -> {
         Path pupil = right
               ? intersectInclude(intersectInclude(
                          newEllipse((offset.x+0.273f)*width, (offset.y+0.166f)*height, 0.180f*width, 0.324f*height),
                   rotate(newEllipse((offset.x+0.320f)*width, (offset.y+0.124f)*height, 0.180f*width, 0.273f*height),
                          new PointF((offset.x+0.320f)*width, (offset.y+0.124f)*height), 35)),
                   rotate(newEllipse((offset.x+0.163f)*width, (offset.y+0.313f)*height, 0.180f*width, 0.266f*height),
                          new PointF((offset.x+0.163f)*width, (offset.y+0.313f)*height), -36))
               : intersectInclude(intersectInclude(
                          newEllipse((offset.x+0.500f)*width, (offset.y+0.166f)*height, 0.180f*width, 0.324f*height),
                   rotate(newEllipse((offset.x+0.486f)*width, (offset.y+0.227f)*height, 0.180f*width, 0.273f*height),
                          new PointF((offset.x+0.486f)*width, (offset.y+0.227f)*height), -35)),
                   rotate(newEllipse((offset.x+0.646f)*width, (offset.y+0.211f)*height, 0.180f*width, 0.266f*height),
                          new PointF((offset.x+0.646f)*width, (offset.y+0.211f)*height), 36));
         if (!disabled) {
            paintFill.setColor(Color.BLACK);
            g.drawPath(pupil, paintFill);
         }
         Path hole = rotate(newEllipse((offset.x+(right?0.303f:0.610f))*width, (offset.y+0.209f)*height, 0.120f*width, 0.160f*height),
                            new PointF((offset.x+(right?0.303f:0.610f))*width, (offset.y+0.209f)*height), 25);
         if (!disabled) {
            paintFill.setColor(Color.WHITE);
            g.drawPath(hole, paintFill);
         } else {
            g.drawPath(intersectExclude(pupil, hole), paintFill);
         }
      };
      if (disabled) {
         paintFill.setColor(Color.WHITE);
         draw.accept(new PointF(0.034f, 0.027f));
         paintFill.setColor(Color.GRAY);
         draw.accept(new PointF());
      } else {
         draw.accept(new PointF());
      }
   }

   private void eyeClosed(Canvas g, boolean right, boolean disabled, Paint paintFill) {
      SmileModel sm = this.getModel();
      float width  = (float)sm.getSize().width;
      float height = (float)sm.getSize().height;

      Consumer<Boolean> eye = isFat -> {
         g.drawPath(newEllipse(((right ? 0.107f : 0.517f)+(isFat?0.015f:0))*width, 0.248f*height, 0.313f*width, 0.034f*(isFat?2:1)*height), paintFill);
         g.drawPath(newEllipse(((right ? 0.230f : 0.640f)+(isFat?0.015f:0))*width, 0.246f*height, 0.205f*width, 0.065f*(isFat?2:1)*height), paintFill);
      };
      if (disabled) {
         paintFill.setColor(Color.WHITE);
         eye.accept(true);
      }
      paintFill.setColor(disabled ? Color.GRAY : Color.BLACK);
      eye.accept(false);
   }

   private static Path rotate(Path shape, PointF rotatePoint, float angle) {
      Matrix m = new Matrix();
      Path res = new Path(shape);
      m.postRotate(angle, rotatePoint.x, rotatePoint.y);
      res.transform(m);
      return res;
   }

   private static Path intersect(Path s1, Path s2) {
      Path res = new Path(s1);
      res.op(s2, Path.Op.INTERSECT);
      return res;
   }

   private static Path intersectExclude(Path s1, Path s2) {
      Path res = new Path(s1);
      res.op(s2, Path.Op.DIFFERENCE);
      return res;
   }

   private static Path intersectInclude(Path s1, Path s2) {
      Path res = new Path(s1);
      res.op(s2, Path.Op.UNION);
      return res;
   }

   private static Path newEllipse(float x, float y, float w, float h) {
      Path p = new Path();
      p.addOval(x, y, x+w, y+h, Path.Direction.CW);
      return p;
   }
   private static Path newLine(float startX, float startY, float endX, float endY) {
      Path p = new Path();
      p.moveTo(startX, startY);
      p.lineTo(endX, endY);
      return p;
   }
   private static Path newRectangle(float x, float y, float w, float h) {
      Path p = new Path();
      p.addRect(x, y, x+w, y+h, Path.Direction.CW);
      return p;
   }
   private static Path newArc(float x, float y, float w, float h, float start, float extent) {
      Path p = new Path();
      p.addArc(x, y, x+w, y+h, 360-start-extent, extent);
      return p;
   }
   private static Shader makeLinearGradient(float startX, float startY, int startClr, float endX, float endY, int endClr) {
      return new LinearGradient(startX, startY, endX, endY, startClr, endClr, Shader.TileMode.CLAMP);
   }

   @Override
   public void close() {
      getModel().close();
      super.close();
   }

   /////////////////////////////////////////////////////////////////////////////////////////////////////
   //    custom implementations
   /////////////////////////////////////////////////////////////////////////////////////////////////////

   /** Smile image view implementation over {@link android.graphics.Bitmap} */
   static class Bitmap extends Smile<android.graphics.Bitmap> {

      private BmpCanvas wrap = new BmpCanvas();

      public Bitmap(EFaceType faceType) { super(faceType); }

      @Override
      protected android.graphics.Bitmap createImage() {
         return wrap.createImage(getModel().getSize());
      }

      @Override
      protected void drawBody() {
         draw(wrap.getCanvas());
      }

      @Override
      public void close() {
         wrap.close();
      }

   }

   /** Smile image controller implementation for {@link Smile.Bitmap} */
   public static class ControllerBitmap extends ImageController<android.graphics.Bitmap, Smile.Bitmap, SmileModel> {

      public ControllerBitmap(EFaceType faceType) {
         super(new Smile.Bitmap(faceType));
      }

      @Override
      public void close() {
         getView().close();
         super.close();
      }

   }

   ////////////// TEST //////////////
   public static List<IImageController<?,?,?>> testData() {
      return Arrays.asList(EFaceType.values()).stream()
            .map(e -> new Smile.ControllerBitmap(e))
            .collect(Collectors.toList());
   }
   //////////////////////////////////

}
