using System;
using System.Linq;
using System.Collections.Generic;
using System.ComponentModel;
using System.Reactive.Subjects;
using System.Reactive.Linq;
using System.Threading.Tasks;
using fmg.common.ui;

namespace fmg.common.notifier {

    /// <summary> Simple UnitTest wrapper for testing {@link INotifyPropertyChanged} objects </summary>
    /// <typeparam name="T">the tested object</typeparam>
    public class PropertyChangeExecutor<T>
        where T : class, INotifyPropertyChanged, IDisposable
    {
        private readonly bool _needClose;
        private readonly Func<T> _dataCreator;
        private readonly IDictionary<string /* property name */, int /* count of modifies */> _modifiedProperties = new Dictionary<string, int>();

        /// <param name="dataCreator">data factory (called from UI thread)</param>
        /// <param name="needClose">need call IDisposable.Dispose() for dataCreator result</param>
        public PropertyChangeExecutor(Func<T> dataCreator, bool needClose = true) {
            this._needClose = needClose;
            this._dataCreator = dataCreator;
        }

        /// <summary> unit test executor </summary>
        /// <param name="notificationsTimeoutMs">timeout call validator if you do not receive a notification</param>
        /// <param name="maxWaitTimeoutMs">maximum timeout to wait for all notifications</param>
        /// <param name="modificator">data modifier (executable in UI thread)</param>
        /// <param name="validator">data validator (executable in current thread)</param>
        /// <returns></returns>
        public async Task Run(
            uint notificationsTimeoutMs/* = 100ms*/,
            uint maxWaitTimeoutMs/* = 1000ms*/,
            Action<T> modificator,
            Action<T, IDictionary<string /* property name */, int /* count of modifies */>> validator)
        {
            var subject = new Subject<PropertyChangedEventArgs>();
            void onDataPropertyChanged(object sender, PropertyChangedEventArgs ev) {
                var name = ev.PropertyName;
                LoggerSimple.Put("PropertyChangeExecutor.onDataPropertyChanged: ev.name=" + name);
                int oldValue;
                if (_modifiedProperties.TryGetValue(name, out oldValue))
                    _modifiedProperties[name] = 1 + oldValue;
                else
                    _modifiedProperties.Add(name, 1);
                subject.OnNext(ev);
            }

            T data = null;
            Exception ex1 = null;
            try {
                using (var signal = new Signal()) {
                    using (var dis = subject
                        .Timeout(TimeSpan.FromMilliseconds(notificationsTimeoutMs))
                        .Subscribe(ev => {
                            LoggerSimple.Put("OnNext: ev={0}", ev);
                        }, ex => {
                            //LoggerSimple.Put("OnError: " + ex);
                            LoggerSimple.Put("Timeout after " + notificationsTimeoutMs + "ms.");
                            signal.Set();
                        }))
                    {
                        UiInvoker.Deferred(() => {
                            if (ex1 != null)
                                return;
                            try {
                                data = _dataCreator(); // 1. Construct in UI thread!
                                data.PropertyChanged += onDataPropertyChanged;
                                modificator(data);
                            } catch(Exception ex) {
                                ex1 = ex;
                            }
                        });
                        if (!await signal.Wait(TimeSpan.FromMilliseconds(maxWaitTimeoutMs))) {
                            ex1 = new Exception("Wait timeout " + maxWaitTimeoutMs + "ms.");
                        } else {
                            if (ex1 == null) {
                                LoggerSimple.Put("  checking... {0}=[{1}]", nameof(_modifiedProperties), String.Join(",", _modifiedProperties.Select(kv => kv.Key+":"+kv.Value)));
                                try {
                                    validator(data, _modifiedProperties);
                                } catch(Exception ex) {
                                    ex1 = ex;
                                }
                            }
                        }
                    }
                }
            } finally {
                if (data != null) {
                    using (var signal = new Signal()) {
                        UiInvoker.Deferred(() => {
                            try {
                                data.PropertyChanged -= onDataPropertyChanged;
                                if (_needClose)
                                    data.Dispose(); // 2. Destruct in UI thread!
                                signal.Set();
                            } catch(Exception ex) {
                                if (ex1 == null)
                                    ex1 = ex;
                                else
                                    System.Diagnostics.Debug.Write(ex.ToString());
                            }
                        });
                        if (!await signal.Wait(TimeSpan.FromMilliseconds(maxWaitTimeoutMs))) {
                            var errMsg = "Wait free timeout " + maxWaitTimeoutMs + "ms.";
                            if (ex1 == null) {
                                ex1 = new Exception(errMsg);
                            } else {
                                System.Diagnostics.Debug.Write(errMsg);
                            }
                        }
                    }
                }
            }

            if (ex1 != null) {
                System.Diagnostics.Debug.Write(ex1.ToString());
                throw ex1;
            }
        }

    }

}
