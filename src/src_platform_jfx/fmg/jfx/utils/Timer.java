package fmg.jfx.utils;

import java.util.Date;
import java.util.function.Consumer;

import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.util.Duration;

import fmg.common.ui.ITimer;

/** JavaFX timer. Worked in UI thread */
public class Timer implements ITimer {

    private Timeline jfxTimer;
    private long interval = 200;
    private Consumer<ITimer> callback;
    private boolean paused = true;
    /** if played: it`s start time; if paused: time offset */
    private long started;

    @Override
    public long getInterval() { return interval; }

    @Override
    public void setInterval(long delay) {
        interval = delay;
        if (jfxTimer != null)
            jfxTimer.setDelay(Duration.millis((int)interval));
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

        if (jfxTimer == null) {
            jfxTimer = new Timeline(new KeyFrame(Duration.millis((int)interval),
                                                 ev -> myCallback()));
            jfxTimer.setCycleCount(Animation.INDEFINITE);
        }
        jfxTimer.play();
    }

    @Override
    public void pause() {
        if (paused)
            return;

        paused = true;
        started = new Date().getTime() - started; // set of pause delta time

        if (jfxTimer != null)
            jfxTimer.stop();
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
        if (jfxTimer != null) {
            jfxTimer.stop();
            jfxTimer = null;
        }
    }

}
