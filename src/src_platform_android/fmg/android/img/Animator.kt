package fmg.android.img

import java.util.Date
import java.util.HashMap
import java.util.function.Consumer

import fmg.common.ui.ITimer
import fmg.core.img.IAnimator
import fmg.android.utils.Timer

class Animator private constructor() : IAnimator, AutoCloseable {

    private class SubscribeInfo {
        var active = true    // enabled?
        var startTime = Date().time // start time of subscribe
        lateinit var callback: Consumer<Long/* time from the beginning of the subscription */>
    }

    private val _timer: ITimer
    private val _subscribers: MutableMap<Any, SubscribeInfo/* subscriber */>

    init {
        _subscribers = HashMap()
        _timer = Timer()
        _timer.interval = (1000 / 60).toLong() // The number of frames per second
        _timer.setCallback {
            val currentTime = Date().time
            _subscribers.forEach { k, v ->
                if (v.active)
                    v.callback.accept(currentTime - v.startTime)
            }
        }
    }

    override fun subscribe(subscriber: Any, subscriberCallbackMethod: Consumer<Long/* time from start subscribe */>) {
        var info: SubscribeInfo? = _subscribers[subscriber]
        if (info == null) {
            info = SubscribeInfo()
            info.callback = subscriberCallbackMethod
            _subscribers[subscriber] = info
        } else {
            info.active = true
            info.startTime = Date().time - info.startTime // apply of pause delta time
        }
    }

    override fun pause(subscriber: Any) {
        val info = _subscribers[subscriber] ?: return
        info.active = false
        info.startTime = Date().time - info.startTime // set of pause delta time
    }

    override fun unsubscribe(subscriber: Any) {
        _subscribers.remove(subscriber)
    }

    override fun close() {
        _timer.setCallback(null)
        _timer.close()
        _subscribers.clear()
    }

    companion object {

        private var _singleton: Animator? = null // not synchronized. since should work only in the thread of the UI.

        val singleton: Animator
            get() {
                if (_singleton == null)
                    _singleton = Animator()
                return _singleton!!
            }
    }

}
