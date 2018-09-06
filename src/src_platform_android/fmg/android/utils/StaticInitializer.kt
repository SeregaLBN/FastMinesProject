package fmg.android.utils

import java.util.function.Consumer
import java.util.function.Supplier

import android.os.Handler
import android.os.Looper
import android.util.Log

import fmg.common.Color
import fmg.common.LoggerSimple
import fmg.common.ui.ITimer
import fmg.common.ui.Factory
import fmg.core.img.IAnimator
import fmg.core.mosaic.MosaicDrawModel
import fmg.android.img.Animator

object StaticInitializer {

    init {
        Factory.DEFERR_INVOKER = Consumer<Runnable> { Handler(Looper.getMainLooper()).post(it) }
        Factory.GET_ANIMATOR = Supplier<IAnimator> { Animator.singleton }
        Factory.TIMER_CREATOR = Supplier<ITimer> { Timer() }

        MosaicDrawModel.DefaultBkColor = Color(-0x111112) // #EEEEEE or #FAFAFA

        LoggerSimple.DEFAULT_WRITER = Consumer<String> { Log.d("fmg", it) }
    }

    fun init() {
        // implicit call static block
    }

}
