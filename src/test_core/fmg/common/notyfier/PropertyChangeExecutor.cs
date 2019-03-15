using System;
using System.Collections.Generic;
using System.ComponentModel;
using System.Reactive.Linq;
using System.Threading.Tasks;
using fmg.common.ui;
using fmg.common.notifier;

namespace fmg.common.notyfier {

    /// <summary> Simple UnitTest wrapper for testing {@link INotifyPropertyChanged} objects </summary>
    /// <typeparam name="T">the tested object</typeparam>
    public class PropertyChangeExecutor<T>
        where T : INotifyPropertyChanged
    {
        private readonly T data;
        private readonly IDictionary<string /* property name */, int /* count of modifies */> modifiedProperties = new Dictionary<string, int>();

        public PropertyChangeExecutor(T data) {
            this.data = data;
        }

        /// <summary> unit test executor </summary>
        /// <param name="notificationsTimeoutMs">timeout call validator if you do not receive a notification</param>
        /// <param name="maxWaitTimeoutMs">maximum timeout to wait for all notifications</param>
        /// <param name="modificator">data modifier (executable in UI thread)</param>
        /// <param name="validator">data validator (executable in current thread)</param>
        /// <returns></returns>
        public async Task Run(
            TimeSpan notificationsTimeout/* = 100ms*/,
            TimeSpan maxWaitTimeout/* = 1000ms*/,
            Action modificator,
            Action<IDictionary<string /* property name */, int /* count of modifies */>> validator)
        {
            using (var signal = new Signal())
            {
                var ob = Observable
                    .FromEventPattern<PropertyChangedEventHandler, PropertyChangedEventArgs>(h => data.PropertyChanged += h, h => data.PropertyChanged -= h);
                using (ob.Subscribe(
                    ev => {
                        string name = ev.EventArgs.PropertyName;
                        LoggerSimple.Put("PropertyChangeExecutor::OnDataPropertyChanged: ev=" + ev.EventArgs);
                        if (modifiedProperties.ContainsKey(name))
                            modifiedProperties[name] = 1 + modifiedProperties[name];
                        else
                            modifiedProperties.Add(name, 1);
                    }))
                {
                    using (ob.Timeout(notificationsTimeout)
                        .Subscribe(ev => {
                            LoggerSimple.Put("OnNext: ev={0}", ev);
                        }, ex => {
                            //LoggerSimple.Put("OnError: " + ex);
                            LoggerSimple.Put("timeout after " + notificationsTimeout.Milliseconds + "ms.");
                            signal.Set();
                        }))
                    {
                        Factory.DEFERR_INVOKER(modificator);
                        if (!await signal.Wait(maxWaitTimeout))
                            throw new Exception("Wait timeout " + maxWaitTimeout.Milliseconds + "ms.");
                        validator(modifiedProperties);
                    }
                }
            }
        }

    }

}
