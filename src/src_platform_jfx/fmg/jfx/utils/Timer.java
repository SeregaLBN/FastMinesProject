package fmg.jfx.utils;

import fmg.common.ui.ITimer;
import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.util.Duration;

public class Timer implements ITimer {

   private Timeline _timer;
   private long _interval = 200;
   private Runnable _callback;

   @Override
   public long getInterval() { return _interval; }

   @Override
   public void setInterval(long delay) {
      _interval = delay;
      setCallback(_callback);
   }

   @Override
   public void setCallback(Runnable cb) {
      if (cb == _callback)
         return;

      clean();
      if (cb == null)
         return;

      _callback = cb;
      _timer = new Timeline(new KeyFrame(Duration.millis((int)_interval),
                                         ev -> cb.run()));
      _timer.setCycleCount(Animation.INDEFINITE);
      _timer.play();
   }

   private void clean() {
      if (_timer == null)
         return;

      _timer.stop();
      _timer = null;
      _callback = null;
   }

   @Override
   public void close() {
      clean();
   }

}
