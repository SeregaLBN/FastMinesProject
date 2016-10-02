using System.ComponentModel;
using System.Runtime.CompilerServices;

namespace fmg.common.notyfier {

   /// <summary> Implementation of <see cref="INotifyPropertyChanged"/> to simplify models. </summary>
#if WINDOWS_UWP
   [Windows.Foundation.Metadata.WebHostHidden]
#endif
   public abstract class NotifyPropertyChanged : Disposable, INotifyPropertyChanged {

      /// <summary> Multicast event for property change notifications. </summary>
      public event PropertyChangedEventHandler PropertyChanged;

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
         OnSelfPropertyChanged(tmp, value, propertyName);
         return true;
      }

      /// <summary> Notifies listeners that a property value has changed. </summary>
      /// <param name="oldValue">old value</param>
      /// <param name="newValue">new value</param>
      /// <param name="propertyName">Name of the property used to notify listeners.  This value is optional and can be provided automatically
      /// when invoked from compilers that support <see cref="CallerMemberNameAttribute"/>.</param>
      protected void OnSelfPropertyChanged<T>(T oldValue, T newValue, [CallerMemberName] string propertyName = null) {
         OnSelfPropertyChanged(new PropertyChangedExEventArgs<T>(newValue, oldValue, propertyName));
      }

      protected void OnSelfPropertyChanged([CallerMemberName] string propertyName = null) {
         OnSelfPropertyChanged(new PropertyChangedEventArgs(propertyName));
      }

      protected virtual void OnSelfPropertyChanged(PropertyChangedEventArgs ev) {
         if (Disposed)
            return;
         var eventHandler = PropertyChanged;
         eventHandler?.Invoke(this, ev);
         //LoggerSimple.Put($"< OnSelfPropertyChanged: {GetType().Name}: PropertyName={ev.PropertyName}");
      }

      /// <summary> rethrow another/my event - notify parent class/container </summary>
      protected void OnSelfPropertyChanged<T>(PropertyChangedEventArgs from, string propertyName) {
         var ev = from as PropertyChangedExEventArgs<T>;
         if (ev == null)
            OnSelfPropertyChanged(propertyName);
         else
            OnSelfPropertyChanged(new PropertyChangedExEventArgs<T>(ev.NewValue, ev.OldValue, propertyName));
      }

   }

}
