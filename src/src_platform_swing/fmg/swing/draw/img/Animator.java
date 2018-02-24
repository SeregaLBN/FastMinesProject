package fmg.swing.draw.img;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

import fmg.common.Pair;
import fmg.common.ui.ITimer;
import fmg.core.img.IAnimator;
import fmg.swing.utils.Timer;

public class Animator implements IAnimator, AutoCloseable {

   private static Animator _singleton;
   public static Animator getSingleton() {
      if (_singleton == null)
         _singleton = new Animator();
      return _singleton;
   }

   private Animator() {
      _subscribers = new HashMap<>();
      _timer = new Timer();
      _timer.setInterval(1000/60); // The number of frames per second
      _timer.setCallback(() -> {
         long currentTime = new Date().getTime();
         _subscribers.forEach((k,v) -> v.second.accept(currentTime - v.first));
      } );
   }

   private final ITimer _timer;
   private final Map<Object, Pair<Long /* start time of subscribe */, Consumer<Long /* time from the beginning of the subscription */>>> _subscribers;

   @Override
   public void subscribe(Object subscriber, Consumer<Long /* time from start subscribe */> subscriberCallbackMethod) {
      _subscribers.put(subscriber, new Pair<>(new Date().getTime(), subscriberCallbackMethod));
   }

   @Override
   public void unsubscribe(Object subscriber) {
      _subscribers.remove(subscriber);
   }

   @Override
   public void close() {
      _timer.setCallback(null);
      _timer.close();
      _subscribers.clear();
   }

}
