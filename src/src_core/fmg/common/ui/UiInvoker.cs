using System;
using Fmg.Core.Img;

namespace Fmg.Common.UI {

    /// <summary> Factory of UI timers/animators/deferred invokers </summary>
    public static class UiInvoker {

        /// <summary> Delayed execution in the thread of the user interface. </summary>
        public static Action<Action> Deferred = doRun => {
            throw new NotImplementedException();
            //System.Diagnostics.Debug.WriteLine("need redefine!");
            //doRun();
        };

        /// <summary> Platform-dependent factory of <see cref="IAnimator"/>. Set from outside... </summary>
        public static Func<IAnimator> Animator = () => {
            throw new NotImplementedException();
        };

        /// <summary> Platform-dependent factory of <see cref="ITimer"/>. Set from outside... </summary>
        public static Func<ITimer> TimerCreator = () => {
            throw new NotImplementedException();
        };

    }

}
