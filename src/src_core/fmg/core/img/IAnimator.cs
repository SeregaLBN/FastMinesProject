using System;

namespace fmg.core.img {

    /// <summary> Perform tasks for calculating / changing the subscribers animation. Executed in the current UI thread </summary>
    public interface IAnimator {

        void Subscribe(object subscriber, Action<TimeSpan /** time in ms from start subscribe */> subscriberCallbackMethod);
        void Pause(object subscriber);
        void Unsubscribe(object subscriber);

    }

}
