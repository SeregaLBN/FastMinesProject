package fmg.core.img;

import java.util.function.Supplier;

import fmg.common.geom.Bound;
import fmg.common.geom.Size;
import fmg.common.ui.ITimer;

public abstract class RotatedImg<T, TImage extends Object> extends StaticImg<T, TImage> {

   public static Supplier<ITimer> TIMER_CREATOR;

   protected RotatedImg(T entity) { super(entity); }
   protected RotatedImg(T entity, int widthAndHeight) { super(entity, widthAndHeight); }
   protected RotatedImg(T entity, int widthAndHeight, int padding) { super(entity, widthAndHeight, padding); }
   protected RotatedImg(T entity, Size sizeImage, Bound padding) { super(entity, sizeImage, padding); }

   private long _redrawInterval = 100;
   /** frequency of redrawing (in milliseconds) */
   public long getRedrawInterval() { return _redrawInterval; }
   public void setRedrawInterval(long value) {
      if (setProperty(_redrawInterval, value, "RedrawInterval") && (_timer != null))
         _timer.setInterval(_redrawInterval);
   }

   private ITimer _timer;

   private boolean _rotate;
   public boolean isRotate() { return _rotate; }
   public void setRotate(boolean value) {
      if (setProperty(_rotate, value, "Rotate")) {
         if (value)
            startTimer();
         else
            stopTimer();
      }
   }

   private double _rotateAngleDelta = 1.4;
   public double getRotateAngleDelta() { return _rotateAngleDelta; }
   public void setRotateAngleDelta(double value) {
      if (setProperty(_rotateAngleDelta, value, "RotateAngleDelta") && isRotate())
         invalidate();
   }

   protected void startTimer() {
      if (_timer == null) {
         _timer = TIMER_CREATOR.get();
         _timer.setInterval(_redrawInterval);
      }
      _timer.setCallback(() -> onTimer()); //  start
   }

   protected void stopTimer() {
      if ((_timer != null) && !isLiveImage())
         _timer.setCallback(null); // stop
   }

   protected void onTimer() {
      if (isRotate())
         rotateStep();
   }

   public boolean isLiveImage() { return isRotate(); }

   private void rotateStep() {
      double rotateAngle = getRotateAngle() + getRotateAngleDelta();
      if (rotateAngle >= 360) {
         rotateAngle -= 360;
      } else {
         if (rotateAngle < 0)
            rotateAngle += 360;
      }
      setRotateAngle(rotateAngle);
      assert (rotateAngle >= 0) && (rotateAngle < 360);
   }

   @Override
   public void close() {
      ITimer t = _timer;
      if (t != null)
         t.close();
      _timer = null;
   }

}
