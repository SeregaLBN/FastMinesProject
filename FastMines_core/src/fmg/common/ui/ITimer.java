package fmg.common.ui;

import java.util.function.Consumer;

/** Timer interface. Pulsates in the UI thread. */
public interface ITimer extends AutoCloseable {

    /** get repeat interval in milliseconds */
    long getInterval();
    /** set repeat interval in milliseconds */
    void setInterval(long delay);

    /** set timer callback */
    void setCallback(Consumer<ITimer> callback);

    /** start / continue timer */
    void start();
    /** stop timer, without reset */
    void pause();
    /** reset timer */
    void reset();

    /** total time in milliseconds after first start / restart */
    long getTime();
    void setTime(long time);

    @Override
    void close();

}
