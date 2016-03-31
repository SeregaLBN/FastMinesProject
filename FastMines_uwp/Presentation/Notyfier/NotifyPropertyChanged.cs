using System;
using System.Linq;
using System.Collections.Generic;
using System.ComponentModel;
using System.Runtime.CompilerServices;

namespace FastMines.Presentation.Notyfier {

   /// <summary> Implementation of <see cref="INotifyPropertyChanged"/> to simplify models. </summary>
   [Windows.Foundation.Metadata.WebHostHidden]
   public abstract class NotifyPropertyChanged : INotifyPropertyChanged {

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
         OnPropertyChanged(tmp, value, propertyName);
         return true;
      }

      /// <summary> Notifies listeners that a property value has changed. </summary>
      /// <param name="oldValue">old value</param>
      /// <param name="newValue">new value</param>
      /// <param name="propertyName">Name of the property used to notify listeners.  This value is optional and can be provided automatically
      /// when invoked from compilers that support <see cref="CallerMemberNameAttribute"/>.</param>
      protected void OnPropertyChanged<T>(T oldValue, T newValue, [CallerMemberName] string propertyName = null) {
         OnPropertyChanged(this, new PropertyChangedExEventArgs<T>(newValue, oldValue, propertyName));
      }

      protected void OnPropertyChanged([CallerMemberName] string propertyName = null) {
         OnPropertyChanged(this, new PropertyChangedEventArgs(propertyName));
      }

      protected virtual void OnPropertyChanged(object sender, PropertyChangedEventArgs ev) {
         if (DeferredNoticeOn)
            _deferredPropertyChanged[ev.PropertyName] = ev;
         else
            PropertyChangedReal(sender, ev);
      }

      // not virtual!
      private void PropertyChangedReal(object sender, PropertyChangedEventArgs ev) {
         var eventHandler = PropertyChanged;
         eventHandler?.Invoke(sender, ev);
      }

      #region Deferr notifications

      private Dictionary<string, PropertyChangedEventArgs> _deferredPropertyChanged;

      private bool _deferredNoticeOn;
      /// <summary> Deferr notifications </summary>
      protected bool DeferredNoticeOn {
         get { return _deferredNoticeOn; }
         set { SetDeferredNoticeOn(value, null); }
      }
      private void SetDeferredNoticeOn(bool value, Action onBeforeNotify) {
         if (_deferredNoticeOn == value)
            return;

         if (value && (_deferredPropertyChanged == null))
            _deferredPropertyChanged = new Dictionary<string, PropertyChangedEventArgs>();

         _deferredNoticeOn = value;
         if (_deferredNoticeOn)
            return;

         onBeforeNotify?.Invoke();

         if (_deferredPropertyChanged.Any()) {
            var tmpCopy = new Dictionary<string, PropertyChangedEventArgs>(_deferredPropertyChanged);
            _deferredPropertyChanged.Clear();
            foreach (var key in tmpCopy.Keys)
               PropertyChangedReal(this, tmpCopy[key]);
            tmpCopy.Clear();
         }
      }

      protected class DeferredNoticeClass : IDisposable {
         private readonly NotifyPropertyChanged _owner;
         private readonly bool _deferred;
         private readonly Action _onDisposed, _onBeforeNotify, _onAfterNotify;

         public DeferredNoticeClass(NotifyPropertyChanged owner, Action onDisposed = null, Action onBeforeNotify = null, Action onAfterNotify = null) {
            _owner = owner;
            _onDisposed = onDisposed;
            _onBeforeNotify = onBeforeNotify;
            _onAfterNotify = onAfterNotify;
            _deferred = !owner.DeferredNoticeOn;
            if (_deferred)
               _owner.DeferredNoticeOn = true;
         }

         public void Dispose() {
            _onDisposed?.Invoke();
            if (!_deferred)
               return;
            _owner.SetDeferredNoticeOn(false, _onBeforeNotify);
            _onAfterNotify?.Invoke();
         }
      }

      public IDisposable DeferredNotice(Action onDisposed = null, Action onBeforeNotify = null, Action onAfterNotify = null) {
         return new DeferredNoticeClass(this, onDisposed, onBeforeNotify, onAfterNotify);
      }

      #endregion

   }
}