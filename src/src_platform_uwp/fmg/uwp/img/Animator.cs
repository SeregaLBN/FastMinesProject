using System;
using System.Collections.Generic;
using Fmg.Common.UI;
using Fmg.Core.Img;
using Fmg.Uwp.Utils;

namespace Fmg.Uwp.Img {

    public class Animator : IAnimator, IDisposable {

        private class SubscribeInfo {
            public bool active = true;    // enabled?
            public DateTime startTime = DateTime.Now; // start time of subscribe
            public Action<TimeSpan /* time from the beginning of the subscription */> callback;
        }
        private readonly ITimer _timer;
        private readonly IDictionary<object /* subscriber */, SubscribeInfo> _subscribers;

        private static Animator _singleton;
        public static Animator Singleton { get { // not synchronized. since should work only in the thread of the UI.
            if (_singleton == null)
                _singleton = new Animator();
            return _singleton;
        } }

        private Animator() {
            _subscribers = new Dictionary<object, SubscribeInfo>();
            _timer = new Timer {
                Interval = (1000 / 60), // The number of frames per second
                Callback = () => {
                    var currentTime = DateTime.Now;
                    foreach (var kv in _subscribers) {
                        if (kv.Value.active)
                            kv.Value.callback(currentTime - kv.Value.startTime);
                    }
                }
            };
        }

        public void Subscribe(object subscriber, Action<TimeSpan /* time from start subscribe */> subscriberCallbackMethod) {
            if (!_subscribers.ContainsKey(subscriber)) {
                var info = new SubscribeInfo();
                info.callback = subscriberCallbackMethod;
                _subscribers.Add(subscriber, info);
            } else {
                var info = _subscribers[subscriber];
                info.active = true;
                info.startTime = DateTime.MinValue + (DateTime.Now - info.startTime); // apply of pause delta time
            }
        }

        public void Pause(object subscriber) {
            SubscribeInfo info;
            if (!_subscribers.TryGetValue(subscriber, out info))
                return;
            info.active = false;
            info.startTime = DateTime.MinValue + (DateTime.Now - info.startTime); // set of pause delta time
        }

        public void Unsubscribe(object subscriber) {
            _subscribers.Remove(subscriber);
        }

        public void Dispose() {
            _timer.Callback = null;
            _timer.Dispose();
            _subscribers.Clear();
        }

    }

}
