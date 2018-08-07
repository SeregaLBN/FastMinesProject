package fmg.android.utils;

import android.os.Handler;
import android.os.Looper;

import fmg.android.img.Animator;
import fmg.common.Color;
import fmg.common.ui.Factory;
import fmg.core.mosaic.MosaicDrawModel;

public final class StaticInitializer {

   static {
      Factory.DEFERR_INVOKER = new Handler(Looper.getMainLooper())::post;
      Factory.GET_ANIMATOR = Animator::getSingleton;
      Factory.TIMER_CREATOR = Timer::new;

      MosaicDrawModel.DefaultBkColor = new Color(0xFFEEEEEE); // #EEEEEE or #FAFAFA
   }

   public static void init() {
      // implicit call static block
   }

}
