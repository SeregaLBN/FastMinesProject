using System;
using fmg.core.img;

namespace fmg.common.ui {

   /// <summary> Factory of UI timers/animators/deffer invokers </summary>
   public static class Factory {

      /// <summary> Delayed execution in the current thread of the user interface. </summary>
      public static Action<Action> DEFERR_INVOKER = doRun => {
         throw new NotImplementedException();
       //System.Diagnostics.Debug.WriteLine("need redefine!");
       //doRun();
      };

      /// <summary> Platform-dependent factory of {@link IAnimator}. Set from outside... </summary>
      public static Func<IAnimator> GET_ANIMATOR = () => {
         throw new NotImplementedException();
      };

      /// <summary> Platform-dependent factory of {@link ITimer}. Set from outside... </summary>
      public static Func<ITimer> TIMER_CREATOR = () => {
         throw new NotImplementedException();
      };

   }

}
