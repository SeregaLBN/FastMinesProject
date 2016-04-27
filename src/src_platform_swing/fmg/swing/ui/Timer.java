package fmg.swing.ui;

import java.awt.event.ActionListener;

import fmg.common.ui.ITimer;

public class Timer implements ITimer {

   private javax.swing.Timer _timer;
   private long _interval = 200;

   @Override
   public long getInterval() { return _interval; }

   @Override
   public void setInterval(long delay) {
      _interval = delay;
      if (_timer != null)
         _timer.setDelay((int)delay);
   }

   @Override
   public void setCallback(Runnable cb) {
      clean();
      if (cb == null)
         return;

      _timer = new javax.swing.Timer((int)_interval, evt -> cb.run());
      _timer.setRepeats(true);
      _timer.start();
   }

   private void clean() {
      if (_timer == null)
         return;

      _timer.stop();
      for (ActionListener al : _timer.getActionListeners())
         _timer.removeActionListener(al);
      _timer = null;
   }

   @Override
   public void close() {
      clean();
   }

}
