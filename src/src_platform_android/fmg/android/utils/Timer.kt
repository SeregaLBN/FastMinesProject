package fmg.android.utils

import android.os.Handler
import android.os.Looper

import fmg.common.ui.ITimer

class Timer : ITimer {

    private var _timer: Handler? = null
    private var _interval: Long = 200
    private var _callback: Runnable? = null
    private lateinit var _repeat: Runnable

    init {
        _repeat = Runnable {
            _callback!!.run()
            if (_timer != null)
                _timer!!.postDelayed(_repeat, _interval)
        }
    }

    override fun getInterval(): Long {
        return _interval
    }

    override fun setInterval(delay: Long) {
        _interval = delay
        setCallback(_callback)
    }

    override fun setCallback(cb: Runnable?) {
        if (cb === _callback)
            return

        clean()
        if (cb == null)
            return

        _callback = cb
        _timer = Handler(Looper.getMainLooper())
        _timer!!.postDelayed(_repeat, _interval)
    }

    private fun clean() {
        if (_timer == null)
            return

        _timer!!.removeCallbacks(_repeat)
        _timer = null
        _callback = null
    }

    override fun close() {
        clean()
    }

}
