using System;
using System.ComponentModel;
using System.Collections.Generic;
using System.Runtime.CompilerServices;
using fmg.common.ui;

namespace fmg.common.notyfier {

   /// <summary> Notifies owner clients that a owner property value has changed </summary>
#if WINDOWS_UWP
   [Windows.Foundation.Metadata.WebHostHidden]
#endif
   public sealed class NotifyPropertyChanged : /* INotifyPropertyChanged */ IDisposable {

      private readonly INotifyPropertyChanged _owner;
      private readonly Action<PropertyChangedEventArgs> _fireOwnerEvent;
      private readonly bool _deferredNotifications;
      private readonly IDictionary<string /* propertyName */, PropertyChangedEventArgs> _deferrNotifications = new Dictionary<string, PropertyChangedEventArgs>();
      private bool _disposed;

      public NotifyPropertyChanged(INotifyPropertyChanged owner, Action<PropertyChangedEventArgs> fireOwnerEvent)
         : this(owner, fireOwnerEvent, false)
      { }
      public NotifyPropertyChanged(INotifyPropertyChanged owner, Action<PropertyChangedEventArgs> fireOwnerEvent, bool deferredNotifications) {
         _owner = owner;
         _fireOwnerEvent = fireOwnerEvent;
         _deferredNotifications = deferredNotifications;
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
         OnPropertyChanged(tmp, value, propertyName);
         return true;
      }

      /// <summary> Notifies listeners that a property value has changed. </summary>
      /// <param name="oldValue">old value</param>
      /// <param name="newValue">new value</param>
      /// <param name="propertyName">Name of the property used to notify listeners.  This value is optional and can be provided automatically
      /// when invoked from compilers that support <see cref="CallerMemberNameAttribute"/>.</param>
      public void OnPropertyChanged<T>(T oldValue, T newValue, [CallerMemberName] string propertyName = null) {
         OnPropertyChanged(new PropertyChangedExEventArgs<T>(oldValue, newValue, propertyName));
      }

      /// <summary> Notifies listeners that a property value has changed. </summary>
      /// <param name="propertyName">Name of the property used to notify listeners.  This value is optional and can be provided automatically
      /// when invoked from compilers that support <see cref="CallerMemberNameAttribute"/>.</param>
      public void OnPropertyChanged([CallerMemberName] string propertyName = null) {
         OnPropertyChanged(new PropertyChangedEventArgs(propertyName));
      }

      public void OnPropertyChanged(PropertyChangedEventArgs ev) {
         if (_disposed)
            return;

         if (!_deferredNotifications) {
            _fireOwnerEvent(ev);
            //LoggerSimple.Put($"< OnPropertyChanged: {_owner.GetType().Name}: PropertyName={ev.PropertyName}");
         } else {
            bool shedule = !_deferrNotifications.ContainsKey(ev.PropertyName);
            _deferrNotifications[ev.PropertyName] = ev; // Re-save only the last event.
            if (shedule)
               Factory.DEFERR_INVOKER(() => {
                  if (_disposed)
                     return;
                  PropertyChangedEventArgs ev2 = _deferrNotifications[ev.PropertyName];
                  if ((ev2 == null) || !_deferrNotifications.Remove(ev.PropertyName))
                     //System.Diagnostics.Trace.TraceError("hmmm... invalid usage ;(");
                     System.Diagnostics.Debug.Assert(false, "hmmm... invalid usage ;(");
                  else
                     _fireOwnerEvent(ev2);
               });
            }
      }

      /// <summary> rethrow member event, notify parent class/container </summary>
      public void OnPropertyChanged<T>(PropertyChangedEventArgs from, [CallerMemberName] string propertyName = null) {
         if (from is PropertyChangedExEventArgs<T> evEx)
            OnPropertyChanged(new PropertyChangedExEventArgs<T>(evEx.OldValue, evEx.NewValue, propertyName));
         else
            OnPropertyChanged(new PropertyChangedEventArgs(propertyName));
      }

      public void Dispose() {
         if (_disposed)
            return;
         _disposed = true;
         _deferrNotifications.Clear();
         GC.SuppressFinalize(this);
      }

   }

}
