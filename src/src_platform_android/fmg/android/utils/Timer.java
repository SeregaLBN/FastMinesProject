package fmg.android.utils;

import android.os.Handler;
import android.os.Looper;

import fmg.common.ui.ITimer;

public class Timer implements ITimer {

    private Handler _timer;
    private long _interval = 200;
    private Runnable _callback;
    private Runnable _repeat;

    public Timer() {
        _repeat = () -> {
            _callback.run();
            if (_timer != null)
                _timer.postDelayed(_repeat, _interval);
        };
    }

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
        _timer = new Handler(Looper.getMainLooper());
        _timer.postDelayed(_repeat, _interval);
    }

    private void clean() {
        if (_timer == null)
            return;

        _timer.removeCallbacks(_repeat);
        _timer = null;
        _callback = null;
    }

    @Override
    public void close() {
        clean();
    }

}
