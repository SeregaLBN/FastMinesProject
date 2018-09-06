package fmg.android.app

import android.os.Looper
import android.support.test.InstrumentationRegistry
import android.support.test.filters.SmallTest
import android.support.test.runner.AndroidJUnit4
import android.util.Log

import org.junit.Test
import org.junit.runner.RunWith

import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

import fmg.android.img.Animator
import fmg.android.utils.Timer

import org.junit.Assert.*

/**
 * Instrumented test, which will execute on an Android device.
 *
 * @see [Testing documentation](http://d.android.com/tools/testing)
 */
@RunWith(AndroidJUnit4::class)
@SmallTest
class ExampleInstrumentedTest {

    @Test
    fun useAppContext() {
        // Context of the app under test.
        val appContext = InstrumentationRegistry.getTargetContext()
        assertEquals("fmg.android.app", appContext.packageName)
    }


    @Test
    @Throws(InterruptedException::class)
    fun uiTimerTest() {
        val logTag = "uiTimerTest"
        Log.d(logTag, "> begin...")

        val fireCount = intArrayOf(0)

        assertFalse(Looper.getMainLooper().isCurrentThread)

        Timer().use { t ->
            t.interval = 100
            t.setCallback {
                ++fireCount[0]
                Log.d(logTag, "  timer callback: fireCount=" + fireCount[0])
                assertTrue("Must be main UI thread!", Looper.getMainLooper().isCurrentThread)
            }

            CountDownLatch(1).await(1, TimeUnit.SECONDS)
        }

        val succ = fireCount[0] > 0
        if (succ)
            Log.d(logTag, "< end...")
        else
            Log.e(logTag, "< end...")

        assertTrue(succ)
    }


    @Test
    @Throws(InterruptedException::class)
    fun animatorTest() {
        val logTag = "animatorTest"
        Log.d(logTag, "> begin...")

        val fireCount = intArrayOf(0)

        assertFalse(Looper.getMainLooper().isCurrentThread)

        val a = Animator.singleton
        a.subscribe(this) { delta ->
            ++fireCount[0]
            Log.d(logTag, "  subscriber: fireCount=" + fireCount[0])
            assertTrue("Must be main UI thread!", Looper.getMainLooper().isCurrentThread)
        }
        CountDownLatch(1).await(300, TimeUnit.MILLISECONDS)

        val succ = fireCount[0] > 0
        if (succ)
            Log.d(logTag, "< end...")
        else
            Log.e(logTag, "< end...")

        assertTrue(succ)
    }

}
