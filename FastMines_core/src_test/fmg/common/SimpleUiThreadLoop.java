package fmg.common;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

/** very simple UI thread loop emulation */
public class SimpleUiThreadLoop {

    private static final SimpleUiThreadLoop Instance = new SimpleUiThreadLoop();
    public static void addTask(Runnable task) {   Instance.scheduler.execute(task);
                                                //Instance.scheduler.schedule(task, 10, TimeUnit.MILLISECONDS);
                                              }

    private final ScheduledExecutorService scheduler =  Executors.newScheduledThreadPool(1);
    private SimpleUiThreadLoop() { }

}
