package fmg.jfx.utils;

import fmg.common.ui.UiInvoker;
import fmg.jfx.img.Animator;

public final class StaticInitializer {

    private StaticInitializer() {}

    static {
        UiInvoker.DEFERRED = javafx.application.Platform::runLater;
        UiInvoker.ANIMATOR = Animator::getSingleton;
        UiInvoker.TIMER_CREATOR = Timer::new;
    }

    public static void init() {
        // implicit call static block
    }

}
