package fmg.common.ui;

import java.util.function.Consumer;
import java.util.function.Supplier;

import fmg.core.img.IAnimator;

/** Factory of UI timers/animators/deferred invokers */
public final class UiInvoker {
    private UiInvoker() {}

    /** Delayed execution in the thread of the user interface. */
    public static Consumer<Runnable> DEFERRED = run -> {
        throw new UnsupportedOperationException("Not implemented...");
//        Logger.info("need redefine!");
//        run.run();
    };

    /** Platform-dependent factory of {@link IAnimator}. Set from outside... */
    public static Supplier<IAnimator> ANIMATOR = () -> {
        throw new UnsupportedOperationException("Not implemented...");
    };

    /** Platform-dependent factory of {@link ITimer}. Set from outside... */
    public static Supplier<ITimer> TIMER_CREATOR = () -> {
        throw new UnsupportedOperationException("Not implemented...");
    };

}
