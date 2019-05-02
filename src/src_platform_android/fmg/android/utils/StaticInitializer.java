package fmg.android.utils;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import fmg.android.img.Animator;
import fmg.common.Color;
import fmg.common.LoggerSimple;
import fmg.common.ui.UiInvoker;
import fmg.core.mosaic.MosaicDrawModel;

public final class StaticInitializer {
    private StaticInitializer() {}

    /**/ // debug
    public enum EProofDrawMode {
        eFull,
        eBorderOnly;

        public boolean isBorderOnly() { return this == eBorderOnly; }
    }

    public static final EProofDrawMode DrawMode = EProofDrawMode.eBorderOnly;
    /**/

    static {
        UiInvoker.DEFERRED = new Handler(Looper.getMainLooper())::post;
        UiInvoker.ANIMATOR = Animator::getSingleton;
        UiInvoker.TIMER_CREATOR = Timer::new;

        MosaicDrawModel.DefaultBkColor = new Color(0xFFEEEEEE); // #EEEEEE or #FAFAFA

        LoggerSimple.DEFAULT_WRITER = message -> Log.d("fmg", message);
    }

    public static void init() {
        // implicit call static block
    }

}
