using System;
using Avalonia.Threading;
using Fmg.Common.UI;

namespace Fmg.Ava.Utils {

    public class Timer : ITimer {

        private DispatcherTimer _timer;
        private long _interval = 200;
        private Action<ITimer> _callback;
        private bool _paused = true;
        /// <summary> if played: it`s start time; if paused: time offset </summary>
        private long _started;

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
            _callback?.Invoke(this);
        }

        public Action<ITimer> Callback { set { _callback = value; } }

        public void Start()
        {
            if (!_paused)
                return;

            _paused = false;
            _started = DateTimeOffset.Now.ToUnixTimeMilliseconds() - _started; // apply of pause delta time

            if (_timer == null) {
                _timer = new DispatcherTimer { Interval = TimeSpan.FromMilliseconds(Interval) };
                _timer.Tick += OnTick;
            }
            _timer.Start();
        }

        public void Pause() {
            if (_paused)
                return;

            _paused = true;
            _started = DateTimeOffset.Now.ToUnixTimeMilliseconds() - _started; // set of pause delta time
            if (_timer != null)
                _timer.Stop();
        }

        public void Reset() {
            Pause();
            _started = 0;
        }

        public long Time {
            get {
                if (_paused)
                    return _started;
                return DateTimeOffset.Now.ToUnixTimeMilliseconds() - _started;
            }
            set {
                if (_paused)
                    _started = value;
                else
                    _started = DateTimeOffset.Now.ToUnixTimeMilliseconds() - value;
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
