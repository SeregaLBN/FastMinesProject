package fmg.android.utils;

import android.os.Handler;
import android.os.Looper;

import java.util.function.Supplier;

public final class AsyncRunner {
    private void AsyncRunner() {}

    public static void RunWithDelay(Runnable run, long delayMs) {
        new Handler(Looper.getMainLooper()).postAtTime(run, delayMs);
    }

    public static void RepeatNoWait(Runnable run, long delay, Supplier<Boolean> cancelation) {
        Runnable[] run2 = { null };
        run2[0] = () -> {
            if (cancelation.get())
                return; // stop
            run.run();
            RunWithDelay(run2[0], delay); // repeat
        };
        RunWithDelay(run2[0], delay); // start
    }

}
