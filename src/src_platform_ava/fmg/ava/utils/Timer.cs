using System;
using Avalonia.Threading;
using fmg.common.ui;

namespace fmg.ava.utils {

   public class Timer : ITimer {

      private DispatcherTimer _timer;
      private long _interval = 200;
      private Action _callback;

      /// <summary> in miliseconds </summary>
      public long Interval {
         get { return _interval; }
         set {
            _interval = value;
            if (_timer != null)
               _timer.Interval = TimeSpan.FromMilliseconds(value);
         }
      }

      private void OnTick(object sender, object e) {
         _callback?.Invoke();
      }

      /// <summary> set null - stop timer; otherwise - started </summary>
      public Action Callback {
         set {
            if (ReferenceEquals(value, _callback))
               return;

            Clean();
            if (value == null)
               return;

            _callback = value;
            _timer = new DispatcherTimer { Interval = TimeSpan.FromMilliseconds(Interval) };
            _timer.Tick += OnTick;
            _timer.Start();
         }
      }

      private void Clean() {
         if (_timer == null)
            return;

         _timer.Tick -= OnTick;
         _timer.Stop();
         _timer = null;
         _callback = null;
      }

      public void Dispose() {
         Clean();
      }

   }

}
