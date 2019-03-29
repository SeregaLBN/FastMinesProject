using System;
using System.Collections.Generic;
using System.ComponentModel;
using System.Runtime.CompilerServices;
using fmg.common.ui;

namespace fmg.common.notifier {

    /// <summary> Notifies owner clients that a owner property value has changed </summary>
#if WINDOWS_UWP
    [Windows.Foundation.Metadata.WebHostHidden]
#endif
    public sealed class NotifyPropertyChanged : IDisposable //, INotifyPropertyChanged
    {
        private readonly INotifyPropertyChanged _owner;
        private readonly Action<PropertyChangedEventArgs> _fireOwnerEvent;
        public bool DeferredNotifications { get; set; }
        private readonly IDictionary<string /* propertyName */, PropertyChangedEventArgs> _deferrNotifications = new Dictionary<string, PropertyChangedEventArgs>();
        private bool _disposed;

        public NotifyPropertyChanged(INotifyPropertyChanged owner, Action<PropertyChangedEventArgs> fireOwnerEvent, bool deferredNotifications = false) {
            _owner = owner;
            _fireOwnerEvent = fireOwnerEvent;
            DeferredNotifications = deferredNotifications;
        }


        /// <summary> Checks if a property already matches a desired value.  Sets the property and notifies listeners only when necessary. </summary>
        /// <typeparam name="T">Type of the property.</typeparam>
        /// <param name="storage">Reference to a property with both getter and setter.</param>
        /// <param name="value">Desired value for the property.</param>
        /// <param name="propertyName">Name of the property used to notify listeners.  This value is optional and can be provided automatically
        /// when invoked from compilers that support CallerMemberName.</param>
        /// <returns>True if the value was changed, false if the existing value matched the desired value.</returns>
        public bool SetProperty<T>(ref T storage, T value, [CallerMemberName] string propertyName = null) {
            if (_disposed) {
                if (value != null) {
                    System.Diagnostics.Debug.WriteLine("Illegal call property " + _owner.GetType().FullName + "." + propertyName + ": object already disposed!");
                    return false;
                }
            }

            if (object.Equals(storage, value)) return false;

            var tmp = storage;
            storage = value;
            FirePropertyChanged(tmp, value, propertyName);
            return true;
        }

        /// <summary> Notifies listeners that a property value has changed. </summary>
        /// <param name="oldValue">old value</param>
        /// <param name="newValue">new value</param>
        /// <param name="propertyName">Name of the property used to notify listeners.  This value is optional and can be provided automatically
        /// when invoked from compilers that support <see cref="CallerMemberNameAttribute"/>.</param>
        public void FirePropertyChanged<T>(T oldValue, T newValue, [CallerMemberName] string propertyName = null) {
            FirePropertyChanged(new PropertyChangedExEventArgs<T>(oldValue, newValue, propertyName));
        }

        /// <summary> Notifies listeners that a property value has changed. </summary>
        /// <param name="propertyName">Name of the property used to notify listeners.  This value is optional and can be provided automatically
        /// when invoked from compilers that support <see cref="CallerMemberNameAttribute"/>.</param>
        public void FirePropertyChanged([CallerMemberName] string propertyName = null) {
            FirePropertyChanged(new PropertyChangedEventArgs(propertyName));
        }

        public void FirePropertyChanged(PropertyChangedEventArgs ev) {
            if (_disposed)
                return;

            void fireOwnerEvent(PropertyChangedEventArgs ev3) {
                //if (ev3.PropertyName == "Image")
                //   LoggerSimple.Put("  Fire event '" + ev3.PropertyName + "'! class " + _owner.GetType().FullName);
                _fireOwnerEvent(ev3);
            }
            if (!DeferredNotifications) {
                fireOwnerEvent(ev);
                //LoggerSimple.Put($"< FirePropertyChanged: {_owner.GetType().Name}: PropertyName={ev.PropertyName}");
            } else {
                bool shedule;
                {
                    PropertyChangedEventArgs oldEvent;
                    PropertyChangedEventArgs newEvent;
                    bool isFirstEvent = !_deferrNotifications.TryGetValue(ev.PropertyName, out oldEvent);
                    if (isFirstEvent) {
                        newEvent = ev;
                    } else {
                        if (ev is IPropertyValuesComparable evExt) {
                            newEvent = evExt.CombineValues(oldEvent);
                            if (newEvent == null) {
                                newEvent = ev;
                            } else {
                                if ((newEvent as IPropertyValuesComparable).IsValuesEqual()) {
                                    // Nothing to fire. First event OldValue and last event NewValue is same objects.
                                    _deferrNotifications.Remove(ev.PropertyName); // HINT_1
                                    return;
                                }
                            }
                        } else {
                            newEvent = ev;
                        }
                    }
                    shedule = isFirstEvent;
                    _deferrNotifications[ev.PropertyName] = newEvent; // Re-save only the last event.
                }
                if (shedule)
                    UiInvoker.Deferred(() => {
                        if (_disposed)
                            return;
                        PropertyChangedEventArgs ev2;
                        if (!_deferrNotifications.TryGetValue(ev.PropertyName, out ev2))
                            return; // event already deleted (see HINT_1)
                        if ((ev2 == null) || !_deferrNotifications.Remove(ev.PropertyName))
                            //System.Diagnostics.Trace.TraceError("hmmm... invalid usage ;(");
                            System.Diagnostics.Debug.Assert(false, "hmmm... invalid usage ;(");
                        else
                            fireOwnerEvent(ev2);
                    });
            }
        }

        /// <summary> rethrow member event, notify parent class/container </summary>
        public void FirePropertyChanged<T>(PropertyChangedEventArgs from, [CallerMemberName] string propertyName = null) {
            if (from is PropertyChangedExEventArgs<T> evEx)
                FirePropertyChanged(new PropertyChangedExEventArgs<T>(evEx.OldValue, evEx.NewValue, propertyName));
            else
                FirePropertyChanged(new PropertyChangedEventArgs(propertyName));
        }

        public bool Disposed => _disposed;

        public void Dispose() {
            if (_disposed)
                return;
            _disposed = true;
            _deferrNotifications.Clear();
            GC.SuppressFinalize(this);
        }

    }

}
