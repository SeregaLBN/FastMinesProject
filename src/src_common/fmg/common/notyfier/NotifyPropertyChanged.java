package fmg.common.notyfier;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import fmg.common.Pair;

public abstract class NotifyPropertyChanged // implements INotifyPropertyChanged
{
   private PropertyChangeSupport propertyChanges = new PropertyChangeSupport(this);
   public void addListener(PropertyChangeListener l) { propertyChanges.addPropertyChangeListener(l); }
   public void removePropertyChangeListener(PropertyChangeListener l) { propertyChanges.removePropertyChangeListener(l); }

   @Deprecated // used reflection :(
   protected <T> boolean setProperty(T newValue, String propertyName) {
      Object oldValue;
      try {
         Field fld = findField(propertyName);
         fld.setAccessible(true);
         oldValue = fld.get(this);
         if ((oldValue == null && newValue==null) || oldValue.equals(newValue))
            return false;
         fld.set(this, newValue);
      } catch (Exception ex) {
         throw new RuntimeException(ex);
      }

      onPropertyChanged(oldValue, newValue, propertyName);
      return true;
   }

   protected void onPropertyChanged(int oldValue, int newValue, String propertyName) {
      onPropertyChanged(Integer.valueOf(oldValue), Integer.valueOf(newValue), propertyName);
   }

   protected void onPropertyChanged(boolean oldValue, boolean newValue, String propertyName) {
      onPropertyChanged(Boolean.valueOf(oldValue), Boolean.valueOf(newValue), propertyName);
   }

   protected void onPropertyChanged(String propertyName) {
      onPropertyChanged(null, null, propertyName);
   }

   protected void onPropertyChanged(Object oldValue, Object newValue, String propertyName) {
      //propertyChanges.firePropertyChange(propertyName, oldValue, newValue);
      if (getDeferredOn())
         _deferredPropertyChanged.put(propertyName, new Pair<Object, Object>(oldValue, newValue));
      else
         EventHandlerInvoke(oldValue, newValue, propertyName);
   }

   // not virtual!
   private final void EventHandlerInvoke(Object oldValue, Object newValue, String propertyName) {
      propertyChanges.firePropertyChange(propertyName, oldValue, newValue);
   }


   private Field findField(String propertyName) {
      return getPlainFields(this)
         .filter(fld -> fld.getName().equalsIgnoreCase(propertyName) ||
                        fld.getName().equalsIgnoreCase("_" + propertyName))
         .findAny()
         .orElseThrow(()-> new RuntimeException("Property '" + propertyName + "' not found"));
   }

   private static <T> Stream<Field> getDeclaredFields(T obj) {
      Class<?> clazz = obj.getClass();
      List<Stream<Field>> arr = new ArrayList<>();
      do {
          arr.add(Arrays.stream(clazz.getDeclaredFields()));
          clazz = clazz.getSuperclass();
      } while (clazz != null);
      return arr.stream().flatMap(x -> x);
   }

   private static <T> Stream<Field> getPlainFields(T obj) {
      return getDeclaredFields(obj)
              .filter(fld -> {
                  int m = fld.getModifiers();
                  return !Modifier.isTransient(m) &&
                         !Modifier.isStatic(m) &&
                         !Modifier.isFinal(m); });
   }


   //////////////////////////////////////////////////////////////////////////////////////////////////////////////
   // #region Deferr notifications
   //

   private Map<String, Pair<Object, Object>> _deferredPropertyChanged;

   private boolean _deferredOn;
   /** Deferr notifications */
   protected boolean getDeferredOn() { return _deferredOn; }
   protected void setDeferredOn(boolean value) { 
      if (_deferredOn == value)
         return;

      if (value && (_deferredPropertyChanged == null))
         _deferredPropertyChanged = new HashMap<>();

      _deferredOn = value;
      if (_deferredOn)
         return;

      DeferredSwitchOff();

      if (!_deferredPropertyChanged.isEmpty()) {
         Map<String, Pair<Object, Object>> tmpCopy = new HashMap<>(_deferredPropertyChanged);
         _deferredPropertyChanged.clear();
         tmpCopy.forEach((name, old_new) -> EventHandlerInvoke(old_new.getFirst(), old_new.getSecond(), name));
         tmpCopy.clear();
      }
   }

   protected void DeferredSwitchOff() { }

   protected class DeferredNotice implements AutoCloseable {
      private final boolean _locked;
      private final Runnable _onDisposed,  _onBeforeNotify,  _onAfterNotify;

      public DeferredNotice() { this(null, null, null); }
      public DeferredNotice(Runnable onDisposed,  Runnable onBeforeNotify,  Runnable onAfterNotify) {
         _onDisposed = onDisposed;
         _onBeforeNotify = onBeforeNotify;
         _onAfterNotify = onAfterNotify;
         if (NotifyPropertyChanged.this.getDeferredOn()) {
            _locked = false;
            return;
         }
         NotifyPropertyChanged.this.setDeferredOn(true);
         _locked = true;
      }

      @Override
      public void close() {
         if (_onDisposed != null)
            _onDisposed.run();
         if (!_locked)
            return;
         if (_onBeforeNotify != null)
            _onBeforeNotify.run();
         NotifyPropertyChanged.this.setDeferredOn(false);
         if (_onAfterNotify != null)
            _onAfterNotify.run();
      }
   }
   
   public AutoCloseable getDeferredNotice() { return new DeferredNotice(); }
   public AutoCloseable getDeferredNotice(Runnable onDisposed,  Runnable onBeforeNotify,  Runnable onAfterNotify) {
      return new DeferredNotice(onDisposed, onBeforeNotify,  onAfterNotify);
   }

   //
   // #endregion
   //////////////////////////////////////////////////////////////////////////////////////////////////////////////

}
