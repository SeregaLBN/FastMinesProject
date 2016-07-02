package fmg.swing.utils;

import java.awt.event.ActionListener;

import fmg.common.ui.ITimer;

public class Timer implements ITimer {

   private javax.swing.Timer _timer;
   private long _interval = 200;
   private Runnable _callback;

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
      if (cb == _callback)
         return;

      clean();
      if (cb == null)
         return;

      _callback = cb;
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
      _callback = null;
   }

   @Override
   public void close() {
      clean();
   }

}
