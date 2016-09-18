package fmg.jfx.draw.img;

import java.util.Arrays;

import fmg.common.geom.Size;
import fmg.jfx.utils.ImgUtils;
import javafx.geometry.Point2D;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.effect.BoxBlur;
import javafx.scene.paint.Color;

/** Flag image */
public abstract class Flag<TImage> {

   protected javafx.scene.canvas.Canvas _canvas;
   private Size _size = new Size(100, 100);

   public abstract TImage getImage();

   public Size getSize() { return _size; }
   public void setSize(Size value) { _size = value; _canvas = null; }

   protected void drawBody(GraphicsContext g) {
      double w = _size.width / 100.0;
      double h = _size.height / 100.0;

      g.setEffect(new BoxBlur());

      // test
//      g.setLineWidth(1);
//      g.setStroke(Color.RED);
//      g.strokeRect(0, 0, _size.width, _size.height);

      // perimeter figure points
      Point2D[] p = new Point2D[] {
            new Point2D(13.50f * w, 90 * h),
            new Point2D(17.44f * w, 51 * h),
            new Point2D(21.00f * w, 16 * h),
            new Point2D(85.00f * w, 15 * h),
            new Point2D(81.45f * w, 50 * h)};

      g.setLineWidth(Math.max(1, 7*(w+h)/2));
      g.setStroke(Color.BLACK);
      g.strokeLine(p[0].getX(), p[0].getY(), p[1].getX(), p[1].getY());

      g.setStroke(Color.RED);
      g.beginPath();
      g.moveTo(p[2].getX(), p[2].getY());
      g.bezierCurveTo(95.0 * w,  0 * h,
                      19.3 * w, 32 * h,
                      p[3].getX(), p[3].getY());
      g.bezierCurveTo(77.80 * w, 32.89 * h,
                      88.05 * w, 22.73 * h,
                      p[4].getX(), p[4].getY());
      g.bezierCurveTo(15.83 * w, 67 * h,
                      91.45 * w, 35 * h,
                      p[1].getX(), p[1].getY());
      g.lineTo(p[2].getX(), p[2].getY());
      g.closePath();

      g.stroke();
   }

   /////////////////////////////////////////////////////////////////////////////////////////////////////
   //    custom implementations
   /////////////////////////////////////////////////////////////////////////////////////////////////////

   public static class Canvas extends Flag<javafx.scene.canvas.Canvas> {

      @Override
      public javafx.scene.canvas.Canvas getImage() {
         if (_canvas == null) {
            _canvas = new javafx.scene.canvas.Canvas(getSize().width, getSize().height);
            drawBody(_canvas.getGraphicsContext2D());
         }
         return _canvas;
      }

   }

   public static class Image extends Flag<javafx.scene.image.Image> {

      @Override
      public javafx.scene.image.Image getImage() {
         if (_canvas == null) {
            _canvas = new javafx.scene.canvas.Canvas(getSize().width, getSize().height);
            drawBody(_canvas.getGraphicsContext2D());
            _img = ImgUtils.toImage(_canvas);
         }
         return _img;
      }
      javafx.scene.image.Image _img;

   }

   ////////////// TEST //////////////
   public static void main(String[] args) {
      TestDrawing.testApp(rnd -> Arrays.asList(new Flag.Canvas()
                                           //, new Flag.Image()
                                     ));
   }
   //////////////////////////////////

}
