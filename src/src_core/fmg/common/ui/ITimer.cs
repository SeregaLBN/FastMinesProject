using System;

namespace fmg.common.ui {

    /// <summary> Timer interface. Pulsates in the UI thread </summary>
    public interface ITimer : IDisposable {

        /// <summary> in miliseconds </summary>
        long Interval { get; set; }

        /// <summary> set null - stop timer; otherwise - started </summary>
        Action Callback { set; }

    }

}
