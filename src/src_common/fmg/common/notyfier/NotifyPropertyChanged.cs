using System;
using System.ComponentModel;
using System.Collections.Generic;
using System.Runtime.CompilerServices;

namespace fmg.common.notyfier {

   /// <summary> Implementation of <see cref="INotifyPropertyChanged"/> to simplify models. </summary>
#if WINDOWS_UWP
   [Windows.Foundation.Metadata.WebHostHidden]
#endif
   public class NotifyPropertyChanged : INotifyPropertyChanged, IDisposable {

      public static Action<Action> DEFERR_INVOKER = doRun => {
         System.Diagnostics.Debug.WriteLine("need redefine!");
         doRun();
      };

      protected bool Disposed { get; private set; }

      private readonly INotifyPropertyChanged _owner;
      /// <summary> Multicast event for property change notifications. </summary>
      public event PropertyChangedEventHandler PropertyChanged;
      private bool _deferredNotifications = false;
      private readonly IDictionary<string /* propertyName */, PropertyChangedEventArgs> _deferrNotifications = new Dictionary<string, PropertyChangedEventArgs>();

      public NotifyPropertyChanged() { _owner = this; }
      public NotifyPropertyChanged(INotifyPropertyChanged owner) { _owner = owner; }
      public NotifyPropertyChanged(INotifyPropertyChanged owner, bool deferredNotifications) : this(owner) { _deferredNotifications = deferredNotifications; }

      /// <summary> Checks if a property already matches a desired value.  Sets the property and notifies listeners only when necessary. </summary>
      /// <typeparam name="T">Type of the property.</typeparam>
      /// <param name="storage">Reference to a property with both getter and setter.</param>
      /// <param name="value">Desired value for the property.</param>
      /// <param name="propertyName">Name of the property used to notify listeners.  This value is optional and can be provided automatically
      /// when invoked from compilers that support CallerMemberName.</param>
      /// <returns>True if the value was changed, false if the existing value matched the desired value.</returns>
      protected bool SetProperty<T>(ref T storage, T value, [CallerMemberName] string propertyName = null) {
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

      protected virtual void OnPropertyChanged(PropertyChangedEventArgs ev) {
         if (Disposed)
            return;

         if (!_deferredNotifications) {
            PropertyChanged?.Invoke(_owner, ev);
            //LoggerSimple.Put($"< OnPropertyChanged: {GetType().Name}: PropertyName={ev.PropertyName}");
         } else {
            bool shedule = _deferrNotifications.ContainsKey(ev.PropertyName);
            _deferrNotifications[ev.PropertyName] = ev;
            if (shedule)
               DEFERR_INVOKER(() => {
                  if (Disposed)
                     return;
                  PropertyChangedEventArgs ev2 = _deferrNotifications[ev.PropertyName];
                  if (_deferrNotifications.Remove(ev.PropertyName))
                     //System.Diagnostics.Trace.TraceError("hmmm... invalid usage ;(");
                     System.Diagnostics.Debug.Assert(false, "hmmm... invalid usage ;(");
                  else
                     PropertyChanged?.Invoke(_owner, ev2);
               });
         }
      }

      /// <summary> rethrow member event, notify parent class/container </summary>
      public void OnPropertyChanged<T>(PropertyChangedEventArgs from, [CallerMemberName] string propertyName = null) {
         if (!(from is PropertyChangedExEventArgs<T> evEx))
            OnPropertyChanged(new PropertyChangedEventArgs(propertyName));
         else
            OnPropertyChanged(new PropertyChangedExEventArgs<T>(evEx.OldValue, evEx.NewValue, propertyName));
      }

      protected virtual void Dispose(bool disposing) {
         if (Disposed)
            return;

         if (disposing) {
            // Dispose managed resources
            _deferrNotifications.Clear();
         }

         // Dispose unmanaged resources

         Disposed = true;
      }

      public void Dispose() {
         Dispose(true);
         GC.SuppressFinalize(this);
      }

      ~NotifyPropertyChanged() {
         Dispose(false);
      }
   }

}
