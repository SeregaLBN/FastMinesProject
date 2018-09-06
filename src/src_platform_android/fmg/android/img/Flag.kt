package fmg.android.img;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PointF;

import java.util.Arrays;
import java.util.List;

import fmg.common.geom.SizeDouble;
import fmg.core.img.FlagModel;
import fmg.core.img.IImageController;
import fmg.core.img.ImageController;
import fmg.core.img.ImageView;
import fmg.android.utils.StaticInitializer;

/** Flag image */
public abstract class Flag<TImage> extends ImageView<TImage, FlagModel> {

   public Flag() {
      super(new FlagModel());
   }

   static {
      StaticInitializer.init();
   }

   protected void draw(Canvas g) {
      SizeDouble size = getSize();
      float h = (float)(size.height / 100.0);
      float w = (float)(size.width  / 100.0);

      // perimeter figure points
      PointF[] p = new PointF[] {
            new PointF(13.50f * w, 90 * h),
            new PointF(17.44f * w, 51 * h),
            new PointF(21.00f * w, 16 * h),
            new PointF(85.00f * w, 15 * h),
            new PointF(81.45f * w, 50 * h)};

      Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
      paint.setStyle(Paint.Style.STROKE);
      paint.setStrokeWidth(Math.max(1, 7*(w+h)/2));
      paint.setColor(Color.BLACK);
      g.drawLine(p[0].x, p[0].y, p[1].x, p[1].y, paint);

      paint.setColor(Color.RED);
      Path path = new Path();
      path.moveTo(p[2].x, p[2].y);
      path.cubicTo(95.0f * w, 0 * h,
                   19.3f * w, 32 * h,
                   p[3].x, p[3].y);
      path.cubicTo(77.80f * w, 32.89f * h,
                   88.05f * w, 22.73f * h,
                   p[4].x, p[4].y);
      path.cubicTo(15.83f * w, 67 * h,
                   91.45f * w, 35 * h,
                   p[1].x, p[1].y);
      path.lineTo(p[2].x, p[2].y);
      path.close();

      g.drawPath(path, paint);
   }

   @Override
   public void close() {
      getModel().close();
      super.close();
   }

   /////////////////////////////////////////////////////////////////////////////////////////////////////
   //    custom implementations
   /////////////////////////////////////////////////////////////////////////////////////////////////////

   /** Flag image view implementation over {@link android.graphics.Bitmap} */
   static class Bitmap extends Flag<android.graphics.Bitmap> {

      private BmpCanvas wrap = new BmpCanvas();

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

   /** Flag image controller implementation for {@link Bitmap} */
   public static class ControllerBitmap extends ImageController<android.graphics.Bitmap, Flag.Bitmap, FlagModel> {

      public ControllerBitmap() {
         super(new Flag.Bitmap());
      }

      @Override
      public void close() {
         getView().close();
         super.close();
      }

   }

}
