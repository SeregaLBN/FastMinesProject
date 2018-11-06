using System;
using System.Threading.Tasks;

namespace fmg.common {

    public static class TaskExec {

        //public static Task InNewTask(Action action, bool longRunning = false) {
        //    return Task.Factory.StartNew(action, longRunning ? TaskCreationOptions.LongRunning : TaskCreationOptions.None);
        //}

        public static async void DelayedStart(TimeSpan interval, Action action) {
            await Task.Delay(interval).ContinueWith(_ => action());
        }

        public static void DelayedStartSync(TimeSpan interval, Action action) {
            Task.Run(async () => {
                await Task.Delay(interval);
                action();
            });
        }

    }

}
