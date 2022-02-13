package fmg.common.ui;

import java.util.function.Consumer;
import java.util.function.Supplier;

import fmg.core.img.IAnimator;

/** Factory of UI timers/animators/deferred invokers */
public final class UiInvoker {
    private UiInvoker() {}

    /** Deferred execution in the UI thread */
    public static Consumer<Runnable> Deferred = run -> {
        throw new UnsupportedOperationException("Not implemented...");
    };

    /** Platform-dependent factory of {@link IAnimator}. Set from outside... */
    public static Supplier<IAnimator> Animator = () -> {
        throw new UnsupportedOperationException("Not implemented...");
    };

    /** Platform-dependent factory of {@link ITimer}. Set from outside... */
    public static Supplier<ITimer> TimeCreator = () -> {
        throw new UnsupportedOperationException("Not implemented...");
    };

}
