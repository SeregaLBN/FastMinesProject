package fmg.android.utils;

import android.os.Handler;
import android.os.Looper;

import java.util.function.BooleanSupplier;
import java.util.function.Supplier;

public final class AsyncRunner {
    private void AsyncRunner() {}

    /** send for execution to the UI thread */
    public static void invokeFromUi(Runnable run) {
        new Handler(Looper.getMainLooper()).post(run);
    }

    /** send for execution to the UI thread with a delay */
    public static void invokeFromUiDelayed(Runnable run, long delayMs) {
        new Handler(Looper.getMainLooper()).postDelayed(run, delayMs);
    }

    public static void Repeat(Runnable run, long delayMs, BooleanSupplier cancelation) {
        Runnable[] run2 = { null };
        run2[0] = () -> {
            if (cancelation.getAsBoolean())
                return; // stop
            run.run();
            invokeFromUiDelayed(run2[0], delayMs); // repeat
        };
        run2[0].run(); // start
    }

}
