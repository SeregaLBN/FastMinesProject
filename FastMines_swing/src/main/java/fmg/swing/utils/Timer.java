package fmg.swing.utils;

import java.awt.event.ActionListener;
import java.util.Date;
import java.util.function.Consumer;

import fmg.common.ui.ITimer;

/** Swing timer. Worked in UI thread */
public class Timer implements ITimer {

    private javax.swing.Timer swTimer;
    private long interval = 200;
    private Consumer<ITimer> callback;
    private boolean paused = true;
    /** if played: it`s start time; if paused: time offset */
    private long started;

    @Override
    public long getInterval() { return interval; }

    @Override
    public void setInterval(long delay) {
        this.interval = delay;
        if (swTimer != null)
            swTimer.setDelay((int)delay);
    }

    private void myCallback() {
        if (callback != null)
            callback.accept(this);
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

        if (swTimer == null) {
            swTimer = new javax.swing.Timer((int)interval, ev -> myCallback());
            swTimer.setRepeats(true);
        }
        swTimer.start();
    }

    @Override
    public void pause() {
        if (paused)
            return;

        paused = true;
        started = new Date().getTime() - started; // set of pause delta time
        if (swTimer != null)
            swTimer.stop();
    }

    @Override
    public void reset() {
        pause();
        started = 0;
    }

    @Override
    public long getTime() {
        if (paused)
            return started;
        return new Date().getTime() - started;
    }

    @Override
    public void setTime(long time) {
        if (paused)
            started = time;
        else
            started = new Date().getTime() - time;
    }

    @Override
    public void close() {
        callback = null;
        if (swTimer != null) {
            swTimer.stop();
            for (ActionListener al : swTimer.getActionListeners())
                swTimer.removeActionListener(al);
            swTimer = null;
        }
    }

}
