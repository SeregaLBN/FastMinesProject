using System;

namespace fmg.common.ui {

   public interface ITimer : IDisposable {

      /// <summary> in miliseconds </summary>
      long Interval { get; set; }

      /// <summary> set null - stop timer; otherwise - started </summary>
      Action Callback { set; }

   }

}
