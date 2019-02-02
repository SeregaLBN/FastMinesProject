package fmg.jfx.utils;

import fmg.common.ui.Factory;
import fmg.jfx.img.Animator;

public final class StaticInitializer {

    private StaticInitializer() {}

    static {
        Factory.DEFERR_INVOKER = javafx.application.Platform::runLater;
        Factory.GET_ANIMATOR = Animator::getSingleton;
        Factory.TIMER_CREATOR = Timer::new;
    }

    public static void init() {
        // implicit call static block
    }

}
