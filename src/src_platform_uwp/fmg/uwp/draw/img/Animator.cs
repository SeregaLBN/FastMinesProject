using System;
using System.Linq;
using System.Collections.Generic;
using fmg.common.ui;
using fmg.core.img;

namespace fmg.uwp.draw.img {

   public class Animator : IAnimator, IDisposable {

      private class SubscribeInfo {
         public bool active = true;    // enabled?
         public DateTime startTime = DateTime.Now; // start time of subscribe
         public Action<TimeSpan /* time from the beginning of the subscription */> callback;
      }
      private readonly ITimer _timer;
      private readonly IDictionary<object /* subscriber */, SubscribeInfo> _subscribers;

      private static Animator _singleton;
      public static Animator getSingleton() { // not synchronized. since should work only in the thread of the UI.
         if (_singleton == null)
            _singleton = new Animator();
         return _singleton;
      }

      private Animator() {
         _subscribers = new Dictionary<object, SubscribeInfo>();
         _timer = new Timer();
         _timer.setInterval(1000 / 60); // The number of frames per second
         _timer.setCallback(() => {
            long currentTime = new Date().getTime();
            _subscribers.forEach((k, v) => {
               if (v.active)
                  v.callback.accept(currentTime - v.startTime);
            });
         });
      }

      public void Subscribe(object subscriber, Action<TimeSpan /* time from start subscribe */> subscriberCallbackMethod) {
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

      public void Pause(object subscriber) {
         SubscribeInfo info = _subscribers.get(subscriber);
         if (info == null)
            return;
         info.active = false;
         info.startTime = new Date().getTime() - info.startTime; // set of pause delta time
      }

      public void Unsubscribe(object subscriber) {
         _subscribers.remove(subscriber);
      }

      public void Dispose() {
         _timer.setCallback(null);
         _timer.close();
         _subscribers.Clear();
      }

   }

}
