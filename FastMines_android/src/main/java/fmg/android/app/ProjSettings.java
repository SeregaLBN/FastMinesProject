package fmg.android.app;

import android.util.Log;

import fmg.android.app.model.dataSource.BaseDataSource;
import fmg.android.img.Animator;
import fmg.android.utils.AsyncRunner;
import fmg.android.utils.Cast;
import fmg.android.utils.Timer;
import fmg.common.Color;
import fmg.common.Logger;
import fmg.common.ui.UiInvoker;
import fmg.core.app.AProjSettings;
import fmg.core.mosaic.MosaicDrawModel;

public final class ProjSettings extends AProjSettings {
    private ProjSettings() {}

    /** Mobile (true) or Desktop/Tablet (false) */
    public static final boolean IS_MOBILE;
    /** android recommended size */
    public static final float MIN_TOUCH_SIZE = Cast.dpToPx(48);

    private static final boolean isDrawModeFullEnabled;
    public static boolean isDrawModeFull() { return isDrawModeFullEnabled; }


    static {
        UiInvoker.Deferred = AsyncRunner::invokeFromUi;
        UiInvoker.Animator = Animator::getSingleton;
        UiInvoker.TimeCreator = Timer::new;

        // various background colors: #E6FFFFFF #FFEEEEEE #FFFAFAFA
//      MosaicDrawModel.DefaultBkColor   = new Color(0xFFFAFAFA);
        MosaicDrawModel.DefaultCellColor = new Color(0xFFEEEEEE);
        BaseDataSource .DefaultBkColor   = new Color(0xFFEEEEEE);

        IS_MOBILE = true;

        setReleaseMode(!BuildConfig.DEBUG);
        setDebugOutput(BuildConfig.DEBUG_OUTPUT);

        if (!isReleaseMode()) {
            // https://developer.android.com/studio/publish
            // Configuring your application for release... At a minimum you need to remove Log calls...
            Logger.  ERROR_WRITER = message -> Log.e("fmg", message);
            Logger.WARNING_WRITER = message -> Log.w("fmg", message);
            Logger.   INFO_WRITER = message -> Log.i("fmg", message);
            Logger.  DEBUG_WRITER = isDebugOutput() ? message -> Log.d("fmg", message) : null;
        }
        Logger.USE_DATE_PREFIX = false;
        isDrawModeFullEnabled = BuildConfig.DRAW_MODE_FULL;
    }

    public static void init() {
        // implicit call static block
    }

}
