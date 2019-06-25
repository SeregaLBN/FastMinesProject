package fmg.android.utils;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import fmg.android.app.BuildConfig;
import fmg.android.img.Animator;
import fmg.common.AProjSettings;
import fmg.common.Color;
import fmg.common.Logger;
import fmg.common.ui.UiInvoker;
import fmg.core.mosaic.MosaicDrawModel;

public final class ProjSettings extends AProjSettings {
    private ProjSettings() {}

    /** Mobile (true) or Desktop/Tablet (false) */
    public static final boolean isMobile;

    private static final boolean DrawModeFull;
    public static boolean isDrawModeFull() { return DrawModeFull; }

    static {
        UiInvoker.DEFERRED = new Handler(Looper.getMainLooper())::post;
        UiInvoker.ANIMATOR = Animator::getSingleton;
        UiInvoker.TIMER_CREATOR = Timer::new;

        MosaicDrawModel.DefaultBkColor = new Color(0xFFEEEEEE); // #EEEEEE or #FAFAFA

        isMobile = true;

        AProjSettings.setDebug(BuildConfig.DEBUG);
        Logger.DEFAULT_WRITER = BuildConfig.DEBUG_OUTPUT ? message -> Log.d("fmg", message) : null;
        DrawModeFull = BuildConfig.DRAW_MODE_FULL;
    }

    public static void init() {
        // implicit call static block
    }

}
