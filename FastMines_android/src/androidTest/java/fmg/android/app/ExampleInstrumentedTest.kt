package fmg.android.app;

import android.content.Context;
import android.os.Looper;
import android.support.test.InstrumentationRegistry;
import android.support.test.filters.SmallTest;
import android.support.test.runner.AndroidJUnit4;
import android.util.Log;

import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import fmg.android.img.Animator;
import fmg.android.utils.Timer;

import static org.junit.Assert.*;

/**
 * Instrumented test, which will execute on an Android device.
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
@RunWith(AndroidJUnit4.class)
@SmallTest
public class ExampleInstrumentedTest {

   @Test
   public void useAppContext() {
      // Context of the app under test.
      Context appContext = InstrumentationRegistry.getTargetContext();
      assertEquals("fmg.android.app", appContext.getPackageName());
   }


   @Test
   public void uiTimerTest() throws InterruptedException {
      String logTag = "uiTimerTest";
      Log.d(logTag, "> begin...");

      int[] fireCount = { 0 };

      assertFalse(Looper.getMainLooper().isCurrentThread());

      try (Timer t = new Timer()) {
         t.setInterval(100);
         t.setCallback(() -> {
            ++fireCount[0];
            Log.d(logTag, "  timer callback: fireCount=" + fireCount[0]);
            assertTrue("Must be main UI thread!", Looper.getMainLooper().isCurrentThread());
         });

         new CountDownLatch(1).await(1, TimeUnit.SECONDS);
      }

      boolean succ = fireCount[0] > 0;
      if (succ)
         Log.d(logTag, "< end...");
      else
         Log.e(logTag, "< end...");

      assertTrue(succ);
   }


   @Test
   public void animatorTest() throws InterruptedException {
      String logTag = "animatorTest";
      Log.d(logTag, "> begin...");

      int[] fireCount = { 0 };

      assertFalse(Looper.getMainLooper().isCurrentThread());

      Animator a = Animator.getSingleton();
      a.subscribe(this, delta -> {
         ++fireCount[0];
         Log.d(logTag, "  subscriber: fireCount=" + fireCount[0]);
         assertTrue("Must be main UI thread!", Looper.getMainLooper().isCurrentThread());
      });
      new CountDownLatch(1).await(300, TimeUnit.MILLISECONDS);

      boolean succ = fireCount[0] > 0;
      if (succ)
         Log.d(logTag, "< end...");
      else
         Log.e(logTag, "< end...");

      assertTrue(succ);
   }

}
