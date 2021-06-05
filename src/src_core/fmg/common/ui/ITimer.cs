using System;

namespace Fmg.Common.UI {

    /// <summary> Timer interface. Pulsates in the UI thread </summary>
    public interface ITimer : IDisposable {

        /// <summary> in miliseconds </summary>
        long Interval { get; set; }

        /// <summary> set timer callback </summary>
        Action<ITimer> Callback { set; }

        /// <summary> start / continue timer </summary>
        void Start();
        /// <summary> stop timer, without reset </summary>
        void Pause();
        /// <summary> reset timer </summary>
        void Reset();

        /// <summary> total time in milliseconds after first start / restart </summary>
        long Time { get; }

    }

}
