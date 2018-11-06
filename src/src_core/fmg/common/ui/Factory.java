package fmg.common.ui;

import java.util.function.Consumer;
import java.util.function.Supplier;

import fmg.core.img.IAnimator;

/** Factory of UI timers/animators/deffer invokers */
public final class Factory {

    /** Delayed execution in the current thread of the user interface. */
    public static Consumer<Runnable> DEFERR_INVOKER = run -> {
        throw new UnsupportedOperationException("Not implemented...");
//        System.out.println("need redefine!");
//        run.run();
    };

    /** Platform-dependent factory of {@link IAnimator}. Set from outside... */
    public static Supplier<IAnimator> GET_ANIMATOR = () -> {
        throw new UnsupportedOperationException("Not implemented...");
    };

    /** Platform-dependent factory of {@link ITimer}. Set from outside... */
    public static Supplier<ITimer> TIMER_CREATOR = () -> {
        throw new UnsupportedOperationException("Not implemented...");
    };

}
