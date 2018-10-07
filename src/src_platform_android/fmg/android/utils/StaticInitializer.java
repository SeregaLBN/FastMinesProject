package fmg.android.utils;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import fmg.android.img.Animator;
import fmg.common.Color;
import fmg.common.LoggerSimple;
import fmg.common.ui.Factory;
import fmg.core.mosaic.MosaicDrawModel;

public final class StaticInitializer {

    static {
        Factory.DEFERR_INVOKER = new Handler(Looper.getMainLooper())::post;
        Factory.GET_ANIMATOR = Animator::getSingleton;
        Factory.TIMER_CREATOR = Timer::new;

        MosaicDrawModel.DefaultBkColor = new Color(0xFFEEEEEE); // #EEEEEE or #FAFAFA

        LoggerSimple.DEFAULT_WRITER = message -> Log.d("fmg", message);
    }

    public static void init() {
        // implicit call static block
    }

}
