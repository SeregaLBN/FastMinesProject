package fmg.android.utils;

import android.os.Handler;
import android.os.Looper;

import fmg.android.draw.img.Animator;
import fmg.common.ui.Factory;

public final class StaticInitializer {

   static {
      Factory.DEFERR_INVOKER = new Handler(Looper.getMainLooper())::post;
      Factory.GET_ANIMATOR = Animator::getSingleton;
      Factory.TIMER_CREATOR = Timer::new;
   }

   public static void init() {
      // implicit call static block
   }

}
