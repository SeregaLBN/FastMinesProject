using System;
using System.Collections.Generic;
using System.Reactive.Linq;
using Avalonia.Animation;

namespace fmg.ava.img {

    public class Animator : fmg.core.img.IAnimator, IDisposable {

        private class SubscribeInfo {
            public bool active = true;    // enabled?
            public DateTime startTime = DateTime.Now; // start time of subscribe
            public Action<TimeSpan /* time from the beginning of the subscription */> callback;
        }
        private readonly IDisposable _unsubscribe;
        private readonly IDictionary<object /* subscriber */, SubscribeInfo> _subscribers;
        private readonly Animatable animatable;

        private static Animator _singleton;
        public static Animator Singleton { get { // not synchronized. since should work only in the thread of the UI.
            if (_singleton == null)
                _singleton = new Animator();
            return _singleton;
        } }

        private Animator() {
            _subscribers = new Dictionary<object, SubscribeInfo>();
            animatable = new Animatable {
                Clock = new Clock()
            };
            _unsubscribe = animatable.Clock.Subscribe(
                value => {
                    var currentTime = DateTime.Now;
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

        public void Subscribe(object subscriber, Action<TimeSpan /* time from start subscribe */> subscriberCallbackMethod) {
            if (!_subscribers.ContainsKey(subscriber)) {
                var info = new SubscribeInfo {
                    callback = subscriberCallbackMethod
                };
                _subscribers.Add(subscriber, info);
            } else {
                var info = _subscribers[subscriber];
                info.active = true;
                info.startTime = DateTime.MinValue + (DateTime.Now - info.startTime); // apply of pause delta time
            }
        }

        public void Pause(object subscriber) {
            SubscribeInfo info = _subscribers[subscriber];
            if (info == null)
                return;
            info.active = false;
            info.startTime = DateTime.MinValue + (DateTime.Now - info.startTime); // set of pause delta time
        }

        public void Unsubscribe(object subscriber) {
            _subscribers.Remove(subscriber);
        }

        public void Dispose() {
            _unsubscribe.Dispose();
            _subscribers.Clear();
            animatable.Clock = null;
        }

    }

}
