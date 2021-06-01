package fmg.android.utils;

import android.os.Handler;
import android.os.Looper;

import java.util.Date;
import java.util.function.Consumer;

import fmg.common.ui.ITimer;

/** Android timer. Worked in UI thread */
public class Timer implements ITimer {

    private Handler aTimer;
    private long interval = 200;
    private Consumer<ITimer> callback;
    private boolean paused = true;
    /** if played: it`s start time; if paused: time offset */
    private long started;

    @Override
    public long getInterval() { return interval; }

    @Override
    public void setInterval(long delay) {
        long old = interval;
        interval = delay;
        if ((interval != old) && (aTimer != null)) {
            aTimer.removeCallbacks(this::myCallback);
            aTimer.postDelayed(this::myCallback, interval);
        }
    }

    private void myCallback() {
        if (callback != null)
            callback.accept(this);
        if (aTimer != null)
            aTimer.postDelayed(this::myCallback, interval); // repeat
    }


    @Override
    public void setCallback(Consumer<ITimer> callback) {
        this.callback = callback;
    }

    @Override
    public void start() {
        if (!paused)
            return;

        paused = false;
        started = new Date().getTime() - started; // apply of pause delta time

        if (aTimer == null)
            aTimer = new Handler(Looper.getMainLooper());
        aTimer.postDelayed(this::myCallback, interval);
    }

    @Override
    public void pause() {
        if (paused)
            return;

        paused = true;
        started = new Date().getTime() - started; // set of pause delta time

        if (aTimer != null)
            aTimer.removeCallbacks(this::myCallback);
    }

    @Override
    public void restart() {
        pause();
        started = 0;
        start();
    }

    @Override
    public long getTime() {
        if (paused)
            return started;
        return new Date().getTime() - started;
    }

    @Override
    public void close() {
        callback = null;

        if (aTimer != null) {
            aTimer.removeCallbacks(this::myCallback);
            aTimer = null;
        }
    }

}
