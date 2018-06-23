using System;
using System.Diagnostics;
using System.Linq;
using System.Collections.Generic;
using System.Reactive.Linq;
using Avalonia.Data;
using Avalonia.Threading;
using Avalonia.Animation;
using fmg.core.img;

namespace fmg.jfx.draw.img {

   public class Animator : IAnimator, IDisposable {

      private class SubscribeInfo {
         public bool active = true;    // enabled?
         public long startTime = Stopwatch.Elapsed.Ticks; // start time of subscribe
         public Action<long /* time from the beginning of the subscription */> callback;
      }
      private readonly IDisposable _unsubscribe;
      private readonly IDictionary<object /* subscriber */, SubscribeInfo> _subscribers;

      private static Animator _singleton;
      public static Animator getSingleton() { // not synchronized. since should work only in the thread of the UI.
         if (_singleton == null)
            _singleton = new Animator();
         return _singleton;
      }

      private Animator() {
         _subscribers = new Dictionary<object, SubscribeInfo>();
         var startTime = Stopwatch.Elapsed.Ticks;
         IObservable<double> timer = Avalonia.Animation.Animate
               .Select(x => (x.Ticks - startTime) / (double)duration.Ticks)
               .StartWith(0.0)
               .Concat(Observable.Return(1.0));
         _unsubscribe = timer.Subscribe(
               value => {
                  long currentTime = DateTime.Now.Ticks;
                  foreach (var item in _subscribers) {
                     var subscribeInfo = item.Value;
                     if (subscribeInfo.active)
                        subscribeInfo.callback(currentTime - subscribeInfo.startTime);
                  }
               },
               ex => { System.Diagnostics.Debug.Assert(false, ex.ToString()); },
               () => { System.Diagnostics.Debug.WriteLine("Closed"); }
            );
      }

      public void Subscribe(object subscriber, Action<long /* time from start subscribe */> subscriberCallbackMethod) {
         SubscribeInfo info = _subscribers[subscriber];
         if (info == null) {
            info = new SubscribeInfo();
            info.callback = subscriberCallbackMethod;
            _subscribers.Add(subscriber, info);
         } else {
            info.active = true;
            info.startTime = DateTime.Now.Ticks - info.startTime; // apply of pause delta time
         }
      }

      public void Pause(object subscriber) {
         SubscribeInfo info = _subscribers[subscriber];
         if (info == null)
            return;
         info.active = false;
         info.startTime = DateTime.Now.Ticks - info.startTime; // set of pause delta time
      }

      public void Unsubscribe(object subscriber) {
         _subscribers.Remove(subscriber);
      }

      public void Dispose() {
         _unsubscribe.Dispose();
         _subscribers.Clear();
      }

   }

}
