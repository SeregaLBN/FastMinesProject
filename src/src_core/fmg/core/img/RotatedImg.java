package fmg.core.img;

import java.awt.event.ActionListener;

import javax.swing.Timer;

import fmg.common.geom.Bound;
import fmg.common.geom.Size;

public abstract class RotatedImg<T, TImage extends Object> extends StaticImg<T, TImage> {

   protected RotatedImg(T entity) { super(entity); }
   protected RotatedImg(T entity, int widthAndHeight) { super(entity, widthAndHeight); }
   protected RotatedImg(T entity, int widthAndHeight, int padding) { super(entity, widthAndHeight, padding); }
   protected RotatedImg(T entity, Size sizeImage, Bound padding) { super(entity, sizeImage, padding); }

   private double _redrawInterval = 100;
   /** frequency of redrawing (in milliseconds) */
   public double getRedrawInterval() { return _redrawInterval; }
   public void setRedrawInterval(double value) { setProperty(_redrawInterval, value, "RedrawInterval"); }

   private Timer _timer;

   private boolean _rotate;
   public boolean isRotate() { return _rotate; }
   public void setRotate(boolean value) {
      if (setProperty(_rotate, value, "Rotate") && value)
         invalidate();
   }

   private double _rotateAngleDelta = 1.4;
   public double getRotateAngleDelta() { return _rotateAngleDelta; }
   public void setRotateAngleDelta(double value) {
      if (setProperty(_rotateAngleDelta, value, "RotateAngleDelta") && isRotate())
         invalidate();
   }

   @Override
   protected void drawEnd() {
      if (isLiveImage()) {
         if (_timer == null)
            _timer = new Timer((int) getRedrawInterval(), timerListener);
         _timer.setRepeats(true);
         _timer.start();
      } else {
         if (_timer != null)
            _timer.stop();
      }
      super.drawEnd();
   }

   private ActionListener timerListener = evt -> onTimer();
   protected void onTimer() { RotateStep(); }

   protected boolean isLiveImage() { return isRotate(); }

   private void RotateStep() {
      if (!isRotate())
         return;

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
   protected void close(boolean disposing) {
      if (disposing) {
         // free managed resources
         Timer t = _timer;
         if (t != null) {
            t.removeActionListener(timerListener);
            t.stop();
         }
         _timer = null;
      }
      // free native resources if there are any.
   }

}
