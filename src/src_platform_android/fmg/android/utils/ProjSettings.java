package fmg.android.utils;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import fmg.android.app.BuildConfig;
import fmg.android.img.Animator;
import fmg.common.AProjSettings;
import fmg.common.Color;
import fmg.common.LoggerSimple;
import fmg.common.ui.UiInvoker;
import fmg.core.mosaic.MosaicDrawModel;

public final class ProjSettings extends AProjSettings {
    private ProjSettings() {}

    private static final boolean DrawModeFull;
    public static boolean isDrawModeFull() { return DrawModeFull; }

    static {
        UiInvoker.DEFERRED = new Handler(Looper.getMainLooper())::post;
        UiInvoker.ANIMATOR = Animator::getSingleton;
        UiInvoker.TIMER_CREATOR = Timer::new;

        MosaicDrawModel.DefaultBkColor = new Color(0xFFEEEEEE); // #EEEEEE or #FAFAFA

        AProjSettings.setDebug(BuildConfig.DEBUG);
        LoggerSimple.DEFAULT_WRITER = BuildConfig.DEBUG_OUTPUT ? message -> Log.d("fmg", message) : null;
        DrawModeFull = BuildConfig.DRAW_MODE_FULL;
    }

    public static void init() {
        // implicit call static block
    }

}
