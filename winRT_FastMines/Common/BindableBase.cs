using System;
using System.ComponentModel;
using System.Runtime.CompilerServices;

namespace FastMines.Common {
   // TODO http://stackoverflow.com/questions/7677854/notifypropertychanged-event-where-event-args-contain-the-old-value

   public interface INotifyPropertyChangedEx : INotifyPropertyChanged {
      event PropertyChangedExEventHandler PropertyChangedEx;
   }

   public delegate void PropertyChangedExEventHandler(object sender, PropertyChangedExEventArgs e);

   public class PropertyChangedExEventArgs : PropertyChangedEventArgs {
      public object OldValue { get; private set; }
      public object NewValue { get; private set; }

      public PropertyChangedExEventArgs(string propertyName, object oldValue, object newValue)
         : base(propertyName) {
         OldValue = oldValue;
         NewValue = newValue;
      }
   }

   /// <summary>
   /// Implementation of <see cref="INotifyPropertyChanged"/> to simplify models.
   /// </summary>
   [Windows.Foundation.Metadata.WebHostHidden]
   public abstract class BindableBase : INotifyPropertyChangedEx {

      /// <summary>
      /// Multicast event for property change notifications.
      /// </summary>
      public event PropertyChangedEventHandler PropertyChanged;
      public event PropertyChangedExEventHandler PropertyChangedEx;

      /// <summary>
      /// Checks if a property already matches a desired value.  Sets the property and
      /// notifies listeners only when necessary.
      /// </summary>
      /// <typeparam name="T">Type of the property.</typeparam>
      /// <param name="storage">Reference to a property with both getter and setter.</param>
      /// <param name="value">Desired value for the property.</param>
      /// <param name="propertyName">Name of the property used to notify listeners.  This
      /// value is optional and can be provided automatically when invoked from compilers that
      /// support CallerMemberName.</param>
      /// <returns>True if the value was changed, false if the existing value matched the
      /// desired value.</returns>
      protected bool SetProperty<T>(ref T storage, T value, [CallerMemberName] String propertyName = null) {
         if (object.Equals(storage, value)) return false;

         var tmp = storage;
         storage = value;
         this.OnPropertyChanged(tmp, value, propertyName);
         return true;
      }

      /// <summary>
      /// Notifies listeners that a property value has changed.
      /// </summary>
      /// <param name="oldValue">old value</param>
      /// <param name="newValue">new value</param>
      /// <param name="propertyName">Name of the property used to notify listeners.  This
      /// value is optional and can be provided automatically when invoked from compilers
      /// that support <see cref="CallerMemberNameAttribute"/>.</param>
      protected void OnPropertyChanged<T>(T oldValue, T newValue, [CallerMemberName] string propertyName = null) {
         var eventHandlerEx = this.PropertyChanged;
         if (eventHandlerEx != null)
            eventHandlerEx(this, new PropertyChangedExEventArgs(propertyName, oldValue, newValue));
         var eventHandler = this.PropertyChanged;
         if (eventHandler != null)
            eventHandler(this, new PropertyChangedEventArgs(propertyName));
      }
   }
}