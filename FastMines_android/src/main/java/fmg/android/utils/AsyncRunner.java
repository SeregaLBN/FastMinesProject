package fmg.android.utils;

import android.os.Handler;
import android.os.Looper;

import java.util.function.Supplier;

public final class AsyncRunner {
    private void AsyncRunner() {}

    public static void runWithDelay(Runnable run, long delayMs) {
        new Handler(Looper.getMainLooper()).postDelayed(run, delayMs);
    }

    public static void repeat(Runnable run, long delayMs, Supplier<Boolean> cancelation) {
        Runnable[] run2 = { null };
        run2[0] = () -> {
            if (cancelation.get())
                return; // stop
            run.run();
            runWithDelay(run2[0], delayMs); // repeat
        };
        run2[0].run(); // start
    }

}
