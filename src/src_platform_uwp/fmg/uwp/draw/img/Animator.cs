using System;
using System.Linq;
using System.Collections.Generic;
using System.Reactive.Linq;
using fmg.core.img;

namespace fmg.uwp.draw.img {

   public class Animator : IAnimator, IDisposable {

      private static class SubscribeInfo {
         public bool active = true;    // enabled?
         public long startTime = new Date().getTime(); // start time of subscribe
         public Consumer<Long /* time from the beginning of the subscription */> callback;
      }
      private final ITimer _timer;
      private final Map<Object /* subscriber */, SubscribeInfo> _subscribers;

      private static Animator _singleton;
      public static Animator getSingleton() { // not synchronized. since should work only in the thread of the UI.
         if (_singleton == null)
            _singleton = new Animator();
         return _singleton;
      }

      private Animator() {
         _subscribers = new HashMap<>();
         _timer = new Timer();
         _timer.setInterval(1000 / 60); // The number of frames per second
         _timer.setCallback(()-> {
            long currentTime = new Date().getTime();
            _subscribers.forEach((k, v)-> {
               if (v.active)
                  v.callback.accept(currentTime - v.startTime);
            });
         });
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
         _timer.setCallback(null);
         _timer.close();
         _subscribers.clear();
      }

   }

}
