package fmg.jfx.draw.img;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

import javafx.animation.AnimationTimer;

import fmg.core.img.IAnimator;

public class Animator implements IAnimator, AutoCloseable {

   private static class SubscribeInfo {
      public boolean active = true;    // enabled?
      public long    startTime = new Date().getTime(); // start time of subscribe
      public Consumer<Long /* time from the beginning of the subscription */> callback;
   }
   private final AnimationTimer _timer;
   private final Map<Object /* subscriber */, SubscribeInfo> _subscribers;

   private static Animator _singleton;
   public static Animator getSingleton() { // not synchronized. since should work only in the thread of the UI.
      if (_singleton == null)
         _singleton = new Animator();
      return _singleton;
   }

   private Animator() {
      _subscribers = new HashMap<>();
      _timer = new AnimationTimer() {

          @Override
          public void handle(long now) {
              long currentTime = new Date().getTime();
              _subscribers.forEach((k,v) -> {
                 if (v.active)
                    v.callback.accept(currentTime - v.startTime);
              });
          }

      };
      _timer.start();
   }

   @Override
   public void subscribe(Object subscriber, Consumer<Long /* time from start subscribe */> subscriberCallbackMethod) {
      SubscribeInfo info = _subscribers.get(subscriber);
      if (info == null) {
         info = new SubscribeInfo();
         info.callback = subscriberCallbackMethod;
         _subscribers.put(subscriber, info);
      } else {
         info.active = true;
         info.startTime = new Date().getTime() - info.startTime; // apply of pause delta time
      }
   }

   @Override
   public void pause(Object subscriber) {
      SubscribeInfo info = _subscribers.get(subscriber);
      if (info == null)
         return;
      info.active = false;
      info.startTime = new Date().getTime() - info.startTime; // set of pause delta time
   }

   @Override
   public void unsubscribe(Object subscriber) {
      _subscribers.remove(subscriber);
   }

   @Override
   public void close() {
      _timer.stop();
      _subscribers.clear();
   }

}
